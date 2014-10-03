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
package org.entirej.framework.plugin.reports.reader;

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
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.reader.EJRWTEJApplicationPropertiesFixV1;
import org.entirej.framework.plugin.framework.properties.reader.EJRWTRendererConfigFix;
import org.entirej.framework.plugin.framework.properties.reader.EntireJPropertiesHandler;
import org.entirej.framework.plugin.framework.properties.reader.EntireJRendererReader;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportProperties;
import org.xml.sax.SAXException;

public class EntireJReportPropertiesReader
{
    public static void readProperties(EJPluginEntireJReportProperties newProperties, IJavaProject project, InputStream inStream, IFile file)
            throws EJDevFrameworkException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxPArser = factory.newSAXParser();
            EntireJReportPropertiesHandler handler = new EntireJReportPropertiesHandler(newProperties);
            saxPArser.parse(inStream, handler);
            
            
            
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
