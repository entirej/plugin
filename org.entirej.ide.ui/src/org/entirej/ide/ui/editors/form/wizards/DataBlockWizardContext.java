/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
package org.entirej.ide.ui.editors.form.wizards;

import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public interface DataBlockWizardContext
{

    void addBlock(String blockName, EJPluginRenderer block, String canvas, boolean createCanvas, String blockService);

    List<EJPluginRenderer> getBlockRenderer();

    List<EJCanvasProperties> getCanvas();

    boolean hasBlock(String blockName);

    boolean hasCanvas(String canvasName);

    IJavaProject getProject();

    boolean supportService();

}
