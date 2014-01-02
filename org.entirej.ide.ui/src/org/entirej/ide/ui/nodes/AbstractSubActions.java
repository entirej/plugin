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
package org.entirej.ide.ui.nodes;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.resource.ImageDescriptor;

public abstract class AbstractSubActions extends Action
{

    public AbstractSubActions()
    {
        super();
    }

    public AbstractSubActions(String text, ImageDescriptor image)
    {
        super(text, image);
    }

    public AbstractSubActions(String text, int style)
    {
        super(text, style);
    }

    public AbstractSubActions(String text)
    {
        super(text);
    }

    public abstract Action[] getActions();
}
