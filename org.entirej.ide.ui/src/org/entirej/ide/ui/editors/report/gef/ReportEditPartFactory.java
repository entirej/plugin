package org.entirej.ide.ui.editors.report.gef;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenCanvasPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenCanvasPart.ReportFormScreenCanvas;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenItemPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenPart;

public class ReportEditPartFactory implements EditPartFactory
{

    public EditPart createEditPart(EditPart context, Object model)
    {
        EditPart part = null;
        if(model instanceof ReportFormScreenCanvas)
        {
            part = new ReportFormScreenCanvasPart();
        }
        if(model instanceof EJPluginReportScreenProperties)
        {
            part = new ReportFormScreenPart();
        }
        if(model instanceof EJPluginReportScreenItemProperties)
        {
            part = new ReportFormScreenItemPart();
        }
        
        
        
        
        //set model 
        if(part!=null)
        {
            part.setModel(model);
        }
        return part;
    }

}
