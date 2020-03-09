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

import java.util.HashMap;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaProject;
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
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.editors.form.builder.EJFormConstBuilder;

public class EJFormConstantsRenameParticipant extends RenameParticipant implements ISharableParticipant
{

    protected IJavaProject           fProject;
    protected HashMap<IFile, String> fElements;

    public String getName()
    {
        return "Rename EJ Form Constants file";
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
            if (!EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension))
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
        CompositeChange result = new CompositeChange(getName());

        for (int i = 0; i < elements.length; i++)
        {
            final IFile element = elements[i];
            String fileExtension = element.getFileExtension();
            final String newName = newNames[i];

            if (EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX.equalsIgnoreCase(fileExtension))
            {

                // rename Constants

                String formId = EJFormConstBuilder.getFormId(element.getName().substring(0, element.getName().lastIndexOf(".")));
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

            }
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

}
