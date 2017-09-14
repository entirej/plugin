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

import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginControlBlockItemProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ItemHandler extends EntireJTagHandler
{
    private EJPluginBlockItemProperties _itemProperties;
    private EJFormProperties            _formProperties;
    private EJBlockProperties           _blockProperties;
    
    private static final String         ELEMENT_ITEM                     = "item";
    private static final String         ELEMENT_BLOCK_SERVICE_ITEM       = "blockServiceItem";
    private static final String         ELEMENT_DATA_TYPE_CLASS_NAME     = "dataTypeClassName";
    private static final String         ELEMENT_MANDATORY_ITEM           = "mandatoryItem";
    private static final String         ELEMENT_ITEM_RENDERER            = "itemRendererName";
    private static final String         ELEMENT_DEFAULT_INSERT_VALUE     = "defaultInsertValue";
    private static final String         ELEMENT_DEFAULT_QUERY_VALUE      = "defaultQueryValue";
    private static final String         ELEMENT_ITEM_RENDERER_PROPERTIES = "itemRendererProperties";
    
    public ItemHandler(EJPluginBlockProperties blockProperties)
    {
        _formProperties = blockProperties.getFormProperties();
        _blockProperties = blockProperties;
        
        if (blockProperties.isControlBlock())
        {
            _itemProperties = new EJPluginControlBlockItemProperties(blockProperties);
        }
        else
        {
            _itemProperties = new EJPluginBlockItemProperties(blockProperties, false);
        }
    }
    
    public EJPluginBlockItemProperties getItemProperties()
    {
        return _itemProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_ITEM))
        {
            _itemProperties.setName(attributes.getValue("name"));
        }
        else if (name.equals(ELEMENT_ITEM_RENDERER_PROPERTIES))
        {
            // Now I am starting the selection of the renderer properties
            setDelegate(new FrameworkExtensionPropertiesHandler(_formProperties, _blockProperties, ELEMENT_ITEM_RENDERER_PROPERTIES));
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
        else if (name.equals(ELEMENT_MANDATORY_ITEM))
        {
            _itemProperties.setMandatoryItem(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_DATA_TYPE_CLASS_NAME))
        {
            _itemProperties.setDataTypeClassName(value);
        }
        else if (name.equals(ELEMENT_ITEM_RENDERER))
        {
            _itemProperties.setItemRendererName(value, false);
        }
        else if (name.equals(ELEMENT_DEFAULT_INSERT_VALUE))
        {
            _itemProperties.setDefaultInsertValue(value);
        }
        else if (name.equals(ELEMENT_DEFAULT_QUERY_VALUE))
        {
            _itemProperties.setDefaultQueryValue(value);
        }
    }
    
    @Override
    protected void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_ITEM_RENDERER_PROPERTIES))
        {
            if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null )
            {
               if(_itemProperties.getItemRendererDefinition() != null)
               {
                   _itemProperties.setItemRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup(_itemProperties.getItemRendererDefinition().getItemPropertyDefinitionGroup()));
               }
               else
               {
                   _itemProperties.setItemRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
               }
                
                
            }
        }
    }
    
}
