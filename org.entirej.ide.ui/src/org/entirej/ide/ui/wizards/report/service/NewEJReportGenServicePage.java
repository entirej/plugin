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
package org.entirej.ide.ui.wizards.report.service;

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
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.entirej.framework.report.service.EJReportServiceGenerator;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public class NewEJReportGenServicePage extends NewTypeWizardPage implements IJavaProjectProvider
{

    private final NewEJReportPojoServiceSelectPage pojoServiceSelectPage;

    public NewEJReportGenServicePage(NewEJReportPojoServiceSelectPage pojoServiceSelectPage)
    {
        super(true, "ejr.gen.service");
        this.pojoServiceSelectPage = pojoServiceSelectPage;
        setTitle("Generate Report Block Service");
        setDescription("Create a new report block service base on pojo.");
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

        _initProjectPref();
        _updateUI();
    }

    private void _updateUI()
    {

    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);
        if (visible)
        {
            if (!pojoServiceSelectPage.getJavaProject().equals(getJavaProject()))
            {
                init(new StructuredSelection(pojoServiceSelectPage.getJavaProject()));
                setPackageFragmentRoot(pojoServiceSelectPage.getPackageFragmentRoot(), false);
                setPackageFragment(pojoServiceSelectPage.getPackageFragment(), true);
            }
            validate();
        }
    }

    public void doStatusUpdate()
    {
        // status of all used components
        IStatus[] status = new IStatus[] { fContainerStatus, fPackageStatus, fTypeNameStatus };

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
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    @Override
    protected String getTypeNameLabel()
    {
        return "Service Name:";
    }

    private void _initProjectPref()
    {
        if (getJavaProject() == null)
            return;
    }

    void setProjectProvider(NewEJReportPojoServiceContentPage project)
    {
        if (!project.getJavaProject().equals(getJavaProject()))
        {
            init(new StructuredSelection(project.getJavaProject()));
            setPackageFragmentRoot(project.getPackageFragmentRoot(), false);
            setPackageFragment(project.getPackageFragment(), true);
        }
        
        if (getTypeName().isEmpty())
        {
            setTypeName(project.getWizardProvider().getServiceSuggest(), true);
        }
        validate();
    }

    @Override
    public IStatus typeNameChanged()
    {
        IStatus typeNameChanged = super.typeNameChanged();
        if (pojoServiceSelectPage.getPackageFragment().equals(getPackageFragment()) && pojoServiceSelectPage.getTypeName().equals(getTypeName()))
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Pojo and Service name can't be same.");
        }
        return typeNameChanged;
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

    public void validate()
    {
        fTypeNameStatus = typeNameChanged();
        fPackageStatus = packageChanged();
        doStatusUpdate();

    }

}
