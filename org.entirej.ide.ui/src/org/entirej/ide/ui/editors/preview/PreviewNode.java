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
    private EJDevPreviewDescriptor  descriptor;
    private PreviewGridConstraint   constraint = PreviewGridConstraint.defaults();
    private PreviewNode             parent;
    private final PreviewBounds     bounds     = new PreviewBounds();
    private final List<PreviewNode> children   = new ArrayList<PreviewNode>();
    private final List<String>      tabLabels  = new ArrayList<String>();
    private int                     columns    = 1;
    private int                     selectedTabIndex;
    private boolean                 compactLayout;
    private boolean                 paintContainerTitle = true;
    private boolean                 paintBorder         = true;
    private boolean                 paintControlLabel   = true;
    private int                     layoutGap           = -1;

    public PreviewNode(Object source, EJDevPreviewDescriptor descriptor)
    {
        this.source = source;
        this.descriptor = descriptor == null ? EJDevPreviewDescriptor.box(null, null, null) : descriptor;
    }

    public Object getSource()
    {
        return source;
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
