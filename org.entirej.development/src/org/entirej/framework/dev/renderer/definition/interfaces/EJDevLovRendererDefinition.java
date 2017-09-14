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
import org.entirej.framework.core.renderers.definitions.interfaces.EJLovRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJQueryScreenRendererDefinition;
import org.entirej.framework.dev.properties.interfaces.EJDevLovDefinitionDisplayProperties;
import org.entirej.framework.dev.renderer.definition.EJDevLovRendererDefinitionControl;

public interface EJDevLovRendererDefinition extends EJLovRendererDefinition
{
    /**
     * Used to add an lov renderer to the given <code>Control</code>
     * <p>
     * This renderer will be used in the EntireJ Form Plugin and not within the
     * runtime application. It acts as a development aid, providing developers
     * with a real-time preview of the renderer
     * 
     * @param lovDisplayProperties
     *            The display properties for this lov
     * @param parent
     *            The canvas upon which this lov should be displayed
     * @param formToolkit
     *            The toolkit to use for the creation of the lov widget
     * @return The lov renderer definition control for this block
     */
    public EJDevLovRendererDefinitionControl addLovControlToCanvas(EJDevLovDefinitionDisplayProperties lovDisplayProperties, Composite parent,
            FormToolkit formToolkit);

    /**
     * If the lov renderer allows a user query, then a
     * <code>{@link EJQueryScreenRendererDefinition}</code> is required. This
     * will be used to display the correct properties and viewer within the EJ
     * Plugin
     * 
     * @return The <code>{@link EJQueryScreenRendererDefinition}</code> for this
     *         Lov Renderer or <code>null</code> if the lov does not allow a
     *         user query
     */
    public EJDevQueryScreenRendererDefinition getQueryScreenRendererDefinition();
}
