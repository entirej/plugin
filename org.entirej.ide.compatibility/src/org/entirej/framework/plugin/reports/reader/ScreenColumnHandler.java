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
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ScreenColumnHandler extends EntireJTagHandler
{
    private EJPluginReportProperties       _formProperties;
    private EJPluginReportBlockProperties  _blockProperties;
    private EJPluginReportColumnProperties _column;
    private static final String            ELEMENT_ITEM          = "columnitem";
    private static final String            ELEMENT_HEADER        = "headerScreen";
    private static final String            ELEMENT_DETAIL        = "detailScreen";
    private static final String            ELEMENT_FOOTER        = "footerScreen";
    
    private static final String            ELEMENT_SCREEN_WIDTH  = "width";
    private static final String            ELEMENT_SCREEN_HEIGHT = "height";
    
    private static final String            ELEMENT_SCREEN_ITEM   = "screenitem";
    
    public ScreenColumnHandler(EJPluginReportBlockProperties blockProperties)
    {
        _formProperties = blockProperties.getReportProperties();
        _blockProperties = blockProperties;
        
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
        else if (name.equals(ELEMENT_HEADER))
        {
            setDelegate(new ColumnHandler(name, _column.getHeaderScreen()));
            return;
        }
        else if (name.equals(ELEMENT_DETAIL))
        {
            setDelegate(new ColumnHandler(name, _column.getDetailScreen()));
            return;
        }
        else if (name.equals(ELEMENT_FOOTER))
        {
            setDelegate(new ColumnHandler(name, _column.getFooterScreen()));
            return;
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
    
    private static class ColumnHandler extends EntireJTagHandler
    {
        private final String                         tag;
        private EJPluginReportScreenProperties screenProperties;
        
        public ColumnHandler(String tag, EJPluginReportScreenProperties screenProperties)
        {
            
            this.screenProperties = screenProperties;
            this.tag = tag;
        }
        
        public void startLocalElement(String name, Attributes attributes) throws SAXException
        {
            if (name.equals(tag))
            {
                
            }
            else if (name.equals(ELEMENT_SCREEN_ITEM))
            {
               
                setDelegate(new ScreenItemHandler(screenProperties));
                return;
            }
            
        }
        
        public void endLocalElement(String name, String value, String untrimmedValue)
        {
            if (name.equals(tag))
            {
                quitAsDelegate();
                return;
            }
            else if (name.equals(ELEMENT_SCREEN_WIDTH))
            {
                screenProperties.setWidth(Integer.parseInt(value));
            }
            else if (name.equals(ELEMENT_SCREEN_HEIGHT))
            {
                screenProperties.setHeight(Integer.parseInt(value));
            }
            
        }
        
    }
    
}
