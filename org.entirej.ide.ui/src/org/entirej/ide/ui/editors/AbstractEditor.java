/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors;

import java.io.IOException;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.DefaultOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistory;
import org.eclipse.core.commands.operations.IOperationHistoryListener;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.commands.operations.ObjectUndoContext;
import org.eclipse.core.commands.operations.OperationHistoryEvent;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorSite;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.dialogs.SaveAsDialog;
import org.eclipse.ui.forms.editor.FormEditor;
import org.eclipse.ui.forms.editor.IFormPage;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.nodes.AbstractNode;

public abstract class AbstractEditor extends FormEditor implements IJavaProjectProvider
{

    private boolean                       dirty;
    private FileMonitor                   fileMonitor;
    private final IOperationHistory       operationHistory = new DefaultOperationHistory();
    private final IUndoContext            undoContext;
    private final IResourceChangeListener buildMonitor     = new IResourceChangeListener()
                                                           {
                                                               final IResourceDeltaVisitor deltaVisitor = new IResourceDeltaVisitor()
                                                                                                        {

                                                                                                            public boolean visit(IResourceDelta delta)
                                                                                                                    throws CoreException
                                                                                                            {
                                                                                                                if (delta.getFlags() == IResourceDelta.MARKERS)
                                                                                                                {
                                                                                                                    IResource resource = delta.getResource();
                                                                                                                    if (resource instanceof IFile)
                                                                                                                    {
                                                                                                                        IFile file = (IFile) resource;
                                                                                                                        IFile eFile = getFile();
                                                                                                                        if (eFile != null && file.equals(eFile))
                                                                                                                        {
                                                                                                                            refreshAfterBuid();

                                                                                                                            return false;
                                                                                                                        }
                                                                                                                    }
                                                                                                                }
                                                                                                                return true;
                                                                                                            }
                                                                                                        };

                                                               public void resourceChanged(IResourceChangeEvent event)
                                                               {
                                                                   try
                                                                   {
                                                                       event.getDelta().accept(deltaVisitor);
                                                                   }
                                                                   catch (CoreException e)
                                                                   {
                                                                       EJCoreLog.log(e);
                                                                   }

                                                               }
                                                           };

    public AbstractEditor()
    {
        operationHistory.addOperationHistoryListener(new IOperationHistoryListener()
        {

            public void historyNotification(OperationHistoryEvent event)
            {
                EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                {
                    
                    public void run()
                    {

                        AbstractEditor.this.getContributor().refreah();
                        
                    }
                });
            }
        });
        undoContext = new ObjectUndoContext(this);
    }

    
    public IUndoContext getUndoContext()
    {
        return undoContext;
    }
    
    public IOperationHistory getOperationHistory()
    {
        return operationHistory;
    }
    
    @Override
    public void init(IEditorSite site, IEditorInput input) throws PartInitException
    {

        super.init(site, input);
        IFile file = getFile();
        // hook file with monitor
        connectFileMonitor(file);
        EJUIPlugin.getWorkspace().addResourceChangeListener(buildMonitor, IResourceChangeEvent.POST_CHANGE);
        loadFile(file);

    }

    public void connectFileMonitor(IFile file)
    {
        if (file != null && fileMonitor == null)
        {
            fileMonitor = new FileMonitor(file);
            EJUIPlugin.getWorkspace().addResourceChangeListener(fileMonitor, IResourceChangeEvent.POST_CHANGE);
        }
    }

    @Override
    protected void addPages()
    {
        setActiveEditor(this);
        setPartName(getEditorInput().getName());
        AbstractEditorPage[] abstractEditorPages = getAbstractEditorPages();
        for (AbstractEditorPage abstractEditorPage : abstractEditorPages)
        {
            try
            {
                addPage(abstractEditorPage);
            }
            catch (PartInitException e)
            {
                EJCoreLog.logException(e);
            }
        }

        setActivePage(getActivePageID());
    }

    public abstract AbstractEditorPage[] getAbstractEditorPages();

    public abstract String getActivePageID();

    {

    }

    public abstract void loadFile(IFile file);

    public abstract void refreshFile(IFile file);

    public abstract void saveFile(IFile file, IProgressMonitor monitor) throws IOException;

    {

    }

    @Override
    public void doSave(IProgressMonitor monitor)
    {

        commitPages(true);
        dirty = false;

        IFile file = getFile();
        try
        {
            setFileMonitor(false);
            saveFile(file, monitor);

        }
        catch (IOException e)
        {
            EJCoreLog.log(e);
        }
        finally
        {
            setFileMonitor(true);
        }
        editorDirtyStateChanged();

    }

    @Override
    public void doSaveAs()
    {

        SaveAsDialog saveAsDialog = new SaveAsDialog(getSite().getShell());

        saveAsDialog.setOriginalFile(getFile());
        saveAsDialog.open();
        IPath path = saveAsDialog.getResult();
        if (path != null)
        {

            IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
            if (file != null)
            {
                // remove old file change monitor
                if (fileMonitor != null)
                {
                    EJUIPlugin.getWorkspace().removeResourceChangeListener(fileMonitor);
                }
                setInput(new FileEditorInput(file));
                setPartName(getEditorInput().getName());
                doSave(new NullProgressMonitor());
                // hook file with monitor
                connectFileMonitor(file);
            }
        }

    }

    @Override
    public boolean isSaveAsAllowed()
    {
        return true;
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormEditor#isDirty()
     */
    public boolean isDirty()
    {
        return dirty || super.isDirty();
    }

    public void setDirty(boolean dirty)
    {
        this.dirty = this.dirty || dirty;
        editorDirtyStateChanged();
    }

    private void setFileMonitor(boolean enable)
    {
        if (fileMonitor != null)
            fileMonitor.enable = enable;
    }

    public IFile getFile()
    {
        IEditorInput input = getEditorInput();
        IFile modelFile = null;
        if (input instanceof IFileEditorInput)
        {
            IFileEditorInput fileEditorInput = (IFileEditorInput) input;
            modelFile = fileEditorInput.getFile();
        }

        return modelFile;
    }

    public void refreshAfterBuid()
    {

    }

    public boolean hasProblumsByMarkerType(String type)
    {
        IResource file = getFile();
        if (file != null)
        {
            try
            {
                IMarker[] markers = file.findMarkers(type, true, IResource.DEPTH_ZERO);

                for (IMarker marker : markers)
                {
                    switch (marker.getAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO))
                    {
                        case IMarker.SEVERITY_ERROR:
                        case IMarker.SEVERITY_WARNING:
                            return true;

                        default:
                            break;
                    }
                }
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }
        }

        return false;
    }

    public IMarker[] getMarkers(String type)
    {
        IResource file = getFile();
        if (file != null)
        {
            try
            {
                IMarker[] markers = file.findMarkers(type, true, IResource.DEPTH_ZERO);

                return markers;
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }
        }

        return new IMarker[0];
    }

    @Override
    public Object getAdapter(@SuppressWarnings("rawtypes") Class adapter)
    {
        if (IContentOutlinePage.class.equals(adapter))
        {
            IFormPage activePageInstance = getActivePageInstance();
            if (activePageInstance instanceof AbstractEditorPage)
            {
                return ((AbstractEditorPage) activePageInstance).getContentOutlinePage();
            }

        }
        return super.getAdapter(adapter);
    }

    @Override
    public void dispose()
    {
        if (fileMonitor != null)
        {
            EJUIPlugin.getWorkspace().removeResourceChangeListener(fileMonitor);
        }
        EJUIPlugin.getWorkspace().removeResourceChangeListener(buildMonitor);
        super.dispose();
    }

    public void refresh()
    {

        refreshFile(getFile());
        // mark model reload
        for (Object page : pages)
        {
            if (page instanceof AbstractEditorPage)
            {
                ((AbstractEditorPage) page).refresh();
            }
        }
        // when refresh remove all pending changes
        dirty = false;
        editorDirtyStateChanged();

    }

    public AbstractActionBarContributor getContributor()
    {
        return (AbstractActionBarContributor) getEditorSite().getActionBarContributor();
    }

    /**
     * Resource change listener for the file input to this editor in case it is
     * deleted or moved.
     */
    private class FileMonitor implements IResourceChangeListener, IResourceDeltaVisitor
    {
        IFile   apdfile;
        boolean enable = true;

        public FileMonitor(IFile apdfile)
        {
            this.apdfile = apdfile;
        }

        public void resourceChanged(IResourceChangeEvent event)
        {
            if (enable)
            {
                IResourceDelta delta = event.getDelta();

                try
                {
                    delta.accept(this);
                }
                catch (CoreException e)
                {
                    EJCoreLog.logException(e);
                }
            }

        }

        public boolean visit(IResourceDelta delta) throws CoreException
        {
            // if monitor not enable do not continue
            if (!enable)
            {
                return false;
            }
            IResource resource = delta.getResource();
            if (resource instanceof IFile)
            {
                IFile file = (IFile) resource;
                if (file.equals(apdfile))
                {
                    switch (delta.getKind())
                    {
                        case IResourceDelta.REMOVED:
                        case IResourceDelta.REPLACED:
                        {
                            Display display = getSite().getShell().getDisplay();
                            display.asyncExec(new Runnable()
                            {
                                public void run()
                                {
                                    // file has been deleted/moved close editor
                                    getSite().getPage().closeEditor(AbstractEditor.this, false);
                                }
                            });
                            break;
                        }
                        case IResourceDelta.CHANGED:
                        {
                            if ((delta.getFlags() & IResourceDelta.CONTENT) != 0)
                            {
                                Display display = getSite().getShell().getDisplay();
                                display.asyncExec(new Runnable()
                                {
                                    public void run()
                                    {
                                        refresh();
                                    }
                                });
                            }
                            break;
                        }

                    }

                    return false;
                }
            }
            return true;
        }
    }

    public void execute(IUndoableOperation operation, IProgressMonitor monitor)
    {
        operation.addContext(undoContext);
        try
        {
            operationHistory.execute(operation, monitor, null);
        }
        catch (ExecutionException e)
        {
            EJCoreLog.log(e);
        }
    }

}
