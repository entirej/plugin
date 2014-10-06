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
package org.entirej.ide.cf.fx.lib;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.ide.cf.fx.EJCFFXPlugin;
import org.entirej.ide.core.cf.CFProjectHelper;

public class FXCFRuntimeClasspathContainer implements IClasspathContainer
{
    public final static Path ID = new Path("org.eclipse.fx.runtime.EJCF_FX_CONTAINER");

    public FXCFRuntimeClasspathContainer(IPath path)
    {
        runtimePath = CFProjectHelper.getPathInPlugin(EJCFFXPlugin.getDefault().getBundle(), new Path("/extlibs/"));
        description = "EntireJ JavaFX CF [2.3.3] Runtime Libraries.";
    }

    final String           description;
    final IPath            runtimePath;
    private FilenameFilter _dirFilter = new FilenameFilter()
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

                entryList.add(JavaCore.newLibraryEntry(new Path(lib.getAbsolutePath()), srcPath, new Path("/")));
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
