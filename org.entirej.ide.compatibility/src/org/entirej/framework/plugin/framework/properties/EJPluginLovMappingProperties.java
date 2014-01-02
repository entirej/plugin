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
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;

public class EJPluginLovMappingProperties implements EJPluginFormPreviewProvider
{
    private String                                 _lovMappingName;
    private EJPluginFormProperties                 _formProperties;
    private String                                 _lovDefinitionName;
    private String                                 _lovDisplayName;
    
    private boolean                                _executeAfterQuery = true;
    private EJPluginBlockProperties                _mappedBlock;
    private List<EJPluginLovItemMappingProperties> _lovItemMappings;
    
    public EJPluginLovMappingProperties(String lovMappingName, EJPluginFormProperties formProperties)
    {
        _lovMappingName = lovMappingName;
        _formProperties = formProperties;
        _lovItemMappings = new ArrayList<EJPluginLovItemMappingProperties>();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public String getName()
    {
        return _lovMappingName;
    }
    
    public void internalSetName(String name)
    {
        _lovMappingName = name;
    }
    
    public void setExecuteAfterQuery(boolean executeAfterQuery)
    {
        _executeAfterQuery = executeAfterQuery;
    }
    
    public boolean executeAfterQuery()
    {
        return _executeAfterQuery;
    }
    
    /**
     * Set the name of the lov definition upon which this mapping is based
     * <p>
     * 
     * @param definitionName
     *            The lov definition name
     */
    public void setLovDefinitionName(String definitionName)
    {
        _lovDefinitionName = definitionName;
    }
    
    /**
     * Returns the name of the lov definition upon which this mapping is based
     * 
     * @return The name of the lov mapping
     */
    public String getLovDefinitionName()
    {
        return _lovDefinitionName;
    }
    
    /**
     * Clears all item mappings creates within this lov mapping
     */
    public void clearAllDefinitionMappings()
    {
        _lovItemMappings.clear();
    }
    
    /**
     * Sets the display name for the lov
     * 
     * @param name
     *            The display name
     */
    public void setLovDisplayName(String name)
    {
        _lovDisplayName = name;
    }
    
    /**
     * Returns the name that the lov renderer should display for this lov
     * 
     * @return The lov display name
     */
    public String getLovDisplayName()
    {
        return _lovDisplayName;
    }
    
    /**
     * Returns the mapped block for this lov
     * <p>
     * The mapped block is the block to which EntreJ will copy lov values to
     * 
     * @return The mapped block of this lov
     */
    public EJPluginBlockProperties getMappedBlock()
    {
        return _mappedBlock;
    }
    
    /**
     * Sets the mapped block for this lov mapping
     * <p>
     * Setting the mapped block property will result in all of the item mappings
     * being removed
     * 
     * @param mappedBlock
     */
    public void setMappedBlock(EJPluginBlockProperties mappedBlock)
    {
        _mappedBlock = mappedBlock;
    }
    
    /**
     * Returns the set of item mappings that are available within this lov
     * mapping properties
     * 
     * @return The set of <code>LovItemMappingProperties</code>
     */
    public List<EJPluginLovItemMappingProperties> getAllItemMappingProperties()
    {
        return _lovItemMappings;
    }
    
    /**
     * Indicates if an Item Mapping already exists for the given block item name
     * 
     * @param blockItemName
     *            The block item name
     * @return <code>true</code> if an item mapping already exists otherwise
     *         <code>false</code>
     */
    public boolean containsItemMappingForBlockItem(String blockItemName)
    {
        Iterator<EJPluginLovItemMappingProperties> itemMappings = getAllItemMappingProperties().iterator();
        while (itemMappings.hasNext())
        {
            EJPluginLovItemMappingProperties props = itemMappings.next();
            if ((props.getBlockItemName().equalsIgnoreCase(blockItemName)))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Indicates if an Item Mapping already exists for the given lov definition
     * name
     * 
     * @param lovDefinitionItem
     *            The name of the lov definition item
     * @return <code>true</code> if an item mapping already exists otherwise
     *         <code>false</code>
     */
    public boolean containsItemMappingForLovDefinitionItem(String lovDefinitionItem)
    {
        Iterator<EJPluginLovItemMappingProperties> itemMappings = getAllItemMappingProperties().iterator();
        while (itemMappings.hasNext())
        {
            EJPluginLovItemMappingProperties props = itemMappings.next();
            if ((props.getLovDefinitionItemName().equalsIgnoreCase(lovDefinitionItem)))
            {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Sets the mapping between the given lov definition item and the block item
     * <p>
     * If there is no lov definition item with the given name, then no mapping
     * will be made
     * 
     * @param lovDefinitionItemName
     *            The name of the lov definition item
     * @param blockItemName
     *            The name of the block item
     */
    public void setMappingItem(String lovDefinitionItemName, String blockItemName)
    {
        Iterator<EJPluginLovItemMappingProperties> itemMappings = getAllItemMappingProperties().iterator();
        while (itemMappings.hasNext())
        {
            EJPluginLovItemMappingProperties props = itemMappings.next();
            if ((props.getLovDefinitionItemName().equalsIgnoreCase(lovDefinitionItemName)))
            {
                props.setBlockItemName(blockItemName);
                break;
            }
        }
    }
    
    /**
     * Adds a new item map to this lov mapping
     * <p>
     * If there is already a mapping for the given item definition item name or
     * the name is empty, then nothing will be done
     * 
     * @param position
     *            The position of the mapping item within this lov mapping
     * @param lovDefinitionItemName
     *            The name of the lov definition item
     * @param blockItemName
     *            The name of the block item
     */
    public EJPluginLovItemMappingProperties addMappingItem(String lovDefinitionItemName, String blockItemName)
    {
        if (lovDefinitionItemName == null || lovDefinitionItemName.trim().length() == 0)
        {
            return null;
        }
        
        EJPluginLovItemMappingProperties props = new EJPluginLovItemMappingProperties(_mappedBlock, lovDefinitionItemName, (blockItemName == null ? ""
                : blockItemName));
        _lovItemMappings.add(props);
        return props;
    }
    
    public EJPluginLovMappingProperties makeCopy(EJPluginBlockProperties forBlock)
    {
        String lovDefName = getLovDefinitionName();
        String lovDisplayName = getLovDisplayName();
        String mappingName = getName();
        
        EJPluginLovMappingProperties newMapping = new EJPluginLovMappingProperties(mappingName, forBlock.getFormProperties());
        newMapping.setExecuteAfterQuery(executeAfterQuery());
        newMapping.setLovDefinitionName(lovDefName);
        newMapping.setLovDisplayName(lovDisplayName);
        newMapping.setMappedBlock(forBlock);
        
        for (EJPluginLovItemMappingProperties mappintItem : getAllItemMappingProperties())
        {
            newMapping.addMappingItem(mappintItem.getLovDefinitionItemName(), mappintItem.getBlockItemName());
        }
        
        return newMapping;
    }
    
}
