package org.entirej.ide.cf.swing.lib;

import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.ide.cf.swing.EJCFSwingPlugin;
import org.entirej.ide.core.cf.CFProjectHelper;

public class SwingRuntimeClasspathContainer implements IClasspathContainer
{
    public final static Path ID          = new Path("org.eclipse.swing.runtime.EJCF_SWING_CONTAINER");

    final IPath              runtimePath = CFProjectHelper.getPathInPlugin(EJCFSwingPlugin.getDefault().getBundle(), new Path("/extlibs/"));
    private FilenameFilter   _dirFilter  = new FilenameFilter()
                                         {

                                             public boolean accept(File dir, String name)
                                             {
                                                 String[] nameSegs = name.split("[.]");
                                                 if (nameSegs.length < 2)
                                                 {
                                                     return false;
                                                 }
                                                 if (nameSegs[nameSegs.length-2].endsWith("-src"))
                                                 {
                                                     return false;
                                                 }
                                                 if ("jar".equals(nameSegs[nameSegs.length-1].toLowerCase()))
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
                // strip off the file extension
                String ext = lib.getName().split("[.]")[1];
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
        return "EntireJ Swing Runtime Libraries.";
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
