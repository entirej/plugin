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

import java.util.Collection;
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
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.ltk.core.refactoring.Change;
import org.eclipse.ltk.core.refactoring.RefactoringStatus;
import org.eclipse.ltk.core.refactoring.participants.CheckConditionsContext;
import org.eclipse.ltk.core.refactoring.participants.ISharableParticipant;
import org.eclipse.ltk.core.refactoring.participants.RefactoringArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameArguments;
import org.eclipse.ltk.core.refactoring.participants.RenameParticipant;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.entirej.ide.core.project.EJProject;

public class EJPropertiesPackageRenameParticipant extends RenameParticipant implements ISharableParticipant
{

    protected IJavaProject                  fProject;
    protected HashMap<IJavaElement, String> fElements;

    public String getName()
    {
        return "Rename packages referenced in application.ejprop";
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

    @Override
    protected boolean initialize(Object element)
    {
        if (element instanceof IPackageFragment)
        {
            IPackageFragment fragment = (IPackageFragment) element;
            // if (!fragment.containsJavaResources())
            // return false;
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
                        String newText = newNames[i];

                        if (element instanceof IPackageFragment)
                        {
                            String oldPkg = ((IPackageFragment) element).getElementName();
                            Collection<String> formPackageNames = entirejProperties.getFormPackageNames();
                            String path = packageToPath(oldPkg);
                            String newPkgpath = packageToPath(newText);
                            if (formPackageNames.contains(path))
                            {
                                createPropChange = true;
                                formPackageNames.remove(path);

                                if (!formPackageNames.contains(newPkgpath))
                                    formPackageNames.add(newPkgpath);
                            }

                            // validate ref lov/block paths
                            if (path.equals(entirejProperties.getReusableLovDefinitionLocation()))
                            {
                                entirejProperties.setReusableLovDefinitionLocation(newPkgpath);
                                createPropChange = true;
                            }
                            if (path.equals(entirejProperties.getReusableBlocksLocation()))
                            {
                                entirejProperties.setReusableBlocksLocation(newPkgpath);
                                createPropChange = true;
                            }

                            // package path reset

                            if (entirejProperties.getApplicationManagerDefinitionClassName() != null
                                    && entirejProperties.getApplicationManagerDefinitionClassName().startsWith(oldPkg))
                            {
                                createPropChange = true;
                                entirejProperties.setApplicationManagerDefinitionClassName(entirejProperties.getApplicationManagerDefinitionClassName()
                                        .replace(oldPkg, newText));
                            }
                            if (entirejProperties.getConnectionFactoryClassName() != null
                                    && entirejProperties.getConnectionFactoryClassName().startsWith(oldPkg))
                            {
                                createPropChange = true;
                                entirejProperties.setConnectionFactoryClassName(entirejProperties.getConnectionFactoryClassName().replace(oldPkg, newText));
                            }
                            if (entirejProperties.getApplicationActionProcessorClassName() != null
                                    && entirejProperties.getApplicationActionProcessorClassName().startsWith(oldPkg))
                            {
                                createPropChange = true;
                                entirejProperties.setApplicationActionProcessorClassName(entirejProperties.getApplicationActionProcessorClassName().replace(oldPkg, newText));
                            }
                            if (entirejProperties.getTranslatorClassName() != null && entirejProperties.getTranslatorClassName().startsWith(oldPkg))
                            {
                                createPropChange = true;
                                entirejProperties.setTranslatorClassName(entirejProperties.getTranslatorClassName().replace(oldPkg, newText));
                            }

                            List<EJPluginRenderer> allPluginRenderers = entirejProperties.getAllPluginRenderers();
                            for (EJPluginRenderer renderer : allPluginRenderers)
                            {
                                if ((renderer.getRendererDefinitionClassName()).startsWith(oldPkg))
                                {
                                    createPropChange = true;
                                    renderer.internalSetRendererDefinitionClassName(renderer.getRendererDefinitionClassName().replace(oldPkg, newText));
                                }
                                else if ((renderer.getRendererClassName()).startsWith(oldPkg))
                                {
                                    createPropChange = true;
                                    renderer.internalSetRendererClassName(renderer.getRendererClassName().replace(oldPkg, newText));
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
                return "Rename packages referenced in application.ejprop";
            }

            @Override
            public Object getModifiedElement()
            {

                return createPropChange ? propertiesFile : null;
            }
        };
        return propChange;

    }

    protected String packageToPath(String pkg)
    {
        if (pkg != null)
        {
            return pkg.replaceAll("\\.", "/");
        }
        return pkg;
    }

    protected String pathToPackage(String path)
    {
        if (path != null)
        {
            return path.replaceAll("/", ".");
        }
        return path;
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
