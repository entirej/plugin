package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportBlockColumnFigure;

public class ReportBlockColumnPart extends AbstractReportGraphicalEditPart 
{

    @Override
    protected IFigure createFigure()
    {
        return new ReportBlockColumnFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
       // installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new ReportBlockResizableEditPolicy());

    }

    @Override
    public EJPluginReportColumnProperties getModel()
    {
        return (EJPluginReportColumnProperties) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        IFigure figure = getFigure();

        AbstractGraphicalEditPart parent = (AbstractGraphicalEditPart) getParent();
        EJPluginReportColumnProperties model = getModel();
        //FIXME
//        final EJPluginReportScreenProperties screenProperties = model.getLayoutScreenProperties();
//
//        int width = screenProperties.getWidth();
//        int height = screenProperties.getHeight();
//
//        Rectangle layout = new Rectangle(screenProperties.getX(), screenProperties.getY(), width, height);
//        parent.setLayoutConstraint(this, figure, layout);
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
