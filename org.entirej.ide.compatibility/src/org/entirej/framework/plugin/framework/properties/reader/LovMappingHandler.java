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
package org.entirej.framework.plugin.framework.properties.reader;

import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LovMappingHandler extends EntireJTagHandler
{
    private EJPluginFormProperties       _formProperties;
    private EJPluginBlockProperties      _blockProperties;
    private EJPluginLovMappingProperties _lovMappingProperties;
    
    private static final String          ELEMENT_LOV_MAPPING  = "lovMapping";
    private static final String          ELEMENT_ITEM_MAP     = "itemMap";
    private static final String          ELEMENT_DISPLAY_NAME = "displayName";
    
    public LovMappingHandler(EJPluginFormProperties formProperties, EJPluginBlockProperties blockProperties)
    {
        _formProperties = formProperties;
        _blockProperties = blockProperties;
    }
    
    public EJPluginLovMappingProperties getLovMappingProperties()
    {
        return _lovMappingProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_LOV_MAPPING))
        {
            String mappingName = attributes.getValue("name");
            String lovDefinitionName = attributes.getValue("lovDefinitionName");
            String executeAfterQuery = attributes.getValue("executeAfterQuery");
            String includeDefaultQueryValues = attributes.getValue("includeDefaultQueryValues");
            
            _lovMappingProperties = new EJPluginLovMappingProperties(mappingName, _formProperties);
            
            _lovMappingProperties.setExecuteAfterQuery((executeAfterQuery == null ? Boolean.TRUE.booleanValue() : Boolean.parseBoolean(executeAfterQuery)));
            _lovMappingProperties.setIncludeDefaultQueryValues((includeDefaultQueryValues == null ? Boolean.FALSE.booleanValue() : Boolean.parseBoolean(includeDefaultQueryValues)));
            _lovMappingProperties.setLovDefinitionName(lovDefinitionName);
            _lovMappingProperties.setMappedBlock(_blockProperties);
            
        }
        else if (name.equals(ELEMENT_ITEM_MAP))
        {
            String blockItemName = attributes.getValue("blockItemName");
            String lovDefItemName = attributes.getValue("lovDefinitionItem");
            
            _lovMappingProperties.addMappingItem(_lovMappingProperties.createMappingProperties(lovDefItemName, blockItemName));
        }
        
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_LOV_MAPPING))
        {
            quitAsDelegate();
            return;
        }
        
        if (name.equals(ELEMENT_DISPLAY_NAME))
        {
            _lovMappingProperties.setLovDisplayName(value);
        }
    }
}
