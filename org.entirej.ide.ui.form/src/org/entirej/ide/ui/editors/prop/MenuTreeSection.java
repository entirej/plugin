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
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormPage;
import org.entirej.framework.core.actionprocessor.EJDefaultMenuActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJMenuActionProcessor;
import org.entirej.framework.core.properties.EJCoreLayoutItem.ItemContainer;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafActionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafBranchProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafContainer;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuLeafSpacerProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMenuProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginMenuContainer;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractProjectSrcFileDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Movable;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;
import org.entirej.ide.ui.utils.FormsUtil;

public class MenuTreeSection extends AbstractNodeTreeSection
{
    private final EJPropertiesEditor editor;

    public MenuTreeSection(EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent);
        this.editor = editor;
        initTree();
        addDnDSupport(null);
    }

    @Override
    public Object getTreeInput()
    {
        return new Object();
    }

    @Override
    public String getSectionTitle()
    {
        return "Menu Setup";
    }

    @Override
    public String getSectionDescription()
    {

        return "Define menus for application in the following section.";
    }

    @Override
    public Action[] getBaseActions()
    {

        return new Action[] { createNewMenuAction() };
    }

    @Override
    public AbstractNodeContentProvider getContentProvider()
    {
        return new AbstractNodeContentProvider()
        {

            public Object[] getElements(Object inputElement)
            {
                List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

                EJPluginMenuContainer menuContainer = editor.getEntireJProperties().getPluginMenuContainer();
                Collection<EJPluginMenuProperties> allMenuProperties = menuContainer.getAllMenuProperties();
                for (EJPluginMenuProperties menuProperties : allMenuProperties)
                {
                    nodes.add(new GroupNode(menuProperties));
                }
                return nodes.toArray(new AbstractNode<?>[0]);
            }
        };
    }

    private Action createNewMenuAction()
    {

        return new Action("New Menu")
        {

            @Override
            public void runWithEvent(Event event)
            {
                EJPluginMenuContainer menuContainer = editor.getEntireJProperties().getPluginMenuContainer();
                EJPluginMenuProperties menuProperties = new EJPluginMenuProperties(editor.getEntireJProperties(), "");
                menuContainer.addMenuProperties(menuProperties);
                editor.setDirty(true);
                refresh();
                selectNodes(true, (menuProperties));
            }

        };
    }

    private Action createNewBranchAction(final EJPluginMenuProperties menu, final EJPluginMenuLeafContainer container)
    {

        return new Action("New Branch")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJPluginMenuLeafBranchProperties menuProperties = new EJPluginMenuLeafBranchProperties(menu, container);
                container.addLeaf(menuProperties);
                editor.setDirty(true);
                Object parent = (container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, (menuProperties));
            }

        };
    }

    private Action createNewSeparatorAction(final EJPluginMenuProperties menu, final EJPluginMenuLeafContainer container)
    {

        return new Action("New Separator")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJPluginMenuLeafSpacerProperties spacer = new EJPluginMenuLeafSpacerProperties(menu, container);

                container.addLeaf(spacer);
                editor.setDirty(true);
                Object parent = (container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
            }

        };
    }

    private Action createNewCommandAction(final EJPluginMenuProperties menu, final EJPluginMenuLeafContainer container)
    {

        return new Action("New Action Menu")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJPluginMenuLeafActionProperties command = new EJPluginMenuLeafActionProperties(menu, container);

                container.addLeaf(command);
                editor.setDirty(true);
                Object parent = (container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, (command));
            }

        };
    }

    private Action createNewFormAction(final EJPluginMenuProperties menu, final EJPluginMenuLeafContainer container)
    {

        return new Action("New Form Menu")
        {

            @Override
            public void runWithEvent(Event event)
            {

                EJPluginMenuLeafFormProperties form = new EJPluginMenuLeafFormProperties(menu, container);

                container.addLeaf(form);
                editor.setDirty(true);
                Object parent = (container);
                refresh(parent);
                selectNodes(false, parent);
                expandNodes();
                selectNodes(true, (form));
            }

        };
    }

    /**
     * 
     * root menu node
     * 
     */
    private class GroupNode extends AbstractNode<EJPluginMenuProperties> implements NodeMoveProvider
    {
        private final Image GROUP = EJUIImages.getImage(EJUIImages.DESC_MENU_GROUP);

        public GroupNode(EJPluginMenuProperties source)
        {
            super(null, source);
        }

        public String getName()
        {
            if (source.getName() == null || source.getName().length() == 0)
                return "<group>";
            return source.getName();
        }

        @Override
        public String getToolTipText()
        {
            return source.getName();
        }

        @Override
        public Image getImage()
        {
            return GROUP;
        }

        @Override
        public boolean isLeaf()
        {
            return source.getLeaves().size() == 0;
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !isAncestor(source);
        }

        public boolean isAncestor(Object s)
        {
            if (s instanceof EJPluginMenuLeafContainer)
            {
                return isAncestor((EJPluginMenuLeafContainer) s, source);

            }
            return false;
        }

        public boolean isAncestor(EJPluginMenuLeafContainer container, Object s)
        {
            List<EJPluginMenuLeafProperties> items = container.getLeaves();
            for (EJPluginMenuLeafProperties item : items)
            {
                if (s.equals(item))
                    return true;
                if (item instanceof ItemContainer)
                {
                    boolean ancestor = isAncestor((EJPluginMenuLeafContainer) item, s);
                    if (ancestor)
                        return ancestor;
                }
            }

            return false;
        }

        public void move(NodeContext context, Neighbor neighbor, Object item, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginMenuLeafProperties> items = source.getLeaves();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.addLeaf(index, (EJPluginMenuLeafProperties) item);
                }
            }
            else
                source.addLeaf((EJPluginMenuLeafProperties) item);

        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginMenuLeafProperties> leaves = new ArrayList<EJPluginMenuLeafProperties>(source.getLeaves());

            for (EJPluginMenuLeafProperties leafProperties : leaves)
            {
                if (leafProperties instanceof EJPluginMenuLeafBranchProperties)
                {
                    nodes.add(new BranchNode(this, (EJPluginMenuLeafBranchProperties) leafProperties));
                }
                else if (leafProperties instanceof EJPluginMenuLeafSpacerProperties)
                {
                    nodes.add(new SeparatorNode(this, (EJPluginMenuLeafSpacerProperties) leafProperties));
                }
                else if (leafProperties instanceof EJPluginMenuLeafActionProperties)
                {
                    nodes.add(new ActionNode(this, (EJPluginMenuLeafActionProperties) leafProperties));
                }
                else if (leafProperties instanceof EJPluginMenuLeafFormProperties)
                {
                    nodes.add(new FormNode(this, (EJPluginMenuLeafFormProperties) leafProperties));
                }
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    EJPluginMenuContainer menuContainer = editor.getEntireJProperties().getPluginMenuContainer();

                    menuContainer.removeMenuProperties(source);
                    editor.setDirty(true);
                    refresh(parent);
                }
                
                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    // TODO Auto-generated method stub
                    return null;
                }
            };
        }

        @Override
        public Action[] getActions()
        {
            return new Action[] { createNewBranchAction(source, source), createNewSeparatorAction(source, source), createNewFormAction(source, source),
                    createNewCommandAction(source, source), null, createNewMenuAction() };
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setName(value);
                    editor.setDirty(true);
                    refresh(GroupNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getName();
                }
            };
            nameDescriptor.setRequired(true);

            AbstractDescriptor<Boolean> defaultMenuDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {

                @Override
                public Boolean getValue()
                {
                    return source.isDefault();
                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setDefault(value.booleanValue());
                    editor.setDirty(true);
                    refresh(GroupNode.this);

                }

            };
            defaultMenuDescriptor.setText("Default");
            AbstractTypeDescriptor actionDescriptor = new AbstractTypeDescriptor(editor, "Action Processor")
            {

                @Override
                public void setValue(String value)
                {
                    source.setActionProcessorClassName(value);
                    editor.setDirty(true);
                    refresh(GroupNode.this);

                }

                @Override
                public String getValue()
                {
                    return source.getActionProcessorClassName();
                }
            };
            actionDescriptor.setBaseClass(EJMenuActionProcessor.class.getName());
            actionDescriptor.setDefaultClass(EJDefaultMenuActionProcessor.class.getName());
            return new AbstractDescriptor<?>[] { nameDescriptor, defaultMenuDescriptor, actionDescriptor };
        }
    }

    private class BranchNode extends AbstractNode<EJPluginMenuLeafBranchProperties> implements Neighbor, Movable, NodeMoveProvider
    {
        private final Image FOLDER = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);

        public BranchNode(AbstractNode<?> parent, EJPluginMenuLeafBranchProperties source)
        {
            super(parent, source);
        }

        public String getName()
        {
            if (source.getDisplayName() != null && source.getDisplayName().length() > 0)
                return source.getDisplayName();

            if (source.getName() == null || source.getName().length() == 0)
                return "<branch>";
            return source.getName();
        }

        @Override
        public String getToolTipText()
        {

            return source.getName();
        }

        @Override
        public Image getImage()
        {
            return FOLDER;
        }

        @Override
        public boolean isLeaf()
        {
            return source.getLeaves().size() == 0;
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !isAncestor(source);
        }

        public boolean isAncestor(Object s)
        {
            if (s instanceof EJPluginMenuLeafContainer)
            {
                return isAncestor((EJPluginMenuLeafContainer) s, source);

            }
            return false;
        }

        public boolean isAncestor(EJPluginMenuLeafContainer container, Object s)
        {
            List<EJPluginMenuLeafProperties> items = container.getLeaves();
            for (EJPluginMenuLeafProperties item : items)
            {
                if (s.equals(item))
                    return true;
                if (item instanceof ItemContainer)
                {
                    boolean ancestor = isAncestor((EJPluginMenuLeafContainer) item, s);
                    if (ancestor)
                        return ancestor;
                }
            }

            return false;
        }

        public void move(NodeContext context, Neighbor neighbor, Object item, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginMenuLeafProperties> items = source.getLeaves();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.addLeaf(index, (EJPluginMenuLeafProperties) item);
                }
            }
            else
                source.addLeaf((EJPluginMenuLeafProperties) item);

        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            List<EJPluginMenuLeafProperties> leaves = new ArrayList<EJPluginMenuLeafProperties>(source.getLeaves());
            for (EJPluginMenuLeafProperties leafProperties : leaves)
            {
                if (leafProperties instanceof EJPluginMenuLeafBranchProperties)
                {
                    nodes.add(new BranchNode(this, (EJPluginMenuLeafBranchProperties) leafProperties));
                }
                else if (leafProperties instanceof EJPluginMenuLeafSpacerProperties)
                {
                    nodes.add(new SeparatorNode(this, (EJPluginMenuLeafSpacerProperties) leafProperties));
                }
                else if (leafProperties instanceof EJPluginMenuLeafActionProperties)
                {
                    nodes.add(new ActionNode(this, (EJPluginMenuLeafActionProperties) leafProperties));
                }
                else if (leafProperties instanceof EJPluginMenuLeafFormProperties)
                {
                    nodes.add(new FormNode(this, (EJPluginMenuLeafFormProperties) leafProperties));
                }
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    if (parent instanceof GroupNode)
                    {
                        ((GroupNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
                    }
                    else if (parent instanceof BranchNode)
                    {
                        ((BranchNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
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
        public Action[] getActions()
        {
            return new Action[] { createNewBranchAction(source.getMenu(), source), createNewSeparatorAction(source.getMenu(), source),
                    createNewFormAction(source.getMenu(), source), createNewCommandAction(source.getMenu(), source), null, createNewMenuAction() };
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setName(value);
                    editor.setDirty(true);
                    refresh(BranchNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getName();
                }
            };
            nameDescriptor.setRequired(true);
            AbstractTextDescriptor displayNameDescriptor = new AbstractTextDescriptor("Display Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setDisplayName(value);
                    editor.setDirty(true);
                    refresh(BranchNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getDisplayName();
                }
            };
            displayNameDescriptor.setRequired(true);

            AbstractProjectSrcFileDescriptor iconDescriptor = new AbstractProjectSrcFileDescriptor(editor, "Icon")
            {

                @Override
                public void setValue(String value)
                {
                    source.setIconName(value);
                    editor.setDirty(true);
                    refresh(BranchNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getIconName();
                }
            };

            return new AbstractDescriptor<?>[] { nameDescriptor, displayNameDescriptor, iconDescriptor };
        }

        public boolean canMove()
        {
            return true;
        }

        public Object getNeighborSource()
        {
            return source;
        }

    }

    private class SeparatorNode extends AbstractNode<EJPluginMenuLeafSpacerProperties> implements Neighbor, Movable
    {
        private final Image SEPARATOR = EJUIImages.getImage(EJUIImages.DESC_MENU_SEPARATOR);

        public SeparatorNode(AbstractNode<?> parent, EJPluginMenuLeafSpacerProperties source)
        {
            super(parent, source);
        }

        public String getName()
        {

            return "<separator>";
        }

        @Override
        public String getToolTipText()
        {
            return getName();
        }

        @Override
        public Image getImage()
        {
            return SEPARATOR;
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    if (parent instanceof GroupNode)
                    {
                        ((GroupNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
                    }
                    else if (parent instanceof BranchNode)
                    {
                        ((BranchNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
                    }

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

    }

    private class ActionNode extends AbstractNode<EJPluginMenuLeafActionProperties> implements Neighbor, Movable
    {
        private final Image ACTION = EJUIImages.getImage(EJUIImages.DESC_ACTION);

        public ActionNode(AbstractNode<?> parent, EJPluginMenuLeafActionProperties source)
        {
            super(parent, source);
        }

        public String getName()
        {

            if (source.getDisplayName() != null && source.getDisplayName().length() > 0)
                return source.getDisplayName();
            if (source.getName() == null || source.getName().length() == 0)
                return "<action>";
            return source.getName();
        }

        @Override
        public String getToolTipText()
        {

            return source.getHint();
        }

        @Override
        public Image getImage()
        {
            return ACTION;
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    if (parent instanceof GroupNode)
                    {
                        ((GroupNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
                    }
                    else if (parent instanceof BranchNode)
                    {
                        ((BranchNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
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
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setName(value);
                    editor.setDirty(true);
                    refresh(ActionNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getName();
                }
            };
            nameDescriptor.setRequired(true);
            AbstractTextDescriptor displayNameDescriptor = new AbstractTextDescriptor("Display Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setDisplayName(value);
                    editor.setDirty(true);
                    refresh(ActionNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getDisplayName();
                }
            };
            displayNameDescriptor.setRequired(true);

            AbstractTextDescDescriptor hintDescriptor = new AbstractTextDescDescriptor("Hint")
            {

                @Override
                public void setValue(String value)
                {
                    source.setHint(value);
                    editor.setDirty(true);
                    refresh(ActionNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getHint();
                }
            };

            AbstractTextDescriptor menuActionDescriptor = new AbstractTextDescriptor("Action Command")
            {

                @Override
                public void setValue(String value)
                {
                    source.setMenuAction(value);
                    editor.setDirty(true);
                    refresh(ActionNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getMenuAction();
                }
            };
            menuActionDescriptor.setRequired(true);
            AbstractProjectSrcFileDescriptor iconDescriptor = new AbstractProjectSrcFileDescriptor(editor, "Icon")
            {

                @Override
                public void setValue(String value)
                {
                    source.setIconName(value);
                    editor.setDirty(true);
                    refresh(ActionNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getIconName();
                }
            };

            return new AbstractDescriptor<?>[] { nameDescriptor, displayNameDescriptor, menuActionDescriptor, iconDescriptor, hintDescriptor };
        }

        public boolean canMove()
        {
            return true;
        }

        public Object getNeighborSource()
        {
            return source;
        }

    }

    private class FormNode extends AbstractNode<EJPluginMenuLeafFormProperties> implements Neighbor, Movable
    {
        private final Image FORM = EJUIImages.getImage(EJUIImages.DESC_FORM);

        public FormNode(AbstractNode<?> parent, EJPluginMenuLeafFormProperties source)
        {
            super(parent, source);
        }

        public String getName()
        {
            String name = null;
            if (source.getDisplayName() != null && source.getDisplayName().length() > 0)
                name = source.getDisplayName();

            if (source.getName() == null || source.getName().length() == 0)
                name = "<form>";

            if (source.getFormName() != null && source.getFormName().length() > 0)
                name = String.format("%s : %s", source.getFormName(), name);
            return name;
        }

        @Override
        public String getToolTipText()
        {
            return source.getHint();
        }

        @Override
        public Image getImage()
        {
            return FORM;
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {
                    if (parent instanceof GroupNode)
                    {
                        ((GroupNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
                    }
                    else if (parent instanceof BranchNode)
                    {
                        ((BranchNode) parent).getSource().removeLeaf(source);
                        editor.setDirty(true);
                        refresh(parent);
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
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setName(value);
                    editor.setDirty(true);
                    refresh(FormNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getName();
                }
            };
            nameDescriptor.setRequired(true);
            AbstractTextDescriptor displayNameDescriptor = new AbstractTextDescriptor("Display Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setDisplayName(value);
                    editor.setDirty(true);
                    refresh(FormNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getDisplayName();
                }
            };
            displayNameDescriptor.setRequired(true);

            AbstractTextDescDescriptor hintDescriptor = new AbstractTextDescDescriptor("Hint")
            {

                @Override
                public void setValue(String value)
                {
                    source.setHint(value);
                    editor.setDirty(true);
                    refresh(FormNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getHint();
                }
            };

            AbstractDropDownDescriptor<String> formNameDescriptor = new AbstractDropDownDescriptor<String>("Form Name")
            {

                @Override
                public void setValue(String value)
                {
                    source.setFormName(value);
                    source.setName(value);
                    if (value != null && value.length() != 0 && (source.getDisplayName() == null || source.getDisplayName().length() == 0))
                    {
                        source.setDisplayName(FormsUtil.getFormTitle(editor.getJavaProject(), value));
                    }

                    editor.setDirty(true);

                    refresh(FormNode.this);
                    getDescriptorViewer().showDetails(getSelectedNode());
                }

                @Override
                public String getValue()
                {
                    return source.getFormName();
                }

                public String[] getOptions()
                {
                    IJavaProject javaProject = editor.getJavaProject();
                    if (javaProject != null)
                    {
                        return FormsUtil.getFormNames(javaProject).toArray(new String[0]);
                    }
                    return new String[0];
                }

                public String getOptionText(String t)
                {
                    return t;
                }
            };
            formNameDescriptor.setRequired(true);
            AbstractProjectSrcFileDescriptor iconDescriptor = new AbstractProjectSrcFileDescriptor(editor, "Icon")
            {

                @Override
                public void setValue(String value)
                {
                    source.setIconName(value);
                    editor.setDirty(true);
                    refresh(FormNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getIconName();
                }
            };

            return new AbstractDescriptor<?>[] { formNameDescriptor, nameDescriptor, displayNameDescriptor, iconDescriptor, hintDescriptor };
        }

        public boolean canMove()
        {

            return true;
        }

        public Object getNeighborSource()
        {

            return source;
        }

    }
}
