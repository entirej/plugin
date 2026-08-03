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
package org.entirej.ide.ui.editors.preview;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.entirej.framework.dev.renderer.definition.EJDevPreviewDescriptor;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;

public class PreviewNode
{
    private final Object            source;
    private Object                  aliasSource;
    private EJDevPreviewDescriptor  descriptor;
    private PreviewGridConstraint   constraint = PreviewGridConstraint.defaults();
    private PreviewNode             parent;
    private final PreviewBounds     bounds     = new PreviewBounds();
    private final List<PreviewNode> children   = new ArrayList<PreviewNode>();
    private final List<String>      tabLabels  = new ArrayList<String>();
    private final List<String>      columnLabels = new ArrayList<String>();
    private final List<Integer>     columnAlignments = new ArrayList<Integer>();
    private int                     columns    = 1;
    private int                     selectedTabIndex;
    private boolean                 compactLayout;
    private boolean                 paintContainerTitle = true;
    private boolean                 paintBorder         = true;
    private boolean                 paintControlLabel   = true;
    private int                     layoutGap           = -1;
    private int                     layoutMargin        = -1;
    private boolean                 verticalOrientation;
    private boolean                 tabsAtBottom;
    private int                     textAlignment       = TEXT_ALIGN_LEFT;
    private int                     titleBarMode        = TITLE_BAR_NONE;
    private boolean                 titleBarExpanded    = true;
    private final List<String>      optionLabels        = new ArrayList<String>();
    private boolean                 optionsFramed;

    public static final int         TEXT_ALIGN_LEFT     = 0;
    public static final int         TEXT_ALIGN_CENTER   = 1;
    public static final int         TEXT_ALIGN_RIGHT    = 2;

    /** No expandable title bar; a plain frame title (SWT <code>Group</code>) is used instead. */
    public static final int         TITLE_BAR_NONE      = 0;
    /** Eclipse Forms <code>Section</code> with a TITLE_BAR but no expand affordance. */
    public static final int         TITLE_BAR_PLAIN     = 1;
    /** Section with a TWISTIE (triangle) expand affordance. */
    public static final int         TITLE_BAR_TWISTIE   = 2;
    /** Section with a TREE_NODE (plus/minus) expand affordance. */
    public static final int         TITLE_BAR_TREE_NODE = 3;

    public PreviewNode(Object source, EJDevPreviewDescriptor descriptor)
    {
        this.source = source;
        this.descriptor = descriptor == null ? EJDevPreviewDescriptor.box(null, null, null) : descriptor;
    }

    public Object getSource()
    {
        return source;
    }

    /**
     * A second model object that also resolves to this node when syncing selection from the tree.
     * A block canvas, for example, is drawn as its block but may be selected as either.
     */
    public Object getAliasSource()
    {
        return aliasSource;
    }

    public void setAliasSource(Object aliasSource)
    {
        this.aliasSource = aliasSource;
    }

    public EJDevPreviewDescriptor getDescriptor()
    {
        return descriptor;
    }

    public void setDescriptor(EJDevPreviewDescriptor descriptor)
    {
        this.descriptor = descriptor == null ? EJDevPreviewDescriptor.box(null, null, null) : descriptor;
    }

    public EJDevPreviewKind getKind()
    {
        return descriptor.getKind();
    }

    public String getDisplayName()
    {
        if (descriptor.getLabel() != null && descriptor.getLabel().trim().length() > 0)
        {
            return descriptor.getLabel();
        }
        if (descriptor.getName() != null && descriptor.getName().trim().length() > 0)
        {
            return descriptor.getName();
        }
        if (descriptor.getRendererName() != null && descriptor.getRendererName().trim().length() > 0)
        {
            return descriptor.getRendererName();
        }
        return descriptor.getKind().name();
    }

    public PreviewGridConstraint getConstraint()
    {
        return constraint;
    }

    public void setConstraint(PreviewGridConstraint constraint)
    {
        this.constraint = constraint == null ? PreviewGridConstraint.defaults() : constraint;
    }

    public PreviewBounds getBounds()
    {
        return bounds;
    }

    public PreviewNode getParent()
    {
        return parent;
    }

    public PreviewNode getRoot()
    {
        PreviewNode node = this;
        while (node.getParent() != null)
        {
            node = node.getParent();
        }
        return node;
    }

    public void setBounds(int x, int y, int width, int height)
    {
        bounds.setBounds(x, y, width, height);
    }

    public int getColumns()
    {
        return columns;
    }

    public void setColumns(int columns)
    {
        this.columns = Math.max(1, columns);
    }

    public void addChild(PreviewNode child)
    {
        if (child != null)
        {
            child.parent = this;
            children.add(child);
        }
    }

    public List<PreviewNode> getChildren()
    {
        return Collections.unmodifiableList(children);
    }

    public List<PreviewNode> getVisibleChildren()
    {
        if (isSinglePageContainer() && !children.isEmpty())
        {
            return Collections.singletonList(children.get(normalizedSelectedTabIndex()));
        }
        return getChildren();
    }

    public PreviewNode getSelectedTabChild()
    {
        if (!isSinglePageContainer() || children.isEmpty())
        {
            return null;
        }
        return children.get(normalizedSelectedTabIndex());
    }

    public void setTabLabels(List<String> labels)
    {
        tabLabels.clear();
        if (labels != null)
        {
            for (String label : labels)
            {
                if (label != null && label.trim().length() > 0)
                {
                    tabLabels.add(label);
                }
            }
        }
    }

    public List<String> getTabLabels()
    {
        return Collections.unmodifiableList(tabLabels);
    }

    /**
     * Column headers, one per column, in display order. Blank headers are kept as empty strings so
     * that the list stays index-aligned with {@link #setColumnAlignments(List)}.
     */
    public void setColumnLabels(List<String> labels)
    {
        columnLabels.clear();
        if (labels != null)
        {
            for (String label : labels)
            {
                columnLabels.add(label == null ? "" : label.trim());
            }
        }
    }

    public List<String> getColumnLabels()
    {
        return Collections.unmodifiableList(columnLabels);
    }

    /**
     * Per-column text alignment for table/tree renderers, parallel to {@link #getColumnLabels()}.
     * Columns without an entry default to {@link #TEXT_ALIGN_LEFT}.
     */
    public void setColumnAlignments(List<Integer> alignments)
    {
        columnAlignments.clear();
        if (alignments != null)
        {
            columnAlignments.addAll(alignments);
        }
    }

    public int getColumnAlignment(int index)
    {
        if (index < 0 || index >= columnAlignments.size() || columnAlignments.get(index) == null)
        {
            return TEXT_ALIGN_LEFT;
        }
        return columnAlignments.get(index).intValue();
    }

    /**
     * Separators and split containers are drawn along the opposite axis when this is set.
     */
    public boolean isVerticalOrientation()
    {
        return verticalOrientation;
    }

    public void setVerticalOrientation(boolean verticalOrientation)
    {
        this.verticalOrientation = verticalOrientation;
    }

    public boolean isTabsAtBottom()
    {
        return tabsAtBottom;
    }

    public void setTabsAtBottom(boolean tabsAtBottom)
    {
        this.tabsAtBottom = tabsAtBottom;
    }

    public int getTextAlignment()
    {
        return textAlignment;
    }

    public void setTextAlignment(int textAlignment)
    {
        this.textAlignment = textAlignment;
    }

    /**
     * One of the TITLE_BAR_* constants; describes the expandable section chrome the block or item
     * group renderer asks for.
     */
    public int getTitleBarMode()
    {
        return titleBarMode;
    }

    public void setTitleBarMode(int titleBarMode)
    {
        this.titleBarMode = titleBarMode;
    }

    public boolean isTitleBarExpanded()
    {
        return titleBarExpanded;
    }

    public void setTitleBarExpanded(boolean titleBarExpanded)
    {
        this.titleBarExpanded = titleBarExpanded;
    }

    /**
     * Choice labels for option-style controls (radio groups). Empty means "unknown", and the
     * figure falls back to generic placeholders.
     */
    public List<String> getOptionLabels()
    {
        return Collections.unmodifiableList(optionLabels);
    }

    public void setOptionLabels(List<String> labels)
    {
        optionLabels.clear();
        if (labels != null)
        {
            for (String label : labels)
            {
                if (label != null && label.trim().length() > 0)
                {
                    optionLabels.add(label.trim());
                }
            }
        }
    }

    public boolean isOptionsFramed()
    {
        return optionsFramed;
    }

    public void setOptionsFramed(boolean optionsFramed)
    {
        this.optionsFramed = optionsFramed;
    }

    public int getSelectedTabIndex()
    {
        return normalizedSelectedTabIndex();
    }

    public void setSelectedTabIndex(int selectedTabIndex)
    {
        this.selectedTabIndex = Math.max(0, selectedTabIndex);
    }

    public boolean isCompactLayout()
    {
        return compactLayout;
    }

    public void setCompactLayout(boolean compactLayout)
    {
        this.compactLayout = compactLayout;
    }

    public boolean isPaintContainerTitle()
    {
        return paintContainerTitle;
    }

    public void setPaintContainerTitle(boolean paintContainerTitle)
    {
        this.paintContainerTitle = paintContainerTitle;
    }

    public boolean isPaintBorder()
    {
        return paintBorder;
    }

    public void setPaintBorder(boolean paintBorder)
    {
        this.paintBorder = paintBorder;
    }

    public boolean isPaintControlLabel()
    {
        return paintControlLabel;
    }

    public void setPaintControlLabel(boolean paintControlLabel)
    {
        this.paintControlLabel = paintControlLabel;
    }

    public int getLayoutGap()
    {
        return layoutGap;
    }

    public void setLayoutGap(int layoutGap)
    {
        this.layoutGap = layoutGap;
    }

    /**
     * Explicit margin around this container's children; negative means "use the default for the
     * container kind".
     */
    public int getLayoutMargin()
    {
        return layoutMargin;
    }

    public void setLayoutMargin(int layoutMargin)
    {
        this.layoutMargin = layoutMargin;
    }

    private int normalizedSelectedTabIndex()
    {
        if (children.isEmpty())
        {
            return 0;
        }
        return Math.min(selectedTabIndex, children.size() - 1);
    }

    private boolean isSinglePageContainer()
    {
        return getKind() == EJDevPreviewKind.TAB_FOLDER || getKind() == EJDevPreviewKind.STACKED;
    }
}
