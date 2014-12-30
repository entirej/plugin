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
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.ide.ui.EJUIPlugin;
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
    protected void addPages()
    {
        super.addPages();
        getSite().getPage().addPartListener(partListener);
    }

    @Override
    public void dispose()
    {

        super.dispose();
        getSite().getPage().removePartListener(partListener);
    }

    protected final IPartListener partListener = new IPartListener()
                                               {
                                                   AtomicBoolean active = new AtomicBoolean(false);

                                                   public void partOpened(IWorkbenchPart part)
                                                   {
                                                   }

                                                   public void partDeactivated(IWorkbenchPart part)
                                                   {
                                                       active.set(false);
                                                   }

                                                   public void partClosed(IWorkbenchPart part)
                                                   {
                                                       active.set(false);
                                                   }

                                                   public void partBroughtToTop(IWorkbenchPart part)
                                                   {
                                                   }

                                                   public void partActivated(IWorkbenchPart part)
                                                   {
                                                       if (!active.get() && part == AbstractEJFormEditor.this)
                                                       {

                                                           active.set(true);
                                                           autoPerspectiveSwitch(
                                                                   "org.entirej.ide.ui.perspective",
                                                                   EJUIPlugin.getDefault().getPreferenceStore(),
                                                                   "AbstractEJFormEditor.autoPerspectiveSwitch",
                                                                   "This Editor is associated with the EntireJ Form Perspective.\n\nIt is highly recommended to switch to that perspective when editing EntireJ Forms.\n\nDo you want to switch to the EntireJ Forms Perspective now?");
                                                       }
                                                       else
                                                       {
                                                           active.set(true);
                                                       }
                                                   }
                                               };

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
