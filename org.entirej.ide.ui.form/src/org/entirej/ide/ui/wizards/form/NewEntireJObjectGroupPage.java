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
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJObjectGroupPage extends NewTypeWizardPage
{

    public static String     P_FORM_HEIGHT       = "FORM_HEIGHT";
    public static String     P_FORM_WIDTH        = "FORM_WIDTH";

    
    public NewEntireJObjectGroupPage()
    {
        super(false, "ej.objgroup");
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
        refreshPackagePath();
    }

    @Override
    protected IStatus containerChanged()
    {

        refreshPackagePath();
        return super.containerChanged();
    }
    
    void refreshPackagePath()
    {
        setPackageFragment(null, false);
        IJavaProject javaProject = getJavaProject();
        try
        {

            EntirejPluginPropertiesEnterpriseEdition entirejProperties = EntirejPropertiesUtils.retrieveEntirejProperties(javaProject);
            if (entirejProperties != null && entirejProperties.getObjectGroupDefinitionLocation() != null
                    && entirejProperties.getObjectGroupDefinitionLocation().trim().length() > 0)
            {
                setPackageFragment(getPackageFragmentRoot().getPackageFragment(entirejProperties.getObjectGroupDefinitionLocation().replaceAll("/", ".")), false);
            }
        }
        catch (CoreException e)
        {
            // ignore
        }
    }
    
    
    private void doStatusUpdate()
    {
        IStatus projectTypeStatus = projectTypeStatus();
        // status of all used components
        IStatus[] status = new IStatus[] {projectTypeStatus, fContainerStatus, fPackageStatus, fTypeNameStatus };

        // the mode severe status will be displayed and the OK button
        // enabled/disabled.
        updateStatus(status);
    }
    
    private IStatus projectTypeStatus()
    {
        IStatus projectType = Status.OK_STATUS;
        
        if(getJavaProject()==null || !EJProject.hasPluginNature(getJavaProject().getProject()))
        {
            projectType = new Status(IStatus.ERROR, EJUIPlugin.getID(), "To create Object-Group Project should be an Entirej Forms Type");
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
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }



    @Override
    protected String getTypeNameLabel()
    {
        return "ObjectGroup Name:";
    }

  



    

   

    

    protected IStatus typeNameChanged()
    {

        String typeName = getTypeName();
        // must not be empty
        if (typeName.length() == 0)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "ObjectGroup name is empty.");
        }

        IStatus result = ResourcesPlugin.getWorkspace().validateName(typeName, IResource.FILE);
        if (!result.isOK())
        {
            return result;
        }

        IPackageFragment packageFragment = getPackageFragment();

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IFolder folder = root.getFolder(packageFragment.getPath());
        final IFile formFile = folder.getFile(new Path(typeName + "." + EJDevConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX));
        if (folder.exists() && formFile.exists())
        {

            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "ObjectGroup already exists.");

        }

        URI location = formFile.getLocationURI();
        if (location != null)
        {
            try
            {
                IFileStore store = EFS.getStore(location);
                if (store.fetchInfo().exists())
                {
                    return new Status(IStatus.ERROR, EJUIPlugin.getID(), "ObjectGroup with same name but different case exists.");
                }
            }
            catch (CoreException e)
            {
                new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
            }
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
                monitor.beginTask("Creating ObjectGroup " + formName, 2);
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IFolder folder = root.getFolder(packageFragment.getPath());
                if (!folder.exists())
                {
                    folder.create(false, true, monitor);
                }

                final IFile formFile = folder.getFile(new Path(formName + "." + EJDevConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX));

                String formTitle = "";
             

                EJPluginObjectGroupProperties formProperties = new EJPluginObjectGroupProperties(formName, project);
                
                formProperties.setFormTitle(formTitle);
               

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
