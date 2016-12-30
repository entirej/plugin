package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportBlockColumnLabelFigure;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ColumnResizableEditPolicy;

public class ReportBlockColumnLabelPart extends AbstractReportGraphicalEditPart  implements ReportSelectionProvider
{

    private ReportBlockColumnLabelFigure reportBlockColumnFigure;

    
    public static class ReportBlockColumnLabel
    {
        private final  EJPluginReportColumnProperties columnProperties;
        public ReportBlockColumnLabel(EJPluginReportColumnProperties columnProperties)
        {
           this.columnProperties = columnProperties;
        }
        
        public EJPluginReportColumnProperties getColumnProperties()
        {
            return columnProperties;
        }
    }
    
    
    public Object getSelectionObject()
    {
        return getModel().columnProperties;
    }
    
    public EditPart getPostSelection()
    {
        return this;
    }

    @Override
    protected IFigure createFigure()
    {
         reportBlockColumnFigure = new ReportBlockColumnLabelFigure(getModel());
        return reportBlockColumnFigure;
    }

    @Override
    protected void createEditPolicies()
    {
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new ColumnResizableEditPolicy());

    }
    
    @Override
    public IFigure getContentPane()
    {
        return reportBlockColumnFigure.getContentPane();
    }

    @Override
    public ReportBlockColumnLabel getModel()
    {
        return (ReportBlockColumnLabel) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        EJPluginReportColumnProperties model = getModel().columnProperties;
        ((ReportBlockColumnLabelFigure)getFigure()).setPreferredSize(model.getDetailScreen().getWidth(), 20);
      
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
