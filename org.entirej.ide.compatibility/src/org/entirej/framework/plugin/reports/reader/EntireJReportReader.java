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

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.ide.core.EJCoreLog;

public class EntireJReportReader
{
    public EJPluginReportProperties readReport(ReportHandler reportHandler, IJavaProject project, InputStream inStream) throws EJDevFrameworkException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxPArser = factory.newSAXParser();
            saxPArser.parse(inStream, reportHandler);
            return reportHandler.getReportProperties();
        }
        
        catch (Throwable e)
        {
            EJCoreLog.log(e);
            throw new EJDevFrameworkException(e.getMessage());
        }
    }
    
    public static String readReportName(IFile file, boolean usemeta)
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
            ReportNameHandler handler = new ReportNameHandler();
            saxPArser.parse(inStream, handler);
            return usemeta ? handler.getDefaultReportName() : handler.getReportName();
            
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
