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
package org.entirej.ide.ui.editors.handlers;

import java.util.Iterator;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;

public class NodeElementDeleteHandler implements PageActionHandler
{

    private final ISelectionProvider selectionProvider;

    public NodeElementDeleteHandler(ISelectionProvider selectionProvider)
    {
        this.selectionProvider = selectionProvider;
    }

    public boolean isEnable()
    {
        ISelection selection = selectionProvider.getSelection();

        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection strutruredSelection = (IStructuredSelection) selection;
            if (strutruredSelection.isEmpty())
                return false;

            for (Iterator<?> iterator = strutruredSelection.iterator(); iterator.hasNext();)
            {
                Object type = iterator.next();
                if (!(type instanceof AbstractNode) || ((AbstractNode<?>) type).getDeleteProvider() == null)
                    return false;
            }

            return true;

        }

        return false;
    }

    public void excecute(AbstractEditor editor)
    {
        ISelection selection = selectionProvider.getSelection();

        if (selection instanceof IStructuredSelection)
        {
            IStructuredSelection strutruredSelection = (IStructuredSelection) selection;

            Object[] elements = strutruredSelection.toArray();
            for (int i = 0; i < elements.length; i++)
            {
                Object element = elements[i];
                if (element instanceof AbstractNode<?>)
                {
                    AbstractNode<?> node = (AbstractNode<?>) element;
                    if (node != null )
                    {
                        INodeDeleteProvider deleteProvider = node.getDeleteProvider();
                        AbstractOperation deleteOperation = deleteProvider.deleteOperation(true);
                        if (deleteOperation == null)
                        {
                            System.err.println("INodeDeleteProvider.deleteOperation : not implements" + node.getClass());
                            node.getDeleteProvider().delete(true);
                        }
                        else
                        {
                            editor.execute(deleteOperation);
                        }
                    }
                }
            }

        }

    }
}
