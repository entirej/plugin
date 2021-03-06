/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.wizards.form;

import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;

public class NewFormAction implements IWorkbenchWindowActionDelegate
{
    private IStructuredSelection _selection;
    private IWorkbenchWindow     workbenchWindow;

    public void init(IWorkbenchWindow workbenchWindow)
    {
        this.workbenchWindow = workbenchWindow;
    }

    public void run(IAction action)
    {
        NewEntireJFormWizard wizard = new NewEntireJFormWizard();
        wizard.init(workbenchWindow.getWorkbench(), _selection);

        WizardDialog dialog = new WizardDialog(workbenchWindow.getShell(), wizard);
        dialog.open();
    }

    public void selectionChanged(IAction action, ISelection selection)
    {
        if (selection != null && selection instanceof IStructuredSelection)
        {
            _selection = (IStructuredSelection) selection;
        }
        else
        {
            _selection = null;
        }
    }

    public void dispose()
    {
        _selection = null;

    }

}
