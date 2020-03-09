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

public class EJPluginMenuLeafActionProperties extends EJPluginMenuLeafProperties
{
    private String _menuAction;
    private String _hint;
    
    public EJPluginMenuLeafActionProperties(EJPluginMenuProperties menu, EJPluginMenuLeafContainer container)
    {
        super(menu, container);
    }
    
    public String getMenuAction()
    {
        return _menuAction;
    }
    
    public void setMenuAction(String menuAction)
    {
        _menuAction = menuAction;
    }
    
    public String getHint()
    {
        return _hint;
    }
    
    public void setHint(String hint)
    {
        _hint = hint;
    }
    
}
