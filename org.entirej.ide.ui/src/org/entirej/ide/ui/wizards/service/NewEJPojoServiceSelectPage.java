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
package org.entirej.ide.ui.wizards.service;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
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
import org.entirej.framework.core.service.EJFormPojoGenerator;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public class NewEJPojoServiceSelectPage extends NewTypeWizardPage implements IJavaProjectProvider
{
   
   
    private boolean              createSerivce   = true;
    private boolean              serviceOptional = true;
    private boolean              needPojo = true;

    public NewEJPojoServiceSelectPage()
    {
        super(true, "ej.pojo.service");
    }

  

    public boolean isCreateSerivce()
    {
        return createSerivce;
    }

    public void setCreateSerivce(boolean createSerivce, boolean serviceOptional)
    {
        this.createSerivce = createSerivce;
        this.serviceOptional = serviceOptional;
    }
    public void setPojoNeed(boolean needPojo)
    {
        this.needPojo = needPojo;
        
        setTypeName(getTypeName(), needPojo);
    }

    @Override
    public IWizardContainer getContainer()
    {
        return super.getContainer();
    }

    /**
     * The wizard owning this page is responsible for calling this method with
     * the current selection. The selection is used to initialize the fields of
     * the wizard page.
     * 
     * @param selection
     *            used to initialize the fields
     */
    public void init(IStructuredSelection selection)
    {
        IJavaElement jelem = getInitialJavaElement(selection);
        initContainerPage(jelem);
        initTypePage(jelem);
        doStatusUpdate();
        _initProjectPref();
    }

    private void doStatusUpdate()
    {
        // status of all used components
        
        IStatus[] status = needPojo ?new IStatus[] { fContainerStatus, fPackageStatus, fTypeNameStatus } : new IStatus[] { fContainerStatus, fPackageStatus, };

        // the mode severe status will be displayed and the OK button
        // enabled/disabled.
        updateStatus(status);
    }

    protected void handleFieldChanged(String fieldName)
    {
        super.handleFieldChanged(fieldName);

        doStatusUpdate();
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        int nColumns = 4;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);

        // pick & choose the wanted UI components
        createContainerControls(composite, nColumns);
        createPackageControls(composite, nColumns);
        createSeparator(composite, nColumns);
        createTypeNameControls(composite, nColumns);
        createEmptySpace(composite, 1);
        if (serviceOptional)
            createServiceOptionControls(composite, 3);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    @Override
    protected String getTypeNameLabel()
    {
        return "Pojo Name:";
    }

    @Override
    protected IStatus containerChanged()
    {
        IStatus containerChanged = super.containerChanged();
        if (containerChanged.isOK() )
        {
            _initProjectPref();
            _updateUI();
        }

        return containerChanged;
    }

    private void _initProjectPref()
    {
        if (getJavaProject() == null)
            return;
    }

    private void _updateUI()
    {
       
    }

    private void createServiceOptionControls(Composite composite, int nColumns)
    {

        final Button btnCreateService = new Button(composite, SWT.CHECK);
        btnCreateService.setText("Generate Block Service");

        btnCreateService.setSelection(createSerivce);

        btnCreateService.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                createSerivce = btnCreateService.getSelection();
                doStatusUpdate();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                createSerivce = btnCreateService.getSelection();
                doStatusUpdate();
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = nColumns;

        btnCreateService.setLayoutData(gd);
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

}
