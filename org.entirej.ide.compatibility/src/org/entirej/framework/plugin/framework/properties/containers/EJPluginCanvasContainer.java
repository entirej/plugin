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
/*
 * Created on 18.09.2006
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.containers.interfaces.IPluginCanvasContainer;


public class EJPluginCanvasContainer implements IPluginCanvasContainer
{

    private static final long serialVersionUID = -3796019094171109179L;
    private List<EJPluginCanvasProperties> _canvasProperties;
    private EJPluginFormProperties         _formProperties;
    
    public EJPluginCanvasContainer(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
        _canvasProperties = new ArrayList<EJPluginCanvasProperties>();
    }
    
    public EJPluginCanvasProperties getParnetCanvas()
    {
        return null;
    }
    
    public void dispose()
    {
        for (EJCanvasProperties canvas : _canvasProperties)
        {
            ((EJPluginCanvasProperties) canvas).dispose();
        }
        _canvasProperties.clear();
        _formProperties = null;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public boolean contains(String canvasName)
    {
        Iterator<EJPluginCanvasProperties> iti = _canvasProperties.iterator();
        while (iti.hasNext())
        {
            EJCanvasProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(canvasName))
            {
                return true;
            }
        }
        return false;
    }
    
    public void addCanvasProperties(EJCanvasProperties canvasProperties)
    {
        if (canvasProperties != null)
        {
            _canvasProperties.add((EJPluginCanvasProperties) canvasProperties);
            ((EJPluginCanvasProperties) canvasProperties).setParentCanvasContainer(this);
        }
    }
    
    public void addCanvasProperties(int index, EJCanvasProperties canvasProperties)
    {
        if (canvasProperties != null)
        {
            _canvasProperties.add(index, (EJPluginCanvasProperties) canvasProperties);
            ((EJPluginCanvasProperties) canvasProperties).setParentCanvasContainer(this);
        }
    }
    public void replaceCanvasProperties(EJPluginCanvasProperties oldProp, EJPluginCanvasProperties newProp)
    {
        if (oldProp != null && newProp !=null)
        {
            int indexOf = _canvasProperties.indexOf(oldProp);
            if(indexOf>-1)
            {
                _canvasProperties.set(indexOf, newProp);
            }
            else
            {
                _canvasProperties.add(newProp);
            }
            ((EJPluginCanvasProperties) newProp).setParentCanvasContainer(this);
        }
    }
    
    public void removeCanvasProperties(String canvasName)
    {
        Iterator<EJPluginCanvasProperties> iti = _canvasProperties.iterator();
        while (iti.hasNext())
        {
            EJCanvasProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(canvasName))
            {
                _canvasProperties.remove(props);
                
                break;
            }
        }
    }
    
    public void removeCanvasProperties(EJCanvasProperties canvasProperties)
    {
        _canvasProperties.remove(canvasProperties);
    }
    
    public boolean isEmpty()
    {
        return _canvasProperties.isEmpty();
    }
    
    /**
     * Return the <code>CanvasProperties</code> for the given name
     * 
     * @param canvasName
     *            The name of the required canvas properties
     * @return The <code>CanvasProperties</code> for the given name or null of
     *         there is no canvas with the given name
     */
    public EJPluginCanvasProperties getCanvasProperties(String canvasName)
    {
        if (canvasName == null || canvasName.trim().length() == 0)
        {
            return null;
        }
        else
        {
            Iterator<EJPluginCanvasProperties> iti = _canvasProperties.iterator();
            while (iti.hasNext())
            {
                EJPluginCanvasProperties props = iti.next();
                if (props.getName().equalsIgnoreCase(canvasName))
                {
                    return props;
                }
            }
            return null;
        }
    }
    
    /**
     * Used to return the whole list of canvases contained within this form.
     * 
     * @return A <code>Collection</code> containing this forms
     *         <code>Canvas Properties</code>
     */
    public Collection<EJCanvasProperties> getAllCanvasProperties()
    {
        return new ArrayList<EJCanvasProperties>(_canvasProperties);
    }
    
    public List<EJPluginCanvasProperties> getCanvasProperties()
    {
        return _canvasProperties;
    }
    
    /**
     * Used to return the whole list of canvases contained within this form.
     * 
     * @return A <code>Collection</code> containing this forms
     *         <code>Canvas Properties</code>
     */
    @Deprecated
    public Collection<EJPluginCanvasProperties> getAllResequencableCanvasProperties()
    {
        
        return new ArrayList<EJPluginCanvasProperties>(_canvasProperties);
    }
    
    /**
     * This is an internal method to generate a default name for a newly created
     * canvas
     * <p>
     * The name will be built as follows:
     * <p>
     * <code>CANVAS_</code> the next highest available canvas number, if there
     * are other canvases with the default name
     * 
     * @return The default canvas name
     */
    public String generateCanvasName()
    {
        int nextNr = 10;
        while (canvasNameExists("CANVAS_" + nextNr))
        {
            nextNr++;
        }
        return "CANVAS_" + nextNr;
    }
    
    private boolean canvasNameExists(String name)
    {
        Iterator<EJPluginCanvasProperties> canvases = _canvasProperties.iterator();
        while (canvases.hasNext())
        {
            EJCanvasProperties props = canvases.next();
            if (props.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
}
