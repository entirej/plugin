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
package org.entirej.ide.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.actions.WorkspaceModifyOperation;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIPlugin;

public abstract class NewWizard extends Wizard implements INewWizard
{
    private org.eclipse.ui.IWorkbench                      workbench;
    private org.eclipse.jface.viewers.IStructuredSelection selection;
    private Map<?, ?>                                      defaultValues;

    public NewWizard()
    {
        super();
        setWindowTitle("New");
        setNeedsProgressMonitor(true);
    }

    public org.eclipse.jface.viewers.IStructuredSelection getSelection()
    {
        return selection;
    }

    public IWorkbench getWorkbench()
    {
        return workbench;
    }

    public void init(IWorkbench workbench, IStructuredSelection selection)
    {
        this.workbench = workbench;
        this.selection = selection;
    }

    /**
     * Subclasses should override to perform the actions of the wizard. This
     * method is run in the wizard container's context as a workspace runnable.
     * 
     * @param monitor
     *            the progress monitor
     * @throws InterruptedException
     *             when the operation is cancelled
     * @throws CoreException
     *             if the element cannot be created
     */
    protected abstract void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException;

    /**
     * Returns the scheduling rule for creating the element.
     * 
     * @return returns the scheduling rule
     */
    protected ISchedulingRule getSchedulingRule()
    {
        return ResourcesPlugin.getWorkspace().getRoot(); // look all by default
    }

    protected boolean canRunForked()
    {
        return true;
    }

    /*
     * @see Wizard#performFinish
     */
    public boolean performFinish()
    {

        try
        {
            ISchedulingRule rule = getSchedulingRule();

            WorkspaceModifyOperation runnable = new WorkspaceModifyOperation(rule)
            {

                @Override
                protected void execute(IProgressMonitor monitor) throws CoreException, InvocationTargetException, InterruptedException
                {
                    finishPage(monitor);

                }
            };

            getContainer().run(canRunForked(), true, runnable);
        }
        catch (InvocationTargetException e)
        {
            EJCoreLog.logException(e);
            return false;
        }
        catch (InterruptedException e)
        {
            return false;
        }
        return true;
    }

    public void openResource(final IFile resource)
    {
        final IWorkbenchPage activePage = EJUIPlugin.getActiveWorkbenchWindow().getActivePage();
        if (activePage != null)
        {
            final Display display = getShell().getDisplay();
            if (display != null)
            {
                display.asyncExec(new Runnable()
                {
                    public void run()
                    {
                        try
                        {
                            IDE.openEditor(activePage, resource, true);
                        }
                        catch (PartInitException e)
                        {
                            EJCoreLog.logException(e);
                        }
                    }
                });
            }
        }
    }

    public void selectAndReveal(IResource newResource)
    {
        BasicNewResourceWizard.selectAndReveal(newResource, workbench.getActiveWorkbenchWindow());
    }

    public final String getDefaultValue(String key)
    {
        if (defaultValues == null)
            return null;
        return (String) defaultValues.get(key);
    }

    public final void init(Map<?, ?> defaultValues)
    {
        this.defaultValues = defaultValues;
    }
}
