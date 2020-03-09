package org.entirej.ide.ui.editors.report.gef.figures;

import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.swt.graphics.Color;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockSectionCanvasPart.BlockSectionCanvas;

public class ReportBlockSectionCanvasFigure extends RectangleFigure
{
    private static final Color GIRD_COLOR = new Color(null, 224, 224, 224, 150);
    final BlockSectionCanvas   model;

    public ReportBlockSectionCanvasFigure(BlockSectionCanvas model)
    {
        this.model = model;
        setBackgroundColor(GIRD_COLOR);
        setAlpha(150);
        setLayoutManager(new XYLayout());

    }


}
