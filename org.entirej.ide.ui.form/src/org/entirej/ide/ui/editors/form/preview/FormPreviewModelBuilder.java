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

import java.lang.reflect.Field;
import java.util.List;

import org.entirej.framework.core.enumerations.EJCanvasSplitOrientation;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewDescriptor;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginDrawerPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginStackedPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.ide.ui.editors.preview.PreviewGridConstraint;
import org.entirej.ide.ui.editors.preview.PreviewGridLayoutSolver;
import org.entirej.ide.ui.editors.preview.PreviewNode;

public class FormPreviewModelBuilder
{
    private static final String XSPAN_PROPERTY              = "XSPAN";
    private static final String YSPAN_PROPERTY              = "YSPAN";
    private static final String EXPAND_X_PROPERTY           = "EXPAND_X";
    private static final String EXPAND_Y_PROPERTY           = "EXPAND_Y";
    private static final String DISPLAYED_WIDTH_PROPERTY    = "DISPLAYED_WIDTH";
    private static final String DISPLAYED_HEIGHT_PROPERTY   = "DISPLAYED_HEIGHT";
    private static final String LABEL_POSITION_PROPERTY     = "LABEL_POSITION";
    private static final String LABEL_POSITION_RIGHT        = "RIGHT";
    private static final String SCREEN_WIDTH_PROPERTY       = "WIDTH";
    private static final String SCREEN_HEIGHT_PROPERTY      = "HEIGHT";
    private static final String SCREEN_NUMCOLS_PROPERTY     = "NUMCOLS";
    private static final int    DEFAULT_CONTROL_WIDTH       = 120;
    private static final int    DEFAULT_CONTROL_HEIGHT      = 22;
    private static final int    DEFAULT_TEXT_AREA_HEIGHT    = 70;
    private static final int    DEFAULT_BUTTON_HEIGHT       = 24;
    private static final int    DEFAULT_SEPARATOR_HEIGHT    = 4;
    private static final int    DEFAULT_SELECTED_WIDTH      = 300;
    private static final int    DEFAULT_SELECTED_HEIGHT     = 300;

    private final FormPreviewDescriptorResolver resolver;

    public FormPreviewModelBuilder()
    {
        this(new FormPreviewDescriptorResolver());
    }

    public FormPreviewModelBuilder(FormPreviewDescriptorResolver resolver)
    {
        this.resolver = resolver;
    }

    public PreviewNode build(EJPluginFormProperties form)
    {
        return buildCanvasPreview(form, null);
    }

    public PreviewNode build(EJPluginFormProperties form, Object selectedSource)
    {
        return build(form, selectedSource, 0, 0);
    }

    public PreviewNode build(EJPluginFormProperties form, Object selectedSource, int availableWidth, int availableHeight)
    {
        PreviewNode selectedPreview = buildSelectedPreview(form, selectedSource, availableWidth, availableHeight);
        return selectedPreview == null ? buildCanvasPreview(form, selectedSource) : selectedPreview;
    }

    private PreviewNode buildCanvasPreview(EJPluginFormProperties form, Object selectedSource)
    {
        EJDevPreviewDescriptor descriptor = EJDevPreviewDescriptor.create(EJDevPreviewKind.FORM_CONTAINER, form.getName(), form.getTitle(),
                form.getFormRendererName(), form.getFormWidth(), form.getFormHeight());
        PreviewNode root = new PreviewNode(form, descriptor);
        root.setColumns(form.getNumCols());
        root.setBounds(0, 0, size(form.getFormWidth(), 900), size(form.getFormHeight(), 650));
        root.setCompactLayout(true);
        root.setPaintContainerTitle(false);
        root.setPaintBorder(false);

        addCanvasContainer(root, form.getCanvasContainer(), selectedSource);
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode buildSelectedPreview(EJPluginFormProperties form, Object selectedSource, int availableWidth, int availableHeight)
    {
        if (selectedSource == null || selectedSource instanceof EJPluginFormProperties)
        {
            return null;
        }

        if (selectedSource instanceof EJPluginCanvasProperties)
        {
            EJPluginCanvasProperties canvas = (EJPluginCanvasProperties) selectedSource;
            return canvas.getType() == EJCanvasType.POPUP ? buildPopupCanvasPreview(form, canvas) : null;
        }

        if (selectedSource instanceof EJPluginScreenItemProperties)
        {
            EJPluginItemGroupProperties itemGroup = ((EJPluginScreenItemProperties) selectedSource).getItemGroupProperties();
            return buildContainerScreenPreview(form, selectedSource, itemGroup == null ? null : itemGroup.getParentItemGroupContainer(), availableWidth,
                    availableHeight);
        }
        if (selectedSource instanceof EJPluginItemGroupProperties)
        {
            EJPluginItemGroupProperties itemGroup = (EJPluginItemGroupProperties) selectedSource;
            return buildContainerScreenPreview(form, selectedSource, itemGroup.getParentItemGroupContainer(), availableWidth, availableHeight);
        }
        if (selectedSource instanceof EJPluginMainScreenProperties)
        {
            return buildMainScreenPreview(form, selectedSource, ((EJPluginMainScreenProperties) selectedSource).getBlockProperties(), availableWidth,
                    availableHeight);
        }
        if (selectedSource instanceof EJPluginBlockProperties)
        {
            return buildMainScreenPreview(form, selectedSource, (EJPluginBlockProperties) selectedSource, availableWidth, availableHeight);
        }

        EJPluginItemGroupProperties itemGroup = reflectedItemGroup(selectedSource);
        if (itemGroup != null)
        {
            return buildContainerScreenPreview(form, selectedSource, itemGroup.getParentItemGroupContainer(), availableWidth, availableHeight);
        }

        EJPluginMainScreenProperties mainScreen = reflectedMainScreen(selectedSource);
        if (mainScreen != null)
        {
            return buildMainScreenPreview(form, selectedSource, mainScreen.getBlockProperties(), availableWidth, availableHeight);
        }

        EJPluginItemGroupContainer container = reflectedItemGroupContainer(selectedSource);
        if (container != null)
        {
            return buildContainerScreenPreview(form, selectedSource, container, availableWidth, availableHeight);
        }
        return null;
    }

    private PreviewNode buildMainScreenPreview(EJPluginFormProperties form, Object selectedSource, EJPluginBlockProperties block, int availableWidth,
            int availableHeight)
    {
        if (block == null || block.getMainScreenProperties() == null)
        {
            return null;
        }

        EJDevPreviewDescriptor blockDescriptor = resolver.forBlock(block);
        if (blockDescriptor.getKind() != EJDevPreviewKind.BLOCK)
        {
            int preferredWidth = size(block.getMainScreenProperties().getWidth(), blockDescriptor.getPreferredWidth());
            int preferredHeight = size(block.getMainScreenProperties().getHeight(), blockDescriptor.getPreferredHeight());
            return buildRendererPlaceholderPreview(form, source(selectedSource, block), blockDescriptor, preferredWidth, preferredHeight, availableWidth,
                    availableHeight);
        }

        PreviewNode root = createSelectedRoot(form, source(selectedSource, block), block.getMainScreenProperties().getNumCols(), block
                .getMainScreenProperties().getWidth(), block.getMainScreenProperties().getHeight(), availableWidth, availableHeight);
        addFlatItemGroups(root, block.getMainScreenItemGroupDisplayContainer());
        if (root.getChildren().isEmpty())
        {
            return null;
        }
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode buildContainerScreenPreview(EJPluginFormProperties form, Object selectedSource, EJPluginItemGroupContainer container,
            int availableWidth, int availableHeight)
    {
        container = rootContainer(container);
        if (container == null)
        {
            return null;
        }

        EJPluginBlockProperties block = container.getBlockProperties();
        if (block == null)
        {
            return null;
        }

        switch (container.getContainerType())
        {
            case EJPluginItemGroupContainer.MAIN_SCREEN:
                return buildMainScreenPreview(form, selectedSource, block, availableWidth, availableHeight);
            case EJPluginItemGroupContainer.INSERT_SCREEN:
                return buildExtensionScreenPreview(form, selectedSource, block, resolver.forInsertScreen(block), block.getInsertScreenRendererProperties(),
                        container, availableWidth, availableHeight);
            case EJPluginItemGroupContainer.UPDATE_SCREEN:
                return buildExtensionScreenPreview(form, selectedSource, block, resolver.forUpdateScreen(block), block.getUpdateScreenRendererProperties(),
                        container, availableWidth, availableHeight);
            case EJPluginItemGroupContainer.QUERY_SCREEN:
                return buildExtensionScreenPreview(form, selectedSource, block, resolver.forQueryScreen(block), block.getQueryScreenRendererProperties(),
                        container, availableWidth, availableHeight);
            default:
                return buildItemGroupContainerPreview(form, selectedSource, container);
        }
    }

    private PreviewNode buildExtensionScreenPreview(EJPluginFormProperties form, Object selectedSource, EJPluginBlockProperties block,
            EJDevPreviewDescriptor descriptor, EJFrameworkExtensionProperties rendererProperties, EJPluginItemGroupContainer container, int availableWidth,
            int availableHeight)
    {
        int width = intProperty(rendererProperties, SCREEN_WIDTH_PROPERTY, size(descriptor.getPreferredWidth(), DEFAULT_SELECTED_WIDTH));
        int height = intProperty(rendererProperties, SCREEN_HEIGHT_PROPERTY, size(descriptor.getPreferredHeight(), DEFAULT_SELECTED_HEIGHT));
        PreviewNode root = createSelectedRoot(form, source(selectedSource, block), screenColumns(rendererProperties), width, height, availableWidth,
                availableHeight);
        addFlatItemGroups(root, container);
        if (root.getChildren().isEmpty())
        {
            return buildRendererPlaceholderPreview(form, source(selectedSource, block), descriptor, width, height, availableWidth, availableHeight);
        }
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode buildRendererPlaceholderPreview(EJPluginFormProperties form, Object source, EJDevPreviewDescriptor descriptor, int preferredWidth,
            int preferredHeight, int availableWidth, int availableHeight)
    {
        PreviewNode root = createSelectedRoot(form, source, 1, preferredWidth, preferredHeight, availableWidth, availableHeight);
        PreviewNode rendererNode = new PreviewNode(source, descriptor);
        rendererNode.setPaintBorder(false);
        rendererNode.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(1).setVerticalSpan(1).setPreferredWidth(preferredWidth)
                .setPreferredHeight(preferredHeight).setFillHorizontal(true).setFillVertical(true).setGrabHorizontal(true).setGrabVertical(true));
        root.addChild(rendererNode);
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode buildPopupCanvasPreview(EJPluginFormProperties form, EJPluginCanvasProperties popup)
    {
        EJDevPreviewDescriptor descriptor = EJDevPreviewDescriptor.create(EJDevPreviewKind.FORM_CONTAINER, popup.getName(),
                value(popup.getPopupPageTitle(), popup.getName()), "popup", popup.getWidth(), popup.getHeight());
        PreviewNode root = new PreviewNode(popup, descriptor);
        root.setColumns(popup.getNumCols());
        root.setBounds(0, 0, size(popup.getWidth(), size(form.getFormWidth(), 900)), size(popup.getHeight(), size(form.getFormHeight(), 650)));
        root.setCompactLayout(true);
        root.setPaintContainerTitle(false);
        root.setPaintBorder(false);

        addCanvasContainer(root, popup.getPopupCanvasContainer(), popup);
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode buildItemGroupPreview(EJPluginFormProperties form, EJPluginItemGroupProperties itemGroup)
    {
        if (itemGroup == null)
        {
            return null;
        }

        PreviewNode root = createFlatRoot(form, itemGroup.getBlockProperties(), 1);
        root.addChild(createFlatItemGroupNode(itemGroup));
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode buildItemGroupContainerPreview(EJPluginFormProperties form, Object selectedSource, EJPluginItemGroupContainer container)
    {
        if (container == null)
        {
            return null;
        }

        PreviewNode root = createFlatRoot(form, selectedSource, 1);
        addFlatItemGroups(root, container);
        if (root.getChildren().isEmpty())
        {
            return null;
        }
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode createFlatRoot(EJPluginFormProperties form, Object source, int columns)
    {
        EJDevPreviewDescriptor descriptor = EJDevPreviewDescriptor.create(EJDevPreviewKind.FORM_CONTAINER, form.getName(), form.getTitle(),
                form.getFormRendererName(), form.getFormWidth(), form.getFormHeight());
        PreviewNode root = new PreviewNode(source, descriptor);
        root.setColumns(columns);
        root.setBounds(0, 0, size(form.getFormWidth(), 900), size(form.getFormHeight(), 650));
        root.setCompactLayout(true);
        root.setPaintContainerTitle(false);
        root.setPaintBorder(false);
        return root;
    }

    private PreviewNode createSelectedRoot(EJPluginFormProperties form, Object source, int columns, int configuredWidth, int configuredHeight,
            int availableWidth, int availableHeight)
    {
        EJDevPreviewDescriptor descriptor = EJDevPreviewDescriptor.create(EJDevPreviewKind.FORM_CONTAINER, form.getName(), form.getTitle(),
                form.getFormRendererName(), configuredWidth, configuredHeight);
        PreviewNode root = new PreviewNode(source, descriptor);
        root.setColumns(columns);
        root.setBounds(0, 0, selectedSize(configuredWidth, availableWidth, size(form.getFormWidth(), DEFAULT_SELECTED_WIDTH)), selectedSize(
                configuredHeight, availableHeight, size(form.getFormHeight(), DEFAULT_SELECTED_HEIGHT)));
        root.setCompactLayout(true);
        root.setPaintContainerTitle(false);
        root.setPaintBorder(false);
        return root;
    }

    private EJPluginItemGroupContainer rootContainer(EJPluginItemGroupContainer container)
    {
        while (container != null && container.getParentItemGroup() != null)
        {
            container = container.getParentItemGroup().getParentItemGroupContainer();
        }
        return container;
    }

    private Object source(Object selectedSource, Object fallback)
    {
        return selectedSource == null ? fallback : selectedSource;
    }

    private void addFlatItemGroups(PreviewNode parent, EJPluginItemGroupContainer container)
    {
        if (container == null)
        {
            return;
        }
        for (EJPluginItemGroupProperties itemGroup : container.getItemGroups())
        {
            parent.addChild(createFlatItemGroupNode(itemGroup));
        }
    }

    private PreviewNode createFlatItemGroupNode(EJPluginItemGroupProperties group)
    {
        PreviewNode node = new PreviewNode(group, resolver.forItemGroup(group));
        node.setColumns(group.getNumCols());
        node.setLayoutGap(0);
        node.setCompactLayout(true);
        node.setPaintBorder(group.dispayGroupFrame());
        node.setPaintContainerTitle(group.dispayGroupFrame());
        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(group.getXspan()).setVerticalSpan(group.getYspan())
                .setPreferredWidth(group.getWidth()).setPreferredHeight(group.getHeight()).setFillHorizontal(group.canExpandHorizontally())
                .setFillVertical(group.canExpandVertically()).setGrabHorizontal(group.canExpandHorizontally()).setGrabVertical(group.canExpandVertically()));

        for (EJScreenItemProperties itemProperties : group.getAllItemProperties())
        {
            if (itemProperties instanceof EJPluginScreenItemProperties)
            {
                addFlatScreenItemNodes(node, (EJPluginScreenItemProperties) itemProperties);
            }
        }
        addFlatItemGroups(node, group.getChildItemGroupContainer());
        return node;
    }

    private void addFlatScreenItemNodes(PreviewNode parent, EJPluginScreenItemProperties item)
    {
        if (!item.isVisible())
        {
            return;
        }

        EJFrameworkExtensionProperties requiredProperties = requiredProperties(item);
        boolean hasLabel = hasLabel(item);
        boolean separateLabel = hasLabel && usesSeparateLabel(item);
        boolean labelAfter = LABEL_POSITION_RIGHT.equalsIgnoreCase(stringProperty(requiredProperties, LABEL_POSITION_PROPERTY));

        PreviewNode labelNode = separateLabel ? createFlatLabelNode(item, requiredProperties) : null;
        PreviewNode controlNode = createFlatControlNode(item, requiredProperties, separateLabel ? Math.max(1, parent.getColumns() - 1) : parent
                .getColumns());

        if (labelAfter)
        {
            parent.addChild(controlNode);
            if (labelNode != null)
            {
                parent.addChild(labelNode);
            }
        }
        else
        {
            if (labelNode != null)
            {
                parent.addChild(labelNode);
            }
            parent.addChild(controlNode);
        }
    }

    private PreviewNode createFlatLabelNode(EJPluginScreenItemProperties item, EJFrameworkExtensionProperties requiredProperties)
    {
        PreviewNode node = new PreviewNode(item, EJDevPreviewDescriptor.create(EJDevPreviewKind.LABEL, item.getName() + ".label",
                value(item.getLabel(), item.getReferencedItemName()), "label", 0, 0));
        node.setPaintBorder(false);
        node.setPaintControlLabel(false);
        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(1).setVerticalSpan(intProperty(requiredProperties, YSPAN_PROPERTY, 1))
                .setPreferredWidth(labelWidth(item)).setPreferredHeight(DEFAULT_CONTROL_HEIGHT).setFillHorizontal(false).setFillVertical(false)
                .setGrabHorizontal(false).setGrabVertical(false));
        return node;
    }

    private PreviewNode createFlatControlNode(EJPluginScreenItemProperties item, EJFrameworkExtensionProperties requiredProperties, int maximumSpan)
    {
        PreviewNode node = new PreviewNode(item, resolver.forScreenItem(item));
        node.setPaintBorder(false);
        node.setPaintControlLabel(false);

        int horizontalSpan = Math.min(Math.max(1, maximumSpan), intProperty(requiredProperties, XSPAN_PROPERTY, 1));
        int verticalSpan = intProperty(requiredProperties, YSPAN_PROPERTY, 1);
        boolean expandHorizontally = booleanProperty(requiredProperties, EXPAND_X_PROPERTY, false);
        boolean expandVertically = booleanProperty(requiredProperties, EXPAND_Y_PROPERTY, false);
        int preferredWidth = preferredControlWidth(item, requiredProperties, expandHorizontally);
        int preferredHeight = preferredControlHeight(item, requiredProperties);

        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(horizontalSpan).setVerticalSpan(verticalSpan)
                .setPreferredWidth(preferredWidth).setPreferredHeight(preferredHeight).setFillHorizontal(expandHorizontally)
                .setFillVertical(expandVertically).setGrabHorizontal(expandHorizontally).setGrabVertical(expandVertically));
        return node;
    }

    private void addCanvasContainer(PreviewNode parent, EJPluginCanvasContainer container, Object selectedSource)
    {
        if (container == null)
        {
            return;
        }
        for (EJPluginCanvasProperties canvas : container.getCanvasProperties())
        {
            if (canvas.getType() == EJCanvasType.POPUP)
            {
                continue;
            }
            parent.addChild(createCanvasNode(canvas, selectedSource));
        }
    }

    private PreviewNode createCanvasNode(EJPluginCanvasProperties canvas, Object selectedSource)
    {
        PreviewNode node = new PreviewNode(canvas, canvasDescriptor(canvas));
        node.setColumns(canvas.getNumCols());
        node.setConstraint(canvasConstraint(canvas));
        node.setCompactLayout(true);

        EJCanvasType type = canvas.getType();
        if (type == EJCanvasType.BLOCK || type == EJCanvasType.FORM)
        {
            return node;
        }
        else if (type == EJCanvasType.GROUP)
        {
            addCanvasContainer(node, canvas.getGroupCanvasContainer(), selectedSource);
        }
        else if (type == EJCanvasType.SPLIT)
        {
            node.setColumns(canvas.getSplitOrientation() == EJCanvasSplitOrientation.HORIZONTAL ? Math.max(1, canvas.getSplitCanvasContainer()
                    .getCanvasProperties().size()) : 1);
            addCanvasContainer(node, canvas.getSplitCanvasContainer(), selectedSource);
        }
        else if (type == EJCanvasType.POPUP)
        {
            addCanvasContainer(node, canvas.getPopupCanvasContainer(), selectedSource);
        }
        else if (type == EJCanvasType.TAB)
        {
            addTabPages(node, canvas, selectedSource);
        }
        else if (type == EJCanvasType.DRAWER)
        {
            addDrawerPages(node, canvas);
        }
        else if (type == EJCanvasType.STACKED)
        {
            addStackedPages(node, canvas, selectedSource);
        }

        return node;
    }

    private EJDevPreviewDescriptor canvasDescriptor(EJPluginCanvasProperties canvas)
    {
        if (canvas.getType() == EJCanvasType.BLOCK)
        {
            return canvasBlockDescriptor(canvas);
        }
        if (canvas.getType() == EJCanvasType.FORM)
        {
            return canvasFormDescriptor(canvas);
        }
        return resolver.forCanvas(canvas);
    }

    private EJDevPreviewDescriptor canvasBlockDescriptor(EJPluginCanvasProperties canvas)
    {
        EJPluginBlockProperties block = canvas.getPluginBlockProperties();
        String blockName = block == null ? null : block.getName();
        String rendererName = block == null ? null : block.getBlockRendererName();
        String label = String.format("<%s>", value(blockName, "<block>"));
        return EJDevPreviewDescriptor.create(EJDevPreviewKind.APP_COMPONENT, value(blockName, canvas.getName()), label, rendererName, 0, 0);
    }

    private EJDevPreviewDescriptor canvasFormDescriptor(EJPluginCanvasProperties canvas)
    {
        String formName = value(canvas.getReferredFormId(), canvas.getName(), "form");
        return EJDevPreviewDescriptor.create(EJDevPreviewKind.APP_COMPONENT, canvas.getName(), String.format("<%s>", formName), "FORM", canvas.getWidth(),
                canvas.getHeight());
    }

    private PreviewNode createBlockNode(EJPluginBlockProperties block)
    {
        PreviewNode node = new PreviewNode(block, resolver.forBlock(block));
        EJPluginMainScreenProperties mainScreen = block.getMainScreenProperties();
        node.setColumns(mainScreen.getNumCols());
        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(mainScreen.getHorizontalSpan()).setVerticalSpan(mainScreen.getVerticalSpan())
                .setPreferredWidth(mainScreen.getWidth()).setPreferredHeight(mainScreen.getHeight()).setFillHorizontal(mainScreen.canExpandHorizontally())
                .setFillVertical(mainScreen.canExpandVertically()).setGrabHorizontal(mainScreen.canExpandHorizontally())
                .setGrabVertical(mainScreen.canExpandVertically()));

        addItemGroups(node, block.getMainScreenItemGroupDisplayContainer());
        addBlockScreens(node, block);
        return node;
    }

    private void addBlockScreens(PreviewNode blockNode, EJPluginBlockProperties block)
    {
        if (block.isInsertAllowed() && !block.getInsertScreenItemGroupDisplayContainer().isEmpty())
        {
            blockNode.addChild(createScreenNode(block, resolver.forInsertScreen(block), block.getInsertScreenRendererProperties(),
                    block.getInsertScreenItemGroupDisplayContainer()));
        }
        if (block.isUpdateAllowed() && !block.getUpdateScreenItemGroupDisplayContainer().isEmpty())
        {
            blockNode.addChild(createScreenNode(block, resolver.forUpdateScreen(block), block.getUpdateScreenRendererProperties(),
                    block.getUpdateScreenItemGroupDisplayContainer()));
        }
        if (block.isQueryAllowed() && !block.getQueryScreenItemGroupDisplayContainer().isEmpty())
        {
            blockNode.addChild(createScreenNode(block, resolver.forQueryScreen(block), block.getQueryScreenRendererProperties(),
                    block.getQueryScreenItemGroupDisplayContainer()));
        }
    }

    private PreviewNode createScreenNode(EJPluginBlockProperties block, EJDevPreviewDescriptor descriptor, EJFrameworkExtensionProperties rendererProperties,
            EJPluginItemGroupContainer container)
    {
        PreviewNode node = new PreviewNode(block, descriptor);
        node.setColumns(screenColumns(rendererProperties));
        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(1).setVerticalSpan(1)
                .setPreferredWidth(descriptor.getPreferredWidth()).setPreferredHeight(descriptor.getPreferredHeight()).setFillHorizontal(true)
                .setFillVertical(false).setGrabHorizontal(true).setGrabVertical(false));
        addItemGroups(node, container);
        return node;
    }

    private void addItemGroups(PreviewNode parent, EJPluginItemGroupContainer container)
    {
        List<EJPluginItemGroupProperties> itemGroups = container.getItemGroups();
        for (EJPluginItemGroupProperties group : itemGroups)
        {
            parent.addChild(createItemGroupNode(group));
        }
    }

    private PreviewNode createItemGroupNode(EJPluginItemGroupProperties group)
    {
        PreviewNode node = new PreviewNode(group, resolver.forItemGroup(group));
        node.setColumns(group.getNumCols());
        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(group.getXspan()).setVerticalSpan(group.getYspan())
                .setPreferredWidth(group.getWidth()).setPreferredHeight(group.getHeight()).setFillHorizontal(group.canExpandHorizontally())
                .setFillVertical(group.canExpandVertically()).setGrabHorizontal(group.canExpandHorizontally()).setGrabVertical(group.canExpandVertically()));

        for (EJScreenItemProperties itemProperties : group.getAllItemProperties())
        {
            if (itemProperties instanceof EJPluginScreenItemProperties)
            {
                node.addChild(createScreenItemNode((EJPluginScreenItemProperties) itemProperties));
            }
        }
        addItemGroups(node, group.getChildItemGroupContainer());
        return node;
    }

    private PreviewNode createScreenItemNode(EJPluginScreenItemProperties item)
    {
        return new PreviewNode(item, resolver.forScreenItem(item));
    }

    private boolean hasLabel(EJPluginScreenItemProperties item)
    {
        return item.getLabel() != null && item.getLabel().trim().length() > 0;
    }

    private boolean usesSeparateLabel(EJPluginScreenItemProperties item)
    {
        if (item.isSpacerItem())
        {
            return false;
        }

        EJDevPreviewKind kind = resolver.forScreenItem(item).getKind();
        switch (kind)
        {
            case BUTTON:
            case CHECKBOX:
            case RADIO_GROUP:
            case LABEL:
                return false;
            default:
                return true;
        }
    }

    private int preferredControlWidth(EJPluginScreenItemProperties item, EJFrameworkExtensionProperties requiredProperties, boolean expandHorizontally)
    {
        int displayedWidth = intProperty(requiredProperties, DISPLAYED_WIDTH_PROPERTY, 0);
        if (displayedWidth > 0)
        {
            return displayedWidth * 8;
        }
        if (item.isSpacerItem())
        {
            return item.isSeparator() ? DEFAULT_CONTROL_WIDTH : 1;
        }

        EJDevPreviewKind kind = resolver.forScreenItem(item).getKind();
        if (kind == EJDevPreviewKind.BUTTON)
        {
            return Math.max(34, textWidth(value(item.getLabel(), item.getReferencedItemName())) + 14);
        }
        return expandHorizontally ? DEFAULT_CONTROL_WIDTH : Math.max(DEFAULT_CONTROL_WIDTH, textWidth(item.getReferencedItemName()) + 20);
    }

    private int preferredControlHeight(EJPluginScreenItemProperties item, EJFrameworkExtensionProperties requiredProperties)
    {
        int displayedHeight = intProperty(requiredProperties, DISPLAYED_HEIGHT_PROPERTY, 0);
        if (displayedHeight > 0)
        {
            return displayedHeight * 18;
        }
        if (item.isSpacerItem())
        {
            return item.isSeparator() ? DEFAULT_SEPARATOR_HEIGHT : 1;
        }

        EJDevPreviewKind kind = resolver.forScreenItem(item).getKind();
        switch (kind)
        {
            case TEXT_AREA:
            case HTML:
                return DEFAULT_TEXT_AREA_HEIGHT;
            case BUTTON:
                return DEFAULT_BUTTON_HEIGHT;
            default:
                return DEFAULT_CONTROL_HEIGHT;
        }
    }

    private int labelWidth(EJPluginScreenItemProperties item)
    {
        return Math.max(20, textWidth(value(item.getLabel(), item.getReferencedItemName())) + 4);
    }

    private int textWidth(String text)
    {
        return text == null ? 0 : text.length() * 7;
    }

    private EJFrameworkExtensionProperties requiredProperties(EJPluginScreenItemProperties item)
    {
        if (item instanceof EJPluginMainScreenItemProperties)
        {
            return ((EJPluginMainScreenItemProperties) item).getBlockRendererRequiredProperties();
        }
        if (item instanceof EJPluginInsertScreenItemProperties)
        {
            return ((EJPluginInsertScreenItemProperties) item).getInsertScreenRendererRequiredProperties();
        }
        if (item instanceof EJPluginUpdateScreenItemProperties)
        {
            return ((EJPluginUpdateScreenItemProperties) item).getUpdateScreenRendererRequiredProperties();
        }
        if (item instanceof EJPluginQueryScreenItemProperties)
        {
            return ((EJPluginQueryScreenItemProperties) item).getQueryScreenRendererRequiredProperties();
        }
        return item.getBlockRendererRequiredProperties();
    }

    private int intProperty(EJFrameworkExtensionProperties properties, String name, int defaultValue)
    {
        return properties == null ? defaultValue : properties.getIntProperty(name, defaultValue);
    }

    private boolean booleanProperty(EJFrameworkExtensionProperties properties, String name, boolean defaultValue)
    {
        return properties == null ? defaultValue : properties.getBooleanProperty(name, defaultValue);
    }

    private String stringProperty(EJFrameworkExtensionProperties properties, String name)
    {
        return properties == null ? null : properties.getStringProperty(name);
    }

    private EJPluginItemGroupProperties reflectedItemGroup(Object selectedSource)
    {
        Object properties = reflectedFieldValue(selectedSource, "properties");
        return properties instanceof EJPluginItemGroupProperties ? (EJPluginItemGroupProperties) properties : null;
    }

    private EJPluginMainScreenProperties reflectedMainScreen(Object selectedSource)
    {
        Object properties = reflectedFieldValue(selectedSource, "properties");
        return properties instanceof EJPluginMainScreenProperties ? (EJPluginMainScreenProperties) properties : null;
    }

    private EJPluginItemGroupContainer reflectedItemGroupContainer(Object selectedSource)
    {
        Object container = reflectedFieldValue(selectedSource, "container");
        return container instanceof EJPluginItemGroupContainer ? (EJPluginItemGroupContainer) container : null;
    }

    private Object reflectedFieldValue(Object source, String fieldName)
    {
        if (source == null)
        {
            return null;
        }

        Class<?> type = source.getClass();
        while (type != null)
        {
            try
            {
                Field field = type.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field.get(source);
            }
            catch (NoSuchFieldException e)
            {
                type = type.getSuperclass();
            }
            catch (IllegalAccessException e)
            {
                return null;
            }
            catch (SecurityException e)
            {
                return null;
            }
        }
        return null;
    }

    private void addTabPages(PreviewNode node, EJPluginCanvasProperties canvas, Object selectedSource)
    {
        java.util.ArrayList<String> labels = new java.util.ArrayList<String>();
        int selectedIndex = 0;
        int index = 0;
        for (EJPluginTabPageProperties page : canvas.getTabPageContainer().getTabPageProperties())
        {
            labels.add(pageLabel(page.getPageTitle(), page.getName()));
            if (contains(page, selectedSource))
            {
                selectedIndex = index;
            }

            PreviewNode pageNode = new PreviewNode(page, EJDevPreviewDescriptor.create(EJDevPreviewKind.GROUP, page.getName(),
                    pageLabel(page.getPageTitle(), page.getName()), "tabPage", 0, 0));
            pageNode.setColumns(page.getNumCols());
            pageNode.setCompactLayout(true);
            pageNode.setPaintContainerTitle(false);
            pageNode.setPaintBorder(false);
            addCanvasContainer(pageNode, page.getContainedCanvases(), selectedSource);
            node.addChild(pageNode);
            index++;
        }
        node.setTabLabels(labels);
        node.setSelectedTabIndex(selectedIndex);
    }

    private void addDrawerPages(PreviewNode node, EJPluginCanvasProperties canvas)
    {
        java.util.ArrayList<String> labels = new java.util.ArrayList<String>();
        for (EJPluginDrawerPageProperties page : canvas.getDrawerPageContainer().getDrawerPageProperties())
        {
            labels.add(pageLabel(page.getPageTitle(), page.getName()));
        }
        node.setTabLabels(labels);
    }

    private void addStackedPages(PreviewNode node, EJPluginCanvasProperties canvas, Object selectedSource)
    {
        java.util.ArrayList<String> labels = new java.util.ArrayList<String>();
        int selectedIndex = 0;
        int index = 0;
        for (EJPluginStackedPageProperties page : canvas.getStackedPageContainer().getStackedPageProperties())
        {
            labels.add("<page>: " + page.getName());
            if (contains(page, selectedSource))
            {
                selectedIndex = index;
            }

            PreviewNode pageNode = new PreviewNode(page, EJDevPreviewDescriptor.create(EJDevPreviewKind.GROUP, page.getName(),
                    "<page>: " + page.getName(), "stackedPage", 0, 0));
            pageNode.setColumns(page.getNumCols());
            pageNode.setCompactLayout(true);
            pageNode.setPaintContainerTitle(false);
            pageNode.setPaintBorder(false);
            addCanvasContainer(pageNode, page.getContainedCanvases(), selectedSource);
            node.addChild(pageNode);
            index++;
        }
        node.setTabLabels(labels);
        node.setSelectedTabIndex(selectedIndex);
    }

    private boolean contains(EJPluginTabPageProperties page, Object selectedSource)
    {
        return matches(page, selectedSource) || contains(page.getContainedCanvases(), selectedSource);
    }

    private boolean contains(EJPluginStackedPageProperties page, Object selectedSource)
    {
        return matches(page, selectedSource) || contains(page.getContainedCanvases(), selectedSource);
    }

    private boolean contains(EJPluginCanvasContainer container, Object selectedSource)
    {
        if (selectedSource == null || container == null)
        {
            return false;
        }

        for (EJPluginCanvasProperties canvas : container.getCanvasProperties())
        {
            if (contains(canvas, selectedSource))
            {
                return true;
            }
        }
        return false;
    }

    private boolean contains(EJPluginCanvasProperties canvas, Object selectedSource)
    {
        if (matches(canvas, selectedSource))
        {
            return true;
        }
        if (matches(canvas.getPluginBlockProperties(), selectedSource))
        {
            return true;
        }

        EJCanvasType type = canvas.getType();
        if (type == EJCanvasType.GROUP)
        {
            return contains(canvas.getGroupCanvasContainer(), selectedSource);
        }
        if (type == EJCanvasType.SPLIT)
        {
            return contains(canvas.getSplitCanvasContainer(), selectedSource);
        }
        if (type == EJCanvasType.POPUP)
        {
            return contains(canvas.getPopupCanvasContainer(), selectedSource);
        }
        if (type == EJCanvasType.TAB)
        {
            for (EJPluginTabPageProperties page : canvas.getTabPageContainer().getTabPageProperties())
            {
                if (contains(page, selectedSource))
                {
                    return true;
                }
            }
        }
        if (type == EJCanvasType.STACKED)
        {
            for (EJPluginStackedPageProperties page : canvas.getStackedPageContainer().getStackedPageProperties())
            {
                if (contains(page, selectedSource))
                {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean matches(Object source, Object selectedSource)
    {
        if (source == null || selectedSource == null)
        {
            return false;
        }
        if (source == selectedSource)
        {
            return true;
        }
        return source == reflectedFieldValue(selectedSource, "properties");
    }

    private PreviewGridConstraint canvasConstraint(EJPluginCanvasProperties canvas)
    {
        if (canvas.getType() == EJCanvasType.BLOCK && canvas.getPluginBlockProperties() != null
                && canvas.getPluginBlockProperties().getMainScreenProperties() != null)
        {
            EJPluginMainScreenProperties screen = canvas.getPluginBlockProperties().getMainScreenProperties();
            return PreviewGridConstraint.defaults().setHorizontalSpan(screen.getHorizontalSpan()).setVerticalSpan(screen.getVerticalSpan())
                    .setPreferredWidth(screen.getWidth()).setPreferredHeight(screen.getHeight()).setMinimumWidth(screen.canExpandHorizontally() ? screen
                            .getWidth() : 0).setMinimumHeight(screen.canExpandVertically() ? screen.getHeight() : 0)
                    .setFillHorizontal(true).setFillVertical(true).setGrabHorizontal(screen.canExpandHorizontally())
                    .setGrabVertical(screen.canExpandVertically());
        }
        return PreviewGridConstraint.defaults().setHorizontalSpan(canvas.getHorizontalSpan()).setVerticalSpan(canvas.getVerticalSpan())
                .setPreferredWidth(canvas.getWidth()).setPreferredHeight(canvas.getHeight()).setFillHorizontal(canvas.canExpandHorizontally())
                .setFillVertical(canvas.canExpandVertically()).setGrabHorizontal(canvas.canExpandHorizontally()).setGrabVertical(canvas.canExpandVertically());
    }

    private int size(int configured, int fallback)
    {
        return configured > 0 ? configured : fallback;
    }

    private int selectedSize(int configured, int available, int fallback)
    {
        int result = configured > 0 ? configured : fallback;
        return available > 0 ? Math.max(result, available) : result;
    }

    private int screenColumns(EJFrameworkExtensionProperties rendererProperties)
    {
        return Math.max(1, rendererProperties == null ? 1 : rendererProperties.getIntProperty(SCREEN_NUMCOLS_PROPERTY, 1));
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

    private String pageLabel(String pageTitle, String pageName)
    {
        return value(pageTitle, "<title>: " + pageName);
    }
}
