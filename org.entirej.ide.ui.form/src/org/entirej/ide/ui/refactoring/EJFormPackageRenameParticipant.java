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
package org.entirej.ide.ui.refactoring;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.entirej.framework.core.actionprocessor.interfaces.EJBlockActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJLovActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJMenuActionProcessor;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class EJFormPackageRenameParticipant extends RenameParticipant implements ISharableParticipant
{

    protected IJavaProject                  fProject;
    protected HashMap<IJavaElement, String> fElements;

    public String getName()
    {
        return "Rename packages referenced in EntireJ forms.";
    }

    public void addElement(Object element, RefactoringArguments arguments)
    {
        if (element instanceof IJavaElement)
        {
            String newName = ((RenameArguments) arguments).getNewName();
            if (element instanceof IResource)
            {
                IPath projectPath = ((IResource) element).getProjectRelativePath();
                newName = projectPath.removeLastSegments(1).append(newName).toString();
            }
            fElements.put((IJavaElement) element, newName);
        }

    }

    boolean isRelated(IPackageFragment fragment) throws CoreException
    {

        IJavaElement[] children = fragment.getChildren();
        Class<?>[] intersting = new Class<?>[] { EJBlockActionProcessor.class, EJFormActionProcessor.class, EJLovActionProcessor.class,
                EJMenuActionProcessor.class, EJBlockService.class };
        for (IJavaElement iJavaElement : children)
        {

            if (iJavaElement instanceof ICompilationUnit)
            {
                IType[] allTypes = ((ICompilationUnit) iJavaElement).getAllTypes();

                if (allTypes.length > 0)
                    iJavaElement = allTypes[0];
            }
            if (iJavaElement instanceof IPackageFragment && isRelated((IPackageFragment) iJavaElement))
            {
                return true;
            }
            if (iJavaElement instanceof IType && (JavaAccessUtils.isSubTypeOfInterface((IType) iJavaElement, intersting)))
            {
                return true;
            }
        }

        return false;
    }

    @Override
    protected boolean initialize(Object element)
    {
        if (element instanceof IPackageFragment)
        {
            IPackageFragment fragment = (IPackageFragment) element;
            try
            {
                if (!fragment.containsJavaResources())
                    return false;
                if (!isRelated(fragment))
                    return false;
            }
            catch (CoreException e)
            {
                // ignore
                return false;
            }
            IJavaProject javaProject = (IJavaProject) fragment.getAncestor(IJavaElement.JAVA_PROJECT);
            IProject project = javaProject.getProject();
            if (EJProject.hasPluginNature(project))
            {
                fProject = javaProject;
                fElements = new HashMap<IJavaElement, String>();
                fElements.put(fragment, getArguments().getNewName());
                return true;
            }
        }
        return false;
    }

    public RefactoringStatus checkConditions(IProgressMonitor pm, CheckConditionsContext context) throws OperationCanceledException
    {
        return new RefactoringStatus();
    }

    @Override
    public Change createChange(IProgressMonitor pm) throws CoreException, OperationCanceledException
    {
        if (!getArguments().getUpdateReferences())
            return null;

        final IJavaElement[] elements = fElements.keySet().toArray(new IJavaElement[0]);
        final String[] newNames = getNewNames();

        IPackageFragmentRoot[] packageFragmentRoots = fProject.getPackageFragmentRoots();
        CompositeChange result = new CompositeChange(getName());
        for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
        {
            if (iPackageFragmentRoot.getResource() instanceof IContainer)
                handelFormsIn((IContainer) iPackageFragmentRoot.getResource(), elements, newNames, result);
        }

        return result;

    }

    private void handelFormsIn(IContainer container, final IJavaElement[] elements, final String[] newNames, CompositeChange result) throws CoreException
    {

        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer)
                handelFormsIn((IContainer) member, elements, newNames, result);
            else if (member instanceof IFile && isFormFile((IFile) member))
            {
                Change propChange = new Change()
                {
                    boolean createPropChange = false;

                    @Override
                    public Change perform(IProgressMonitor pm) throws CoreException
                    {

                        // update application.ejprop
                        if (fProject != null)
                        {
                            final EJPluginFormProperties formProperties = getFormProperties((IFile) member, fProject);
                            if (formProperties == null)
                                return null;
                            for (int i = 0; i < elements.length; i++)
                            {
                                IJavaElement element = elements[i];
                                String newName = newNames[i];

                                if (element instanceof IPackageFragment)
                                {
                                    String oldName = ((IPackageFragment) element).getElementName();
                                    // form Action Processor

                                    if (formProperties.getActionProcessorClassName() != null
                                            && formProperties.getActionProcessorClassName().startsWith(oldName))
                                    {
                                        createPropChange = true;
                                        formProperties.setActionProcessorClassName(formProperties.getActionProcessorClassName().replace(oldName, newName));
                                    }
                                    List<EJPluginBlockProperties> allBlockProperties = formProperties.getBlockContainer().getAllBlockProperties();
                                    for (EJPluginBlockProperties blockProp : allBlockProperties)
                                    {
                                        if (blockProp.isReferenceBlock())
                                            continue;
                                        // block Action Processor
                                        if (blockProp.getActionProcessorClassName() != null && blockProp.getActionProcessorClassName().startsWith(oldName))
                                        {
                                            createPropChange = true;
                                            blockProp.setActionProcessorClassName(blockProp.getActionProcessorClassName().replace(oldName, newName));
                                        }
                                        // block service
                                        if (blockProp.getServiceClassName() != null && blockProp.getServiceClassName().startsWith(oldName))
                                        {
                                            createPropChange = true;
                                            blockProp.setServiceClassName(blockProp.getServiceClassName().replace(oldName, newName));
                                        }
                                    }
                                    List<EJPluginLovDefinitionProperties> definitionProperties = formProperties.getLovDefinitionContainer()
                                            .getAllLovDefinitionProperties();
                                    for (EJPluginLovDefinitionProperties blockProp : definitionProperties)
                                    {
                                        if (blockProp.isReferenceBlock())
                                            continue;

                                        // block Action Processor
                                        if (blockProp.getActionProcessorClassName() != null && blockProp.getActionProcessorClassName().startsWith(oldName))
                                        {
                                            createPropChange = true;
                                            blockProp.setActionProcessorClassName(blockProp.getActionProcessorClassName().replace(oldName, newName));
                                        }
                                        // block service
                                        if (blockProp.getBlockProperties().getServiceClassName() != null
                                                && blockProp.getBlockProperties().getServiceClassName().startsWith(oldName))
                                        {
                                            createPropChange = true;
                                            blockProp.getBlockProperties().setServiceClassName(
                                                    blockProp.getBlockProperties().getServiceClassName().replace(oldName, newName));
                                        }
                                    }
                                }
                            }
                            if (createPropChange)
                            {

                                FormPropertiesWriter write = new FormPropertiesWriter();
                                write.saveForm(formProperties, (IFile) member, pm);
                            }
                        }
                        // do we need to support undo change as well ?
                        return null;
                    }

                    @Override
                    public RefactoringStatus isValid(IProgressMonitor pm) throws CoreException, OperationCanceledException
                    {
                        return new RefactoringStatus();
                    }

                    @Override
                    public void initializeValidationData(IProgressMonitor pm)
                    {
                        // ignore

                    }

                    @Override
                    public String getName()
                    {
                        return String.format("Rename packages referenced in EntireJ form : %s.", member.getName());
                    }

                    @Override
                    public Object getModifiedElement()
                    {

                        return createPropChange ? member : null;
                    }
                };
                result.add(propChange);
            }
        }
    }

    private boolean isFormFile(IFile file)
    {
        return EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(file.getFileExtension()) || isRefFormFile(file);
    }

    private boolean isRefFormFile(IFile file)
    {
        String fileExtension = file.getFileExtension();
        return EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension)
                || EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension);
    }

    EJPluginFormProperties getFormProperties(IFile file, IJavaProject project)
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
            formProperties = reader.readForm(new FormHandler(project, fileName), project,file, inStream);
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

    protected String[] getNewNames()
    {
        String[] result = new String[fElements.size()];
        Iterator<String> iter = fElements.values().iterator();
        for (int i = 0; i < fElements.size(); i++)
            result[i] = iter.next().toString();
        return result;
    }

}
