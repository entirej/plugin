package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.FreeformLayer;
import org.eclipse.draw2d.FreeformLayeredPane;
import org.eclipse.draw2d.FreeformLayout;
import org.eclipse.draw2d.FreeformViewport;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.gef.LayerConstants;
import org.eclipse.gef.editparts.GridLayer;
import org.eclipse.gef.editparts.GuideLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;

public class ReportFormScreenFigure extends FreeformViewport
{
    final EJPluginReportScreenProperties model;
    private LayeredPane                  innerLayers;
    private LayeredPane                  printableLayers;

    public ReportFormScreenFigure(EJPluginReportScreenProperties model)
    {
        this.model = model;
        innerLayers = new FreeformLayeredPane();
        createLayers(innerLayers);
        setContents(innerLayers);

       

    }

  
    protected void createLayers(LayeredPane layeredPane)
    {
        layeredPane.add(createGridLayer(), LayerConstants.GRID_LAYER);
        layeredPane.add(getPrintableLayers(), LayerConstants.PRINTABLE_LAYERS);
        layeredPane.add(new FreeformLayer(), LayerConstants.HANDLE_LAYER);
        layeredPane.add(new FeedbackLayer(), LayerConstants.FEEDBACK_LAYER);
        layeredPane.add(new GuideLayer(), LayerConstants.GUIDE_LAYER);
    }

    /**
     * Creates a layered pane and the layers that should be printed.
     * 
     * @see org.eclipse.gef.print.PrintGraphicalViewerOperation
     * @return a new LayeredPane containing the printable layers
     */
    protected LayeredPane createPrintableLayers()
    {
        FreeformLayeredPane layeredPane = new FreeformLayeredPane();
        FreeformLayer figure = new FreeformLayer();
        figure.setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND));

        figure.setBorder(new LineBorder(null, 1, Graphics.LINE_DASH));
        figure.setLayoutManager(new FreeformLayout());
        layeredPane.add(figure, LayerConstants.PRIMARY_LAYER);
        // layeredPane.add(new ConnectionLayer(),
        // LayerConstants.CONNECTION_LAYER);
        return layeredPane;
    }

    /**
     * The contents' Figure will be added to the PRIMARY_LAYER.
     * 
     * @see org.eclipse.gef.GraphicalEditPart#getContentPane()
     */
    public IFigure getContentPane()
    {
        return getLayer(LayerConstants.PRIMARY_LAYER);
    }

    public IFigure getLayer(Object key)
    {
        if (innerLayers == null)
            return null;
        IFigure layer = innerLayers.getLayer(key);
        if (layer != null)
            return layer;
        if (printableLayers == null)
            return null;
        return printableLayers.getLayer(key);
    }

    protected GridLayer createGridLayer()
    {
        return new GridLayer();
    }

    public LayeredPane getPrintableLayers()
    {
        if (printableLayers == null)
            printableLayers = createPrintableLayers();
        return printableLayers;
    }

    class FeedbackLayer extends FreeformLayer
    {
        FeedbackLayer()
        {
            setEnabled(false);
        }
    }

}
