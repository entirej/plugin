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

public class BlockItemWizard extends NewWizard
{

    private int                          selection = Window.CANCEL;
    private BlockItemSelectionPage       selectionPage;
    private final BlockItemWizardContext wizardContext;

    public BlockItemWizard(BlockItemWizardContext wizardContext)
    {
        // setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle("New Report Block Item");
        this.wizardContext = wizardContext;
    }

    public int open()
    {
        setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        WizardDialog dialog = new WizardDialog(EJUIPlugin.getActiveWorkbenchShell(), this);

        dialog.open();
        return selection;
    }

    @Override
    public void addPages()
    {
        addPage(selectionPage = new BlockItemSelectionPage(wizardContext));
        super.addPages();
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {

        wizardContext.addBlock(selectionPage.getBlockItemName(), selectionPage.getDataTypeClass(), 
                selectionPage.isServiceItem());

    }

}