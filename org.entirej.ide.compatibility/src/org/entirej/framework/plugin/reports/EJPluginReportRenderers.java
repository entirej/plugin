package org.entirej.framework.plugin.reports;

import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionListener;
import org.entirej.framework.dev.properties.EJDevPropertyDefinitionGroup;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.reports.renderers.definitions.interfaces.EJReportFrameworkExtensionProperties;
import org.entirej.framework.reports.renderers.definitions.interfaces.EJReportRendererDefinition;

public class EJPluginReportRenderers
{
    
    public static EJReportFrameworkExtensionProperties createReportRendererProperties(EJPluginReportProperties pluginReportProperties, boolean addDefaults)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public static EJReportRendererDefinition loadReportRendererDefinition(EJPluginEntireJProperties entireJProperties, String reportRendererName)
    {
        // TODO Auto-generated method stub
        return new EJReportRendererDefinition()
        {
            
            @Override
            public void propertyChanged(EJPropertyDefinitionListener arg0, EJFrameworkExtensionProperties arg1, String arg2)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public void loadValidValuesForProperty(EJFrameworkExtensionProperties arg0, EJPropertyDefinition arg1)
            {
                // TODO Auto-generated method stub
                
            }
            
            @Override
            public String getRendererClassName()
            {
                // TODO Auto-generated method stub
                return "";
            }
            
            @Override
            public EJPropertyDefinitionGroup getReportPropertyDefinitionGroup()
            {
                return new EJDevPropertyDefinitionGroup("REPORT");
            }
        };
    }
    
}
