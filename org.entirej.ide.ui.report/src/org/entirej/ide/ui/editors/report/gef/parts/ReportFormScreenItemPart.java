package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPolicy;
import org.eclipse.gef.Request;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.swt.widgets.Control;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.ReportEditorContext;
import org.entirej.ide.ui.editors.report.gef.commands.OperationCommand;
import org.entirej.ide.ui.editors.report.gef.figures.ReportFormScreenItemFigure;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ScreenItemResizableEditPolicy;

public class ReportFormScreenItemPart extends AbstractReportGraphicalEditPart implements ReportXYMoveCommandProvider
{

    public ReportFormScreenItemPart()
    {
        // TODO Auto-generated constructor stub
    }

    @Override
    protected IFigure createFigure()
    {
        return new ReportFormScreenItemFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new ScreenItemResizableEditPolicy());

    }

    @Override
    public EJPluginReportScreenItemProperties getModel()
    {
        return (EJPluginReportScreenItemProperties) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        IFigure figure = getFigure();

        AbstractGraphicalEditPart parent = (AbstractGraphicalEditPart) getParent();
        EJPluginReportScreenItemProperties model = getModel();

        int width = model.getWidth();
        int height = model.getHeight();
        if (model.isWidthAsPercentage())
        {
            width = (int) Math.round(((double) ((EJPluginReportScreenProperties) getParent().getModel()).getWidth() / 100) * model.getWidth());
        }

        if (model.isHeightAsPercentage())
        {
            height = (int) Math.round(((double) ((EJPluginReportScreenProperties) getParent().getModel()).getHeight() / 100) * model.getHeight());
        }
        Rectangle layout = new Rectangle(model.getX(), model.getY(), width, height);
        parent.setLayoutConstraint(this, figure, layout);
    }

    public DragTracker getDragTracker(Request request)
    {

        return new DragEditPartsTracker(this)
        {
            @Override
            protected EditPart getTargetEditPart()
            {
                return getParent();
            }
        };
    }

    public Command createMoveCommand(Integer xDelta, Integer yDelta)
    {
        double zoom = getZoom();
        xDelta = (int) Math.round(xDelta / zoom);
        yDelta = (int) Math.round(yDelta / zoom);
        final EJPluginReportScreenItemProperties model = getModel();
        final ReportEditorContext editorContext = getReportEditorContext();
        final int x = model.getX() + xDelta;
        ;
        final int y = model.getY() + yDelta;
        final int oldX = model.getX();
        final int oldY = model.getY();

        AbstractOperation operation = new AbstractOperation("Move Screen Item")
        {

            @Override
            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
            {
                model.setX(oldX);
                model.setY(oldY);
                editorContext.setDirty(true);
                editorContext.refresh(model);
                editorContext.refreshProperties();
                editorContext.refreshPreview();

                refresh();
                return Status.OK_STATUS;
            }

            @Override
            public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
            {
                model.setX(x);
                model.setY(y);
                editorContext.setDirty(true);
                editorContext.refresh(model);
                editorContext.refreshProperties();
                editorContext.refreshPreview();

                refresh();
                return Status.OK_STATUS;
            }

            @Override
            public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
            {

                model.setX(x);
                model.setY(y);

                editorContext.setDirty(true);
                editorContext.refresh(model);
                editorContext.refreshProperties();

                refresh();
                
                return Status.OK_STATUS;
            }
        };

        return new OperationCommand(editorContext, operation);
    }

}
