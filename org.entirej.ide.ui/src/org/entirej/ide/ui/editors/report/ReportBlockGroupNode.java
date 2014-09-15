/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.report;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.entirej.framework.core.actionprocessor.interfaces.EJBlockActionProcessor;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportBlockRenderers;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.reports.actionprocessor.EJDefaultReportActionProcessor;
import org.entirej.framework.reports.actionprocessor.interfaces.EJReportActionProcessor;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.form.FormNodeTag;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.wizards.service.NewEJPojoServiceWizard;

public class ReportBlockGroupNode extends AbstractNode<EJReportBlockContainer> implements NodeMoveProvider
{
    private final ReportDesignTreeSection       treeSection;
    private final AbstractEJReportEditor        editor;
    private final static Image                  GROUP            = EJUIImages.getImage(EJUIImages.DESC_MENU_GROUP);
    private final static Image                  BLOCK            = EJUIImages.getImage(EJUIImages.DESC_BLOCK);
    private final static Image                  BLOCK_MIRROR     = EJUIImages.getImage(EJUIImages.DESC_BLOCK_MIRROR);
    private final static Image                  BLOCK_MIRROR_REF = EJUIImages.getImage(EJUIImages.DESC_BLOCK_MIRROR_REF);
    private final static Image                  BLOCK_NTB        = EJUIImages.getImage(EJUIImages.DESC_BLOCK_NTB);
    private final static Image                  BLOCK_NTB_REF    = EJUIImages.getImage(EJUIImages.DESC_BLOCK_NTB_REF);
    private final static Image                  BLOCK_REF        = EJUIImages.getImage(EJUIImages.DESC_BLOCK_REF);
    private final EJDevItemWidgetChosenListener chosenListener   = new EJDevItemWidgetChosenListener()
                                                                 {

                                                                     public void fireRendererChosen(EJDevScreenItemDisplayProperties arg0)
                                                                     {
                                                                         if (arg0 != null && treeSection != null)
                                                                         {

                                                                             AbstractNode<?> findNode = treeSection.findNode(arg0, true);
                                                                             if (findNode != null)
                                                                             {
                                                                                 treeSection.selectNodes(true, findNode);
                                                                             }
                                                                         }

                                                                     }
                                                                 };

    public ReportBlockGroupNode(ReportDesignTreeSection treeSection)
    {
        super(null, treeSection.getEditor().getReportProperties().getBlockContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public String getName()
    {

        return "Blocks";
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
                    treeSection.refresh(ReportBlockGroupNode.this);
                }

                @Override
                public List<IMarker> getMarkers()
                {
                    List<IMarker> fmarkers = new ArrayList<IMarker>();

                    IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                    for (IMarker marker : markers)
                    {
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                        if ((tag & FormNodeTag.GROUP) != 0 && (tag & FormNodeTag.BLOCK) != 0)
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
        return "Report block definitions";
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

        List<BlockContainerItem> blockContainerItems = source.getBlockContainerItems();

        for (BlockContainerItem item : blockContainerItems)
        {
            if (item instanceof EJPluginReportBlockProperties)
            {

                nodes.add(new BlockNode(this, (EJPluginReportBlockProperties) item));
            }
            else if (item instanceof BlockGroup)
            {

                nodes.add(new BlockSubGroupNode(this, (BlockGroup) item));
            }
        }

        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        return new Action[] { treeSection.createNewBlockAction(false), treeSection.createNewBlockAction(true), null,createNewBlockGroupAction() };
    }

    public Action createNewBlockGroupAction()
    {

        return new Action("New Block Group")
        {

            @Override
            public void runWithEvent(Event event)
            {
                InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), "New Block Group", "Group Name", null, new IInputValidator()
                {

                    public String isValid(String newText)
                    {
                        if (newText == null || newText.trim().length() == 0)
                            return "Group name can't be empty.";
                        return null;
                    }
                });
                if (dlg.open() == Window.OK)
                {
                    BlockGroup blockGroup = new BlockGroup();
                    blockGroup.setName(dlg.getValue());
                    source.addBlockProperties(blockGroup);
                    treeSection.refresh(ReportBlockGroupNode.this);
                    treeSection.selectNodes(false, treeSection.findNode(blockGroup,true));
                }
            }
        };
    }

    protected boolean supportBlockDelete()
    {
        return true;
    }

    protected boolean supportBlockRename()
    {
        return true;
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

    class BlockSubGroupNode extends AbstractNode<BlockGroup> implements Neighbor, Movable, NodeOverview, NodeMoveProvider
    {
        public BlockSubGroupNode(AbstractNode<?> parent, BlockGroup source)
        {
            super(parent, source);

        }

        @Override
        public <S> S getAdapter(Class<S> adapter)
        {
            return ReportBlockGroupNode.this.getAdapter(adapter);
        }

        @Override
        public String getName()
        {
            return source.getName();
        }

        public void addOverview(StyledString styledString)
        {
            // todo:

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
        public Image getImage()
        {
            return ReportBlockGroupNode.this.getImage();
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    ReportBlockGroupNode.this.source.removeBlockContainerItem(source);

                    editor.setDirty(true);
                    treeSection.refresh(ReportBlockGroupNode.this);

                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Block Group [%s]", source.getName()),
                            "Group Name", source.getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Canvas name can't be empty.";
                                    if (source.getName().equals(newText.trim()))
                                        return "";
                                    if (source.getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        // String oldName = source.getName();
                        String newName = dlg.getValue().trim();

                        source.setName(newName);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh();

                            }
                        });
                    }

                }
            };
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

            List<EJPluginReportBlockProperties> allBlockProperties = source.getAllBlockProperties();
            for (EJPluginReportBlockProperties properties : allBlockProperties)
            {
                nodes.add(new BlockNode(this, properties));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return source instanceof EJPluginReportBlockProperties;
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginReportBlockProperties> items = source.getAllBlockProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.addBlockProperties(index, (EJPluginReportBlockProperties) dSource);
                }
            }
            else
                source.addBlockProperties((EJPluginReportBlockProperties) dSource);

        }

    }

    class BlockNode extends AbstractNode<EJPluginReportBlockProperties> implements Neighbor, Movable, NodeOverview
    {

        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(ReportBlockGroupNode.this);
                                                          }

                                                          @Override
                                                          public List<IMarker> getMarkers()
                                                          {
                                                              List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                              IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                                                              for (IMarker marker : markers)
                                                              {
                                                                  int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                  if ((tag & FormNodeTag.BLOCK) != 0 && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(FormNodeTag.BLOCK_ID, null)))
                                                                  {

                                                                      fmarkers.add(marker);
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

        public BlockNode(AbstractNode<?> parent, EJPluginReportBlockProperties source)
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
            return "Click <a href=\"http://docs.entirej.com/display/EJ1/Block+Properties\">here</a> for more information on Block Properties.  All mandatory properties are denoted by \"*\"";
        }

        @Override
        public Action[] getActions()
        {

            return new Action[] { treeSection.createNewBlockAction(false), treeSection.createNewBlockAction(true), null,createCopyNameAction() };

        }

        public void addOverview(StyledString styledString)
        {

            if (source.getBlockRendererName() != null && source.getBlockRendererName().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getBlockRendererName(), StyledString.DECORATIONS_STYLER);

            }

        }

        public <S> S getAdapter(Class<S> adapter)
        {

            if (NodeValidateProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(validator);
            }

            return null;
        }

        @Override
        public Image getImage()
        {

            if (source.isReferenceBlock())
                return BLOCK_REF;
            if (source.isControlBlock())
                return BLOCK_NTB;

            return BLOCK;
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

            List<AbstractNode<?>> list = new ArrayList<AbstractNode<?>>();
             list.add(new ReportBlockItemsGroupNode(treeSection, this));

            return list.toArray(new AbstractNode[0]);
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            if (supportBlockDelete())
                return new INodeDeleteProvider()
                {

                    public void delete(boolean cleanup)
                    {

                        ReportBlockGroupNode.this.source.removeBlockProperties(source, cleanup);

                        editor.setDirty(true);
                        treeSection.refresh(ReportBlockGroupNode.this);

                    }
                };
            return super.getDeleteProvider();
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            if (!supportBlockRename())
                return null;

            return new INodeRenameProvider()
            {

                private void renameBlockReferences(String oldName, String newName)
                {

                    // FIXME

                }

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Block [%s]", source.getName()), "Block Name",
                            source.getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Block name can't be empty.";
                                    if (source.getName().equals(newText.trim()))
                                        return "";
                                    if (source.getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (ReportBlockGroupNode.this.source.contains(newText.trim()))
                                        return "Block with this name already exists.";
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        String oldName = source.getName();
                        String blockName = dlg.getValue().trim();
                        source.internalSetName(blockName);
                        renameBlockReferences(oldName, blockName);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh();

                            }
                        });
                    }

                }
            };
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            final List<IMarker> fmarkers = validator.getMarkers();
            List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();

            
            
            AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Renderer", "The renderer you have chosen for your block")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.RENDERER) != 0;
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

                public String[] getOptions()
                {
                    return EJPluginReportBlockRenderers.getBlockRenderers().toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return t;
                }

                @Override
                public void setValue(String value)
                {
                    source.setBlockRendererName(value);

                    editor.setDirty(true);
                    treeSection.refresh(BlockNode.this);
                    if (treeSection.getDescriptorViewer() != null)
                        treeSection.getDescriptorViewer().showDetails(BlockNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getBlockRendererName();
                }
            };

           
           // descriptors.add(rendererDescriptor);
            if (source.isControlBlock())
            {
                AbstractTypeDescriptor actionDescriptor = new AbstractTypeDescriptor(
                        editor,
                        "Action Processor",
                        "If you are creating a very large report, then your Report Level Action Processor may be getting a little too large, if this is the case you can split action to each block. EntireJ will always send events to the block level action processor instead of the report level one if it exists. Any block not having its own action processor will be managed by the the report level action processor")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & FormNodeTag.ACTION_PROCESSOR) != 0;
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

                    @Override
                    public void setValue(String value)
                    {
                        source.setActionProcessorClassName(value);
                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);

                    }

                    @Override
                    public String getValue()
                    {
                        return source.getActionProcessorClassName();
                    }
                };

                actionDescriptor.setBaseClass(EJReportActionProcessor.class.getName());
                actionDescriptor.setDefaultClass(EJDefaultReportActionProcessor.class.getName());
                descriptors.add(actionDescriptor);
            }
            else
            {

                AbstractTypeDescriptor serivceDescriptor = new AbstractTypeDescriptor(editor, "Block Service")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & FormNodeTag.SERVICE) != 0;
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

                    @Override
                    public void setValue(String value)
                    {

                        source.setServiceClassName(value, true);
                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getServiceClassName();
                    }

                    @Override
                    public String lableLinkActivator()
                    {
                        if (getValue() == null || getValue().trim().length() == 0)
                        {
                            NewEJPojoServiceWizard wizard = new NewEJPojoServiceWizard();
                            wizard.setServiceOptional(false);
                            wizard.init(EJUIPlugin.getDefault().getWorkbench(), new StructuredSelection(editor.getFile()));
                            EJUIPlugin.getDefault();
                            WizardDialog dialog = new WizardDialog(EJUIPlugin.getActiveWorkbenchShell(), wizard);
                            dialog.open();
                            String typeName = wizard.getServiceTypeName();
                            return typeName != null ? typeName : "";
                        }
                        return super.lableLinkActivator();
                    }

                };
                serivceDescriptor.setBaseClass(EJBlockService.class.getName());
                AbstractTypeDescriptor actionDescriptor = new AbstractTypeDescriptor(
                        editor,
                        "Action Processor",
                        "If you are creating a very large report, then your Block Level Action Processor may be getting a little too large, if this is the case you can split action to each block. EntireJ will always send events to the block level action processor instead of the report level one if it exists. Any block not having its own action processor will be managed by the the report level action processor")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & FormNodeTag.ACTION_PROCESSOR) != 0;
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

                    @Override
                    public void setValue(String value)
                    {
                        source.setActionProcessorClassName(value);
                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);

                    }

                    @Override
                    public String getValue()
                    {
                        return source.getActionProcessorClassName();
                    }
                };
                actionDescriptor.setBaseClass(EJBlockActionProcessor.class.getName());

                descriptors.add(serivceDescriptor);
                descriptors.add(actionDescriptor);

            }

            return descriptors.toArray(new AbstractDescriptor<?>[0]);

        }

        public Action createCopyNameAction()
        {

            return new Action("Copy Block Name")
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

    public boolean canMove(Neighbor relation, Object source)
    {
        return source instanceof BlockContainerItem;
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<BlockContainerItem> items = source.getBlockContainerItems();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addBlockProperties(index, (BlockContainerItem) dSource);
            }
        }
        else
            source.addBlockProperties((BlockContainerItem) dSource);

    }
}
