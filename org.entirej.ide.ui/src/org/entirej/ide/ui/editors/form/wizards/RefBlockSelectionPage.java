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

import java.util.ArrayList;
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
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public class RefBlockSelectionPage extends WizardPage
{
    public static final String          NEW_CANVAS  = "[New Canvas]";

    public static final String          NONE_CANVAS = "[NONE]";
    private final RefBlockWizardContext wizardContext;
    private String                      blockName;

    private String                      newCanvasName;
    private Text                        newCanvasText;

    private String                      blockRef;
    private ComboViewer                 blockRefViewer;

    private String                      blockCanves;
    private ComboViewer                 blockCanvesViewer;

    private boolean                     copyRefBlock;

    protected RefBlockSelectionPage(RefBlockWizardContext wizardContext)
    {
        super("ej.ref.block.selection");
        this.wizardContext = wizardContext;
        setTitle("Referenced Block");
        setDescription("Properties for the new referenced block.");
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
        createBlockName(composite, nColumns);
        createBlockRefControls(composite, nColumns);
        if(wizardContext.copyOption())
            createRefCopyOptionControls(composite, nColumns);
        createSeparator(composite, nColumns);
        createBlockCanvasControls(composite, nColumns);
        createNewCanvasName(composite, nColumns);

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

    private void createBlockName(Composite composite, int nColumns)
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
                blockName = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createBlockRefControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Referenced Block:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        blockRefViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockRefViewer.getCombo().setLayoutData(gd);
        blockRefViewer.setLabelProvider(new ColumnLabelProvider()
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

        blockRefViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<String> renderers = wizardContext.getReferencedBlockNames();

                return renderers.toArray();
            }
        });

        blockRefViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockRefViewer.getSelection() instanceof IStructuredSelection)
                    blockRef = (String) ((IStructuredSelection) blockRefViewer.getSelection()).getFirstElement();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        refreshRefBlocks();
    }

    public void refreshRefBlocks()
    {
        if (blockRefViewer != null)
        {
            blockRefViewer.setInput(new Object());
            blockRefViewer.getCombo().select(-1);
            if (blockRefViewer.getCombo().getItemCount() > 0 && blockRefViewer.getCombo().getSelectionIndex() == -1)
            {
                blockRefViewer.getCombo().select(0);
                if (blockRefViewer.getSelection() instanceof IStructuredSelection)
                    blockRef = (String) ((IStructuredSelection) blockRefViewer.getSelection()).getFirstElement();
            }
            doUpdateStatus();
        }
    }

    private void createRefCopyOptionControls(Composite composite, int nColumns)
    {
        createEmptySpace(composite, 1);
        final Button btnCreateService = new Button(composite, SWT.CHECK);
        btnCreateService.setText("Copy Referenced Block");
        btnCreateService.setSelection(copyRefBlock);
        btnCreateService.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                copyRefBlock = btnCreateService.getSelection();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                copyRefBlock = btnCreateService.getSelection();
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = nColumns - 1;

        btnCreateService.setLayoutData(gd);
    }

    private void createBlockCanvasControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Block Canvas:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        blockCanvesViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockCanvesViewer.getCombo().setLayoutData(gd);
        blockCanvesViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {

                return super.getText(element);
            }

        });

        blockCanvesViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<EJCanvasProperties> cances = wizardContext.getCanvas();

                List<String> list = new ArrayList<String>(cances.size());
                list.add(NONE_CANVAS);
                list.add(NEW_CANVAS);
                for (EJCanvasProperties canvasProp : cances)
                {
                    list.add(canvasProp.getName());
                }
                return list.toArray();
            }
        });

        blockCanvesViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockCanvesViewer.getSelection() instanceof IStructuredSelection)
                    blockCanves = (String) ((IStructuredSelection) blockCanvesViewer.getSelection()).getFirstElement();
                if (newCanvasText != null)
                    if (NEW_CANVAS.equals(blockCanves))
                    {
                        newCanvasText.setEnabled(true);
                    }
                    else
                    {
                        newCanvasText.setText("");
                        newCanvasText.setEnabled(false);
                    }
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        refreshCanvases();
    }

    public void refreshCanvases()
    {
        if (blockCanvesViewer != null)
        {
            blockCanvesViewer.setInput(new Object());
            if (newCanvasText != null)
                newCanvasText.setText("");
            blockCanves = NEW_CANVAS;
            blockCanvesViewer.getCombo().select(1);
            doUpdateStatus();
        }
    }

    private void createNewCanvasName(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("New Canvas Name:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        newCanvasText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        newCanvasText.setLayoutData(gd);
        newCanvasText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                newCanvasName = newCanvasText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    public String getBlockName()
    {
        return blockName;
    }

    public String getBlockRef()
    {
        return blockRef;
    }

    public String getBlockCanves()
    {
        if (NONE_CANVAS.equals(blockCanves))
        {
            return null;
        }
        return blockCanves;
    }

    public boolean isCopyRefBlock()
    {
        return wizardContext.copyOption() && copyRefBlock;
    }

    public String getNewCanvasName()
    {
        return newCanvasName;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (blockName == null || blockName.trim().length() == 0)
        {
            setErrorMessage("Block name can't be empty.");
            return false;
        }
        else if (wizardContext.hasBlock(blockName))
        {
            setErrorMessage("A block with this name already exists.");
            return false;
        }

        if (blockRef == null)
        {
            setErrorMessage("A refernced block  must be specified.");
            return false;
        }

        if (NEW_CANVAS.equals(blockCanves))
        {
            if (newCanvasName == null || newCanvasName.trim().length() == 0)
            {
                setErrorMessage("new canvas name can't be empty.");
                return false;
            }
            else if (wizardContext.hasCanvas(newCanvasName))
            {
                setErrorMessage("A canvas with this name already exists.");
                return false;
            }
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
