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
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.dnd.Clipboard;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJItemProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.DisplayItemGroupNode.ItemGroup;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart.IExtensionValues;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Movable;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;

public class DisplayItemNode extends AbstractNode<EJPluginScreenItemProperties> implements Neighbor, Movable, NodeOverview
{

    private final FormDesignTreeSection treeSection;
    private final ItemGroup             group;

    public DisplayItemNode(FormDesignTreeSection treeSection, ItemGroup group, AbstractNode<?> parent, EJPluginScreenItemProperties source)
    {
        super(parent, source);
        this.treeSection = treeSection;
        this.group = group;
    }

    public <S> S getAdapter(Class<S> adapter)
    {
        return group.getAdapter(adapter);
    }

    @Override
    public String getName()
    {
        if (source.isSpacerItem())
            return "<spacer>";
        return source.getName();
    }

    @Override
    public String getNodeDescriptorDetails()
    {
        return "Choose the items from the <b>Block Items</b> that should be displayed on your screen. All mandatory items are denoted by \"*\"";
    }

    public void addOverview(StyledString styledString)
    {
        if (source.getLabel() != null && source.getLabel().length() != 0)
        {
            styledString.append(" : ", StyledString.QUALIFIER_STYLER);
            styledString.append(source.getLabel(), StyledString.COUNTER_STYLER);

        }
        if (source.isMandatory())
        {
            styledString.append(" [ * ] ", StyledString.DECORATIONS_STYLER);
            
        }
    }

    @Override
    public Action[] getActions()
    {
        EJDevBlockRendererDefinition blockRendererDefinition = group.properties.getParentItemGroupContainer().getContainerType() == EJPluginItemGroupContainer.MAIN_SCREEN ? group.properties
                .getBlockProperties().getBlockRendererDefinition() : null;

        int index = group.properties.getItemProperties().indexOf(source);
        if (blockRendererDefinition == null || blockRendererDefinition.allowMultipleItemGroupsOnMainScreen())
            return new Action[] { group.createaddDisplayItemAction(blockRendererDefinition, treeSection, parent.getParent(), ++index),
                    DisplayItemGroupNode.createNewItemGroupAction(treeSection, parent.getParent(), group.properties.getChildItemGroupContainer()), null,
                    createCopySINameAction() };

        return new Action[] { group.createaddDisplayItemAction(blockRendererDefinition, treeSection, parent.getParent(), ++index), null,
                createCopySINameAction() };
    }

    @Override
    public Image getImage()
    {
        if (source.isSpacerItem())
            return DisplayItemGroupNode.ITEMS_SPACE;
        EJItemProperties itemProperties = source.getBlockProperties().getItemProperties(source.getReferencedItemName());
        return (itemProperties != null && itemProperties.isBlockServiceItem()) ? BlockItemsGroupNode.BLOCK : BlockItemsGroupNode.BLOCK_ND;
    }

    public Action createCopySINameAction()
    {

        return new Action("Copy Screen Item Name")
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

    @Override
    public INodeDeleteProvider getDeleteProvider()
    {
        return new INodeDeleteProvider()
        {

            public void delete(boolean cleanup)
            {
                group.properties.deleteItem(source);
                treeSection.getEditor().setDirty(true);
                treeSection.refresh(getParent());
            }
            
            
            public AbstractOperation deleteOperation(boolean cleanup)
            {
                // TODO Auto-generated method stub
                return null;
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
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        final AbstractEJFormEditor editor = treeSection.getEditor();

        final AbstractDescriptor<?>[] screenRendererDefDescriptors;
        EJFrameworkExtensionProperties rendererRequiredProperties = null;
        if ((!(source instanceof EJPluginMainScreenItemProperties)) || !source.getBlockProperties().isUsedInLovDefinition())
        {
            rendererRequiredProperties = source.getBlockRendererRequiredProperties();
        }
        else
        {
            rendererRequiredProperties = ((EJPluginMainScreenItemProperties) source).getLovRendererRequiredProperties();
        }
        if (rendererRequiredProperties != null)
        {
            EJPropertyDefinitionGroup definitionGroup = null;
            IExtensionValues values;
            final int containerType = group.properties.getParentItemGroupContainer().getContainerType();

            switch (containerType)
            {
                case EJPluginItemGroupContainer.INSERT_SCREEN:
                    EJDevInsertScreenRendererDefinition idefinition = source.getBlockProperties().getInsertScreenRendererDefinition();
                    definitionGroup = idefinition != null ? (source.isSpacerItem() ? idefinition.getSpacerItemPropertyDefinitionGroup() : idefinition
                            .getItemPropertyDefinitionGroup()) : null;
                    values = new IExtensionValues()
                    {

                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                EJPropertyDefinition propertyDefinition)
                        {
                            propertyDefinition.clearValidValues();
                            EJDevInsertScreenRendererDefinition definition = source.getBlockProperties().getInsertScreenRendererDefinition();
                            if (definition != null)
                            {
                                definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                            }
                        }

                        public EJPluginBlockProperties getBlockProperties()
                        {
                            return source.getBlockProperties();
                        }
                    };
                    break;
                case EJPluginItemGroupContainer.UPDATE_SCREEN:
                    EJDevUpdateScreenRendererDefinition udefinition = source.getBlockProperties().getUpdateScreenRendererDefinition();
                    definitionGroup = udefinition != null ? (source.isSpacerItem() ? udefinition.getSpacerItemPropertyDefinitionGroup() : udefinition
                            .getItemPropertyDefinitionGroup()) : null;
                    values = new IExtensionValues()
                    {

                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                EJPropertyDefinition propertyDefinition)
                        {
                            propertyDefinition.clearValidValues();
                            EJDevUpdateScreenRendererDefinition definition = source.getBlockProperties().getUpdateScreenRendererDefinition();
                            if (definition != null)
                            {
                                definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                            }
                        }

                        public EJPluginBlockProperties getBlockProperties()
                        {
                            return source.getBlockProperties();
                        }
                    };
                    break;
                case EJPluginItemGroupContainer.QUERY_SCREEN:
                    EJDevQueryScreenRendererDefinition qdefinition = source.getBlockProperties().getQueryScreenRendererDefinition();
                    definitionGroup = qdefinition != null ? (source.isSpacerItem() ? qdefinition.getSpacerItemPropertyDefinitionGroup() : qdefinition
                            .getItemPropertyDefinitionGroup()) : null;
                    values = new IExtensionValues()
                    {

                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                EJPropertyDefinition propertyDefinition)
                        {
                            propertyDefinition.clearValidValues();
                            EJDevQueryScreenRendererDefinition definition = source.getBlockProperties().getQueryScreenRendererDefinition();
                            if (definition != null)
                            {
                                definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                            }
                        }

                        public EJPluginBlockProperties getBlockProperties()
                        {
                            return source.getBlockProperties();
                        }
                    };
                    break;
                default:

                    if (source.getBlockProperties().isUsedInLovDefinition())
                    {
                        final EJPluginLovDefinitionProperties lovDefinition = source.getBlockProperties().getLovDefinition();
                        EJDevLovRendererDefinition definition = lovDefinition.getRendererDefinition();

                        definitionGroup = definition != null ? (source.isSpacerItem() ? definition.getSpacerItemPropertiesDefinitionGroup() : definition
                                .getItemPropertiesDefinitionGroup()) : null;
                        values = new IExtensionValues()
                        {

                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                    EJPropertyDefinition propertyDefinition)
                            {
                                propertyDefinition.clearValidValues();
                                EJDevLovRendererDefinition definition = lovDefinition.getRendererDefinition();
                                if (definition != null)
                                {
                                    definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                                }
                            }

                            public EJPluginBlockProperties getBlockProperties()
                            {
                                return source.getBlockProperties();
                            }
                        };
                    }
                    else
                    {
                        EJDevBlockRendererDefinition definition = source.getBlockProperties().getBlockRendererDefinition();

                        definitionGroup = definition != null ? (source.isSpacerItem() ? definition.getSpacerItemPropertiesDefinitionGroup() : definition
                                .getItemPropertiesDefinitionGroup()) : null;
                        values = new IExtensionValues()
                        {

                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                    EJPropertyDefinition propertyDefinition)
                            {
                                propertyDefinition.clearValidValues();
                                EJDevBlockRendererDefinition definition = source.getBlockProperties().getBlockRendererDefinition();
                                if (definition != null)
                                {
                                    definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                                }
                            }

                            public EJPluginBlockProperties getBlockProperties()
                            {
                                return source.getBlockProperties();
                            }
                        };
                    }
                    break;
            }

            if (definitionGroup != null)
            {
                screenRendererDefDescriptors = PropertyDefinitionGroupPart.createGroupDescriptors(editor, source.getBlockProperties().getEntireJProperties(),
                        definitionGroup, rendererRequiredProperties, values);
            }
            else
            {
                screenRendererDefDescriptors = new AbstractDescriptor<?>[0];
            }

        }
        else
        {
            screenRendererDefDescriptors = new AbstractDescriptor<?>[0];
        }
        if (source.isSpacerItem())
        {
            return screenRendererDefDescriptors;
        }
        List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();

        AbstractTextDescriptor labelDescriptor = new AbstractTextDescriptor(
                "Label",
                "An items label is displayed either before or after the item. Always allow one extra column for the items lable when calculating the number of required columns in your item groupF")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(String value)
            {
                source.setLabel(value);
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);
            }

            @Override
            public String getValue()
            {
                return source.getLabel();
            }
        };

        AbstractTextDescDescriptor hintDescriptor = new AbstractTextDescDescriptor("Hint",
                "The hint is displayed when the user hovers the mouse over this item")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(String value)
            {
                source.setHint(value);
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);
            }

            @Override
            public String getValue()
            {
                return source.getHint();
            }
        };

        AbstractTextDescriptor refItemDescriptor = new AbstractTextDescriptor("Block Item",
                "The block item that this screen item represents. Click the label to navigate to the block item")
        {

            @Override
            public void setValue(String value)
            {
            }
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public String getValue()
            {
                return source.getReferencedItemName();
            }

            @Override
            public void addEditorAssist(Control control)
            {
                if (control instanceof Text)
                {
                    ((Text) control).setEditable(false);
                }
            }

            @Override
            public boolean hasLableLink()
            {
                return true;
            }

            @Override
            public String lableLinkActivator()
            {
                String ref = source.getReferencedItemName();
                EJItemProperties item = source.getBlockProperties().getItemProperties(ref);

                AbstractNode<?> findNode = treeSection.findNode(source.getBlockProperties().getItemContainer());
                if (item != null && findNode != null)
                {
                    treeSection.selectNodes(false, findNode);
                    treeSection.expand(findNode);
                    treeSection.selectNodes(true, treeSection.findNode(item));
                }
                return ref;
            }
        };

        final AbstractBooleanDescriptor visiableDescriptor = new AbstractBooleanDescriptor(
                "Visible",
                "Indicates if the item is visible to the user. If you would like to show/hide items from the user at runtime, add them to the screen and set them to be non visible. You can then make the item visible via the forms Action Processor")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(Boolean value)
            {
                source.setVisible(value.booleanValue());
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);

            }

            @Override
            public Boolean getValue()
            {
                return source.isVisible();
            }
        };

        final AbstractBooleanDescriptor editableDescriptor = new AbstractBooleanDescriptor("Editable",
                "Indicates if the item can be modified by the user directly on the main screen. Note: This option is only available for <b>Control Blocks</b>")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(Boolean value)
            {
                source.setEditAllowed(value.booleanValue());
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);

            }

            @Override
            public Boolean getValue()
            {
                return source.isEditAllowed();
            }
        };

        final AbstractBooleanDescriptor mandatoryDescriptor = new AbstractBooleanDescriptor("Mandatory",
                "Indicates if the item is mandatory. Mandatory items will be marked during runtime according to the client framework you are using")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(Boolean value)
            {
                source.setMandatory(value.booleanValue());
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);

            }

            @Override
            public Boolean getValue()
            {
                return source.isMandatory();
            }
        };
        final AbstractBooleanDescriptor enableLovDescriptor = new AbstractBooleanDescriptor(
                "Enable LOV",
                "If you choose an LOV Mapping, then this property will automatically be set. However, if you would like to add non-standard lov code to be fired, then set this property and implement the lovActivated method within your forms Action Processor")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(Boolean value)
            {
                source.enableLovNotification(value.booleanValue());
                if (!value)
                {
                    source.setLovMappingName("");
                }
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);
                treeSection.showNodeDetails(DisplayItemNode.this);
            }

            @Override
            public Boolean getValue()
            {
                return source.isLovNotificationEnabled();
            }
        };

        final AbstractTextDropDownDescriptor lovMapDescriptor = new AbstractTextDropDownDescriptor(
                "LOV Mapping",
                "If the item should receive it's value from an LOV, then you should choose the LOV here. If the Validate From LOV property has been set, then only values fro this LOV will be allowed for the item. Click <a href=\"http://docs.entirej.com/display/EJ1/Working+with+LOV%27s\">here</a> for more information on LOVs")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            public String[] getOptions()
            {
                List<String> options = new ArrayList<String>();
                List<EJPluginLovMappingProperties> lovMappingProperties = source.getBlockProperties().getLovMappingContainer().getAllLovMappingProperties();

                options.add("");
                for (EJPluginLovMappingProperties lovProp : lovMappingProperties)
                {
                    options.add(lovProp.getName());
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
                source.setLovMappingName(value);
                source.enableLovNotification(value != null && value.trim().length() != 0);
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);
                treeSection.showNodeDetails(DisplayItemNode.this);
            }

            @Override
            public String getValue()
            {
                return source.getLovMappingName();
            }

            @Override
            public boolean hasLableLink()
            {
                return true;
            }

            @Override
            public String lableLinkActivator()
            {
                String ref = source.getLovMappingName();
                EJPluginLovMappingProperties item = source.getBlockProperties().getLovMappingContainer().getLovMappingProperties(ref);

                AbstractNode<?> findNode = treeSection.findNode(source.getBlockProperties().getLovMappingContainer());
                if (item != null && findNode != null)
                {
                    treeSection.selectNodes(false, findNode);
                    treeSection.expandNodes();
                    treeSection.selectNodes(true, treeSection.findNode(item));
                }
                return ref;
            }
        };

        final AbstractBooleanDescriptor validateFromLovDescriptor = new AbstractBooleanDescriptor(
                "Validate From LOV",
                "If set, EntireJ will force the user to enter a value in the item that exists within the LOV. If not set, then the user can enter anything in the item or choose something from the LOV mapped to this item")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(Boolean value)
            {
                source.setValidateFromLov(value.booleanValue());
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);

            }

            @Override
            public Boolean getValue()
            {
                return source.validateFromLov();
            }
        };

        final EJDevBlockItemDisplayProperties blockItemDisplayProperties = source.getBlockItemDisplayProperties();

        final AbstractTextDescriptor actionIdDescriptor = new AbstractTextDescriptor(
                "Action Command",
                "If enabled, you can add an action command to a creen item. The item renderer will indicate if an action command is possible. If it is, then the renderer will send the command your forms Action Processors <b>executeActionCommand</b> method when the change event fires or when the item renderer requires it")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            @Override
            public void setValue(String value)
            {
                source.setActionCommand(value);
                editor.setDirty(true);
                treeSection.refresh(DisplayItemNode.this);
            }

            @Override
            public String getValue()
            {
                return source.getActionCommand();
            }

            @Override
            public void addEditorAssist(Control control)
            {
                if (blockItemDisplayProperties == null || blockItemDisplayProperties.getItemRendererDefinition() ==null || !blockItemDisplayProperties.getItemRendererDefinition().canExecuteActionCommand())
                {
                    (control).setEnabled(false);
                }
            }
        };

        AbstractGroupDescriptor displayGroupDescriptor = new AbstractGroupDescriptor("Display Settings")
        {
            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);
                
            }
            public AbstractDescriptor<?>[] getDescriptors()
            {
                if (group.properties.getParentItemGroupContainer().getContainerType() == EJPluginItemGroupContainer.MAIN_SCREEN
                        && (!(source.getBlockProperties().isControlBlock() || (blockItemDisplayProperties != null && blockItemDisplayProperties
                                .getItemRendererDefinition().isReadOnly()))))

                    return new AbstractDescriptor<?>[] { visiableDescriptor, mandatoryDescriptor, actionIdDescriptor, lovMapDescriptor, enableLovDescriptor,
                            validateFromLovDescriptor };

                return new AbstractDescriptor<?>[] { visiableDescriptor, editableDescriptor, mandatoryDescriptor, actionIdDescriptor, lovMapDescriptor,
                        enableLovDescriptor, validateFromLovDescriptor };
            }
        };
        descriptors.add(refItemDescriptor);
        descriptors.add(labelDescriptor);
        descriptors.add(hintDescriptor);

        descriptors.add(displayGroupDescriptor);

        // try to load renderer group
        if (screenRendererDefDescriptors.length > 0)
        {

            AbstractGroupDescriptor rendererGroupDescriptor = new AbstractGroupDescriptor("Screen Renderer Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return screenRendererDefDescriptors;

                }
            };
            descriptors.add(rendererGroupDescriptor);

        }

        if (blockItemDisplayProperties != null)
        {
            final EJFrameworkExtensionProperties rendereProperties = blockItemDisplayProperties.getItemRendererProperties();
            EJDevItemRendererDefinition itemRendererDefinition = blockItemDisplayProperties.getItemRendererDefinition();

            // try to load renderer group
            if (rendereProperties != null && itemRendererDefinition != null)
            {

                final EJPropertyDefinitionGroup definitionGroup = itemRendererDefinition.getItemPropertyDefinitionGroup();
                if (definitionGroup != null)
                {

                    AbstractGroupDescriptor rendererGroupDescriptor = new AbstractGroupDescriptor("Item Renderer Settings",
                            "Item Renderer properties are valid for all block screens.")
                    {
                        @Override
                        public boolean isExpand()
                        {
                            return false;
                        }
                        @Override
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);
                            
                        }
                        public AbstractDescriptor<?>[] getDescriptors()
                        {
                            return PropertyDefinitionGroupPart.createGroupDescriptors(editor, source.getBlockProperties().getEntireJProperties(),
                                    definitionGroup, rendereProperties, new IExtensionValues()
                                    {

                                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                                EJPropertyDefinition propertyDefinition)
                                        {
                                            propertyDefinition.clearValidValues();
                                            blockItemDisplayProperties.getItemRendererDefinition().loadValidValuesForProperty(frameworkExtensionProperties,
                                                    propertyDefinition);

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
}
