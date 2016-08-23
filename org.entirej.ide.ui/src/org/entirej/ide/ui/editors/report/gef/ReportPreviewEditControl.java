package org.entirej.ide.ui.editors.report.gef;

import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.draw2d.ColorConstants;
import org.eclipse.gef.EditDomain;
import org.eclipse.gef.GraphicalViewer;
import org.eclipse.gef.commands.CommandStackListener;
import org.eclipse.gef.editparts.AbstractEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.eclipse.gef.ui.actions.RedoAction;
import org.eclipse.gef.ui.actions.UndoAction;
import org.eclipse.gef.ui.parts.ScrollingGraphicalViewer;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IActionBars;
import org.eclipse.ui.IViewPart;
import org.entirej.ide.ui.editors.report.AbstractEJReportEditor;

public class ReportPreviewEditControl extends Composite
{

    private final EditDomain             editDomain;
    private final GraphicalViewer        viewer;

    public ReportPreviewEditControl(final AbstractEJReportEditor editor, Composite parent)
    {
        super(parent, SWT.NONE);
        setLayout(new FillLayout());

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
        final Collection<?> theSelection = collection;

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

    public GraphicalViewer getViewer()
    {
        return viewer;
    }

    public void setModel(Object model)
    {
        viewer.setContents(model);

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
