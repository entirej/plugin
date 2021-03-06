package org.entirej.ide.ui.editors.report.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.containers.EJReportColumnContainer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class ReportBlockColumnAddOperation extends AbstractOperation
{

    private EJReportColumnContainer    container;
    private EJPluginReportColumnProperties item;
    private AbstractNodeTreeSection      treeSection;
    private boolean                      dirty;

    private int                          index = -1;

    public ReportBlockColumnAddOperation(final AbstractNodeTreeSection treeSection, EJReportColumnContainer container,
            EJPluginReportColumnProperties blockProperties, int index)
    {
        super("Add Block Column");
        this.treeSection = treeSection;
        this.container = container;
        this.item = blockProperties;
        this.index = index;
    }

    public ReportBlockColumnAddOperation(final AbstractNodeTreeSection treeSection, EJReportColumnContainer container,
            EJPluginReportColumnProperties blockProperties)
    {
        this(treeSection, container, blockProperties, -1);
    }

    @Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        return redo(monitor, info);
    }

    @Override
    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        dirty = treeSection.isDirty();

        if (container != null)
        {
            if (index == -1 || index>=container.getAllColumnProperties().size())
                container.addColumnProperties(item);
            else
            {
                container.addColumnProperties(index, item);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh((container), true);
                    treeSection.selectNodes(true, item);
                    //treeSection.expand(abstractNode, 2);

                }
            });
        }

        return Status.OK_STATUS;
    }

    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        if (container != null)
        {
            container.removeColumn(item);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh((container), true);
                }
            });
        }

        return Status.OK_STATUS;
    }

}
