package org.entirej.ide.cf.swing.lib;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class SwingRuntimeClasspathContainerInitializer extends ClasspathContainerInitializer
{

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.jdt.core.ClasspathContainerInitializer#initialize(org.eclipse
     * .core.runtime.IPath, org.eclipse.jdt.core.IJavaProject)
     */
    @Override
    public void initialize(IPath containerPath, IJavaProject project) throws CoreException
    {
        SwingRuntimeClasspathContainer container = new SwingRuntimeClasspathContainer();

        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, new IClasspathContainer[] { container }, null);

    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.ClasspathContainerInitializer#
     * canUpdateClasspathContainer(org.eclipse.core.runtime.IPath,
     * org.eclipse.jdt.core.IJavaProject)
     */
    @Override
    public boolean canUpdateClasspathContainer(IPath containerPath, IJavaProject project)
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.jdt.core.ClasspathContainerInitializer#
     * requestClasspathContainerUpdate(org.eclipse.core.runtime.IPath,
     * org.eclipse.jdt.core.IJavaProject,
     * org.eclipse.jdt.core.IClasspathContainer)
     */
    @Override
    public void requestClasspathContainerUpdate(IPath containerPath, IJavaProject project, IClasspathContainer containerSuggestion) throws CoreException
    {
        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, new IClasspathContainer[] { containerSuggestion }, null);
    }

}
