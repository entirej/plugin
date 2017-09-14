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

public class EJPluginMainScreenSpacerItemProperties extends EJPluginMainScreenItemProperties
{
    /**
     * 
     */
    private static final long serialVersionUID = 1585998569402140250L;

    public EJPluginMainScreenSpacerItemProperties(EJPluginItemGroupProperties itemGroupProperties, boolean addDefaults)
    {
        super(itemGroupProperties, addDefaults, true);
    }
    
    public void refreshBlockRendererRequiredProperties(boolean addDefaults)
    {
        if (getBlockProperties().getBlockRendererName() == null || getBlockProperties().getBlockRendererName().trim().length() == 0)
        {
            setBlockRendererRequiredProperties(null);
        }
        else
        {
            setBlockRendererRequiredProperties(ExtensionsPropertiesFactory.createBlockRendererSpacerItemProperties(getBlockProperties(), addDefaults,
                    getBlockRendererRequiredProperties()));
        }
    }
    
    public void refreshLovRendererRequiredProperties()
    {
        if (getBlockProperties().getLovDefinition() == null)
        {
            return;
        }
        
        if (getBlockProperties().getLovDefinition().getLovRendererName() == null
                || getBlockProperties().getLovDefinition().getLovRendererName().trim().length() == 0)
        {
            setLovRendererRequiredProperties(null);
        }
        else
        {
            setLovRendererRequiredProperties(ExtensionsPropertiesFactory.createLovRequiredSpacerItemProperties(getBlockProperties().getEntireJProperties(),
                    getBlockProperties().getFormProperties(), getBlockProperties().getLovDefinition(), false, getLovRendererRequiredProperties()));
        }
    }
}
