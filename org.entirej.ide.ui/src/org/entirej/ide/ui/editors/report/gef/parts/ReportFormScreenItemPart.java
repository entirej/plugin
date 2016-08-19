package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportFormScreenItemFigure;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ScreenItemResizableEditPolicy;

public class ReportFormScreenItemPart extends AbstractReportGraphicalEditPart
{

    public ReportFormScreenItemPart()
    {
        // TODO Auto-generated constructor stub
    }
    @Override
    protected IFigure createFigure()
    {
        return new ReportFormScreenItemFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
                new ScreenItemResizableEditPolicy());

    }

    @Override
    public EJPluginReportScreenItemProperties getModel()
    {
        return (EJPluginReportScreenItemProperties) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        IFigure figure = getFigure();

        AbstractGraphicalEditPart parent = (AbstractGraphicalEditPart) getParent();
        EJPluginReportScreenItemProperties model = getModel();

        Rectangle layout = new Rectangle(model.getX(), model.getY(), model.getWidth(), model.getHeight());
        parent.setLayoutConstraint(this, figure, layout);
    }
    
    public DragTracker getDragTracker(Request request) {
        
        return new DragEditPartsTracker(this){@Override
        protected EditPart getTargetEditPart()
        {
            return getParent();
        }};
    }

}
