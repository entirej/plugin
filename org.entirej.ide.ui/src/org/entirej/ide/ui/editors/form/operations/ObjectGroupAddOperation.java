package org.entirej.ide.ui.editors.form.operations;

import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginObjectGroupContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginRelationContainer;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.operations.ReversibleOperation;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class ObjectGroupAddOperation extends AbstractOperation
{

    private EJPluginObjectGroupContainer  container;

    private EJPluginObjectGroupProperties blockProperties;
    private AbstractNodeTreeSection         treeSection;
    private boolean                         dirty;

    private int                             index = -1;

    public ObjectGroupAddOperation(final AbstractNodeTreeSection treeSection, EJPluginObjectGroupContainer container,
            EJPluginObjectGroupProperties blockProperties, int index)
    {
        super("Add ObjectGroup");
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
                container.addObjectGroupProperties(blockProperties);
            else
            {
                container.addObjectGroupProperties(index, blockProperties);
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
            container.removeObjectGroupProperties(blockProperties);
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

    
    
    public static AbstractOperation importObjectsToForm(final AbstractNodeTreeSection treeSection, final EJPluginObjectGroupProperties groupProperties,
            final EJPluginFormProperties form,ReversibleOperation operation)
    {
       
        EJPluginBlockContainer blockContainer = groupProperties.getBlockContainer();
        List<EJPluginBlockProperties> allBlockProperties = blockContainer.getAllBlockProperties();
        // import all blocks to form
        for (final EJPluginBlockProperties block : allBlockProperties)
        {

            operation.add(new AbstractOperation(" Block")
            {

                @Override
                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    block.setReferencedObjectGroupName("");
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
                    block.setReferencedObjectGroupName(groupProperties.getName());// mark
                                                                                  // as
                                                                                  // import
                                                                                  // from
                                                                                  // this
                    return Status.OK_STATUS;
                }
            });

            final EJPluginBlockProperties oldProp = form.getBlockContainer().getBlockProperties(block.getName());
            if (oldProp != null && groupProperties.getName().equals(oldProp.getReferencedObjectGroupName()))
            {

                operation.add(new AbstractOperation("Peplace Block")
                {

                    @Override
                    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                    {
                        form.getBlockContainer().replaceBlockProperties(block, oldProp);
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
                        form.getBlockContainer().replaceBlockProperties(oldProp, block);
                        return Status.OK_STATUS;
                    }
                });
            }
            else
            {
                operation.add(new BlockAddOperation(treeSection, form.getBlockContainer(), block, -1));
            }

        }

        EJPluginRelationContainer relationContainer = groupProperties.getRelationContainer();
        List<EJPluginRelationProperties> relationProperties = relationContainer.getAllRelationProperties();
        // import all relations
        for (final EJPluginRelationProperties relation : relationProperties)
        {

            operation.add(new AbstractOperation(" Block")
            {

                @Override
                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    relation.setReferencedObjectGroupName("");
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
                    relation.setReferencedObjectGroupName(groupProperties.getName());
                    return Status.OK_STATUS;
                }
            });
            operation.add(new RelationAddOperation(treeSection, form.getRelationContainer(), relation, -1));
        }

        EJPluginCanvasContainer formContainer = form.getCanvasContainer();

        EJPluginCanvasContainer canvasContainer = groupProperties.getCanvasContainer();
        Collection<EJPluginCanvasProperties> allCanvasProperties = canvasContainer.getCanvasProperties();
        for (final EJPluginCanvasProperties canvas : allCanvasProperties)
        {

            if (canvas.getType() == EJCanvasType.POPUP)
            {
                operation.add(new CanvasAddOperation(treeSection, form.getCanvasContainer(), canvas, -1));

            }
            else
            {
                final EJPluginCanvasProperties canvasProperties = (EJPluginCanvasProperties) EJPluginCanvasRetriever
                        .getCanvasProperties(form, canvas.getName());
                if (canvasProperties == null)
                {
                    operation.add(new CanvasAddOperation(treeSection, formContainer, canvas, -1));
                }
                else
                {

                    operation.add(new AbstractOperation(" Block")
                    {

                        @Override
                        public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                        {

                            canvasProperties.setObjectGroupRoot(false);
                            canvasProperties.getParentCanvasContainer().replaceCanvasProperties(canvas, canvasProperties);
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
                            canvasProperties.setObjectGroupRoot(true);
                            canvas.setWidth(canvasProperties.getWidth());
                            canvas.setHeight(canvasProperties.getHeight());
                            canvas.setExpandHorizontally(canvasProperties.canExpandHorizontally());
                            canvas.setExpandVertically(canvasProperties.canExpandVertically());
                            canvas.setVerticalSpan(canvasProperties.getVerticalSpan());
                            canvas.setReferredFormId(canvasProperties.getReferredFormId());
                            canvas.setHorizontalSpan(canvasProperties.getHorizontalSpan());
                            canvasProperties.getParentCanvasContainer().replaceCanvasProperties(canvasProperties, canvas);
                            return Status.OK_STATUS;
                        }
                    });
                }

                operation.add(new AbstractOperation(" Block")
                {

                    @Override
                    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                    {
                        canvas.setObjectGroupRoot(false);
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
                        canvas.setObjectGroupRoot(true);
                        return Status.OK_STATUS;
                    }
                });
            }
        }

        Collection<EJCanvasProperties> retriveAllCanvases = EJPluginCanvasRetriever.retriveAllCanvases(groupProperties);
        for (EJCanvasProperties canvas : retriveAllCanvases)
        {
            final EJPluginCanvasProperties canvasPlug = (EJPluginCanvasProperties) canvas;
          
            
            
            operation.add(new AbstractOperation(" Block")
            {

                @Override
                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    canvasPlug.setReferencedObjectGroupName("");
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
                    canvasPlug.setReferencedObjectGroupName(groupProperties.getName());
                    return Status.OK_STATUS;
                }
            });

        }

        EJPluginLovDefinitionContainer lovDefinitionContainer = groupProperties.getLovDefinitionContainer();
        List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = lovDefinitionContainer.getAllLovDefinitionProperties();
        for (final EJPluginLovDefinitionProperties lov : allLovDefinitionProperties)
        {
            
         
            operation.add(new AbstractOperation("Block")
            {

                @Override
                public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
                {
                    lov.setReferencedObjectGroupName("");
                    lov.getBlockProperties().setReferencedObjectGroupName("");
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
                    lov.setReferencedObjectGroupName(groupProperties.getName());
                    lov.getBlockProperties().setReferencedObjectGroupName(groupProperties.getName());
                    return Status.OK_STATUS;
                }
            });
            operation.add(new LovAddOperation(treeSection, form.getLovDefinitionContainer(), lov, -1));
        }

        operation.add(new ObjectGroupAddOperation(treeSection, form.getObjectGroupContainer(), groupProperties, -1));
       
        return operation;

    }
    
}
