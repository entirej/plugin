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
package org.entirej.ide.ui.wizards.db;

import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.wizard.IWizardPage;
import org.entirej.framework.core.service.EJFormPojoGenerator;
import org.entirej.framework.core.service.EJFormServiceGenerator;
import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJServiceGeneratorType;
import org.entirej.framework.report.service.EJReportPojoGeneratorType;
import org.entirej.framework.report.service.EJReportServiceGeneratorType;
import org.entirej.ide.core.spi.BlockServiceContentProvider;

public class StatementBlockServiceContentProvider implements BlockServiceContentProvider
{

    public StatementBlockServiceContentProvider()
    {
    }

    public String getProviderName()
    {
        return "DB Select";
    }

    public String getProviderId()
    {
        return "org.entirej.select.block.service.content";
    }

    public String getDescription()
    {
        return "Creates a block pojo/service using the statement/columns from the database";
    }

    public BlockServiceWizardProvider createWizardProvider()
    {
        return new BlockServiceWizardProvider()
        {

            public boolean skipMainPojo()
            {
                // TODO Auto-generated method stub
                return false;
            }

            private DBSelectStatementWizardPage columnSelectionPage = new DBSelectStatementWizardPage();
            private DBStatementsWizardPage statementsWizardPage = new DBStatementsWizardPage();
            private GeneratorContext context;
            private ReportGeneratorContext rcontext;

            public void init(GeneratorContext context)
            {
                columnSelectionPage.init(context.getProject());
                this.context = context;
            }

            public void init(ReportGeneratorContext context)
            {
                columnSelectionPage.init(context.getProject());
                this.rcontext = context;
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

            public String getPogoGenerator()
            {
                return EJFormPojoGenerator.class.getName();
            }

            public String getServiceGenerator()
            {

                return EJFormServiceGenerator.class.getName();

            }
            
            
            public List<IWizardPage> getOptionalPages()
            {
                if ((context != null && context.skipService()) || (rcontext != null))
                    return Arrays.<IWizardPage> asList();

                return Arrays.<IWizardPage> asList(statementsWizardPage);
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
                    serviceGeneratorType.setQueryStatement(escapeNextLine(columnSelectionPage.getSelectStatement()));
                    serviceGeneratorType.setInsertStatement(escapeNextLine(statementsWizardPage.getInsertStatement()));
                    serviceGeneratorType.setUpdateStatement(escapeNextLine(statementsWizardPage.getUpdateStatement()));
                    serviceGeneratorType.setDeleteStatement(escapeNextLine(statementsWizardPage.getDeleteStatement()));
                    EJPojoGeneratorType pojoGeneratorType = new EJPojoGeneratorType();
                    pojoGeneratorType.setColumnNames(columnSelectionPage.getColumns());
                    return new BlockServiceContent(serviceGeneratorType, pojoGeneratorType);
                }
                return null;
            }

            public ReportBlockServiceContent getReportContent()
            {
                if (columnSelectionPage.isPageComplete())
                {
                    EJReportServiceGeneratorType serviceGeneratorType = new EJReportServiceGeneratorType();
                    serviceGeneratorType.setQueryStatement(escapeNextLine(columnSelectionPage.getSelectStatement()));
                    EJReportPojoGeneratorType pojoGeneratorType = new EJReportPojoGeneratorType();
                    pojoGeneratorType.setColumnNames(columnSelectionPage.getReportColumns());
                    return new ReportBlockServiceContent(serviceGeneratorType, pojoGeneratorType);
                }
                return null;
            }

            private String escapeNextLine(String text)
            {
                if (text == null)
                    return text;
                return text.replace("\r\n", " ").replace("\n", " ");
            }
        };
    }

    public boolean isActive(IJavaProject project)
    {
        return true;
    }

}
