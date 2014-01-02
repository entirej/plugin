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
package org.entirej.framework.dev.renderer.definition.interfaces;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.framework.core.renderers.definitions.interfaces.EJItemRendererDefinition;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.EJDevItemRendererDefinitionControl;

public interface EJDevItemRendererDefinition extends EJItemRendererDefinition
{

    /**
     * Used to return the control for this item
     * <p>
     * The item should be added to the given <code>Control</code> and returned
     * from this method
     * 
     * @param parent
     *            The <code>Composite</code> that will contain this item
     * @param screenDisplayProperties
     *            The screen properties for the screen upon which this item will
     *            be displayed
     * @param formToolkit
     *            The toolkit to use for the creation of the item widget
     * @return This items, plugin GUI widget
     */
    public EJDevItemRendererDefinitionControl getItemControl(EJDevScreenItemDisplayProperties screenDisplayProperties, Composite parent, FormToolkit formToolkit);

    /**
     * Used to return the label widget for this item
     * <p>
     * If the widget does not display a label, then this method should do
     * nothing and <code>null</code> should be returned
     * 
     * @param parent
     *            The <code>Composite</code> upon wich this widgets label will
     *            be displayed
     * @param screemDisplayProperties
     *            The display properties of this item
     * @param formToolkit
     *            The toolkit to use for the creation of the label widget
     * @return The label widget or <code>null</code> if this item displays no
     *         label
     */
    public Control getLabelControl(EJDevScreenItemDisplayProperties itemProperties, Composite parent, FormToolkit toolkit);
}
