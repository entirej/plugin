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
package org.entirej.framework.plugin.framework.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.core.enumerations.EJItemGroupAlignment;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevItemGroupDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;

public class EJPluginItemGroupProperties implements EJItemGroupProperties, EJDevItemGroupDisplayProperties, EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long serialVersionUID = 7766909510062397447L;
    private EJPluginItemGroupContainer         _parentItemGroupContainer;
    private EJPluginItemGroupContainer         _childItemGroupContainer;
    private EJPluginBlockProperties            _blockProperties;
    private String                             _name;
    
    private boolean                            _displayGroupFrame;
    private String                             _frameTitle;
    
    private int                                _numCols;
    private int                                _width;
    private int                                _height;
    private int                                _xspan;
    private int                                _yspan;
    
    private boolean                            _expandHorizontally;
    private boolean                            _expandVertically;
    private EJItemGroupAlignment               _horizontalAlignment = EJItemGroupAlignment.FILL;
    private EJItemGroupAlignment               _verticalAlignment = EJItemGroupAlignment.FILL;
    
    private List<EJPluginScreenItemProperties> _itemProperties;
    
    private EJFrameworkExtensionProperties     _rendererProperties;
    
    public EJPluginItemGroupProperties(String name, EJPluginItemGroupContainer itemGroupContainer)
    {
        _parentItemGroupContainer = itemGroupContainer;
        _childItemGroupContainer = new EJPluginItemGroupContainer(itemGroupContainer.getBlockProperties(), this, itemGroupContainer.getContainerType());
        _itemProperties = new ArrayList<EJPluginScreenItemProperties>();
        _blockProperties = itemGroupContainer.getBlockProperties();
        _name = name;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _blockProperties.getFormProperties();
    }
    
    public EJPluginItemGroupContainer getParentItemGroupContainer()
    {
        return _parentItemGroupContainer;
    }
    
    public boolean isEmpty()
    {
        return _itemProperties.size() == 0 && _childItemGroupContainer.count() == 0;
    }
    
    public void setParentItemGroupContainer(EJPluginItemGroupContainer newParent)
    {
        _parentItemGroupContainer = newParent;
    }
    
    public EJPluginItemGroupContainer getChildItemGroupContainer()
    {
        return _childItemGroupContainer;
    }
    
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    /**
     * used to set a new name to this item group properties
     * <p>
     * The name will only be set if the name given is not a zero length string
     * 
     * @param name
     *            The new name
     */
    public void setName(String name)
    {
        if (name != null && name.trim().length() > 0)
        {
            _name = name;
        }
    }
    
    /**
     * If set to <code>true</code> then the block renderer should add a frame
     * around the groups items
     * 
     * @param display
     *            The display group frame indicator
     */
    public void setDisplayGroupFrame(boolean display)
    {
        _displayGroupFrame = display;
    }
    
    /**
     * Indicates if a frame should be displayed around the groups items
     * 
     * @return The display indicator
     */
    public boolean dispayGroupFrame()
    {
        return _displayGroupFrame;
    }
    
    /**
     * Set the name that will be displayed within the item groups frame
     * 
     * @param name
     *            The display name
     */
    public void setFrameTitle(String title)
    {
        _frameTitle = title;
    }
    
    /**
     * Returns the name that will be displayed within the item groups frame
     * 
     * @return The item groups frame
     */
    public String getFrameTitle()
    {
        return _frameTitle;
    }
    
    /**
     * Indicates how many display columns this group will have
     * <p>
     * All items being added to this group will be inserted into a grid. The
     * grid will have any number of rows but will be limited to the amount of
     * columns as set by this parameter.
     * <p>
     * Items added to this page can span multiple columns and rows
     * 
     * @return The number of columns defined for this group
     */
    public int getNumCols()
    {
        return _numCols;
    }
    
    /**
     * Sets the number of columns that this group will have
     * <p>
     * All items being added to the group page will be inserted into a grid. The
     * grid will have any number of rows but will be limited to the amount of
     * columns as set by this parameter.
     * 
     * @param numCols
     *            The number of columns to set
     */
    public void setNumCols(int numCols)
    {
        _numCols = numCols;
    }
    
    public void setHeight(int height)
    {
        _height = height;
    }
    
    public int getHeight()
    {
        return _height;
    }
    
    public void setWidth(int width)
    {
        _width = width;
    }
    
    public int getWidth()
    {
        return _width;
    }
    
    public int getXspan()
    {
        return _xspan;
    }
    
    public void setXspan(int xspan)
    {
        _xspan = xspan;
    }
    
    public int getYspan()
    {
        return _yspan;
    }
    
    public void setYspan(int yspan)
    {
        _yspan = yspan;
    }
    
    public boolean canExpandHorizontally()
    {
        return _expandHorizontally;
    }
    
    public void setExpandHorizontally(boolean expandHorizontally)
    {
        _expandHorizontally = expandHorizontally;
    }
    
    public boolean canExpandVertically()
    {
        return _expandVertically;
    }
    
    public void setExpandVertically(boolean expandVertically)
    {
        _expandVertically = expandVertically;
    }
    
    
    @Override
    public EJItemGroupAlignment getHorizontalAlignment()
    {
        return _horizontalAlignment;
    }
    
    public void setHorizontalAlignment(EJItemGroupAlignment horizontalAlignment)
    {
        this._horizontalAlignment = horizontalAlignment;
    }
    
    @Override
    public EJItemGroupAlignment getVerticalAlignment()
    {
        return _verticalAlignment;
    }
    
    public void setVerticalAlignment(EJItemGroupAlignment verticalAlignment)
    {
        this._verticalAlignment = verticalAlignment;
    }
    
    /**
     * Returns the name of this item group
     * 
     * @return The item group name
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * Indicates if there is an item in this container that already references
     * the given block item name
     * 
     * @param itemName
     *            The name of the item to check for
     * 
     * @return true if the item exists otherwise false
     */
    public boolean containsItemReference(String blockItemName, boolean searchSubGroups)
    {
        Iterator<EJPluginScreenItemProperties> iti = _itemProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginScreenItemProperties props = iti.next();
            if (props.getReferencedItemName().equalsIgnoreCase(blockItemName))
            {
                return true;
            }
        }
        if (searchSubGroups && _childItemGroupContainer.containsItemProperties(blockItemName, searchSubGroups)) return true;
        return false;
    }
    
    /**
     * Indicates if there is an item in this container that already references
     * the given block item name
     * 
     * @param itemName
     *            The name of the item to check for
     * 
     * @return true if the item exists otherwise false
     */
    public boolean containsItemReference(String blockItemName)
    {
        Iterator<EJPluginScreenItemProperties> iti = _itemProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginScreenItemProperties props = iti.next();
            if (props.getReferencedItemName().equalsIgnoreCase(blockItemName))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsItemReference(EJPluginScreenItemProperties blockItem)
    {
        
        return _itemProperties.contains(blockItem);
    }
    
    public int getNextAvailableSpacerItemName()
    {
        int largestPos = 0;
        Iterator<EJPluginScreenItemProperties> props = _itemProperties.iterator();
        while (props.hasNext())
        {
            EJPluginScreenItemProperties item = props.next();
            
            if (item.isSpacerItem())
            {
                if (getName() != null && getName().trim().length() > 0)
                {
                    // All spacer item names start with "spacer" and then have a
                    // number.
                    String posString = item.getName().substring(6);
                    try
                    {
                        int pos = Integer.parseInt(posString);
                        if (pos > largestPos)
                        {
                            largestPos = pos;
                        }
                    }
                    catch (NumberFormatException e)
                    {
                        
                    }
                }
            }
        }
        
        return largestPos + 100;
    }
    
    /**
     * Adds a <code>IScreenItemProperties<code> object to this blocks item
     * properties store
     * 
     * @param itemProperties
     *            The <code>IScreenItemProperties</code> to be added
     */
    public void addItemProperties(EJPluginScreenItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(itemProperties);
        }
    }
    
    public void addItemProperties(int index, EJPluginScreenItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(index, itemProperties);
        }
    }
    
    /**
     * Returns all <code>EJScreenItemProperties</code> contained within this
     * container
     * 
     * @return All <code>EJScreenItemProperties</code> contained within this
     *         container
     */
    public Collection<EJScreenItemProperties> getAllItemProperties()
    {
        
        return new ArrayList<EJScreenItemProperties>(_itemProperties);
    }
    
    /**
     * Returns all <code>ScreenItemProperties</code> contained within this
     * container
     * 
     * @return All <code>ScreenItemProperties</code> contained within this
     *         container
     */
    public Collection<EJPluginScreenItemProperties> getAllResequencableItemProperties()
    {
        
        return new ArrayList<EJPluginScreenItemProperties>(_itemProperties);
    }
    
    /**
     * Returns all <code>EJDevScreenItemDisplayProperties</code> contained
     * within this container
     * 
     * @return All <code>EJDevScreenItemDisplayProperties</code> contained
     *         within this container
     */
    public Collection<EJDevScreenItemDisplayProperties> getAllItemDisplayProperties()
    {
        return new ArrayList<EJDevScreenItemDisplayProperties>(_itemProperties);
    }
    
    public List<EJPluginScreenItemProperties> getItemProperties()
    {
        return new ArrayList<EJPluginScreenItemProperties>(_itemProperties);
    }
    
    /**
     * Get a <code>IScreenItemProperties</code> object which references the
     * given block item
     * 
     * @param itemName
     *            The block item name
     * 
     * @return The <code>IScreenItemProperties</code> which references the given
     *         block item or null if there is no reference to the given item
     *         name
     */
    public EJPluginScreenItemProperties getItemPropertiesForBlockItem(String itemName)
    {
        
        Iterator<EJPluginScreenItemProperties> props = _itemProperties.iterator();
        
        while (props.hasNext())
        {
            EJPluginScreenItemProperties item = props.next();
            
            if (item.getReferencedItemName().equalsIgnoreCase(itemName))
            {
                return item;
            }
        }
        return null;
    }
    
    /**
     * Get a <code>IScreenItemDisplayProperties</code> object which references
     * the given block item
     * 
     * @param itemName
     *            The block item name
     * 
     * @return The <code>IScreenItemDisplayProperties</code> which references
     *         the given block item or <code>null</code> if there is no
     *         reference to the given item name
     */
    public EJDevScreenItemDisplayProperties getItemDisplayPropertiesForBlockItem(String itemName)
    {
        return getItemPropertiesForBlockItem(itemName);
    }
    
    /**
     * Delete the item that references the given block item name
     * 
     * @param itemName
     *            The name of the block item
     */
    public int deleteItem(String itemName)
    {
        int indexOf = -1;
        Iterator<EJPluginScreenItemProperties> props = _itemProperties.iterator();
        
        while (props.hasNext())
        {
            EJPluginScreenItemProperties item = props.next();
            
            if (item.getReferencedItemName().equalsIgnoreCase(itemName))
            {
                 indexOf = _itemProperties.indexOf(item);
                _itemProperties.remove(item);
                break;
            }
        }
        return indexOf;
    }
    
    public int deleteItem(EJPluginScreenItemProperties item)
    {
        int indexOf = _itemProperties.indexOf(item);
        _itemProperties.remove(item);
        return indexOf;
    }
    
    /**
     * Clears all items that the item group contained leaving the group empty
     */
    public void deleteAllItems()
    {
        _itemProperties.clear();
    }
    
    @Override
    public EJFrameworkExtensionProperties getRendererProperties()
    {
        return _rendererProperties;
    }
    
    public void setRendererProperties(EJFrameworkExtensionProperties properties)
    {
        _rendererProperties = properties;
    }
    
}
