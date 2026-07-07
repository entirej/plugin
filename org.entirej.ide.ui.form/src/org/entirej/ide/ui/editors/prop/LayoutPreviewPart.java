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
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.framework.core.properties.EJCoreLayoutContainer;
import org.entirej.framework.core.properties.EJCoreLayoutItem;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutComponent;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutSpace;
import org.entirej.framework.core.properties.EJCoreLayoutItem.SplitGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.SplitGroup.ORIENTATION;
import org.entirej.framework.core.properties.EJCoreLayoutItem.TabGroup;
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
    private final AppLayoutPreviewModelBuilder modelBuilder       = new AppLayoutPreviewModelBuilder();

    private final Color                        COLOR_LIGHT_RED    = new Color(Display.getCurrent(), new RGB(255, 170, 170));
    private final Color                        COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);

    private CTabFolder                         previewTabs;
    private CTabItem                           gefPreviewTab;
    private CTabItem                           swtPreviewTab;
    private Composite                          gefPreviewBody;
    private Composite                          swtPreviewBody;

    private PreviewEditControl                 gefPreviewControl;
    private ScrolledComposite                  swtPreviewComposite;
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
        gefPreviewControl = null;
        COLOR_LIGHT_RED.dispose();
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
        if (previewTabs != null && !previewTabs.isDisposed())
        {
            refreshGefPreview();
            refreshSwtPreview();
        }
        else if (gefPreviewControl != null && !gefPreviewControl.isDisposed() && selectedNode != null)
        {
            gefPreviewControl.selectSource(selectedNode.getSource());
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
            PreviewNode model = modelBuilder.build(editor.getEntireJProperties(), area.width, area.height, selectedSource());
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
                if (source != null && treeSection != null)
                {
                    treeSection.selectNodes(true, source);
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
        try
        {
            if (swtPreviewComposite != null && !swtPreviewComposite.isDisposed())
            {
                swtPreviewComposite.dispose();
            }
            swtPreviewBody.setLayout(new GridLayout());
            swtPreviewComposite = createSwtPreviewComposite();

            Composite pContent = new Composite(swtPreviewComposite, SWT.NONE);
            pContent.setBackground(body.getBackground());
            EJCoreLayoutContainer container = editor.getEntireJProperties().getLayoutContainer();
            int width = container.getWidth();
            int height = container.getHeight();
            swtPreviewComposite.setContent(pContent);
            swtPreviewComposite.setBackground(body.getBackground());
            swtPreviewComposite.setExpandHorizontal(true);
            swtPreviewComposite.setExpandVertical(true);

            pContent.setLayout(new GridLayout());

            Composite layoutBody = new Composite(pContent, SWT.BORDER);
            layoutBody.setLayout(new GridLayout(container.getColumns(), false));

            GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
            sectionData.widthHint = width;
            sectionData.heightHint = height;
            layoutBody.setLayoutData(sectionData);
            layoutBody.setBackground(COLOR_LIGHT_YELLOW);

            List<EJCoreLayoutItem> items = container.getItems();
            for (EJCoreLayoutItem item : items)
            {
                createLegacyLayoutItem(layoutBody, item);
            }

            if (width > 0 && height > 0)
            {
                swtPreviewComposite.setMinSize(width, height);
            }
            else
            {
                swtPreviewComposite.setMinSize(pContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));
            }
            swtPreviewTab.setText("SWT Preview");
            swtPreviewBody.layout(true, true);
        }
        catch (Throwable e)
        {
            showSwtError(e);
        }
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
        swtPreviewTab.setText("SWT Preview *");
        Text content = createErrorText(swtPreviewComposite, e);
        swtPreviewComposite.setContent(content);
        swtPreviewComposite.setExpandHorizontal(true);
        swtPreviewComposite.setExpandVertical(true);
        swtPreviewBody.layout(true, true);
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

    private GridData createGridData(EJCoreLayoutItem layoutItem)
    {
        GridData gd = new GridData();
        gd.minimumHeight = layoutItem.getMinHeight();
        gd.minimumWidth = layoutItem.getMinWidth();
        gd.heightHint = layoutItem.getHintHeight();
        gd.widthHint = layoutItem.getHintWidth();
        gd.verticalSpan = layoutItem.getVerticalSpan();
        gd.horizontalSpan = layoutItem.getHorizontalSpan();

        switch (layoutItem.getGrab())
        {
            case BOTH:
                gd.grabExcessHorizontalSpace = true;
                gd.grabExcessVerticalSpace = true;
                break;
            case HORIZONTAL:
                gd.grabExcessHorizontalSpace = true;
                break;
            case VERTICAL:
                gd.grabExcessVerticalSpace = true;
                break;
        }
        switch (layoutItem.getFill())
        {
            case BOTH:
                gd.verticalAlignment = SWT.FILL;
                gd.horizontalAlignment = SWT.FILL;
                break;
            case VERTICAL:
                gd.verticalAlignment = SWT.FILL;
                break;
            case HORIZONTAL:
                gd.horizontalAlignment = SWT.FILL;
                break;
            case NONE:
                break;
        }

        if (gd.grabExcessHorizontalSpace && gd.widthHint == 0)
        {
            gd.horizontalAlignment = SWT.FILL;
        }

        if (gd.grabExcessVerticalSpace && gd.heightHint == 0)
        {
            gd.verticalAlignment = SWT.FILL;
        }

        return gd;
    }

    private void createLegacyLayoutItem(Composite parent, EJCoreLayoutItem item)
    {
        switch (item.getType())
        {
            case GROUP:
                createGroupLayout(parent, (LayoutGroup) item);
                break;
            case SPACE:
                createSpace(parent, (LayoutSpace) item);
                break;
            case COMPONENT:
                createComponent(parent, (LayoutComponent) item);
                break;
            case SPLIT:
                createSplitLayout(parent, (SplitGroup) item);
                break;
            case TAB:
                createTabLayout(parent, (TabGroup) item);
                break;
        }
    }

    private void createSpace(Composite parent, EJCoreLayoutItem.LayoutSpace space)
    {
        Composite layoutBody = new Composite(parent, SWT.BORDER);
        layoutBody.setBackground(COLOR_LIGHT_YELLOW);
        layoutBody.setLayoutData(createGridData(space));
        layoutBody.setLayout(new GridLayout());
        Label spaceLabel = new Label(layoutBody, SWT.NONE);
        spaceLabel.setBackground(COLOR_LIGHT_YELLOW);
        spaceLabel.setText("<space>");
        spaceLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void createComponent(Composite parent, EJCoreLayoutItem.LayoutComponent component)
    {
        Composite layoutBody = new Composite(parent, SWT.BORDER);
        layoutBody.setLayoutData(createGridData(component));
        layoutBody.setLayout(new GridLayout());
        Label spaceLabel = new Label(layoutBody, SWT.NONE);
        spaceLabel.setText(String.format("<%s>",
                (component.getRenderer() == null || component.getRenderer().length() == 0) ? "<component>" : component.getRenderer()));
        spaceLabel.setLayoutData(createGridData(component));
        layoutBody.setBackground(COLOR_LIGHT_RED);
        spaceLabel.setBackground(COLOR_LIGHT_RED);
        spaceLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
    }

    private void createGroupLayout(Composite parent, EJCoreLayoutItem.LayoutGroup group)
    {
        Composite layoutBody = new Composite(parent, SWT.BORDER);
        layoutBody.setLayoutData(createGridData(group));
        layoutBody.setBackground(COLOR_LIGHT_YELLOW);
        List<EJCoreLayoutItem> items = group.getItems();
        if (items.size() > 0)
        {
            GridLayout gridLayout = new GridLayout(group.getColumns(), false);
            if (group.isHideMargin())
            {
                gridLayout.marginHeight = 0;
                gridLayout.marginWidth = 0;
            }
            layoutBody.setLayout(gridLayout);
            for (EJCoreLayoutItem item : items)
            {
                createLegacyLayoutItem(layoutBody, item);
            }
        }
        else
        {
            layoutBody.setLayout(new GridLayout());
            Label compLabel = new Label(layoutBody, SWT.NONE);
            compLabel.setText(String.format("<%s>", (group.getTitle() == null || group.getTitle().length() == 0) ? "<group>" : group.getTitle()));
            compLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
            compLabel.setBackground(COLOR_LIGHT_YELLOW);
        }
    }

    private void createSplitLayout(Composite parent, EJCoreLayoutItem.SplitGroup group)
    {
        SashForm layoutBody = new SashForm(parent, group.getOrientation() == ORIENTATION.HORIZONTAL ? SWT.HORIZONTAL : SWT.VERTICAL);
        layoutBody.setLayoutData(createGridData(group));
        layoutBody.setBackground(COLOR_LIGHT_YELLOW);
        List<EJCoreLayoutItem> items = group.getItems();
        if (items.size() > 0)
        {
            int[] weights = new int[items.size()];

            for (EJCoreLayoutItem item : items)
            {
                weights[items.indexOf(item)] = item.getHintWidth() + 1;
                createLegacyLayoutItem(layoutBody, item);
            }

            layoutBody.setWeights(weights);
        }
        else
        {
            layoutBody.setLayout(new GridLayout());
            Label compLabel = new Label(layoutBody, SWT.NONE);
            compLabel.setText("<split>");
            compLabel.setLayoutData(new GridData(GridData.FILL_BOTH));
            compLabel.setBackground(COLOR_LIGHT_YELLOW);
        }
    }

    private void createTabLayout(Composite parent, EJCoreLayoutItem.TabGroup group)
    {
        CTabFolder layoutBody = new CTabFolder(parent, SWT.BORDER | (group.getOrientation() == TabGroup.ORIENTATION.TOP ? SWT.TOP : SWT.BOTTOM));
        layoutBody.setLayoutData(createGridData(group));
        layoutBody.setBackground(COLOR_LIGHT_YELLOW);
        List<EJCoreLayoutItem> items = group.getItems();

        for (EJCoreLayoutItem item : items)
        {
            CTabItem tabItem = new CTabItem(layoutBody, SWT.NONE);
            Composite composite = new Composite(layoutBody, SWT.NONE);
            composite.setLayout(new FillLayout());
            tabItem.setControl(composite);
            tabItem.setData(item);
            tabItem.setText(item.getName() != null ? item.getName() : "<title>");
            createLegacyLayoutItem(composite, item);
        }
        if (items.size() > 0)
        {
            layoutBody.setSelection(selectedTabIndex(group));
        }
        layoutBody.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent event)
            {
                if (treeSection == null)
                {
                    return;
                }

                CTabItem item = layoutBody.getSelection();
                if (item != null && item.getData() != null)
                {
                    treeSection.selectNodes(true, item.getData());
                }
            }
        });
    }

    private Object selectedSource()
    {
        return selectedNode == null ? null : selectedNode.getSource();
    }

    private int selectedTabIndex(TabGroup group)
    {
        Object selectedSource = selectedSource();
        if (selectedSource == null)
        {
            return 0;
        }

        List<EJCoreLayoutItem> items = group.getItems();
        for (int i = 0; i < items.size(); i++)
        {
            if (contains(items.get(i), selectedSource))
            {
                return i;
            }
        }
        return 0;
    }

    private boolean contains(EJCoreLayoutItem item, Object selectedSource)
    {
        if (item == selectedSource)
        {
            return true;
        }
        if (item instanceof EJCoreLayoutItem.ItemContainer)
        {
            for (EJCoreLayoutItem child : ((EJCoreLayoutItem.ItemContainer) item).getItems())
            {
                if (contains(child, selectedSource))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private void updateSectionDescription()
    {
        getSection().setDescription(getSectionDescription());
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
