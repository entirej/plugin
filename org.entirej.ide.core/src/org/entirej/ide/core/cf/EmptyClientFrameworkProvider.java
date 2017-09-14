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
package org.entirej.ide.core.cf;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.EJCorePlugin;
import org.entirej.ide.core.spi.ClientFrameworkProvider;

public class EmptyClientFrameworkProvider implements ClientFrameworkProvider
{

    private static final String EMPTY_PROJECT_PROPERTIES_FILE = "/templates/empty/application.ejprop";

    public void addEntireJNature(IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");
            CFProjectHelper.addFile(project, EJCorePlugin.getDefault().getBundle(), EMPTY_PROJECT_PROPERTIES_FILE, "src/application.ejprop");

            CFProjectHelper.addEntireJBaseLibraries(project);

            // adding Required Generator Files
            addGeneratorFiles(project, monitor);

            CFProjectHelper.refreshProject(project, monitor);
            final IFile file = project.getProject().getFile("src/application.ejprop");
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try
                    {
                        IDE.openEditor(page, file, true);
                    }
                    catch (PartInitException e)
                    {
                        EJCoreLog.logException(e);
                    }
                }
            });
        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }

    public static void addGeneratorFiles(IJavaProject project, IProgressMonitor monitor) throws IOException
    {

    }

    public String getProviderName()
    {
        return "Empty";
    }

    public String getProviderId()
    {
        return "org.entirej.framework.cf.empty";
    }

    public String getDescription()
    {
        return "Creates an empty project with no defaults";
    }

    public IClasspathAttribute[] getClasspathAttributes()
    {
        return new IClasspathAttribute[0];
    }

}
