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
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
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
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.framework.report.service.EJReportBlockService;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public class DataBlockServiceSelectionPage extends WizardPage
{

    private final DataBlockWizardContext wizardContext;
    private String                       blockName;

    private String                       blockServiceClass;

    private EJReportScreenType           type = EJReportScreenType.FORM_LAYOUT;

    private int                          x;
    private int                          y;
    private int                          width;
    private int                          height;

    protected DataBlockServiceSelectionPage(DataBlockWizardContext wizardContext)
    {
        super("ej.data.service.selection");
        this.wizardContext = wizardContext;
        setTitle("Data Block");
        setDescription("Properties for the new data block.");
        width = this.wizardContext.getDefaultWidth();
        height = this.wizardContext.getDefaultHeight();
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
        if (wizardContext.supportService())
        {
            createBlockServiceControls(composite, nColumns);
        }
        
        createBlockScreenTypeControls(composite, nColumns);
        setDefaults();
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

    private void createBlockServiceControls(Composite composite, int nColumns)
    {
        Label serviceGenLabel = new Label(composite, SWT.NULL);
        serviceGenLabel.setText("Block Service:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        serviceGenLabel.setLayoutData(gd);
        final Text serviceGenText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        serviceGenText.setLayoutData(gd);
        if (blockServiceClass != null)
            serviceGenText.setText(blockServiceClass);
        serviceGenText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                blockServiceClass = serviceGenText.getText().trim();
                doUpdateStatus();
            }
        });
        TypeAssistProvider.createTypeAssist(serviceGenText, new IJavaProjectProvider()
        {

            public IJavaProject getJavaProject()
            {
                return wizardContext.getProject();
            }
        }, IJavaElementSearchConstants.CONSIDER_CLASSES, EJReportBlockService.class.getName());
        final Button browse = new Button(composite, SWT.PUSH);
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                String value = serviceGenText.getText();
                IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), wizardContext.getProject().getResource(),
                        IJavaElementSearchConstants.CONSIDER_CLASSES, value == null ? "" : value, EJReportBlockService.class.getName());
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

    public String getBlockName()
    {
        return blockName;
    }

    public String getBlockServiceClass()
    {
        return blockServiceClass;
    }
    
    public int getX()
    {
        return x;
    }
    
    public int getY()
    {
        return y;
    }
    
    public EJReportScreenType getType()
    {
        return type;
    }
    
    public int getWidth()
    {
        return width;
    }
    
    public int getHeight()
    {
        return height;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    
    
    
    private void createBlockScreenTypeControls(final Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Screen Layout:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final ComboViewer blockRenderersViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockRenderersViewer.getCombo().setLayoutData(gd);
        blockRenderersViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                
                return super.getText(element);
            }

        });

        blockRenderersViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {

                if(wizardContext.isBlockTablelayout())
                {
                    return new EJReportScreenType[]{EJReportScreenType.NONE,EJReportScreenType.FORM_LAYOUT};
                }
                
                return EJReportScreenType.values();
            }
        });

        blockRenderersViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockRenderersViewer.getSelection() instanceof IStructuredSelection)
                {
                    type = (EJReportScreenType) ((IStructuredSelection) blockRenderersViewer.getSelection()).getFirstElement();

                    setDefaults();
                    
                }
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        blockRenderersViewer.setInput(new Object());
        blockRenderersViewer.setSelection(new StructuredSelection(type));
    }
    
    
    
   void setDefaults()
   {
       switch (type)
    {
        case FORM_LAYOUT:
            
            break;
        case TABLE_LAYOUT:
            
            break;

        default:
            break;
    }
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

        if (wizardContext.supportService() && blockServiceClass != null && blockServiceClass.trim().length() > 0)
        {
            IJavaProject javaProject = wizardContext.getProject();
            if (javaProject != null)
            {

                try
                {
                    IType findType = javaProject.findType(blockServiceClass);
                    if (findType == null)
                    {
                        setErrorMessage(String.format("%s can't find in project build path.", blockServiceClass));
                        return false;
                    }
                }
                catch (CoreException e)
                {
                    setErrorMessage(e.getMessage());
                    return false;
                }
            }
        }
        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
