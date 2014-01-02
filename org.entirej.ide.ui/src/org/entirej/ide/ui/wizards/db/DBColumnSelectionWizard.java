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
package org.entirej.ide.ui.wizards.db;

import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.entirej.framework.core.service.EJTableColumn;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class DBColumnSelectionWizard extends NewWizard
{
    private final IJavaProject    javaProject;
    private DBColumnSelectionPage columnSelectionPage;

    private int                   selection = Window.CANCEL;

    private List<EJTableColumn>   columns;
    private Table                 table;

    public DBColumnSelectionWizard(IJavaProject javaProject)
    {
        // setDialogSettings(EJUIPlugin.getDefault().getDialogSettings());
        setWindowTitle("Select DB Columns");
        this.javaProject = javaProject;
    }

    public int open()
    {

        WizardDialog dialog = new WizardDialog(EJUIPlugin.getActiveWorkbenchShell(), this);
        dialog.setMinimumPageSize(600, 400);
        dialog.open();
        return selection;
    }

    public List<EJTableColumn> getColumns()
    {

        return columns;
    }

    @Override
    public void addPages()
    {
        columnSelectionPage = new DBColumnSelectionPage();
        columnSelectionPage.init(javaProject);
        addPage(columnSelectionPage);

        super.addPages();
    }

    @Override
    protected void finishPage(IProgressMonitor monitor) throws InterruptedException, CoreException
    {
        table = columnSelectionPage.getSelectedTable();
        columns = columnSelectionPage.getColumns();
        selection = Window.OK;
    }

    String getTable()
    {
        return table != null ? table.getName() : null;
    }

}
