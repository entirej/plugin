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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.CompositeChange;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.DeleteParticipant;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.entirej.framework.core.actionprocessor.EJDefaultFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJBlockActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJLovActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJMenuActionProcessor;
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

public class EJFormTypeDeleteParticipant extends DeleteParticipant implements ISharableParticipant
{

    protected IJavaProject fProject;
    protected List<IType>  fElements;

    public String getName()
    {
        return "Remove deleted classes referenced in EntireJ forms.";
    }

    public void addElement(Object element, RefactoringArguments arguments)
    {
        if (element instanceof IType)
        {

            fElements.add((IType) element);
        }

    }

    @Override
    protected boolean initialize(Object element)
    {
        if (element instanceof IType)
        {
            IType type = (IType) element;
            IJavaProject javaProject = (IJavaProject) type.getAncestor(IJavaElement.JAVA_PROJECT);
            IProject project = javaProject.getProject();
            if (EJProject.hasPluginNature(project))
            {
                fProject = javaProject;

                Class<?>[] intersting = new Class<?>[] { EJBlockActionProcessor.class, EJFormActionProcessor.class, EJLovActionProcessor.class,
                        EJMenuActionProcessor.class,/* EJBlockService.class */};

                try
                {
                    if (JavaAccessUtils.isSubTypeOfInterface(type, intersting))
                    {
                        fElements = new ArrayList<IType>();
                        fElements.add(type);
                        return true;
                    }
                }
                catch (CoreException e)
                {
                    // ignore
                }
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

        final IJavaElement[] elements = fElements.toArray(new IJavaElement[0]);

        IPackageFragmentRoot[] packageFragmentRoots = fProject.getPackageFragmentRoots();
        CompositeChange result = new CompositeChange(getName());
        for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
        {
            if (iPackageFragmentRoot.getResource() instanceof IContainer)
                handelFormsIn((IContainer) iPackageFragmentRoot.getResource(), elements, result);
        }

        return result;

    }

    private void handelFormsIn(IContainer container, final IJavaElement[] elements, CompositeChange result) throws CoreException
    {

        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            final IResource member = members[i];
            if (member instanceof IContainer)
                handelFormsIn((IContainer) member, elements, result);
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

                                if (element instanceof IType)
                                {
                                    IType type = (IType) element;

                                    String oldName = type.getFullyQualifiedName('$');
                                    // form Action Processor
                                    if (oldName.equals(formProperties.getActionProcessorClassName()))
                                    {
                                        createPropChange = true;
                                        formProperties.setActionProcessorClassName(EJDefaultFormActionProcessor.class.getName());
                                    }
                                    List<EJPluginBlockProperties> allBlockProperties = formProperties.getBlockContainer().getAllBlockProperties();
                                    for (EJPluginBlockProperties blockProp : allBlockProperties)
                                    {
                                        if (blockProp.isReferenceBlock())
                                            continue;
                                        // block Action Processor
                                        if (oldName.equals(blockProp.getActionProcessorClassName()))
                                        {
                                            createPropChange = true;
                                            blockProp.setActionProcessorClassName("");
                                        }
                                        // //block service
                                        // if
                                        // (oldName.equals(blockProp.getServiceClassName()))
                                        // {
                                        // createPropChange = true;
                                        // blockProp.setServiceClassName("");
                                        // }
                                    }
                                    List<EJPluginLovDefinitionProperties> definitionProperties = formProperties.getLovDefinitionContainer()
                                            .getAllLovDefinitionProperties();
                                    for (EJPluginLovDefinitionProperties blockProp : definitionProperties)
                                    {
                                        if (blockProp.isReferenceBlock())
                                            continue;

                                        if (oldName.equals(blockProp.getActionProcessorClassName()))
                                        {
                                            createPropChange = true;
                                            blockProp.setActionProcessorClassName("");
                                        }
                                        // //block service
                                        // if
                                        // (oldName.equals(blockProp.getBlockProperties().getServiceClassName()))
                                        // {
                                        // createPropChange = true;
                                        // blockProp.getBlockProperties().setServiceClassName("");
                                        // }
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
                        return String.format("Remove deleted classes referenced in EntireJ form : %s.", member.getName());
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

}
