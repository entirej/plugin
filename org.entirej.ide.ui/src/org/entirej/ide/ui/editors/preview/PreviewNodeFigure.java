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

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.Figure;
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

        Rectangle content = area.getCopy();
        if (model.isPaintBorder())
        {
            content.shrink(5, 5);
        }
        paintKind(graphics, content, kind);
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
            case HTML:
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
                drawTitle(graphics, area, text);
                graphics.drawOval(area.x + 2, area.y + 22, 10, 10);
                graphics.drawText("Option", area.x + 18, area.y + 19);
                graphics.drawOval(area.x + 2, area.y + 38, 10, 10);
                graphics.drawText("Option", area.x + 18, area.y + 35);
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
                graphics.drawText(text, area.x, area.y + 3);
                break;
            case TABLE:
            case TREE:
            case LIST:
                drawTitle(graphics, area, text);
                drawInsetBox(graphics, area.x, area.y + 18, area.width, Math.max(36, area.height - 20));
                graphics.drawLine(area.x, area.y + 39, area.x + area.width, area.y + 39);
                if (kind != EJDevPreviewKind.LIST)
                {
                    graphics.drawLine(area.x + area.width / 3, area.y + 18, area.x + area.width / 3, area.y + area.height - 2);
                    graphics.drawLine(area.x + (area.width * 2) / 3, area.y + 18, area.x + (area.width * 2) / 3, area.y + area.height - 2);
                }
                else
                {
                    graphics.drawLine(area.x + 6, area.y + 54, area.x + area.width - 8, area.y + 54);
                    graphics.drawLine(area.x + 6, area.y + 69, area.x + area.width - 8, area.y + 69);
                }
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
                graphics.fillRectangle(area.x + 22, baseLine - 20, 14, 20);
                graphics.fillRectangle(area.x + 46, baseLine - 34, 14, 34);
                graphics.fillRectangle(area.x + 70, baseLine - 26, 14, 26);
                break;
            case SPLIT:
                if (shouldPaintContainerTitle())
                {
                    drawTitle(graphics, area, text);
                    graphics.drawLine(area.x + area.width / 2, area.y + 19, area.x + area.width / 2, area.y + area.height - 1);
                }
                break;
            case TAB_FOLDER:
                if (shouldPaintContainerTitle())
                {
                    drawTitle(graphics, area, text);
                }
                drawTabs(graphics, area, shouldPaintContainerTitle() ? 18 : 0);
                break;
            case STACKED:
                drawTitle(graphics, area, "<STACKED_COMPONENT>");
                drawTabs(graphics, area, 18);
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
                    drawTitle(graphics, area, text);
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
                drawTitle(graphics, area, text);
                String renderer = model.getDescriptor().getRendererName();
                if (renderer != null && renderer.trim().length() > 0)
                {
                    graphics.drawText(renderer, area.x + 2, area.y + 18);
                }
                break;
        }
    }

    private void drawTitle(Graphics graphics, Rectangle area, String text)
    {
        graphics.setForegroundColor(ColorConstants.black);
        graphics.drawText(text, area.x + 2, area.y + 1);
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

    private void drawTabs(Graphics graphics, Rectangle area, int offset)
    {
        int tabX = area.x;
        int tabY = area.y + offset;
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
                graphics.setForegroundColor(ColorConstants.white);
                graphics.drawLine(tabX + 1, tabY + 20, tabX + width - 1, tabY + 20);
            }
            tabX += width - 1;
        }
        if (model.getTabLabels().isEmpty())
        {
            graphics.drawRectangle(area.x, tabY, Math.min(70, area.width - 1), 20);
            graphics.drawText("Tab", area.x + 8, tabY + 3);
        }
        graphics.setForegroundColor(ColorConstants.gray);
        graphics.drawLine(area.x, tabY + 21, area.x + area.width, tabY + 21);
    }

    public int getTabIndexAt(int x, int y)
    {
        if (model.getKind() != EJDevPreviewKind.TAB_FOLDER && model.getKind() != EJDevPreviewKind.STACKED)
        {
            return -1;
        }

        Rectangle area = getClientArea().getCopy().shrink(5, 5);
        int tabX = area.x;
        int tabY = area.y + (shouldPaintContainerTitle() ? 18 : 0);
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
