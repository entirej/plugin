package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.properties.EJCoreReportScreenProperties;

public class ReportBlockColumnFigure extends RectangleFigure
{
    final EJPluginReportColumnProperties model;

    public ReportBlockColumnFigure(EJPluginReportColumnProperties model)
    {
        this.model = model;
        EJPluginReportScreenProperties screenProperties = model.getBlockProperties().getLayoutScreenProperties();
        setPreferredSize(model.getDetailScreen().getWidth(), screenProperties.getHeight());
        GridLayout manager = new GridLayout();
        manager.marginHeight = 0;
        manager.marginWidth = 0;
        setLayoutManager(manager);
        Label figure = new Label(model.getName());
        add(figure);
       setAlpha(200);
       // setOutline(false);
       // setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        getLayoutManager().setConstraint(figure, new GridData(
                GridData.FILL, GridData.FILL, false, false));
    }

}
