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
package org.entirej.ide.ui.editors.prop;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.interfaces.EJConnectionFactory;
import org.entirej.framework.core.interfaces.EJTranslator;
import org.entirej.framework.core.renderers.definitions.interfaces.EJAppComponentRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJBlockRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJFormRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJItemRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJLovRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.framework.properties.reader.EntireJPropertiesReader;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.core.spi.EJPropertiesValidateProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class EJPropertiesValidateImpl implements EJPropertiesValidateProvider
{

    public void validate(IFile file, IProgressMonitor monitor)
    {
        IProject _project = file.getProject();

        IJavaProject project = JavaCore.create(_project);

        InputStream inStream = null;
        try
        {

            try
            {
                inStream = file.getContents();
                EJPluginEntireJProperties entireJProperties = new EntirejPluginPropertiesEnterpriseEdition(project);
                EntireJPropertiesReader.readProperties(entireJProperties, project, inStream,file,EJProject.getRendererFile(_project));
                
                validateProperties(file, entireJProperties, project);
            }
            catch (Exception exception)
            {
                EJCoreLog.logException(exception);
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
        }
        catch (Exception exception)
        {
            EJCoreLog.logException(exception);
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
        monitor.done();
    }

    private void validateProperties(IFile file, EJPluginEntireJProperties entireJProperties, IJavaProject project)
    {
        // validate EJApplicationDefinition related problems
        addMarker(file, validateApplicationDef(file, entireJProperties, project));
        // validate EJConnectionFactory related problems
        addMarker(file, validateConnectionFactory(file, entireJProperties, project));
        // validate EJTranslator related problems
        addMarker(file, validateTranslator(file, entireJProperties, project));
        // validate form packages related problems
        addMarker(file, validateFormPackages(file, entireJProperties, project));
        // validate Reusable Block Location related problems
        addMarker(file, validateReusableBlockPath(file, entireJProperties, project));
        // validate Reusable Lov Definition Location related problems
        addMarker(file, validateReusableLovPath(file, entireJProperties, project));
        // validate object group Definition Location related problems
        addMarker(file, validateObjectGroupPath(file, entireJProperties, project));
        // validate application parameters
        validateApplicationParameters(file, entireJProperties, project);
        // validate Renderers related problems
        validateRenderers(file, entireJProperties, project);
    }

    Problem validateApplicationDef(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        String defClassName = prop.getApplicationManagerDefinitionClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Application Definition: [EJApplicationDefinition] must be specified.");
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s can't find in project build path.", defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJApplicationDefinition.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJApplicationDefinition.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }

        return null;
    }

    void validateApplicationParameters(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        Collection<EJPluginApplicationParameter> parameters = prop.getAllApplicationLevelParameters();
        for (EJPluginApplicationParameter parameter : parameters)
        {
            if (parameter.getName() == null || parameter.getName().length() == 0)
            {
                addMarker(file, new Problem(Problem.TYPE.ERROR, String.format("Application parameter name cannot be empty.")));
                continue;
            }

            try
            {
                IType findType = project.findType(parameter.getDataTypeName());
                if (findType == null)
                {
                    addMarker(
                            file,
                            new Problem(Problem.TYPE.ERROR, String.format("Application parameter %s data type: %s can't find in project build path.",
                                    parameter.getName(), parameter.getDataTypeName())));
                }

            }
            catch (CoreException e)
            {
                addMarker(file, new Problem(Problem.TYPE.ERROR, e.getMessage()));
            }
        }
    }

    Problem validateConnectionFactory(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        String defClassName = prop.getConnectionFactoryClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Connection Factory: [EJConnectionFactory] must be specified.");
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s can't find in project build path.", defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJConnectionFactory.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJConnectionFactory.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }

        return null;
    }

    Problem validateTranslator(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        String defClassName = prop.getTranslatorClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return null;
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s can't find in project build path.", defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJTranslator.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJTranslator.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }

        return null;
    }

    Problem validateFormPackages(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        Collection<String> packages = prop.getFormPackageNames();
        if (packages == null || packages.size() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, "Form packages are not specified.At least one Package should be specified");
        }

        return null;
    }

    void validateRenderers(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        validateRendererContainer(file, "Form", prop.getFormRendererContainer(), project);
        validateRendererContainer(file, "Block", prop.getBlockRendererContainer(), project);
        validateRendererContainer(file, "Component", prop.getAppComponentRendererContainer(), project);
        validateRendererContainer(file, "Item", prop.getItemRendererContainer(), project);
        validateRendererContainer(file, "LOV", prop.getLovRendererContainer(), project);

    }

    void validateRendererContainer(IFile file, String containerName, EJPluginAssignedRendererContainer container, IJavaProject project)
    {
        Collection<EJPluginRenderer> renderers = container.getAllRenderers();
        if (renderers == null || renderers.size() == 0)
        {
            addMarker(
                    file,
                    new Problem(Problem.TYPE.WARNING, String.format("%s renderers are not specified.At least one renderer should be specified.", containerName)));
        }

        for (EJPluginRenderer renderer : renderers)
        {
            String assignedName = renderer.getAssignedName();
            if (assignedName == null || assignedName.trim().length() == 0)
            {
                addMarker(file,
                        new Problem(Problem.TYPE.ERROR, String.format("%s Renderer Container: Renderer assigned name must be specified.", containerName)));

                return;
            }
            else
            {
                String defClassName = renderer.getRendererDefinitionClassName();
                if (defClassName == null || defClassName.trim().length() == 0)
                {
                    addMarker(file, new Problem(Problem.TYPE.ERROR, String.format("%s Renderer: Renderer definition must be specified.", assignedName)));
                    return;
                }

                Class<?> defClass;
                switch (renderer.getRendererType())
                {
                    case FORM:
                        defClass = (EJFormRendererDefinition.class);
                        break;
                    case BLOCK:
                        defClass = (EJBlockRendererDefinition.class);
                        break;
                    case ITEM:
                        defClass = (EJItemRendererDefinition.class);
                        break;
                    case LOV:
                        defClass = (EJLovRendererDefinition.class);
                        break;
                    case APP_COMPONENT:
                        defClass = (EJAppComponentRendererDefinition.class);
                        break;
                    default:
                        defClass = (EJRendererDefinition.class);
                        break;
                }

                Class<?> rendererDefinitionClass = null;
                try
                {
                    rendererDefinitionClass = EJPluginEntireJClassLoader.loadClass(project, defClassName);
                    if (!defClass.isAssignableFrom(rendererDefinitionClass))
                    {
                        addMarker(
                                file,
                                new Problem(Problem.TYPE.ERROR, String.format("%s Renderer: %s is not a sub type of %s.", assignedName, defClassName,
                                        defClass.getName())));
                        return;
                    }

                }
                catch (ClassNotFoundException e)
                {
                    addMarker(file,
                            new Problem(Problem.TYPE.ERROR, String.format("%s Renderer: %s can't find in project build path.", assignedName, defClassName)));

                }
                catch (NoClassDefFoundError e)
                {
                    addMarker(file,
                            new Problem(Problem.TYPE.ERROR, String.format("%s Renderer: %s can't find in project build path.", assignedName, defClassName)));
                    
                }

                if (renderer.getRendererClassName() == null || renderer.getRendererClassName().trim().length() == 0)
                {
                    addMarker(
                            file,
                            new Problem(Problem.TYPE.ERROR, String.format("%s Renderer: Renderer class name is missing, Check renderer definition.",
                                    assignedName)));
                    return;
                }
                else
                {
                   
                    try
                    {
                        EJPluginEntireJClassLoader.loadClass(project, renderer.getRendererClassName());
                        

                    }
                    catch (ClassNotFoundException e)
                    {
                        addMarker(file,
                                new Problem(Problem.TYPE.ERROR, String.format("%s Renderer: %s can't find in project build path.", assignedName, renderer.getRendererClassName())));

                    }
                    catch (NoClassDefFoundError e)
                    {
                        addMarker(file,
                                new Problem(Problem.TYPE.ERROR, String.format("%s Renderer: %s can't find in project build path.", assignedName, renderer.getRendererClassName())));
                        
                    }
                }

            }
        }
    }

    Problem validateReusableBlockPath(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        String refLocation = prop.getReusableBlocksLocation();
        if (refLocation == null || refLocation.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, "If no Referenced Block Location is specified then it will not be possible to generate Reusable Blocks.");
        }

        return null;
    }
    Problem validateObjectGroupPath(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        String refLocation = prop.getObjectGroupDefinitionLocation();
        if (refLocation == null || refLocation.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, "If no ObjectGroup Location is specified then it will not be possible to generate ObjectGroups.");
        }
        
        return null;
    }

    Problem validateReusableLovPath(IFile file, EJPluginEntireJProperties prop, IJavaProject project)
    {
        String refLocation = prop.getReusableLovDefinitionLocation();
        if (refLocation == null || refLocation.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING,
                    "If no Referenced Lov Definition Location is specified then it will not be possible to generate Reusable Lov Devfinitions.");
        }

        return null;
    }

    void addMarker(IFile file, Problem p)
    {
        if (p != null)
        {
            try
            {
                IMarker marker = EJMarkerFactory.createMarker(file);
                switch (p.type)
                {
                    case ERROR:
                        EJMarkerFactory.addErrorMessage(marker, p.message);
                        break;
                    case INFO:
                        EJMarkerFactory.addInfoMessage(marker, p.message);
                        break;
                    case WARNING:
                        EJMarkerFactory.addWarningMessage(marker, p.message);
                        break;
                }
            }
            catch (CoreException e)
            {
                EJCoreLog.logException(e);
            }
        }
    }

    private static class Problem
    {
        enum TYPE
        {
            INFO, ERROR, WARNING
        }

        final TYPE   type;
        final String message;

        Problem(TYPE type, String message)
        {
            super();
            this.type = type;
            this.message = message;
        }

    }

}
