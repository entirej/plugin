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
package org.entirej.framework.plugin.framework.properties;

import org.entirej.framework.core.properties.interfaces.EJDrawerPageProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginDrawerPageCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginTabPageCanvasContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;

public class EJPluginDrawerPageProperties implements EJDrawerPageProperties, EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long serialVersionUID = -4534192189486279699L;
    private EJPluginCanvasProperties       _canvasProperties;
    private String                         _name;
    private String                         _pageTitle;
    private int                            _numCols = 1;
    private int                            _wdith = 200;
    private String                         _firstNavigationalBlock;
    private String                         _firstNavigationalItem;
    private boolean                        _enabled = true;
    private boolean                        _visible = true;
    
    private EJPluginDrawerPageCanvasContainer _containedCanvases;
    
    public EJPluginDrawerPageProperties(EJPluginCanvasProperties tabCanvas, String name)
    {
        _name = name;
        _canvasProperties = tabCanvas;
        _containedCanvases = new EJPluginDrawerPageCanvasContainer(tabCanvas.getFormProperties(), this);
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _canvasProperties.getFormProperties();
    }
    
    public EJPluginCanvasProperties getDrawerCanvasProperties()
    {
        return _canvasProperties;
    }
    
    /**
     * If the tab page is not visible then it will need to be made visible
     * before the user can navigate to it
     * <p>
     * 
     * @return Returns the enabled.
     * @see EJPluginDrawerPageProperties#isEnabled()
     */
    public boolean isVisible()
    {
        return _visible;
    }
    
    /**
     * If the tab page is not visible then it will need to be made visible
     * before the user can navigate to it
     * <p>
     * 
     * @param visible
     *            The flag to set
     */
    public void setVisible(boolean visible)
    {
        _visible = visible;
    }
    
    /**
     * If the tab page is not enabled, the user can still see it, but not
     * navigate to it
     * <p>
     * 
     * @return Returns the enabled flag
     * @see EJPluginDrawerPageProperties#isVisible()
     */
    public boolean isEnabled()
    {
        return _enabled;
    }
    
    /**
     * If the tab page is not enabled, the user can still see it, but not
     * navigate to it
     * <p>
     * 
     * @param enabled
     *            The enabled flag
     * @see EJPluginDrawerPageProperties#setVisible(boolean)
     */
    public void setEnabled(boolean enabled)
    {
        _enabled = enabled;
    }
    
    /**
     * The first navigational block is the block to which navigation will be
     * passed when the user clicks on this tab page
     * <p>
     * this property <b>must</b> be used in conjunction with
     * <code>FirstNavigationalItem</code> otherwise it will have no effect and
     * the focus will remain on the last block or the tab itself
     * 
     * @return Returns the firstNavigationalBlock
     * @see EJPluginDrawerPageProperties#getFirstNavigationalItem()
     */
    public String getFirstNavigationalBlock()
    {
        return _firstNavigationalBlock;
    }
    
    /**
     * The first navigational block is the block to which navigation will be
     * passed when the user clicks on this tab page
     * <p>
     * this property <b>must</b> be used in conjunction with
     * <code>FirstNavigationalItem</code> otherwise it will have no effect and
     * the focus will remain on the last block or the tab itself
     * 
     * @param firstNavigationalBlock
     *            The firstNavigationalBlock to set.
     * @throws NullPointerException
     *             if the block name passed is null or of zero length
     */
    public void setFirstNavigationalBlock(String firstNavigationalBlock)
    {
        _firstNavigationalBlock = firstNavigationalBlock.trim();
    }
    
    /**
     * Indicates how many display columns this page will have
     * <p>
     * All canvases being added to the tab page will be inserted into a grid.
     * The grid will have any number of rows but will be limited to the amount
     * of columns as set by this parameter.
     * <p>
     * Canvases added to this page can span multiple columns and rows
     * 
     * @return The number of columns defined for this page
     */
    public int getNumCols()
    {
        return _numCols;
    }
    
    /**
     * Sets the number of columns that this page will have
     * <p>
     * All canvases being added to the tab page will be inserted into a grid.
     * The grid will have any number of rows but will be limited to the amount
     * of columns as set by this parameter.
     * 
     * @param numCols
     *            The number of columns to set
     */
    public void setNumCols(int numCols)
    {
        _numCols = numCols;
    }
    
    /**
     * The first navigational item is the item to which navigation will be
     * passed when the user clicks on this tab page
     * <p>
     * this property <b>must</b> be used in conjunction with
     * <code>FirstNavigationalBlock</code> otherwise it will have no effect and
     * the focus will remain on the last block or the tab itself
     * 
     * @return Returns the firstNavigationalItem
     */
    public String getFirstNavigationalItem()
    {
        return _firstNavigationalItem;
    }
    
    /**
     * The first navigational item is the item to which navigation will be
     * passed when the user clicks on this tab page
     * <p>
     * this property <b>must</b> be used in conjunction with
     * <code>FirstNavigationalBlock</code> otherwise it will have no effect and
     * the focus will remain on the last block or the tab itself
     * 
     * @param firstNavigationalItem
     *            The name of the firstNavigationalItem
     * @throws NullPointerException
     *             if the item name passed is null or of zero length
     * 
     */
    public void setFirstNavigationalItem(String firstNavigationalItem)
    {
        _firstNavigationalItem = firstNavigationalItem.trim();
    }
    
    /**
     * @return Returns the name of this tab page
     */
    public String getName()
    {
        return _name;
    }
    
    public void internalSetName(String name)
    {
        _name = name;
    }
    
    /**
     * The page title is the text that will apear in the tab itself
     * <p>
     * The page title is the translated version of the page title code if it was
     * translated otherwise the page title code will be returned
     * 
     * @return Returns the pageTitle of this tab page
     */
    public String getPageTitle()
    {
        return _pageTitle;
    }
    
    /**
     * Sets the translated version of the page title code
     * 
     * @param pageTitle
     *            The pageTitle to set
     */
    public void setPageTitle(String pageTitle)
    {
        _pageTitle = pageTitle;
    }
    
    public EJPluginDrawerPageCanvasContainer getContainedCanvases()
    {
        return _containedCanvases;
    }
    
    public void addContainedCanvas(EJPluginCanvasProperties canvas)
    {
        if (canvas != null)
        {
            _containedCanvases.addCanvasProperties(canvas);
        }
    }
    
    @Override
    public int getDrawerWidth()
    {
        return _wdith;
    }
    public void setDrawerWidth(int width)
    {
         _wdith = width;
    }
    
}
