package org.entirej.ide.ui.editors.report.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockItemContainer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class ReportBlockItemRemoveOperation extends AbstractOperation
{

    private EJReportBlockItemContainer   container;
    private EJPluginReportItemProperties item;
    private AbstractNodeTreeSection       treeSection;
    private boolean                       dirty;
    private int                           index = -1;

    public ReportBlockItemRemoveOperation(final AbstractNodeTreeSection treeSection, EJReportBlockItemContainer container, EJPluginReportItemProperties item)
    {
        super("Remove Block Item");
        this.treeSection = treeSection;
        this.container = container;
        this.item = item;
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

            index = container.removeItem(item);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(treeSection.findNode(container), true);
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
            if (index == -1)
            {
                container.addItemProperties(item);
            }
            else
            {
                container.addItemProperties(index, item);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh(treeSection.findNode(container), true);
                    AbstractNode<?> abstractNode = treeSection.findNode(item, true);
                    treeSection.selectNodes(true, abstractNode);
                   // treeSection.expand(abstractNode, 2);

                }
            });
        }
       

        return Status.OK_STATUS;
    }

}
