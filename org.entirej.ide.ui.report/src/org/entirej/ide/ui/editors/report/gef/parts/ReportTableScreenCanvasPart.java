package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.ZoomManager;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportTableScreenCanvasFigure;
import org.entirej.ide.ui.editors.report.gef.parts.ReportColumnLabelScreenPart.ReportColumnLabel;

public class ReportTableScreenCanvasPart extends AbstractReportGraphicalEditPart
{
    ReportTableScreenCanvasFigure canvasFigure;

    public static class ReportTableScreenCanvas
    {
        final int width;
        final int height;
        
        final EJPluginReportScreenProperties screenProperties;
       
        public ReportTableScreenCanvas(EJPluginReportScreenProperties screenProperties,int width,int height)
        {
            this.screenProperties = screenProperties;
            this.width = width;
            this.height = height;
        }
        
        public EJPluginReportScreenProperties getScreenProperties()
        {
            return screenProperties;
        }

        public int getWidth()
        {
            return width;
        }

        public int getHeight()
        {
            
            return height;
        }
    }

    @Override
    protected IFigure createFigure()
    {
        return canvasFigure = new ReportTableScreenCanvasFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public ReportTableScreenCanvas getModel()
    {
        return (ReportTableScreenCanvas) super.getModel();
    }
    
    @Override
    protected void refreshVisuals()
    {
        
        super.refreshVisuals();
        ZoomManager zoomManager = (ZoomManager) getViewer().getProperty(ZoomManager.class.toString());
        if(zoomManager!=null)
        {
            //zoomManager.setZoom(1);
        }
    }

    
    @Override
    public IFigure getContentPane()
    {
        return canvasFigure.getContentPane();
    }
    
    @Override
    public List<?> getModelChildren()
    {
        return Arrays.asList(new ReportColumnLabel(getModel().screenProperties) , getModel().screenProperties);
    }
}
