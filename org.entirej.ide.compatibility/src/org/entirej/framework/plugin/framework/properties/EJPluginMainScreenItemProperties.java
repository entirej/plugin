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

import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;
import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJLovDefinitionProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevMainScreenItemDisplayProperties;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;

/**
 * 
 * 
 *         Contains the properties required for the main screen if the block
 *         renderer is a GridLayoutRenderer
 *         <p>
 */
public class EJPluginMainScreenItemProperties implements EJDevMainScreenItemDisplayProperties, EJPluginScreenItemProperties, EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long serialVersionUID = -6363972669691241559L;
    private EJPluginItemGroupProperties    _itemGroupProperties;
    private EJPluginBlockProperties        _blockProperties;
    private EJFrameworkExtensionProperties _blockRendererRequiredProperties = null;
    private EJFrameworkExtensionProperties _lovRendererRequiredProperties   = null;
    private String                         _itemLabel                       = "";
    private String                         _itemHint                        = "";
    private String                         _referencedItemName              = "";
    private boolean                        _visible                         = true;
    private boolean                        _editAllowed                     = false;
    private boolean                        _mandatory                       = false;
    private boolean                        _enableLovNotification           = false;
    private String                         _lovMappingName                  = "";
    private boolean                        _validateFromLov                 = true;
    private String                         _actionCommand                   = "";
    private boolean                        _isSpacerItem                    = false;
    private boolean                         _isSeparator           = false;
    private EJLineStyle                     _separatorLineStyle    = EJLineStyle.SOLID;
    private EJSeparatorOrientation          _separatorOrientation  = EJSeparatorOrientation.HORIZONTAL;
    
    public EJPluginMainScreenItemProperties(EJPluginItemGroupProperties itemGroupProperties, boolean addDefaults, boolean isSpacerItem)
    {
        _itemGroupProperties = itemGroupProperties;
        _blockProperties = itemGroupProperties.getBlockProperties();
        refreshBlockRendererRequiredProperties(addDefaults);
        refreshLovRendererRequiredProperties();
        _isSpacerItem = isSpacerItem;
    }
    
    public boolean isSpacerItem()
    {
        return _isSpacerItem;
    }
    
    public void setIsSpacerItem(boolean isSpacerItem)
    {
        _isSpacerItem = isSpacerItem;
    }
    
    /**
     * Returns the name of the block to which this item belongs
     * 
     * @return The name of the block
     */
    public String getBlockName()
    {
        return _blockProperties.getName();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _blockProperties.getFormProperties();
    }
    
    public void internalSetName(String newName)
    {
        _referencedItemName = newName;
    }
    
    /**
     * Returns the label defined for this block item
     * <p>
     * It is the <code>BlockRenderer</code> that decides if and how the items
     * label should be displayed
     * 
     * @return The label defined for this item
     */
    public String getLabel()
    {
        return _itemLabel;
    }
    
    /**
     * Sets this items label
     * 
     * @param label
     *            This items label
     */
    public void setLabel(String label)
    {
        _itemLabel = label;
    }
    
    /**
     * Returns the hint defined for this block item
     * <p>
     * It is the <code>ItemRenderer</code> that decides if and how the items
     * hint should be displayed
     * 
     * @return The label defined for this item
     */
    public String getHint()
    {
        return _itemHint;
    }
    
    /**
     * Sets this items hint
     * 
     * @param label
     *            This items hint
     */
    public void setHint(String hint)
    {
        _itemHint = hint;
    }
    
    public EJPluginItemGroupProperties getItemGroupProperties()
    {
        return _itemGroupProperties;
    }
    
    public void InternalSetItemGroupProperties(EJPluginItemGroupProperties properties)
    {
        _itemGroupProperties = properties;
    }
    
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public EJLovDefinitionProperties getLovDefinitionProperties()
    {
        return _blockProperties.getLovDefinition();
    }
    
    public String getReferencedItemName()
    {
        return _referencedItemName;
    }
    
    public EJDevBlockItemDisplayProperties getBlockItemDisplayProperties()
    {
        return _blockProperties.getBlockItemDisplayContainer().getItemProperties(_referencedItemName);
    }
    
    public void setReferencedItemName(String name)
    {
        _referencedItemName = name;
    }
    
    public String getName()
    {
        return getReferencedItemName();
    }
    
    public void refreshBlockRendererRequiredProperties(boolean addDefaults)
    {
        
        if (_blockProperties.isUsedInLovDefinition())
        {
            _blockRendererRequiredProperties = ExtensionsPropertiesFactory.createBlockRequiredItemRendererProperties(_blockProperties, addDefaults,
                    _blockRendererRequiredProperties);
        }
        else
        {
            if (_blockProperties.getBlockRendererName() == null || _blockProperties.getBlockRendererName().trim().length() == 0)
            {
                _blockRendererRequiredProperties = null;
            }
            else
            {
                _blockRendererRequiredProperties = ExtensionsPropertiesFactory.createBlockRequiredItemRendererProperties(_blockProperties, addDefaults,
                        _blockRendererRequiredProperties);
            }
        }
    }
    
    public void refreshLovRendererRequiredProperties()
    {
        if (_blockProperties.getLovDefinition() == null)
        {
            return;
        }
        
        if (_blockProperties.getLovDefinition().getLovRendererName() == null || _blockProperties.getLovDefinition().getLovRendererName().trim().length() == 0)
        {
            _lovRendererRequiredProperties = null;
        }
        else
        {
            _lovRendererRequiredProperties = ExtensionsPropertiesFactory.createLovRequiredItemRendererProperties(_blockProperties.getEntireJProperties(),
                    _blockProperties.getFormProperties(), _blockProperties.getLovDefinition(), false, _lovRendererRequiredProperties);
        }
    }
    
    /**
     * Returns the <code>RenderingProperties</code> that are required by the
     * block renderer
     * 
     * @return The required block renderer properties for this item
     */
    public EJFrameworkExtensionProperties getBlockRendererRequiredProperties()
    {
        return _blockRendererRequiredProperties;
    }
    
    public void setBlockRendererRequiredProperties(EJFrameworkExtensionProperties properties)
    {
        _blockRendererRequiredProperties = properties;
    }
    
    public EJFrameworkExtensionProperties getLovRendererRequiredProperties()
    {
        return _lovRendererRequiredProperties;
    }
    
    public void setLovRendererRequiredProperties(EJFrameworkExtensionProperties properties)
    {
        _lovRendererRequiredProperties = properties;
    }
    
    public boolean isVisible()
    {
        return _visible;
    }
    
    public void setVisible(boolean visible)
    {
        _visible = visible;
    }
    
    public void setEditAllowed(boolean editAllowed)
    {
        _editAllowed = editAllowed;
    }
    
    public boolean isEditAllowed()
    {
        return _editAllowed;
    }
    
    public void setMandatory(boolean mandatory)
    {
        _mandatory = mandatory;
    }
    
    public boolean isMandatory()
    {
        return _mandatory;
    }
    
    /**
     * Sets the enabled flag of Lov Notification
     * <p>
     * Lov Notification is automatically enabled for items with an lov attached.
     * However it is also possible to enable lov notification for non lov items.
     * If this is the case, then EJ will call the action processor to notify the
     * developer that the lov has been activated, but no LOV will be displayed.
     * The developer can then do what is required for the specific business
     * case, e.g. Check the value entered against a Business Service or call
     * another form etc
     * 
     * @param enable
     *            Enables lov notification
     */
    public void enableLovNotification(boolean enable)
    {
        _enableLovNotification = enable;
    }
    
    /**
     * Indicates if lov notification has been enabled
     * <p>
     * Lov Notification is automatically enabled for items with an lov attached.
     * However it is also possible to enable lov notification for non lov items.
     * If this is the case, then EJ will call the action processor to notify the
     * developer that the lov has been activated, but no LOV will be displayed.
     * The developer can then do what is required for the specific business
     * case, e.g. Check the value entered against a Business Service or call
     * another form etc
     * 
     * @return <code>true</code> if lov notification has been enabled, otherwise
     *         <code>false</code>
     */
    public boolean isLovNotificationEnabled()
    {
        return _enableLovNotification;
    }
    
    public void setLovMappingName(String lovMappingName)
    {
        _lovMappingName = lovMappingName;
    }
    
    public String getLovMappingName()
    {
        return _lovMappingName;
    }
    
    /**
     * Sets a flag to indicate if the LOV which is assigned to this item should
     * be used to validate the items value
     * <p>
     * If validate from love is true, then the value entered by the user will
     * validated against
     * 
     * @param validateFromLov
     */
    public void setValidateFromLov(boolean validateFromLov)
    {
        _validateFromLov = validateFromLov;
    }
    
    /**
     * Indicates if this screen item should be validated agains the lov values
     * 
     * @return <code>true</code> if the item should be validated against the lov
     *         values otherwise <code>false</code>
     */
    public boolean validateFromLov()
    {
        return _validateFromLov;
    }
    
    public void setActionCommand(String command)
    {
        _actionCommand = command;
    }
    
    public String getActionCommand()
    {
        return _actionCommand;
    }
    
    public EJCoreVisualAttributeProperties getVisualAttributeProperties()
    {
        return null;
    }
    
    @Override
    public EJLineStyle getSeparatorLineStyle()
    {
        return _separatorLineStyle;
    }

    @Override
    public EJSeparatorOrientation getSeparatorOrientation()
    {
        return _separatorOrientation;
    }

    @Override
    public boolean isSeparator()
    {
        return _isSeparator;
    }

    public void setSeparatorLineStyle(EJLineStyle separatorLineStyle)
    {
        _separatorLineStyle = separatorLineStyle;
    }

    public void setSeparatorOrientation(EJSeparatorOrientation separatorOrientation)
    {
        _separatorOrientation = separatorOrientation;
    }

    public void setSeparator(boolean isSeparator)
    {
        _isSeparator = isSeparator;
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("\nItem: ");
        buffer.append("\n    ReferencedBlockItemName: ");
        buffer.append(_referencedItemName);
        
        buffer.append("\n    Label:                   ");
        buffer.append(_itemLabel);
        buffer.append("\n    Hint:                    ");
        buffer.append(_itemHint);
        buffer.append("\n    Visible                  ");
        buffer.append(_visible);
        buffer.append("\n    LovMappingName           ");
        buffer.append(_lovMappingName);
        buffer.append("\n    ActionCommand            ");
        buffer.append(_actionCommand);
        buffer.append("\n    Manditory:               ");
        buffer.append(_mandatory);
        buffer.append("      EditAllowed:             ");
        buffer.append(_editAllowed);
        buffer.append("\nBlockRendererRequiredProperties:\n");
        buffer.append(_blockRendererRequiredProperties);
        
        return buffer.toString();
    }
    
}
