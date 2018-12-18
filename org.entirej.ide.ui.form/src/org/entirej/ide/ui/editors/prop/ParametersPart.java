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
package org.entirej.ide.ui.editors.prop;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.DelegatingStyledCellLabelProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.SectionPart;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.ExpandableComposite;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public class ParametersPart extends SectionPart
{
    private final Image              ARG_IMG = JavaUI.getSharedImages().getImage(ISharedImages.IMG_OBJS_JAVADOCTAG);
    private final EJPropertiesEditor editor;
    private TableViewer              viewer;
    private Button                   addButton;
    private Button                   editButton;
    private Button                   removeButton;

    public ParametersPart(final EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(parent, page.getManagedForm().getToolkit(), Section.DESCRIPTION | ExpandableComposite.TITLE_BAR);

        this.editor = editor;
        buildBody(getSection(), page.getEditor().getToolkit());
    }

    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setText("Application Level Parameters");
        section.setDescription("These parameters are available throughout the application and can be directly referenced by block items.");
        section.setLayout(EditorLayoutFactory.createClearTableWrapLayout(false, 1));
        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        section.setLayoutData(sectionData);

        Composite body = toolkit.createComposite(section);
        section.setTabList(new Control[] { body });
        GridLayout glayout = new GridLayout();
        glayout.marginWidth = 2;
        glayout.marginHeight = 2;
        glayout.numColumns = 2;
        glayout.makeColumnsEqualWidth = false;

        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.grabExcessHorizontalSpace = true;

        body.setLayout(glayout);
        body.setLayoutData(gd);

        createViewer(body);
        createButtons(body, toolkit);
        viewer.setInput(new Object());
        updateButtons();
        toolkit.paintBordersFor(body);
        section.setClient(body);
        section.layout();
    }

    public void createViewer(Composite body)
    {
        GridData gd;
        viewer = new TableViewer(body, SWT.MULTI | SWT.BORDER | SWT.VIRTUAL | SWT.V_SCROLL);
        gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_FILL);
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.FILL;
        gd.grabExcessVerticalSpace = true;
        gd.horizontalSpan = 1;
        gd.widthHint = 30;

        final ParameterLabelProvider labelProvider = new ParameterLabelProvider();
        DelegatingStyledCellLabelProvider cellLabelProvider = new DelegatingStyledCellLabelProvider(labelProvider);
        viewer.getTable().setLayoutData(gd);
        viewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {

            }

            public void dispose()
            {
                //
            }

            public Object[] getElements(Object inputElement)
            {

                return editor.getEntireJProperties().getAllApplicationLevelParameters().toArray();
            }
        });
        viewer.setLabelProvider(cellLabelProvider);
        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                updateButtons();

            }
        });
        viewer.addDoubleClickListener(new IDoubleClickListener()
        {

            public void doubleClick(DoubleClickEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                Object[] items = selection.toArray();

                if (items.length == 1 && items[0] instanceof EJPluginApplicationParameter)
                {
                    EJPluginApplicationParameter applicationParameter = (EJPluginApplicationParameter) items[0];
                    ParameterDialog dialog = new ParameterDialog(EJUIPlugin.getActiveWorkbenchShell(), applicationParameter);
                    if (dialog.open() == Window.OK)
                    {
                        editor.setDirty(true);
                        refresh();
                    }
                }

            }
        });
    }

    private void createButtons(final Composite body, FormToolkit toolkit)
    {
        Composite buttonClient = toolkit.createComposite(body);
        GridData layoutData = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        layoutData.grabExcessHorizontalSpace =false;
        layoutData.widthHint = 60;
        buttonClient.setLayoutData(layoutData);
        GridLayout glayout = new GridLayout(1,true);
        glayout.marginWidth = glayout.marginHeight = 2;
        buttonClient.setLayout(glayout);

        addButton = toolkit.createButton(buttonClient, "Add", SWT.PUSH);
        addButton.addSelectionListener(new SelectionAdapter()
        {

            public void widgetSelected(SelectionEvent e)
            {
                ParameterDialog dialog = new ParameterDialog(EJUIPlugin.getActiveWorkbenchShell(), null);
                if (dialog.open() == Window.OK)
                {
                    EJPluginEntireJProperties entireJProperties = editor.getEntireJProperties();
                    entireJProperties.addApplicationLevelParameter(dialog.getApplicationParameter());
                    editor.setDirty(true);
                    refresh();
                }
            }
        });
        GridData gd = new GridData(GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        addButton.setLayoutData(gd);

        editButton = toolkit.createButton(buttonClient, "Edit", SWT.PUSH);
        editButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                Object[] items = selection.toArray();

                if (items.length == 1 && items[0] instanceof EJPluginApplicationParameter)
                {
                    EJPluginApplicationParameter applicationParameter = (EJPluginApplicationParameter) items[0];
                    ParameterDialog dialog = new ParameterDialog(EJUIPlugin.getActiveWorkbenchShell(), applicationParameter);
                    if (dialog.open() == Window.OK)
                    {
                        editor.setDirty(true);
                        refresh();
                    }
                }

            }
        });
        gd  = new GridData(GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        editButton.setLayoutData(gd);

        removeButton = toolkit.createButton(buttonClient, "Remove", SWT.PUSH);
        removeButton.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
                Object[] items = selection.toArray();
                EJPluginEntireJProperties entireJProperties = editor.getEntireJProperties();

                for (Object object : items)
                {
                    if (object instanceof EJPluginApplicationParameter)
                    {
                        entireJProperties.removeApplicationLevelParameter((EJPluginApplicationParameter) object);
                    }
                }
                editor.setDirty(true);
                refresh();

            }
        });
        gd  = new GridData(GridData.FILL_HORIZONTAL );
        gd.horizontalSpan = 1;
        removeButton.setLayoutData(gd);

        toolkit.paintBordersFor(body);
    }

    private void updateButtons()
    {
        IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
        Object[] items = selection.toArray();
        removeButton.setEnabled(items.length > 0);
        editButton.setEnabled(items.length == 1);

    }

    @Override
    public void refresh()
    {
        if (viewer != null)
        {
            viewer.setInput(new Object());
        }
        super.refresh();
        updateButtons();
    }

    private class ParameterLabelProvider extends LabelProvider implements ILabelProvider, DelegatingStyledCellLabelProvider.IStyledLabelProvider
    {

        public Image getImage(Object element)
        {

            return ARG_IMG;
        }

        @Override
        public String getText(Object element)
        {
            return getStyledText(element).toString();
        }

        public StyledString getStyledText(Object element)
        {
            StyledString ss = new StyledString();
            if (element instanceof EJPluginApplicationParameter)
            {
                EJPluginApplicationParameter type = ((EJPluginApplicationParameter) element);
                ss.append(type.getName());
                if (type.getDataTypeName() != null)
                {
                    ss.append(" - ", StyledString.QUALIFIER_STYLER);
                    ss.append(" [ ", StyledString.QUALIFIER_STYLER);
                    ss.append(type.getDataTypeName(), StyledString.COUNTER_STYLER);
                    ss.append(" ] ", StyledString.QUALIFIER_STYLER);
                }
                
                if (type.getDefaultValue() != null && type.getDefaultValue().length()>0)
                {
                    ss.append(" = ", StyledString.QUALIFIER_STYLER);
                    ss.append(type.getDefaultValue(), StyledString.DECORATIONS_STYLER);
                }
            }
            return ss;
        }

    }

    private class ParameterDialog extends TitleAreaDialog
    {

        private String                       message = "";

        private EJPluginApplicationParameter applicationParameter;

        private Text                         nameText;
        private Text                         dataTypeText;
        private Text                         defaultValueText;

        public ParameterDialog(Shell parentShell, EJPluginApplicationParameter applicationParameter)
        {
            super(parentShell);
            setShellStyle(SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
            setHelpAvailable(false);
            this.applicationParameter = applicationParameter;
        }

        @Override
        protected Control createContents(Composite parent)
        {
            Control contents = super.createContents(parent);

            setTitle("Application Parameter");
            if (applicationParameter == null)
            {

                message = "Add a application level parameter.";
                getShell().setText("Add Parameter");
                setMessage(message);
                getButton(IDialogConstants.OK_ID).setEnabled(false);
            }
            else
            {
                message = "Edit application level parameter";
                getShell().setText("Edit Parameter");
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
            nameLabel.setText("Parameter Name:");
            nameText = new Text(container, SWT.BORDER | SWT.SINGLE);
            if (applicationParameter != null && applicationParameter.getName() != null)
                nameText.setText(applicationParameter.getName());
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
            classLabel.setText("Parameter Type:");
            dataTypeText = new Text(container, SWT.BORDER | SWT.SINGLE);
            if (applicationParameter != null && applicationParameter.getDataTypeName() != null)
                dataTypeText.setText(applicationParameter.getDataTypeName());
            TypeAssistProvider.createTypeAssist(dataTypeText, editor, IJavaElementSearchConstants.CONSIDER_ALL_TYPES, null);
            dataTypeText.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    verifyDefaultValue();
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
                    IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), editor.getJavaProject().getResource(),
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

            Label defaultValueLabel = new Label(container, SWT.NULL);
            defaultValueLabel.setText("Default Value:");
            defaultValueText = new Text(container, SWT.BORDER | SWT.SINGLE);
            if (applicationParameter != null && applicationParameter.getDefaultValue() != null)
                defaultValueText.setText(applicationParameter.getDefaultValue());
             gd = new GridData(GridData.FILL_HORIZONTAL);
            gd.horizontalSpan = 3;
            defaultValueText.setLayoutData(gd);
            defaultValueText.addModifyListener(new ModifyListener()
            {
                public void modifyText(ModifyEvent e)
                {
                    validate();
                }
            });
            verifyDefaultValue();
        }

        
        private void verifyDefaultValue()
        {
            String type = dataTypeText.getText();
            if(type.length()==0 || !EJPluginApplicationParameter.isValidDefaultValueType(type))
            {
                defaultValueText.setEnabled(false);
                defaultValueText.setText("");
            }
            else
            {
                defaultValueText.setEnabled(true);
                
            }
        }
        
        private void validate()
        {
            IStatus iStatus = org.eclipse.core.runtime.Status.OK_STATUS;

            if (nameText != null && nameText.getText().length() == 0)
            {
                iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "Parameter name is empty.");
            }
            if (iStatus.isOK() && nameText != null && (nameText.getText().length() > 0) && !JavaAccessUtils.isJavaIdentifier(nameText.getText()))
            {
                iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "Parameter name is not a valid Java identifier.");
            }
            if (iStatus.isOK() && dataTypeText != null && (dataTypeText.getText().length() == 0))
            {
                iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "Please choose a parameter type.");
            }
            else  if (iStatus.isOK() && defaultValueText != null && (defaultValueText.getText().length() != 0))
            {
                String defaultValueError = EJPluginApplicationParameter.validateDefaultValue(dataTypeText.getText(), defaultValueText.getText());
                if(defaultValueError!=null)
                    iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), defaultValueError);
            }

            if (iStatus.isOK() && nameText != null)
            {
                if ((applicationParameter == null || !applicationParameter.getName().equals(nameText.getText()))
                        && editor.getEntireJProperties().containsApplicationLevelParameter(nameText.getText()))
                {
                    iStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), "The application already contains a parameter with this name.");
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
            if(getButton(IDialogConstants.OK_ID)!=null)
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
                if (applicationParameter == null)
                {
                    applicationParameter = new EJPluginApplicationParameter(null, null);
                }
                if (nameText != null && dataTypeText != null)
                {
                    applicationParameter.setName(nameText.getText());
                    applicationParameter.setDataTypeName(dataTypeText.getText());
                    applicationParameter.setDefaultValue(defaultValueText.getText());
                }
                super.buttonPressed(buttonId);
            }

        }

        private EJPluginApplicationParameter getApplicationParameter()
        {

            return applicationParameter;
        }

    }
}
