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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;

public class EJPluginBlockContainer
{
    private List<EJPluginBlockProperties> _blockProperties;
    private EJPluginFormProperties        _formProperties;
    
    public EJPluginBlockContainer(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
        _blockProperties = new ArrayList<EJPluginBlockProperties>();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public void dispose()
    {
        for (EJPluginBlockProperties props : _blockProperties)
        {
            props.dispose();
        }
        _blockProperties.clear();
        _formProperties = null;
        _blockProperties = null;
    }
    
    public boolean isEmpty()
    {
        return _blockProperties.isEmpty();
        
    }
    
    public boolean contains(String blockName)
    {
        Iterator<EJPluginBlockProperties> iti = _blockProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginBlockProperties props = iti.next();
            if (props.getName().equalsIgnoreCase(blockName))
            {
                return true;
            }
        }
        return false;
    }
    
    public void addBlockProperties(EJPluginBlockProperties blockProperties)
    {
        if (blockProperties != null)
        {
            _blockProperties.add(blockProperties);
        }
    }
    
    public void replaceBlockProperties(EJPluginBlockProperties oldProp, EJPluginBlockProperties newProp)
    {
        if (oldProp != null && newProp !=null)
        {
            int indexOf = _blockProperties.indexOf(oldProp);
            if(indexOf>-1)
            {
                _blockProperties.set(indexOf, newProp);
            }
            else
            {
                _blockProperties.add(newProp);
            }
        }
    }
    
    public void addBlockProperties(int index, EJPluginBlockProperties blockProperties)
    {
        if (blockProperties != null)
        {
            _blockProperties.add(index, blockProperties);
        }
    }
    
    public void removeBlockProperties(EJPluginBlockProperties props)
    {
        
        if (_blockProperties.contains(props))
        {
            
            if (props.isMirrorChild())
            {
                props.getMirrorParent().removeMirrorChild(props);
            }
            
            // First remove it from all mirrored blocks
            for (EJPluginBlockProperties mirroredBlock : new ArrayList<EJPluginBlockProperties>(props.getMirrorChildren()))
            {
                removeBlockProperties(mirroredBlock);
            }
            
            List<EJPluginBlockProperties> allBlockProperties = new ArrayList<EJPluginBlockProperties>(props.getFormProperties().getBlockContainer()
                    .getAllBlockProperties());
            List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = props.getFormProperties().getLovDefinitionContainer()
                    .getAllLovDefinitionProperties();
            for (EJPluginLovDefinitionProperties lovDefinitionProperties : allLovDefinitionProperties)
            {
                allBlockProperties.add(lovDefinitionProperties.getBlockProperties());
            }
            
            for (EJPluginBlockProperties properties : allBlockProperties)
            {
                List<EJPluginBlockItemProperties> itemProperties = properties.getItemContainer().getAllItemProperties();
                for (EJPluginBlockItemProperties blockItemProperties : itemProperties)
                {
                    
                    String insertValue = blockItemProperties.getDefaultInsertValue();
                    if (insertValue != null && insertValue.trim().length() > 0 && insertValue.indexOf(":") > 0)
                    {
                        if ("BLOCK_ITEM".equals(insertValue.substring(0, insertValue.indexOf(":"))))
                        {
                            String value = insertValue.substring(insertValue.indexOf(":") + 1);
                            String[] split = value.split("\\.");
                            if (split.length == 2)
                            {
                                if (props.getName().equals(split[0]))
                                {
                                    
                                    blockItemProperties.setDefaultInsertValue("");
                                    
                                }
                            }
                        }
                        
                    }
                    
                    String queryValue = blockItemProperties.getDefaultQueryValue();
                    if (queryValue != null && queryValue.trim().length() > 0 && queryValue.indexOf(":") > 0)
                    {
                        if ("BLOCK_ITEM".equals(queryValue.substring(0, queryValue.indexOf(":"))))
                        {
                            String value = queryValue.substring(queryValue.indexOf(":") + 1);
                            String[] split = value.split("\\.");
                            if (split.length == 2)
                            {
                                if (props.getName().equals(split[0]))
                                {
                                    
                                    blockItemProperties.setDefaultQueryValue("");
                                    
                                }
                            }
                        }
                        
                    }
                }
            }
            
            _blockProperties.remove(props);
            
        }
        
    }
    
    /**
     * Used to retrieve a specific blocks properties.
     * 
     * @return If the block name parameter is a valid block contained within
     *         this form, then its properties will be returned if however the
     *         name is null or not valid, then a <b>null</b> object will be
     *         returned.
     * 
     * @see com.ottomobil.dsys.sprintframework.dataobjects.properties.interfaces.IFormProperties#getBlockProperties(java.lang.String)
     */
    public EJPluginBlockProperties getBlockProperties(String blockName)
    {
        
        Iterator<EJPluginBlockProperties> iti = _blockProperties.iterator();
        
        while (iti.hasNext())
        {
            EJPluginBlockProperties props = iti.next();
            
            if (props.getName().equalsIgnoreCase(blockName))
            {
                return props;
            }
        }
        return null;
    }
    
    /**
     * Used to return the whole list of blocks contained within this form. The
     * key of the <code>HashMap</code> is the name of the block <b>in upper
     * case</b>. The value will be a <code>IBlockProperties</code> object.
     * 
     * @return A <code>HashMap</code> containing this forms
     *         <code>Block Properties</code>
     * @see com.ottomobil.dsys.sprintframework.dataobjects.properties.interfaces.IFormProperties#getAllBlockProperties()
     */
    public List<EJPluginBlockProperties> getAllBlockProperties()
    {
        return _blockProperties;
    }
    
    public String getDefaultBlockName()
    {
        String blockName = "BLOCK_";
        
        for (int i = 10;; i += 10)
        {
            if (!contains(blockName + "_" + i))
            {
                return blockName + i;
            }
        }
    }
}
