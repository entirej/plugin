package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.figures.ReportFormScreenCanvasFigure;

public class ReportFormScreenCanvasPart extends AbstractGraphicalEditPart
{

    public static class ReportFormScreenCanvas
    {
        final int width;
        final int height;
        final EJPluginReportScreenProperties screenProperties;

        public ReportFormScreenCanvas(EJPluginReportScreenProperties screenProperties,int width,int height)
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
        return new ReportFormScreenCanvasFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public ReportFormScreenCanvas getModel()
    {
        return (ReportFormScreenCanvas) super.getModel();
    }
    
    @Override
    protected void refreshVisuals()
    {
        
        super.refreshVisuals();
        ZoomManager zoomManager = (ZoomManager) getViewer().getProperty(ZoomManager.class.toString());
        if(zoomManager!=null)
        {
            zoomManager.setZoom(1);
        }
    }

    
    @Override
    public List<?> getModelChildren()
    {
        return Arrays.asList(getModel().screenProperties);
    }
}
