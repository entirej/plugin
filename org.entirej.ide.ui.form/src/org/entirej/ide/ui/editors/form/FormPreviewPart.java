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
package org.entirej.ide.ui.editors.form;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.concurrent.atomic.AtomicBoolean;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
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
import org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder;
import org.entirej.ide.ui.editors.preview.PreviewEditControl;
import org.entirej.ide.ui.editors.preview.PreviewNode;
import org.entirej.ide.ui.editors.preview.PreviewSelectionHandler;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;

public class FormPreviewPart extends AbstractDescriptorPart implements INodeDescriptorViewer, FormPreviewer
{
    private final AbstractEJFormEditor    editor;
    private final AtomicBoolean           autoRefrsh   = new AtomicBoolean(true);
    private final FormPreviewModelBuilder modelBuilder = new FormPreviewModelBuilder();

    private PreviewEditControl            previewControl;
    private AbstractNode<?>               selectedNode;

    public FormPreviewPart(AbstractEJFormEditor editor, FormPage page, Composite parent)
    {
        super(editor.getToolkit(), parent, true);
        this.editor = editor;
        buildUI();
    }

    @Override
    public void dispose()
    {
        selectedNode = null;
        previewControl = null;
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

        return new Action[] { autoRefresh, refreshAction };
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

    @Override
    public void setFocus()
    {
        if (getSection().isDisposed() || getSection().getClient() == null || getSection().getClient().isDisposed())
            return;

        super.setFocus();
    }

    private void previewLayout()
    {
        if (!autoRefrsh.get())
        {
            return;
        }

        getSection().setRedraw(false);
        try
        {
            refreshPreview();
            getSection().setDescription(getSectionDescription());
            body.layout(true, true);
        }
        finally
        {
            getSection().layout(true);
            getSection().setRedraw(true);
        }
    }

    private void refreshPreview()
    {
        try
        {
            ensurePreviewControl();

            Rectangle area = body.getClientArea();
            PreviewNode model = modelBuilder.build(editor.getFormProperties(), selectedNode == null ? null : selectedNode.getSource(), area.width,
                    area.height);
            previewControl.setModel(model);
            if (selectedNode != null)
            {
                previewControl.selectSource(selectedNode.getSource());
            }
            body.layout(true, true);
        }
        catch (Throwable e)
        {
            showError(e);
        }
    }

    private void ensurePreviewControl()
    {
        if (previewControl != null && !previewControl.isDisposed())
        {
            return;
        }

        for (Control child : body.getChildren())
        {
            child.dispose();
        }

        previewControl = new PreviewEditControl(body, new PreviewSelectionHandler()
        {
            public void select(Object source)
            {
                if (source != null && editor.getFormBasePage() != null && editor.getFormBasePage().getTreeSection() != null)
                {
                    editor.getFormBasePage().getTreeSection().selectNodes(true, source);
                }
            }
        });
    }

    private void showError(Throwable e)
    {
        for (Control child : body.getChildren())
        {
            child.dispose();
        }
        previewControl = null;

        final Writer result = new StringWriter();
        e.printStackTrace(new PrintWriter(result));
        Text content = new Text(body, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        content.setText(result.toString());

        body.layout(true, true);
        EJCoreLog.log(e);
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

        toolkit.paintBordersFor(body);
        section.setClient(body);
        ensurePreviewControl();
        section.layout();
    }
}
