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
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.core.actionprocessor.EJDefaultFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJFormActionProcessor;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJPropertiesLoader;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;
import org.entirej.ide.ui.wizards.NewWizard;
import org.entirej.ide.ui.wizards.form.NewEntireJRefLovWizard.LovContext;

public class NewEntireJRefLovPage extends NewTypeWizardPage implements IJavaProjectProvider
{

    public static String         P_FORM_HEIGHT        = "FORM_HEIGHT";
    public static String         P_FORM_WIDTH         = "FORM_WIDTH";
    private static final IStatus S_DEFAULT_OK         = new Status(IStatus.OK, EJUIPlugin.getID(), null);

    protected IStatus            fBlockRendererStatus = new Status(IStatus.OK, EJUIPlugin.getID(), null);

    private EJPluginRenderer     lovRenderer;

    private ComboViewer          lovRenderersViewer;
    private Text                 blockServiceText;
    private String               blockServiceClass;
    protected IStatus            blockServiceStatus   = new Status(IStatus.OK, EJUIPlugin.getID(), null); ;
    private Button               browse;

    public NewEntireJRefLovPage()
    {
        super(false, "ej.ref.lov");
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
        setSuperClass(EJDefaultFormActionProcessor.class.getName(), true);
    }

    private void doStatusUpdate()
    {
        
        IStatus projectTypeStatus = projectTypeStatus();
        // status of all used components
        IStatus[] status = new IStatus[] {projectTypeStatus, fContainerStatus, fPackageStatus, fTypeNameStatus, fBlockRendererStatus, blockServiceStatus };

        // the mode severe status will be displayed and the OK button
        // enabled/disabled.
        updateStatus(status);
    }
    
    private IStatus projectTypeStatus()
    {
        IStatus projectType = Status.OK_STATUS;
        
        if(getJavaProject()==null || !EJProject.hasPluginNature(getJavaProject().getProject()))
        {
            projectType = new Status(IStatus.ERROR, EJUIPlugin.getID(), "To create Ref-Lov Project should be an Entirej Forms Type");
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
        createLovRendererControls(composite, nColumns);
        createblockServiceeratorControls(composite, nColumns);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    @Override
    protected String getTypeNameLabel()
    {
        return "LOV Definition Name:";
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

    private void createLovRendererControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("LOV Renderer:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        lovRenderersViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        lovRenderersViewer.getCombo().setLayoutData(gd);
        lovRenderersViewer.setLabelProvider(new ColumnLabelProvider()
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

        lovRenderersViewer.setContentProvider(new IStructuredContentProvider()
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
                        renderers.addAll(entirejProperties.getLovRendererContainer().getAllRenderers());
                    }
                    catch (CoreException e)
                    {
                        fBlockRendererStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
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

        lovRenderersViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (lovRenderersViewer.getSelection() instanceof IStructuredSelection)
                    lovRenderer = (EJPluginRenderer) ((IStructuredSelection) lovRenderersViewer.getSelection()).getFirstElement();

                fBlockRendererStatus = formRendererChanged();
                doStatusUpdate();
            }
        });
        createEmptySpace(composite, 1);
        refreshFormRenderers();
    }

    public void refreshFormRenderers()
    {
        if (lovRenderersViewer != null)
        {
            lovRenderersViewer.setInput(new Object());
            lovRenderersViewer.getCombo().select(-1);
            if (lovRenderersViewer.getCombo().getItemCount() > 0 && lovRenderersViewer.getCombo().getSelectionIndex() == -1)
            {
                lovRenderersViewer.getCombo().select(0);
                if (lovRenderersViewer.getSelection() instanceof IStructuredSelection)
                    lovRenderer = (EJPluginRenderer) ((IStructuredSelection) lovRenderersViewer.getSelection()).getFirstElement();
            }
            fBlockRendererStatus = formRendererChanged();
            doStatusUpdate();
        }
    }

    protected IStatus typeNameChanged()
    {

        String typeName = getTypeName();
        // must not be empty
        if (typeName.length() == 0)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "LOV Definition name is empty.");
        }

        IStatus result = ResourcesPlugin.getWorkspace().validateName(typeName, IResource.FILE);
        if (!result.isOK())
        {
            return result;
        }

        IPackageFragment packageFragment = getPackageFragment();

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IFolder folder = root.getFolder(packageFragment.getPath());
        final IFile formFile = folder.getFile(new Path(typeName + "." + EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX));
        if (folder.exists() && formFile.exists())
        {

            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "LOV Definition already exists.");

        }

        URI location = formFile.getLocationURI();
        if (location != null)
        {
            try
            {
                IFileStore store = EFS.getStore(location);
                if (store.fetchInfo().exists())
                {
                    return new Status(IStatus.ERROR, EJUIPlugin.getID(), "LOV Definition with same name but different case exists.");
                }
            }
            catch (CoreException e)
            {
                new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
            }
        }

        return new Status(IStatus.OK, EJUIPlugin.getID(), "");
    }

    @Override
    protected IStatus containerChanged()
    {
        refreshFormRenderers();
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
            if (entirejProperties != null && entirejProperties.getReusableLovDefinitionLocation() != null
                    && entirejProperties.getReusableLovDefinitionLocation().trim().length() > 0)
            {
                setPackageFragment(getPackageFragmentRoot().getPackageFragment(entirejProperties.getReusableLovDefinitionLocation().replaceAll("/", ".")),
                        false);
            }
        }
        catch (CoreException e)
        {
            // ignore
        }
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
        if (lovRenderer == null)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Please choose a LOV renderer.");
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

    private void createblockServiceeratorControls(Composite composite, int nColumns)
    {
        Label blockServiceLabel = new Label(composite, SWT.NULL);
        blockServiceLabel.setText("Block Service:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        blockServiceLabel.setLayoutData(gd);
        blockServiceText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockServiceText.setLayoutData(gd);
        if (blockServiceClass != null)
            blockServiceText.setText(blockServiceClass);
        blockServiceText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                blockServiceClass = blockServiceText.getText().trim();
                blockServiceStatus = blockServiceChanged();
                doStatusUpdate();
            }
        });
        TypeAssistProvider.createTypeAssist(blockServiceText, this, IJavaElementSearchConstants.CONSIDER_CLASSES, EJBlockService.class.getName());
        browse = new Button(composite, SWT.PUSH);
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                String value = blockServiceText.getText();
                IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), getJavaProject().getResource(),
                        IJavaElementSearchConstants.CONSIDER_CLASSES, value == null ? "" : value, EJBlockService.class.getName());
                if (type != null)
                {
                    blockServiceText.setText(type.getFullyQualifiedName('$'));
                }
            }

        });
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 1;
        browse.setLayoutData(gd);
    }

    protected IStatus blockServiceChanged()
    {
        IJavaProject javaProject = getJavaProject();
        if (javaProject != null)
        {
            if (blockServiceClass == null || blockServiceClass.trim().length() == 0)
            {
                return S_DEFAULT_OK;
            }

            try
            {
                IType findType = javaProject.findType(blockServiceClass);
                if (findType == null)
                {
                    return new Status(IStatus.ERROR, EJUIPlugin.getID(), String.format("%s can't find in project build path.", blockServiceClass));
                }
                else
                {

                    if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJBlockService.class))
                    {
                        return new Status(IStatus.ERROR, EJUIPlugin.getID(), String.format("%s is not a sub type of %s.", blockServiceClass,
                                EJBlockService.class.getName()));
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

    public void createForm(LovContext lovContext, IConfigurationElement configElement, IProgressMonitor monitor)
    {
        try
        {
            IJavaProject project = getJavaProject();
            if (project != null)
            {
                IPackageFragment packageFragment = getPackageFragment();
                String formName = getTypeName();

                // create a sample file
                monitor.beginTask("Creating Referenced LOV Definition " + formName, 2);
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IFolder folder = root.getFolder(packageFragment.getPath());
                if (!folder.exists())
                {
                    folder.create(false, true, monitor);
                }

                final IFile formFile = folder.getFile(new Path(formName + "." + EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX));

                String rendererName = lovRenderer.getAssignedName();

                EJPluginFormProperties formProperties = new EJPluginFormProperties(formName, project);

                int height = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_HEIGHT);
                int width = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_WIDTH);

                formProperties.setFormHeight(height);
                formProperties.setFormWidth(width);
                formProperties.setNumCols(1);

                final EJPluginLovDefinitionProperties lovProperties = new EJPluginLovDefinitionProperties(formName, formProperties);

                formProperties.getLovDefinitionContainer().addLovDefinitionProperties(lovProperties);
                lovProperties.setLovRendererName(rendererName, true);
                final EJPluginBlockProperties blockProperties = new EJPluginBlockProperties(formProperties, formName, false);
                lovProperties.setBlockProperties(blockProperties);

                // create items if service is also selected
                if (blockServiceClass != null && blockServiceClass.trim().length() > 0)
                {
                    blockProperties.setServiceClassName(blockServiceClass, true);
                }

                FormPropertiesWriter writer = new FormPropertiesWriter();
                writer.saveForm(formProperties, formFile, monitor);
                EJPluginEntireJPropertiesLoader.reload(formProperties.getJavaProject());
                folder.refreshLocal(IResource.DEPTH_ONE, monitor);
                lovContext.addRefrence(formName, lovProperties);
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
