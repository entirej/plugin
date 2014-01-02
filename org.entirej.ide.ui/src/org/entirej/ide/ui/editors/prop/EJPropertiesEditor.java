/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 *     Mojave Innovations GmbH - initial API and implementation
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
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.reader.EntireJPropertiesReader;
import org.entirej.framework.plugin.framework.properties.reader.EntireJRendererReader;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.editors.AbstractEditorPage;

public class EJPropertiesEditor extends AbstractEditor
{

    private final Object                       MODEL_LOCK = new Object();
    private volatile EJPluginEntireJProperties entireJProperties;
    private volatile IJavaProject              project;

    @Override
    public AbstractEditorPage[] getAbstractEditorPages()
    {
        return new AbstractEditorPage[] { new EJPropertiesPage(this), new EJLayoutPage(this), new EJRenderersPage(this), new EJMenuPage(this),
                new EJVisualAttributePage(this) };
    }

    @Override
    public String getActivePageID()
    {
        return EJPropertiesPage.PAGE_ID;
    }

    @Override
    public void saveFile(IFile file, IProgressMonitor monitor) throws IOException
    {
        EntireJPropertiesWriter saver = new EntireJPropertiesWriter();
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

    public EJPluginEntireJProperties getEntireJProperties()
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
                entireJProperties = new EntirejPluginPropertiesEnterpriseEdition(project);
                EntireJPropertiesReader.readProperties(entireJProperties,project, inStream,file,EJProject.getRendererFile(_project));
                

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
