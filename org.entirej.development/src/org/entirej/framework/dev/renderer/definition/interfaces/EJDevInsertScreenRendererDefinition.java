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
package org.entirej.framework.dev.renderer.definition.interfaces;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.framework.core.renderers.definitions.interfaces.EJInsertScreenRendererDefinition;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.EJDevItemRendererDefinitionControl;
import org.entirej.framework.dev.renderer.definition.EJDevScreenRendererDefinitionControl;

public interface EJDevInsertScreenRendererDefinition extends EJInsertScreenRendererDefinition
{
    /**
     * Used to add an Insert Screen Widget to the given <code>Control</code>
     * <p>
     * This renderer will be used in the EntireJ Form Plugin and not within the
     * runtime application. It acts as a development aid, providing developers
     * with a real-time preview of the renderer
     * 
     * @param blockDisplayProperties
     *            The block display properties containing all information
     *            required for the creation of the insert screen widget
     * @param parent
     *            The canvas upon which this widget will be displayed
     * @param formToolkit
     *            The toolkit to use for the creation of the item widget
     * @param The
     *            Screen renderer definition control for this insert screen
     */
    public EJDevScreenRendererDefinitionControl addInsertScreenControl(EJDevBlockDisplayProperties blockDisplayProperties, Composite parent,
            FormToolkit formToolkit);

    /**
     * Used to return the control for a spacer item using the specified display
     * properties
     * <p>
     * The spacer item should be added to the given <code>Control</code> and
     * returned from this method
     * 
     * @param parent
     *            The <code>Composite</code> that will contain this item
     * @param screenDisplayProperties
     *            The screen item properties which contains the display
     *            properties for the spacer item to be created
     * @param formToolkit
     *            The toolkit to use for the creation of the spacer item widget
     * @return The spacer items plugin GUI widget
     */
    public EJDevItemRendererDefinitionControl getSpacerItemControl(EJDevScreenItemDisplayProperties screenDisplayProperties, Composite parent,
            FormToolkit formToolkit);
}
