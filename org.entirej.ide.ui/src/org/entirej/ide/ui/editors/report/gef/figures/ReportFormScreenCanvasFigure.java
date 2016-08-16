package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenCanvasPart.ReportFormScreenCanvas;

public class ReportFormScreenCanvasFigure extends RectangleFigure
{
   
    public ReportFormScreenCanvasFigure(ReportFormScreenCanvas model)
    {
       
        setPreferredSize(model.getWidth(), model.getHeight());
        setSize(model.getWidth(), model.getHeight());
        setBackgroundColor(Display.getCurrent().getSystemColor(SWT.COLOR_WHITE));
        setOutline(false);
        
        setLayoutManager(new XYLayout());
        
        
    }

}
