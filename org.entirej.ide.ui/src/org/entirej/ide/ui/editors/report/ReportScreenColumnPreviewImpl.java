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
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.interfaces.EJReportBorderProperties;

public class ReportScreenColumnPreviewImpl implements IReportPreviewProvider
{
    protected final Color                          COLOR_HEADER       = new Color(Display.getCurrent(), new RGB(180, 180, 180));
    protected final Color                          COLOR_DETAIL       = new Color(Display.getCurrent(), new RGB(236, 236, 236));
    protected final Color                          COLOR_FOOTER       = new Color(Display.getCurrent(), new RGB(218, 218, 218));
    protected final Color                          COLOR_LINE         = new Color(Display.getCurrent(), new RGB(118, 118, 118));
    protected final Color                          COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    protected final Color                          COLOR_WHITE        = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final Color                          COLOR_BLACK        = Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
    protected final Color                          COLOR_LIGHT_SHADOW = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);

    protected final EJPluginReportScreenProperties properties;

    private int                                    x, y;

    public ReportScreenColumnPreviewImpl(EJPluginReportScreenProperties properties)
    {
        this.properties = properties;
    }

    public void dispose()
    {
        COLOR_HEADER.dispose();
        COLOR_DETAIL.dispose();
        COLOR_FOOTER.dispose();
        COLOR_LINE.dispose();
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

        EJPluginReportScreenProperties layoutScreenProperties = properties;

        Composite header = new Composite(reportBody, SWT.NONE);
        header.setLayout(null);
        setPreviewBackground(header, COLOR_HEADER);
        Composite detail = new Composite(reportBody, SWT.NONE);
        detail.setLayout(null);
        setPreviewBackground(detail, COLOR_DETAIL);
        Composite footer = new Composite(reportBody, SWT.NONE);
        footer.setLayout(null);
        setPreviewBackground(footer, COLOR_FOOTER);

        List<EJPluginReportColumnProperties> columnProperties = layoutScreenProperties.getColumnContainer().getAllColumnProperties();

        int headerH = layoutScreenProperties.getHeaderColumnHeight();
        int headerW = 0;
        int detailH = layoutScreenProperties.getDetailColumnHeight();
        int detailW = 0;
        int footerH = layoutScreenProperties.getDetailColumnHeight();
        int footerW = 0;

        int totalW = 0;
        // calculate sections
        for (EJPluginReportColumnProperties column : columnProperties)
        {

            if (column.isShowHeader())
            {
                if (headerH < column.getHeaderScreen().getHeight())
                {
                    headerH = column.getHeaderScreen().getHeight();
                }
                if (headerW < column.getHeaderScreen().getWidth())
                {
                    headerW = column.getHeaderScreen().getWidth();
                }
            }
            if (detailH < column.getDetailScreen().getHeight())
            {
                detailH = column.getDetailScreen().getHeight();
            }
            if (detailW < column.getDetailScreen().getWidth())
            {
                detailW = column.getDetailScreen().getWidth();
            }
            if (column.isShowFooter())
            {
                if (footerH < column.getFooterScreen().getHeight())
                {
                    footerH = column.getFooterScreen().getHeight();
                }
                if (footerW < column.getFooterScreen().getWidth())
                {
                    footerW = column.getFooterScreen().getWidth();
                }
            }
            
            
            if(column.isShowHeader() && column.isShowFooter())
            {
                totalW += column.getHeaderScreen().getWidth() > column.getFooterScreen().getWidth() ? (column.getHeaderScreen().getWidth() > column
                        .getDetailScreen().getWidth() ? column.getHeaderScreen().getWidth() : column.getDetailScreen().getWidth()) : (column
                        .getFooterScreen().getWidth() > column.getDetailScreen().getWidth() ? column.getFooterScreen().getWidth() : column
                        .getDetailScreen().getWidth());
            }
            else if(!column.isShowFooter() && column.isShowHeader())
            {
                totalW += column.getHeaderScreen().getWidth() > column
                        .getDetailScreen().getWidth() ? column.getHeaderScreen().getWidth() : column.getDetailScreen().getWidth() ;
            }
            else
            {
                totalW += column
                        .getDetailScreen().getWidth();
            }
           
        }

        header.setBounds(0, 0, totalW, headerH);
        detail.setBounds(0, headerH, totalW, detailH);
        footer.setBounds(0, headerH + detailH, totalW, footerH);

        addColumnLines(header, columnProperties, true, false);
        addColumnLines(detail, columnProperties, false, false);
        addColumnLines(footer, columnProperties, false, true);
        int sectionsH = (headerH + detailH + footerH);

        reportBody.setBounds(10, 10, layoutScreenProperties.getWidth() > totalW ? layoutScreenProperties.getWidth() : totalW,
                layoutScreenProperties.getHeight() > sectionsH ? layoutScreenProperties.getHeight() : sectionsH);

    }

    protected void setPreviewBackground(Control control, Color color)
    {
        control.setBackground(color);
    }

    public String getDescription()
    {
        return "preview the defined canvas layout in form.";
    }

    void addColumnLines(final Composite composite, final List<EJPluginReportColumnProperties> columnProperties, final boolean header, final boolean footer)
    {
        composite.addPaintListener(new PaintListener()
        {
            public void paintControl(PaintEvent e)
            {
            
                
                int colLocation = 0;
                for (EJPluginReportColumnProperties column : columnProperties)
                {
                   
                    e.gc.setLineStyle(SWT.LINE_DASH);
                    e.gc.setForeground(COLOR_LINE);
                    
                    int x ;
                    if(column.isShowHeader() && column.isShowFooter())
                    {
                        x = column.getHeaderScreen().getWidth() > column.getFooterScreen().getWidth() ? (column.getHeaderScreen().getWidth() > column
                                .getDetailScreen().getWidth() ? column.getHeaderScreen().getWidth() : column.getDetailScreen().getWidth()) : (column
                                .getFooterScreen().getWidth() > column.getDetailScreen().getWidth() ? column.getFooterScreen().getWidth() : column
                                .getDetailScreen().getWidth());
                    }
                    else if(!column.isShowFooter() && column.isShowHeader())
                    {
                        x = column.getHeaderScreen().getWidth() > column
                                .getDetailScreen().getWidth() ? column.getHeaderScreen().getWidth() : column.getDetailScreen().getWidth() ;
                    }
                    else
                    {
                        x = column
                                .getDetailScreen().getWidth();
                    }
                    

                            
                            
                    EJReportBorderProperties borderProperties  = null;        
                    if (header && !footer && column.isShowHeader())
                    {
                        e.gc.drawText(column.getName(), colLocation+(5), (composite.getBounds().height / 2) - 10);
                        borderProperties = column.getHeaderBorderProperties();
                    }
                    if (!header && footer && column.isShowFooter())
                    {
                        e.gc.drawText(column.getName(), colLocation+(5), (composite.getBounds().height / 2) - 10);
                        borderProperties = column.getFooterBorderProperties();
                    }
                    if (!header && !footer)
                    {
                        e.gc.drawText(column.getName(), colLocation+(5), (composite.getBounds().height / 2) - 10);
                        borderProperties = column.getDetailBorderProperties();
                    }

                    
                    if(borderProperties!=null)
                    {
                        if(!borderProperties.isShowRightLine())
                        {
                            //add dummy maker
                            e.gc.setLineWidth(1);
                            

                            e.gc.drawLine( (colLocation+x)-1, 0, (colLocation+x)-1, composite.getBounds().height);
                        }
                        
                        int lineWidth = (int) Math.ceil(borderProperties.getLineWidth());
                        e.gc.setLineWidth(lineWidth);
                        e.gc.setForeground(COLOR_BLACK);
                        
                        
                        switch (borderProperties.getLineStyle())
                        {
                            case DOTTED:
                                e.gc.setLineStyle(SWT.LINE_DOT);
                                break;
                            case DASHED:
                                e.gc.setLineStyle(SWT.LINE_DASH);
                                break;

                            default:
                                e.gc.setLineStyle(SWT.LINE_SOLID);
                                break;
                        }
                        if(borderProperties.isShowTopLine())
                        {
                           

                            e.gc.drawLine(colLocation, 0, colLocation+x, 0);
                        }
                        if(borderProperties.isShowBottomLine())
                        {
                           
                            
                            
                            e.gc.drawLine(colLocation, composite.getBounds().height-lineWidth, colLocation+x, composite.getBounds().height-lineWidth);
                        }
                        if(borderProperties.isShowLeftLine())
                        {
                            
                            
                            
                            e.gc.drawLine(colLocation, 0, colLocation, composite.getBounds().height);
                        }
                        
                        if(borderProperties.isShowRightLine())
                        {
                            

                            e.gc.drawLine((colLocation+x)-lineWidth, 0, (colLocation+x)-lineWidth, composite.getBounds().height);
                        }
                        
                        
                    }
                    else
                    {
                        e.gc.setLineWidth(1);
                        

                        e.gc.drawLine((colLocation+x)-1, 0, (colLocation+x)-1, composite.getBounds().height);
                    }
                   

                    colLocation+=x;
                }

            }
        });
    }

}
