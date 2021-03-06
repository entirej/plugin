/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginObjectGroupContainer;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.form.operations.ObjectGroupAddOperation;
import org.entirej.ide.ui.editors.form.operations.ObjectGroupRemoveOperation;
import org.entirej.ide.ui.editors.operations.ReversibleOperation;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;

public class ObjectGroupNode extends AbstractNode<EJPluginObjectGroupContainer> implements NodeMoveProvider
{
    private final FormDesignTreeSection treeSection;
    private final AbstractEJFormEditor  editor;
    private final static Image          GROUP    = EJUIImages.getImage(EJUIImages.DESC_MENU_GROUP);
    private final static Image          OBJGROUP = EJUIImages.getImage(EJUIImages.DESC_OBJGROUP);

    public ObjectGroupNode(FormDesignTreeSection treeSection)
    {
        super(null, treeSection.getEditor().getFormProperties().getObjectGroupContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public String getName()
    {

        return "ObjectGroups";
    }

    @Override
    public <S> S getAdapter(Class<S> adapter)
    {
        if (NodeValidateProvider.class.isAssignableFrom(adapter))
        {
            return adapter.cast(new AbstractMarkerNodeValidator()
            {

                public void refreshNode()
                {
                    treeSection.refresh(ObjectGroupNode.this);
                }

                @Override
                public List<IMarker> getMarkers()
                {
                    List<IMarker> fmarkers = new ArrayList<IMarker>();

                    IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                    for (IMarker marker : markers)
                    {
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                        if ((tag & FormNodeTag.GROUP) != 0 && (tag & FormNodeTag.OBJGROUP) != 0)
                        {
                            fmarkers.add(marker);
                        }
                    }

                    return fmarkers;
                }
            });
        }
        return super.getAdapter(adapter);
    }

    @Override
    public String getToolTipText()
    {
        return "Form ObjectGroup definitions";
    }

    @Override
    public Image getImage()
    {
        return GROUP;
    }

    @Override
    public boolean isLeaf()
    {
        return source.isEmpty();
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {
        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
        List<EJPluginObjectGroupProperties> allObjectGroupProperties = source.getAllObjectGroupProperties();
        for (EJPluginObjectGroupProperties ObjectGroupProperties : allObjectGroupProperties)
        {
            nodes.add(new ObjectNode(this, ObjectGroupProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        return new Action[] { treeSection.createObjectGroupAction() };
    }

    protected boolean supportObjectGroupDelete()
    {
        return true;
    }

    protected boolean supportObjectGroupRename()
    {
        return false;
    }

    protected boolean supportCanvas()
    {
        return true;
    }

    @Override
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        return new AbstractDescriptor<?>[] {};
    }

    class ObjectNode extends AbstractNode<EJPluginObjectGroupProperties> implements Neighbor, Movable, NodeOverview
    {

        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(ObjectGroupNode.this);
                                                          }

                                                          @Override
                                                          public List<IMarker> getMarkers()
                                                          {
                                                              List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                              IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                                                              for (IMarker marker : markers)
                                                              {
                                                                  int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                  if ((tag & FormNodeTag.OBJGROUP) != 0 && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(FormNodeTag.OBJGROUP_ID, null)))
                                                                  {

                                                                      fmarkers.add(marker);
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

        public ObjectNode(AbstractNode<?> parent, EJPluginObjectGroupProperties source)
        {
            super(parent, source);

        }

        @Override
        public String getName()
        {
            return source.getName();
        }

        @Override
        public String getNodeDescriptorDetails()
        {
            return "Click <a href=\"http://docs.entirej.com/display/EJ1/ObjectGroup+Properties\">here</a> for more information on ObjectGroup Properties.  All mandatory properties are denoted by \"*\"";
        }

        @Override
        public Action[] getActions()
        {

            return new Action[] {};

        }

        public void addOverview(StyledString styledString)
        {

            if (!source.isInitialized())
            {
                styledString.append(" [ ", StyledString.QUALIFIER_STYLER);

                styledString.append(" < missing! >", StyledString.QUALIFIER_STYLER);

                styledString.append(" ] ", StyledString.QUALIFIER_STYLER);
            }

        }

        public <S> S getAdapter(Class<S> adapter)
        {

            if (NodeValidateProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(validator);
            }

            if (IFormPreviewProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(new FormCanvasPreviewImpl()
                {
                    @Override
                    protected void setPreviewBackground(Control control, Color color)
                    {
                        // IGNORE
                    }

                    @Override
                    protected EJPluginFormProperties getFormProperties(AbstractEJFormEditor editor)
                    {
                        return source;
                    }

                    @Override
                    public String getDescription()
                    {
                        return "preview the defined layout in ObjectGroup.";
                    }

                    @Override
                    protected void createComponent(Composite parent, EJPluginCanvasProperties component)
                    {
                        if (component.getPluginBlockProperties() != null)
                        {
                            EJPluginMainScreenProperties mainScreenProperties = component.getPluginBlockProperties().getMainScreenProperties();

                            Composite layoutBody = new Composite(parent, SWT.NONE);

                            layoutBody.setLayout(new FillLayout());

                            layoutBody.setLayoutData(createGridData(component));
                            component.getPluginBlockProperties().getBlockRendererDefinition()
                                    .addBlockControlToCanvas(mainScreenProperties, component.getPluginBlockProperties(), layoutBody, editor.getToolkit());
                        }
                        else
                        {
                            super.createComponent(parent, component);
                        }
                    }
                });
            }
            return null;
        }

        @Override
        public Image getImage()
        {

            return OBJGROUP;
        }

        public boolean canMove()
        {
            return true;
        }

        public Object getNeighborSource()
        {
            return source;
        }

        @Override
        public boolean isLeaf()
        {
            return false;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            final List<EJPluginBlockProperties> allBlockProperties = source.getBlockContainer().getAllBlockProperties();
            if (allBlockProperties.size() > 0)
            {
                AbstractNode<?> blocks = new AbstractNode<EJPluginObjectGroupProperties>(this, source)
                {
                    public String getName()
                    {

                        return "Blocks";
                    }

                    public Image getImage()
                    {

                        return EJUIImages.getImage(EJUIImages.DESC_MENU_GROUP);
                    }

                    @Override
                    public boolean isLeaf()
                    {
                        return false;
                    }

                    @Override
                    public AbstractNode<?>[] getChildren()
                    {
                        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
                        final Image BLOCK = EJUIImages.getImage(EJUIImages.DESC_BLOCK);
                        final Image BLOCK_MIRROR = EJUIImages.getImage(EJUIImages.DESC_BLOCK_MIRROR);
                        final Image BLOCK_NTB = EJUIImages.getImage(EJUIImages.DESC_BLOCK_NTB);
                        final Image BLOCK_REF = EJUIImages.getImage(EJUIImages.DESC_BLOCK_REF);
                        for (final EJPluginBlockProperties blockProperties : allBlockProperties)
                        {
                            nodes.add(new GroupElementNode(this, new GroupElement()
                            {

                                public AbstractDescriptor<?>[] getNodeDescriptors()
                                {
                                    return new AbstractDescriptor<?>[] { new AbstractTextDescriptor("Block")
                                    {
                                        @Override
                                        public void runOperation(AbstractOperation operation)
                                        {
                                            editor.execute(operation);

                                        }

                                        public boolean hasLableLink()
                                        {
                                            return true;
                                        }

                                        @Override
                                        public String lableLinkActivator()
                                        {

                                            treeSection.selectNodes(true, (blockProperties));

                                            return getValue();
                                        }

                                        @Override
                                        public void setValue(String value)
                                        {

                                        }

                                        @Override
                                        public String getValue()
                                        {
                                            return getName();
                                        }

                                        Text text;

                                        @Override
                                        public void addEditorAssist(Control control)
                                        {

                                            text = (Text) control;
                                            text.setEditable(false);
                                        }
                                    } };
                                }

                                public String getName()
                                {

                                    return blockProperties.getName();
                                }

                                public Image getImage()
                                {
                                    if (blockProperties.isMirrorChild())
                                        return BLOCK_MIRROR;

                                    if (blockProperties.isReferenceBlock())
                                        return BLOCK_REF;
                                    if (blockProperties.isControlBlock())
                                        return BLOCK_NTB;

                                    return BLOCK;
                                }

                                public void addOverview(StyledString styledString)
                                {

                                }
                            }));
                        }

                        return nodes.toArray(new AbstractNode<?>[0]);
                    }

                };
                nodes.add(blocks);
            }

            final List<EJPluginRelationProperties> allRelationProperties = source.getRelationContainer().getAllRelationProperties();
            if (allRelationProperties.size() > 0)
            {
                AbstractNode<?> relations = new AbstractNode<EJPluginObjectGroupProperties>(this, source)
                {
                    public String getName()
                    {

                        return "Relations";
                    }

                    public Image getImage()
                    {

                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                    }

                    @Override
                    public boolean isLeaf()
                    {
                        return false;
                    }

                    @Override
                    public AbstractNode<?>[] getChildren()
                    {
                        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

                        for (final EJPluginRelationProperties blockProperties : allRelationProperties)
                        {
                            nodes.add(new GroupElementNode(this, new GroupElement()
                            {

                                public AbstractDescriptor<?>[] getNodeDescriptors()
                                {
                                    return new AbstractDescriptor<?>[] { new AbstractTextDescriptor("Relation")
                                    {

                                        public boolean hasLableLink()
                                        {
                                            return true;
                                        }

                                        @Override
                                        public void runOperation(AbstractOperation operation)
                                        {
                                            editor.execute(operation);

                                        }

                                        @Override
                                        public String lableLinkActivator()
                                        {

                                            treeSection.selectNodes(true, (blockProperties));

                                            return getValue();
                                        }

                                        @Override
                                        public void setValue(String value)
                                        {

                                        }

                                        @Override
                                        public String getValue()
                                        {
                                            return getName();
                                        }

                                        Text text;

                                        @Override
                                        public void addEditorAssist(Control control)
                                        {

                                            text = (Text) control;
                                            text.setEditable(false);
                                        }
                                    } };
                                }

                                public String getName()
                                {

                                    return blockProperties.getName();
                                }

                                public Image getImage()
                                {
                                    return EJUIImages.getImage(EJUIImages.DESC_BLOCK_RELATION);
                                }

                                public void addOverview(StyledString styledString)
                                {

                                }
                            }));
                        }

                        return nodes.toArray(new AbstractNode<?>[0]);
                    }

                };
                nodes.add(relations);
            }

            final Collection<EJCanvasProperties> retriveAllCanvases = EJPluginCanvasRetriever.retriveAllCanvases(source);
            if (retriveAllCanvases.size() > 0)
            {
                final Image BLOCK = EJUIImages.getImage(EJUIImages.DESC_CANVAS_BLOCK);
                final Image GROUP = EJUIImages.getImage(EJUIImages.DESC_CANVAS_GROUP);
                final Image STACKED = EJUIImages.getImage(EJUIImages.DESC_CANVAS_STACKED);
                final Image POPUP = EJUIImages.getImage(EJUIImages.DESC_CANVAS_POPUP);
                final Image TAB = EJUIImages.getImage(EJUIImages.DESC_CANVAS_TAB);
                final Image DRAWER = EJUIImages.getImage(EJUIImages.DESC_CANVAS_DRAWER);
                AbstractNode<?> canvases = new AbstractNode<EJPluginObjectGroupProperties>(this, source)
                {
                    public String getName()
                    {

                        return "Canvases";
                    }

                    public Image getImage()
                    {

                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                    }

                    @Override
                    public boolean isLeaf()
                    {
                        return false;
                    }

                    @Override
                    public AbstractNode<?>[] getChildren()
                    {
                        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

                        for (final EJCanvasProperties blockProperties : retriveAllCanvases)
                        {
                            nodes.add(new GroupElementNode(this, new GroupElement()
                            {

                                public AbstractDescriptor<?>[] getNodeDescriptors()
                                {
                                    return new AbstractDescriptor<?>[] { new AbstractTextDescriptor("Canvas")
                                    {

                                        public boolean hasLableLink()
                                        {
                                            return true;
                                        }

                                        @Override
                                        public String lableLinkActivator()
                                        {

                                            treeSection.selectNodes(true, (blockProperties));

                                            return getValue();
                                        }

                                        @Override
                                        public void runOperation(AbstractOperation operation)
                                        {
                                            editor.execute(operation);

                                        }

                                        @Override
                                        public void setValue(String value)
                                        {

                                        }

                                        @Override
                                        public String getValue()
                                        {
                                            return getName();
                                        }

                                        Text text;

                                        @Override
                                        public void addEditorAssist(Control control)
                                        {

                                            text = (Text) control;
                                            text.setEditable(false);
                                        }
                                    } };
                                }

                                public String getName()
                                {

                                    return blockProperties.getName();
                                }

                                public Image getImage()
                                {
                                    switch (blockProperties.getType())
                                    {
                                        case GROUP:
                                        case SPLIT:
                                            return GROUP;
                                        case POPUP:
                                            return POPUP;
                                        case TAB:
                                            return TAB;
                                        case DRAWER:
                                            return DRAWER;
                                        case STACKED:
                                            return STACKED;
                                        default:
                                            return BLOCK;
                                    }
                                }

                                public void addOverview(StyledString styledString)
                                {

                                }
                            }));
                        }

                        return nodes.toArray(new AbstractNode<?>[0]);
                    }

                };
                nodes.add(canvases);
            }

            final List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = source.getLovDefinitionContainer().getAllLovDefinitionProperties();

            if (allLovDefinitionProperties.size() > 0)
            {
                AbstractNode<?> lovs = new AbstractNode<EJPluginObjectGroupProperties>(this, source)
                {
                    public String getName()
                    {

                        return "LOV Definitions";
                    }

                    public Image getImage()
                    {

                        return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                    }

                    @Override
                    public boolean isLeaf()
                    {
                        return false;
                    }

                    @Override
                    public AbstractNode<?>[] getChildren()
                    {
                        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

                        for (final EJPluginLovDefinitionProperties blockProperties : allLovDefinitionProperties)
                        {
                            nodes.add(new GroupElementNode(this, new GroupElement()
                            {

                                public AbstractDescriptor<?>[] getNodeDescriptors()
                                {
                                    return new AbstractDescriptor<?>[] { new AbstractTextDescriptor("LOV")
                                    {

                                        public boolean hasLableLink()
                                        {
                                            return true;
                                        }

                                        @Override
                                        public void runOperation(AbstractOperation operation)
                                        {
                                            editor.execute(operation);

                                        }

                                        @Override
                                        public String lableLinkActivator()
                                        {

                                            treeSection.selectNodes(true, (blockProperties));

                                            return getValue();
                                        }

                                        @Override
                                        public void setValue(String value)
                                        {

                                        }

                                        @Override
                                        public String getValue()
                                        {
                                            return getName();
                                        }

                                        Text text;

                                        @Override
                                        public void addEditorAssist(Control control)
                                        {

                                            text = (Text) control;
                                            text.setEditable(false);
                                        }
                                    } };
                                }

                                public String getName()
                                {

                                    return blockProperties.getName();
                                }

                                public Image getImage()
                                {
                                    return EJUIImages.getImage(EJUIImages.DESC_LOV_DEF);
                                }

                                public void addOverview(StyledString styledString)
                                {

                                }
                            }));
                        }

                        return nodes.toArray(new AbstractNode<?>[0]);
                    }

                };
                nodes.add(lovs);
            }

            return nodes.toArray(new AbstractNode<?>[0]);

        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            if (supportObjectGroupDelete())
                return new INodeDeleteProvider()
                {

                    public void delete(boolean cleanup)
                    {

                        if (cleanup)
                        {
                            source.removeObjects(ObjectGroupNode.this.source.getFormProperties());
                            ObjectGroupNode.this.source.removeObjectGroupProperties(source);
                            
                        }
                        else
                        {
                            ObjectGroupNode.this.source.getAllObjectGroupProperties().remove(source);
                        }
                        editor.setDirty(true);
                        treeSection.refresh();

                    }

                    public AbstractOperation deleteOperation(boolean cleanup)
                    {
                        if (cleanup)
                        {
                            ReversibleOperation operation = new ReversibleOperation("Remove Object Group")
                            {

                                @Override
                                protected void refresh()
                                {
                                    Display.getDefault().asyncExec(new Runnable()
                                    {
                                        
                                        public void run()
                                        {
                                            treeSection.refresh();
                                            
                                        }
                                    });
                                   
                                }
                            };
                            ObjectGroupRemoveOperation.removeObjects(treeSection, source, (ObjectGroupNode.this.source.getFormProperties()), operation);
                            return operation;
                        }
                        return new ObjectGroupRemoveOperation(treeSection, ObjectGroupNode.this.source, source);
                    }
                };
            return super.getDeleteProvider();
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            if (!supportObjectGroupRename())
                return null;

            return null;
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            final List<IMarker> fmarkers = validator.getMarkers();

            final AbstractTextDescriptor referencedDescriptor = new AbstractTextDescriptor("Referenced ObjectGroup")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.REF) != 0;
                                   }
                               };

                @Override
                public String getErrors()
                {

                    return validator.getErrorMarkerMsg(fmarkers, vfilter);
                }

                @Override
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }

                public boolean hasLableLink()
                {
                    return true;
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public String lableLinkActivator()
                {

                    try
                    {
                        IFile file = editor.getFormProperties().getEntireJProperties().getObjectGroupFile(getValue());
                        if (file != null)
                        {
                            BasicNewResourceWizard.selectAndReveal(file, EJUIPlugin.getActiveWorkbenchWindow());
                            final IWorkbenchPage activePage = EJUIPlugin.getActiveWorkbenchWindow().getActivePage();
                            if (activePage != null)
                            {
                                IDE.openEditor(activePage, file, true);
                            }
                        }
                    }
                    catch (EJDevFrameworkException e)
                    {
                        EJCoreLog.logException(e);
                    }
                    catch (PartInitException e)
                    {
                        EJCoreLog.logException(e);
                    }

                    return getValue();
                }

                @Override
                public void setValue(String value)
                {

                }

                @Override
                public String getValue()
                {
                    return source.getName();
                }

                Text text;

                @Override
                public void addEditorAssist(Control control)
                {

                    text = (Text) control;
                    text.setEditable(false);
                }
            };

            return new AbstractDescriptor<?>[] { referencedDescriptor };

        }

        public Action createCopyNameAction()
        {

            return new Action("Copy ObjectGroup Name")
            {

                @Override
                public void runWithEvent(Event event)
                {
                    final Clipboard cb = new Clipboard(EJUIPlugin.getStandardDisplay());
                    TextTransfer textTransfer = TextTransfer.getInstance();
                    cb.setContents(new Object[] { source.getName() }, new Transfer[] { textTransfer });
                }
            };
        }

    }

    private static interface GroupElement
    {

        public String getName();

        public Image getImage();

        public AbstractDescriptor<?>[] getNodeDescriptors();

        public void addOverview(StyledString styledString);
    }

    private static class GroupElementNode extends AbstractNode<GroupElement> implements NodeOverview
    {

        public GroupElementNode(AbstractNode<?> parent, GroupElement source)
        {
            super(parent, source);
        }

        public void addOverview(StyledString styledString)
        {
            source.addOverview(styledString);

        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            return source.getNodeDescriptors();
        }

        @Override
        public Image getImage()
        {
            return source.getImage();
        }

        @Override
        public String getName()
        {
            return source.getName();
        }

    }

    public boolean canMove(Neighbor relation, Object source)
    {
        return source instanceof EJPluginObjectGroupProperties;
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginObjectGroupProperties> items = source.getAllObjectGroupProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addObjectGroupProperties(index, (EJPluginObjectGroupProperties) dSource);
            }
        }
        else
            source.addObjectGroupProperties((EJPluginObjectGroupProperties) dSource);

    }

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginObjectGroupProperties> items = source.getAllObjectGroupProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                return new ObjectGroupAddOperation(treeSection, source, (EJPluginObjectGroupProperties) dSource, index);
            }
        }
        return new ObjectGroupAddOperation(treeSection, source, (EJPluginObjectGroupProperties) dSource, -1);
    }

}
