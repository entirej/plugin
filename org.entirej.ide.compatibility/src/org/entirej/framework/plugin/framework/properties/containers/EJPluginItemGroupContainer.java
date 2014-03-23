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
package org.entirej.framework.plugin.framework.properties.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevItemGroupDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevItemGroupDisplayPropertiesContainer;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;

public class EJPluginItemGroupContainer implements EJItemGroupPropertiesContainer, EJDevItemGroupDisplayPropertiesContainer
{
    /**
     * 
     */
    private static final long serialVersionUID = -4267593072417662170L;

    boolean                                   _isRoot       = false;
    
    public static final int                   MAIN_SCREEN   = 0;
    public static final int                   INSERT_SCREEN = 1;
    public static final int                   UPDATE_SCREEN = 2;
    public static final int                   QUERY_SCREEN  = 3;
    
    private EJPluginItemGroupProperties       _parentItemGroup;
    private int                               _containerType;
    private EJPluginBlockProperties           _blockProperties;
    private List<EJPluginItemGroupProperties> _itemGroups;
    
    public EJPluginItemGroupContainer(EJPluginBlockProperties blockProperties, int containerType)
    {
        this(blockProperties, null, containerType);
    }
    
    public void dispose()
    {
        _parentItemGroup = null;
        _blockProperties = null;
        _itemGroups.clear();
        
    }
    
    public boolean isRoot()
    {
        return _isRoot;
    }
    
    public EJPluginItemGroupContainer(EJPluginBlockProperties blockProperties, EJPluginItemGroupProperties parentItemGroup, int containerType)
    {
        _blockProperties = blockProperties;
        _containerType = containerType;
        _parentItemGroup = parentItemGroup;
        _itemGroups = new ArrayList<EJPluginItemGroupProperties>();
    }
    
    public EJPluginItemGroupProperties getParentItemGroup()
    {
        return _parentItemGroup;
    }
    
    public int count()
    {
        return _itemGroups.size();
    }
    
    /**
     * Returns the number of groups within this container and all containers of
     * the contained groups
     * 
     * @return The total number of groups
     */
    public int getTotalCount()
    {
        int count = count();
        
        Iterator<EJPluginItemGroupProperties> allSubGroups = _itemGroups.iterator();
        while (allSubGroups.hasNext())
        {
            EJPluginItemGroupProperties props = (EJPluginItemGroupProperties) allSubGroups.next();
            count += props.getChildItemGroupContainer().getTotalCount();
        }
        
        return count;
    }
    
    /**
     * Removes all groups from this container
     * <p>
     * This method will first remove all items from the item group before
     * removing the group. This will allow the garbage collector to do its job
     * 
     * @see #clear()
     */
    public void clearAndDeleteItems()
    {
        Iterator<EJPluginItemGroupProperties> allGroups = _itemGroups.iterator();
        while (allGroups.hasNext())
        {
            EJPluginItemGroupProperties group = (EJPluginItemGroupProperties) allGroups.next();
            group.deleteAllItems();
        }
        _itemGroups.clear();
    }
    
    /**
     * Removes all groups from this container without removing the items
     * 
     * @see #clearAndDeleteItems()
     */
    public void clear()
    {
        Iterator<EJPluginItemGroupProperties> allGroups = _itemGroups.iterator();
        while (allGroups.hasNext())
        {
            EJPluginItemGroupProperties group = (EJPluginItemGroupProperties) allGroups.next();
            group.deleteAllItems();
        }
        _itemGroups.clear();
    }
    
    public int getContainerType()
    {
        return _containerType;
    }
    
    public EJScreenType getScreenType()
    {
        switch (_containerType)
        {
            case INSERT_SCREEN:
                return EJScreenType.INSERT;
            case QUERY_SCREEN:
                return EJScreenType.QUERY;
            case UPDATE_SCREEN:
                return EJScreenType.UPDATE;
                
            default:
                return EJScreenType.MAIN;
        }
    }
    
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    /**
     * Indicates if there is an item group in this container with the given name
     * <p>
     * This method will first check this container then search all item groups
     * to see if there is a child group with the given name
     * 
     * @param itemGroupName
     *            The name of the item group to check for
     * 
     * @return true if the item group exists otherwise false
     */
    public boolean containsItemGroup(String itemGroupName)
    {
        Iterator<EJPluginItemGroupProperties> iti = _itemGroups.iterator();
        while (iti.hasNext())
        {
            EJItemGroupProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(itemGroupName))
            {
                return true;
            }
        }
        
        return itemGroupExistsWithinChildren(itemGroupName);
    }
    
    private boolean itemGroupExistsWithinChildren(String itemGroupName)
    {
        Iterator<EJPluginItemGroupProperties> iti = _itemGroups.iterator();
        while (iti.hasNext())
        {
            EJPluginItemGroupProperties props = iti.next();
            if (props.getChildItemGroupContainer().containsItemGroup(itemGroupName))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsItemGroup(EJPluginItemGroupProperties properties)
    {
        
        if (_itemGroups.contains(properties))
        {
            
            return true;
        }
        return itemGroupExistsWithinChildren(properties);
    }
    
    private boolean itemGroupExistsWithinChildren(EJPluginItemGroupProperties properties)
    {
        Iterator<EJPluginItemGroupProperties> iti = _itemGroups.iterator();
        while (iti.hasNext())
        {
            EJPluginItemGroupProperties props = iti.next();
            if (props.getChildItemGroupContainer().containsItemGroup(properties))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Searches all groups for an item with the given name and indicates if it
     * exists
     * 
     * @param name
     *            The name of the item to search for
     * @return <code>true</code> if the item exists, otherwise
     *         <code>false</code>
     */
    public boolean containsItemProperties(String name, boolean includeSubGroups)
    {
        
        Iterator<EJPluginItemGroupProperties> groupProperties = _itemGroups.iterator();
        while (groupProperties.hasNext())
        {
            if (groupProperties.next().containsItemReference(name, includeSubGroups))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Searches all groups for an item with the given name and indicates if it
     * exists
     * 
     * @param name
     *            The name of the item to search for
     * @return <code>true</code> if the item exists, otherwise
     *         <code>false</code>
     */
    public boolean containsItemProperties(String name)
    {
        Iterator<EJPluginItemGroupProperties> groupProperties = _itemGroups.iterator();
        while (groupProperties.hasNext())
        {
            if (groupProperties.next().containsItemReference(name))
            {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Adds an <code>ItemGroupProperties<code> object to this blocks item
     * properties store
     * 
     * @param itemGroupProperties
     *            The <code>ItemGroupProperties</code> to be added
     */
    public void addItemGroupProperties(EJItemGroupProperties itemGroupProperties)
    {
        if (itemGroupProperties != null)
        {
            String name = itemGroupProperties.getName();
            int id = 0;
            while (containsItemGroup(name))
            {
                name = String.format("%s_%s", itemGroupProperties.getName(), String.valueOf(++id));
                
            }
            EJPluginItemGroupProperties groupProperties = ((EJPluginItemGroupProperties) itemGroupProperties);
            groupProperties.setName(name);
            groupProperties.setParentItemGroupContainer(this);
            _itemGroups.add(groupProperties);
        }
    }
    
    public void addItemGroupProperties(int index, EJItemGroupProperties itemGroupProperties)
    {
        if (itemGroupProperties != null)
        {
            String name = itemGroupProperties.getName();
            int id = 0;
            while (containsItemGroup(name))
            {
                name = String.format("%s_%s", itemGroupProperties.getName(), String.valueOf(++id));
                
            }
            EJPluginItemGroupProperties groupProperties = ((EJPluginItemGroupProperties) itemGroupProperties);
            groupProperties.setName(name);
            groupProperties.setParentItemGroupContainer(this);
            _itemGroups.add(index, groupProperties);
        }
    }
    
    /**
     * Returns all <code>IItemGroupProperties</code> contained within this
     * container
     * 
     * @return All <code>IItemGroupProperties</code> contained within this
     *         container
     */
    public Collection<EJItemGroupProperties> getAllItemGroupProperties()
    {
        
        return new ArrayList<EJItemGroupProperties>(_itemGroups);
    }
    
    public List<EJPluginItemGroupProperties> getItemGroups()
    {
        return new ArrayList<EJPluginItemGroupProperties>(_itemGroups);
    }
    
    /**
     * Returns all <code>ItemGroupProperties</code> contained within this
     * container
     * 
     * @return All <code>ItemGroupProperties</code> contained within this
     *         container
     */
    public Collection<EJPluginItemGroupProperties> getAllResequancableItems()
    {
        
        return new ArrayList<EJPluginItemGroupProperties>(_itemGroups);
    }
    
    /**
     * Returns all <code>IItemGroupDisplayProperties</code> contained within
     * this container
     * 
     * @return All <code>IItemGroupDisplayProperties</code> contained within
     *         this container
     */
    public Collection<EJDevItemGroupDisplayProperties> getAllItemGroupDisplayProperties()
    {
        return new ArrayList<EJDevItemGroupDisplayProperties>(_itemGroups);
    }
    
    /**
     * Remove the item group with the given name
     * <p>
     * All item definitions that have been added to the group will also be
     * removed and will need to be referenced within a difference item group
     * 
     * @param itemGroupName
     *            The name of the item group to remove
     */
    public void removeItemGroup(String itemGroupName)
    {
        Iterator<EJPluginItemGroupProperties> props = _itemGroups.iterator();
        
        while (props.hasNext())
        {
            EJItemGroupProperties item = props.next();
            
            if (item.getName().equalsIgnoreCase(itemGroupName))
            {
                _itemGroups.remove(item);
                
                break;
            }
        }
    }
    
    /**
     * Remove the item with the given name from this container or one of the
     * child containers
     * 
     * @param itemName
     *            The name of the item to remove
     */
    public void removeItem(String itemName)
    {
        Iterator<EJPluginItemGroupProperties> props = _itemGroups.iterator();
        
        while (props.hasNext())
        {
            EJPluginItemGroupProperties itemGroup = props.next();
            
            if (itemGroup.containsItemReference(itemName))
            {
                itemGroup.deleteItem(itemName);
                return;
            }
            
            itemGroup.getChildItemGroupContainer().removeItem(itemName);
        }
    }
    
    public void removeItem(EJPluginItemGroupProperties item)
    {
        _itemGroups.remove(item);
    }
    
    public EJPluginItemGroupProperties getLastAddedGroup()
    {
        return _itemGroups.size() == 0 ? null : _itemGroups.get(_itemGroups.size() - 1);
    }
    
    public void copyGroupForScreen(EJPluginItemGroupContainer container, int screenType)
    {
        container.clearAndDeleteItems();
        for (EJPluginItemGroupProperties itemGroup : _itemGroups)
        {
            addItemGroups(container, itemGroup, screenType);
        }
    }
    
    private void addItemGroups(EJPluginItemGroupContainer newContainer, EJPluginItemGroupProperties itemGroup, int screenType)
    {
        EJPluginItemGroupProperties newItemGroup = new EJPluginItemGroupProperties(itemGroup.getName(), newContainer);
        newItemGroup.setDisplayGroupFrame(itemGroup.dispayGroupFrame());
        newItemGroup.setExpandHorizontally(itemGroup.canExpandHorizontally());
        newItemGroup.setExpandVertically(itemGroup.canExpandVertically());
        newItemGroup.setFrameTitle(itemGroup.getFrameTitle());
        newItemGroup.setHeight(itemGroup.getHeight());
        newItemGroup.setNumCols(itemGroup.getNumCols());
        newItemGroup.setWidth(itemGroup.getWidth());
        newItemGroup.setXspan(itemGroup.getXspan());
        newItemGroup.setYspan(itemGroup.getYspan());
        newItemGroup.setHorizontalAlignment(itemGroup.getHorizontalAlignment());
        newItemGroup.setVerticalAlignment(itemGroup.getVerticalAlignment());
        
        EJPropertyDefinitionGroup definitionGroup = null;
        
        switch (screenType)
        {
            case EJPluginItemGroupContainer.INSERT_SCREEN:
                EJDevInsertScreenRendererDefinition idefinition = newItemGroup.getBlockProperties().getInsertScreenRendererDefinition();
                definitionGroup = idefinition != null ? (idefinition.getItemGroupPropertiesDefinitionGroup()) : null;
                
                break;
            case EJPluginItemGroupContainer.UPDATE_SCREEN:
                EJDevUpdateScreenRendererDefinition udefinition = newItemGroup.getBlockProperties().getUpdateScreenRendererDefinition();
                definitionGroup = udefinition != null ? (udefinition.getItemGroupPropertiesDefinitionGroup()) : null;
                
                break;
            case EJPluginItemGroupContainer.QUERY_SCREEN:
                EJDevQueryScreenRendererDefinition qdefinition = newItemGroup.getBlockProperties().getQueryScreenRendererDefinition();
                definitionGroup = qdefinition != null ? (qdefinition.getItemGroupPropertiesDefinitionGroup()) : null;
                
                break;
            default:
                
                if (newItemGroup.getBlockProperties().isUsedInLovDefinition())
                {
                    final EJPluginLovDefinitionProperties lovDefinition = newItemGroup.getBlockProperties().getLovDefinition();
                    EJDevLovRendererDefinition definition = lovDefinition.getRendererDefinition();
                    
                    definitionGroup = definition != null ? (definition.getItemGroupPropertiesDefinitionGroup()) : null;
                    
                }
                else
                {
                    EJDevBlockRendererDefinition definition = newItemGroup.getBlockProperties().getBlockRendererDefinition();
                    
                    definitionGroup = definition != null ? (definition.getItemGroupPropertiesDefinitionGroup()) : null;
                    
                }
                break;
        }
        
        if (definitionGroup != null)
        {
            newItemGroup.setRendererProperties(ExtensionsPropertiesFactory.addExtensionProperties(newItemGroup.getFormProperties(),
                    newItemGroup.getBlockProperties(), definitionGroup, null, true));
        }
        
        if (itemGroup.getRendererProperties() != null && newItemGroup.getRendererProperties() != null)
        {
            ExtensionsPropertiesFactory.copyMatchingProperties(newItemGroup.getRendererProperties(), itemGroup.getRendererProperties());
        }
        
        newContainer.addItemGroupProperties(newItemGroup);
        
        for (EJScreenItemProperties screenItem : itemGroup.getAllItemProperties())
        {
            newItemGroup.addItemProperties(copyToType(newItemGroup, screenItem, screenType));
        }
        
        for (EJItemGroupProperties itemGroupProperties : itemGroup.getChildItemGroupContainer().getAllItemGroupProperties())
        {
            addItemGroups(newItemGroup.getChildItemGroupContainer(), (EJPluginItemGroupProperties) itemGroupProperties, screenType);
        }
    }
    
    /**
     * Searches all groups for an item with the given name and returns the items
     * screen properties
     * 
     * @param name
     *            The name of the item to search for
     * @return Returns the screen properties of the required item, otherwise
     *         <code>null</code>
     */
    public EJScreenItemProperties getScreenItemProperties(String name)
    {
        EJScreenItemProperties props = null;
        
        Iterator<EJPluginItemGroupProperties> groupPropertiesIti = _itemGroups.iterator();
        while (groupPropertiesIti.hasNext())
        {
            EJPluginItemGroupProperties itemGroupProps = groupPropertiesIti.next();
            props = itemGroupProps.getItemPropertiesForBlockItem(name);
            if (props != null)
            {
                return props;
            }
            
            props = itemGroupProps.getChildItemGroupContainer().getScreenItemProperties(name);
            if (props != null)
            {
                return props;
            }
            
        }
        
        return props;
    }
    
    private EJPluginScreenItemProperties copyToType(EJPluginItemGroupProperties itemGroup, EJScreenItemProperties itemProperties, int screenType)
    {
        
        EJPropertyDefinitionGroup definitionGroup = null;
        
        switch (screenType)
        {
            case EJPluginItemGroupContainer.INSERT_SCREEN:
                EJDevInsertScreenRendererDefinition idefinition = itemGroup.getBlockProperties().getInsertScreenRendererDefinition();
                definitionGroup = idefinition != null ? (idefinition.getItemPropertyDefinitionGroup()) : null;
                
                break;
            case EJPluginItemGroupContainer.UPDATE_SCREEN:
                EJDevUpdateScreenRendererDefinition udefinition = itemGroup.getBlockProperties().getUpdateScreenRendererDefinition();
                definitionGroup = udefinition != null ? (udefinition.getItemPropertyDefinitionGroup()) : null;
                
                break;
            case EJPluginItemGroupContainer.QUERY_SCREEN:
                EJDevQueryScreenRendererDefinition qdefinition = itemGroup.getBlockProperties().getQueryScreenRendererDefinition();
                definitionGroup = qdefinition != null ? (qdefinition.getItemPropertyDefinitionGroup()) : null;
                
                break;
            default:
                
                if (itemGroup.getBlockProperties().isUsedInLovDefinition())
                {
                    final EJPluginLovDefinitionProperties lovDefinition = itemGroup.getBlockProperties().getLovDefinition();
                    EJDevLovRendererDefinition definition = lovDefinition.getRendererDefinition();
                    
                    definitionGroup = definition != null ? (definition.getItemPropertiesDefinitionGroup()) : null;
                    
                }
                else
                {
                    EJDevBlockRendererDefinition definition = itemGroup.getBlockProperties().getBlockRendererDefinition();
                    
                    definitionGroup = definition != null ? (definition.getItemPropertiesDefinitionGroup()) : null;
                    
                }
                break;
        }
        EJFrameworkExtensionProperties target = null;
        try
        {
            if (screenType == QUERY_SCREEN)
            {
                EJPluginQueryScreenItemProperties screenItemProperties;
                
                if (itemProperties.isSpacerItem())
                {
                    screenItemProperties = new EJPluginQueryScreenSpacerItemProperties(itemGroup, true);
                }
                else
                {
                    screenItemProperties = new EJPluginQueryScreenItemProperties(itemGroup, true, itemProperties.isSpacerItem());
                }
                
                screenItemProperties.setActionCommand(itemProperties.getActionCommand());
                screenItemProperties.setEditAllowed(itemProperties.isEditAllowed());
                screenItemProperties.setMandatory(itemProperties.isMandatory());
                screenItemProperties.setLabel(itemProperties.getLabel());
                screenItemProperties.setHint(itemProperties.getHint());
                screenItemProperties.setReferencedItemName(itemProperties.getReferencedItemName());
                screenItemProperties.setVisible(itemProperties.isVisible());
                screenItemProperties.setIsSpacerItem(itemProperties.isSpacerItem());
                if (definitionGroup != null)
                {
                    screenItemProperties.setQueryScreenRendererRequiredProperties(ExtensionsPropertiesFactory.addExtensionProperties(
                            itemGroup.getFormProperties(), itemGroup.getBlockProperties(), definitionGroup, null, true));
                }
                
                target = screenItemProperties.getQueryScreenRendererRequiredProperties();
                
                return screenItemProperties;
            }
            else if (screenType == INSERT_SCREEN)
            {
                EJPluginInsertScreenItemProperties screenItemProperties;
                if (itemProperties.isSpacerItem())
                {
                    screenItemProperties = new EJPluginInsertScreenSpacerItemProperties(itemGroup, true);
                }
                else
                {
                    screenItemProperties = new EJPluginInsertScreenItemProperties(itemGroup, false, itemProperties.isSpacerItem());
                }
                screenItemProperties.setActionCommand(itemProperties.getActionCommand());
                screenItemProperties.setEditAllowed(itemProperties.isEditAllowed());
                screenItemProperties.setLabel(itemProperties.getLabel());
                screenItemProperties.setHint(itemProperties.getHint());
                screenItemProperties.setMandatory(itemProperties.isMandatory());
                screenItemProperties.setReferencedItemName(itemProperties.getReferencedItemName());
                screenItemProperties.setVisible(itemProperties.isVisible());
                screenItemProperties.setIsSpacerItem(itemProperties.isSpacerItem());
                
                if (definitionGroup != null)
                {
                    screenItemProperties.setInsertScreenRendererRequiredProperties(ExtensionsPropertiesFactory.addExtensionProperties(
                            itemGroup.getFormProperties(), itemGroup.getBlockProperties(), definitionGroup, null, true));
                }
                
                target = screenItemProperties.getInsertScreenRendererRequiredProperties();
                
                return screenItemProperties;
            }
            else if (screenType == UPDATE_SCREEN)
            {
                EJPluginUpdateScreenItemProperties screenItemProperties;
                if (itemProperties.isSpacerItem())
                {
                    screenItemProperties = new EJPluginUpdateScreenSpacerItemProperties(itemGroup, true);
                }
                else
                {
                    screenItemProperties = new EJPluginUpdateScreenItemProperties(itemGroup, false, itemProperties.isSpacerItem());
                }
                
                screenItemProperties.setActionCommand(itemProperties.getActionCommand());
                screenItemProperties.setEditAllowed(itemProperties.isEditAllowed());
                screenItemProperties.setLabel(itemProperties.getLabel());
                screenItemProperties.setHint(itemProperties.getHint());
                screenItemProperties.setMandatory(itemProperties.isMandatory());
                screenItemProperties.setReferencedItemName(itemProperties.getReferencedItemName());
                screenItemProperties.setVisible(itemProperties.isVisible());
                screenItemProperties.setIsSpacerItem(itemProperties.isSpacerItem());
                
                if (definitionGroup != null)
                {
                    screenItemProperties.setUpdateScreenRendererRequiredProperties(ExtensionsPropertiesFactory.addExtensionProperties(
                            itemGroup.getFormProperties(), itemGroup.getBlockProperties(), definitionGroup, null, true));
                }
                
                target = screenItemProperties.getUpdateScreenRendererRequiredProperties();
                
                return screenItemProperties;
            }
            else if (screenType == MAIN_SCREEN)
            {
                EJPluginMainScreenItemProperties screenItemProperties;
                if (itemProperties.isSpacerItem())
                {
                    screenItemProperties = new EJPluginMainScreenSpacerItemProperties(itemGroup, true);
                }
                else
                {
                    screenItemProperties = new EJPluginMainScreenItemProperties(itemGroup, false, itemProperties.isSpacerItem());
                }
                
                screenItemProperties.setActionCommand(itemProperties.getActionCommand());
                screenItemProperties.setEditAllowed(itemProperties.isEditAllowed());
                screenItemProperties.setLabel(itemProperties.getLabel());
                screenItemProperties.setHint(itemProperties.getHint());
                screenItemProperties.setMandatory(itemProperties.isMandatory());
                screenItemProperties.setReferencedItemName(itemProperties.getReferencedItemName());
                screenItemProperties.setVisible(itemProperties.isVisible());
                screenItemProperties.setIsSpacerItem(itemProperties.isSpacerItem());
                
                if (screenItemProperties.getBlockProperties().isUsedInLovDefinition())
                {
                    if (definitionGroup != null)
                    {
                        screenItemProperties.setLovRendererRequiredProperties(ExtensionsPropertiesFactory.addExtensionProperties(itemGroup.getFormProperties(),
                                itemGroup.getBlockProperties(), definitionGroup, null, true));
                    }
                    
                    target = screenItemProperties.getLovRendererRequiredProperties();
                }
                else
                {
                    if (definitionGroup != null)
                    {
                        screenItemProperties.setBlockRendererRequiredProperties(ExtensionsPropertiesFactory.addExtensionProperties(
                                itemGroup.getFormProperties(), itemGroup.getBlockProperties(), definitionGroup, null, true));
                    }
                    
                    target = screenItemProperties.getBlockRendererRequiredProperties();
                }
                
                return screenItemProperties;
            }
        }
        finally
        {
            if (target != null)
            {
                if (_containerType == INSERT_SCREEN)
                {
                    
                    ExtensionsPropertiesFactory.copyMatchingProperties(target,
                            ((EJPluginInsertScreenItemProperties) itemProperties).getInsertScreenRendererRequiredProperties());
                    
                }
                else if (_containerType == UPDATE_SCREEN)
                {
                    
                    ExtensionsPropertiesFactory.copyMatchingProperties(target,
                            ((EJPluginUpdateScreenItemProperties) itemProperties).getUpdateScreenRendererRequiredProperties());
                    
                }
                else if (_containerType == QUERY_SCREEN)
                {
                    
                    ExtensionsPropertiesFactory.copyMatchingProperties(target,
                            ((EJPluginQueryScreenItemProperties) itemProperties).getQueryScreenRendererRequiredProperties());
                    
                }
                else if (_containerType == MAIN_SCREEN)
                {
                    
                    ExtensionsPropertiesFactory.copyMatchingProperties(target,
                            ((EJPluginMainScreenItemProperties) itemProperties).getBlockRendererRequiredProperties());
                    
                }
            }
        }
        
        return null;
    }
    
    public boolean isEmpty()
    {
        return _itemGroups.isEmpty();
    }
    
}
