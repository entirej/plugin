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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.form.FormDesignTreeSection.FormPreviewer;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;

public class FormPreviewPart extends AbstractDescriptorPart implements INodeDescriptorViewer, FormPreviewer
{
    private final AbstractEJFormEditor editor;
    // private AbstractNode<?> selectedNode;
    private ScrolledComposite          previewComposite;

    private IFormPreviewProvider       previewProvider;
    private final IFormPreviewProvider defaultPreviewProvider = new IFormPreviewProvider()
                                                              {

                                                                  public void dispose()
                                                                  {
                                                                      // ignore

                                                                  }

                                                                  public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                                                                  {
                                                                      previewComposite.setBackground(body.getBackground());
                                                                      // ignore
                                                                  }

                                                                  public String getDescription()
                                                                  {
                                                                      return "select ui element to preview.";
                                                                  }
                                                              };

    private AbstractNode<?>            selectedNode;

    public FormPreviewPart(AbstractEJFormEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent, true);
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
                previewLayout();
            }

        };
        refreshAction.setImageDescriptor(EJUIImages.DESC_REFRESH);
        return new Action[] { refreshAction };
    }

    @Override
    public AbstractDescriptor<?>[] getDescriptors()
    {

        return new AbstractDescriptor<?>[0];
    }

    @Override
    public String getSectionTitle()
    {
        return "Preview";
    }

    @Override
    public String getSectionDescription()
    {

        return "preview the defined layout in form.";
    }

    public void showDetails(AbstractNode<?> node)
    {
        selectedNode = node;
        previewLayout();

    }

    private void previewLayout()
    {
        getSection().setRedraw(false);
        if (previewComposite != null)
        {
            previewComposite.dispose();
            previewComposite = null;
            if (previewProvider != null)
            {
                previewProvider.dispose();
                previewProvider = null;
            }
            getSection().setDescription(getSectionDescription());
        }
        body.setLayout(new GridLayout());

        previewComposite = new ScrolledComposite(body, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        previewComposite.setLayoutData(layoutData);
        layoutData.widthHint = 100;
        layoutData.heightHint = 100;

        if (selectedNode != null)
        {
            previewProvider = selectedNode.getAdapter(IFormPreviewProvider.class);
        }
        try
        {
            if (previewProvider != null)
            {
                previewProvider.buildPreview(editor, previewComposite);
                getSection().setDescription(previewProvider.getDescription());
            }
            else
            {
                getSection().setDescription(defaultPreviewProvider.getDescription());
                defaultPreviewProvider.buildPreview(editor, previewComposite);
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
