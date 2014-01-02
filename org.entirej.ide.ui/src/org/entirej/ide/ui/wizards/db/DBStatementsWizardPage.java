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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.entirej.framework.plugin.ui.wizards.utils.SyntaxHighlightListener;

public class DBStatementsWizardPage extends WizardPage
{

    private String insertStatement;
    private String updateStatement;
    private String deleteStatement;

    public DBStatementsWizardPage()
    {
        super("ej.db.statementselection");
        setTitle("Insert/Update/Delete Statements");
        setDescription("provide insert/update/delete statements for service.");
    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);
        if (visible)
        {

        }

    }

    public String getInsertStatement()
    {
        return insertStatement;
    }

    public String getUpdateStatement()
    {
        return updateStatement;
    }

    public String getDeleteStatement()
    {
        return deleteStatement;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {
        setErrorMessage(null);
        setMessage(null);

        return true;
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
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        container.setLayout(gridLayout);
        setControl(container);

        final StyledText statementText = new StyledText(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        statementText.addModifyListener(new SyntaxHighlightListener(statementText, "sql"));

        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.grabExcessHorizontalSpace = true;
        gd.widthHint = 400;
        gd.heightHint = 300;
        statementText.setLayoutData(gd);
        statementText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                insertStatement = statementText.getText();
            }
        });
    }

    private void createUpdateQueryTabPage(final TabFolder tabFolder)
    {
        TabItem itemSelect = new TabItem(tabFolder, SWT.NULL);
        itemSelect.setText("Update");
        Composite container = new Composite(tabFolder, SWT.NULL);
        itemSelect.setControl(container);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        container.setLayout(gridLayout);
        setControl(container);

        final StyledText statementText = new StyledText(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        statementText.addModifyListener(new SyntaxHighlightListener(statementText, "sql"));

        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.grabExcessHorizontalSpace = true;
        gd.widthHint = 400;
        gd.heightHint = 300;
        statementText.setLayoutData(gd);
        statementText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                updateStatement = statementText.getText();
            }
        });
    }

    private void createDeleteQueryTabPage(final TabFolder tabFolder)
    {
        TabItem itemSelect = new TabItem(tabFolder, SWT.NULL);
        itemSelect.setText("Delete");
        Composite container = new Composite(tabFolder, SWT.NULL);
        itemSelect.setControl(container);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        container.setLayout(gridLayout);
        setControl(container);

        final StyledText statementText = new StyledText(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        statementText.addModifyListener(new SyntaxHighlightListener(statementText, "sql"));

        GridData gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.grabExcessHorizontalSpace = true;
        gd.widthHint = 400;
        gd.heightHint = 300;
        statementText.setLayoutData(gd);
        statementText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                deleteStatement = statementText.getText();
            }
        });
    }
}
