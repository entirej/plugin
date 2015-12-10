/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.dev.properties.interfaces;

import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;

public interface EJDevScreenItemDisplayProperties
{
    /**
     * Indicates if this screen item is a spacer item
     * <p>
     * A spacer item is a special type of item that has no reference to a block
     * item and only acts as a help to build complex displays
     * <p>
     * The {@link #getBlockItemDisplayProperties()}
     * 
     * @return
     */
    public boolean isSpacerItem();
    
    
    /**
     * Returns the orientation of the Separator
     * 
     * @return The separator orientation
     */
    public EJSeparatorOrientation getSeparatorOrientation();
    
    /**
     * Returns the style of the line
     * 
     * @return The line style
     * 
     */
    public EJLineStyle getSeparatorLineStyle();
    
    /**
     * Indicates if this spacer screen item renderer as separator
     * 
     * @return <code>true</code> if this spacer item is a separator, otherwise
     *         <code>false</code>
     */
    public boolean isSeparator();
    

    /**
     * Returns the name of the block to which this item belongs
     * 
     * @return The name of the block
     */
    public String getBlockName();

    /**
     * Returns the item properties for this item
     * 
     * @return The block item properties for this item
     */
    public EJDevBlockItemDisplayProperties getBlockItemDisplayProperties();

    /**
     * Returns the name of the data block item to which this item references
     * <p>
     * Each item that is displayed on the query screen can reference a block
     * item. If a reference is specified, then value on the query screen will be
     * used when making the blocks insert. All other items will be display items
     * only and have no effect on the insert being made
     * 
     * @return The name of the block item that this item references
     */
    public String getReferencedItemName();

    /**
     * Indicates if this item is to be visible on the insert screen
     * <p>
     * 
     * @return <code>true</code> if the item should be visible, otherwise
     *         <code>false</code>
     */
    public boolean isVisible();

    /**
     * Indicates if this item can be modified
     * <p>
     * 
     * @return <code>true</code> if the item should is editable, otherwise
     *         <code>false</code>
     */
    public boolean isEditAllowed();

    /**
     * Indicates that a value is required during insert operations
     * <p>
     * EntireJ will ensure that a value has been entered before issuing the
     * insert
     * 
     * @return The mandatory indicator
     */
    public boolean isMandatory();

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
    public void enableLovNotification(boolean enable);

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
    public boolean isLovNotificationEnabled();

    /**
     * Returns the name of the lov mapping that has been assigned to this screen
     * item
     * 
     * @return The name of the lov mapping assigned to this item or
     *         <code>null</code> if no mapping has been assigned
     */
    public String getLovMappingName();

    /**
     * Sets a flag to indicate if the LOV which is assigned to this item should
     * be used to validate the items value
     * <p>
     * If validate from love is true, then the value entered by the user will
     * validated against
     * 
     * @param validateFromLov
     */
    public void setValidateFromLov(boolean validateFromLov);

    /**
     * Indicates if this screen item should be validated agains the lov values
     * 
     * @return <code>true</code> if the item should be validated against the lov
     *         values otherwise <code>false</code>
     */
    public boolean validateFromLov();

    /**
     * Returns the label defined for this block item
     * <p>
     * It is the <code>BlockRenderer</code> that decides if and how the items
     * label should be displayed
     * 
     * @return The label defined for this item
     */
    public String getLabel();

    /**
     * Returns the hint defined for this block item
     * <p>
     * It is the <code>ItemRenderer</code> that decides if and how the items
     * hint should be displayed
     * 
     * @return The label defined for this item
     */
    public String getHint();

}
