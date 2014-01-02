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
package org.entirej.framework.plugin.utils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovItemMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationJoinProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;

public class EJPluginItemChanger
{
    public static void renameItemOnForm(EJPluginBlockProperties blockProperties, String oldName, String newName)
    {
        changeItem(blockProperties, oldName, newName, false);
    }
    
    public static void deleteItemOnForm(EJPluginBlockProperties blockProperties, String itemName)
    {
        changeItem(blockProperties, itemName, "", true);
    }
    
    public static void removeItemActionCommand(EJPluginBlockProperties blockProperties, String itemName)
    {
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), itemName);
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), itemName);
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), itemName);
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), itemName);
    }
    
    private static void changeItem(EJPluginBlockProperties blockProperties, String oldName, String newName, boolean deleteReference)
    {
        // Rename the main screen items
        
        if (deleteReference)
        {
            removeItemFromContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), oldName);
            removeItemFromContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), oldName);
            removeItemFromContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), oldName);
            removeItemFromContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), oldName);
        }
        else
        {
            renameItemInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), oldName, newName);
            renameItemInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), oldName, newName);
            renameItemInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), oldName, newName);
            renameItemInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), oldName, newName);
        }
        
        // Now check if item is being used on an LOV Mapping and update it
        // accordingly
        Iterator<EJPluginLovMappingProperties> allLovMappings = blockProperties.getLovMappingContainer().getAllLovMappingProperties().iterator();
        while (allLovMappings.hasNext())
        {
            Iterator<EJPluginLovItemMappingProperties> allMappedItems = allLovMappings.next().getAllItemMappingProperties().iterator();
            while (allMappedItems.hasNext())
            {
                EJPluginLovItemMappingProperties mappedItem = allMappedItems.next();
                if (mappedItem.getBlockItemName() != null && mappedItem.getBlockItemName().equals(oldName))
                {
                    if (deleteReference)
                    {
                        mappedItem.setBlockItemName("");
                    }
                    else
                    {
                        mappedItem.setBlockItemName(newName);
                    }
                }
            }
        }
        
        // If the item being renamed belongs to a block that is used within an
        // LOV Definition, then all LOV mappings on the form that use the
        // definition will need to be checked and renamed accordingly
        // Now check if item is being used on an LOV Mapping and update it
        // accordingly
        if (blockProperties.isUsedInLovDefinition())
        {
            Iterator<EJPluginBlockProperties> allBlocks = blockProperties.getFormProperties().getBlockContainer().getAllBlockProperties().iterator();
            while (allBlocks.hasNext())
            {
                Iterator<EJPluginLovMappingProperties> allBlockLovMappings = allBlocks.next().getLovMappingContainer().getAllLovMappingProperties().iterator();
                while (allBlockLovMappings.hasNext())
                {
                    Iterator<EJPluginLovItemMappingProperties> allMappedItems = allBlockLovMappings.next().getAllItemMappingProperties().iterator();
                    while (allMappedItems.hasNext())
                    {
                        EJPluginLovItemMappingProperties mappedItem = allMappedItems.next();
                        if (mappedItem.getLovDefinitionItemName() != null && mappedItem.getLovDefinitionItemName().equals(oldName))
                        {
                            if (deleteReference)
                            {
                                mappedItem.setLovDefinitionItemName("");
                            }
                            else
                            {
                                mappedItem.setLovDefinitionItemName(newName);
                            }
                        }
                    }
                }
            }
        }
        
        // If the block is used in a relation, then I also need to update the
        // item name if it is used
        Iterator<EJPluginRelationProperties> allRelations = blockProperties.getFormProperties().getRelationContainer().getAllRelationProperties().iterator();
        while (allRelations.hasNext())
        {
            EJPluginRelationProperties relation = allRelations.next();
            if (relation.getDetailBlockName().equals(blockProperties.getName()))
            {
                Iterator<EJPluginRelationJoinProperties> allRelJoins = relation.getRelationJoins().iterator();
                while (allRelJoins.hasNext())
                {
                    EJPluginRelationJoinProperties joinProperties = allRelJoins.next();
                    if (joinProperties.getDetailItemName().equals(oldName))
                    {
                        if (deleteReference)
                        {
                            joinProperties.setDetailItemName("");
                        }
                        else
                        {
                            joinProperties.setDetailItemName(newName);
                        }
                    }
                }
            }
            
            if (relation.getMasterBlockName().equals(blockProperties.getName()))
            {
                Iterator<EJPluginRelationJoinProperties> allRelJoins = relation.getRelationJoins().iterator();
                while (allRelJoins.hasNext())
                {
                    EJPluginRelationJoinProperties joinProperties = allRelJoins.next();
                    if (joinProperties.getMasterItemName().equals(oldName))
                    {
                        if (deleteReference)
                        {
                            joinProperties.setMasterItemName("");
                        }
                        else
                        {
                            joinProperties.setMasterItemName(newName);
                        }
                    }
                }
            }
        }
        
        // Now rename the item on all mirrored blocks
        if (blockProperties.isMirrorBlock() && blockProperties.getMirrorParent() == null)
        {
            for (EJPluginBlockProperties childBlock : blockProperties.getMirrorChildren())
            {
                if (childBlock.getItemContainer().getItemProperties(oldName) != null)
                {
                    childBlock.getItemContainer().getItemProperties(oldName).setName(newName);
                }
                changeItem(childBlock, oldName, newName, deleteReference);
            }
        }
        
        List<EJPluginBlockProperties> allBlockProperties = new ArrayList<EJPluginBlockProperties>(blockProperties.getFormProperties().getBlockContainer()
                .getAllBlockProperties());
        List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = blockProperties.getFormProperties().getLovDefinitionContainer()
                .getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties lovDefinitionProperties : allLovDefinitionProperties)
        {
            allBlockProperties.add(lovDefinitionProperties.getBlockProperties());
        }
        for (EJPluginBlockProperties properties : allBlockProperties)
        {
            List<EJPluginBlockItemProperties> itemProperties = properties.getItemContainer().getAllItemProperties();
            for (EJPluginBlockItemProperties blockItemProperties : itemProperties)
            {
                
                String insertValue = blockItemProperties.getDefaultInsertValue();
                if (insertValue != null && insertValue.trim().length() > 0 && insertValue.indexOf(":") > 0)
                {
                    if ("BLOCK_ITEM".equals(insertValue.substring(0, insertValue.indexOf(":"))))
                    {
                        String value = insertValue.substring(insertValue.indexOf(":") + 1);
                        String[] split = value.split("\\.");
                        if (split.length == 2)
                        {
                            if (blockProperties.getName().equals(split[0]) && oldName.equals(split[1]))
                            {
                                if (deleteReference)
                                {
                                    blockItemProperties.setDefaultInsertValue("");
                                }
                                else
                                {
                                    blockItemProperties.setDefaultInsertValue(String.format("BLOCK_ITEM:%s.%s", split[0], newName));
                                }
                            }
                        }
                    }
                    
                }
                
                String queryValue = blockItemProperties.getDefaultQueryValue();
                if (queryValue != null && queryValue.trim().length() > 0 && queryValue.indexOf(":") > 0)
                {
                    if ("BLOCK_ITEM".equals(queryValue.substring(0, queryValue.indexOf(":"))))
                    {
                        String value = queryValue.substring(queryValue.indexOf(":") + 1);
                        String[] split = value.split("\\.");
                        if (split.length == 2)
                        {
                            if (blockProperties.getName().equals(split[0]) && oldName.equals(split[1]))
                            {
                                if (deleteReference)
                                {
                                    blockItemProperties.setDefaultQueryValue("");
                                }
                                else
                                {
                                    blockItemProperties.setDefaultQueryValue(String.format("BLOCK_ITEM:%s.%s", split[0], newName));
                                }
                            }
                        }
                    }
                    
                }
            }
        }
        
    }
    
    private static void removeItemFromContainer(EJPluginItemGroupContainer container, String name)
    {
        Iterator<EJPluginItemGroupProperties> allItemGroups = container.getAllResequancableItems().iterator();
        while (allItemGroups.hasNext())
        {
            EJPluginItemGroupProperties props = allItemGroups.next();
            
            if (props.containsItemReference(name))
            {
                props.deleteItem(name);
            }
            
            // Now check all child item groups
            removeItemFromContainer(props.getChildItemGroupContainer(), name);
        }
    }
    
    private static void renameItemInContainer(EJItemGroupPropertiesContainer container, String oldName, String newName)
    {
        Iterator<EJItemGroupProperties> allItemGroups = container.getAllItemGroupProperties().iterator();
        while (allItemGroups.hasNext())
        {
            EJItemGroupProperties props = allItemGroups.next();
            
            if (props.containsItemReference(oldName))
            {
                Iterator<EJScreenItemProperties> allItemProps = props.getAllItemProperties().iterator();
                while (allItemProps.hasNext())
                {
                    EJPluginScreenItemProperties itemProperties = (EJPluginScreenItemProperties) allItemProps.next();
                    if (itemProperties.getName() != null && itemProperties.getName().equals(oldName))
                    {
                        itemProperties.internalSetName(newName);
                    }
                }
            }
            
            // Now check all child item groups
            renameItemInContainer(props.getChildItemGroupContainer(), oldName, newName);
        }
    }
    
    private static void removeActionCommandInContainer(EJItemGroupPropertiesContainer container, String item)
    {
        Iterator<EJItemGroupProperties> allItemGroups = container.getAllItemGroupProperties().iterator();
        while (allItemGroups.hasNext())
        {
            EJItemGroupProperties props = allItemGroups.next();
            
            if (props.containsItemReference(item))
            {
                Iterator<EJScreenItemProperties> allItemProps = props.getAllItemProperties().iterator();
                while (allItemProps.hasNext())
                {
                    EJPluginScreenItemProperties itemProperties = (EJPluginScreenItemProperties) allItemProps.next();
                    if (itemProperties.getName() != null && itemProperties.getName().equals(item))
                    {
                        itemProperties.setActionCommand("");
                    }
                }
            }
            
            // Now check all child item groups
            removeActionCommandInContainer(props.getChildItemGroupContainer(), item);
        }
    }
    
}
