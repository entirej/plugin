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
import java.io.InputStream;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.editor.FormPage;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;

public class EJRefLovEditor extends AbstractEJFormEditor
{

    protected EJFormBasePage createFormPage()
    {
        return new EJFormBasePage(this)
        {
            protected FormDesignTreeSection createTreeSection(Composite body)
            {
                return new LovDesignTreeSection(editor, this, body);
            }

            @Override
            protected String getPageHeader()
            {
                return "LOV Design";
            }

        };
    }

    @Override
    public String getActivePageID()
    {
        return EJFormBasePage.PAGE_ID;
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
                try
                {
                    inStream = file.getContents();

                }
                catch (CoreException e)
                {
                    file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
                    inStream = file.getContents();
                }
                EntireJFormReader reader = new EntireJFormReader();
                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                formProperties = reader.readForm(new FormHandler(project, fileName), project, inStream);
                formProperties.initialisationCompleted();
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

    private static class LovDesignTreeSection extends FormDesignTreeSection
    {

        public LovDesignTreeSection(AbstractEJFormEditor editor, FormPage page, Composite parent)
        {
            super(editor, page, parent);
        }

        @Override
        public String getSectionTitle()
        {
            return "LOV Setup";
        }

        @Override
        public String getSectionDescription()
        {

            return "Define design/settings of the LOV in the following section.";
        }

        @Override
        public AbstractNodeContentProvider getContentProvider()
        {
            return new AbstractNodeContentProvider()
            {

                public Object[] getElements(Object inputElement)
                {
                    // project build errors
                    if (editor.getFormProperties() == null)
                        return new Object[0];
                    LovGroupNode blockGroupNode = new LovGroupNode(LovDesignTreeSection.this)
                    {
                        @Override
                        public Action[] getActions()
                        {

                            return new Action[] {};
                        }

                        @Override
                        protected boolean supportLovDelete()
                        {
                            return false;
                        }

                        @Override
                        protected boolean supportLovRename()
                        {
                            return false;
                        }
                    };
                    AbstractNode<?>[] children = blockGroupNode.getChildren();
                    assert children.length > 0;

                    return new Object[] { baseNode = children[0] };
                }
            };
        }

        @Override
        public void addToolbarCustomActions(ToolBarManager toolBarManager, ToolBar toolbar)
        {
            // ignore
        }

        @Override
        public Action[] getBaseActions()
        {

            return new Action[] { /* ignore */};
        }

        protected Action[] getNewBlockActions()
        {
            return new Action[] { /* ignore */};
        }

    }

}
