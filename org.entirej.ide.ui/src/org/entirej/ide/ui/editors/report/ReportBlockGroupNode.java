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

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.core.actionprocessor.interfaces.EJBlockActionProcessor;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.report.actionprocessor.EJDefaultReportActionProcessor;
import org.entirej.framework.report.actionprocessor.interfaces.EJReportActionProcessor;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.framework.report.service.EJReportBlockService;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.report.operations.ReportBlockAddOperation;
import org.entirej.ide.ui.editors.report.operations.ReportBlockContainerItemAddOperation;
import org.entirej.ide.ui.editors.report.operations.ReportBlockGroupAddOperation;
import org.entirej.ide.ui.editors.report.operations.ReportBlockRemoveOperation;
import org.entirej.ide.ui.editors.report.operations.ReportGroupRemoveOperation;
import org.entirej.ide.ui.editors.report.wizards.DataBlockServiceWizard;
import org.entirej.ide.ui.editors.report.wizards.DataBlockWizardContext;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;
import org.entirej.ide.ui.wizards.service.NewEJPojoServiceWizard;

public class ReportBlockGroupNode extends AbstractNode<EJReportBlockContainer> implements NodeMoveProvider
{
    private final ReportDesignTreeSection       treeSection;
    private final AbstractEJReportEditor        editor;
    private final static Image                  GROUP            = EJUIImages.getImage(EJUIImages.DESC_MENU_GROUP);
    private final static Image                  REPORT_PAGE            = EJUIImages.getImage(EJUIImages.DESC_REPORT_PAGE);
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
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, ReportNodeTag.NONE);
                        if ((tag & ReportNodeTag.GROUP) != 0 && (tag & ReportNodeTag.BLOCK) != 0)
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
        return false;
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {
        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
        nodes.add(new BlockSectionGroupNode(this, source.getHeaderSection()));
        nodes.add(new BlockDetailSectionGroupNode(this));
        nodes.add(new BlockSectionGroupNode(this, source.getFooterSection()));
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        BlockGroup firstPage = source.getFirstPage();
        return new Action[] { treeSection.createNewBlockAction(firstPage,false), treeSection.createNewBlockAction(firstPage,true), null, createNewBlockGroupAction() };
    }

    public Action createNewBlockGroupAction()
    {

        return new Action("New Report Page")
        {

            @Override
            public void runWithEvent(Event event)
            {
                InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), "New  Report Page", "Page Name", null, new IInputValidator()
                {

                    public String isValid(String newText)
                    {
                        if (newText == null || newText.trim().length() == 0)
                            return "Page name can't be empty.";
                        return null;
                    }
                });
                if (dlg.open() == Window.OK)
                {
                    BlockGroup blockGroup = new BlockGroup();
                    blockGroup.setName(dlg.getValue());

                    ReportBlockGroupAddOperation addOperation = new ReportBlockGroupAddOperation(treeSection, source, blockGroup, -1);
                    treeSection.getEditor().execute(addOperation, new NullProgressMonitor());
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
            if (IReportPreviewProvider.class.isAssignableFrom(adapter))
            {
                  return adapter.cast(new ReportPreviewImpl(source));
            }
            
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
            return REPORT_PAGE;
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

                public AbstractOperation deleteOperation(boolean cleanup)
                {

                    return new ReportGroupRemoveOperation(treeSection, ReportBlockGroupNode.this.source, source);
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

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
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
                    return new ReportBlockAddOperation(treeSection, source, (EJPluginReportBlockProperties) dSource, index);
                }
            }
            return new ReportBlockAddOperation(treeSection, source, (EJPluginReportBlockProperties) dSource, -1);
        }

    }

    class BlockDetailSectionGroupNode extends AbstractNode<ReportBlockGroupNode> implements NodeOverview, NodeMoveProvider
    {
        public BlockDetailSectionGroupNode(AbstractNode<?> parent)
        {
            super(parent, ReportBlockGroupNode.this);

        }

        @Override
        public <S> S getAdapter(Class<S> adapter)
        {
            return ReportBlockGroupNode.this.getAdapter(adapter);
        }

        @Override
        public String getName()
        {
            return "Detail Section";
        }

        public void addOverview(StyledString styledString)
        {
            // todo:

        }

        @Override
        public Action[] getActions()
        {
            return source.getActions();
        }

        @Override
        public Image getImage()
        {
            return GROUP;
        }

        @Override
        public boolean isLeaf()
        {
            return source.source.isEmpty();
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<BlockGroup> blockContainerItems = source.source.getPages();

            for (BlockGroup item : blockContainerItems)
            {
               
              

                    nodes.add(new BlockSubGroupNode(this, (BlockGroup) item));
                
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return source instanceof BlockGroup;
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginReportBlockProperties> items = source.source.getAllBlockProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.source.addPage(index, (BlockGroup) dSource);
                }
            }
            else
                source.source.addPage((BlockGroup) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginReportBlockProperties> items = source.source.getAllBlockProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;
                    
                    return new ReportBlockGroupAddOperation(treeSection, source.source, (BlockGroup) dSource, index);
                    
                }
            }
            return new ReportBlockGroupAddOperation(treeSection, source.source, (BlockGroup) dSource,-1);
            
        }
    }

    class BlockSectionGroupNode extends AbstractNode<BlockGroup> implements NodeOverview, NodeMoveProvider
    {
        public BlockSectionGroupNode(AbstractNode<?> parent, BlockGroup source)
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

        public Action[] getActions()
        {

            return new Action[] { createNewSubBlockAction(true) };
        }

        public Action createNewSubBlockAction(final boolean controlBlock)
        {

            return new Action(controlBlock ? "New Report Control Block" : "New Report Service Block")
            {

                @Override
                public void runWithEvent(Event event)
                {
                    DataBlockServiceWizard wizard = new DataBlockServiceWizard(new DataBlockWizardContext()
                    {

                        public boolean isBlockTablelayout()
                        {
                            return true;
                        }

                        public int getDefaultWidth()
                        {
                            final EJPluginReportProperties formProperties = editor.getReportProperties();
                            return formProperties.getReportWidth() - (formProperties.getMarginLeft() + formProperties.getMarginRight());
                        }

                        public int getDefaultHeight()
                        {
                            final EJPluginReportProperties formProperties = editor.getReportProperties();

                            if (formProperties.getBlockContainer().getHeaderSection() == source)
                            {
                                return formProperties.getHeaderSectionHeight();
                            }
                            if (formProperties.getBlockContainer().getFooterSection() == source)
                            {
                                return formProperties.getFooterSectionHeight();
                            }

                            int dtlHeight = formProperties.getReportHeight()
                                    - (formProperties.getMarginTop() + formProperties.getMarginBottom() + formProperties.getHeaderSectionHeight() + formProperties
                                            .getFooterSectionHeight());
                            return dtlHeight > 40 ? 40 : dtlHeight;
                        }

                        public void addBlock(String blockName, String serviceClass, EJReportScreenType type, int x, int y, int width, int height)
                        {
                            final EJPluginReportProperties formProperties = editor.getReportProperties();
                            final EJPluginReportBlockProperties blockProperties = new EJPluginReportBlockProperties(formProperties, blockName, controlBlock);
                            EJPluginReportScreenProperties screenProperties = blockProperties.getLayoutScreenProperties();
                            screenProperties.setScreenType(type);
                            screenProperties.setX(x);
                            screenProperties.setY(y);
                            screenProperties.setWidth(width);
                            screenProperties.setHeight(height);

                            // create items if service is also selected
                            if (supportService() && serviceClass != null && serviceClass.trim().length() > 0)
                            {
                                blockProperties.setServiceClassName(serviceClass, true);
                            }
                            ReportBlockAddOperation addOperation = new ReportBlockAddOperation(treeSection, source, blockProperties, -1);

                            editor.execute(addOperation, new NullProgressMonitor());

                        }

                        public boolean hasBlock(String blockName)
                        {
                            return editor.getReportProperties().getBlockContainer().contains(blockName);
                        }

                        public IJavaProject getProject()
                        {
                            return editor.getJavaProject();
                        }

                        public boolean supportService()
                        {
                            return !controlBlock;
                        }

                    });
                    wizard.open();
                }

            };
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

            List<EJPluginReportBlockProperties> allBlockProperties = source.getAllBlockProperties();
            for (EJPluginReportBlockProperties properties : allBlockProperties)
            {
                nodes.add(new BlockNode(this, properties));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return source instanceof EJPluginReportBlockProperties && ((EJPluginReportBlockProperties) source).isControlBlock()
                    && ((EJPluginReportBlockProperties) source).getLayoutScreenProperties().getScreenType() != EJReportScreenType.TABLE_LAYOUT;
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

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
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

                   
                    return new ReportBlockAddOperation(treeSection, source, (EJPluginReportBlockProperties) dSource, index);
                }
            }
            return new ReportBlockAddOperation(treeSection, source, (EJPluginReportBlockProperties) dSource, -1);
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
                                                                  int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, ReportNodeTag.NONE);
                                                                  if ((tag & ReportNodeTag.BLOCK) != 0 && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(ReportNodeTag.BLOCK_ID, null)))
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

            BlockGroup groupByBlock = ReportBlockGroupNode.this.source.getBlockGroupByBlock(source);
            return new Action[] { treeSection.createNewBlockAction(groupByBlock,false), treeSection.createNewBlockAction(groupByBlock,true), null, createCopyNameAction() };

        }

        public void addOverview(StyledString styledString)
        {

            if (source.getLayoutScreenProperties().getScreenType() != EJReportScreenType.NONE)
            {
                styledString.append(" : ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getLayoutScreenProperties().getScreenType().toString(), StyledString.QUALIFIER_STYLER);
            }
            if (source.getLayoutScreenProperties().getScreenType() != EJReportScreenType.NONE)
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append("(x,y) = (" + source.getLayoutScreenProperties().getX() + " ," + source.getLayoutScreenProperties().getY() + ")",
                        StyledString.DECORATIONS_STYLER);

                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }

        }

        public <S> S getAdapter(Class<S> adapter)
        {

            if (NodeValidateProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(validator);
            }

            return parent==null ?null:parent.getAdapter(adapter);
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
            list.add(new ReportScreenNode(treeSection, this, ReportBlockGroupNode.this, source.getLayoutScreenProperties()));

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

                    public AbstractOperation deleteOperation(boolean cleanup)
                    {
                        BlockGroup groupByBlock = ReportBlockGroupNode.this.source.getBlockGroupByBlock(source);
                        assert groupByBlock !=null;
                        return  new ReportBlockRemoveOperation(treeSection, groupByBlock, source);
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

            final AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Width")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & ReportNodeTag.WIDTH) != 0;
                                   }
                               };

                @Override
                public String getErrors()
                {

                    return validator.getErrorMarkerMsg(fmarkers, vfilter);
                }

                @Override
                public String getTooltip()
                {

                    return "The width <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.getLayoutScreenProperties().setWidth(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.getLayoutScreenProperties().setWidth(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(BlockNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getLayoutScreenProperties().getWidth());
                }

                Text text;

                @Override
                public void addEditorAssist(Control control)
                {

                    text = (Text) control;
                    text.addVerifyListener(new EJPluginEntireJNumberVerifier());

                    super.addEditorAssist(control);
                }
            };

            final AbstractTextDescriptor heightDescriptor = new AbstractTextDescriptor("Height")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & ReportNodeTag.HEIGHT) != 0;
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
                public String getTooltip()
                {

                    return "The height <b>(in pixels)</b> of the report within it's Page.";
                }
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.getLayoutScreenProperties().setHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.getLayoutScreenProperties().setHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(BlockNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getLayoutScreenProperties().getHeight());
                }

                Text text;

                @Override
                public void addEditorAssist(Control control)
                {

                    text = (Text) control;
                    text.addVerifyListener(new EJPluginEntireJNumberVerifier());

                    super.addEditorAssist(control);
                }
            };

            final AbstractTextDescriptor xDescriptor = new AbstractTextDescriptor("X")
            {

                @Override
                public String getTooltip()
                {

                    return "The X <b>(in pixels)</b> of the report within it's Page.";
                }
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.getLayoutScreenProperties().setX(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.getLayoutScreenProperties().setX(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(BlockNode.this);

                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getLayoutScreenProperties().getX());
                }

                Text text;

                @Override
                public void addEditorAssist(Control control)
                {

                    text = (Text) control;
                    text.addVerifyListener(new EJPluginEntireJNumberVerifier());

                    super.addEditorAssist(control);
                }
            };

            final AbstractTextDescriptor yDescriptor = new AbstractTextDescriptor("Y")
            {

                @Override
                public String getTooltip()
                {

                    return "The Y <b>(in pixels)</b> of the report within it's Page.";
                }
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.getLayoutScreenProperties().setY(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.getLayoutScreenProperties().setY(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(BlockNode.this);

                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getLayoutScreenProperties().getY());
                }

                Text text;

                @Override
                public void addEditorAssist(Control control)
                {

                    text = (Text) control;
                    text.addVerifyListener(new EJPluginEntireJNumberVerifier());

                    super.addEditorAssist(control);
                }
            };

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

                                           return (tag & ReportNodeTag.ACTION_PROCESSOR) != 0;
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
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);
                        
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

                                           return (tag & ReportNodeTag.SERVICE) != 0;
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
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);
                        
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
                serivceDescriptor.setBaseClass(EJReportBlockService.class.getName());
                AbstractTypeDescriptor actionDescriptor = new AbstractTypeDescriptor(
                        editor,
                        "Action Processor",
                        "If you are creating a very large report, then your Block Level Action Processor may be getting a little too large, if this is the case you can split action to each block. EntireJ will always send events to the block level action processor instead of the report level one if it exists. Any block not having its own action processor will be managed by the the report level action processor")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & ReportNodeTag.ACTION_PROCESSOR) != 0;
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
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);
                        
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
                descriptors.add(xDescriptor);
                descriptors.add(yDescriptor);
                descriptors.add(widthDescriptor);
                descriptors.add(heightDescriptor);

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

    class ScreenBlockSubGroupNode extends AbstractNode<BlockGroup> implements Neighbor, NodeOverview, NodeMoveProvider
    {
        public ScreenBlockSubGroupNode(AbstractNode<?> parent, BlockGroup source)
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

        public Action[] getActions()
        {

            return new Action[] { createNewSubBlockAction(false), createNewSubBlockAction(true) };
        }

        public Action createNewSubBlockAction(final boolean controlBlock)
        {

            return new Action(controlBlock ? "New Report Control Block" : "New Report Service Block")
            {

                @Override
                public void runWithEvent(Event event)
                {
                    DataBlockServiceWizard wizard = new DataBlockServiceWizard(new DataBlockWizardContext()
                    {

                        public boolean isBlockTablelayout()
                        {
                            return false;
                        }

                        public int getDefaultWidth()
                        {
                            final EJPluginReportProperties formProperties = editor.getReportProperties();
                            return formProperties.getReportWidth() - (formProperties.getMarginLeft() + formProperties.getMarginRight());
                        }

                        public int getDefaultHeight()
                        {
                            final EJPluginReportProperties formProperties = editor.getReportProperties();

                            if (formProperties.getBlockContainer().getHeaderSection() == source)
                            {
                                return formProperties.getHeaderSectionHeight();
                            }
                            if (formProperties.getBlockContainer().getFooterSection() == source)
                            {
                                return formProperties.getFooterSectionHeight();
                            }

                            int dtlHeight = formProperties.getReportHeight()
                                    - (formProperties.getMarginTop() + formProperties.getMarginBottom() + formProperties.getHeaderSectionHeight() + formProperties
                                            .getFooterSectionHeight());
                            return dtlHeight > 40 ? 40 : dtlHeight;
                        }

                        public void addBlock(String blockName, String serviceClass, EJReportScreenType type, int x, int y, int width, int height)
                        {
                            final EJPluginReportProperties formProperties = editor.getReportProperties();
                            final EJPluginReportBlockProperties blockProperties = new EJPluginReportBlockProperties(formProperties, blockName, controlBlock);
                            EJPluginReportScreenProperties screenProperties = blockProperties.getLayoutScreenProperties();
                            screenProperties.setScreenType(type);
                            screenProperties.setX(x);
                            screenProperties.setY(y);
                            screenProperties.setWidth(width);
                            screenProperties.setHeight(height);

                            // create items if service is also selected
                            if (supportService() && serviceClass != null && serviceClass.trim().length() > 0)
                            {
                                blockProperties.setServiceClassName(serviceClass, true);
                            }
                            ReportBlockAddOperation addOperation = new ReportBlockAddOperation(treeSection, source, blockProperties, -1);

                            editor.execute(addOperation, new NullProgressMonitor());

                        }

                        public boolean hasBlock(String blockName)
                        {
                            return editor.getReportProperties().getBlockContainer().contains(blockName);
                        }

                        public IJavaProject getProject()
                        {
                            return editor.getJavaProject();
                        }

                        public boolean supportService()
                        {
                            return !controlBlock;
                        }

                    });
                    wizard.open();
                }

            };
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

            return null;
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            return null;
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

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
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

                    return new ReportBlockAddOperation(treeSection, source, (EJPluginReportBlockProperties) dSource, index);
                }
            }
            return new ReportBlockAddOperation(treeSection, source, (EJPluginReportBlockProperties) dSource, -1);
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
            List<BlockGroup> items = source.getPages();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addPage(index, (BlockGroup) dSource);
            }
        }
        else
            source.addPage((BlockGroup) dSource);

    }

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<BlockGroup> items = source.getPages();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                return new ReportBlockContainerItemAddOperation(treeSection, source, (BlockGroup) dSource, index);
            }

        }

        return new ReportBlockContainerItemAddOperation(treeSection, source, (BlockGroup) dSource, -1);
    }

    public AbstractNode<?> createScreenGroupNode(ReportScreenNode reportScreenNode, BlockGroup subBlocks)
    {

        return new ScreenBlockSubGroupNode(reportScreenNode, subBlocks);
    }
}
