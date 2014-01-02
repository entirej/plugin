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

import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;

public class EJPluginInsertScreenItemGroupContainer extends EJPluginItemGroupContainer
{
    /**
     * 
     */
    private static final long serialVersionUID = -1784627202764392760L;

    public EJPluginInsertScreenItemGroupContainer(EJPluginBlockProperties blockProperties)
    {
        super(blockProperties, EJPluginItemGroupContainer.INSERT_SCREEN);
    }
    
    public boolean isRoot()
    {
        return true;
    }
}
