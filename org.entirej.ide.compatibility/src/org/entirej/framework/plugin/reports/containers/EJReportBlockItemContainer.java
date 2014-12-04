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
package org.entirej.framework.plugin.reports.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;

public class EJReportBlockItemContainer
{
    private List<EJPluginReportItemProperties> _itemProperties;
    private EJPluginReportBlockProperties      _blockProperties;
    
    public EJReportBlockItemContainer(EJPluginReportBlockProperties blockProperties)
    {
        _blockProperties = blockProperties;
        _itemProperties = new ArrayList<EJPluginReportItemProperties>();
    }
    
    public void dispose()
    {
        if (_itemProperties != null)
        {
            _itemProperties.clear();
        }
        
        _blockProperties = null;
    }
    
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public boolean containsItemProperty(String name)
    {
        Iterator<EJPluginReportItemProperties> iti = _itemProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginReportItemProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
    
   
    public void addItemProperties(EJPluginReportItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(itemProperties);
            
        }
    }
    
    public void addItemProperties(int index, EJPluginReportItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(index, itemProperties);
            
        }
    }
    
    public List<EJPluginReportItemProperties> getAllItemProperties()
    {
        return _itemProperties;
    }
    

    public boolean contains(String itemName)
    {
        Iterator<EJPluginReportItemProperties> iti = _itemProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginReportItemProperties item = iti.next();
            
            if (item.getName()!=null && item.getName().equalsIgnoreCase(itemName))
            {
                return true;
            }
            
        }
        return false;
    }
    

    public EJPluginReportItemProperties getItemProperties(String itemName)
    {
        
        Iterator<EJPluginReportItemProperties> props = _itemProperties.iterator();
        
        while (props.hasNext())
        {
            EJPluginReportItemProperties item = props.next();
            
            if (item.getName().equalsIgnoreCase(itemName))
            {
                return item;
            }
        }
        return null;
    }
    
    public void removeItem(String itemName)
    {
        Iterator<EJPluginReportItemProperties> props = _itemProperties.iterator();
        
        while (props.hasNext())
        {
            EJPluginReportItemProperties item = props.next();
            
            if (item.getName().equalsIgnoreCase(itemName))
            {
                
                removeItem(item);
                
                break;
            }
        }
    }
    
    public void sync(List<EJPluginReportItemProperties> newItems)
    {
        
        List<EJPluginReportItemProperties> markedRemove = new ArrayList<EJPluginReportItemProperties>(_itemProperties);
        for (EJPluginReportItemProperties newItem : new ArrayList<EJPluginReportItemProperties>(newItems))
        {
            for (EJPluginReportItemProperties item : new ArrayList<EJPluginReportItemProperties>(markedRemove))
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
        for (EJPluginReportItemProperties EJPluginReportItemProperties : markedRemove)
        {
            removeItem(EJPluginReportItemProperties);
        }
        for (EJPluginReportItemProperties EJPluginReportItemProperties : newItems)
        {
            
            addItemProperties(EJPluginReportItemProperties);
        }
        
    }
    
    public int getItemCount()
    {
        return _itemProperties.size();
    }
    
    public int removeItem(EJPluginReportItemProperties item)
    {
        int indexOf = _itemProperties.indexOf(item);
        // FIXME
        // EJPluginItemChanger.deleteItemOnForm(_blockProperties,
        // item.getName());
        _itemProperties.remove(item);
        
        return indexOf;
    }
    
}
