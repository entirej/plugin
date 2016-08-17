package org.entirej.ide.ui.editors.report.gef.commands;

import org.eclipse.gef.commands.Command;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;

public class ResizeScreenCommand extends Command
{
    final EJPluginReportScreenProperties model;
    final int                            width;
    final int                            height;
    final int                            oldwidth;
    final int                            oldheight;

    public ResizeScreenCommand(EJPluginReportScreenProperties model, int width, int height)
    {
        super();
        this.model = model;
        this.width = width;
        this.height = height;
        oldwidth = model.getWidth();
        oldheight = model.getHeight();
        setLabel("Resize Report Screen");
    }

    @Override
    public void execute()
    {
        model.setWidth(width);
        model.setHeight(height);
    }

    @Override
    public void undo()
    {
        model.setWidth(oldwidth);
        model.setHeight(oldheight);
    }
}
