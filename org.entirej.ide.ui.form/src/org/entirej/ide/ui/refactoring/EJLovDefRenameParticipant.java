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
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.refactoring.IJavaRefactorings;
import org.eclipse.jdt.core.refactoring.descriptors.RenameJavaElementDescriptor;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.Refactoring;
import org.eclipse.ltk.core.refactoring.RefactoringContribution;
import org.eclipse.ltk.core.refactoring.RefactoringCore;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.editors.form.builder.EJFormConstBuilder;

public class EJLovDefRenameParticipant extends RenameParticipant implements ISharableParticipant
{

    protected IJavaProject           fProject;
    protected HashMap<IFile, String> fElements;

    public String getName()
    {
        return "Rename EJ LOV definition usage in EJ forms";
    }

    public void addElement(Object element, RefactoringArguments arguments)
    {
        if (element instanceof IFile)
        {
            String newName = ((RenameArguments) arguments).getNewName();
            if (element instanceof IResource)
            {
                IPath projectPath = ((IResource) element).getProjectRelativePath();
                newName = projectPath.removeLastSegments(1).append(newName).toString();
            }
            fElements.put((IFile) element, newName);
        }

    }

    @Override
    protected boolean initialize(Object element)
    {
        if (element instanceof IFile)
        {
            IFile type = (IFile) element;
            String fileExtension = type.getFileExtension();
            if (!EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension))
                return false;

            IProject project = type.getProject();
            if (EJProject.hasPluginNature(project))
            {
                fProject = JavaCore.create(project);
                fElements = new HashMap<IFile, String>();
                fElements.put(type, getArguments().getNewName());
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
        if (!getArguments().getUpdateReferences() || fProject == null)
            return null;

        final IFile[] elements = fElements.keySet().toArray(new IFile[0]);
        final String[] newNames = getNewNames();
        IPackageFragmentRoot[] packageFragmentRoots = fProject.getPackageFragmentRoots();
        CompositeChange result = new CompositeChange(getName());

        for (int i = 0; i < elements.length; i++)
        {
            final IFile element = elements[i];
            String fileExtension = element.getFileExtension();
            final String newName = newNames[i];
            final IFile newelement = element.getParent().getFile(new Path(newName));
            final EJPluginFormProperties formProperties = getFormProperties(element, fProject);
            if (EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension))
            {

                // rename Constants

                String formId = EJFormConstBuilder.getFormId(formProperties.getFormName());
                IFile formConstantsSource = EJFormConstBuilder.getFormJavaSource(element, pm, formId);
                if (formConstantsSource.exists())
                {

                    String newNameref = EJFormConstBuilder.getFormId(newName.substring(0, newName.lastIndexOf(".")));

                    String id = IJavaRefactorings.RENAME_COMPILATION_UNIT;
                    RefactoringContribution contrib = RefactoringCore.getRefactoringContribution(id);
                    RenameJavaElementDescriptor desc = (RenameJavaElementDescriptor) contrib.createDescriptor();
                    desc.setUpdateReferences(true);
                    desc.setProject(formConstantsSource.getProject().getName());
                    desc.setJavaElement(JavaCore.create(formConstantsSource));
                    desc.setNewName(newNameref);
                    Refactoring refactoring = desc.createRefactoring(new RefactoringStatus());
                    refactoring.checkInitialConditions(pm);
                    refactoring.checkFinalConditions(pm);
                    result.add(refactoring.createChange(pm));
                }
                Change propChange = new Change()
                {

                    @Override
                    public Change perform(IProgressMonitor pm) throws CoreException
                    {

                        String oldName = element.getName();
                        oldName = oldName.substring(0, oldName.lastIndexOf("."));

                        List<EJPluginLovDefinitionProperties> definitionProperties = formProperties.getLovDefinitionContainer().getAllLovDefinitionProperties();
                        for (EJPluginLovDefinitionProperties blockProp : definitionProperties)
                        {

                            String newDef = newName.substring(0, newName.lastIndexOf("."));
                            blockProp.internalSetName(newDef);
                            blockProp.getBlockProperties().internalSetName(newDef);

                        }
                        FormPropertiesWriter write = new FormPropertiesWriter();
                        write.saveForm(formProperties, newelement, pm);

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
                        return String.format("Rename LOV definition name : %s.", element.getName());
                    }

                    @Override
                    public Object getModifiedElement()
                    {

                        return newelement;
                    }
                };
                result.add(propChange);

            }
        }

        for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
        {
            if (iPackageFragmentRoot.getResource() instanceof IContainer)
                handelFormsIn((IContainer) iPackageFragmentRoot.getResource(), elements, newNames, result);
        }

        return result;

    }

    protected String[] getNewNames()
    {
        String[] result = new String[fElements.size()];
        Iterator<IFile> iter = fElements.keySet().iterator();
        for (int i = 0; i < fElements.size(); i++)
        {
            IFile type = iter.next();

            result[i] = fElements.get(type);
        }
        return result;
    }

    private void handelFormsIn(IContainer container, final IFile[] elements, final String[] newNames, CompositeChange result) throws CoreException
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

                        if (fProject != null)
                        {
                            final EJPluginFormProperties formProperties = getFormProperties((IFile) member, fProject);
                            if (formProperties == null)
                                return null;
                            for (int i = 0; i < elements.length; i++)
                            {
                                IFile element = elements[i];
                                String fileExtension = element.getFileExtension();
                                String newName = newNames[i];
                                if (EJDevConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension))
                                {

                                    String oldName = element.getName();
                                    oldName = oldName.substring(0, oldName.lastIndexOf("."));

                                    List<EJPluginLovDefinitionProperties> definitionProperties = formProperties.getLovDefinitionContainer()
                                            .getAllLovDefinitionProperties();
                                    for (EJPluginLovDefinitionProperties blockProp : definitionProperties)
                                    {
                                        if (blockProp.getReferencedLovDefinitionName() != null && blockProp.getReferencedLovDefinitionName().equals(oldName))
                                        {
                                            createPropChange = true;
                                            String newDef = newName.substring(0, newName.lastIndexOf("."));
                                            blockProp.setReferencedLovDefinitionName(newDef);
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
                        return String.format("Rename LOV definition referenced in EntireJ form : %s.", member.getName());
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
        return EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension);
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
}
