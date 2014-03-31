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
package org.entirej.ide.ui.wizards.form;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJObjectGroupWizard extends NewWizard implements IExecutableExtension
{
    public static final String     DEF_OBJ_GROUP_NAME = "objgroup_name"; //$NON-NLS-1$
    private NewEntireJObjectGroupPage refBlockCreationPage;

    private IConfigurationElement  configElement;

    public NewEntireJObjectGroupWizard()
    {
        setDefaultPageImageDescriptor(EJUIImages.DESC_NEWEJFRM_WIZ);
        setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle(" New EntireJ ObjectGroup");

    }

    public void addPages()
    {
        refBlockCreationPage = new NewEntireJObjectGroupPage(); //$NON-NLS-1$
        refBlockCreationPage.setTitle("EntireJ ObjectGroup");
        refBlockCreationPage.setDescription("Create a new ObjectGroup");
        String fname = getDefaultValue(DEF_OBJ_GROUP_NAME);
        if (fname != null)
            refBlockCreationPage.setFormName(fname);
        refBlockCreationPage.init(getSelection());
        addPage(refBlockCreationPage);

    }

    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {

        refBlockCreationPage.createForm(configElement, monitor);
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        this.configElement = config;

    }

}
