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
package org.entirej.ide.ui.editors.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.entirej.framework.core.actionprocessor.EJDefaultBlockActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJBlockActionProcessor;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.properties.interfaces.EJDrawerPageProperties;
import org.entirej.framework.core.properties.interfaces.EJStackedPageProperties;
import org.entirej.framework.core.properties.interfaces.EJTabPageProperties;
import org.entirej.framework.core.renderers.definitions.interfaces.EJBlockRendererDefinition;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.EJDevBlockRendererDefinitionControl;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EJPluginStackedPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockGroup;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.form.DisplayItemGroupNode.MainDisplayItemGroup;
import org.entirej.ide.ui.editors.form.operations.BlockAddOperation;
import org.entirej.ide.ui.editors.form.operations.BlockGroupAddOperation;
import org.entirej.ide.ui.editors.form.operations.BlockGroupRemoveOperation;
import org.entirej.ide.ui.editors.form.operations.BlockRemoveOperation;
import org.entirej.ide.ui.editors.form.operations.CanvasAddOperation;
import org.entirej.ide.ui.editors.form.wizards.ReplicateBlockWizard;
import org.entirej.ide.ui.editors.form.wizards.ReplicateBlockWizardContext;
import org.entirej.ide.ui.editors.operations.ReversibleOperation;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart.IExtensionValues;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.wizards.service.NewEJPojoServiceWizard;

public class BlockGroupNode extends AbstractNode<EJPluginBlockContainer> implements NodeMoveProvider
{
    private final FormDesignTreeSection         treeSection;
    private final AbstractEJFormEditor          editor;
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

                                                                            
                                                                                 treeSection.selectNodes(true, arg0);
                                                                             
                                                                         }

                                                                     }
                                                                 };

    public BlockGroupNode(AbstractNode<?> parent,FormDesignTreeSection treeSection)
    {
        super(parent, treeSection.getEditor().getFormProperties().getBlockContainer());
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
                    treeSection.refresh(BlockGroupNode.this);
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
        return "Form block definitions";
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
            if (item instanceof EJPluginBlockProperties)
            {

                nodes.add(new BlockNode(this, (EJPluginBlockProperties) item));
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

        return new Action[] { treeSection.createNewBlockAction(false), treeSection.createNewBlockAction(true), treeSection.createNewMirrorBlockAction(null),
                treeSection.createNewRefBlockAction(true), null, createNewBlockGroupAction() };
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
                    
                    BlockGroupAddOperation addOperation = new BlockGroupAddOperation(treeSection, source, blockGroup, -1); 
                    editor.execute(addOperation);
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
            return BlockGroupNode.this.getAdapter(adapter);
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
            return BlockGroupNode.this.getImage();
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    BlockGroupNode.this.source.removeBlockContainerItem(source);

                    editor.setDirty(true);
                    treeSection.refresh(BlockGroupNode.this);

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    return new BlockGroupRemoveOperation(treeSection, BlockGroupNode.this.source, source);
                }
            };
        }

        
        @Override
        public Action[] getActions()
        {

            return new Action[] { treeSection.createNewBlockAction(false,source), treeSection.createNewBlockAction(true,source), treeSection.createNewMirrorBlockAction(null,source),
                    treeSection.createNewRefBlockAction(true,source),  };
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

            List<EJPluginBlockProperties> allBlockProperties = source.getAllBlockProperties();
            for (EJPluginBlockProperties properties : allBlockProperties)
            {
                nodes.add(new BlockNode(this, properties));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return source instanceof EJPluginBlockProperties;
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginBlockProperties> items = source.getAllBlockProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.addBlockProperties(index, (EJPluginBlockProperties) dSource);
                }
            }
            else
                source.addBlockProperties((EJPluginBlockProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginBlockProperties> items = source.getAllBlockProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new BlockAddOperation(treeSection, source, (EJPluginBlockProperties) dSource, index);
                }
            }

            return new BlockAddOperation(treeSection, source, (EJPluginBlockProperties) dSource, -1);

        }

    }

    class BlockNode extends AbstractNode<EJPluginBlockProperties> implements Neighbor, Movable, NodeOverview
    {

        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(BlockGroupNode.this);
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

        public BlockNode(AbstractNode<?> parent, EJPluginBlockProperties source)
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
            if (source.isImportFromObjectGroup())
            {
                return new Action[0];
            }

            if (treeSection.getEditor().getFormProperties() instanceof EJPluginObjectGroupProperties)
            {

                return new Action[] { treeSection.createNewMirrorBlockAction(source.getName()), treeSection.createNewRefBlockAction(true), null,
                        createCopyNameAction() };

            }

            if (source.isReferenceBlock() || source.isMirrorChild())
            {
                return new Action[] { createReplicateAction(), treeSection.createNewMirrorBlockAction(source.getName()), null,
                        treeSection.createNewBlockAction(false), treeSection.createNewBlockAction(true), treeSection.createNewRefBlockAction(true), null,
                        createCopyNameAction() };
            }

            return new Action[] { createReplicateAction(), treeSection.createNewMirrorBlockAction(source.getName()),
                    treeSection.createGenerateRefBlockAction(source), null, treeSection.createNewBlockAction(false), treeSection.createNewBlockAction(true),
                    treeSection.createNewRefBlockAction(true), null, createCopyNameAction() };

        }

        public void addOverview(StyledString styledString)
        {

            if (source.isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }

            if (source.isReferenceBlock() && source.getReferencedBlockName() != null && source.getReferencedBlockName().length() != 0)
            {
                styledString.append(" [ ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getReferencedBlockName(), StyledString.QUALIFIER_STYLER);
                if (!source.getFormProperties().getEntireJProperties().containsReusableBlockProperties(source.getReferencedBlockName()))
                {
                    styledString.append(" < missing! >", StyledString.QUALIFIER_STYLER);
                }
                styledString.append(" ] ", StyledString.QUALIFIER_STYLER);
            }

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

            if (IFormPreviewProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(new IFormPreviewProvider()

                {

                    public String getDescription()
                    {
                        return "preview the defined layout in block main screen.";
                    }

                    public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                    {

                        Composite pContent = new Composite(previewComposite, SWT.NONE);

                        pContent.setLayout(new FillLayout());

                        EJPluginMainScreenProperties mainScreenProperties = source.getMainScreenProperties();
                        int width = mainScreenProperties.getWidth();
                        int height = mainScreenProperties.getHeight();
                        previewComposite.setContent(pContent);
                        previewComposite.setExpandHorizontal(true);
                        previewComposite.setExpandVertical(true);

                        EJDevBlockRendererDefinition blockRendererDefinition = source.getBlockRendererDefinition();
                        if (blockRendererDefinition != null)
                        {
                            EJDevBlockRendererDefinitionControl addBlockControlToCanvas = blockRendererDefinition.addBlockControlToCanvas(mainScreenProperties,
                                    source, pContent, editor.getToolkit());
                            addBlockControlToCanvas.addItemWidgetChosenListener(chosenListener);

                        }
                        if (width > 0 && height > 0)
                            previewComposite.setMinSize(width, height);
                        else
                            previewComposite.setMinSize(pContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

                    }

                    public void dispose()
                    {

                    }
                });
            }
            return null;
        }

        @Override
        public Image getImage()
        {
            if (source.isMirrorChild())
                return source.isImportFromObjectGroup() ? BLOCK_MIRROR_REF : BLOCK_MIRROR;

            if (source.isReferenceBlock())
                return BLOCK_REF;
            if (source.isControlBlock())
                return source.isImportFromObjectGroup() ? BLOCK_NTB_REF : BLOCK_NTB;

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
            if (source.isReferenceBlock())
            {
                return new AbstractNode<?>[] { new BlockItemsGroupNode(treeSection, this) };
            }

            List<AbstractNode<?>> list = new ArrayList<AbstractNode<?>>();
            list.add(new BlockItemsGroupNode(treeSection, this));

            if (source.getMainScreenProperties() != null)
            {
                MainDisplayItemGroup itemGroup = new DisplayItemGroupNode.MainDisplayItemGroup("Main Screen", source.getMainScreenProperties(),
                        source.getMainScreenItemGroupDisplayContainer())
                {
                    private AbstractMarkerNodeValidator svalidator = new AbstractMarkerNodeValidator()
                                                                   {

                                                                       public void refreshNode()
                                                                       {
                                                                           treeSection.refresh(BlockNode.this);
                                                                       }

                                                                       @Override
                                                                       public List<IMarker> getMarkers()
                                                                       {
                                                                           List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                           List<IMarker> markers = validator.getMarkers();
                                                                           for (IMarker marker : markers)
                                                                           {
                                                                               int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                               if ((tag & FormNodeTag.MAIN) != 0)
                                                                               {

                                                                                   fmarkers.add(marker);
                                                                               }
                                                                           }

                                                                           return fmarkers;
                                                                       }
                                                                   };

                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (NodeValidateProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(svalidator);
                        }
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                            return BlockNode.this.getAdapter(adapter);

                        return super.getAdapter(adapter);

                    }
                };
                list.add(new DisplayItemGroupNode(treeSection, this, itemGroup)
                {

                    @Override
                    public String getNodeDescriptorDetails()
                    {
                        return "The Main Screen is the screen that the user sees when the form is opened and displays data according to the Block Rendere you have chosen. Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on Screens.  All mandatory properties are denoted by \"*\"";
                    }

                });
            }

            if (!source.isControlBlock() && source.getQueryScreenRendererProperties() != null && source.getQueryScreenRendererDefinition() != null)
            {
                DisplayItemGroupNode.ExtensionDisplayItemGroup itemGroup = new DisplayItemGroupNode.ExtensionDisplayItemGroup("Query Screen",
                        source.getQueryScreenRendererProperties(), source.getQueryScreenItemGroupDisplayContainer())
                {

                    @Override
                    public EJPropertyDefinitionGroup getDefinitionGroup()
                    {
                        return source.getQueryScreenRendererDefinition().getQueryScreenPropertyDefinitionGroup();
                    }

                    @Override
                    public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                            EJPropertyDefinition propertyDefinition)
                    {
                        source.getQueryScreenRendererDefinition().loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                    }

                    private AbstractMarkerNodeValidator svalidator = new AbstractMarkerNodeValidator()
                                                                   {

                                                                       public void refreshNode()
                                                                       {
                                                                           treeSection.refresh(BlockNode.this);
                                                                       }

                                                                       @Override
                                                                       public List<IMarker> getMarkers()
                                                                       {
                                                                           List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                           List<IMarker> markers = validator.getMarkers();
                                                                           for (IMarker marker : markers)
                                                                           {
                                                                               int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                               if ((tag & FormNodeTag.QUERY) != 0)
                                                                               {

                                                                                   fmarkers.add(marker);
                                                                               }
                                                                           }

                                                                           return fmarkers;
                                                                       }
                                                                   };

                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (NodeValidateProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(svalidator);
                        }
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(new IFormPreviewProvider()

                            {

                                public String getDescription()
                                {
                                    return "preview the defined layout in block query screen.";
                                }

                                public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                                {
                                    source.getQueryScreenRendererDefinition()
                                            .addQueryScreenControl(BlockNode.this.source, previewComposite, editor.getToolkit())
                                            .addItemWidgetChosenListener(chosenListener);
                                    Control[] children = previewComposite.getChildren();
                                    if (children.length > 0)
                                    {
                                        previewComposite.setContent(previewComposite.getChildren()[0]);
                                        previewComposite.setExpandHorizontal(true);
                                        previewComposite.setExpandVertical(true);
                                    }

                                }

                                public void dispose()
                                {

                                }
                            });
                        }
                        return null;
                    }
                };
                list.add(new DisplayItemGroupNode(treeSection, this, itemGroup)
                {
                    @Override
                    public String getNodeDescriptorDetails()
                    {
                        return "Query screens are used by users to enter filter criteria for query block records. Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on EntireJ Screens. All mandatory items are denoted by \"*\"";
                    }

                });
            }

            if (source.getInsertScreenRendererProperties() != null && source.getInsertScreenRendererDefinition() != null)
            {
                DisplayItemGroupNode.ExtensionDisplayItemGroup itemGroup = new DisplayItemGroupNode.ExtensionDisplayItemGroup("Insert Screen",
                        source.getInsertScreenRendererProperties(), source.getInsertScreenItemGroupDisplayContainer())
                {

                    @Override
                    public EJPropertyDefinitionGroup getDefinitionGroup()
                    {
                        return source.getInsertScreenRendererDefinition().getInsertScreenPropertyDefinitionGroup();
                    }

                    @Override
                    public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                            EJPropertyDefinition propertyDefinition)
                    {
                        source.getInsertScreenRendererDefinition().loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                    }

                    private AbstractMarkerNodeValidator svalidator = new AbstractMarkerNodeValidator()
                                                                   {

                                                                       public void refreshNode()
                                                                       {
                                                                           treeSection.refresh(BlockNode.this);
                                                                       }

                                                                       @Override
                                                                       public List<IMarker> getMarkers()
                                                                       {
                                                                           List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                           List<IMarker> markers = validator.getMarkers();
                                                                           for (IMarker marker : markers)
                                                                           {
                                                                               int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                               if ((tag & FormNodeTag.INSET) != 0)
                                                                               {

                                                                                   fmarkers.add(marker);
                                                                               }
                                                                           }

                                                                           return fmarkers;
                                                                       }
                                                                   };

                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (NodeValidateProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(svalidator);
                        }
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(new IFormPreviewProvider()

                            {

                                public String getDescription()
                                {
                                    return "preview the defined layout in block insert screen.";
                                }

                                public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                                {
                                    source.getInsertScreenRendererDefinition()
                                            .addInsertScreenControl(BlockNode.this.source, previewComposite, editor.getToolkit())
                                            .addItemWidgetChosenListener(chosenListener);
                                    Control[] children = previewComposite.getChildren();
                                    if (children.length > 0)
                                    {
                                        previewComposite.setContent(previewComposite.getChildren()[0]);
                                        previewComposite.setExpandHorizontal(true);
                                        previewComposite.setExpandVertical(true);
                                    }

                                }

                                public void dispose()
                                {

                                }
                            });
                        }
                        return null;
                    }
                };
                list.add(new DisplayItemGroupNode(treeSection, this, itemGroup)
                {

                    @Override
                    public String getNodeDescriptorDetails()
                    {
                        return "Insert screens are used by users to create new records for the block. Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on EntireJ Screens. All mandatory items are denoted by \"*\"";
                    }

                });
            }

            if (source.getUpdateScreenRendererProperties() != null && source.getUpdateScreenRendererDefinition() != null)
            {
                DisplayItemGroupNode.ExtensionDisplayItemGroup itemGroup = new DisplayItemGroupNode.ExtensionDisplayItemGroup("Update Screen",
                        source.getUpdateScreenRendererProperties(), source.getUpdateScreenItemGroupDisplayContainer())
                {

                    @Override
                    public EJPropertyDefinitionGroup getDefinitionGroup()
                    {
                        return source.getUpdateScreenRendererDefinition().getUpdateScreenPropertyDefinitionGroup();
                    }

                    @Override
                    public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                            EJPropertyDefinition propertyDefinition)
                    {
                        source.getUpdateScreenRendererDefinition().loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                    }

                    private AbstractMarkerNodeValidator svalidator = new AbstractMarkerNodeValidator()
                                                                   {

                                                                       public void refreshNode()
                                                                       {
                                                                           treeSection.refresh(BlockNode.this);
                                                                       }

                                                                       @Override
                                                                       public List<IMarker> getMarkers()
                                                                       {
                                                                           List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                           List<IMarker> markers = validator.getMarkers();
                                                                           for (IMarker marker : markers)
                                                                           {
                                                                               int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                               if ((tag & FormNodeTag.UPDATE) != 0)
                                                                               {

                                                                                   fmarkers.add(marker);
                                                                               }
                                                                           }

                                                                           return fmarkers;
                                                                       }
                                                                   };

                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (NodeValidateProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(svalidator);
                        }
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(new IFormPreviewProvider()

                            {

                                public String getDescription()
                                {
                                    return "preview the defined layout in block update screen.";
                                }

                                public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                                {
                                    source.getUpdateScreenRendererDefinition()
                                            .addUpdateScreenControl(BlockNode.this.source, previewComposite, editor.getToolkit())
                                            .addItemWidgetChosenListener(chosenListener);
                                    Control[] children = previewComposite.getChildren();
                                    if (children.length > 0)
                                    {
                                        previewComposite.setContent(previewComposite.getChildren()[0]);
                                        previewComposite.setExpandHorizontal(true);
                                        previewComposite.setExpandVertical(true);
                                    }

                                }

                                public void dispose()
                                {

                                }
                            });
                        }
                        return null;
                    }
                };
                list.add(new DisplayItemGroupNode(treeSection, this, itemGroup)
                {

                    @Override
                    public String getNodeDescriptorDetails()
                    {
                        return "Update screens are used by users to modify data of the block. Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on EntireJ Screens. All mandatory items are denoted by \"*\"";
                    }
                });
            }

            if (!source.isMirrorChild())
            {
                list.add(new LovMappingGroupNode(treeSection, this));
            }
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

                        BlockGroupNode.this.source.removeBlockProperties(source, cleanup);

                        editor.setDirty(true);
                        treeSection.refresh(BlockGroupNode.this);

                    }

                    public AbstractOperation deleteOperation(boolean cleanup)
                    {
                        if(cleanup)
                        {
                            return BlockRemoveOperation.createCleanupOperation(treeSection, BlockGroupNode.this.source, source);
                        }
                        return new BlockRemoveOperation(treeSection, BlockGroupNode.this.source, source);
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

                    for (EJPluginBlockProperties mirrorCh : source.getMirrorChildren())
                    {
                        mirrorCh.setMirrorBlockName(newName);
                    }

                    for (EJPluginRelationProperties relation : source.getFormProperties().getRelationContainer().getAllRelationProperties())
                    {

                        if (relation.getMasterBlockName().equals(oldName))
                        {
                            relation.setMasterBlockName(newName);
                        }
                        if (relation.getDetailBlockName().equals(oldName))
                        {
                            relation.setDetailBlockName(newName);
                        }
                    }

                    for (EJCanvasProperties canvas : source.getFormProperties().getCanvasContainer().getAllCanvasProperties())
                    {
                        if (canvas.getType() == EJCanvasType.TAB)
                        {

                            for (EJTabPageProperties tabPage : canvas.getTabPageContainer().getAllTabPageProperties())
                            {
                                if (tabPage.getFirstNavigationalBlock() != null && tabPage.getFirstNavigationalBlock().equals(oldName))
                                {
                                    ((EJPluginTabPageProperties) tabPage).setFirstNavigationalBlock(newName);
                                }
                            }
                        }
                        if (canvas.getType() == EJCanvasType.DRAWER)
                        {
                            
                            for (EJDrawerPageProperties tabPage : canvas.getDrawerPageContainer().getAllDrawerPageProperties())
                            {
                                if (tabPage.getFirstNavigationalBlock() != null && tabPage.getFirstNavigationalBlock().equals(oldName))
                                {
                                    ((EJPluginTabPageProperties) tabPage).setFirstNavigationalBlock(newName);
                                }
                            }
                        }
                        else if (canvas.getType() == EJCanvasType.STACKED)
                        {

                            for (EJStackedPageProperties stPage : canvas.getStackedPageContainer().getAllStackedPageProperties())
                            {
                                if (stPage.getFirstNavigationalBlock() != null && stPage.getFirstNavigationalBlock().equals(oldName))
                                {
                                    ((EJPluginStackedPageProperties) stPage).setFirstNavigationalBlock(newName);
                                }
                            }
                        }

                    }
                    List<EJPluginBlockProperties> allBlockProperties = new ArrayList<EJPluginBlockProperties>(source.getFormProperties().getBlockContainer()
                            .getAllBlockProperties());
                    List<EJPluginLovDefinitionProperties> allLovDefinitionProperties = source.getFormProperties().getLovDefinitionContainer()
                            .getAllLovDefinitionProperties();
                    for (EJPluginLovDefinitionProperties lovDefinitionProperties : allLovDefinitionProperties)
                    {
                        allBlockProperties.add(lovDefinitionProperties.getBlockProperties());
                    }
                    if (source.getFormProperties().getFirstNavigableBlock().equals(oldName))
                    {
                        
                        source.getFormProperties().setFirstNavigableBlock(newName);
                            
                    }
                    for (EJPluginBlockProperties properties : allBlockProperties)
                    {
                        List<EJPluginBlockItemProperties> itemProperties = properties.getItemContainer().getAllItemProperties();
                        for (EJPluginBlockItemProperties blockItemProperties : itemProperties)
                        {

                            String insertValue = blockItemProperties.getDefaultInsertValue();
                            if (insertValue != null && insertValue.trim().length() > 0 && insertValue.indexOf(":") > 0)
                            {
                                if ("BLOCK_ITEM".equals(insertValue.substring(0, insertValue.indexOf(":"))))
                                {
                                    String value = insertValue.substring(insertValue.indexOf(":") + 1);
                                    String[] split = value.split("\\.");
                                    if (split.length == 2)
                                    {
                                        if (oldName.equals(split[0]))
                                        {

                                            blockItemProperties.setDefaultInsertValue(String.format("BLOCK_ITEM:%s.%s", newName, split[1]));

                                        }
                                    }
                                }

                            }

                            String queryValue = blockItemProperties.getDefaultQueryValue();
                            if (queryValue != null && queryValue.trim().length() > 0 && queryValue.indexOf(":") > 0)
                            {
                                if ("BLOCK_ITEM".equals(queryValue.substring(0, queryValue.indexOf(":"))))
                                {
                                    String value = queryValue.substring(queryValue.indexOf(":") + 1);
                                    String[] split = value.split("\\.");
                                    if (split.length == 2)
                                    {
                                        if (oldName.equals(split[0]))
                                        {

                                            blockItemProperties.setDefaultQueryValue(String.format("BLOCK_ITEM:%s.%s", newName, split[1]));

                                        }
                                    }
                                }

                            }
                        }
                    }

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
                                    if (BlockGroupNode.this.source.contains(newText.trim()))
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

        protected void updateMirrorBlocks()
        {
            for (EJPluginBlockProperties childProperties : source.getMirrorChildren())
            {
                
                    treeSection.refresh(childProperties);
                
            }

        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            if (source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { new AbstractTextDescriptor("Referenced ObjectGroup")
                {

                    public boolean hasLableLink()
                    {
                        return true;
                    }

                    @Override
                    public String lableLinkActivator()
                    {

                        EJPluginObjectGroupProperties file = editor.getFormProperties().getObjectGroupContainer()
                                .getObjectGroupProperties(source.getReferencedObjectGroupName());
                        if (file != null)
                        {
                            treeSection.selectNodes(true, (file));
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
                        return source.getReferencedObjectGroupName();
                    }

                    Text text;

                    @Override
                    public void addEditorAssist(Control control)
                    {

                        text = (Text) control;
                        text.setEditable(false);
                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }
                } };
            }

            final List<IMarker> fmarkers = validator.getMarkers();
            List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();
            final List<AbstractDescriptor<?>> dataDescriptors = new ArrayList<AbstractDescriptor<?>>();

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
                    List<String> options = new ArrayList<String>();
                    EJPluginAssignedRendererContainer rendererContainer = source.getEntireJProperties().getBlockRendererContainer();
                    Collection<EJPluginRenderer> allRenderers = rendererContainer.getAllRenderers();
                    for (EJPluginRenderer renderer : allRenderers)
                    {
                        options.add(renderer.getAssignedName());
                    }

                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return t;
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    source.setBlockRendererName(value, true);

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

            descriptors.add(rendererDescriptor);

            AbstractTextDropDownDescriptor canvasDescriptor = null;

            if (supportCanvas())
            {
                canvasDescriptor = new AbstractTextDropDownDescriptor(
                        "Canvas",
                        "Any block displayed on your form requires a block canvas. Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-Canvases\">here</a> for more information on EntireJ Canvases")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & FormNodeTag.CANVAS) != 0;
                                       }
                                   };

                    @Override
                    public String getErrors()
                    {

                        return validator.getErrorMarkerMsg(fmarkers, vfilter);
                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

                    @Override
                    public String getWarnings()
                    {
                        return validator.getWarningMarkerMsg(fmarkers, vfilter);
                    }

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();
                        options.add("");
                        Collection<EJCanvasProperties> canvasCollection = EJPluginCanvasRetriever
                                .retriveAllNonAssignedBlockCanvases(editor.getFormProperties());
                        if (source.getCanvasName() != null)
                            options.add(source.getCanvasName());
                        for (EJCanvasProperties canvasProperties : canvasCollection)
                        {
                            options.add(canvasProperties.getName());
                        }

                        return options.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return t;
                    }

                    @Override
                    public boolean hasLableLink()
                    {
                        return true;
                    }

                    @Override
                    public String lableLinkActivator()
                    {

                        EJPluginCanvasProperties canvasProperties = (EJPluginCanvasProperties) EJPluginCanvasRetriever.getCanvasProperties(
                                editor.getFormProperties(), getValue());
                        if (canvasProperties != null)
                        {
                            // when finding canvas it need to expand nested
                            // groups
                            // as well
                            List<Object> parnets = new ArrayList<Object>();
                            EJPluginCanvasContainer container = canvasProperties.getParentCanvasContainer();

                            while ((container.getParnetCanvas() != null))
                            {

                                parnets.add(container);
                                parnets.add(container.getParnetCanvas());
                                container = container.getParnetCanvas().getParentCanvasContainer();
                            }
                            parnets.add(editor.getFormProperties().getCanvasContainer());
                            Collections.reverse(parnets);
                            for (Object o : parnets)
                            {
                                Object findNode = (o);
                                if (findNode != null)
                                {
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                }
                            }

                            treeSection.selectNodes(false, (canvasProperties));
                        }
                        return super.lableLinkActivator();
                    }

                    @Override
                    public void setValue(String value)
                    {
                        String oldCanvas = getValue();
                        source.setCanvasName(value);

                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);

                        // refresh old canvas
                        EJCanvasProperties canvasProperties = EJPluginCanvasRetriever.getCanvasProperties(editor.getFormProperties(), oldCanvas);
                        if (canvasProperties != null)
                        {
                            Object findNode = (canvasProperties);
                            if (findNode != null)
                                treeSection.refresh(findNode);
                        }

                        // refresh new canvas
                        canvasProperties = EJPluginCanvasRetriever.getCanvasProperties(editor.getFormProperties(), value);
                        if (canvasProperties != null)
                        {
                            Object findNode = (canvasProperties);
                            if (findNode != null)
                                treeSection.refresh(findNode);
                        }
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getCanvasName();
                    }
                };
            }
            if (canvasDescriptor != null)
            {
                descriptors.add(canvasDescriptor);
            }
            if (source.isMirrorChild())
            {
                final AbstractTextDescriptor referencedDescriptor = new AbstractTextDescriptor("Mirrored Block")
                {
                    public boolean hasLableLink()
                    {
                        return true;
                    }

                    @Override
                    public String lableLinkActivator()
                    {

                        EJPluginBlockProperties item = BlockGroupNode.this.source.getBlockProperties(getValue());
                        if (item != null)
                        {

                            if (item != null)
                            {
                                Object findNode = (item);
                                if (findNode != null)
                                {
                                    treeSection.selectNodes(true, findNode);
                                }
                            }
                        }

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
                        return source.getMirrorBlockName();
                    }

                    Text text;

                    @Override
                    public void addEditorAssist(Control control)
                    {

                        text = (Text) control;
                        text.setEditable(false);
                    }
                };
                descriptors.add(0, referencedDescriptor);
            }
            else if (source.isReferenceBlock())
            {
                final AbstractTextDescriptor referencedDescriptor = new AbstractTextDescriptor("Referenced Block")
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

                        try
                        {
                            IFile file = editor.getFormProperties().getEntireJProperties().getReusableBlockFile(getValue());
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
                        return source.getReferencedBlockName();
                    }

                    Text text;

                    @Override
                    public void addEditorAssist(Control control)
                    {

                        text = (Text) control;
                        text.setEditable(false);
                    }
                };
                if (canvasDescriptor != null)
                {
                    return new AbstractDescriptor<?>[] { referencedDescriptor, canvasDescriptor };
                }
                else
                {
                    return new AbstractDescriptor<?>[] { referencedDescriptor };
                }
            }
            else if (source.isControlBlock())
            {
                AbstractTypeDescriptor actionDescriptor = new AbstractTypeDescriptor(
                        editor,
                        "Action Processor",
                        "If you are creating a very large form, then your Form Level Action Processor may be getting a little too large, if this is the case you can split action to each block. EntireJ will always send events to the block level action processor instead of the form level one if it exists. Any block not having its own action processor will be managed by the the form level action processor")
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
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

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

                AbstractDescriptor<Boolean> addControlBlockDefaultRecordDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
                {

                    @Override
                    public Boolean getValue()
                    {
                        return source.isQueryAllowed();
                    }

                    @Override
                    public void setValue(Boolean value)
                    {
                        source.setQueryAllowed(value.booleanValue());
                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);

                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

                };
                addControlBlockDefaultRecordDescriptor.setText("Add Default Record");
                addControlBlockDefaultRecordDescriptor.setTooltip("Indicates if a default record should be created for a control block");
                actionDescriptor.setBaseClass(EJBlockActionProcessor.class.getName());
                actionDescriptor.setDefaultClass(EJDefaultBlockActionProcessor.class.getName());
                descriptors.add(actionDescriptor);
                dataDescriptors.add(addControlBlockDefaultRecordDescriptor);
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
                        updateMirrorBlocks();
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
                        "If you are creating a very large form, then your Form Level Action Processor may be getting a little too large, if this is the case you can split action to each block. EntireJ will always send events to the block level action processor instead of the form level one if it exists. Any block not having its own action processor will be managed by the the form level action processor")
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

                final AbstractTextDescriptor maxResultsDescriptor = new AbstractTextDescriptor(
                        "Max Results",
                        "If your block can handle paging then you can define the maximum amount of rows that should be retrieved by your block for each query. Click <a href=\"http://docs.entirej.com/display/EJ1/Block+Services\">here</a> for more details on block services and their available methods.")
                {

                    @Override
                    public void setValue(String value)
                    {
                        try
                        {
                            source.setMaxResults(Integer.parseInt(value));
                        }
                        catch (NumberFormatException e)
                        {
                            source.setMaxResults(0);

                            if (text != null)
                            {
                                text.setText(getValue());
                                text.selectAll();
                            }
                        }
                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);
                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

                    @Override
                    public String getValue()
                    {
                        return String.valueOf(source.getMaxResults());
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
                final AbstractTextDescriptor pageSizeDescriptor = new AbstractTextDescriptor(
                        "Page Size",
                        "If your block can handle paging then you can define the amount of rows that should be retrieved for each page. Click <a href=\"http://docs.entirej.com/display/EJ1/Block+Services\">here</a> for more details on block services and their available methods.")
                {

                    @Override
                    public void setValue(String value)
                    {
                        try
                        {
                            source.setPageSize(Integer.parseInt(value));
                        }
                        catch (NumberFormatException e)
                        {
                            source.setPageSize(0);
                            if (text != null)
                            {
                                text.setText(getValue());
                                text.selectAll();
                            }
                        }
                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return String.valueOf(source.getPageSize());
                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

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

                AbstractDescriptor<Boolean> queryAllowMenuDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
                {

                    @Override
                    public Boolean getValue()
                    {
                        return source.isQueryAllowed();
                    }

                    @Override
                    public void setValue(Boolean value)
                    {
                        source.setQueryAllowed(value.booleanValue());
                        editor.setDirty(true);
                        treeSection.refresh(BlockNode.this);

                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

                    @Override
                    public String getTooltip()
                    {
                        return "Indicates if queries should be allowed for this block";
                    }

                };
                queryAllowMenuDescriptor.setText("Query Allowed");

                dataDescriptors.add(maxResultsDescriptor);
                dataDescriptors.add(pageSizeDescriptor);
                dataDescriptors.add(queryAllowMenuDescriptor);
            }

            AbstractDescriptor<Boolean> insertAllowMenuDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {

                @Override
                public Boolean getValue()
                {
                    return source.isInsertAllowed();
                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setInsertAllowed(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(BlockNode.this);

                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public String getTooltip()
                {
                    return "Indicates if the user can create records within this block";
                }

            };
            insertAllowMenuDescriptor.setText("Insert Allowed");
            AbstractDescriptor<Boolean> updateAllowMenuDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {

                @Override
                public Boolean getValue()
                {
                    return source.isUpdateAllowed();
                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setUpdateAllowed(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(BlockNode.this);

                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public String getTooltip()
                {
                    return "Indicates if the user can modify records of this block";
                }

            };
            updateAllowMenuDescriptor.setText("Update Allowed");
            AbstractDescriptor<Boolean> deleteAllowMenuDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {

                @Override
                public Boolean getValue()
                {
                    return source.isDeleteAllowed();
                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setDeleteAllowed(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(BlockNode.this);

                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public String getTooltip()
                {
                    return "Indicates if the user can delete records from this block";
                }

            };
            deleteAllowMenuDescriptor.setText("Delete Allowed");
            dataDescriptors.add(insertAllowMenuDescriptor);
            dataDescriptors.add(updateAllowMenuDescriptor);
            dataDescriptors.add(deleteAllowMenuDescriptor);

            AbstractGroupDescriptor dataGroupDescriptor = new AbstractGroupDescriptor("Data Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return dataDescriptors.toArray(new AbstractDescriptor<?>[0]);
                }
            };
            if (dataDescriptors.size() > 0)
                descriptors.add(dataGroupDescriptor);
            // try to load renderer group
            final EJFrameworkExtensionProperties rendereProperties = source.getBlockRendererProperties();

            if (rendereProperties != null)
            {

                final EJDevBlockRendererDefinition formRendererDefinition = ExtensionsPropertiesFactory.loadBlockRendererDefinition(
                        source.getEntireJProperties(), source.getBlockRendererName());
                if (formRendererDefinition != null)
                {
                    final EJPropertyDefinitionGroup definitionGroup = formRendererDefinition.getBlockPropertyDefinitionGroup();
                    if (definitionGroup != null)
                    {

                        AbstractGroupDescriptor rendererGroupDescriptor = new AbstractGroupDescriptor("Renderer Settings")
                        {
                            @Override
                            public void runOperation(AbstractOperation operation)
                            {
                                editor.execute(operation);

                            }

                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                                return PropertyDefinitionGroupPart.createGroupDescriptors(editor, source.getEntireJProperties(), definitionGroup,
                                        rendereProperties, new IExtensionValues()
                                        {
                                            public EJPluginBlockProperties getBlockProperties()
                                            {
                                                return source;
                                            }

                                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                                    EJPropertyDefinition propertyDefinition)
                                            {
                                                propertyDefinition.clearValidValues();
                                                EJBlockRendererDefinition rendererDef = ExtensionsPropertiesFactory.loadBlockRendererDefinition(
                                                        source.getEntireJProperties(), source.getBlockRendererName());

                                                rendererDef.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                                            }
                                        });
                            }
                        };
                        descriptors.add(rendererGroupDescriptor);

                    }
                }
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

        public Action createReplicateAction()
        {

            return new Action("Replicate Block")
            {

                @Override
                public void runWithEvent(Event event)
                {
                    ReplicateBlockWizard wizard = new ReplicateBlockWizard(new ReplicateBlockWizardContext()
                    {

                        public void addBlock(String blockName, String canvas, boolean createCanvas)
                        {
                            final EJPluginFormProperties formProperties = editor.getFormProperties();
                            final EJPluginBlockProperties blockProperties = source.makeCopy(blockName, false);

                            
                            
                            blockProperties.setCanvasName(canvas);
                            blockProperties.setBlockRendererName(source.getBlockRendererName(), true);
                            // create items if service is also selected
                            if (createCanvas)
                            {

                                EJPluginCanvasProperties canvasProp = new EJPluginCanvasProperties(formProperties, canvas);
                                ReversibleOperation operation = new ReversibleOperation("Add Block");
                                operation.add(new BlockAddOperation(treeSection, formProperties.getBlockContainer(), blockProperties, -1));
                                operation.add(new CanvasAddOperation(treeSection, formProperties.getCanvasContainer(), canvasProp, -1));
                                editor.execute(operation);
                            }
                            else
                            {
                                BlockAddOperation addOperation = new BlockAddOperation(treeSection, formProperties.getBlockContainer(),
                                        blockProperties, -1);
                                editor.execute(addOperation);
                            }

                        }

                        public List<EJCanvasProperties> getCanvas()
                        {
                            Collection<EJCanvasProperties> canvasCollection = EJPluginCanvasRetriever.retriveAllNonAssignedBlockCanvases(editor
                                    .getFormProperties());
                            return new ArrayList<EJCanvasProperties>(canvasCollection);
                        }

                        public boolean hasBlock(String blockName)
                        {
                            return editor.getFormProperties().getBlockContainer().contains(blockName);
                        }

                        public boolean hasCanvas(String canvasName)
                        {
                            final EJPluginFormProperties formProperties = editor.getFormProperties();
                            return EJPluginCanvasRetriever.canvasExists(formProperties, canvasName);
                        }

                        public IJavaProject getProject()
                        {
                            return editor.getJavaProject();
                        }

                    });
                    wizard.open();
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

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
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

                return new BlockAddOperation(treeSection, source, (BlockContainerItem) dSource, index);
            }
        }
      
        
        return new BlockAddOperation(treeSection, source, (BlockContainerItem) dSource, -1);
    }
}
