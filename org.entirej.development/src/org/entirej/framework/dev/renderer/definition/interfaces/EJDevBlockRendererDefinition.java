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
import org.entirej.framework.core.properties.interfaces.EJMainScreenProperties;
import org.entirej.framework.core.renderers.definitions.interfaces.EJBlockRendererDefinition;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.EJDevBlockRendererDefinitionControl;
import org.entirej.framework.dev.renderer.definition.EJDevItemRendererDefinitionControl;

public interface EJDevBlockRendererDefinition extends EJBlockRendererDefinition
{
    /**
     * Used to add a block renderer to the given <code>Control</code>
     * <p>
     * This renderer will be used in the EntireJ Form Plugin and not within the
     * runtime application. It acts as a development aid, providing developers
     * with a real-time preview of the renderer
     * 
     * @param blockDisplayProperties
     *            The display properties for this block
     * @param parent
     *            The canvas upon which this block should be displayed
     * @param formToolkit
     *            The toolkit to use for the creation of the block widget
     * @return The block renderer definition control for this block
     */
    public EJDevBlockRendererDefinitionControl addBlockControlToCanvas(EJMainScreenProperties mainScreenProperties,
            EJDevBlockDisplayProperties blockDisplayProperties, Composite parent, FormToolkit formToolkit);

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

    public EJDevQueryScreenRendererDefinition getQueryScreenRendererDefinition();

    public EJDevInsertScreenRendererDefinition getInsertScreenRendererDefinition();

    public EJDevUpdateScreenRendererDefinition getUpdateScreenRendererDefinition();

}
