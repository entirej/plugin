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
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJItemProperties;
import org.entirej.framework.core.renderers.definitions.interfaces.EJItemRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockItemContainer;
import org.entirej.framework.plugin.utils.EJPluginItemChanger;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractCustomDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.form.BlockGroupNode.BlockNode;
import org.entirej.ide.ui.editors.form.wizards.BlockItemWizard;
import org.entirej.ide.ui.editors.form.wizards.BlockItemWizardContext;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart.IExtensionValues;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class BlockItemsGroupNode extends AbstractNode<EJPluginBlockItemContainer> implements NodeMoveProvider
{

    public static final Image           GROUP    = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    public static final Image           BLOCK_ND = EJUIImages.getImage(EJUIImages.DESC_BLOCK_ITEM_ND);
    public static final Image           BLOCK    = EJUIImages.getImage(EJUIImages.DESC_BLOCK_ITEM);

    private final FormDesignTreeSection treeSection;
    private final AbstractEJFormEditor  editor;

    public BlockItemsGroupNode(FormDesignTreeSection treeSection, BlockNode node)
    {
        super(node, node.getSource().getItemContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public BlockItemsGroupNode(FormDesignTreeSection treeSection, AbstractNode<?> node, EJPluginBlockItemContainer container)
    {
        super(node, container);
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    @Override
    public String getName()
    {
        return "Items";
    }

    @Override
    public String getToolTipText()
    {
        return "block item definitions";
    }

    @Override
    public Image getImage()
    {
        return GROUP;
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
                    treeSection.refresh(BlockItemsGroupNode.this);
                }

                @Override
                public List<IMarker> getMarkers()
                {
                    List<IMarker> fmarkers = new ArrayList<IMarker>();

                    IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                    for (IMarker marker : markers)
                    {
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                        if ((tag & FormNodeTag.GROUP) != 0 && ((tag & FormNodeTag.BLOCK) != 0 || (tag & FormNodeTag.LOV) != 0) && (tag & FormNodeTag.ITEM) != 0)
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
    public boolean isLeaf()
    {
        return source.getItemCount() == 0;
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {
        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
        List<EJPluginBlockItemProperties> items = source.getAllItemProperties();
        for (EJPluginBlockItemProperties itemProperties : items)
        {
            nodes.add(new ItemNode(this, itemProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    protected void updateMirrorItems()
    {
        for (EJPluginBlockProperties childProperties : source.getBlockProperties().getMirrorChildren())
        {
            EJPluginBlockItemContainer itemContainer = childProperties.getItemContainer();
            AbstractNode<?> findNode = treeSection.findNode(itemContainer);
            if (findNode != null)
            {
                treeSection.refresh(findNode.getParent());
            }
        }

    }

    @Override
    public Action[] getActions()
    {
        if (source.getBlockProperties().isMirrorChild() || source.getBlockProperties().isReferenceBlock() || source.getBlockProperties().isImportFromObjectGroup())
            return new Action[] {};

        return new Action[] { createNewBlockItemAction(-1) };
    }

    class ItemNode extends AbstractNode<EJPluginBlockItemProperties> implements Neighbor, Movable, NodeOverview
    {
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(ItemNode.this);
                                                          }

                                                          @Override
                                                          public List<IMarker> getMarkers()
                                                          {
                                                              List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                              IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                                                              for (IMarker marker : markers)
                                                              {
                                                                  int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                  if ((tag & FormNodeTag.BLOCK) != 0
                                                                          && source.getBlockProperties().getName() != null
                                                                          && source.getBlockProperties().getName()
                                                                                  .equals(marker.getAttribute(FormNodeTag.BLOCK_ID, null))
                                                                          && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(FormNodeTag.ITEM_ID, null)))
                                                                  {

                                                                      fmarkers.add(marker);
                                                                  }
                                                                  // items may
                                                                  // used in lov
                                                                  if ((tag & FormNodeTag.LOV) != 0)
                                                                  {
                                                                      EJPluginLovDefinitionProperties lovDefinition = source.getBlockProperties()
                                                                              .getLovDefinition();
                                                                      if (lovDefinition != null
                                                                              && lovDefinition.getName() != null
                                                                              && lovDefinition.getName()
                                                                                      .equals(marker.getAttribute(FormNodeTag.BLOCK_ID, null))
                                                                              && source.getName() != null
                                                                              && source.getName().equals(marker.getAttribute(FormNodeTag.ITEM_ID, null)))
                                                                      {

                                                                          fmarkers.add(marker);
                                                                      }
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

        public ItemNode(AbstractNode<?> parent, EJPluginBlockItemProperties source)
        {
            super(parent, source);

        }

        @Override
        public <S> S getAdapter(Class<S> adapter)
        {
            if (NodeValidateProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(validator);
            }
            return super.getAdapter(adapter);
        }

        @Override
        public String getName()
        {
            return source.getName();
        }

        @Override
        public String getNodeDescriptorDetails()
        {
            return "Click <a href=\"http://docs.entirej.com/display/EJ1/Item+Properties\">here</a> for more information on Item Properties.  All mandatory properties are denoted by \"*\"";
        }

        @Override
        public Image getImage()
        {
            return source.isBlockServiceItem() ? BLOCK : BLOCK_ND;
        }

        @Override
        public Action[] getActions()
        {
            if (source.getBlockProperties().isMirrorChild() || source.getBlockProperties().isReferenceBlock()|| source.getBlockProperties().isImportFromObjectGroup())
                return new Action[] { createCopyBINameAction() };

            int indexOf = BlockItemsGroupNode.this.source.getAllItemProperties().indexOf(source);
            return new Action[] { createNewBlockItemAction(++indexOf), null,createCopyBINameAction()  };
        }

        public Action createCopyBINameAction()
        {

            return new Action("Copy Block Item Name")
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

        public boolean canMove()
        {
            // if it is a mirror child should not be able to DnD from mirror
            // level
            return !(this.source.getBlockProperties().isMirrorChild() || this.source.getBlockProperties().isReferenceBlock() || this.source.getBlockProperties().isImportFromObjectGroup());
        }

        public Object getNeighborSource()
        {
            return source;
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            // if it is a mirror child or Referenced should not be able to
            // delete from mirror
            // level
            if (this.source.getBlockProperties().isMirrorChild() || source.getBlockProperties().isReferenceBlock()|| source.getBlockProperties().isImportFromObjectGroup())
                return null;

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    if (cleanup)
                    {
                        BlockItemsGroupNode.this.source.removeItem(source);
                    }
                    else
                    {
                        BlockItemsGroupNode.this.source.getAllItemProperties().remove(source);
                    }
                    editor.setDirty(true);
                    treeSection.refresh(BlockItemsGroupNode.this.getParent());
                    if (BlockItemsGroupNode.this.source.getBlockProperties().isMirrorBlock())
                    {
                        // update mirror items
                        updateMirrorItems();
                    }
                }
                
                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            // if it is a mirror child or Referenced should not be able to
            // rename from mirror
            // level
            if (this.source.getBlockProperties().isMirrorChild() || source.getBlockProperties().isReferenceBlock()|| source.getBlockProperties().isImportFromObjectGroup())
                return null;

            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Item [%s]", source.getName()), "Item Name",
                            source.getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Item name can't be empty.";
                                    if (source.getName().equals(newText.trim()))
                                        return "";
                                    if (source.getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (BlockItemsGroupNode.this.source.contains(newText.trim()))
                                        return "Item with this name already exists.";
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        String oldName = source.getName();
                        String newName = dlg.getValue().trim();
                        source.setName(newName);
                        EJPluginItemChanger.renameItemOnForm(source.getBlockProperties(), oldName, newName);
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
            ItemDefaultValue queryItemDefaultValue = new ItemDefaultValue(source.getFormProperties(), "Default Query Value")
            {
                @Override
                public String getValue()
                {
                    return source.getDefaultQueryValue();
                }

                @Override
                public void setValue(Object value)
                {
                    source.setDefaultQueryValue((String) value);
                    editor.setDirty(true);
                    treeSection.refresh(ItemNode.this);
                }

                @Override
                public String getTooltip()
                {
                    return "Click <a href=\"http://docs.entirej.com/pages/viewpage.action?pageId=1769493\">here</a> for more information on the Default Query Value";
                }

            };
            ItemDefaultValue insertItemDefaultValue = new ItemDefaultValue(source.getFormProperties(), "Default Insert Value")
            {
                @Override
                public String getValue()
                {
                    return source.getDefaultInsertValue();
                }

                @Override
                public void setValue(Object value)
                {
                    source.setDefaultInsertValue((String) value);
                    editor.setDirty(true);
                    treeSection.refresh(ItemNode.this);
                }

                @Override
                public String getTooltip()
                {
                    return "Click <a href=\"http://docs.entirej.com/pages/viewpage.action?pageId=1769493\">here</a> for more information on the Default Insert Value";
                }

            };
            if (this.source.getBlockProperties().isMirrorChild())
            {
                final AbstractTextDescriptor referencedDescriptor = new AbstractTextDescriptor("Mirrored Item")
                {
                    public boolean hasLableLink()
                    {
                        return true;
                    }

                    @Override
                    public String lableLinkActivator()
                    {

                        EJPluginBlockItemProperties item = source.getBlockProperties().getMirrorParent().getItemContainer().getItemProperties(getValue());
                        if (item != null)
                        {
                            System.out.println(item);
                            AbstractNode<?> findNode = treeSection.findNode(source.getBlockProperties().getMirrorParent());
                            if (item != null && findNode != null)
                            {
                                treeSection.expand(findNode);
                                findNode = treeSection.findNode(source.getBlockProperties().getMirrorParent().getItemContainer());
                                if (findNode != null)
                                {
                                    treeSection.expand(findNode);
                                    findNode = treeSection.findNode(item);
                                    if (findNode != null)
                                        treeSection.selectNodes(true, findNode);
                                }
                            }
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
                descriptors.add(referencedDescriptor);
            }
            else if (this.source.getBlockProperties().isImportFromObjectGroup())
            {

                return new AbstractDescriptor<?>[]{  new AbstractTextDescriptor("Referenced ObjectGroup")
                {

                    public boolean hasLableLink()
                    {
                        return true;
                    }

                    @Override
                    public String lableLinkActivator()
                    {

                        EJPluginObjectGroupProperties file = editor.getFormProperties().getObjectGroupContainer()
                                .getObjectGroupProperties(source.getBlockProperties().getReferencedObjectGroupName());
                        if (file != null)
                        {
                            treeSection.selectNodes(true, treeSection.findNode(file));
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
                        return source.getBlockProperties().getReferencedObjectGroupName();
                    }

                    Text text;

                    @Override
                    public void addEditorAssist(Control control)
                    {

                        text = (Text) control;
                        text.setEditable(false);
                    }
                }};
            }
            else if (this.source.getBlockProperties().isReferenceBlock())
            {
                
                return new AbstractDescriptor<?>[] { queryItemDefaultValue, insertItemDefaultValue };
            }
            else
            {
                AbstractTypeDescriptor dataTypeDescriptor = new AbstractTypeDescriptor(editor, "Data Type")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & FormNodeTag.TYPE) != 0;
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
                        source.setDataTypeClassName(value);
                        editor.setDirty(true);
                        treeSection.refresh(ItemNode.this);

                    }

                    @Override
                    public String getValue()
                    {
                        return source.getDataTypeClassName();
                    }
                };

                descriptors.add(dataTypeDescriptor);
                
                if(!source.getBlockProperties().isControlBlock())
                {
                    AbstractDescriptor<Boolean> blockServiceDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
                    {
    
                        @Override
                        public Boolean getValue()
                        {
                            return source.isBlockServiceItem();
                        }
    
                        @Override
                        public void setValue(Boolean value)
                        {
                            source.setBlockServiceItem(value.booleanValue());
                            editor.setDirty(true);
                            treeSection.refresh(ItemNode.this);
    
                        }
    
                        @Override
                        public String getTooltip()
                        {
                            return "Indicates if this item is controlled by the blocks service. All Block Service Items must exist within the block service pojo. If you create a Block Service Item that does not exist in the pojo, then an exception will be thrown as soon as the Block Service is called to query, insert, update or delete data.";
                        }
    
                    };
                    blockServiceDescriptor.setText("Block Service Item");
                    
    
                    
                    descriptors.add(blockServiceDescriptor);
                }
            }
            AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Item Renderer",
                    "The renderer you have chosen for your item")
            {
                final String EMPTY   = "";
                Filter       vfilter = new Filter()
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
                    options.add(EMPTY);
                    EJPluginAssignedRendererContainer rendererContainer = editor.getFormProperties().getEntireJProperties().getItemRendererContainer();
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
                public void setValue(String value)
                {
                    source.setItemRendererName(value, true);
                    editor.setDirty(true);
                    if (EMPTY.equals(value))
                    {
                        EJPluginItemChanger.deleteItemOnForm(source.getBlockProperties(), source.getName());
                        treeSection.refresh();
                    }
                    else
                    {

                        if (!source.getItemRendererDefinition().canExecuteActionCommand())
                        {
                            EJPluginItemChanger.removeItemActionCommand(source.getBlockProperties(), source.getName());

                        }
                        treeSection.refresh(ItemNode.this);
                    }

                    if (treeSection.getDescriptorViewer() != null)
                        treeSection.getDescriptorViewer().showDetails(ItemNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getItemRendererName();
                }
            };

            descriptors.add(rendererDescriptor);

            descriptors.add(queryItemDefaultValue);
            descriptors.add(insertItemDefaultValue);

            // try to load renderer group
            final EJFrameworkExtensionProperties rendereProperties = source.getItemRendererProperties();

            if (rendereProperties != null)
            {
                final EJPluginEntireJProperties entireJProperties = source.getFormProperties().getEntireJProperties();
                final EJDevItemRendererDefinition formRendererDefinition = ExtensionsPropertiesFactory.loadItemRendererDefinition(entireJProperties,
                        source.getItemRendererName());
                if (formRendererDefinition != null)
                {
                    final EJPropertyDefinitionGroup definitionGroup = formRendererDefinition.getItemPropertyDefinitionGroup();
                    if (definitionGroup != null)
                    {

                        AbstractGroupDescriptor rendererGroupDescriptor = new AbstractGroupDescriptor("Renderer Settings")
                        {

                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                                return PropertyDefinitionGroupPart.createGroupDescriptors(editor, entireJProperties, definitionGroup, rendereProperties,
                                        new IExtensionValues()
                                        {

                                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                                    EJPropertyDefinition propertyDefinition)
                                            {
                                                propertyDefinition.clearValidValues();
                                                EJItemRendererDefinition renderer = ExtensionsPropertiesFactory.loadItemRendererDefinition(source
                                                        .getBlockProperties().getEntireJProperties(), source.getItemRendererName());

                                                renderer.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                                            }

                                            public EJPluginBlockProperties getBlockProperties()
                                            {

                                                return source.getBlockProperties();
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

        public void addOverview(StyledString styledString)
        {
            if (source.getItemRendererName() != null && source.getItemRendererName().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getItemRendererName(), StyledString.DECORATIONS_STYLER);

            }

        }

    }

    public boolean canMove(Neighbor relation, Object source)
    {
        // only allow to DnD with in the same block
        return (source instanceof EJPluginBlockItemProperties && ((EJPluginBlockItemProperties) source).getBlockProperties().equals(
                this.source.getBlockProperties()));
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        try
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginBlockItemProperties> items = source.getAllItemProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.addItemProperties(index, (EJPluginBlockItemProperties) dSource);
                }
            }
            else
                source.addItemProperties((EJPluginBlockItemProperties) dSource);
        }
        finally
        {
            updateMirrorItems();
        }
    }

    public Action createNewBlockItemAction(final int index)
    {

        return new Action("New Block Item")
        {

            @Override
            public void runWithEvent(Event event)
            {
                final BlockItemWizardContext context = new BlockItemWizardContext()
                {

                    public boolean hasBlockItem(String blockName)
                    {
                        return source.contains(blockName);
                    }

                    public IJavaProject getProject()
                    {
                        return editor.getJavaProject();
                    }
                    
                    public boolean isContorl()
                    {
                        return source.getBlockProperties().isControlBlock();
                    }

                    public List<EJPluginRenderer> getBlockItemRenderer()
                    {
                        Collection<EJPluginRenderer> allRenderers = editor.getFormProperties().getEntireJProperties().getItemRendererContainer()
                                .getAllRenderers();
                        return new ArrayList<EJPluginRenderer>(allRenderers);
                    }

                    public void addBlock(String blockItemName, String dataType, EJPluginRenderer blockItem, boolean serviceItem)
                    {
                        final EJPluginBlockItemProperties itemProperties = new EJPluginBlockItemProperties(source.getBlockProperties(), blockItemName,
                                !serviceItem);
                        itemProperties.setDataTypeClassName(dataType);
                        itemProperties.setBlockServiceItem(serviceItem);
                        if (blockItem != null)
                            itemProperties.setItemRendererName(blockItem.getAssignedName(), true);

                        if (index == -1)
                        {
                            source.addItemProperties(itemProperties);
                        }
                        else
                        {
                            source.addItemProperties(index, itemProperties);
                        }
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh(BlockItemsGroupNode.this);
                                treeSection.selectNodes(true, treeSection.findNode(itemProperties));
                                if (BlockItemsGroupNode.this.source.getBlockProperties().isMirrorBlock())
                                {
                                    // update mirror items
                                    updateMirrorItems();
                                }

                            }
                        });
                    }
                };
                BlockItemWizard wizard = new BlockItemWizard(context);
                wizard.open();
            }

        };
    }

    private static class ItemDefaultValue extends AbstractGroupDescriptor
    {

        final EJPluginFormProperties formProp;

        @Override
        public boolean isExpand()
        {
            return true;
        }

        enum TYPE
        {
            EMPTY, BLOCK_ITEM, FORM_PARAMETER, APP_PARAMETER, CLASS_FIELD;

            public String toString()
            {
                switch (this)
                {
                    case EMPTY:
                        return "";
                    case BLOCK_ITEM:
                        return "Block Item";
                    case APP_PARAMETER:
                        return "Applcation Level Parameter";
                    case FORM_PARAMETER:
                        return "Form Parameter";
                    case CLASS_FIELD:
                        return "Class Field";
                    default:
                        return super.toString();
                }
            }
        }

        TYPE entry;

        public ItemDefaultValue(EJPluginFormProperties formProp, String lable)
        {
            super(lable);
            this.formProp = formProp;
        }

        public Control createHeader(final IRefreshHandler handler, Composite parent, GridData gd)
        {
            final ComboViewer comboViewer = new ComboViewer(parent, SWT.READ_ONLY | SWT.BORDER);

            gd.verticalSpan = 2;
            gd.widthHint = 100;
            gd.horizontalIndent = 0;
            comboViewer.getCombo().setLayoutData(gd);

            comboViewer.setContentProvider(new IStructuredContentProvider()
            {

                public void inputChanged(Viewer arg0, Object arg1, Object arg2)
                {
                }

                public void dispose()
                {
                }

                public Object[] getElements(Object arg0)
                {

                    return TYPE.values();
                }
            });

            comboViewer.setInput(new Object());

            String value = getValue();
            if (value != null && value.trim().length() > 0 && value.indexOf(":") > 0)
            {
                try
                {
                    entry = TYPE.valueOf(value.substring(0, value.indexOf(":")));

                }
                catch (IllegalArgumentException e)
                {

                }

            }
            else
            {
                entry = TYPE.EMPTY;
            }
            comboViewer.setSelection(new StructuredSelection(entry));
            comboViewer.addSelectionChangedListener(new ISelectionChangedListener()
            {

                public void selectionChanged(SelectionChangedEvent event)
                {
                    TYPE newEntry = null;
                    if (comboViewer.getSelection() instanceof IStructuredSelection)
                        newEntry = (TYPE) ((IStructuredSelection) comboViewer.getSelection()).getFirstElement();
                    if ((newEntry == null && entry != null) || (!newEntry.equals(entry)))
                    {
                        entry = newEntry;
                        setValue("");// clear old value
                        handler.refresh();
                    }

                }
            });

            return comboViewer.getCombo();
        }

        public AbstractDescriptor<?>[] getDescriptors()
        {
            if (entry != null)
                switch (entry)
                {
                    case FORM_PARAMETER:
                    case APP_PARAMETER:
                        return new AbstractDescriptor<?>[] { new AbstractTextDropDownDescriptor("Parameter")
                        {

                            public String[] getOptions()
                            {
                                List<String> list = new ArrayList<String>();
                                list.add("");
                                if (entry == ItemDefaultValue.TYPE.FORM_PARAMETER)
                                {
                                    Collection<EJPluginApplicationParameter> allFormParameters = formProp.getAllFormParameters();
                                    for (EJPluginApplicationParameter parameter : allFormParameters)
                                    {
                                        list.add(parameter.getName());
                                    }
                                }
                                if (entry == ItemDefaultValue.TYPE.APP_PARAMETER)
                                {
                                    Collection<EJPluginApplicationParameter> allFormParameters = formProp.getEntireJProperties()
                                            .getAllApplicationLevelParameters();
                                    for (EJPluginApplicationParameter parameter : allFormParameters)
                                    {
                                        list.add(parameter.getName());
                                    }
                                }

                                return list.toArray(new String[0]);
                            }

                            public String getOptionText(String t)
                            {
                                return t;
                            }

                            @Override
                            public void setValue(String value)
                            {
                                if (value != null && value.trim().length() > 0)
                                {
                                    ItemDefaultValue.this.setValue(String.format("%s:%s", entry.name(), value));
                                }
                                else
                                    ItemDefaultValue.this.setValue("");

                            }

                            @Override
                            public String getValue()
                            {
                                String value = ItemDefaultValue.this.getValue();
                                if (value != null && value.trim().length() > 0 && value.indexOf(":") > 0)
                                {
                                    return value.substring(value.indexOf(":") + 1);
                                }
                                return "";
                            }
                        } };
                    case BLOCK_ITEM:
                        return new AbstractDescriptor<?>[] { new AbstractCustomDescriptor<String>("Block Item", "")
                        {
                            ComboViewer blockViewer;
                            ComboViewer itemViewer;

                            @Override
                            public void setValue(String value)
                            {
                                if (value != null && value.trim().length() > 0)
                                {
                                    ItemDefaultValue.this.setValue(String.format("%s:%s", entry.name(), value));
                                }
                                else
                                    ItemDefaultValue.this.setValue("");
                            }

                            @Override
                            public String getValue()
                            {
                                String value = ItemDefaultValue.this.getValue();
                                if (value != null && value.trim().length() > 0 && value.indexOf(":") > 0)
                                {
                                    return value.substring(value.indexOf(":") + 1);
                                }
                                return "";
                            }

                            private void updateUIState()
                            {
                                if (blockViewer != null && itemViewer != null)
                                {
                                    itemViewer.getCombo().setEnabled(blockViewer.getCombo().getSelectionIndex() != -1);
                                }
                            }

                            public boolean isUseLabel()
                            {
                                return true;
                            }

                            public Control createBody(Composite parent, GridData gd)
                            {
                                String defaultValue = getValue();
                                Composite body = new Composite(parent, SWT.NULL);
                                gd.verticalSpan = 2;
                                body.setLayoutData(gd);
                                if (isUseLabel())
                                    new Label(parent, SWT.NULL);
                                else
                                {
                                    gd.horizontalSpan = 2;
                                }
                                GridLayout layout = new GridLayout(1, true);
                                layout.marginWidth = 0;
                                layout.marginRight = 0;
                                layout.marginLeft = 0;
                                layout.marginHeight = 0;
                                layout.marginTop = 0;
                                layout.marginBottom = 0;
                                body.setLayout(layout);
                                GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                                blockViewer = new ComboViewer(body, SWT.READ_ONLY);
                                blockViewer.getCombo().setLayoutData(gridData);
                                itemViewer = new ComboViewer(body, SWT.READ_ONLY);
                                itemViewer.getCombo().setLayoutData(gridData);

                                blockViewer.setContentProvider(new IStructuredContentProvider()
                                {

                                    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
                                    {
                                    }

                                    public void dispose()
                                    {
                                    }

                                    public Object[] getElements(Object inputElement)
                                    {

                                        return formProp.getBlockNames().toArray();
                                    }
                                });
                                itemViewer.setContentProvider(new IStructuredContentProvider()
                                {

                                    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
                                    {
                                    }

                                    public void dispose()
                                    {
                                    }

                                    public Object[] getElements(Object inputElement)
                                    {
                                        Collection<EJItemProperties> allItemProperties = formProp.getBlockProperties((String) inputElement)
                                                .getAllItemProperties();
                                        List<String> blockItemNames = new ArrayList<String>();
                                        for (EJItemProperties ejItemProperties : allItemProperties)
                                        {

                                            blockItemNames.add(ejItemProperties.getName());
                                        }
                                        return blockItemNames.toArray();

                                    }
                                });

                                blockViewer.addSelectionChangedListener(new ISelectionChangedListener()
                                {

                                    public void selectionChanged(SelectionChangedEvent event)
                                    {
                                        String lov = null;
                                        if (blockViewer.getSelection() instanceof IStructuredSelection)
                                        {
                                            lov = (String) ((IStructuredSelection) blockViewer.getSelection()).getFirstElement();

                                        }
                                        itemViewer.getCombo().select(-1);

                                        itemViewer.setInput(lov);
                                        if (itemViewer.getCombo().getItemCount() > 0)
                                        {
                                            itemViewer.setSelection(new StructuredSelection(itemViewer.getCombo().getItem(0)));
                                        }
                                        updateUIState();

                                    }
                                });

                                blockViewer.setInput(new Object());
                                if (defaultValue != null)
                                {
                                    String[] split = defaultValue.split("\\.");
                                    if (split.length == 2)
                                    {
                                        blockViewer.setSelection(new StructuredSelection(split[0]));
                                        itemViewer.setSelection(new StructuredSelection(split[1]));
                                    }
                                }

                                itemViewer.addSelectionChangedListener(new ISelectionChangedListener()
                                {

                                    public void selectionChanged(SelectionChangedEvent event)
                                    {
                                        if (blockViewer.getSelection() instanceof IStructuredSelection)
                                        {
                                            String lov = (String) ((IStructuredSelection) blockViewer.getSelection()).getFirstElement();
                                            if (itemViewer.getSelection() instanceof IStructuredSelection)
                                            {
                                                String item = (String) ((IStructuredSelection) itemViewer.getSelection()).getFirstElement();
                                                setValue(String.format("%s.%s", lov, item));
                                            }

                                        }

                                    }
                                });

                                updateUIState();
                                return body;
                            }

                        } };

                    case CLASS_FIELD:
                        return new AbstractDescriptor<?>[] { new AbstractCustomDescriptor<String>("Class Field", "")
                        {
                            Text        classNameText;
                            ComboViewer itemViewer;

                            @Override
                            public void setValue(String value)
                            {
                                if (value != null && value.trim().length() > 0)
                                {
                                    ItemDefaultValue.this.setValue(String.format("%s:%s", entry.name(), value));
                                }
                                else
                                    ItemDefaultValue.this.setValue("");
                            }

                            @Override
                            public String getValue()
                            {
                                String value = ItemDefaultValue.this.getValue();
                                if (value != null && value.trim().length() > 0 && value.indexOf(":") > 0)
                                {
                                    return value.substring(value.indexOf(":") + 1);
                                }
                                return "";
                            }

                            private void updateUIState()
                            {
                                if (classNameText != null && itemViewer != null)
                                {
                                    itemViewer.getCombo().setEnabled(classNameText.getText().length() > 0);
                                }
                            }

                            public boolean isUseLabel()
                            {
                                return true;
                            }

                            public Control createBody(Composite parent, GridData gd)
                            {
                                String defaultValue = getValue();
                                Composite body = new Composite(parent, SWT.NULL);
                                gd.verticalSpan = 2;
                                body.setLayoutData(gd);
                                if (isUseLabel())
                                    new Label(parent, SWT.NULL);
                                else
                                {
                                    gd.horizontalSpan = 2;
                                }
                                GridLayout layout = new GridLayout(2, false);
                                layout.marginWidth = 0;
                                layout.marginRight = 0;
                                layout.marginLeft = 0;
                                layout.marginHeight = 0;
                                layout.marginTop = 0;
                                layout.marginBottom = 0;
                                body.setLayout(layout);
                                classNameText = new Text(body, SWT.BORDER);

                                classNameText.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
                                classNameText.setEditable(false);
                                Button typeSelect = new Button(body, SWT.PUSH);
                                typeSelect.setText("Type");
                                typeSelect.addSelectionListener(new SelectionAdapter()
                                {
                                    @Override
                                    public void widgetSelected(SelectionEvent e)
                                    {
                                        IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), formProp.getJavaProject().getResource(),
                                                IJavaElementSearchConstants.CONSIDER_CLASSES_AND_INTERFACES);
                                        if (type != null)
                                        {
                                            classNameText.setText(type.getFullyQualifiedName('$'));
                                            itemViewer.setInput(type.getFullyQualifiedName('$'));
                                            itemViewer.getCombo().select(-1);

                                            updateUIState();
                                        }
                                    }
                                });

                                itemViewer = new ComboViewer(body, SWT.READ_ONLY);
                                GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
                                gridData.horizontalSpan = 2;
                                itemViewer.getCombo().setLayoutData(gridData);

                                itemViewer.setContentProvider(new IStructuredContentProvider()
                                {

                                    public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
                                    {
                                    }

                                    public void dispose()
                                    {
                                    }

                                    public Object[] getElements(Object inputElement)
                                    {

                                        List<String> itemNames = new ArrayList<String>();

                                        if (inputElement != null && ((String) inputElement).trim().length() > 0)
                                        {
                                            try
                                            {
                                                IType findType = formProp.getJavaProject().findType((String) inputElement);
                                                if (findType != null)
                                                {
                                                    itemNames.addAll(JavaAccessUtils.getStaticFieldsOfType(findType));
                                                }
                                            }
                                            catch (JavaModelException e)
                                            {
                                                // ignore
                                            }
                                            catch (CoreException e)
                                            {
                                                // ignore
                                            }
                                        }

                                        return itemNames.toArray();

                                    }
                                });

                                if (defaultValue != null)
                                {
                                    int lastIndexOf = defaultValue.lastIndexOf(".");
                                    if (lastIndexOf > 0)
                                    {
                                        classNameText.setText(defaultValue.substring(0, lastIndexOf));
                                        itemViewer.setInput(classNameText.getText());
                                        itemViewer.setSelection(new StructuredSelection(defaultValue.substring(lastIndexOf + 1)));
                                    }
                                }

                                itemViewer.addSelectionChangedListener(new ISelectionChangedListener()
                                {

                                    public void selectionChanged(SelectionChangedEvent event)
                                    {

                                        String lov = classNameText.getText();
                                        if (itemViewer.getSelection() instanceof IStructuredSelection)
                                        {
                                            String item = (String) ((IStructuredSelection) itemViewer.getSelection()).getFirstElement();
                                            setValue(String.format("%s.%s", lov, item));
                                        }
                                    }
                                });

                                updateUIState();
                                return body;
                            }

                        } };

                }
            return new AbstractDescriptor<?>[0];
        }

        @Override
        public String getValue()
        {
            return null;
        }

    }

}
