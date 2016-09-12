package org.entirej.ide.ui.editors.report.gef.figures;


import org.eclipse.draw2d.GridLayout;
import org.eclipse.draw2d.Label;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.swt.graphics.Color;
import org.entirej.ide.ui.editors.report.gef.parts.ReportColumnScreenPart.ReportColumnScreen;

public class ReportColumnScreenFigure extends RectangleFigure
{
    private static final Color GIRD_COLOR = new Color(null, 224, 224, 224,150);
    
    public ReportColumnScreenFigure(ReportColumnScreen model)
    {
        setPreferredSize(model.getWidth(), model.getHeight());
        setSize(model.getWidth(), model.getHeight());
        setBackgroundColor(GIRD_COLOR);
        
        GridLayout manager = new GridLayout();
        manager.marginHeight = 0;
        manager.marginWidth = 0;
        setAlpha(150);
        setLayoutManager(manager);
        Label figure = new Label(model.getText());
        add(figure);
    }


}
