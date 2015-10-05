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
package org.entirej.ide.ui.editors.form;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.editors.form.UsageTreeSection.Usage;
import org.entirej.ide.ui.editors.form.UsageTreeSection.UsageGroup;
import org.entirej.ide.ui.editors.handlers.PageActionHandler;
import org.entirej.ide.ui.editors.handlers.PageActionHandlerProvider;
import org.entirej.ide.ui.utils.FormsUtil;

public class EJFormReferencePage extends AbstractEditorPage implements PageActionHandlerProvider
{
    protected AbstractEJFormEditor editor;
    protected UsageTreeSection     refrenceSection;
    public static final String     PAGE_ID = "ej.form.usage.References"; //$NON-NLS-1$

    private UsageGroup[]           refrs   = null;
    private final Object           LOCK    = new Object();

    public EJFormReferencePage(AbstractEJFormEditor editor)
    {
        super(editor, PAGE_ID, "References");
        this.editor = editor;
    }

    @Override
    protected void buildBody(IManagedForm managedForm, FormToolkit toolkit)
    {

        Composite body = managedForm.getForm().getBody();
        body.setLayout(EditorLayoutFactory.createFormGridLayout(true, 1));

        refrenceSection = createRefrenceSection(body);

        managedForm.addPart(refrenceSection);
    }

    protected UsageTreeSection createRefrenceSection(Composite body)
    {

        return new UsageTreeSection(editor, this, body)
        {

            @Override
            protected UsageGroup[] getUsageGroups()
            {
                synchronized (LOCK)
                {
                    if (refrs != null)
                    {
                        return refrs;
                    }
                    findReferences();

                    return new UsageGroup[0];
                }
            }

            @Override
            public String getSectionTitle()
            {
                return "References";
            }

            @Override
            public String getSectionDescription()
            {

                return "Referred from other resources.";
            }
        };
    }

    private void findReferences()
    {
        IRunnableWithProgress progress = new IRunnableWithProgress()
        {

            public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException
            {

                List<UsageGroup> groups = new ArrayList<UsageTreeSection.UsageGroup>();

                try
                {

                    findRefs(groups, monitor);

                }
                catch (JavaModelException e)
                {

                    e.printStackTrace();
                }
                catch (CoreException e)
                {

                    e.printStackTrace();
                }
                finally
                {
                    // synchronized (LOCK)
                    {
                        refrs = groups.toArray(new UsageGroup[0]);
                        Display.getDefault().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                if (!refrenceSection.getSection().isDisposed())
                                {
                                    refrenceSection.refresh();

                                    refrenceSection.expandNodes();
                                }

                            }
                        });
                    }
                }

            }
        };

        try
        {
            new ProgressMonitorDialog(getEditor().getEditorSite().getShell()).run(true, true, progress);
        }
        catch (InvocationTargetException e)
        {
            EJCoreLog.log(e);
        }
        catch (InterruptedException e)
        {
            EJCoreLog.log(e);
        }
    }

    protected void findRefs(List<UsageGroup> groups, IProgressMonitor monitor) throws JavaModelException, CoreException
    {
        IPackageFragmentRoot[] packageFragmentRoots = editor.getJavaProject().getPackageFragmentRoots();

        for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
        {
            if (monitor.isCanceled())
                break;
            if (iPackageFragmentRoot.getResource() instanceof IContainer)
                findFormsIn((IContainer) iPackageFragmentRoot.getResource(), groups, monitor);
        }

    }

    private void findFormsIn(IContainer container, List<UsageGroup> groups, IProgressMonitor monitor) throws CoreException
    {
        if (monitor.isCanceled())
            return;
        monitor.subTask("Finding references in  EJ Forms...");
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            IResource member = members[i];
            if (member instanceof IContainer)
                findFormsIn((IContainer) member, groups, monitor);
            else if (member instanceof IFile && isFormFile((IFile) member))
            {
                findInFile((IFile) member, groups, monitor);
            }
        }
        monitor.done();

    }

    public static void updateObjectGroupRef(EJPluginObjectGroupProperties properties, final AbstractEJFormEditor editor, IProgressMonitor monitor)
    {
        IPackageFragmentRoot[] packageFragmentRoots;
        try
        {
            packageFragmentRoots = editor.getJavaProject().getPackageFragmentRoots();
            for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
            {
                if (monitor.isCanceled())
                    break;
                if (iPackageFragmentRoot.getResource() instanceof IContainer)
                    updateOBjInForm((IContainer) iPackageFragmentRoot.getResource(), properties, editor, monitor);
            }
        }
        catch (JavaModelException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        catch (CoreException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private static void updateOBjInForm(IContainer container, EJPluginObjectGroupProperties properties, final AbstractEJFormEditor editor,
            IProgressMonitor monitor) throws CoreException
    {
        if (monitor.isCanceled())
            return;
        monitor.subTask("Finding references in  EJ Forms...");
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            IResource member = members[i];
            if (member instanceof IContainer)
                updateOBjInForm((IContainer) member, properties, editor, monitor);
            else if (member instanceof IFile && isFormFile((IFile) member))
            {
                IFile file = (IFile) member;
                if (monitor.isCanceled())
                    return;

                if (!(isForm(file)))
                {
                    return;
                }

                try
                {
                    // try to ignore outpu path
                    IJavaProject project = editor.getJavaProject();
                    IPath outputLocation = project.getOutputLocation();
                    if (outputLocation.isPrefixOf(file.getFullPath()))
                        return;
                }
                catch (JavaModelException e)
                {
                    // ignore
                }

                IFile propFile = EJProject.getPropertiesFile(file.getProject());
                if (propFile == null || !propFile.exists())
                {
                    return;
                }

                EJPluginFormProperties formProperties = getFormProperties(file, editor.getJavaProject());
                if (formProperties != null)
                {
                    boolean updateCanvasSettings = properties.updateCanvasSettings(formProperties);
                    if (updateCanvasSettings)

                    {
                        FormPropertiesWriter write = new FormPropertiesWriter();
                        write.saveForm(formProperties, file, monitor);
                    }
                }
            }
        }
        monitor.done();

    }

    private void findInFile(IFile file, List<UsageGroup> groups, IProgressMonitor monitor)
    {
        if (monitor.isCanceled())
            return;

        if (isForm(editor.getFile()) || isObjGroup(editor.getFile()) || isRefBlock(editor.getFile()))
        {
            // if form or ref block only forms & object groups will have
            // References
            if (!(isForm(file) || isObjGroup(file)))
            {
                return;
            }
        }
        else if (isObjGroup(editor.getFile()))
        {
            // if OBj Group only forms will have References
            if (!(isForm(file)))
            {
                return;
            }
        }

        try
        {
            // try to ignore outpu path
            IJavaProject project = editor.getJavaProject();
            IPath outputLocation = project.getOutputLocation();
            if (outputLocation.isPrefixOf(file.getFullPath()))
                return;
        }
        catch (JavaModelException e)
        {
            // ignore
        }
        IFile propFile = EJProject.getPropertiesFile(file.getProject());
        if (propFile == null || !propFile.exists())
        {
            return;
        }
        String message = NLS.bind("References lookup in  {0} ...", file.getFullPath().toString());
        monitor.subTask(message);

        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, 1);

        //

        EJPluginFormProperties formProperties = getFormProperties(file, editor.getJavaProject());
        if (formProperties != null)
        {

            if (isForm(editor.getFile()))
            {
                // check on form canvas

                {// detect inner forms
                    List<Usage> innerFormUsage = new ArrayList<UsageTreeSection.Usage>();
                    Collection<EJCanvasProperties> canvases = EJPluginCanvasRetriever.retriveAllCanvases(formProperties);
                    final String name = formProperties.getName();
                    for (final EJCanvasProperties canvas : canvases)
                    {

                        if (canvas.getType() == EJCanvasType.FORM)
                        {
                            final boolean form = isForm(file);
                            final String formId = canvas.getReferredFormId();
                            if (formId != null && formId.equals(editor.getFormProperties().getFormName()))
                            {
                                Usage usage = new Usage(name)
                                {

                                    @Override
                                    public void open()
                                    {
                                        FormsUtil.openForm(editor.getJavaProject(), name);

                                    }

                                    @Override
                                    public String getUsageInfo()
                                    {

                                        return String.format("Used in  Canvas : '%s'", name, canvas.getName());
                                    }

                                    @Override
                                    public Image getImage()
                                    {

                                        return EJUIImages.getImage(form ? EJUIImages.DESC_CANVAS_FORM : EJUIImages.DESC_OBJGROUP);
                                    }
                                };
                                innerFormUsage.add(usage);

                            }
                        }
                    }

                    if (innerFormUsage.size() > 0)
                    {
                        createBlockRefGroup("Form Reference in", "Referred in other forms/objectgroups.", groups, innerFormUsage);

                    }
                }
            }
            else if (isObjGroup(editor.getFile()))
            {
                {// ref Object groups
                    List<Usage> innerObjGroupUsage = new ArrayList<UsageTreeSection.Usage>();
                    List<EJPluginObjectGroupProperties> groupProperties = formProperties.getObjectGroupContainer().getAllObjectGroupProperties();
                    final boolean form = isForm(file);
                    final String name = formProperties.getName();
                    for (final EJPluginObjectGroupProperties objgroup : groupProperties)
                    {

                        if (!objgroup.getName().equals(editor.getFormProperties().getName()))
                        {
                            continue;
                        }
                        Usage usage = new Usage(name)
                        {

                            @Override
                            public void open()
                            {
                                if (form)
                                {
                                    FormsUtil.openForm(editor.getJavaProject(), name);
                                }
                                else
                                {
                                    FormsUtil.openObjectGroupRefrence(editor.getJavaProject(), name);
                                }

                            }

                            @Override
                            public String getUsageInfo()
                            {
                                return String.format("Imported ObjectGroup: '%s'", objgroup.getName());
                            }

                            @Override
                            public Image getImage()
                            {
                                return EJUIImages.getImage(form ? EJUIImages.DESC_CANVAS_FORM : EJUIImages.DESC_OBJGROUP);
                            }
                        };
                        innerObjGroupUsage.add(usage);
                    }
                    if (innerObjGroupUsage.size() > 0)
                    {
                        createBlockRefGroup("ObjectGroup Reference in", "Referred in other forms/objectgroups", groups, innerObjGroupUsage);

                    }

                }
            }

            else if (isRefBlock(editor.getFile()))
            {
                {// ref Blocks groups
                    List<Usage> innerObjGroupUsage = new ArrayList<UsageTreeSection.Usage>();
                    List<EJPluginBlockProperties> groupProperties = formProperties.getBlockContainer().getAllBlockProperties();
                    final boolean form = isForm(file);
                    final String name = formProperties.getName();
                    for (final EJPluginBlockProperties objgroup : groupProperties)
                    {
                        if (objgroup.isImportFromObjectGroup() || objgroup.isMirrorChild())
                        {
                            continue;
                        }
                        if (objgroup.isReferenceBlock() && editor.getFormProperties().getName().equals(objgroup.getReferencedBlockName()))
                        {
                            Usage usage = new Usage(name)
                            {

                                @Override
                                public void open()
                                {
                                    if (form)
                                    {
                                        FormsUtil.openForm(editor.getJavaProject(), name);
                                    }
                                    else
                                    {
                                        FormsUtil.openObjectGroupRefrence(editor.getJavaProject(), name);
                                    }

                                }

                                @Override
                                public String getUsageInfo()
                                {
                                    return String.format("Referenced by  Block : '%s'", objgroup.getName());
                                }

                                @Override
                                public Image getImage()
                                {
                                    return EJUIImages.getImage(form ? EJUIImages.DESC_CANVAS_FORM : EJUIImages.DESC_OBJGROUP);
                                }
                            };
                            innerObjGroupUsage.add(usage);
                        }
                    }
                    if (innerObjGroupUsage.size() > 0)
                    {
                        createBlockRefGroup("Referred Block Reference in", "Referred in other forms/objectgroups.", groups, innerObjGroupUsage);

                    }
                }

            }
            else
            {
                // lov
                {// ref LOV groups

                    final boolean form = isForm(file);
                    final boolean refBlock = isRefBlock(file);
                    final String name = formProperties.getName();
                    System.err.println(name);
                    List<Usage> innerObjGroupUsage = new ArrayList<UsageTreeSection.Usage>();
                    List<EJPluginLovDefinitionProperties> groupProperties = formProperties.getLovDefinitionContainer().getAllLovDefinitionProperties();
                    for (final EJPluginLovDefinitionProperties objgroup : groupProperties)
                    {
                        if (objgroup.isImportFromObjectGroup())
                        {
                            continue;
                        }

                        if (!editor.getFormProperties().getName().equals(objgroup.getReferencedLovDefinitionName()))
                        {
                            continue;
                        }

                        Usage usage = new Usage(name)
                        {

                            @Override
                            public void open()
                            {
                                if (form)
                                {

                                    FormsUtil.openForm(editor.getJavaProject(), name);

                                }
                                else if (refBlock)
                                {
                                    FormsUtil.openRefBlockRefrence(editor.getJavaProject(), name);
                                }
                                else
                                {
                                    FormsUtil.openObjectGroupRefrence(editor.getJavaProject(), name);
                                }

                            }

                            @Override
                            public String getUsageInfo()
                            {
                                return String.format("Referenced as LOV : '%s'", objgroup.getName());
                            }

                            @Override
                            public Image getImage()
                            {
                                if (form)
                                {
                                    return EJUIImages.getImage(EJUIImages.DESC_FORM);
                                }
                                if (refBlock)
                                {
                                    return EJUIImages.getImage(EJUIImages.DESC_BLOCK_REF);
                                }
                                return EJUIImages.getImage(EJUIImages.DESC_OBJGROUP);
                            }
                        };
                        innerObjGroupUsage.add(usage);
                    }
                    if (innerObjGroupUsage.size() > 0)
                    {
                        createBlockRefGroup("Referred LOV Reference in", "Referred in other forms/objectgroups/ref-blocks..", groups, innerObjGroupUsage);
                    }

                }
            }

        }
        subProgressMonitor.done();
        monitor.subTask(" Updating ...");
        monitor.done();
    }

    private UsageGroup createBlockRefGroup(String id, String desc, List<UsageGroup> groups, List<Usage> innerObjGroupUsage)
    {
        UsageGroup group = null;

        for (UsageGroup usageGroup : groups)
        {

            if (usageGroup.getName().equals(id))
            {
                group = usageGroup;
                break;
            }
        }
        if (group == null)
        {
            group = new UsageGroup(id, desc, innerObjGroupUsage);
            groups.add(group);
        }
        else
        {
            group.getUsages().addAll(innerObjGroupUsage);
        }
        return group;
    }

    private static boolean isFormFile(IFile file)
    {
        return EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(file.getFileExtension()) || isRefFormFile(file);
    }

    private static boolean isRefFormFile(IFile file)
    {
        String fileExtension = file.getFileExtension();
        return EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJDevConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension);
    }

    @Override
    protected String getPageHeader()
    {
        return "References";
    }

    public PageActionHandler getActionHandler(String commandId)
    {

        return null;
    }

    public boolean isHandlerActive(String commandId)
    {

        return false;
    }

    public void refreshAfterBuid()
    {

    }

    @Override
    public void setActive(boolean active)
    {

        if (active)
        {
            refrs = null;
            refrenceSection.refresh();

            refrenceSection.expandNodes();
        }
    }

    static boolean isForm(IFile file)
    {
        return file.getName().endsWith(EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX);
    }

    static boolean isRefBlock(IFile file)
    {
        return file.getName().endsWith(EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
    }

    static boolean isObjGroup(IFile file)
    {
        return file.getName().endsWith(EJDevConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX);
    }

    static boolean isRefLov(IFile file)
    {
        return file.getName().endsWith(EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
    }

    static EJPluginFormProperties getFormProperties(IFile file, IJavaProject project)
    {

        EJPluginFormProperties formProperties = null;
        /*
         * IWorkbenchWindow[] windows =
         * EJUIPlugin.getDefault().getWorkbench().getWorkbenchWindows(); for
         * (IWorkbenchWindow window : windows) { if (window != null) {
         * IWorkbenchPage[] activePages = window.getPages(); for (IWorkbenchPage
         * page : activePages) { try { IEditorPart editor = page.findEditor(new
         * FileEditorInput(file)); if (editor instanceof AbstractEJFormEditor) {
         * formProperties = ((AbstractEJFormEditor) editor).getFormProperties();
         * if (formProperties != null) return formProperties; } } catch
         * (Throwable e) { //ignore any error } } } }
         */

        // read from file
        InputStream inStream = null;
        try
        {

            inStream = file.getContents();

            EntireJFormReader reader = new EntireJFormReader();
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            formProperties = reader.readForm(new FormHandler(project, fileName), project, inStream);
            formProperties.initialisationCompleted();
        }
        catch (Exception exception)
        {

            EJCoreLog.logWarnningMessage(exception.getMessage());
        }
        finally
        {

            try
            {
                if (inStream != null)
                    inStream.close();
            }
            catch (IOException e)
            {
                EJCoreLog.logException(e);
            }
        }

        return formProperties;
    }

}
