package org.entirej;

import org.eclipse.rap.rwt.application.Application;
import org.entirej.applicationframework.tmt.application.launcher.EJTMTApplicationLauncher;
import org.entirej.applicationframework.tmt.pages.EJTMTFormComponentPage;
import org.entirej.framework.core.EJFrameworkHelper;

import com.eclipsesource.tabris.ui.UIConfiguration;

public class ApplicationLauncher extends EJTMTApplicationLauncher
{

   
    
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
    
    /*uncomment to provide default Authenticate support
    @Override
    public EJTMTAuthenticateProvider getAuthenticateProvider(EJFrameworkHelper frameworkHelper)
    {
        return new EJTMTDefaultAuthenticateProvider()
        {
            
            @Override
            public String authenticate(EJFrameworkHelper frameworkHelper, String user, String password)
            {
                //return validation error or null signin
                return null;
            }
        };
    }
    */
    
}