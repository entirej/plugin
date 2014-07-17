/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.reports;

import java.util.Arrays;
import java.util.Collection;

import org.entirej.framework.core.enumerations.EJRendererType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionListener;
import org.entirej.framework.dev.properties.EJDevPropertyDefinitionGroup;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
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

    public static Collection<? extends EJPluginRenderer> getReportRenderers(EJPluginEntireJProperties entireJProperties)
    {
        EJPluginRenderer jasperRenderer = new EJPluginRenderer(entireJProperties, "JasperReport", EJRendererType.APP_COMPONENT,
                "org.entirej.reports.jasper.renderers.EJJasperReportRendererDefinition",
                "org.entirej.reports.jasper.renderers.EJJasperReportRenderer");
        return Arrays.asList(jasperRenderer);
    }
    
}
