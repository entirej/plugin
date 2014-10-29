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
package org.entirej.framework.plugin.reports.containers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;

public class EJReportBlockContainer
{
    private List<BlockContainerItem> _blockProperties;
    private EJPluginReportProperties _reportProperties;
    
    private  BlockGroup         headerSection = new BlockGroup("Header Section");
    private  BlockGroup         footerSection = new BlockGroup("Footer Section");
    
    public EJReportBlockContainer(EJPluginReportProperties reportProperties)
    {
        _reportProperties = reportProperties;
        _blockProperties = new ArrayList<BlockContainerItem>();
    }
    
    public EJPluginReportProperties getReportProperties()
    {
        return _reportProperties;
    }
    
    public boolean isEmpty()
    {
        return _blockProperties.isEmpty();
        
    }
    
    
    public void setHeaderSection(BlockGroup headerSection)
    {
        this.headerSection = headerSection;
        headerSection.setName("Header Section");
    }
    
    public BlockGroup getHeaderSection()
    {
        return headerSection;
    }
    
    
    public void setFooterSection(BlockGroup footerSection)
    {
        this.footerSection = footerSection;
        footerSection.setName("Footer Section");
    }
    
    public BlockGroup getFooterSection()
    {
        return footerSection;
    }
    
    
    public boolean contains(String blockName)
    {
        Iterator<BlockContainerItem> iti = _blockProperties.iterator();
        while (iti.hasNext())
        {
            
            BlockContainerItem containerItem = iti.next();
            if ((containerItem instanceof BlockGroup))
            {
                BlockGroup blockGroup = (BlockGroup) containerItem;
                EJPluginReportBlockProperties blockProperties = blockGroup.getBlockProperties(blockName);
                if (blockProperties != null)
                {
                    return true;
                }
                continue;
            }
            EJPluginReportBlockProperties props = (EJPluginReportBlockProperties) containerItem;
            if (props.getName().equalsIgnoreCase(blockName))
            {
                return true;
            }
            EJPluginReportBlockProperties blockProperties = props.getLayoutScreenProperties().getSubBlocks().getBlockProperties(blockName);
            if (blockProperties != null)
            {
                return true;
            }
        }
        
        EJPluginReportBlockProperties blockProperties = headerSection.getBlockProperties(blockName);
        if (blockProperties != null)
        {
            return true;
        }
         blockProperties = footerSection.getBlockProperties(blockName);
        if (blockProperties != null)
        {
            return true;
        }
        
        return false;
    }
    
    public void addBlockProperties(BlockContainerItem blockProperties)
    {
        if (blockProperties != null)
        {
            _blockProperties.add(blockProperties);
        }
    }
    
    public void removeBlockContainerItem(BlockContainerItem blockProperties)
    {
        if (blockProperties != null)
        {
            _blockProperties.remove(blockProperties);
        }
    }
    
    public void replaceBlockProperties(EJPluginReportBlockProperties oldProp, EJPluginReportBlockProperties newProp)
    {
        if (oldProp != null && newProp != null)
        {
            
            BlockGroup blockGroupByBlock = getBlockGroupByBlock(oldProp);
            if (blockGroupByBlock == null)
            {
                int indexOf = _blockProperties.indexOf(oldProp);
                if (indexOf > -1)
                {
                    _blockProperties.set(indexOf, newProp);
                }
                else
                {
                    _blockProperties.add(newProp);
                }
            }
            else
            {
                blockGroupByBlock.replaceBlockProperties(oldProp, newProp);
            }
        }
    }
    
    public void addBlockProperties(int index, BlockContainerItem blockProperties)
    {
        if (blockProperties != null)
        {
            _blockProperties.add(index, blockProperties);
        }
    }
    
    public void removeBlockProperties(EJPluginReportBlockProperties props, boolean cleanup)
    {
        
        if (cleanup && contains(props.getName()))
        {
            
            // FIXME
            
        }
        
        BlockGroup blockGroup = getBlockGroupByBlock(props);
        if (blockGroup == null)
        {
            _blockProperties.remove(props);
        }
        else
        {
            blockGroup.removeBlockProperties(props);
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
    public EJPluginReportBlockProperties getBlockProperties(String blockName)
    {
        
        Iterator<BlockContainerItem> iti = _blockProperties.iterator();
        
        while (iti.hasNext())
        {
            
            BlockContainerItem containerItem = iti.next();
            if ((containerItem instanceof BlockGroup))
            {
                EJPluginReportBlockProperties blockProperties = ((BlockGroup) containerItem).getBlockProperties(blockName);
                if (blockProperties != null)
                {
                    return blockProperties;
                }
                continue;
            }
            EJPluginReportBlockProperties props = (EJPluginReportBlockProperties) containerItem;
            
            if (props.getName().equalsIgnoreCase(blockName))
            {
                return props;
            }
            
            EJPluginReportBlockProperties blockProperties = props.getLayoutScreenProperties().getSubBlocks().getBlockProperties(blockName);
            if (blockProperties != null)
            {
                return blockProperties;
            }
        }
        return null;
    }
    
    public BlockGroup getBlockGroupByBlock(EJPluginReportBlockProperties blockProperties)
    {
        
        Iterator<BlockContainerItem> iti = _blockProperties.iterator();
        
        while (iti.hasNext())
        {
            
            BlockContainerItem containerItem = iti.next();
            if ((containerItem instanceof BlockGroup))
            {
                BlockGroup blockGroup = (BlockGroup) containerItem;
                
                BlockGroup blockGroupByBlock = blockGroup.getBlockGroupByBlock(blockProperties);
                if (blockGroupByBlock != null)
                {
                    return blockGroupByBlock;
                }
                continue;
            }
            EJPluginReportBlockProperties props = (EJPluginReportBlockProperties) containerItem;
            
            if (props.equals(blockProperties))
            {
                return null;
            }
            
            BlockGroup blockGroup = props.getLayoutScreenProperties().getSubBlocks().getBlockGroupByBlock(blockProperties);
            if (blockGroup != null)
            {
                return blockGroup;
            }
        }
        
        

        BlockGroup blockGroupByBlock = headerSection.getBlockGroupByBlock(blockProperties);
        if (blockGroupByBlock != null)
        {
            return blockGroupByBlock;
        }
        blockGroupByBlock = footerSection.getBlockGroupByBlock(blockProperties);
        if (blockGroupByBlock != null)
        {
            return blockGroupByBlock;
        }
        return null;
    }
    
    public List<EJPluginReportBlockProperties> getAllBlockProperties()
    {
        List<EJPluginReportBlockProperties> list = new ArrayList<EJPluginReportBlockProperties>();
        
        Iterator<BlockContainerItem> iti = _blockProperties.iterator();
        while (iti.hasNext())
        {
            
            BlockContainerItem containerItem = iti.next();
            if ((containerItem instanceof BlockGroup))
            {
                list.addAll(((BlockGroup) containerItem).getAllBlockProperties());
                continue;
            }
            EJPluginReportBlockProperties props = (EJPluginReportBlockProperties) containerItem;
            list.add(props);
        }
        
        return list;
    }
    
    public List<BlockContainerItem> getBlockContainerItems()
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
    
    public static interface BlockContainerItem
    {
        // marker interface
    }
    
    public static class BlockGroup implements BlockContainerItem
    {
        
        public BlockGroup()
        {
        }
        
        public BlockGroup getBlockGroupByBlock(EJPluginReportBlockProperties blockProperties)
        {
            for (EJPluginReportBlockProperties properties : _blockProperties)
            {
                if (blockProperties.equals(properties))
                {
                    return this;
                }
                
                BlockGroup blockGroup = blockProperties.getLayoutScreenProperties().getSubBlocks().getBlockGroupByBlock(blockProperties);
                if (blockGroup != null)
                {
                    return blockGroup;
                }
            }
            return null;
        }
        
        public BlockGroup(String name)
        {
            this.name = name;
        }
        
        private String name;
        
        public void setName(String name)
        {
            this.name = name;
        }
        
        public String getName()
        {
            return name;
        }
        
        private List<EJPluginReportBlockProperties> _blockProperties = new ArrayList<EJPluginReportBlockProperties>();
        
        public boolean isEmpty()
        {
            return _blockProperties.isEmpty();
            
        }
        
        public boolean contains(String blockName)
        {
            Iterator<EJPluginReportBlockProperties> iti = _blockProperties.iterator();
            while (iti.hasNext())
            {
                EJPluginReportBlockProperties props = iti.next();
                if (props.getName().equalsIgnoreCase(blockName))
                {
                    return true;
                }
            }
            return false;
        }
        
        public void addBlockProperties(EJPluginReportBlockProperties blockProperties)
        {
            if (blockProperties != null)
            {
                _blockProperties.add(blockProperties);
            }
        }
        
        public void replaceBlockProperties(EJPluginReportBlockProperties oldProp, EJPluginReportBlockProperties newProp)
        {
            if (oldProp != null && newProp != null)
            {
                int indexOf = _blockProperties.indexOf(oldProp);
                if (indexOf > -1)
                {
                    _blockProperties.set(indexOf, newProp);
                }
                else
                {
                    _blockProperties.add(newProp);
                }
            }
        }
        
        public void addBlockProperties(int index, EJPluginReportBlockProperties blockProperties)
        {
            if (blockProperties != null)
            {
                _blockProperties.add(index, blockProperties);
            }
        }
        
        public void removeBlockProperties(EJPluginReportBlockProperties props)
        {
            
            if (_blockProperties.contains(props))
            {
                
                _blockProperties.remove(props);
                
            }
            
        }
        
        public EJPluginReportBlockProperties getBlockProperties(String blockName)
        {
            
            Iterator<EJPluginReportBlockProperties> iti = _blockProperties.iterator();
            
            while (iti.hasNext())
            {
                EJPluginReportBlockProperties props = iti.next();
                
                if (props.getName().equalsIgnoreCase(blockName))
                {
                    return props;
                }
                EJPluginReportBlockProperties blockProperties = props.getLayoutScreenProperties().getSubBlocks().getBlockProperties(blockName);
                if (blockProperties != null)
                {
                    return blockProperties;
                }
            }
            return null;
        }
        
        public List<EJPluginReportBlockProperties> getAllBlockProperties()
        {
            return _blockProperties;
        }
    }
    
}
