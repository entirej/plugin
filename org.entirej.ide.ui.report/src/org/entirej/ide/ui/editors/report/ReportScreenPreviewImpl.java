/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.report;

import java.util.Arrays;

import org.eclipse.gef.ui.actions.ZoomInAction;
import org.eclipse.gef.ui.actions.ZoomOutAction;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.widgets.Composite;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.ReportPreviewEditControl;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenCanvasPart.ReportFormScreenCanvas;

public class ReportScreenPreviewImpl implements IReportPreviewProvider
{
   
    protected final EJPluginReportScreenProperties properties;

    private ReportPreviewEditControl previewEditControl;

    public ReportScreenPreviewImpl(EJPluginReportScreenProperties properties)
    {
        this.properties = properties;
    }

    

    protected int getHeight()
    {

        return properties.getHeight();
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
            previewEditControl.setModel(new ReportFormScreenCanvas(properties,editor.getReportProperties().getReportWidth(),getHeight()));
            
            previewEditControl.setSelectionToViewer(Arrays.asList(o));
        }
        
    }

    public void buildPreview(final AbstractEJReportEditor editor, Composite previewComposite,Object o)
    {
        final EJPluginReportScreenProperties layoutScreenProperties = properties;

         previewEditControl = new ReportPreviewEditControl(editor,previewComposite,true,0,0);
       
        refresh(editor, previewComposite, o==null?layoutScreenProperties:o);
        
    }

    public String getDescription()
    {
        return "editor the defined canvas layout in form.";
    }



    public void dispose()
    {
        if(previewEditControl!=null)
            previewEditControl.dispose();
        
        previewEditControl = null;
        
    }
}
