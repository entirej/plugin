package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenCanvasPart.ReportFormScreenCanvas;

public class ReportFormScreenCanvasFigure extends RectangleFigure
{
    private static final Color GIRD_COLOR = new Color(null, 240, 240, 240);
    
    public ReportFormScreenCanvasFigure(ReportFormScreenCanvas model)
    {
       
        setPreferredSize(model.getWidth(), model.getHeight());
        setSize(model.getWidth(), model.getHeight());
        setBackgroundColor(GIRD_COLOR);
        setOutline(false);
        
        setLayoutManager(new XYLayout());
        
        
    }

}
