package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Dimension;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;

public class ReportBlockColumnFigure extends RectangleFigure
{
    final EJPluginReportColumnProperties model;
    private RectangleFigure              contentPane;

    public ReportBlockColumnFigure(EJPluginReportColumnProperties model)
    {
        this.model = model;
        setPreferredSize(model.getDetailScreen().getWidth(), model.getBlockProperties().getLayoutScreenProperties().getHeight()+20);
        GridLayout manager = new GridLayout();
        manager.marginHeight = 0;
        manager.marginWidth = 0;
        setLayoutManager(manager);
        Label figure = new Label(model.getName());
        add(figure);
        figure.setPreferredSize(model.getDetailScreen().getWidth(), 8);
        figure.setMaximumSize(new Dimension(model.getDetailScreen().getWidth(), 8));
        setAlpha(150);
        
        // setOutline(false);
        // setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
        getLayoutManager().setConstraint(figure, new GridData(GridData.FILL, GridData.FILL, true, false));

        RectangleFigure rectangleFigure = contentPane = new RectangleFigure();
        add(rectangleFigure);
        getLayoutManager().setConstraint(rectangleFigure, new GridData(GridData.FILL, GridData.FILL, true, true));
        rectangleFigure.setAlpha(150);

        FlowLayout submanager = new FlowLayout(false);
        submanager.setMajorSpacing(0);
        submanager.setMinorSpacing(0);
        rectangleFigure.setLayoutManager(submanager);
        
    }

    public RectangleFigure getContentPane()
    {
        return contentPane;
    }

}
