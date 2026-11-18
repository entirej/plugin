/*******************************************************************************
 * Copyright 2026 CRESOFT AG
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy at http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by law or agreed in writing, software is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND.
 ******************************************************************************/
package org.entirej.ide.ui.editors.preview;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.draw2d.FigureUtilities;
import org.eclipse.swt.widgets.Display;

/**
 * Read-only preview metrics for the simple widget rules used by RAP themes. This is deliberately
 * not a browser/CSS engine: descendant selectors, imports, animations and calculated lengths are
 * ignored. Supported selectors are widget/sub-element names, SWT attributes, custom variants and
 * pseudo states. Normal CSS specificity and source order apply to supported rules.
 */
public final class PreviewTheme
{
    // Geometry defaults from entirej-rwt-rap/src/theme/default.css. Project rules are layered last.
    private static final String RAP_DEFAULTS = """
        * { font: 11px Verdana; padding: 0; border: none; }
        Composite { padding: 0; border: none; }
        Composite[BORDER] { border: 1px solid gray; }
        Button { padding: 4px 4px; spacing: 2px; }
        Button[PUSH] { border: 1px solid gray; }
        Button[CHECK], Button[RADIO] { padding: 3px 3px 3px 0; spacing: 7px; }
        Text, Combo-Field, Spinner-Field, DateTime-Field { padding: 2px; }
        Text[MULTI] { padding: 5px 10px; }
        Text[BORDER], Combo[BORDER], DateTime[BORDER] { border: 1px solid gray; }
        Group-Frame { margin: 20px 0 0; padding: 8px 0 0; border: 1px solid gray; }
        Group-Label { padding: 2px 5px; margin: 10px; border: 1px solid gray; }
        CTabItem { padding: 6px 15px; spacing: 10px; }
        """;
    private static final Pattern BLOCK = Pattern.compile("([^{}]+)\\{([^{}]*)\\}");
    private static final Pattern TOKEN = Pattern.compile("^(\\*|[\\w-]+)?((?:\\.[\\w-]+|\\[[\\w-]+\\]|:[\\w-]+)*)$");
    private static final Pattern QUALIFIER = Pattern.compile("\\.[\\w-]+|\\[[\\w-]+\\]|:[\\w-]+");
    private final List<Rule> rules = new ArrayList<Rule>();

    public static PreviewTheme rapDefault() { return fromCss(); }

    public static PreviewTheme fromCss(String... stylesheets)
    {
        PreviewTheme theme = new PreviewTheme();
        theme.append(RAP_DEFAULTS);
        for (String css : stylesheets) if (css != null) theme.append(css);
        return theme;
    }

    private void append(String css)
    {
        css = css.replaceAll("(?s)/\\*.*?\\*/", "");
        Matcher blocks = BLOCK.matcher(css);
        while (blocks.find())
        {
            for (String selector : blocks.group(1).split(","))
            {
                selector = selector.trim();
                Matcher token = TOKEN.matcher(selector);
                if (!token.matches()) continue;
                int specificity = selector.startsWith("*") || selector.startsWith(".") ? 0 : 1;
                Matcher qualifiers = QUALIFIER.matcher(selector);
                while (qualifiers.find()) specificity += 10;
                for (String declaration : blocks.group(2).split(";"))
                {
                    int colon = declaration.indexOf(':');
                    if (colon < 0) continue;
                    String value = declaration.substring(colon + 1).trim();
                    boolean important = value.toLowerCase(Locale.ROOT).endsWith("!important");
                    value = value.replaceFirst("(?i)\\s*!important$", "");
                    String property = declaration.substring(0, colon).trim().toLowerCase(Locale.ROOT);
                    if (!supportedValue(property, value)) continue;
                    rules.add(new Rule(selector, property, value,
                            specificity + (important ? 10000 : 0)));
                }
            }
        }
    }

    private boolean supportedValue(String property, String value)
    {
        switch (property)
        {
            case "padding":
                return value.matches("\\d+(?:px)?(?:\\s+\\d+(?:px)?){0,3}");
            case "margin":
                return value.matches("-?\\d+(?:px)?(?:\\s+-?\\d+(?:px)?){0,3}");
            case "spacing":
                return value.matches("\\d+(?:px)?");
            case "border":
                return "none".equals(value) || value.matches("\\d+(?:px)?(?:\\s+.*)?");
            case "font":
                return value.matches("(?i)(?:(?:bold|normal|italic)\\s+)?\\d+(?:\\.\\d+)?px\\s+.+");
            default:
                return false;
        }
    }

    public String property(String widget, String variant, String property)
    {
        String value = null;
        int specificity = -1;
        for (Rule rule : rules)
        {
            if (rule.property.equals(property) && rule.specificity >= specificity && matches(rule.selector, widget, variant))
            {
                value = rule.value;
                specificity = rule.specificity;
            }
        }
        return value;
    }

    private boolean matches(String selector, String widget, String variant)
    {
        Matcher pattern = TOKEN.matcher(selector);
        Matcher actual = TOKEN.matcher(widget);
        if (!pattern.matches() || !actual.matches()) return false;
        String type = pattern.group(1);
        if (type != null && !"*".equals(type) && !type.equalsIgnoreCase(actual.group(1))) return false;
        Matcher qualifiers = QUALIFIER.matcher(pattern.group(2));
        while (qualifiers.find())
        {
            String qualifier = qualifiers.group();
            if (qualifier.startsWith("."))
            {
                if (!qualifier.substring(1).equals(variant)) return false;
            }
            else if (!actual.group(2).contains(qualifier)) return false;
        }
        return true;
    }

    public int[] box(String widget, String variant, String property)
    {
        int[] box = new int[4];
        String value = property(widget, variant, property);
        if (value == null) return box;
        String[] values = value.trim().split("\\s+");
        if (values.length > 4) return box;
        int[] pixels = new int[values.length];
        for (int i = 0; i < values.length; i++)
        {
            if (!values[i].matches("-?\\d+(?:px)?")) return box;
            pixels[i] = Math.max(0, Integer.parseInt(values[i].replace("px", "")));
        }
        box[0] = pixels[0]; box[1] = pixels.length > 1 ? pixels[1] : pixels[0];
        box[2] = pixels.length > 2 ? pixels[2] : pixels[0]; box[3] = pixels.length > 3 ? pixels[3] : box[1];
        return box;
    }

    public int border(String widget, String variant)
    {
        String value = property(widget, variant, "border");
        if (value == null || "none".equals(value)) return 0;
        Matcher width = Pattern.compile("^(\\d+)(?:px)?(?:\\s|$)").matcher(value);
        return width.find() ? Integer.parseInt(width.group(1)) : 0;
    }

    public int spacing(String widget, String variant, int fallback)
    {
        String value = property(widget, variant, "spacing");
        return value != null && value.matches("\\d+(?:px)?") ? Integer.parseInt(value.replace("px", "")) : fallback;
    }

    public Font font(String widget, String variant)
    {
        Display display = Display.getCurrent();
        if (display == null) return null;
        String value = property(widget, variant, "font");
        Matcher font = Pattern.compile("(?i)(?:(bold|normal|italic)\\s+)?(\\d+(?:\\.\\d+)?)px\\s+([^,]+).*").matcher(value == null ? "" : value);
        if (!font.matches()) return display.getSystemFont();
        String family = font.group(3).replace("\"", "").replace("'", "").trim();
        int pixels = Math.max(1, (int) Math.round(Double.parseDouble(font.group(2))));
        int points = Math.max(1, Math.round(pixels * 72f / display.getDPI().y));
        int style = "bold".equalsIgnoreCase(font.group(1)) ? SWT.BOLD : "italic".equalsIgnoreCase(font.group(1)) ? SWT.ITALIC : SWT.NORMAL;
        String key = PreviewTheme.class.getName() + ":" + family + ":" + points + ":" + style;
        // The JFace registry owns/disposes these fonts; figures must not dispose shared fonts.
        if (!JFaceResources.getFontRegistry().hasValueFor(key))
            JFaceResources.getFontRegistry().put(key, new FontData[] { new FontData(family, points, style) });
        return JFaceResources.getFontRegistry().get(key);
    }

    public int textWidth(String widget, String variant, String text)
    {
        if (text == null || text.isEmpty()) return 0;
        if (Display.getCurrent() == null) return text.length() * 7;
        return FigureUtilities.getTextWidth(text, font(widget, variant));
    }

    public int textHeight(String widget, String variant)
    {
        if (Display.getCurrent() == null) return 18;
        return FigureUtilities.getTextExtents("Mg", font(widget, variant)).height;
    }

    public int characterWidth(String widget, String variant)
    {
        return Display.getCurrent() == null ? 8 : Math.max(1, (int) Math.ceil(textWidth(widget, variant, "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz") / 52.0));
    }

    public int controlHeight(String widget, String variant)
    {
        String field = widget.startsWith("Combo") ? "Combo-Field" : widget.startsWith("DateTime") ? "DateTime-Field" : widget;
        int[] padding = box(field, variant, "padding");
        return Math.max(widget.startsWith("Button[") ? 13 : 1, textHeight(widget, variant)) + padding[0] + padding[2] + 2 * border(widget, variant);
    }

    public int tabHeight()
    {
        int[] padding = box("CTabItem:selected", null, "padding");
        int[] normal = box("CTabItem", null, "padding");
        return Math.max(textHeight("CTabItem:selected", null) + padding[0] + padding[2],
                textHeight("CTabItem", null) + normal[0] + normal[2]) + 2;
    }

    public int[] groupInsets(int margin, String variant)
    {
        int[] padding = box("Group-Frame", variant, "padding");
        int[] frameMargin = box("Group-Frame", variant, "margin");
        int[] titlePadding = box("Group-Label", variant, "padding");
        int border = border("Group-Frame", variant);
        int title = textHeight("Group", variant) + titlePadding[0] + titlePadding[2];
        return new int[] { margin + Math.max(title, frameMargin[0] + padding[0] + border),
                margin + frameMargin[1] + padding[1] + border, margin + frameMargin[2] + padding[2] + border,
                margin + frameMargin[3] + padding[3] + border };
    }

    public int radioWidth(String text, String variant)
    {
        int[] padding = box("Button[RADIO]", variant, "padding");
        return 13 + spacing("Button[RADIO]", variant, 7) + padding[1] + padding[3] + textWidth("Button[RADIO]", variant, text);
    }

    public void applyPageInsets(PreviewNode node)
    {
        int title = node.isPaintContainerTitle() || node.getKind() == org.entirej.framework.dev.renderer.definition.EJDevPreviewKind.STACKED && node.isDesignHintsVisible()
                ? textHeight("Label", null) + 2 : 0;
        node.setLayoutInsets(title + (node.isTabsAtBottom() ? 1 : tabHeight() + 1), 1,
                node.isTabsAtBottom() ? tabHeight() + 1 : 1, 1);
    }

    private record Rule(String selector, String property, String value, int specificity) { }
}
