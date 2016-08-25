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
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.core.enumerations.EJItemGroupAlignment;
import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.enumerations.EJSeparatorOrientation;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginInsertScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginQueryScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginUpdateScreenSpacerItemProperties;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockItemContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.form.DisplayItemGroupNode.DisplayItemGroup;
import org.entirej.ide.ui.editors.form.operations.DisplayItemAddOperation;
import org.entirej.ide.ui.editors.form.operations.DisplayItemGroupAddOperation;
import org.entirej.ide.ui.editors.form.operations.DisplayItemGroupRemoveOperation;
import org.entirej.ide.ui.editors.form.wizards.ItemGroupWizard;
import org.entirej.ide.ui.editors.form.wizards.ItemGroupWizardContext;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart.IExtensionValues;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractSubActions;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Movable;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;

public class DisplayItemGroupNode extends AbstractNode<DisplayItemGroup> implements NodeMoveProvider, Neighbor, Movable, NodeOverview
{

    private static final Image          GROUP       = EJUIImages.getImage(EJUIImages.DESC_ITEMS_SCREEN);
    private static final Image          GROUP_ITEM  = EJUIImages.getImage(EJUIImages.DESC_ITEMS_GROUP);
    static final Image                  ITEMS_SPACE = EJUIImages.getImage(EJUIImages.DESC_ITEMS_SPACE);
    private final FormDesignTreeSection treeSection;

    public DisplayItemGroupNode(FormDesignTreeSection treeSection, AbstractNode<?> node, DisplayItemGroup group)
    {
        super(node, group);
        this.treeSection = treeSection;
    }

    public static abstract class DisplayItemGroup
    {

        public AbstractDescriptor<?>[] getNodeDescriptors(FormDesignTreeSection treeSection, DisplayItemGroupNode node)
        {
            return new AbstractDescriptor[0];
        }

        public Image getImage()
        {
            return GROUP;
        }

        public String getName()
        {

            return "null";
        }

        public void addOverview(StyledString styledString)
        {

        }

        public AbstractNode<?>[] getChildren(FormDesignTreeSection treeSection, DisplayItemGroupNode node)
        {

            return (new AbstractNode<?>[0]);
        }

        public boolean isLeaf()
        {
            return false;
        }

        public boolean canMove()
        {
            return false;
        }

        public Object getNeighborSource()
        {
            return this;
        }

        public INodeDeleteProvider getDeleteProvider(FormDesignTreeSection treeSection, DisplayItemGroupNode node)
        {

            return null;
        }

        public INodeRenameProvider getRenameProvider(FormDesignTreeSection treeSection, DisplayItemGroupNode node)
        {

            return null;
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return false;
        }

        public void move(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // ignore

        }

        public abstract AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before);

        public Action[] getActions(FormDesignTreeSection treeSection, AbstractNode<?> patentNode)
        {

            return new Action[0];
        }

        public <S> S getAdapter(Class<S> adapter)
        {

            return null;
        }
    }

    @Override
    public String getName()
    {

        return source.getName();
    }

    @Override
    public String getToolTipText()
    {
        return "display item definitions for given screen.";
    }

    public void addOverview(StyledString styledString)
    {
        source.addOverview(styledString);

    }

    @Override
    public Image getImage()
    {
        return source.getImage();
    }

    @Override
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        return source.getNodeDescriptors(treeSection, this);
    }

    public <S> S getAdapter(Class<S> adapter)
    {
        return source.getAdapter(adapter);
    }

    @Override
    public INodeDeleteProvider getDeleteProvider()
    {

        return source.getDeleteProvider(treeSection, this);
    }

    @Override
    public INodeRenameProvider getRenameProvider()
    {
        return source.getRenameProvider(treeSection, this);
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {

        return source.getChildren(treeSection, this);
    }

    @Override
    public boolean isLeaf()
    {
        return source.isLeaf();
    }

    public boolean canMove(Neighbor relation, Object source)
    {
        return this.source.canMove(relation, source);
    }

    public void move(NodeContext context, Neighbor neighbor, Object source, boolean before)
    {
        this.source.move(context, neighbor, source, before);

    }

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
    {
        return this.source.moveOperation(context, neighbor, source, before);
    }

    @Override
    public Action[] getActions()
    {
        return source.getActions(treeSection, this);
    }

    public static Action createCopyScreenAction(final FormDesignTreeSection treeSection, final EJPluginItemGroupContainer newContainer,
            final EJScreenType[] types)
    {

        return new AbstractSubActions("Copy Screen Layout")
        {

            @Override
            public Action[] getActions()
            {
                Action[] actions = new Action[types.length];

                for (int i = 0; i < types.length; i++)
                {
                    final EJScreenType type = types[i];
                    String name;
                    switch (type)
                    {
                        case INSERT:
                            name = "Insert";
                            break;
                        case MAIN:
                            name = "Main";
                            break;
                        case UPDATE:
                            name = "Update";
                            break;
                        case QUERY:
                            name = "Query";
                            break;

                        default:
                            name = type.name();
                            break;
                    }

                    actions[i] = new Action(name)
                    {
                        @Override
                        public void runWithEvent(Event event)
                        {
                            if (MessageDialog.openQuestion(EJUIPlugin.getActiveWorkbenchShell(), "Copy Screen Layout",
                                    "This will overwrite all screen definitions already defined, are you sure you want to continue?"))
                            {
                                EJPluginItemGroupContainer sourceContainer = newContainer.getBlockProperties().getScreenItemGroupContainer(type);
                                sourceContainer.copyGroupForScreen(newContainer, newContainer.getContainerType());
                                treeSection.refresh((newContainer));
                                treeSection.getEditor().setDirty(true);
                            }
                        }

                    };
                }
                return actions;
            }
        };

    }

    public static abstract class ExtensionDisplayItemGroup extends DisplayItemGroup
    {
        final String                         name;
        final EJPluginItemGroupContainer     container;
        final EJFrameworkExtensionProperties rendereProperties;

        public ExtensionDisplayItemGroup(String name, EJFrameworkExtensionProperties rendereProperties, EJPluginItemGroupContainer container)
        {

            this.name = name;
            this.container = container;
            this.rendereProperties = rendereProperties;
        }

        public abstract EJPropertyDefinitionGroup getDefinitionGroup();

        public abstract void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties, EJPropertyDefinition propertyDefinition);

        @Override
        public boolean canMove(Neighbor relation, Object source)
        {
            return source instanceof ItemGroup;
        }

        @Override
        public Action[] getActions(FormDesignTreeSection treeSection, AbstractNode<?> patentNode)
        {

            List<EJScreenType> types = new ArrayList<EJScreenType>();

            for (EJScreenType type : EJScreenType.values())
            {
                if (container.getScreenType() == type)
                    continue;

                EJPluginItemGroupContainer screenContainer = container.getBlockProperties().getScreenItemGroupContainer(type);
                if (screenContainer == null || screenContainer.isEmpty())
                    continue;

                types.add(type);
            }
            if (types.isEmpty())
                return new Action[] { createNewItemGroupAction(treeSection, patentNode, container,false) ,null,createNewItemGroupAction(treeSection, patentNode, container,true)};

            // create copy actions.
            return new Action[] { createNewItemGroupAction(treeSection, patentNode, container,false), null,createNewItemGroupAction(treeSection, patentNode, container,true),null,
                    createCopyScreenAction(treeSection, container, types.toArray(new EJScreenType[0])) };
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                if (methodNeighbor instanceof ItemGroup)
                    methodNeighbor = ((ItemGroup) methodNeighbor).properties;
                List<EJPluginItemGroupProperties> items = container.getItemGroups();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    container.addItemGroupProperties(index, ((ItemGroup) dSource).properties);
                }
            }
            else
                container.addItemGroupProperties(((ItemGroup) dSource).properties);

        }

        @Override
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                if (methodNeighbor instanceof ItemGroup)
                    methodNeighbor = ((ItemGroup) methodNeighbor).properties;
                List<EJPluginItemGroupProperties> items = container.getItemGroups();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new DisplayItemGroupAddOperation(context.getTreeSection(), container, ((ItemGroup) dSource).properties, index);

                }
            }

            return new DisplayItemGroupAddOperation(context.getTreeSection(), container, ((ItemGroup) dSource).properties, -1);

        }

        @Override
        public AbstractNode<?>[] getChildren(FormDesignTreeSection treeSection, DisplayItemGroupNode node)
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJPluginItemGroupProperties> itemGroups = container.getItemGroups();
            for (EJPluginItemGroupProperties groupProperties : itemGroups)
            {
                ItemGroup itemGroup = new ItemGroup(groupProperties)
                {
                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                            return ExtensionDisplayItemGroup.this.getAdapter(adapter);

                        return super.getAdapter(adapter);

                    }
                };
                nodes.add(new DisplayItemGroupNode(treeSection, node, itemGroup));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors(final FormDesignTreeSection treeSection, final DisplayItemGroupNode node)
        {
            final EJPropertyDefinitionGroup definitionGroup = getDefinitionGroup();
            if (definitionGroup != null)
            {
                AbstractEJFormEditor editor = treeSection.getEditor();
                return PropertyDefinitionGroupPart.createGroupDescriptors(editor, editor.getFormProperties().getEntireJProperties(), definitionGroup,
                        rendereProperties, new IExtensionValues()
                        {

                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                    EJPropertyDefinition propertyDefinition)
                            {
                                propertyDefinition.clearValidValues();
                                loadValidValuesFromExtension(frameworkExtensionProperties, propertyDefinition);

                            }

                            public EJPluginBlockProperties getBlockProperties()
                            {
                                return null;
                            }
                        });
            }
            return new AbstractDescriptor<?>[0];
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean isLeaf()
        {
            return container.isEmpty();
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((container == null) ? 0 : container.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ExtensionDisplayItemGroup other = (ExtensionDisplayItemGroup) obj;
            if (container == null)
            {
                if (other.container != null)
                    return false;
            }
            else if (!container.equals(other.container))
                return false;
            return true;
        }

    }

    public static class MainDisplayItemGroup extends DisplayItemGroup
    {
        final String                       name;
        final EJPluginItemGroupContainer   container;
        final EJPluginMainScreenProperties properties;

        public MainDisplayItemGroup(String name, EJPluginMainScreenProperties properties, EJPluginItemGroupContainer container)
        {
            this.name = name;
            this.container = container;
            this.properties = properties;
        }

        @Override
        public String getName()
        {
            return name;
        }

        @Override
        public boolean isLeaf()
        {
            return container.isEmpty() ;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((properties == null) ? 0 : properties.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            MainDisplayItemGroup other = (MainDisplayItemGroup) obj;
            if (properties == null)
            {
                if (other.properties != null)
                    return false;
            }
            else if (!properties.equals(other.properties))
                return false;
            return true;
        }

        @Override
        public void addOverview(StyledString styledString)
        {
            if (properties.getFrameTitle() != null && properties.getFrameTitle().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(properties.getFrameTitle(), StyledString.COUNTER_STYLER);

            }
        }

        @Override
        public Action[] getActions(FormDesignTreeSection treeSection, AbstractNode<?> patentNode)
        {
            if (container.count() == 0)
            {
                return new Action[] { createNewItemGroupAction(treeSection, patentNode, container,false) };
            }
           

            EJDevBlockRendererDefinition blockRendererDefinition = properties.getBlockProperties().getBlockRendererDefinition();
            if (blockRendererDefinition != null && (blockRendererDefinition.allowMultipleItemGroupsOnMainScreen()))
                return new Action[] { createNewItemGroupAction(treeSection, patentNode, container,false),null, createNewItemGroupAction(treeSection, patentNode, container,true)  };

            return new Action[0];
        }

        @Override
        public boolean canMove(Neighbor relation, Object source)
        {
            return source instanceof ItemGroup;
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                if (methodNeighbor instanceof ItemGroup)
                    methodNeighbor = ((ItemGroup) methodNeighbor).properties;
                List<EJPluginItemGroupProperties> items = container.getItemGroups();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    container.addItemGroupProperties(index, ((ItemGroup) dSource).properties);
                }
            }
            else
                container.addItemGroupProperties(((ItemGroup) dSource).properties);

        }

        @Override
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                if (methodNeighbor instanceof ItemGroup)
                    methodNeighbor = ((ItemGroup) methodNeighbor).properties;
                List<EJPluginItemGroupProperties> items = container.getItemGroups();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new DisplayItemGroupAddOperation(context.getTreeSection(), container, ((ItemGroup) dSource).properties, index);
                }
            }
            return new DisplayItemGroupAddOperation(context.getTreeSection(), container, ((ItemGroup) dSource).properties, -1);
        }

        @Override
        public AbstractNode<?>[] getChildren(FormDesignTreeSection treeSection, DisplayItemGroupNode node)
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJPluginItemGroupProperties> itemGroups = container.getItemGroups();
            for (EJPluginItemGroupProperties groupProperties : itemGroups)
            {
                ItemGroup itemGroup = new ItemGroup(groupProperties)
                {
                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                            return MainDisplayItemGroup.this.getAdapter(adapter);

                        return super.getAdapter(adapter);

                    }

                };
                nodes.add(new DisplayItemGroupNode(treeSection, node, itemGroup)
                {
                    public String getNodeDescriptorDetails()
                    {
                        return "Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-ItemGroups\">here</a> for more information on Item Groups.  All mandatory properties are denoted by \"*\"";

                    };
                });
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors(final FormDesignTreeSection treeSection, final DisplayItemGroupNode node)
        {
            final AbstractEJFormEditor editor = treeSection.getEditor();
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor(
                    "Frame Title",
                    "If the Display Frame property has been set, there will be a frame around the the entire contents of the main screen. The Frame Title will appear on the frame if one has been entered")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    properties.setFrameTitle(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return properties.getFrameTitle();
                }
            };
            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns",
                    "Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on how to use columns to layout your EntireJ Forms")
            {
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
                        properties.setNumCols(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setNumCols(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);

                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getNumCols());
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

            AbstractDescriptor<Boolean> borderDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return properties.getDisplayFrame();
                }

                @Override
                public void setValue(Boolean value)
                {
                    properties.setDisplayFrame(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            borderDescriptor.setText("Display Frame");
            borderDescriptor.setTooltip("If set, a frame will appear around the entire contents of this screen");

            AbstractGroupDescriptor layoutGroupDescriptor = getLayoutDescriptors(properties, treeSection, node, editor);
            return new AbstractDescriptor<?>[] { nameDescriptor, borderDescriptor, colDescriptor, layoutGroupDescriptor };
        }

        public static AbstractGroupDescriptor getLayoutDescriptors(final EJPluginMainScreenProperties properties, final FormDesignTreeSection treeSection,
                final AbstractNode<?> node, final AbstractEJFormEditor editor)
        {
            final AbstractTextDescriptor hSapnDescriptor = new AbstractTextDescriptor(
                    "Horizontal Span",
                    "Inicates how many columns the main screen will span within your form. Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-hspanvspan\">here</a> for more information regarding horizontal and vertical span. To change how the blocks are laid out on your form, change the columns <b>Form Property</b>.")
            {
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
                        properties.setHorizontalSpan(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setHorizontalSpan(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);

                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getHorizontalSpan());
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

            final AbstractTextDescriptor vSapnDescriptor = new AbstractTextDescriptor(
                    "Vertical Span",
                    "Inicates how many rows the main screen will span within your form. Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-hspanvspan\">here</a> for more information regarding horizontal and vertical span. To change how the blocks are laid out on your form, change the columns <b>Form Property</b>.")
            {
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
                        properties.setVerticalSpan(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setVerticalSpan(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);

                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getVerticalSpan());
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

            final AbstractDescriptor<Boolean> hExpandDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return properties.canExpandHorizontally();
                }

                @Override
                public void setValue(Boolean value)
                {
                    properties.setExpandHorizontally(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            hExpandDescriptor.setText("Expand Horizontally");
            hExpandDescriptor.setTooltip("Indicates if the screen should expand, when the form is resized, to fit the available horizontal space");
            final AbstractDescriptor<Boolean> vExpandDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return properties.canExpandVertically();
                }

                @Override
                public void setValue(Boolean value)
                {
                    properties.setExpandVertically(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            vExpandDescriptor.setText("Expand Vertically");
            vExpandDescriptor.setTooltip("Indicates if the screen should expand, when the form is resized, to fit the available vertical space");

            final AbstractTextDescriptor widthHintDescriptor = new AbstractTextDescriptor(
                    "Width",
                    "If the Expand Horizontally property is not set, then you need to indicate the size of your block. Set the width as a unit of <b>Pixels</b>. Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-widthheight\">here</a> for more information")
            {
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
                        properties.setWidth(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setWidth(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getWidth());
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
            final AbstractTextDescriptor heightHintDescriptor = new AbstractTextDescriptor(
                    "Height",
                    "If the Expand Vertically property is not set, then you need to indicate the size of your block. Set the height as a unit of <b>Pixels</b>. Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-widthheight\">here</a> for more information")
            {
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
                        properties.setHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setWidth(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getHeight());
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

            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings",
                    "Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on laying out an EntireJ Form")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { hSapnDescriptor, vSapnDescriptor, hExpandDescriptor, vExpandDescriptor, widthHintDescriptor,
                            heightHintDescriptor };
                }
            };
            return layoutGroupDescriptor;
        }

    }

    public static class ItemGroup extends DisplayItemGroup
    {

        final EJPluginItemGroupProperties properties;

        public ItemGroup(EJPluginItemGroupProperties properties)
        {
            this.properties = properties;
        }

        @Override
        public String getName()
        {
            return properties.getName();
        }

        @Override
        public void addOverview(StyledString styledString)
        {
            if (properties.getFrameTitle() != null && properties.getFrameTitle().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(properties.getFrameTitle(), StyledString.COUNTER_STYLER);

            }
        }

        @Override
        public boolean isLeaf()
        {
            return properties.isSeparator() || properties.isEmpty();
        }

        @Override
        public Image getImage()
        {
            return properties.isSeparator() ?EJUIImages.getImage(EJUIImages.DESC_MENU_SEPARATOR): GROUP_ITEM;
        }

        String toLable(String item)
        {
            StringBuilder builder = new StringBuilder();
            char[] charArray = item.toCharArray();
            boolean onStart = true;
            for (char c : charArray)
            {
                if (onStart)
                {
                    builder.append(String.valueOf(c).toUpperCase());
                    onStart = false;
                    continue;
                }
                if (Character.isUpperCase(c))
                {
                    builder.append(" ");

                }
                builder.append(c);

                if (Character.isDigit(c))
                {
                    builder.append(" ");

                }
            }
            return builder.toString();
        }

        @Override
        public Action[] getActions(FormDesignTreeSection treeSection, AbstractNode<?> patentNode)
        {
            if(properties.isSeparator())
            {
                return new Action[0];
            }
            EJDevBlockRendererDefinition blockRendererDefinition = properties.getParentItemGroupContainer().getContainerType() == EJPluginItemGroupContainer.MAIN_SCREEN ? properties
                    .getBlockProperties().getBlockRendererDefinition() : null;

            if (blockRendererDefinition == null || blockRendererDefinition.allowMultipleItemGroupsOnMainScreen())
                return new Action[] { createaddDisplayItemAction(blockRendererDefinition, treeSection, patentNode, -1),
                        createNewItemGroupAction(treeSection, patentNode, properties.getChildItemGroupContainer(),false),null,createNewItemGroupAction(treeSection, patentNode, properties.getChildItemGroupContainer(),true) };

            return new Action[] { createaddDisplayItemAction(blockRendererDefinition, treeSection, patentNode, -1) };
        }

        public Action createaddDisplayItemAction(final EJDevBlockRendererDefinition blockRendererDefinition, final FormDesignTreeSection treeSection,
                final AbstractNode<?> patentNode, final int index)
        {
            AbstractSubActions abstractSubActions = new AbstractSubActions("Add Display Item")
            {
                private void addDisplayItem(final FormDesignTreeSection treeSection, final AbstractNode<?> patentNode, final EJPluginBlockItemProperties item)
                {
                    int containerType = properties.getParentItemGroupContainer().getContainerType();

                    EJPluginScreenItemProperties itemProperties;
                    switch (containerType)
                    {
                        case EJPluginItemGroupContainer.INSERT_SCREEN:
                            itemProperties = new EJPluginInsertScreenItemProperties(properties, true, false);
                            itemProperties.setEditAllowed(true);
                            break;
                        case EJPluginItemGroupContainer.UPDATE_SCREEN:
                            itemProperties = new EJPluginUpdateScreenItemProperties(properties, true, false);
                            itemProperties.setEditAllowed(true);
                            break;
                        case EJPluginItemGroupContainer.QUERY_SCREEN:
                            itemProperties = new EJPluginQueryScreenItemProperties(properties, true, false);
                            itemProperties.setEditAllowed(true);
                            break;
                        default:
                            itemProperties = new EJPluginMainScreenItemProperties(properties, true, false);
                            if (itemProperties.getBlockProperties().isControlBlock())
                                itemProperties.setEditAllowed(true);
                            else
                            {
                                itemProperties.setReferencedItemName(item.getName());
                                final EJDevBlockItemDisplayProperties blockItemDisplayProperties = itemProperties.getBlockItemDisplayProperties();
                                itemProperties.setEditAllowed((blockItemDisplayProperties != null && blockItemDisplayProperties.getItemRendererDefinition()
                                        .isReadOnly()));
                            }
                            break;
                    }

                    itemProperties.setReferencedItemName(item.getName());
                    itemProperties.setMandatory(item.isMandatoryItem());
                    itemProperties.setLabel(toLable(itemProperties.getName()));
                    itemProperties.setVisible(true);

                    DisplayItemAddOperation addOperation = new DisplayItemAddOperation(treeSection, properties, itemProperties, index);

                    treeSection.getEditor().execute(addOperation);

                }

                String toLable(String item)
                {
                    StringBuilder builder = new StringBuilder();
                    char[] charArray = item.toCharArray();
                    boolean onStart = true;
                    boolean forceUpperCase = false;
                    for (char c : charArray)
                    {
                        if (onStart)
                        {
                            onStart = false;
                            if (c == '_')
                            {
                                forceUpperCase = true;
                                continue;
                            }
                            builder.append(String.valueOf(c).toUpperCase());

                            continue;
                        }
                        if (c == '_')
                        {
                            forceUpperCase = true;
                            builder.append(" ");
                            continue;
                        }

                        if (Character.isUpperCase(c) || Character.isDigit(c))
                        {
                            builder.append(" ");

                        }
                        if (forceUpperCase)
                        {
                            forceUpperCase = false;
                            builder.append(String.valueOf(c).toUpperCase());
                        }
                        else
                        {
                            builder.append(c);
                        }
                        if (Character.isDigit(c))
                        {
                            builder.append(" ");

                        }
                    }
                    return builder.toString().trim();
                }

                @Override
                public Action[] getActions()
                {
                    List<Action> actions = new ArrayList<Action>();

                    final EJPluginBlockProperties blockProperties = properties.getBlockProperties();
                    EJPluginItemGroupContainer itemGroupContainer = blockProperties.getScreenItemGroupContainer(properties.getParentItemGroupContainer()
                            .getScreenType());
                    EJPluginBlockItemContainer container = blockProperties.getItemContainer();
                    for (final EJPluginBlockItemProperties item : container.getAllItemProperties())
                    {
                        if (!itemGroupContainer.containsItemProperties(item.getName(), true))
                        {
                            String rendererName = item.getItemRendererName();

                            if (rendererName != null && rendererName.length() > 0)
                            {

                                actions.add(new Action(String.format("%s : [ %s ]", item.getName(), rendererName))
                                {
                                    @Override
                                    public void runWithEvent(Event event)
                                    {
                                        addDisplayItem(treeSection, patentNode, item);
                                    }

                                });
                            }
                            else
                            {
                                actions.add(new AbstractSubActions(item.getName())
                                {

                                    @Override
                                    public Action[] getActions()
                                    {
                                        List<Action> actions = new ArrayList<Action>();
                                        EJPluginAssignedRendererContainer itemRendererContainer = blockProperties.getFormProperties().getEntireJProperties()
                                                .getItemRendererContainer();
                                        for (final EJPluginRenderer renderer : itemRendererContainer.getAllRenderers())
                                        {

                                            actions.add(new Action(renderer.getAssignedName())
                                            {
                                                @Override
                                                public void runWithEvent(Event event)
                                                {
                                                    item.setItemRendererName(renderer.getAssignedName(), true);
                                                    Object findNode = (item);
                                                    if (findNode != null)
                                                        treeSection.refresh(findNode);
                                                    addDisplayItem(treeSection, patentNode, item);
                                                }

                                            });
                                        }
                                        return actions.toArray(new Action[0]);
                                    }

                                });
                            }
                        }
                    }

                    if (blockRendererDefinition == null || blockRendererDefinition.allowSpacerItems())
                    {
                        if (actions.size() > 0)
                            actions.add(null);
                        actions.add(new Action("Spacer Item")
                        {
                            @Override
                            public void runWithEvent(Event event)
                            {

                                final int containerType = properties.getParentItemGroupContainer().getContainerType();

                                EJPluginScreenItemProperties itemProperties;
                                switch (containerType)
                                {
                                    case EJPluginItemGroupContainer.INSERT_SCREEN:
                                        itemProperties = new EJPluginInsertScreenSpacerItemProperties(properties, true);
                                        break;
                                    case EJPluginItemGroupContainer.UPDATE_SCREEN:
                                        itemProperties = new EJPluginUpdateScreenSpacerItemProperties(properties, true);
                                        break;
                                    case EJPluginItemGroupContainer.QUERY_SCREEN:
                                        itemProperties = new EJPluginQueryScreenSpacerItemProperties(properties, true);
                                        break;
                                    default:
                                        itemProperties = new EJPluginMainScreenSpacerItemProperties(properties, true);
                                        break;
                                }

                                itemProperties.setReferencedItemName("spacer" + properties.getNextAvailableSpacerItemName());
                                itemProperties.setVisible(true);
                                itemProperties.setEditAllowed(false);

                                DisplayItemAddOperation addOperation = new DisplayItemAddOperation(treeSection, properties, itemProperties, index);

                                treeSection.getEditor().execute(addOperation);

                            }
                        });
                    }

                    return actions.toArray(new Action[0]);
                }
            };
            return abstractSubActions;
        }

        @Override
        public boolean canMove()
        {
            return true;
        }

        @Override
        public Object getNeighborSource()
        {
            return this;
        }

        @Override
        public INodeDeleteProvider getDeleteProvider(final FormDesignTreeSection treeSection, final DisplayItemGroupNode node)
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    properties.getParentItemGroupContainer().removeItem(properties);
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(node.getParent());

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {

                    return new DisplayItemGroupRemoveOperation(treeSection, properties.getParentItemGroupContainer(), properties);
                }
            };
        }

        public INodeRenameProvider getRenameProvider(final FormDesignTreeSection treeSection, final DisplayItemGroupNode node)
        {

            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Item Group [%s]", getName()),
                            "Item Group Name", getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Item group name can't be empty.";
                                    if (getName().equals(newText.trim()))
                                        return "";
                                    if (getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (properties.getParentItemGroupContainer().containsItemGroup(newText.trim()))
                                        return "Item group with this name already exists.";
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        String newName = dlg.getValue().trim();
                        properties.setName(newName);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                treeSection.getEditor().setDirty(true);
                                treeSection.refresh(node);

                            }
                        });
                    }

                }
            };

        };

        public boolean isAncestor(Object source)
        {
            if (source instanceof ItemGroup)
            {
                return ((ItemGroup) source).properties.getChildItemGroupContainer().containsItemGroup(properties);

            }
            return false;
        }

        @Override
        public boolean canMove(Neighbor relation, Object source)
        {
            if(properties.isSeparator())
                return false;
            
            return ((relation == null || relation.getNeighborSource() instanceof ItemGroup) && source instanceof ItemGroup && !isAncestor(source))
                    || ((relation == null || relation.getNeighborSource() instanceof EJPluginScreenItemProperties) && source instanceof EJPluginScreenItemProperties);
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (dSource instanceof EJPluginScreenItemProperties)
            {
                if (neighbor != null)
                {
                    Object methodNeighbor = neighbor.getNeighborSource();

                    List<EJPluginScreenItemProperties> items = properties.getItemProperties();
                    if (items.contains(methodNeighbor))
                    {
                        int index = items.indexOf(methodNeighbor);
                        if (!before)
                            index++;

                        properties.addItemProperties(index, (EJPluginScreenItemProperties) dSource);
                    }
                    else
                        properties.addItemProperties((EJPluginScreenItemProperties) dSource);
                }
                else
                    properties.addItemProperties((EJPluginScreenItemProperties) dSource);
                return;
            }

            EJPluginItemGroupContainer container = properties.getChildItemGroupContainer();
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                if (methodNeighbor instanceof ItemGroup)
                    methodNeighbor = ((ItemGroup) methodNeighbor).properties;
                List<EJPluginItemGroupProperties> items = container.getItemGroups();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    container.addItemGroupProperties(index, ((ItemGroup) dSource).properties);
                }
                else
                    container.addItemGroupProperties(((ItemGroup) dSource).properties);
            }
            else
                container.addItemGroupProperties(((ItemGroup) dSource).properties);
        }

        @Override
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (dSource instanceof EJPluginScreenItemProperties)
            {
                if (neighbor != null)
                {
                    Object methodNeighbor = neighbor.getNeighborSource();

                    List<EJPluginScreenItemProperties> items = properties.getItemProperties();
                    if (items.contains(methodNeighbor))
                    {
                        int index = items.indexOf(methodNeighbor);
                        if (!before)
                            index++;

                        return new DisplayItemAddOperation(context.getTreeSection(), properties, (EJPluginScreenItemProperties) dSource, index);
                    }

                }
                return new DisplayItemAddOperation(context.getTreeSection(), properties, (EJPluginScreenItemProperties) dSource, -1);

            }

            EJPluginItemGroupContainer container = properties.getChildItemGroupContainer();
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                if (methodNeighbor instanceof ItemGroup)
                    methodNeighbor = ((ItemGroup) methodNeighbor).properties;
                List<EJPluginItemGroupProperties> items = container.getItemGroups();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new DisplayItemGroupAddOperation(context.getTreeSection(), container, ((ItemGroup) dSource).properties, index);

                }
                return null;

            }

            return new DisplayItemGroupAddOperation(context.getTreeSection(), container, ((ItemGroup) dSource).properties, -1);

        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((properties == null) ? 0 : properties.hashCode());
            return result;
        }

        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
                return true;
            if (obj == null)
                return false;
            if (getClass() != obj.getClass())
                return false;
            ItemGroup other = (ItemGroup) obj;
            if (properties == null)
            {
                if (other.properties != null)
                    return false;
            }
            else if (!properties.equals(other.properties))
                return false;
            return true;
        }

        @Override
        public AbstractNode<?>[] getChildren(FormDesignTreeSection treeSection, DisplayItemGroupNode node)
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJPluginScreenItemProperties> itemProperties = properties.getItemProperties();
            for (EJPluginScreenItemProperties item : itemProperties)
            {
                nodes.add(new DisplayItemNode(treeSection, this, node, item));
            }

            EJPluginItemGroupContainer container = properties.getChildItemGroupContainer();
            List<EJPluginItemGroupProperties> itemGroups = container.getItemGroups();
            for (EJPluginItemGroupProperties groupProperties : itemGroups)
            {
                ItemGroup itemGroup = new ItemGroup(groupProperties)
                {
                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                            return ItemGroup.this.getAdapter(adapter);

                        return super.getAdapter(adapter);

                    }
                };

                nodes.add(new DisplayItemGroupNode(treeSection, node, itemGroup));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors(final FormDesignTreeSection treeSection, final DisplayItemGroupNode node)
        {
            final AbstractEJFormEditor editor = treeSection.getEditor();
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor(
                    "Frame Title",
                    "If the Display Frame property has been set, there will be a frame around the the entire contents of the item group. The Frame Title will appear on the frame if one has been entered")
            {

                @Override
                public void setValue(String value)
                {
                    properties.setFrameTitle(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public String getValue()
                {
                    return properties.getFrameTitle();
                }
            };
            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns",
                    "Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on how to use columns to layout your EntireJ Forms")
            {
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
                        properties.setNumCols(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setNumCols(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);

                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getNumCols());
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

            AbstractDescriptor<Boolean> borderDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return properties.dispayGroupFrame();
                }

                @Override
                public void setValue(Boolean value)
                {
                    properties.setDisplayGroupFrame(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            borderDescriptor.setText("Display Frame");
            borderDescriptor.setTooltip("If set, a frame will appear around the entire contents of this item group");

            final AbstractTextDescriptor hSapnDescriptor = new AbstractTextDescriptor(
                    "Horizontal Span",
                    "Inicates how many columns the item groups will span within your block Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-hspanvspan\">here</a> for more information regarding horizontal and vertical span. To change how the item groups are laid out on your bock change the columns <b>Main Screen Property</b>.")
            {
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
                        properties.setXspan(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setXspan(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);

                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getXspan());
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

            final AbstractTextDescriptor vSapnDescriptor = new AbstractTextDescriptor(
                    "Vertical Span",
                    "Inicates how many rows the item groups will span within your block Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-hspanvspan\">here</a> for more information regarding horizontal and vertical span. To change how the item groups are laid out on your bock change the columns <b>Main Screen Property</b>.")
            {
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
                        properties.setYspan(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setYspan(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);

                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getYspan());
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

            final AbstractDescriptor<Boolean> hExpandDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return properties.canExpandHorizontally();
                }

                @Override
                public void setValue(Boolean value)
                {
                    properties.setExpandHorizontally(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            hExpandDescriptor.setText("Expand Horizontally");
            hExpandDescriptor.setTooltip("Indicates if the item group should expand, when the form is resized, to fit the available horizontal space");
            final AbstractDescriptor<Boolean> vExpandDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return properties.canExpandVertically();
                }

                @Override
                public void setValue(Boolean value)
                {
                    properties.setExpandVertically(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            vExpandDescriptor.setText("Expand Vertically");
            vExpandDescriptor.setTooltip("Indicates if the item group should expand, when the form is resized, to fit the available vertical space");

            final AbstractTextDescriptor widthHintDescriptor = new AbstractTextDescriptor(
                    "Width",
                    "If the Expand Horizontally property is not set, then you need to indicate the size of your item group. Set the width as a unit of <b>Pixels</b>. Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-widthheight\">here</a> for more information")
            {
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
                        properties.setWidth(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setWidth(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getWidth());
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
            final AbstractTextDescriptor heightHintDescriptor = new AbstractTextDescriptor(
                    "Height",
                    "If the Expand Vertically property is not set, then you need to indicate the size of your item group. Set the height as a unit of <b>Pixels</b>. Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form#LayingoutanEntireJForm-widthheight\">here</a> for more information")
            {
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
                        properties.setHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        properties.setWidth(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(properties.getHeight());
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

            final AbstractDropDownDescriptor<EJItemGroupAlignment> hAlignmentDescriptor = new AbstractDropDownDescriptor<EJItemGroupAlignment>(
                    "Horizontal Alignment")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public EJItemGroupAlignment[] getOptions()
                {

                    return EJItemGroupAlignment.values();
                }

                public String getOptionText(EJItemGroupAlignment t)
                {
                    return t.name();
                }

                public void setValue(EJItemGroupAlignment value)
                {
                    properties.setHorizontalAlignment(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                public EJItemGroupAlignment getValue()
                {
                    return properties.getHorizontalAlignment();
                }
            };
            final AbstractDropDownDescriptor<EJItemGroupAlignment> vAlignmentDescriptor = new AbstractDropDownDescriptor<EJItemGroupAlignment>(
                    "Vertical Alignment")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public EJItemGroupAlignment[] getOptions()
                {

                    return EJItemGroupAlignment.values();
                }

                public String getOptionText(EJItemGroupAlignment t)
                {
                    return t.name();
                }

                public void setValue(EJItemGroupAlignment value)
                {
                    properties.setVerticalAlignment(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                public EJItemGroupAlignment getValue()
                {
                    return properties.getVerticalAlignment();
                }
            };

            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings",
                    "Click <a href=\"http://docs.entirej.com/display/EJ1/EntireJ+Screens\">here</a> for more information on laying out an EntireJ Form")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { hSapnDescriptor, vSapnDescriptor, hExpandDescriptor, vExpandDescriptor, widthHintDescriptor,
                            heightHintDescriptor, hAlignmentDescriptor, vAlignmentDescriptor };
                }
            };

            final AbstractDescriptor<?>[] screenRendererDefDescriptors;
            EJPropertyDefinitionGroup definitionGroup = null;
            IExtensionValues values;
            final int containerType = properties.getParentItemGroupContainer().getContainerType();

            switch (containerType)
            {
                case EJPluginItemGroupContainer.INSERT_SCREEN:
                    EJDevInsertScreenRendererDefinition idefinition = properties.getBlockProperties().getInsertScreenRendererDefinition();
                    definitionGroup = idefinition != null ? (idefinition.getItemGroupPropertiesDefinitionGroup()) : null;
                    values = new IExtensionValues()
                    {

                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                EJPropertyDefinition propertyDefinition)
                        {
                            propertyDefinition.clearValidValues();
                            EJDevInsertScreenRendererDefinition definition = properties.getBlockProperties().getInsertScreenRendererDefinition();
                            if (definition != null)
                            {
                                definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                            }
                        }

                        public EJPluginBlockProperties getBlockProperties()
                        {
                            return properties.getBlockProperties();
                        }
                    };
                    break;
                case EJPluginItemGroupContainer.UPDATE_SCREEN:
                    EJDevUpdateScreenRendererDefinition udefinition = properties.getBlockProperties().getUpdateScreenRendererDefinition();
                    definitionGroup = udefinition != null ? (udefinition.getItemGroupPropertiesDefinitionGroup()) : null;
                    values = new IExtensionValues()
                    {

                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                EJPropertyDefinition propertyDefinition)
                        {
                            propertyDefinition.clearValidValues();
                            EJDevUpdateScreenRendererDefinition definition = properties.getBlockProperties().getUpdateScreenRendererDefinition();
                            if (definition != null)
                            {
                                definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                            }
                        }

                        public EJPluginBlockProperties getBlockProperties()
                        {
                            return properties.getBlockProperties();
                        }
                    };
                    break;
                case EJPluginItemGroupContainer.QUERY_SCREEN:
                    EJDevQueryScreenRendererDefinition qdefinition = properties.getBlockProperties().getQueryScreenRendererDefinition();
                    definitionGroup = qdefinition != null ? (qdefinition.getItemGroupPropertiesDefinitionGroup()) : null;
                    values = new IExtensionValues()
                    {

                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                EJPropertyDefinition propertyDefinition)
                        {
                            propertyDefinition.clearValidValues();
                            EJDevQueryScreenRendererDefinition definition = properties.getBlockProperties().getQueryScreenRendererDefinition();
                            if (definition != null)
                            {
                                definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                            }
                        }

                        public EJPluginBlockProperties getBlockProperties()
                        {
                            return properties.getBlockProperties();
                        }
                    };
                    break;
                default:

                    if (properties.getBlockProperties().isUsedInLovDefinition())
                    {
                        final EJPluginLovDefinitionProperties lovDefinition = properties.getBlockProperties().getLovDefinition();
                        EJDevLovRendererDefinition definition = lovDefinition.getRendererDefinition();

                        definitionGroup = definition != null ? (definition.getItemGroupPropertiesDefinitionGroup()) : null;
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
                                return properties.getBlockProperties();
                            }
                        };
                    }
                    else
                    {
                        EJDevBlockRendererDefinition definition = properties.getBlockProperties().getBlockRendererDefinition();

                        definitionGroup = definition != null ? (definition.getItemGroupPropertiesDefinitionGroup()) : null;
                        values = new IExtensionValues()
                        {

                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                    EJPropertyDefinition propertyDefinition)
                            {
                                propertyDefinition.clearValidValues();
                                EJDevBlockRendererDefinition definition = properties.getBlockProperties().getBlockRendererDefinition();
                                if (definition != null)
                                {
                                    definition.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                                }
                            }

                            public EJPluginBlockProperties getBlockProperties()
                            {
                                return properties.getBlockProperties();
                            }
                        };
                    }
                    break;
            }

            
            
           
               
                
                
                AbstractDropDownDescriptor<EJSeparatorOrientation> orientationDescriptor = new AbstractDropDownDescriptor<EJSeparatorOrientation>("Separator Orientation")
                {

                    public EJSeparatorOrientation[] getOptions()
                    {

                        return EJSeparatorOrientation.values();
                    }

                    public String getOptionText(EJSeparatorOrientation t)
                    {
                        return t.toString();
                    }

                    public void setValue(EJSeparatorOrientation value)
                    {
                        properties.setSeparatorOrientation(value);

                      

                        editor.setDirty(true);
                        properties.setExpandVertically(value != EJSeparatorOrientation.HORIZONTAL);
                        properties.setExpandHorizontally(value == EJSeparatorOrientation.HORIZONTAL);
                     treeSection.getDescriptorViewer().showDetails(node);
                        treeSection.refreshPreview();
                        treeSection.refresh(node);
                    }

                    public EJSeparatorOrientation getValue()
                    {
                        return properties.getSeparatorOrientation();
                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }
                };
                AbstractDropDownDescriptor<EJLineStyle> styleDecriptor = new AbstractDropDownDescriptor<EJLineStyle>("Separator Line Style")
                {
                    
                    public EJLineStyle[] getOptions()
                    {
                        
                        return EJLineStyle.values();
                    }
                    
                    public String getOptionText(EJLineStyle t)
                    {
                        return t.toString();
                    }
                    
                    public void setValue(EJLineStyle value)
                    {
                        properties.setSeparatorLineStyle(value);
                        
                        editor.setDirty(true);
                    }
                    
                    public EJLineStyle getValue()
                    {
                        return properties.getSeparatorLineStyle();
                    }
                    
                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);
                        
                    }
                };
                
                
            if(properties.isSeparator())
            {
                return new AbstractDescriptor<?>[] { orientationDescriptor, styleDecriptor, layoutGroupDescriptor };
            }
            
            
            if (definitionGroup != null)
            {
                EJFrameworkExtensionProperties rendererProperties = properties.getRendererProperties();
                if (rendererProperties == null)
                {
                    rendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(properties.getFormProperties(), properties.getBlockProperties(),
                            definitionGroup, null, true);
                    properties.setRendererProperties(rendererProperties);
                }
                screenRendererDefDescriptors = PropertyDefinitionGroupPart.createGroupDescriptors(editor, properties.getBlockProperties()
                        .getEntireJProperties(), definitionGroup, rendererProperties, values);

                if (screenRendererDefDescriptors.length > 0)
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
                            return screenRendererDefDescriptors;

                        }
                    };
                    
                    
                    return new AbstractDescriptor<?>[] { nameDescriptor, borderDescriptor, colDescriptor, layoutGroupDescriptor, rendererGroupDescriptor };

                }
            }

            return new AbstractDescriptor<?>[] { nameDescriptor, borderDescriptor, colDescriptor, layoutGroupDescriptor };
        }

    }

    public boolean canMove()
    {
        return source.canMove();
    }

    public Object getNeighborSource()
    {
        return source.getNeighborSource();
    }

    public static Action createNewItemGroupAction(final FormDesignTreeSection treeSection, final AbstractNode<?> patentNode,
            final EJPluginItemGroupContainer itemGroupContainer ,final boolean separator)
    {

        return new Action(separator?"Add Separator" :"New Item Group")
        {

            @Override
            public void runWithEvent(Event event)
            {
                final ItemGroupWizardContext context = new ItemGroupWizardContext()
                {

                    public boolean hasGroup(String blockName)
                    {
                        return itemGroupContainer.containsItemGroup(blockName);
                    }

                    public void addGroup(String groupName, String title, boolean showFrame, int numCols)
                    {

                        final EJPluginItemGroupProperties itemProperties = new EJPluginItemGroupProperties(groupName, itemGroupContainer);
                        if(separator)
                        {
                            itemProperties.setSeparator(separator);
                           
                            itemProperties.setExpandHorizontally(true);
                            itemProperties.setExpandVertically(false); 
                        }
                        else
                            
                        {
                            itemProperties.setDisplayGroupFrame(showFrame);
                            itemProperties.setFrameTitle(title);
                            itemProperties.setNumCols(numCols);
                            itemProperties.setExpandHorizontally(true);
                            itemProperties.setExpandVertically(true);
                        }
                        itemProperties.setXspan(1);
                        itemProperties.setYspan(1);

                        DisplayItemGroupAddOperation addOperation = new DisplayItemGroupAddOperation(treeSection, itemGroupContainer, itemProperties, -1);
                        treeSection.getEditor().execute(addOperation);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                treeSection.getEditor().setDirty(true);
                                treeSection.refresh(patentNode, true);
                                Object findNode = (new ItemGroup(itemProperties));
                                if (findNode != null)
                                    treeSection.selectNodes(true, findNode);

                            }
                        });


                    }
                };
                if(separator)
                {
                    context.addGroup("Separator", "", false, 1);
                    return;
                }
                ItemGroupWizard wizard = new ItemGroupWizard(context);
                wizard.open();
            }

        };
    }
}
