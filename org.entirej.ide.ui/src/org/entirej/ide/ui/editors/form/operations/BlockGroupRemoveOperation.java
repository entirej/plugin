package org.entirej.ide.ui.editors.form.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockGroup;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class BlockGroupRemoveOperation extends AbstractOperation
{

    private EJPluginBlockContainer        container;
    private BlockGroup                    group;
    private AbstractNodeTreeSection       treeSection;
    private boolean                       dirty;
    private int                           index = -1;

    public BlockGroupRemoveOperation(final AbstractNodeTreeSection treeSection, EJPluginBlockContainer container, BlockGroup group)
    {
        super("Remove Block Group");
        this.treeSection = treeSection;
        this.container = container;
        this.group = group;
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

            index = container.removeBlockContainerItem(group);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh((container), true);
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
                container.addBlockProperties(group);
            }
            else
            {
                container.addBlockProperties(index, group);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh((container), true);
                    treeSection.selectNodes(true, group);
                    //treeSection.expand(abstractNode, 2);

                }
            });
        }
       

        return Status.OK_STATUS;
    }

}
