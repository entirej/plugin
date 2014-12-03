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
package org.entirej.ide.ui.wizards.custom;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.entirej.framework.core.service.EJTableColumn;
import org.entirej.framework.report.service.EJReportTableColumn;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public class CustomFieldsPage extends WizardPage implements IJavaProjectProvider
{

    private IJavaProject       javaProject;

    private TableViewer        listViewer;

    private Set<EJTableColumn> selectedColumns = new HashSet<EJTableColumn>();

    public CustomFieldsPage()
    {
        super("ej.custom.fields");
        setTitle("Fields");
        setDescription("Add custom fields.");
    }

    protected void init(IJavaProject javaProject)
    {
        this.javaProject = javaProject;

    }

    public void refresh()
    {
        if (listViewer != null)
        {
            listViewer.setInput(new Object());
        }
    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);
        if (visible)
        {
            doUpdateStatus();
        }

    }

    @Override
    public void dispose()
    {

        super.dispose();
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

    public List<EJTableColumn> getColumns()
    {
        return new ArrayList<EJTableColumn>(selectedColumns);
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
        createListViewer(composite);
        createToolbar(composite);

        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    private void createToolbar(final Composite parent)
    {

        ToolBar toolbar = new ToolBar(parent, SWT.FLAT | SWT.RIGHT);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);

        toolbar.setLayoutData(gd);
        // create toolbar buttons
        ToolItem selectAll = new ToolItem(toolbar, SWT.PUSH);
        selectAll.setImage(EJUIImages.getImage(EJUIImages.DESC_ADD_ITEM));
        selectAll.setToolTipText("Select All");
        selectAll.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (listViewer != null)
                {
                    FieldDialog dialog = new FieldDialog(parent.getShell(), null);
                    if (dialog.open() == Window.OK)
                    {
                        selectedColumns.add(dialog.getEJTableColumn());
                        refresh();
                        doUpdateStatus();
                    }

                }

            }
        });
        ToolItem deselectAll = new ToolItem(toolbar, SWT.PUSH);
        deselectAll.setImage(EJUIImages.getImage(EJUIImages.DESC_DELETE_ITEM));
        deselectAll.setToolTipText("Deselect All");
        deselectAll.addSelectionListener(new SelectionAdapter()
        {

            @Override
            public void widgetSelected(SelectionEvent e)
            {
                if (listViewer != null)
                {
                    IStructuredSelection selection = (IStructuredSelection) listViewer.getSelection();
                    Object[] items = selection.toArray();

                    if (items.length > 0)
                    {
                        selectedColumns.removeAll(Arrays.asList(items));
                        refresh();
                        doUpdateStatus();
                    }
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
        listViewer = new TableViewer(composite, SWT.BORDER);
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
        listViewer.getTable().setFont(composite.getFont());
        listViewer.setContentProvider(new DBColumnContentProvider());
        listViewer.setLabelProvider(new DelegatingStyledCellLabelProvider(new ColumnLabelProvider()));

    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (selectedColumns.size() == 0)
        {
            setErrorMessage("Fields can't be empty.");
            return false;
        }
        setErrorMessage(null);
        setMessage(null);
        return true;
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

        public void dispose()
        {
        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {

        }

        public Object[] getElements(Object inputElement)
        {

            return selectedColumns.toArray();
        }

        public Object[] getChildren(Object parentElement)
        {
            return null;
        }

        public Object getParent(Object element)
        {

            return null;
        }

        public boolean hasChildren(Object element)
        {
            return false;
        }

    }

    private class FieldDialog extends TitleAreaDialog
    {

        private String        message = "";

        private EJTableColumn field;

        private Text          nameText;
        private Text          dataTypeText;

        public FieldDialog(Shell parentShell, EJTableColumn applicationParameter)
        {
            super(parentShell);
            setShellStyle(SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
            setHelpAvailable(false);
            this.field = applicationParameter;
        }

        @Override
        protected Control createContents(Composite parent)
        {
            Control contents = super.createContents(parent);

            setTitle("Custom Field");
            if (field == null)
            {

                message = "Add a field.";
                getShell().setText("Add Field");
                setMessage(message);
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            }
            else
            {
                message = "Edit field";
                getShell().setText("Edit Field");
                validate();
            }

            return contents;
        }

        @Override
        protected Control createDialogArea(Composite parent)
        {

            Composite body = new Composite(parent, SWT.BORDER);
            body.setLayout(new GridLayout());
            GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
            body.setLayoutData(sectionData);
            buildControls(body);

            return parent;
        }

        private void buildControls(Composite parent)
        {
            // TODO: rework layout code
            Composite container = new Composite(parent, SWT.NULL);
            GridLayout layout = new GridLayout();
            container.setLayout(layout);
            container.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL));
            layout.numColumns = 4;
            layout.verticalSpacing = 9;

            Label nameLabel = new Label(container, SWT.NULL);
            nameLabel.setText("Field  Name:");
            nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
            if (field != null && field.getName() != null)
                nameText.setText(field.getName());
            GridData gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 3;
            nameText.setLayoutData(gd);
            nameText.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    validate();
                }
            });

            Label classLabel = new Label(container, SWT.NULL);
            classLabel.setText("Field Type:");
            dataTypeText = new Text(container, SWT.BORDER | SWT.SINGLE);
            if (field != null && field.getDatatypeName() != null)
                dataTypeText.setText(field.getDatatypeName());
            TypeAssistProvider.createTypeAssist(dataTypeText, CustomFieldsPage.this, IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES, null);
            dataTypeText.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    validate();
                }
            });

            gd = new GridData();
            gd.horizontalAlignment = GridData.FILL;
            gd.grabExcessHorizontalSpace = true;
            gd.horizontalSpan = 2;
            dataTypeText.setLayoutData(gd);

            Button _dataTypeChoiceButton = new Button(container, SWT.PUSH);
            _dataTypeChoiceButton.setText("Browse...");
            _dataTypeChoiceButton.addSelectionListener(new SelectionAdapter()
            {
                @Override
                public void widgetSelected(SelectionEvent e)
                {
                    IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), getJavaProject().getResource(),
                            IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES);
                    if (type != null)
                    {
                        dataTypeText.setText(type.getFullyQualifiedName('$'));
                    }
                }
            });

            GridData buttonGd = new GridData();
            buttonGd.horizontalAlignment = GridData.END;
            buttonGd.verticalAlignment = GridData.CENTER;
            buttonGd.widthHint = 60;
            _dataTypeChoiceButton.setLayoutData(buttonGd);

        }

        private void validate()
        {
            IStatus iStatus = org.eclipse.core.runtime.Status.OK_STATUS;

            if (nameText != null && nameText.getText().length() == 0)
            {
                iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "Field name is empty.");
            }
            if (iStatus.isOK() && nameText != null && (nameText.getText().length() > 0) && !JavaAccessUtils.isJavaIdentifier(nameText.getText()))
            {
                iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "Field name is not a valid Java identifier.");
            }
            if (iStatus.isOK() && dataTypeText != null && (dataTypeText.getText().length() == 0))
            {
                iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "Please choose a field type.");
            }

            if (iStatus.isOK() && nameText != null)
            {
                if ((field == null || !field.getName().equals(nameText.getText())) && containsField(nameText.getText()))
                {
                    iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "already contains a field with this name.");
                }
            }
            if (iStatus.isOK())
            {
                setMessage(message);
            }
            else
            {
                setMessage(iStatus.getMessage(), IMessageProvider.ERROR);

            }

            getButton(IDialogConstants.OK_ID).setEnabled(iStatus.isOK());

        }

        @Override
        protected void buttonPressed(int buttonId)
        {
            if (buttonId == IDialogConstants.CANCEL_ID)
            {
                super.buttonPressed(buttonId);
            }
            else
            {
                if (field == null)
                {
                    field = new EJTableColumn();
                }
                if (nameText != null && dataTypeText != null)
                {
                    field.setName(nameText.getText());
                    field.setDatatypeName(dataTypeText.getText());
                }
                super.buttonPressed(buttonId);
            }

        }

        private EJTableColumn getEJTableColumn()
        {

            return field;
        }

    }

    public IJavaProject getJavaProject()
    {
        return javaProject;
    }

    public boolean containsField(String text)
    {
        for (EJTableColumn column : selectedColumns)
        {
            if (text.equalsIgnoreCase(column.getName()))
            {
                return true;
            }
        }
        return false;
    }

}
