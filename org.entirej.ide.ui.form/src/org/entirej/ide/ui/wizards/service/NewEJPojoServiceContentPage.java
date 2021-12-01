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
package org.entirej.ide.ui.wizards.service;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectDescription;
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
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.ToolFactory;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.formatter.CodeFormatter;
import org.eclipse.jdt.core.manipulation.OrganizeImportsOperation;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
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
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.text.edits.TextEdit;
import org.entirej.framework.core.service.EJPojoContentGenerator;
import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJServiceContentGenerator;
import org.entirej.framework.core.service.EJServiceGeneratorType;
import org.entirej.framework.plugin.gen.FTLEngine;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;
import org.entirej.ide.core.spi.BlockServiceContentProvider;
import org.entirej.ide.core.spi.BlockServiceContentProvider.BlockServiceContent;
import org.entirej.ide.core.spi.BlockServiceContentProvider.BlockServiceWizardProvider;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEJPojoServiceContentPage extends NewTypeWizardPage implements BlockServiceContentProvider.GeneratorContext
{

    private ComboViewer                 comboProviderViewer;
    private Label                       providerDescription;
    private BlockServiceContentProvider blockServiceContentProvider;
    private BlockServiceWizardProvider  wizardProvider;

    private boolean                     createSerivce   = true;
    private boolean                     serviceOptional = true;

    private IJavaProject                currentProject;
    private String                      contentProviderError;
    /**
     * This wizard's list of pages (element type: <code>IWizardPage</code>).
     */
    private List<IWizardPage>           pages           = new ArrayList<IWizardPage>();
    private List<IWizardPage>           opPages         = new ArrayList<IWizardPage>();
    private List<Runnable>              postActions     = new ArrayList<Runnable>();
    private NewEJPojoServiceSelectPage  pojoPage;

    public NewEJPojoServiceContentPage(NewEJPojoServiceSelectPage pojoServiceSelectPage)
    {
        super(true, "ej.pojo.content");
        setTitle("Block Service/Pojo Content");
        setDescription("Enter the data required to generate the block service/pojo.");

        this.pojoPage = pojoServiceSelectPage;
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
                wizardProvider = null;
                initServiceContentProvider();
            }

            public void widgetDefaultSelected(SelectionEvent e)
            {
                createSerivce = btnCreateService.getSelection();
                wizardProvider = null;
                initServiceContentProvider();
            }
        });
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = nColumns;

        btnCreateService.setLayoutData(gd);
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

    public IPackageFragmentRoot getPackageFragmentRoot()
    {
        return pojoPage.getPackageFragmentRoot();
    }

    public BlockServiceWizardProvider getWizardProvider()
    {
        return wizardProvider;
    }

    public IJavaProject getProject()
    {
        return getJavaProject();
    }

    public boolean skipService()
    {
        return !isCreateSerivce();
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
        createContainerControls(composite, nColumns);
        if (serviceOptional)
        {
            createSeparator(composite, nColumns);
            createServiceOptionControls(composite, 3);
            createDescComponent(composite).setText("If you choose not to generate the Block Service then only the Pojo will be generated");
        }

        createEmptySpace(composite, 1);
        createEmptySpace(composite, 4);
        createEmptySpace(composite, 4);

        createEmptySpace(composite, 4);
        createSeparator(composite, nColumns);
        createProviderGroup(composite);

        setControl(composite);
        Dialog.applyDialogFont(composite);
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
        IJavaProject javaProject = getJavaProject();
        if (!javaProject.equals(currentProject))
        {
            currentProject = javaProject;
            comboProviderViewer.setInput(new Object());
            cleanSubPages();
            blockServiceContentProvider = null;
            wizardProvider = null;
            setDescription("Enter the data required to generate the block service/pojo.");
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
                        subPages = wizardProvider.getOptionalPages();

                        for (IWizardPage wizardPage : subPages)
                        {
                            opPages.add(wizardPage);
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
        for (IWizardPage page : opPages)
        {
            page.dispose();
        }
        opPages.clear();
    }

    private void updateProviderDesc()
    {
        providerDescription.setText(blockServiceContentProvider != null ? blockServiceContentProvider.getDescription()
                : "Select block service content provider to generate the pojo and service");
    }

    protected void createProviderGroup(final Composite container)
    {
        final Group group = new Group(container, SWT.NONE);
        group.setText("Content Provider");
        group.setLayout(new GridLayout(2, false));
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.horizontalSpan = 4;
        group.setLayoutData(layoutData);

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
                Collections.sort(exportProviders, new Comparator<BlockServiceContentProvider>()
                {

                    public int compare(BlockServiceContentProvider o1, BlockServiceContentProvider o2)
                    {

                        return o1.getProviderName().compareTo(o2.getProviderName());
                    }
                });
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
            setMessage("Please select block service/pojo content provider.");
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

    public IWizardPage getOptionalNextPage(IWizardPage page)
    {
        int index = opPages.indexOf(page);
        if (index == -1)
        {
            if (opPages.isEmpty())
                return null;

            return opPages.get(0);
        }
        if (opPages.size() >= (index + 1))
        {
            return null;
        }
        IWizardPage iWizardPage = opPages.get(index + 1);
        if (wizardProvider.skipPage(iWizardPage))
            return getOptionalNextPage(iWizardPage);
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

    public IWizardPage getOptinalStartingPage()
    {
        if (opPages.size() == 0)
        {
            return null;
        }
        return opPages.get(0);
    }

    public int getPageCount()
    {
        return pages.size();
    }

    public int getOptionalPageCount()
    {
        return opPages.size();
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

    public IWizardPage getOptionalPreviousPage(IWizardPage page)
    {
        int index = opPages.indexOf(page);
        if (index == 0 || index == -1)
        {
            // first page or page not found
            return null;
        }
        IWizardPage iWizardPage = opPages.get(index - 1);
        if (wizardProvider.skipPage(iWizardPage))
            return getOptionalPreviousPage(iWizardPage);
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

    public void createPojoService(NewEJPojoServiceSelectPage pojoPage, NewEJGenServicePage servicePage, boolean build, IProgressMonitor monitor)
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
            String pojoClassName = null;
            if (!wizardProvider.skipMainPojo())
                pojoClassName = createPojoClass(pojoGeneratorType, build, monitor);

            if (isCreateSerivce())
            {

                createServiceClass(blockServiceContent, pojoClassName, servicePage, monitor);
            }
            runPostActions();
        }
        catch (final Throwable e)
        {
            EJCoreLog.logException(e);
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

    public void runPostActions()
    {
        List<Runnable> _postActions = new ArrayList<Runnable>(postActions);
        postActions.clear();

        for (Runnable runnable : _postActions)
        {
            Display.getDefault().asyncExec(runnable);
        }
    }

    public String createPojoClass(EJPojoGeneratorType pojoGeneratorType, boolean build, IProgressMonitor monitor) throws Exception, CoreException
    {

        EJCoreLog.logInfoMessage("Start - > createPojoClass");
        Class<?> pojoGeneratorClass = EJPluginEntireJClassLoader.loadClass(getJavaProject(), wizardProvider.getPogoGenerator());
        if (!EJPojoContentGenerator.class.isAssignableFrom(pojoGeneratorClass))
        {
            throw new IllegalArgumentException("The pojo generator does not implement the interface: EJPojoContentGenerator");
        }
        EJCoreLog.logInfoMessage("Start - > EJPojoContentGenerator");
        EJPojoContentGenerator pojoContentGenerator = (EJPojoContentGenerator) pojoGeneratorClass.newInstance();

        pojoGeneratorType.setPackageName(pojoPage.getPackageText());

        EJCoreLog.logInfoMessage("pkg - > " + pojoPage.getPackageText());

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
            EJCoreLog.logInfoMessage("start class - > " + pojoGeneratorType.getClassName());
            ICompilationUnit parentCU = pack.createCompilationUnit(pojoGeneratorType.getClassName() + ".java", "", true, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$
            // create a working copy with a new owner
            parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));
            connectedCU = parentCU;
            EJCoreLog.logInfoMessage("end class - > " + pojoGeneratorType.getClassName());

            IBuffer buffer = parentCU.getBuffer();

            EJCoreLog.logInfoMessage("start FTLEngine - > " + pojoGeneratorType);
            EJCoreLog.logInfoMessage("start FTLEngine src- > " + pojoContentGenerator.getTemplate());
            String fileContents = FTLEngine.genrateFormPojo(pojoContentGenerator.getTemplate(), pojoGeneratorType);

            if (fileContents == null)
            {
                throw new IllegalArgumentException("No content provided by chosen pojo generator.");
            }
            EJCoreLog.logInfoMessage("end FTLEngine - > " + fileContents);

            EJCoreLog.logInfoMessage("start CodeFormatter - > ");
            CodeFormatter codeFormatter = ToolFactory.createCodeFormatter(getProject().getOptions(true));
            IDocument doc = new Document(fileContents);
            TextEdit edit = codeFormatter.format(CodeFormatter.K_COMPILATION_UNIT, doc.get(), 0, doc.get().length(), 0, null);
            if (edit != null)
            {
                edit.apply(doc);
                fileContents = doc.get();
            }
            EJCoreLog.logInfoMessage("END CodeFormatter - > " + fileContents);

            buffer.setContents(fileContents);
            final IType createdType = parentCU.getType(pojoGeneratorType.getClassName());

            // EJCoreLog.logInfoMessage("start commitWorkingCopy - > ");
            // connectedCU.commitWorkingCopy(true, new
            // SubProgressMonitor(monitor, 1));
            // EJCoreLog.logInfoMessage("end commitWorkingCopy - > ");

            EJCoreLog.logInfoMessage("start organizeImports - > ");
            organizeImports(connectedCU, monitor);
            EJCoreLog.logInfoMessage("end organizeImports - > ");

            EJCoreLog.logInfoMessage("start commitWorkingCopy - > ");
            connectedCU.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
            EJCoreLog.logInfoMessage("end commitWorkingCopy - > ");

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
            EJCoreLog.logInfoMessage("return - > " + createdType.getFullyQualifiedName('$'));
            return createdType.getFullyQualifiedName('$');
        }
        finally
        {
            if (connectedCU != null)
            {
                connectedCU.close();
                connectedCU.discardWorkingCopy();
            }

            IJavaProject javaProject = getJavaProject();

            IProjectDescription description = javaProject.getProject().getDescription();
            String[] natures = description.getNatureIds();
            boolean updateNatures = false;
            try
            {

                List<String> newNatures = new ArrayList<String>(Arrays.asList(natures));
                try
                {
                    if (newNatures.contains("org.eclipse.m2e.core.maven2Nature"))
                    {
                        updateNatures = true;
                        newNatures.remove("org.eclipse.m2e.core.maven2Nature");
                        description.setNatureIds(newNatures.toArray(new String[0]));
                        javaProject.getProject().setDescription(description, null);
                    }

                    javaProject.getProject().build(IncrementalProjectBuilder.AUTO_BUILD, monitor);

                }
                finally
                {
                    if (updateNatures)
                    {
                        description.setNatureIds(natures);
                        javaProject.getProject().setDescription(description, null);
                    }
                }
            }
            catch (CoreException e)
            {
                e.printStackTrace();
            }

        }

    }

    public void build(IProgressMonitor monitor)
    {
        IJavaProject javaProject = getJavaProject();
        try
        {
            javaProject.getProject().build(IncrementalProjectBuilder.AUTO_BUILD, monitor);
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }

    }

    private void organizeImports(final ICompilationUnit cu, final IProgressMonitor monitor) throws OperationCanceledException, CoreException
    {

        postActions.add(new Runnable()
        {

            public void run()
            {
                CompilationUnit unit;
                try
                {
                    unit = cu.reconcile(AST.JLS8, false, null, monitor);
                    OrganizeImportsOperation op = new OrganizeImportsOperation(cu, unit, true, true, true, null);
                    op.run(monitor);
                    
                    cu.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
                }
                catch (JavaModelException e)
                {
                    e.printStackTrace();
                }
                catch (OperationCanceledException e)
                {
                    e.printStackTrace();
                }
                catch (CoreException e)
                {
                    e.printStackTrace();
                }

            }
        });

    }

    private void createServiceClass(BlockServiceContent blockServiceContent, final String pojoClassName, NewEJGenServicePage servicePage,
            IProgressMonitor monitor) throws Exception, CoreException
    {
        EJServiceContentGenerator serviceContentGenerator = createServiceContentGenerator(servicePage.getJavaProject(), wizardProvider.getServiceGenerator());

        EJServiceGeneratorType serviceGeneratorType = blockServiceContent.getServiceGeneratorType();
        String serviceClassName = servicePage.getTypeName();

        serviceGeneratorType.setServiceName(serviceClassName);

        if (pojoClassName != null)
        {

            Class<?> pojoClass = EJPluginEntireJClassLoader.loadClass(servicePage.getJavaProject(), pojoClassName);
            serviceGeneratorType.setPojo(pojoClass);
        }
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
            final ICompilationUnit parentCU = pack.createCompilationUnit(serviceClassName + ".java", "", false, new SubProgressMonitor(monitor, 2)); //$NON-NLS-1$
            // create a working copy with a new owner
            parentCU.becomeWorkingCopy(new SubProgressMonitor(monitor, 1));
            connectedCU = parentCU;

            IBuffer buffer = parentCU.getBuffer();
            String fileContents = FTLEngine.genrateFormService(serviceContentGenerator.getTemplate(), serviceGeneratorType);

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

            organizeImports(connectedCU, monitor);
            connectedCU.commitWorkingCopy(true, new SubProgressMonitor(monitor, 1));
            getShell().getDisplay().asyncExec(new Runnable()
            {
                public void run()
                {
                    IWizard iWizard = getWizard();
                    if (iWizard instanceof NewWizard)
                    {
                        NewWizard wizard = (NewWizard) iWizard;
                        if (pojoClassName != null)
                        {
                            IType createdType = parentCU.getType(pojoClassName);
                            wizard.selectAndReveal(createdType.getResource());
                            wizard.openResource((IFile) createdType.getResource());
                        }
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

    public boolean pageOfMain(IWizardPage page)
    {
        // TODO Auto-generated method stub
        return pages.contains(page);
    }

    public void init(IStructuredSelection selection)
    {
        IJavaElement jelem = getInitialJavaElement(selection);
        initContainerPage(jelem);
        initTypePage(jelem);
    }
}
