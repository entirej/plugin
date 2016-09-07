package org.entirej.ide.ui.editors.report.gef;

import org.eclipse.gef.EditPart;
import org.eclipse.gef.EditPartFactory;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.ide.ui.editors.report.gef.parts.AbstractReportGraphicalEditPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockColumnPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockSectionCanvasPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportBlockSectionCanvasPart.BlockSectionCanvas;
import org.entirej.ide.ui.editors.report.gef.parts.ReportCanvasPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportCanvasPart.ReportCanvas;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenCanvasPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenCanvasPart.ReportFormScreenCanvas;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenItemPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportFormScreenPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportTableScreenCanvasPart;
import org.entirej.ide.ui.editors.report.gef.parts.ReportTableScreenCanvasPart.ReportTableScreenCanvas;

public class ReportEditPartFactory implements EditPartFactory
{
    private final ReportEditorContext reportEditorContext;

    public ReportEditPartFactory(ReportEditorContext reportEditorContext)
    {
        this.reportEditorContext = reportEditorContext;
    }

    public EditPart createEditPart(EditPart context, Object model)
    {
        EditPart part = null;
        if (model instanceof ReportCanvas)
        {
            part = new ReportCanvasPart();
        }
        if (model instanceof BlockSectionCanvas)
        {
            part = new ReportBlockSectionCanvasPart();
        }
        else if (model instanceof ReportFormScreenCanvas)
        {
            part = new ReportFormScreenCanvasPart();
        }
        else if (model instanceof EJPluginReportColumnProperties)
        {
            part = new ReportBlockColumnPart();
        }
        else if (model instanceof ReportTableScreenCanvas)
        {
            part = new ReportTableScreenCanvasPart();
        }
        else if (model instanceof EJPluginReportBlockProperties)
        {
            part = new ReportBlockPart();
        }
        else if (model instanceof EJPluginReportScreenProperties)
        {
            part = createScreenPart();
        }
        else if (model instanceof EJPluginReportScreenItemProperties)
        {
            part = new ReportFormScreenItemPart();
        }

        if (part instanceof AbstractReportGraphicalEditPart)
        {
            ((AbstractReportGraphicalEditPart) part).setReportEditorContext(reportEditorContext);
        }
        // set model
        if (part != null)
        {
            part.setModel(model);
        }
        return part;
    }

    protected ReportFormScreenPart createScreenPart()
    {
        return new ReportFormScreenPart();
    }

}
