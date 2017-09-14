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

public interface EJDevBlockItemDisplayPropertiesContainer
{
    public boolean containsItemProperty(String name);

    /**
     * Returns a Map of <code>EJDevBlockItemDisplayProperties</code> contained
     * within this block. The key of the map is the name of the item in
     * uppercase.
     * 
     * @return A set containing all EJDevBlockItemDisplayProperties contained
     *         within this block.
     */
    public Collection<EJDevBlockItemDisplayProperties> getAllItemDisplayProperties();

    /**
     * Indicates if there is an item in this container with the given name
     * 
     * @param itemName
     *            The name of the item to check for
     * @return true if the item exists otherwise false
     */
    public boolean contains(String itemName);

    /**
     * Get an <code>EJDevBlockItemDisplayProperties</code> object for a given
     * item. The item property store within the <code>IBlockProperties</code>
     * will be searched for the given item name. A case insensitive query will
     * be made.
     * 
     * @param itemName
     *            The name of the item to search for
     * @return The properties of the given item or null if an invalid or
     *         nonexistent item name was passed
     */
    public EJDevBlockItemDisplayProperties getItemProperties(String itemName);

}
