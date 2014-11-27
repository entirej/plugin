package org.entirej.ide.ui.editors.report.operations;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class ReportBlockAddOperation extends AbstractOperation
{

    private EJReportBlockContainer        container;
    private BlockGroup        group;
    private EJPluginReportBlockProperties blockProperties;
    private AbstractNodeTreeSection       treeSection;
    private boolean dirty;

    public ReportBlockAddOperation(final AbstractNodeTreeSection treeSection, EJReportBlockContainer container, EJPluginReportBlockProperties blockProperties)
    {
        super("Add Block");
        this.treeSection = treeSection;
        this.container = container;
        this.blockProperties = blockProperties;
    }
    public ReportBlockAddOperation(final AbstractNodeTreeSection treeSection, BlockGroup group, EJPluginReportBlockProperties blockProperties)
    {
        super("Add Block");
        this.treeSection = treeSection;
        this.group = group;
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
        
        if(container!=null)
        {
            container.addBlockProperties(blockProperties);

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
        if(group!=null)
        {
            group.addBlockProperties(blockProperties);
            
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
        if(container!=null)
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
        if(group!=null)
        {
            group.removeBlockProperties(blockProperties);
            
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
