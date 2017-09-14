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
package org.entirej.ide.ui.wizards.form;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.core.actionprocessor.EJDefaultFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJFormActionProcessor;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJFormPage extends NewTypeWizardPage
{

    public static String     P_FORM_HEIGHT       = "FORM_HEIGHT";
    public static String     P_FORM_WIDTH        = "FORM_WIDTH";
    private String           formTitle;

    protected IStatus        fFormRendererStatus = new Status(IStatus.OK, EJUIPlugin.getID(), null);
    protected IStatus        fFormTitleStatus = new Status(IStatus.OK, EJUIPlugin.getID(), null);

    private EJPluginRenderer formRenderer;

    private ComboViewer      formRenderersViewer;

    public NewEntireJFormPage()
    {
        super(false, "ej.form");
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

        setSuperClass(EJDefaultFormActionProcessor.class.getName(), true);
    }

    private void doStatusUpdate()
    {
        IStatus projectType = projectTypeStatus();
        
        // status of all used components
        IStatus[] status = new IStatus[] { projectType,fContainerStatus, fPackageStatus, fTypeNameStatus, fFormRendererStatus,fFormTitleStatus };

        // the mode severe status will be displayed and the OK button
        // enabled/disabled.
        updateStatus(status);
    }

    private IStatus projectTypeStatus()
    {
        IStatus projectType = Status.OK_STATUS;
        
        if(getJavaProject()==null || !EJProject.hasPluginNature(getJavaProject().getProject()))
        {
            projectType = new Status(IStatus.ERROR, EJUIPlugin.getID(), "To create Forms Project should be an Entirej Forms Type");
        }
        return projectType;
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
        createFormTitleControls(composite, nColumns);
        createFormRendererControls(composite, nColumns);
        createSuperClassControls(composite, nColumns);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    public String getTitleText()
    {
        return formTitle;
    }

    @Override
    protected String getTypeNameLabel()
    {
        return "Form Name:";
    }

    @Override
    protected String getSuperClassLabel()
    {
        return "Action Processor:";
    }

    protected IType chooseSuperClass()
    {
        IJavaProject project = getJavaProject();
        if (project == null)
        {
            return null;
        }

        return JavaAccessUtils.selectType(getShell(), project.getResource(), IJavaElementSearchConstants.CONSIDER_CLASSES, getSuperClass(),
                EJFormActionProcessor.class.getName());
    }

    private void createFormTitleControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Form Title:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final Text formTitleText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        formTitleText.setLayoutData(gd);
        formTitleText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                formTitle = formTitleText.getText();
                fFormTitleStatus = formTitleChanged();
                doStatusUpdate();
            }
        });
        fFormTitleStatus = formTitleChanged();
        createEmptySpace(composite, 1);
    }

    private void createFormRendererControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Form Renderer:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        formRenderersViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        formRenderersViewer.getCombo().setLayoutData(gd);
        formRenderersViewer.setLabelProvider(new ColumnLabelProvider()
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

        formRenderersViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<EJPluginRenderer> renderers = new ArrayList<EJPluginRenderer>();

                IJavaProject project = getJavaProject();
                if (project != null)
                {
                    try
                    {

                        EntirejPluginPropertiesEnterpriseEdition entirejProperties = EntirejPropertiesUtils.retrieveEntirejProperties(project);
                        renderers.addAll(entirejProperties.getFormRendererContainer().getAllRenderers());
                    }
                    catch (CoreException e)
                    {
                        fFormRendererStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
                        doStatusUpdate();
                    }
                }
                Collections.sort(renderers,new Comparator<EJPluginRenderer>()
                {

                    public int compare(EJPluginRenderer o1, EJPluginRenderer o2)
                    {
                     
                        return o1.getAssignedName().compareTo(o2.getAssignedName());
                    }
                });
                return renderers.toArray();
            }
        });

        formRenderersViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (formRenderersViewer.getSelection() instanceof IStructuredSelection)
                    formRenderer = (EJPluginRenderer) ((IStructuredSelection) formRenderersViewer.getSelection()).getFirstElement();

                fFormRendererStatus = formRendererChanged();
                doStatusUpdate();
            }
        });
        createEmptySpace(composite, 1);
        refreshFormRenderers();
    }

    public void refreshFormRenderers()
    {
        if (formRenderersViewer != null)
        {
            formRenderersViewer.setInput(new Object());
            formRenderersViewer.getCombo().select(-1);
            if (formRenderersViewer.getCombo().getItemCount() > 0 && formRenderersViewer.getCombo().getSelectionIndex() == -1)
            {
                formRenderersViewer.getCombo().select(0);
                if (formRenderersViewer.getSelection() instanceof IStructuredSelection)
                    formRenderer = (EJPluginRenderer) ((IStructuredSelection) formRenderersViewer.getSelection()).getFirstElement();
            }
            fFormRendererStatus = formRendererChanged();
            doStatusUpdate();
        }
    }

    protected IStatus typeNameChanged()
    {

        String typeName = getTypeName();
        // must not be empty
        if (typeName.length() == 0)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Form name is empty.");
        }

        IStatus result = ResourcesPlugin.getWorkspace().validateName(typeName, IResource.FILE);
        if (!result.isOK())
        {
            return result;
        }

        IPackageFragment packageFragment = getPackageFragment();

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IFolder folder = root.getFolder(packageFragment.getPath());
        final IFile formFile = folder.getFile(new Path(typeName + "." + EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX));
        if (folder.exists() && formFile.exists())
        {

            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Form already exists.");

        }

        URI location = formFile.getLocationURI();
        if (location != null)
        {
            try
            {
                IFileStore store = EFS.getStore(location);
                if (store.fetchInfo().exists())
                {
                    return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Form with same name but different case exists.");
                }
            }
            catch (CoreException e)
            {
                new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
            }
        }

        return new Status(IStatus.OK, EJUIPlugin.getID(), "");
    }

    
    protected IStatus formTitleChanged()
    {

        String typeName = getTitleText();
        // must not be empty
        if (typeName==null || typeName.length() == 0)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Form title is empty.");
        }

        

        return new Status(IStatus.OK, EJUIPlugin.getID(), "");
    }
    
    @Override
    protected IStatus containerChanged()
    {
        refreshFormRenderers();
        return super.containerChanged();
    }

    protected IStatus formRendererChanged()
    {
        IJavaProject javaProject = getJavaProject();
        if (javaProject != null)
        {
            try
            {
                EntirejPropertiesUtils.varifyEntirejProperties(javaProject);
            }
            catch (CoreException e)
            {
                return new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
            }
        }
        if (formRenderer == null)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Please choose a form renderer.");
        }
        return new Status(IStatus.OK, EJUIPlugin.getID(), "");
    }

    public void setFormName(String fname)
    {
        setTypeName(fname, true);

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

    public void createForm(IConfigurationElement configElement, IProgressMonitor monitor)
    {
        try
        {
            IJavaProject project = getJavaProject();
            if (project != null)
            {
                IPackageFragment packageFragment = getPackageFragment();
                String formName = getTypeName();

                // create a sample file
                monitor.beginTask("Creating Form " + formName, 2);
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IFolder folder = root.getFolder(packageFragment.getPath());
                if (!folder.exists())
                {
                    folder.create(false, true, monitor);
                }

                final IFile formFile = folder.getFile(new Path(formName + "." + EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX));

                String formTitle = getTitleText();
                String rendererName = formRenderer.getAssignedName();
                String actionProcessor = getSuperClass();

                EJPluginFormProperties formProperties = new EJPluginFormProperties(formName, project);
                formProperties.setFormRendererName(rendererName);
                formProperties.setFormTitle(formTitle);
                if (actionProcessor != null && actionProcessor.length() > 0)
                {
                    formProperties.setActionProcessorClassName(actionProcessor);
                }

                int height = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_HEIGHT);
                int width = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_WIDTH);

                formProperties.setFormHeight(height);
                formProperties.setFormWidth(width);
                formProperties.setNumCols(1);

                FormPropertiesWriter writer = new FormPropertiesWriter();
                writer.saveForm(formProperties, formFile, monitor);
                folder.refreshLocal(IResource.DEPTH_ONE, monitor);
                getShell().getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        IWizard iWizard = getWizard();
                        if (iWizard instanceof NewWizard)
                        {
                            NewWizard wizard = (NewWizard) iWizard;
                            wizard.selectAndReveal(formFile);
                            wizard.openResource(formFile);
                        }
                    }
                });
            }

        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }
}
