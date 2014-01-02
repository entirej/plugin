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

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
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
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.framework.core.service.EJTableColumn;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.preferences.EntirejConnectionPreferencePage;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.utils.ProjectConnectionFactory;

public class DBColumnSelectionPage extends WizardPage
{

    private IJavaProject        javaProject;

    private TreeViewer          dbfilteredTree;
    private CheckboxTableViewer listViewer;

    private Table               selectedTable;
    private Set<EJTableColumn>  selectedColumns = new HashSet<EJTableColumn>();

    private String              dbError;
    private Connection          conn;

    public DBColumnSelectionPage()
    {
        super("ej.db.columnselection");
        setTitle("Column Selection");
        setDescription("Select columns from db table.");
    }

    protected void init(IJavaProject javaProject)
    {
        this.javaProject = javaProject;
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

            if (dbfilteredTree != null)
                dbfilteredTree.setInput(getDBInput());
            doUpdateStatus();
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

    public Table getSelectedTable()
    {
        return selectedTable;
    }

    public Set<EJTableColumn> getSelectedColumns()
    {
        return new HashSet<EJTableColumn>(selectedColumns);
    }

    public List<EJTableColumn> getColumns()
    {
        return new ArrayList<EJTableColumn>(selectedColumns);
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        int nColumns = 2;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        layout.makeColumnsEqualWidth = true;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        createDBViewComponent(composite);
        createListViewer(composite);
        createEmptySpace(composite, 1);
        createToolbar(composite);

        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    private void createToolbar(Composite parent)
    {

        ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
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
                if (selectedTable != null && listViewer != null)
                {
                    ITreeContentProvider contentProvider = (ITreeContentProvider) listViewer.getContentProvider();
                    Object[] elements = contentProvider.getElements(selectedTable);
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
                if (selectedTable != null && listViewer != null)
                {
                    selectedColumns.clear();
                    listViewer.setAllChecked(false);
                    doUpdateStatus();
                }

            }
        });
    }

    public static Control createEmptySpace(Composite parent, int span)
    {
        Label label = new Label(parent, SWT.LEFT);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.horizontalIndent = 0;
        gd.widthHint = 0;
        gd.heightHint = 0;
        label.setLayoutData(gd);
        return label;
    }

    protected void createListViewer(Composite composite)
    {
        listViewer = CheckboxTableViewer.newCheckList(composite, SWT.BORDER);

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
        GridData listGridData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);

        listViewer.getTable().setLayoutData(listGridData);
        listViewer.getTable().setFont(composite.getFont());
        listViewer.setContentProvider(new DBColumnContentProvider());
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
    }

    private void createDBViewComponent(Composite composite)
    {
        dbfilteredTree = new TreeViewer(composite, SWT.VIRTUAL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI);

        GridData treeGD = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        treeGD.widthHint = 250;
        treeGD.heightHint = 300;
        dbfilteredTree.getControl().setLayoutData(treeGD);
        final LabelProvider provider = new LabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof Schema)
                {
                    String schemaName = ((Schema) element).getName();
                    return schemaName == null ? "db" : schemaName;
                }
                if (element instanceof Table)
                {
                    return ((Table) element).getName();
                }
                if (element instanceof Group)
                {
                    return ((Group) element).name;
                }
                return super.getText(element);
            }

            @Override
            public Image getImage(Object element)
            {
                if (element instanceof Schema)
                {
                    return EJUIImages.getImage(EJUIImages.DESC_SCHEMA);
                }
                if (element instanceof Table)
                {
                    return EJUIImages.getImage(EJUIImages.DESC_TABLE);
                }
                if (element instanceof Group)
                {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                }
                return super.getImage(element);
            }
        };
        dbfilteredTree.setComparator(new ViewerComparator()
        {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2)
            {

                String name1 = provider.getText(e1);
                String name2 = provider.getText(e2);
                return name1.compareToIgnoreCase(name2);

            }

        });
        final TreeViewer viewer = dbfilteredTree;
        viewer.setContentProvider(new DBIContentProvider());
        viewer.setLabelProvider(provider);
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
                selectedTable = null;
                selectedColumns.clear();
                if (listViewer != null)
                {
                    listViewer.setInput(node);
                }
                if (node instanceof Table)
                {
                    selectedTable = (Table) node;

                }
                doUpdateStatus();
            }
        });
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {
        if (dbError != null)
        {
            setErrorMessage(dbError);
            return false;
        }
        if (selectedTable == null)
        {
            setErrorMessage("Table not selected.");
            return false;
        }
        if (selectedColumns.size() == 0)
        {
            setErrorMessage("Selected columns can't be empty.");
            return false;
        }
        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    private Object getDBInput()
    {
        return new Object();
    }

    private static class Group
    {
        private final String   name;
        private final Object[] items;

        public Group(String name, Object[] items)
        {
            this.name = name;
            this.items = items;
        }

        public Object[] getItems()
        {
            return items;
        }
    }

    static class ColumnLabelProvider extends LabelProvider implements ILabelProvider, DelegatingStyledCellLabelProvider.IStyledLabelProvider
    {

        @Override
        public String getText(Object element)
        {
            return getStyledText(element).toString();
        }

        public StyledString getStyledText(Object element)
        {
            StyledString ss = new StyledString();
            if (element instanceof EJTableColumn)
            {
                EJTableColumn column = (EJTableColumn) element;
                ss.append(column.getName());
                if (column.getDatatypeName() != null)
                {
                    ss.append(" - ", StyledString.QUALIFIER_STYLER);
                    ss.append(" [ ", StyledString.QUALIFIER_STYLER);
                    ss.append(column.getDatatypeName(), StyledString.COUNTER_STYLER);
                    ss.append(" ] ", StyledString.QUALIFIER_STYLER);
                }
            }
            return ss;
        }

    }

    private class DBColumnContentProvider implements ITreeContentProvider
    {

        private Object   inputElement;
        private Object[] elements;

        public void dispose()
        {
            elements = null;
            inputElement = null;
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {

        }

        public Object[] getElements(Object inputElement)
        {
            if (this.inputElement == inputElement && elements != null)
                return elements;

            this.inputElement = inputElement;

            if (inputElement instanceof Table)
            {
                final Table table = (Table) inputElement;
                final List<EJTableColumn> columns = new ArrayList<EJTableColumn>();

                IRunnableWithProgress loadColumns = new IRunnableWithProgress()
                {
                    public Class<?> getDataTypeForJdbcType(int jdbcType)
                    {
                        switch (jdbcType)
                        {
                            case Types.BOOLEAN:
                            case Types.BIT:
                                return Boolean.class;
                            case Types.CHAR:
                            case Types.VARCHAR:
                            case Types.NCHAR:
                            case Types.NVARCHAR:
                            case Types.LONGVARCHAR:
                                return String.class;
                            case Types.REAL:
                                return Float.class;
                            case Types.FLOAT:
                            case Types.DOUBLE:
                                return Double.class;
                            case Types.NUMERIC:
                            case Types.DECIMAL:
                                return BigDecimal.class;
                            case Types.TINYINT:
                            case Types.SMALLINT:
                            case Types.INTEGER:
                                return Integer.class;
                            case Types.BIGINT:
                                return Long.class;
                            case Types.BINARY:
                            case Types.TIMESTAMP:
                                return Timestamp.class;
                            case Types.DATE:
                                return Date.class;
                            case Types.TIME:
                                return Time.class;
                            case Types.VARBINARY:
                            case Types.LONGVARBINARY:
                            default:
                                return Object.class;
                        }
                    }

                    public void run(IProgressMonitor monitor)
                    {
                        try
                        {

                            monitor.beginTask("Loading table columns ...", 3);

                            if (conn != null && !conn.isClosed())
                            {
                                dbError = null;
                                monitor.worked(1);
                                ResultSet rset = null;
                                try
                                {
                                    rset = conn.getMetaData().getColumns(null, table.getSchema().getName(), table.getName(), null);
                                    monitor.worked(1);
                                    // - TABLE_CAT String => table catalog (may
                                    // be null)
                                    // - TABLE_SCHEM String => table schema (may
                                    // be null)
                                    // - TABLE_NAME String => table name
                                    // - COLUMN_NAME String => column name
                                    // - DATA_TYPE int => SQL type from
                                    // java.sql.Types

                                    String itemName;
                                    int dataType;
                                    while (rset.next())
                                    {
                                        itemName = rset.getString("COLUMN_NAME");
                                        dataType = rset.getInt("DATA_TYPE");

                                        EJTableColumn tableColumn = new EJTableColumn();
                                        tableColumn.setName(itemName);

                                        Class<?> type = getDataTypeForJdbcType(dataType);
                                        if (type != null)
                                        {
                                            tableColumn.setDatatypeName(type.getName());
                                            type = null;
                                        }
                                        else
                                        {
                                            tableColumn.setDatatypeName(String.class.getName());
                                        }

                                        columns.add(tableColumn);
                                    }
                                    monitor.worked(1);
                                }
                                finally
                                {
                                    if (rset != null)
                                    {
                                        rset.close();

                                    }

                                }
                            }
                        }
                        catch (Exception e)
                        {
                            dbError = e.getMessage();

                        }
                        finally
                        {
                            doUpdateStatus();
                            monitor.done();
                        }
                    }
                };
                setPageComplete(false);
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
                }
                return elements = columns.toArray();
            }
            return new Object[0];
        }

        public Object[] getChildren(Object parentElement)
        {
            return null;
        }

        public Object getParent(Object element)
        {
            if (element instanceof Table)
            {
                return ((Table) element).getSchema();
            }
            return null;
        }

        public boolean hasChildren(Object element)
        {
            return false;
        }

    }

    private class DBIContentProvider implements ITreeContentProvider
    {
        private Object[] objects;

        public void dispose()
        {
            objects = null;

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {

        }

        public Object[] getElements(Object inputElement)
        {
            if (objects != null)
                return objects;
            final List<Schema> schemas = new ArrayList<Schema>();
            final Object[] schemasRoots = new Object[2];
            IRunnableWithProgress loadSchemas = new IRunnableWithProgress()
            {
                public void run(IProgressMonitor monitor)

                {
                    try
                    {

                        monitor.beginTask("Loading database schemas ...", 3);

                        if (conn != null && !conn.isClosed())
                        {
                            String ownerSchema = ProjectConnectionFactory.getDBSchema(javaProject);
                            if (ownerSchema != null && ownerSchema.trim().length() > 0)
                            {
                                schemasRoots[0] = new Schema(ownerSchema);
                            }
                            dbError = null;
                            monitor.worked(1);
                            ResultSet rset = null;
                            try
                            {
                                rset = conn.getMetaData().getSchemas();
                                monitor.worked(1);
                                while (rset.next())
                                {
                                    Schema schema = new Schema(rset.getString("TABLE_SCHEM"));
                                    schemas.add(schema);

                                }
                                monitor.worked(1);
                            }
                            finally
                            {
                                if (rset != null)
                                {
                                    rset.close();

                                }

                            }
                        }
                    }
                    catch (Exception e)
                    {
                        dbError = e.getMessage();

                    }
                    finally
                    {
                        monitor.done();
                        doUpdateStatus();
                    }
                }
            };

            setPageComplete(false);
            try
            {
                getContainer().run(false, false, loadSchemas);
            }
            catch (Exception e)
            {
                dbError = e.getMessage();

            }
            finally
            {
                doUpdateStatus();
            }
            if (schemas.size() > 0)
            {
                if (schemasRoots[0] != null)
                {
                    schemasRoots[1] = new Group("All Schemas", schemas.toArray());
                    return objects = schemasRoots;
                }
                return objects = schemas.toArray();
            }

            // use default tables
            return getChildren(new Schema(null));

        }

        public Object[] getChildren(Object parentElement)
        {
            if (parentElement instanceof Group)
            {
                return ((Group) parentElement).getItems();
            }
            if (parentElement instanceof Schema)
            {
                final Schema schema = ((Schema) parentElement);
                final List<Table> tables = new ArrayList<Table>();
                final List<Table> views = new ArrayList<Table>();

                IRunnableWithProgress loadTables = new IRunnableWithProgress()
                {
                    public void run(IProgressMonitor monitor)
                    {
                        try
                        {

                            monitor.beginTask("Loading database tables/views ...", 3);

                            if (conn != null && !conn.isClosed())
                            {
                                dbError = null;
                                monitor.worked(1);
                                // read all tables
                                ResultSet rset = null;
                                try
                                {
                                    rset = conn.getMetaData().getTables(null, schema.getName(), null, new String[] { "TABLE" });

                                    String itemName;
                                    while (rset.next())
                                    {
                                        itemName = rset.getString("TABLE_NAME");
                                        tables.add(new Table(itemName, schema));

                                    }
                                    monitor.worked(1);
                                }
                                finally
                                {
                                    if (rset != null)
                                    {
                                        rset.close();

                                    }

                                }
                                monitor.worked(1);
                                // read all views
                                try
                                {
                                    rset = conn.getMetaData().getTables(null, schema.getName(), null, new String[] { "VIEW" });

                                    String itemName;
                                    while (rset.next())
                                    {
                                        itemName = rset.getString("TABLE_NAME");
                                        views.add(new Table(itemName, schema));

                                    }
                                    monitor.worked(1);
                                }
                                finally
                                {
                                    if (rset != null)
                                    {
                                        rset.close();

                                    }

                                }
                            }
                        }
                        catch (Exception e)
                        {
                            dbError = e.getMessage();

                        }
                        finally
                        {
                            doUpdateStatus();
                            monitor.done();
                        }
                    }

                };
                setPageComplete(false);
                try
                {
                    getContainer().run(false, false, loadTables);
                }
                catch (Exception e)
                {
                    dbError = e.getMessage();

                }
                finally
                {
                    doUpdateStatus();
                }
                return new Object[] { new Group("tables", tables.toArray()), new Group("views", views.toArray()) };
            }
            return new Object[] {};
        }

        public Object getParent(Object element)
        {
            if (element instanceof Table)
            {
                return ((Table) element).getSchema();
            }
            return null;
        }

        public boolean hasChildren(Object element)
        {
            if (element instanceof Schema)
            {
                return true;
            }
            if (element instanceof Group)
            {
                return true;
            }
            return false;
        }

    }
}
