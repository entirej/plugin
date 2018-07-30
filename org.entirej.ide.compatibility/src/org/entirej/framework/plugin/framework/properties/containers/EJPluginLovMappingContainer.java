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
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;

public class EJPluginLovMappingContainer
{
    private List<EJPluginLovMappingProperties> _lovMappingProperties;
    private EJPluginBlockProperties            _blockProperties;
    
    public EJPluginLovMappingContainer(EJPluginBlockProperties blockProperties)
    {
        _blockProperties = blockProperties;
        _lovMappingProperties = new ArrayList<EJPluginLovMappingProperties>();
    }
    
    public void dispose()
    {
        _blockProperties = null;
        _lovMappingProperties.clear();
        
    }
    
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public int size()
    {
        return _lovMappingProperties.size();
    }
    
    public boolean contains(String lovDefinitionName)
    {
        Iterator<EJPluginLovMappingProperties> iti = _lovMappingProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginLovMappingProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(lovDefinitionName))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean containsBlockItem(String itemName)
    {
        if (itemName == null)
        {
            return false;
        }
        
        Iterator<EJPluginLovMappingProperties> iti = _lovMappingProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginLovMappingProperties props = iti.next();
            if (props.containsItemMappingForBlockItem(itemName))
            {
                return true;
            }
        }
        
        return false;
    }
    
    public void addLovMappingProperties(EJPluginLovMappingProperties lovMappingProperties)
    {
        if (lovMappingProperties != null)
        {
            _lovMappingProperties.add(lovMappingProperties);
            
           
        }
    }
    
    public void addLovMappingProperties(int index, EJPluginLovMappingProperties lovMappingProperties)
    {
        if (lovMappingProperties != null)
        {
            _lovMappingProperties.add(index, lovMappingProperties);
            
           
        }
    }
    
    public void removeLovMappingProperties(String name)
    {
        Iterator<EJPluginLovMappingProperties> iti = _lovMappingProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginLovMappingProperties props = iti.next();
            
            if (props.getName().equalsIgnoreCase(name))
            {
                _lovMappingProperties.remove(props);
                
                break;
            }
        }
        
       
    }
    
    public int removeLovMappingProperties(EJPluginLovMappingProperties props)
    {
        int indexOf = _lovMappingProperties.indexOf(props);
        _lovMappingProperties.remove(props);
        return indexOf;
        
    }
    
    /**
     * Used to retrieve a specific lov mapping properties
     * 
     * @return If the lov mapping name parameter is a valid lov mapping
     *         contained within this form, then its properties will be returned
     *         if however the name is null or not valid, then a <b>null</b>
     *         object will be returned.
     */
    public EJPluginLovMappingProperties getLovMappingProperties(String mappingName)
    {
        
        Iterator<EJPluginLovMappingProperties> iti = _lovMappingProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginLovMappingProperties props = iti.next();
            
            if (props.getName().equalsIgnoreCase(mappingName))
            {
                return props;
            }
        }
        return null;
    }
    
    /**
     * Used to return the whole list of lov mappings contained within this form
     * 
     * @return The lov mappings contained within this form
     */
    public List<EJPluginLovMappingProperties> getAllLovMappingProperties()
    {
        return _lovMappingProperties;
    }
    
    public EJPluginLovMappingContainer makeCopy(EJPluginBlockProperties forBlock)
    {
        EJPluginLovMappingContainer target = new EJPluginLovMappingContainer(forBlock);
        
        for (EJPluginLovMappingProperties mapping : _lovMappingProperties)
        {
            target.addLovMappingProperties(mapping.makeCopy(forBlock));
        }
        
        return target;
    }
    
    public boolean isEmpty()
    {
        return _lovMappingProperties.isEmpty();
    }
}
