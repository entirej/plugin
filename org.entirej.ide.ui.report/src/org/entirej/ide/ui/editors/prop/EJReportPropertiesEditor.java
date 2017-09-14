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
package org.entirej.ide.ui.editors.prop;

import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportProperties;
import org.entirej.framework.plugin.reports.reader.EntireJReportPropertiesReader;
import org.entirej.framework.plugin.reports.writer.EntireJReportPropertiesWriter;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.editors.AbstractEditorPage;

public class EJReportPropertiesEditor extends AbstractEditor
{

    private final Object                       MODEL_LOCK = new Object();
    private volatile EJPluginEntireJReportProperties entireJProperties;
    private volatile IJavaProject              project;

    @Override
    public AbstractEditorPage[] getAbstractEditorPages()
    {
        return new AbstractEditorPage[] { new EJReportPropertiesPage(this),
                new EJReportVisualAttributePage(this) };
    }

    @Override
    public String getActivePageID()
    {
        return EJReportPropertiesPage.PAGE_ID;
    }

    @Override
    public void saveFile(IFile file, IProgressMonitor monitor) throws IOException
    {
        EntireJReportPropertiesWriter saver = new EntireJReportPropertiesWriter();
        saver.saveEntireJProperitesFile(entireJProperties, file, monitor);

    }

    @Override
    public void dispose()
    {

        super.dispose();
        if (entireJProperties != null)
            entireJProperties.clear();
        entireJProperties = null;
    }

    public EJPluginEntireJReportProperties getEntireJProperties()
    {
        synchronized (MODEL_LOCK)
        {
            return entireJProperties;
        }
    }

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
                file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
                inStream = file.getContents();
                entireJProperties = new EJPluginEntireJReportProperties(project);
                EntireJReportPropertiesReader.readProperties(entireJProperties,project, inStream,file);
                

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

    @Override
    public void refreshFile(IFile file)
    {
        loadFile(file);
    }

    public IJavaProject getJavaProject()
    {
        synchronized (MODEL_LOCK)
        {
            return project;
        }
    }
}
