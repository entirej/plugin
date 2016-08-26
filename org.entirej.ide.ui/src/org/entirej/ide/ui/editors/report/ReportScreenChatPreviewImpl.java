/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.report;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;

public class ReportScreenChatPreviewImpl implements IReportPreviewProvider
{
    protected final Color                          COLOR_BLOCK        = new Color(Display.getCurrent(), new RGB(255, 251, 227));
    protected final Color                          COLOR_BLOCK_ITEM   = new Color(Display.getCurrent(), new RGB(240, 240, 240));
    protected final Color                          COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    protected final Color                          COLOR_WHITE        = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final Color                          COLOR_LIGHT_SHADOW = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);
    protected final Cursor                         RESIZE             = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZESE);
    protected final Cursor                         MOVE               = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);
    protected final EJPluginReportScreenProperties properties;

    private int                                    x, y;

    public ReportScreenChatPreviewImpl(EJPluginReportScreenProperties properties)
    {
        this.properties = properties;
    }

    public void dispose()
    {
        COLOR_BLOCK_ITEM.dispose();
        COLOR_BLOCK.dispose();
        RESIZE.dispose();
        MOVE.dispose();
    }

    protected int getHeight()
    {

        return properties.getHeight();
    }

    protected EJPluginReportProperties getReportProperties(AbstractEJReportEditor editor)
    {
        return editor.getReportProperties();
    }

    void updateSize(final AbstractEJReportEditor editor, EJPluginReportScreenProperties layoutScreenProperties, Composite parent)
    {

        int width = layoutScreenProperties.getWidth();
        int height = getHeight();

        boolean updated = false;
        Control[] children = parent.getChildren();
        for (Control control : children)
        {
            if (width < (control.getBounds().x + control.getBounds().width))
            {
                width = (control.getBounds().x + control.getBounds().width);
                updated = true;
            }
            if (height < (control.getBounds().y + control.getBounds().height))
            {
                height = (control.getBounds().y + control.getBounds().height);
                updated = true;
            }
        }

        if (updated)
        {
            parent.setBounds(parent.getBounds().x, parent.getBounds().y, width, height);
        }
    }

    public void buildPreview(final AbstractEJReportEditor editor, ScrolledComposite previewComposite,Object o )
    {
        // layout canvas preview
        Composite pContent = new Composite(previewComposite, SWT.NONE);

        // EJPluginReportProperties formProperties =
        // getReportProperties(editor);

        previewComposite.setContent(pContent);
        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

        pContent.setLayout(null);
        setPreviewBackground(pContent, COLOR_LIGHT_YELLOW);

        final Composite reportBody = new Composite(pContent, SWT.BORDER);
        reportBody.setLayout(null);
        setPreviewBackground(reportBody, COLOR_WHITE);

        final EJPluginReportScreenProperties layoutScreenProperties = properties;

        reportBody.setBounds(10, 10, layoutScreenProperties.getWidth(), getHeight());

        previewComposite.setMinSize(layoutScreenProperties.getWidth() + 20, getHeight() + 20);// add
                                                                                              // offset
        Label info = new Label(reportBody, SWT.NONE);
        info.setText(properties.getChartProperties().getChartType().toString());
        info.setBounds(0, 0, layoutScreenProperties.getWidth(), 25);

        updateSize(editor, layoutScreenProperties, reportBody);
    }

    protected void setPreviewBackground(Control control, Color color)
    {
        control.setBackground(color);
    }

    public String getDescription()
    {
        return "preview the defined canvas layout in form.";
    }

}
