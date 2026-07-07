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
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
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
import org.entirej.ide.ui.editors.form.FormDesignTreeSection.FormPreviewer;
import org.entirej.ide.ui.editors.form.preview.FormPreviewModelBuilder;
import org.entirej.ide.ui.editors.preview.PreviewEditControl;
import org.entirej.ide.ui.editors.preview.PreviewNode;
import org.entirej.ide.ui.editors.preview.PreviewSelectionHandler;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;

public class FormPreviewPart extends AbstractDescriptorPart implements INodeDescriptorViewer, FormPreviewer
{
    private final AbstractEJFormEditor   editor;
    private final AtomicBoolean          autoRefrsh           = new AtomicBoolean(true);
    private final FormPreviewModelBuilder modelBuilder        = new FormPreviewModelBuilder();

    private CTabFolder                   previewTabs;
    private CTabItem                     gefPreviewTab;
    private CTabItem                     swtPreviewTab;
    private Composite                    gefPreviewBody;
    private Composite                    swtPreviewBody;

    private PreviewEditControl           gefPreviewControl;
    private ScrolledComposite            swtPreviewComposite;
    private IFormPreviewProvider         swtPreviewProvider;
    private String                       swtPreviewDescription = "select ui element to preview.";
    private AbstractNode<?>              selectedNode;

    private final IFormPreviewProvider   defaultPreviewProvider = new IFormPreviewProvider()
    {
        public void dispose()
        {
            // Nothing to dispose.
        }

        public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
        {
            if (swtPreviewBody != null && !swtPreviewBody.isDisposed())
            {
                previewComposite.setBackground(swtPreviewBody.getBackground());
            }
        }

        public String getDescription()
        {
            return "select ui element to preview.";
        }
    };

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
        gefPreviewControl = null;
        disposeSwtPreviewProvider();
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
            ensurePreviewTabs();
            refreshGefPreview();
            refreshSwtPreview();
            updateSectionDescription();
            body.layout(true, true);
        }
        finally
        {
            getSection().layout(true);
            getSection().setRedraw(true);
        }
    }

    private void ensurePreviewTabs()
    {
        if (previewTabs != null && !previewTabs.isDisposed())
        {
            return;
        }

        for (Control child : body.getChildren())
        {
            child.dispose();
        }

        previewTabs = new CTabFolder(body, SWT.BOTTOM | SWT.FLAT);
        previewTabs.setSimple(false);
        editor.getToolkit().adapt(previewTabs, true, true);

        gefPreviewBody = editor.getToolkit().createComposite(previewTabs);
        gefPreviewBody.setLayout(new FillLayout());
        gefPreviewTab = new CTabItem(previewTabs, SWT.NONE);
        gefPreviewTab.setText("GEF Preview");
        gefPreviewTab.setControl(gefPreviewBody);

        swtPreviewBody = editor.getToolkit().createComposite(previewTabs);
        swtPreviewBody.setLayout(new FillLayout());
        swtPreviewTab = new CTabItem(previewTabs, SWT.NONE);
        swtPreviewTab.setText("SWT Preview");
        swtPreviewTab.setControl(swtPreviewBody);

        previewTabs.setSelection(gefPreviewTab);
        previewTabs.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent e)
            {
                updateSectionDescription();
            }
        });
        body.setTabList(new Control[] { previewTabs });
    }

    private void refreshGefPreview()
    {
        try
        {
            gefPreviewBody.setLayout(new FillLayout());
            ensureGefPreviewControl();

            org.eclipse.swt.graphics.Rectangle area = gefPreviewBody.getClientArea();
            if ((area.width <= 0 || area.height <= 0) && previewTabs != null && !previewTabs.isDisposed())
            {
                area = previewTabs.getClientArea();
            }

            PreviewNode model = modelBuilder.build(editor.getFormProperties(), selectedNode == null ? null : selectedNode.getSource(), area.width,
                    area.height);
            gefPreviewControl.setModel(model);
            if (selectedNode != null)
            {
                gefPreviewControl.selectSource(selectedNode.getSource());
            }
            gefPreviewTab.setText("GEF Preview");
            gefPreviewBody.layout(true, true);
        }
        catch (Throwable e)
        {
            showGefError(e);
        }
    }

    private void ensureGefPreviewControl()
    {
        if (gefPreviewControl != null && !gefPreviewControl.isDisposed())
        {
            return;
        }

        for (Control child : gefPreviewBody.getChildren())
        {
            child.dispose();
        }

        gefPreviewControl = new PreviewEditControl(gefPreviewBody, new PreviewSelectionHandler()
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

    private void showGefError(Throwable e)
    {
        for (Control child : gefPreviewBody.getChildren())
        {
            child.dispose();
        }
        gefPreviewControl = null;
        gefPreviewTab.setText("GEF Preview *");
        createErrorText(gefPreviewBody, e);
        gefPreviewBody.layout(true, true);
        EJCoreLog.log(e);
    }

    private void refreshSwtPreview()
    {
        swtPreviewDescription = getSectionDescription();
        disposeSwtPreviewComposite();
        disposeSwtPreviewProvider();

        swtPreviewBody.setLayout(new GridLayout());
        swtPreviewComposite = createSwtPreviewComposite();

        if (selectedNode != null)
        {
            swtPreviewProvider = selectedNode.getAdapter(IFormPreviewProvider.class);
        }

        try
        {
            IFormPreviewProvider provider = swtPreviewProvider == null ? defaultPreviewProvider : swtPreviewProvider;
            provider.buildPreview(editor, swtPreviewComposite);
            swtPreviewDescription = provider.getDescription();
            swtPreviewTab.setText("SWT Preview");
        }
        catch (Throwable e)
        {
            showSwtError(e);
        }

        swtPreviewBody.layout(true, true);
    }

    private ScrolledComposite createSwtPreviewComposite()
    {
        ScrolledComposite previewComposite = new ScrolledComposite(swtPreviewBody, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        GridData layoutData = new GridData(GridData.FILL_BOTH);
        layoutData.widthHint = 100;
        layoutData.heightHint = 100;
        previewComposite.setLayoutData(layoutData);
        return previewComposite;
    }

    private void showSwtError(Throwable e)
    {
        if (swtPreviewComposite != null && !swtPreviewComposite.isDisposed())
        {
            swtPreviewComposite.dispose();
        }
        swtPreviewComposite = createSwtPreviewComposite();
        swtPreviewDescription = "error occurred on SWT preview.";
        swtPreviewTab.setText("SWT Preview *");
        Text content = createErrorText(swtPreviewComposite, e);
        swtPreviewComposite.setContent(content);
        swtPreviewComposite.setExpandHorizontal(true);
        swtPreviewComposite.setExpandVertical(true);
        disposeSwtPreviewProvider();
        EJCoreLog.log(e);
    }

    private Text createErrorText(Composite parent, Throwable e)
    {
        final Writer result = new StringWriter();
        e.printStackTrace(new PrintWriter(result));
        Text content = new Text(parent, SWT.MULTI | SWT.V_SCROLL | SWT.H_SCROLL);
        content.setText(result.toString());
        return content;
    }

    private void disposeSwtPreviewProvider()
    {
        if (swtPreviewProvider != null)
        {
            swtPreviewProvider.dispose();
            swtPreviewProvider = null;
        }
    }

    private void disposeSwtPreviewComposite()
    {
        if (swtPreviewComposite == null)
        {
            return;
        }

        if (swtPreviewComposite.isDisposed())
        {
            swtPreviewComposite = null;
            return;
        }

        final Composite drop = swtPreviewComposite;
        final Shell shell = new Shell(drop.getDisplay());
        drop.setParent(shell);
        drop.getDisplay().asyncExec(new Runnable()
        {
            public void run()
            {
                if (!drop.isDisposed())
                {
                    drop.dispose();
                }
                if (!shell.isDisposed())
                {
                    shell.dispose();
                }
            }
        });
        swtPreviewComposite = null;
    }

    private void updateSectionDescription()
    {
        if (previewTabs != null && !previewTabs.isDisposed() && previewTabs.getSelection() == swtPreviewTab)
        {
            getSection().setDescription(swtPreviewDescription);
        }
        else
        {
            getSection().setDescription(getSectionDescription());
        }
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
        ensurePreviewTabs();
        section.layout();
    }
}
