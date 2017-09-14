/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.dev.properties.interfaces;

import java.util.Collection;

import org.entirej.framework.core.enumerations.EJItemGroupAlignment;
import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;

public interface EJDevItemGroupDisplayProperties
{
    public EJDevItemGroupDisplayPropertiesContainer getParentItemGroupContainer();

    public EJDevItemGroupDisplayPropertiesContainer getChildItemGroupContainer();

    /**
     * Indicates if a frame should be displayed around the groups items
     * 
     * @return The display indicator
     */
    public boolean dispayGroupFrame();

    /**
     * Returns the name that will be displayed within the item groups frame
     * 
     * @return The item groups frame
     */
    public String getFrameTitle();

    /**
     * Indicates how many display columns this group will have
     * <p>
     * All items being added to this group will be inserted into a grid. The
     * grid will have any number of rows but will be limited to the amount of
     * columns as set by this parameter.
     * <p>
     * Items added to this page can span multiple columns and rows
     * 
     * @return The number of columns defined for this group
     */
    public int getNumCols();

    public int getHeight();

    public int getWidth();

    public int getXspan();

    public int getYspan();

    public boolean canExpandHorizontally();

    public boolean canExpandVertically();

    
    public EJItemGroupAlignment getHorizontalAlignment();
    
    public EJItemGroupAlignment getVerticalAlignment();
    
    
    
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
     * Returns the name of this item group
     * 
     * @return The item group name
     */
    public String getName();

    /**
     * Returns all <code>EJDevScreenItemDisplayProperties</code> contained
     * within this container
     * 
     * @return All <code>EJDevScreenItemDisplayProperties</code> contained
     *         within this container
     */
    public Collection<EJDevScreenItemDisplayProperties> getAllItemDisplayProperties();

    /**
     * Get a <code>EJDevScreenItemDisplayProperties</code> object which
     * references the given block item
     * 
     * @param itemName
     *            The block item name
     * 
     * @return The <code>EJDevScreenItemDisplayProperties</code> which
     *         references the given block item or null if there is no reference
     *         to the given item name
     */
    public EJDevScreenItemDisplayProperties getItemDisplayPropertiesForBlockItem(String itemName);

    public EJFrameworkExtensionProperties getRendererProperties();
}
