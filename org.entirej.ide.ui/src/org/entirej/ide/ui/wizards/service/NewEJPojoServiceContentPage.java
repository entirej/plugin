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

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IBuffer;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.internal.corext.codemanipulation.OrganizeImportsOperation;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.text.Document;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.text.edits.TextEdit;
import org.entirej.framework.core.service.EJPojoContentGenerator;
import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJServiceContentGenerator;
import org.entirej.framework.core.service.EJServiceGeneratorType;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;
import org.entirej.ide.core.spi.BlockServiceContentProvider;
import org.entirej.ide.core.spi.BlockServiceContentProvider.BlockServiceContent;
import org.entirej.ide.core.spi.BlockServiceContentProvider.BlockServiceWizardProvider;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEJPojoServiceContentPage extends WizardPage implements BlockServiceContentProvider.GeneratorContext
{

    private ComboViewer                      comboProviderViewer;
    private Label                            providerDescription;
    private BlockServiceContentProvider      blockServiceContentProvider;
    private BlockServiceWizardProvider       wizardProvider;
    private final IJavaProjectProvider       projectProvider;
    private final NewEJPojoServiceSelectPage pojoPage;

    private IJavaProject                     currentProject;
    private String                           contentProviderError;
    /**
     * This wizard's list of pages (element type: <code>IWizardPage</code>).
     */
    private List<IWizardPage>                pages = new ArrayList<IWizardPage>();

    public NewEJPojoServiceContentPage(NewEJPojoServiceSelectPage pojoServiceSelectPage)
    {
        super("ej.pojo.content");
        setTitle("Block Service Content");
        setDescription("Enter the data required to generate the block service.");
        this.projectProvider = pojoServiceSelectPage;
        this.pojoPage = pojoServiceSelectPage;
    }

    public IPackageFragmentRoot getPackageFragmentRoot()
    {
        return pojoPage.getPackageFragmentRoot();
    }

    public IJavaProject getProject()
    {
        return projectProvider.getJavaProject();
    }

    public boolean skipService()
    {
        return !pojoPage.isCreateSerivce();
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        int nColumns = 1;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);

        createProviderGroup(composite);
        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    protected void addSeparator(Composite composite)
    {
        Label separator = new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL);
        gd.horizontalAlignment = GridData.FILL;
        gd.verticalAlignment = GridData.BEGINNING;
        gd.heightHint = convertHeightInCharsToPixels(1);
        gd.horizontalSpan = 1;
        separator.setLayoutData(gd);
    }

    @Override
    public void setVisible(boolean visible)
    {
        super.setVisible(visible);
        if (visible)
        {
            Display.getDefault().asyncExec(new Runnable()
            {

                public void run()
                {

                    initServiceContentProvider();

                }
            });
        }
    }

    public void initServiceContentProvider()
    {
        IJavaProject javaProject = projectProvider.getJavaProject();
        if (!javaProject.equals(currentProject))
        {
            currentProject = javaProject;
            comboProviderViewer.setInput(new Object());
            cleanSubPages();
            blockServiceContentProvider = null;
            wizardProvider = null;
            setDescription("Enter the data required to generate the block service.");
            return;
        }
        if (blockServiceContentProvider != null)
        {

            if (wizardProvider != null)
            {
                // make sure refresh if project is updated
                if (!javaProject.equals(currentProject))
                {
                    currentProject = javaProject;

                    wizardProvider.init(this);
                }
                return;
            }
            // cleanup old pages
            cleanSubPages();
            wizardProvider = blockServiceContentProvider.createWizardProvider();
            IRunnableWithProgress loadColumns = new IRunnableWithProgress()
            {

                public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
                {
                    try
                    {
                        contentProviderError = null;
                        wizardProvider.init(NewEJPojoServiceContentPage.this);
                        List<IWizardPage> subPages = wizardProvider.getPages();

                        for (IWizardPage wizardPage : subPages)
                        {
                            pages.add(wizardPage);
                            wizardPage.setWizard(getWizard());
                        }

                    }
                    catch (Exception e)
                    {
                        contentProviderError = e.getMessage();
                    }

                }

            };
            setPageComplete(false);
            try
            {
                getContainer().run(false, false, loadColumns);

            }
            catch (Exception e)
            {
                contentProviderError = e.getMessage();
            }
            finally
            {
                setPageComplete(validatePage());
            }

        }

    }

    private void cleanSubPages()
    {
        for (IWizardPage page : pages)
        {
            page.dispose();
        }
        pages.clear();
    }

    private void updateProviderDesc()
    {
        providerDescription.setText(blockServiceContentProvider != null ? blockServiceContentProvider.getDescription()
                : "Select block service content provider to generate the pojo and service");
    }

    protected void createProviderGroup(final Composite container)
    {
        final Group group = new Group(container, SWT.NONE);
        group.setText("Block Service Content Provider");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_BOTH));

        comboProviderViewer = new ComboViewer(group);
        comboProviderViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        providerDescription = createDescComponent(group);

        comboProviderViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof BlockServiceContentProvider)
                {
                    return ((BlockServiceContentProvider) element).getProviderName();
                }
                return super.getText(element);
            }

        });
        final List<BlockServiceContentProvider> exportProviders = new ArrayList<BlockServiceContentProvider>();
        currentProject = getProject();
        comboProviderViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                exportProviders.clear();
                IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(BlockServiceContentProvider.EXTENSION_POINT_ID);

                try
                {
                    for (IConfigurationElement element : config)
                    {
                        final Object impl = element.createExecutableExtension("class");
                        if (impl instanceof BlockServiceContentProvider)
                        {
                            BlockServiceContentProvider provider = (BlockServiceContentProvider) impl;
                            if (provider.isActive(currentProject))
                                exportProviders.add(provider);
                        }
                    }
                }
                catch (CoreException ex)
                {
                    EJCoreLog.log(ex);
                }
                return exportProviders.toArray();
            }
        });
        comboProviderViewer.setInput(new Object());
        comboProviderViewer.setSelection(new StructuredSelection());
        comboProviderViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (comboProviderViewer.getSelection() instanceof IStructuredSelection)
                {
                    BlockServiceContentProvider newProvider = (BlockServiceContentProvider) ((IStructuredSelection) comboProviderViewer.getSelection())
                            .getFirstElement();
                    if (newProvider != null && !newProvider.equals(blockServiceContentProvider))
                    {
                        blockServiceContentProvider = newProvider;
                        wizardProvider = null;
                        // discard blockServiceContent
                        Display.getDefault().asyncExec(new Runnable()
                        {

                            public void run()
                            {

                                initServiceContentProvider();

                            }
                        });

                    }

                    updateProviderDesc();
                    container.layout();
                }

            }
        });
        updateProviderDesc();
        setPageComplete(validatePage());
    }

    private Label createDescComponent(Composite composite)
    {
        Label label = new Label(composite, SWT.NULL | SWT.WRAP);
        GridData gd = new GridData();
        gd.horizontalIndent = 10;
        gd.verticalIndent = 5;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        gd.minimumHeight = 150;
        label.setLayoutData(gd);
        return label;
    }

    protected boolean validatePage()
    {

        if (blockServiceContentProvider == null)
        {
            setMessage("Please select block service content provider.");
            return false;
        }

        if (contentProviderError != null)
        {
            setErrorMessage(contentProviderError);
            return false;
        }
        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    public boolean canFinish()
    {

        for (IWizardPage page : pages)
        {
            if (!wizardProvider.canFinish(page))
            {
                return false;
            }
        }

        return true;
    }

    public IWizardPage getNextPage(IWizardPage page)
    {
        int index = pages.indexOf(page);
        if (index == pages.size() - 1 || index == -1)
        {
            // last page or page not found
            return null;
        }
        IWizardPage iWizardPage = pages.get(index + 1);
        if (wizardProvider.skipPage(iWizardPage))
            return getNextPage(iWizardPage);
        return iWizardPage;
    }

    public IWizardPage getStartingPage()
    {
        if (pages.size() == 0)
        {
            return null;
        }
        return pages.get(0);
    }

    public int getPageCount()
    {
        return pages.size();
    }

    public IWizardPage getPreviousPage(IWizardPage page)
    {
        int index = pages.indexOf(page);
        if (index == 0 || index == -1)
        {
            // first page or page not found
            return null;
        }
        IWizardPage iWizardPage = pages.get(index - 1);
        if (wizardProvider.skipPage(iWizardPage))
            return getPreviousPage(iWizardPage);
        return iWizardPage;
    }

    @Override
    public void dispose()
    {
        cleanSubPages();
        blockServiceContentProvider = null;
        super.dispose();
    }

    private EJServiceContentGenerator createServiceContentGenerator(IJavaProject project, String className) throws Exception
    {

        Class<?> serviceGeneratorClass = EJPluginEntireJClassLoader.loadClass(project, className);
        if (!EJServiceContentGenerator.class.isAssignableFrom(serviceGeneratorClass))
        {
            throw new IllegalArgumentException("The service generator does not implement the interface: EJServiceContentGenerator");
        }

        return (EJServiceContentGenerator) serviceGeneratorClass.newInstance();

    }

    public void createPojoService(NewEJPojoServiceSelectPage pojoPage, NewEJGenServicePage servicePage, IProgressMonitor monitor)
    {
        try
        {
            if (wizardProvider == null)
            {
                return;
            }
            wizardProvider.createRequiredResources(monitor);
            BlockServiceContent blockServiceContent = wizardProvider.getContent();
            if (blockServiceContent == null)
            {
                return;
            }

            EJPojoGeneratorType pojoGeneratorType = blockServiceContent.getpPojoGeneratorType();
            pojoGeneratorType.setPackageName(pojoGeneratorType.getPackageName());
            pojoGeneratorType.setClassName(pojoPage.getTypeName());
            String pojoClassName = createPojoClass(pojoGeneratorType, monitor);

            if (pojoPage.isCreateSerivce())
            {
                createServiceClass(blockServiceContent, pojoClassName, servicePage, monitor);
            }
        }
        catch (final Exception e)
        {
            getShell().getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    setErrorMessage(e.getMessage());
                }
            });
            throw new RuntimeException(e);
        }
    }

    public String createPojoClass(EJPojoGeneratorType pojoGeneratorType, IProgressMonitor monitor) throws Exception, CoreException
    {

        Class<?> pojoGeneratorClass = EJPluginEntireJClassLoader.loadClass(projectProvider.getJavaProject(), pojoPage.getPojoGeneratorClass());
        if (!EJPojoContentGenerator.class.isAssignableFrom(pojoGeneratorClass))
        {
            throw new IllegalArgumentException("The pojo generator does not implement the interface: EJPojoContentGenerator");
        }

        EJPojoContentGenerator pojoContentGenerator = (EJPojoContentGenerator) pojoGeneratorClass.newInstance();

        pojoGeneratorType.setPackageName(pojoPage.getPackageText());

        IPackageFragmentRoot root = getPackageFragmentRoot();
        IPackageFragment pack = root.getPackageFragment(pojoGeneratorType.getPackageName());
        if (pack == null)
        {
            pack = root.getPackageFragment(""); //$NON-NLS-1$
        }

        if (!pack.exists())
        {
            String packName = pack.getElementName();
            pack = root.createPackageFragment(packName, true, new SubProgressMonitor(monitor, 1));
        }
        else
        {
            monitor.worked(1);
        }
        pojoGeneratorType.setPackageName(pack.getElementName());
        ICompilationUnit connectedCU = null;

        try
        {
            ICompilationUnit parentCU = pack.createCompilationUnit(pojoGeneratorType.getClassName() + ".java", "", true, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$
            // create a working copy with a new owner
            parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));
            connectedCU = parentCU;

            IBuffer buffer = parentCU.getBuffer();
            String fileContents = pojoContentGenerator.generateContent(pojoGeneratorType);

            if (fileContents == null)
            {
                throw new IllegalArgumentException("No content provided by chosen pojo generator.");
            }

            CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(getProject().getOptions(true));
            IDocument doc = new Document(fileContents);
            TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0, doc.get().length(), 0, null);
            if (edit != null)
            {
                edit.apply(doc);
                fileContents = doc.get();
            }

            buffer.setContents(fileContents);
            final IType createdType = parentCU.getType(pojoGeneratorType.getClassName());
            organizeImports(connectedCU);
            connectedCU.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
            
            
           
            getShell().getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    IWizard iWizard = getWizard();
                    if (iWizard instanceof NewWizard)
                    {
                        NewWizard wizard = (NewWizard) iWizard;
                        wizard.selectAndReveal(createdType.getResource());
                        wizard.openResource((IFile) createdType.getResource());
                    }

                }
            });
            return createdType.getFullyQualifiedName('$');
        }
        finally
        {
            if (connectedCU != null)
            {
                connectedCU.discardWorkingCopy();
            }
            IJavaProject javaProject = pojoPage.getJavaProject();
            javaProject.getProject().build(IncrementalProjectBuilder.INCREMENTAL_BUILD, monitor);
        }

    }
    
    
    @SuppressWarnings("restriction")
    private void organizeImports(ICompilationUnit cu)
            throws OperationCanceledException, CoreException {

       
        CompilationUnit unit = cu.reconcile(AST.JLS4, false, null, new NullProgressMonitor());
      
        OrganizeImportsOperation op = new OrganizeImportsOperation(cu, unit,
                true, true, true, null);
        op.run(new NullProgressMonitor());
        
    }

    private void createServiceClass(BlockServiceContent blockServiceContent, String pojoClassName, NewEJGenServicePage servicePage, IProgressMonitor monitor)
            throws Exception, CoreException
    {
        EJServiceContentGenerator serviceContentGenerator = createServiceContentGenerator(servicePage.getJavaProject(), servicePage.getPojoGeneratorClass());
        Class<?> pojoClass = EJPluginEntireJClassLoader.loadClass(servicePage.getJavaProject(), pojoClassName);

        EJServiceGeneratorType serviceGeneratorType = blockServiceContent.getServiceGeneratorType();
        String serviceClassName = servicePage.getTypeName();
        serviceGeneratorType.setServiceName(serviceClassName);
        serviceGeneratorType.setPojo(pojoClass);
        serviceGeneratorType.setPackageName(servicePage.getPackageText());

        IPackageFragmentRoot root = servicePage.getPackageFragmentRoot();
        IPackageFragment pack = servicePage.getPackageFragment();

        if (!pack.exists())
        {
            String packName = pack.getElementName();
            pack = root.createPackageFragment(packName, true, new SubProgressMonitor(monitor, 1));
        }
        else
        {
            monitor.worked(1);
        }

        ICompilationUnit connectedCU = null;

        try
        {
            ICompilationUnit parentCU = pack.createCompilationUnit(serviceClassName + ".java", "", false, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$
            // create a working copy with a new owner
            parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));
            connectedCU = parentCU;

            IBuffer buffer = parentCU.getBuffer();
            String fileContents = serviceContentGenerator.generateContent(serviceGeneratorType);

            if (fileContents == null)
            {
                throw new IllegalArgumentException("No content provided by chosen service generator.");
            }
            CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(getProject().getOptions(true));
            IDocument doc = new Document(fileContents);
            TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0, doc.get().length(), 0, null);
            if (edit != null)
            {
                edit.apply(doc);
                fileContents = doc.get();
            }

            buffer.setContents(fileContents);
            final IType createdType = parentCU.getType(pojoClassName);
            organizeImports(connectedCU);
            connectedCU.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
          
            getShell().getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    IWizard iWizard = getWizard();
                    if (iWizard instanceof NewWizard)
                    {
                        NewWizard wizard = (NewWizard) iWizard;
                        wizard.selectAndReveal(createdType.getResource());
                        wizard.openResource((IFile) createdType.getResource());
                    }

                }
            });
        }
        finally
        {
            if (connectedCU != null)
            {
                connectedCU.discardWorkingCopy();
            }
        }

    }
}
