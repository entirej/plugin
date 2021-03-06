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
package org.entirej.framework.plugin.framework.properties.reader;

import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginItemGroupContainer;

public class MainScreenItemGroupHandler extends ItemGroupHandler
{
    private MainScreenItemHandler _itemHandler;
    
    public MainScreenItemGroupHandler(EJPluginItemGroupContainer itemGroupContainer, String exitTag)
    {
        super(itemGroupContainer, exitTag);
    }
    
    public void setDelegate()
    {
        _itemHandler = new MainScreenItemHandler(getItemGroupProperties());
        setDelegate(_itemHandler);
    }
    
    public void addItemPropertiesToGroup()
    {
        getItemGroupProperties().addItemProperties(_itemHandler.getItemProperties());
    }
    
    public ItemGroupHandler createChildItemGroupHandler(EJPluginItemGroupProperties itemGroup)
    {
        return new MainScreenItemGroupHandler(itemGroup.getChildItemGroupContainer(), "itemGroupList");
    }
}
