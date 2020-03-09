/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.nodes.dnd;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;

public class NodeDragAdapter implements DragSourceListener
{

    protected Viewer               viewer;

    protected IStructuredSelection selection;

    public NodeDragAdapter(Viewer viewer)
    {
        super();
        this.viewer = viewer;
    }

    public void dragStart(DragSourceEvent event)
    {
        selection = (IStructuredSelection) viewer.getSelection();
        // support one selection only
        if (selection.size() == 0)
        {
            event.doit = false;
            return;
        }

        Object[] elements = selection.toArray();

        for (int i = 0; i < elements.length; i++)
        {
            Object element = elements[i];
            event.doit = element != null && element instanceof NodeMoveProvider.Movable && ((NodeMoveProvider.Movable) element).canMove();
            if (!event.doit)
                break;
        }

        if (event.doit)
            NodeTransfer.getInstance().javaToNative(elements, event.dataType);
    }

    public void dragFinished(DragSourceEvent event)
    {
        selection = null;
        NodeTransfer.getInstance().javaToNative(null, null);
    }

    public void dragSetData(DragSourceEvent event)
    {
        if (NodeTransfer.getInstance().isSupportedType(event.dataType))
        {
            event.data = selection.toArray();
        }
    }
}
