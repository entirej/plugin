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
package org.entirej.ext.oracle.db;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

public class DBProceduresWizardPage extends WizardPage
{

    private final DBTypeSelectionPage typeSelectionPage;

    private Procedure                 insertProcedure;
    private Procedure                 updateProcedure;
    private Procedure                 deleteProcedure;

    public DBProceduresWizardPage(DBTypeSelectionPage typeSelectionPage)
    {
        super("ej.db.procedureselection");
        this.typeSelectionPage = typeSelectionPage;
        setTitle("Insert/Update/Delete Procedures/Functions");
        setDescription("provide insert/update/delete procedures/functions for service.");
    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);
        if (visible)
        {

        }

    }

    public Procedure getInsertProcedure()
    {
        return insertProcedure;
    }

    public Procedure getUpdateProcedure()
    {
        return updateProcedure;
    }

    public Procedure getDeleteProcedure()
    {
        return deleteProcedure;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {
        setErrorMessage(null);
        setMessage(null);

       
        
        if (typeSelectionPage.getProcedure() == null)
        {
            setErrorMessage("Select Function/Procedure not selected.");
            return false;
        }
        ObjectArgument collectionType = typeSelectionPage.getProcedure().getCollectionType();

        final String MSG = "%s [%s] Function/Procedure dose not have an argument matching with Select Collection Type : %s";
        if (!validateCollectionType(insertProcedure, collectionType))
        {
            setErrorMessage(String.format(MSG, "Insert", insertProcedure.getName(), collectionType.tableName));
            return false;
        }
        if (!validateCollectionType(updateProcedure, collectionType))
        {
            setErrorMessage(String.format(MSG, "Update", updateProcedure.getName(), collectionType.tableName));
            return false;
        }
        if (!validateCollectionType(deleteProcedure, collectionType))
        {
            setErrorMessage(String.format(MSG, "Delete", deleteProcedure.getName(), collectionType.tableName));
            return false;
        }

        return true;
    }

    private boolean validateCollectionType(Procedure procedure, ObjectArgument collectionType)
    {
        if (procedure == null)
            return true;

        List<Argument> arguments = procedure.getArguments();
        for (Argument argument : arguments)
        {
            if (argument instanceof ObjectArgument)
            {
                ObjectArgument objectArgument = (ObjectArgument) argument;
                if (objectArgument.tableName != null && collectionType.tableName.equals(objectArgument.tableName))
                {
                    return true;
                }
            }

        }

        return false;
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        int nColumns = 1;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        layout.makeColumnsEqualWidth = true;
        layout.verticalSpacing = 0;

        composite.setLayout(layout);
        createTabControl(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    private void createTabControl(Composite composite)
    {
        TabFolder tabFolder = new TabFolder(composite, SWT.NONE);
        tabFolder.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL));

        createInsertQueryTabPage(tabFolder);
        createUpdateQueryTabPage(tabFolder);
        createDeleteQueryTabPage(tabFolder);

    }

    private void createInsertQueryTabPage(final TabFolder tabFolder)
    {
        TabItem itemSelect = new TabItem(tabFolder, SWT.NULL);
        itemSelect.setText("Insert");
        Composite container = new Composite(tabFolder, SWT.NULL);
        itemSelect.setControl(container);

        container.setLayout(new FillLayout());
        setControl(container);
        createDBViewComponent(container, new SelectCallback()
        {

            public void select(Procedure procedure)
            {
                insertProcedure = procedure;

            }
        });
    }

    private void createUpdateQueryTabPage(final TabFolder tabFolder)
    {
        TabItem itemSelect = new TabItem(tabFolder, SWT.NULL);
        itemSelect.setText("Update");
        Composite container = new Composite(tabFolder, SWT.NULL);
        itemSelect.setControl(container);

        container.setLayout(new FillLayout());
        setControl(container);
        createDBViewComponent(container, new SelectCallback()
        {

            public void select(Procedure procedure)
            {
                updateProcedure = procedure;

            }
        });
    }

    private void createDeleteQueryTabPage(final TabFolder tabFolder)
    {
        TabItem itemSelect = new TabItem(tabFolder, SWT.NULL);
        itemSelect.setText("Delete");
        Composite container = new Composite(tabFolder, SWT.NULL);
        itemSelect.setControl(container);

        container.setLayout(new FillLayout());
        setControl(container);
        createDBViewComponent(container, new SelectCallback()
        {

            public void select(Procedure procedure)
            {
                deleteProcedure = procedure;

            }
        });
    }

    private TreeViewer createDBViewComponent(Composite composite, final SelectCallback callback)
    {
        TreeViewer dbfilteredTree = new TreeViewer(composite, SWT.VIRTUAL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);

        final TreeViewer viewer = dbfilteredTree;
        viewer.setContentProvider(typeSelectionPage.getContentProvider());
        viewer.setLabelProvider(typeSelectionPage.getLabelProvider());
        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                Object node = new Object();
                ISelection selection = viewer.getSelection();
                IStructuredSelection strutruredSelection = (IStructuredSelection) selection;
                if (strutruredSelection.size() == 1 && strutruredSelection.getFirstElement() != null)
                {
                    node = strutruredSelection.getFirstElement();
                }
                callback.select(null);

                if (node instanceof Procedure)
                {
                    callback.select((Procedure) node);
                }
                doUpdateStatus();
            }
        });
        dbfilteredTree.setInput(new Object());
        return dbfilteredTree;
    }

    private static interface SelectCallback
    {
        void select(Procedure procedure);
    }

}
