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
package org.entirej.ide.core.cf.lib;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.core.runtime.EJRuntimePlugin;
import org.entirej.ide.core.cf.CFProjectHelper;

public class CoreRuntimeClasspathContainer implements IClasspathContainer
{
   

   
    public CoreRuntimeClasspathContainer(IPath path)
    {
//        if(CoreRuntimeVersions.V_1_0.getPath().equals(path))
//        {
//            runtimePath = CFProjectHelper.getPathInPlugin(EJRuntimePlugin.getDefault().getBundle(), new Path("/extlibs_v_1_0/"));
//            description = "EntireJ Core [1.0] Runtime Libraries.";
//        }
//        else
//        {
            runtimePath = CFProjectHelper.getPathInPlugin(EJRuntimePlugin.getDefault().getBundle(), new Path("/extlibs/"));
            description = "EntireJ Core [2.1] Runtime Libraries.";
//        }
    }
    
    final String description ;
    final IPath              runtimePath ;
    private FilenameFilter   _dirFilter  = new FilenameFilter()
                                         {

                                             public boolean accept(File dir, String name)
                                             {
                                                 String[] nameSegs = name.split("[.]");
                                                 if (nameSegs.length < 2)
                                                 {
                                                     return false;
                                                 }
                                                 if (nameSegs[nameSegs.length - 2].endsWith("-src"))
                                                 {
                                                     return false;
                                                 }
                                                 if (nameSegs[nameSegs.length - 2].endsWith("-javadoc"))
                                                 {
                                                     return false;
                                                 }
                                                 if ("jar".equals(nameSegs[nameSegs.length - 1].toLowerCase()))
                                                 {
                                                     return true;
                                                 }
                                                 return false;
                                             }
                                         };

    public IClasspathEntry[] getClasspathEntries()
    {
        ArrayList<IClasspathEntry> entryList = new ArrayList<IClasspathEntry>();
        // fetch the names of all files that match our filter
        File _dir = new File(runtimePath.toOSString());
        if (_dir.exists())
        {
            File[] libs = _dir.listFiles(_dirFilter);
            for (File lib : libs)
            {

                IClasspathAttribute atts[] = null;
                String[] split = lib.getName().split("[.]");
                // strip off the file extension
                String ext = split[split.length - 1];
                // now see if this archive has an associated src jar
                File srcArc = new File(lib.getAbsolutePath().replace("." + ext, "-src." + ext));
                Path srcPath = null;
                // if the source archive exists then get the path to attach it
                if (srcArc.exists())
                {
                    srcPath = new Path(srcArc.getAbsolutePath());
                }
                // now see if this archive has an associated src jar
                File docArc = new File(lib.getAbsolutePath().replace("." + ext, "-javadoc." + ext));
                Path docPath = null;
                // if the source archive exists then get the path to attach it
                if (docArc.exists())
                {
                    docPath = new Path(docArc.getAbsolutePath());
                    atts = new IClasspathAttribute[] { JavaCore.newClasspathAttribute(IClasspathAttribute.JAVADOC_LOCATION_ATTRIBUTE_NAME, "jar:"
                            + docPath.toFile().toURI().toString() + "!/") };
                }
                IAccessRule[] accessRules = {};
                IClasspathEntry entry = JavaCore.newLibraryEntry(new Path(lib.getAbsolutePath()), srcPath, new Path("/"), accessRules, atts, false);

                entryList.add(entry);
            }
        }
        return (IClasspathEntry[]) entryList.toArray(new IClasspathEntry[entryList.size()]);
    }

    public String getDescription()
    {
        return description;
    }

    public int getKind()
    {
        return IClasspathContainer.K_APPLICATION;
    }

    public IPath getPath()
    {
        return runtimePath;
    }

}
