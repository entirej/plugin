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

import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ObjGroupDefinitionHandler extends EntireJTagHandler
{
    private EJPluginObjectGroupProperties _objgroupDefinitionProperties;
    private EJPluginFormProperties        _formProperties;
    
    private static final String           ELEMENT_OBJGROUP_DEF = "objGroupDefinition";
    
    public ObjGroupDefinitionHandler(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
    }
    
    public EJPluginObjectGroupProperties getObjectGroupDefinitionProperties()
    {
        return _objgroupDefinitionProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        
        if (name.equals(ELEMENT_OBJGROUP_DEF))
        {
            
            String defName = attributes.getValue("name");
            try
            {
                _objgroupDefinitionProperties = _formProperties.getEntireJProperties().getObjectGroupDefinitionProperties(defName);
                if(_objgroupDefinitionProperties==null)
                {
                    _objgroupDefinitionProperties = new EJPluginObjectGroupProperties(defName, _formProperties.getJavaProject());
                    _objgroupDefinitionProperties.setInitialized(false);
                }
            }
            catch (EJDevFrameworkException e)
            {
                _objgroupDefinitionProperties = new EJPluginObjectGroupProperties(defName, _formProperties.getJavaProject());
                _objgroupDefinitionProperties.setInitialized(false);
            }
        }
        
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_OBJGROUP_DEF))
        {
            quitAsDelegate();
            return;
        }
        
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        
    }
}
