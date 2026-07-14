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

import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.draw2d.Graphics;
import org.eclipse.draw2d.LineBorder;
import org.eclipse.draw2d.XYLayout;
import org.eclipse.draw2d.geometry.Rectangle;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Display;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;

public class PreviewNodeFigure extends Figure
{
    private static final List<String> DEFAULT_OPTIONS = java.util.Arrays.asList("Option", "Option");

    private PreviewNode model;
    private Color       appComponentBackground;

    public PreviewNodeFigure(PreviewNode model)
    {
        this.model = model;
        setOpaque(true);
        setLayoutManager(new XYLayout());
        applyBorder();
    }

    public void setModel(PreviewNode model)
    {
        this.model = model;
        applyBorder();
        repaint();
    }

    @Override
    public void removeNotify()
    {
        if (appComponentBackground != null && !appComponentBackground.isDisposed())
        {
            appComponentBackground.dispose();
        }
        super.removeNotify();
    }

    @Override
    protected void paintFigure(Graphics graphics)
    {
        Rectangle area = getClientArea().getCopy();
        EJDevPreviewKind kind = model.getKind();

        graphics.setBackgroundColor(background(kind));
        graphics.fillRectangle(area);
        if (model.isPaintBorder())
        {
            graphics.setForegroundColor(ColorConstants.gray);
            graphics.drawRectangle(area.x, area.y, area.width - 1, area.height - 1);
        }

        paintKind(graphics, contentArea(area), kind);
        if (kind == EJDevPreviewKind.SPLIT)
        {
            drawSplitDividers(graphics, area);
        }
    }

    /**
     * The rectangle {@link #paintKind} draws into. Kept in one place so that hit testing
     * (see {@link #getTabIndexAt}) uses exactly the same geometry as painting.
     */
    private Rectangle contentArea(Rectangle area)
    {
        Rectangle content = area.getCopy();
        if (model.isPaintBorder())
        {
            content.shrink(5, 5);
        }
        return content;
    }

    private void paintKind(Graphics graphics, Rectangle area, EJDevPreviewKind kind)
    {
        String text = model.getDisplayName();
        switch (kind)
        {
            case TEXT_FIELD:
            case NUMBER_FIELD:
            case DATE_FIELD:
            case DATE_TIME_FIELD:
                if (model.isPaintControlLabel())
                {
                    drawTitle(graphics, area, text);
                    drawInsetBox(graphics, area.x, area.y + 18, area.width, 22);
                }
                else
                {
                    drawInsetBox(graphics, area.x, area.y, area.width, Math.max(1, Math.min(22, area.height)));
                }
                break;
            case TEXT_AREA:
                if (model.isPaintControlLabel())
                {
                    drawTitle(graphics, area, text);
                    drawInsetBox(graphics, area.x, area.y + 18, area.width, Math.max(34, area.height - 20));
                    drawLine(graphics, area.x + 6, area.y + 31, area.x + area.width - 8, area.y + 31);
                    drawLine(graphics, area.x + 6, area.y + 43, area.x + area.width - 24, area.y + 43);
                }
                else
                {
                    drawInsetBox(graphics, area.x, area.y, area.width, Math.max(34, area.height));
                    drawLine(graphics, area.x + 6, area.y + 13, area.x + area.width - 8, area.y + 13);
                    drawLine(graphics, area.x + 6, area.y + 25, area.x + area.width - 24, area.y + 25);
                }
                break;
            case HTML:
                if (isHtmlViewRenderer())
                {
                    drawInsetBox(graphics, area.x, area.y, area.width, Math.max(34, area.height));
                    graphics.setForegroundColor(ColorConstants.black);
                    graphics.drawText(text, area.x + 5, area.y + 5);
                }
                else if (model.isPaintControlLabel())
                {
                    drawTitle(graphics, area, text);
                    drawInsetBox(graphics, area.x, area.y + 18, area.width, Math.max(34, area.height - 20));
                    drawLine(graphics, area.x + 6, area.y + 31, area.x + area.width - 8, area.y + 31);
                    drawLine(graphics, area.x + 6, area.y + 43, area.x + area.width - 24, area.y + 43);
                }
                else
                {
                    drawInsetBox(graphics, area.x, area.y, area.width, Math.max(34, area.height));
                    drawLine(graphics, area.x + 6, area.y + 13, area.x + area.width - 8, area.y + 13);
                    drawLine(graphics, area.x + 6, area.y + 25, area.x + area.width - 24, area.y + 25);
                }
                break;
            case COMBO:
                if (model.isPaintControlLabel())
                {
                    drawTitle(graphics, area, text);
                    drawInsetBox(graphics, area.x, area.y + 18, area.width, 22);
                    graphics.drawLine(area.x + area.width - 18, area.y + 22, area.x + area.width - 10, area.y + 22);
                    graphics.drawLine(area.x + area.width - 18, area.y + 22, area.x + area.width - 14, area.y + 29);
                    graphics.drawLine(area.x + area.width - 10, area.y + 22, area.x + area.width - 14, area.y + 29);
                }
                else
                {
                    drawInsetBox(graphics, area.x, area.y, area.width, Math.max(1, Math.min(22, area.height)));
                    graphics.drawLine(area.x + area.width - 18, area.y + 4, area.x + area.width - 10, area.y + 4);
                    graphics.drawLine(area.x + area.width - 18, area.y + 4, area.x + area.width - 14, area.y + 11);
                    graphics.drawLine(area.x + area.width - 10, area.y + 4, area.x + area.width - 14, area.y + 11);
                }
                break;
            case CHECKBOX:
                drawInsetBox(graphics, area.x, area.y + 2, 13, 13);
                graphics.drawText(text, area.x + 20, area.y);
                break;
            case RADIO_GROUP:
                drawRadioGroup(graphics, area, text);
                break;
            case BUTTON:
                if (model.isPaintControlLabel())
                {
                    drawInsetBox(graphics, area.x, area.y + 4, Math.min(area.width, 110), 26);
                    graphics.drawText(text, area.x + 10, area.y + 10);
                }
                else
                {
                    int height = Math.max(1, Math.min(24, area.height));
                    drawInsetBox(graphics, area.x, area.y, Math.max(1, area.width), height);
                    graphics.drawText(text, area.x + 5, area.y + Math.max(2, (height - 14) / 2));
                }
                break;
            case LABEL:
                graphics.drawText(text, alignedTextX(area, text), area.y + 3);
                break;
            case TABLE:
            case TREE:
            case LIST:
                drawTableLikeRenderer(graphics, area, kind, text);
                break;
            case IMAGE:
                drawTitle(graphics, area, text);
                drawInsetBox(graphics, area.x, area.y + 18, area.width, Math.max(36, area.height - 20));
                graphics.drawLine(area.x + 8, area.y + area.height - 8, area.x + area.width / 2, area.y + 42);
                graphics.drawLine(area.x + area.width / 2, area.y + 42, area.x + area.width - 8, area.y + area.height - 8);
                graphics.drawOval(area.x + area.width - 30, area.y + 26, 12, 12);
                break;
            case CHART:
                drawTitle(graphics, area, text);
                drawInsetBox(graphics, area.x, area.y + 18, area.width, Math.max(42, area.height - 20));
                int baseLine = area.y + area.height - 8;
                graphics.drawLine(area.x + 10, baseLine, area.x + area.width - 8, baseLine);
                graphics.drawLine(area.x + 10, area.y + 28, area.x + 10, baseLine);
                // drawInsetBox leaves the background white; the bars would be invisible without this.
                graphics.setBackgroundColor(ColorConstants.lightGray);
                graphics.fillRectangle(area.x + 22, baseLine - 20, 14, 20);
                graphics.fillRectangle(area.x + 46, baseLine - 34, 14, 34);
                graphics.fillRectangle(area.x + 70, baseLine - 26, 14, 26);
                break;
            case SPLIT:
                if (shouldPaintContainerTitle())
                {
                    drawTitle(graphics, area, text);
                }
                if (model.getChildren().isEmpty())
                {
                    drawEmptySplitDivider(graphics, area);
                }
                break;
            case TAB_FOLDER:
                if (shouldPaintContainerTitle())
                {
                    drawTitle(graphics, area, text);
                }
                drawTabs(graphics, area, tabStripOffset());
                break;
            case STACKED:
                drawTitle(graphics, area, "<STACKED_COMPONENT>");
                drawTabs(graphics, area, tabStripOffset());
                break;
            case DRAWER:
                drawDrawer(graphics, area);
                break;
            case FORM_CONTAINER:
            case CANVAS:
            case GROUP:
            case BLOCK:
                if (shouldPaintContainerTitle())
                {
                    if (model.getTitleBarMode() == PreviewNode.TITLE_BAR_NONE)
                    {
                        drawTitle(graphics, area, text);
                    }
                    else
                    {
                        drawSectionTitleBar(graphics, area, text);
                    }
                }
                break;
            case TAB_PAGE:
                drawTitle(graphics, area, text);
                graphics.drawRectangle(area.x, area.y + 18, Math.min(70, area.width - 1), 18);
                graphics.drawText("Tab", area.x + 8, area.y + 21);
                break;
            case APP_COMPONENT:
            case MENU_TREE:
            case TOOLBAR:
            case STATUS_BAR:
            case BANNER:
                drawAppComponent(graphics, area);
                break;
            case SEPARATOR:
                if (model.isPaintControlLabel())
                {
                    graphics.drawText(text, area.x, area.y);
                    graphics.drawLine(area.x, area.y + 18, area.x + area.width, area.y + 18);
                }
                else if (model.isVerticalOrientation())
                {
                    int x = area.x + Math.max(0, area.width / 2);
                    graphics.setForegroundColor(ColorConstants.gray);
                    graphics.drawLine(x, area.y, x, area.y + area.height);
                }
                else
                {
                    int y = area.y + Math.max(0, area.height / 2);
                    graphics.setForegroundColor(ColorConstants.gray);
                    graphics.drawLine(area.x, y, area.x + area.width, y);
                }
                break;
            case SPACE:
            case SPACER:
                if (model.isPaintControlLabel())
                {
                    graphics.setForegroundColor(ColorConstants.lightGray);
                    graphics.drawLine(area.x, area.y, area.x + area.width, area.y + area.height);
                    graphics.drawLine(area.x, area.y + area.height, area.x + area.width, area.y);
                    graphics.setForegroundColor(ColorConstants.gray);
                    graphics.drawText(text, area.x + 4, area.y + 4);
                }
                break;
            default:
                drawUnknownRenderer(graphics, area, text);
                break;
        }
    }

    private void drawUnknownRenderer(Graphics graphics, Rectangle area, String text)
    {
        drawInsetBox(graphics, area.x, area.y, Math.max(1, area.width), Math.max(24, area.height));
        graphics.setForegroundColor(ColorConstants.black);
        graphics.drawText(text, area.x + 5, area.y + 5);

        String renderer = model.getDescriptor().getRendererName();
        if (renderer != null && renderer.trim().length() > 0 && !renderer.equals(text))
        {
            graphics.setForegroundColor(ColorConstants.darkGray);
            graphics.drawText(renderer, area.x + 5, area.y + 21);
        }
    }

    private void drawTitle(Graphics graphics, Rectangle area, String text)
    {
        graphics.setForegroundColor(ColorConstants.black);
        graphics.drawText(text, area.x + 2, area.y + 1);
    }

    /**
     * An Eclipse Forms Section title bar: the expand affordance (twistie triangle or tree-node
     * plus/minus box), the title, and the rule under it.
     */
    private void drawSectionTitleBar(Graphics graphics, Rectangle area, String text)
    {
        int textX = area.x + 2;
        int mode = model.getTitleBarMode();
        boolean expanded = model.isTitleBarExpanded();

        if (mode == PreviewNode.TITLE_BAR_TWISTIE)
        {
            int top = area.y + 3;
            graphics.setForegroundColor(ColorConstants.darkGray);
            graphics.setBackgroundColor(ColorConstants.darkGray);
            int[] points = expanded ? new int[] { area.x + 2, top + 2, area.x + 12, top + 2, area.x + 7, top + 9 }
                    : new int[] { area.x + 3, top, area.x + 10, top + 5, area.x + 3, top + 10 };
            graphics.fillPolygon(points);
            textX = area.x + 16;
        }
        else if (mode == PreviewNode.TITLE_BAR_TREE_NODE)
        {
            graphics.setForegroundColor(ColorConstants.gray);
            graphics.drawRectangle(area.x + 2, area.y + 3, 10, 10);
            graphics.setForegroundColor(ColorConstants.black);
            graphics.drawLine(area.x + 4, area.y + 8, area.x + 10, area.y + 8);
            if (!expanded)
            {
                graphics.drawLine(area.x + 7, area.y + 5, area.x + 7, area.y + 11);
            }
            textX = area.x + 16;
        }

        graphics.setForegroundColor(ColorConstants.black);
        graphics.drawText(text, textX, area.y + 1);
        graphics.setForegroundColor(ColorConstants.buttonDarker);
        graphics.drawLine(area.x, area.y + 16, area.x + area.width, area.y + 16);
    }

    /**
     * The radio group's real choices when the renderer supplied them, honouring the horizontal or
     * vertical row layout and the optional titled frame.
     */
    private void drawRadioGroup(Graphics graphics, Rectangle area, String text)
    {
        Rectangle body = area.getCopy();
        if (model.isOptionsFramed())
        {
            graphics.setForegroundColor(ColorConstants.gray);
            graphics.drawRectangle(area.x, area.y + 6, Math.max(1, area.width - 1), Math.max(1, area.height - 7));
            if (text != null)
            {
                graphics.setBackgroundColor(background(model.getKind()));
                graphics.fillRectangle(area.x + 6, area.y, textWidth(text) + 4, 12);
                graphics.setForegroundColor(ColorConstants.black);
                graphics.drawText(text, area.x + 8, area.y);
            }
            body.shrink(6, 0);
            body.y += 12;
        }
        else
        {
            drawTitle(graphics, area, text);
            body.y += 18;
        }

        List<String> options = model.getOptionLabels();
        if (options.isEmpty())
        {
            options = DEFAULT_OPTIONS;
        }

        boolean horizontal = !model.isVerticalOrientation();
        int x = body.x + 2;
        int y = body.y;
        for (String option : options)
        {
            if (horizontal && x + 14 + textWidth(option) > body.x + body.width)
            {
                break;
            }
            if (!horizontal && y + 12 > body.y + body.height)
            {
                break;
            }
            graphics.setForegroundColor(ColorConstants.gray);
            graphics.drawOval(x, y + 2, 10, 10);
            graphics.setForegroundColor(ColorConstants.black);
            graphics.drawText(option, x + 16, y);
            if (horizontal)
            {
                x += 16 + textWidth(option) + 10;
            }
            else
            {
                y += 16;
            }
        }
    }

    private void applyBorder()
    {
        setBorder(model != null && model.isPaintBorder() ? new LineBorder(ColorConstants.gray) : null);
    }

    private void drawInsetBox(Graphics graphics, int x, int y, int width, int height)
    {
        graphics.setBackgroundColor(ColorConstants.white);
        graphics.fillRectangle(x, y, Math.max(1, width), Math.max(1, height));
        graphics.setForegroundColor(ColorConstants.gray);
        graphics.drawRectangle(x, y, Math.max(1, width) - 1, Math.max(1, height) - 1);
    }

    private boolean shouldPaintContainerTitle()
    {
        return model.isPaintContainerTitle();
    }

    /**
     * Vertical offset of the tab strip from the top of the content area. STACKED always draws a
     * title above its tabs; TAB_FOLDER only when it paints a container title.
     */
    private int tabStripOffset()
    {
        if (model.getKind() == EJDevPreviewKind.STACKED)
        {
            return 18;
        }
        return shouldPaintContainerTitle() ? 18 : 0;
    }

    /**
     * Y coordinate of the simulated tab strip. Shared by painting and hit testing.
     */
    private int tabStripY(Rectangle area, int offset)
    {
        return model.isTabsAtBottom() ? area.y + Math.max(0, area.height - 21) : area.y + offset;
    }

    private void drawTabs(Graphics graphics, Rectangle area, int offset)
    {
        int tabX = area.x;
        int tabY = tabStripY(area, offset);
        int maxRight = area.x + area.width - 1;
        int selectedIndex = model.getSelectedTabIndex();
        for (int i = 0; i < model.getTabLabels().size(); i++)
        {
            String label = model.getTabLabels().get(i);
            int width = Math.max(48, Math.min(120, (label.length() * 7) + 18));
            if (tabX + width > maxRight)
            {
                break;
            }
            boolean selected = i == selectedIndex;
            graphics.setBackgroundColor(selected ? ColorConstants.white : ColorConstants.buttonLightest);
            graphics.fillRectangle(tabX, tabY, width, 20);
            graphics.setForegroundColor(ColorConstants.gray);
            graphics.drawRectangle(tabX, tabY, width, 20);
            graphics.setForegroundColor(ColorConstants.black);
            graphics.drawText(label, tabX + 6, tabY + 3);
            if (selected)
            {
                // Erase the folder edge under (or above) the active tab so it reads as connected.
                int edgeY = model.isTabsAtBottom() ? tabY : tabY + 20;
                graphics.setForegroundColor(ColorConstants.white);
                graphics.drawLine(tabX + 1, edgeY, tabX + width - 1, edgeY);
            }
            tabX += width - 1;
        }
        if (model.getTabLabels().isEmpty())
        {
            graphics.drawRectangle(area.x, tabY, Math.min(70, area.width - 1), 20);
            graphics.drawText("Tab", area.x + 8, tabY + 3);
        }
        graphics.setForegroundColor(ColorConstants.gray);
        int edgeY = model.isTabsAtBottom() ? tabY - 1 : tabY + 21;
        graphics.drawLine(area.x, edgeY, area.x + area.width, edgeY);
    }

    /**
     * Draws a sash line in the gutter between each pair of adjacent panes, mirroring the
     * SashForm the SWT preview builds. Child bounds are model-absolute, so they are mapped
     * onto the figure's client area via the parent's model origin.
     */
    private void drawSplitDividers(Graphics graphics, Rectangle area)
    {
        List<PreviewNode> children = model.getVisibleChildren();
        if (children.size() < 2)
        {
            return;
        }

        boolean horizontal = model.getColumns() > 1;
        int originX = model.getBounds().getX();
        int originY = model.getBounds().getY();

        graphics.setForegroundColor(ColorConstants.buttonDarker);
        for (int i = 0; i < children.size() - 1; i++)
        {
            PreviewBounds before = children.get(i).getBounds();
            PreviewBounds after = children.get(i + 1).getBounds();
            if (horizontal)
            {
                int endOfBefore = before.getX() + before.getWidth();
                int x = area.x + ((endOfBefore + after.getX()) / 2) - originX;
                graphics.drawLine(x, area.y, x, area.y + area.height);
            }
            else
            {
                int endOfBefore = before.getY() + before.getHeight();
                int y = area.y + ((endOfBefore + after.getY()) / 2) - originY;
                graphics.drawLine(area.x, y, area.x + area.width, y);
            }
        }
    }

    private void drawEmptySplitDivider(Graphics graphics, Rectangle area)
    {
        graphics.setForegroundColor(ColorConstants.gray);
        int top = shouldPaintContainerTitle() ? area.y + 19 : area.y;
        if (model.isVerticalOrientation())
        {
            int y = top + Math.max(0, (area.y + area.height - top) / 2);
            graphics.drawLine(area.x, y, area.x + area.width, y);
        }
        else
        {
            int x = area.x + area.width / 2;
            graphics.drawLine(x, top, x, area.y + area.height - 1);
        }
    }

    private int alignedTextX(Rectangle area, String text)
    {
        if (text == null || model.getTextAlignment() == PreviewNode.TEXT_ALIGN_LEFT)
        {
            return area.x;
        }
        int slack = Math.max(0, area.width - textWidth(text));
        return model.getTextAlignment() == PreviewNode.TEXT_ALIGN_CENTER ? area.x + (slack / 2) : area.x + slack;
    }

    private int textWidth(String text)
    {
        if (text == null)
        {
            return 0;
        }
        // getFont() is only guaranteed once the figure is attached to a viewer.
        return getFont() == null ? text.length() * 7 : FigureUtilities.getTextWidth(text, getFont());
    }

    private boolean isHtmlViewRenderer()
    {
        String renderer = model.getDescriptor().getRendererName();
        if (renderer == null)
        {
            return false;
        }
        String normalized = renderer.toLowerCase();
        return normalized.indexOf("htmlview") > -1 || normalized.indexOf("html view") > -1 || normalized.indexOf("exthtmlview") > -1;
    }

    private void drawTableLikeRenderer(Graphics graphics, Rectangle area, EJDevPreviewKind kind, String text)
    {
        List<String> labels = model.getColumnLabels();
        if (labels.isEmpty())
        {
            drawTitle(graphics, area, text);
            drawInsetBox(graphics, area.x, area.y + 18, area.width, Math.max(36, area.height - 20));
            if (kind == EJDevPreviewKind.LIST)
            {
                graphics.drawLine(area.x + 6, area.y + 54, area.x + area.width - 8, area.y + 54);
                graphics.drawLine(area.x + 6, area.y + 69, area.x + area.width - 8, area.y + 69);
            }
            else
            {
                graphics.drawLine(area.x, area.y + 39, area.x + area.width, area.y + 39);
                graphics.drawLine(area.x + area.width / 3, area.y + 18, area.x + area.width / 3, area.y + area.height - 2);
                graphics.drawLine(area.x + (area.width * 2) / 3, area.y + 18, area.x + (area.width * 2) / 3, area.y + area.height - 2);
            }
            return;
        }

        int tableHeight = Math.max(36, area.height);
        int headerHeight = 28;
        int rowHeight = 22;
        int right = area.x + Math.max(1, area.width) - 1;
        int bottom = area.y + tableHeight - 1;

        drawInsetBox(graphics, area.x, area.y, area.width, tableHeight);

        graphics.setForegroundColor(ColorConstants.lightGray);
        graphics.drawLine(area.x, area.y + headerHeight, right, area.y + headerHeight);
        for (int y = area.y + headerHeight + rowHeight; y < bottom; y += rowHeight)
        {
            graphics.drawLine(area.x, y, right, y);
        }

        graphics.setForegroundColor(ColorConstants.gray);
        int count = Math.max(1, labels.size());
        for (int i = 1; i < count; i++)
        {
            int x = area.x + (area.width * i) / count;
            graphics.drawLine(x, area.y, x, bottom);
        }

        graphics.setForegroundColor(ColorConstants.black);
        for (int i = 0; i < labels.size(); i++)
        {
            int left = area.x + (area.width * i) / count;
            int next = area.x + (area.width * (i + 1)) / count;
            int maxChars = Math.max(1, (next - left - 12) / 7);
            String label = shortText(labels.get(i), maxChars);
            int slack = Math.max(0, (next - left) - 14 - textWidth(label));
            int offset;
            switch (model.getColumnAlignment(i))
            {
                case PreviewNode.TEXT_ALIGN_CENTER:
                    offset = slack / 2;
                    break;
                case PreviewNode.TEXT_ALIGN_RIGHT:
                    offset = slack;
                    break;
                default:
                    offset = 0;
                    break;
            }
            graphics.drawText(label, left + 7 + offset, area.y + 6);
        }
    }

    public int getTabIndexAt(int x, int y)
    {
        if (model.getKind() != EJDevPreviewKind.TAB_FOLDER && model.getKind() != EJDevPreviewKind.STACKED)
        {
            return -1;
        }

        Rectangle area = contentArea(getClientArea().getCopy());
        int tabX = area.x;
        int tabY = tabStripY(area, tabStripOffset());
        if (y < tabY || y > tabY + 20)
        {
            return -1;
        }

        int maxRight = area.x + area.width - 1;
        for (int i = 0; i < model.getTabLabels().size(); i++)
        {
            String label = model.getTabLabels().get(i);
            int width = Math.max(48, Math.min(120, (label.length() * 7) + 18));
            if (tabX + width > maxRight)
            {
                return -1;
            }
            if (x >= tabX && x <= tabX + width)
            {
                return i;
            }
            tabX += width - 1;
        }
        return -1;
    }

    private void drawDrawer(Graphics graphics, Rectangle area)
    {
        int y = area.y;
        for (String label : model.getTabLabels())
        {
            int height = Math.max(22, Math.min(80, (label.length() * 7) + 18));
            if (y + height > area.y + area.height)
            {
                break;
            }
            graphics.setBackgroundColor(ColorConstants.buttonLightest);
            graphics.fillRectangle(area.x, y, Math.min(32, area.width), height);
            graphics.setForegroundColor(ColorConstants.gray);
            graphics.drawRectangle(area.x, y, Math.min(32, area.width) - 1, height - 1);
            graphics.setForegroundColor(ColorConstants.black);
            graphics.drawText(shortLabel(label), area.x + 4, y + 4);
            y += height + 1;
        }
    }

    private String shortLabel(String label)
    {
        if (label == null)
        {
            return "";
        }
        return label.length() > 3 ? label.substring(0, 3) : label;
    }

    private String shortText(String text, int maxChars)
    {
        if (text == null)
        {
            return "";
        }
        if (text.length() <= maxChars)
        {
            return text;
        }
        if (maxChars <= 3)
        {
            return text.substring(0, maxChars);
        }
        return text.substring(0, maxChars - 3) + "...";
    }

    private void drawAppComponent(Graphics graphics, Rectangle area)
    {
        graphics.setForegroundColor(ColorConstants.black);
        graphics.drawText(appComponentLabel(), area.x + 2, area.y + 2);
    }

    private String appComponentLabel()
    {
        String label = model.getDisplayName();
        String renderer = model.getDescriptor().getRendererName();
        if (renderer != null && renderer.trim().length() > 0)
        {
            if (label == null || label.equals(model.getDescriptor().getName()) || label.equals(model.getKind().name()))
            {
                return String.format("<%s>", renderer);
            }
        }
        return label == null ? "<component>" : label;
    }

    private void drawLine(Graphics graphics, int x1, int y1, int x2, int y2)
    {
        graphics.setForegroundColor(ColorConstants.lightGray);
        graphics.drawLine(x1, y1, x2, y2);
        graphics.setForegroundColor(ColorConstants.gray);
    }

    private org.eclipse.swt.graphics.Color background(EJDevPreviewKind kind)
    {
        if (!model.isPaintBorder() && !model.isPaintContainerTitle())
        {
            return ColorConstants.white;
        }
        switch (kind)
        {
            case FORM_CONTAINER:
            case CANVAS:
            case GROUP:
            case BLOCK:
            case DRAWER:
            case STACKED:
                return ColorConstants.listBackground;
            case APP_COMPONENT:
            case MENU_TREE:
            case TOOLBAR:
            case STATUS_BAR:
            case BANNER:
                return appComponentBackground();
            case INSERT_SCREEN:
            case UPDATE_SCREEN:
            case QUERY_SCREEN:
                return ColorConstants.buttonLightest;
            case UNKNOWN:
                return ColorConstants.tooltipBackground;
            default:
                return ColorConstants.white;
        }
    }

    private Color appComponentBackground()
    {
        if (appComponentBackground == null || appComponentBackground.isDisposed())
        {
            appComponentBackground = new Color(Display.getDefault(), new RGB(255, 170, 170));
        }
        return appComponentBackground;
    }
}
