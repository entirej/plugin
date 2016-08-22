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

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.util.LocalSelectionTransfer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.report.gef.ReportEditPartFactory;
import org.entirej.ide.ui.editors.report.gef.ReportPreviewEditControl;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockSectionCanvasPart;

public class ReportPreviewImpl implements IReportPreviewProvider
{
    protected final Color  COLOR_BLOCK        = new Color(Display.getCurrent(), new RGB(255, 251, 227));
    protected final Color  COLOR_LIGHT_YELLOW = Display.getCurrent().getSystemColor(SWT.COLOR_INFO_BACKGROUND);
    protected final Color  COLOR_WHITE        = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    protected final Color  COLOR_LIGHT_SHADOW = Display.getCurrent().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW);

    protected final Cursor RESIZE             = new Cursor(Display.getCurrent(), SWT.CURSOR_SIZESE);
    protected final Cursor MOVE               = new Cursor(Display.getCurrent(), SWT.CURSOR_HAND);

    protected final Color  COLOR_HEADER       = new Color(Display.getCurrent(), new RGB(180, 180, 180));
    protected final Color  COLOR_FOOTER       = new Color(Display.getCurrent(), new RGB(218, 218, 218));

    protected BlockGroup   page;

    public void dispose()
    {
        COLOR_BLOCK.dispose();
        RESIZE.dispose();
        COLOR_HEADER.dispose();
        COLOR_FOOTER.dispose();
        MOVE.dispose();
    }

    public ReportPreviewImpl()
    {
        this(null);
    }

    public ReportPreviewImpl(BlockGroup page)
    {
        this.page = page;
    }

    protected EJPluginReportProperties getReportProperties(AbstractEJReportEditor editor)
    {
        return editor.getReportProperties();
    }

    public void buildPreview(final AbstractEJReportEditor editor, ScrolledComposite previewComposite)
    {
        // layout canvas preview
        final Composite pContent = new Composite(previewComposite, SWT.NONE);

        final EJPluginReportProperties formProperties = getReportProperties(editor);
        int width = formProperties.getReportWidth();
        int height = formProperties.getReportHeight();
        previewComposite.setContent(pContent);
        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

        pContent.setLayout(null);
        setPreviewBackground(pContent, COLOR_LIGHT_YELLOW);

        pContent.addPaintListener(new PaintListener()
        {

            public void paintControl(PaintEvent e)
            {
                if (formProperties.getHeaderSectionHeight() > 0)
                {
                    int y1 = formProperties.getHeaderSectionHeight() + 10 + formProperties.getMarginTop() + 1;
                    e.gc.drawLine(0, y1, pContent.getBounds().width, y1);
                    e.gc.drawString("H", 5, y1 > 20 ? y1 - 20 : 2, true);
                }
                if (formProperties.getFooterSectionHeight() > 0)
                {

                    int y1 = pContent.getBounds().height - (formProperties.getFooterSectionHeight() + 10 + formProperties.getMarginBottom());
                    e.gc.drawLine(0, y1, pContent.getBounds().width, y1);

                    e.gc.drawString("F", 5, y1 > 20 ? y1 - 20 : y1, true);

                }

            }
        });
        Composite report = new Composite(pContent, SWT.BORDER);
        setPreviewBackground(report, COLOR_LIGHT_SHADOW);
        previewComposite.setMinSize(width+25, height+10);
        report.setBounds(25, 10, width, height);
        report.setLayout(null);

        EJReportBlockContainer blockContainer = formProperties.getBlockContainer();

        if (page == null)
        {
            headerSection(editor, previewComposite, formProperties, width, height, report, blockContainer);
            detailSection(editor, previewComposite, formProperties, width, height, report, blockContainer);
            footerSection(editor, previewComposite, formProperties, width, height, report, blockContainer);
        }
        else
        {
           

           

            ReportPreviewEditControl previewEditControl = new ReportPreviewEditControl(editor,report){
                
                
                @Override
                protected ReportEditPartFactory createPartFactory(AbstractEJReportEditor editor)
                {
                    return new ReportEditPartFactory(createContext(editor));
                }
            };
            
            previewEditControl.setModel(new ReportBlockSectionCanvasPart.BlockSectionCanvas(page,
                    (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), (height - (formProperties.getMarginBottom()
                            + formProperties.getMarginTop() + formProperties.getHeaderSectionHeight() + formProperties.getFooterSectionHeight()))));
            previewEditControl.setBounds(formProperties.getMarginLeft(), formProperties.getMarginTop() + formProperties.getHeaderSectionHeight(),
                    (width - (formProperties.getMarginRight() + formProperties.getMarginLeft()))+2, (height - (formProperties.getMarginBottom()
                            + formProperties.getMarginTop() + formProperties.getHeaderSectionHeight() + formProperties.getFooterSectionHeight())));
        }

    }

    private void headerSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer)
    {
        
        ReportPreviewEditControl previewEditControl = new ReportPreviewEditControl(editor,report){
            
            
            @Override
            protected ReportEditPartFactory createPartFactory(AbstractEJReportEditor editor)
            {
                return new ReportEditPartFactory(createContext(editor));
            }
        };
        
        previewEditControl.setModel(new ReportBlockSectionCanvasPart.BlockSectionCanvas(blockContainer.getHeaderSection(),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), formProperties.getHeaderSectionHeight()));
        previewEditControl.setBounds(formProperties.getMarginLeft(), formProperties.getMarginTop(),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft()))+2, formProperties.getHeaderSectionHeight());


    }

    private void footerSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer)
    {
        
        
       ReportPreviewEditControl previewEditControl = new ReportPreviewEditControl(editor,report){
            
            
            @Override
            protected ReportEditPartFactory createPartFactory(AbstractEJReportEditor editor)
            {
                return new ReportEditPartFactory(createContext(editor));
            }
        };
        
        previewEditControl.setModel(new ReportBlockSectionCanvasPart.BlockSectionCanvas(blockContainer.getFooterSection(),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), formProperties.getFooterSectionHeight()));
        previewEditControl.setBounds(formProperties.getMarginLeft(), height - (formProperties.getMarginBottom() + formProperties.getFooterSectionHeight()),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft()))+2, formProperties.getFooterSectionHeight());
        
       
    }

    private void detailSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer)
    {
        
        BlockGroup firstPage = blockContainer.getFirstPage();
        
        
       ReportPreviewEditControl previewEditControl = new ReportPreviewEditControl(editor,report){
            
            
            @Override
            protected ReportEditPartFactory createPartFactory(AbstractEJReportEditor editor)
            {
                return new ReportEditPartFactory(createContext(editor));
            }
        };
        
        previewEditControl.setModel(new ReportBlockSectionCanvasPart.BlockSectionCanvas(firstPage,
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), (height - (formProperties.getMarginBottom()
                        + formProperties.getMarginTop() + formProperties.getHeaderSectionHeight() + formProperties.getFooterSectionHeight()))));
        previewEditControl.setBounds(formProperties.getMarginLeft(), formProperties.getMarginTop() + formProperties.getHeaderSectionHeight(),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft()))+2, (height - (formProperties.getMarginBottom()
                        + formProperties.getMarginTop() + formProperties.getHeaderSectionHeight() + formProperties.getFooterSectionHeight())));
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
