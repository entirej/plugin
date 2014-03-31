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
/*
 * Created on Nov 5, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties.writer;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperty;
import org.entirej.framework.core.properties.EJCoreLayoutContainer;
import org.entirej.framework.core.properties.EJCoreLayoutItem;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutComponent;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.SplitGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.TabGroup;
import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyList;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyListEntry;
import org.entirej.framework.plugin.EJPluginParameterChecker;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafActionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafBranchProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafContainer;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafSpacerProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.utils.EJPluginLogger;

public class EntireJPropertiesWriter extends AbstractXmlWriter
{
    public void saveEntireJProperitesFile(EJPluginEntireJProperties properties, IFile file, IProgressMonitor monitor)
    {
        EJPluginParameterChecker.checkNotNull(properties, "createEntireJProperitesFile", "properties");
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        startTAG(buffer, "entirejFramework");
        {
            EJApplicationDefinition applicationDefinition = properties.getApplicationManager();
            writeStringTAG(buffer, "version", properties.getVersion() != null ? properties.getVersion() : "1.0");
            writeStringTAG(buffer, "applicationManager", applicationDefinition != null ? applicationDefinition.getApplicationManagerClassName() : "");
            writeStringTAG(buffer, "applicationManagerDefinition", properties.getApplicationManagerDefinitionClassName());
            writeStringTAG(buffer, "connectionFactoryClassName", properties.getConnectionFactoryClassName());
            writeStringTAG(buffer, "reusableBlocksLocation", properties.getReusableBlocksLocation());
            writeStringTAG(buffer, "reusableLovDefinitionLocation", properties.getReusableLovDefinitionLocation());
            writeStringTAG(buffer, "objectGroupDefinitionLocation", properties.getObjectGroupDefinitionLocation());
            writeStringTAG(buffer, "translatorClassName", properties.getTranslatorClassName());
            
            // Now add the application level parameters
            startTAG(buffer, "applicationLevelParameterList");
            {
                addApplicationLevelParameters(properties, buffer);
            }
            endTAG(buffer, "applicationLevelParameterList");
            
            // Now add the application defined properties
            startTAG(buffer, "applicationDefinedProperties");
            {
                addFrameworkExtensionProperties(properties.getApplicationDefinedProperties(), buffer);
            }
            endTAG(buffer, "applicationDefinedProperties");
            
            startTAG(buffer, "packages");
            {
                // Retrieve the forms package names
                Iterator<String> formPackageNames = properties.getFormPackageNames().iterator();
                while (formPackageNames.hasNext())
                {
                    String name = formPackageNames.next();
                    if (name != null && name.trim().length() > 0)
                    {
                        startOpenTAG(buffer, "formsPackage");
                        {
                            writePROPERTY(buffer, "name", name);
                        }
                        endStartTAG(buffer);
                    }
                }
            }
            endTAG(buffer, "packages");
            
            startTAG(buffer, "renderer");
            {
                startTAG(buffer, "formRenderers");
                {
                    // Add the FormRenderers
                    Iterator<EJPluginRenderer> formRenderers = properties.getFormRendererContainer().getAllRenderers().iterator();
                    while (formRenderers.hasNext())
                    {
                        EJPluginRenderer property = formRenderers.next();
                        
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "rendererClassName", property.getRendererClassName());
                            // writePROPERTY(buffer,
                            // "rendererDefinitionClassName",
                            // property.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                }
                endTAG(buffer, "formRenderers");
                
                startTAG(buffer, "blockRenderers");
                {
                    // Add the BlockRenderers
                    Iterator<EJPluginRenderer> blockRenderers = properties.getBlockRendererContainer().getAllRenderers().iterator();
                    while (blockRenderers.hasNext())
                    {
                        EJPluginRenderer property = blockRenderers.next();
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "rendererClassName", property.getRendererClassName());
                            // writePROPERTY(buffer,
                            // "rendererDefinitionClassName",
                            // property.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                }
                endTAG(buffer, "blockRenderers");
                
                startTAG(buffer, "itemRenderers");
                {
                    // Add the ItemRenderers
                    Iterator<EJPluginRenderer> itemRenderers = properties.getItemRendererContainer().getAllRenderers().iterator();
                    while (itemRenderers.hasNext())
                    {
                        EJPluginRenderer property = itemRenderers.next();
                        
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "rendererClassName", property.getRendererClassName());
                            // writePROPERTY(buffer,
                            // "rendererDefinitionClassName",
                            // property.getRendererDefinitionClassName());
                            
                            closeOpenTAG(buffer);
                            
                            // Now get the assigned data types
                            Iterator<String> dataTypeNames = property.getDataTypeNames().iterator();
                            while (dataTypeNames.hasNext())
                            {
                                writeStringTAG(buffer, "renderedDataType", dataTypeNames.next());
                            }
                        }
                        endTAG(buffer, "renderer");
                    }
                }
                endTAG(buffer, "itemRenderers");
                
                startTAG(buffer, "lovRenderers");
                {
                    // Add the LovRenderers
                    Iterator<EJPluginRenderer> lovRenderers = properties.getLovRendererContainer().getAllRenderers().iterator();
                    while (lovRenderers.hasNext())
                    {
                        EJPluginRenderer property = lovRenderers.next();
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "rendererClassName", property.getRendererClassName());
                            // writePROPERTY(buffer,
                            // "rendererDefinitionClassName",
                            // property.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                }
                endTAG(buffer, "lovRenderers");
                
                startTAG(buffer, "menuRenderers");
                {
                    // Add the menuRenderers
                    Collection<EJPluginRenderer> menuRenderers = properties.getMenuRendererContainer().getAllRenderers();
                    for (EJPluginRenderer renderer : menuRenderers)
                    {
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", renderer.getAssignedName());
                            writePROPERTY(buffer, "rendererClassName", renderer.getRendererClassName());
                            // writePROPERTY(buffer,
                            // "rendererDefinitionClassName",
                            // renderer.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                    
                }
                endTAG(buffer, "menuRenderers");
                startTAG(buffer, "appCompRenderers");
                {
                    // Add the application component Renderers
                    Collection<EJPluginRenderer> appCompRenderers = properties.getAppComponentRendererContainer().getAllRenderers();
                    for (EJPluginRenderer renderer : appCompRenderers)
                    {
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", renderer.getAssignedName());
                            writePROPERTY(buffer, "rendererClassName", renderer.getRendererClassName());
                            // writePROPERTY(buffer,
                            // "rendererDefinitionClassName",
                            // renderer.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                    
                }
                endTAG(buffer, "appCompRenderers");
            }
            endTAG(buffer, "renderer");
            
            startTAG(buffer, "applicationMenus");
            {
                // Now add the Application Menus
                addApplicationMenus(properties, buffer);
            }
            endTAG(buffer, "applicationMenus");
            
            startTAG(buffer, "applicationLayout");
            {
                // Now add the Application layout
                addApplicationLayout(properties, buffer);
            }
            endTAG(buffer, "applicationLayout");
            
            startTAG(buffer, "visualAttributes");
            {
                // Now add the Visual Attribute definitions
                addVisualAttributes(properties, buffer);
            }
            endTAG(buffer, "visualAttributes");
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        try
        {
            file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            
            // create renderer file.
            IFile rendererFile = file.getParent().getFile(new Path("renderers.ejprop"));
            EntireJRendererWriter writer = new EntireJRendererWriter();
            writer.saveEntireJProperitesFile(properties, rendererFile, monitor);
        }
        catch (CoreException e)
        {
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save EntireJ Properties");
        }
        catch (UnsupportedEncodingException e)
        {
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save EntireJ Properties");
        }
    }
    
    private void addVisualAttributes(EJPluginEntireJProperties properties, StringBuffer buffer)
    {
        Iterator<EJCoreVisualAttributeProperties> visualAttributes = properties.getVisualAttributesContainer().getVisualAttributes().iterator();
        EJCoreVisualAttributeProperties visAttr;
        while (visualAttributes.hasNext())
        {
            visAttr = visualAttributes.next();
            
            startOpenTAG(buffer, "visualAttribute");
            {
                writePROPERTY(buffer, "name", visAttr.getName());
                closeOpenTAG(buffer);
                
                writeStringTAG(buffer, "fontName", visAttr.getFontName());
                writeIntTAG(buffer, "fontSize", visAttr.getFontSize());
                writeBooleanTAG(buffer, "fontSizeAsPercentage", visAttr.isFontSizeAsPercentage());
                writeStringTAG(buffer, "style", visAttr.getFontStyle().toString());
                writeStringTAG(buffer, "weight", visAttr.getFontWeight().toString());
                writeStringTAG(buffer, "foregroundColor", getColorString(visAttr.getForegroundColor()));
                writeStringTAG(buffer, "backgroundColor", getColorString(visAttr.getBackgroundColor()));
            }
            endTAG(buffer, "visualAttribute");
        }
    }
    
    private void addApplicationMenus(EJPluginEntireJProperties properties, StringBuffer buffer)
    {
        Iterator<EJPluginMenuProperties> applicationMenus = properties.getPluginMenuContainer().getAllMenuProperties().iterator();
        EJPluginMenuProperties menu;
        while (applicationMenus.hasNext())
        {
            menu = applicationMenus.next();
            
            startOpenTAG(buffer, "applicationMenu");
            {
                writePROPERTY(buffer, "name", menu.getName());
                writePROPERTY(buffer, "actionProcessorClassName", menu.getActionProcessorClassName());
                if (menu.isDefault()) writePROPERTY(buffer, "default", "true");
                closeOpenTAG(buffer);
                
                addApplicationMenuLeaves(menu, buffer);
            }
            endTAG(buffer, "applicationMenu");
        }
    }
    
    private void addApplicationLayout(EJPluginEntireJProperties properties, StringBuffer buffer)
    {
        EJCoreLayoutContainer layoutContainer = properties.getLayoutContainer();
        writeStringTAG(buffer, "title", layoutContainer.getTitle());
        writeIntTAG(buffer, "col", layoutContainer.getColumns());
        writeIntTAG(buffer, "height", layoutContainer.getHeight());
        writeIntTAG(buffer, "width", layoutContainer.getWidth());
        List<EJCoreLayoutItem> items = layoutContainer.getItems();
        startTAG(buffer, "items");
        
        for (EJCoreLayoutItem item : items)
        {
            addApplicationLayoutItem(item, buffer);
        }
        endTAG(buffer, "items");
    }
    
    private void addApplicationLayoutItem(EJCoreLayoutItem item, StringBuffer buffer)
    {
        startOpenTAG(buffer, "item");
        writePROPERTY(buffer, "type", item.getType().name());
        closeOpenTAG(buffer);
        writeStringTAG(buffer, "name", item.getName());
        writeStringTAG(buffer, "fill", item.getFill().name());
        writeStringTAG(buffer, "grab", item.getGrab().name());
        writeIntTAG(buffer, "hHint", item.getHintHeight());
        writeIntTAG(buffer, "wHint", item.getHintWidth());
        writeIntTAG(buffer, "hMin", item.getMinHeight());
        writeIntTAG(buffer, "wMin", item.getMinWidth());
        writeIntTAG(buffer, "hSpan", item.getHorizontalSpan());
        writeIntTAG(buffer, "VSpan", item.getVerticalSpan());
        switch (item.getType())
        {
            case GROUP:
            {
                EJCoreLayoutItem.LayoutGroup group = (LayoutGroup) item;
                writeIntTAG(buffer, "col", group.getColumns());
                writeStringTAG(buffer, "title", group.getTitle());
                writeBooleanTAG(buffer, "border", group.isBorder());
                writeBooleanTAG(buffer, "hideMargin", group.isHideMargin());
                List<EJCoreLayoutItem> items = group.getItems();
                startTAG(buffer, "items");
                for (EJCoreLayoutItem sitem : items)
                {
                    addApplicationLayoutItem(sitem, buffer);
                }
                endTAG(buffer, "items");
                break;
            }
            
            case SPACE:
                // nothing
                break;
            case COMPONENT:
            {
                EJCoreLayoutItem.LayoutComponent component = (LayoutComponent) item;
                writeStringTAG(buffer, "renderer", component.getRenderer());
                startTAG(buffer, "rendererProperties");
                {
                    addFrameworkExtensionProperties(component.getRendereProperties(), buffer);
                }
                endTAG(buffer, "rendererProperties");
                break;
            }
            
            case SPLIT:
            {
                EJCoreLayoutItem.SplitGroup splitGroup = (SplitGroup) item;
                writeStringTAG(buffer, "orientation", splitGroup.getOrientation().name());
                List<EJCoreLayoutItem> items = splitGroup.getItems();
                startTAG(buffer, "items");
                for (EJCoreLayoutItem sitem : items)
                {
                    addApplicationLayoutItem(sitem, buffer);
                }
                endTAG(buffer, "items");
                break;
            }
            case TAB:
            {
                EJCoreLayoutItem.TabGroup tabGroup = (TabGroup) item;
                writeStringTAG(buffer, "orientation", tabGroup.getOrientation().name());
                List<EJCoreLayoutItem> items = tabGroup.getItems();
                startTAG(buffer, "items");
                for (EJCoreLayoutItem sitem : items)
                {
                    addApplicationLayoutItem(sitem, buffer);
                }
                endTAG(buffer, "items");
                break;
            }
            
        }
        endTAG(buffer, "item");
    }
    
    private void addApplicationMenuLeaves(EJPluginMenuLeafContainer menuContainer, StringBuffer buffer)
    {
        for (EJPluginMenuLeafProperties leaf : menuContainer.getLeaves())
        {
            if (leaf instanceof EJPluginMenuLeafSpacerProperties)
            {
                addMenuSpacer((EJPluginMenuLeafSpacerProperties) leaf, buffer);
            }
            else if (leaf instanceof EJPluginMenuLeafActionProperties)
            {
                addMenuAction((EJPluginMenuLeafActionProperties) leaf, buffer);
            }
            else if (leaf instanceof EJPluginMenuLeafFormProperties)
            {
                addMenuForm((EJPluginMenuLeafFormProperties) leaf, buffer);
            }
            else if (leaf instanceof EJPluginMenuLeafBranchProperties)
            {
                addMenuBranch((EJPluginMenuLeafBranchProperties) leaf, buffer);
            }
        }
    }
    
    private void addMenuForm(EJPluginMenuLeafFormProperties menuForm, StringBuffer buffer)
    {
        startOpenTAG(buffer, "leaf");
        {
            writePROPERTY(buffer, "type", "FORM");
            writePROPERTY(buffer, "name", menuForm.getName());
            
            closeOpenTAG(buffer);
            
            writeStringTAG(buffer, "displayName", menuForm.getDisplayName());
            writeStringTAG(buffer, "formName", menuForm.getFormName());
            writeStringTAG(buffer, "hint", menuForm.getHint());
            writeStringTAG(buffer, "icon", menuForm.getIconName());
        }
        endTAG(buffer, "leaf");
    }
    
    private void addMenuAction(EJPluginMenuLeafActionProperties menuAction, StringBuffer buffer)
    {
        startOpenTAG(buffer, "leaf");
        {
            writePROPERTY(buffer, "type", "ACTION");
            writePROPERTY(buffer, "name", menuAction.getName());
            
            closeOpenTAG(buffer);
            
            writeStringTAG(buffer, "displayName", menuAction.getDisplayName());
            writeStringTAG(buffer, "actionCommand", menuAction.getMenuAction());
            writeStringTAG(buffer, "hint", menuAction.getHint());
            writeStringTAG(buffer, "icon", menuAction.getIconName());
        }
        endTAG(buffer, "leaf");
    }
    
    private void addMenuBranch(EJPluginMenuLeafBranchProperties menuBranch, StringBuffer buffer)
    {
        startOpenTAG(buffer, "leaf");
        {
            writePROPERTY(buffer, "type", "BRANCH");
            writePROPERTY(buffer, "name", menuBranch.getName());
            
            closeOpenTAG(buffer);
            
            writeStringTAG(buffer, "displayName", menuBranch.getDisplayName());
            writeStringTAG(buffer, "icon", menuBranch.getIconName());
            
            addApplicationMenuLeaves(menuBranch, buffer);
        }
        endTAG(buffer, "leaf");
    }
    
    private void addMenuSpacer(EJPluginMenuLeafSpacerProperties menuSpacer, StringBuffer buffer)
    {
        startOpenTAG(buffer, "leaf");
        {
            writePROPERTY(buffer, "type", "SPACER");
            writePROPERTY(buffer, "name", menuSpacer.getName());
            
            closeOpenTAG(buffer);
            
        }
        endTAG(buffer, "leaf");
    }
    
    private String getColorString(java.awt.Color color)
    {
        if (color == null)
        {
            return "";
        }
        
        return "r" + color.getRed() + "g" + color.getGreen() + "b" + color.getBlue();
    }
    
    private void addFrameworkExtensionProperties(EJFrameworkExtensionProperties renderer, StringBuffer buffer)
    {
        // If there is no renderer passed, then just do nothing and return
        if (renderer == null)
        {
            return;
        }
        
        for (EJCoreFrameworkExtensionProperty property : renderer.getAllProperties().values())
        {
            startOpenTAG(buffer, "property");
            {
                writePROPERTY(buffer, "name", property.getName());
                writePROPERTY(buffer, "multilingual", "" + property.isMultilingual());
                writePROPERTY(buffer, "propertyType", "" + property.getPropertyType());
                closeOpenTAG(buffer);
                
                writeTagValue(buffer, property.getValue());
            }
            closeTAG(buffer, "property");
        }
        
        
        EJFrameworkExtensionPropertyList propertyList;
        Iterator<EJFrameworkExtensionPropertyList> propertyLists = renderer.getAllPropertyLists().iterator();
        while (propertyLists.hasNext())
        {
            propertyList = propertyLists.next();
            
            startOpenTAG(buffer, "propertyList");
            {
                writePROPERTY(buffer, "name", propertyList.getName());
                closeOpenTAG(buffer);
                
                Iterator<EJFrameworkExtensionPropertyListEntry> listEntries = propertyList.getAllListEntries().iterator();
                while (listEntries.hasNext())
                {
                    EJFrameworkExtensionPropertyListEntry entry = listEntries.next();
                    startTAG(buffer, "listEntry");
                    {
                        Map<String, String> leProperties = entry.getAllProperties();
                        String leKey = "", leValue = "";
                        Iterator<String> leKeys = leProperties.keySet().iterator();
                        while (leKeys.hasNext())
                        {
                            leKey = leKeys.next();
                            leValue = entry.getProperty(leKey);
                            
                            startOpenTAG(buffer, "property");
                            {
                                writePROPERTY(buffer, "name", leKey);
                                closeOpenTAG(buffer);
                                
                                writeTagValue(buffer, leValue);
                            }
                            closeTAG(buffer, "property");
                        }
                        
                    }
                    endTAG(buffer, "listEntry");
                }
            }
            endTAG(buffer, "propertyList");
        }
        
        Iterator<EJFrameworkExtensionProperties> groups = renderer.getAllPropertyGroups().iterator();
        while (groups.hasNext())
        {
            EJFrameworkExtensionProperties group = groups.next();
            startOpenTAG(buffer, "propertyGroup");
            {
                writePROPERTY(buffer, "name", group.getName());
                closeOpenTAG(buffer);
                
                addFrameworkExtensionProperties(group, buffer);
            }
            endTAG(buffer, "propertyGroup");
        }
    }
    
    private void addApplicationLevelParameters(EJPluginEntireJProperties entireJProperties, StringBuffer buffer)
    {
        // If there is no renderer passed, then just do nothing and return
        if (entireJProperties.getAllApplicationLevelParameters() == null)
        {
            return;
        }
        
        for (EJPluginApplicationParameter parameter : entireJProperties.getAllApplicationLevelParameters())
        {
            startOpenTAG(buffer, "appicationLevelParameter");
            {
                writePROPERTY(buffer, "name", parameter.getName());
                writePROPERTY(buffer, "dataType", parameter.getDataTypeName());
                closeOpenTAG(buffer);
            }
            closeTAG(buffer, "appicationLevelParameter");
        }
    }
    
}
