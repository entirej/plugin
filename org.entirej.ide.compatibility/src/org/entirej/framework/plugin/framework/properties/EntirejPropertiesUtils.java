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
package org.entirej.framework.plugin.framework.properties;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.reader.EntireJPropertiesReader;
import org.entirej.framework.plugin.framework.properties.reader.EntireJRendererReader;
import org.entirej.ide.core.project.EJProject;

public class EntirejPropertiesUtils
{
    private static final String ENTIREJ_PROPERTY_FILE_NAME = "application.ejprop";
    
    public static void varifyEntirejProperties(IJavaProject project) throws CoreException
    {
        IFile file = project.getProject().getFile("src/application.ejprop");
        if (file == null)
        {
            throw new CoreException(new Status(IStatus.ERROR, EntireJFrameworkPlugin.PLUGIN_ID, "Unable to load the " + ENTIREJ_PROPERTY_FILE_NAME
                    + " from project path"));
        }
    }
    
    public static EntirejPluginPropertiesEnterpriseEdition retrieveEntirejProperties(IJavaProject project) throws CoreException
    {
        if (project == null) return null;
        
        IFile file = project.getProject().getFile("src/application.ejprop");
        if (file == null)
        {
            throw new CoreException(new Status(IStatus.ERROR, EntireJFrameworkPlugin.PLUGIN_ID, "Unable to load the " + ENTIREJ_PROPERTY_FILE_NAME
                    + " from project path"));
        }
        try
        {
            EntirejPluginPropertiesEnterpriseEdition entirejProperties = new EntirejPluginPropertiesEnterpriseEdition(project);
            EntireJPropertiesReader.readProperties(entirejProperties,project, file.getContents(),file,EJProject.getRendererFile(project.getProject()));
            
            file = null;
            return entirejProperties;
        }
        catch (EJDevFrameworkException e)
        {
            throw new CoreException(new Status(IStatus.ERROR, EntireJFrameworkPlugin.PLUGIN_ID, e.getMessage(), e));
        }
        
    }
}
