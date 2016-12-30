package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.gef.EditPart;

public interface ReportSelectionProvider
{
   Object getSelectionObject();
   
   EditPart getPostSelection();
}
