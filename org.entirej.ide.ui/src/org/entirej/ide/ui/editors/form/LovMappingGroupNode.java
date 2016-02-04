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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJItemProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovItemMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovMappingContainer;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.form.BlockGroupNode.BlockNode;
import org.entirej.ide.ui.editors.form.operations.LovMappingAddOperation;
import org.entirej.ide.ui.editors.form.operations.LovMappingItemAddOperation;
import org.entirej.ide.ui.editors.form.operations.LovMappingItemRemoveOperation;
import org.entirej.ide.ui.editors.form.operations.LovMappingRemoveOperation;
import org.entirej.ide.ui.editors.form.wizards.LovMappingLinkWizard;
import org.entirej.ide.ui.editors.form.wizards.LovMappingLinkWizardContext;
import org.entirej.ide.ui.editors.form.wizards.LovMappingWizard;
import org.entirej.ide.ui.editors.form.wizards.LovMappingWizardContext;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;

public class LovMappingGroupNode extends AbstractNode<EJPluginLovMappingContainer> implements NodeMoveProvider
{
    private final FormDesignTreeSection treeSection;
    private final AbstractEJFormEditor  editor;
    private final static Image          GROUP              = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    private final static Image          MAPPING            = EJUIImages.getImage(EJUIImages.DESC_LOV_MAPPING);
    private final static Image          BLOCK_MAPPING_LINK = EJUIImages.getImage(EJUIImages.DESC_BLOCK_RELATION_LINK);

    public LovMappingGroupNode(FormDesignTreeSection treeSection, BlockNode node)
    {
        super(node, node.getSource().getLovMappingContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public String getName()
    {

        return "LOV Mappings";
    }

    @Override
    public String getToolTipText()
    {
        return "LOV mappings for blocks";
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
        List<EJPluginLovMappingProperties> allBlockProperties = source.getAllLovMappingProperties();
        for (EJPluginLovMappingProperties blockProperties : allBlockProperties)
        {
            nodes.add(new MappingNode(this, blockProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
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
                    treeSection.refresh(LovMappingGroupNode.this);
                }

                @Override
                public List<IMarker> getMarkers()
                {
                    List<IMarker> fmarkers = new ArrayList<IMarker>();

                    IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                    for (IMarker marker : markers)
                    {
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                        if ((tag & FormNodeTag.GROUP) != 0 && (tag & FormNodeTag.BLOCK) != 0 && (tag & FormNodeTag.MAPPING) != 0
                                && (tag & FormNodeTag.LOV) != 0)
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
    public Action[] getActions()
    {

        return new Action[] { createNewMappingAction() };
    }

    public Action createNewMappingAction()
    {

        return new Action("New LOV Mapping")
        {

            @Override
            public void runWithEvent(Event event)
            {
                LovMappingWizard wizard = new LovMappingWizard(new LovMappingWizardContext()
                {

                    public IJavaProject getProject()
                    {
                        return editor.getJavaProject();
                    }

                    public void addLovMapping(String lovName, String lovDefName, boolean executeAfterQuery)
                    {

                        final EJPluginLovMappingProperties lovMappingProperties = new EJPluginLovMappingProperties(lovName, editor.getFormProperties());

                        lovMappingProperties.setLovDefinitionName(lovDefName);
                        lovMappingProperties.setExecuteAfterQuery(executeAfterQuery);
                        lovMappingProperties.setMappedBlock(source.getBlockProperties());

                        LovMappingAddOperation addOperation = new LovMappingAddOperation(treeSection, source, lovMappingProperties, -1);
                        editor.execute(addOperation);
                    }

                    public List<String> getLovDefinitionNames()
                    {

                        return editor.getFormProperties().getLovDefinitionNames();
                    }

                    public boolean hasLovMapping(String lovName)
                    {
                        return source.contains(lovName);
                    }

                });
                wizard.open();
            }

        };
    }

    @Override
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        return new AbstractDescriptor<?>[] {};
    }

    class MappingNode extends AbstractNode<EJPluginLovMappingProperties> implements Neighbor, Movable, NodeOverview
    {
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(MappingNode.this);
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
                                                                          && (tag & FormNodeTag.MAPPING) != 0
                                                                          && (tag & FormNodeTag.LOV) != 0
                                                                          && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(FormNodeTag.MAPPING_ID, null))
                                                                          && source.getMappedBlock().getName() != null
                                                                          && source.getMappedBlock().getName()
                                                                                  .equals(marker.getAttribute(FormNodeTag.BLOCK_ID, null)))
                                                                  {

                                                                      fmarkers.add(marker);
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

        public MappingNode(AbstractNode<?> parent, EJPluginLovMappingProperties source)
        {
            super(parent, source);

        }

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

        public void addOverview(StyledString styledString)
        {
            if (source.getLovDefinitionName() != null && source.getLovDefinitionName().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getLovDefinitionName(), StyledString.COUNTER_STYLER);

            }

        }

        @Override
        public Action[] getActions()
        {
            return new Action[] { createItemMapping(), null, createNewMappingAction() };
        }

        private Action createItemMapping()
        {
            return new Action("New Item Mapping")
            {

                @Override
                public void runWithEvent(Event event)
                {
                    LovMappingLinkWizard wizard = new LovMappingLinkWizard(new LovMappingLinkWizardContext()
                    {

                        public void addLink(String defItem, String blockItem)
                        {
                            final EJPluginLovItemMappingProperties addedMappingItem = MappingNode.this.source.createMappingProperties(defItem, blockItem);

                            LovMappingItemAddOperation addOperation = new LovMappingItemAddOperation(treeSection, source, addedMappingItem, -1);
                            editor.execute(addOperation);

                        }

                        public List<String> getBlockItems()
                        {
                            List<String> blocknames = new ArrayList<String>();

                            EJBlockProperties blockProperties = MappingNode.this.source.getMappedBlock();
                            if (blockProperties != null)
                            {
                                Collection<EJItemProperties> allItemProperties = blockProperties.getAllItemProperties();
                                for (EJItemProperties ejItemProperties : allItemProperties)
                                {
                                    if (MappingNode.this.source.containsItemMappingForBlockItem(ejItemProperties.getName()))
                                        continue;

                                    blocknames.add(ejItemProperties.getName());
                                }
                            }

                            return blocknames;
                        }

                        public List<String> getDefItems()
                        {
                            List<String> blocknames = new ArrayList<String>();
                            if (MappingNode.this.source.getLovDefinitionName() != null)
                            {
                                EJPluginFormProperties formProperties = editor.getFormProperties();
                                List<String> blockProperties = formProperties.getLovDefinitionItemNames(MappingNode.this.source.getLovDefinitionName());
                                blocknames.addAll(blockProperties);
                            }
                            return blocknames;
                        }

                    });
                    wizard.open();
                }

            };
        }

        @Override
        public Image getImage()
        {
            return MAPPING;
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
            return source.getAllItemMappingProperties().isEmpty();
        }

        private final class ItemMappingNode extends AbstractNode<EJPluginLovItemMappingProperties> implements NodeOverview
        {
            private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                          {

                                                              public void refreshNode()
                                                              {
                                                                  treeSection.refresh(MappingNode.this);
                                                              }

                                                              @Override
                                                              public List<IMarker> getMarkers()
                                                              {
                                                                  List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                  List<IMarker> markers = MappingNode.this.validator.getMarkers();
                                                                  for (IMarker marker : markers)
                                                                  {

                                                                      if ((source.getLovDefinitionItemName() != null && source.getLovDefinitionItemName()
                                                                              .equals(marker.getAttribute(FormNodeTag.LOV_ID, null)))
                                                                              || (source.getBlockItemName() != null && source.getBlockItemName().equals(
                                                                                      marker.getAttribute(FormNodeTag.ITEM_ID, null))))
                                                                      {

                                                                          fmarkers.add(marker);
                                                                      }
                                                                  }

                                                                  return fmarkers;
                                                              }
                                                          };

            private ItemMappingNode(AbstractNode<?> parent, EJPluginLovItemMappingProperties source)
            {
                super(parent, source);
            }

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
                String masterItemName = source.getLovDefinitionItemName();
                return masterItemName != null ? masterItemName : "<undefined>";
            }

            public void addOverview(StyledString styledString)
            {

                if (source.getBlockItemName() != null && source.getBlockItemName().length() != 0)
                {
                    styledString.append(" = ", StyledString.QUALIFIER_STYLER);
                    styledString.append(source.getBlockItemName(), StyledString.DECORATIONS_STYLER);
                }
                else
                {
                    styledString.append(" = ", StyledString.QUALIFIER_STYLER);
                    styledString.append("<unassigned>", StyledString.DECORATIONS_STYLER);
                }

            }

            @Override
            public Action[] getActions()
            {
                return new Action[] { createItemMapping() };
            }

            public AbstractDescriptor<?>[] getNodeDescriptors()
            {
                final List<IMarker> fmarkers = validator.getMarkers();
                AbstractTextDropDownDescriptor masterDescriptor = new AbstractTextDropDownDescriptor("Block Item")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (source.getBlockItemName() != null && source.getBlockItemName().equals(
                                                   marker.getAttribute(FormNodeTag.ITEM_ID, null)));
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

                        List<String> blocknames = new ArrayList<String>();
                        EJBlockProperties blockProperties = MappingNode.this.source.getMappedBlock();
                        if (blockProperties != null)
                        {
                            Collection<EJItemProperties> allItemProperties = blockProperties.getAllItemProperties();
                            for (EJItemProperties ejItemProperties : allItemProperties)
                            {

                                blocknames.add(ejItemProperties.getName());
                            }
                        }
                        return blocknames.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return t;
                    }

                    @Override
                    public void setValue(String value)
                    {
                        source.setBlockItemName(value);

                        editor.setDirty(true);
                        treeSection.refresh(ItemMappingNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getBlockItemName();
                    }
                };

                AbstractTextDropDownDescriptor dtlDescriptor = new AbstractTextDropDownDescriptor("LOV Item")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (source.getLovDefinitionItemName() != null && source.getLovDefinitionItemName().equals(
                                                   marker.getAttribute(FormNodeTag.LOV_ID, null)));
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

                        List<String> blocknames = new ArrayList<String>();

                        if (MappingNode.this.source.getLovDefinitionName() != null)
                        {
                            EJPluginFormProperties formProperties = editor.getFormProperties();
                            blocknames.addAll(formProperties.getLovDefinitionItemNames(MappingNode.this.source.getLovDefinitionName()));
                        }

                        return blocknames.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return t;
                    }

                    @Override
                    public void setValue(String value)
                    {
                        source.setLovDefinitionItemName(value);

                        editor.setDirty(true);
                        treeSection.refresh(ItemMappingNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getLovDefinitionItemName();
                    }
                };

                return new AbstractDescriptor<?>[] { dtlDescriptor, masterDescriptor };

            }

            @Override
            public Image getImage()
            {
                return BLOCK_MAPPING_LINK;
            }

            @Override
            public INodeDeleteProvider getDeleteProvider()
            {
                return new INodeDeleteProvider()
                {

                    public void delete(boolean cleanup)
                    {
                        MappingNode.this.source.getAllItemMappingProperties().remove(source);
                        editor.setDirty(true);
                        treeSection.refresh(MappingNode.this);
                    }

                    public AbstractOperation deleteOperation(boolean cleanup)
                    {
                        return new LovMappingItemRemoveOperation(treeSection, MappingNode.this.source, source);
                    }
                };
            }
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> list = new ArrayList<AbstractNode<?>>();
            List<EJPluginLovItemMappingProperties> allItemMappingProperties = source.getAllItemMappingProperties();
            for (EJPluginLovItemMappingProperties mappingProperties : allItemMappingProperties)
            {
                list.add(new ItemMappingNode(this, mappingProperties));
            }

            return list.toArray(new AbstractNode[0]);
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

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

                        }
                    }

                    removeMappingOnBlock(oldName,  itemGroupProperties.getChildItemGroupContainer(), EJScreenType);
                }
                
                
                public void delete(boolean cleanup)
                {

                    LovMappingGroupNode.this.source.removeLovMappingProperties(source);
                    if(cleanup)
                    {
                        
                        removeMappingOnBlock(source.getName(), source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.MAIN), EJScreenType.MAIN);
                        removeMappingOnBlock(source.getName(), source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.INSERT), EJScreenType.INSERT);
                        removeMappingOnBlock(source.getName(), source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.UPDATE), EJScreenType.UPDATE);
                        removeMappingOnBlock(source.getName(), source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.QUERY), EJScreenType.QUERY);
                    }
                    editor.setDirty(true);
                    treeSection.refresh(LovMappingGroupNode.this);

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    return new LovMappingRemoveOperation(treeSection, LovMappingGroupNode.this.source, source,cleanup);
                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            return new INodeRenameProvider()
            {

                private void renameMappingOnBlock(String oldName, String newName, EJItemGroupPropertiesContainer container, EJScreenType EJScreenType)
                {
                    for (EJItemGroupProperties itemGroupProperties : container.getAllItemGroupProperties())
                    {
                        renameMappingOnBlock(oldName, newName, itemGroupProperties, EJScreenType);
                    }
                }

                private void renameMappingOnBlock(String oldName, String newName, EJItemGroupProperties itemGroupProperties, EJScreenType EJScreenType)
                {
                    for (EJScreenItemProperties screenItemProperties : itemGroupProperties.getAllItemProperties())
                    {
                        if (screenItemProperties.getLovMappingName() != null && screenItemProperties.getLovMappingName().equals(oldName))
                        {
                            switch (EJScreenType)
                            {
                                case MAIN:
                                    ((EJPluginMainScreenItemProperties) screenItemProperties).setLovMappingName(newName);
                                    break;
                                case INSERT:
                                    ((EJPluginInsertScreenItemProperties) screenItemProperties).setLovMappingName(newName);
                                    break;
                                case QUERY:
                                    ((EJPluginQueryScreenItemProperties) screenItemProperties).setLovMappingName(newName);
                                    break;
                                case UPDATE:
                                    ((EJPluginUpdateScreenItemProperties) screenItemProperties).setLovMappingName(newName);
                                    break;
                            }

                        }
                    }

                    renameMappingOnBlock(oldName, newName, itemGroupProperties.getChildItemGroupContainer(), EJScreenType);
                }

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Mapping [%s]", getName()), "Mapping Name",
                            getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Mapping name can't be empty.";
                                    if (getName().equals(newText.trim()))
                                        return "";
                                    if (getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (LovMappingGroupNode.this.source.contains(newText.trim()))
                                        return "Mapping with this name already exists.";
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        String oldName = source.getName();
                        String newName = dlg.getValue().trim();
                        source.internalSetName(newName);

                        renameMappingOnBlock(oldName, newName, source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.MAIN), EJScreenType.MAIN);
                        renameMappingOnBlock(oldName, newName, source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.INSERT), EJScreenType.INSERT);
                        renameMappingOnBlock(oldName, newName, source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.UPDATE), EJScreenType.UPDATE);
                        renameMappingOnBlock(oldName, newName, source.getMappedBlock().getScreenItemGroupContainer(EJScreenType.QUERY), EJScreenType.QUERY);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                treeSection.getEditor().setDirty(true);
                                treeSection.refresh();

                            }
                        });
                    }

                }
            };
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            final AbstractTextDescriptor dispalyNameDescriptor = new AbstractTextDescriptor("Display Name")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    source.setLovDisplayName(value);
                    editor.setDirty(true);

                    treeSection.refresh(MappingNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getLovDisplayName();
                }

            };

            final AbstractBooleanDescriptor executeAfterQueryDescriptor = new AbstractBooleanDescriptor("Execute After Query")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setExecuteAfterQuery(value.booleanValue());
                    editor.setDirty(true);

                    treeSection.refresh(MappingNode.this);
                }

                @Override
                public Boolean getValue()
                {
                    return Boolean.valueOf(source.executeAfterQuery());
                }

            };
            return new AbstractDescriptor[] { dispalyNameDescriptor, executeAfterQueryDescriptor };

        }
    }

    public boolean canMove(Neighbor relation, Object source)
    {
        return source instanceof EJPluginLovMappingProperties;
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginLovMappingProperties> items = source.getAllLovMappingProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addLovMappingProperties(index, (EJPluginLovMappingProperties) dSource);
            }
        }
        else
            source.addLovMappingProperties((EJPluginLovMappingProperties) dSource);

    }

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginLovMappingProperties> items = source.getAllLovMappingProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                return new LovMappingAddOperation(treeSection, source, (EJPluginLovMappingProperties) dSource, index);
            }
        }

        return new LovMappingAddOperation(treeSection, source, (EJPluginLovMappingProperties) dSource, -1);
    }
}
