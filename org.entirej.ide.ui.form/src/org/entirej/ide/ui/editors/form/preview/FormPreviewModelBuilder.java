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
import java.util.ArrayList;
import java.util.List;

import org.entirej.framework.core.enumerations.EJCanvasSplitOrientation;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.enumerations.EJItemGroupAlignment;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyList;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionPropertyListEntry;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewDescriptor;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginDrawerPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
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
    private static final String LABEL_ORIENTATION_PROPERTY  = "LABEL_ORIENTATION";
    private static final String ORIENTATION_RIGHT           = "RIGHT";
    private static final String ORIENTATION_CENTER          = "CENTER";
    private static final String COLUMN_ALIGNMENT_PROPERTY   = "COL_ALLIGN";
    private static final String TITLE_BAR_GROUP             = "TITLE_BAR";
    private static final String TITLE_BAR_TITLE_PROPERTY    = "TITLE";
    private static final String TITLE_BAR_MODE_PROPERTY     = "TITLE_BAR_MODE";
    private static final String TITLE_BAR_EXPANDED_PROPERTY = "TITLE_BAR_EXPANDED";
    private static final String TITLE_BAR_MODE_GROUP        = "GROUP";
    private static final String TITLE_BAR_MODE_TWISTIE      = "TWISTIE";
    private static final String TITLE_BAR_MODE_TREE_NODE    = "TREE_NODE";
    private static final String RADIO_BUTTONS_LIST          = "RADIO_BUTTONS";
    private static final String RADIO_LABEL_PROPERTY        = "LABEL";
    private static final String SHOW_BORDER_PROPERTY        = "SHOW_BORDER";
    private static final String ORIENTATION_PROPERTY        = "ORIENTATION";
    private static final String ORIENTATION_HORIZONTAL      = "HORIZONTAL";
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
    /** Approximates the average character width/height the SWT preview derives from the font. */
    private static final int    CHAR_WIDTH                  = 8;
    private static final int    CHAR_HEIGHT                 = 18;

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
        if (selectedSource == null || selectedSource == form)
        {
            return null;
        }

        // An object group is itself a form (EJPluginObjectGroupProperties extends
        // EJPluginFormProperties); preview its own canvases, not the enclosing form's.
        if (selectedSource instanceof EJPluginFormProperties)
        {
            return buildCanvasPreview((EJPluginFormProperties) selectedSource, selectedSource);
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
        if (selectedSource instanceof EJPluginLovDefinitionProperties)
        {
            EJPluginLovDefinitionProperties lov = (EJPluginLovDefinitionProperties) selectedSource;
            return buildMainScreenPreview(form, selectedSource, lov.getBlockProperties(), size(lov.getWidth(), availableWidth), size(lov.getHeight(),
                    availableHeight));
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
            return buildRendererPlaceholderPreview(form, source(selectedSource, block), block, blockDescriptor, preferredWidth, preferredHeight,
                    availableWidth, availableHeight);
        }

        // A single-record block with no item groups has nothing to show; fall back to the canvas
        // preview. (Checked on the model, not on the built node, because the block body may be
        // wrapped in a title-bar section node.)
        if (block.getMainScreenItemGroupDisplayContainer() == null || block.getMainScreenItemGroupDisplayContainer().getItemGroups().isEmpty())
        {
            return null;
        }

        PreviewNode root = createSelectedRoot(form, source(selectedSource, block), 1, block.getMainScreenProperties().getWidth(), block
                .getMainScreenProperties().getHeight(), availableWidth, availableHeight);
        PreviewNode content = createBlockContentNode(block);
        content.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(1).setVerticalSpan(1).setFillHorizontal(true).setFillVertical(true)
                .setGrabHorizontal(true).setGrabVertical(true));
        root.addChild(content);
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
            return buildRendererPlaceholderPreview(form, source(selectedSource, block), block, descriptor, width, height, availableWidth, availableHeight);
        }
        new PreviewGridLayoutSolver().layout(root);
        return root;
    }

    private PreviewNode buildRendererPlaceholderPreview(EJPluginFormProperties form, Object source, EJPluginBlockProperties block,
            EJDevPreviewDescriptor descriptor, int preferredWidth, int preferredHeight, int availableWidth, int availableHeight)
    {
        PreviewNode root = createSelectedRoot(form, source, 1, preferredWidth, preferredHeight, availableWidth, availableHeight);
        PreviewNode rendererNode = new PreviewNode(source, descriptor);
        rendererNode.setPaintBorder(false);
        if (isMultiTableRenderer(block, descriptor))
        {
            rendererNode.setColumnLabels(blockColumnLabels(block));
            rendererNode.setColumnAlignments(blockColumnAlignments(block));
        }
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

    private boolean isMultiTableRenderer(EJPluginBlockProperties block, EJDevPreviewDescriptor descriptor)
    {
        if (descriptor == null || descriptor.getKind() != EJDevPreviewKind.TABLE || block == null)
        {
            return false;
        }

        String rendererName = normalized(block.getBlockRendererName());
        if (contains(rendererName, "multirecord", "multi record", "multi_record", "multitable", "multi table", "multi_table"))
        {
            return true;
        }

        Object definition = block.getBlockRendererDefinition();
        String definitionName = definition == null ? null : normalized(definition.getClass().getSimpleName());
        return contains(definitionName, "multirecordblockdefinition", "multitableblockdefinition");
    }

    private List<String> blockColumnLabels(EJPluginBlockProperties block)
    {
        List<String> labels = new ArrayList<String>();
        if (block != null)
        {
            collectColumnLabels(labels, block.getMainScreenItemGroupDisplayContainer());
        }
        return labels;
    }

    private List<Integer> blockColumnAlignments(EJPluginBlockProperties block)
    {
        List<Integer> alignments = new ArrayList<Integer>();
        if (block != null)
        {
            collectColumnItems(block.getMainScreenItemGroupDisplayContainer(), null, alignments);
        }
        return alignments;
    }

    private void collectColumnLabels(List<String> labels, EJPluginItemGroupContainer container)
    {
        collectColumnItems(container, labels, null);
    }

    /**
     * Walks the main-screen item groups in display order, collecting the header label and column
     * alignment for each item that becomes a table/tree column.
     */
    private void collectColumnItems(EJPluginItemGroupContainer container, List<String> labels, List<Integer> alignments)
    {
        if (container == null)
        {
            return;
        }
        for (EJPluginItemGroupProperties itemGroup : container.getItemGroups())
        {
            if (!itemGroup.isSeparator())
            {
                for (EJScreenItemProperties itemProperties : itemGroup.getAllItemProperties())
                {
                    if (itemProperties instanceof EJPluginScreenItemProperties)
                    {
                        EJPluginScreenItemProperties item = (EJPluginScreenItemProperties) itemProperties;
                        if (item.isVisible() && !item.isSpacerItem() && !item.isSeparator())
                        {
                            if (labels != null)
                            {
                                labels.add(value(item.getLabel(), item.getReferencedItemName()));
                            }
                            if (alignments != null)
                            {
                                alignments.add(Integer.valueOf(columnAlignment(requiredProperties(item))));
                            }
                        }
                    }
                }
            }
            collectColumnItems(itemGroup.getChildItemGroupContainer(), labels, alignments);
        }
    }

    private int columnAlignment(EJFrameworkExtensionProperties requiredProperties)
    {
        String alignment = stringProperty(requiredProperties, COLUMN_ALIGNMENT_PROPERTY);
        if (ORIENTATION_RIGHT.equalsIgnoreCase(alignment))
        {
            return PreviewNode.TEXT_ALIGN_RIGHT;
        }
        if (ORIENTATION_CENTER.equalsIgnoreCase(alignment))
        {
            return PreviewNode.TEXT_ALIGN_CENTER;
        }
        return PreviewNode.TEXT_ALIGN_LEFT;
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
        if (group.isSeparator())
        {
            boolean vertical = isVerticalSeparator(group);
            node.setColumns(1);
            node.setCompactLayout(true);
            node.setPaintBorder(false);
            node.setPaintContainerTitle(false);
            node.setPaintControlLabel(false);
            node.setVerticalOrientation(vertical);
            node.setConstraint(itemGroupConstraint(group, vertical ? DEFAULT_CONTROL_HEIGHT : DEFAULT_SEPARATOR_HEIGHT));
            return node;
        }

        node.setColumns(group.getNumCols());
        node.setLayoutGap(0);
        node.setCompactLayout(true);
        // EJRWTBlockPreviewerCreator#addItemGroup: a frameless group, or a framed group with no
        // frame title, is a plain Composite with no border at all. Only a framed + titled group
        // becomes either an etched Group or an expandable Section.
        boolean titled = group.dispayGroupFrame() && value(group.getFrameTitle()) != null;
        node.setPaintBorder(titled);
        node.setPaintContainerTitle(titled);
        if (titled)
        {
            applyTitleBar(node, group.getRendererProperties());
        }
        node.setConstraint(itemGroupConstraint(group, 0));

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
        node.setTextAlignment(labelTextAlignment(requiredProperties));
        // SWT (createBlockLableGridData): GridData(FILL_HORIZONTAL) with grabExcessHorizontalSpace
        // = false. Vertically the label is centred in its cell, except that it is pinned to the top
        // when it spans rows or its item expands vertically.
        int verticalSpan = intProperty(requiredProperties, YSPAN_PROPERTY, 1);
        boolean top = verticalSpan > 1 || booleanProperty(requiredProperties, EXPAND_Y_PROPERTY, false);
        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(1).setVerticalSpan(verticalSpan)
                .setPreferredWidth(labelWidth(item)).setPreferredHeight(DEFAULT_CONTROL_HEIGHT).setFillHorizontal(true).setFillVertical(false)
                .setGrabHorizontal(false).setGrabVertical(false)
                .setVerticalAlignment(top ? PreviewGridConstraint.Alignment.BEGINNING : PreviewGridConstraint.Alignment.CENTER));
        return node;
    }

    /**
     * Applies the expandable-section chrome the renderer asks for via its TITLE_BAR_MODE property.
     * Any mode other than GROUP (and other than "absent") produces an Eclipse Forms Section, with
     * TWISTIE / TREE_NODE adding an expand affordance. Mirrors EJRWTBlockPreviewerCreator and
     * EJRWTSingleRecordBlockDefinition.
     */
    private void applyTitleBar(PreviewNode node, EJFrameworkExtensionProperties titleBarProperties)
    {
        String mode = stringProperty(titleBarProperties, TITLE_BAR_MODE_PROPERTY);
        if (mode == null || TITLE_BAR_MODE_GROUP.equals(mode))
        {
            node.setTitleBarMode(PreviewNode.TITLE_BAR_NONE);
            return;
        }

        if (TITLE_BAR_MODE_TWISTIE.equals(mode))
        {
            node.setTitleBarMode(PreviewNode.TITLE_BAR_TWISTIE);
        }
        else if (TITLE_BAR_MODE_TREE_NODE.equals(mode))
        {
            node.setTitleBarMode(PreviewNode.TITLE_BAR_TREE_NODE);
        }
        else
        {
            node.setTitleBarMode(PreviewNode.TITLE_BAR_PLAIN);
        }
        node.setTitleBarExpanded(booleanProperty(titleBarProperties, TITLE_BAR_EXPANDED_PROPERTY, true));
    }

    /**
     * Radio groups render their configured RADIO_BUTTONS entries, laid out horizontally or
     * vertically, optionally inside a titled frame. See EJRWTRadioGroupItemRendererDefinition.
     */
    private void applyRadioGroup(PreviewNode node, EJPluginScreenItemProperties item)
    {
        EJDevBlockItemDisplayProperties blockItem = item.getBlockItemDisplayProperties();
        EJFrameworkExtensionProperties rendererProperties = blockItem == null ? null : blockItem.getItemRendererProperties();
        node.setOptionsFramed(booleanProperty(rendererProperties, SHOW_BORDER_PROPERTY, false));
        node.setVerticalOrientation(!isHorizontalRadioGroup(item));
        node.setOptionLabels(radioOptionLabels(item));
    }

    private int labelTextAlignment(EJFrameworkExtensionProperties requiredProperties)
    {
        String orientation = stringProperty(requiredProperties, LABEL_ORIENTATION_PROPERTY);
        if (ORIENTATION_RIGHT.equalsIgnoreCase(orientation))
        {
            return PreviewNode.TEXT_ALIGN_RIGHT;
        }
        if (ORIENTATION_CENTER.equalsIgnoreCase(orientation))
        {
            return PreviewNode.TEXT_ALIGN_CENTER;
        }
        return PreviewNode.TEXT_ALIGN_LEFT;
    }

    private PreviewNode createFlatControlNode(EJPluginScreenItemProperties item, EJFrameworkExtensionProperties requiredProperties, int maximumSpan)
    {
        PreviewNode node = new PreviewNode(item, resolver.forScreenItem(item));
        node.setPaintBorder(false);
        node.setPaintControlLabel(false);
        node.setVerticalOrientation(isVerticalSeparator(item));
        if (node.getKind() == EJDevPreviewKind.RADIO_GROUP)
        {
            applyRadioGroup(node, item);
        }

        int horizontalSpan = Math.min(Math.max(1, maximumSpan), intProperty(requiredProperties, XSPAN_PROPERTY, 1));
        int verticalSpan = intProperty(requiredProperties, YSPAN_PROPERTY, 1);
        boolean expandHorizontally = booleanProperty(requiredProperties, EXPAND_X_PROPERTY, false);
        boolean expandVertically = booleanProperty(requiredProperties, EXPAND_Y_PROPERTY, false);
        int preferredWidth = preferredControlWidth(item, requiredProperties, expandHorizontally);
        int preferredHeight = preferredControlHeight(item, requiredProperties);

        // Keep controls pinned to the top of their preview cell. The old SWT preview did not
        // center controls in tall block rows, and expanded blocks should preserve that placement.
        node.setConstraint(PreviewGridConstraint.defaults().setHorizontalSpan(horizontalSpan).setVerticalSpan(verticalSpan)
                .setPreferredWidth(preferredWidth).setPreferredHeight(preferredHeight).setFillHorizontal(expandHorizontally)
                .setFillVertical(expandVertically).setGrabHorizontal(expandHorizontally).setGrabVertical(expandVertically)
                .setVerticalAlignment(PreviewGridConstraint.Alignment.BEGINNING));
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
        EJCanvasType type = canvas.getType();
        if (type == EJCanvasType.BLOCK)
        {
            return createCanvasBlockNode(canvas);
        }

        PreviewNode node = new PreviewNode(canvas, canvasDescriptor(canvas));
        node.setColumns(canvas.getNumCols());
        node.setConstraint(canvasConstraint(canvas));
        node.setCompactLayout(true);

        if (type == EJCanvasType.FORM)
        {
            return node;
        }
        else if (type == EJCanvasType.SEPARATOR)
        {
            node.setPaintBorder(false);
            node.setPaintContainerTitle(false);
            node.setPaintControlLabel(false);
            node.setVerticalOrientation(canvas.getSplitOrientation() != EJCanvasSplitOrientation.HORIZONTAL);
        }
        else if (type == EJCanvasType.GROUP)
        {
            // SWT only titles a group canvas when it asks for a frame and supplies a title.
            node.setPaintContainerTitle(Boolean.TRUE.equals(canvas.getDisplayGroupFrame()) && value(canvas.getGroupFrameTitle()) != null);
            addCanvasContainer(node, canvas.getGroupCanvasContainer(), selectedSource);
        }
        else if (type == EJCanvasType.SPLIT)
        {
            boolean horizontal = canvas.getSplitOrientation() == EJCanvasSplitOrientation.HORIZONTAL;
            node.setVerticalOrientation(!horizontal);
            node.setColumns(horizontal ? Math.max(1, canvas.getSplitCanvasContainer().getCanvasProperties().size()) : 1);
            node.setPaintContainerTitle(canvas.getSplitCanvasContainer().getCanvasProperties().isEmpty());
            addCanvasContainer(node, canvas.getSplitCanvasContainer(), selectedSource);
            normalizeSplitChildren(node);
        }
        else if (type == EJCanvasType.TAB)
        {
            addTabPages(node, canvas, selectedSource);
        }
        else if (type == EJCanvasType.DRAWER)
        {
            // Parity note: the SWT preview draws only the drawer's tab buttons, never page content.
            addDrawerPages(node, canvas);
        }
        else if (type == EJCanvasType.STACKED)
        {
            addStackedPages(node, canvas, selectedSource);
        }

        return node;
    }

    /**
     * A SashForm gives every pane one slot and stretches it; spans would otherwise wrap panes onto
     * extra rows/columns. The per-pane size hints are preserved because the solver turns them into
     * sash weights (see PreviewGridLayoutSolver#splitWeight).
     */
    private void normalizeSplitChildren(PreviewNode split)
    {
        for (PreviewNode child : split.getChildren())
        {
            child.getConstraint().setHorizontalSpan(1).setVerticalSpan(1).setFillHorizontal(true).setFillVertical(true).setGrabHorizontal(true)
                    .setGrabVertical(true).setHorizontalAlignment(PreviewGridConstraint.Alignment.BEGINNING).setVerticalAlignment(
                            PreviewGridConstraint.Alignment.BEGINNING);
        }
    }

    /**
     * Renders a block canvas with its real main-screen content, matching the SWT preview's
     * <code>addBlockControlToCanvas</code> path. Falls back to a labelled placeholder box when the
     * canvas has no resolvable block.
     */
    private PreviewNode createCanvasBlockNode(EJPluginCanvasProperties canvas)
    {
        EJPluginBlockProperties block = canvas.getPluginBlockProperties();
        if (block == null || block.getMainScreenProperties() == null)
        {
            PreviewNode placeholder = new PreviewNode(canvas, canvasBlockDescriptor(canvas));
            placeholder.setColumns(canvas.getNumCols());
            placeholder.setConstraint(canvasConstraint(canvas));
            placeholder.setCompactLayout(true);
            return placeholder;
        }

        PreviewNode node = createBlockContentNode(block);
        node.setConstraint(canvasConstraint(canvas));
        // Selecting either the canvas or its block in the tree should highlight this node.
        node.setAliasSource(canvas);
        return node;
    }

    /**
     * The block's main screen as the block renderer would draw it: item groups and items for a
     * single-record block, a column header strip for table/tree renderers, a chart body otherwise.
     */
    private PreviewNode createBlockContentNode(EJPluginBlockProperties block)
    {
        EJPluginMainScreenProperties mainScreen = block.getMainScreenProperties();
        EJDevPreviewDescriptor descriptor = resolver.forBlock(block);
        boolean frame = mainScreen.getDisplayFrame();
        String frameTitle = value(mainScreen.getFrameTitle());
        if (frame && frameTitle != null)
        {
            descriptor = descriptor.withLabel(frameTitle);
        }

        PreviewNode node = new PreviewNode(block, descriptor);
        node.setColumns(mainScreen.getNumCols());
        node.setCompactLayout(true);
        // Only a framed main screen gets a Group box; table/tree renderers draw their own border.
        node.setPaintBorder(frame);

        if (descriptor.getKind() == EJDevPreviewKind.BLOCK)
        {
            node.setPaintContainerTitle(frame && frameTitle != null);
            addFlatItemGroups(node, block.getMainScreenItemGroupDisplayContainer());
        }
        else
        {
            node.setPaintContainerTitle(false);
            if (isMultiTableRenderer(block, descriptor))
            {
                node.setColumnLabels(blockColumnLabels(block));
                node.setColumnAlignments(blockColumnAlignments(block));
            }
        }
        return wrapInBlockTitleBar(block, node);
    }

    /**
     * Single- and multi-record blocks may wrap their body in an Eclipse Forms Section carrying a
     * title bar (see EJRWTSingleRecordBlockDefinition#addBlockControlToCanvas). Model that as an
     * outer container node so both the title bar and the inner frame survive.
     */
    private PreviewNode wrapInBlockTitleBar(EJPluginBlockProperties block, PreviewNode body)
    {
        EJFrameworkExtensionProperties rendererProperties = block.getBlockRendererProperties();
        EJFrameworkExtensionProperties titleBar = rendererProperties == null ? null : rendererProperties.getPropertyGroup(TITLE_BAR_GROUP);
        String mode = stringProperty(titleBar, TITLE_BAR_MODE_PROPERTY);
        if (mode == null || TITLE_BAR_MODE_GROUP.equals(mode))
        {
            return body;
        }

        String title = value(stringProperty(titleBar, TITLE_BAR_TITLE_PROPERTY), block.getName());
        PreviewNode section = new PreviewNode(block, EJDevPreviewDescriptor.create(EJDevPreviewKind.BLOCK, block.getName(), title,
                block.getBlockRendererName(), 0, 0));
        section.setColumns(1);
        section.setCompactLayout(true);
        section.setPaintBorder(false);
        section.setPaintContainerTitle(true);
        applyTitleBar(section, titleBar);

        body.setConstraint(PreviewGridConstraint.defaults().setFillHorizontal(true).setFillVertical(true).setGrabHorizontal(true).setGrabVertical(true));
        section.addChild(body);
        return section;
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

    /**
     * Mirrors EJRWTBlockPreviewerCreator#createItemGroupGridData: the group always starts from
     * FILL_BOTH, expand flags only control grabbing, and an explicit alignment overrides the fill
     * on that axis (and forces a grab, as SWT does).
     */
    private PreviewGridConstraint itemGroupConstraint(EJPluginItemGroupProperties group, int fallbackHeight)
    {
        int preferredHeight = group.getHeight() > 0 ? group.getHeight() : fallbackHeight;
        boolean grabHorizontal = group.canExpandHorizontally();
        boolean grabVertical = group.canExpandVertically();

        PreviewGridConstraint constraint = PreviewGridConstraint.defaults().setHorizontalSpan(group.getXspan()).setVerticalSpan(group.getYspan())
                .setPreferredWidth(group.getWidth()).setPreferredHeight(preferredHeight).setFillHorizontal(true).setFillVertical(true)
                .setMinimumWidth(grabHorizontal ? group.getWidth() : 0).setMinimumHeight(grabVertical ? preferredHeight : 0);

        EJItemGroupAlignment horizontal = group.getHorizontalAlignment();
        if (horizontal != null && horizontal != EJItemGroupAlignment.FILL)
        {
            constraint.setFillHorizontal(false).setHorizontalAlignment(alignment(horizontal));
            grabHorizontal = grabHorizontal || horizontal != EJItemGroupAlignment.BEGINNING;
        }

        EJItemGroupAlignment vertical = group.getVerticalAlignment();
        if (vertical != null && vertical != EJItemGroupAlignment.FILL)
        {
            constraint.setFillVertical(false).setVerticalAlignment(alignment(vertical));
            grabVertical = grabVertical || vertical != EJItemGroupAlignment.BEGINNING;
        }

        return constraint.setGrabHorizontal(grabHorizontal).setGrabVertical(grabVertical);
    }

    private PreviewGridConstraint.Alignment alignment(EJItemGroupAlignment alignment)
    {
        switch (alignment)
        {
            case CENTER:
                return PreviewGridConstraint.Alignment.CENTER;
            case END:
                return PreviewGridConstraint.Alignment.END;
            default:
                return PreviewGridConstraint.Alignment.BEGINNING;
        }
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

    /**
     * Whether DISPLAYED_WIDTH/HEIGHT are character counts (scaled by the font) or literal pixels.
     * Mirrors EJDevItemRendererDefinitionControl#useFontDimensions: the RAP definitions clear the
     * flag for image, HTML, check box, radio group, button and help renderers, and the block
     * previewer always passes <code>false</code> for spacer items.
     */
    private boolean usesFontDimensions(EJPluginScreenItemProperties item, EJDevPreviewKind kind)
    {
        if (item.isSpacerItem())
        {
            return false;
        }
        switch (kind)
        {
            case IMAGE:
            case HTML:
            case CHECKBOX:
            case RADIO_GROUP:
            case BUTTON:
                return false;
            default:
                return true;
        }
    }

    private int preferredControlWidth(EJPluginScreenItemProperties item, EJFrameworkExtensionProperties requiredProperties, boolean expandHorizontally)
    {
        EJDevPreviewKind kind = resolver.forScreenItem(item).getKind();
        int displayedWidth = intProperty(requiredProperties, DISPLAYED_WIDTH_PROPERTY, 0);
        if (displayedWidth > 0)
        {
            return usesFontDimensions(item, kind) ? (displayedWidth + 1) * CHAR_WIDTH : displayedWidth;
        }
        if (item.isSpacerItem())
        {
            return item.isSeparator() && !isVerticalSeparator(item) ? DEFAULT_CONTROL_WIDTH : (item.isSeparator() ? DEFAULT_SEPARATOR_HEIGHT : 1);
        }

        if (kind == EJDevPreviewKind.BUTTON)
        {
            return Math.max(34, textWidth(value(item.getLabel(), item.getReferencedItemName())) + 14);
        }
        return expandHorizontally ? DEFAULT_CONTROL_WIDTH : Math.max(DEFAULT_CONTROL_WIDTH, textWidth(item.getReferencedItemName()) + 20);
    }

    private int preferredControlHeight(EJPluginScreenItemProperties item, EJFrameworkExtensionProperties requiredProperties)
    {
        EJDevPreviewKind kind = resolver.forScreenItem(item).getKind();
        int displayedHeight = intProperty(requiredProperties, DISPLAYED_HEIGHT_PROPERTY, 0);
        if (displayedHeight > 0)
        {
            return usesFontDimensions(item, kind) ? (displayedHeight + 1) * CHAR_HEIGHT : displayedHeight;
        }
        if (item.isSpacerItem())
        {
            return item.isSeparator() && isVerticalSeparator(item) ? DEFAULT_CONTROL_HEIGHT : (item.isSeparator() ? DEFAULT_SEPARATOR_HEIGHT : 1);
        }

        switch (kind)
        {
            case TEXT_AREA:
            case HTML:
                return DEFAULT_TEXT_AREA_HEIGHT;
            case BUTTON:
                return DEFAULT_BUTTON_HEIGHT;
            case RADIO_GROUP:
                // Title/frame band plus one row per option when stacked vertically.
                return 20 + (16 * (isHorizontalRadioGroup(item) ? 1 : Math.max(1, radioOptionLabels(item).size())));
            default:
                return DEFAULT_CONTROL_HEIGHT;
        }
    }

    private boolean isHorizontalRadioGroup(EJPluginScreenItemProperties item)
    {
        EJDevBlockItemDisplayProperties blockItem = item.getBlockItemDisplayProperties();
        EJFrameworkExtensionProperties rendererProperties = blockItem == null ? null : blockItem.getItemRendererProperties();
        return ORIENTATION_HORIZONTAL.equals(stringProperty(rendererProperties, ORIENTATION_PROPERTY));
    }

    private List<String> radioOptionLabels(EJPluginScreenItemProperties item)
    {
        List<String> labels = new ArrayList<String>();
        EJDevBlockItemDisplayProperties blockItem = item.getBlockItemDisplayProperties();
        EJFrameworkExtensionProperties rendererProperties = blockItem == null ? null : blockItem.getItemRendererProperties();
        if (rendererProperties == null)
        {
            return labels;
        }
        EJFrameworkExtensionPropertyList radioButtons = rendererProperties.getPropertyList(RADIO_BUTTONS_LIST);
        if (radioButtons == null)
        {
            return labels;
        }
        for (EJFrameworkExtensionPropertyListEntry entry : radioButtons.getAllListEntries())
        {
            labels.add(entry.getProperty(RADIO_LABEL_PROPERTY));
        }
        return labels;
    }

    private boolean isVerticalSeparator(EJPluginScreenItemProperties item)
    {
        return item.isSeparator() && item.getSeparatorOrientation() == EJSeparatorOrientation.VERTICAL;
    }

    private boolean isVerticalSeparator(EJPluginItemGroupProperties group)
    {
        return group.isSeparator() && group.getSeparatorOrientation() == EJSeparatorOrientation.VERTICAL;
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

    private String normalized(String value)
    {
        return value == null ? "" : value.toLowerCase();
    }

    private boolean contains(String value, String... fragments)
    {
        if (value == null)
        {
            return false;
        }
        for (String fragment : fragments)
        {
            if (value.indexOf(fragment) > -1)
            {
                return true;
            }
        }
        return false;
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
                    .setFillHorizontal(screen.canExpandHorizontally()).setFillVertical(screen.canExpandVertically()).setGrabHorizontal(screen
                            .canExpandHorizontally()).setGrabVertical(screen.canExpandVertically());
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
