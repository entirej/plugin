package org.entirej.ide.ui.editors.form.operations;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.operations.ReversibleOperation;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class ObjectGroupRemoveOperation extends AbstractOperation
{

    private EJPluginObjectGroupContainer  container;

    private EJPluginObjectGroupProperties blockProperties;
    private AbstractNodeTreeSection       treeSection;
    private boolean                       dirty;
    private int                           index = -1;

    public ObjectGroupRemoveOperation(final AbstractNodeTreeSection treeSection, EJPluginObjectGroupContainer container,
            EJPluginObjectGroupProperties blockProperties)
    {
        super("Remove Relation");
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

            index = container.removeObjectGroupProperties(blockProperties);
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh((container.getFormProperties()), true);
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
                container.addObjectGroupProperties(blockProperties);
            }
            else
            {
                container.addObjectGroupProperties(index, blockProperties);
            }

            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.getEditor().setDirty(dirty);
                    treeSection.refresh((container.getFormProperties()), true);
                    treeSection.selectNodes(true, blockProperties);
                    //treeSection.expand(abstractNode, 2);

                }
            });
        }

        return Status.OK_STATUS;
    }

    public static void removeObjects(final AbstractNodeTreeSection treeSection, final EJPluginObjectGroupProperties groupProperties,
            final EJPluginFormProperties form, ReversibleOperation operation)
    {
        EJPluginBlockContainer blockContainer = form.getBlockContainer();
        List<EJPluginBlockProperties> allBlockProperties = blockContainer.getAllBlockProperties();
        // clean all blocks to form
        for (EJPluginBlockProperties block : new ArrayList<EJPluginBlockProperties>(allBlockProperties))
        {
            if (block.isImportFromObjectGroup() && block.getReferencedObjectGroupName().equals(groupProperties.getName()))

            {
                operation.add(BlockRemoveOperation.createCleanupOperation(treeSection, blockContainer, block));
            }
        }

        EJPluginRelationContainer relationContainer = form.getRelationContainer();
        List<EJPluginRelationProperties> relationProperties = relationContainer.getAllRelationProperties();
        // clean all relations
        for (EJPluginRelationProperties relation : new ArrayList<EJPluginRelationProperties>(relationProperties))
        {
            if (relation.isImportFromObjectGroup() && relation.getReferencedObjectGroupName().equals(groupProperties.getName()))

            {

                operation.add(new RelationRemoveOperation(treeSection, relationContainer, relation));
            }
        }
        // clean all canvas
        EJPluginCanvasContainer canvasContainer = form.getCanvasContainer();
        Collection<EJPluginCanvasProperties> allCanvasProperties = canvasContainer.getCanvasProperties();
        for (EJPluginCanvasProperties canvas : new ArrayList<EJPluginCanvasProperties>(allCanvasProperties))
        {
            if (canvas.isImportFromObjectGroup() && canvas.getReferencedObjectGroupName().equals(groupProperties.getName()))

            {

                operation.add(new CanvasRemoveOperation(treeSection, canvasContainer, canvas));
            }
        }

        // clean all LOV
        EJPluginLovDefinitionContainer lovDefinitionContainer = form.getLovDefinitionContainer();
        List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = lovDefinitionContainer.getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties lov : new ArrayList<EJPluginLovDefinitionProperties>(allLovDefinitionProperties))
        {
            if (lov.isImportFromObjectGroup() && lov.getReferencedObjectGroupName().equals(groupProperties.getName()))

            {
                operation.add(new LovRemoveOperation(treeSection, lovDefinitionContainer, lov));
            }
        }
        operation.add(new ObjectGroupRemoveOperation(treeSection, form.getObjectGroupContainer(), groupProperties));
    }

}
