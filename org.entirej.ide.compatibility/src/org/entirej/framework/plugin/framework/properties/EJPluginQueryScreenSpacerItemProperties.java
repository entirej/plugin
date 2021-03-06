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

import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;

public class EJPluginQueryScreenSpacerItemProperties extends EJPluginQueryScreenItemProperties
{
    /**
     * 
     */
    private static final long serialVersionUID = 48671811096662667L;

    public EJPluginQueryScreenSpacerItemProperties(EJPluginItemGroupProperties itemGroupProperties, boolean addDefaults)
    {
        super(itemGroupProperties, addDefaults, true);
        
        int spacerCount = 0;
        for (EJDevScreenItemDisplayProperties props : itemGroupProperties.getAllItemDisplayProperties())
        {
            if (props.isSpacerItem())
            {
                spacerCount++;
            }
        }
        setReferencedItemName("spacer" + spacerCount);
    }
    
    public void refreshQueryScreenRendererRequiredProperties(boolean addDefaults)
    {
        if (getBlockProperties().getQueryScreenRendererDefinition() == null)
        {
            setQueryScreenRendererRequiredProperties(null);
        }
        else
        {
            setQueryScreenRendererRequiredProperties(ExtensionsPropertiesFactory.createQueryScreenRendererSpacerItemProperties(getBlockProperties(),
                    addDefaults));
        }
    }
}
