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
/*
 * Created on Nov 5, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties.writer;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.framework.plugin.EJPluginParameterChecker;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public class EntireJRendererWriter extends AbstractXmlWriter
{
    public void saveEntireJProperitesFile(EJPluginEntireJProperties properties, IFile file, IProgressMonitor monitor) throws UnsupportedEncodingException,
            CoreException
    {
        EJPluginParameterChecker.checkNotNull(properties, "createEntireJRendererFile", "properties");
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        startTAG(buffer, "entirejFramework");
        {
            
            startTAG(buffer, "renderer");
            {
                startTAG(buffer, "formRenderers");
                {
                    // Add the FormRenderers
                    Iterator<EJPluginRenderer> formRenderers = properties.getFormRendererContainer().getAllRenderers().iterator();
                    while (formRenderers.hasNext())
                    {
                        EJPluginRenderer property = formRenderers.next();
                        
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "group", property.getGroup());
                            // writePROPERTY(buffer, "rendererClassName",
                            // property.getRendererClassName());
                            writePROPERTY(buffer, "rendererDefinitionClassName", property.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                }
                endTAG(buffer, "formRenderers");
                
                startTAG(buffer, "blockRenderers");
                {
                    // Add the BlockRenderers
                    Iterator<EJPluginRenderer> blockRenderers = properties.getBlockRendererContainer().getAllRenderers().iterator();
                    while (blockRenderers.hasNext())
                    {
                        EJPluginRenderer property = blockRenderers.next();
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "group", property.getGroup());
                            // writePROPERTY(buffer, "rendererClassName",
                            // property.getRendererClassName());
                            writePROPERTY(buffer, "rendererDefinitionClassName", property.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                }
                endTAG(buffer, "blockRenderers");
                
                startTAG(buffer, "itemRenderers");
                {
                    // Add the ItemRenderers
                    Iterator<EJPluginRenderer> itemRenderers = properties.getItemRendererContainer().getAllRenderers().iterator();
                    while (itemRenderers.hasNext())
                    {
                        EJPluginRenderer property = itemRenderers.next();
                        
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "group", property.getGroup());
                            // writePROPERTY(buffer, "rendererClassName",
                            // property.getRendererClassName());
                            writePROPERTY(buffer, "rendererDefinitionClassName", property.getRendererDefinitionClassName());
                            
                            closeOpenTAG(buffer);
                            
                        }
                        endTAG(buffer, "renderer");
                    }
                }
                endTAG(buffer, "itemRenderers");
                
                startTAG(buffer, "lovRenderers");
                {
                    // Add the LovRenderers
                    Iterator<EJPluginRenderer> lovRenderers = properties.getLovRendererContainer().getAllRenderers().iterator();
                    while (lovRenderers.hasNext())
                    {
                        EJPluginRenderer property = lovRenderers.next();
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", property.getAssignedName());
                            writePROPERTY(buffer, "group", property.getGroup());
                            // writePROPERTY(buffer, "rendererClassName",
                            // property.getRendererClassName());
                            writePROPERTY(buffer, "rendererDefinitionClassName", property.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                }
                endTAG(buffer, "lovRenderers");
                
                startTAG(buffer, "menuRenderers");
                {
                    // Add the menuRenderers
                    Collection<EJPluginRenderer> menuRenderers = properties.getMenuRendererContainer().getAllRenderers();
                    for (EJPluginRenderer renderer : menuRenderers)
                    {
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", renderer.getAssignedName());
                            writePROPERTY(buffer, "group", renderer.getGroup());
                            // writePROPERTY(buffer, "rendererClassName",
                            // renderer.getRendererClassName());
                            writePROPERTY(buffer, "rendererDefinitionClassName", renderer.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                    
                }
                endTAG(buffer, "menuRenderers");
                startTAG(buffer, "appCompRenderers");
                {
                    // Add the application component Renderers
                    Collection<EJPluginRenderer> appCompRenderers = properties.getAppComponentRendererContainer().getAllRenderers();
                    for (EJPluginRenderer renderer : appCompRenderers)
                    {
                        startOpenTAG(buffer, "renderer");
                        {
                            writePROPERTY(buffer, "name", renderer.getAssignedName());
                            writePROPERTY(buffer, "group", renderer.getGroup());
                            // writePROPERTY(buffer, "rendererClassName",
                            // renderer.getRendererClassName());
                            writePROPERTY(buffer, "rendererDefinitionClassName", renderer.getRendererDefinitionClassName());
                        }
                        endStartTAG(buffer);
                    }
                    
                }
                endTAG(buffer, "appCompRenderers");
            }
            endTAG(buffer, "renderer");
            
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        if (!file.exists())
        {
            file.create(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), true, monitor);
        }
        else
        {
            file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
        }
        
    }
    
}
