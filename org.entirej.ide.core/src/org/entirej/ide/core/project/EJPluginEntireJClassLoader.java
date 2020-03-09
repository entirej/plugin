/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.core.project;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.entirej.ide.core.EJCoreLog;

public class EJPluginEntireJClassLoader
{

    private static final WeakHashMap<IJavaProject, ClassLoader> WEAK_LOADERS = new WeakHashMap<IJavaProject, ClassLoader>();
    static
    {
        ResourcesPlugin.getWorkspace().addResourceChangeListener(new IResourceChangeListener()
        {
            final IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor()
                                                     {

                                                         public boolean visit(IResourceDelta delta) throws CoreException
                                                         {
                                                             switch (delta.getKind())
                                                             {
                                                                 case IResourceDelta.REPLACED:
                                                                 case IResourceDelta.CHANGED:
                                                                     IResource resource = delta.getResource();
                                                                     if (resource instanceof IFile)
                                                                     {
                                                                         IFile cfile = (IFile) resource;

                                                                         if (cfile.getName().equals(".classpath") && cfile.getProject() != null)
                                                                         {
                                                                             IJavaProject project = JavaCore.create(cfile.getProject());
                                                                             WEAK_LOADERS.remove(project);
                                                                             return false;
                                                                         }
                                                                     }
                                                                     break;

                                                             }

                                                             return true;
                                                         }
                                                     };

            public void resourceChanged(IResourceChangeEvent event)
            {
                try
                {
                    event.getDelta().accept(deltaVisitor);
                }
                catch (CoreException e)
                {
                    EJCoreLog.log(e);
                }

            }
        }, IResourceChangeEvent.POST_CHANGE);
    }

    private EJPluginEntireJClassLoader()
    {
    }

    private static class OrderedUniqueList extends ArrayList<URL>
    {
        private static final long serialVersionUID = 1L;

        public boolean add(URL element)
        {
            if (contains(element))
            {
                return false;
            }
            return super.add(element);
        }
    };

    public static void reload(final IJavaProject javaProject)
    {
        WEAK_LOADERS.remove(javaProject);
        getClassloader(javaProject);
    }

    public static Class<?> loadClass(final IJavaProject javaProject, String className) throws ClassNotFoundException
    {
       // EJCoreLog.logInfoMessage("loadClass - > "+className);
        ClassLoader classLoader = WEAK_LOADERS.get(javaProject);

        if (classLoader == null)
        {
            classLoader = getClassloader(javaProject);
            WEAK_LOADERS.put(javaProject, classLoader);
            EJCoreLog.logInfoMessage("load classLoader for  - > "+javaProject.hashCode()+":"+className +": "+String.valueOf(classLoader!=null));
        }

        try
        {
            Class<?> loadClass = classLoader.loadClass(className);
            if (loadClass != null)
                return loadClass;
        }
        catch (Throwable e)
        {
            // ignore
            //EJCoreLog.log(e);
        }

        // get output path level call loading

        try
        {
            // javaProject.getResolvedClasspath(true);//make sure project in
            // build
            classLoader = new OutputClassLoader(javaProject.getOutputLocation(), classLoader);
        }
        catch (JavaModelException e)
        {// ignore
            EJCoreLog.log(e);
        }
        
       // EJCoreLog.logInfoMessage("end loadClass - > "+className +": "+String.valueOf(classLoader!=null));
        return classLoader.loadClass(className);

    }

    private static URLClassLoader getBuildPathClassloader(IJavaProject javaProject, ClassLoader parent)
    {
        Collection<URL> classpathEntries = getClasspathEntries(javaProject, true);

        URLClassLoader classLoader = new URLClassLoader(classpathEntries.toArray(new URL[classpathEntries.size()]), parent)
        {

        };

        return classLoader;
    }

    private static URLClassLoader getClassloader(IJavaProject javaProject)
    {
        return getBuildPathClassloader(javaProject, EJPluginEntireJClassLoader.class.getClassLoader());
    }

    private static Collection<URL> getClasspathEntries(IJavaProject javaProject, boolean ignoreSource)
    {
        if (javaProject == null)
        {
            throw new NullPointerException("Trying to get hte projects class path, but the project passed was null");
        }
        try
        {
            if (!javaProject.exists())
            {
                return new ArrayList<URL>();
            }
            // IClasspathEntry[] rawClasspath = javaProject.getRawClasspath();

            IClasspathEntry[] entries = javaProject.getResolvedClasspath(true);

            List<URL> urlList = toURLS(javaProject, entries, ignoreSource);
            
            //add Project output path
            IPath outputLocation =null;
                try
                {
                    outputLocation = javaProject.getOutputLocation();

                    IPath path = javaProject.getProject().getLocation();

                    outputLocation = path.append(outputLocation.removeFirstSegments(1));
                }
                catch (JavaModelException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            
            if (outputLocation != null)
            {
                URL url = new URL("file", null, outputLocation.toString() + "/");
                urlList.add(url);
            }
            return urlList;
        }
        catch (JavaModelException e)
        {
            e.printStackTrace();
        }
        catch (MalformedURLException e)
        {
            e.printStackTrace();
        }
        return null;
    }

    private static List<URL> toURLS(IJavaProject javaProject, IClasspathEntry[] entries, boolean ignoreSource) throws MalformedURLException
    {
        // URL entries should be stored in an ordered list with no
        // duplicates
        List<URL> urlList = new OrderedUniqueList();

        for (int i = 0; i < entries.length; i++)
        {
            IClasspathEntry entry = entries[i];
            processEntry(javaProject, urlList, entry, ignoreSource);
        }
        return urlList;
    }

    private static void processEntry(IJavaProject javaProject, List<URL> urlList, IClasspathEntry entry, boolean ignoreSource) throws MalformedURLException
    {
        // This source output ... always included & exported
        if (!ignoreSource && entry.getEntryKind() == IClasspathEntry.CPE_SOURCE)
        {
            IPath outputLocation = entry.getOutputLocation();

            
            if (outputLocation != null)
            {
                URL url = new URL("file", null, outputLocation.toString() + "/");
                urlList.add(url);
            }
        }

        // Referenced project classpath. If this project is exported,
        // Then all *exported* entries are exported with respect to this
        // project,
        else if (entry.getEntryKind() == IClasspathEntry.CPE_PROJECT)
        {
            IProject ijproject = ResourcesPlugin.getWorkspace().getRoot().getProject(entry.getPath().segment(0));
            IJavaProject ref = JavaCore.create(ijproject);
            Collection<URL> cpEntries = getClasspathEntries(ref, false);
            urlList.addAll(cpEntries);
        }

        // This is the Directories classpath
        else if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER)
        {
            IPath entryPath = entry.getPath();
            URL url = new URL("file", null, entryPath.toString());
            IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(entryPath);

            if (res != null && res.exists())
            {
                url = new URL("file", null, res.getLocation().toString());
            }
            urlList.add(url);
        }
        // This is Library classpath
        else if (entry.getEntryKind() == IClasspathEntry.CPE_LIBRARY)
        {
            IPath entryPath = entry.getPath();
            URL url = new URL("file", null, entryPath.toString());

            IResource res = ResourcesPlugin.getWorkspace().getRoot().findMember(entryPath);
            if (res != null && res.exists())
            {
                url = new URL("file", null, res.getLocation().toString());
            }
            urlList.add(url);
        }
        // This is Variables classpath
        else if (entry.getEntryKind() == IClasspathEntry.CPE_VARIABLE)
        {
            String variableName = entry.getPath().segment(0);
            IPath variablePath = JavaCore.getClasspathVariable(variableName);
            if (variablePath != null)
            {
                URL url = new URL("file", null, variablePath.toString());
                urlList.add(url);
            }
        }
    }

    public static class OutputClassLoader extends ClassLoader
    {
        private IContainer            outputPath;
        private Map<String, Class<?>> classes = new HashMap<String, Class<?>>();

        public OutputClassLoader(IPath outputPath, ClassLoader loader)
        {
            super(loader);
            this.outputPath = (IContainer) ResourcesPlugin.getWorkspace().getRoot().findMember(outputPath);
        }

        public Class<?> loadClass(String className) throws ClassNotFoundException
        {
            Class<?> findClass = findOutClass(className, false);
            if (findClass != null)
                return findClass;
            return super.loadClass(className);
        }

        public Class<?> findOutClass(String className, boolean useCache)
        {
            byte classByte[];
            Class<?> result = null;

            if (useCache)
            {
                result = classes.get(className); // checks in cached classes
                if (result != null)
                {
                    return result;
                }
            }

            try
            {
                String path = className.replace('.', '/').concat(".class");
                IFile file = outputPath.getFile(Path.fromPortableString(path));
                if (file != null)
                {
                    InputStream is = new FileInputStream(file.getRawLocation().makeAbsolute().toFile());
                    try
                    {
                        ByteArrayOutputStream byteStream = new ByteArrayOutputStream();

                        BufferedInputStream buffIn = new BufferedInputStream(is);

                        /** iStream is the InputStream object **/

                        BufferedOutputStream buffOut = new BufferedOutputStream(byteStream);

                        byte[] arr = new byte[8 * 1024];

                        int available = -1;

                        while ((available = buffIn.read(arr)) > 0)
                        {

                            buffOut.write(arr, 0, available);

                        }

                        buffOut.flush();

                        buffOut.close();
                        classByte = byteStream.toByteArray();
                        byteStream.close();
                        result = defineClass(className, classByte, 0, classByte.length);
                        if (useCache)
                            classes.put(className, result);
                    }
                    finally
                    {
                        if (is != null)
                            is.close();
                    }

                    return result;
                }
            }
            catch (Exception e)
            {
                return null;
            }
            return null;
        }

    }

}
