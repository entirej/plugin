/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.report.interfaces.EJEntireJReportProperties;
import org.entirej.framework.report.properties.EJCoreReportVisualAttributeProperties;
import org.entirej.framework.report.properties.EJReportVisualAttributeContainer;
import org.entirej.framework.report.properties.EJReportVisualAttributeProperties;

public class EJPluginEntireJReportProperties implements EJEntireJReportProperties
{
    
    private String                             _version = "1.0";           // default
                                                                            
    private IJavaProject                       _javaProject;
    private ArrayList<String>                  _reportPackageNames;
    private String                             _connectionFactoryClassName;
    private String                             _translatorClassName;
    private String                             _reportRunnerClassName= "org.entirej.report.jasper.EJJasperReportRunner";
    private EJReportVisualAttributeContainer   _visualAttributeContainer;
    
    private List<EJPluginApplicationParameter> _applicationLevelParameters;
    
    public EJPluginEntireJReportProperties(IJavaProject javaProject)
    {
        _javaProject = javaProject;
        _reportPackageNames = new ArrayList<String>();
        _visualAttributeContainer = new EJReportVisualAttributeContainer(new ArrayList<EJCoreReportVisualAttributeProperties>());
        
        _applicationLevelParameters = new ArrayList<EJPluginApplicationParameter>();
    }
    
    public void clear()
    {
        
        _javaProject = null;
    }
    
    public String getVersion()
    {
        return _version;
    }
    
    public void setVersion(String version)
    {
        _version = version;
    }
    
    public String getConnectionFactoryClassName()
    {
        return _connectionFactoryClassName;
    }
    
    public void setConnectionFactoryClassName(String className)
    {
        _connectionFactoryClassName = className;
        
        if (className == null || className.trim().length() == 0)
        {
            _connectionFactoryClassName = null;
        }
    }
    
    public String getTranslatorClassName()
    {
        return _translatorClassName;
    }
    
    public void setTranslatorClassName(String className)
    {
        _translatorClassName = className;
        
        if (className == null || className.trim().length() == 0)
        {
            _translatorClassName = null;
        }
    }
    
    public String getReportRunnerClassName()
    {
        return _reportRunnerClassName;
    }
    
    public void setReportRunnerClassName(String reportRunnerClassName)
    {
        _reportRunnerClassName = reportRunnerClassName;
        if (reportRunnerClassName == null || reportRunnerClassName.trim().length() == 0)
        {
            _reportRunnerClassName = null;
        }
    }
    
    /**
     * Used to log an information message to the workbench log file
     * 
     * @param message
     *            The message to log
     */
    public void logInfoMessage(String message)
    {
        EntireJFrameworkPlugin.logInfo(message);
    }
    
    /**
     * Used to log an error message to the workbench log file
     * 
     * @param message
     *            The message to log
     * @param ex
     *            The exception that caused the error or <code>null</code> if
     *            none is available
     */
    public void logErrorMessage(String message, Exception ex)
    {
        EntireJFrameworkPlugin.logError(message, ex);
    }
    
    public EJReportVisualAttributeContainer getVisualAttributesContainer()
    {
        return _visualAttributeContainer;
    }
    
    /**
     * Retrieve a <code>Collection</code> containing all package names where
     * report definition files can be found
     * 
     * @return A <code>Collection</code> containing report package names
     */
    public Collection<String> getReportPackageNames()
    {
        return _reportPackageNames;
    }
    
    /**
     * Adds a given package name to the list of report package names
     * 
     * @param packageName
     *            The package name to add
     * @throws NullPointerException
     *             if the package name passed is either null or of zero length
     */
    public void addReportPackageName(String packageName)
    {
        if (packageName == null || packageName.trim().length() == 0)
        {
            throw new NullPointerException("The package name passed to addReportPackageName is either null or of zero length");
        }
        _reportPackageNames.add(packageName);
    }
    
    /**
     * Checks this properties object and indicates if it is valid
     * <p>
     * An Invalid property file could be for example that it has renderers
     * defined that do not exist. If this is the case, then any form based upon
     * these properties will not run correctly and should therefore not be
     * loaded
     * 
     * @return
     */
    public boolean isValid()
    {
        
        return true;
    }
    
    private List<String> loadFileNames(String path, String fileSuffix) throws EJDevFrameworkException
    {
        ArrayList<String> fileNames = new ArrayList<String>();
        try
        {
            IJavaElement element = _javaProject.findElement(new Path(path));
            
            if (element == null)
            {
                return null;
            }
            
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            {
                IPackageFragment packageFragment = (IPackageFragment) element;
                
                Object[] obs = packageFragment.getNonJavaResources();
                for (Object object : obs)
                {
                    if (object instanceof IFile)
                    {
                        IFile file = (IFile) object;
                        if (file.getFileExtension().equals(fileSuffix))
                        {
                            String fileName = file.getName();
                            fileName = fileName.substring(0, fileName.indexOf("." + fileSuffix));
                            fileNames.add(fileName);
                        }
                    }
                }
                
            }
            return fileNames;
        }
        catch (JavaModelException e)
        {
            throw new EJDevFrameworkException(e.getMessage());
        }
    }
    
    private boolean hasFileName(String path, String name, String fileSuffix) throws EJDevFrameworkException
    {
        
        try
        {
            IJavaElement element = _javaProject.findElement(new Path(path));
            
            if (element == null)
            {
                return false;
            }
            
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            {
                IPackageFragment packageFragment = (IPackageFragment) element;
                
                Object[] obs = packageFragment.getNonJavaResources();
                for (Object object : obs)
                {
                    if (object instanceof IFile)
                    {
                        IFile file = (IFile) object;
                        if (file.getFileExtension().equals(fileSuffix))
                        {
                            String fileName = file.getName();
                            fileName = fileName.substring(0, fileName.indexOf("." + fileSuffix));
                            if (fileName.equals(name)) return true;
                        }
                    }
                }
                
            }
            
        }
        catch (JavaModelException e)
        {
            throw new EJDevFrameworkException(e.getMessage());
        }
        return false;
    }
    
    private IFile loadFile(String name, String location, String fileSuffix) throws EJDevFrameworkException
    {
        if (location == null || location.trim().length() == 0)
        {
            return null;
        }
        
        try
        {
            IJavaElement element = getJavaProject().findElement(new Path(location));
            
            if (element == null)
            {
                return null;
            }
            
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            {
                IPackageFragment packageFragment = (IPackageFragment) element;
                
                Object[] obs = packageFragment.getNonJavaResources();
                for (Object object : obs)
                {
                    if (object instanceof IFile)
                    {
                        IFile file = (IFile) object;
                        if (file.getFileExtension().equals(fileSuffix))
                        {
                            String fileName = file.getName();
                            fileName = fileName.substring(0, fileName.indexOf("." + fileSuffix));
                            if (fileName.equals(name))
                            {
                                return file;
                            }
                        }
                    }
                }
                
            }
            
            return null;
        }
        catch (JavaModelException e)
        {
            throw new EJDevFrameworkException(e.getMessage());
        }
    }
    
    public IJavaProject getJavaProject()
    {
        return _javaProject;
    }
    
    public Collection<EJPluginApplicationParameter> getAllApplicationLevelParameters()
    {
        return _applicationLevelParameters;
    }
    
    public void addApplicationLevelParameter(EJPluginApplicationParameter parameter)
    {
        if (parameter != null)
        {
            _applicationLevelParameters.add(parameter);
        }
    }
    
    public EJPluginApplicationParameter getApplicationLevelParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _applicationLevelParameters)
        {
            if (parameter.getName().equals(name))
            {
                return parameter;
            }
        }
        return null;
    }
    
    public void removeApplicationLevelParameter(EJPluginApplicationParameter parameter)
    {
        _applicationLevelParameters.remove(parameter);
    }
    
    public boolean containsApplicationLevelParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _applicationLevelParameters)
        {
            if (parameter.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    
}
