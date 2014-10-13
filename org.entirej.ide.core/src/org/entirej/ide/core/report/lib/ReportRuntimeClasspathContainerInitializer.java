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
package org.entirej.ide.core.report.lib;

import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ClasspathContainerInitializer;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.ide.core.EJConstants;
import org.entirej.ide.core.project.EJProject;

public class ReportRuntimeClasspathContainerInitializer extends ClasspathContainerInitializer
{

    public final static IPath  ID       = new Path("org.eclipse.core.runtime.EJ_REPORT_CONTAINER");
    //public final static IPath  ID_V_1_0 = new Path("org.eclipse.core.runtime.EJ_CORE_CONTAINER").append("V_1_0");

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
        ReportRuntimeClasspathContainer container = new ReportRuntimeClasspathContainer(containerPath);

        JavaCore.setClasspathContainer(containerPath, new IJavaProject[] { project }, new IClasspathContainer[] { container }, null);

        if (EJProject.hasPluginNature(project.getProject()))
        {
            IProjectNature nature = project.getProject().getNature(EJConstants.EJ_NATURE);
            if (nature instanceof EJProject)
            {
                ((EJProject) nature).verifyBuilder();
            }
        }

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
