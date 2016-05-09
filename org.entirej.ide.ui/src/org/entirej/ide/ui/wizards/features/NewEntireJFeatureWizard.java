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
package org.entirej.ide.ui.wizards.features;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIMessages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJFeatureWizard extends NewWizard implements IExecutableExtension
{
    
    private NewEntireJFeaturePage    formCreationPage;

    private IConfigurationElement configElement;

    public NewEntireJFeatureWizard()
    {
        //setDefaultPageImageDescriptor(EJUIImages.DESC_NEWEJREPORT_SERV_WIZ);
        setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle("Add Feature");

    }

    public void addPages()
    {
        formCreationPage = new NewEntireJFeaturePage(this); //$NON-NLS-1$
        formCreationPage.setTitle("EntireJ Feature");
        formCreationPage.setDescription("Add a feature to EntireJ project.");
       
        formCreationPage.init(getSelection());
        addPage(formCreationPage);

    }

    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {

        formCreationPage.addFeature(configElement, monitor);
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        this.configElement = config;

    }

}
