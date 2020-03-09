package org.entirej.ide.ui.editors.report.gef.commands;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.gef.commands.Command;
import org.entirej.ide.ui.editors.report.gef.ReportEditorContext;

public class OperationCommand extends Command
{
    final AbstractOperation operation;
    final ReportEditorContext editorContext;

    public OperationCommand(final ReportEditorContext editorContext,AbstractOperation operation)
    {
        this.operation = operation;
        this.editorContext = editorContext;
        setLabel(operation.getLabel());
    }

    
    @Override
    public void execute()
    {
        editorContext.execute(operation);
    }
    
    @Override
    public void undo()
    {
      //ignore
    }
    
    @Override
    public void redo()
    {
        //ignore
    }
   
}
