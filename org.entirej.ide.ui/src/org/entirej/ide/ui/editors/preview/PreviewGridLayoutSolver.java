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
import java.util.List;

import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;

public class PreviewGridLayoutSolver
{
    private static final int DEFAULT_CELL_WIDTH  = 120;
    private static final int DEFAULT_CELL_HEIGHT = 34;
    private static final int MARGIN              = 24;
    private static final int COMPACT_MARGIN      = 5;
    private static final int GAP                 = 5;
    private static final int TAB_MARGIN          = 8;
    private static final int TAB_TOP_MARGIN      = 30;
    private static final int TAB_TITLE_HEIGHT    = 18;
    private static final int TITLE_HEIGHT        = 18;

    public void layout(PreviewNode root)
    {
        if (root != null)
        {
            layoutGrid(root);
        }
    }

    private void layoutGrid(PreviewNode parent)
    {
        List<PreviewNode> children = parent.getVisibleChildren();
        if (children.isEmpty())
        {
            return;
        }

        int columns = Math.max(1, parent.getColumns());
        List<Placement> placements = place(children, columns);
        int rowCount = rowCount(placements);

        int left = leftInset(parent);
        int right = rightInset(parent);
        int top = topInset(parent);
        int bottom = bottomInset(parent);
        int gap = gap(parent);

        int availableWidth = parent.getBounds().getWidth() - left - right - (gap * (columns - 1));
        int[] columnWidths = columnWidths(parent, placements, columns, availableWidth, gap);

        int availableHeight = parent.getBounds().getHeight() - top - bottom - (gap * (rowCount - 1));
        int[] rowHeights = rowHeights(parent, placements, rowCount, availableHeight);

        for (Placement placement : placements)
        {
            int x = parent.getBounds().getX() + left + widthBefore(columnWidths, placement.column) + (gap * placement.column);
            int y = parent.getBounds().getY() + top + heightBefore(rowHeights, placement.row) + (gap * placement.row);
            int width = widthSpan(columnWidths, placement.column, placement.columnSpan) + (gap * (placement.columnSpan - 1));
            int height = heightSpan(rowHeights, placement.row, placement.rowSpan) + (gap * (placement.rowSpan - 1));

            PreviewGridConstraint constraint = placement.node.getConstraint();
            int cellWidth = width;
            int cellHeight = height;
            if (!constraint.isFillHorizontal())
            {
                width = Math.min(width, preferredWidth(placement.node));
                x += align(cellWidth - width, constraint.getHorizontalAlignment());
            }
            if (!constraint.isFillVertical())
            {
                height = Math.min(height, preferredHeight(placement.node));
                y += align(cellHeight - height, constraint.getVerticalAlignment());
            }

            placement.node.setBounds(x, y, width, height);
            layoutGrid(placement.node);
        }
    }

    private int leftInset(PreviewNode parent)
    {
        switch (parent.getKind())
        {
            case TAB_FOLDER:
            case STACKED:
                return TAB_MARGIN;
            default:
                return margin(parent);
        }
    }

    private int rightInset(PreviewNode parent)
    {
        switch (parent.getKind())
        {
            case TAB_FOLDER:
            case STACKED:
                return TAB_MARGIN;
            default:
                return margin(parent);
        }
    }

    private int topInset(PreviewNode parent)
    {
        switch (parent.getKind())
        {
            case TAB_FOLDER:
            case STACKED:
                if (parent.isTabsAtBottom())
                {
                    return parent.isPaintContainerTitle() ? TAB_MARGIN + TAB_TITLE_HEIGHT : TAB_MARGIN;
                }
                return parent.isPaintContainerTitle() ? TAB_TOP_MARGIN + TAB_TITLE_HEIGHT : TAB_TOP_MARGIN;
            case DRAWER:
                return margin(parent);
            default:
                // A painted container title occupies a band across the top of the client area
                // (see PreviewNodeFigure#drawTitle); reserve it so children do not sit under it.
                return margin(parent) + (parent.isPaintContainerTitle() ? TITLE_HEIGHT : 0);
        }
    }

    private int align(int slack, PreviewGridConstraint.Alignment alignment)
    {
        if (slack <= 0 || alignment == null)
        {
            return 0;
        }
        switch (alignment)
        {
            case CENTER:
                return slack / 2;
            case END:
                return slack;
            default:
                return 0;
        }
    }

    private int bottomInset(PreviewNode parent)
    {
        switch (parent.getKind())
        {
            case TAB_FOLDER:
            case STACKED:
                return parent.isTabsAtBottom() ? TAB_TOP_MARGIN : TAB_MARGIN;
            default:
                return margin(parent);
        }
    }

    private int margin(PreviewNode parent)
    {
        if (parent.getLayoutMargin() >= 0)
        {
            return parent.getLayoutMargin();
        }
        return parent.isCompactLayout() ? COMPACT_MARGIN : MARGIN;
    }

    private int gap(PreviewNode parent)
    {
        return parent.getLayoutGap() >= 0 ? parent.getLayoutGap() : GAP;
    }

    private int[] columnWidths(PreviewNode parent, List<Placement> placements, int columns, int availableWidth, int gap)
    {
        if (isHorizontalSplit(parent, columns))
        {
            return weightedSplitWidths(placements, columns, availableWidth);
        }

        int[] widths = new int[columns];
        boolean[] grabColumns = new boolean[columns];

        for (Placement placement : placements)
        {
            if (placement.columnSpan == 1)
            {
                widths[placement.column] = Math.max(widths[placement.column], preferredWidth(placement.node));
                markGrabColumns(grabColumns, placement, columns);
            }
        }

        for (int i = 0; i < widths.length; i++)
        {
            if (widths[i] == 0)
            {
                widths[i] = DEFAULT_CELL_WIDTH;
            }
        }

        for (Placement placement : placements)
        {
            int preferredWidth = preferredWidth(placement.node);
            int currentWidth = widthSpan(widths, placement.column, placement.columnSpan) + (gap * (placement.columnSpan - 1));
            if (currentWidth < preferredWidth)
            {
                growSpan(widths, placement.column, placement.columnSpan, preferredWidth - currentWidth);
            }
            if (placement.columnSpan > 1)
            {
                markSpanningGrabColumns(grabColumns, placement, columns);
            }
        }

        distributeExtra(widths, grabColumns, availableWidth);
        return widths;
    }

    private void markGrabColumns(boolean[] grabColumns, Placement placement, int parentColumns)
    {
        if (!placement.node.getConstraint().isGrabHorizontal())
        {
            return;
        }
        for (int i = placement.column; i < placement.column + placement.columnSpan && i < grabColumns.length; i++)
        {
            grabColumns[i] = true;
        }
    }

    private void markSpanningGrabColumns(boolean[] grabColumns, Placement placement, int parentColumns)
    {
        if (!placement.node.getConstraint().isGrabHorizontal())
        {
            return;
        }

        int end = Math.min(parentColumns, placement.column + placement.columnSpan);
        for (int i = placement.column; i < end && i < grabColumns.length; i++)
        {
            if (grabColumns[i])
            {
                return;
            }
        }

        for (int i = placement.column; i < end && i < grabColumns.length; i++)
        {
            grabColumns[i] = true;
        }
    }

    private int[] rowHeights(PreviewNode parent, List<Placement> placements, int rowCount, int availableHeight)
    {
        if (isVerticalSplit(parent, rowCount))
        {
            return weightedSplitHeights(placements, rowCount, availableHeight);
        }

        int[] heights = new int[Math.max(1, rowCount)];
        boolean[] grabRows = new boolean[heights.length];

        for (Placement placement : placements)
        {
            int preferredHeight = preferredHeight(placement.node);
            int heightPerRow = Math.max(1, (preferredHeight + Math.max(1, placement.rowSpan) - 1) / Math.max(1, placement.rowSpan));
            for (int i = placement.row; i < placement.row + placement.rowSpan && i < heights.length; i++)
            {
                heights[i] = Math.max(heights[i], heightPerRow);
            }
            if (placement.rowSpan == 1)
            {
                markGrabRows(grabRows, placement);
            }
        }
        for (int i = 0; i < heights.length; i++)
        {
            if (heights[i] == 0)
            {
                heights[i] = DEFAULT_CELL_HEIGHT;
            }
        }

        for (Placement placement : placements)
        {
            if (placement.rowSpan > 1)
            {
                markSpanningGrabRows(grabRows, placement);
            }
        }

        distributeExtra(heights, grabRows, availableHeight);
        return heights;
    }

    private void markGrabRows(boolean[] grabRows, Placement placement)
    {
        if (!placement.node.getConstraint().isGrabVertical())
        {
            return;
        }
        for (int i = placement.row; i < placement.row + placement.rowSpan && i < grabRows.length; i++)
        {
            grabRows[i] = true;
        }
    }

    private void markSpanningGrabRows(boolean[] grabRows, Placement placement)
    {
        if (!placement.node.getConstraint().isGrabVertical())
        {
            return;
        }

        int end = Math.min(grabRows.length, placement.row + placement.rowSpan);
        for (int i = placement.row; i < end && i < grabRows.length; i++)
        {
            if (grabRows[i])
            {
                return;
            }
        }

        for (int i = placement.row; i < end && i < grabRows.length; i++)
        {
            grabRows[i] = true;
        }
    }

    private boolean isHorizontalSplit(PreviewNode parent, int columns)
    {
        return parent.getKind() == EJDevPreviewKind.SPLIT && columns > 1;
    }

    private boolean isVerticalSplit(PreviewNode parent, int rowCount)
    {
        return parent.getKind() == EJDevPreviewKind.SPLIT && parent.getColumns() == 1 && rowCount > 1;
    }

    private int[] weightedSplitWidths(List<Placement> placements, int columns, int availableWidth)
    {
        int[] minimums = new int[columns];
        int[] weights = new int[columns];

        for (Placement placement : placements)
        {
            if (placement.columnSpan == 1 && placement.column < columns)
            {
                minimums[placement.column] = Math.max(minimums[placement.column], placement.node.getConstraint().getMinimumWidth());
                weights[placement.column] = Math.max(weights[placement.column], splitWeight(placement.node, true));
            }
        }

        return distributeWeighted(minimums, weights, availableWidth, DEFAULT_CELL_WIDTH);
    }

    private int[] weightedSplitHeights(List<Placement> placements, int rowCount, int availableHeight)
    {
        int[] minimums = new int[Math.max(1, rowCount)];
        int[] weights = new int[minimums.length];

        for (Placement placement : placements)
        {
            if (placement.rowSpan == 1 && placement.row < minimums.length)
            {
                minimums[placement.row] = Math.max(minimums[placement.row], placement.node.getConstraint().getMinimumHeight());
                weights[placement.row] = Math.max(weights[placement.row], splitWeight(placement.node, false));
            }
        }

        return distributeWeighted(minimums, weights, availableHeight, DEFAULT_CELL_HEIGHT);
    }

    private int splitWeight(PreviewNode node, boolean horizontal)
    {
        int weight = horizontal ? Math.max(node.getConstraint().getPreferredWidth(), node.getDescriptor().getPreferredWidth()) : Math.max(node
                .getConstraint().getPreferredHeight(), node.getDescriptor().getPreferredHeight());
        return Math.max(1, weight + 1);
    }

    private int[] distributeWeighted(int[] minimums, int[] weights, int available, int fallback)
    {
        int[] sizes = new int[minimums.length];
        if (sizes.length == 0)
        {
            return sizes;
        }
        if (available <= 0)
        {
            for (int i = 0; i < sizes.length; i++)
            {
                sizes[i] = Math.max(minimums[i], fallback);
            }
            return sizes;
        }

        int totalWeight = 0;
        for (int i = 0; i < weights.length; i++)
        {
            weights[i] = Math.max(1, weights[i]);
            totalWeight += weights[i];
        }

        int assigned = 0;
        for (int i = 0; i < sizes.length; i++)
        {
            sizes[i] = Math.max(minimums[i], (available * weights[i]) / totalWeight);
            assigned += sizes[i];
        }

        normalizeWeightedSizes(sizes, minimums, available, assigned);
        return sizes;
    }

    private void normalizeWeightedSizes(int[] sizes, int[] minimums, int available, int assigned)
    {
        int remaining = available - assigned;
        int index = 0;
        while (remaining > 0 && sizes.length > 0)
        {
            sizes[index % sizes.length]++;
            remaining--;
            index++;
        }

        int overflow = -remaining;
        while (overflow > 0)
        {
            int indexToReduce = largestReducibleIndex(sizes, minimums);
            if (indexToReduce < 0)
            {
                return;
            }
            int reduction = Math.min(overflow, sizes[indexToReduce] - minimums[indexToReduce]);
            sizes[indexToReduce] -= reduction;
            overflow -= reduction;
        }
    }

    private int largestReducibleIndex(int[] sizes, int[] minimums)
    {
        int index = -1;
        int size = -1;
        for (int i = 0; i < sizes.length; i++)
        {
            if (sizes[i] > minimums[i] && sizes[i] > size)
            {
                index = i;
                size = sizes[i];
            }
        }
        return index;
    }

    private void distributeExtra(int[] sizes, boolean[] grab, int available)
    {
        if (available <= 0)
        {
            return;
        }
        int used = 0;
        for (int size : sizes)
        {
            used += size;
        }

        int extra = available - used;
        if (extra <= 0)
        {
            return;
        }

        int grabCount = 0;
        for (boolean value : grab)
        {
            if (value)
            {
                grabCount++;
            }
        }
        if (grabCount == 0)
        {
            return;
        }

        int perSlot = extra / grabCount;
        int remainder = extra % grabCount;
        for (int i = 0; i < sizes.length; i++)
        {
            if (grab[i])
            {
                sizes[i] += perSlot;
                if (remainder > 0)
                {
                    sizes[i]++;
                    remainder--;
                }
            }
        }
    }

    private void growSpan(int[] sizes, int start, int span, int extra)
    {
        int slots = Math.max(1, Math.min(span, sizes.length - start));
        int perSlot = extra / slots;
        int remainder = extra % slots;
        for (int i = start; i < start + slots; i++)
        {
            sizes[i] += perSlot;
            if (remainder > 0)
            {
                sizes[i]++;
                remainder--;
            }
        }
    }

    private int preferredWidth(PreviewNode node)
    {
        PreviewGridConstraint constraint = node.getConstraint();
        int descriptorWidth = node.getDescriptor().getPreferredWidth();
        int configuredWidth = Math.max(constraint.getPreferredWidth(), descriptorWidth);
        configuredWidth = Math.max(configuredWidth, constraint.getMinimumWidth());
        return configuredWidth > 0 ? configuredWidth : DEFAULT_CELL_WIDTH;
    }

    private int preferredHeight(PreviewNode node)
    {
        PreviewGridConstraint constraint = node.getConstraint();
        int descriptorHeight = node.getDescriptor().getPreferredHeight();
        int configuredHeight = Math.max(constraint.getPreferredHeight(), descriptorHeight);
        configuredHeight = Math.max(configuredHeight, constraint.getMinimumHeight());
        return configuredHeight > 0 ? configuredHeight : DEFAULT_CELL_HEIGHT;
    }

    private int widthBefore(int[] columnWidths, int column)
    {
        int total = 0;
        for (int i = 0; i < column && i < columnWidths.length; i++)
        {
            total += columnWidths[i];
        }
        return total;
    }

    private int widthSpan(int[] columnWidths, int column, int columnSpan)
    {
        int total = 0;
        for (int i = column; i < column + columnSpan && i < columnWidths.length; i++)
        {
            total += columnWidths[i];
        }
        return total;
    }

    private int heightBefore(int[] rowHeights, int row)
    {
        int total = 0;
        for (int i = 0; i < row && i < rowHeights.length; i++)
        {
            total += rowHeights[i];
        }
        return total;
    }

    private int heightSpan(int[] rowHeights, int row, int rowSpan)
    {
        int total = 0;
        for (int i = row; i < row + rowSpan && i < rowHeights.length; i++)
        {
            total += rowHeights[i];
        }
        return total;
    }

    private int rowCount(List<Placement> placements)
    {
        int rowCount = 1;
        for (Placement placement : placements)
        {
            rowCount = Math.max(rowCount, placement.row + placement.rowSpan);
        }
        return rowCount;
    }

    private List<Placement> place(List<PreviewNode> children, int columns)
    {
        List<boolean[]> occupied = new ArrayList<boolean[]>();
        List<Placement> placements = new ArrayList<Placement>();
        int row = 0;
        int column = 0;

        for (PreviewNode child : children)
        {
            PreviewGridConstraint constraint = child.getConstraint();
            int columnSpan = Math.min(columns, Math.max(1, constraint.getHorizontalSpan()));
            int rowSpan = Math.max(1, constraint.getVerticalSpan());

            // Scan cell by cell, wrapping to column 0 of the next row. Advancing the row must
            // always reset the column, otherwise a fully occupied row leaves the column past the
            // limit and the wrap check below skips the following row entirely.
            while (true)
            {
                if (column > columns - columnSpan)
                {
                    row++;
                    column = 0;
                }
                ensureRows(occupied, row + rowSpan, columns);
                if (isFree(occupied, row, column, rowSpan, columnSpan))
                {
                    break;
                }
                column++;
            }

            mark(occupied, row, column, rowSpan, columnSpan);
            placements.add(new Placement(child, row, column, rowSpan, columnSpan));
            column += columnSpan;
            if (column >= columns)
            {
                row++;
                column = 0;
            }
        }

        return placements;
    }

    private void ensureRows(List<boolean[]> occupied, int count, int columns)
    {
        while (occupied.size() < count)
        {
            occupied.add(new boolean[columns]);
        }
    }

    private boolean isFree(List<boolean[]> occupied, int row, int column, int rowSpan, int columnSpan)
    {
        for (int r = row; r < row + rowSpan; r++)
        {
            boolean[] rowData = occupied.get(r);
            for (int c = column; c < column + columnSpan; c++)
            {
                if (rowData[c])
                {
                    return false;
                }
            }
        }
        return true;
    }

    private void mark(List<boolean[]> occupied, int row, int column, int rowSpan, int columnSpan)
    {
        for (int r = row; r < row + rowSpan; r++)
        {
            boolean[] rowData = occupied.get(r);
            for (int c = column; c < column + columnSpan; c++)
            {
                rowData[c] = true;
            }
        }
    }

    private static final class Placement
    {
        private final PreviewNode node;
        private final int         row;
        private final int         column;
        private final int         rowSpan;
        private final int         columnSpan;

        private Placement(PreviewNode node, int row, int column, int rowSpan, int columnSpan)
        {
            this.node = node;
            this.row = row;
            this.column = column;
            this.rowSpan = rowSpan;
            this.columnSpan = columnSpan;
        }
    }
}
