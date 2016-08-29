package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
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
    
    @Override
    public Object getAdapter(Class key)
    {
        if (key == SnapToHelper.class) {
            List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();
            SnapToGuides snapToGuides = new SnapToGuides(this);
            snapStrategies.add(snapToGuides);
            //snapStrategies.add(new SnapToGrid(this));
            SnapToGeometry snapToGeometry = new SnapToGeometry(this);
           
            snapStrategies.add(snapToGeometry);
            return new CompoundSnapToHelper(snapStrategies.toArray(new SnapToHelper[0]));
        }
        return super.getAdapter(key);
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

        AbstractReportGraphicalEditPart parent = (AbstractReportGraphicalEditPart) getParent();
        EJPluginReportScreenProperties model = getModel();

        Rectangle layout = new Rectangle(0, 0, model.getWidth() , model.getHeight() );
        parent.setLayoutConstraint(this, figure, layout);
    }

    @Override
    public List<?> getModelChildren()
    {
        ArrayList<Object> list= new ArrayList<Object>();
        Collection<?> screenItems = getModel().getScreenItems();
        list.addAll(screenItems);
        
        List<EJPluginReportBlockProperties> allSubBlocks = getModel().getAllSubBlocks();
        list.addAll(allSubBlocks);
        return list;
    }
    
    
    public DragTracker getDragTracker(Request request) {
        
        return new DragEditPartsTracker(this);
    }

}
