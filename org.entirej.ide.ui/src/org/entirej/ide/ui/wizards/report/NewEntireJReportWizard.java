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
package org.entirej.ide.ui.wizards.report;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIMessages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJReportWizard extends NewWizard implements IExecutableExtension
{
    public static final String    DEF_REPORT_NAME = "report_name"; //$NON-NLS-1$
    
    private NewEntireJReportPage    formCreationPage;

    private IConfigurationElement configElement;

    public NewEntireJReportWizard()
    {
        setDefaultPageImageDescriptor(EJUIImages.DESC_NEWEJFRM_WIZ);
        setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(EJUIMessages.NewReportWizard_title);

    }

    public void addPages()
    {
        formCreationPage = new NewEntireJReportPage(); //$NON-NLS-1$
        formCreationPage.setTitle(EJUIMessages.NewReportWizard_MainPage_title);
        formCreationPage.setDescription(EJUIMessages.NewReportWizard_MainPage_desc);
        String fname = getDefaultValue(DEF_REPORT_NAME);
        if (fname != null)
            formCreationPage.setFormName(fname);
        formCreationPage.init(getSelection());
        addPage(formCreationPage);

    }

    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {

        formCreationPage.createReport(configElement, monitor);
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        this.configElement = config;

    }

}
