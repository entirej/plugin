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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.core.properties.containers.interfaces.EJDrawerPagePropertiesContainer;
import org.entirej.framework.core.properties.interfaces.EJDrawerPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginDrawerPageProperties;

public class EJPluginDrawerPageContainer implements EJDrawerPagePropertiesContainer
{
    /**
     * 
     */
    private static final long serialVersionUID = -4497521722579102174L;
    private List<EJPluginDrawerPageProperties> _drawerPageProperties;
    private EJPluginCanvasProperties        _canvasProperties;
    
    public EJPluginDrawerPageContainer(EJPluginCanvasProperties canvasProperties)
    {
        _canvasProperties = canvasProperties;
        _drawerPageProperties = new ArrayList<EJPluginDrawerPageProperties>();
    }
    
    public EJPluginCanvasProperties getCanvasProperties()
    {
        return _canvasProperties;
    }
    
    public void clear()
    {
        _drawerPageProperties.clear();
        _canvasProperties = null;
    }
    
    public boolean contains(String pageName)
    {
        Iterator<EJPluginDrawerPageProperties> iti = _drawerPageProperties.iterator();
        while (iti.hasNext())
        {
            EJDrawerPageProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(pageName))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean contains(EJPluginDrawerPageProperties page)
    {
        
        return _drawerPageProperties.contains(page);
    }
    
    public void addDrawerPageProperties(EJPluginDrawerPageProperties tabPageProperties)
    {
        if (tabPageProperties != null)
        {
            _drawerPageProperties.add(tabPageProperties);
        }
    }
    
    public void addDrawerPageProperties(int index, EJPluginDrawerPageProperties tabPageProperties)
    {
        if (tabPageProperties != null)
        {
            _drawerPageProperties.add(index, tabPageProperties);
        }
    }
    
    public void removeDrawerPageProperties(String pageName)
    {
        Iterator<EJPluginDrawerPageProperties> iti = _drawerPageProperties.iterator();
        while (iti.hasNext())
        {
            EJDrawerPageProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(pageName))
            {
                _drawerPageProperties.remove(props);
                
                break;
            }
        }
    }
    
    public void removeDrawerPageProperties(EJPluginDrawerPageProperties properties)
    {
        _drawerPageProperties.remove(properties);
    }
    
    /**
     * Return the <code>DrawerPageProperties</code> for the given name
     * 
     * @param pageNa
     *            ,e The name of the required tab page properties
     * @return The <code>DrawerPageProperties</code> for the given name or null of
     *         there is no tab page with the given name
     */
    public EJDrawerPageProperties getDrawerPageProperties(String pageName)
    {
        if (pageName == null || pageName.trim().length() == 0)
        {
            return null;
        }
        else
        {
            Iterator<EJPluginDrawerPageProperties> iti = _drawerPageProperties.iterator();
            while (iti.hasNext())
            {
                EJDrawerPageProperties props = iti.next();
                if (props.getName().equalsIgnoreCase(pageName))
                {
                    return props;
                }
            }
            return null;
        }
    }
    
    /**
     * Used to return the whole list of tab pages contained within this canvas.
     * 
     * @return A <code>Collection</code> containing this canvases
     *         <code>DrawerPageProperties</code>
     */
    public Collection<EJDrawerPageProperties> getAllDrawerPageProperties()
    {
        return new ArrayList<EJDrawerPageProperties>(_drawerPageProperties);
    }
    
    public List<EJPluginDrawerPageProperties> getDrawerPageProperties()
    {
        return _drawerPageProperties;
    }
    
    /**
     * Used to return the whole list of tab pages contained within this canvas.
     * 
     * @return A <code>Collection</code> containing this canvases
     *         <code>DrawerPageProperties</code>
     */
    public Collection<EJPluginDrawerPageProperties> getAllResequancableDrawerPageProperties()
    {
        
        return new ArrayList<EJPluginDrawerPageProperties>(_drawerPageProperties);
    }
    
    /**
     * This is an internal method to generate a default name for a newly created
     * tab page
     * <p>
     * The name will be built as follows:
     * <p>
     * <code>TAB_PAGE_</code> plus the next highest available tab page number,
     * if there are other tab page with the default name
     * 
     * @return The default canvas name
     */
    public String generateDrawerName()
    {
        int nextNr = 10;
        while (drawerNameExists("TAB_PAGE_" + nextNr))
        {
            nextNr++;
        }
        return "TAB_PAGE_" + nextNr;
    }
    
    public boolean drawerNameExists(String name)
    {
        Iterator<EJPluginDrawerPageProperties> tabPagesProps = _drawerPageProperties.iterator();
        while (tabPagesProps.hasNext())
        {
            EJDrawerPageProperties props = tabPagesProps.next();
            if (props.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEmpty()
    {
        return _drawerPageProperties.isEmpty();
    }
}
