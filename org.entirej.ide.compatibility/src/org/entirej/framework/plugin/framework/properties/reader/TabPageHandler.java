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

import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class TabPageHandler extends EntireJTagHandler
{
    private EJPluginCanvasProperties  _canvasProperties;
    private EJPluginTabPageProperties _tabPageProperties;
    
    private static final String       ELEMENT_TAB_PAGE        = "tabPage";
    private static final String       ELEMENT_PAGE_TITLE      = "pageTitle";
    private static final String       ELEMENT_FIRST_NAV_BLOCK = "firstNavigationalBlock";
    private static final String       ELEMENT_FIRST_NAV_ITEM  = "firstNavigationalItem";
    private static final String       ELEMENT_ENABLED         = "enabled";
    private static final String       ELEMENT_VISIBLE         = "visible";
    private static final String       ELEMENT_NUM_COLS        = "numCols";
    private static final String       ELEMENT_CANVAS          = "canvas";
    
    public TabPageHandler(EJPluginCanvasProperties canvasProperties)
    {
        _canvasProperties = canvasProperties;
    }
    
    public EJPluginTabPageProperties getTabPageProperties()
    {
        return _tabPageProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_CANVAS))
        {
            setDelegate(new CanvasHandler(_canvasProperties.getFormProperties()));
            return;
        }
        else if (name.equals(ELEMENT_TAB_PAGE))
        {
            String tabName = attributes.getValue("name");
            _tabPageProperties = new EJPluginTabPageProperties(_canvasProperties, tabName);
        }
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_TAB_PAGE))
        {
            quitAsDelegate();
            return;
        }
        else if (name.equals(ELEMENT_PAGE_TITLE))
        {
            _tabPageProperties.setPageTitle(value);
        }
        else if (name.equals(ELEMENT_FIRST_NAV_BLOCK))
        {
            _tabPageProperties.setFirstNavigationalBlock(value);
        }
        else if (name.equals(ELEMENT_FIRST_NAV_ITEM))
        {
            _tabPageProperties.setFirstNavigationalItem(value);
        }
        else if (name.equals(ELEMENT_ENABLED))
        {
            if (value.length() > 0)
            {
                _tabPageProperties.setEnabled(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_VISIBLE))
        {
            if (value.length() > 0)
            {
                _tabPageProperties.setVisible(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_NUM_COLS))
        {
            if (value.length() > 0)
            {
                _tabPageProperties.setNumCols(Integer.parseInt(value));
            }
            else
            {
                _tabPageProperties.setNumCols(1);
            }
        }
        
    }
    
    @Override
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_CANVAS))
        {
            _tabPageProperties.addContainedCanvas(((CanvasHandler) currentDelegate).getCanvasProperties());
        }
    }
    
}
