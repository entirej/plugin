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

import java.util.Arrays;
import java.util.List;

import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.interfaces.EJReportBorderProperties;
import org.entirej.ide.ui.editors.report.gef.ReportPreviewEditControl;
import org.entirej.ide.ui.editors.report.gef.parts.ReportTableScreenCanvasPart.ReportTableScreenCanvas;

public class ReportScreenColumnPreviewImpl implements IReportPreviewProvider
{

    protected final EJPluginReportScreenProperties properties;
    private ReportPreviewEditControl previewEditControl;

    

    public ReportScreenColumnPreviewImpl(EJPluginReportScreenProperties properties)
    {
        this.properties = properties;
    }

    public void dispose()
    {
    }
    
    public Action[] getToolbarActions()
    {
        return new Action[]{new ZoomInAction(previewEditControl.getZoomManager()),new ZoomOutAction(previewEditControl.getZoomManager())};
        
    }

    protected EJPluginReportProperties getReportProperties(AbstractEJReportEditor editor)
    {
        return editor.getReportProperties();
    }
    
    public void refresh(AbstractEJReportEditor editor, Composite previewComposite, Object o)
    {
        if(previewEditControl!=null && !previewEditControl.isDisposed())
        {
            previewEditControl.setModel(new ReportTableScreenCanvas(properties,editor.getReportProperties().getReportWidth(),editor.getReportProperties().getReportHeight()));
            
            previewEditControl.setSelectionToViewer(Arrays.asList(o));
        }
        
    }

    public void buildPreview(final AbstractEJReportEditor editor, Composite previewComposite,Object o)
    {
        final EJPluginReportScreenProperties layoutScreenProperties = properties;

         previewEditControl = new ReportPreviewEditControl(editor,previewComposite,true,0,0);
       
        refresh(editor, previewComposite, layoutScreenProperties);
        
    }

    protected void setPreviewBackground(Control control, Color color)
    {
        control.setBackground(color);
    }

    public String getDescription()
    {
        return "editor the defined canvas layout in form.";
    }

    
    
    @Deprecated
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
                  //  e.gc.setForeground(COLOR_LINE);

                    int x;
                    if (column.isShowHeader() && column.isShowFooter())
                    {
                        x = column.getDetailScreen().getWidth();
                    }
                    else if (!column.isShowFooter() && column.isShowHeader())
                    {
                        x = column.getHeaderScreen().getWidth() > column.getDetailScreen().getWidth() ? column.getHeaderScreen().getWidth()
                                : column.getDetailScreen().getWidth();
                    }
                    else
                    {
                        x = column.getDetailScreen().getWidth();
                    }

                    EJReportBorderProperties borderProperties = null;
                    if (header && !footer && column.isShowHeader())
                    {
                        e.gc.drawText(column.getName(), colLocation + (5), (composite.getBounds().height / 2) - 10);
                        borderProperties = column.getHeaderBorderProperties();
                    }
                    if (!header && footer && column.isShowFooter())
                    {
                        e.gc.drawText(column.getName(), colLocation + (5), (composite.getBounds().height / 2) - 10);
                        borderProperties = column.getFooterBorderProperties();
                    }
                    if (!header && !footer)
                    {
                        e.gc.drawText(column.getName(), colLocation + (5), (composite.getBounds().height / 2) - 10);
                        borderProperties = column.getDetailBorderProperties();
                    }

                    if (borderProperties != null)
                    {
                        if (!borderProperties.showRightLine())
                        {
                            // add dummy maker
                            e.gc.setLineWidth(1);

                            e.gc.drawLine((colLocation + x) - 1, 0, (colLocation + x) - 1, composite.getBounds().height);
                        }

                        int lineWidth = (int) Math.ceil(borderProperties.getLineWidth());
                        e.gc.setLineWidth(lineWidth);
                        //e.gc.setForeground(COLOR_BLACK);

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
                        if (borderProperties.showTopLine())
                        {

                            e.gc.drawLine(colLocation, 0, colLocation + x, 0);
                        }
                        if (borderProperties.showBottomLine())
                        {

                            e.gc.drawLine(colLocation, composite.getBounds().height - lineWidth, colLocation + x, composite.getBounds().height - lineWidth);
                        }
                        if (borderProperties.showLeftLine())
                        {

                            e.gc.drawLine(colLocation, 0, colLocation, composite.getBounds().height);
                        }

                        if (borderProperties.showRightLine())
                        {

                            e.gc.drawLine((colLocation + x) - lineWidth, 0, (colLocation + x) - lineWidth, composite.getBounds().height);
                        }

                    }
                    else
                    {
                        e.gc.setLineWidth(1);

                        e.gc.drawLine((colLocation + x) - 1, 0, (colLocation + x) - 1, composite.getBounds().height);
                    }

                    colLocation += x;
                }

            }
        });
    }

    

}
