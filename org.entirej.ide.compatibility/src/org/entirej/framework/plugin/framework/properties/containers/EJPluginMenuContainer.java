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
import java.util.List;

import org.entirej.framework.plugin.framework.properties.EJPluginMenuProperties;

public class EJPluginMenuContainer
{
    private List<EJPluginMenuProperties> _menuProperties;
    
    public EJPluginMenuContainer()
    {
        _menuProperties = new ArrayList<EJPluginMenuProperties>();
    }
    
    public void dispose()
    {
        _menuProperties.clear();
    }
    
    public void clear()
    {
        _menuProperties.clear();
    }
    
    public void addMenuProperties(EJPluginMenuProperties menuProperties)
    {
        if (menuProperties != null)
        {
            _menuProperties.add(menuProperties);
        }
    }
    
    public void removeMenuProperties(EJPluginMenuProperties menu)
    {
        _menuProperties.remove(menu);
    }
    
    public Collection<EJPluginMenuProperties> getAllMenuProperties()
    {
        return _menuProperties;
    }
    
}
