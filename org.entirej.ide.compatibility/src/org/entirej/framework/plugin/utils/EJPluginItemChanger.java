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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
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
    public static interface UndoProvider
    {
        void undo();
    }
    
    public static List<UndoProvider> renameItemOnForm(EJPluginBlockProperties blockProperties, String oldName, String newName)
    {
        List<UndoProvider> undoProviders = new ArrayList<UndoProvider>();
        changeItem(undoProviders, blockProperties, oldName, newName, false);
        
        return undoProviders;
    }
    
    public static List<UndoProvider> deleteItemOnForm(EJPluginBlockProperties blockProperties, String itemName)
    {
        List<UndoProvider> undoProviders = new ArrayList<UndoProvider>();
        changeItem(undoProviders, blockProperties, itemName, "", true);
        
        return undoProviders;
    }
    
    public static List<UndoProvider> removeItemActionCommand(EJPluginBlockProperties blockProperties, String itemName)
    {
        List<UndoProvider> undoProviders = new ArrayList<UndoProvider>();
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), itemName);
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), itemName);
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), itemName);
        removeActionCommandInContainer(blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), itemName);
        
        return undoProviders;
    }
    
    private static void changeItem(List<UndoProvider> undoProviders, EJPluginBlockProperties blockProperties, final String oldName, final String newName,
            boolean deleteReference)
    {
        // Rename the main screen items
        
        if (deleteReference)
        {
            removeItemFromContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), oldName);
            removeItemFromContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), oldName);
            removeItemFromContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), oldName);
            removeItemFromContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), oldName);
        }
        else
        {
            renameItemInContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), oldName, newName);
            renameItemInContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), oldName, newName);
            renameItemInContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), oldName, newName);
            renameItemInContainer(undoProviders, blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), oldName, newName);
        }
        
        // Now check if item is being used on an LOV Mapping and update it
        // accordingly
        Iterator<EJPluginLovMappingProperties> allLovMappings = blockProperties.getLovMappingContainer().getAllLovMappingProperties().iterator();
        while (allLovMappings.hasNext())
        {
            Iterator<EJPluginLovItemMappingProperties> allMappedItems = allLovMappings.next().getAllItemMappingProperties().iterator();
            while (allMappedItems.hasNext())
            {
                final EJPluginLovItemMappingProperties mappedItem = allMappedItems.next();
                if (mappedItem.getBlockItemName() != null && mappedItem.getBlockItemName().equals(oldName))
                {
                    if (deleteReference)
                    {
                        mappedItem.setBlockItemName("");
                        undoProviders.add(new UndoProvider()
                        {
                            
                            @Override
                            public void undo()
                            {
                                mappedItem.setBlockItemName(oldName);
                                
                            }
                        });
                    }
                    else
                    {
                        mappedItem.setBlockItemName(newName);
                        undoProviders.add(new UndoProvider()
                        {
                            
                            @Override
                            public void undo()
                            {
                                mappedItem.setBlockItemName(oldName);
                                
                            }
                        });
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
                        final EJPluginLovItemMappingProperties mappedItem = allMappedItems.next();
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
                            undoProviders.add(new UndoProvider()
                            {
                                
                                @Override
                                public void undo()
                                {
                                    mappedItem.setLovDefinitionItemName(oldName);
                                    
                                }
                            });
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
                    final EJPluginRelationJoinProperties joinProperties = allRelJoins.next();
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
                        undoProviders.add(new UndoProvider()
                        {
                            
                            @Override
                            public void undo()
                            {
                                joinProperties.setDetailItemName(oldName);
                                
                            }
                        });
                    }
                }
            }
            
            if (relation.getMasterBlockName().equals(blockProperties.getName()))
            {
                Iterator<EJPluginRelationJoinProperties> allRelJoins = relation.getRelationJoins().iterator();
                while (allRelJoins.hasNext())
                {
                    final EJPluginRelationJoinProperties joinProperties = allRelJoins.next();
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
                        
                        undoProviders.add(new UndoProvider()
                        {
                            
                            @Override
                            public void undo()
                            {
                                joinProperties.setDetailItemName(oldName);
                                
                            }
                        });
                    }
                }
            }
            
        }
        
        // Now rename the item on all mirrored blocks
        if (blockProperties.isMirrorBlock() && blockProperties.getMirrorParent() == null)
        {
            for (final EJPluginBlockProperties childBlock : blockProperties.getMirrorChildren())
            {
                if (childBlock.getItemContainer().getItemProperties(oldName) != null)
                {
                    childBlock.getItemContainer().getItemProperties(oldName).setName(newName);
                    undoProviders.add(new UndoProvider()
                    {
                        
                        @Override
                        public void undo()
                        {
                            childBlock.getItemContainer().getItemProperties(newName).setName(oldName);
                            
                        }
                    });
                    
                }
                changeItem(undoProviders, childBlock, oldName, newName, deleteReference);
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
            for (final EJPluginBlockItemProperties blockItemProperties : itemProperties)
            {
                
                String insertValue = blockItemProperties.getDefaultInsertValue();
                if (insertValue != null && insertValue.trim().length() > 0 && insertValue.indexOf(":") > 0)
                {
                    if ("BLOCK_ITEM".equals(insertValue.substring(0, insertValue.indexOf(":"))))
                    {
                        String value = insertValue.substring(insertValue.indexOf(":") + 1);
                        final String[] split = value.split("\\.");
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
                                undoProviders.add(new UndoProvider()
                                {
                                    
                                    @Override
                                    public void undo()
                                    {
                                        blockItemProperties.setDefaultInsertValue(String.format("BLOCK_ITEM:%s.%s", split[0], oldName));
                                        
                                    }
                                });
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
                        final String[] split = value.split("\\.");
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
                                
                                undoProviders.add(new UndoProvider()
                                {
                                    
                                    @Override
                                    public void undo()
                                    {
                                        blockItemProperties.setDefaultQueryValue(String.format("BLOCK_ITEM:%s.%s", split[0], oldName));
                                        
                                    }
                                });
                            }
                        }
                    }
                    
                }
            }
        }
        
        // renderer changes
        
        EJDevBlockRendererDefinition rendererDefinition = blockProperties.getBlockRendererDefinition();
        EJFrameworkExtensionProperties rendererProperties = blockProperties.getBlockRendererProperties();
        if (rendererDefinition != null)
        {
            
            validateRendererProperties(oldName, newName, blockProperties.getFormProperties(), blockProperties,
                    rendererDefinition.getBlockPropertyDefinitionGroup(), rendererProperties);
        }
        
    }
    
    private static void removeItemFromContainer(List<UndoProvider> undoProviders, EJPluginItemGroupContainer container, String name)
    {
        Iterator<EJPluginItemGroupProperties> allItemGroups = container.getAllResequancableItems().iterator();
        while (allItemGroups.hasNext())
        {
            final EJPluginItemGroupProperties props = allItemGroups.next();
            
            if (props.containsItemReference(name))
            {
                final EJPluginScreenItemProperties item = props.getItemPropertiesForBlockItem(name);
                final int index = props.deleteItem(item);
                undoProviders.add(new UndoProvider()
                {
                    
                    @Override
                    public void undo()
                    {
                        props.addItemProperties(index, item);
                        
                    }
                });
            }
            
            // Now check all child item groups
            removeItemFromContainer(undoProviders, props.getChildItemGroupContainer(), name);
        }
    }
    
    private static void renameItemInContainer(List<UndoProvider> undoProviders, EJItemGroupPropertiesContainer container, final String oldName,
            final String newName)
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
                    final EJPluginScreenItemProperties itemProperties = (EJPluginScreenItemProperties) allItemProps.next();
                    if (itemProperties.getName() != null && itemProperties.getName().equals(oldName))
                    {
                        itemProperties.internalSetName(newName);
                        undoProviders.add(new UndoProvider()
                        {
                            
                            @Override
                            public void undo()
                            {
                                itemProperties.internalSetName(oldName);
                                
                            }
                        });
                    }
                }
            }
            
            // Now check all child item groups
            renameItemInContainer(undoProviders, props.getChildItemGroupContainer(), oldName, newName);
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
    
    static void validatePropertyDefinitionGroup(final String oldName, final String newName, EJPluginFormProperties formProperties,
            EJPluginBlockProperties blockProperties, EJFrameworkExtensionProperties rendererProperties, EJPropertyDefinitionGroup definitionGroup)
    {
        if (definitionGroup == null) return;
        
        Collection<EJPropertyDefinition> propertyDefinitions = definitionGroup.getPropertyDefinitions();
        for (EJPropertyDefinition definition : propertyDefinitions)
        {
            validatePropertyDefinition(oldName, newName, formProperties, blockProperties, rendererProperties, definitionGroup, definition);
        }
        
        // handle sub groups
        Collection<EJPropertyDefinitionGroup> subGroups = definitionGroup.getSubGroups();
        for (final EJPropertyDefinitionGroup subGroup : subGroups)
        {
            validatePropertyDefinitionGroup(oldName, newName, formProperties, blockProperties, rendererProperties, subGroup);
        }
        
    }
    
    static void validatePropertyDefinition(final String oldName, final String newName, EJPluginFormProperties formProperties,
            EJPluginBlockProperties blockProperties, EJFrameworkExtensionProperties rendererProperties, EJPropertyDefinitionGroup definitionGroup,
            EJPropertyDefinition definition)
    {
        
        final String groupName;
        if (definitionGroup.getFullGroupName() == null || definitionGroup.getFullGroupName().trim().length() == 0)
        {
            groupName = definition.getName();
        }
        else
        {
            groupName = String.format("%s.%s", definitionGroup.getFullGroupName(), definition.getName());
        }
        
        String strValue = rendererProperties.getStringProperty(groupName);
        boolean vlaueNull = (strValue == null || strValue.trim().length() == 0);
        
        if (vlaueNull) return;
        
        final EJPropertyDefinitionType dataType = definition.getPropertyType();
        switch (dataType)
        {
            case BLOCK_ITEM:
            {
                if (oldName.equals(strValue))
                {
                    rendererProperties.setPropertyValue(groupName, newName);
                }
            }
                break;
            default:
                break;
        
        }
        
    }
    
    static void validateRendererProperties(final String oldName, final String newName, EJPluginFormProperties formProperties,
            EJPluginBlockProperties blockProperties, EJPropertyDefinitionGroup definitionGroup, EJFrameworkExtensionProperties rendererProperties)
    {
        if (definitionGroup != null && rendererProperties != null)
        {
            validatePropertyDefinitionGroup(oldName, newName, formProperties, blockProperties, rendererProperties, definitionGroup);
        }
    }
}
