package org.entirej.ide.ui.editors.form.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovMappingContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class LovMappingRemoveOperation extends AbstractOperation
{

    private EJPluginLovMappingContainer container;
    private EJPluginLovMappingProperties  blockProperties;
    private AbstractNodeTreeSection     treeSection;
    private boolean                     dirty;
    private int                         index = -1;
    private boolean cleanup;
    private List<EJPluginScreenItemProperties> useage = new ArrayList<EJPluginScreenItemProperties>();
    

    public LovMappingRemoveOperation(final AbstractNodeTreeSection treeSection, EJPluginLovMappingContainer container, EJPluginLovMappingProperties blockProperties,boolean cleanup)
    {
        super("Remove Lov Mapping");
        this.treeSection = treeSection;

        this.container = container;
        this.blockProperties = blockProperties;
        
        this.cleanup = cleanup;
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

            index = container.removeLovMappingProperties(blockProperties);
            
            if(cleanup)
            {
                
                removeMappingOnBlock(blockProperties.getName(), blockProperties.getMappedBlock().getScreenItemGroupContainer(EJScreenType.MAIN), EJScreenType.MAIN);
                removeMappingOnBlock(blockProperties.getName(), blockProperties.getMappedBlock().getScreenItemGroupContainer(EJScreenType.INSERT), EJScreenType.INSERT);
                removeMappingOnBlock(blockProperties.getName(), blockProperties.getMappedBlock().getScreenItemGroupContainer(EJScreenType.UPDATE), EJScreenType.UPDATE);
                removeMappingOnBlock(blockProperties.getName(), blockProperties.getMappedBlock().getScreenItemGroupContainer(EJScreenType.QUERY), EJScreenType.QUERY);
            }
            
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {

                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh((container), true);
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
                container.addLovMappingProperties(blockProperties);
            }
            else
            {
                container.addLovMappingProperties(index, blockProperties);
            }
            
            if(cleanup)
            {
                for (EJPluginScreenItemProperties ejPluginScreenItemProperties : useage)
                {
                    ejPluginScreenItemProperties.setLovMappingName(blockProperties.getName());
                }
                useage.clear();
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

        return Status.OK_STATUS;
    }
    
    
    private void removeMappingOnBlock(String oldName,EJItemGroupPropertiesContainer container, EJScreenType EJScreenType)
    {
        for (EJItemGroupProperties itemGroupProperties : container.getAllItemGroupProperties())
        {
            removeMappingOnBlock(oldName, itemGroupProperties, EJScreenType);
        }
    }

    private void removeMappingOnBlock(String oldName, EJItemGroupProperties itemGroupProperties, EJScreenType EJScreenType)
    {
        for (EJScreenItemProperties screenItemProperties : itemGroupProperties.getAllItemProperties())
        {
            if (screenItemProperties.getLovMappingName() != null && screenItemProperties.getLovMappingName().equals(oldName))
            {
                switch (EJScreenType)
                {
                    case MAIN:
                        ((EJPluginMainScreenItemProperties) screenItemProperties).setLovMappingName(null);
                        break;
                    case INSERT:
                        ((EJPluginInsertScreenItemProperties) screenItemProperties).setLovMappingName(null);
                        break;
                    case QUERY:
                        ((EJPluginQueryScreenItemProperties) screenItemProperties).setLovMappingName(null);
                        break;
                    case UPDATE:
                        ((EJPluginUpdateScreenItemProperties) screenItemProperties).setLovMappingName(null);
                        break;
                        
                }
                
                useage.add((EJPluginScreenItemProperties)screenItemProperties);

            }
        }

        removeMappingOnBlock(oldName,  itemGroupProperties.getChildItemGroupContainer(), EJScreenType);
    }
    

}
