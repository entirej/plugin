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

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
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
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;
import org.entirej.ide.ui.editors.prop.LayoutTreeSection.LayoutPreviewer;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;

public class LayoutPreviewPart extends AbstractDescriptorPart implements INodeDescriptorViewer, LayoutPreviewer
{
    private final EJPropertiesEditor editor;
    // private AbstractNode<?> selectedNode;
    private ScrolledComposite        previewComposite;

    private final Color              COLOR_LIGHT_RED    = new Color(Display.getCurrent(), new RGB(255, 170, 170));
    private final Color              COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);

    public LayoutPreviewPart(EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor.getToolkit(),  parent, true);
        this.editor = editor;
        buildUI();

    }

    @Override
    public void dispose()
    {

        super.dispose();
        COLOR_LIGHT_RED.dispose();
    }
    
    @Override
    public void setFocus()
    {
        if(getSection().isDisposed() || getSection().getClient()==null || getSection().getClient().isDisposed())
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
        // selectedNode = node;

    }

    private void previewLayout()
    {
        getSection().setRedraw(false);
        if (previewComposite != null)
        {
            previewComposite.dispose();
            previewComposite = null;
        }

        body.setLayout(new GridLayout());
        previewComposite = new ScrolledComposite(body, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);

        GridData layoutData = new GridData(GridData.FILL_BOTH);
        previewComposite.setLayoutData(layoutData);
        layoutData.widthHint = 100;
        layoutData.heightHint = 100;

        Composite pContent = new Composite(previewComposite, SWT.NONE);
        pContent.setBackground(body.getBackground());
        EJCoreLayoutContainer container = editor.getEntireJProperties().getLayoutContainer();
        int width = container.getWidth();
        int height = container.getHeight();
        previewComposite.setContent(pContent);
        previewComposite.setBackground(body.getBackground());
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

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
            switch (item.getType())
            {
                case GROUP:
                    createGroupLayout(layoutBody, (LayoutGroup) item);
                    break;
                case SPACE:
                    createSpace(layoutBody, (LayoutSpace) item);
                    break;
                case COMPONENT:
                    createComponent(layoutBody, (LayoutComponent) item);
                    break;
                case SPLIT:
                    createSplitLayout(layoutBody, (SplitGroup) item);
                    break;
                case TAB:
                    createTabLayout(layoutBody, (TabGroup) item);
                    break;
            }
        }
        body.layout();
        getSection().setRedraw(true);
        if (width > 0 && height > 0)
            previewComposite.setMinSize(width, height);
        else
            previewComposite.setMinSize(pContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

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
        
        if(gd.grabExcessHorizontalSpace && gd.widthHint==0)
        {
            gd.horizontalAlignment = SWT.FILL;
        }
        
        if(gd.grabExcessVerticalSpace && gd.heightHint==0)
        {
            gd.verticalAlignment = SWT.FILL;
        }

        return gd;
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
                switch (item.getType())
                {
                    case GROUP:
                        createGroupLayout(layoutBody, (LayoutGroup) item);
                        break;
                    case SPACE:
                        createSpace(layoutBody, (LayoutSpace) item);
                        break;
                    case COMPONENT:
                        createComponent(layoutBody, (LayoutComponent) item);
                        break;
                    case SPLIT:
                        createSplitLayout(layoutBody, (SplitGroup) item);
                        break;
                    case TAB:
                        createTabLayout(layoutBody, (TabGroup) item);
                        break;

                }
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
                weights[items.indexOf(item)] = (item.getHintWidth()) + 1;
                switch (item.getType())
                {
                    case GROUP:
                        createGroupLayout(layoutBody, (LayoutGroup) item);
                        break;
                    case SPACE:
                        createSpace(layoutBody, (LayoutSpace) item);
                        break;
                    case COMPONENT:
                        createComponent(layoutBody, (LayoutComponent) item);
                        break;
                    case SPLIT:
                        createSplitLayout(layoutBody, (SplitGroup) item);
                        break;
                    case TAB:
                        createTabLayout(layoutBody, (TabGroup) item);
                        break;

                }
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
            tabItem.setText(item.getName() != null ? item.getName() : "<title>");
            switch (item.getType())
            {
                case GROUP:
                    createGroupLayout(composite, (LayoutGroup) item);
                    break;
                case SPACE:
                    createSpace(composite, (LayoutSpace) item);
                    break;
                case COMPONENT:
                    createComponent(composite, (LayoutComponent) item);
                    break;
                case SPLIT:
                    createSplitLayout(composite, (SplitGroup) item);
                    break;
                case TAB:
                    createTabLayout(composite, (TabGroup) item);
                    break;

            }
        }
        if (items.size() > 0)
            layoutBody.setSelection(0);

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
