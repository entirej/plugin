package org.entirej.ide.ui.editors.form.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovMappingContainer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class LovMappingAddOperation extends AbstractOperation
{

    private EJPluginLovMappingContainer  container;

    private EJPluginLovMappingProperties blockProperties;
    private AbstractNodeTreeSection         treeSection;
    private boolean                         dirty;

    private int                             index = -1;

    public LovMappingAddOperation(final AbstractNodeTreeSection treeSection, EJPluginLovMappingContainer container,
            EJPluginLovMappingProperties blockProperties, int index)
    {
        super("Add Lov Mapping");
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
            if (index == -1)
                container.addLovMappingProperties(blockProperties);
            else
            {
                container.addLovMappingProperties(index, blockProperties);
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
       

        return Status.OK_STATUS;
    }

    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        if (container != null)
        {
            container.removeLovMappingProperties(blockProperties);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh(treeSection.findNode(container), true);
                }
            });
        }
        

        return Status.OK_STATUS;
    }

}
