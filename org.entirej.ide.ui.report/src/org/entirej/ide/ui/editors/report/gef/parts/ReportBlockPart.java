package org.entirej.ide.ui.editors.report.gef.parts;

import java.util.ArrayList;
import java.util.List;

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
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.ui.editors.report.gef.ReportEditorContext;
import org.entirej.ide.ui.editors.report.gef.commands.OperationCommand;
import org.entirej.ide.ui.editors.report.gef.figures.ReportBlockFigure;
import org.entirej.ide.ui.editors.report.gef.parts.policies.ReportBlockResizableEditPolicy;

public class ReportBlockPart extends AbstractReportGraphicalEditPart implements ReportXYMoveCommandProvider
{

    @Override
    protected IFigure createFigure()
    {
        return new ReportBlockFigure(getModel());
    }

    @Override
    protected void createEditPolicies()
    {
        installEditPolicy(EditPolicy.PRIMARY_DRAG_ROLE, new ReportBlockResizableEditPolicy());

    }

    @Override
    public EJPluginReportBlockProperties getModel()
    {
        return (EJPluginReportBlockProperties) super.getModel();
    }

    @Override
    protected void refreshVisuals()
    {
        IFigure figure = getFigure();

        AbstractGraphicalEditPart parent = (AbstractGraphicalEditPart) getParent();
        EJPluginReportBlockProperties model = getModel();
        final EJPluginReportScreenProperties screenProperties = model.getLayoutScreenProperties();

        int width = screenProperties.getWidth();
        int height = screenProperties.getHeight();

        Rectangle layout = new Rectangle(screenProperties.getX(), screenProperties.getY(), width, height);
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
        final EJPluginReportBlockProperties model = getModel();
        final ReportEditorContext editorContext = getReportEditorContext();
        final int x = model.getLayoutScreenProperties().getX() + xDelta;
        ;
        final int y = model.getLayoutScreenProperties().getY() + yDelta;
        final int oldX = model.getLayoutScreenProperties().getX();
        final int oldY = model.getLayoutScreenProperties().getY();

        AbstractOperation operation = new AbstractOperation("Move Screen Item")
        {

            @Override
            public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
            {
                model.getLayoutScreenProperties().setX(oldX);
                model.getLayoutScreenProperties().setY(oldY);
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
                model.getLayoutScreenProperties().setX(x);
                model.getLayoutScreenProperties().setY(y);
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

                model.getLayoutScreenProperties().setX(x);
                model.getLayoutScreenProperties().setY(y);

                editorContext.setDirty(true);
                editorContext.refresh(model);
                editorContext.refreshProperties();

                refresh();

                return Status.OK_STATUS;
            }
        };

        return new OperationCommand(editorContext, operation);
    }

    @Override
    protected List getModelChildren()
    {
        List<Object> objects = new ArrayList<Object>();
        
        EJPluginReportBlockProperties reportBlockProperties = getModel();
        EJPluginReportScreenProperties layoutScreenProperties = reportBlockProperties.getLayoutScreenProperties();
//        switch (layoutScreenProperties.getScreenType())
//        {
//            case FORM_LAYOUT:
//                objects.add(layoutScreenProperties);
//                break;
//
//            default:
//                break;
//        }
        
        return objects;
    }
}
