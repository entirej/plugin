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

/**
 * Contains the properties required for the query screen
 * <p>
 * EntireJ requires the query screen to be displayed as a grid. Meaning each
 * item and the items label will be placed within a part of the grid using X and
 * Y coordinates.
 */
public interface EJDevQueryScreenItemDisplayProperties extends EJDevScreenItemDisplayProperties
{
    public EJFrameworkExtensionProperties getQueryScreenRendererRequiredProperties();

}
