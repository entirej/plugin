package org.entirej.ide.ui.editors.form.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginRelationContainer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class RelationRemoveOperation extends AbstractOperation
{

    private EJPluginRelationContainer container;
    private EJPluginRelationProperties      blockProperties;
    private AbstractNodeTreeSection treeSection;
    private boolean                 dirty;
    private int                     index = -1;

    public RelationRemoveOperation(final AbstractNodeTreeSection treeSection, EJPluginRelationContainer container, EJPluginRelationProperties blockProperties)
    {
        super("Remove ObjectGroup");
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

            index = container.removeRelationProperties(blockProperties);
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
                container.addRelationProperties(blockProperties);
            }
            else
            {
                container.addRelationProperties(index, blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(dirty);
                     treeSection.refresh();
                    treeSection.selectNodes(true, blockProperties);
                    //treeSection.expand(abstractNode, 2);

                }
            });
        }

        return Status.OK_STATUS;
    }

   
}
