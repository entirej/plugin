package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportBlockFigure;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ReportBlockResizableEditPolicy;

public class ReportBlockPart extends AbstractReportGraphicalEditPart
{

    

    @Override
    protected IFigure createFigure()
    {
        return new ReportBlockFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
       installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new ReportBlockResizableEditPolicy());

    }

    @Override
    public EJPluginReportBlockProperties getModel()
    {
        return (EJPluginReportBlockProperties) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        IFigure figure = getFigure();

        AbstractGraphicalEditPart parent = (AbstractGraphicalEditPart) getParent();
        EJPluginReportBlockProperties model = getModel();
        final EJPluginReportScreenProperties screenProperties = model.getLayoutScreenProperties();

        int width = screenProperties.getWidth();
        int height = screenProperties.getHeight();

        Rectangle layout = new Rectangle(screenProperties.getX(), screenProperties.getY(), width, height);
        parent.setLayoutConstraint(this, figure, layout);
    }

    public DragTracker getDragTracker(Request request)
    {

        return new DragEditPartsTracker(this)
        {
            @Override
            protected EditPart getTargetEditPart()
            {
                return getParent();
            }
        };
    }
}
