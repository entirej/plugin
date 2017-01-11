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

import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.enumerations.EJRendererType;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class EntireJPropertiesHandler extends EntireJTagHandler
{
    private EJPluginEntireJProperties _properties;
                                      
    private static final String       VERSION                                 = "version";
    private static final String       FRAMEWORK                               = "entirejFramework";
    private static final String       APPLICATION_MANAGER_DEFCLASS_NAME       = "applicationManagerDefinition";
    private static final String       REUSABLE_BLOCK_LOC                      = "reusableBlocksLocation";
    private static final String       REUSABLE_LOV_LOV                        = "reusableLovDefinitionLocation";
    private static final String       OBJECT_GROUP_LOV                        = "objectGroupDefinitionLocation";
                                                                              
    private static final String       APPLICATION_PROPERTIES                  = "applicationDefinedProperties";
                                                                              
    private static final String       CONNECTION_FACTORY_CLASS_NAME           = "connectionFactoryClassName";
                                                                              
    protected static final String     APPLICATION_ACTION_PROCESSOR_CLASS_NAME = "applicationActionProcessor";
    private static final String       TRANSLATOR_CLASS_NAME                   = "translatorClassName";
    private static final String       APPLICATION_LEVEL_PARAMETER             = "appicationLevelParameter";
    private static final String       FORMS_PACKAGE                           = "formsPackage";
                                                                              
    private static final String       FORM_RENDERERS                          = "formRenderers";
    private static final String       BLOCK_RENDERERS                         = "blockRenderers";
    private static final String       ITEM_RENDERERS                          = "itemRenderers";
    private static final String       LOV_RENDERERS                           = "lovRenderers";
    private static final String       MENU_RENDERERS                          = "menuRenderers";
    private static final String       APP_COMP_RENDERERS                      = "appCompRenderers";
    private static final String       RENDERER                                = "renderer";
    private static final String       APP_MENU                                = "applicationMenu";
                                                                              
    private static final String       VISUAL_ATTRIBUTE                        = "visualAttribute";
    private static final String       APP_LAYOUT                              = "applicationLayout";
                                                                              
    private boolean                   _selectingFormRenderers                 = false;
    private boolean                   _selectingMenuRenderers                 = false;
    private boolean                   _selectingAppCompRenderers              = false;
    private boolean                   _selectingBlockRenderers                = false;
    private boolean                   _selectingItemRenderers                 = false;
    private boolean                   _selectingLovRenderers                  = false;
                                                                              
    public EntireJPropertiesHandler(EJPluginEntireJProperties properties)
    {
        
        _properties = properties;
        
        _properties.getItemRendererContainer().clear();
        _properties.getBlockRendererContainer().clear();
        _properties.getFormRendererContainer().clear();
        _properties.getLovRendererContainer().clear();
        _properties.getAllApplicationLevelParameters().clear();
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
        
        if (name.equals(APPLICATION_LEVEL_PARAMETER))
        {
            String paramName = attributes.getValue("name");
            String dataTypeName = attributes.getValue("dataType");
            String defaultValue = attributes.getValue("defaultValue");
            
            _properties.addApplicationLevelParameter(new EJPluginApplicationParameter(paramName, dataTypeName, defaultValue));
        }
        else if (name.equals(FORMS_PACKAGE))
        {
            _properties.getFormPackageNames().add(attributes.getValue("name"));
        }
        else if (name.equals(FORM_RENDERERS))
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
        else if (name.equals(VISUAL_ATTRIBUTE))
        {
            setDelegate(new VisualAttributeHandler());
        }
        else if (name.equals(APPLICATION_PROPERTIES))
        {
            setDelegate(new FrameworkExtensionPropertiesHandler(null, null, APPLICATION_PROPERTIES));
        }
        else if (name.equals(APP_MENU))
        {
            setDelegate(new MenuHandler(_properties));
        }
        else if (name.equals(APP_LAYOUT))
        {
            AppLayoutHandler appLayoutHandler = new AppLayoutHandler(_properties);
            
            setDelegate(appLayoutHandler);
        }
    }
    
    @Override
    public void endLocalElement(String name, String value, String untrimmedValue) throws SAXException
    {
        
        if (name.endsWith(FRAMEWORK))
        {
            // ignore
        }
        else if (name.equals(VERSION))
        {
            _properties.setVersion(value);
        }
        else if (name.equals(APPLICATION_MANAGER_DEFCLASS_NAME))
        {
            _properties.setApplicationManagerDefinitionClassName(value);
        }
        else if (name.equals(CONNECTION_FACTORY_CLASS_NAME))
        {
            _properties.setConnectionFactoryClassName(value);
        }
        else if (name.equals(APPLICATION_ACTION_PROCESSOR_CLASS_NAME))
        {
            _properties.setApplicationActionProcessorClassName(value);
        }
        else if (name.equals(TRANSLATOR_CLASS_NAME))
        {
            _properties.setTranslatorClassName(value);
        }
        else if (name.equals(REUSABLE_BLOCK_LOC))
        {
            _properties.setReusableBlocksLocation(value);
        }
        else if (name.equals(REUSABLE_LOV_LOV))
        {
            _properties.setReusableLovDefinitionLocation(value);
        }
        else if (name.equals(OBJECT_GROUP_LOV))
        {
            _properties.setObjectGroupDefinitionLocation(value);
        }
        else if (name.equals(FORMS_PACKAGE))
        {
            if (value != null && value.trim().length() > 0)
            {
                _properties.getFormPackageNames().add(value);
            }
        }
        else if (name.equals(FORM_RENDERERS))
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
        if (name.equals(VISUAL_ATTRIBUTE))
        {
            _properties.getVisualAttributesContainer().addVisualAttribute(((VisualAttributeHandler) currentDelegate).getProperties());
        }
        else if (name.equals(APP_MENU))
        {
            _properties.getPluginMenuContainer().addMenuProperties(((MenuHandler) currentDelegate).getProperties());
        }
        else if (name.equals(APP_LAYOUT))
        {
            _properties.setLayoutContainer(((AppLayoutHandler) currentDelegate).getContainer());
        }
        else if (name.equals(APPLICATION_PROPERTIES))
        {
            if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
            {
                EJApplicationDefinition applicationDefinition = ExtensionsPropertiesFactory.loadApplicationManager(_properties,
                        _properties.getApplicationManagerDefinitionClassName());
                if (applicationDefinition != null)
                {
                    _properties.setApplicationDefinedProperties(((FrameworkExtensionPropertiesHandler) currentDelegate)
                            .getMainPropertiesGroup(applicationDefinition.getApplicationPropertyDefinitionGroup()));
                }
                else
                {
                    _properties.setApplicationDefinedProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
                }
            }
        }
    }
    
    private void addFormRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        String rendererClassName = attributes.getValue("rendererClassName");
        EJPluginRenderer def = new EJPluginRenderer(_properties, name, EJRendererType.FORM, rendererDefClassName, rendererClassName);
        
        _properties.getFormRendererContainer().addRendererAssignment(def);
    }
    
    private void addBlockRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        String rendererClassName = attributes.getValue("rendererClassName");
        
        EJPluginRenderer def = new EJPluginRenderer(_properties, name, EJRendererType.BLOCK, rendererDefClassName, rendererClassName);
        
        _properties.getBlockRendererContainer().addRendererAssignment(def);
    }
    
    private void addItemRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        String rendererClassName = attributes.getValue("rendererClassName");
        
        EJPluginRenderer def = new EJPluginRenderer(_properties, name, EJRendererType.ITEM, rendererDefClassName, rendererClassName);
        
        _properties.getItemRendererContainer().addRendererAssignment(def);
    }
    
    private void addLovRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        String rendererClassName = attributes.getValue("rendererClassName");
        
        EJPluginRenderer def = new EJPluginRenderer(_properties, name, EJRendererType.LOV, rendererDefClassName, rendererClassName);
        
        _properties.getLovRendererContainer().addRendererAssignment(def);
    }
    
    private void addMenuRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        String rendererClassName = attributes.getValue("rendererClassName");
        
        EJPluginRenderer def = new EJPluginRenderer(_properties, name, EJRendererType.MENU, rendererDefClassName, rendererClassName);
        
        _properties.getMenuRendererContainer().addRendererAssignment(def);
    }
    
    private void addAppCompRenderer(Attributes attributes)
    {
        String name = attributes.getValue("name");
        String rendererDefClassName = attributes.getValue("rendererDefinitionClassName");
        String rendererClassName = attributes.getValue("rendererClassName");
        
        EJPluginRenderer def = new EJPluginRenderer(_properties, name, EJRendererType.MENU, rendererDefClassName, rendererClassName);
        
        _properties.getAppComponentRendererContainer().addRendererAssignment(def);
    }
}
