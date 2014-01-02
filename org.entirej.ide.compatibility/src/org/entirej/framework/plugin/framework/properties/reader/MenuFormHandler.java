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
package org.entirej.framework.plugin.framework.properties.reader;

import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafContainer;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class MenuFormHandler extends EntireJTagHandler implements MenuLeafHandler
{
    private EJPluginMenuProperties         _menu;
    private EJPluginMenuLeafContainer      _container;
    private EJPluginMenuLeafFormProperties _form;
    
    private static final String            LEAF         = "leaf";
    private static final String            NAME         = "name";
    private static final String            DISPLAY_NAME = "displayName";
    private static final String            FORM_NAME    = "formName";
    private static final String            HINT         = "hint";
    private static final String            ICON         = "icon";
    
    public MenuFormHandler(EJPluginMenuProperties menu, EJPluginMenuLeafContainer container)
    {
        _menu = menu;
        _container = container;
    }
    
    @Override
    public EJPluginMenuLeafProperties getLeafProperties()
    {
        return _form;
    }
    
    @Override
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(LEAF))
        {
            _form = new EJPluginMenuLeafFormProperties(_menu, _container);
            
            String menuName = attributes.getValue(NAME);
            _form.setName(menuName);
        }
    }
    
    public EJPluginMenuProperties getProperties()
    {
        return _menu;
    }
    
    @Override
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(LEAF))
        {
            quitAsDelegate();
            return;
        }
        
        else if (DISPLAY_NAME.equals(name))
        {
            _form.setDisplayName(value);
        }
        
        else if (FORM_NAME.equals(name))
        {
            _form.setFormName(value);
        }
        else if (HINT.equals(name))
        {
            _form.setHint(value);
        }
        else if (ICON.equals(name))
        {
            _form.setIconName(value);
        }
    }
}
