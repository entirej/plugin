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
package org.entirej.ide.ui.wizards.project;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageOne;
import org.eclipse.jdt.ui.wizards.NewJavaProjectWizardPageTwo;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkingSet;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.wizards.newresource.BasicNewProjectResourceWizard;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIMessages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJProjectWizard extends NewWizard implements IExecutableExtension
{
    public static final String          DEF_PROJECT_NAME = "project_name"; //$NON-NLS-1$
    private NewJavaProjectWizardPageOne projectCreationPage;
    private NewJavaProjectWizardPageTwo projectSetupPage;
    private NewEJProjectConfigPage      projectSettingsPage;

    private IConfigurationElement       configElement;

    public NewEntireJProjectWizard()
    {
        setDefaultPageImageDescriptor(EJUIImages.DESC_NEWEJPRJ_WIZ);
        setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(EJUIMessages.NewProjectWizard_title);

    }

    public void addPages()
    {
        projectCreationPage = new NewJavaProjectWizardPageOne(); //$NON-NLS-1$
        projectCreationPage.setTitle(EJUIMessages.NewProjectWizard_MainPage_title);
        projectCreationPage.setDescription(EJUIMessages.NewProjectWizard_MainPage_desc);
        String pname = getDefaultValue(DEF_PROJECT_NAME);
        if (pname != null)
            projectCreationPage.setProjectName(pname);
        addPage(projectCreationPage);

        projectSettingsPage = new NewEJProjectConfigPage();
        addPage(projectSettingsPage);

        projectSetupPage = new NewJavaProjectWizardPageTwo(projectCreationPage);
        addPage(projectSetupPage);
    }

    public boolean canFinish()
    {
        IWizardPage page = getContainer().getCurrentPage();
        return super.canFinish() && page != projectCreationPage;
    }

    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {
        projectSetupPage.performFinish(monitor);
        final IJavaProject javaProject = getCreatedElement();
        projectSettingsPage.addEntireNature(configElement, javaProject, monitor);
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {

                IWorkingSet[] workingSets = projectCreationPage.getWorkingSets();
                if (workingSets.length > 0)
                {
                    PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(javaProject, workingSets);
                }

                BasicNewProjectResourceWizard.updatePerspective(configElement);
                selectAndReveal(projectSetupPage.getJavaProject().getProject());
            }
        });

    }

    public boolean performCancel()
    {
        projectSetupPage.performCancel();
        return super.performCancel();
    }

    public IJavaProject getCreatedElement()
    {
        return projectSetupPage.getJavaProject();
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        this.configElement = config;

    }

}
