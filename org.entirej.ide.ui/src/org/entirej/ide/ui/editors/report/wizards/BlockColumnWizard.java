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
package org.entirej.ide.ui.editors.report.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class BlockColumnWizard extends NewWizard
{

    private int                          selection = Window.CANCEL;
    private BlockColumnSelectionPage       selectionPage;
    private final BlockColumnWizardContext wizardContext;

    public BlockColumnWizard(BlockColumnWizardContext wizardContext)
    {
        // setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle("New Report Block Column");
        this.wizardContext = wizardContext;
    }

    public int open()
    {
        setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        WizardDialog dialog = new WizardDialog(EJUIPlugin.getActiveWorkbenchShell(), this);
        dialog.create();
        dialog.getShell().setSize(500, 600);
        dialog.open();
        return selection;
    }

    @Override
    public void addPages()
    {
        addPage(selectionPage = new BlockColumnSelectionPage(wizardContext));
        super.addPages();
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {

        wizardContext.addBlockColumn(selectionPage.getBlockColumnName(), selectionPage.getBlockColumnLabel(),selectionPage.getWidth(), 
                selectionPage.getItemProperties());

    }

}
