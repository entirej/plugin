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

import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class RelationHandler extends EntireJTagHandler
{
    private EJPluginFormProperties     _formProperties;
    private EJPluginRelationProperties _relationProperties;
    
    private static final String        ELEMENT_RELATION = "relation";
    private static final String        ELEMENT_JOIN     = "join";
    
    public RelationHandler(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
    }
    
    public EJPluginRelationProperties getRelationProperties()
    {
        return _relationProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_RELATION))
        {
            _relationProperties = new EJPluginRelationProperties(_formProperties, attributes.getValue("name"));
            _relationProperties.setMasterBlockName(attributes.getValue("masterBlockName"));
            _relationProperties.setDetailBlockName(attributes.getValue("detailBlockName"));
            
            String value = attributes.getValue("preventMasterlessOperations");
            _relationProperties.setPreventMasterlessOperations(Boolean.parseBoolean((value == null ? "true" : value)));
            value = attributes.getValue("deferredQuery");
            _relationProperties.setDeferredQuery(Boolean.parseBoolean((value == null ? "false" : value)));
            value = attributes.getValue("autoQuery");
            _relationProperties.setAutoQuery(Boolean.parseBoolean((value == null ? "true" : value)));
        }
        else if (name.equals(ELEMENT_JOIN))
        {
            String masterItem = attributes.getValue("masterItem");
            String detailItem = attributes.getValue("detailItem");
            _relationProperties.addJoin(masterItem, detailItem);
        }
    }
    
    @Override
    protected void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_RELATION))
        {
            quitAsDelegate();
        }
    }
    
}
