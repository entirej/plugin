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
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;

public class EJPluginLovDefinitionContainer
{
    private List<EJPluginLovDefinitionProperties> _lovDefProperties;
    private EJPluginFormProperties                _formProperties;
    
    public EJPluginLovDefinitionContainer(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
        _lovDefProperties = new ArrayList<EJPluginLovDefinitionProperties>();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public void dispose()
    {
        _lovDefProperties.clear();
        _formProperties = null;
    }
    
    public boolean contains(String lovDefinitionName)
    {
        Iterator<EJPluginLovDefinitionProperties> iti = _lovDefProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginLovDefinitionProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(lovDefinitionName))
            {
                return true;
            }
        }
        return false;
    }
    
    public void addLovDefinitionProperties(EJPluginLovDefinitionProperties defProperties)
    {
        if (defProperties != null)
        {
            _lovDefProperties.add(defProperties);
        }
    }
    
    public void addLovDefinitionProperties(int index, EJPluginLovDefinitionProperties defProperties)
    {
        if (defProperties != null)
        {
            _lovDefProperties.add(index, defProperties);
        }
    }
    
    public void removeLovDefinitionProperties(EJPluginLovDefinitionProperties defProperties)
    {
        _lovDefProperties.remove(defProperties);
    }
    
    public void removeLovDefinitionProperties(String defName)
    {
        Iterator<EJPluginLovDefinitionProperties> iti = _lovDefProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginLovDefinitionProperties props = iti.next();
            
            if (props.getName().equalsIgnoreCase(defName))
            {
                _lovDefProperties.remove(props);
                
                break;
            }
        }
    }
    
    public EJPluginLovDefinitionProperties getLastAddedDef()
    {
        return _lovDefProperties.size() > 0 ? _lovDefProperties.get(_lovDefProperties.size() - 1) : null;
    }
    
    /**
     * Used to retrieve a specific lov definition properties.
     * 
     * @return If the lov definition name parameter is a valid lov definition
     *         contained within this form, then its properties will be returned
     *         if however the name is <code>null</code> or not valid, then a
     *         <b>null</b> object will be returned.
     */
    public EJPluginLovDefinitionProperties getLovDefinitionProperties(String defName)
    {
        
        Iterator<EJPluginLovDefinitionProperties> iti = _lovDefProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginLovDefinitionProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(defName))
            {
                return (EJPluginLovDefinitionProperties) props;
            }
        }
        return null;
    }
    
    /**
     * Used to retrieve a specific lov definition properties where it is
     * referenced to a reusable lov definition properties with the given name
     * 
     * @return If there is a lov definition that referenced this reusable love
     *         definition then its properties will be returned, if however the
     *         name is <code>null</code> or not referenced, then a <b>null</b>
     *         object will be returned.
     */
    public EJPluginLovDefinitionProperties getLovDefinitionPropertiesForReferencedName(String referencedName)
    {
        
        Iterator<EJPluginLovDefinitionProperties> iti = _lovDefProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginLovDefinitionProperties props = iti.next();
            if (props.getReferencedLovDefinitionName() != null && props.getReferencedLovDefinitionName().equalsIgnoreCase(referencedName))
            {
                return (EJPluginLovDefinitionProperties) props;
            }
        }
        return null;
    }
    
    /**
     * Used to return the whole list of lov definitions contained within this
     * form. The
     * 
     * @return The lov definitions contained within this form
     */
    public List<EJPluginLovDefinitionProperties> getAllLovDefinitionProperties()
    {
        return new ArrayList<EJPluginLovDefinitionProperties>(_lovDefProperties);
    }
    
    public boolean isEmpty()
    {
        return _lovDefProperties.isEmpty();
    }
}
