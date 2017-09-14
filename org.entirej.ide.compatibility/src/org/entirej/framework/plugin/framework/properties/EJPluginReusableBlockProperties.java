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

import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;

public class EJPluginReusableBlockProperties
{
    private EJPluginLovDefinitionContainer _lovDefinitionContainer;
    private EJPluginBlockProperties        _blockProperties;
    private EJPluginCanvasProperties       _canvasProperties;
    
    public EJPluginReusableBlockProperties(EJPluginBlockProperties blockProperties)
    {
        _blockProperties = blockProperties;
    }
    
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    /**
     * @return the canvasProperties
     */
    public EJPluginCanvasProperties getCanvasProperties()
    {
        return _canvasProperties;
    }
    
    /**
     * @param canvasProperties
     *            the canvasProperties to set
     */
    public void setCanvasProperties(EJPluginCanvasProperties canvasProperties)
    {
        _canvasProperties = canvasProperties;
    }
    
    public EJPluginLovDefinitionContainer getLovDefinitionContainer()
    {
        return _lovDefinitionContainer;
    }
    
    public void setLovDefinitionContainer(EJPluginLovDefinitionContainer lovDefinitionContainer)
    {
        _lovDefinitionContainer = lovDefinitionContainer;
    }
}
