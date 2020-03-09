package org.entirej.ide.ui.editors.report.gef.parts.policies;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Cursors;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.IFigure;
import org.eclipse.draw2d.PositionConstants;
import org.eclipse.draw2d.RectangleFigure;
import org.eclipse.draw2d.geometry.PrecisionRectangle;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.gef.EditPart;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.handles.NonResizableHandleKit;
import org.eclipse.gef.handles.ResizableHandleKit;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.ide.ui.editors.report.gef.ReportEditorContext;
import org.entirej.ide.ui.editors.report.gef.commands.OperationCommand;
import org.entirej.ide.ui.editors.report.gef.parts.AbstractReportGraphicalEditPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockColumnLabelPart.ReportBlockColumnLabel;
import org.entirej.ide.ui.editors.report.gef.parts.ReportTableScreenCanvasPart;

public class ColumnResizableEditPolicy extends ResizableEditPolicy
{

    public ColumnResizableEditPolicy()
    {
        super();
    }

    @Override
    protected List<?> createSelectionHandles()
    {
        setDragAllowed(false);
        if (getResizeDirections() == PositionConstants.NONE)
        {
            // non resizable, so delegate to super implementation
            return super.createSelectionHandles();
        }

        // resizable in at least one direction
        List<?> list = new ArrayList<Object>();
       // createMoveHandle(list);
        createResizeHandle(list, PositionConstants.EAST);
        createResizeHandle(list, PositionConstants.WEST);
        return list;
    }

    /**
     * Shows or updates feedback for a change bounds request.
     * 
     * @param request
     *            the request
     */
    protected void showChangeBoundsFeedback(ChangeBoundsRequest request)
    {

        IFigure feedback = getDragSourceFeedbackFigure();

        PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
        getHostFigure().translateToAbsolute(rect);
        rect.translate(request.getMoveDelta());
        rect.resize(request.getSizeDelta());

        // Calculate changes for the figure...
        String s = "";
        int scaleH = 0;
        int scaleW = 0;
        if (getHost().getModel() instanceof EJPluginReportColumnProperties || getHost().getModel() instanceof ReportBlockColumnLabel)
        {
            IFigure figure = getHostFigure();
            Rectangle oldBounds = new Rectangle(figure.getBounds().x, figure.getBounds().y, figure.getBounds().width, figure.getBounds().height);

            PrecisionRectangle rect2 = new PrecisionRectangle(
                    new Rectangle(request.getMoveDelta().x, request.getMoveDelta().y, request.getSizeDelta().width, request.getSizeDelta().height));
            getHostFigure().translateToRelative(rect2);

            oldBounds.translate(rect2.x, rect2.y);
            oldBounds.resize(rect2.width, rect2.height);

            s +=  + oldBounds.width ;
            if (oldBounds.width != 0)
                scaleW = rect.width / oldBounds.width - 1;
        }

        feedback.translateToRelative(rect);

        ((ElementFeedbackFigure) feedback).setText(s);

        feedback.setBounds(rect.resize(-scaleW, -scaleH));
    }

    @Override
    protected void createMoveHandle(@SuppressWarnings("rawtypes") List handles)
    {
        if (isDragAllowed())
        {
            // display 'move' handle to allow dragging
            ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(), handles, getDragTracker(), Cursors.SIZEALL);
        }
        else
        {
            // display 'move' handle only to indicate selection
            ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(), handles, getSelectTracker(), SharedCursors.ARROW);
        }
    }

    @Override
    protected void createDragHandle(@SuppressWarnings("rawtypes") List handles, int direction)
    {
        if (isDragAllowed())
        {
            // display 'resize' handles to allow dragging (drag tracker)
            NonResizableHandleKit.addHandle((GraphicalEditPart) getHost(), handles, direction, getDragTracker(), SharedCursors.SIZEALL);
        }
        else
        {
            // display 'resize' handles to indicate selection only (selection
            // tracker)
            NonResizableHandleKit.addHandle((GraphicalEditPart) getHost(), handles, direction, getSelectTracker(), SharedCursors.ARROW);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void createResizeHandle(@SuppressWarnings("rawtypes") List handles, int direction)
    {
        if ((getResizeDirections() & direction) == direction)
        {
            // ColoredSquareHandles handle = new
            // ColoredSquareHandles((GraphicalEditPart) getHost(), direction);
            // handle.setDragTracker(getResizeTracker(direction));
            // handle.setCursor(Cursors.getDirectionalCursor(direction,
            // getHostFigure().isMirrored()));
            // handles.add(handle);
            super.createResizeHandle(handles, direction);
        }
        else
        {
            // display 'resize' handle to allow dragging or indicate selection
            // only
            createDragHandle(handles, direction);
        }

    }

    /**
     * Creates the figure used for feedback.
     * 
     * @return the new feedback figure
     */
    protected IFigure createDragSourceFeedbackFigure()
    {
        // Use a ghost rectangle for feedback
        RectangleFigure r = new ElementFeedbackFigure();

        // FigureUtilities.makeGhostShape(r);
        r.setLineStyle(Graphics.LINE_DOT);
        r.setForegroundColor(ColorConstants.black);
        r.setBounds(getInitialFeedbackBounds().resize(-1, -1));// new
                                                               // Rectangle(ifb.x,
                                                               // ifb.y,
                                                               // ifb.width
                                                               // -100,
                                                               // ifb.height));
        addFeedback(r);
        return r;
    }

    /**
     * Remove the feedback also if the request is a move children
     */
    public void eraseSourceFeedback(Request request)
    {
        if (REQ_MOVE_CHILDREN.equals(request.getType()))
        {
            eraseChangeBoundsFeedback((ChangeBoundsRequest) request);
        }
        else
        {
            super.eraseSourceFeedback(request);
        }
    }

    // ==== BACK COMPATIBILITY METHODS ==== //

    // /**
    // * Returns a drag tracker to use by a resize handle.
    // *
    // * @return a new {@link SearchParentDragTracker}
    // * @since 3.7
    // */
    // protected DragEditPartsTracker getDragTracker() {
    // return new SearchParentDragTracker(getHost());
    // }

    /**
     * Returns a selection tracker to use by a selection handle.
     * 
     * @return a new {@link SelectEditPartTracker}
     * @since 3.7
     */
    protected SelectEditPartTracker getSelectTracker()
    {
        return new SelectEditPartTracker(getHost());
    }



    // =================================== //

    public List<?> getHandles()
    {
        return handles;
    }

    /**
     * Resize command used when the band is drag and dropped
     */
    @Override
    protected Command getResizeCommand(ChangeBoundsRequest request)
    {
        if (getHost().getModel() instanceof EJPluginReportColumnProperties || getHost().getModel() instanceof ReportBlockColumnLabel)
        {
            AbstractReportGraphicalEditPart part = (AbstractReportGraphicalEditPart) getHost();
            final EJPluginReportColumnProperties model;
            if(getHost().getModel() instanceof ReportBlockColumnLabel)
            {
                model = ((ReportBlockColumnLabel) part.getModel()).getColumnProperties();
            }
            else
            {
                model = (EJPluginReportColumnProperties) part.getModel();
            }
            final ReportEditorContext editorContext = part.getReportEditorContext();
            int widthDelta = request.getSizeDelta().width;
            int heightDelta = request.getSizeDelta().height;
            double zoom = part.getZoom();
            widthDelta = (int) Math.round(widthDelta / zoom);
            heightDelta = (int) Math.round(heightDelta / zoom);
            final int                            width = model.getDetailScreen().getWidth() + widthDelta;
            ;
            
            final int                            oldWidth =model.getDetailScreen().getWidth();
            
            AbstractOperation operation = new AbstractOperation("Resize Screen")
            {

                @Override
                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    model.getDetailScreen().setWidth(oldWidth);
                    editorContext.setDirty(true);
                    editorContext.refresh(model);
                    editorContext.refreshProperties();
                    editorContext.refreshPreview();

                    refreshParent();
                    return Status.OK_STATUS;
                }

                @Override
                public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    model.getDetailScreen().setWidth(width);
                   
                    editorContext.setDirty(true);
                    editorContext.refresh(model);
                    editorContext.refreshProperties();
                    editorContext.refreshPreview();

                    refreshParent();
                    return Status.OK_STATUS;
                }

                @Override
                public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {

                    model.getDetailScreen().setWidth(width);
                    
                    editorContext.setDirty(true);
                    editorContext.refresh(model);
                    editorContext.refreshProperties();

                    refreshParent();
                    return Status.OK_STATUS;
                }
                
                void refreshParent()
                {
                    EditPart editPart = getHost().getParent();
                    while (editPart.getParent()!=null && !(editPart instanceof ReportTableScreenCanvasPart))
                    {
                        editPart.refresh();
                        editPart = editPart.getParent();
                        
                    }
                    refreshPart(editPart);
                }
                
                void refreshPart(EditPart editPart )
                {
                    editPart.refresh();
                    List children = editPart.getChildren();
                    for (Object object : children)
                    {
                        refreshPart((EditPart) object);
                    }
                }
            };

            

            return new OperationCommand(editorContext, operation);
        }

        return null;
    }
}
