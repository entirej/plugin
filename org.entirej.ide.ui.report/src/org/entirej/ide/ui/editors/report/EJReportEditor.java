/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.report;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.framework.plugin.reports.reader.EntireJReportReader;
import org.entirej.framework.plugin.reports.reader.ReportHandler;
import org.entirej.ide.core.EJCoreLog;

public class EJReportEditor extends AbstractEJReportEditor
{

    @Override
    public void loadFile(IFile file)
    {
        IProject _project = file.getProject();
        synchronized (MODEL_LOCK)
        {
            project = JavaCore.create(_project);

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
                    inStream = file.getContents();
                }
                EntireJReportReader reader = new EntireJReportReader();
                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                reportProperties = reader.readReport(new ReportHandler(project, fileName), project, inStream);
                reportProperties.initialisationCompleted();
            }
            catch (Exception exception)
            {
                close(false);
                EJCoreLog.logException(exception);

            }
            finally
            {

                try
                {
                    if (inStream != null)
                        inStream.close();
                }
                catch (IOException e)
                {
                    EJCoreLog.logException(e);
                }
            }
        }

    }

}
