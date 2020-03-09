/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.reports.reader;

import org.entirej.framework.plugin.framework.properties.reader.EntireJTagHandler;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ItemHandler extends EntireJTagHandler
{
    private EJPluginReportItemProperties  _itemProperties;
    private EJPluginReportProperties      _formProperties;
    private EJPluginReportBlockProperties _blockProperties;
    
    private static final String           ELEMENT_ITEM                 = "item";
    private static final String           ELEMENT_BLOCK_SERVICE_ITEM   = "blockServiceItem";
    private static final String           ELEMENT_DATA_TYPE_CLASS_NAME = "dataTypeClassName";
    private static final String           ELEMENT_DEFAULT_QUERY_VALUE  = "defaultQueryValue";
    
    public ItemHandler(EJPluginReportBlockProperties blockProperties)
    {
        _formProperties = blockProperties.getReportProperties();
        _blockProperties = blockProperties;
        
        _itemProperties = new EJPluginReportItemProperties(blockProperties);
        _itemProperties.setBlockServiceItem(!_blockProperties.isControlBlock());
        
    }
    
    public EJPluginReportItemProperties getItemProperties()
    {
        return _itemProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_ITEM))
        {
            _itemProperties.setName(attributes.getValue("name"));
        }
        
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_ITEM))
        {
            quitAsDelegate();
            return;
        }
        
        else if (name.equals(ELEMENT_BLOCK_SERVICE_ITEM))
        {
            _itemProperties.setBlockServiceItem(Boolean.parseBoolean(value));
        }
        
        else if (name.equals(ELEMENT_DATA_TYPE_CLASS_NAME))
        {
            _itemProperties.setDataTypeClassName(value);
        }
        
        else if (name.equals(ELEMENT_DEFAULT_QUERY_VALUE))
        {
            _itemProperties.setDefaultQueryValue(value);
        }
    }
    
    @Override
    protected void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        
    }
    
}
