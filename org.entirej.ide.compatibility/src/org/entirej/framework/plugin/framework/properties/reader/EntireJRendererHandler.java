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

import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class EntireJRendererHandler extends EntireJTagHandler
{
    private EJPluginEntireJProperties _properties;
    
    private static final String       FORM_RENDERERS             = "formRenderers";
    private static final String       BLOCK_RENDERERS            = "blockRenderers";
    private static final String       ITEM_RENDERERS             = "itemRenderers";
    private static final String       LOV_RENDERERS              = "lovRenderers";
    private static final String       MENU_RENDERERS             = "menuRenderers";
    private static final String       APP_COMP_RENDERERS         = "appCompRenderers";
    private static final String       RENDERER                   = "renderer";
    
    private boolean                   _selectingFormRenderers    = false;
    private boolean                   _selectingMenuRenderers    = false;
    private boolean                   _selectingAppCompRenderers = false;
    private boolean                   _selectingBlockRenderers   = false;
    private boolean                   _selectingItemRenderers    = false;
    private boolean                   _selectingLovRenderers     = false;
    
    public EntireJRendererHandler(EJPluginEntireJProperties properties)
    {
        _properties = properties;
        
    }
    
    public void dispose()
    {
        _properties = null;
    }
    
    public EJPluginEntireJProperties getProperties()
    {
        return _properties;
    }
    
    @Override
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(RENDERER))
        {
            if (_selectingFormRenderers)
            {
                addFormRenderer(attributes);
                return;
            }
            else if (_selectingBlockRenderers)
            {
                addBlockRenderer(attributes);
                return;
            }
            else if (_selectingItemRenderers)
            {
                addItemRenderer(attributes);
                return;
            }
            else if (_selectingLovRenderers)
            {
                addLovRenderer(attributes);
                return;
            }
            else if (_selectingMenuRenderers)
            {
                addMenuRenderer(attributes);
                return;
            }
            else if (_selectingAppCompRenderers)
            {
                addAppCompRenderer(attributes);
                return;
            }
        }
        
        if (name.equals(FORM_RENDERERS))
        {
            _selectingFormRenderers = true;
        }
        else if (name.equals(BLOCK_RENDERERS))
        {
            _selectingBlockRenderers = true;
        }
        else if (name.equals(ITEM_RENDERERS))
        {
            _selectingItemRenderers = true;
        }
        else if (name.equals(LOV_RENDERERS))
        {
            _selectingLovRenderers = true;
        }
        else if (name.equals(MENU_RENDERERS))
        {
            _selectingMenuRenderers = true;
        }
        else if (name.equals(APP_COMP_RENDERERS))
        {
            _selectingAppCompRenderers = true;
        }
        
    }
    
    @Override
    public void endLocalElement(String name, String value, String untrimmedValue) throws SAXException
    {
        
        if (name.equals(FORM_RENDERERS))
        {
            _selectingFormRenderers = false;
        }
        else if (name.equals(BLOCK_RENDERERS))
        {
            _selectingBlockRenderers = false;
        }
        else if (name.equals(ITEM_RENDERERS))
        {
            _selectingItemRenderers = false;
        }
        else if (name.equals(LOV_RENDERERS))
        {
            _selectingLovRenderers = false;
        }
        else if (name.equals(MENU_RENDERERS))
        {
            _selectingMenuRenderers = false;
        }
        else if (name.equals(APP_COMP_RENDERERS))
        {
            _selectingAppCompRenderers = false;
        }
    }
    
    @Override
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        
    }
    
    private void addFormRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        
        if (name != null && rendererDefClassName != null)
        {
            EJPluginRenderer def = _properties.getFormRendererContainer().getRenderer(name);
            if (def != null) def.setRendererDefinitionClassName(rendererDefClassName, false);
        }
    }
    
    private void addBlockRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        
        if (name != null && rendererDefClassName != null)
        {
            EJPluginRenderer def = _properties.getBlockRendererContainer().getRenderer(name);
            if (def != null) def.setRendererDefinitionClassName(rendererDefClassName, false);
        }
    }
    
    private void addItemRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        
        if (name != null && rendererDefClassName != null)
        {
            EJPluginRenderer def = _properties.getItemRendererContainer().getRenderer(name);
            if (def != null) def.setRendererDefinitionClassName(rendererDefClassName, false);
        }
    }
    
    private void addLovRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        
        if (name != null && rendererDefClassName != null)
        {
            EJPluginRenderer def = _properties.getLovRendererContainer().getRenderer(name);
            if (def != null) def.setRendererDefinitionClassName(rendererDefClassName, false);
        }
    }
    
    private void addMenuRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        
        if (name != null && rendererDefClassName != null)
        {
            EJPluginRenderer def = _properties.getMenuRendererContainer().getRenderer(name);
            if (def != null) def.setRendererDefinitionClassName(rendererDefClassName, false);
        }
    }
    
    private void addAppCompRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        if (name != null && rendererDefClassName != null)
        {
            EJPluginRenderer def = _properties.getAppComponentRendererContainer().getRenderer(name);
            if (def != null) def.setRendererDefinitionClassName(rendererDefClassName, false);
        }
    }
}
