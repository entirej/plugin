package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.gef.editparts.AbstractGraphicalEditPart;
import org.entirej.ide.ui.editors.report.gef.ReportEditorContext;

public abstract class AbstractReportGraphicalEditPart extends AbstractGraphicalEditPart
{
    protected ReportEditorContext reportEditorContext;

    public void setReportEditorContext(ReportEditorContext reportEditorContext)
    {
        this.reportEditorContext = reportEditorContext;
    }

    public ReportEditorContext getReportEditorContext()
    {
        return reportEditorContext;
    }
}
