package org.entirej.ide.ui.editors.report.gef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.FigureCanvas;
import org.eclipse.draw2d.geometry.Dimension;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.SnapToGrid;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ZoomListener;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.rulers.RulerProvider;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.gef.ui.rulers.RulerComposite;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.entirej.ide.ui.editors.report.AbstractEJReportEditor;
import org.entirej.ide.ui.editors.report.gef.parts.ReportSelectionProvider;
import org.entirej.ide.ui.editors.report.gef.ruler.ReportRuler;
import org.entirej.ide.ui.editors.report.gef.ruler.ReportRulerProvider;

public class ReportPreviewEditControl extends RulerComposite
{

    private final EditDomain               editDomain;
    private final ScrollingGraphicalViewer viewer;

    private AtomicBoolean                  evntTrigger = new AtomicBoolean(true);
    private int vOffset;
    private int hOffset;

    public ReportPreviewEditControl(final AbstractEJReportEditor editor, Composite parent, boolean showRuler,int hOffset,int vOffset)
    {
        super(parent, SWT.NONE);
        // setLayout(new FillLayout());

        this.hOffset = hOffset;
        this.vOffset = vOffset;
        editDomain = new EditDomain();

        viewer = new ScrollingGraphicalViewer();

        // connect external Drop support

        // viewer.addDropTargetListener(new ProxyDropTargetListener(
        //
        // ));

        viewer.createControl(this);
        editDomain.addViewer(viewer);

        viewer.getControl().setBackground(ColorConstants.listBackground);

        viewer.setEditPartFactory(createPartFactory(editor));
        viewer.setKeyHandler(new ReportGraphicalViewerKeyHandler(viewer));
        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                if (!evntTrigger.get())
                    return;

                IStructuredSelection selection = (IStructuredSelection) event.getSelection();
                Object firstElement = selection.getFirstElement();
                if (firstElement instanceof AbstractGraphicalEditPart)
                {
                    AbstractGraphicalEditPart part = (AbstractGraphicalEditPart) firstElement;
                    if(part instanceof ReportSelectionProvider)
                    {
                        ReportSelectionProvider selectionProvider = (ReportSelectionProvider)part;
                        editor.select(selectionProvider.getSelectionObject(), false);
                        evntTrigger.set(false);
                        viewer.setSelection(new StructuredSelection(selectionProvider.getPostSelection()));
                        evntTrigger.set(true);
                    }
                    else
                    {
                        editor.select(part.getModel(), false);
                    }

                    if (viewer.getControl() != null)
                        viewer.getControl().forceFocus();
                }
            }
        });
        viewer.setProperty(RulerProvider.PROPERTY_RULER_VISIBILITY, showRuler);

        ReportRuler hRuler = new ReportRuler(true);
        hRuler.setHoffset(hOffset);
        hRuler.setVoffset(hOffset);
        viewer.setProperty(RulerProvider.PROPERTY_HORIZONTAL_RULER, new ReportRulerProvider(hRuler));
        viewer.setProperty(SnapToGrid.PROPERTY_GRID_SPACING, new Dimension(10, 10));

        ReportRuler vRuler = new ReportRuler(false);

       
        vRuler.setVoffset(vOffset);
        vRuler.setHoffset(vOffset);
        viewer.setProperty(RulerProvider.PROPERTY_VERTICAL_RULER, new ReportRulerProvider(vRuler));
        setGraphicalViewer(viewer);

        if(vOffset!=0 || hOffset!=0)
        getZoomManager().addZoomListener(new ZoomListener()
        {
            
            public void zoomChanged(double zoom)
            {
                Display.getDefault().asyncExec(new  Runnable()
                {
                    public void run()
                    {
                        layout(true);
                    }
                });
                
            }
        });

    }
    
    
    public void doLayout() {
        if(vOffset==0 && hOffset==0)
        {
            super.doLayout();
            return;
        }
        GraphicalViewer left = getLeft();
        GraphicalViewer top = getTop();
        FigureCanvas editor = getEditor();
        
        if (left == null && top == null) {
                Rectangle area = getClientArea();
                if (editor != null && !editor.isDisposed()
                                && !editor.getBounds().equals(area))
                        editor.setBounds(area);
                return;
        }

        int leftWidth = 0, topHeight = 0;
        Rectangle leftTrim = null, topTrim = null;
        if (left != null) {
                leftTrim = calculateRulerTrim((Canvas) left.getControl());
                // Adding the trim width here because FigureCanvas#computeSize()
                // does not
                leftWidth = left.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).x
                                + leftTrim.width;
        }
        if (top != null) {
                topTrim = calculateRulerTrim((Canvas) top.getControl());
                topHeight = top.getControl().computeSize(SWT.DEFAULT, SWT.DEFAULT).y
                                + topTrim.height;
        }

        Rectangle editorSize = getClientArea();
        editorSize.x = leftWidth;
        editorSize.y = topHeight;
        editorSize.width -= leftWidth;
        editorSize.height -= topHeight;
        editor.setBounds(editorSize);

        /*
         * Fix for Bug# 67554 Take trim into account. Some platforms (such as
         * MacOS and Motif) leave some trimming around some canvasses.
         */
        double zoom = getZoomManager().getZoom();
        Rectangle trim = calculateEditorTrim(editor);
        if (left != null) {
                // The - 1 and + 1 are to compensate for the RulerBorder
                left.getControl().setBounds(0, (topHeight - trim.x + leftTrim.x - 1)+(int) Math.round(vOffset * zoom),
                                leftWidth,
                                editorSize.height - trim.height + leftTrim.height + 1);
        }
        if (top != null) {
                top.getControl().setBounds((leftWidth - trim.y + topTrim.y - 1)+(int) Math.round(hOffset * zoom), 0,
                                editorSize.width - trim.width + topTrim.width + 1,
                                topHeight);
        }
}
    
    public ZoomManager getZoomManager()
    {
        return (ZoomManager) viewer.getProperty(ZoomManager.class.toString());
    }

    protected ReportEditPartFactory createPartFactory(final AbstractEJReportEditor editor)
    {
        return new ReportEditPartFactory(createContext(editor));
    }

    public ReportEditorContext createContext(final AbstractEJReportEditor editor)
    {
        return new ReportEditorContext()
        {

            public void execute(AbstractOperation operation)
            {
                editor.execute(operation);

            }

            public void setDirty(boolean b)
            {
                editor.setDirty(b);

            }

            public void refresh(Object model)
            {
                editor.refresh(model);

            }

            public void refreshPreview()
            {
                editor.refreshPreview();

            }

            public void refreshProperties()
            {
                editor.refreshProperties();

            }
        };
    }

    /**
     * This method should return a
     * {@link org.eclipse.jface.viewers.StructuredSelection} containing one or
     * more of the viewer's EditParts underline EMF objects . If no editparts
     * are selected, root EMF object will return
     * 
     */
    public ISelection getSelection()
    {
        StructuredSelection selection = (StructuredSelection) viewer.getSelection();
        if (!selection.isEmpty())
        {
            List<Object> emfObj = new ArrayList<Object>();
            Object[] array = selection.toArray();
            for (Object object : array)
            {
                if (object instanceof AbstractEditPart)
                {
                    AbstractEditPart abstractEditPart = (AbstractEditPart) object;

                    emfObj.add(abstractEditPart.getModel());
                }
            }
            return new StructuredSelection(emfObj);
        }

        return viewer.getSelection();
    }

    public void setSelectionToViewer(Collection<?> collection)
    {
        evntTrigger.set(false);
        try
        {

            final ArrayList<Object> theSelection = new ArrayList<Object>();
            for (Object object : collection)
            {
                Object part = viewer.getEditPartRegistry().get(object);
                if (part != null)
                {
                    theSelection.add(part);
                }
            }

            if (theSelection != null && !theSelection.isEmpty())
            {
                Runnable runnable = new Runnable()
                {
                    public void run()
                    {

                        if (viewer != null)
                        {
                            viewer.setSelection(new StructuredSelection(theSelection.toArray()));
                        }
                    }
                };
                Display.getCurrent().asyncExec(runnable);
            }
        }
        finally
        {
            evntTrigger.set(true);
        }
    }

    public GraphicalViewer getViewer()
    {
        return viewer;
    }

    public void setModel(Object model)
    {
        try
        {
            evntTrigger.set(false);
            viewer.setContents(model);
        }
        finally
        {
            evntTrigger.set(true);
        }

    }

    public void init(IViewPart view)
    {
        IActionBars actionBars = view.getViewSite().getActionBars();
        IToolBarManager toolBarManager = actionBars.getToolBarManager();

        final UndoAction undoAction = new UndoAction(view);
        toolBarManager.add(undoAction);
        final RedoAction redoAction = new RedoAction(view);
        toolBarManager.add(redoAction);

        viewer.getEditDomain().getCommandStack().addCommandStackListener(new CommandStackListener()
        {

            public void commandStackChanged(EventObject event)
            {
                undoAction.setEnabled(undoAction.isEnabled());
                redoAction.setEnabled(redoAction.isEnabled());
            }
        });

    }
}
