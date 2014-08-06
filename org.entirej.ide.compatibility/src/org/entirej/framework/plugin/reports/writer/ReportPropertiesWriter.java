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
package org.entirej.framework.plugin.reports.writer;

import java.io.ByteArrayInputStream;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.writer.AbstractXmlWriter;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.utils.EJPluginLogger;

public class ReportPropertiesWriter extends AbstractXmlWriter
{
    public void saveReport(EJPluginReportProperties form, IFile file, IProgressMonitor monitor)
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        startTAG(buffer, "entirejFramework");
        {
            startTAG(buffer, "report");
            {
                writeStringTAG(buffer, "reportTitle", form.getTitle());
                writeStringTAG(buffer, "reportDisplayName", form.getReportDisplayName());
                writeIntTAG(buffer, "width", form.getReportWidth());
                writeIntTAG(buffer, "height", form.getReportHeight());
                writeIntTAG(buffer, "marginTop", form.getMarginTop());
                writeIntTAG(buffer, "marginBottom", form.getMarginBottom());
                writeIntTAG(buffer, "marginLeft", form.getMarginLeft());
                writeIntTAG(buffer, "marginRight", form.getMarginRight());
                writeStringTAG(buffer, "orientation", form.getOrientation().name());
                writeStringTAG(buffer, "actionProcessorClassName", form.getActionProcessorClassName());
                
                // Now add the forms parameters
                Iterator<EJPluginApplicationParameter> paramNamesIti = form.getAllReportParameters().iterator();
                startTAG(buffer, "reportParameterList");
                {
                    EJPluginApplicationParameter parameter;
                    while (paramNamesIti.hasNext())
                    {
                        parameter = paramNamesIti.next();
                        
                        startOpenTAG(buffer, "reportParameter");
                        {
                            writePROPERTY(buffer, "name", parameter.getName());
                            writePROPERTY(buffer, "dataType", parameter.getDataTypeName());
                            writePROPERTY(buffer, "defaultValue", parameter.getDefaultValue());
                            closeOpenTAG(buffer);
                        }
                        closeTAG(buffer, "reportParameter");
                    }
                }
                endTAG(buffer, "reportParameterList");
                
                // Now add the forms application properties
                Iterator<String> applNamesIti = form.getAllApplicationPropertyNames().iterator();
                startTAG(buffer, "applicationProperties");
                {
                    String name = "";
                    String value = "";
                    while (applNamesIti.hasNext())
                    {
                        name = applNamesIti.next();
                        value = form.getApplicationProperty(name);
                        
                        startOpenTAG(buffer, "property");
                        {
                            writePROPERTY(buffer, "name", name);
                            closeOpenTAG(buffer);
                            
                            writeTagValue(buffer, value);
                        }
                        closeTAG(buffer, "property");
                    }
                }
                endTAG(buffer, "applicationProperties");
                
                
            }
            endTAG(buffer, "report");
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        try
        {
            if (file.exists())
            {
                // byte[] encryptedData =
                // Encrypter.encrypt(buffer.toString().getBytes("UTF-8"));
                // file.setContents(new ByteArrayInputStream(encryptedData),
                // IResource.KEEP_HISTORY, monitor);
                file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
            else
            {
                file.create(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            }
        }
        catch (Exception e)
        {
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save report: " + form.getName());
        }
    }
  
}
