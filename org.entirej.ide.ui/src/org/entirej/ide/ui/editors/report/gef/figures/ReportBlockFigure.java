package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;

public class ReportBlockFigure extends RectangleFigure
{
    final EJPluginReportBlockProperties model;

    public ReportBlockFigure(EJPluginReportBlockProperties model)
    {
        this.model = model;
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
