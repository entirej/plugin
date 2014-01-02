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
package org.entirej.ide.core.spi;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.wizard.IWizardPage;
import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJServiceGeneratorType;

public interface BlockServiceContentProvider
{

    public static final String EXTENSION_POINT_ID = "org.entirej.ide.core.spi.blockservicecontent.provider";

    public String getProviderName();

    public String getDescription();

    public String getProviderId();

    public boolean isActive(IJavaProject project);

    BlockServiceWizardProvider createWizardProvider();

    public interface BlockServiceWizardProvider
    {
        void init(GeneratorContext context);

        List<IWizardPage> getPages();

        boolean canFinish(IWizardPage page);

        boolean skipPage(IWizardPage page);

        void createRequiredResources(IProgressMonitor monitor);

        BlockServiceContent getContent();
    }

    public interface GeneratorContext
    {
        IPackageFragmentRoot getPackageFragmentRoot();

        IJavaProject getProject();

        boolean skipService();

        String createPojoClass(EJPojoGeneratorType pojoGeneratorType, IProgressMonitor monitor) throws Exception;
    }

    public class BlockServiceContent
    {
        private final EJServiceGeneratorType serviceGeneratorType;
        private final EJPojoGeneratorType    pojoGeneratorType;

        public BlockServiceContent(EJServiceGeneratorType serviceGeneratorType, EJPojoGeneratorType pojoGeneratorType)
        {
            this.serviceGeneratorType = serviceGeneratorType;
            this.pojoGeneratorType = pojoGeneratorType;
        }

        public EJServiceGeneratorType getServiceGeneratorType()
        {
            return serviceGeneratorType;
        }

        public EJPojoGeneratorType getpPojoGeneratorType()
        {
            return pojoGeneratorType;
        }
    }

}
