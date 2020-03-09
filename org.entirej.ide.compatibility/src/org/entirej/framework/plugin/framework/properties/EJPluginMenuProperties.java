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
package org.entirej.framework.plugin.framework.properties;

import java.util.ArrayList;
import java.util.List;

public class EJPluginMenuProperties implements EJPluginMenuLeafContainer
{
    private EJPluginEntireJProperties        _ejProperties;
    private boolean                          _default                  = false;
    private String                           _name;
    private String                           _actionProcessorClassName = "";
    
    private List<EJPluginMenuLeafProperties> _menuLeaves;
    
    public EJPluginMenuProperties(EJPluginEntireJProperties ejProperties, String name)
    {
        _ejProperties = ejProperties;
        _name = name;
        _menuLeaves = new ArrayList<EJPluginMenuLeafProperties>();
    }
    
    public EJPluginEntireJProperties getEntireJProperties()
    {
        return _ejProperties;
    }
    
    public void clear()
    {
        _menuLeaves.clear();
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public boolean isDefault()
    {
        return _default;
    }
    
    public void setDefault(boolean default1)
    {
        _default = default1;
    }
    
    public void dispose()
    {
        _menuLeaves.clear();
    }
    
    public String getActionProcessorClassName()
    {
        return _actionProcessorClassName;
    }
    
    public void setActionProcessorClassName(String processorClassName)
    {
        _actionProcessorClassName = processorClassName;
    }
    
    @Override
    public void addLeaf(EJPluginMenuLeafProperties leaf)
    {
        if (leaf != null)
        {
            _menuLeaves.add(leaf);
            leaf.resetContainer(this);
        }
    }
    
    public void addLeaf(int index, EJPluginMenuLeafProperties leaf)
    {
        if (leaf != null)
        {
            _menuLeaves.add(index, leaf);
            leaf.resetContainer(this);
        }
    }
    
    public void removeLeaf(EJPluginMenuLeafProperties leaf)
    {
        _menuLeaves.remove(leaf);
    }
    
    @Override
    public List<EJPluginMenuLeafProperties> getLeaves()
    {
        return _menuLeaves;
    }
    
}
