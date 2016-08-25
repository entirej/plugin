package org.entirej.ide.ui.editors.form.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockItemContainer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class BlockItemAddOperation extends AbstractOperation
{

    private EJPluginBlockItemContainer   container;
    private EJPluginBlockItemProperties item;
    private AbstractNodeTreeSection      treeSection;
    private boolean                      dirty;

    private int                          index = -1;

    public BlockItemAddOperation(final AbstractNodeTreeSection treeSection, EJPluginBlockItemContainer container,
            EJPluginBlockItemProperties blockProperties, int index)
    {
        super("Add Block Item");
        this.treeSection = treeSection;
        this.container = container;
        this.item = blockProperties;
        this.index = index;
    }

    public BlockItemAddOperation(final AbstractNodeTreeSection treeSection, EJPluginBlockItemContainer container,
            EJPluginBlockItemProperties blockProperties)
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
            if (index == -1|| index>=container.getItemCount())
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
                   // treeSection.expand(abstractNode, 2);

                    updateMirrorItems();
                    
                }
            });
        }

        return Status.OK_STATUS;
    }

    
    
    protected void updateMirrorItems()
    {
        for (EJPluginBlockProperties childProperties : container.getBlockProperties().getMirrorChildren())
        {
            EJPluginBlockItemContainer itemContainer = childProperties.getItemContainer();
            Object findNode = (itemContainer);
            if (findNode != null)
            {
                treeSection.refresh(itemContainer.getBlockProperties());
            }
        }

    }
    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        if (container != null)
        {
            container.removeItem(item,false);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh((container), true);
                    updateMirrorItems();
                }
            });
        }

        return Status.OK_STATUS;
    }

}
