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
package org.entirej.framework.plugin.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.containers.interfaces.EJCanvasPropertiesContainer;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.properties.interfaces.EJStackedPageProperties;
import org.entirej.framework.core.properties.interfaces.EJTabPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginStackedPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginStackedPageCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginTabPageCanvasContainer;

public class EJPluginCanvasRetriever
{
    
    /**
     * Retrieves all <code>BLOCK</code> canvases that have not yet been assigned
     * a block
     * <p>
     * There can only be one block to a <code>BLOCK</code> canvas
     * 
     * @return A <code>Collection</code> of <code>BLOCK</code> canvases
     */
    public static Collection<EJCanvasProperties> retriveAllNonAssignedBlockCanvases(EJPluginFormProperties formProperties)
    {
        ArrayList<EJCanvasProperties> nonAssignedCanvasList = new ArrayList<EJCanvasProperties>();
        ArrayList<EJCanvasProperties> canvasList = new ArrayList<EJCanvasProperties>();
        
        addBlockCanvasesFromContainer(formProperties, formProperties.getCanvasContainer(), canvasList);
        
        for (EJCanvasProperties canvas : canvasList)
        {
            if (!hasBlockBeenAssignedToCanvas(formProperties, canvas.getName()))
            {
                nonAssignedCanvasList.add(canvas);
            }
        }
        
        return nonAssignedCanvasList;
    }
    
    public static Collection<EJCanvasProperties> retriveAllCanvases(EJPluginFormProperties formProperties)
    {
        ArrayList<EJCanvasProperties> canvasList = new ArrayList<EJCanvasProperties>();
        
        addCanvasesFromContainer(formProperties, formProperties.getCanvasContainer(), canvasList);
        
        return canvasList;
    }
    public static Collection<EJCanvasProperties> retriveAllCanvasesOnMainScreen(EJPluginFormProperties formProperties)
    {
        ArrayList<EJCanvasProperties> canvasList = new ArrayList<EJCanvasProperties>();
        
        Iterator<EJCanvasProperties> allCanvases = formProperties.getCanvasContainer().getAllCanvasProperties().iterator();
        while (allCanvases.hasNext())
        {
            EJCanvasProperties canvas = allCanvases.next();
            canvasList.add(canvas);
            if (canvas.getType() == EJCanvasType.POPUP)
            {
                continue;
            }
            else if (canvas.getType() == EJCanvasType.TAB)
            {
                Iterator<EJTabPageProperties> allTabPages = canvas.getTabPageContainer().getAllTabPageProperties().iterator();
                while (allTabPages.hasNext())
                {
                    addCanvasesFromContainer(formProperties, allTabPages.next().getContainedCanvases(), canvasList);
                }
            }
            else if (canvas.getType() == EJCanvasType.STACKED)
            {
                Iterator<EJStackedPageProperties> allStackedPages = canvas.getStackedPageContainer().getAllStackedPageProperties().iterator();
                while (allStackedPages.hasNext())
                {
                    addCanvasesFromContainer(formProperties, allStackedPages.next().getContainedCanvases(), canvasList);
                }
            }
            else if (canvas.getType() == EJCanvasType.GROUP)
            {
                addCanvasesFromContainer(formProperties, canvas.getGroupCanvasContainer(), canvasList);
            }
            else if (canvas.getType() == EJCanvasType.SPLIT)
            {
                addCanvasesFromContainer(formProperties, canvas.getSplitCanvasContainer(), canvasList);
            }
        }
        
        return canvasList;
    }
    
    public static Collection<EJCanvasProperties> retriveAllBlockCanvasesAssignedTabPage(EJPluginFormProperties formProperties, EJPluginTabPageProperties page)
    {
        ArrayList<EJCanvasProperties> bloclList = new ArrayList<EJCanvasProperties>();
        ArrayList<EJCanvasProperties> canvasList = new ArrayList<EJCanvasProperties>();
        EJPluginTabPageCanvasContainer containedCanvases = page.getContainedCanvases();
        addBlockCanvasesFromContainer(formProperties, containedCanvases, canvasList);
        
        for (EJCanvasProperties canvas : canvasList)
        {
            if (hasBlockBeenAssignedToCanvas(formProperties, canvas.getName()))
            {
                bloclList.add(canvas);
            }
        }
        
        return bloclList;
    }
    
    public static Collection<EJCanvasProperties> retriveAllBlockCanvasesAssignedStackedPage(EJPluginFormProperties formProperties,
            EJPluginStackedPageProperties page)
    {
        ArrayList<EJCanvasProperties> bloclList = new ArrayList<EJCanvasProperties>();
        ArrayList<EJCanvasProperties> canvasList = new ArrayList<EJCanvasProperties>();
        EJPluginStackedPageCanvasContainer containedCanvases = page.getContainedCanvases();
        addBlockCanvasesFromContainer(formProperties, containedCanvases, canvasList);
        
        for (EJCanvasProperties canvas : canvasList)
        {
            if (hasBlockBeenAssignedToCanvas(formProperties, canvas.getName()))
            {
                bloclList.add(canvas);
            }
        }
        
        return bloclList;
    }
    
    /**
     * Finds the canvas with the given name and returns its properties
     * <p>
     * The canvas could belong to another canvas. This method will loop through
     * all parent and child canvases to find required one. If no canvas is
     * found, <code>null</code> is returned
     * 
     * @param name
     *            The name of the required canvas
     * @return The properties of the required canvas or <code>null</code> if the
     *         canvas was not found
     */
    public static EJCanvasProperties getCanvasProperties(EJPluginFormProperties formProperties, String name)
    {
        Collection<EJCanvasProperties> canvasList =  retriveAllCanvases(formProperties);
        
        for (EJCanvasProperties canvas : canvasList)
        {
            if (canvas.getName().equals(name))
            {
                return canvas;
            }
        }
        return null;
    }
    
    /**
     * Checks to see if there is already a canvas existing with the given name
     * 
     * @param formProperties
     *            The form to check
     * @param name
     *            The name of the canvas to check for
     * @return <code>true</code> if the canvas exists, otherwise
     *         <code>false</code>
     */
    public static boolean canvasExists(EJPluginFormProperties formProperties, String name)
    {
        ArrayList<EJCanvasProperties> canvasList = new ArrayList<EJCanvasProperties>();
        addCanvasesFromContainer(formProperties, formProperties.getCanvasContainer(), canvasList);
        
        for (EJCanvasProperties canvas : canvasList)
        {
            if (canvas.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    
    private static void addBlockCanvasesFromContainer(EJPluginFormProperties formProperties, EJCanvasPropertiesContainer container,
            ArrayList<EJCanvasProperties> canvasList)
    {
        Iterator<EJCanvasProperties> allCanvases = container.getAllCanvasProperties().iterator();
        while (allCanvases.hasNext())
        {
            EJCanvasProperties canvas = allCanvases.next();
            
            if (canvas.getType() == EJCanvasType.BLOCK)
            {
                canvasList.add(canvas);
            }
            else if (canvas.getType() == EJCanvasType.POPUP)
            {
                addBlockCanvasesFromContainer(formProperties, canvas.getPopupCanvasContainer(), canvasList);
            }
            else if (canvas.getType() == EJCanvasType.TAB)
            {
                Iterator<EJTabPageProperties> allTabPages = canvas.getTabPageContainer().getAllTabPageProperties().iterator();
                while (allTabPages.hasNext())
                {
                    addBlockCanvasesFromContainer(formProperties, allTabPages.next().getContainedCanvases(), canvasList);
                }
            }
            else if (canvas.getType() == EJCanvasType.STACKED)
            {
                Iterator<EJStackedPageProperties> allStackedPages = canvas.getStackedPageContainer().getAllStackedPageProperties().iterator();
                while (allStackedPages.hasNext())
                {
                    addBlockCanvasesFromContainer(formProperties, allStackedPages.next().getContainedCanvases(), canvasList);
                }
            }
            else if (canvas.getType() == EJCanvasType.GROUP)
            {
                addBlockCanvasesFromContainer(formProperties, canvas.getGroupCanvasContainer(), canvasList);
            }
            else if (canvas.getType() == EJCanvasType.SPLIT)
            {
                addBlockCanvasesFromContainer(formProperties, canvas.getSplitCanvasContainer(), canvasList);
            }
        }
    }
    
    private static void addCanvasesFromContainer(EJPluginFormProperties formProperties, EJCanvasPropertiesContainer container,
            ArrayList<EJCanvasProperties> canvasList)
    {
        Iterator<EJCanvasProperties> allCanvases = container.getAllCanvasProperties().iterator();
        while (allCanvases.hasNext())
        {
            EJCanvasProperties canvas = allCanvases.next();
            canvasList.add(canvas);
            if (canvas.getType() == EJCanvasType.POPUP)
            {
                addCanvasesFromContainer(formProperties, canvas.getPopupCanvasContainer(), canvasList);
            }
            else if (canvas.getType() == EJCanvasType.TAB)
            {
                Iterator<EJTabPageProperties> allTabPages = canvas.getTabPageContainer().getAllTabPageProperties().iterator();
                while (allTabPages.hasNext())
                {
                    addCanvasesFromContainer(formProperties, allTabPages.next().getContainedCanvases(), canvasList);
                }
            }
            else if (canvas.getType() == EJCanvasType.STACKED)
            {
                Iterator<EJStackedPageProperties> allStackedPages = canvas.getStackedPageContainer().getAllStackedPageProperties().iterator();
                while (allStackedPages.hasNext())
                {
                    addCanvasesFromContainer(formProperties, allStackedPages.next().getContainedCanvases(), canvasList);
                }
            }
            else if (canvas.getType() == EJCanvasType.GROUP)
            {
                addCanvasesFromContainer(formProperties, canvas.getGroupCanvasContainer(), canvasList);
            }
            else if (canvas.getType() == EJCanvasType.SPLIT)
            {
                addCanvasesFromContainer(formProperties, canvas.getSplitCanvasContainer(), canvasList);
            }
        }
    }
    
    private static boolean hasBlockBeenAssignedToCanvas(EJPluginFormProperties formProperties, String canvasName)
    {
        Iterator<EJPluginBlockProperties> allBlocks = formProperties.getBlockContainer().getAllBlockProperties().iterator();
        while (allBlocks.hasNext())
        {
            EJPluginBlockProperties block = allBlocks.next();
            if (block.getCanvasName() != null && block.getCanvasName().equals(canvasName))
            {
                return true;
            }
        }
        return false;
    }
    
}
