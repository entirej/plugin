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

import org.entirej.framework.plugin.framework.properties.reader.EntireJTagHandler;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportBorderProperties;
import org.entirej.framework.plugin.reports.EJPluginReportChartProperties;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.interfaces.EJReportBorderProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ScreenChartHandler extends EntireJTagHandler
{
    private EJPluginReportProperties       _formProperties;
    private EJPluginReportBlockProperties  _blockProperties;
    private EJPluginReportColumnProperties _column;
    private static final String            ELEMENT_ITEM            = "config";
    private final EJPluginReportChartProperties chartProperties;
   
    
    public ScreenChartHandler(EJPluginReportBlockProperties blockProperties)
    {
        _formProperties = blockProperties.getReportProperties();
        _blockProperties = blockProperties;
        chartProperties = _blockProperties.getLayoutScreenProperties().getChartProperties();
        
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_ITEM))
        {
            
            String itemname = attributes.getValue("name");
            
            _column = new EJPluginReportColumnProperties(_blockProperties);
            _column.setName(itemname);
            _column.setShowHeader(Boolean.valueOf(attributes.getValue("showHeader")));
            _column.setShowFooter(Boolean.valueOf(attributes.getValue("showFooter")));
            _blockProperties.getLayoutScreenProperties().getColumnContainer().addColumnProperties(_column);
        }
        
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_ITEM))
        {
            quitAsDelegate();
            return;
        }
        
    }
    
    @Override
    protected void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        
    }
    
}
