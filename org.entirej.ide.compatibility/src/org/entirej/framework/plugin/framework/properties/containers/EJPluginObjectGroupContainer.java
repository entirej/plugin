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
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;

public class EJPluginObjectGroupContainer
{
    private List<EJPluginObjectGroupProperties> _objGroupProperties;
    private EJPluginFormProperties        _formProperties;
    
    public EJPluginObjectGroupContainer(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
        _objGroupProperties = new ArrayList<EJPluginObjectGroupProperties>();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public void dispose()
    {
        for (EJPluginObjectGroupProperties props : _objGroupProperties)
        {
            props.dispose();
        }
        _objGroupProperties.clear();
        _formProperties = null;
        _objGroupProperties = null;
    }
    
    public boolean isEmpty()
    {
        return _objGroupProperties.isEmpty();
        
    }
    
    public boolean contains(String objGroupName)
    {
        Iterator<EJPluginObjectGroupProperties> iti = _objGroupProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginObjectGroupProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(objGroupName))
            {
                return true;
            }
        }
        return false;
    }
    
    public void addObjectGroupProperties(EJPluginObjectGroupProperties blockProperties)
    {
        if (blockProperties != null)
        {
            _objGroupProperties.add(blockProperties);
        }
    }
    
    public void addObjectGroupProperties(int index, EJPluginObjectGroupProperties objGroupProperties)
    {
        if (objGroupProperties != null)
        {
            _objGroupProperties.add(index, objGroupProperties);
        }
    }
    
    public void removeObjectGroupProperties(EJPluginObjectGroupProperties props)
    {
        
        if (_objGroupProperties.contains(props))
        {
            
            //FIXME: add complete remove ref.
            
            _objGroupProperties.remove(props);
            
        }
        
    }
    

    public EJPluginObjectGroupProperties getObjectGroupProperties(String objGroupName)
    {
        
        Iterator<EJPluginObjectGroupProperties> iti = _objGroupProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginObjectGroupProperties props = iti.next();
            
            if (props.getName().equalsIgnoreCase(objGroupName))
            {
                return props;
            }
        }
        return null;
    }

    public List<EJPluginObjectGroupProperties> getAllObjectGroupProperties()
    {
        return _objGroupProperties;
    }
    

}
