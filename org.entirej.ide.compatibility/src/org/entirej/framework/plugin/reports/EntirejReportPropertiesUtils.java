/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.reports;

import java.io.ByteArrayInputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.reports.reader.EntireJReportPropertiesReader;
import org.entirej.framework.plugin.reports.writer.EntireJReportPropertiesWriter;


public class EntirejReportPropertiesUtils
{
    private static final String ENTIREJ_PROPERTY_FILE_NAME = "src/report.ejprop";
    
    public static void varifyEntirejProperties(IJavaProject project) throws CoreException
    {
        IFile file = project.getProject().getFile(ENTIREJ_PROPERTY_FILE_NAME);
        if (!file.exists())
        {
            createDefaultFile(project);
        }
    }
    
    public static EJPluginEntireJReportProperties retrieveEntirejProperties(IJavaProject project) throws CoreException
    {
        if (project == null) return null;
        
        IFile file = project.getProject().getFile(ENTIREJ_PROPERTY_FILE_NAME);
        if (!file.exists())
        {
            createDefaultFile(project);
        }
        try
        {
            EJPluginEntireJReportProperties entirejProperties = new EJPluginEntireJReportProperties(project);
            EntireJReportPropertiesReader.readProperties(entirejProperties, project, file.getContents(), file);
            
            file = null;
            return entirejProperties;
        }
        catch (EJDevFrameworkException e)
        {
            throw new CoreException(new Status(IStatus.ERROR, EntireJFrameworkPlugin.PLUGIN_ID, e.getMessage(), e));
        }
        
    }
    
    static IFile createDefaultFile(IJavaProject project) throws CoreException
    {
        IFile file = project.getProject().getFile(ENTIREJ_PROPERTY_FILE_NAME);
        file.create(new ByteArrayInputStream(new byte[0]), true, new NullProgressMonitor());
        EJPluginEntireJReportProperties entirejProperties = new EJPluginEntireJReportProperties(project);
        new EntireJReportPropertiesWriter().saveEntireJProperitesFile(entirejProperties, file, new NullProgressMonitor());
        
        return file;
    }
}
