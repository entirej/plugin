package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.ui.editors.report.gef.figures.ReportColumnLabelScreenFigure;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockColumnLabelPart.ReportBlockColumnLabel;

public class ReportColumnLabelScreenPart extends AbstractReportGraphicalEditPart implements ReportSelectionProvider
{
    private ReportColumnLabelScreenFigure  base;
    @Override
    protected IFigure createFigure()
    {
        return (base = new ReportColumnLabelScreenFigure(getModel()));
    }

    @Override
    protected void createEditPolicies()
    {
       

      
    }
    
    public static class ReportColumnLabel
    {
       final EJPluginReportScreenProperties screenProperties;
        
        public ReportColumnLabel(EJPluginReportScreenProperties screenProperties)
        {
            this.screenProperties = screenProperties;
        }
        
        
        public EJPluginReportScreenProperties getScreenProperties()
        {
            return screenProperties;
        }
    }
    
    
    public Object getSelectionObject()
    {
       
        return getModel().screenProperties;
    }
    
    public EditPart getPostSelection()
    {
        return this;
    }
  
    
    @Override
    public ReportColumnLabel getModel()
    {
        return (ReportColumnLabel) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        IFigure figure = getFigure();

        AbstractReportGraphicalEditPart parent = (AbstractReportGraphicalEditPart) getParent();
        EJPluginReportScreenProperties model = getModel().screenProperties;

        Rectangle layout = new Rectangle(0, 0, model.getWidth() , 20 );
       
        parent.setLayoutConstraint(this, figure, layout);
    }

    @Override
    public List<?> getModelChildren()
    {
        ArrayList<Object> list= new ArrayList<Object>();
        
        EJPluginReportScreenProperties model = getModel().screenProperties;
        if(model.getScreenType()==EJReportScreenType.TABLE_LAYOUT)
        {
            List<EJPluginReportColumnProperties> allColumnProperties = model.getColumnContainer().getAllColumnProperties();
            for (EJPluginReportColumnProperties ejPluginReportColumnProperties : allColumnProperties)
            {
                list.add(new ReportBlockColumnLabel(ejPluginReportColumnProperties));
            }
        }
        return list;
    }
    
    
    public DragTracker getDragTracker(Request request) {
        
        return new DragEditPartsTracker(this);
    }

}
