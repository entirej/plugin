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

import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MenuHandler extends EntireJTagHandler
{
    private EJPluginEntireJProperties _ejProperties;
    private EJPluginMenuProperties    _menu;
    
    private static final String       APP_MENU                    = "applicationMenu";
    private static final String       LEAF                        = "leaf";
    private static final String       NAME                        = "name";
    private static final String       DEFAULT                     = "default";
    private static final String       ACTION_PROCESSOR_CLASS_NAME = "actionProcessorClassName";
    
    public MenuHandler(EJPluginEntireJProperties ejProperties)
    {
        _ejProperties = ejProperties;
    }
    
    @Override
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(APP_MENU))
        {
            String menuName = attributes.getValue(NAME);
            _menu = new EJPluginMenuProperties(_ejProperties, menuName);
            _menu.setActionProcessorClassName(attributes.getValue(ACTION_PROCESSOR_CLASS_NAME));
            _menu.setDefault(Boolean.valueOf(attributes.getValue(DEFAULT)));
        }
        else if (name.equals(LEAF))
        {
            String type = attributes.getValue("type");
            if ("BRANCH".equals(type))
            {
                setDelegate(new MenuBranchHandler(_menu, _menu));
            }
            else if ("FORM".equals(type))
            {
                setDelegate(new MenuFormHandler(_menu, _menu));
            }
            else if ("ACTION".equals(type))
            {
                setDelegate(new MenuActionHandler(_menu, _menu));
            }
            else if ("SPACER".equals(type))
            {
                setDelegate(new MenuSpacerHandler(_menu, _menu));
            }
        }
    }
    
    public EJPluginMenuProperties getProperties()
    {
        return _menu;
    }
    
    @Override
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(APP_MENU))
        {
            quitAsDelegate();
            return;
        }
        
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(LEAF))
        {
            _menu.addLeaf(((MenuLeafHandler) currentDelegate).getLeafProperties());
            return;
        }
        
    }
}
