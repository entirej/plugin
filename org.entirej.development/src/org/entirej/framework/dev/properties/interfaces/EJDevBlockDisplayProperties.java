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

import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;

public interface EJDevBlockDisplayProperties
{
    /**
     * Returns the name of the canvas upon which this blocks data should be
     * displayed
     * <p>
     * If the canvas that was chosen is a type of TAB then the blocks data
     * should be displayed on the correct tab page
     * {@link EJDevBlockDisplayProperties#getCanvasTabPageName()}
     * <p>
     * If the canvas name has been set to <code>null</code> then null will be
     * returned. This will mean that the items will not be displayed anywhere
     * <p>
     * 
     * @return the name of the canvas upon which the blocks data will be
     *         displayed
     */
    public String getCanvasName();

    /**
     * Returns the internal name of this block
     * 
     * @return The name of this block
     */
    public String getName();

    /**
     * Returns the rendering properties for this block
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the block
     * <p>
     * 
     * @return The blocks rendering properties
     */
    public EJFrameworkExtensionProperties getBlockRendererProperties();

    /**
     * Returns the rendering properties for this blocks query screen
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the blocks query screen
     * <p>
     * 
     * @return The blocks query screen rendering properties
     */
    public EJFrameworkExtensionProperties getQueryScreenRendererProperties();

    /**
     * Returns the rendering properties for this blocks insert screen
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the blocks insert screen
     * <p>
     * 
     * @return The blocks insert screen rendering properties
     */
    public EJFrameworkExtensionProperties getInsertScreenRendererProperties();

    /**
     * Returns the rendering properties for this blocks update screen
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the blocks update screen
     * <p>
     * 
     * @return The blocks update screen rendering properties
     */
    public EJFrameworkExtensionProperties getUpdateScreenRendererProperties();

    /**
     * @return the itemContainer
     */
    public EJDevBlockItemDisplayPropertiesContainer getBlockItemDisplayContainer();

    public EJDevItemGroupDisplayPropertiesContainer getMainScreenItemGroupDisplayContainer();

    public EJDevItemGroupDisplayPropertiesContainer getUpdateScreenItemGroupDisplayContainer();

    public EJDevItemGroupDisplayPropertiesContainer getQueryScreenItemGroupDisplayContainer();

    public EJDevItemGroupDisplayPropertiesContainer getInsertScreenItemGroupDisplayContainer();

}
