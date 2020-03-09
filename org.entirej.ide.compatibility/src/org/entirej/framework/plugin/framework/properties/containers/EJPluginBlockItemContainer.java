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
package org.entirej.framework.plugin.framework.properties.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayPropertiesContainer;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.utils.EJPluginItemChanger;

public class EJPluginBlockItemContainer implements EJDevBlockItemDisplayPropertiesContainer
{
    private List<EJPluginBlockItemProperties> _itemProperties;
    private EJPluginBlockProperties           _blockProperties;
    
    public EJPluginBlockItemContainer(EJPluginBlockProperties blockProperties)
    {
        _blockProperties = blockProperties;
        _itemProperties = new ArrayList<EJPluginBlockItemProperties>();
    }
    
    public void dispose()
    {
        if (_itemProperties != null)
        {
            _itemProperties.clear();
        }
        
        _blockProperties = null;
    }
    
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public boolean containsItemProperty(String name)
    {
        Iterator<EJPluginBlockItemProperties> iti = _itemProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginBlockItemProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Adds a
     * <code>BlockItemPRoperties<code> object to this blocks item properties store
     * 
     * @param pItemProperties
     *            The Item Properties object to be added
     */
    public void addItemProperties(EJPluginBlockItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(itemProperties);
            
            if (_blockProperties.isMirrorBlock() && _blockProperties.getMirrorParent() == null)
            {
                // this is a mirror parent
                for (EJPluginBlockProperties childProperties : _blockProperties.getMirrorChildren())
                {
                    EJPluginBlockItemProperties copy = itemProperties.makeCopy(childProperties, true);
                    childProperties.getItemContainer().addItemProperties(copy);
                }
            }
        }
    }
    
    public void addItemProperties(int index, EJPluginBlockItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(index, itemProperties);
            
            if (_blockProperties.isMirrorBlock() && _blockProperties.getMirrorParent() == null)
            {
                // this is a mirror parent
                for (EJPluginBlockProperties childProperties : _blockProperties.getMirrorChildren())
                {
                    EJPluginBlockItemProperties copy = itemProperties.makeCopy(childProperties, true);
                    childProperties.getItemContainer().addItemProperties(index, copy);
                }
            }
        }
    }
    
    /**
     * Returns a Map of <code>IItemProperties</code> contained within this
     * block. The key of the map is the name of the item in uppercase.
     * 
     * @return A set containing all IItemProperties contained within this block.
     * 
     * @see com.ottomobil.dsys.sprintframework.dataobjects.properties.interfaces.IBlockProperties#addColumnProperties(IItemProperties)
     * @see com.ottomobil.dsys.sprintframework.dataobjects.properties.interfaces.IBlockProperties#getItemProperties()
     */
    public List<EJPluginBlockItemProperties> getAllItemProperties()
    {
        return _itemProperties;
    }
    
    /**
     * Returns a Map of <code>IBlockItemDisplayProperties</code> contained
     * within this block. The key of the map is the name of the item in
     * uppercase.
     * 
     * @return A set containing all IBlockItemDisplayProperties contained within
     *         this block.
     */
    public Collection<EJDevBlockItemDisplayProperties> getAllItemDisplayProperties()
    {
        return new ArrayList<EJDevBlockItemDisplayProperties>(_itemProperties);
    }
    
    /**
     * Indicates if there is an item in this container with the given name
     * 
     * @param itemNameq
     *            The name of the item to check for
     * @return true if the item exists otherwise false
     */
    public boolean contains(String itemName)
    {
        Iterator<EJPluginBlockItemProperties> iti = _itemProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginBlockItemProperties item = iti.next();
            
            if (item.getName().equalsIgnoreCase(itemName))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Get an <code>IItemsProperties</code> object for a given item. The item
     * property store within the <code>IBlockProperties</code> will be searched
     * for the given item name. A case insensitive query will be made.
     * 
     * @param itemName
     *            The name of the item to search for
     * @return The properties of the given item or null if an invalid or
     *         nonexistent item name was passed
     */
    public EJPluginBlockItemProperties getItemProperties(String itemName)
    {
        
        Iterator<EJPluginBlockItemProperties> props = _itemProperties.iterator();
        
        while (props.hasNext())
        {
            EJPluginBlockItemProperties item = props.next();
            
            if (item.getName().equalsIgnoreCase(itemName))
            {
                return item;
            }
        }
        return null;
    }
   
    
    public void sync(List<EJPluginBlockItemProperties> newItems)
    {
        
        List<EJPluginBlockItemProperties> markedRemove = new ArrayList<EJPluginBlockItemProperties>(_itemProperties);
        for (EJPluginBlockItemProperties newItem : new ArrayList<EJPluginBlockItemProperties>(newItems))
        {
            for (EJPluginBlockItemProperties item : new ArrayList<EJPluginBlockItemProperties>(markedRemove))
            {
                if (!item.isBlockServiceItem())
                {
                    markedRemove.remove(item);
                    continue;
                }
                
                if (newItem.getName() != null && newItem.getDataTypeClassName() != null && newItem.getName().equals(item.getName())
                        && newItem.getDataTypeClassName().equals(item.getDataTypeClassName()))
                {
                    markedRemove.remove(item);
                    newItems.remove(newItem);
                    break;
                }
                
            }
        }
        for (EJPluginBlockItemProperties ejPluginBlockItemProperties : markedRemove)
        {
            removeItem(ejPluginBlockItemProperties,true);
        }
        for (EJPluginBlockItemProperties ejPluginBlockItemProperties : newItems)
        {
            
            addItemProperties(ejPluginBlockItemProperties);
        }
        
    }
    
    public int getItemCount()
    {
        return _itemProperties.size();
    }
    
    public int  removeItem(EJPluginBlockItemProperties item,boolean cleanup)
    {
        if (_blockProperties.isMirrorBlock() && _blockProperties.getMirrorParent() == null)
        {
            // this is a mirror parent
            for (EJPluginBlockProperties childProperties : _blockProperties.getMirrorChildren())
            {
                childProperties.getItemContainer().removeItem(childProperties.getItemContainer().getItemProperties(item.getName()), cleanup);
            }
        }
        if(cleanup)
            EJPluginItemChanger.deleteItemOnForm(_blockProperties, item.getName());
        int indexOf = _itemProperties.indexOf(item);
        _itemProperties.remove(item);
        return indexOf;
    }
    
}
