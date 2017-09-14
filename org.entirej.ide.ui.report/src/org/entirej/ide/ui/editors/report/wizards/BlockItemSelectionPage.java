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
package org.entirej.ide.ui.editors.report.wizards;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public class BlockItemSelectionPage extends WizardPage
{

    private final BlockItemWizardContext wizardContext;
    private String                       blockItemName;

    private boolean                      serviceItem;
    private String                       dataTypeClass = String.class.getName();

    protected BlockItemSelectionPage(BlockItemWizardContext wizardContext)
    {
        super("ej.data.item.selection");
        this.wizardContext = wizardContext;
        setTitle("Report Block Item");
        setDescription("Properties for the new block item.");
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
        createDataTypeControls(composite, nColumns);

        

        if(!wizardContext.isContorl())
        {
            createSeparator(composite, nColumns);
            createServiceItemOptionControls(composite, nColumns);
        }
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
                blockItemName = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

   

    private void createServiceItemOptionControls(Composite composite, int nColumns)
    {
        createEmptySpace(composite, 1);
        final Button btnCreateService = new Button(composite, SWT.CHECK);
        btnCreateService.setText("Block Service Item");
        btnCreateService.setSelection(serviceItem);
        btnCreateService.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                serviceItem = btnCreateService.getSelection();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                serviceItem = btnCreateService.getSelection();
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = nColumns - 1;

        btnCreateService.setLayoutData(gd);
    }

    private void createDataTypeControls(Composite composite, int nColumns)
    {
        Label serviceGenLabel = new Label(composite, SWT.NULL);
        serviceGenLabel.setText("Data Type:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        serviceGenLabel.setLayoutData(gd);
        final Text serviceGenText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        serviceGenText.setLayoutData(gd);
        if (dataTypeClass != null)
            serviceGenText.setText(dataTypeClass);
        serviceGenText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                dataTypeClass = serviceGenText.getText().trim();
                doUpdateStatus();
            }
        });
        TypeAssistProvider.createTypeAssist(serviceGenText, new IJavaProjectProvider()
        {

            public IJavaProject getJavaProject()
            {
                return wizardContext.getProject();
            }
        }, IJavaElementSearchConstants.CONSIDER_CLASSES|IJavaElementSearchConstants.CONSIDER_ENUMS, Object.class.getName());
        final Button browse = new Button(composite, SWT.PUSH);
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                String value = serviceGenText.getText();
                IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), wizardContext.getProject().getResource(),
                        IJavaElementSearchConstants.CONSIDER_CLASSES, value == null ? "" : value, Object.class.getName());
                if (type != null)
                {
                    serviceGenText.setText(type.getFullyQualifiedName('$'));
                }
            }

        });
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 1;
        browse.setLayoutData(gd);
    }

    public String getBlockItemName()
    {
        return blockItemName;
    }


    public boolean isServiceItem()
    {
        return serviceItem;
    }

    public String getDataTypeClass()
    {
        return dataTypeClass;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (blockItemName == null || blockItemName.trim().length() == 0)
        {
            setErrorMessage("Block item name can't be empty.");
            return false;
        }
        else if (wizardContext.hasBlockItem(blockItemName))
        {
            setErrorMessage("A block item with this name already exists.");
            return false;
        }

        if (dataTypeClass == null)
        {
            setErrorMessage("A block renderer must be specified.");
            return false;
        }

        IJavaProject javaProject = wizardContext.getProject();
        if (javaProject != null)
        {

            if (dataTypeClass == null || dataTypeClass.trim().length() == 0)
            {
                setErrorMessage("A data type must be specified.");
                return false;
            }

            try
            {
                IType findType = javaProject.findType(dataTypeClass);
                if (findType == null)
                {
                    setErrorMessage(String.format("%s can't find in project build path.", dataTypeClass));
                    return false;
                }
            }
            catch (CoreException e)
            {
                setErrorMessage(e.getMessage());
                return false;
            }
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
