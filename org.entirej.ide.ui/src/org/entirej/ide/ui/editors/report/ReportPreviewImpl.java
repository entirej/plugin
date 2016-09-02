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

import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.ide.ui.editors.report.gef.ReportEditPartFactory;
import org.entirej.ide.ui.editors.report.gef.ReportPreviewEditControl;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockSectionCanvasPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportCanvasPart.ReportCanvas;

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
    private ReportPreviewEditControl previewEditControl;

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

    public void refresh(AbstractEJReportEditor editor, ScrolledComposite previewComposite, Object o)
    {
        
       if(previewEditControl!=null && !previewEditControl.isDisposed())
       {
           final EJPluginReportProperties formProperties = getReportProperties(editor);
           int width = formProperties.getReportWidth();
           int height = formProperties.getReportHeight();
           EJReportBlockContainer blockContainer = formProperties.getBlockContainer();

         
           if (page == null)
           {
               ReportCanvas canvas = new ReportCanvas(width,height,headerSection(editor, previewComposite, formProperties, width, height, null, blockContainer,o),
               detailSection(editor, previewComposite, formProperties, width, height, null, blockContainer,o),
               footerSection(editor, previewComposite, formProperties, width, height, null, blockContainer,o));
               
               
               previewEditControl.setModel(canvas);
               
               previewEditControl.setSelectionToViewer(Arrays.asList(o));
           }
           else
           {
              

              

               ReportCanvas canvas = new ReportCanvas(width,height,new ReportBlockSectionCanvasPart.BlockSectionCanvas(page,formProperties.getMarginLeft(), formProperties.getMarginTop() + formProperties.getHeaderSectionHeight(),
                       (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), (height - (formProperties.getMarginBottom()
                               + formProperties.getMarginTop() + formProperties.getHeaderSectionHeight() + formProperties.getFooterSectionHeight()))));
               previewEditControl.setModel(canvas);
              
               previewEditControl.setSelectionToViewer(Arrays.asList(o));
           }
       }
       else
       {
           buildPreview(editor, previewComposite, o);
           
       }
        
    }
    
    
    public Action[] getToolbarActions()
    {
        return new Action[]{new ZoomInAction(previewEditControl.getZoomManager()),new ZoomOutAction(previewEditControl.getZoomManager())};
    }
    
    public void buildPreview(final AbstractEJReportEditor editor, ScrolledComposite previewComposite,Object o)
    {
        
        final EJPluginReportProperties formProperties = getReportProperties(editor);
        int width = formProperties.getReportWidth();
        int height = formProperties.getReportHeight();
       
        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
        previewComposite.setExpandHorizontal(true);
        previewComposite.setExpandVertical(true);

       
        previewComposite.setMinSize(width, height);
       
       
         previewEditControl = new ReportPreviewEditControl(editor,previewComposite,true){
            
            
            @Override
            protected ReportEditPartFactory createPartFactory(AbstractEJReportEditor editor)
            {
                return new ReportEditPartFactory(createContext(editor));
            }
        };
        previewComposite.setContent(previewEditControl);
        refresh(editor, previewComposite, o);

    }

    private ReportBlockSectionCanvasPart.BlockSectionCanvas headerSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer, Object o)
    {
        
      
        
       
        return new ReportBlockSectionCanvasPart.BlockSectionCanvas(blockContainer.getHeaderSection(),
                formProperties.getMarginLeft(), formProperties.getMarginTop(),  (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), formProperties.getHeaderSectionHeight());
    }

    private ReportBlockSectionCanvasPart.BlockSectionCanvas footerSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer, Object o)
    {
        
        
      
      
        return new ReportBlockSectionCanvasPart.BlockSectionCanvas(blockContainer.getFooterSection(),
                formProperties.getMarginLeft(), height - (formProperties.getMarginBottom() + formProperties.getFooterSectionHeight()),(width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), formProperties.getFooterSectionHeight());
       
    }

    private ReportBlockSectionCanvasPart.BlockSectionCanvas detailSection(final AbstractEJReportEditor editor, ScrolledComposite previewComposite, final EJPluginReportProperties formProperties,
            int width, int height, Composite report, EJReportBlockContainer blockContainer, Object o)
    {
        
        BlockGroup firstPage = blockContainer.getFirstPage();
        
        
       
        return new ReportBlockSectionCanvasPart.BlockSectionCanvas(firstPage,formProperties.getMarginLeft(), formProperties.getMarginTop() + formProperties.getHeaderSectionHeight(),
                (width - (formProperties.getMarginRight() + formProperties.getMarginLeft())), (height - (formProperties.getMarginBottom()
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
