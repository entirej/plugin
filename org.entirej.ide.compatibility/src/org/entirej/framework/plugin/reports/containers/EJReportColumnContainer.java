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
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;

public class EJReportColumnContainer
{
    private List<EJPluginReportColumnProperties> _columnProperties;
    private EJPluginReportBlockProperties        _blockProperties;
    
    public EJReportColumnContainer(EJPluginReportBlockProperties blockProperties)
    {
        _blockProperties = blockProperties;
        _columnProperties = new ArrayList<EJPluginReportColumnProperties>();
    }
    
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public void addColumnProperties(EJPluginReportColumnProperties ColumnProperties)
    {
        if (ColumnProperties != null)
        {
            _columnProperties.add(ColumnProperties);
            
        }
    }
    
    public void addColumnProperties(int index, EJPluginReportColumnProperties ColumnProperties)
    {
        if (ColumnProperties != null)
        {
            _columnProperties.add(index, ColumnProperties);
            
        }
    }
    
    public List<EJPluginReportColumnProperties> getAllColumnProperties()
    {
        return _columnProperties;
    }
    
    public boolean contains(String name)
    {
        Iterator<EJPluginReportColumnProperties> iti = _columnProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginReportColumnProperties Column = iti.next();
            
            if (Column.getName() != null && Column.getName().equalsIgnoreCase(name))
            {
                return true;
            }
            
        }
        return false;
    }
    
    public EJPluginReportColumnProperties getColumnProperties(String name)
    {
        
        Iterator<EJPluginReportColumnProperties> props = _columnProperties.iterator();
        
        while (props.hasNext())
        {
            EJPluginReportColumnProperties Column = props.next();
            
            if (Column.getName().equalsIgnoreCase(name))
            {
                return Column;
            }
        }
        return null;
    }
    
    public int getColumnCount()
    {
        return _columnProperties.size();
    }
    
    public void removeColumn(EJPluginReportColumnProperties column)
    {
        
        _columnProperties.remove(column);
    }
    
}
