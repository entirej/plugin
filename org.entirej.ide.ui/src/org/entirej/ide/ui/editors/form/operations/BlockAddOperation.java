package org.entirej.ide.ui.editors.form.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockGroup;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class BlockAddOperation extends AbstractOperation
{

    private EJPluginBlockContainer        container;
    private BlockGroup                    group;
    private BlockContainerItem blockProperties;
    private AbstractNodeTreeSection       treeSection;
    private boolean                       dirty;

    private int                           index = -1;

    public BlockAddOperation(final AbstractNodeTreeSection treeSection, EJPluginBlockContainer container, BlockContainerItem blockProperties,
            int index)
    {
        super("Add Block");
        this.treeSection = treeSection;
        this.container = container;
        this.blockProperties = blockProperties;
        this.index = index;
    }

    public BlockAddOperation(final AbstractNodeTreeSection treeSection, BlockGroup group, BlockContainerItem blockProperties, int index)
    {
        super("Add Block");
        this.treeSection = treeSection;
        this.group = group;
        this.blockProperties = blockProperties;
        this.index = index;
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
            if (index == -1 || index>=container.getBlockContainerItems().size())
                container.addBlockProperties(blockProperties);
            else
            {
                container.addBlockProperties(index, blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(treeSection.findNode(container), true);
                    AbstractNode<?> abstractNode = treeSection.findNode(blockProperties, true);
                    treeSection.selectNodes(true, abstractNode);
                    treeSection.expand(abstractNode, 2);

                }
            });
        }
        if (group != null)
        {
            if (index == -1 || index>=group.getAllBlockProperties().size())
                group.addBlockProperties((EJPluginBlockProperties)blockProperties);
            else
            {
                group.addBlockProperties(index, (EJPluginBlockProperties)blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(treeSection.findNode(group), true);
                    AbstractNode<?> abstractNode = treeSection.findNode(blockProperties, true);
                    treeSection.selectNodes(true, abstractNode);
                    treeSection.expand(abstractNode, 2);

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
            container.removeBlockContainerItem(blockProperties);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh(treeSection.findNode(container), true);
                }
            });
        }
        if (group != null)
        {
            group.removeBlockProperties((EJPluginBlockProperties)blockProperties);

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh(treeSection.findNode(group), true);

                }
            });
        }

        return Status.OK_STATUS;
    }

}
