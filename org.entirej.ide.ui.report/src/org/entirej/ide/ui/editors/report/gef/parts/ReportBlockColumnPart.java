package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportBlockColumnFigure;
import org.entirej.ide.ui.editors.report.gef.parts.ReportColumnScreenPart.ReportColumnScreen;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ColumnResizableEditPolicy;

public class ReportBlockColumnPart extends AbstractReportGraphicalEditPart 
{

    private ReportBlockColumnFigure reportBlockColumnFigure;


    @Override
    protected IFigure createFigure()
    {
         reportBlockColumnFigure = new ReportBlockColumnFigure(getModel());
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
    
    
    public List<?> getModelChildren()
    {
        ArrayList<Object> list= new ArrayList<Object>();
        
        EJPluginReportColumnProperties model = getModel();
        
       
        EJPluginReportScreenProperties layoutScreenProperties = model.getBlockProperties().getLayoutScreenProperties();
        list.add(new ReportColumnScreen("[header]",layoutScreenProperties.getHeaderColumnHeight(),model.getDetailScreen().getWidth(),model.getHeaderScreen(),!model.isShowHeader()));
       
        list.add(new ReportColumnScreen("[detail]",layoutScreenProperties.getDetailColumnHeight(),model.getDetailScreen().getWidth(),model.getDetailScreen(),false));
        
        if(model.isShowFooter())
            list.add(new ReportColumnScreen("[footer]",layoutScreenProperties.getFooterColumnHeight(),model.getDetailScreen().getWidth(),model.getFooterScreen(),!model.isShowFooter()));
        
       
        return list;
    }




}
