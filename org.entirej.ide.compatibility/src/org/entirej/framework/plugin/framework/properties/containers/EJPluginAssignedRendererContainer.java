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

import org.entirej.framework.core.enumerations.EJRendererType;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public class EJPluginAssignedRendererContainer
{
    
    private List<EJPluginRenderer> _renderers;
    
    private EJRendererType         _rendererType;
    
    public EJPluginAssignedRendererContainer(EJRendererType rendererType)
    {
        _rendererType = rendererType;
        _renderers = new ArrayList<EJPluginRenderer>();
    }
    
    public void clear()
    {
        for (EJPluginRenderer renderer : _renderers)
        {
            renderer.clear();
        }
        _renderers.clear();
    }
    
    public EJRendererType getRendererType()
    {
        return _rendererType;
    }
    
    public boolean rendererAssignmentExists(String name)
    {
        Iterator<EJPluginRenderer> iti = _renderers.iterator();
        while (iti.hasNext())
        {
            EJPluginRenderer props = iti.next();
            if (props.getAssignedName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
    
    public void addRendererAssignment(EJPluginRenderer rendererAssignment)
    {
        if (rendererAssignment != null)
        {
            _renderers.add(rendererAssignment);
        }
    }
    public void addRendererAssignment(int index,EJPluginRenderer rendererAssignment)
    {
        if (rendererAssignment != null)
        {
            _renderers.add(index,rendererAssignment);
        }
    }
    
    public void removeRendererAssignment(String name)
    {
        Iterator<EJPluginRenderer> iti = _renderers.iterator();
        
        while (iti.hasNext())
        {
            EJPluginRenderer props = iti.next();
            
            if (props.getAssignedName().equalsIgnoreCase(name))
            {
                iti.remove();

                break;
            }
        }
    }
    
    public void removeRendererAssignment(EJPluginRenderer rendererAssignment)
    {
        _renderers.remove(rendererAssignment);
    }
    

    
    /**
     * Used to retrieve a specific renderer
     * 
     * @param name
     *            The name of the required renderer
     * 
     * @return If the renderer name parameter is a valid renderer contained
     *         within this container, then its properties will be returned. If
     *         however the name is null or not valid, then a <b>null</b> object
     *         will be returned.
     */
    public EJPluginRenderer getRenderer(String name)
    {
        if (name == null || name.trim().length() == 0)
        {
            return null;
        }
        
        Iterator<EJPluginRenderer> iti = _renderers.iterator();
        while (iti.hasNext())
        {
            EJPluginRenderer props = iti.next();
            
            if (props.getAssignedName().equalsIgnoreCase(name))
            {
                return props;
            }
        }
        return null;
    }
    
    /**
     * Used to return the whole list of renderers contained within this
     * container
     * <p>
     * 
     * @return The renderers within this container
     */
    public Collection<EJPluginRenderer> getAllRenderers()
    {
        return _renderers;
    }
}
