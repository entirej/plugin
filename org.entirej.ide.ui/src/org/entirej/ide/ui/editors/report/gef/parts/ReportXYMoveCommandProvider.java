package org.entirej.ide.ui.editors.report.gef.parts;

import org.eclipse.gef.commands.Command;

public interface ReportXYMoveCommandProvider
{

    Command createMoveCommand(Integer xDelta, Integer yDelta);

}
