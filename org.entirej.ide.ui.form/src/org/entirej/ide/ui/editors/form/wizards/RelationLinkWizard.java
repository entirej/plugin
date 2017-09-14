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
package org.entirej.ide.ui.editors.form.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class RelationLinkWizard extends NewWizard
{

    private int                             selection = Window.CANCEL;
    private RelationLinkSelectionPage       selectionPage;
    private final RelationLinkWizardContext wizardContext;

    public RelationLinkWizard(RelationLinkWizardContext wizardContext)
    {
        // setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle("New Relation Join");
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
        addPage(selectionPage = new RelationLinkSelectionPage(wizardContext));
        super.addPages();
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {

        wizardContext.addRelationLink(selectionPage.getMasterBlock(), selectionPage.getDetailsBlock());

    }

}
