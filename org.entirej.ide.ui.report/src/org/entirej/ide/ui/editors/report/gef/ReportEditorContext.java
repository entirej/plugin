package org.entirej.ide.ui.editors.report.gef;

import org.eclipse.core.commands.operations.AbstractOperation;

public interface ReportEditorContext
{

    void execute(AbstractOperation operation);

    void setDirty(boolean b);

    void refresh(Object model);

    void refreshPreview();

    void refreshProperties();

}
