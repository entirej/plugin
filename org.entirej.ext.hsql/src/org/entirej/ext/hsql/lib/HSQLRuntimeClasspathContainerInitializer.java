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
package org.entirej.ext.hsql.lib;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;

public class HSQLRuntimeClasspathContainerInitializer extends ClasspathContainerInitializer
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
        HSQLRuntimeClasspathContainer container = new HSQLRuntimeClasspathContainer();

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
