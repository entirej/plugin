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
import java.util.List;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.xml.sax.SAXException;

public class EntireJPropertiesReader
{
    public static void readProperties(EJPluginEntireJProperties newProperties, IJavaProject project, InputStream inStream, IFile file,  IFile rfile)
            throws EJDevFrameworkException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxPArser = factory.newSAXParser();
            EntireJPropertiesHandler handler = new EntireJPropertiesHandler(newProperties);
            saxPArser.parse(inStream, handler);
            if (rfile != null && rfile.exists()) try
            {
                EntireJRendererReader.readProperties(newProperties, rfile.getContents());
            }
            catch (CoreException e1)
            {
                throw new EJDevFrameworkException(e1);
            }
            // try to refactor with auto fix
            if (newProperties.getVersion().equals("1.0"))
            {
                
                try
                {
                    EJRWTEJApplicationPropertiesFixV1 propertiesFix = new EJRWTEJApplicationPropertiesFixV1();
                    newProperties.setApplicationManagerDefinitionClassName(propertiesFix.fixRendererDefName(newProperties
                            .getApplicationManagerDefinitionClassName()));
                    List<EJPluginRenderer> allPluginRenderers = newProperties.getAllPluginRenderers();
                    for (EJPluginRenderer ejPluginRenderer : allPluginRenderers)
                    {
                        ejPluginRenderer.internalSetRendererClassName(propertiesFix.fixRendererName(ejPluginRenderer.getRendererClassName()));
                        ejPluginRenderer
                                .internalSetRendererDefinitionClassName(propertiesFix.fixRendererName(ejPluginRenderer.getRendererDefinitionClassName()));
                    }
                    newProperties.setVersion(propertiesFix.getUpdateVesion());
                    new EntireJPropertiesWriter().saveEntireJProperitesFile(newProperties, file, null);
                    
                }
                catch (Exception e)
                {
                    // ignore
                }
                
            }
            // try automatic renderer settings fix 
            if (EJRWTRendererConfigFix.isRWTCF(newProperties))
            {
                
                try
                {
                    EJRWTRendererConfigFix rendererFix = new EJRWTRendererConfigFix();
                    boolean configed = rendererFix.config(newProperties);
                    if(configed)
                    {
                        new EntireJPropertiesWriter().saveEntireJProperitesFile(newProperties, file, null);
                    }
                    
                }
                catch (Exception e)
                {
                    // ignore
                }
                
            }
            if (EJFXRendererConfigFix.isFXCF(newProperties))
            {
                
                try
                {
                    EJFXRendererConfigFix rendererFix = new EJFXRendererConfigFix();
                    boolean configed = rendererFix.config(newProperties);
                    if(configed)
                    {
                        new EntireJPropertiesWriter().saveEntireJProperitesFile(newProperties, file, null);
                    }
                    
                }
                catch (Exception e)
                {
                    // ignore
                }
                
            }
            
        }
        catch (SAXException e)
        {
            throw new EJDevFrameworkException(e);
        }
        catch (ParserConfigurationException e)
        {
            throw new EJDevFrameworkException(e);
        }
        catch (IOException e)
        {
            throw new EJDevFrameworkException(e);
        }
    }
    

    
}
