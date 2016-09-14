package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.FlowLayout;
import org.eclipse.draw2d.GridData;
import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockColumnLabelPart.ReportBlockColumnLabel;

public class ReportBlockColumnLabelFigure extends RectangleFigure
{
    final EJPluginReportColumnProperties model;
    private RectangleFigure              contentPane;

    private static final Color GIRD_COLOR = new Color(null, 128,128,128,150);
    
    
    public ReportBlockColumnLabelFigure(ReportBlockColumnLabel wrap)
    {
        this.model = wrap.getColumnProperties();
        setPreferredSize(model.getDetailScreen().getWidth(), 20);
        GridLayout manager = new GridLayout();
        manager.marginHeight = 0;
        manager.marginWidth = 0;
        setLayoutManager(manager);
        Label figure = new Label(model.getName());
        add(figure);
        setAlpha(150);
        
        // setOutline(false);
         setBackgroundColor(GIRD_COLOR);
        getLayoutManager().setConstraint(figure, new GridData(GridData.FILL, GridData.FILL, true, true));

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
