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
package org.entirej.framework.plugin.framework.properties.writer;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperty;
import org.entirej.framework.core.properties.containers.interfaces.EJCanvasPropertiesContainer;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyList;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyListEntry;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJLovDefinitionProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.core.properties.interfaces.EJStackedPageProperties;
import org.entirej.framework.core.properties.interfaces.EJTabPageProperties;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovItemMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationJoinProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginReusableBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginReusableLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockGroup;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockItemContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginRelationContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.framework.plugin.utils.EJPluginLogger;

public class FormPropertiesWriter extends AbstractXmlWriter
{
    public void saveForm(EJPluginFormProperties form, IFile file, IProgressMonitor monitor)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        startTAG(buffer, "entirejFramework");
        {
            startTAG(buffer, "form");
            {
                writeStringTAG(buffer, "formTitle", form.getTitle());
                writeStringTAG(buffer, "formDisplayName", form.getFormDisplayName());
                writeIntTAG(buffer, "formWidth", form.getFormWidth());
                writeIntTAG(buffer, "formHeight", form.getFormHeight());
                writeIntTAG(buffer, "numCols", form.getNumCols());
                writeStringTAG(buffer, "actionProcessorClassName", form.getActionProcessorClassName());
                writeStringTAG(buffer, "formRendererName", form.getFormRendererName());
                
                // Now add the forms parameters
                Iterator<EJPluginApplicationParameter> paramNamesIti = form.getAllFormParameters().iterator();
                startTAG(buffer, "formParameterList");
                {
                    EJPluginApplicationParameter parameter;
                    while (paramNamesIti.hasNext())
                    {
                        parameter = paramNamesIti.next();
                        
                        startOpenTAG(buffer, "formParameter");
                        {
                            writePROPERTY(buffer, "name", parameter.getName());
                            writePROPERTY(buffer, "dataType", parameter.getDataTypeName());
                            writePROPERTY(buffer, "defaultValue", parameter.getDefaultValue());
                            closeOpenTAG(buffer);
                        }
                        closeTAG(buffer, "formParameter");
                    }
                }
                endTAG(buffer, "formParameterList");
                
                // Now add the forms application properties
                Iterator<String> applNamesIti = form.getAllApplicationPropertyNames().iterator();
                startTAG(buffer, "applicationProperties");
                {
                    String name = "";
                    String value = "";
                    while (applNamesIti.hasNext())
                    {
                        name = applNamesIti.next();
                        value = form.getApplicationProperty(name);
                        
                        startOpenTAG(buffer, "property");
                        {
                            writePROPERTY(buffer, "name", name);
                            closeOpenTAG(buffer);
                            
                            writeTagValue(buffer, value);
                        }
                        closeTAG(buffer, "property");
                    }
                }
                endTAG(buffer, "applicationProperties");
                
                // Now add the form renderer properties
                startTAG(buffer, "formRendererProperties");
                {
                    addFrameworkExtensionProperties(form.getFormRendererProperties(), buffer);
                }
                endTAG(buffer, "formRendererProperties");
                
    
                
                
                // Now add the forms canvases
                startTAG(buffer, "canvasList");
                {
                    addCanvasList(form.getCanvasContainer(), buffer);
                }
                endTAG(buffer, "canvasList");
                
                
                // Now add the blocks
                // The block items will be added during the addBlockList method
                startTAG(buffer, "blockList");
                {
                    addBlockList(form.getBlockContainer(), buffer);
                }
                endTAG(buffer, "blockList");
                
                // Now add the ObjectGroup Definitions
                startTAG(buffer, "objGroupDefinitionList");
                {
                    List<EJPluginObjectGroupProperties> objectGroupProperties = form.getObjectGroupContainer().getAllObjectGroupProperties();
                    for (EJPluginObjectGroupProperties properties : objectGroupProperties)
                    {
                        startOpenTAG(buffer, "objGroupDefinition");
                        {
                            writePROPERTY(buffer, "name", properties.getName());
                            closeOpenTAG(buffer);
                        }
                        endTAG(buffer, "objGroupDefinition");
                    }
                }
                endTAG(buffer, "objGroupDefinitionList");
                
                
               
                
                // Now add the forms relations
                startTAG(buffer, "relationList");
                {
                    addRelationList(form.getRelationContainer(), buffer);
                }
                endTAG(buffer, "relationList");
                
                // Now add the LOV Definitions
                startTAG(buffer, "lovDefinitionList");
                {
                    addLovDefinitionList(form.getLovDefinitionContainer(), buffer);
                }
                endTAG(buffer, "lovDefinitionList");
                
            }
            endTAG(buffer, "form");
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        try
        {
            if (file.exists())
            {
                // byte[] encryptedData =
                // Encrypter.encrypt(buffer.toString().getBytes("UTF-8"));
                // file.setContents(new ByteArrayInputStream(encryptedData),
                // IResource.KEEP_HISTORY, monitor);
                file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
            else
            {
                file.create(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
        }
        catch (Exception e)
        {
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save form: " + form.getName());
        }
    }
    
    public void saveReusableBlock(EJPluginReusableBlockProperties blockProperties, IFile file, IProgressMonitor monitor)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        startTAG(buffer, "entirejFramework");
        {
            startTAG(buffer, "form");
            {
                // Now add the blocks properties
                startTAG(buffer, "blockList");
                {
                    addBlockProperties(blockProperties.getBlockProperties(), buffer);
                }
                endTAG(buffer, "blockList");
                
                // Now add the LOV Definitions
                startTAG(buffer, "lovDefinitionList");
                {
                    if (blockProperties.getLovDefinitionContainer() != null)
                    {
                        addLovDefinitionList(blockProperties.getLovDefinitionContainer(), buffer);
                    }
                }
                endTAG(buffer, "lovDefinitionList");
            }
            endTAG(buffer, "form");
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        try
        {
            if (file.exists())
            {
                file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
            else
            {
                file.create(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save reusable block: "
                    + blockProperties.getBlockProperties().getName());
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save reusable block: "
                    + blockProperties.getBlockProperties().getName());
        }
    }
    
    public void saveReusableLovDefinition(EJPluginReusableLovDefinitionProperties lovDefinitionProperties, IFile file, IProgressMonitor monitor)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        
        startTAG(buffer, "entirejFramework");
        {
            startTAG(buffer, "form");
            {
                // Now add the lov definition properties
                startTAG(buffer, "lovDefinitionList");
                {
                    addLovDefinitionProperties(lovDefinitionProperties.getLovDefinitionProperties(), buffer);
                }
                endTAG(buffer, "lovDefinitionList");
            }
            endTAG(buffer, "form");
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        try
        {
            if (file.exists())
            {
                file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
            else
            {
                file.create(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
        }
        catch (CoreException e)
        {
            e.printStackTrace();
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save reusable lov definition: "
                    + lovDefinitionProperties.getLovDefinitionProperties().getName());
        }
        catch (UnsupportedEncodingException e)
        {
            e.printStackTrace();
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save reusable lov definition: "
                    + lovDefinitionProperties.getLovDefinitionProperties().getName());
        }
    }
    
    protected void addFrameworkExtensionProperties(EJFrameworkExtensionProperties renderer, StringBuffer buffer)
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
                writePROPERTY(buffer, "propertyType", property.getPropertyType().toString());
                
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
    
    protected void addRelationList(EJPluginRelationContainer relationContainer, StringBuffer buffer)
    {
        Iterator<EJPluginRelationProperties> relations = relationContainer.getAllRelationProperties().iterator();
        
        while (relations.hasNext())
        {
            EJPluginRelationProperties relationProps = relations.next();
            if(relationProps.isImportFromObjectGroup())
            {
                continue;
            }
            startOpenTAG(buffer, "relation");
            {
                writePROPERTY(buffer, "name", relationProps.getName());
                writePROPERTY(buffer, "masterBlockName", relationProps.getMasterBlockName());
                writePROPERTY(buffer, "detailBlockName", relationProps.getDetailBlockName());
                writePROPERTY(buffer, "preventMasterlessOperations", "" + relationProps.preventMasterlessOperations());
                writePROPERTY(buffer, "deferredQuery", "" + relationProps.isDeferredQuery());
                writePROPERTY(buffer, "autoQuery", "" + relationProps.isAutoQuery());
                closeOpenTAG(buffer);
                
                Iterator<EJPluginRelationJoinProperties> joinIti = relationProps.getRelationJoins().iterator();
                while (joinIti.hasNext())
                {
                    EJPluginRelationJoinProperties join = joinIti.next();
                    startOpenTAG(buffer, "join");
                    {
                        writePROPERTY(buffer, "masterItem", join.getMasterItemName());
                        writePROPERTY(buffer, "detailItem", join.getDetailItemName());
                        closeOpenTAG(buffer);
                    }
                    endTAG(buffer, "join");
                }
            }
            endTAG(buffer, "relation");
        }
    }
    
    protected void addCanvasList(EJCanvasPropertiesContainer canvasContainer, StringBuffer buffer)
    {
        Iterator<EJCanvasProperties> canvases = canvasContainer.getAllCanvasProperties().iterator();
        
        while (canvases.hasNext())
        {
            EJCanvasProperties canvasProps = canvases.next();
            
            
            if(canvasProps instanceof EJPluginCanvasProperties && (!((EJPluginCanvasProperties)canvasProps).isObjectGroupRoot() && ((EJPluginCanvasProperties)canvasProps).isImportFromObjectGroup()))
            {
                continue;
            }
            
            
            startOpenTAG(buffer, "canvas");
            {
                writePROPERTY(buffer, "name", canvasProps.getName());
                writePROPERTY(buffer, "type", canvasProps.getType().name());
                closeOpenTAG(buffer);
                
                if (canvasProps.getType() != EJCanvasType.BLOCK)
                {
                    writeIntTAG(buffer, "width", canvasProps.getWidth());
                    writeIntTAG(buffer, "height", canvasProps.getHeight());
                    writeIntTAG(buffer, "numCols", canvasProps.getNumCols());
                    writeIntTAG(buffer, "horizontalSpan", canvasProps.getHorizontalSpan());
                    writeIntTAG(buffer, "verticalSpan", canvasProps.getVerticalSpan());
                    writeBooleanTAG(buffer, "expandHorizontally", canvasProps.canExpandHorizontally());
                    writeBooleanTAG(buffer, "expandVertically", canvasProps.canExpandVertically());
                }
                
                
                
                
                
                
                if(canvasProps instanceof EJPluginCanvasProperties && ((EJPluginCanvasProperties)canvasProps).isObjectGroupRoot())
                {
                    //ignore and do not add any sub items
                }
                else
                {
                    
                    if (canvasProps.getType() == EJCanvasType.TAB)
                    {
                        writeStringTAG(buffer, "tabPosition", canvasProps.getTabPosition().name());
                    }
                    else if (canvasProps.getType() == EJCanvasType.STACKED)
                    {
                        writeStringTAG(buffer, "initialStackedPageName", canvasProps.getInitialStackedPageName());
                    }
                    else if (canvasProps.getType() == EJCanvasType.POPUP)
                    {
                        writeStringTAG(buffer, "popupPageTitle", canvasProps.getPopupPageTitle());
                        writeStringTAG(buffer, "buttonOneText", canvasProps.getButtonOneText());
                        writeStringTAG(buffer, "buttonTwoText", canvasProps.getButtonTwoText());
                        writeStringTAG(buffer, "buttonThreeText", canvasProps.getButtonThreeText());
                        
                        startTAG(buffer, "canvasList");
                        {
                            addCanvasList(canvasProps.getPopupCanvasContainer(), buffer);
                        }
                        endTAG(buffer, "canvasList");
                    }
                    else if (canvasProps.getType() == EJCanvasType.GROUP)
                    {
                        writeBooleanTAG(buffer, "displayGroupFrame", canvasProps.getDisplayGroupFrame());
                        writeStringTAG(buffer, "groupFrameTitle", canvasProps.getGroupFrameTitle());
                    }
                    else if (canvasProps.getType() == EJCanvasType.SPLIT)
                    {
                        writeStringTAG(buffer, "splitOrientation", canvasProps.getSplitOrientation().name());
                    }
                    
                    addTabPageProperties(canvasProps, buffer);
                    addStackedPageProperties(canvasProps, buffer);
                    addCanvasGroupCanvases(canvasProps, buffer);
                    addCanvasSplitCanvases(canvasProps, buffer);
                    
                    
                }
                
                
            }
            endTAG(buffer, "canvas");
        }
    }
    
    protected void addBlockList(EJPluginBlockContainer blockContainer, StringBuffer buffer)
    {
        
        List<BlockContainerItem> blockContainerItems = blockContainer.getBlockContainerItems();
        for (BlockContainerItem item : blockContainerItems)
        {
            if(item instanceof EJPluginBlockProperties)
            {
                EJPluginBlockProperties blockProps = (EJPluginBlockProperties) item;
                if(blockProps.isImportFromObjectGroup())
                {
                    addObjectGroupBlockProperties(blockProps, buffer);
                    continue;
                }
                
                if (blockProps.isReferenceBlock())
                {
                    addReferencedBlockProperties(blockProps, buffer);
                }
                else
                {
                    addBlockProperties(blockProps, buffer);
                }
                continue;
            }
            //write Block groups
            if(item instanceof BlockGroup)
            {
                BlockGroup group = (BlockGroup) item;
                startOpenTAG(buffer, "blockGroup");
                {
                    writePROPERTY(buffer, "name", group.getName());
                    closeOpenTAG(buffer);
                    List<EJPluginBlockProperties> allBlockProperties = group.getAllBlockProperties();
                    for (EJPluginBlockProperties blockProps : allBlockProperties)
                    {
                        if(blockProps.isImportFromObjectGroup())
                        {
                            addObjectGroupBlockProperties(blockProps, buffer);
                            continue;
                        }
                        
                        if (blockProps.isReferenceBlock())
                        {
                            addReferencedBlockProperties(blockProps, buffer);
                        }
                        else
                        {
                            addBlockProperties(blockProps, buffer);
                        }
                        continue;
                    }
                }
                endTAG(buffer, "blockGroup");
            }
        }
        
    }
    
    protected void addLovDefinitionList(EJPluginLovDefinitionContainer lovDefinitionContainer, StringBuffer buffer)
    {
        Iterator<EJPluginLovDefinitionProperties> lovDefinitions = lovDefinitionContainer.getAllLovDefinitionProperties().iterator();
        while (lovDefinitions.hasNext())
        {
            EJLovDefinitionProperties lovDefinition = lovDefinitions.next();
            if(lovDefinition instanceof EJPluginLovDefinitionProperties && ((EJPluginLovDefinitionProperties)lovDefinition).isImportFromObjectGroup())
            {
                continue;
            }
            
            if (lovDefinition.isReferenced())
            {
                addReferencedLovDefinitionProperties((EJPluginLovDefinitionProperties) lovDefinition, buffer);
                
            }
            else
            {
                addLovDefinitionProperties((EJPluginLovDefinitionProperties) lovDefinition, buffer);
            }
        }
    }
    
    private void addReferencedLovDefinitionProperties(EJPluginLovDefinitionProperties lovDefProps, StringBuffer buffer)
    {
        startOpenTAG(buffer, "lovDefinition");
        {
            writePROPERTY(buffer, "name", lovDefProps.getName());
            writePROPERTY(buffer, "isReferenced", "true");
            
            writePROPERTY(buffer, "referencedLovDefinitionName", lovDefProps.getReferencedLovDefinitionName());
            
            closeOpenTAG(buffer);
            
            // Now add the block items
            startTAG(buffer, "itemList");
            {
                addReferencedBlockItemProperties(lovDefProps.getBlockProperties().getItemContainer(), buffer);
            }
            endTAG(buffer, "itemList");
        }
        endTAG(buffer, "lovDefinition");
        
    }
    
    protected void addLovDefinitionProperties(EJPluginLovDefinitionProperties lovDefProps, StringBuffer buffer)
    {
        startOpenTAG(buffer, "lovDefinition");
        {
            writePROPERTY(buffer, "name", lovDefProps.getName());
            writePROPERTY(buffer, "isReferenced", "false");
            writePROPERTY(buffer, "rendererName", lovDefProps.getLovRendererName());
            writePROPERTY(buffer, "allowUserQuery", "" + lovDefProps.isUserQueryAllowed());
            writePROPERTY(buffer, "automaticQuery", "" + lovDefProps.isAutomaticQuery());
            
            closeOpenTAG(buffer);
            
            writeIntTAG(buffer, "width", lovDefProps.getWidth());
            writeIntTAG(buffer, "height", lovDefProps.getHeight());
            writeStringTAG(buffer, "actionProcessorClassName", lovDefProps.getActionProcessorClassName());
            
            startTAG(buffer, "lovRendererProperties");
            {
                addFrameworkExtensionProperties(lovDefProps.getLovRendererProperties(), buffer);
            }
            endTAG(buffer, "lovRendererProperties");
            lovDefProps.getBlockProperties().internalSetName(lovDefProps.getName());// force
                                                                                    // lov
                                                                                    // name
                                                                                    // as
                                                                                    // block
                                                                                    // name
            // Add the properties of the block contained within the lov
            // definition
            addBlockProperties(lovDefProps.getBlockProperties(), buffer);
        }
        endTAG(buffer, "lovDefinition");
    }
    
    protected void addBlockProperties(EJPluginBlockProperties blockProperties, StringBuffer buffer)
    {
        startOpenTAG(buffer, "block");
        {
            writePROPERTY(buffer, "name", blockProperties.getName());
            writePROPERTY(buffer, "referenced", "false");
            writePROPERTY(buffer, "controlBlock", "" + blockProperties.isControlBlock());
            closeOpenTAG(buffer);
            
            writeBooleanTAG(buffer, "isMirrored", blockProperties.isMirrorBlock());
            writeStringTAG(buffer, "mirrorParent", blockProperties.getMirrorBlockName());
            writeStringTAG(buffer, "description", blockProperties.getDescription());
            writeBooleanTAG(buffer, "queryAllowed", blockProperties.isQueryAllowed());
            writeBooleanTAG(buffer, "insertAllowed", blockProperties.isInsertAllowed());
            writeBooleanTAG(buffer, "updateAllowed", blockProperties.isUpdateAllowed());
            writeBooleanTAG(buffer, "addControlBlockDefaultRecord", blockProperties.addControlBlockDefaultRecord());
            writeBooleanTAG(buffer, "deleteAllowed", blockProperties.isDeleteAllowed());
            writeStringTAG(buffer, "canvasName", blockProperties.getCanvasName());
            writeStringTAG(buffer, "blockRendererName", blockProperties.getBlockRendererName());
            // writeStringTAG(buffer, "queryScreenRendererName",
            // blockProperties.getQueryScreenRendererName());
            // writeStringTAG(buffer, "insertScreenRendererName",
            // blockProperties.getInsertScreenRendererName());
            // writeStringTAG(buffer, "updateScreenRendererName",
            // blockProperties.getUpdateScreenRendererName());
            if (!blockProperties.isMirrorChild())
            {
                writeStringTAG(buffer, "serviceClassName", blockProperties.getServiceClassName());
            }
            writeStringTAG(buffer, "actionProcessorClassName", blockProperties.getActionProcessorClassName());
            writeBooleanTAG(buffer, "queryAllRows", blockProperties.queryAllRows());
            writeIntTAG(buffer, "maxResults", blockProperties.getMaxResults());
            writeIntTAG(buffer, "pageSize", blockProperties.getPageSize());
            
            // Now add the blocks application properties
            Iterator<String> applNamesIti = blockProperties.getAllApplicationPropertyNames().iterator();
            startTAG(buffer, "applicationProperties");
            {
                String name = "";
                String value = "";
                while (applNamesIti.hasNext())
                {
                    name = applNamesIti.next();
                    value = blockProperties.getApplicationProperty(name);
                    startOpenTAG(buffer, "property");
                    {
                        writePROPERTY(buffer, "name", name);
                        closeOpenTAG(buffer);
                        writeTagValue(buffer, value);
                    }
                    closeTAG(buffer, "property");
                }
            }
            endTAG(buffer, "applicationProperties");
            
            // Now add the Block Renderer properties
            startTAG(buffer, "blockRendererProperties");
            {
                addFrameworkExtensionProperties(blockProperties.getBlockRendererProperties(), buffer);
            }
            endTAG(buffer, "blockRendererProperties");
            
            // Now add the Query Screen Renderer properties
            startTAG(buffer, "queryScreenRendererProperties");
            {
                addFrameworkExtensionProperties(blockProperties.getQueryScreenRendererProperties(), buffer);
            }
            endTAG(buffer, "queryScreenRendererProperties");
            
            // Now add the insert Screen Renderer properties
            startTAG(buffer, "insertScreenRendererProperties");
            {
                addFrameworkExtensionProperties(blockProperties.getInsertScreenRendererProperties(), buffer);
            }
            endTAG(buffer, "insertScreenRendererProperties");
            
            // Now add the UpdateScreen Renderer properties
            startTAG(buffer, "updateScreenRendererProperties");
            {
                addFrameworkExtensionProperties(blockProperties.getUpdateScreenRendererProperties(), buffer);
            }
            endTAG(buffer, "updateScreenRendererProperties");
            
            if (!blockProperties.isMirrorChild())
            {
                // Now add the lov mappings
                startTAG(buffer, "lovMappingList");
                {
                    addLovMappingProperties(blockProperties, buffer);
                }
                endTAG(buffer, "lovMappingList");
            }
            
            // Now add the block items
            startTAG(buffer, "itemList");
            {
                addBlockItemProperties(blockProperties.getItemContainer(), buffer);
            }
            endTAG(buffer, "itemList");
            
            // Now add the main screen properties
            startTAG(buffer, "mainScreenProperties");
            {
                writeBooleanTAG(buffer, "displayFrame", blockProperties.getMainScreenProperties().getDisplayFrame());
                writeStringTAG(buffer, "frameTitle", blockProperties.getMainScreenProperties().getFrameTitle());
                writeIntTAG(buffer, "numCols", blockProperties.getMainScreenProperties().getNumCols());
                writeIntTAG(buffer, "height", blockProperties.getMainScreenProperties().getHeight());
                writeIntTAG(buffer, "width", blockProperties.getMainScreenProperties().getWidth());
                writeIntTAG(buffer, "horizontalSpan", blockProperties.getMainScreenProperties().getHorizontalSpan());
                writeIntTAG(buffer, "verticalSpan", blockProperties.getMainScreenProperties().getVerticalSpan());
                writeBooleanTAG(buffer, "expandHorizontally", blockProperties.getMainScreenProperties().canExpandHorizontally());
                writeBooleanTAG(buffer, "expandVertically", blockProperties.getMainScreenProperties().canExpandVertically());
            }
            endTAG(buffer, "mainScreenProperties");
            
            // Now add the main screen items
            startTAG(buffer, "mainScreen");
            {
                addMainScreenItemProperties(blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), buffer);
            }
            endTAG(buffer, "mainScreen");
            
            // Now add the query screen items
            startTAG(buffer, "queryScreen");
            {
                addQueryScreenItemProperties(blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), buffer);
            }
            endTAG(buffer, "queryScreen");
            
            // Now add the insert screen items
            startTAG(buffer, "insertScreen");
            {
                addInsertScreenItemProperties(blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), buffer);
            }
            endTAG(buffer, "insertScreen");
            
            // Now add the update screen items
            startTAG(buffer, "updateScreen");
            {
                addUpdateScreenItemProperties(blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), buffer);
            }
            endTAG(buffer, "updateScreen");
        }
        endTAG(buffer, "block");
    }
    
    protected void addLovMappingProperties(EJPluginBlockProperties blockProperties, StringBuffer buffer)
    {
        Iterator<EJPluginLovMappingProperties> mappings = blockProperties.getLovMappingContainer().getAllLovMappingProperties().iterator();
        while (mappings.hasNext())
        {
            EJPluginLovMappingProperties props = mappings.next();
            
            startOpenTAG(buffer, "lovMapping");
            {
                writePROPERTY(buffer, "name", props.getName());
                writePROPERTY(buffer, "lovDefinitionName", props.getLovDefinitionName());
                writePROPERTY(buffer, "executeAfterQuery", "" + props.executeAfterQuery());
                closeOpenTAG(buffer);
                
                writeStringTAG(buffer, "displayName", props.getLovDisplayName());
                
                if (!blockProperties.isMirrorChild())
                
                // now add the lov item mappings
                    startTAG(buffer, "lovItemMappingList");
                {
                    Iterator<EJPluginLovItemMappingProperties> itemMaps = props.getAllItemMappingProperties().iterator();
                    while (itemMaps.hasNext())
                    {
                        EJPluginLovItemMappingProperties itemProps = itemMaps.next();
                        startOpenTAG(buffer, "itemMap");
                        {
                            writePROPERTY(buffer, "lovDefinitionItem", itemProps.getLovDefinitionItemName());
                            writePROPERTY(buffer, "blockItemName", itemProps.getBlockItemName());
                            closeOpenTAG(buffer);
                        }
                        endTAG(buffer, "itemMap");
                    }
                }
                endTAG(buffer, "lovItemMappingList");
            }
            endTAG(buffer, "lovMapping");
        }
    }
    
    protected void addReferencedBlockProperties(EJPluginBlockProperties blockProperties, StringBuffer buffer)
    {
        startOpenTAG(buffer, "block");
        {
            writePROPERTY(buffer, "name", blockProperties.getName());
            writePROPERTY(buffer, "referenced", "true");
            writePROPERTY(buffer, "referencedBlockName", blockProperties.getReferencedBlockName());
            writePROPERTY(buffer, "controlBlock", "" + blockProperties.isControlBlock());
            closeOpenTAG(buffer);
            
            writeStringTAG(buffer, "description", blockProperties.getDescription());
            writeStringTAG(buffer, "canvasName", blockProperties.getCanvasName());
            // Now add the block items
            startTAG(buffer, "itemList");
            {
                addReferencedBlockItemProperties(blockProperties.getItemContainer(), buffer);
            }
            endTAG(buffer, "itemList");
            
            // Now add the main screen properties
            startTAG(buffer, "mainScreenProperties");
            {
                writeIntTAG(buffer, "numCols", blockProperties.getMainScreenProperties().getNumCols());
                writeIntTAG(buffer, "height", blockProperties.getMainScreenProperties().getHeight());
                writeIntTAG(buffer, "width", blockProperties.getMainScreenProperties().getWidth());
                writeIntTAG(buffer, "horizontalSpan", blockProperties.getMainScreenProperties().getHorizontalSpan());
                writeIntTAG(buffer, "verticalSpan", blockProperties.getMainScreenProperties().getVerticalSpan());
                writeBooleanTAG(buffer, "expandHorizontally", blockProperties.getMainScreenProperties().canExpandHorizontally());
                writeBooleanTAG(buffer, "expandVertically", blockProperties.getMainScreenProperties().canExpandVertically());
            }
            endTAG(buffer, "mainScreenProperties");
        }
        endTAG(buffer, "block");
    }
    protected void addObjectGroupBlockProperties(EJPluginBlockProperties blockProperties, StringBuffer buffer)
    {
        startOpenTAG(buffer, "block");
        {
            writePROPERTY(buffer, "name", blockProperties.getName());
            writePROPERTY(buffer, "objectgroup", blockProperties.getReferencedObjectGroupName());
            writePROPERTY(buffer, "referenced", "true");
            closeOpenTAG(buffer);
            
            
        }
        endTAG(buffer, "block");
    }
    
    protected void addBlockItemProperties(EJPluginBlockItemContainer itemContainer, StringBuffer buffer)
    {
        Iterator<EJPluginBlockItemProperties> items = itemContainer.getAllItemProperties().iterator();
        while (items.hasNext())
        {
            EJPluginBlockItemProperties itemProps = items.next();
            
            startOpenTAG(buffer, "item");
            {
                writePROPERTY(buffer, "name", itemProps.getName());
                closeOpenTAG(buffer);
                
                writeBooleanTAG(buffer, "blockServiceItem", itemProps.isBlockServiceItem());
                writeBooleanTAG(buffer, "mandatoryItem", itemProps.isMandatoryItem());
                writeStringTAG(buffer, "dataTypeClassName", itemProps.getDataTypeClassName());
                writeStringTAG(buffer, "itemRendererName", itemProps.getItemRendererName());
                writeStringTAG(buffer, "defaultInsertValue", itemProps.getDefaultInsertValue());
                writeStringTAG(buffer, "defaultQueryValue", itemProps.getDefaultQueryValue());
                
                // Now add the Item Renderer properties
                startTAG(buffer, "itemRendererProperties");
                {
                    addFrameworkExtensionProperties(itemProps.getItemRendererProperties(), buffer);
                }
                endTAG(buffer, "itemRendererProperties");
            }
            endTAG(buffer, "item");
        }
    }
    
    protected void addReferencedBlockItemProperties(EJPluginBlockItemContainer itemContainer, StringBuffer buffer)
    {
        Iterator<EJPluginBlockItemProperties> items = itemContainer.getAllItemProperties().iterator();
        while (items.hasNext())
        {
            EJPluginBlockItemProperties itemProps = items.next();
            boolean addQuery = itemProps.getDefaultQueryValue() != null && itemProps.getDefaultQueryValue().trim().length() > 0;
            boolean addInsert = itemProps.getDefaultInsertValue() != null && itemProps.getDefaultInsertValue().trim().length() > 0;
            
            if (addQuery || addInsert)
            {
                startOpenTAG(buffer, "item");
                {
                    writePROPERTY(buffer, "name", itemProps.getName());
                    closeOpenTAG(buffer);
                    if (addInsert) writeStringTAG(buffer, "defaultInsertValue", itemProps.getDefaultInsertValue());
                    if (addQuery) writeStringTAG(buffer, "defaultQueryValue", itemProps.getDefaultQueryValue());
                }
                endTAG(buffer, "item");
            }
        }
    }
    
    protected void addItemGroupProperties(EJItemGroupProperties itemGroup, StringBuffer buffer)
    {
        writeBooleanTAG(buffer, "displayFrame", itemGroup.dispayGroupFrame());
        writeStringTAG(buffer, "frameTitle", itemGroup.getFrameTitle());
        writeIntTAG(buffer, "numCols", itemGroup.getNumCols());
        writeIntTAG(buffer, "height", itemGroup.getHeight());
        writeIntTAG(buffer, "width", itemGroup.getWidth());
        writeIntTAG(buffer, "xspan", itemGroup.getXspan());
        writeIntTAG(buffer, "yspan", itemGroup.getYspan());
        writeBooleanTAG(buffer, "expandHorizontally", itemGroup.canExpandHorizontally());
        writeBooleanTAG(buffer, "expandVertically", itemGroup.canExpandVertically());
        writeStringTAG(buffer, "verticalAlignment", itemGroup.getVerticalAlignment().name());
        writeStringTAG(buffer, "horizontalAlignment", itemGroup.getHorizontalAlignment().name());
        if (itemGroup.getRendererProperties() != null)
        {
            
            startTAG(buffer, "rendererProperties");
            {
                addFrameworkExtensionProperties(itemGroup.getRendererProperties(), buffer);
            }
            endTAG(buffer, "rendererProperties");
        }
    }
    
    protected void addMainScreenItemProperties(EJItemGroupPropertiesContainer itemContainer, StringBuffer buffer)
    {
        startTAG(buffer, "itemGroupList");
        {
            Iterator<EJItemGroupProperties> itemGroups = itemContainer.getAllItemGroupProperties().iterator();
            while (itemGroups.hasNext())
            {
                EJItemGroupProperties itemGroup = itemGroups.next();
                
                startOpenTAG(buffer, "itemGroup");
                {
                    writePROPERTY(buffer, "name", itemGroup.getName());
                    closeOpenTAG(buffer);
                    
                    addItemGroupProperties(itemGroup, buffer);
                    
                    startTAG(buffer, "itemGroupItemList");
                    {
                        Iterator<EJScreenItemProperties> items = itemGroup.getAllItemProperties().iterator();
                        while (items.hasNext())
                        {
                            EJPluginScreenItemProperties itemProps = (EJPluginScreenItemProperties) items.next();
                            
                            startOpenTAG(buffer, "item");
                            {
                                writePROPERTY(buffer, "referencedItemName", itemProps.getReferencedItemName());
                                writePROPERTY(buffer, "isSpacerItem", "" + itemProps.isSpacerItem());
                                closeOpenTAG(buffer);
                                
                                writeStringTAG(buffer, "label", itemProps.getLabel());
                                writeStringTAG(buffer, "hint", itemProps.getHint());
                                writeBooleanTAG(buffer, "editAllowed", itemProps.isEditAllowed());
                                writeBooleanTAG(buffer, "visible", itemProps.isVisible());
                                writeBooleanTAG(buffer, "mandatory", itemProps.isMandatory());
                                writeBooleanTAG(buffer, "enableLovNotification", itemProps.isLovNotificationEnabled());
                                writeStringTAG(buffer, "lovMappingName", itemProps.getLovMappingName());
                                writeBooleanTAG(buffer, "validateFromLov", itemProps.validateFromLov());
                                writeStringTAG(buffer, "actionCommand", itemProps.getActionCommand());
                                
                                // Now add the Block Renderer required
                                // properties
                                if (!itemProps.getBlockProperties().isUsedInLovDefinition())
                                {
                                    
                                    startTAG(buffer, "blockRendererRequiredProperties");
                                    {
                                        addFrameworkExtensionProperties(((EJPluginMainScreenItemProperties) itemProps).getBlockRendererRequiredProperties(),
                                                buffer);
                                    }
                                    endTAG(buffer, "blockRendererRequiredProperties");
                                }
                                else
                                {
                                    // Now add the Lov Renderer required
                                    // properties
                                    startTAG(buffer, "lovRendererRequiredProperties");
                                    {
                                        addFrameworkExtensionProperties(((EJPluginMainScreenItemProperties) itemProps).getLovRendererRequiredProperties(),
                                                buffer);
                                    }
                                    endTAG(buffer, "lovRendererRequiredProperties");
                                }
                            }
                            endTAG(buffer, "item");
                        }
                    }
                    endTAG(buffer, "itemGroupItemList");
                    
                    // Now add any sub groups
                    addMainScreenItemProperties(itemGroup.getChildItemGroupContainer(), buffer);
                    
                }
                endTAG(buffer, "itemGroup");
            }
        }
        endTAG(buffer, "itemGroupList");
    }
    
    protected void addQueryScreenItemProperties(EJItemGroupPropertiesContainer itemContainer, StringBuffer buffer)
    {
        startTAG(buffer, "itemGroupList");
        {
            Iterator<EJItemGroupProperties> itemGroups = itemContainer.getAllItemGroupProperties().iterator();
            while (itemGroups.hasNext())
            {
                EJItemGroupProperties itemGroup = itemGroups.next();
                
                startOpenTAG(buffer, "itemGroup");
                {
                    writePROPERTY(buffer, "name", itemGroup.getName());
                    closeOpenTAG(buffer);
                    
                    addItemGroupProperties(itemGroup, buffer);
                    
                    startTAG(buffer, "itemGroupItemList");
                    {
                        Iterator<EJScreenItemProperties> items = itemGroup.getAllItemProperties().iterator();
                        while (items.hasNext())
                        {
                            EJPluginQueryScreenItemProperties itemProps = (EJPluginQueryScreenItemProperties) items.next();
                            
                            startOpenTAG(buffer, "item");
                            {
                                writePROPERTY(buffer, "referencedItemName", itemProps.getReferencedItemName());
                                writePROPERTY(buffer, "isSpacerItem", "" + itemProps.isSpacerItem());
                                closeOpenTAG(buffer);
                                
                                writeStringTAG(buffer, "label", itemProps.getLabel());
                                writeStringTAG(buffer, "hint", itemProps.getHint());
                                writeBooleanTAG(buffer, "editAllowed", itemProps.isEditAllowed());
                                writeBooleanTAG(buffer, "visible", itemProps.isVisible());
                                writeBooleanTAG(buffer, "mandatory", itemProps.isMandatory());
                                writeBooleanTAG(buffer, "enableLovNotification", itemProps.isLovNotificationEnabled());
                                writeStringTAG(buffer, "lovMappingName", itemProps.getLovMappingName());
                                writeBooleanTAG(buffer, "validateFromLov", itemProps.validateFromLov());
                                writeStringTAG(buffer, "actionCommand", itemProps.getActionCommand());
                                
                                // Now add the Query Screen Renderer Item
                                // properties
                                startTAG(buffer, "queryScreenRendererItemProperties");
                                {
                                    addFrameworkExtensionProperties(itemProps.getQueryScreenRendererRequiredProperties(), buffer);
                                }
                                endTAG(buffer, "queryScreenRendererItemProperties");
                                
                            }
                            endTAG(buffer, "item");
                        }
                    }
                    endTAG(buffer, "itemGroupItemList");
                    
                    // Now add any sub groups
                    addQueryScreenItemProperties(itemGroup.getChildItemGroupContainer(), buffer);
                }
                endTAG(buffer, "itemGroup");
            }
        }
        endTAG(buffer, "itemGroupList");
    }
    
    protected void addInsertScreenItemProperties(EJItemGroupPropertiesContainer itemContainer, StringBuffer buffer)
    {
        startTAG(buffer, "itemGroupList");
        {
            Iterator<EJItemGroupProperties> itemGroups = itemContainer.getAllItemGroupProperties().iterator();
            while (itemGroups.hasNext())
            {
                EJItemGroupProperties itemGroup = itemGroups.next();
                
                startOpenTAG(buffer, "itemGroup");
                {
                    writePROPERTY(buffer, "name", itemGroup.getName());
                    closeOpenTAG(buffer);
                    
                    addItemGroupProperties(itemGroup, buffer);
                    
                    startTAG(buffer, "itemGroupItemList");
                    {
                        Iterator<EJScreenItemProperties> items = itemGroup.getAllItemProperties().iterator();
                        while (items.hasNext())
                        {
                            EJPluginInsertScreenItemProperties itemProps = (EJPluginInsertScreenItemProperties) items.next();
                            
                            startOpenTAG(buffer, "item");
                            {
                                writePROPERTY(buffer, "referencedItemName", itemProps.getReferencedItemName());
                                writePROPERTY(buffer, "isSpacerItem", "" + itemProps.isSpacerItem());
                                closeOpenTAG(buffer);
                                
                                writeStringTAG(buffer, "label", itemProps.getLabel());
                                writeStringTAG(buffer, "hint", itemProps.getHint());
                                writeBooleanTAG(buffer, "editAllowed", itemProps.isEditAllowed());
                                writeBooleanTAG(buffer, "visible", itemProps.isVisible());
                                writeBooleanTAG(buffer, "mandatory", itemProps.isMandatory());
                                writeBooleanTAG(buffer, "enableLovNotification", itemProps.isLovNotificationEnabled());
                                writeStringTAG(buffer, "lovMappingName", itemProps.getLovMappingName());
                                writeBooleanTAG(buffer, "validateFromLov", itemProps.validateFromLov());
                                writeStringTAG(buffer, "actionCommand", itemProps.getActionCommand());
                                
                                // Now add the insert Screen Renderer Item
                                // properties
                                startTAG(buffer, "insertScreenRendererItemProperties");
                                {
                                    addFrameworkExtensionProperties(itemProps.getInsertScreenRendererRequiredProperties(), buffer);
                                }
                                endTAG(buffer, "insertScreenRendererItemProperties");
                            }
                            endTAG(buffer, "item");
                        }
                    }
                    endTAG(buffer, "itemGroupItemList");
                    
                    // Now add any sub groups
                    addInsertScreenItemProperties(itemGroup.getChildItemGroupContainer(), buffer);
                    
                }
                endTAG(buffer, "itemGroup");
            }
        }
        endTAG(buffer, "itemGroupList");
    }
    
    protected void addUpdateScreenItemProperties(EJItemGroupPropertiesContainer itemContainer, StringBuffer buffer)
    {
        startTAG(buffer, "itemGroupList");
        {
            Iterator<EJItemGroupProperties> itemGroups = itemContainer.getAllItemGroupProperties().iterator();
            while (itemGroups.hasNext())
            {
                EJItemGroupProperties itemGroup = itemGroups.next();
                startOpenTAG(buffer, "itemGroup");
                {
                    writePROPERTY(buffer, "name", itemGroup.getName());
                    closeOpenTAG(buffer);
                    
                    addItemGroupProperties(itemGroup, buffer);
                    
                    startTAG(buffer, "itemGroupItemList");
                    {
                        Iterator<EJScreenItemProperties> items = itemGroup.getAllItemProperties().iterator();
                        while (items.hasNext())
                        {
                            EJPluginUpdateScreenItemProperties itemProps = (EJPluginUpdateScreenItemProperties) items.next();
                            
                            startOpenTAG(buffer, "item");
                            {
                                writePROPERTY(buffer, "referencedItemName", itemProps.getReferencedItemName());
                                writePROPERTY(buffer, "isSpacerItem", "" + itemProps.isSpacerItem());
                                closeOpenTAG(buffer);
                                
                                writeStringTAG(buffer, "label", itemProps.getLabel());
                                writeStringTAG(buffer, "hint", itemProps.getHint());
                                writeBooleanTAG(buffer, "editAllowed", itemProps.isEditAllowed());
                                writeBooleanTAG(buffer, "visible", itemProps.isVisible());
                                writeBooleanTAG(buffer, "mandatory", itemProps.isMandatory());
                                writeBooleanTAG(buffer, "enableLovNotification", itemProps.isLovNotificationEnabled());
                                writeStringTAG(buffer, "lovMappingName", itemProps.getLovMappingName());
                                writeBooleanTAG(buffer, "validateFromLov", itemProps.validateFromLov());
                                writeStringTAG(buffer, "actionCommand", itemProps.getActionCommand());
                                
                                // Now add the UpdateScreen Renderer Item
                                // properties
                                startTAG(buffer, "updateScreenRendererItemProperties");
                                {
                                    addFrameworkExtensionProperties(itemProps.getUpdateScreenRendererRequiredProperties(), buffer);
                                }
                                endTAG(buffer, "updateScreenRendererItemProperties");
                            }
                            endTAG(buffer, "item");
                        }
                    }
                    endTAG(buffer, "itemGroupItemList");
                    
                    // Now add any sub groups
                    addUpdateScreenItemProperties(itemGroup.getChildItemGroupContainer(), buffer);
                }
                endTAG(buffer, "itemGroup");
            }
        }
        endTAG(buffer, "itemGroupList");
    }
    
    protected void addTabPageProperties(EJCanvasProperties canvasProperties, StringBuffer buffer)
    {
        // Only do this for tab canvases
        if (canvasProperties.getType() != EJCanvasType.TAB)
        {
            return;
        }
        
        Iterator<EJTabPageProperties> tabPagePropertiesIti = canvasProperties.getTabPageContainer().getAllTabPageProperties().iterator();
        startTAG(buffer, "tabPageList");
        {
            while (tabPagePropertiesIti.hasNext())
            {
                EJTabPageProperties tabPage = tabPagePropertiesIti.next();
                
                startOpenTAG(buffer, "tabPage");
                {
                    writePROPERTY(buffer, "name", tabPage.getName());
                    closeOpenTAG(buffer);
                    
                    writeStringTAG(buffer, "pageTitle", tabPage.getPageTitle());
                    writeStringTAG(buffer, "firstNavigationalBlock", tabPage.getFirstNavigationalBlock());
                    writeStringTAG(buffer, "firstNavigationalItem", tabPage.getFirstNavigationalItem());
                    writeBooleanTAG(buffer, "enabled", tabPage.isEnabled());
                    writeBooleanTAG(buffer, "visible", tabPage.isVisible());
                    writeIntTAG(buffer, "numCols", tabPage.getNumCols());
                    
                    startTAG(buffer, "canvasList");
                    {
                        addCanvasList(tabPage.getContainedCanvases(), buffer);
                    }
                    endTAG(buffer, "canvasList");
                }
                endTAG(buffer, "tabPage");
            }
        }
        endTAG(buffer, "tabPageList");
    }
    
    protected void addStackedPageProperties(EJCanvasProperties canvasProperties, StringBuffer buffer)
    {
        // Only do this for tab canvases
        if (canvasProperties.getType() != EJCanvasType.STACKED)
        {
            return;
        }
        
        Iterator<EJStackedPageProperties> stackedPagePropertiesIti = canvasProperties.getStackedPageContainer().getAllStackedPageProperties().iterator();
        
        startTAG(buffer, "stackedPageList");
        {
            while (stackedPagePropertiesIti.hasNext())
            {
                EJStackedPageProperties stackedPage = stackedPagePropertiesIti.next();
                
                startOpenTAG(buffer, "stackedPage");
                {
                    writePROPERTY(buffer, "name", stackedPage.getName());
                    closeOpenTAG(buffer);
                    
                    writeStringTAG(buffer, "backgroundColourVisualAttributeName", stackedPage.getBackgroundColourVisualAttributeName());
                    writeStringTAG(buffer, "foregroundColourVisualAttributeName", stackedPage.getForegroundColourVisualAttributeName());
                    writeStringTAG(buffer, "firstNavigationalBlock", stackedPage.getFirstNavigationalBlock());
                    writeStringTAG(buffer, "firstNavigationalItem", stackedPage.getFirstNavigationalItem());
                    writeIntTAG(buffer, "numCols", stackedPage.getNumCols());
                    
                    startTAG(buffer, "canvasList");
                    {
                        addCanvasList(stackedPage.getContainedCanvases(), buffer);
                    }
                    endTAG(buffer, "canvasList");
                }
                endTAG(buffer, "stackedPage");
            }
        }
        endTAG(buffer, "stackedPageList");
    }
    
    protected void addCanvasGroupCanvases(EJCanvasProperties canvasProperties, StringBuffer buffer)
    {
        // Only do this for group canvases
        if (canvasProperties.getType() != EJCanvasType.GROUP)
        {
            return;
        }
        
        startTAG(buffer, "groupCanvasList");
        {
            addCanvasList(canvasProperties.getGroupCanvasContainer(), buffer);
        }
        endTAG(buffer, "groupCanvasList");
    }
    
    protected void addCanvasSplitCanvases(EJCanvasProperties canvasProperties, StringBuffer buffer)
    {
        // Only do this for group canvases
        if (canvasProperties.getType() != EJCanvasType.SPLIT)
        {
            return;
        }
        
        startTAG(buffer, "groupCanvasList");
        {
            addCanvasList(canvasProperties.getSplitCanvasContainer(), buffer);
        }
        endTAG(buffer, "groupCanvasList");
    }
}
