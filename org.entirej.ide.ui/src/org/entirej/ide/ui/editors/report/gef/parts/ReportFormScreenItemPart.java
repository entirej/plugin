package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportFormScreenItemFigure;

public class ReportFormScreenItemPart extends AbstractGraphicalEditPart
{

    @Override
    protected IFigure createFigure()
    {
        return new ReportFormScreenItemFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
        // TODO Auto-generated method stub

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
    
    

}
