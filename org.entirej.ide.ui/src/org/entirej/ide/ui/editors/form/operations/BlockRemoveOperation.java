package org.entirej.ide.ui.editors.form.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.commands.operations.IUndoContext;
import org.eclipse.core.commands.operations.IUndoableOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockGroup;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.operations.ReversibleOperation;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class BlockRemoveOperation extends AbstractOperation
{

    private EJPluginBlockContainer  container;
    private BlockGroup              group;
    private EJPluginBlockProperties blockProperties;
    private AbstractNodeTreeSection treeSection;
    private boolean                 dirty;
    private int                     index = -1;

    public BlockRemoveOperation(final AbstractNodeTreeSection treeSection, EJPluginBlockContainer container, EJPluginBlockProperties blockProperties)
    {
        super("Remove Block");
        this.treeSection = treeSection;

        BlockGroup blockGroupByBlock = container.getBlockGroupByBlock(blockProperties);
        if (blockGroupByBlock != null)
            this.group = blockGroupByBlock;
        else
            this.container = container;
        this.blockProperties = blockProperties;
    }

    public static AbstractOperation createCleanupOperation(final AbstractNodeTreeSection treeSection, EJPluginBlockContainer container,
            final EJPluginBlockProperties props)
    {
        ReversibleOperation operation = new ReversibleOperation("Remove Block");

        if (props.isMirrorChild() && props.getMirrorParent() != null)
        {
            final EJPluginBlockProperties mirrorParent = props.getMirrorParent();
            operation.add(new AbstractOperation("Remove Mirror")
            {

                @Override
                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    mirrorParent.addMirrorChild(props);
                    return Status.OK_STATUS;
                }

                @Override
                public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    return execute(monitor, info);
                }

                @Override
                public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    mirrorParent.removeMirrorChild(props);
                    return Status.OK_STATUS;
                }
            });
        }

        // First remove it from all mirrored blocks
        for (EJPluginBlockProperties mirroredBlock : new ArrayList<EJPluginBlockProperties>(props.getMirrorChildren()))
        {
            operation.add(createCleanupOperation(treeSection, container, mirroredBlock));
        }

        List<EJPluginBlockProperties> allBlockProperties = new ArrayList<EJPluginBlockProperties>(
                props.getFormProperties().getBlockContainer().getAllBlockProperties());
        List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = props.getFormProperties().getLovDefinitionContainer()
                .getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties lovDefinitionProperties : allLovDefinitionProperties)
        {
            allBlockProperties.add(lovDefinitionProperties.getBlockProperties());
        }

        for (EJPluginBlockProperties properties : allBlockProperties)
        {
            List<EJPluginBlockItemProperties> itemProperties = properties.getItemContainer().getAllItemProperties();
            for (final EJPluginBlockItemProperties blockItemProperties : itemProperties)
            {

                String insertValue = blockItemProperties.getDefaultInsertValue();
                if (insertValue != null && insertValue.trim().length() > 0 && insertValue.indexOf(":") > 0)
                {
                    if ("BLOCK_ITEM".equals(insertValue.substring(0, insertValue.indexOf(":"))))
                    {
                        String value = insertValue.substring(insertValue.indexOf(":") + 1);
                        String[] split = value.split("\\.");
                        if (split.length == 2)
                        {
                            if (props.getName().equals(split[0]))
                            {

                                final String old = blockItemProperties.getDefaultInsertValue();
                                operation.add(new AbstractOperation("SET")
                                {

                                    @Override
                                    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                                    {
                                        blockItemProperties.setDefaultInsertValue(old);
                                        return Status.OK_STATUS;
                                    }

                                    @Override
                                    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                                    {
                                        return execute(monitor, info);
                                    }

                                    @Override
                                    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                                    {
                                        blockItemProperties.setDefaultInsertValue("");
                                        return Status.OK_STATUS;
                                    }
                                });
                            }
                        }
                    }

                }

                String queryValue = blockItemProperties.getDefaultQueryValue();
                if (queryValue != null && queryValue.trim().length() > 0 && queryValue.indexOf(":") > 0)
                {
                    if ("BLOCK_ITEM".equals(queryValue.substring(0, queryValue.indexOf(":"))))
                    {
                        String value = queryValue.substring(queryValue.indexOf(":") + 1);
                        String[] split = value.split("\\.");
                        if (split.length == 2)
                        {
                            if (props.getName().equals(split[0]))
                            {

                                final String old = blockItemProperties.getDefaultQueryValue();
                                operation.add(new AbstractOperation("SET")
                                {

                                    @Override
                                    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                                    {
                                        blockItemProperties.setDefaultInsertValue(old);
                                        return Status.OK_STATUS;
                                    }

                                    @Override
                                    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                                    {
                                        return execute(monitor, info);
                                    }

                                    @Override
                                    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                                    {
                                        blockItemProperties.setDefaultQueryValue("");
                                        return Status.OK_STATUS;
                                    }
                                });

                            }
                        }
                    }

                }
            }
        }

        if (props.getFormProperties().getFirstNavigableBlock().equals(props.getName()))
        {
            operation.add(new AbstractOperation("First Navigable Block")
            {

                @Override
                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    props.getFormProperties().setFirstNavigableBlock(props.getName());
                    return Status.OK_STATUS;
                }

                @Override
                public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    return execute(monitor, info);
                }

                @Override
                public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    props.getFormProperties().setFirstNavigableBlock("");
                    return Status.OK_STATUS;
                }
            });
        }

        operation.add(new BlockRemoveOperation(treeSection, container, props));

        return operation;
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

            index = container.removeBlockContainerItem(blockProperties);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh((container), true);
                }
            });
        }
        if (group != null)
        {

            index = group.removeBlockProperties(blockProperties);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh((group), true);
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
                container.addBlockProperties(blockProperties);
            }
            else
            {
                container.addBlockProperties(index, blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh((container), true);
                    treeSection.selectNodes(true, blockProperties);
                    // treeSection.expand(abstractNode, 2);

                }
            });
        }
        if (group != null)
        {
            if (index == -1)
            {
                group.addBlockProperties(blockProperties);
            }
            else
            {
                group.addBlockProperties(index, blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh((group), true);
                    treeSection.selectNodes(true, blockProperties);
                    // treeSection.expand(abstractNode, 2);

                }
            });
        }

        return Status.OK_STATUS;
    }

}
