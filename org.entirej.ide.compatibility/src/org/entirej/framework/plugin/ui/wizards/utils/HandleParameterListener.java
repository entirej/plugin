/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 * Contributors: Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.ui.wizards.utils;

import org.eclipse.swt.widgets.Listener;

/**
 * Listener that is called when a parameter (preceded by the ':' character) is
 * about to be formatted. If <code>event.doit</code> is <code>true</code>(the
 * default value), then it is colored as ok. Otherwise it is colored as error
 * (with red).
 */
public interface HandleParameterListener extends Listener
{
    int ParameterEvent = 1025;
}
