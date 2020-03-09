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
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.framework.core.actionprocessor.EJDefaultFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJFormActionProcessor;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJPropertiesLoader;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.common.viewers.CTreeComboViewer;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJRefBlockPage extends NewTypeWizardPage implements IJavaProjectProvider
{

    public static String         P_FORM_HEIGHT        = "FORM_HEIGHT";
    public static String         P_FORM_WIDTH         = "FORM_WIDTH";
    private static final IStatus S_DEFAULT_OK         = new Status(IStatus.OK, EJUIPlugin.getID(), null);

    protected IStatus            fBlockRendererStatus = new Status(IStatus.OK, EJUIPlugin.getID(), null);

    private EJPluginRenderer     blockRenderer;

    private CTreeComboViewer          blockRenderersViewer;
    private Text                 blockServiceText;
    private String               blockServiceClass;
    protected IStatus            blockServiceStatus   = new Status(IStatus.OK, EJUIPlugin.getID(), null);
    private boolean              controlBlock;
    private Button               browse;

    public NewEntireJRefBlockPage()
    {
        super(false, "ej.ref.block");
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
    
    private IStatus projectTypeStatus()
    {
        IStatus projectType = Status.OK_STATUS;
        
        if(getJavaProject()==null || !EJProject.hasPluginNature(getJavaProject().getProject()))
        {
            projectType = new Status(IStatus.ERROR, EJUIPlugin.getID(), "To create Forms Ref-Block should be an Entirej Forms Type");
        }
        return projectType;
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

    void refreshPackagePath()
    {
        setPackageFragment(null, false);
        IJavaProject javaProject = getJavaProject();
        try
        {

            EntirejPluginPropertiesEnterpriseEdition entirejProperties = EntirejPropertiesUtils.retrieveEntirejProperties(javaProject);
            if (entirejProperties != null && entirejProperties.getReusableBlocksLocation() != null
                    && entirejProperties.getReusableBlocksLocation().trim().length() > 0)
            {
                setPackageFragment(getPackageFragmentRoot().getPackageFragment(entirejProperties.getReusableBlocksLocation().replaceAll("/", ".")), false);
            }
        }
        catch (CoreException e)
        {
            // ignore
        }
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
        createBlockRendererControls(composite, nColumns);
        createEmptySpace(composite, 1);
        createControlBlockControls(composite, 3);
        createblockServiceeratorControls(composite, nColumns);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    @Override
    protected String getTypeNameLabel()
    {
        return "Block Name:";
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

    private void createBlockRendererControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Block Renderer:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        blockRenderersViewer = new CTreeComboViewer(composite,SWT.READ_ONLY|SWT.SINGLE|SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockRenderersViewer.getTree().setLayoutData(gd);
         final Image  GROUP = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        
        blockRenderersViewer.setLabelProvider(new ColumnLabelProvider()
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
            
            @Override
            public Image getImage(Object element)
            {
                if(element instanceof String)
                {
                    return GROUP;
                }
                return super.getImage(element);
            }

        });
        
        
        blockRenderersViewer.setAutoExpandLevel(3);
       
        blockRenderersViewer.setContentProvider(new ITreeContentProvider()
        {
            List<EJPluginRenderer> 
            renderers = new ArrayList<EJPluginRenderer>();;
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
                 
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
               
                IJavaProject project = getJavaProject();
                if (project != null)
                {
                    try
                    {
                        renderers = new ArrayList<EJPluginRenderer>();
                        EntirejPluginPropertiesEnterpriseEdition entirejProperties = EntirejPropertiesUtils.retrieveEntirejProperties(project);
                        renderers.addAll(entirejProperties.getBlockRendererContainer().getAllRenderers());
                    }
                    catch (CoreException e)
                    {
                        fBlockRendererStatus = new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
                        doStatusUpdate();
                    }
                }
                List<String>  groups = new ArrayList<String>();
                
                List<EJPluginRenderer> other = new ArrayList<EJPluginRenderer>();
                
                for (EJPluginRenderer renderer : renderers)
                {
                    if(renderer.getGroup()!=null && !renderer.getGroup().isEmpty())
                    {
                        if(!groups.contains(renderer.getGroup()))
                        groups.add(renderer.getGroup());
                    }
                    else
                        other.add(renderer); 
                        
                    
                }
                
                Collections.sort(groups,new Comparator<String>()
                {

                    public int compare(String o1, String o2)
                    {
                         if("Standard Renderers".equals(o1))
                         {
                             return -1;
                         }
                         if("Standard Renderers".equals(o2))
                         {
                             return 1;
                         }
                         if("Graph Renderers".equals(o1))
                         {
                             return -1;
                         }
                         if("Graph Renderers".equals(o2))
                         {
                             return 1;
                         }
                        return 0;
                    }
                });
                
                Collections.sort(other,new Comparator<EJPluginRenderer>()
                {

                    public int compare(EJPluginRenderer o1, EJPluginRenderer o2)
                    {
                     
                        return o1.getAssignedName().compareTo(o2.getAssignedName());
                    }
                });
               
                List<Object> all = new ArrayList<Object>();
                all.addAll(groups);
                all.addAll(other);
                
                return all.toArray();
            }

            public Object[] getChildren(Object parentElement)
            {
                if(parentElement instanceof String)
                {
                    List<EJPluginRenderer> group = new ArrayList<EJPluginRenderer>();
                    
                    for (EJPluginRenderer renderer : renderers)
                    {
                        if(parentElement.equals(renderer.getGroup()))
                        {
                            group.add(renderer);
                        }
                    }
                    
                    Collections.sort(group,new Comparator<EJPluginRenderer>()
                    {

                        public int compare(EJPluginRenderer o1, EJPluginRenderer o2)
                        {
                         
                            return o1.getAssignedName().compareTo(o2.getAssignedName());
                        }
                    });
                    
                    return group.toArray();
                }
                return new  Object[0];
            }

            public Object getParent(Object element)
            {
                if(element instanceof EJPluginRenderer)
                    return ((EJPluginRenderer)element).getGroup();
                return null;
            }

            public boolean hasChildren(Object element)
            {
                return element instanceof String;
            }
        });
        blockRenderersViewer.getTree().setItemCount(12);
        
        blockRenderersViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockRenderersViewer.getSelection() instanceof IStructuredSelection  )
                {
                    Object firstElement = ((IStructuredSelection) blockRenderersViewer.getSelection()).getFirstElement();
                    if(firstElement instanceof EJPluginRenderer)
                    {
                        blockRenderer = (EJPluginRenderer) firstElement; 
                        blockRenderersViewer.getTree().hideDropDown();
                    }
                    else
                        blockRenderer = null;
                    
                }
                fBlockRendererStatus = formRendererChanged();
                doStatusUpdate();
            }
        });
        createEmptySpace(composite, 1);
        refreshFormRenderers();
    }

    public void refreshFormRenderers()
    {
        if (blockRenderersViewer != null)
        {
            blockRenderersViewer.setInput(new Object());
//            blockRenderersViewer.getCombo().select(-1);
//            if (blockRenderersViewer.getCombo().getItemCount() > 0 && blockRenderersViewer.getCombo().getSelectionIndex() == -1)
//            {
//                blockRenderersViewer.getCombo().select(0);
//                if (blockRenderersViewer.getSelection() instanceof IStructuredSelection)
//                    blockRenderer = (EJPluginRenderer) ((IStructuredSelection) blockRenderersViewer.getSelection()).getFirstElement();
//            }
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
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Block name is empty.");
        }

        IStatus result = ResourcesPlugin.getWorkspace().validateName(typeName, IResource.FILE);
        if (!result.isOK())
        {
            return result;
        }

        IPackageFragment packageFragment = getPackageFragment();

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IFolder folder = root.getFolder(packageFragment.getPath());
        final IFile formFile = folder.getFile(new Path(typeName + "." + EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX));
        if (folder.exists() && formFile.exists())
        {

            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Block already exists.");

        }

        URI location = formFile.getLocationURI();
        if (location != null)
        {
            try
            {
                IFileStore store = EFS.getStore(location);
                if (store.fetchInfo().exists())
                {
                    return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Block with same name but different case exists.");
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
        if (blockRenderer == null)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Please choose a block renderer.");
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
            if (controlBlock || blockServiceClass == null || blockServiceClass.trim().length() == 0)
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

    private void createControlBlockControls(Composite composite, int nColumns)
    {

        final Button btnCreateService = new Button(composite, SWT.CHECK);
        btnCreateService.setText("Control Block");

        btnCreateService.setSelection(controlBlock);

        btnCreateService.addSelectionListener(new SelectionListener()
        {

            public void widgetSelected(SelectionEvent e)
            {
                controlBlock = btnCreateService.getSelection();
                blockServiceStatus = blockServiceChanged();
                doStatusUpdate();
                blockServiceText.setEnabled(!controlBlock);
                browse.setEnabled(!controlBlock);
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                controlBlock = btnCreateService.getSelection();
                blockServiceStatus = blockServiceChanged();
                doStatusUpdate();
                blockServiceText.setEnabled(!controlBlock);
                browse.setEnabled(!controlBlock);
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = nColumns;

        btnCreateService.setLayoutData(gd);
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
                monitor.beginTask("Creating Referenced Block " + formName, 2);
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IFolder folder = root.getFolder(packageFragment.getPath());
                if (!folder.exists())
                {
                    folder.create(false, true, monitor);
                }

                final IFile formFile = folder.getFile(new Path(formName + "." + EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX));

                String rendererName = blockRenderer.getAssignedName();

                EJPluginFormProperties formProperties = new EJPluginFormProperties(formName, project);

                int height = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_HEIGHT);
                int width = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_WIDTH);

                formProperties.setFormHeight(height);
                formProperties.setFormWidth(width);
                formProperties.setNumCols(1);

                final EJPluginBlockProperties blockProperties = new EJPluginBlockProperties(formProperties, formName, controlBlock);

                formProperties.getBlockContainer().addBlockProperties(blockProperties);
                blockProperties.setBlockRendererName(rendererName, true);
                // create items if service is also selected
                if (!controlBlock && blockServiceClass != null && blockServiceClass.trim().length() > 0)
                {
                    blockProperties.setServiceClassName(blockServiceClass, true);
                }

                FormPropertiesWriter writer = new FormPropertiesWriter();
                writer.saveForm(formProperties, formFile, monitor);
                EJPluginEntireJPropertiesLoader.reload(formProperties.getJavaProject());
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
