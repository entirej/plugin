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
/*
 * Created on 18.09.2006
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties.containers;

import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginStackedPageProperties;

/**
 * This class is basically a copy of the CanvasContainer. I need a separate
 * class so that the forms ListBlock Tree works correctly
 */
public class EJPluginStackedPageCanvasContainer extends EJPluginCanvasContainer
{
    /**
     * 
     */
    private static final long serialVersionUID = -2828989478906206865L;
    private EJPluginStackedPageProperties _stackedPage;
    
    public EJPluginStackedPageCanvasContainer(EJPluginFormProperties formProperties, EJPluginStackedPageProperties stackedPage)
    {
        super(formProperties);
        _stackedPage = stackedPage;
    }
    
    public EJPluginStackedPageProperties getStackedPageProperties()
    {
        return _stackedPage;
    }
    
    public EJPluginCanvasProperties getParnetCanvas()
    {
        return _stackedPage.getStackedCanvasProperties();
    }
}
