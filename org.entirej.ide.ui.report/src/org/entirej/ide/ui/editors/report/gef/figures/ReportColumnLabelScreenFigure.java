package org.entirej.ide.ui.editors.report.gef.figures;


import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.ui.editors.report.gef.parts.ReportColumnLabelScreenPart.ReportColumnLabel;

public class ReportColumnLabelScreenFigure extends RectangleFigure
{
    private static final Color GIRD_COLOR = new Color(null, 128,128,128,150);
    final EJPluginReportScreenProperties model;

    public ReportColumnLabelScreenFigure(ReportColumnLabel mo)
    {
        this.model = mo.getScreenProperties();
        setPreferredSize(model.getWidth(), 20);
        setSize(model.getWidth(), model.getHeight());
        //setBackgroundColor(GIRD_COLOR);
        setAlpha(150);
        setOutline(false);
       if(model.getScreenType()==EJReportScreenType.TABLE_LAYOUT)
        {
            FlowLayout manager = new FlowLayout(true);
            manager.setMajorSpacing(0);
            manager.setMinorSpacing(0);
            setLayoutManager(manager);
        }
    }


}
