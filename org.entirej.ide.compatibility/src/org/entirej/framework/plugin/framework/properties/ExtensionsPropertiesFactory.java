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
package org.entirej.framework.plugin.framework.properties;

import java.util.Iterator;

import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperties;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperty;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionPropertyList;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionPropertyListEntry;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyList;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyListEntry;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionList;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJFormProperties;
import org.entirej.framework.core.properties.interfaces.EJLovDefinitionProperties;
import org.entirej.framework.core.renderers.definitions.interfaces.EJBlockRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJFormRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJItemRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevAppComponentRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevFormRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevMenuRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.EJPluginParameterChecker;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class ExtensionsPropertiesFactory
{
    
    private ExtensionsPropertiesFactory()
    {
        throw new AssertionError();
    }
    
    public static EJFrameworkExtensionProperties createApplicationProperties(EJPluginEntireJProperties entireJProperties, boolean addDefaultValues)
    {
        
        EJPluginParameterChecker.checkNotNull(entireJProperties, "createApplicationProperties", "entireJProperties");
        
        EJApplicationDefinition appDefinition = entireJProperties.getApplicationManager();
        if (appDefinition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = appDefinition.getApplicationPropertyDefinitionGroup();
        
        EJFrameworkExtensionProperties props = addExtensionProperties(null, null, definitionGroup, null, addDefaultValues);
        
        return props;
    }
    
    /**
     * Creates a <code>EJFrameworkExtensionProperties</code> for the form
     * renderer within the given form properties
     * <p>
     * 
     * @return The <code>EJFrameworkExtensionProperties</code> containing all
     *         form renderer properties renderer
     * @throws IllegalArgumentException
     *             if there is no renderer with the name defined within the form
     *             properties
     * @throws NullPointerException
     *             if the given name is null
     */
    public static EJFrameworkExtensionProperties createFormRendererProperties(EJPluginFormProperties formProperties, boolean addDefaultValues)
    {
        
        EJPluginParameterChecker.checkNotNull(formProperties, "createFormRendererProperties", "formProperties");
        
        EJFormRendererDefinition rendererDefinition = loadFormRendererDefinition(formProperties.getEntireJProperties(), formProperties.getFormRendererName());
        if (rendererDefinition == null)
        {
            
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = rendererDefinition.getFormPropertyDefinitionGroup();
        
        EJFrameworkExtensionProperties props = addExtensionProperties(formProperties, null, definitionGroup, null, addDefaultValues);
        
        return props;
    }
    
    public static EJFrameworkExtensionProperties createBlockRendererSpacerItemProperties(EJPluginBlockProperties blockProperties, boolean addDefaultValues,
            EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createBlockRendererSpacerItemProperties", "blockProperties");
        if (blockProperties.getBlockRendererName() == null || blockProperties.getBlockRendererName().trim().length() == 0)
        {
            return null;
        }
        
        EJDevBlockRendererDefinition renderer = loadBlockRendererDefinition(blockProperties.getEntireJProperties(), blockProperties.getBlockRendererName());
        if (renderer == null)
        {
            
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = renderer.getSpacerItemPropertiesDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(blockProperties.getFormProperties(), blockProperties,
                definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createQueryScreenRendererSpacerItemProperties(EJPluginBlockProperties blockProperties, boolean addDefaultValues)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createQueryScreenRendererSpacerItemProperties", "blockProperties");
        
        EJDevQueryScreenRendererDefinition definition = blockProperties.getQueryScreenRendererDefinition();
        if (definition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = definition.getSpacerItemPropertyDefinitionGroup();
        return addExtensionProperties(blockProperties.getFormProperties(), blockProperties, definitionGroup, null, addDefaultValues);
    }
    
    public static EJFrameworkExtensionProperties createInsertScreenRendererSpacerItemProperties(EJPluginBlockProperties blockProperties,
            boolean addDefaultValues, EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createInsertScreenRendererSpacerItemProperties", "blockProperties");
        EJDevInsertScreenRendererDefinition definition = blockProperties.getInsertScreenRendererDefinition();
        if (definition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = definition.getSpacerItemPropertyDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(blockProperties.getFormProperties(), blockProperties,
                definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createUpdateScreenRendererSpacerItemProperties(EJPluginBlockProperties blockProperties,
            boolean addDefaultValues)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createUpdateScreenRendererSpacerItemProperties", "blockProperties");
        EJDevUpdateScreenRendererDefinition definition = blockProperties.getUpdateScreenRendererDefinition();
        if (definition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = definition.getSpacerItemPropertyDefinitionGroup();
        return addExtensionProperties(blockProperties.getFormProperties(), blockProperties, definitionGroup, null, addDefaultValues);
    }
    
    public static EJFrameworkExtensionProperties createQueryScreenRendererProperties(EJPluginBlockProperties blockProperties, boolean addDefaultValues,
            EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createQueryScreenRendererProperties", "blockProperties");
        EJDevQueryScreenRendererDefinition definition = blockProperties.getQueryScreenRendererDefinition();
        if (definition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = definition.getQueryScreenPropertyDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(blockProperties.getFormProperties(), blockProperties,
                definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createQueryScreenRendererItemProperties(EJPluginBlockProperties blockProperties, boolean addDefaultValues,
            EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createQueryScreenRendererItemProperties", "blockProperties");
        EJDevQueryScreenRendererDefinition definition = blockProperties.getQueryScreenRendererDefinition();
        if (definition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = definition.getItemPropertyDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(blockProperties.getFormProperties(), blockProperties,
                definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createInsertScreenRendererItemProperties(EJPluginBlockProperties blockProperties, boolean addDefaultValues,
            EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createInsertScreenRendererItemProperties", "blockProperties");
        
        EJDevInsertScreenRendererDefinition definition = blockProperties.getInsertScreenRendererDefinition();
        if (definition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = definition.getItemPropertyDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(blockProperties.getFormProperties(), blockProperties,
                definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createUpdateScreenRendererItemProperties(EJPluginBlockProperties blockProperties, boolean addDefaultValues,
            EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createUpdateScreenRendererItemProperties", "blockProperties");
        
        EJDevUpdateScreenRendererDefinition definition = blockProperties.getUpdateScreenRendererDefinition();
        if (definition == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = definition.getItemPropertyDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(blockProperties.getFormProperties(), blockProperties,
                definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createItemRendererProperties(EJPluginBlockItemProperties itemProperties, boolean addDefaultValues)
    {
        EJPluginParameterChecker.checkNotNull(itemProperties, "createItemRendererProperties", "itemProperties");
        
        EJItemRendererDefinition renderer = loadItemRendererDefinition(itemProperties.getBlockProperties().getEntireJProperties(),
                itemProperties.getItemRendererName());
        if (renderer == null)
        {
            
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = renderer.getItemPropertyDefinitionGroup();
        return addExtensionProperties(itemProperties.getBlockProperties().getFormProperties(), itemProperties.getBlockProperties(), definitionGroup, null,
                addDefaultValues);
    }
    
    public static EJFrameworkExtensionProperties createBlockRequiredItemRendererProperties(EJPluginBlockProperties blockProperties, boolean addDefaultValues,
            EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "createBlockRequiredItemRendererProperties", "blockProperties");
        
        EJPropertyDefinitionGroup definitionGroup;
        if (blockProperties.isUsedInLovDefinition())
        {
            EJDevLovRendererDefinition renderer = blockProperties.getLovDefinition().getRendererDefinition();
            if (renderer == null)
            {
                // No need for a message here as it is already handled in the
                // retrieval of the block renderer
                return null;
            }
            definitionGroup = renderer.getItemPropertiesDefinitionGroup();
        }
        else
        {
            EJBlockRendererDefinition renderer = loadBlockRendererDefinition(blockProperties.getEntireJProperties(), blockProperties.getBlockRendererName());
            if (renderer == null)
            {
                // No need for a message here as it is already handled in the
                // retrieval of the block renderer
                return null;
            }
            definitionGroup = renderer.getItemPropertiesDefinitionGroup();
        }
        
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(blockProperties.getFormProperties(), blockProperties,
                definitionGroup, null, addDefaultValues);
        
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createLovRendererProperties(EJPluginFormProperties formProperties, String lovRendererName,
            boolean addDefaultValues, EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(lovRendererName, "createLovRendererProperties", "lovRendererName");
        
        EJLovRendererDefinition rendererDefinition = loadLovRendererDefinition(formProperties.getEntireJProperties(), lovRendererName);
        if (rendererDefinition == null)
        {
            
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = rendererDefinition.getLovPropertyDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(formProperties, null, definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createLovRequiredItemRendererProperties(EJPluginEntireJProperties entireJProperties,
            EJPluginFormProperties formProperties, EJLovDefinitionProperties lovDefProperties, boolean addDefaultValues, EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(lovDefProperties, "createLovRequiredItemRendererProperties", "lovDefProperties");
        
        if (lovDefProperties.getLovRendererName() == null || lovDefProperties.getLovRendererName().trim().length() == 0)
        {
            return null;
        }
        
        EJLovRendererDefinition renderer = loadLovRendererDefinition(entireJProperties, lovDefProperties.getLovRendererName());
        if (renderer == null)
        {
            
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = renderer.getItemPropertiesDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(formProperties, null, definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createLovRequiredSpacerItemProperties(EJPluginEntireJProperties entireJProperties,
            EJPluginFormProperties formProperties, EJLovDefinitionProperties lovDefProperties, boolean addDefaultValues, EJFrameworkExtensionProperties source)
    {
        EJPluginParameterChecker.checkNotNull(lovDefProperties, "createLovRequiredSpacerItemProperties", "lovDefProperties");
        
        if (lovDefProperties.getLovRendererName() == null || lovDefProperties.getLovRendererName().trim().length() == 0)
        {
            return null;
        }
        
        EJLovRendererDefinition renderer = loadLovRendererDefinition(entireJProperties, lovDefProperties.getLovRendererName());
        if (renderer == null)
        {
            
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = renderer.getSpacerItemPropertiesDefinitionGroup();
        EJCoreFrameworkExtensionProperties newExtensionProperties = addExtensionProperties(formProperties, null, definitionGroup, null, addDefaultValues);
        if (newExtensionProperties != null && source != null)
        {
            copyMatchingProperties(newExtensionProperties, source);
            
        }
        return newExtensionProperties;
    }
    
    public static EJFrameworkExtensionProperties createApplicationManagerProperties(EJPluginEntireJProperties entirejProperties, boolean addDefaultValues)
    {
        EJPluginParameterChecker.checkNotNull(entirejProperties, "createApplicationManagerProperties", "entirejProperties");
        
        EJApplicationDefinition def = entirejProperties.getApplicationManager();
        if (def == null)
        {
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = def.getApplicationPropertyDefinitionGroup();
        EJFrameworkExtensionProperties props = addExtensionProperties(null, null, definitionGroup, null, addDefaultValues);
        
        return props;
    }
    
    public static EJFrameworkExtensionProperties createApplicationComponentProperties(EJPluginEntireJProperties formProperties, String rendererName,
            boolean addDefaultValues)
    {
        
        EJDevAppComponentRendererDefinition def = loadAppComponentDefinition(formProperties, rendererName);
        if (def == null)
        {
            
            return null;
        }
        
        EJPropertyDefinitionGroup definitionGroup = def.getComponentPropertyDefinitionGroup();
        EJFrameworkExtensionProperties props = addExtensionProperties(null, null, definitionGroup, null, addDefaultValues);
        
        return props;
    }
    
    public static EJCoreFrameworkExtensionProperties addExtensionProperties(EJFormProperties formProperties, EJBlockProperties blockProperties,
            EJPropertyDefinitionGroup definitionGroup, EJCoreFrameworkExtensionProperties rendererProperties, boolean addDefaultValue)
    {
        // If the RendererPropertyDefinitionGroup is null, then do nothing
        if (definitionGroup == null)
        {
            return rendererProperties;
        }
        
        EJCoreFrameworkExtensionProperties props = rendererProperties;
        
        if (props == null)
        {
            props = new EJCoreFrameworkExtensionProperties(formProperties, blockProperties, definitionGroup.getName(), null);
        }
        
        // Add the properties of the current group
        for (EJPropertyDefinition definition : definitionGroup.getPropertyDefinitions())
        {
            if (addDefaultValue)
            {
                EJCoreFrameworkExtensionProperty property = new EJCoreFrameworkExtensionProperty(definition.getPropertyType(), definition.getName());
                property.setValue(definition.getDefaultValue() == null ? "" : definition.getDefaultValue());
                property.setMultilingual(definition.isMultilingual());
                property.setMandatory(definition.isMandatory());
                props.addProperty(property);
            }
            else
            {
                EJCoreFrameworkExtensionProperty property = new EJCoreFrameworkExtensionProperty(definition.getPropertyType(), definition.getName());
                property.setMultilingual(definition.isMultilingual());
                property.setMandatory(definition.isMandatory());
                props.addProperty(property);
            }
        }
        
        // Add the property lists of the current group
        Iterator<EJPropertyDefinitionList> itiPropertyLists = definitionGroup.getPropertyDefinitionLists().iterator();
        while (itiPropertyLists.hasNext())
        {
            EJPropertyDefinitionList definitionList = itiPropertyLists.next();
            EJFrameworkExtensionPropertyList list = new EJCoreFrameworkExtensionPropertyList(definitionList.getName());
            props.addPropertyList(list);
        }
        
        // Now add any sub groups
        Iterator<EJPropertyDefinitionGroup> itiPropertyGroups = definitionGroup.getSubGroups().iterator();
        while (itiPropertyGroups.hasNext())
        {
            EJPropertyDefinitionGroup group = itiPropertyGroups.next();
            EJCoreFrameworkExtensionProperties groupProps = new EJCoreFrameworkExtensionProperties(null, blockProperties, group.getName(), props);
            props.addPropertyGroup(addExtensionProperties(formProperties, blockProperties, group, groupProps, addDefaultValue));
        }
        
        return props;
    }
    
    public static void populateRendererProperties(Node renderingNode, EJCoreFrameworkExtensionProperties rendererProperties)
    {
        EJPluginParameterChecker.checkNotNull(renderingNode, "populateRendererProperties", "renderingNode");
        EJPluginParameterChecker.checkNotNull(rendererProperties, "populateRendererProperties", "rendererProperties");
        
        String name;
        
        NodeList children = renderingNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++)
        {
            Node childNode = children.item(j);
            
            if (childNode.getNodeType() == 1)
            {
                if (childNode.getNodeName().equals("propertyGroup"))
                {
                    setRenderPropertyGroup(rendererProperties, childNode.getAttributes().getNamedItem("name").getNodeValue(), childNode);
                }
                else
                {
                    name = childNode.getAttributes().getNamedItem("name").getNodeValue();
                    if (childNode.getFirstChild() == null)
                    {
                        rendererProperties.setPropertyValue(name, null);
                    }
                    else
                    {
                        rendererProperties.setPropertyValue(name, childNode.getFirstChild().getNodeValue());
                    }
                }
            }
        }
    }
    
    private static void setRenderPropertyGroup(EJCoreFrameworkExtensionProperties parentProperties, String name, Node renderNode)
    {
        EJCoreFrameworkExtensionProperties renderPropertiesGroup = parentProperties.getPropertyGroup(name);
        if (renderPropertiesGroup == null)
        {
            return;
        }
        
        String propertyName, groupName;
        
        NodeList children = renderNode.getChildNodes();
        for (int j = 0; j < children.getLength(); j++)
        {
            Node childNode = children.item(j);
            if (childNode.getNodeType() == 1)
            {
                if (childNode.getNodeName().equals("propertyGroup"))
                {
                    groupName = childNode.getAttributes().getNamedItem("name").getNodeValue();
                    setRenderPropertyGroup(renderPropertiesGroup, groupName, childNode);
                }
                else
                {
                    propertyName = childNode.getAttributes().getNamedItem("name").getNodeValue();
                    Node propertyValueNode = childNode.getFirstChild();
                    
                    if (propertyValueNode == null)
                    {
                        renderPropertiesGroup.setPropertyValue(propertyName, "");
                    }
                    else
                    {
                        renderPropertiesGroup.setPropertyValue(propertyName, propertyValueNode.getNodeValue());
                    }
                }
            }
        }
    }
    
    public static EJDevMenuRendererDefinition loadMenuDefinition(EJPluginEntireJProperties entireJProperties, String renderer)
    {
        if (renderer == null || renderer.trim().length() == 0)
        {
            return null;
        }
        
        EJPluginRenderer rendererDefProp = entireJProperties.getMenuRendererContainer().getRenderer(renderer);
        if (rendererDefProp == null)
        {
            return null;
        }
        
        return (EJDevMenuRendererDefinition) rendererDefProp.getRendererDefinition();
    }
    
    public static EJDevFormRendererDefinition loadFormRendererDefinition(EJPluginEntireJProperties entireJProperties, String rendererName)
    {
        if (rendererName == null)
        {
            return null;
        }
        
        EJPluginRenderer rendererDefProp = entireJProperties.getFormRendererContainer().getRenderer(rendererName);
        if (rendererDefProp == null)
        {
            return null;
        }
        
        return (EJDevFormRendererDefinition) rendererDefProp.getRendererDefinition();
    }
    
    public static EJDevBlockRendererDefinition loadBlockRendererDefinition(EJPluginEntireJProperties entireJProperties, String rendererName)
    {
        if (rendererName == null)
        {
            return null;
        }
        
        EJPluginRenderer rendererDefinitionProperty = entireJProperties.getBlockRendererContainer().getRenderer(rendererName);
        if (rendererDefinitionProperty == null)
        {
            return null;
        }
        
        return (EJDevBlockRendererDefinition) rendererDefinitionProperty.getRendererDefinition();
    }
    
    public static EJDevItemRendererDefinition loadItemRendererDefinition(EJPluginEntireJProperties entireJProperties, String rendererName)
    {
        if (rendererName == null)
        {
            return null;
        }
        
        EJPluginRenderer rendererProperty = entireJProperties.getItemRendererContainer().getRenderer(rendererName);
        
        if (rendererProperty == null)
        {
            return null;
        }
        
        return (EJDevItemRendererDefinition) rendererProperty.getRendererDefinition();
    }
    
    public static EJDevAppComponentRendererDefinition loadAppComponentDefinition(EJPluginEntireJProperties entireJProperties, String rendererName)
    {
        if (rendererName == null)
        {
            return null;
        }
        
        EJPluginRenderer rendererProperty = entireJProperties.getAppComponentRendererContainer().getRenderer(rendererName);
        
        if (rendererProperty == null)
        {
            return null;
        }
        
        return (EJDevAppComponentRendererDefinition) rendererProperty.getRendererDefinition();
    }
    
    public static EJDevLovRendererDefinition loadLovRendererDefinition(EJPluginEntireJProperties entireJProperties, String lovRendererName)
    {
        if (lovRendererName == null)
        {
            return null;
        }
        
        EJPluginRenderer rendererDefProp = entireJProperties.getLovRendererContainer().getRenderer(lovRendererName);
        
        if (rendererDefProp == null)
        {
            return null;
        }
        
        return (EJDevLovRendererDefinition) rendererDefProp.getRendererDefinition();
    }
    
    public static EJApplicationDefinition loadApplicationManager(EJPluginEntireJProperties entireJProperties, String className)
    {
        if (className == null)
        {
            return null;
        }
        
        return (EJApplicationDefinition) loadClassInstance(entireJProperties, className);
    }
    
    public static Object loadClassInstance(EJPluginEntireJProperties entireJProperties, String className)
    {
        try
        {
            if (className == null)
            {
                return null;
            }
            Class<?> appClass = EJPluginEntireJClassLoader.loadClass(entireJProperties.getJavaProject(), className);
            Object obj = appClass.newInstance();
            return obj;
        }
        catch (ClassNotFoundException e)
        {
            EJCoreLog.logWarnningMessage("Unable to load class: " + className
                    + ".\nPlease ensure the class path has been set correctly and the given class exists");
            return null;
        }
        catch (NoClassDefFoundError e)
        {
            EJCoreLog.logWarnningMessage("Unable to load class: " + className
                    + ".\nPlease ensure the class path has been set correctly and the given class exists");
            return null;
        }
        catch (InstantiationException e)
        {
            EJCoreLog.logWarnningMessage("Unable to load class: " + className
                    + ".\nPlease ensure the class path has been set correctly and the given class exists");
            return null;
        }
        catch (IllegalAccessException e)
        {
            EJCoreLog.logWarnningMessage("Unable to load class: " + className
                    + ".\nPlease ensure the class path has been set correctly and the given class exists");
            return null;
        }
    }
    
    public static void copyMatchingProperties(EJFrameworkExtensionProperties target, EJFrameworkExtensionProperties source)
    {
        if (source == null)
        {
            return;
        }
        
        Iterator<String> propertyNames = source.getAllProperties().keySet().iterator();
        
        while (propertyNames.hasNext())
        {
            String name = propertyNames.next();
            if (target.propertyExists(name)) target.setPropertyValue(name, source.getStringProperty(name));
        }
        
        Iterator<EJFrameworkExtensionPropertyList> propertyLists = source.getAllPropertyLists().iterator();
        while (propertyLists.hasNext())
        {
            EJFrameworkExtensionPropertyList list = propertyLists.next();
            EJCoreFrameworkExtensionPropertyList newList = target.getPropertyList(list.getName());
            if (newList == null)
            {
                continue;
            }
            newList.removeAllEntries();
            for (EJFrameworkExtensionPropertyListEntry entry : list.getAllListEntries())
            {
                
                EJCoreFrameworkExtensionPropertyListEntry newEntry = new EJCoreFrameworkExtensionPropertyListEntry();
                for (String name : entry.getAllProperties().keySet())
                {
                    newEntry.addProperty(name, entry.getProperty(name));
                }
                
                newList.addListEntry(newEntry);
            }
            
        }
        
        Iterator<EJFrameworkExtensionProperties> subGroups = source.getAllPropertyGroups().iterator();
        while (subGroups.hasNext())
        {
            EJFrameworkExtensionProperties next = subGroups.next();
            EJFrameworkExtensionProperties targetSub = target.getPropertyGroup(next.getName());
            if (targetSub != null) copyMatchingProperties(targetSub, next);
        }
    }
}
