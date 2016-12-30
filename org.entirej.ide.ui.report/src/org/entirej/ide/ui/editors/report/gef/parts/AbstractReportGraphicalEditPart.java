package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.gef.EditPartViewer;
import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.eclipse.gef.editparts.ZoomManager;
import org.entirej.ide.ui.editors.report.gef.ReportEditorContext;

public abstract class AbstractReportGraphicalEditPart extends AbstractGraphicalEditPart
{
    protected ReportEditorContext reportEditorContext;
    private ZoomManager           zoomManager;

    public void setReportEditorContext(ReportEditorContext reportEditorContext)
    {
        this.reportEditorContext = reportEditorContext;
    }

    public ReportEditorContext getReportEditorContext()
    {
        return reportEditorContext;
    }

    public double getZoom()
    {
        if (zoomManager == null)
        {
            EditPartViewer viewer = getViewer();
            zoomManager = (ZoomManager) viewer.getProperty(ZoomManager.class.toString());

        }
        return zoomManager != null ? zoomManager.getZoom() : 1d;
    }
}
