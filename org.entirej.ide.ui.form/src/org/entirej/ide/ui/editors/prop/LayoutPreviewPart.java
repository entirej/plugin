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

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.form.preview.AppLayoutPreviewModelBuilder;
import org.entirej.ide.ui.editors.preview.PreviewEditControl;
import org.entirej.ide.ui.editors.preview.PreviewNode;
import org.entirej.ide.ui.editors.preview.PreviewSelectionHandler;
import org.entirej.ide.ui.editors.prop.LayoutTreeSection.LayoutPreviewer;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;

public class LayoutPreviewPart extends AbstractDescriptorPart implements INodeDescriptorViewer, LayoutPreviewer
{
    private final EJPropertiesEditor           editor;
    private final LayoutTreeSection            treeSection;
    private final AppLayoutPreviewModelBuilder modelBuilder = new AppLayoutPreviewModelBuilder();

    private PreviewEditControl                 previewControl;
    private AbstractNode<?>                    selectedNode;

    public LayoutPreviewPart(EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        this(editor, page, parent, null);
    }

    public LayoutPreviewPart(EJPropertiesEditor editor, FormPage page, Composite parent, LayoutTreeSection treeSection)
    {
        super(editor.getToolkit(), parent, true);
        this.editor = editor;
        this.treeSection = treeSection;
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
    public void setFocus()
    {
        if (getSection().isDisposed() || getSection().getClient() == null || getSection().getClient().isDisposed())
            return;

        super.setFocus();
    }

    @Override
    protected void buildBody(Section section, FormToolkit toolkit)
    {
        section.setLayout(new FillLayout());
        GridData sectionData = new GridData(GridData.FILL_BOTH);
        section.setLayoutData(sectionData);
    }

    @Override
    public void refresh()
    {
        super.refresh();
        Display.getDefault().asyncExec(new Runnable()
        {
            public void run()
            {
                previewLayout();
            }
        });
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
        return "preview the defined layout in application.";
    }

    public void showDetails(AbstractNode<?> node)
    {
        selectedNode = node;
        if (previewControl != null && !previewControl.isDisposed() && selectedNode != null)
        {
            previewControl.selectSource(selectedNode.getSource());
        }
        else
        {
            previewLayout();
        }
    }

    private void previewLayout()
    {
        if (getSection().isDisposed())
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
            PreviewNode model = modelBuilder.build(editor.getEntireJProperties(), area.width, area.height, selectedSource());
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
                if (source != null && treeSection != null)
                {
                    treeSection.selectNodes(true, source);
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

    private Object selectedSource()
    {
        return selectedNode == null ? null : selectedNode.getSource();
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
