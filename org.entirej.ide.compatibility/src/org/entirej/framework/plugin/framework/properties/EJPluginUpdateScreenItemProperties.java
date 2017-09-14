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
package org.entirej.framework.plugin.framework.properties;

import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;
import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevUpdateScreenItemDisplayProperties;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;

/**
 * 
 * 
 *         Contains the properties required for the update screen
 *         <p>
 *         EntireJ requires the query screen to be displayed as a grid. Meaning
 *         each item and the items label will be placed within a part of the
 *         grid using X and Y coordinates.
 */
public class EJPluginUpdateScreenItemProperties implements EJDevUpdateScreenItemDisplayProperties, EJPluginScreenItemProperties
{
    /**
     * 
     */
    private static final long serialVersionUID = 1889865644426349984L;
    private EJPluginItemGroupProperties    _itemGroupProperties;
    private EJPluginBlockProperties        _blockProperties;
    private EJFrameworkExtensionProperties _updateScreenRendererItemProperties;
    private String                         _itemLabel             = "";
    private String                         _itemHint              = "";
    private String                         _referencedItemName    = "";
    private boolean                        _visible               = true;
    private boolean                        _editAllowed           = false;
    private boolean                        _mandatory             = false;
    private boolean                        _enableLovNotification = false;
    private boolean                        _validateFromLov       = true;
    private String                         _lovMappingName        = "";
    private String                         _actionCommand         = "";
    private boolean                        _isSpacerItem          = false;
    private boolean                         _isSeparator           = false;
    private EJLineStyle                     _separatorLineStyle    = EJLineStyle.SOLID;
    private EJSeparatorOrientation          _separatorOrientation  = EJSeparatorOrientation.HORIZONTAL;
    
    public EJPluginUpdateScreenItemProperties(EJPluginItemGroupProperties itemGroupProperties, boolean addDefaults, boolean isSpacerItem)
    {
        _itemGroupProperties = itemGroupProperties;
        _blockProperties = itemGroupProperties.getBlockProperties();
        refreshUpdateScreenRendererRequiredProperties(addDefaults);
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
    
    public void internalSetName(String newName)
    {
        _referencedItemName = newName;
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
    
    public void refreshUpdateScreenRendererRequiredProperties(boolean addDefaults)
    {
        if (_blockProperties.getUpdateScreenRendererDefinition() == null)
        {
            _updateScreenRendererItemProperties = null;
        }
        else
        {
            _updateScreenRendererItemProperties = ExtensionsPropertiesFactory.createUpdateScreenRendererItemProperties(_blockProperties, addDefaults,
                    _updateScreenRendererItemProperties);
        }
    }
    
    public EJFrameworkExtensionProperties getUpdateScreenRendererRequiredProperties()
    {
        return _updateScreenRendererItemProperties;
    }
    
    public EJFrameworkExtensionProperties getBlockRendererRequiredProperties()
    {
        return _updateScreenRendererItemProperties;
    }
    
    public void setUpdateScreenRendererRequiredProperties(EJFrameworkExtensionProperties properties)
    {
        _updateScreenRendererItemProperties = properties;
    }
    
    /**
     * Returns the name of the data block item to which this item references
     * <p>
     * Each item that is displayed on the query screen can reference a block
     * item. If a reference is specified, then value on the update screen will
     * be used when making the blocks update. All other items will be display
     * items only and have no effect on the update being made
     * 
     * @return The name of the block item that this item references
     */
    public String getReferencedItemName()
    {
        return _referencedItemName;
    }
    
    public EJDevBlockItemDisplayProperties getBlockItemDisplayProperties()
    {
        return _blockProperties.getBlockItemDisplayContainer().getItemProperties(_referencedItemName);
    }
    
    /**
     * Sets the name of the block item to which this item references
     * <p>
     * 
     * @return The name of the block item
     */
    public void setReferencedItemName(String name)
    {
        _referencedItemName = name;
    }
    
    public String getName()
    {
        return getReferencedItemName();
    }
    
    /**
     * Indicates if this item is to be visible on the update screen
     * <p>
     * 
     * @return <code>true</code> if the item should be visible, otherwise
     *         <code>false</code>
     */
    public boolean isVisible()
    {
        return _visible;
    }
    
    /**
     * If set to <code>true</code>, this item will be visible on the update
     * screen of the block
     * 
     * @param visible
     */
    public void setVisible(boolean visible)
    {
        _visible = visible;
    }
    
    /**
     * If set to <code>true</code>, users will be able to modify this items
     * value
     * 
     * @param editAllowed
     */
    public void setEditAllowed(boolean editAllowed)
    {
        _editAllowed = editAllowed;
    }
    
    /**
     * Indicates if this item can be modified
     * <p>
     * 
     * @return <code>true</code> if the item should is editable, otherwise
     *         <code>false</code>
     */
    public boolean isEditAllowed()
    {
        return _editAllowed;
    }
    
    /**
     * Indicates that a value is required by this item during update operations
     * <p>
     * EntireJ will ensure that a value has been entered before issuing the
     * update
     * 
     * @param mandatory
     *            The mandatory flag
     */
    public void setMandatory(boolean mandatory)
    {
        _mandatory = mandatory;
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
    
    /**
     * Indicates that a value is required during update operations
     * <p>
     * EntireJ will ensure that a value has been entered before issuing the
     * update
     * 
     * @return The mandatory indicator
     */
    public boolean isMandatory()
    {
        return _mandatory;
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
        buffer.append("\n    Manditory:               ");
        buffer.append(_mandatory);
        buffer.append("      EditAllowed:             ");
        buffer.append(_editAllowed);
        
        return buffer.toString();
    }

    @Override
    public void enableLovValidation(boolean arg0)
    {
        // TODO Auto-generated method stub
        
    }
    
}
