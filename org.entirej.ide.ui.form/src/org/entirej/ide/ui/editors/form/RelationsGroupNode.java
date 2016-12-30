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
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationJoinProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginRelationContainer;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.operations.RelationAddOperation;
import org.entirej.ide.ui.editors.form.operations.RelationRemoveOperation;
import org.entirej.ide.ui.editors.form.wizards.RelationLinkWizard;
import org.entirej.ide.ui.editors.form.wizards.RelationLinkWizardContext;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;

public class RelationsGroupNode extends AbstractNode<EJPluginRelationContainer> implements NodeMoveProvider
{
    private final FormDesignTreeSection treeSection;
    private final AbstractEJFormEditor  editor;
    private final static Image          GROUP               = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    private final static Image          BLOCK_RELATION      = EJUIImages.getImage(EJUIImages.DESC_BLOCK_RELATION);
    private final static Image          BLOCK_RELATION_REF      = EJUIImages.getImage(EJUIImages.DESC_BLOCK_RELATION_REF);
    private final static Image          BLOCK_RELATION_LINK = EJUIImages.getImage(EJUIImages.DESC_BLOCK_RELATION_LINK);

    public RelationsGroupNode(FormDesignTreeSection treeSection)
    {
        super(null, treeSection.getEditor().getFormProperties().getRelationContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public String getName()
    {
        return "Relations";
    }

    @Override
    public String getToolTipText()
    {
        return "Relation definitions for blocks";
    }

    @Override
    public String getNodeDescriptorDetails()
    {
        return "Click <a href=\"http://docs.entirej.com/display/EJ1/Working+with+relations\">here</a> for more information on block relations";
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
    public <S> S getAdapter(Class<S> adapter)
    {
        if (NodeValidateProvider.class.isAssignableFrom(adapter))
        {
            return adapter.cast(new AbstractMarkerNodeValidator()
            {

                public void refreshNode()
                {
                    treeSection.refresh(RelationsGroupNode.this);
                }

                @Override
                public List<IMarker> getMarkers()
                {
                    List<IMarker> fmarkers = new ArrayList<IMarker>();

                    IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                    for (IMarker marker : markers)
                    {
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                        if ((tag & FormNodeTag.GROUP) != 0 && (tag & FormNodeTag.REALTION) != 0)
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
    public AbstractNode<?>[] getChildren()
    {
        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
        Collection<EJPluginRelationProperties> allBlockProperties = source.getAllRelationProperties();
        for (EJPluginRelationProperties blockProperties : allBlockProperties)
        {
            nodes.add(new RelationNode(this, blockProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        return new Action[] { treeSection.createNewRelationAction() };
    }

    @Override
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        return new AbstractDescriptor<?>[] {};
    }

    class RelationNode extends AbstractNode<EJPluginRelationProperties> implements Neighbor, Movable, NodeOverview
    {
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(RelationNode.this);
                                                          }

                                                          @Override
                                                          public List<IMarker> getMarkers()
                                                          {
                                                              List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                              IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                                                              for (IMarker marker : markers)
                                                              {
                                                                  int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                  if ((tag & FormNodeTag.REALTION) != 0 && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(FormNodeTag.REALTION_ID, null)))
                                                                  {

                                                                      fmarkers.add(marker);
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

        @Override
        public String getNodeDescriptorDetails()
        {
            return "Click <a href=\"http://docs.entirej.com/display/EJ1/Working+with+relations\">here</a> for more information on block relations";
        }

        private final class RelationLinkNode extends AbstractNode<EJPluginRelationJoinProperties> implements NodeOverview
        {
            boolean isImportFromObjectGroup ;
            private RelationLinkNode(AbstractNode<?> parent, EJPluginRelationJoinProperties source, boolean isImportFromObjectGroup)
            {
                super(parent, source);
                this.isImportFromObjectGroup =isImportFromObjectGroup;
            }

            @Override
            public String getName()
            {
                String masterItemName = source.getMasterItemName();
                return masterItemName != null ? masterItemName : "<undedined>";
            }

            public void addOverview(StyledString styledString)
            {

               
                
                if (source.getDetailItemName() != null && source.getDetailItemName().length() != 0)
                {
                    styledString.append(" = ", StyledString.QUALIFIER_STYLER);
                    styledString.append(source.getDetailItemName(), StyledString.DECORATIONS_STYLER);
                }

            }

            @Override
            public Action[] getActions()
            {
                if(isImportFromObjectGroup)
                {
                    return new Action[]{};
                }
                return new Action[] { createNewRelationJoinAction() };
            }

            public AbstractDescriptor<?>[] getNodeDescriptors()
            {

                if(isImportFromObjectGroup)
                {
                    return new  AbstractDescriptor<?>[0];
                }
                
                AbstractTextDropDownDescriptor masterDescriptor = new AbstractTextDropDownDescriptor("Master Item")
                {
                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);
                        
                    }
                    public String[] getOptions()
                    {

                        List<String> blocknames = new ArrayList<String>();
                        if (RelationNode.this.source.getMasterBlockName() != null)
                        {
                            EJPluginFormProperties formProperties = editor.getFormProperties();
                            EJBlockProperties blockProperties = formProperties.getBlockProperties(RelationNode.this.source.getMasterBlockName());
                            if (blockProperties != null)
                            {
                                Collection<EJItemProperties> allItemProperties = blockProperties.getAllItemProperties();
                                for (EJItemProperties ejItemProperties : allItemProperties)
                                {

                                    blocknames.add(ejItemProperties.getName());
                                }
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
                        source.setMasterItemName(value);

                        editor.setDirty(true);
                        treeSection.refresh(RelationLinkNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getMasterItemName();
                    }
                };

                AbstractTextDropDownDescriptor dtlDescriptor = new AbstractTextDropDownDescriptor("Detail Item")
                {
                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);
                        
                    }
                    public String[] getOptions()
                    {

                        List<String> blocknames = new ArrayList<String>();

                        if (RelationNode.this.source.getDetailBlockName() != null)
                        {
                            EJPluginFormProperties formProperties = editor.getFormProperties();
                            EJBlockProperties blockProperties = formProperties.getBlockProperties(RelationNode.this.source.getDetailBlockName());
                            if (blockProperties != null)
                            {
                                Collection<EJItemProperties> allItemProperties = blockProperties.getAllItemProperties();
                                for (EJItemProperties ejItemProperties : allItemProperties)
                                {

                                    blocknames.add(ejItemProperties.getName());
                                }
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
                        source.setDetailItemName(value);

                        editor.setDirty(true);
                        treeSection.refresh(RelationLinkNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getDetailItemName();
                    }
                };

                return new AbstractDescriptor<?>[] { masterDescriptor, dtlDescriptor };

            }

            @Override
            public Image getImage()
            {
                return BLOCK_RELATION_LINK;
            }

            @Override
            public INodeDeleteProvider getDeleteProvider()
            {
                if(isImportFromObjectGroup)
                {
                    return null;
                }
                return new INodeDeleteProvider()
                {

                    public void delete(boolean cleanup)
                    {
                        RelationNode.this.source.getRelationJoins().remove(source);
                        editor.setDirty(true);
                        treeSection.refresh(RelationNode.this);
                    }
                    
                    public AbstractOperation deleteOperation(boolean cleanup)
                    {
                        // TODO Auto-generated method stub
                        return null;
                    }
                };
            }
        }

        public RelationNode(AbstractNode<?> parent, EJPluginRelationProperties source)
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

        public void addOverview(StyledString styledString)
        {
            if(source.isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }
            
            if (source.getMasterBlockName() != null && source.getMasterBlockName().length() != 0)
            {
                styledString.append(" : ");
                styledString.append(source.getMasterBlockName(), StyledString.COUNTER_STYLER);
                styledString.append(" --> ", StyledString.QUALIFIER_STYLER);

            }

            if (source.getDetailBlockName() != null && source.getDetailBlockName().length() != 0)
            {

                styledString.append(source.getDetailBlockName(), StyledString.COUNTER_STYLER);

            }

        }

        @Override
        public Image getImage()
        {
            return source.isImportFromObjectGroup() ? BLOCK_RELATION_REF : BLOCK_RELATION;
        }

        public boolean canMove()
        {
            return !source.isImportFromObjectGroup();
        }

        public Object getNeighborSource()
        {
            return source;
        }

        @Override
        public Action[] getActions()
        {
            if(source.isImportFromObjectGroup())
            {
                return new Action[]{};
            }
            return new Action[] { createNewRelationJoinAction(), null, treeSection.createNewRelationAction() };
        }

        private Action createNewRelationJoinAction()
        {
            return new Action("New Relation Join")
            {

                @Override
                public void runWithEvent(Event event)
                {
                    RelationLinkWizard wizard = new RelationLinkWizard(new RelationLinkWizardContext()
                    {

                        public void addRelationLink(String master, String detail)
                        {
                            final EJPluginRelationJoinProperties joinProperties = new EJPluginRelationJoinProperties(master, detail);
                            RelationNode.this.source.getRelationJoins().add(joinProperties);

                            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                            {

                                public void run()
                                {
                                    editor.setDirty(true);
                                    treeSection.refresh(RelationNode.this);
                                    treeSection.expand(RelationNode.this);
                                    treeSection.selectNodes(true, (joinProperties));

                                }
                            });

                        }

                        public List<String> getMasterBlockItems()
                        {
                            List<String> blocknames = new ArrayList<String>();
                            if (RelationNode.this.source.getMasterBlockName() != null)
                            {
                                EJPluginFormProperties formProperties = editor.getFormProperties();
                                EJBlockProperties blockProperties = formProperties.getBlockProperties(RelationNode.this.source.getMasterBlockName());
                                if (blockProperties != null)
                                {
                                    Collection<EJItemProperties> allItemProperties = blockProperties.getAllItemProperties();
                                    for (EJItemProperties ejItemProperties : allItemProperties)
                                    {

                                        blocknames.add(ejItemProperties.getName());
                                    }
                                }
                            }
                            return blocknames;
                        }

                        public List<String> getDetailBlockItems()
                        {
                            List<String> blocknames = new ArrayList<String>();
                            if (RelationNode.this.source.getDetailBlockName() != null)
                            {
                                EJPluginFormProperties formProperties = editor.getFormProperties();
                                EJBlockProperties blockProperties = formProperties.getBlockProperties(RelationNode.this.source.getDetailBlockName());
                                if (blockProperties != null)
                                {
                                    Collection<EJItemProperties> allItemProperties = blockProperties.getAllItemProperties();
                                    for (EJItemProperties ejItemProperties : allItemProperties)
                                    {

                                        blocknames.add(ejItemProperties.getName());
                                    }
                                }
                            }
                            return blocknames;
                        }

                    });
                    wizard.open();
                }

            };
        }

        @Override
        public boolean isLeaf()
        {

            return source.getRelationJoins().isEmpty();
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            Collection<EJPluginRelationJoinProperties> relationJoins = source.getRelationJoins();
            for (final EJPluginRelationJoinProperties joinProperties : relationJoins)
            {
                nodes.add(new RelationLinkNode(this, joinProperties,source.isImportFromObjectGroup()));
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            if(source.isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    RelationsGroupNode.this.source.removeRelationProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(RelationsGroupNode.this);

                }
                
                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    return new RelationRemoveOperation(treeSection, RelationsGroupNode.this.source, source);
                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            
            if(source.isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Relation [%s]", getName()), "Relation Name",
                            getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Relation name can't be empty.";
                                    if (getName().equals(newText.trim()))
                                        return "";
                                    if (getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (RelationsGroupNode.this.source.contains(newText.trim()))
                                        return "Relation with this name already exists.";
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        String newName = dlg.getValue().trim();
                        source.setName(newName);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                treeSection.getEditor().setDirty(true);
                                treeSection.refresh(RelationNode.this);

                            }
                        });
                    }

                }
            };
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            
            if(source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[]{  new AbstractTextDescriptor("Referenced ObjectGroup")
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
                }};
            }
            final List<IMarker> fmarkers = validator.getMarkers();
            List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();
            AbstractTextDropDownDescriptor masterDescriptor = new AbstractTextDropDownDescriptor("Master Block", "The parent block of the relation")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.MASTER) != 0;
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
                public String[] getOptions()
                {

                    List<String> blocknames = source.getFormProperties().getBlockNames();

                    return blocknames.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return t;
                }

                @Override
                public void setValue(String value)
                {
                    source.setMasterBlockName(value);

                    editor.setDirty(true);
                    treeSection.refresh(RelationNode.this);
                    // if (treeSection.getDescriptorViewer() != null)
                    // treeSection.getDescriptorViewer().showDetails(BlockNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getMasterBlockName();
                }
            };

            descriptors.add(masterDescriptor);

            AbstractTextDropDownDescriptor dtlDescriptor = new AbstractTextDropDownDescriptor("Detail Block", "The child block of the relation")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.DETAIL) != 0;
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
                public String[] getOptions()
                {

                    List<String> blocknames = source.getFormProperties().getBlockNames();

                    return blocknames.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return t;
                }

                @Override
                public void setValue(String value)
                {
                    source.setDetailBlockName(value);

                    editor.setDirty(true);
                    treeSection.refresh(RelationNode.this);
                    // if (treeSection.getDescriptorViewer() != null)
                    // treeSection.getDescriptorViewer().showDetails(BlockNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getDetailBlockName();
                }
            };

            descriptors.add(dtlDescriptor);

            AbstractBooleanDescriptor masterReqDescriptor = new AbstractBooleanDescriptor(
                    "Master Required",
                    "Indicates if a record must exist in the master block for operations to be allowed in the detail block. i.e. You will not be able to query the detail block if no master record exists if a master is required")
            {

                @Override
                public void setValue(Boolean value)
                {
                    source.setPreventMasterlessOperations(value.booleanValue());

                    editor.setDirty(true);
                    treeSection.refresh(RelationNode.this);

                }
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                @Override
                public Boolean getValue()
                {
                    return source.preventMasterlessOperations();
                }
            };
            descriptors.add(masterReqDescriptor);
            AbstractBooleanDescriptor deferredQueryReqDescriptor = new AbstractBooleanDescriptor(
                    "Deferred Query",
                    "EntierJ will, by default, ensure that all detail blocks are queried after their master blocks are queried. This operation can be deferred if you so wish. Click <a href=\"http://docs.entirej.com/display/EJ1/Deferred+and+Auto+Query\">here</a> for more information on deferred queries")
            {

                @Override
                public void setValue(Boolean value)
                {
                    source.setDeferredQuery(value.booleanValue());

                    editor.setDirty(true);
                    treeSection.refresh(RelationNode.this);

                }
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                @Override
                public Boolean getValue()
                {
                    return source.isDeferredQuery();
                }
            };
            descriptors.add(deferredQueryReqDescriptor);
            AbstractBooleanDescriptor autoQueryReqDescriptor = new AbstractBooleanDescriptor("Auto Query","This property works in conjunction with the Deferred Query property. If deferred query is set, you can instruct EntireJ to automatically execute a query on the detail block as soon as it gains focus. If this property is not set, the user will have to make the query manually. Click <a href=\"http://docs.entirej.com/display/EJ1/Deferred+and+Auto+Query\">here</a> for more information on auto queries")
            {

                @Override
                public void setValue(Boolean value)
                {
                    source.setAutoQuery(value.booleanValue());

                    editor.setDirty(true);
                    treeSection.refresh(RelationNode.this);

                }
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                @Override
                public Boolean getValue()
                {
                    return source.isAutoQuery();
                }
            };
            descriptors.add(autoQueryReqDescriptor);

            return descriptors.toArray(new AbstractDescriptor<?>[0]);

        }
    }

    public boolean canMove(Neighbor relation, Object source)
    {
        return source instanceof EJPluginRelationProperties && !((EJPluginRelationProperties)source).isImportFromObjectGroup();
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginRelationProperties> items = source.getAllRelationProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addRelationProperties(index, (EJPluginRelationProperties) dSource);
            }
        }
        else
            source.addRelationProperties((EJPluginRelationProperties) dSource);

    }
    
    
    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginRelationProperties> items = source.getAllRelationProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                return new RelationAddOperation(treeSection, source,  (EJPluginRelationProperties) dSource, -1);
            }
        }
        return new RelationAddOperation(treeSection, source,  (EJPluginRelationProperties) dSource, -1);
    }
}
