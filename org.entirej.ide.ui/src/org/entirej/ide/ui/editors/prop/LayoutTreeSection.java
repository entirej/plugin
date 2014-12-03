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
package org.entirej.ide.ui.editors.prop;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.properties.EJCoreLayoutContainer;
import org.entirej.framework.core.properties.EJCoreLayoutItem;
import org.entirej.framework.core.properties.EJCoreLayoutItem.FILL;
import org.entirej.framework.core.properties.EJCoreLayoutItem.GRAB;
import org.entirej.framework.core.properties.EJCoreLayoutItem.ItemContainer;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutComponent;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.LayoutSpace;
import org.entirej.framework.core.properties.EJCoreLayoutItem.SplitGroup;
import org.entirej.framework.core.properties.EJCoreLayoutItem.SplitGroup.ORIENTATION;
import org.entirej.framework.core.properties.EJCoreLayoutItem.TYPE;
import org.entirej.framework.core.properties.EJCoreLayoutItem.TabGroup;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevAppComponentRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Movable;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;

public class LayoutTreeSection extends AbstractNodeTreeSection
{
    private final EJPropertiesEditor editor;
    private LayoutPreviewer          layoutPreviewer;
    private TYPE[]                   supportedLayoutTypes = TYPE.values();

    public LayoutTreeSection(EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent);
        this.editor = editor;
        initTree();
        addDnDSupport(null);// no root move need in layout
    }

    @Override
    protected void nodesUpdated()
    {
        editor.setDirty(true);
    }

    @Override
    protected void buildBody(Section section, FormToolkit toolkit)
    {
        super.buildBody(section, toolkit);
        GridData sectionData = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
        sectionData.widthHint = 300;
        section.setLayoutData(sectionData);
    }

    boolean isSupportLayoutSettings()
    {
        for (TYPE type : supportedLayoutTypes)
        {
            switch (type)
            {
                case GROUP:
                   return true;
            }
        }
        return false;
    }
    
    @Override
    public Object getTreeInput()
    {
        return new Object();
    }

    public void setLayoutPreviewer(LayoutPreviewer layoutPreviewer)
    {
        this.layoutPreviewer = layoutPreviewer;
    }

    @Override
    public String getSectionTitle()
    {
        return "Layout Setup";
    }

    @Override
    public String getSectionDescription()
    {

        return "Define layout of the application in the following section.";
    }

    @Override
    public Action[] getBaseActions()
    {

        return new Action[] {};
    }

    @Override
    public void refresh()
    {
        EJApplicationDefinition applicationManager = editor.getEntireJProperties().getApplicationManager();
        if (applicationManager != null)
        {

            supportedLayoutTypes = applicationManager.getSupportedLayoutTypes();

        }
        else
        {
            supportedLayoutTypes = TYPE.values();
        }
        super.refresh();
    }

    @Override
    public AbstractNodeContentProvider getContentProvider()
    {
        return new AbstractNodeContentProvider()
        {

            public Object[] getElements(Object inputElement)
            {

                EJCoreLayoutContainer container = editor.getEntireJProperties().getLayoutContainer();

                return new Object[] { new LayoutContainerNode(container) };
            }
        };
    }

    private void refreshPreview()
    {
        if (layoutPreviewer != null)
            Display.getDefault().asyncExec(new Runnable()
            {

                public void run()
                {
                    layoutPreviewer.refresh();

                }
            });
    }

    private Action createNewGroupAction(final ItemContainer container)
    {

        return new Action("New Group")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJCoreLayoutItem.LayoutGroup group = new LayoutGroup();
                container.addItem(group);
                editor.setDirty(true);
                AbstractNode<?> parent = findNode(container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, findNode(group));
                refreshPreview();
            }

        };
    }

    private Action createNewSplitGroupAction(final ItemContainer container)
    {

        return new Action("New Split")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJCoreLayoutItem.SplitGroup group = new SplitGroup();
                container.addItem(group);
                editor.setDirty(true);
                AbstractNode<?> parent = findNode(container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, findNode(group));
                refreshPreview();
            }

        };
    }

    private Action createNewTabGroupAction(final ItemContainer container)
    {

        return new Action("New Tab")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJCoreLayoutItem.TabGroup group = new TabGroup();
                container.addItem(group);
                editor.setDirty(true);
                AbstractNode<?> parent = findNode(container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, findNode(group));
                refreshPreview();
            }

        };
    }

    private Action createNewSpaceAction(final ItemContainer container)
    {

        return new Action("New Space")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJCoreLayoutItem.LayoutSpace group = new LayoutSpace();
                container.addItem(group);
                editor.setDirty(true);
                AbstractNode<?> parent = findNode(container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, findNode(group));
                refreshPreview();
            }

        };
    }

    private Action createNewCompAction(final ItemContainer container)
    {

        return new Action("New Component")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJCoreLayoutItem.LayoutComponent group = new LayoutComponent();
                container.addItem(group);
                editor.setDirty(true);
                AbstractNode<?> parent = findNode(container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, findNode(group));
                refreshPreview();
            }

        };
    }

    private class LayoutContainerNode extends AbstractNode<EJCoreLayoutContainer> implements NodeMoveProvider
    {
        private final Image         MAIN = EJUIImages.getImage(EJUIImages.DESC_LAYOUT_MAIN);
        final EJCoreLayoutContainer container;

        public LayoutContainerNode(EJCoreLayoutContainer source)
        {
            super(null, source);
            container = source;
        }

        @Override
        public Image getImage()
        {
            return MAIN;
        }

        @Override
        public String getName()
        {

            return "Container";
        }

        @Override
        public String getToolTipText()
        {
            return "Defined application container setup.";
        }

        @Override
        public boolean isLeaf()
        {
            return source.getItems().size() == 0;
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setTitle(value);
                    editor.setDirty(true);
                    refresh(LayoutContainerNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getTitle();
                }
            };
            nameDescriptor.setRequired(true);

            AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Width")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setWidth(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setWidth(0);
                    }
                    editor.setDirty(true);
                    refresh(LayoutContainerNode.this);
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getWidth());
                }

                @Override
                public void addEditorAssist(Control control)
                {

                    ((Text) control).addVerifyListener(new EJPluginEntireJNumberVerifier());

                    super.addEditorAssist(control);
                }
            };

            AbstractTextDescriptor heightDescriptor = new AbstractTextDescriptor("Height")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setWidth(0);
                    }
                    editor.setDirty(true);
                    refresh(LayoutContainerNode.this);
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getHeight());
                }

                @Override
                public void addEditorAssist(Control control)
                {

                    ((Text) control).addVerifyListener(new EJPluginEntireJNumberVerifier());

                    super.addEditorAssist(control);
                }
            };

            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setColumns(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setColumns(1);
                    }
                    editor.setDirty(true);
                    refresh(LayoutContainerNode.this);
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getColumns());
                }

                @Override
                public void addEditorAssist(Control control)
                {

                    ((Text) control).addVerifyListener(new EJPluginEntireJNumberVerifier());

                    super.addEditorAssist(control);
                }
            };

            
            if(isSupportLayoutSettings())
            {

                return new AbstractDescriptor<?>[] { nameDescriptor, widthDescriptor, heightDescriptor, colDescriptor };
            }
            else
            {

                return new AbstractDescriptor<?>[] { nameDescriptor, widthDescriptor, heightDescriptor };
            }
        }

        @Override
        public Action[] getActions()
        {
            List<Action> actions = new ArrayList<Action>();

            for (TYPE type : supportedLayoutTypes)
            {
                switch (type)
                {
                    case GROUP:
                        actions.add(createNewGroupAction(source));
                        break;
                    case COMPONENT:
                        actions.add(createNewCompAction(source));
                        break;
                    case SPLIT:
                        actions.add(createNewSplitGroupAction(source));
                        break;
                    case TAB:
                        actions.add(createNewTabGroupAction(source));
                        break;
                    case SPACE:

                        actions.add(createNewSpaceAction(source));
                        break;
                }
            }

            return actions.toArray(new Action[0]);
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJCoreLayoutItem> items = source.getItems();
            for (EJCoreLayoutItem layoutItem : items)
            {
                switch (layoutItem.getType())
                {
                    case GROUP:
                        nodes.add(new GroupNode(this, (LayoutGroup) layoutItem));
                        break;
                    case SPACE:
                        nodes.add(new SpaceNode(this, (LayoutSpace) layoutItem));
                        break;
                    case COMPONENT:
                        nodes.add(new ComponentNode(this, (LayoutComponent) layoutItem));
                        break;
                    case SPLIT:
                        nodes.add(new SplitNode(this, (SplitGroup) layoutItem));
                        break;
                    case TAB:
                        nodes.add(new TabNode(this, (TabGroup) layoutItem));
                        break;
                }
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return true;
        }

        public void move(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJCoreLayoutItem> items = container.getItems();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    container.addItem(index, (EJCoreLayoutItem) source);
                }
            }
            else
                container.addItem((EJCoreLayoutItem) source);

        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }

    }

    private class SpaceNode extends AbstractLayoutItem
    {
        private final Image IMG_SPACE = EJUIImages.getImage(EJUIImages.DESC_MENU_SEPARATOR);

        public SpaceNode(AbstractNode<?> parent, EJCoreLayoutItem.LayoutSpace source)
        {
            super(parent, source);
        }

        @Override
        public Image getImage()
        {
            return IMG_SPACE;
        }

        @Override
        public String getName()
        {
            return "<space>";
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if(isSupportLayoutSettings())
            {
                
               return getItemDescriptors().toArray(new AbstractDescriptor<?>[0]);
            }
            
            return new AbstractDescriptor<?>[0];
        }
    }

    private class GroupNode extends AbstractLayoutItem implements NodeMoveProvider
    {
        private final EJCoreLayoutItem.LayoutGroup group;
        private final Image                        IMG_GROUP = EJUIImages.getImage(EJUIImages.DESC_LAYOUT_GROUP);

        public GroupNode(AbstractNode<?> parent, EJCoreLayoutItem.LayoutGroup source)
        {
            super(parent, source);
            this.group = source;
        }

        @Override
        public String getName()
        {
            if (group.getTitle() != null && group.getTitle().length() > 0)
            {
                return group.getTitle();
            }
            return "<group>";
        }

        @Override
        public Image getImage()
        {
            return IMG_GROUP;
        }

        @Override
        public boolean isLeaf()
        {
            return group.getItems().size() == 0;
        }

        public boolean isAncestor(Object source)
        {
            if (source instanceof ItemContainer)
            {
                return isAncestor((ItemContainer) source, group);

            }
            return false;
        }

        public boolean isAncestor(ItemContainer container, Object s)
        {
            List<EJCoreLayoutItem> items = container.getItems();
            for (EJCoreLayoutItem item : items)
            {
                if (s.equals(item))
                    return true;
                if (item instanceof ItemContainer)
                {
                    boolean ancestor = isAncestor((ItemContainer) item, s);
                    if (ancestor)
                        return ancestor;
                }
            }

            return false;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJCoreLayoutItem> items = group.getItems();
            for (EJCoreLayoutItem layoutItem : items)
            {
                switch (layoutItem.getType())
                {
                    case GROUP:
                        nodes.add(new GroupNode(this, (LayoutGroup) layoutItem));
                        break;
                    case SPACE:
                        nodes.add(new SpaceNode(this, (LayoutSpace) layoutItem));
                        break;
                    case COMPONENT:
                        nodes.add(new ComponentNode(this, (LayoutComponent) layoutItem));
                        break;
                    case SPLIT:
                        nodes.add(new SplitNode(this, (SplitGroup) layoutItem));
                        break;
                    case TAB:
                        nodes.add(new TabNode(this, (TabGroup) layoutItem));
                        break;
                }
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public Action[] getActions()
        {
            List<Action> actions = new ArrayList<Action>();

            for (TYPE type : supportedLayoutTypes)
            {
                switch (type)
                {
                    case GROUP:
                        actions.add(createNewGroupAction(group));
                        break;
                    case COMPONENT:
                        actions.add(createNewCompAction(group));
                        break;
                    case SPLIT:
                        actions.add(createNewSplitGroupAction(group));
                        break;
                    case TAB:
                        actions.add(createNewTabGroupAction(group));
                        break;
                    case SPACE:

                        actions.add(createNewSpaceAction(group));
                        break;
                }
            }

            return actions.toArray(new Action[0]);
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Group Name")
            {

                @Override
                public void setValue(String value)
                {
                    group.setTitle(value);
                    editor.setDirty(true);
                    refresh(GroupNode.this);
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return group.getTitle();
                }
            };
            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        group.setColumns(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        group.setColumns(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(GroupNode.this);
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(group.getColumns());
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
                public Boolean getValue()
                {
                    return group.isBorder();
                }

                @Override
                public void setValue(Boolean value)
                {
                    group.setBorder(value.booleanValue());
                    editor.setDirty(true);
                    refresh(GroupNode.this);
                    refreshPreview();
                }

            };
            borderDescriptor.setText("Group Border");
            AbstractDescriptor<Boolean> hideMarginDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {

                @Override
                public Boolean getValue()
                {
                    return group.isHideMargin();
                }

                @Override
                public void setValue(Boolean value)
                {
                    group.setHideMargin(value.booleanValue());
                    editor.setDirty(true);
                    refresh(GroupNode.this);
                    refreshPreview();
                }

            };
            hideMarginDescriptor.setText("Hide Margin");

            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return getItemDescriptors().toArray(new AbstractDescriptor<?>[0]);
                }
            };
            return new AbstractDescriptor<?>[] { nameDescriptor, borderDescriptor, hideMarginDescriptor, colDescriptor, layoutGroupDescriptor };
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !isAncestor(source);
        }

        public void move(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJCoreLayoutItem> items = group.getItems();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    group.addItem(index, (EJCoreLayoutItem) source);
                }
            }
            else
                group.addItem((EJCoreLayoutItem) source);

        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private class SplitNode extends AbstractLayoutItem implements NodeMoveProvider
    {
        private final EJCoreLayoutItem.SplitGroup group;
        private final Image                       IMG_GROUP = EJUIImages.getImage(EJUIImages.DESC_LAYOUT_SPLIT);

        public SplitNode(AbstractNode<?> parent, EJCoreLayoutItem.SplitGroup source)
        {
            super(parent, source);
            this.group = source;
        }

        @Override
        public String getName()
        {

            return "<split>";
        }

        @Override
        public Image getImage()
        {
            return IMG_GROUP;
        }

        @Override
        public boolean isLeaf()
        {
            return group.getItems().size() == 0;
        }

        public boolean isAncestor(Object source)
        {
            if (source instanceof ItemContainer)
            {
                return isAncestor((ItemContainer) source, group);

            }
            return false;
        }

        public boolean isAncestor(ItemContainer container, Object s)
        {
            List<EJCoreLayoutItem> items = container.getItems();
            for (EJCoreLayoutItem item : items)
            {
                if (s.equals(item))
                    return true;
                if (item instanceof ItemContainer)
                {
                    boolean ancestor = isAncestor((ItemContainer) item, s);
                    if (ancestor)
                        return ancestor;
                }
            }

            return false;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJCoreLayoutItem> items = group.getItems();
            for (EJCoreLayoutItem layoutItem : items)
            {
                switch (layoutItem.getType())
                {
                    case GROUP:
                        nodes.add(new GroupNode(this, (LayoutGroup) layoutItem));
                        break;
                    case SPACE:
                        nodes.add(new SpaceNode(this, (LayoutSpace) layoutItem));
                        break;
                    case COMPONENT:
                        nodes.add(new ComponentNode(this, (LayoutComponent) layoutItem));
                        break;
                    case SPLIT:
                        nodes.add(new SplitNode(this, (SplitGroup) layoutItem));
                        break;
                    case TAB:
                        nodes.add(new TabNode(this, (TabGroup) layoutItem));
                        break;
                }
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public Action[] getActions()
        {
            List<Action> actions = new ArrayList<Action>();

            for (TYPE type : supportedLayoutTypes)
            {
                switch (type)
                {
                    case GROUP:
                        actions.add(createNewGroupAction(group));
                        break;
                    case COMPONENT:
                        actions.add(createNewCompAction(group));
                        break;
                    case SPLIT:
                        actions.add(createNewSplitGroupAction(group));
                        break;
                    case TAB:
                        actions.add(createNewTabGroupAction(group));
                        break;
                    case SPACE:

                        actions.add(createNewSpaceAction(group));
                        break;
                }
            }

            return actions.toArray(new Action[0]);
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractDropDownDescriptor<ORIENTATION> orientationDescriptor = new AbstractDropDownDescriptor<ORIENTATION>("Orientation")
            {

                public ORIENTATION[] getOptions()
                {

                    return ORIENTATION.values();
                }

                public String getOptionText(ORIENTATION t)
                {
                    return t.name();
                }

                public void setValue(ORIENTATION value)
                {
                    group.setOrientation(value);
                    editor.setDirty(true);
                    refresh(SplitNode.this);
                    refreshPreview();
                }

                public ORIENTATION getValue()
                {
                    return group.getOrientation();
                }
            };

            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return getItemDescriptors().toArray(new AbstractDescriptor<?>[0]);
                }
            };
            
            if(isSupportLayoutSettings())
            {

                return new AbstractDescriptor<?>[] { orientationDescriptor, layoutGroupDescriptor };
            }
            else
            {

                return new AbstractDescriptor<?>[] { orientationDescriptor };
            }
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !isAncestor(source);
        }

        public void move(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJCoreLayoutItem> items = group.getItems();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    group.addItem(index, (EJCoreLayoutItem) source);
                }
            }
            else
                group.addItem((EJCoreLayoutItem) source);

        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private class TabNode extends AbstractLayoutItem implements NodeMoveProvider
    {
        private final EJCoreLayoutItem.TabGroup group;
        private final Image                     IMG_GROUP = EJUIImages.getImage(EJUIImages.DESC_LAYOUT_TAB);

        public TabNode(AbstractNode<?> parent, EJCoreLayoutItem.TabGroup source)
        {
            super(parent, source);
            this.group = source;
        }

        @Override
        public String getName()
        {

            return "<tab>";
        }

        @Override
        public Image getImage()
        {
            return IMG_GROUP;
        }

        @Override
        public boolean isLeaf()
        {
            return group.getItems().size() == 0;
        }

        public boolean isAncestor(Object source)
        {
            if (source instanceof ItemContainer)
            {
                return isAncestor((ItemContainer) source, group);

            }
            return false;
        }

        public boolean isAncestor(ItemContainer container, Object s)
        {
            List<EJCoreLayoutItem> items = container.getItems();
            for (EJCoreLayoutItem item : items)
            {
                if (s.equals(item))
                    return true;
                if (item instanceof ItemContainer)
                {
                    boolean ancestor = isAncestor((ItemContainer) item, s);
                    if (ancestor)
                        return ancestor;
                }
            }

            return false;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJCoreLayoutItem> items = group.getItems();
            for (EJCoreLayoutItem layoutItem : items)
            {
                switch (layoutItem.getType())
                {
                    case GROUP:
                        nodes.add(new GroupNode(this, (LayoutGroup) layoutItem));
                        break;
                    case SPACE:
                        nodes.add(new SpaceNode(this, (LayoutSpace) layoutItem));
                        break;
                    case COMPONENT:
                        nodes.add(new ComponentNode(this, (LayoutComponent) layoutItem));
                        break;
                    case SPLIT:
                        nodes.add(new SplitNode(this, (SplitGroup) layoutItem));
                        break;
                    case TAB:
                        nodes.add(new TabNode(this, (TabGroup) layoutItem));
                        break;
                }
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public Action[] getActions()
        {
            List<Action> actions = new ArrayList<Action>();

            for (TYPE type : supportedLayoutTypes)
            {
                switch (type)
                {
                    case GROUP:
                        actions.add(createNewGroupAction(group));
                        break;
                    case COMPONENT:
                        actions.add(createNewCompAction(group));
                        break;
                    case SPLIT:
                        actions.add(createNewSplitGroupAction(group));
                        break;
                    case TAB:
                        actions.add(createNewTabGroupAction(group));
                        break;
                    case SPACE:

                        actions.add(createNewSpaceAction(group));
                        break;
                }
            }

            return actions.toArray(new Action[0]);
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractDropDownDescriptor<TabGroup.ORIENTATION> orientationDescriptor = new AbstractDropDownDescriptor<TabGroup.ORIENTATION>("Orientation")
            {

                public TabGroup.ORIENTATION[] getOptions()
                {

                    return TabGroup.ORIENTATION.values();
                }

                public String getOptionText(TabGroup.ORIENTATION t)
                {
                    return t.name();
                }

                public void setValue(TabGroup.ORIENTATION value)
                {
                    group.setOrientation(value);
                    editor.setDirty(true);
                    refresh(TabNode.this);
                    refreshPreview();
                }

                public TabGroup.ORIENTATION getValue()
                {
                    return group.getOrientation();
                }
            };

            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return getItemDescriptors().toArray(new AbstractDescriptor<?>[0]);
                }
            };
            
            if(isSupportLayoutSettings())
            {

                return new AbstractDescriptor<?>[] { orientationDescriptor, layoutGroupDescriptor };
            }
            else
            {

                return new AbstractDescriptor<?>[] { orientationDescriptor };
            }
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !isAncestor(source);
        }

        public void move(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJCoreLayoutItem> items = group.getItems();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    group.addItem(index, (EJCoreLayoutItem) source);
                }
            }
            else
                group.addItem((EJCoreLayoutItem) source);

        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }
    }

    private class ComponentNode extends AbstractLayoutItem
    {
        private final LayoutComponent component;
        private final Image           IMG_COMP = EJUIImages.getImage(EJUIImages.DESC_LAYOUT_COMP);

        public ComponentNode(AbstractNode<?> parent, LayoutComponent source)
        {
            super(parent, source);
            this.component = source;
        }

        @Override
        public String getName()
        {
            if (component.getRenderer() != null && component.getRenderer().length() > 0)
            {
                return component.getRenderer();
            }
            return "<component>";
        }

        @Override
        public Image getImage()
        {
            return IMG_COMP;
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Renderer",
                    "Component renderer defined in application renderers.")
            {

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    EJPluginAssignedRendererContainer rendererContainer = editor.getEntireJProperties().getAppComponentRendererContainer();
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
                    component.setRenderer(value);
                    EJPluginEntireJProperties pluginEntireJProperties = editor.getEntireJProperties();
                    EJFrameworkExtensionProperties extensionProperties = ExtensionsPropertiesFactory.createApplicationComponentProperties(
                            pluginEntireJProperties, value, true);
                    component.setRendereProperties(extensionProperties);
                    editor.setDirty(true);
                    refresh(ComponentNode.this);
                    if (descriptorViewer != null)
                    {
                        descriptorViewer.showDetails(ComponentNode.this);
                    }
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return component.getRenderer();
                }
            };
            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return getItemDescriptors().toArray(new AbstractDescriptor<?>[0]);
                }
            };

            EJFrameworkExtensionProperties rendereProperties = component.getRendereProperties();
            if (rendereProperties == null && component.getRenderer() != null)
            {
                EJPluginEntireJProperties pluginEntireJProperties = editor.getEntireJProperties();
                rendereProperties = ExtensionsPropertiesFactory.createApplicationComponentProperties(pluginEntireJProperties, component.getRenderer(), true);
                component.setRendereProperties(rendereProperties);
            }
            if (rendereProperties != null)
            {
                EJPluginEntireJProperties pluginEntireJProperties = editor.getEntireJProperties();
                final EJDevAppComponentRendererDefinition componentDefinition = ExtensionsPropertiesFactory.loadAppComponentDefinition(pluginEntireJProperties,
                        component.getRenderer());
                if (componentDefinition != null)
                {
                    final EJPropertyDefinitionGroup definitionGroup = componentDefinition.getComponentPropertyDefinitionGroup();
                    if (definitionGroup != null)
                    {

                        AbstractGroupDescriptor rendererGroupDescriptor = new AbstractGroupDescriptor("Renderer Settings")
                        {

                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                                return PropertyDefinitionGroupPart.createGroupDescriptors(editor, editor.getEntireJProperties(), definitionGroup,
                                        component.getRendereProperties());
                            }
                        };
                        if(isSupportLayoutSettings())
                        {
                            return new AbstractDescriptor<?>[] { rendererDescriptor, rendererGroupDescriptor, layoutGroupDescriptor };
                        }
                        else
                        {
                            return new AbstractDescriptor<?>[] { rendererDescriptor, rendererGroupDescriptor };
                        }
                        
                    }
                }
            }
            if(isSupportLayoutSettings())
            {
                return new AbstractDescriptor<?>[] { rendererDescriptor, layoutGroupDescriptor };
            }
            else
            {
                return new AbstractDescriptor<?>[] { rendererDescriptor };
            }
           
        }
    }

    private abstract class AbstractLayoutItem extends AbstractNode<EJCoreLayoutItem> implements Neighbor, Movable
    {

        public AbstractLayoutItem(AbstractNode<?> parent, EJCoreLayoutItem source)
        {
            super(parent, source);
        }

        public Object getNeighborSource()
        {
            return source;
        }

        public boolean canMove()
        {
            return true;
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    if (parent != null && parent.getSource() instanceof EJCoreLayoutItem.ItemContainer)
                    {
                        ((EJCoreLayoutItem.ItemContainer) parent.getSource()).removeItem(source);
                        editor.setDirty(true);
                        refresh(parent);
                        refreshPreview();
                    }

                }
                
                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
        }
        
       

        protected List<AbstractDescriptor<?>> getItemDescriptors()
        {
            List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();

            // if split ignore other properties and use HintWidth as Weight
            if (getParent() instanceof SplitNode)
            {
                AbstractTextDescriptor weightDescriptor = new AbstractTextDescriptor("Weight")
                {

                    @Override
                    public void setValue(String value)
                    {
                        try
                        {
                            source.setHintWidth(Integer.parseInt(value));
                        }
                        catch (NumberFormatException e)
                        {
                            source.setHintWidth(0);
                            if (text != null)
                            {
                                text.setText(getValue());
                                text.selectAll();
                            }
                        }
                        editor.setDirty(true);
                        refresh(AbstractLayoutItem.this);
                        refreshPreview();
                    }

                    @Override
                    public String getValue()
                    {
                        return String.valueOf(source.getHintWidth());
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
                descriptors.add(weightDescriptor);
                return descriptors;
            }
            if (getParent() instanceof TabNode)
            {
                AbstractTextDescriptor titleDescriptor = new AbstractTextDescriptor("Title")
                {

                    @Override
                    public void setValue(String value)
                    {
                        source.setName(value);

                        editor.setDirty(true);
                        refresh(AbstractLayoutItem.this);
                        refreshPreview();
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getName();
                    }

                };
                descriptors.add(titleDescriptor);
                return descriptors;
            }
            AbstractTextDescriptor hSapnDescriptor = new AbstractTextDescriptor("Horizontal Span")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setHorizontalSpan(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setHorizontalSpan(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(AbstractLayoutItem.this);
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getHorizontalSpan());
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

            AbstractTextDescriptor vSapnDescriptor = new AbstractTextDescriptor("Vertical Span")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setVerticalSpan(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setVerticalSpan(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(AbstractLayoutItem.this);
                    refreshPreview();
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getVerticalSpan());
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
            descriptors.add(hSapnDescriptor);
            descriptors.add(vSapnDescriptor);

            AbstractGroupDescriptor sizeHintGroupDescriptor = new AbstractGroupDescriptor("Preferred Size")
            {
                @Override
                public boolean isExpand()
                {
                    return true;
                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    AbstractTextDescriptor widthHintDescriptor = new AbstractTextDescriptor("Width")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            try
                            {
                                source.setHintWidth(Integer.parseInt(value));
                            }
                            catch (NumberFormatException e)
                            {
                                source.setHintWidth(0);
                                if (text != null)
                                {
                                    text.setText(getValue());
                                    text.selectAll();
                                }
                            }
                            editor.setDirty(true);
                            refresh(AbstractLayoutItem.this);
                            refreshPreview();
                        }

                        @Override
                        public String getValue()
                        {
                            return String.valueOf(source.getHintWidth());
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
                    AbstractTextDescriptor heightHintDescriptor = new AbstractTextDescriptor("Height")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            try
                            {
                                source.setHintHeight(Integer.parseInt(value));
                            }
                            catch (NumberFormatException e)
                            {
                                source.setHintWidth(0);
                                if (text != null)
                                {
                                    text.setText(getValue());
                                    text.selectAll();
                                }
                            }
                            editor.setDirty(true);
                            refresh(AbstractLayoutItem.this);
                            refreshPreview();
                        }

                        @Override
                        public String getValue()
                        {
                            return String.valueOf(source.getHintHeight());
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
                    return new AbstractDescriptor<?>[] { widthHintDescriptor, heightHintDescriptor };
                }
            };

            descriptors.add(sizeHintGroupDescriptor);

            AbstractGroupDescriptor sizeMinGroupDescriptor = new AbstractGroupDescriptor("Minimum Size")
            {
                @Override
                public boolean isExpand()
                {
                    return false;
                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Width")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            try
                            {
                                source.setMinWidth(Integer.parseInt(value));
                            }
                            catch (NumberFormatException e)
                            {
                                source.setMinWidth(0);
                                if (text != null)
                                {
                                    text.setText(getValue());
                                    text.selectAll();
                                }
                            }
                            editor.setDirty(true);
                            refresh(AbstractLayoutItem.this);
                            refreshPreview();
                        }

                        @Override
                        public String getValue()
                        {
                            return String.valueOf(source.getMinWidth());
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
                    AbstractTextDescriptor heightDescriptor = new AbstractTextDescriptor("Height")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            try
                            {
                                source.setMinHight(Integer.parseInt(value));
                            }
                            catch (NumberFormatException e)
                            {
                                source.setMinWidth(0);
                                if (text != null)
                                {
                                    text.setText(getValue());
                                    text.selectAll();
                                }
                            }
                            editor.setDirty(true);
                            refresh(AbstractLayoutItem.this);
                            refreshPreview();
                        }

                        @Override
                        public String getValue()
                        {
                            return String.valueOf(source.getMinHeight());
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
                    return new AbstractDescriptor<?>[] { widthDescriptor, heightDescriptor };
                }
            };
            descriptors.add(sizeMinGroupDescriptor);

            AbstractDropDownDescriptor<FILL> fillDescriptor = new AbstractDropDownDescriptor<FILL>("Fill")
            {

                public FILL[] getOptions()
                {

                    return FILL.values();
                }

                public String getOptionText(FILL t)
                {
                    return t.name();
                }

                public void setValue(FILL value)
                {
                    source.setFill(value);
                    editor.setDirty(true);
                    refresh(AbstractLayoutItem.this);
                    refreshPreview();
                }

                public FILL getValue()
                {
                    return source.getFill();
                }
            };
            descriptors.add(fillDescriptor);
            AbstractDropDownDescriptor<GRAB> grabDescriptor = new AbstractDropDownDescriptor<GRAB>("Grab")
            {

                public GRAB[] getOptions()
                {

                    return GRAB.values();
                }

                public String getOptionText(GRAB t)
                {
                    return t.name();
                }

                public void setValue(GRAB value)
                {
                    source.setGrab(value);
                    editor.setDirty(true);
                    refresh(AbstractLayoutItem.this);
                    refreshPreview();
                }

                public GRAB getValue()
                {
                    return source.getGrab();
                }
            };
            descriptors.add(grabDescriptor);

            return descriptors;
        }
    }

    static interface LayoutPreviewer
    {
        void refresh();
    }
}
