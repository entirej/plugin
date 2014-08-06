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
package org.entirej.framework.plugin.reports.reader;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.reader.BlockGroupHandler;
import org.entirej.framework.plugin.framework.properties.reader.BlockHandler;
import org.entirej.framework.plugin.framework.properties.reader.EntireJTagHandler;
import org.entirej.framework.plugin.framework.properties.reader.LovDefinitionHandler;
import org.entirej.framework.plugin.framework.properties.reader.ObjGroupDefinitionHandler;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.reports.interfaces.EJReportProperties.ORIENTATION;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ReportHandler extends EntireJTagHandler
{
    private EJPluginReportProperties _reportProperties;
    
    private static final String      ELEMENT_REPORT_TITLE           = "reportTitle";
    private static final String      ELEMENT_REPORT_DISPLAY_NAME    = "reportDisplayName";
    private static final String      ELEMENT_REPORT_WIDTH           = "width";
    private static final String      ELEMENT_MARGIN_TOP             = "marginTop";
    private static final String      ELEMENT_MARGIN_BOTTOM          = "marginBottom";
    private static final String      ELEMENT_MARGIN_LEFT            = "marginLeft";
    private static final String      ELEMENT_MARGIN_RIGHT           = "marginRight";
    private static final String      ELEMENT_REPORT_ORIENTATION     = "orientation";
    private static final String      ELEMENT_REPORT_HEIGHT          = "height";
    private static final String      ELEMENT_ACTION_PROCESSOR       = "actionProcessorClassName";
    
    private static final String      ELEMENT_REPORT_PARAMETER       = "reportParameter";
    private static final String      ELEMENT_APPLICATION_PROPERTIES = "applicationProperties";
    private static final String      ELEMENT_PROPERTY               = "property";
    
    private boolean                  _gettingApplicationProperties  = false;
    private String                   _lastApplicationPropertyName   = "";
    
    public ReportHandler(IJavaProject javaProject, String formName)
    {
        _reportProperties = new EJPluginReportProperties(formName, javaProject);
    }
    
    public ReportHandler(EJPluginReportProperties reportProperties)
    {
        _reportProperties = reportProperties;
    }
    
    public EJPluginReportProperties getReportProperties()
    {
        return _reportProperties;
    }
    
    public EntireJTagHandler getBlockHandler(EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties lovDefinitionProperties)
    {
        return new BlockHandler(formProperties, lovDefinitionProperties);
    }
    
    public EntireJTagHandler getBlockGroupHandler(EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties lovDefinitionProperties)
    {
        return new BlockGroupHandler(formProperties);
    }
    
    public EntireJTagHandler getLovDefinitionHandler(EJPluginFormProperties formProperties)
    {
        return new LovDefinitionHandler(formProperties);
    }
    
    public EntireJTagHandler getObjectgroupDefinitionHandler(EJPluginFormProperties formProperties)
    {
        return new ObjGroupDefinitionHandler(formProperties);
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        
        if (name.equals(ELEMENT_APPLICATION_PROPERTIES))
        {
            _gettingApplicationProperties = true;
        }
        
        if (_gettingApplicationProperties)
        {
            if (name.equals(ELEMENT_PROPERTY))
            {
                _lastApplicationPropertyName = attributes.getValue("name");
            }
            return;
        }
        
        if (name.equals(ELEMENT_REPORT_PARAMETER))
        {
            String paramName = attributes.getValue("name");
            String dataTypeName = attributes.getValue("dataType");
            String defaultValue = attributes.getValue("defaultValue");
            
            _reportProperties.addReportParameter(new EJPluginApplicationParameter(paramName, dataTypeName, defaultValue));
        }
        
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (_gettingApplicationProperties)
        {
            if (name.equals(ELEMENT_APPLICATION_PROPERTIES))
            {
                _gettingApplicationProperties = false;
            }
            else if (name.equals(ELEMENT_PROPERTY))
            {
                _reportProperties.addApplicationProperty(_lastApplicationPropertyName, value);
            }
            return;
        }
        
        if (name.equals(ELEMENT_REPORT_TITLE))
        {
            _reportProperties.setReportTitle(value);
        }
        else if (name.equals(ELEMENT_REPORT_DISPLAY_NAME))
        {
            _reportProperties.setReportDisplayName(value);
        }
        else if (name.equals(ELEMENT_REPORT_HEIGHT))
        {
            if (value.length() > 0)
            {
                _reportProperties.setReportHeight(Integer.parseInt(value));
            }
            else
            {
                _reportProperties.setReportHeight(0);
            }
        }
        else if (name.equals(ELEMENT_REPORT_WIDTH))
        {
            if (value.length() > 0)
            {
                _reportProperties.setReportWidth(Integer.parseInt(value));
            }
            else
            {
                _reportProperties.setReportWidth(0);
            }
        }
        else if (name.equals(ELEMENT_MARGIN_TOP))
        {
            if (value.length() > 0)
            {
                _reportProperties.setMarginTop(Integer.parseInt(value));
            }
            else
            {
                _reportProperties.setMarginTop(1);
            }
        }
        else if (name.equals(ELEMENT_MARGIN_BOTTOM))
        {
            if (value.length() > 0)
            {
                _reportProperties.setMarginBottom(Integer.parseInt(value));
            }
            else
            {
                _reportProperties.setMarginBottom(1);
            }
        }
        else if (name.equals(ELEMENT_MARGIN_LEFT))
        {
            if (value.length() > 0)
            {
                _reportProperties.setMarginLeft(Integer.parseInt(value));
            }
            else
            {
                _reportProperties.setMarginLeft(1);
            }
        }
        else if (name.equals(ELEMENT_MARGIN_RIGHT))
        {
            if (value.length() > 0)
            {
                _reportProperties.setMarginRight(Integer.parseInt(value));
            }
            else
            {
                _reportProperties.setMarginRight(1);
            }
        }
        else if (name.equals(ELEMENT_ACTION_PROCESSOR))
        {
            _reportProperties.setActionProcessorClassName(value);
        }
        else if (name.equals(ELEMENT_REPORT_ORIENTATION))
        {
            _reportProperties.setOrientation(ORIENTATION.valueOf(value));
        }
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        
       
    }
}
