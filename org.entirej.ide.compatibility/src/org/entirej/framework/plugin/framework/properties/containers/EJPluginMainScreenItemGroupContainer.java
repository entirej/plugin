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

import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;

public class EJPluginMainScreenItemGroupContainer extends EJPluginItemGroupContainer implements EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long serialVersionUID = 4096402479060180159L;

    public EJPluginMainScreenItemGroupContainer(EJPluginBlockProperties blockProperties)
    {
        super(blockProperties, EJPluginItemGroupContainer.MAIN_SCREEN);
    }
    
    public boolean isRoot()
    {
        return true;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return getBlockProperties().getFormProperties();
    }
    
}
