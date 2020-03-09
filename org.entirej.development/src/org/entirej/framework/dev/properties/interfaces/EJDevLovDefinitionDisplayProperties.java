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

import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;

public interface EJDevLovDefinitionDisplayProperties
{
    /**
     * Returns the name of the Lov Definition
     * 
     * @return The name of this Lov Definition
     */
    public String getName();

    /**
     * Returns the rendering properties for this lov
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the lov
     * <p>
     * 
     * @return The lov rendering properties
     */
    public EJFrameworkExtensionProperties getLovRendererProperties();

    /**
     * Returns a container containing all the items of the Lov definition block
     * 
     * @return the itemContainer
     */
    public EJDevBlockItemDisplayPropertiesContainer getBlockItemDisplayContainer();

    /**
     * Returns a container containing all the item groups displayed on the lov.
     * Loop through the item groups to find all the displayed items. The item
     * renderer properties can be retrieved from the properties returned by the
     * {@link #getBlockItemDisplayContainer()}
     * 
     * @return All item groups displayed on this lov
     */
    public EJDevItemGroupDisplayPropertiesContainer getMainScreenItemGroupDisplayContainer();
}
