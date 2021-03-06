package org.entirej.ide.ui.editors.report.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class ReportBlockScreenItemAddOperation extends AbstractOperation
{

    private EJReportScreenItemContainer    container;
    private EJPluginReportScreenItemProperties item;
    private AbstractNodeTreeSection      treeSection;
    private boolean                      dirty;

    private int                          index = -1;

    public ReportBlockScreenItemAddOperation(final AbstractNodeTreeSection treeSection, EJReportScreenItemContainer container,
            EJPluginReportScreenItemProperties blockProperties, int index)
    {
        super("Add Screen Item");
        this.treeSection = treeSection;
        this.container = container;
        this.item = blockProperties;
        this.index = index;
    }

    public ReportBlockScreenItemAddOperation(final AbstractNodeTreeSection treeSection, EJReportScreenItemContainer container,
            EJPluginReportScreenItemProperties blockProperties)
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
            if (index == -1 || index>=container.getAllItemProperties().size())
                container.addItemProperties(item);
            else
            {
                container.addItemProperties(index, item);
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
            container.removeItem(item);
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
