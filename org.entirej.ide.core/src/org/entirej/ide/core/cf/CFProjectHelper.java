/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.core.cf;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.entirej.ide.core.cf.lib.CoreRuntimeClasspathContainerInitializer;
import org.entirej.ide.core.report.lib.ReportRuntimeClasspathContainerInitializer;
import org.osgi.framework.Bundle;

/**
 * Helper methods to set up a IJavaProject.
 */
public class CFProjectHelper
{

    public static void refreshProject(IJavaProject project, IProgressMonitor monitor) throws CoreException
    {
        project.getProject().refreshLocal(IResource.DEPTH_INFINITE, monitor);
    }

    public static IPath getPathInPlugin(Bundle bundle, IPath path)
    {
        try
        {

            File file = FileLocator.getBundleFile(bundle);

            return new Path(file.getAbsolutePath()).append(path);
        }
        catch (IOException e)
        {
            return null;
        }
    }

    public static IPath addPathToProject(IJavaProject project, Bundle bundle, String source, String target) throws IOException
    {
        addFile(project, bundle, source, target);
        return project.getProject().getLocation().append(target);
    }

    public static void addEntireJBaseLibraries(IJavaProject project) throws JavaModelException
    {
        addToClasspath(project, JavaCore.newContainerEntry(CoreRuntimeClasspathContainerInitializer.ID, true));
    }

    public static void addEntireJBaseLibraries(IJavaProject project, IClasspathAttribute[] attributes) throws JavaModelException
    {
        addToClasspath(project, JavaCore.newContainerEntry(CoreRuntimeClasspathContainerInitializer.ID, new IAccessRule[0], attributes, true));
    }
    public static void addEntireJReportLibraries(IJavaProject project) throws JavaModelException
    {
        addToClasspath(project, JavaCore.newContainerEntry(ReportRuntimeClasspathContainerInitializer.ID, true));
        addToClasspath(project, JavaCore.newContainerEntry(new Path("org.eclipse.jasper.runtime.EJ_REPORT_JASPER_CONTAINER"), true));
    }
    
    public static void addEntireJReportLibraries(IJavaProject project, IClasspathAttribute[] attributes) throws JavaModelException
    {
        addToClasspath(project, JavaCore.newContainerEntry(ReportRuntimeClasspathContainerInitializer.ID, new IAccessRule[0], attributes, true));
    }

    public static void setClasspathVariable(String var, IPath ejCoreJar) throws JavaModelException
    {
        JavaCore.setClasspathVariable(var, //
                ejCoreJar, //
                new NullProgressMonitor());
    }

    public static void addFile(IJavaProject project, Bundle bundle, String sourcefile, String targetFileName) throws IOException
    {
        InputStream stream = FileLocator.openStream(bundle, new Path(sourcefile), true);
        BufferedInputStream in = new BufferedInputStream(stream);
        try
        {
            File tempFile = new File(project.getProject().getLocation().toOSString(), targetFileName);

            if (!tempFile.exists() || !tempFile.getParentFile().exists())
            {
                tempFile.getParentFile().mkdirs();
            }

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));

            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf)) > 0)
            {

                out.write(buf, 0, len);

            }
            out.close();
        }
        finally
        {
            in.close();
        }
    }
    
    static String convertStreamToString(java.io.InputStream is) {
        java.util.Scanner s = new java.util.Scanner(is).useDelimiter("\\A");
        return s.hasNext() ? s.next() : "";
    }
    
    public static void addFile(IJavaProject project, Bundle bundle, String sourcefile, String targetFileName,Map<String,String> params) throws IOException
    {
        InputStream stream = FileLocator.openStream(bundle, new Path(sourcefile), true);
        
        String content = convertStreamToString(stream);
        stream.close();
        
        for (Entry<String, String> entry : params.entrySet())
        {
            content = content.replaceAll(entry.getKey(), entry.getValue());
        }
        
        BufferedInputStream in = new BufferedInputStream(new ByteArrayInputStream(content.getBytes()));
        try
        {
            File tempFile = new File(project.getProject().getLocation().toOSString(), targetFileName);
            
            if (!tempFile.exists() || !tempFile.getParentFile().exists())
            {
                tempFile.getParentFile().mkdirs();
            }
            
            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));
            
            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf)) > 0)
            {
                
                out.write(buf, 0, len);
                
            }
            out.close();
        }
        finally
        {
            in.close();
        }
    }

    public static void addFile(IJavaProject project, Bundle bundle, byte[] source, String targetFileName) throws IOException
    {
        InputStream stream = new ByteArrayInputStream(source);
        BufferedInputStream in = new BufferedInputStream(stream);
        try
        {
            File tempFile = new File(project.getProject().getLocation().toOSString(), targetFileName);

            if (!tempFile.exists() || !tempFile.getParentFile().exists())
            {
                tempFile.getParentFile().mkdirs();
            }

            BufferedOutputStream out = new BufferedOutputStream(new FileOutputStream(tempFile));

            byte[] buf = new byte[2048];
            int len;
            while ((len = in.read(buf)) > 0)
            {

                out.write(buf, 0, len);

            }
            out.close();
        }
        finally
        {
            in.close();
        }
    }

    /**
     * Adds a source container to a IJavaProject.
     */
    public static IPackageFragmentRoot verifySourceContainer(IJavaProject jproject, String containerName) throws CoreException
    {
        IProject project = jproject.getProject();
        IContainer container = null;
        if (containerName == null || containerName.length() == 0)
        {
            container = project;
        }
        else
        {
            IFolder folder = project.getFolder(containerName);
            if (!folder.exists())
            {
                folder.create(false, true, null);
            }
            container = folder;
        }
        IPackageFragmentRoot root = jproject.getPackageFragmentRoot(containerName);
        if (root != null)
        {
            root = jproject.getPackageFragmentRoot(container);

            IClasspathEntry cpe = JavaCore.newSourceEntry(root.getPath());
            addToClasspath(jproject, cpe);
        }
        return root;
    }

    /**
     * Removes a source folder from a IJavaProject.
     */
    public static void removeSourceContainer(IJavaProject jproject, String containerName) throws CoreException
    {
        IFolder folder = jproject.getProject().getFolder(containerName);
        removeFromClasspath(jproject, folder.getFullPath());
        folder.delete(true, null);
    }

    /**
     * Adds a library entry to a IJavaProject.
     */
    public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path) throws JavaModelException
    {
        return addLibrary(jproject, path, null, null);
    }

    /**
     * Adds a library entry with source attachment to a IJavaProject.
     */
    public static IPackageFragmentRoot addLibrary(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot) throws JavaModelException
    {
        IClasspathEntry cpe = JavaCore.newLibraryEntry(path, sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        return jproject.getPackageFragmentRoot(path.toString());
    }

    /**
     * Adds a variable entry with source attchment to a IJavaProject. Can return
     * null if variable can not be resolved.
     */
    public static IPackageFragmentRoot addVariableEntry(IJavaProject jproject, IPath path, IPath sourceAttachPath, IPath sourceAttachRoot)
            throws JavaModelException
    {
        IClasspathEntry cpe = JavaCore.newVariableEntry(path, sourceAttachPath, sourceAttachRoot);
        addToClasspath(jproject, cpe);
        IPath resolvedPath = JavaCore.getResolvedVariablePath(path);
        if (resolvedPath != null)
        {
            return jproject.getPackageFragmentRoot(resolvedPath.toString());
        }
        return null;
    }

    /**
     * Adds a required project entry.
     */
    public static void addRequiredProject(IJavaProject jproject, IJavaProject required) throws JavaModelException
    {
        IClasspathEntry cpe = JavaCore.newProjectEntry(required.getProject().getFullPath());
        addToClasspath(jproject, cpe);
    }

    public static void removeFromClasspath(IJavaProject jproject, IPath path) throws JavaModelException
    {
        IClasspathEntry[] oldEntries = jproject.getRawClasspath();
        int nEntries = oldEntries.length;
        ArrayList<IClasspathEntry> list = new ArrayList<IClasspathEntry>(nEntries);
        for (int i = 0; i < nEntries; i++)
        {
            IClasspathEntry curr = oldEntries[i];
            if (!path.equals(curr.getPath()))
            {
                list.add(curr);
            }
        }
        IClasspathEntry[] newEntries = (IClasspathEntry[]) list.toArray(new IClasspathEntry[list.size()]);
        jproject.setRawClasspath(newEntries, null);
    }

    public static void addToClasspath(IJavaProject jproject, IPath path) throws JavaModelException
    {
        addToClasspath(jproject, JavaCore.newContainerEntry(path, true));
    }

    public static void addToClasspath(IJavaProject jproject, IClasspathEntry cpe) throws JavaModelException
    {
        IClasspathEntry[] oldEntries = jproject.getRawClasspath();
        for (int i = 0; i < oldEntries.length; i++)
        {
            if (oldEntries[i].equals(cpe))
            {
                return;
            }
        }
        int nEntries = oldEntries.length;
        IClasspathEntry[] newEntries = new IClasspathEntry[nEntries + 1];
        System.arraycopy(oldEntries, 0, newEntries, 0, nEntries);
        newEntries[nEntries] = cpe;
        jproject.setRawClasspath(newEntries, null);
    }

    public static boolean hasClasspath(IJavaProject jproject, IPath path) throws JavaModelException
    {

        return hasClasspath(jproject, JavaCore.newContainerEntry(path, true));
    }

    public static boolean hasClasspath(IJavaProject jproject, IClasspathEntry cpe) throws JavaModelException
    {
        IClasspathEntry[] oldEntries = jproject.getRawClasspath();
        for (int i = 0; i < oldEntries.length; i++)
        {
            if (oldEntries[i].getPath().equals(cpe.getPath()))
            {
                return true;
            }
        }
        return false;
    }

}
