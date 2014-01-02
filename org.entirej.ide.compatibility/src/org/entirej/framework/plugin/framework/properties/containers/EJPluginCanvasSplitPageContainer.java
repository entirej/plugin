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

import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;

public class EJPluginCanvasSplitPageContainer extends EJPluginCanvasContainer
{
    /**
     * 
     */
    private static final long serialVersionUID = 4686820413532760942L;
    private EJPluginCanvasProperties _canvasGroup;
    
    public EJPluginCanvasSplitPageContainer(EJPluginFormProperties formProperties, EJPluginCanvasProperties canvasGroup)
    {
        super(formProperties);
        _canvasGroup = canvasGroup;
    }
    
    public EJPluginCanvasProperties getCanvasSplit()
    {
        return _canvasGroup;
    }
    
    public EJPluginCanvasProperties getParnetCanvas()
    {
        return _canvasGroup;
    }
}
