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
package org.entirej.ide.ui.editors.form.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public class LovMappingSelectionPage extends WizardPage
{

    private final LovMappingWizardContext wizardContext;
    private String                        lovMappingName;

    private String                        lovDef;
    private ComboViewer                   lovDefViewer;

    private boolean                       executeAfterQuery = true;

    protected LovMappingSelectionPage(LovMappingWizardContext wizardContext)
    {
        super("ej.lov.mapping.selection");
        this.wizardContext = wizardContext;
        setTitle("LOV Mapping");
        setDescription("Properties for the new LOV Mapping.");
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        Dialog.applyDialogFont(composite);
        int nColumns = 4;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);
        createLovName(composite, nColumns);
        createSeparator(composite, nColumns);
        createLovRendererControls(composite, nColumns);
        createMandatoryOptionControls(composite, nColumns);
        setControl(composite);

        setPageComplete(false);
    }

    protected void createSeparator(Composite composite, int nColumns)
    {
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = nColumns;
        gridData.heightHint = convertHeightInCharsToPixels(1);
        (new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)).setLayoutData(gridData);
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

    private void createMandatoryOptionControls(Composite composite, int nColumns)
    {
        createEmptySpace(composite, 1);
        final Button btnCreateService = new Button(composite, SWT.CHECK);
        btnCreateService.setText("Execute After Query");
        btnCreateService.setSelection(executeAfterQuery);
        btnCreateService.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                executeAfterQuery = btnCreateService.getSelection();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                executeAfterQuery = btnCreateService.getSelection();
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = nColumns - 1;

        btnCreateService.setLayoutData(gd);
    }

    private void createLovName(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Name:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final Text blockNameText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        blockNameText.setLayoutData(gd);
        blockNameText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                lovMappingName = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createLovRendererControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("LOV Definition:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        lovDefViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        lovDefViewer.getCombo().setLayoutData(gd);
        lovDefViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof EJPluginRenderer)
                {
                    EJPluginRenderer renderer = ((EJPluginRenderer) element);
                    return String.format("%s", renderer.getAssignedName(), renderer.getRendererClassName());
                }
                return super.getText(element);
            }

        });

        lovDefViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<String> renderers = wizardContext.getLovDefinitionNames();

                return renderers.toArray();
            }
        });

        lovDefViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (lovDefViewer.getSelection() instanceof IStructuredSelection)
                    lovDef = (String) ((IStructuredSelection) lovDefViewer.getSelection()).getFirstElement();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        refreshFormRenderers();
    }

    public void refreshFormRenderers()
    {
        if (lovDefViewer != null)
        {
            lovDefViewer.setInput(new Object());
            lovDefViewer.getCombo().select(-1);
            if (lovDefViewer.getCombo().getItemCount() > 0 && lovDefViewer.getCombo().getSelectionIndex() == -1)
            {
                lovDefViewer.getCombo().select(0);
                if (lovDefViewer.getSelection() instanceof IStructuredSelection)
                    lovDef = (String) ((IStructuredSelection) lovDefViewer.getSelection()).getFirstElement();
            }
            doUpdateStatus();
        }
    }

    public String getLovMappingName()
    {
        return lovMappingName;
    }

    public String getLovDef()
    {
        return lovDef;
    }

    public boolean isExecuteAfterQuery()
    {
        return executeAfterQuery;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (lovMappingName == null || lovMappingName.trim().length() == 0)
        {
            setErrorMessage("LOV mapping name can't be empty.");
            return false;
        }
        else if (wizardContext.hasLovMapping(lovMappingName))
        {
            setErrorMessage("A LOV mapping with this name already exists.");
            return false;
        }

        if (lovDef == null)
        {
            setErrorMessage("A LOV mapping definition must be specified.");
            return false;
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
