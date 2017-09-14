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

import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginControlBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class LovDefinitionHandler extends EntireJTagHandler
{
    private FrameworkExtensionPropertiesHandler _lovRendererHandler;
    private EJPluginLovDefinitionProperties     _lovDefinitionProperties;
    private EJPluginFormProperties              _formProperties;
    
    private static final String                 ELEMENT_LOV_DEF             = "lovDefinition";
    private static final String                 ELEMENT_WIDTH               = "width";
    private static final String                 ELEMENT_HEIGHT              = "height";
    
    private static final String                 ELEMENT_AUTOMATIC_REFRESH   = "automaticRefresh";
    private static final String                 ELEMENT_RENDERER_PROPERTIES = "lovRendererProperties";
    private static final String                 ELEMENT_BLOCK               = "block";
    private static final String                 ELEMENT_ITEM                = "item";
    private static final String                 ELEMENT_ACTION_PROCESSOR    = "actionProcessorClassName";
    
    private EJPluginBlockProperties             _formLovProperties;
    
    private boolean                             _isReferenced               = false;
    
    public BlockHandler createNewBlockHandler(EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties lovDefinitionProperties)
    {
        return new BlockHandler(formProperties, lovDefinitionProperties);
    }
    
    public LovDefinitionHandler(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
    }
    
    public EJPluginLovDefinitionProperties getLovDefinitionProperties()
    {
        return _lovDefinitionProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_LOV_DEF))
        {
            String defName = attributes.getValue("name");
            String isReferenced = attributes.getValue("isReferenced");
            
            if (Boolean.parseBoolean(isReferenced))
            {
                String refName = attributes.getValue("referencedLovDefinitionName");
                try
                {
                    _isReferenced = true;
                    if (_formProperties.getEntireJProperties().getReusableLovDefinitionLocation() == null)
                    {
                        throw new SAXException("A reusable lov definition called " + defName
                                + " needs to be loaded but no Reusable Lov Definition Location has been defined within the application.ejprop file.");
                    }
                    
                    EJPluginLovDefinitionProperties reusableLovDefinitionProperties = _formProperties.getEntireJProperties()
                            .getReusableLovDefinitionProperties(refName);
                    if (reusableLovDefinitionProperties != null)
                    {
                        _lovDefinitionProperties = reusableLovDefinitionProperties.makeCopy(defName);
                        _lovDefinitionProperties.setReferencedLovDefinitionName(refName);
                        _lovDefinitionProperties.setIsReferenced(true);
                        _formLovProperties = _lovDefinitionProperties.getBlockProperties().makeCopy("forLov", false);
                    }
                    else
                    {
                        // creatae dummy
                        _lovDefinitionProperties = new EJPluginLovDefinitionProperties(defName, _formProperties);
                        _lovDefinitionProperties.setReferencedLovDefinitionName(refName);
                        _formLovProperties = new EJPluginControlBlockProperties(_formProperties, refName);
                        _lovDefinitionProperties.setBlockProperties(_formLovProperties);
                        _lovDefinitionProperties.setIsReferenced(true);
                    }
                    
                }
                catch (EJDevFrameworkException e)
                {
                    // creatae dummy
                    _lovDefinitionProperties = new EJPluginLovDefinitionProperties(defName, _formProperties);
                    _lovDefinitionProperties.setReferencedLovDefinitionName(refName);
                    _formLovProperties = new EJPluginControlBlockProperties(_formProperties, refName);
                    _lovDefinitionProperties.setBlockProperties(_formLovProperties);
                    _lovDefinitionProperties.setIsReferenced(true);
                }
            }
            else
            {
                _isReferenced = false;
                
                String rendererName = attributes.getValue("rendererName");
                
                _lovDefinitionProperties = new EJPluginLovDefinitionProperties(defName, _formProperties);
                _lovDefinitionProperties.setLovRendererName(rendererName, false);
                
                if (isReferenced != null)
                {
                    _lovDefinitionProperties.setIsReferenced(Boolean.parseBoolean(isReferenced));
                }
            }
        }
        else if (name.equals(ELEMENT_BLOCK))
        {
            if (!_isReferenced)
            {
                setDelegate(createNewBlockHandler(_formProperties, _lovDefinitionProperties));
            }
        }
        else if (name.equals(ELEMENT_RENDERER_PROPERTIES))
        {
            // Now I am starting the selection of the renderer properties
            _lovRendererHandler = new FrameworkExtensionPropertiesHandler(_formProperties, null, ELEMENT_RENDERER_PROPERTIES);
            setDelegate(_lovRendererHandler);
        }
        else if (name.equals(ELEMENT_ITEM))
        {
            if (_isReferenced)
            {
                setDelegate(new ItemHandler(_formLovProperties));
                return;
            }
        }
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_LOV_DEF))
        {
            quitAsDelegate();
            return;
        }
        
        if (!_isReferenced)
        {
            if (name.equals(ELEMENT_HEIGHT))
            {
                if (value.length() > 0)
                {
                    _lovDefinitionProperties.setHeight(Integer.parseInt(value.trim()));
                }
            }
            else if (name.equals(ELEMENT_WIDTH))
            {
                if (value.length() > 0)
                {
                    _lovDefinitionProperties.setWidth(Integer.parseInt(value.trim()));
                }
            }
            else if (name.equals(ELEMENT_AUTOMATIC_REFRESH))
            {
                if (value.length() > 0)
                {
                    _lovDefinitionProperties.setAutomaticRefresh(Boolean.parseBoolean(value.trim()));
                }
            }
            else if (name.equals(ELEMENT_ACTION_PROCESSOR))
            {
                _lovDefinitionProperties.setActionProcessorClassName(value);
            }
        }
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_BLOCK))
        {
            _lovDefinitionProperties.setBlockProperties(((BlockHandler) currentDelegate).getBlockProperties());
        }
        else if (name.equals(ELEMENT_RENDERER_PROPERTIES))
        {
            if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
            {
                if (_lovDefinitionProperties.getRendererDefinition() != null)
                {
                    _lovDefinitionProperties.setLovRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate)
                            .getMainPropertiesGroup(_lovDefinitionProperties.getRendererDefinition().getLovPropertyDefinitionGroup()));
                }
                else
                {
                    _lovDefinitionProperties.setLovRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
                }
            }
        }
        // I am only interested in the default query values for Lov Items as
        // these can be overridden within the form
        if (name.equals(ELEMENT_ITEM))
        {
            EJPluginBlockItemProperties itemProperties = ((ItemHandler) currentDelegate).getItemProperties();
            if (itemProperties == null)
            {
                return;
            }
            
            // If the item name is null, then this item is for a screen item and
            // should be ignored
            if (itemProperties.getName() == null)
            {
                return;
            }
            
            EJPluginBlockItemProperties lovItemProps = _lovDefinitionProperties.getBlockProperties().getItemContainer()
                    .getItemProperties(itemProperties.getName());
            
            if (lovItemProps != null)
            {
                if (itemProperties.getDefaultQueryValue() != null && itemProperties.getDefaultQueryValue().trim().length() > 0)
                {
                    lovItemProps.setDefaultQueryValue(itemProperties.getDefaultQueryValue());
                }
            }
            
            return;
        }
    }
}
