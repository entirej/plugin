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
/*
 * Created on 18.09.2006
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties.containers;

import org.entirej.framework.core.properties.containers.interfaces.EJCanvasPropertiesContainer;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;

/**
 * This class is basically a copy of the CanvasContainer. I need a separate
 * class so that the forms ListBlock Tree works correctly
 */
public class EJPluginTabPageCanvasContainer extends EJPluginCanvasContainer implements EJCanvasPropertiesContainer
{
    /**
     * 
     */
    private static final long serialVersionUID = -3069921111442543165L;
    private EJPluginTabPageProperties _tabPageProperties;
    
    public EJPluginTabPageCanvasContainer(EJPluginFormProperties formProperties, EJPluginTabPageProperties tabPageProperties)
    {
        super(formProperties);
        _tabPageProperties = tabPageProperties;
    }
    
    public EJPluginTabPageProperties getTabPageProperties()
    {
        return _tabPageProperties;
    }
    
    public EJPluginCanvasProperties getParnetCanvas()
    {
        return _tabPageProperties.getTabCanvasProperties();
    }
}
