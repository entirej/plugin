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
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;

/**
 * Contains all item properties. These properties should be used to define the
 * actions that are allowed to be performed upon this item and how the item
 * should look.
 */
public interface EJDevBlockItemDisplayProperties
{

    /**
     * Indicates if this item belongs to a control block
     * 
     * @return <code>true</code> if this item belongs to a control block
     *         otherwise <code>false</code>
     */
    public boolean belongsToControlBlock();

    /**
     * Returns the name of the item
     * <p>
     * If this is a base table item, meaning that it will receive its data
     * directly from the blocks data source, then the name of the item will also
     * be the name of the property within the blocks base object.
     * 
     * @return The name of this item
     */
    public String getName();

    /**
     * Indicates if this item is mandatory
     * <p>
     * This value will be used when adding the item to either the insert or
     * Update screens, otherwise the property has no use
     * 
     * @return <code>true</code> if this is a mandatory item, otherwise
     *         <code>false</code>
     */
    public boolean isMandatoryItem();

    /**
     * The name of the renderer used for display this item
     * <p>
     * All renderers are defined within the <b>EntireJ Properties</b>
     * 
     * @return the name of this items renderer
     */
    public String getItemRendererName();

    /**
     * Returns the <code>RenderingProperties</code> that are required by the
     * <code>ItemRenderer</code>
     * 
     * @return The required item renderer properties for this item
     */
    public EJFrameworkExtensionProperties getItemRendererProperties();

    /**
     * Returns the item renderer definition for this item
     * 
     * @return The item renderer definition for this item
     */
    public EJDevItemRendererDefinition getItemRendererDefinition();

}
