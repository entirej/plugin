
package org.entirej.ide.ui.editors.report.gef;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.commands.CompoundCommand;
import org.eclipse.gef.ui.parts.GraphicalViewerKeyHandler;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.widgets.Control;
import org.entirej.ide.ui.editors.report.gef.parts.ReportXYMoveCommandProvider;

public class ReportGraphicalViewerKeyHandler extends GraphicalViewerKeyHandler
{

    public ReportGraphicalViewerKeyHandler(GraphicalViewer viewer)
    {
        super(viewer);
    }

    @Override
    public boolean keyPressed(KeyEvent event)
    {
        if (isArrowKey(event.keyCode))
        {
            EditPartViewer viewer = getViewer();
            if (viewer instanceof GraphicalViewer)
            {
                CompoundCommand ccmd = new CompoundCommand(null)
                {

                    @Override
                    public void execute()
                    {
                        super.execute();
                        Control control = getViewer().getControl();
                        if (!control.isDisposed())
                        {
                            control.forceFocus();
                        }
                    }
                };
                for (Object selectedEditPart : getViewer().getSelectedEditParts())
                {
                    if (selectedEditPart instanceof GraphicalEditPart)
                    {
                        if (selectedEditPart instanceof ReportXYMoveCommandProvider)
                        {
                            ReportXYMoveCommandProvider node = (ReportXYMoveCommandProvider) selectedEditPart;
                            ccmd.add(getNewXYCommand(event, node));
                        }
                    }
                }
                if (!ccmd.isEmpty())
                {
                    getViewer().getEditDomain().getCommandStack().execute(ccmd);
                    return true;
                }
            }
        }
        return super.keyPressed(event);
    }

    /*
     * Gets a new command that modify the x or y coordinate depending on the
     * arrow key pressed. Standard movement is 1px. If SHIFT key is also pressed
     * 10px is the step.
     */
    private Command getNewXYCommand(KeyEvent event, ReportXYMoveCommandProvider node)
    {
        int step = 1;
        int arrowKeyCode = event.keyCode;
        if ((event.stateMask & SWT.SHIFT) != 0)
            step = 10;
        Integer xDelta = 0;
        Integer yDelta = 0;
        switch (arrowKeyCode)
        {
            case SWT.ARROW_UP:
                yDelta = yDelta - step;
                break;
            case SWT.ARROW_DOWN:
                yDelta = yDelta + step;
                break;
            case SWT.ARROW_LEFT:
                xDelta = xDelta - step;
                break;
            case SWT.ARROW_RIGHT:
                xDelta = xDelta + step;
                break;
            default:

        }

        return node.createMoveCommand(xDelta, yDelta);
    }

    public static boolean isArrowKey(int keyCode)
    {
        return keyCode == SWT.ARROW_DOWN || keyCode == SWT.ARROW_LEFT || keyCode == SWT.ARROW_RIGHT || keyCode == SWT.ARROW_UP;
    }

}
