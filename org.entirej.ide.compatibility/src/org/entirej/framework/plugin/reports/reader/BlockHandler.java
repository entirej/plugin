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

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.reader.EntireJTagHandler;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BlockHandler extends EntireJTagHandler
{
    private EJPluginReportBlockProperties         _blockProperties;
    private EJPluginReportProperties          _formProperties;
    
    private static final String             ELEMENT_BLOCK                             = "block";
    private static final String             ELEMENT_DESCRIPTION                       = "description";
    private static final String             ELEMENT_CANVAS                            = "canvasName";
    private static final String             ELEMENT_SERVICE_CLASS_NAME                = "serviceClassName";
    private static final String             ELEMENT_ACTION_PROCESSOR                  = "actionProcessorClassName";
    
    
    private static final String             ELEMENT_ITEM                              = "item";
    
    
    public BlockHandler(EJPluginReportProperties formProperties)
    {
        _formProperties = formProperties;
    }
    
    public void dispose()
    {
        _formProperties = null;
        _blockProperties = null;
    }
    
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public EJPluginBlockProperties createNewBlockProperties(IJavaProject javaProject, EJPluginFormProperties formProperties, String blockName,
            boolean isControlBlock)
    {
        return new EJPluginBlockProperties(formProperties, blockName, isControlBlock);
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
       
        
        if (name.equals(ELEMENT_ITEM))
        {
            setDelegate(new ItemHandler(_blockProperties));
            return;
        }
        
        if (name.equals(ELEMENT_BLOCK))
        {
            String blockName = attributes.getValue("name");
            String referenced = attributes.getValue("referenced");
            String referencedBlockName = attributes.getValue("referencedBlockName");
            String isControlBlock = attributes.getValue("controlBlock");
            
            if (Boolean.parseBoolean(referenced))
            {
                //FIXME
                
            }
            else
            {
                _blockProperties = new EJPluginReportBlockProperties(_formProperties, blockName, Boolean.parseBoolean(isControlBlock == null ? "false" : isControlBlock));
              
            }
            if (_blockProperties != null)
            {
                //_blockProperties.setd(Boolean.parseBoolean(referenced));
            }
        }
        
        if (_blockProperties == null || !_blockProperties.isReferenceBlock())
        {
           //TODO
        }
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_BLOCK))
        {
            quitAsDelegate();
            return;
        }
        
        
       
        else if (name.equals(ELEMENT_DESCRIPTION))
        {
            _blockProperties.setDescription(value);
        }
        
       
        else if (name.equals(ELEMENT_CANVAS))
        {
            _blockProperties.setCanvasName(value);
        }

       
        else if (name.equals(ELEMENT_SERVICE_CLASS_NAME))
        {
            _blockProperties.setServiceClassName(value, false);
        }
        else if (name.equals(ELEMENT_ACTION_PROCESSOR))
        {
            _blockProperties.setActionProcessorClassName(value);
        }
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_ITEM))
        {
            EJPluginReportItemProperties itemProperties = ((ItemHandler) currentDelegate).getItemProperties();
            if (itemProperties == null)
            {
                return;
            }
            
            // If the item name is null, then this item is for a screen item and
            // should be ignored
            if (itemProperties.getName() == null)
            {
                return;
            }
            if (_blockProperties.isReferenceBlock())
            {
               //FIXME
            }
            else
            {
                _blockProperties.getItemContainer().addItemProperties(itemProperties);
            }
            return;
        }
        
        if (_blockProperties == null || !_blockProperties.isReferenceBlock())
        {
            
        }
    }
}
