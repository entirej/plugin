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

import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.reports.enumerations.EJReportScreenType;

public class ReportBlockPreviewImpl implements IReportPreviewProvider
{
    protected final Color                         COLOR_BLOCK        = new Color(Display.getCurrent(), new RGB(255, 251, 227));
    protected final Color                         COLOR_BLOCK_ITEM   = new Color(Display.getCurrent(), new RGB(240,240,240));
    protected final Color                         COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    protected final Color                         COLOR_WHITE        = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final Color                         COLOR_LIGHT_SHADOW = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);

    protected final EJPluginReportBlockProperties properties;

    public ReportBlockPreviewImpl(EJPluginReportBlockProperties properties)
    {
        this.properties = properties;
    }

    public void dispose()
    {
        COLOR_BLOCK.dispose();
    }

    protected EJPluginReportProperties getReportProperties(AbstractEJReportEditor editor)
    {
        return editor.getReportProperties();
    }

    public void buildPreview(final AbstractEJReportEditor editor, ScrolledComposite previewComposite)
    {
        // layout canvas preview
        Composite pContent = new Composite(previewComposite, SWT.NONE);

        EJPluginReportProperties formProperties = getReportProperties(editor);
       
        previewComposite.setContent(pContent);
        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

        pContent.setLayout(null);
        setPreviewBackground(pContent, COLOR_LIGHT_YELLOW);

     

        Composite reportBody = new Composite(pContent, SWT.BORDER);
        reportBody.setLayout(null);
        setPreviewBackground(reportBody, COLOR_WHITE);

        EJPluginReportScreenProperties layoutScreenProperties = properties.getLayoutScreenProperties();

        reportBody.setBounds(10, 10, layoutScreenProperties.getWidth(),
                layoutScreenProperties.getHeight());

        List<EJPluginReportScreenItemProperties> allItemProperties = layoutScreenProperties.getScreenItemContainer().getAllItemProperties();
        for (EJPluginReportScreenItemProperties screenItemProperties : allItemProperties)
        {
            createItemPreview(editor, reportBody, screenItemProperties);
        }

        for (EJPluginReportBlockProperties properties : layoutScreenProperties.getAllSubBlocks())
        {
            createBlockPreview(editor,reportBody, properties);
        }

        
        
        previewComposite.setMinSize(layoutScreenProperties.getWidth() + 20, layoutScreenProperties.getHeight() + 20);// add offset

    }

    
    protected void createItemPreview(final AbstractEJReportEditor editor, Composite reportBody,final EJPluginReportScreenItemProperties screenItemProperties)
    {
        
        
        
            
            Composite block = new Composite(reportBody, SWT.BORDER);
            setPreviewBackground(block, COLOR_BLOCK_ITEM);
            
            block.setBounds(screenItemProperties.getX(), screenItemProperties.getY(), screenItemProperties.getWidth(), screenItemProperties.getHeight());
            
            block.setLayout(null);
            
            Label hint = new Label(block, SWT.NONE);
            hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]", screenItemProperties.getName(), screenItemProperties.getX(), screenItemProperties.getY(),
                    screenItemProperties.getWidth(), screenItemProperties.getHeight()));
            hint.setToolTipText(hint.getText());
            hint.setBounds(5, 5, screenItemProperties.getWidth() - 5, 25);
            hint.addMouseListener(new MouseAdapter()
            {
                
                @Override
                public void mouseUp(MouseEvent e)
                {
                    editor.select(screenItemProperties);
                }
            });
        
    }

    
    protected void createBlockPreview(final AbstractEJReportEditor editor,Composite reportBody, EJPluginReportBlockProperties properties)
    {

        final EJPluginReportScreenProperties screenProperties = properties.getLayoutScreenProperties();
        if (screenProperties.getScreenType() != EJReportScreenType.NONE)
        {

            Composite block = new Composite(reportBody, SWT.BORDER);
            setPreviewBackground(block, COLOR_BLOCK);

            block.setBounds(screenProperties.getX(), screenProperties.getY(), screenProperties.getWidth(), screenProperties.getHeight());

            block.setLayout(null);

            Label hint = new Label(block, SWT.NONE);
            hint.setText(String.format("%s [ %d, %d ] [ %d, %d ]",properties.getName(), screenProperties.getX(), screenProperties.getY(), screenProperties.getWidth(),
                    screenProperties.getHeight()));
            hint.setToolTipText(hint.getText());
            hint.setBounds(5, 5, screenProperties.getWidth()-5, 25);
            hint.addMouseListener(new MouseAdapter()
            {
                
                @Override
                public void mouseUp(MouseEvent e)
                {
                   editor.select(screenProperties);
                   editor.expand(screenProperties);
                }
            });
        }
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
