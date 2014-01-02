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
package org.entirej.ide.ui.wizards.custom;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJServiceGeneratorType;
import org.entirej.ide.core.spi.BlockServiceContentProvider;

public class CustomBlockServiceContentProvider implements BlockServiceContentProvider
{

    public CustomBlockServiceContentProvider()
    {
    }

    public String getProviderName()
    {
        return "Custom";
    }

    public String getProviderId()
    {
        return "org.entirej.custom.block.service.content";
    }

    public String getDescription()
    {
        return "Creates a block pojo/service using the custom fields";
    }

    public boolean isActive(IJavaProject project)
    {
        return true;
    }

    public BlockServiceWizardProvider createWizardProvider()
    {
        return new BlockServiceWizardProvider()
        {
            private CustomFieldsPage columnSelectionPage = new CustomFieldsPage();

            public void init(GeneratorContext context)
            {
                columnSelectionPage.init(context.getProject());

            }

            public boolean canFinish(IWizardPage page)
            {
                return page.isPageComplete();
            }

            public boolean skipPage(IWizardPage page)
            {
                return false;
            }

            public List<IWizardPage> getPages()
            {
                return Arrays.<IWizardPage> asList(columnSelectionPage);
            }

            public void createRequiredResources(IProgressMonitor monitor)
            {
                // ignore

            }

            public BlockServiceContent getContent()
            {
                if (columnSelectionPage.isPageComplete())
                {
                    EJServiceGeneratorType serviceGeneratorType = new EJServiceGeneratorType();
                    EJPojoGeneratorType pojoGeneratorType = new EJPojoGeneratorType();

                    pojoGeneratorType.setColumnNames(columnSelectionPage.getSelectedColumns());
                    return new BlockServiceContent(serviceGeneratorType, pojoGeneratorType);
                }
                return null;
            }
        };
    }

}
