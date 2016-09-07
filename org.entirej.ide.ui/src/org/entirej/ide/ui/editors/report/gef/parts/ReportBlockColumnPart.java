package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportBlockColumnFigure;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ColumnResizableEditPolicy;

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
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new ColumnResizableEditPolicy());

    }

    @Override
    public EJPluginReportColumnProperties getModel()
    {
        return (EJPluginReportColumnProperties) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        EJPluginReportColumnProperties model = getModel();
        EJPluginReportScreenProperties screenProperties = model.getBlockProperties().getLayoutScreenProperties();
        ((ReportBlockColumnFigure)getFigure()).setPreferredSize(model.getDetailScreen().getWidth(), screenProperties.getHeight());
      getParent().refresh();
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
