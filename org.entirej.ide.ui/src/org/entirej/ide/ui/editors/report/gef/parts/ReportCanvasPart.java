package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.Arrays;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.ide.ui.editors.report.gef.figures.ReportCanvsFigure;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockSectionCanvasPart.BlockSectionCanvas;

public class ReportCanvasPart extends AbstractReportGraphicalEditPart
{

    public static class ReportCanvas
    {
       final int width,height;

        final List<BlockSectionCanvas> canvas;
        public ReportCanvas(int width,int height,BlockSectionCanvas... canvs)
        {
            canvas = Arrays.asList(canvs);
            this.width = width;
            this.height = height;
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

    private ReportCanvsFigure base;

    @Override
    protected IFigure createFigure()
    {
        return base = new ReportCanvsFigure();
    }

    @Override
    public IFigure getContentPane()
    {
        return base.getContentPane();
    }

    public DragTracker getDragTracker(Request request)
    {

        return new DragEditPartsTracker(this);
    }

    @Override
    protected void createEditPolicies()
    {
        // TODO Auto-generated method stub

    }

 

    @Override
    public ReportCanvas getModel()
    {
        return (ReportCanvas) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {

        super.refreshVisuals();
        ZoomManager zoomManager = (ZoomManager) getViewer().getProperty(ZoomManager.class.toString());
        if (zoomManager != null)
        {
            // zoomManager.setZoom(1);
        }
        ReportCanvas model = getModel();
        base.setPreferredSize(model.getWidth(), model.getHeight());
        base.setSize(model.getWidth(), model.getHeight());
    }

    @Override
    public List<?> getModelChildren()
    {
        return (getModel().canvas);
    }
}
