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
import org.eclipse.gef.DragTracker;
import org.eclipse.gef.GraphicalEditPart;
import org.eclipse.gef.Request;
import org.eclipse.gef.SharedCursors;
import org.eclipse.gef.commands.Command;
import org.eclipse.gef.editpolicies.ResizableEditPolicy;
import org.eclipse.gef.handles.NonResizableHandleKit;
import org.eclipse.gef.handles.ResizableHandleKit;
import org.eclipse.gef.requests.ChangeBoundsRequest;
import org.eclipse.gef.tools.DragEditPartsTracker;
import org.eclipse.gef.tools.SelectEditPartTracker;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.ReportEditorContext;
import org.entirej.ide.ui.editors.report.gef.commands.OperationCommand;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenItemPart;


public class ScreenItemResizableEditPolicy extends ResizableEditPolicy {

	
	public ScreenItemResizableEditPolicy() {
		super();
	}

	@Override
	protected List<?> createSelectionHandles() {
		if (getResizeDirections() == PositionConstants.NONE) {
			// non resizable, so delegate to super implementation
			return super.createSelectionHandles();
		}

		// resizable in at least one direction
		List<?> list = new ArrayList<Object>();
		createMoveHandle(list);
		createResizeHandle(list, PositionConstants.NORTH);
		createResizeHandle(list, PositionConstants.EAST);
		createResizeHandle(list, PositionConstants.SOUTH);
		createResizeHandle(list, PositionConstants.WEST);
		createResizeHandle(list, PositionConstants.SOUTH_EAST);
		createResizeHandle(list, PositionConstants.SOUTH_WEST);
		createResizeHandle(list, PositionConstants.NORTH_WEST);
		createResizeHandle(list, PositionConstants.NORTH_EAST);
		return list;
	}

	/**
	 * Shows or updates feedback for a change bounds request.
	 * 
	 * @param request
	 *          the request
	 */
	protected void showChangeBoundsFeedback(ChangeBoundsRequest request) {

		IFigure feedback = getDragSourceFeedbackFigure();

		PrecisionRectangle rect = new PrecisionRectangle(getInitialFeedbackBounds().getCopy());
		getHostFigure().translateToAbsolute(rect);
		rect.translate(request.getMoveDelta());
		rect.resize(request.getSizeDelta());

		// Calculate changes for the figure...
		String s = "";
		int scaleH = 0;
		int scaleW = 0;
		if (getHost().getModel()  instanceof EJPluginReportScreenItemProperties) {
		    EJPluginReportScreenItemProperties model = (EJPluginReportScreenItemProperties) getHost().getModel();
            Rectangle oldBounds = new Rectangle(model.getX() + request.getMoveDelta().x, model.getY() + request.getMoveDelta().y,
                    model.getWidth() + request.getSizeDelta().width, model.getHeight() + request.getSizeDelta().height);

			

			s += oldBounds.x + ", " + oldBounds.y + ", " + oldBounds.width + ", " + oldBounds.height;
			if (oldBounds.width != 0)
				scaleW = rect.width / oldBounds.width - 1;
			if (oldBounds.height != 0)
				scaleH = rect.height / oldBounds.height - 1;
		}

		feedback.translateToRelative(rect);

		((ElementFeedbackFigure) feedback).setText(s);

		feedback.setBounds(rect.resize(-scaleW, -scaleH));
	}

	@Override
	protected void createMoveHandle(@SuppressWarnings("rawtypes") List handles) {
		if (isDragAllowed()) {
			// display 'move' handle to allow dragging
			ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(), handles, getDragTracker(), Cursors.SIZEALL);
		} else {
			// display 'move' handle only to indicate selection
			ResizableHandleKit.addMoveHandle((GraphicalEditPart) getHost(), handles, getSelectTracker(), SharedCursors.ARROW);
		}
	}

	@Override
	protected void createDragHandle(@SuppressWarnings("rawtypes") List handles, int direction) {
		if (isDragAllowed()) {
			// display 'resize' handles to allow dragging (drag tracker)
			NonResizableHandleKit.addHandle((GraphicalEditPart) getHost(), handles, direction, getDragTracker(),
					SharedCursors.SIZEALL);
		} else {
			// display 'resize' handles to indicate selection only (selection
			// tracker)
			NonResizableHandleKit.addHandle((GraphicalEditPart) getHost(), handles, direction, getSelectTracker(),
					SharedCursors.ARROW);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected void createResizeHandle(@SuppressWarnings("rawtypes") List handles, int direction) {
		if ((getResizeDirections() & direction) == direction) {
//			ColoredSquareHandles handle = new ColoredSquareHandles((GraphicalEditPart) getHost(), direction);
//			handle.setDragTracker(getResizeTracker(direction));
//			handle.setCursor(Cursors.getDirectionalCursor(direction, getHostFigure().isMirrored()));
//			handles.add(handle);
		    super.createResizeHandle(handles, direction);
		} else {
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
	protected IFigure createDragSourceFeedbackFigure() {
		// Use a ghost rectangle for feedback
		RectangleFigure r = new ElementFeedbackFigure();

		// FigureUtilities.makeGhostShape(r);
		r.setLineStyle(Graphics.LINE_DOT);
		r.setForegroundColor(ColorConstants.black);
		r.setBounds(getInitialFeedbackBounds().resize(-1, -1));// new Rectangle(ifb.x, ifb.y, ifb.width -100,
																														// ifb.height));
		addFeedback(r);
		return r;
	}
	
	/**
	 * Remove the feedback also if the request is a move children
	 */
	public void eraseSourceFeedback(Request request) {
		if (REQ_MOVE_CHILDREN.equals(request.getType())) {
			eraseChangeBoundsFeedback((ChangeBoundsRequest) request);
		} else {
			super.eraseSourceFeedback(request);
		}
	}


	// ==== BACK COMPATIBILITY METHODS ==== //

//	/**
//	 * Returns a drag tracker to use by a resize handle.
//	 * 
//	 * @return a new {@link SearchParentDragTracker}
//	 * @since 3.7
//	 */
//	protected DragEditPartsTracker getDragTracker() {
//		return new SearchParentDragTracker(getHost());
//	}

	/**
	 * Returns a selection tracker to use by a selection handle.
	 * 
	 * @return a new {@link SelectEditPartTracker}
	 * @since 3.7
	 */
	protected SelectEditPartTracker getSelectTracker() {
		return new SelectEditPartTracker(getHost());
	}
	
	

//	/**
//	 * Returns a resize tracker for the given direction to be used by a resize handle.
//	 * 
//	 * @param direction
//	 *          the resize direction for the {@link JSSCompoundResizeTracker}.
//	 * @return a new {@link JSSCompoundResizeTracker}
//	 * @since 3.7
//	 */
//	protected ResizeTracker getResizeTracker(int direction) {
//		return new JSSCompoundResizeTracker((GraphicalEditPart) getHost(), direction);
//	}

	// =================================== //

	public List<?> getHandles() {
		return handles;
	}
	
	
	
	@Override
	protected Command getMoveCommand(ChangeBoundsRequest request)
	{
	    if (getHost().getModel() instanceof EJPluginReportScreenItemProperties)
            {
                ReportFormScreenItemPart part = (ReportFormScreenItemPart) getHost();
                return part.createMoveCommand(request.getMoveDelta().x, request.getMoveDelta().y);
            }
	    return null;
	}
	/**
	     * Resize command used when the band is drag and dropped
	     */
	    @Override
	    protected Command getResizeCommand(ChangeBoundsRequest request)
	    {
	        if (getHost().getModel() instanceof EJPluginReportScreenItemProperties)
	        {
	            ReportFormScreenItemPart part = (ReportFormScreenItemPart) getHost();
	            final EJPluginReportScreenItemProperties model = part.getModel();
	            final ReportEditorContext editorContext = part.getReportEditorContext();
	            
	            int basewidth = model.getWidth();
	            int baseheight = model.getHeight();
	            if (model.isWidthAsPercentage())
	            {
	                basewidth = (int) (((double) ((EJPluginReportScreenProperties)part.getParent().getModel()).getWidth() / 100) * model.getWidth());
	            }

	            
	            if (model.isHeightAsPercentage())
	            {
	                baseheight = (int) (((double) ((EJPluginReportScreenProperties)part.getParent().getModel()).getHeight() / 100) * model.getHeight());
	            }
	            
	            final int                            width ;
	            ;
	            final int                            height ;
	            
	            
	            int widthDelta = request.getSizeDelta().width;
	            int heightDelta = request.getSizeDelta().height;
	            
	            
	            double zoom = part.getZoom();
	            widthDelta = (int) Math.round(widthDelta / zoom);
	            heightDelta = (int) Math.round(heightDelta / zoom);
                if(model.isWidthAsPercentage())
	                width = (int) (((double) (basewidth + widthDelta) / ((EJPluginReportScreenProperties)part.getParent().getModel()).getWidth()) * 100);
	            else
	                width = model.getWidth() + widthDelta;
	           
                if(model.isHeightAsPercentage())
	                height = (int) (((double) (baseheight + heightDelta) / ((EJPluginReportScreenProperties)part.getParent().getModel()).getHeight()) * 100);
	            else
	                height = model.getHeight() + heightDelta;
	            
	            
	            
	            final int                            oldWidth =model.getWidth();
	            final int                            oldHeight = model.getHeight();
	            
	            AbstractOperation operation = new AbstractOperation("Resize Screen Item")
	            {

	                @Override
	                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
	                {
	                    model.setWidth(oldWidth);
	                    model.setHeight(oldHeight);
	                    editorContext.setDirty(true);
	                    editorContext.refresh(model);
	                    editorContext.refreshProperties();
	                    editorContext.refreshPreview();

	                    getHost().refresh();
	                    return Status.OK_STATUS;
	                }

	                @Override
	                public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
	                {
	                    model.setWidth(width);
	                    model.setHeight(height);
	                    editorContext.setDirty(true);
	                    editorContext.refresh(model);
	                    editorContext.refreshProperties();
	                    editorContext.refreshPreview();

	                    getHost().refresh();
	                    return Status.OK_STATUS;
	                }

	                @Override
	                public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
	                {

	                    model.setWidth(width);
	                    model.setHeight(height);
	                    
	                    editorContext.setDirty(true);
	                    editorContext.refresh(model);
	                    editorContext.refreshProperties();

	                    getHost().refresh();
	                    return Status.OK_STATUS;
	                }
	            };

	            

	            return new OperationCommand(editorContext, operation);
	        }

	        return null;
	    }
}
