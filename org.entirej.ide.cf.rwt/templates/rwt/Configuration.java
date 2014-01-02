package org.entirej.tabris;

import org.entirej.applicationframework.rwt.application.launcher.EJTabrisApplicationLauncher;
import org.entirej.applicationframework.rwt.application.launcher.EJTabrisEntryPoint;

import com.eclipsesource.tabris.ui.UIConfiguration;

public class Configuration extends EJTabrisApplicationLauncher
{

    @Override
    protected EJTabrisEntryPoint createEntryPoint()
    {
        return new EJTabrisEntryPoint()
        {
            
            private static final long serialVersionUID = -7459462468626319126L;

            protected void initRootPageConfiguration(UIConfiguration configuration)
            {			
            			addRootPageConfiguration(configuration, "ej.defaulMenu", DefaultMenuPage.class, "Menu");
                        //un-comment to add Form as a Root Page 
            			//addRootPageConfiguration(configuration, "ej.aform", CountryFormPage.class, "Form Title");
                         
            }
        };
    }
}
