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
package org.entirej.framework.plugin.framework.properties;

public class EJPluginMenuLeafProperties
{
    private EJPluginMenuProperties    _menu;
    private EJPluginMenuLeafContainer _container;
    private String                    _name;
    private String                    _displayName;
    private String                    _iconName;
    
    public EJPluginMenuLeafProperties(EJPluginMenuProperties menu, EJPluginMenuLeafContainer contianer)
    {
        _menu = menu;
        _container = contianer;
    }
    
    public void resetContainer(EJPluginMenuLeafContainer contianer)
    {
        _container = contianer;
    }
    
    public EJPluginMenuProperties getMenu()
    {
        return _menu;
    }
    
    public EJPluginMenuLeafContainer getContainer()
    {
        return _container;
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public String getDisplayName()
    {
        return _displayName;
    }
    
    public void setDisplayName(String displayName)
    {
        _displayName = displayName;
    }
    
    public String getIconName()
    {
        return _iconName;
    }
    
    public void setIconName(String iconName)
    {
        _iconName = iconName;
    }
    
}
