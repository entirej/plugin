package org.entirej;

import org.eclipse.rap.rwt.application.Application;
import org.entirej.applicationframework.tmt.application.launcher.EJTMTApplicationLauncher;
import org.entirej.applicationframework.tmt.pages.EJTMTFormComponentPage;
import org.entirej.framework.core.EJFrameworkHelper;

import com.eclipsesource.tabris.ui.UIConfiguration;

public class ApplicationLauncher extends EJTMTApplicationLauncher
{

    @Override
    protected void initRootPageConfiguration(UIConfiguration configuration)
    {
        //OVERRIDE to add default root pages - by default CF will add Menu page
        addRootPageConfiguration(configuration, DefaultMenuPage.ID, DefaultMenuPage.class, "Menu");
        
        //adding Form as a Root Page
        //addRootFormComponentPage(configuration, ARootFormPage.PAGE_ID,ARootFormPage.FORM_ID, ARootFormPage.class, "AForm");
    }
    
    @Override
    public void configure(Application configuration)
    {
        super.configure(configuration);
    }
    
    @Override
    public void postApplicationBuild(EJFrameworkHelper frameworkHelper)
    {

    }

    @Override
    protected String getLoadingMessage()
    {
        return "Loading EntireJ Tabris Application (Mobile)";
    }
    
    
    public static class ARootFormPage extends EJTMTFormComponentPage
    {
        static final String PAGE_ID = "A_FORM_ID_ROOT_PAGE";// change A_FORM_ID to EJ Form Name
        static final String FORM_ID = "A_FORM_ID";// change A_FORM_ID to EJ Form Name

        
        public ARootFormPage()
        {
            super(FORM_ID);
        }
        
    }
}