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

import java.util.Iterator;

import org.entirej.framework.core.enumerations.EJCanvasSplitOrientation;
import org.entirej.framework.core.enumerations.EJCanvasTabPosition;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.properties.interfaces.EJStackedPageProperties;
import org.entirej.framework.core.properties.interfaces.EJTabPageProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasGroupPageContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasSplitPageContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginPopupCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginStackedPageContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginTabPageContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;


public class EJPluginCanvasProperties implements EJCanvasProperties, EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long serialVersionUID = 2469290104959744115L;

    // This is the container to which this canvas property object belongs. This
    // is required by the item mover to know where to remove the canvas from and
    // where to put it
    private EJPluginCanvasContainer          _parentContainer;
    
    private EJPluginFormProperties           _formProperties;
    
    private EJCanvasType                     _type                        = EJCanvasType.BLOCK;
    
    private int                              _width                       = 0;
    private int                              _height                      = 0;
    private int                              _numCols                     = 1;
    private int                              _verticalSpan                = 1;
    private int                              _horizontalSpan              = 1;
    private boolean                          _expandHorizontally          = true;
    private boolean                          _expandVertically            = true;
    private boolean                          _displayGroupFrame           = false;
    private String                           _name                        = "";
    private String                           _popupPageTitle              = "";
    private String                           _groupFrameTitle             = "";
    private String                           _buttonOneText               = "Not Assigned";
    private String                           _buttonTwoText               = "";
    private String                           _buttonThreeText             = "";
    private String                           _firstInitialStackedPageName = "";

    private String                           _referencedObjectGroupName    = "";
    
    private boolean                          _objectGroupRoot;
    
    private EJPluginTabPageContainer         _tabPageContainer;
    private EJPluginStackedPageContainer     _stackedPageContainer;
    private EJPluginPopupCanvasContainer     _popupCanvasContainer;
    private EJPluginCanvasGroupPageContainer _canvasGroupContainer;
    private EJPluginCanvasSplitPageContainer _canvasSplitContainer;
    
    private String                               _referredFormId;
    //
    // If the Canvas type is TAB, then the following properties are also
    // available
    private EJCanvasTabPosition              _tabPosition                 = EJCanvasTabPosition.TOP;
    private EJCanvasSplitOrientation         _splitOrientation            = EJCanvasSplitOrientation.HORIZONTAL;
    
    public EJPluginCanvasProperties(EJPluginFormProperties formProperties, String name)
    {
        _name = name;
        _formProperties = formProperties;
        _tabPageContainer = new EJPluginTabPageContainer(this);
        _stackedPageContainer = new EJPluginStackedPageContainer(this);
        _popupCanvasContainer = new EJPluginPopupCanvasContainer(formProperties, this);
        _canvasGroupContainer = new EJPluginCanvasGroupPageContainer(formProperties, this);
        _canvasSplitContainer = new EJPluginCanvasSplitPageContainer(formProperties, this);
    }
    
    public void dispose()
    {
        _tabPageContainer.clear();
        _stackedPageContainer.clear();
    }
    
    /**
     * Returns the canvas container to which this canvas belongs
     * 
     * @return This canvas's container
     */
    public EJPluginCanvasContainer getParentCanvasContainer()
    {
        return _parentContainer;
    }
    
    /**
     * Sets the container where this canvas belongs
     * 
     * @param container
     *            This canvas's container
     */
    public void setParentCanvasContainer(EJPluginCanvasContainer container)
    {
        _parentContainer = container;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    /**
     * The popup canvas can have a minimum of one button and a maximum of three
     * buttons. Button one is always the default and will always be displayed.
     * <p>
     * Button two and button three will only be displayed if a button text has
     * been assigned
     * 
     * @return Returns the text for button one
     */
    public String getButtonOneText()
    {
        return _buttonOneText;
    }
    
    /**
     * The popup canvas can have a minimum of one button and a maximum of three
     * buttons. Button one is always the default and will always be displayed.
     * <p>
     * Button two and button three will only be displayed if a button text has
     * been assigned
     * 
     * @param buttonText
     *            The text to display on button one
     */
    public void setButtonOneText(String buttonText)
    {
        _buttonOneText = buttonText;
    }
    
    /**
     * The popup canvas can have a minimum of one button and a maximum of three
     * buttons. Button one is always the default and will always be displayed.
     * <p>
     * Button two and button three will only be displayed if a button text has
     * been assigned
     * 
     * @return Returns the text for button two
     */
    public String getButtonTwoText()
    {
        return _buttonTwoText;
    }
    
    /**
     * The popup canvas can have a minimum of one button and a maximum of three
     * buttons. Button one is always the default and will always be displayed.
     * <p>
     * Button two and button three will only be displayed if a button text has
     * been assigned
     * 
     * @param buttonText
     *            The text to display on button two or <code>null</code> if the
     *            button is not to be displayed
     */
    public void setButtonTwoText(String buttonText)
    {
        _buttonTwoText = buttonText;
    }
    
    /**
     * The popup canvas can have a minimum of one button and a maximum of three
     * buttons. Button one is always the default and will always be displayed.
     * <p>
     * Button two and button three will only be displayed if a button text has
     * been assigned
     * 
     * @return Returns the text for button three
     */
    public String getButtonThreeText()
    {
        return _buttonThreeText;
    }
    
    /**
     * The popup canvas can have a minimum of one button and a maximum of three
     * buttons. Button one is always the default and will always be displayed.
     * <p>
     * Button two and button three will only be displayed if a button text has
     * been assigned
     * 
     * @param buttonText
     *            The text to display on button three or <code>null</code> if
     *            the button is not to be displayed
     */
    public void setButtonThreeText(String buttonText)
    {
        _buttonThreeText = buttonText;
    }
    
    /**
     * @return the canvasContainer
     */
    public EJPluginTabPageContainer getTabPageContainer()
    {
        return _tabPageContainer;
    }
    
    /**
     * @return the canvasContainer
     */
    public EJPluginStackedPageContainer getStackedPageContainer()
    {
        return _stackedPageContainer;
    }
    
    /**
     * If this canvas is a Stacked canvas then the canvas will have various
     * stacked pages, this method will return a specific stacked page properties
     * for the name specified
     * <p>
     * 
     * @param name
     *            The name of the required stacked page
     * @return The <code>{@link EJPluginStackedPageProperties}</code> of the
     *         required stacked page or null if there is no stacked page with
     *         the given name
     */
    public EJStackedPageProperties getStackedPageProperties(String name)
    {
        if (name == null)
        {
            return null;
        }
        
        return _stackedPageContainer.getStackedPageProperties(name);
    }
    
    public EJPluginPopupCanvasContainer getPopupCanvasContainer()
    {
        return _popupCanvasContainer;
    }
    
    /**
     * If this canvas is a Popup canvas then it will contain one or more
     * canvases. this method will return a specific stacked page properties for
     * the name specified
     * <p>
     * 
     * @param name
     *            The name of the required popup canvas
     * @return The <code>{@link EJPluginCanvasProperties}</code> of the required
     *         canvas or null if there is no canvas with the given name
     */
    public EJCanvasProperties getPopupCanvasProperties(String name)
    {
        if (name == null)
        {
            return null;
        }
        
        return _popupCanvasContainer.getCanvasProperties(name);
    }
    
    public EJPluginCanvasGroupPageContainer getGroupCanvasContainer()
    {
        return _canvasGroupContainer;
    }
    
    @Override
    public EJPluginCanvasSplitPageContainer getSplitCanvasContainer()
    {
        return _canvasSplitContainer;
    }
    
    /**
     * If this canvas is a Group Canvas then it will contain one or more
     * canvases. this method will return a specific canvas properties for the
     * canvas name specified
     * <p>
     * 
     * @param canvasName
     *            The canvas name
     * @return The <code>{@link EJPluginCanvasProperties}</code> of the required
     *         canvas or <code>null</code> if there is no canvas with the given
     *         name
     */
    public EJCanvasProperties getGroupCanvasProperties(String canvasName)
    {
        if (canvasName == null)
        {
            return null;
        }
        
        return _canvasGroupContainer.getCanvasProperties(canvasName);
    }
    
    @Override
    public EJCanvasProperties getSplitCanvasProperties(String canvasName)
    {
        if (canvasName == null)
        {
            return null;
        }
        
        return _canvasSplitContainer.getCanvasProperties(canvasName);
    }
    
    /**
     * If the canvas has been set as Horizontally Expandable, then the canvas
     * must be expanded if the user stretches the form. This is however
     * dependent upon the actually display renderers used for the application
     * 
     * @return Returns the expand horizontally flag
     */
    public boolean canExpandHorizontally()
    {
        return _expandHorizontally;
    }
    
    /**
     * @param expand
     *            Sets the horizontally expandable flag for this canvas
     */
    public void setExpandHorizontally(boolean expand)
    {
        _expandHorizontally = expand;
    }
    
    /**
     * If the canvas has been set as Vertically Expandable, then the canvas must
     * be expanded if the user stretches the form. This is however dependent
     * upon the actually display renderers used for the application
     */
    public boolean canExpandVertically()
    {
        return _expandVertically;
    }
    
    /**
     * @param expand
     *            The vertically expandable flag for this canvas
     */
    public void setExpandVertically(boolean expand)
    {
        _expandVertically = expand;
    }
    
    /**
     * @return Returns the height of this canvas
     */
    public int getHeight()
    {
        return _height;
    }
    
    public void setHeight(int height)
    {
        _height = height;
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
     * The horizontal span of this canvas
     * <p>
     * All canvases will be displayed within a grid. The horizontal span
     * indicates how many columns in the grid this canvas will span
     * 
     * @return Returns the horizontalSpan for this canvas
     * @see EJPluginCanvasProperties#getVerticalSpan()
     */
    public int getHorizontalSpan()
    {
        return _horizontalSpan;
    }
    
    /**
     * @param horizontalSpan
     *            The horizontalSpan of this block
     * @see EJPluginCanvasProperties#getHorizontalSpan()
     * @see EJPluginCanvasProperties#getVerticalSpan()
     */
    public void setHorizontalSpan(int horizontalSpan)
    {
        _horizontalSpan = horizontalSpan;
    }
    
    /**
     * The vertical span of this canvas
     * <p>
     * All canvases will be displayed within a grid. The vertical span indicates
     * how many rows in the grid this canvas will span
     * 
     * @return Returns the verticalSpan for this canvas
     * @see EJPluginCanvasProperties#getHorizontalSpan()
     */
    public int getVerticalSpan()
    {
        return _verticalSpan;
    }
    
    /**
     * @param verticalSpan
     *            The verticalSpan of this canvas
     */
    public void setVerticalSpan(int verticalSpan)
    {
        _verticalSpan = verticalSpan;
    }
    
    /**
     * @return Returns the type of this canvas.
     * @see EJPluginCanvasProperties#setType(CanvasType)
     */
    public EJCanvasType getType()
    {
        return _type;
    }
    
    /**
     * Sets the type of this canvas
     * 
     * @param type
     *            The type to set
     */
    public void setType(EJCanvasType type)
    {
        _type = type;
    }
    
    /**
     * Sets the tab position for this canvas
     * 
     * @param position
     *            the position of the tab pages
     */
    public void setTabPosition(EJCanvasTabPosition position)
    {
        _tabPosition = position;
    }
    
    public void setSplitOrientation(EJCanvasSplitOrientation position)
    {
        _splitOrientation = position;
    }
    
    /**
     * Sets the name of the initially displayed stacked page
     * 
     * @param pageName
     *            The name of the initially displayed stacked page
     */
    public void setInitalStackedPageName(String pageName)
    {
        _firstInitialStackedPageName = pageName;
    }
    
    /**
     * Sets the name of the initially displayed page of a stacked canvas
     * 
     * @return The name of the initially displayed stacked page
     */
    public String getInitialStackedPageName()
    {
        return _firstInitialStackedPageName;
    }
    
    /**
     * If this canvas is a Tab then the canvas will have various tab pages, this
     * method will return a specific tab page properties for the name specified
     * <p>
     * 
     * @param name
     *            The name of the required tab
     * @return The <code>TabPageProperties</code> of the required tab page or
     *         null if there is no tab page with the given name
     */
    public EJTabPageProperties getTabPageProperties(String name)
    {
        if (name == null)
        {
            return null;
        }
        
        return _tabPageContainer.getTabPageProperties(name);
    }
    
    /**
     * Returns the position of the tab
     * 
     * @return The tab position
     * @see EJPluginCanvasProperties#setTabPosition(CanvasTabPosition)
     */
    public EJCanvasTabPosition getTabPosition()
    {
        return _tabPosition;
    }
    
    @Override
    public EJCanvasSplitOrientation getSplitOrientation()
    {
        return _splitOrientation;
    }
    
    /**
     * @return Returns the width of this canvas
     */
    public int getWidth()
    {
        return _width;
    }
    
    /**
     * @param width
     *            The width of this canvas
     */
    public void setWidth(int width)
    {
        _width = width;
    }
    
    /**
     * @return Returns the page title for this popup canvas
     */
    public String getPopupPageTitle()
    {
        return _popupPageTitle;
    }
    
    /**
     * Sets the frame title of this popup canvas
     * 
     * @param name
     *            The frame title of this popup canvas
     */
    public void setPopupPageTitle(String title)
    {
        _popupPageTitle = title;
    }
    
    /**
     * @return Returns the frame title of the Group Canvas
     */
    public String getGroupFrameTitle()
    {
        return _groupFrameTitle;
    }
    
    /**
     * Sets the group frame title of this canvas
     * 
     * @param name
     *            The group frame title of this canvas
     */
    public void setGroupFrameTitle(String title)
    {
        _groupFrameTitle = title;
    }
    
    /**
     * @return Returns the display group frame flag of this canvas
     */
    public Boolean getDisplayGroupFrame()
    {
        return _displayGroupFrame;
    }
    
    /**
     * Sets the display group frame flag of this canvas
     * 
     * @param name
     *            The display group frame flag of this canvas
     */
    public void setDisplayGroupFrame(Boolean display)
    {
        _displayGroupFrame = display;
    }
    
    /**
     * @return Returns the name of this canvas
     */
    public String getName()
    {
        return _name;
    }
    
    public void internalSetName(String name)
    {
        _name = name;
    }
    
    // ////////////////////////////////////////////////////////////////
    /**
     * <b>Only used from within the running framework</b>
     */
    public String getContentCanvasName()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * <b>Only used from within the running framework</b>
     */
    public String getContentCanvasStackedPageName()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * <b>Only used from within the running framework</b>
     */
    public String getContentCanvasTabPageName()
    {
        throw new UnsupportedOperationException();
    }
    
    /**
     * <b>Only used from within the running framework</b>
     */
    public EJBlockProperties getBlockProperties()
    {
        return getPluginBlockProperties();
    }
    
    public EJPluginBlockProperties getPluginBlockProperties()
    {
        Iterator<EJPluginBlockProperties> allBlocks = _formProperties.getBlockContainer().getAllBlockProperties().iterator();
        while (allBlocks.hasNext())
        {
            EJPluginBlockProperties block = allBlocks.next();
            if (block.getCanvasName() != null && block.getCanvasName().equals(_name))
            {
                return block;
            }
        }
        return null;
    }
    
    
    
    public String getReferencedObjectGroupName()
    {
        return _referencedObjectGroupName;
    }
    
    public void setReferencedObjectGroupName(String name)
    {
        _referencedObjectGroupName = name;
    }
    
    public boolean isImportFromObjectGroup()
    {
        return _referencedObjectGroupName!=null && _referencedObjectGroupName.trim().length()>0;
    }
    
    public boolean isObjectGroupRoot()
    {
        return _objectGroupRoot;
    }
    
    public void setObjectGroupRoot(boolean _objectGroupRoot)
    {
        this._objectGroupRoot = _objectGroupRoot;
    }
    
    @Override
    public String getReferredFormId()
    {
        return _referredFormId;
    }

    public void setReferredFormId(String referredFormId)
    {
        this._referredFormId = referredFormId;
    }
}
