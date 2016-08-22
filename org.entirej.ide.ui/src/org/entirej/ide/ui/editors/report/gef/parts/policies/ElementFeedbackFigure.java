package org.entirej.ide.ui.editors.report.gef.parts.policies;

import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.FontMetrics;
import org.eclipse.swt.widgets.Display;

public class ElementFeedbackFigure extends RectangleFigure
{

    String text = "";

    public ElementFeedbackFigure()
    {
        setFill(false);
    }

    public void setText(String text)
    {
        this.text = text;
    }

    @Override
    public void paintClientArea(Graphics g)
    {

        if (g == null)
            return;

        Rectangle clientArea = getClientArea();
        Graphics gr = g;

        if (clientArea.width < 20 || clientArea.height < 20 || text.isEmpty())
            return;

        FontMetrics fm = gr.getFontMetrics();
        int textWidth = fm.getAverageCharWidth() * text.length();
        int textHeight = fm.getHeight();
        Rectangle textBgBounds = new Rectangle(clientArea.x - 30 + (clientArea.width + 60) / 2 - (int) textWidth / 2 - 10,
                clientArea.y - 30 + (clientArea.height + 60) / 2 - (int) textHeight / 2 - 2, (int) textWidth + 20, (int) textHeight + 4);

        gr.drawLine(clientArea.x - 30, // X
                clientArea.y - 30 + (clientArea.height + 60) / 2, // Half Y
                clientArea.x - 30 + (clientArea.width + 60 - textBgBounds.width) / 2, // Up
                                                                                      // to
                                                                                      // the
                                                                                      // right
                                                                                      // side
                                                                                      // of
                                                                                      // the
                                                                                      // label
                clientArea.y - 30 + (clientArea.height + 60) / 2); // Same Y...

        gr.drawLine(clientArea.x - 30 + (clientArea.width + 60 + textBgBounds.width) / 2, // From
                                                                                          // the
                                                                                          // left
                                                                                          // side
                                                                                          // of
                                                                                          // the
                                                                                          // label
                clientArea.y - 30 + (clientArea.height + 60) / 2, // Half Y
                clientArea.x - 30 + clientArea.width + 60, // Up to the full
                                                           // width
                clientArea.y - 30 + (clientArea.height + 60) / 2); // Same Y...

        gr.drawLine(clientArea.x - 30 + (clientArea.width + 60) / 2, // Half X
                clientArea.y - 30, // Half Y
                clientArea.x - 30 + (clientArea.width + 60) / 2, // Half X
                clientArea.y - 30 + (clientArea.height + 60 - textBgBounds.height) / 2); // Up
                                                                                         // to
                                                                                         // the
                                                                                         // top
                                                                                         // of
                                                                                         // the
                                                                                         // label...

        gr.drawLine(clientArea.x - 30 + (clientArea.width + 60) / 2, // Half X
                clientArea.y - 30 + (clientArea.height + 60 + textBgBounds.height) / 2, // //
                                                                                        // Up
                                                                                        // to
                                                                                        // the
                                                                                        // bottom
                                                                                        // of
                                                                                        // the
                                                                                        // label...
                clientArea.x - 30 + (clientArea.width + 60) / 2, // Half X
                clientArea.y - 30 + clientArea.height + 60); // Up to the bounds
                                                             // height...

        gr.setForegroundColor(Display.getDefault().getSystemColor(SWT.COLOR_DARK_GRAY));
        gr.drawString(text, textBgBounds.x + 10, textBgBounds.y + 5);

    }
}
