package org.entirej.ide.ui.editors.form.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class LovRemoveOperation extends AbstractOperation
{

    private EJPluginLovDefinitionContainer container;
    private EJPluginLovDefinitionProperties      blockProperties;
    private AbstractNodeTreeSection treeSection;
    private boolean                 dirty;
    private int                     index = -1;

    public LovRemoveOperation(final AbstractNodeTreeSection treeSection, EJPluginLovDefinitionContainer container, EJPluginLovDefinitionProperties blockProperties)
    {
        super("Remove LOV");
        this.treeSection = treeSection;

        this.container = container;
        this.blockProperties = blockProperties;
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

            index = container.removeLovDefinitionProperties(blockProperties);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(true);
                     treeSection.refresh();
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
                container.addLovDefinitionProperties(blockProperties);
            }
            else
            {
                container.addLovDefinitionProperties(index, blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(dirty);
                     treeSection.refresh();
                    treeSection.selectNodes(true, blockProperties);
                   // treeSection.expand(abstractNode, 2);

                }
            });
        }

        return Status.OK_STATUS;
    }

   
}
