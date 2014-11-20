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
    private String               pojoGeneratorClass;
    private Text                 pojoGenText;
    private boolean              createSerivce   = true;
    private boolean              serviceOptional = true;

    private static final IStatus S_DEFAULT_OK    = new Status(IStatus.OK, EJUIPlugin.getID(), null);
    private IStatus              pojoGenStatus   = S_DEFAULT_OK;

    public NewEJPojoServiceSelectPage()
    {
        super(true, "ej.pojo.service");
    }

    public String getPojoGeneratorClass()
    {
        return pojoGeneratorClass;
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
        IStatus[] status = new IStatus[] { fContainerStatus, fPackageStatus, fTypeNameStatus, pojoGenStatus };

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
        createPojoGeneratorControls(composite, nColumns);
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
        if (containerChanged.isOK() && pojoGenText != null)
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
        pojoGeneratorClass = "";
    }

    private void _updateUI()
    {
        if (pojoGenText != null)
            pojoGenText.setText(pojoGeneratorClass != null ? pojoGeneratorClass : "");
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

    private void createPojoGeneratorControls(Composite composite, int nColumns)
    {
        Label pojoGenLabel = new Label(composite, SWT.NULL);
        pojoGenLabel.setText("Pojo Generator:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        pojoGenLabel.setLayoutData(gd);
        pojoGenText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        pojoGenText.setLayoutData(gd);
        if (pojoGeneratorClass != null)
            pojoGenText.setText(pojoGeneratorClass);
        pojoGenText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                pojoGeneratorClass = pojoGenText.getText().trim();
                pojoGenStatus = pojoGeneratorChanged();
                doStatusUpdate();
            }
        });
        pojoGenText.setText(pojoGeneratorClass = EJFormPojoGenerator.class.getName());
        TypeAssistProvider.createTypeAssist(pojoGenText, this, IJavaElementSearchConstants.CONSIDER_CLASSES,
                org.entirej.framework.core.service.EJPojoContentGenerator.class.getName());
        final Button browse = new Button(composite, SWT.PUSH);
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                String value = pojoGenText.getText();
                IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), getJavaProject().getResource(),
                        IJavaElementSearchConstants.CONSIDER_CLASSES, value == null ? "" : value,
                        org.entirej.framework.core.service.EJPojoContentGenerator.class.getName());
                if (type != null)
                {
                    pojoGenText.setText(type.getFullyQualifiedName('$'));
                }
            }

        });
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 1;
        browse.setLayoutData(gd);
    }

    protected IStatus pojoGeneratorChanged()
    {
        IJavaProject javaProject = getJavaProject();
        if (javaProject != null)
        {
            // Pojo Generator
            if (pojoGeneratorClass == null)
            {
                return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Pojo Generator can't be empty.");
            }

            try
            {
                IType findType = javaProject.findType(pojoGeneratorClass);
                if (findType == null)
                {
                    return new Status(IStatus.ERROR, EJUIPlugin.getID(), String.format("%s can't find in project build path.", pojoGeneratorClass));
                }
                else
                {

                    if (!JavaAccessUtils.isSubTypeOfInterface(findType, org.entirej.framework.core.service.EJPojoContentGenerator.class))
                    {
                        return new Status(IStatus.ERROR, EJUIPlugin.getID(), String.format("%s is not a sub type of %s.", pojoGeneratorClass,
                                org.entirej.framework.core.service.EJPojoContentGenerator.class.getName()));
                    }
                }
            }
            catch (CoreException e)
            {
                return new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
            }
        }

        return S_DEFAULT_OK;
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
