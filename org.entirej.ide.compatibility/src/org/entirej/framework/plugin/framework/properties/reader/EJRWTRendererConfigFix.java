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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.entirej.framework.core.enumerations.EJRendererType;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public class EJRWTRendererConfigFix
{
    
    private List<Entry> mappingBlock = new ArrayList<Entry>();
    private List<Entry> mappingItem  = new ArrayList<Entry>();
    
    private static class Entry
    {
        String name;
        String renderer;
        String def;
        
        public Entry(String name, String renderer, String def)
        {
            super();
            this.name = name;
            this.renderer = renderer;
            this.def = def;
        }
        
    }
    
    public EJRWTRendererConfigFix()
    {
        //after 2.1 changes 
        // blocks
        mappingBlock.add(new Entry("HTMLMultiRecord", "org.entirej.applicationframework.rwt.renderers.html.EJRWTHtmlTableBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTHtmlTableBlockRendererDefinition"));
        
        mappingBlock.add(new Entry("LineChartRecord", "org.entirej.applicationframework.rwt.renderers.chart.EJRWTLineChartRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTLineChartRecordBlockDefinition"));
        mappingBlock.add(new Entry("PieChartRecord", "org.entirej.applicationframework.rwt.renderers.chart.EJRWTPieChartRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTPieChartRecordBlockDefinition"));
        mappingBlock.add(new Entry("BarChartRecord", "org.entirej.applicationframework.rwt.renderers.chart.EJRWTBarChartRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTBarChartRecordBlockDefinition"));
        mappingBlock.add(new Entry("RadarChartRecord", "org.entirej.applicationframework.rwt.renderers.chart.EJRWTRadarChartRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTRadarChartRecordBlockDefinition"));
        
        // items
        mappingItem.add(new Entry("ListItem", "org.entirej.applicationframework.rwt.renderers.item.EJRWTListItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTListBoxRendererDefinition"));
        // items
        mappingItem.add(new Entry("StackedItem", "org.entirej.applicationframework.rwt.renderers.item.EJRWTStackedItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTStackedItemRendererDefinition"));
    }
    
    
    String getBlockRenderGroup(String id)
    {
        
        if("SingleRecord".equals(id)||"MultiRecord".equals(id)||"TreeTableRecord".equals(id)||"HTMLMultiRecord".equals(id)||"TreeRecord".equals(id))
        {
            return "Standard Renderers";
        }
        if("LineChartRecord".equals(id)||"PieChartRecord".equals(id)||"BarChartRecord".equals(id)||"RadarChartRecord".equals(id))
        {
            return "Graph Renderers";
        }
       
        return "User Defined";
                
    }
    
    
    public boolean config(EJPluginEntireJProperties properties)
    {
        boolean confied = false;
        // check block
        Collection<EJPluginRenderer> blockRenderers = properties.getBlockRendererContainer().getAllRenderers();
        
        
        
        BLOCK_ENTRES: 
        for (Entry entry : mappingBlock)
        {
            for (EJPluginRenderer renderer : blockRenderers)
            {
                
                if (renderer.getRendererDefinitionClassName().equals(entry.def))
                {
                    
                    continue BLOCK_ENTRES;
                }
            }
            
            properties.getBlockRendererContainer().addRendererAssignment(
                    new EJPluginRenderer(properties, entry.name, EJRendererType.BLOCK, entry.def, entry.renderer));
            confied = true;
        }
        
        blockRenderers = properties.getBlockRendererContainer().getAllRenderers();
        for (EJPluginRenderer renderer : blockRenderers)
        {
            String group = getBlockRenderGroup(renderer.getAssignedName());
            if (!group.equals(renderer.getGroup()))
            {
                renderer.setGroup(group);
                confied = true;
            }
        }
        //check items
        Collection<EJPluginRenderer> itemRenderers = properties.getItemRendererContainer().getAllRenderers();
        
        ITEM_ENTRES: 
            for (Entry entry : mappingItem)
            {
                for (EJPluginRenderer renderer : itemRenderers)
                {
                    if (renderer.getRendererDefinitionClassName().equals(entry.def))
                    {
                        
                        continue ITEM_ENTRES;
                    }
                }
                
                properties.getItemRendererContainer().addRendererAssignment(
                        new EJPluginRenderer(properties, entry.name, EJRendererType.ITEM, entry.def, entry.renderer));
                confied = true;
            }
        
        if(confied)
        {
            properties.setVersion("2.7");
        }
        return confied;
    }
    
    public static boolean isRWTCF(EJPluginEntireJProperties properties)
    {
        
        return "org.entirej.applicationframework.rwt.renderers.application.EJRWTApplicationDefinition".equals(properties
                .getApplicationManagerDefinitionClassName()) && (properties.getVersion().equals("1.0") 
                        || properties.getVersion().equals("2.0") 
                        || properties.getVersion().equals("2.1")
                        ||  properties.getVersion().equals("2.2")
                        ||  properties.getVersion().equals("2.3")
                        ||  properties.getVersion().equals("2.4")
                        ||  properties.getVersion().equals("2.5")
                        ||  properties.getVersion().equals("2.5")
                        ||  properties.getVersion().equals("2.6")
                        
                        );
    }
    
}
