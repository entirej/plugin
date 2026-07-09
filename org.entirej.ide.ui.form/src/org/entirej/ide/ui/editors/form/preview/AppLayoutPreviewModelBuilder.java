/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.form.preview;

import java.util.ArrayList;
import java.util.List;

import org.entirej.framework.core.properties.EJCoreLayoutContainer;
import org.entirej.framework.core.properties.EJCoreLayoutItem;
import org.entirej.framework.core.properties.EJCoreLayoutItem.FILL;
import org.entirej.framework.core.properties.EJCoreLayoutItem.GRAB;
import org.entirej.framework.core.properties.EJCoreLayoutItem.ItemContainer;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutComponent;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.SplitGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.TabGroup;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewDescriptor;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.ide.ui.editors.preview.PreviewGridConstraint;
import org.entirej.ide.ui.editors.preview.PreviewGridLayoutSolver;
import org.entirej.ide.ui.editors.preview.PreviewNode;

public class AppLayoutPreviewModelBuilder
{
    private final FormPreviewDescriptorResolver resolver;

    public AppLayoutPreviewModelBuilder()
    {
        this(new FormPreviewDescriptorResolver());
    }

    public AppLayoutPreviewModelBuilder(FormPreviewDescriptorResolver resolver)
    {
        this.resolver = resolver;
    }

    public PreviewNode build(EJPluginEntireJProperties properties)
    {
        return build(properties, 0, 0, null);
    }

    public PreviewNode build(EJPluginEntireJProperties properties, int availableWidth, int availableHeight)
    {
        return build(properties, availableWidth, availableHeight, null);
    }

    public PreviewNode build(EJPluginEntireJProperties properties, int availableWidth, int availableHeight, Object selectedSource)
    {
        EJCoreLayoutContainer container = properties.getLayoutContainer();
        EJDevPreviewDescriptor descriptor = EJDevPreviewDescriptor.create(EJDevPreviewKind.FORM_CONTAINER, container.getTitle(), container.getTitle(),
                "applicationLayout", container.getWidth(), container.getHeight());
        PreviewNode root = new PreviewNode(container, descriptor);
        root.setColumns(container.getColumns());
        root.setCompactLayout(true);
        root.setPaintContainerTitle(false);
        root.setBounds(0, 0, size(container.getWidth(), availableWidth, 900), size(container.getHeight(), availableHeight, 650));

        addContainerChildren(properties, root, container, selectedSource);
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private void addContainerChildren(EJPluginEntireJProperties properties, PreviewNode parent, ItemContainer container, Object selectedSource)
    {
        List<EJCoreLayoutItem> items = container.getItems();
        for (EJCoreLayoutItem item : items)
        {
            PreviewNode child = createNode(properties, item, selectedSource);
            parent.addChild(child);
        }
    }

    private PreviewNode createNode(EJPluginEntireJProperties properties, EJCoreLayoutItem item, Object selectedSource)
    {
        PreviewNode node;
        if (item instanceof LayoutComponent)
        {
            node = new PreviewNode(item, forLayoutComponent((LayoutComponent) item));
        }
        else
        {
            node = new PreviewNode(item, resolver.forLayoutItem(item));
        }
        node.setConstraint(constraint(item));
        node.setCompactLayout(true);

        if (item instanceof LayoutGroup)
        {
            LayoutGroup group = (LayoutGroup) item;
            node.setColumns(group.getColumns());
            node.setPaintContainerTitle(group.getItems().isEmpty());
            // SWT zeroes the GridLayout margins for a hide-margin group.
            if (group.isHideMargin())
            {
                node.setLayoutMargin(0);
            }
            addContainerChildren(properties, node, group, selectedSource);
        }
        else if (item instanceof SplitGroup)
        {
            SplitGroup split = (SplitGroup) item;
            boolean horizontal = split.getOrientation() == SplitGroup.ORIENTATION.HORIZONTAL;
            node.setColumns(horizontal ? Math.max(1, split.getItems().size()) : 1);
            node.setVerticalOrientation(!horizontal);
            node.setPaintContainerTitle(split.getItems().isEmpty());
            addContainerChildren(properties, node, split, selectedSource);
            normalizeSplitChildren(node);
        }
        else if (item instanceof TabGroup)
        {
            TabGroup tab = (TabGroup) item;
            node.setColumns(1);
            node.setPaintContainerTitle(false);
            node.setTabsAtBottom(tab.getOrientation() != TabGroup.ORIENTATION.TOP);
            node.setTabLabels(tabLabels(tab));
            node.setSelectedTabIndex(selectedTabIndex(tab, selectedSource));
            for (EJCoreLayoutItem tabItem : tab.getItems())
            {
                node.addChild(createNode(properties, tabItem, selectedSource));
            }
        }

        return node;
    }

    /**
     * A SashForm gives every pane one slot and stretches it; spans would otherwise wrap panes onto
     * extra rows/columns. Size hints survive as sash weights.
     */
    private void normalizeSplitChildren(PreviewNode split)
    {
        for (PreviewNode child : split.getChildren())
        {
            child.getConstraint().setHorizontalSpan(1).setVerticalSpan(1).setFillHorizontal(true).setFillVertical(true).setGrabHorizontal(true)
                    .setGrabVertical(true);
        }
    }

    private EJDevPreviewDescriptor forLayoutComponent(LayoutComponent component)
    {
        String renderer = component.getRenderer();
        String label = String.format("<%s>", value(renderer, "<component>"));
        return EJDevPreviewDescriptor.create(EJDevPreviewKind.APP_COMPONENT, component.getName(), label, renderer, component.getHintWidth(),
                component.getHintHeight());
    }

    private List<String> tabLabels(TabGroup tab)
    {
        List<String> labels = new ArrayList<String>();
        for (EJCoreLayoutItem item : tab.getItems())
        {
            labels.add(value(item.getName(), item.getTitle(), "Tab"));
        }
        return labels;
    }

    private int selectedTabIndex(TabGroup tab, Object selectedSource)
    {
        if (selectedSource == null)
        {
            return 0;
        }

        List<EJCoreLayoutItem> items = tab.getItems();
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
        if (item instanceof ItemContainer)
        {
            for (EJCoreLayoutItem child : ((ItemContainer) item).getItems())
            {
                if (contains(child, selectedSource))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private PreviewGridConstraint constraint(EJCoreLayoutItem item)
    {
        return PreviewGridConstraint.defaults().setHorizontalSpan(item.getHorizontalSpan()).setVerticalSpan(item.getVerticalSpan())
                .setPreferredWidth(item.getHintWidth()).setPreferredHeight(item.getHintHeight()).setMinimumWidth(item.getMinWidth())
                .setMinimumHeight(item.getMinHeight()).setFillHorizontal(item.getFill() == FILL.HORIZONTAL || item.getFill() == FILL.BOTH)
                .setFillVertical(item.getFill() == FILL.VERTICAL || item.getFill() == FILL.BOTH)
                .setGrabHorizontal(item.getGrab() == GRAB.HORIZONTAL || item.getGrab() == GRAB.BOTH)
                .setGrabVertical(item.getGrab() == GRAB.VERTICAL || item.getGrab() == GRAB.BOTH);
    }

    private int size(int configured, int available, int fallback)
    {
        if (configured > 0)
        {
            return configured;
        }
        return available > 0 ? available : fallback;
    }

    private String value(String... values)
    {
        for (String value : values)
        {
            if (value != null && value.trim().length() > 0)
            {
                return value;
            }
        }
        return null;
    }
}
