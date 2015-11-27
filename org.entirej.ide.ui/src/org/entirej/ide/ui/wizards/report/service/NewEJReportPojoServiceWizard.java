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
package org.entirej.ide.ui.wizards.report.service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.IWizardPage;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEJReportPojoServiceWizard extends NewWizard implements IExecutableExtension
{

    // private IConfigurationElement configElement;

    private NewEJReportPojoServiceSelectPage  pojoServiceSelectPage;
    private NewEJReportGenServicePage         servicePage;
    private NewEJReportPojoServiceContentPage contentPage;

    private boolean                     serviceOptional = true;
    private String                      serviceTypeName;

    public NewEJReportPojoServiceWizard()
    {
        setDefaultPageImageDescriptor(EJUIImages.DESC_NEWEJPOJO_SERV_WIZ);
        setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle("New Report Block Service");
    }

    public void setInitializationData(IConfigurationElement config, String propertyName, Object data) throws CoreException
    {
        // this.configElement = config;
    }

    @Override
    public void addPages()
    {
        pojoServiceSelectPage = new NewEJReportPojoServiceSelectPage();
        pojoServiceSelectPage.setTitle("EntireJ Report Block Pojo");
        pojoServiceSelectPage.setDescription("Create a new report block pojo");
        pojoServiceSelectPage.init(getSelection());
        pojoServiceSelectPage.setCreateSerivce(true, serviceOptional);
        contentPage = new NewEJReportPojoServiceContentPage(pojoServiceSelectPage);
        contentPage.init(getSelection());
        addPage(contentPage);
        
        addPage(pojoServiceSelectPage);

        servicePage = new NewEJReportGenServicePage(pojoServiceSelectPage);
        addPage(servicePage);

        
        super.addPages();
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {
        contentPage.createPojoService(pojoServiceSelectPage, servicePage, monitor);
        serviceTypeName = servicePage != null ? servicePage.getTypeName() : null;
    }

    @Override
    public boolean canFinish()
    {
        IWizardPage page = getContainer().getCurrentPage();
        if ( page == contentPage)
            return false;

        return contentPage.canFinish() && (pojoServiceSelectPage.isPageComplete() && (!pojoServiceSelectPage.isCreateSerivce() || servicePage.isPageComplete()));
    }

    @Override
    public IWizardPage getNextPage(IWizardPage page)
    {
        
        if (page == contentPage)
            return contentPage.getStartingPage();
        
        IWizardPage nextPage = contentPage.getNextPage(page);
        
        if(nextPage!=null)
        {
            return nextPage;
        }
        
        if(contentPage.pageOfMain(page))
        {
            pojoServiceSelectPage.setPojoNeed(!contentPage.getWizardProvider().skipMainPojo());
            pojoServiceSelectPage.setProjectProvider(contentPage);
            return pojoServiceSelectPage;
        }
        
        
        if (page == pojoServiceSelectPage)
        {
            servicePage.setProjectProvider(contentPage);
            if (!pojoServiceSelectPage.isCreateSerivce())
                return getNextPage(servicePage);
            else
                return servicePage;
        }
       

        

       
        return contentPage.getOptionalNextPage(page);
    }

    @Override
    public IWizardPage getPreviousPage(IWizardPage page)
    {
        if (page == contentPage)
        {
            return null;
        }
        if (page == contentPage.getStartingPage())
        {
            return contentPage;
        }
        
        IWizardPage previousPage = contentPage.getPreviousPage(page);
        
        if(previousPage!=null)
        {
            return previousPage;
        }
        
        
        
        if (page == contentPage.getOptinalStartingPage())
        {
            pojoServiceSelectPage.setPojoNeed(!contentPage.getWizardProvider().skipMainPojo());
            if (pojoServiceSelectPage.isCreateSerivce())
                return servicePage;
            else
                return pojoServiceSelectPage;
        }
        
        if (page == servicePage)
        {
            pojoServiceSelectPage.setPojoNeed(!contentPage.getWizardProvider().skipMainPojo());
            return pojoServiceSelectPage;
        }

       
     

        
        return contentPage.getOptionalPreviousPage(page);
    }

    @Override
    public int getPageCount()
    {
        int pageCount = super.getPageCount();

        int  i = pageCount + contentPage.getPageCount()+contentPage.getOptionalPageCount();
        return  i;
    }

    public boolean isServiceOptional()
    {
        return serviceOptional;
    }

    public void setServiceOptional(boolean serviceOptional)
    {
        this.serviceOptional = serviceOptional;
    }

    public String getServiceTypeName()
    {
        return serviceTypeName;
    }

}
