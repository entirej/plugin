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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.report.ReportDesignTreeSection.ReportPreviewer;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;

public class ReportPreviewPart extends AbstractDescriptorPart implements INodeDescriptorViewer, ReportPreviewer
{
    private final AbstractEJReportEditor editor;
    // private AbstractNode<?> selectedNode;
    private ScrolledComposite            previewComposite;
    private AtomicBoolean              autoRefrsh = new AtomicBoolean(true);
    private IReportPreviewProvider       previewProvider;
    private final IReportPreviewProvider defaultPreviewProvider = new IReportPreviewProvider()
                                                                {

                                                                    public void dispose()
                                                                    {
                                                                        // ignore

                                                                    }

                                                                    public void buildPreview(AbstractEJReportEditor editor, ScrolledComposite previewComposite,Object o)
                                                                    {
                                                                        previewComposite.setBackground(body.getBackground());
                                                                        // ignore
                                                                    }

                                                                    public String getDescription()
                                                                    {
                                                                        return "select ui element to Edit.";
                                                                    }

                                                                    public void refresh(AbstractEJReportEditor editor, ScrolledComposite previewComposite,
                                                                            Object selection)
                                                                    {
                                                                        // TODO Auto-generated method stub
                                                                        
                                                                    }
                                                                };

    private AbstractNode<?>              selectedNode;
    private ReportPreviewImpl previewProviderBase;

    public ReportPreviewPart(AbstractEJReportEditor editor, FormPage page, Composite parent)
    {
        super(editor.getToolkit(), parent, true);
        this.editor = editor;
        buildUI();

    }

    @Override
    public void dispose()
    {
        selectedNode = null;
        if (previewProvider != null)
        {
            previewProvider.dispose();
            previewProvider = null;
        }
        defaultPreviewProvider.dispose();

        super.dispose();

    }

    @Override
    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setLayout(new FillLayout());
        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL | GridData.GRAB_HORIZONTAL);
        section.setLayoutData(sectionData);
    }

    @Override
    public void refresh()
    {
        super.refresh();
        previewLayout();

    }

    @Override
    public Action[] getToolbarActions()
    {
        final Action refreshAction = new Action("Refresh", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {
                boolean state = autoRefrsh.get();
                try
                {
                    autoRefrsh.set(true);
                    previewLayout();
                }
                finally
                {
                    autoRefrsh.set(state);
                }
                
            }

        };
        refreshAction.setImageDescriptor(EJUIImages.DESC_REFRESH);
       
        final Action autoRefresh = new Action("Toggle Auto Refresh", IAction.AS_CHECK_BOX)
        {
            
            @Override
            public void run()
            {
                autoRefrsh.set(isChecked());
            }
            
        };
        autoRefresh.setImageDescriptor(EJUIImages.DESC_AUTO_REFRESH);
        autoRefresh.setChecked(true);
      
        return new Action[] { autoRefresh,refreshAction };
    }

    @Override
    public AbstractDescriptor<?>[] getDescriptors()
    {

        return new AbstractDescriptor<?>[0];
    }
    
    @Override
    public Object getInput()
    {
        return new Object();
    }

    @Override
    public String getSectionTitle()
    {
        return "Report Editor";
    }

    @Override
    public String getSectionDescription()
    {

        return "edit the defined layout in report.";
    }

    public void showDetails(AbstractNode<?> node)
    {
        selectedNode = node;
        previewLayout();

    }

    @Override
    public void setFocus()
    {
        if (getSection().isDisposed() || getSection().getClient() == null || getSection().getClient().isDisposed())
            return;

        super.setFocus();
    }

    private void previewLayout()
    {
        if(!autoRefrsh.get())
            return;
        
        
        
        
        if (previewComposite != null)
        {
            if (selectedNode != null)
            {
                IReportPreviewProvider provider = selectedNode.getAdapter(IReportPreviewProvider.class);
                if(previewProvider==provider && provider!=null)
                {
                    previewProvider.refresh(editor, previewComposite, selectedNode.getSource());
                    return;
                }
               
                if(provider!=null || previewProvider!=previewProviderBase)
                    previewProvider = provider;
            }
            if(previewProvider==previewProviderBase && previewProviderBase!=null)
            {
                previewProvider.refresh(editor, previewComposite, selectedNode.getSource());
                return;
            }
            if (previewProvider == null)
            {
                if(previewProviderBase==null)
                {
                    previewProviderBase = new ReportPreviewImpl();
                }
                previewProvider = previewProviderBase;
            }
            
            getSection().setRedraw(false);
            final Composite drop = previewComposite;
            final Shell shell = new Shell();
            drop.setParent(shell);
            // Defer disposal so that the effect of refresh in cleaning resources happens.
            drop. getDisplay().asyncExec(new Runnable() {
              public void run() {
                  drop.dispose();
                shell.dispose();
              }
            });
            previewComposite.dispose();
            previewComposite = null;
            if (previewProvider != null)
            {
                previewProvider.dispose();
            }
            getSection().setDescription(getSectionDescription());
        }
        body.setLayout(new GridLayout());

        previewComposite = new ScrolledComposite(body, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        previewComposite.setLayoutData(layoutData);
        layoutData.widthHint = 100;
        layoutData.heightHint = 100;

        
        try
        {
            if (previewProvider != null)
            {
                previewProvider.buildPreview(editor, previewComposite,selectedNode.getSource());
                getSection().setDescription(previewProvider.getDescription());
            }
            else
            {
                getSection().setDescription(defaultPreviewProvider.getDescription());
                defaultPreviewProvider.buildPreview(editor, previewComposite,selectedNode.getSource());
            }
        }
        catch (Throwable e)
        {
            previewComposite.dispose();
            previewComposite = null;
            if (previewProvider != null)
            {
                previewProvider.dispose();
                previewProvider = null;
            }
            previewComposite = new ScrolledComposite(body, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

            previewComposite.setLayoutData(layoutData);
            final Writer result = new StringWriter();
            e.printStackTrace(new PrintWriter(result));
            getSection().setDescription("error occurred on preview.");
            Text content = new Text(previewComposite, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
            content.setText(result.toString());
            previewComposite.setContent(content);
            previewComposite.setExpandHorizontal(true);
            previewComposite.setExpandVertical(true);
            EJCoreLog.log(e);
        }

        getSection().layout();
        getSection().setRedraw(true);
    }

    public void buildUI()
    {

        FormToolkit toolkit = editor.getToolkit();
        final Section section = getSection();
        section.setText(getSectionTitle());
        section.setDescription(getSectionDescription());

        body = toolkit.createComposite(section);

        body.setLayout(new FillLayout());
        body.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        section.setTabList(new Control[] { body });

        toolkit.paintBordersFor(body);
        section.setClient(body);
        section.layout();

    }

}
