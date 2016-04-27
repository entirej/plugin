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
/*
 * Created on Nov 5, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.containers.interfaces.EJCanvasPropertiesContainer;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.properties.interfaces.EJFormProperties;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.core.properties.interfaces.EJStackedPageProperties;
import org.entirej.framework.core.properties.interfaces.EJTabPageProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevFormRendererDefinition;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginObjectGroupContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginRelationContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;

public class EJPluginFormProperties implements EJFormProperties, Comparable<EJPluginFormProperties>, EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long                  serialVersionUID          = -8063612191054623340L;
    private boolean                            _isReusableLovForm        = false;
    private boolean                            _isReusableBlockForm      = false;
    private boolean                            _isObjectGroupForm      = false;
    private IJavaProject                       _formsProject;
    
    private String                             _name                     = "";
    private String                             _formRendererName         = "";
    private EJFrameworkExtensionProperties     _formRendererProperties;
    private String                             _formTitle                = "";
    private String                             _formDisplayName          = "";
    
    private String                             _actionProcessorClassName = "";
    
    private List<EJPluginApplicationParameter> _formParameters;
    private HashMap<String, String>            _applicationProperties;
    
    private String                                 _firstNavigableBlock      = "";
    
    // Display Properties
    private int                                _formWidth;
    private int                                _formHeight;
    private int                                _numCols;
    
    // Containers for the forms underlying objects
    private EJPluginBlockContainer             _blockContainer;
    private EJPluginObjectGroupContainer       _objGroupContainer;
    private EJPluginCanvasContainer            _canvasContainer;
    private EJPluginRelationContainer          _relationContainer;
    private EJPluginLovDefinitionContainer     _lovDefinitionContainer;
    
    public EJPluginFormProperties(String formName, IJavaProject javaProject)
    {
        _name = formName;
        _formsProject = javaProject;
        _blockContainer = new EJPluginBlockContainer(this);
        _objGroupContainer = new EJPluginObjectGroupContainer(this);
        _canvasContainer = new EJPluginCanvasContainer(this);
        _relationContainer = new EJPluginRelationContainer(this);
        _lovDefinitionContainer = new EJPluginLovDefinitionContainer(this);
        _applicationProperties = new HashMap<String, String>();
        _formParameters = new ArrayList<EJPluginApplicationParameter>();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return this;
    }
    

    
    /**
     * This method will be called after the form has been read
     * <p>
     * This method will perform any post reading steps
     * 
     */
    public void initialisationCompleted()
    {
        // Set all mirrored block children names into the parent mirror and the
        // parent into its children
        for (EJPluginBlockProperties parentBlock : _blockContainer.getAllBlockProperties())
        {
            if (parentBlock.isMirrorBlock() && parentBlock.getMirrorBlockName() == null)
            {
                for (EJPluginBlockProperties childBlock : _blockContainer.getAllBlockProperties())
                {
                    if (childBlock.isMirrorBlock() && childBlock.getMirrorBlockName() != null
                            && parentBlock.getName().equalsIgnoreCase(childBlock.getMirrorBlockName()))
                    {
                        parentBlock.addMirrorChild(childBlock);
                        childBlock.setMirrorParent(parentBlock, false);
                    }
                }
            }
        }
    }
    
    public IJavaProject getJavaProject()
    {
        return _formsProject;
    }
    
    /**
     * Returns the <b>EntireJ</b> Properties for this form
     * 
     * @return The name of the <b>EntireJProperties</b> file
     */
    public EJPluginEntireJProperties getEntireJProperties()
    {
        return EJPluginEntireJPropertiesLoader.getEntireJProperties(getJavaProject());
    }
    
    /**
     * Used to retrieve the name of the form for which these properties are
     * valid
     * 
     * @return The name of the form
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * This is an EntireJ internal method and should not be used. It will change
     * all references within the Properties from the previous name to the name
     * given. This is needed when nesting forms within each other.
     * 
     * @param newName
     *            The new name or internal name for this form when used as a
     *            nested form
     */
    public void changeName(String newName)
    {
        _name = newName;
    }
    
    /**
     * Retieves the class name that is responsible for displaying this form
     * 
     * @return the fully qualified class name for the form renderer
     */
    public String getFormRendererName()
    {
        return _formRendererName;
    }
    
    public EJDevFormRendererDefinition getFormRendererDefinition()
    {
        return ExtensionsPropertiesFactory.loadFormRendererDefinition(getEntireJProperties(), this.getFormRendererName());
    }
    
    /**
     * Sets the name of the form renderer
     * <p>
     * the renderer names are defined within the <b>EntireJ Properties</b>
     * 
     * @param formRendererName
     *            The renderer name
     */
    public void setFormRendererName(String formRendererName)
    {
        _formRendererName = formRendererName;
        
        if (formRendererName == null || formRendererName.trim().length() == 0)
        {
            _formRendererProperties = null;
        }
        else
        {
            _formRendererProperties = ExtensionsPropertiesFactory.createFormRendererProperties(this, false);
        }
    }
    
    public void setFormRendererProperties(EJFrameworkExtensionProperties properties)
    {
        _formRendererProperties = properties;
    }
    
    public EJFrameworkExtensionProperties getFormRendererProperties()
    {
        return _formRendererProperties;
    }
    
    public void setBlockContainer(EJPluginBlockContainer container)
    {
        _blockContainer = container;
    }
    
    /**
     * @return the blockContainer
     */
    public EJPluginBlockContainer getBlockContainer()
    {
        return _blockContainer;
    }
    
    public void setObjectGroupContainer(EJPluginObjectGroupContainer container)
    {
        _objGroupContainer = container;
    }
    
    /**
     * @return the blockContainer
     */
    public EJPluginObjectGroupContainer getObjectGroupContainer()
    {
        return _objGroupContainer;
    }
    
    public void setCanvasContainer(EJPluginCanvasContainer container)
    {
        _canvasContainer = container;
    }
    
    /**
     * @return the canvasContainer
     */
    public EJPluginCanvasContainer getCanvasContainer()
    {
        return _canvasContainer;
    }
    
    /**
     * Returns the canvas properties for the given name
     * 
     * @param name
     *            The name of the required canvas properties
     * @return The <code>CanvasProperties</code> with the required name or
     *         <code>null</code> if there is no canvas with the given name or
     *         the name given was null or a zero length string
     */
    public EJCanvasProperties getCanvasProperties(String name)
    {
        if (name == null || name.trim().length() == 0)
        {
            return null;
        }
        
        return getCanvasProps(_canvasContainer, name);
    }
    
    private EJCanvasProperties getCanvasProps(EJCanvasPropertiesContainer container, String canvasName)
    {
        Iterator<EJCanvasProperties> allCanvases = container.getAllCanvasProperties().iterator();
        while (allCanvases.hasNext())
        {
            EJCanvasProperties canvas = allCanvases.next();
            
            if (canvas.getName().equalsIgnoreCase(canvasName))
            {
                return canvas;
            }
            
            if (canvas.getType() == EJCanvasType.POPUP)
            {
                EJCanvasProperties popupChildCanvas = getCanvasProps(canvas.getPopupCanvasContainer(), canvasName);
                if (popupChildCanvas != null && popupChildCanvas.getName().equalsIgnoreCase(canvasName))
                {
                    return popupChildCanvas;
                }
            }
            else if (canvas.getType() == EJCanvasType.TAB)
            {
                Iterator<EJTabPageProperties> allTabPages = canvas.getTabPageContainer().getAllTabPageProperties().iterator();
                while (allTabPages.hasNext())
                {
                    EJCanvasProperties tabPageChildCanvas = getCanvasProps(allTabPages.next().getContainedCanvases(), canvasName);
                    if (tabPageChildCanvas != null && tabPageChildCanvas.getName().equalsIgnoreCase(canvasName))
                    {
                        return tabPageChildCanvas;
                    }
                }
            }
            else if (canvas.getType() == EJCanvasType.STACKED)
            {
                Iterator<EJStackedPageProperties> allStackedPages = canvas.getStackedPageContainer().getAllStackedPageProperties().iterator();
                while (allStackedPages.hasNext())
                {
                    EJCanvasProperties stackedPageChildCanvas = getCanvasProps(allStackedPages.next().getContainedCanvases(), canvasName);
                    if (stackedPageChildCanvas != null && stackedPageChildCanvas.getName().equalsIgnoreCase(canvasName))
                    {
                        return stackedPageChildCanvas;
                    }
                }
            }
            else if (canvas.getType() == EJCanvasType.GROUP)
            {
                return getCanvasProps(canvas.getGroupCanvasContainer(), canvasName);
            }
            else if (canvas.getType() == EJCanvasType.SPLIT)
            {
                return getCanvasProps(canvas.getSplitCanvasContainer(), canvasName);
            }
        }
        return null;
    }
    
    public void setRelationContainer(EJPluginRelationContainer container)
    {
        _relationContainer = container;
    }
    
    /**
     * @return the relationContainer
     */
    public EJPluginRelationContainer getRelationContainer()
    {
        return _relationContainer;
    }
    
    public void setLovDefinitionContainer(EJPluginLovDefinitionContainer container)
    {
        _lovDefinitionContainer = container;
    }
    
    public EJPluginLovDefinitionContainer getLovDefinitionContainer()
    {
        return _lovDefinitionContainer;
    }
    
    /**
     * the title of the form. This will be the translated title code if a title
     * code has been set otherwise it will return the title code.
     * 
     * @return The title of the form
     */
    public String getTitle()
    {
        return _formTitle;
    }
    
    public String getFormDisplayName()
    {
        return _formDisplayName;
    }
    
    public void setFormDisplayName(String _formDisplayName)
    {
        this._formDisplayName = _formDisplayName;
    }
    
    /**
     * Sets the title of this form.
     * 
     * @param title
     *            The form title
     */
    public void setFormTitle(String title)
    {
        _formTitle = title;
    }
    
    /**
     * Returns the required width of the form
     * <p>
     * The value is the width in pixels
     * 
     * @return The required width of the form
     */
    public int getFormWidth()
    {
        return _formWidth;
    }
    
    /**
     * Sets the required width of the form
     * <p>
     * The value is the width in pixels
     * 
     * @param formWidth
     *            The required width of the form
     */
    public void setFormWidth(int formWidth)
    {
        _formWidth = formWidth;
    }
    
    /**
     * Returns the required height of the form
     * <p>
     * The value is the height in pixels
     * 
     * @return The required height of the form
     */
    public int getFormHeight()
    {
        return _formHeight;
    }
    
    /**
     * Sets the required height of the form
     * <p>
     * The value is the height in pixels
     * 
     * @param formHeight
     *            The required height of the form
     */
    public void setFormHeight(int formHeight)
    {
        _formHeight = formHeight;
    }
    
    /**
     * Returns the number of display columns that this for uses
     * <p>
     * The form will lay out the main content canvases within a grid. This
     * property defines how many columns the grid should have. A value of
     * <code>1</code> (the default), indicates that all content canvases will be
     * stacked one above each other
     * 
     * @return The number of columns that the form will use to display the
     *         content canvases
     */
    public int getNumCols()
    {
        return _numCols;
    }
    
    /**
     * Sets the number of columns the form should use to display the content
     * canvases
     * 
     * @param numCols
     *            The number of columns
     * @see #getNumCols()
     */
    public void setNumCols(int numCols)
    {
        _numCols = numCols;
    }
    
    /**
     * The Action Processor is responsible for actions within the form. Actions
     * can include buttons being pressed, check boxes being selected or pre-post
     * query methods etc.
     * 
     * @return The name of the Action Processor responsible for this form.
     */
    public String getActionProcessorClassName()
    {
        return _actionProcessorClassName;
    }
    
    /**
     * Sets the action processor name for this form
     * 
     * @param processorClassName
     *            The action processor name for this form
     */
    public void setActionProcessorClassName(String processorClassName)
    {
        _actionProcessorClassName = processorClassName;
    }
    
    public int compareTo(EJPluginFormProperties arg0)
    {
        return this.getName().compareTo(((EJPluginFormProperties) arg0).getName());
    }
    
    public boolean isReusableBlockForm()
    {
        return _isReusableBlockForm;
    }
    
    public void setIsReusableBlockForm(boolean isReusableBlockForm)
    {
        _isReusableBlockForm = isReusableBlockForm;
    }
    
    public boolean isReusableLovForm()
    {
        return _isReusableLovForm;
    }
    public boolean isObjectGroupForm()
    {
        return _isObjectGroupForm;
    }
    
    public void setIsReusableLovForm(boolean isReusableLovForm)
    {
        _isReusableLovForm = isReusableLovForm;
    }
    public void setIsObjectGroupForm(boolean isObjectGroupForm)
    {
        _isObjectGroupForm = isObjectGroupForm;
    }
    
    public Collection<String> getAllApplicationPropertyNames()
    {
        return _applicationProperties.keySet();
    }
    
    public void addApplicationProperty(String name, String value)
    {
        _applicationProperties.put(name, value);
    }
    
    public String getApplicationProperty(String name)
    {
        return _applicationProperties.get(name);
    }
    
    public void removeApplicationProperty(String name)
    {
        if (containsApplicationProperty(name))
        {
            _applicationProperties.remove(name);
        }
    }
    
    public boolean containsApplicationProperty(String name)
    {
        return _applicationProperties.containsKey(name);
    }
    
    public Collection<EJPluginApplicationParameter> getAllFormParameters()
    {
        return _formParameters;
    }
    
    public void addFormParameter(EJPluginApplicationParameter parameter)
    {
        if (parameter != null)
        {
            _formParameters.add(parameter);
        }
    }
    
    public EJPluginApplicationParameter getFormParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _formParameters)
        {
            if (parameter.getName().equalsIgnoreCase(name))
            {
                return parameter;
            }
        }
        return null;
    }
    
    public void removeFormParameter(EJPluginApplicationParameter parameter)
    {
        _formParameters.remove(parameter);
    }
    
    public boolean containsFormParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _formParameters)
        {
            if (parameter.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean setApplicationProperty(String name, String value)
    {
        if (containsApplicationProperty(name))
        {
            addApplicationProperty(name, value);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public List<String> getBlockNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        
        Iterator<EJPluginBlockProperties> blocks = _blockContainer.getAllBlockProperties().iterator();
        while (blocks.hasNext())
        {
            names.add(blocks.next().getName());
        }
        
        return names;
    }
    
    public EJPluginBlockProperties getBlockProperties(String blockName)
    {
        return _blockContainer.getBlockProperties(blockName);
    }
    
    public List<String> getLovDefinitionItemNames(String lovDefinitionName) throws IllegalArgumentException
    {
        EJPluginLovDefinitionProperties lovDef = _lovDefinitionContainer.getLovDefinitionProperties(lovDefinitionName);
        if (lovDef == null)
        {
            return Collections.emptyList();
        }
        ArrayList<String> names = new ArrayList<String>();
        List<EJPluginBlockItemProperties> allItemProperties = lovDef.getBlockProperties().getItemContainer().getAllItemProperties();
        for (EJPluginBlockItemProperties itemProperties : allItemProperties)
        {
            names.add(itemProperties.getName());
        }
        
        return names;
    }
    
    public String getFormName()
    {
        return _name;
    }
    
    public List<String> getLovDefinitionNames()
    {
        ArrayList<String> names = new ArrayList<String>();
        
        Iterator<EJPluginLovDefinitionProperties> lovDefs = _lovDefinitionContainer.getAllLovDefinitionProperties().iterator();
        while (lovDefs.hasNext())
        {
            names.add(lovDefs.next().getName());
        }
        
        return names;
    }
    
    /**
     * Returns the lov definition properties for the given name
     * 
     * @param name
     *            The name of the required lov definition properties
     * @return The <code>LovDefinitionProperties</code> with the required name
     *         or <code>null</code> if there is no lov definition with the give
     *         name
     */
    public EJPluginLovDefinitionProperties getLovDefinitionProperties(String name)
    {
        if (name == null || name.trim().length() == 0)
        {
            return null;
        }
        if (_lovDefinitionContainer.contains(name))
        {
            return _lovDefinitionContainer.getLovDefinitionProperties(name);
        }
        else
        {
            return null;
        }
    }
    
    public List<String> getInsertScreenBlockItems(String blockName) throws IllegalArgumentException
    {
        EJPluginBlockProperties blockProperties = _blockContainer.getBlockProperties(blockName);
        if (blockProperties == null)
        {
            throw new IllegalArgumentException("Invalid block name passed to getInsertScreenBlockItems: " + blockName);
        }
        ArrayList<String> names = new ArrayList<String>();
        addGroupItems(blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), names);
        return names;
    }
    
    public List<String> getMainScreenBlockItems(String blockName) throws IllegalArgumentException
    {
        EJPluginBlockProperties blockProperties = _blockContainer.getBlockProperties(blockName);
        if (blockProperties == null)
        {
            throw new IllegalArgumentException("Invalid block name passed to getMainScreenBlockItems: " + blockName);
        }
        ArrayList<String> names = new ArrayList<String>();
        addGroupItems(blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), names);
        return names;
    }
    
    public List<String> getQueryScreenBlockItems(String blockName) throws IllegalArgumentException
    {
        EJPluginBlockProperties blockProperties = _blockContainer.getBlockProperties(blockName);
        if (blockProperties == null)
        {
            throw new IllegalArgumentException("Invalid block name passed to getQueryScreenBlockItems: " + blockName);
        }
        ArrayList<String> names = new ArrayList<String>();
        addGroupItems(blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), names);
        return names;
    }
    
    public List<String> getUpdateScreenBlockItems(String blockName) throws IllegalArgumentException
    {
        EJPluginBlockProperties blockProperties = _blockContainer.getBlockProperties(blockName);
        if (blockProperties == null)
        {
            throw new IllegalArgumentException("Invalid block name passed to getUpdateScreenBlockItems: " + blockName);
        }
        ArrayList<String> names = new ArrayList<String>();
        addGroupItems(blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), names);
        return names;
    }
    
    private void addGroupItems(EJItemGroupPropertiesContainer itemGroupContainer, ArrayList<String> itemNameList)
    {
        Iterator<EJItemGroupProperties> groupProperties = itemGroupContainer.getAllItemGroupProperties().iterator();
        while (groupProperties.hasNext())
        {
            EJItemGroupProperties itemGroupProperties = groupProperties.next();
            Iterator<EJScreenItemProperties> items = itemGroupProperties.getAllItemProperties().iterator();
            while (items.hasNext())
            {
                EJScreenItemProperties itemProps = items.next();
                itemNameList.add(itemProps.getReferencedItemName());
            }
            
            addGroupItems(itemGroupProperties.getChildItemGroupContainer(), itemNameList);
        }
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("\nForm: ");
        buffer.append(getName());
        buffer.append("\n  FormRendererName:              " + _formRendererName);
        buffer.append("\n  FormTitle:                     " + _formTitle);
        buffer.append("\n  ActionProcessorClassName:      " + _actionProcessorClassName);
        buffer.append("\n  Form Height:                   " + _formHeight);
        buffer.append("\n  Form Width:                    " + _formWidth);
        buffer.append("\n  Number of Columns:             " + _numCols);
        buffer.append("\n  RendererProperties:\n");
        if (_formRendererProperties != null)
        {
            buffer.append(_formRendererProperties.toString());
        }
        buffer.append("\nBlocks:\n");
        
        Iterator<EJPluginBlockProperties> blocks = _blockContainer.getAllBlockProperties().iterator();
        while (blocks.hasNext())
        {
            buffer.append(blocks.next().toString());
            buffer.append("\n");
        }
        
        return buffer.toString();
    }
    
    @Override
    public String getFirstNavigableBlock()
    {
        return _firstNavigableBlock;
    }
    
    public void setFirstNavigableBlock(String firstNavigableBlock)
    {
        _firstNavigableBlock = firstNavigableBlock;
    }
}
