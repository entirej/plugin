package org.entirej.ide.ui.editors.form.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class DisplayItemAddOperation extends AbstractOperation
{

    private EJPluginItemGroupProperties container;
    private EJPluginScreenItemProperties      blockProperties;
    private AbstractNodeTreeSection treeSection;
    private boolean                 dirty;

    private int                     index = -1;

    public DisplayItemAddOperation(final AbstractNodeTreeSection treeSection, EJPluginItemGroupProperties container, EJPluginScreenItemProperties blockProperties, int index)
    {
        super("Add Item ");
        this.treeSection = treeSection;
        this.container = container;
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
            if (index == -1 || index>=container.getItemProperties().size())
                container.addItemProperties(blockProperties);
            else
            {
                container.addItemProperties(index, blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(true);
                    if(container.getBlockProperties().getLovDefinition()!=null)
                        treeSection.refresh((container.getBlockProperties().getLovDefinition()), true);
                    else
                        treeSection.refresh((container.getBlockProperties()), true);
                    treeSection.selectNodes(true, blockProperties);
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
            container.deleteItem(blockProperties);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(dirty);
                    if(container.getBlockProperties().getLovDefinition()!=null)
                        treeSection.refresh((container.getBlockProperties().getLovDefinition()), true);
                    else
                        treeSection.refresh((container.getBlockProperties()), true);
                }
            });
        }

        return Status.OK_STATUS;
    }

}
