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

/**
 * Contains the properties required for the insert screen
 * <p>
 * EntireJ requires the insert screen to be displayed as a grid. Meaning each
 * item and the items label will be placed within a part of the grid using X and
 * Y coordinates.
 */
public interface EJDevInsertScreenItemDisplayProperties extends EJDevScreenItemDisplayProperties
{

    /**
     * Returns the <code>RenderingProperties</code> that are required by the
     * blocks insert screen renderer
     * 
     * @return The required blocks insert screen renderer properties
     */

    public EJFrameworkExtensionProperties getInsertScreenRendererRequiredProperties();
}
