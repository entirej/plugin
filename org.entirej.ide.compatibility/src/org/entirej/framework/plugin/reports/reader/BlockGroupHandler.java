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
package org.entirej.framework.plugin.reports.reader;

import org.entirej.framework.plugin.framework.properties.reader.EntireJTagHandler;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BlockGroupHandler extends EntireJTagHandler
{
    private EJPluginReportProperties     _formProperties;
    private BlockGroup _blockGroup;
    

    private static final String    ELEMENT_BLOCK                  = "block";
    private static final String    ELEMENT_BLOCK_GROUP            = "blockGroup";
    
    public BlockGroupHandler(EJPluginReportProperties formProperties)
    {
        _formProperties = formProperties;
        _blockGroup = new BlockGroup();
    }
    public BlockGroupHandler(EJPluginReportProperties formProperties,BlockGroup _blockGroup)
    {
        _formProperties = formProperties;
        this._blockGroup = _blockGroup;
    }
    
    public BlockGroup getBlockGroup()
    {
        return _blockGroup;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_BLOCK_GROUP))
        {
          
            String value = attributes.getValue("name");
            if(value!=null && value.trim().length()>0)
                _blockGroup.setName(value);
           
        }
        else if (name.equals(ELEMENT_BLOCK))
        {
            setDelegate(new BlockHandler(_formProperties));
        }
    }
    
    @Override
    protected void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_BLOCK_GROUP))
        {
            quitAsDelegate();
        }
    }
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_BLOCK))
        {
            _blockGroup.addBlockProperties(((BlockHandler) currentDelegate).getBlockProperties());
            return;
        }
    }
    
}
