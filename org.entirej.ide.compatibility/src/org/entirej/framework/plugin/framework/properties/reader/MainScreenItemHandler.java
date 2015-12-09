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
package org.entirej.framework.plugin.framework.properties.reader;

import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenSpacerItemProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MainScreenItemHandler extends EntireJTagHandler
{
    private EJPluginMainScreenItemProperties _itemProperties;
    
    private static final String              ELEMENT_ITEM                      = "item";
    private static final String              ELEMENT_LABEL                     = "label";
    private static final String              ELEMENT_HINT                      = "hint";
    private static final String              ELEMENT_LOV_MAPPING_NAME          = "lovMappingName";
    private static final String              ELEMENT_EDIT_ALLOWED              = "editAllowed";
    private static final String              ELEMENT_VISIBLE                   = "visible";
    private static final String              ELEMENT_MANDATORY                 = "mandatory";
    private static final String              ELEMENT_ENABLE_LOV_NOTIFICATION   = "enableLovNotification";
    private static final String              ELEMENT_VALIDATE_FROM_LOV         = "validateFromLov";
    private static final String              ELEMENT_ACTION_COMMAND            = "actionCommand";
    private static final String              ELEMENT_BLOCK_RENDERER_PROPERTIES = "blockRendererRequiredProperties";
    private static final String              ELEMENT_LOV_RENDERER_PROPERTIES   = "lovRendererRequiredProperties";
    
    private EJPluginItemGroupProperties      _itemGroupProperties;
    
    public MainScreenItemHandler(EJPluginItemGroupProperties itemGroupProperties)
    {
        _itemGroupProperties = itemGroupProperties;
    }
    
    public EJPluginMainScreenItemProperties getItemProperties()
    {
        return _itemProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_ITEM))
        {
            String referencedItemName = attributes.getValue("referencedItemName");
            String isSpacerItem = attributes.getValue("isSpacerItem");
            
            if (isSpacerItem != null && Boolean.parseBoolean(isSpacerItem))
            {
                _itemProperties = new EJPluginMainScreenSpacerItemProperties(_itemGroupProperties, false);
                _itemProperties.setReferencedItemName(referencedItemName);
                String isSeparator = attributes.getValue("isSeparator");
                if (isSeparator != null && Boolean.parseBoolean(isSeparator))
                {
                    _itemProperties.setSeparator(true);
                }
                
                String linestyle = attributes.getValue("separatorLineStyle");
                if (linestyle != null )
                {
                    _itemProperties.setSeparatorLineStyle(EJLineStyle.valueOf(linestyle));
                }
                String separatorOrientation = attributes.getValue("separatorOrientation");
                if (separatorOrientation != null )
                {
                    _itemProperties.setSeparatorOrientation(EJSeparatorOrientation.valueOf(separatorOrientation));
                }
            }
            else
            {
                _itemProperties = new EJPluginMainScreenItemProperties(_itemGroupProperties, false, false);
                _itemProperties.setReferencedItemName(referencedItemName);
            }
        }
        else if (name.equals(ELEMENT_BLOCK_RENDERER_PROPERTIES))
        {
            // Now I am starting the selection of the block required renderer
            // properties
            setDelegate(new FrameworkExtensionPropertiesHandler(_itemProperties.getBlockProperties().getFormProperties(),
                    _itemGroupProperties.getBlockProperties(), ELEMENT_BLOCK_RENDERER_PROPERTIES));
        }
        else if (name.equals(ELEMENT_LOV_RENDERER_PROPERTIES))
        {
            // Now I am starting the selection of the lov required renderer
            // properties
            setDelegate(new FrameworkExtensionPropertiesHandler(_itemProperties.getBlockProperties().getFormProperties(),
                    _itemGroupProperties.getBlockProperties(), ELEMENT_LOV_RENDERER_PROPERTIES));
        }
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_ITEM))
        {
            quitAsDelegate();
            return;
        }
        
        if (name.equals(ELEMENT_LABEL))
        {
            _itemProperties.setLabel(value);
        }
        else if (name.equals(ELEMENT_HINT))
        {
            _itemProperties.setHint(value);
        }
        else if (name.equals(ELEMENT_LOV_MAPPING_NAME))
        {
            if (value.length() > 0)
            {
                _itemProperties.setLovMappingName(value);
            }
        }
        else if (name.equals(ELEMENT_EDIT_ALLOWED))
        {
            if (value.length() > 0)
            {
                _itemProperties.setEditAllowed(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_VISIBLE))
        {
            if (value.length() > 0)
            {
                _itemProperties.setVisible(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_MANDATORY))
        {
            if (value.length() > 0)
            {
                _itemProperties.setMandatory(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_VALIDATE_FROM_LOV))
        {
            if (value.length() > 0)
            {
                _itemProperties.setValidateFromLov(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_ENABLE_LOV_NOTIFICATION))
        {
            if (value.length() > 0)
            {
                _itemProperties.enableLovNotification(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_ACTION_COMMAND))
        {
            _itemProperties.setActionCommand(value);
        }
    }
    
    @Override
    protected void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_BLOCK_RENDERER_PROPERTIES))
        {
            if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
            {
                if(_itemProperties.getBlockProperties().getBlockRendererDefinition() != null
                        && _itemProperties.getBlockProperties().getBlockRendererDefinition().getItemPropertiesDefinitionGroup()!= null)
                {
                    _itemProperties.setBlockRendererRequiredProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup(_itemProperties.getBlockProperties().getBlockRendererDefinition().getItemPropertiesDefinitionGroup()));
                }
                else
                _itemProperties.setBlockRendererRequiredProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
            }
        }
        else if (name.equals(ELEMENT_LOV_RENDERER_PROPERTIES))
        {
            if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
            {
                _itemProperties.setLovRendererRequiredProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
            }
        }
    }
    
}
