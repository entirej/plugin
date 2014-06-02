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
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.editor.FormPage;
import org.entirej.framework.core.enumerations.EJRendererType;
import org.entirej.framework.core.renderers.definitions.interfaces.EJAppComponentRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJBlockRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJFormRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJItemRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJLovRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
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

public class RendererTreeSection extends AbstractNodeTreeSection
{
    private final EJPropertiesEditor editor;

    public RendererTreeSection(EJPropertiesEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent);
        this.editor = editor;
        initTree();

        addDnDSupport(null);// no root move need in layout
    }

    @Override
    public Object getTreeInput()
    {
        return new Object();
    }

    @Override
    public String getSectionTitle()
    {
        return "Defined Renderers";
    }

    @Override
    public String getSectionDescription()
    {

        return "Define renderers for application in the following section.";
    }

    @Override
    public Action[] getBaseActions()
    {

        return new Action[] { createNewRenderer(EJRendererType.FORM), createNewRenderer(EJRendererType.BLOCK), createNewRenderer(EJRendererType.ITEM),
                createNewRenderer(EJRendererType.LOV), createNewRenderer(EJRendererType.MENU), createNewRenderer(EJRendererType.APP_COMPONENT) };
    }

    @Override
    public AbstractNodeContentProvider getContentProvider()
    {
        return new AbstractNodeContentProvider()
        {

            public Object[] getElements(Object inputElement)
            {
                EJPluginEntireJProperties props = editor.getEntireJProperties();
                if (props != null)
                {
                    GroupNode formNode = new GroupNode("Form Renderers", "Application defined form renderers", props.getFormRendererContainer());
                    GroupNode blockNode = new GroupNode("Block Renderers", "Application defined block renderers", props.getBlockRendererContainer());
                    GroupNode itemNode = new GroupNode("Item Renderers", "Application defined item renderers", props.getItemRendererContainer());
                    GroupNode lovNode = new GroupNode("Lov Renderers", "Application defined lov renderers", props.getLovRendererContainer());
                    // GroupNode menuNode = new GroupNode("Menu Renderers",
                    // "Application defined menu renderers",
                    // props.getMenuRendererContainer());
                    GroupNode appCompNode = new GroupNode("Component Renderers", "Application defined component renderers",
                            props.getAppComponentRendererContainer());
                    return new Object[] { formNode, blockNode, itemNode, lovNode, appCompNode };
                }

                return new Object[0];
            }
        };
    }

    private Action createNewRenderer(final EJRendererType type)
    {
        String lable;
        switch (type)
        {
            case FORM:
                lable = "New Form Renderer";
                break;
            case BLOCK:
                lable = "New Block Renderer";
                break;
            case ITEM:
                lable = "New Item Renderer";
                break;
            case LOV:
                lable = "New Lov Renderer";
                break;
            case APP_COMPONENT:
                lable = "New Component Renderer";
                break;
            default:
                lable = "New Renderer";
                break;
        }
        return new Action(lable)
        {

            @Override
            public void runWithEvent(Event event)
            {
                EJPluginEntireJProperties entireJProperties = editor.getEntireJProperties();
                EJPluginRenderer def = new EJPluginRenderer(editor.getEntireJProperties(), "", type);
                EJPluginAssignedRendererContainer container = null;
                switch (type)
                {
                    case FORM:
                        container = entireJProperties.getFormRendererContainer();
                        break;
                    case BLOCK:
                        container = entireJProperties.getBlockRendererContainer();
                        break;
                    case ITEM:
                        container = entireJProperties.getItemRendererContainer();
                        break;
                    case LOV:
                        container = entireJProperties.getLovRendererContainer();
                        break;
                    case APP_COMPONENT:
                        container = entireJProperties.getAppComponentRendererContainer();
                        break;

                }
                if (container != null)
                {
                    container.addRendererAssignment(def);
                    editor.setDirty(true);
                    AbstractNode<?> parent = findNode(container);
                    refresh(parent);
                    selectNodes(false, parent);
                    expandNodes();
                    selectNodes(true, findNode(def));
                }
            }

        };
    }

    private class GroupNode extends AbstractNode<EJPluginAssignedRendererContainer> implements  NodeMoveProvider
    {
        private final Image  GROUP = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
        private final String lable;
        private final String tooltip;

        public GroupNode(String lable, String tooltip, EJPluginAssignedRendererContainer source)
        {
            super(null, source);
            this.lable = lable;
            this.tooltip = tooltip;
        }

        public String getName()
        {
            return lable;
        }

        @Override
        public String getToolTipText()
        {
            return tooltip;
        }

        @Override
        public Image getImage()
        {
            return GROUP;
        }

        @Override
        public boolean isLeaf()
        {
            return source.getAllRenderers().size() == 0;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<RendererNode> nodes = new ArrayList<RendererNode>();

            for (EJPluginRenderer renderer : source.getAllRenderers())
            {
                nodes.add(new RendererNode(this, renderer));
            }

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        @Override
        public Action[] getActions()
        {
            return new Action[] { createNewRenderer(source.getRendererType()) };
        }

        public boolean canMove(Neighbor relation, Object source)
        {
           
            return source instanceof EJPluginRenderer && this.source.getRendererType()== (((EJPluginRenderer)source).getRendererType());
        }

        public void move(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginRenderer> items = new ArrayList<EJPluginRenderer>(this.source.getAllRenderers());
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    this.source.addRendererAssignment(index,(EJPluginRenderer)source);
                }
            }
            else
                this.source.addRendererAssignment((EJPluginRenderer)source);
            
        }
    }

    private class RendererNode extends AbstractNode<EJPluginRenderer> implements Neighbor, Movable
    {
        private final Image ELEMENT = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);

        public RendererNode(final GroupNode parent, EJPluginRenderer source)
        {
            super(parent, source);
        }

        public String getName()
        {
            return source.getAssignedName();
        }

        @Override
        public String getToolTipText()
        {
            return source.getRendererDefinitionClassName();
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
                        ((GroupNode) parent).getSource().removeRendererAssignment(source);
                        editor.setDirty(true);
                        refresh(parent);
                    }

                }

            };
        }

        @Override
        public Image getImage()
        {
            return ELEMENT;
        }

        @Override
        public Action[] getActions()
        {
            return new Action[] { createNewRenderer(source.getRendererType()) };
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Name", "Renderer name")
            {

                @Override
                public void setValue(String value)
                {
                    source.internalSetName(value);
                    editor.setDirty(true);
                    refresh(RendererNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getAssignedName();
                }
            };
            AbstractTypeDescriptor typeDescriptor = new AbstractTypeDescriptor(editor, "Renderer")
            {

                @Override
                public void setValue(String value)
                {
                    source.setRendererDefinitionClassName(value, true);
                    editor.setDirty(true);
                    refresh(RendererNode.this);

                }

                @Override
                public String getValue()
                {

                    return source.getRendererDefinitionClassName();
                }
            };
            switch (source.getRendererType())
            {
                case FORM:
                    typeDescriptor.setBaseClass(EJFormRendererDefinition.class.getName());
                    break;
                case BLOCK:
                    typeDescriptor.setBaseClass(EJBlockRendererDefinition.class.getName());
                    break;
                case ITEM:
                    typeDescriptor.setBaseClass(EJItemRendererDefinition.class.getName());
                    break;
                case LOV:
                    typeDescriptor.setBaseClass(EJLovRendererDefinition.class.getName());
                    break;
                case APP_COMPONENT:
                    typeDescriptor.setBaseClass(EJAppComponentRendererDefinition.class.getName());
                    break;
                default:
                    typeDescriptor.setBaseClass(EJRendererDefinition.class.getName());
                    break;
            }

            nameDescriptor.setRequired(true);
            typeDescriptor.setRequired(true);
            return new AbstractDescriptor<?>[] { nameDescriptor, typeDescriptor };
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
