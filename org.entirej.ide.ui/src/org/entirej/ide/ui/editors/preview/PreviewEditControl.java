/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.preview;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;

public class PreviewEditControl extends Composite
{
    private final ScrollingGraphicalViewer viewer;
    private final AtomicBoolean            selectionEvents = new AtomicBoolean(true);
    private final PreviewSelectionHandler  selectionHandler;
    private final Map<Object, PreviewNode> sourceMap       = new HashMap<Object, PreviewNode>();

    public PreviewEditControl(Composite parent, PreviewSelectionHandler selectionHandler)
    {
        super(parent, SWT.NONE);
        this.selectionHandler = selectionHandler;
        setLayout(new FillLayout());

        viewer = new ScrollingGraphicalViewer();
        viewer.createControl(this);
        viewer.getControl().setBackground(ColorConstants.listBackground);
        viewer.setEditPartFactory(new PreviewEditPartFactory());

        EditDomain editDomain = new EditDomain();
        editDomain.addViewer(viewer);

        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {
            public void selectionChanged(SelectionChangedEvent event)
            {
                if (!selectionEvents.get() || PreviewEditControl.this.selectionHandler == null)
                {
                    return;
                }

                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof AbstractGraphicalEditPart)
                {
                    Object model = ((AbstractGraphicalEditPart) firstElement).getModel();
                    if (model instanceof PreviewNode)
                    {
                        PreviewEditControl.this.selectionHandler.select(((PreviewNode) model).getSource());
                    }
                }
            }
        });
    }

    public void setModel(PreviewNode model)
    {
        sourceMap.clear();
        index(model);
        try
        {
            selectionEvents.set(false);
            viewer.setContents(model);
        }
        finally
        {
            selectionEvents.set(true);
        }
    }

    public void selectSource(Object source)
    {
        if (source == null)
        {
            return;
        }
        final PreviewNode node = sourceMap.get(source);
        if (node == null || !isVisible(node) || viewer.getControl() == null || viewer.getControl().isDisposed())
        {
            return;
        }

        viewer.getControl().getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                if (viewer.getControl() == null || viewer.getControl().isDisposed())
                {
                    return;
                }
                Object editPart = viewer.getEditPartRegistry().get(node);
                if (editPart instanceof EditPart)
                {
                    try
                    {
                        selectionEvents.set(false);
                        viewer.setSelection(new StructuredSelection(editPart));
                        viewer.reveal((EditPart) editPart);
                    }
                    finally
                    {
                        selectionEvents.set(true);
                    }
                }
            }
        });
    }

    public EditPartViewer getViewer()
    {
        return viewer;
    }

    private void index(PreviewNode node)
    {
        if (node == null)
        {
            return;
        }
        if (node.getSource() != null)
        {
            sourceMap.put(node.getSource(), node);
        }
        if (node.getAliasSource() != null && !sourceMap.containsKey(node.getAliasSource()))
        {
            sourceMap.put(node.getAliasSource(), node);
        }
        for (PreviewNode child : node.getChildren())
        {
            index(child);
        }
    }

    private boolean isVisible(PreviewNode node)
    {
        PreviewNode child = node;
        PreviewNode parent = child.getParent();
        while (parent != null)
        {
            if (!parent.getVisibleChildren().contains(child))
            {
                return false;
            }
            child = parent;
            parent = child.getParent();
        }
        return true;
    }
}
