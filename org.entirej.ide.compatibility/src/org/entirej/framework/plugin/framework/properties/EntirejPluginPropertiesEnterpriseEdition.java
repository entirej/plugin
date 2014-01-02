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

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.ide.core.EJCoreLog;

public class EntirejPluginPropertiesEnterpriseEdition extends EJPluginEntireJProperties
{
    /**
     * 
     */
    private static final long serialVersionUID = 2883801678104569690L;

    public EntirejPluginPropertiesEnterpriseEdition(IJavaProject javaProject)
    {
        super(javaProject);
    }
    
    public EJPluginReusableBlockProperties loadBlock(EJPluginEntireJProperties entirejProperties, IProject project, IFile file, String blockName)
            throws EJDevFrameworkException
    {
        
        InputStream inStream;
        try
        {
            inStream = file.getContents();
        }
        catch (CoreException e)
        {
            try
            {
                file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
            }
            catch (CoreException e1)
            {
                // ignore
            }
            if (file.exists())
            {
                try
                {
                    inStream = file.getContents();
                }
                catch (CoreException e1)
                {
                    EJCoreLog.logWarnning(e);
                    return null;
                }
            }
            else
            {
                return null;
            }
            
        }
        
        FormHandler formHandler = new FormHandler(getJavaProject(), blockName);
        
        EntireJFormReader reader = new EntireJFormReader();
        EJPluginFormProperties formProperties = reader.readForm(formHandler, getJavaProject(), inStream);
        
        EJPluginBlockProperties blockProperties = formProperties.getBlockContainer().getBlockProperties(blockName);
        if (blockProperties == null && !formProperties.getBlockContainer().isEmpty())
        {
            // load first block on it assuming EJPluginReusableBlockProperties
            // will always have one block
            blockProperties = formProperties.getBlockContainer().getAllBlockProperties().get(0);
        }
        EJPluginReusableBlockProperties reusableBlockProperties = new EJPluginReusableBlockProperties(blockProperties);
        reusableBlockProperties.setLovDefinitionContainer(formProperties.getLovDefinitionContainer());
        
        try
        {
            inStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        reader = null;
        formProperties = null;
        
        return reusableBlockProperties;
    }
    
    public EJPluginLovDefinitionProperties loadLovDefinition(EJPluginEntireJProperties entirejProperties, IProject project, IFile file, String definitionName)
            throws EJDevFrameworkException
    {
        
        InputStream inStream;
        try
        {
            inStream = file.getContents();
        }
        catch (CoreException e)
        {
            try
            {
                file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
            }
            catch (CoreException e1)
            {
                // ignore
            }
            if (file.exists())
            {
                try
                {
                    inStream = file.getContents();
                }
                catch (CoreException e1)
                {
                    EJCoreLog.logWarnning(e);
                    return null;
                }
            }
            else
            {
                return null;
            }
        }
        
        FormHandler formHandler = new FormHandler(getJavaProject(), definitionName);
        EntireJFormReader reader = new EntireJFormReader();
        EJPluginFormProperties formProperties = reader.readForm(formHandler, getJavaProject(), inStream);
        
        try
        {
            inStream.close();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        
        EJPluginLovDefinitionProperties props = formProperties.getLovDefinitionContainer().getLovDefinitionProperties(definitionName);
        if (props == null && !formProperties.getLovDefinitionContainer().isEmpty())
        {
            props = formProperties.getLovDefinitionContainer().getAllLovDefinitionProperties().get(0);
        }
        formProperties = null;
        
        return props;
    }
}
