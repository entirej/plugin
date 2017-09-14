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

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public class RelationSelectionPage extends WizardPage
{

    private final RelationWizardContext wizardContext;
    private String                      relationName;

    private String                      masterBlock;
    private String                      detailsBlock;

    protected RelationSelectionPage(RelationWizardContext wizardContext)
    {
        super("ej.relation.service.selection");
        this.wizardContext = wizardContext;
        setTitle("Block Relation");
        setDescription("Properties for the new block relation.");
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
        createRelationName(composite, nColumns);
        createSeparator(composite, nColumns);
        createMasterBlockLabel(composite, nColumns);
        createDetailsBlockLabel(composite, nColumns);
        createMasterBlockControls(composite, nColumns);
        createDetailsBlockControls(composite, nColumns);
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

    private void createRelationName(Composite composite, int nColumns)
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
        gd.horizontalSpan = 3;
        blockNameText.setLayoutData(gd);
        blockNameText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                relationName = blockNameText.getText();
                doUpdateStatus();
            }
        });
    }

    private void createMasterBlockLabel(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Master Block:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        formTitleLabel.setLayoutData(gd);

    }

    private void createMasterBlockControls(Composite composite, int nColumns)
    {

        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 100;
        gd.widthHint = 100;
        final ListViewer masterBlockViewer = new ListViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
        masterBlockViewer.getList().setLayoutData(gd);
        masterBlockViewer.setLabelProvider(new ColumnLabelProvider()
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

        masterBlockViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<String> renderers = wizardContext.getBlockNames();

                return renderers.toArray();
            }
        });

        masterBlockViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (masterBlockViewer.getSelection() instanceof IStructuredSelection)
                    masterBlock = (String) ((IStructuredSelection) masterBlockViewer.getSelection()).getFirstElement();
                doUpdateStatus();
            }
        });

        if (masterBlockViewer != null)
        {
            masterBlockViewer.setInput(new Object());
            masterBlockViewer.getList().select(-1);
            if (masterBlockViewer.getList().getItemCount() > 0 && masterBlockViewer.getList().getSelectionIndex() == -1)
            {
                masterBlockViewer.getList().select(0);
                if (masterBlockViewer.getSelection() instanceof IStructuredSelection)
                    masterBlock = (String) ((IStructuredSelection) masterBlockViewer.getSelection()).getFirstElement();
            }
            doUpdateStatus();
        }
    }

    private void createDetailsBlockLabel(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Detail Block:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 2;
        formTitleLabel.setLayoutData(gd);
    }

    private void createDetailsBlockControls(Composite composite, int nColumns)
    {

        final ListViewer masterBlockViewer = new ListViewer(composite, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER | SWT.SINGLE);
        GridData gd = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
        gd.horizontalSpan = 2;
        gd.heightHint = 100;
        gd.widthHint = 100;
        masterBlockViewer.getList().setLayoutData(gd);
        masterBlockViewer.setLabelProvider(new ColumnLabelProvider()
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

        masterBlockViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<String> renderers = wizardContext.getBlockNames();

                return renderers.toArray();
            }
        });

        masterBlockViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (masterBlockViewer.getSelection() instanceof IStructuredSelection)
                    detailsBlock = (String) ((IStructuredSelection) masterBlockViewer.getSelection()).getFirstElement();
                doUpdateStatus();
            }
        });

        if (masterBlockViewer != null)
        {
            masterBlockViewer.setInput(new Object());
            masterBlockViewer.getList().select(-1);
            // if (masterBlockViewer.getCombo().getItemCount() > 0 &&
            // masterBlockViewer.getCombo().getSelectionIndex() == -1)
            // {
            // masterBlockViewer.getCombo().select(0);
            // if (masterBlockViewer.getSelection() instanceof
            // IStructuredSelection)
            // detailsBlock = (String) ((IStructuredSelection)
            // masterBlockViewer.getSelection()).getFirstElement();
            // }
            doUpdateStatus();
        }
    }

    public String getRelationName()
    {
        return relationName;
    }

    public String getMasterBlock()
    {
        return masterBlock;
    }

    public String getDetailsBlock()
    {
        return detailsBlock;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (relationName == null || relationName.trim().length() == 0)
        {
            setErrorMessage("Relation name can't be empty.");
            return false;
        }
        else if (wizardContext.hasRelation(relationName))
        {
            setErrorMessage("A relation with this name already exists.");
            return false;
        }

        if (masterBlock == null)
        {
            setErrorMessage("A master block must be specified.");
            return false;
        }
        if (detailsBlock == null)
        {
            setErrorMessage("A detail block must be specified.");
            return false;
        }

        if (masterBlock.equals(detailsBlock))
        {
            setErrorMessage("master block and deatil block can not be same.");
            return false;
        }

        String dtlValidate = wizardContext.validDtlRelation(detailsBlock);
        if (dtlValidate != null)
        {
            setErrorMessage(dtlValidate);
            return false;
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }
}
