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
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportProperties;
import org.entirej.framework.plugin.reports.reader.EntireJReportPropertiesReader;
import org.entirej.framework.report.interfaces.EJReportConnectionFactory;
import org.entirej.framework.report.interfaces.EJReportRunner;
import org.entirej.framework.report.interfaces.EJReportTranslator;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.core.spi.EJReportPropertiesValidateProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class EJReportPropertiesValidateImpl implements EJReportPropertiesValidateProvider
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
                EJPluginEntireJReportProperties entireJProperties = new EJPluginEntireJReportProperties(project);
                EntireJReportPropertiesReader.readProperties(entireJProperties, project, inStream, file);

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

    private void validateProperties(IFile file, EJPluginEntireJReportProperties entireJProperties, IJavaProject project)
    {
        // validate EJReportRunner related problems
        addMarker(file, validateReportRunnerDef(file, entireJProperties, project));
        // validate EJConnectionFactory related problems
        addMarker(file, validateConnectionFactory(file, entireJProperties, project));
        // validate EJTranslator related problems
        addMarker(file, validateTranslator(file, entireJProperties, project));
        // validate form packages related problems
        addMarker(file, validateReportPackages(file, entireJProperties, project));

        // validate application parameters
        validateApplicationParameters(file, entireJProperties, project);
    }

    Problem validateReportRunnerDef(IFile file, EJPluginEntireJReportProperties prop, IJavaProject project)
    {
        String defClassName = prop.getReportRunnerClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Report Runner: [EJReportRunner] must be specified.");
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
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJReportRunner.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }

        return null;
    }

    void validateApplicationParameters(IFile file, EJPluginEntireJReportProperties prop, IJavaProject project)
    {
        Collection<EJPluginApplicationParameter> parameters = prop.getAllApplicationLevelParameters();
        for (EJPluginApplicationParameter parameter : parameters)
        {
            if (parameter.getName() == null || parameter.getName().length() == 0)
            {
                addMarker(file, new Problem(Problem.TYPE.ERROR, String.format("Report parameter name cannot be empty.")));
                continue;
            }

            try
            {
                IType findType = project.findType(parameter.getDataTypeName());
                if (findType == null)
                {
                    addMarker(
                            file,
                            new Problem(Problem.TYPE.ERROR, String.format("Report parameter %s data type: %s can't find in project build path.",
                                    parameter.getName(), parameter.getDataTypeName())));
                }

            }
            catch (CoreException e)
            {
                addMarker(file, new Problem(Problem.TYPE.ERROR, e.getMessage()));
            }
        }
    }

    Problem validateConnectionFactory(IFile file, EJPluginEntireJReportProperties prop, IJavaProject project)
    {
        String defClassName = prop.getConnectionFactoryClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Connection Factory: [EJReportConnectionFactory] must be specified.");
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
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJReportConnectionFactory.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }

        return null;
    }

    Problem validateTranslator(IFile file, EJPluginEntireJReportProperties prop, IJavaProject project)
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
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJReportTranslator.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }

        return null;
    }

    Problem validateReportPackages(IFile file, EJPluginEntireJReportProperties prop, IJavaProject project)
    {
        Collection<String> packages = prop.getReportPackageNames();
        if (packages == null || packages.size() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, "Report packages are not specified.At least one Package should be specified");
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
