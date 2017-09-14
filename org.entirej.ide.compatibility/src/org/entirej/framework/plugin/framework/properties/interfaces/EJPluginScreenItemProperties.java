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
package org.entirej.framework.plugin.framework.properties.interfaces;

import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;

public interface EJPluginScreenItemProperties extends EJScreenItemProperties, EJDevScreenItemDisplayProperties
{
    /**
     * Used internally within EntireJ to reset the items name
     * 
     * @param newName
     *            The new name of this item
     */
    public void internalSetName(String newName);
    
    public EJPluginItemGroupProperties getItemGroupProperties();
    
    public abstract EJPluginBlockProperties getBlockProperties();
    
    public void InternalSetItemGroupProperties(EJPluginItemGroupProperties properties);
    
    public boolean isSpacerItem();
    
    /**
     * Sets this items label
     * 
     * @param label
     *            This items label
     */
    public void setLabel(String label);
    
    /**
     * Returns the name of the data block item to which this item references
     * <p>
     * 
     * @return The name of the block item that this item references
     */
    public abstract String getReferencedItemName();
    
    /**
     * Sets the name of the block item to which this item references
     * <p>
     * 
     * @return The name of the block item
     */
    public abstract void setReferencedItemName(String name);
    
    public abstract String getName();
    
    /**
     * The action command is a string identifier sent by the item renderer to
     * the action processor when, for example a button is pressed
     * <p>
     * Only item renderers that can actually send action commands will have an
     * action command property set. Whether or not the renderer can send action
     * commands is defined within the renderer definition
     * 
     * @param command
     *            The command the item renderer will send
     * 
     * @see IActionProcessor
     */
    public void setActionCommand(String command);
    
    /**
     * The item renderers action command
     * 
     * @return The item renderers action command
     * @see #setActionCommand(String)
     */
    public String getActionCommand();
    
    /**
     * Indicates if this item is to be made visible
     * <p>
     * 
     * @return <code>true</code> if the item should be made visible, otherwise
     *         <code>false</code>
     */
    public abstract boolean isVisible();
    
    /**
     * If set to <code>true</code>, this item will be visible
     * 
     * @param visible
     */
    public abstract void setVisible(boolean visible);
    
    /**
     * If set to <code>true</code>, users will be able to modify this items
     * value
     * 
     * @param editAllowed
     */
    public abstract void setEditAllowed(boolean editAllowed);
    
    /**
     * Indicates if this item can be modified
     * <p>
     * 
     * @return <code>true</code> if the item should is editable, otherwise
     *         <code>false</code>
     */
    public abstract boolean isEditAllowed();
    
    /**
     * If the block renderer requires no insert or update screen. Then it
     * probably means that users edit the data directly within the main screen.
     * If this is the case then it is possible to set a mandatory flag. The
     * mandatory flag means that the user must enter a value for the given item.
     * 
     * @param mandatory
     */
    public abstract void setMandatory(boolean mandatory);
    
    /**
     * If the block renderer requires no insert or update screen. Then it
     * probably means that users edit the data directly within the main screen.
     * If this is the case then it is possible to set a mandatory flag. The
     * mandatory flag means that the user must enter a value for the given item.
     * 
     * @return <code>true</code> if users must enter a value for this item,
     *         otherwise <code>false</code>
     */
    public abstract boolean isMandatory();
    
    /**
     * Assigns an lov mapping to this item
     * <p>
     * If an lov mapping is assigned, the item will respond to the user opening
     * a lov screen from the item. How the activation is implemented is
     * dependent on how the GUI is implemented
     * 
     * @param enable
     *            The enable flag for lov activation
     */
    public void setLovMappingName(String lovMappingName);
    
    /**
     * Returns the name of the lov mapping assigned to this item
     * 
     * @return The name of the lov mapping assigned to this item or
     *         <code>null</code> if none was assigned
     */
    public String getLovMappingName();
    
    public void setHint(String hint);
    
    public EJFrameworkExtensionProperties getBlockRendererRequiredProperties();

    public void setSeparatorLineStyle(EJLineStyle value);

    public void setSeparatorOrientation(EJSeparatorOrientation value);

    public void setSeparator(boolean value);
    
}
