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
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.reports.enumerations.EJReportScreenItemType;

public class EJReportScreenItemContainer
{
    private List<EJPluginReportScreenItemProperties> _itemProperties;
    private EJPluginReportBlockProperties            _blockProperties;
    
    public EJReportScreenItemContainer(EJPluginReportBlockProperties blockProperties)
    {
        _blockProperties = blockProperties;
        _itemProperties = new ArrayList<EJPluginReportScreenItemProperties>();
    }
    
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public void addItemProperties(EJPluginReportScreenItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(itemProperties);
            
        }
    }
    
    public void addItemProperties(int index, EJPluginReportScreenItemProperties itemProperties)
    {
        if (itemProperties != null)
        {
            _itemProperties.add(index, itemProperties);
            
        }
    }
    
    public List<EJPluginReportScreenItemProperties> getAllItemProperties()
    {
        return _itemProperties;
    }
    
    public boolean contains(String name)
    {
        Iterator<EJPluginReportScreenItemProperties> iti = _itemProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginReportScreenItemProperties item = iti.next();
            
            if (item.getName() != null && item.getName().equalsIgnoreCase(name))
            {
                return true;
            }
            
        }
        return false;
    }
    
    public EJPluginReportScreenItemProperties getItemProperties(String name)
    {
        
        Iterator<EJPluginReportScreenItemProperties> props = _itemProperties.iterator();
        
        while (props.hasNext())
        {
            EJPluginReportScreenItemProperties item = props.next();
            
            if (item.getName().equalsIgnoreCase(name))
            {
                return item;
            }
        }
        return null;
    }
    
    // public void removeItem(String itemName)
    // {
    // Iterator<EJPluginReportScreenItemProperties> props =
    // _itemProperties.iterator();
    //
    // while (props.hasNext())
    // {
    // EJPluginReportScreenItemProperties item = props.next();
    //
    // if (item.getName().equalsIgnoreCase(itemName))
    // {
    //
    // removeItem(item);
    //
    // break;
    // }
    // }
    // }
    
    public int getItemCount()
    {
        return _itemProperties.size();
    }
    
    public void removeItem(EJPluginReportScreenItemProperties item)
    {
        
        _itemProperties.remove(item);
    }
    
    public EJPluginReportScreenItemProperties createItem(EJReportScreenItemType type, String name, int index)
    {
        EJPluginReportScreenItemProperties itemProperties = null;
        switch (type)
        {
            case LABEL:
                itemProperties = new EJPluginReportScreenItemProperties.Label(_blockProperties);
                break;
            case TEXT:
                itemProperties = new EJPluginReportScreenItemProperties.Text(_blockProperties);
                break;
            case NUMBER:
                itemProperties = new EJPluginReportScreenItemProperties.Number(_blockProperties);
                break;
            case DATE:
                itemProperties = new EJPluginReportScreenItemProperties.Date(_blockProperties);
                break;
            case IMAGE:
                itemProperties = new EJPluginReportScreenItemProperties.Image(_blockProperties);
                break;
            
            default:
                return null;
        }
        
        itemProperties.setName(name);
        
        if (index == -1)
        {
            addItemProperties(itemProperties);
        }
        else
        {
            addItemProperties(index, itemProperties);
        }
        
        return itemProperties;
    }
    
}
