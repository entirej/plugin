package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.IFigure;
import org.eclipse.gef.CompoundSnapToHelper;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.Request;
import org.eclipse.gef.SnapToGeometry;
import org.eclipse.gef.SnapToGuides;
import org.eclipse.gef.SnapToHelper;
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
        return base = new ReportBlockSectionCanvasFigure(getModel());
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
    public Object getAdapter(Class key)
    {
        if (key == SnapToHelper.class)
        {
            List<SnapToHelper> snapStrategies = new ArrayList<SnapToHelper>();
            SnapToGuides snapToGuides = new SnapToGuides(this);
            snapStrategies.add(snapToGuides);
            // snapStrategies.add(new SnapToGrid(this));
            SnapToGeometry snapToGeometry = new SnapToGeometry(this);

            snapStrategies.add(snapToGeometry);
            return new CompoundSnapToHelper(snapStrategies.toArray(new SnapToHelper[0]));
        }
        return super.getAdapter(key);
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
