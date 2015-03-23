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
/*
 * Created on Nov 5, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.reports.writer;

import java.io.ByteArrayInputStream;
import java.io.UnsupportedEncodingException;
import java.util.Iterator;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.entirej.framework.plugin.EJPluginParameterChecker;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.writer.AbstractXmlWriter;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportProperties;
import org.entirej.framework.plugin.utils.EJPluginLogger;
import org.entirej.framework.report.properties.EJCoreReportVisualAttributeProperties;
import org.entirej.framework.report.properties.EJReportVisualAttributeProperties;

public class EntireJReportPropertiesWriter extends AbstractXmlWriter
{
    public void saveEntireJProperitesFile(EJPluginEntireJReportProperties properties, IFile file, IProgressMonitor monitor)
    {
        EJPluginParameterChecker.checkNotNull(properties, "createEntireJProperitesFile", "properties");
        
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        
        startTAG(buffer, "entirejFramework");
        {
          
            writeStringTAG(buffer, "version", properties.getVersion() != null ? properties.getVersion() : "1.0");
            writeStringTAG(buffer, "connectionFactoryClassName", properties.getConnectionFactoryClassName());
            writeStringTAG(buffer, "translatorClassName", properties.getTranslatorClassName());
            writeStringTAG(buffer, "reportRunnerClassName", properties.getReportRunnerClassName());
            
            // Now add the application level parameters
            startTAG(buffer, "applicationLevelParameterList");
            {
                addApplicationLevelParameters(properties, buffer);
            }
            endTAG(buffer, "applicationLevelParameterList");
            
           
            startTAG(buffer, "packages");
            {
                // Retrieve the forms package names
                Iterator<String> formPackageNames = properties.getReportPackageNames().iterator();
                while (formPackageNames.hasNext())
                {
                    String name = formPackageNames.next();
                    if (name != null && name.trim().length() > 0)
                    {
                        startOpenTAG(buffer, "reportPackage");
                        {
                            writePROPERTY(buffer, "name", name);
                        }
                        endStartTAG(buffer);
                    }
                }
            }
            endTAG(buffer, "packages");
            
           
            
            
            startTAG(buffer, "visualAttributes");
            {
                // Now add the Visual Attribute definitions
                addVisualAttributes(properties, buffer);
            }
            endTAG(buffer, "visualAttributes");
        }
        endTAG(buffer, "entirejFramework");
        
        // Now set the contents of the file
        try
        {
            file.setContents(new ByteArrayInputStream(buffer.toString().getBytes("UTF-8")), IResource.KEEP_HISTORY, monitor);
            
         
        }
        catch (CoreException e)
        {
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save EntireJ Properties");
        }
        catch (UnsupportedEncodingException e)
        {
            EJPluginLogger.logError(EntireJFrameworkPlugin.getSharedInstance(), "Unable to save EntireJ Properties");
        }
    }
    
    private void addVisualAttributes(EJPluginEntireJReportProperties properties, StringBuffer buffer)
    {
        Iterator<EJCoreReportVisualAttributeProperties> visualAttributes = properties.getVisualAttributesContainer().getVisualAttributes().iterator();
        EJReportVisualAttributeProperties visAttr;
        while (visualAttributes.hasNext())
        {
            visAttr = visualAttributes.next();
            
            startOpenTAG(buffer, "visualAttribute");
            {
                writePROPERTY(buffer, "name", visAttr.getName());
                closeOpenTAG(buffer);
                
                writeStringTAG(buffer, "fontName", visAttr.getFontName());
                writeIntTAG(buffer, "fontSize", visAttr.getFontSize());
                writeBooleanTAG(buffer, "useAsDynamicVA", visAttr.isUsedAsDynamicVA());
                writeStringTAG(buffer, "style", visAttr.getFontStyle().toString());
                writeStringTAG(buffer, "weight", visAttr.getFontWeight().toString());
                writeStringTAG(buffer, "foregroundColor", getColorString(visAttr.getForegroundColor()));
                writeStringTAG(buffer, "backgroundColor", getColorString(visAttr.getBackgroundColor()));
                writeStringTAG(buffer, "markup", visAttr.getMarkupType().name());
                writeStringTAG(buffer, "hAlignment", visAttr.getHAlignment().name());
                writeStringTAG(buffer, "vAlignment", visAttr.getVAlignment().name());
                writeStringTAG(buffer, "localeFormat", visAttr.getLocalePattern().name());
                writeStringTAG(buffer, "manualFormat", visAttr.getManualPattern());
            }
            endTAG(buffer, "visualAttribute");
        }
    }
    
    private String getColorString(java.awt.Color color)
    {
        if (color == null)
        {
            return "";
        }
        
        return "r" + color.getRed() + "g" + color.getGreen() + "b" + color.getBlue();
    }
    
    
    private void addApplicationLevelParameters(EJPluginEntireJReportProperties entireJProperties, StringBuffer buffer)
    {
        // If there is no renderer passed, then just do nothing and return
        if (entireJProperties.getAllApplicationLevelParameters() == null)
        {
            return;
        }
        
        for (EJPluginApplicationParameter parameter : entireJProperties.getAllApplicationLevelParameters())
        {
            startOpenTAG(buffer, "appicationLevelParameter");
            {
                writePROPERTY(buffer, "name", parameter.getName());
                writePROPERTY(buffer, "dataType", parameter.getDataTypeName());
                writePROPERTY(buffer, "defaultValue", parameter.getDefaultValue());
                closeOpenTAG(buffer);
            }
            closeTAG(buffer, "appicationLevelParameter");
        }
    }
    
}
