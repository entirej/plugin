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
package org.entirej.ide.ui.editors.form;

import java.io.IOException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;

public abstract class AbstractEJFormEditor extends AbstractEditor implements IJavaProjectProvider
{

    protected final Object                    MODEL_LOCK = new Object();
    protected volatile EJPluginFormProperties formProperties;
    protected volatile IJavaProject           project;
    protected EJFormBasePage                  formBasePage;

    @Override
    public AbstractEditorPage[] getAbstractEditorPages()
    {
        return new AbstractEditorPage[] { formBasePage = createFormPage() };
    }

    protected EJFormBasePage createFormPage()
    {
        return new EJFormBasePage(this);
    }

    @Override
    public String getActivePageID()
    {
        return EJFormBasePage.PAGE_ID;
    }

    @Override
    public void refreshAfterBuid()
    {
        if (formBasePage != null)
            formBasePage.refreshAfterBuid();
    }

    @Override
    public void saveFile(IFile file, IProgressMonitor monitor) throws IOException
    {
        FormPropertiesWriter write = new FormPropertiesWriter();
        write.saveForm(formProperties, file, monitor);

    }

    @Override
    public void dispose()
    {

        super.dispose();
        if (formProperties != null)
            formProperties.dispose();
        formProperties = null;
    }

    public EJPluginFormProperties getFormProperties()
    {
        synchronized (MODEL_LOCK)
        {
            return formProperties;
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
