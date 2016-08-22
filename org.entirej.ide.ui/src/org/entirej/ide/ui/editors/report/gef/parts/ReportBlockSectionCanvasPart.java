package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.ide.ui.editors.report.gef.figures.ReportBlockSectionCanvasFigure;

public class ReportBlockSectionCanvasPart extends AbstractReportGraphicalEditPart
{

    public static class BlockSectionCanvas
    {

        final BlockGroup blockGroup;
        final int        width;
        final int        height;

        public BlockSectionCanvas(BlockGroup blockGroup, int width, int height)
        {
            this.blockGroup = blockGroup;
            this.width = width;
            this.height = height;
        }

        public BlockGroup getBlockGroup()
        {
            return blockGroup;
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

    private ReportBlockSectionCanvasFigure base;

    @Override
    protected IFigure createFigure()
    {
        return base =new ReportBlockSectionCanvasFigure(getModel());
    }

    @Override
    public IFigure getContentPane()
    {
        return base.getContentPane();
    }
     public DragTracker getDragTracker(Request request) {
        
        return new DragEditPartsTracker(this);
    }
    
    @Override
    protected void createEditPolicies()
    {
        // TODO Auto-generated method stub

    }

    @Override
    public BlockSectionCanvas getModel()
    {
        return (BlockSectionCanvas) super.getModel();
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
    }

    @Override
    public List<?> getModelChildren()
    {
        return (getModel().getBlockGroup().getAllBlockProperties());
    }
}
