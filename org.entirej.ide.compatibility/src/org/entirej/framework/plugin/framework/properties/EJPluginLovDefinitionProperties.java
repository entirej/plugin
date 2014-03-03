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

import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJLovDefinitionProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayPropertiesContainer;
import org.entirej.framework.dev.properties.interfaces.EJDevItemGroupDisplayPropertiesContainer;
import org.entirej.framework.dev.properties.interfaces.EJDevLovDefinitionDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;

public class EJPluginLovDefinitionProperties implements EJLovDefinitionProperties, EJDevLovDefinitionDisplayProperties, EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long serialVersionUID = -5120997856003950841L;

    private EJPluginFormProperties         _formProperties;
    
    private String                         _name;
    private String                         _referencedLovDefinitionName;
    private boolean                        _isReferenced = false;
    private String                         _lovRendererName;
    private int                            _width;
    private int                            _height;
    private EJFrameworkExtensionProperties _lovRendererProperties;
    private String                         _actionProcessorClassName;

    private String                                 _referencedObjectGroupName    = "";
    
    
    private EJPluginBlockProperties        _blockProperties;
    
    
    
    public EJPluginLovDefinitionProperties(String name, EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
        _name = name;
    }
    
    public EJPluginEntireJProperties getEntireJProperties()
    {
        return _formProperties.getEntireJProperties();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public void setFormProperties(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
    }
    
    /**
     * Sets the name of this Lov Definition
     * <p>
     * The method should only be used within the EntireJ Plugin internal code
     * when renaming the definition
     * 
     * @param name
     *            The new name of this definition
     */
    public void internalSetName(String name)
    {
        _name = name;
    }
    
    /**
     * Returns the name of this LovDefinition
     * 
     * @return
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * Sets the name of the referenced lov definition name
     * 
     * @param referencedName
     *            The reference name
     */
    public void setReferencedLovDefinitionName(String referencedName)
    {
        _referencedLovDefinitionName = referencedName;
    }
    
    /**
     * Returns the name of the lov definition upon which this definition is
     * based
     * 
     * @return The name of the referenced lov definition name
     */
    public String getReferencedLovDefinitionName()
    {
        return _referencedLovDefinitionName;
    }
    
    /**
     * Indicates if this LovDefinition references a re-usable definition
     * 
     * @return <code>true</code> if references otherwise <code>false</code>
     */
    public boolean isReferenceBlock()
    {
        return _isReferenced;
    }
    
    /**
     * Used to set the referenced flag for this lovDefinition
     * 
     * @param isReferenced
     *            <code>true</code> if references otherwise <code>false</code>
     */
    public void setIsReferenced(boolean isReferenced)
    {
        _isReferenced = isReferenced;
        if (_blockProperties != null)
        {
            if (_isReferenced)
            {
                _blockProperties.setIsReferenced(true);
            }
        }
    }
    
    /**
     * If set to <code>true</code> then the lov definition can use the blocks
     * defined query option
     * <p>
     * This could include showing a query screen for hte user to reduce the
     * number of records within the lov thus simplifying the selection of the
     * correct value
     * 
     * @return true if query operations are allowed otherwise false
     */
    public boolean isUserQueryAllowed()
    {
        EJDevLovRendererDefinition _rendererDefinition = getRendererDefinition();
        if (_rendererDefinition != null)
        {
            return _rendererDefinition.allowsUserQuery();
        }
        else
        {
            return false;
        }
    }
    
    /**
     * If automatic query is set to true, then the LovDefinition will
     * automatically make a query before the values are shown to the user. If
     * the automatic query is set to false, then an empty screen will be shown
     * and the user must execute a query or use the query screen to display the
     * lov values
     * 
     * @return true if an automatic query will be executed, otherwise false
     */
    public boolean isAutomaticQuery()
    {
        EJDevLovRendererDefinition _rendererDefinition = getRendererDefinition();
        if (_rendererDefinition != null)
        {
            return _rendererDefinition.executeAutomaticQuery();
        }
        else
        {
            return false;
        }
    }
    
    public void setHeight(int height)
    {
        _height = height;
    }
    
    public int getHeight()
    {
        return _height;
    }
    
    public void setWidth(int width)
    {
        _width = width;
    }
    
    public int getWidth()
    {
        return _width;
    }
    
    /**
     * The name of the renderer used for the display of this lov
     * <p>
     * All renderers are defined within the <b>EntireJ Properties</b>
     * 
     * @return The renderer for this lov
     */
    public String getLovRendererName()
    {
        return _lovRendererName;
    }
    
    /**
     * Sets the name of the lov renderer that is responsible for displaying this
     * lov
     * 
     * @param lovRendererName
     *            the name of the Lov Renderer
     */
    public void setLovRendererName(String lovRendererName, boolean addDefaultValues)
    {
        if (null == lovRendererName || lovRendererName.trim().length() == 0)
        {
            _lovRendererName = null;
            _lovRendererProperties = null;
        }
        else if (_lovRendererName != null && lovRendererName.equalsIgnoreCase(_lovRendererName))
        {
            // do nothing otherwise all renderer property values
            // that have already been set will be lost.
            return;
        }
        else
        {
            _lovRendererName = lovRendererName;
            
            _lovRendererProperties = ExtensionsPropertiesFactory.createLovRendererProperties(_formProperties, lovRendererName, addDefaultValues,
                    _lovRendererProperties);
            
            if (_blockProperties != null)
            {
                if (isUserQueryAllowed())
                {
                    _blockProperties.setQueryScreenRendererProperties(ExtensionsPropertiesFactory.createQueryScreenRendererProperties(_blockProperties,
                            addDefaultValues, _blockProperties.getQueryScreenRendererProperties()));
                }
                else
                {
                    _blockProperties.setQueryScreenRendererProperties(null);
                }
            }
        }
    }
    
    public void refreshRendererPropertiesForItems()
    {
        if (_blockProperties != null)
        {
            Iterator<EJItemGroupProperties> itemGroups = _blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN).getAllItemGroupProperties().iterator();
            while (itemGroups.hasNext())
            {
                EJItemGroupProperties itemGroup = itemGroups.next();
                Iterator<EJScreenItemProperties> screenItems = itemGroup.getAllItemProperties().iterator();
                while (screenItems.hasNext())
                {
                    EJPluginMainScreenItemProperties props = (EJPluginMainScreenItemProperties) screenItems.next();
                    props.refreshLovRendererRequiredProperties();
                }
            }
        }
    }
    
    /**
     * Returns the definition properties for the lov renderer or
     * <code>null</code> if no lov renderer has been set
     * 
     * @return The renderer definition properties for the lov renderer or
     *         <code>null</code> if no renderer has been set
     */
    public EJDevLovRendererDefinition getRendererDefinition()
    {
        return ExtensionsPropertiesFactory.loadLovRendererDefinition(_formProperties.getEntireJProperties(), _lovRendererName);
        
    }
    
    /**
     * Returns the rendering properties for this lov
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the lov
     * <p>
     * 
     * @return The blocks rendering properties
     */
    public EJFrameworkExtensionProperties getLovRendererProperties()
    {
        return _lovRendererProperties;
    }
    
    public void setLovRendererProperties(EJFrameworkExtensionProperties properties)
    {
        _lovRendererProperties = properties;
    }
    
    /**
     * Sets the BlockProperties of the block that will contain the data for this
     * lov definition
     * 
     * @param blockProperties
     *            The properties of the block for this lov definition
     */
    public void setBlockProperties(EJPluginBlockProperties blockProperties)
    {
        _blockProperties = blockProperties;
        if (_blockProperties != null)
        {
            if (_isReferenced)
            {
                _blockProperties.setIsReferenced(true);
            }
            blockProperties.internalSetName(getName());
            
            _blockProperties.setLovDefinitionProperties(this);
            if (_blockProperties.getQueryScreenRendererProperties() == null)
            {
                EJDevLovRendererDefinition _rendererDefinition = getRendererDefinition();
                if (_rendererDefinition != null && _rendererDefinition.allowsUserQuery() && _rendererDefinition.getQueryScreenRendererDefinition() != null)
                {
                    try
                    {
                        EJDevQueryScreenRendererDefinition def = _rendererDefinition.getQueryScreenRendererDefinition();
                        _blockProperties.setQueryScreenRendererDefinition(def, true);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
    
    public EJDevBlockItemDisplayPropertiesContainer getBlockItemDisplayContainer()
    {
        return _blockProperties.getBlockItemDisplayContainer();
    }
    
    public EJDevItemGroupDisplayPropertiesContainer getMainScreenItemGroupDisplayContainer()
    {
        return _blockProperties.getMainScreenItemGroupDisplayContainer();
    }
    
    /**
     * The Action Processor is responsible for actions within the lov. Actions
     * can include buttons being pressed, check boxes being selected or pre-post
     * query methods etc.
     * 
     * @return The name of the Action Processor responsible for this lov
     */
    public String getActionProcessorClassName()
    {
        return _actionProcessorClassName;
    }
    
    /**
     * Sets the action processor name for this lov
     * 
     * @param processorClassName
     *            The action processor class name for this lov
     */
    public void setActionProcessorClassName(String processorClassName)
    {
        _actionProcessorClassName = processorClassName;
    }
    
    /**
     * Returns the <code>BlockProperties</code> for this lov definition
     * 
     * @return the lov definition block properties
     */
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public EJPluginLovDefinitionProperties makeCopy(String newName)
    {
        return makeCopy(newName, _formProperties);
    }
    
    public EJPluginLovDefinitionProperties makeCopy(String newName, EJPluginFormProperties formProperties)
    {
        
        EJPluginLovDefinitionProperties newLovDef = new EJPluginLovDefinitionProperties(newName, formProperties);
        
        newLovDef.setReferencedLovDefinitionName(_referencedLovDefinitionName);
        newLovDef.setIsReferenced(_isReferenced);
        newLovDef.setWidth(_width);
        newLovDef.setHeight(_height);
        newLovDef.setLovRendererName(_lovRendererName, false);
        newLovDef.setActionProcessorClassName(_actionProcessorClassName);
        
        if (newLovDef.getLovRendererProperties() != null)
        {
            newLovDef.getLovRendererProperties().copyValuesFromGroup(_lovRendererProperties);
        }
        
        try
        {
            newLovDef.setBlockProperties(_blockProperties.makeCopy(_blockProperties.getName(), false, newLovDef, formProperties));
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
        
        return newLovDef;
    }
    
    @Override
    public boolean isReferenced()
    {
        return _isReferenced;
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
    
}
