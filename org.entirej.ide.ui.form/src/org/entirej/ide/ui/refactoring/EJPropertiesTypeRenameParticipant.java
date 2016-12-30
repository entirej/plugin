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
package org.entirej.ide.ui.refactoring;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.interfaces.EJConnectionFactory;
import org.entirej.framework.core.interfaces.EJTranslator;
import org.entirej.framework.core.renderers.definitions.interfaces.EJRendererDefinition;
import org.entirej.framework.core.renderers.interfaces.EJRenderer;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class EJPropertiesTypeRenameParticipant extends RenameParticipant implements ISharableParticipant
{

    protected IJavaProject           fProject;
    protected HashMap<IType, String> fElements;

    public String getName()
    {
        return "Rename classes referenced in application.ejprop";
    }

    public void addElement(Object element, RefactoringArguments arguments)
    {
        if (element instanceof IType)
        {
            String newName = ((RenameArguments) arguments).getNewName();
            if (element instanceof IResource)
            {
                IPath projectPath = ((IResource) element).getProjectRelativePath();
                newName = projectPath.removeLastSegments(1).append(newName).toString();
            }
            fElements.put((IType) element, newName);
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

                Class<?>[] intersting = new Class<?>[] { EJConnectionFactory.class, EJTranslator.class, EJApplicationDefinition.class,
                        EJRendererDefinition.class, EJRenderer.class };

                try
                {
                    if (JavaAccessUtils.isSubTypeOfInterface(type, intersting))
                    {
                        fElements = new HashMap<IType, String>();
                        fElements.put(type, getArguments().getNewName());
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
        if (!getArguments().getUpdateReferences())
            return null;

        final IJavaElement[] elements = fElements.keySet().toArray(new IJavaElement[0]);
        final String[] newNames = getNewNames();

        final IFile propertiesFile = EJProject.getPropertiesFile(fProject.getProject());
        Change propChange = new Change()
        {
            boolean createPropChange = false;

            @Override
            public Change perform(IProgressMonitor pm) throws CoreException
            {

                // update application.ejprop
                if (fProject != null)
                {
                    final EJPluginEntireJProperties entirejProperties = EntirejPropertiesUtils.retrieveEntirejProperties(fProject);

                    for (int i = 0; i < elements.length; i++)
                    {
                        IJavaElement element = elements[i];
                        String newName = newNames[i];
                        if (element instanceof IType)
                        {
                            IType type = (IType) element;

                            String oldName = type.getFullyQualifiedName('$');

                            if (oldName.equals(entirejProperties.getApplicationManagerDefinitionClassName()))
                            {
                                createPropChange = true;
                                entirejProperties.setApplicationManagerDefinitionClassName(newName);
                            }
                            if (oldName.equals(entirejProperties.getConnectionFactoryClassName()))
                            {
                                createPropChange = true;
                                entirejProperties.setConnectionFactoryClassName(newName);
                            }
                            if (oldName.equals(entirejProperties.getTranslatorClassName()))
                            {
                                createPropChange = true;
                                entirejProperties.setTranslatorClassName(newName);
                            }

                            List<EJPluginRenderer> allPluginRenderers = entirejProperties.getAllPluginRenderers();
                            for (EJPluginRenderer renderer : allPluginRenderers)
                            {
                                if (oldName.equals(renderer.getRendererDefinitionClassName()))
                                {
                                    createPropChange = true;
                                    renderer.internalSetRendererDefinitionClassName(newName);
                                }
                                else if (oldName.equals(renderer.getRendererClassName()))
                                {
                                    createPropChange = true;
                                    renderer.internalSetRendererClassName(newName);
                                }
                            }

                        }
                    }
                    if (createPropChange)
                    {
                        EntireJPropertiesWriter saver = new EntireJPropertiesWriter();
                        saver.saveEntireJProperitesFile(entirejProperties, propertiesFile, pm);
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
                return "Rename classes referenced in application.ejprop";
            }

            @Override
            public Object getModifiedElement()
            {

                return createPropChange ? propertiesFile : null;
            }
        };
        return propChange;

    }

    protected String[] getNewNames()
    {
        String[] result = new String[fElements.size()];
        Iterator<IType> iter = fElements.keySet().iterator();
        for (int i = 0; i < fElements.size(); i++)
        {
            IType type = iter.next();
            String oldName = type.getFullyQualifiedName('$');
            int index = oldName.lastIndexOf(type.getElementName());
            StringBuffer buffer = new StringBuffer(oldName.substring(0, index));
            buffer.append(fElements.get(type));
            result[i] = buffer.toString();
        }
        return result;
    }

}
