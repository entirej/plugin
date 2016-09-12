package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;

public class ReportBlockColumnFigure extends RectangleFigure
{
    final EJPluginReportColumnProperties model;

    public ReportBlockColumnFigure(EJPluginReportColumnProperties model)
    {
        this.model = model;

        Label figure = new Label(model.getName());
        add(figure);
       setAlpha(200);
       FlowLayout manager = new FlowLayout(false);
       manager.setMajorSpacing(0);
       manager.setMinorSpacing(0);
       setLayoutManager(manager);
    }

}
