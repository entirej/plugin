package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportFormScreenFigure;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ScreenResizableEditPolicy;

public class ReportFormScreenPart extends AbstractReportGraphicalEditPart
{
    private ReportFormScreenFigure  base;
    @Override
    protected IFigure createFigure()
    {
        return (base = new ReportFormScreenFigure(getModel()));
    }

    @Override
    protected void createEditPolicies()
    {
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE,
                new ScreenResizableEditPolicy());

    }

    public ReportFormScreenFigure getFigureBase()
    {
        return base;
    }
    
    @Override
    public EJPluginReportScreenProperties getModel()
    {
        return (EJPluginReportScreenProperties) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        IFigure figure = getFigure();

        ReportFormScreenCanvasPart parent = (ReportFormScreenCanvasPart) getParent();
        EJPluginReportScreenProperties model = getModel();

        Rectangle layout = new Rectangle(5, 5, model.getWidth() , model.getHeight() );
        parent.setLayoutConstraint(this, figure, layout);
    }

    @Override
    public List<?> getModelChildren()
    {
        Collection<?> screenItems = getModel().getScreenItems();

        return new ArrayList<Object>(screenItems);
    }
    @Override
    public IFigure getContentPane()
    {
        return base.getContentPane();
    }
    
    public DragTracker getDragTracker(Request request) {
        
        return new DragEditPartsTracker(this);
    }

}
