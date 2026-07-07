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

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.MouseEvent;
import org.eclipse.draw2d.MouseListener;
import org.eclipse.draw2d.geometry.Point;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.jface.viewers.StructuredSelection;

public class PreviewNodePart extends AbstractGraphicalEditPart
{
    @Override
    protected IFigure createFigure()
    {
        PreviewNodeFigure figure = new PreviewNodeFigure(getModel());
        figure.addMouseListener(new MouseListener.Stub()
        {
            @Override
            public void mousePressed(MouseEvent event)
            {
                handleMousePressed(event);
            }
        });
        return figure;
    }

    @Override
    public IFigure getContentPane()
    {
        return getFigure();
    }

    @Override
    protected void createEditPolicies()
    {
        // Read-only preview for the first GEF migration slice.
    }

    @Override
    public PreviewNode getModel()
    {
        return (PreviewNode) super.getModel();
    }

    @Override
    protected List<?> getModelChildren()
    {
        return getModel().getVisibleChildren();
    }

    @Override
    protected void refreshVisuals()
    {
        PreviewNode model = getModel();
        PreviewNodeFigure figure = (PreviewNodeFigure) getFigure();
        figure.setModel(model);
        figure.setPreferredSize(model.getBounds().getWidth(), model.getBounds().getHeight());
        figure.setSize(model.getBounds().getWidth(), model.getBounds().getHeight());

        EditPart parent = getParent();
        if (parent instanceof GraphicalEditPart && parent.getModel() instanceof PreviewNode)
        {
            PreviewBounds bounds = model.getBounds();
            PreviewBounds parentBounds = ((PreviewNode) parent.getModel()).getBounds();
            Rectangle constraint = new Rectangle(bounds.getX() - parentBounds.getX(), bounds.getY() - parentBounds.getY(), bounds.getWidth(),
                    bounds.getHeight());
            ((GraphicalEditPart) parent).setLayoutConstraint(this, figure, constraint);
        }
    }

    private void handleMousePressed(MouseEvent event)
    {
        PreviewNode model = getModel();
        if (model.getKind() == null)
        {
            return;
        }

        PreviewNodeFigure figure = (PreviewNodeFigure) getFigure();
        Point location = new Point(event.x, event.y);
        figure.translateToRelative(location);
        int tabIndex = figure.getTabIndexAt(location.x, location.y);
        if (tabIndex < 0)
        {
            return;
        }

        model.setSelectedTabIndex(tabIndex);
        new PreviewGridLayoutSolver().layout(model.getRoot());
        refreshRootPart();
        selectActiveTabChild();
    }

    private void refreshRootPart()
    {
        EditPart part = this;
        while (part.getParent() instanceof PreviewNodePart)
        {
            part = part.getParent();
        }
        if (part instanceof PreviewNodePart)
        {
            ((PreviewNodePart) part).refreshSubtree();
        }
    }

    private void refreshSubtree()
    {
        refreshVisuals();
        refreshChildren();
        for (Object child : getChildren())
        {
            if (child instanceof PreviewNodePart)
            {
                ((PreviewNodePart) child).refreshSubtree();
            }
        }
    }

    private void selectActiveTabChild()
    {
        PreviewNode activeChild = getModel().getSelectedTabChild();
        if (activeChild == null)
        {
            return;
        }

        Object editPart = getViewer().getEditPartRegistry().get(activeChild);
        if (editPart instanceof EditPart)
        {
            getViewer().setSelection(new StructuredSelection(editPart));
        }
    }
}
