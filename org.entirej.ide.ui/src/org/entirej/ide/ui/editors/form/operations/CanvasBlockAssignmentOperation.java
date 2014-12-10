package org.entirej.ide.ui.editors.form.operations;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginStackedPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;

public class CanvasBlockAssignmentOperation extends AbstractOperation
{

    private EJPluginCanvasProperties      canvas;
    private AbstractNodeTreeSection       treeSection;
    private boolean                       dirty;
    private List<EJPluginBlockProperties> properties = new ArrayList<EJPluginBlockProperties>();

    public CanvasBlockAssignmentOperation(final AbstractNodeTreeSection treeSection, EJPluginCanvasProperties blockProperties)
    {
        super("Remove Canvas Block Assignment");
        this.treeSection = treeSection;

        this.canvas = blockProperties;
    }

    @Override
    public IStatus execute(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {
        dirty = treeSection.isDirty();
        properties.clear();
        cleanBlockAssignment(canvas);
        treeSection.getEditor().setDirty(true);
        return Status.OK_STATUS;
    }

    @Override
    public IStatus redo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {

        for (EJPluginBlockProperties item : properties)
        {
            item.setCanvasName("");
        }
        treeSection.getEditor().setDirty(true);
        return Status.OK_STATUS;
    }

    @Override
    public IStatus undo(IProgressMonitor monitor, IAdaptable info) throws ExecutionException
    {

        for (EJPluginBlockProperties item : properties)
        {
            item.setCanvasName(canvas.getName());
        }
        treeSection.getEditor().setDirty(dirty);
        return Status.OK_STATUS;
    }

    private void cleanBlockAssignment(EJPluginCanvasProperties source)
    {
        EJBlockProperties blockProperties = source.getBlockProperties();
        if (blockProperties != null)
        {
            properties.add((EJPluginBlockProperties) blockProperties);
            ((EJPluginBlockProperties) blockProperties).setCanvasName("");
        }

        for (EJPluginCanvasProperties sub : source.getGroupCanvasContainer().getCanvasProperties())
        {
            cleanBlockAssignment(sub);
        }
        for (EJPluginCanvasProperties sub : source.getPopupCanvasContainer().getCanvasProperties())
        {
            cleanBlockAssignment(sub);
        }
        for (EJPluginCanvasProperties sub : source.getSplitCanvasContainer().getCanvasProperties())
        {
            cleanBlockAssignment(sub);
        }
        for (EJPluginTabPageProperties tabPageProperties : source.getTabPageContainer().getTabPageProperties())
        {
            for (EJPluginCanvasProperties sub : tabPageProperties.getContainedCanvases().getCanvasProperties())
            {
                cleanBlockAssignment(sub);
            }
        }
        for (EJPluginStackedPageProperties stackedPageProperties : source.getStackedPageContainer().getStackedPageProperties())
        {
            for (EJPluginCanvasProperties sub : stackedPageProperties.getContainedCanvases().getCanvasProperties())
            {
                cleanBlockAssignment(sub);
            }
        }
    }

}
