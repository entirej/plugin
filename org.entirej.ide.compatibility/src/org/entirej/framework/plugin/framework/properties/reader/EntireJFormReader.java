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
package org.entirej.framework.plugin.framework.properties.reader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EJPluginConstants;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.ide.core.EJCoreLog;

public class EntireJFormReader
{
    public EJPluginFormProperties readForm(FormHandler formHandler, IJavaProject project,IFile file, InputStream inStream) throws EJDevFrameworkException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxPArser = factory.newSAXParser();
            saxPArser.parse(inStream, formHandler);
            EJPluginFormProperties formProperties = formHandler.getFormProperties();
            String fileExtension = file.getFileExtension();
            if((fileExtension.equals(EJPluginConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX) ||fileExtension.equals(EJPluginConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX)) &&
                    !formProperties.getName().equals(formProperties.getBlockContainer().getAllBlockProperties().get(0).getName()))
            {
                formProperties.getBlockContainer().getAllBlockProperties().get(0).internalSetName(formProperties.getName());
                FormPropertiesWriter formPropertiesWriter = new FormPropertiesWriter(); 
                formPropertiesWriter.saveForm(formProperties, file, new NullProgressMonitor());
            }
           
           
            
            return formProperties;
        }
        
        catch (Throwable e)
        {
            EJCoreLog.log(e);
            throw new EJDevFrameworkException(e.getMessage());
        }
    }
    
    public static String readFormName(IFile file, boolean usemeta)
    {
        InputStream inStream = null;
        try
        {
            
            try
            {
                inStream = file.getContents();
                
            }
            catch (CoreException e)
            {
                file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
                if (file.exists())
                {
                    inStream = file.getContents();
                }
                else
                {
                    return null;
                }
            }
            
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxPArser = factory.newSAXParser();
            FormNameHandler handler = new FormNameHandler();
            saxPArser.parse(inStream, handler);
            return usemeta ? handler.getDefaultFormName() : handler.getFormName();
            
        }
        catch (Exception exception)
        {
            
            EJCoreLog.logException(exception);
            
        }
        finally
        {
            
            try
            {
                if (inStream != null) inStream.close();
            }
            catch (IOException e)
            {
                EJCoreLog.logException(e);
            }
        }
        return null;
    }
}
