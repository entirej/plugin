package org.entirej.ide.ui.editors.report.gef.figures;


import org.eclipse.draw2d.LayeredPane;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;

public class ReportFormScreenFigure extends RectangleFigure
{
    private static final Color GIRD_COLOR = new Color(null, 224, 224, 224,150);
    final EJPluginReportScreenProperties model;

    public ReportFormScreenFigure(EJPluginReportScreenProperties model)
    {
        this.model = model;
        setPreferredSize(model.getWidth(), model.getHeight());
        setSize(model.getWidth(), model.getHeight());
        setBackgroundColor(GIRD_COLOR);
        setAlpha(150);
        setLayoutManager(new XYLayout());
    }


}
