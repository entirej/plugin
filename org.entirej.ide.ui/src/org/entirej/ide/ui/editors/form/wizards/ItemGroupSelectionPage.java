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

import org.eclipse.jface.dialogs.Dialog;
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
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;

public class ItemGroupSelectionPage extends WizardPage
{

    private final ItemGroupWizardContext wizardContext;
    private String                       itemGroupName;
    private String                       itemGroupTitle;
    private int                          numCol = 1;
    private boolean                      showGroupFrame;

    protected ItemGroupSelectionPage(ItemGroupWizardContext wizardContext)
    {
        super("ej.item.group.selection");
        this.wizardContext = wizardContext;
        setTitle("Item Group");
        setDescription("Properties for the new item group.");
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
        createGroupName(composite, nColumns);

        createSeparator(composite, nColumns);
        createGroupCol(composite, nColumns);
        createGroupTitle(composite, nColumns);
        createGroupFrameOptionControls(composite, nColumns);

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

    private void createGroupName(Composite composite, int nColumns)
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
                itemGroupName = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createGroupTitle(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Title:");
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
                itemGroupTitle = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createGroupCol(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Columns:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final Text blockNameText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.widthHint = 120;
        gd.horizontalSpan = 1;
        blockNameText.setLayoutData(gd);
        blockNameText.setText(String.valueOf(numCol));
        blockNameText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                try
                {
                    numCol = (Integer.parseInt(blockNameText.getText()));
                }
                catch (NumberFormatException ex)
                {
                    numCol = 1;
                    blockNameText.setText("1");
                    blockNameText.selectAll();
                }
                doUpdateStatus();
            }
        });
        blockNameText.addVerifyListener(new EJPluginEntireJNumberVerifier());
        createEmptySpace(composite, 2);
    }

    private void createGroupFrameOptionControls(Composite composite, int nColumns)
    {
        createEmptySpace(composite, 1);
        final Button btnCreateService = new Button(composite, SWT.CHECK);
        btnCreateService.setText("Display Group Frame");
        btnCreateService.setSelection(showGroupFrame);
        btnCreateService.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                showGroupFrame = btnCreateService.getSelection();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                showGroupFrame = btnCreateService.getSelection();
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = nColumns - 1;

        btnCreateService.setLayoutData(gd);
    }

    public String getItemGroupName()
    {
        return itemGroupName;
    }

    public boolean isShowGroupFrame()
    {
        return showGroupFrame;
    }

    public String getItemGroupTitle()
    {
        return itemGroupTitle;
    }

    public int getNumCol()
    {
        return numCol;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (itemGroupName == null || itemGroupName.trim().length() == 0)
        {
            setErrorMessage("Item group name can't be empty.");
            return false;
        }
        else if (wizardContext.hasGroup(itemGroupName))
        {
            setErrorMessage("A item group with this name already exists.");
            return false;
        }

        if (itemGroupName == null || itemGroupName.trim().length() == 0)
        {
            setErrorMessage("Item group name can't be empty.");
            return false;
        }
        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
