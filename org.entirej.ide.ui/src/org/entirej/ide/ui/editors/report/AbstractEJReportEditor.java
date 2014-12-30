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
package org.entirej.ide.ui.editors.report;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.ui.IPartListener;
import org.eclipse.ui.IWorkbenchPart;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.writer.ReportPropertiesWriter;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.editors.form.AbstractEJFormEditor;
import org.entirej.ide.ui.editors.form.EJFormBasePage;
import org.entirej.ide.ui.nodes.AbstractNode;

public abstract class AbstractEJReportEditor extends AbstractEditor implements IJavaProjectProvider
{

    protected final Object                    MODEL_LOCK = new Object();
    protected volatile EJPluginReportProperties reportProperties;
    protected volatile IJavaProject           project;
    protected EJReportBasePage                  formBasePage;

    
    
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
                                                       if (!active.get() && part == AbstractEJReportEditor.this)
                                                       {

                                                           active.set(true);
                                                           autoPerspectiveSwitch(
                                                                   "org.entirej.ide.ui.report.perspective",
                                                                   EJUIPlugin.getDefault().getPreferenceStore(),
                                                                   "AbstractEJReportEditor.autoPerspectiveSwitch",
                                                                   "This Editor is associated with the EntireJ Report Perspective.\n\nIt is highly recommended to switch to that perspective when editing an EntireJ Reports.\n\nDo you want to switch to the EntireJ Report Perspective now?");
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
        return new AbstractEditorPage[] { formBasePage = createReportPage() };
    }

    protected EJReportBasePage createReportPage()
    {
        return new EJReportBasePage(this);
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
        ReportPropertiesWriter write = new ReportPropertiesWriter();
        write.saveReport(reportProperties, file, monitor);

    }


    public EJPluginReportProperties getReportProperties()
    {
        synchronized (MODEL_LOCK)
        {
            return reportProperties;
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
    
    
    public void select(Object objects )
    {
        if(formBasePage!=null )
        {
            formBasePage.select(objects);
        }
    }

    public void expand(Object objects)
    {
        if(formBasePage!=null )
        {
            formBasePage.expand(objects);
        }
        
    }
    public void refresh(Object objects)
    {
        if(formBasePage!=null )
        {
            formBasePage.refresh(objects);
        }
        
    }
    public AbstractNode<?>  findNode(Object object)
    {
        if(formBasePage!=null )
        {
            return formBasePage.treeSection.findNode(object);
        }
        
        return null;
    }
    public void refreshProperties()
    {
        if(formBasePage!=null )
        {
            formBasePage.refreshProperties();
        }
        
    }

    public void refreshPreview()
    {
        if(formBasePage!=null )
        {
             formBasePage.treeSection.refreshPreview();
        }
        
    }

}
