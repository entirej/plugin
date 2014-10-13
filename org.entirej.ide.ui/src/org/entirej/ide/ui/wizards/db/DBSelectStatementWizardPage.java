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

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.entirej.framework.core.service.EJTableColumn;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.preferences.EntirejConnectionPreferencePage;
import org.entirej.framework.plugin.ui.wizards.utils.InvalidStatementException;
import org.entirej.framework.plugin.ui.wizards.utils.StatementValidator;
import org.entirej.framework.plugin.ui.wizards.utils.SyntaxHighlightListener;
import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportTableColumn;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.utils.ProjectConnectionFactory;
import org.entirej.ide.ui.wizards.db.DBColumnSelectionPage.ColumnLabelProvider;

public class DBSelectStatementWizardPage extends WizardPage
{

    private String             selectStatement;
    private Set<EJTableColumn> selectedColumns = new HashSet<EJTableColumn>();

    private String             dbError;
    private Connection         conn;

    private boolean            selectStmtValidation;

    public DBSelectStatementWizardPage()
    {
        super("ej.db.select.selection");
        setTitle("Select Statement");
        setDescription("Select columns/statement from database.");
    }

    protected void init(IJavaProject javaProject)
    {
        try
        {
            if (conn != null && !conn.isClosed())
            {
                conn.close();
                conn = null;
            }
            try
            {
                conn = ProjectConnectionFactory.createConnection(javaProject);
            }
            catch (EJDevFrameworkException e)
            {
                if (MessageDialog.openQuestion(EJUIPlugin.getActiveWorkbenchShell(), "Connection Error",
                        String.format("%s \n\n Change Project Connection Settings ?",e.getMessage())))
                {
                    EntirejConnectionPreferencePage.openPage(javaProject.getProject());
                    conn = ProjectConnectionFactory.createConnection(javaProject);
                }
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);
        if (visible)
        {

        }

    }

    @Override
    public void dispose()
    {
        try
        {
            if (conn != null && !conn.isClosed())
            {
                conn.close();
                conn = null;
            }
        }
        catch (SQLException e)
        {
            EJCoreLog.logException(e);
        }
        super.dispose();
    }

    public String getSelectStatement()
    {
        return selectStatement;
    }

    public Set<EJTableColumn> getSelectedColumns()
    {
        return new HashSet<EJTableColumn>(selectedColumns);
    }

    public List<EJTableColumn> getColumns()
    {
        return new ArrayList<EJTableColumn>(selectedColumns);
    }

    
    public List<EJReportTableColumn> getReportColumns()
    {
        List<EJReportTableColumn> columns =  new ArrayList<EJReportTableColumn>();
        
        for (EJTableColumn column : selectedColumns)
        {
            EJReportTableColumn reportTableColumn = new EJReportTableColumn();
            reportTableColumn.setArray(column.isArray());
            reportTableColumn.setDatatypeName(column.getDatatypeName());
            reportTableColumn.setName(column.getName());
           
            
            columns.add(reportTableColumn);
            
        }
        
        return columns;
    }
    
    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {
        setErrorMessage(null);
        setMessage(null);
        if (dbError != null)
        {
            setErrorMessage(dbError);
            return false;
        }
        if (selectStatement == null || selectStatement.trim().length() == 0)
        {
            setErrorMessage("Select statement can't be empty.");
            return false;
        }
        if (!selectStmtValidation)
        {
            setMessage("Please validate select statement.");
            return false;
        }

        if (selectedColumns.size() == 0)
        {
            setErrorMessage("Columns can't be empty.");
            return false;
        }

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

        createSelectQueryTabPage(tabFolder);

    }

    private void createSelectQueryTabPage(final TabFolder tabFolder)
    {
        TabItem itemSelect = new TabItem(tabFolder, SWT.NULL);
        itemSelect.setText("Select");
        final TabItem itemColumns = new TabItem(tabFolder, SWT.NULL);
        itemColumns.setText("Columns");

        Composite container = new Composite(tabFolder, SWT.NULL);
        itemSelect.setControl(container);
        GridLayout gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        container.setLayout(gridLayout);
        final CheckboxTableViewer listViewer = CheckboxTableViewer.newCheckList(container, SWT.BORDER);
        GridData listGridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        listViewer.setComparator(new ViewerComparator()
        {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2)
            {
                if (e1 instanceof EJTableColumn && e2 instanceof EJTableColumn)
                {
                    EJTableColumn column1 = (EJTableColumn) e1;
                    EJTableColumn column2 = (EJTableColumn) e2;
                    String name1 = column1.getName();
                    String name2 = column2.getName();
                    if (name1 == null)
                    {
                        name1 = "";//$NON-NLS-1$
                    }
                    if (name2 == null)
                    {
                        name2 = "";//$NON-NLS-1$
                    }
                    return name1.compareToIgnoreCase(name2);
                }
                return super.compare(viewer, e1, e2);
            }

        });
        listViewer.getTable().setLayoutData(listGridData);
        listViewer.getTable().setFont(container.getFont());
        listViewer.setContentProvider(new ITreeContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {

            }

            public void dispose()
            {
            }

            public boolean hasChildren(Object element)
            {
                return false;
            }

            public Object getParent(Object element)
            {
                return null;
            }

            public Object[] getElements(Object inputElement)
            {
                if (inputElement instanceof List)
                {
                    return ((List<?>) inputElement).toArray();
                }
                return new Object[0];
            }

            public Object[] getChildren(Object parentElement)
            {
                return new Object[0];
            }
        });
        listViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new ColumnLabelProvider()));
        listViewer.addCheckStateListener(new ICheckStateListener()
        {

            public void checkStateChanged(CheckStateChangedEvent event)
            {
                Object element = event.getElement();
                if (element instanceof EJTableColumn)
                {
                    EJTableColumn column = (EJTableColumn) element;
                    if (event.getChecked())
                        selectedColumns.add(column);

                    else
                        selectedColumns.remove(column);

                    doUpdateStatus();
                }

            }
        });

        itemColumns.setControl(container);

        ToolBar toolbar = new ToolBar(container, SWT.FLAT | SWT.RIGHT);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);

        toolbar.setLayoutData(gd);
        // create toolbar buttons
        ToolItem selectAll = new ToolItem(toolbar, SWT.PUSH);
        selectAll.setImage(EJUIImages.getImage(EJUIImages.DESC_SELECT_ALL));
        selectAll.setToolTipText("Select All");
        selectAll.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (listViewer != null)
                {
                    ITreeContentProvider contentProvider = (ITreeContentProvider) listViewer.getContentProvider();
                    Object[] elements = contentProvider.getElements(listViewer.getInput());
                    selectedColumns.clear();
                    for (Object object : elements)
                    {

                        if (object instanceof EJTableColumn)
                        {
                            selectedColumns.add((EJTableColumn) object);
                        }
                    }
                    listViewer.setAllChecked(true);
                    doUpdateStatus();
                }

            }
        });
        ToolItem deselectAll = new ToolItem(toolbar, SWT.PUSH);
        deselectAll.setImage(EJUIImages.getImage(EJUIImages.DESC_DESELECT_ALL));
        deselectAll.setToolTipText("Deselect All");
        deselectAll.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (listViewer != null)
                {
                    selectedColumns.clear();
                    listViewer.setAllChecked(false);
                    doUpdateStatus();
                }

            }
        });

        container = new Composite(tabFolder, SWT.NULL);
        itemSelect.setControl(container);
        gridLayout = new GridLayout();
        gridLayout.numColumns = 1;
        container.setLayout(gridLayout);
        setControl(container);

        final StyledText statementText = new StyledText(container, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        statementText.addModifyListener(new SyntaxHighlightListener(statementText, "sql"));
        statementText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                selectStatement = statementText.getText();
                selectedColumns.clear();
                listViewer.setInput(null);
                selectStmtValidation = false;
                doUpdateStatus();
            }
        });

        gd = new GridData();
        gd.horizontalAlignment = SWT.FILL;
        gd.verticalAlignment = SWT.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.grabExcessHorizontalSpace = true;
        gd.widthHint = 400;
        gd.heightHint = 300;
        statementText.setLayoutData(gd);

        Button validateButton = new Button(container, SWT.PUSH);
        validateButton.setText("Validate");
        validateButton.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                IRunnableWithProgress loadColumns = new IRunnableWithProgress()
                {

                    public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                    {
                        try
                        {
                            dbError = null;
                            selectedColumns.clear();
                            listViewer.setInput(StatementValidator.validateStatement(conn, selectStatement));
                            selectStmtValidation = true;

                        }
                        catch (InvalidStatementException e)
                        {
                            dbError = e.getMessage();
                            listViewer.setInput(new Object());
                        }

                    }

                };
                try
                {
                    getContainer().run(false, false, loadColumns);

                }
                catch (Exception e)
                {
                    dbError = e.getMessage();
                }
                finally
                {
                    doUpdateStatus();
                    if (selectStmtValidation)
                    {
                        tabFolder.setSelection(itemColumns);
                    }

                }

            }
        });
        validateButton.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));
    }
}
