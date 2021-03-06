/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.core.enumerations.EJCanvasDrawerPosition;
import org.entirej.framework.core.enumerations.EJCanvasMessagePosition;
import org.entirej.framework.core.enumerations.EJCanvasSplitOrientation;
import org.entirej.framework.core.enumerations.EJCanvasTabPosition;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.enumerations.EJLineStyle;
import org.entirej.framework.core.enumerations.EJPopupButton;
import org.entirej.framework.core.properties.EJCoreMessagePaneProperties;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginDrawerPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginStackedPageProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginTabPageProperties;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.DisplayItemGroupNode.MainDisplayItemGroup;
import org.entirej.ide.ui.editors.form.operations.CanvasAddOperation;
import org.entirej.ide.ui.editors.form.operations.CanvasBlockAssignmentOperation;
import org.entirej.ide.ui.editors.form.operations.CanvasRemoveOperation;
import org.entirej.ide.ui.editors.operations.ReversibleOperation;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractSubActions;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.utils.FormsUtil;

public class CanvasGroupNode extends AbstractNode<EJPluginCanvasContainer> implements NodeMoveProvider
{
    private final FormDesignTreeSection treeSection;
    private final AbstractEJFormEditor  editor;
    private final static Image          MAIN         = EJUIImages.getImage(EJUIImages.DESC_LAYOUT_COMP);
    private final static Image          BLOCK        = EJUIImages.getImage(EJUIImages.DESC_CANVAS_BLOCK);
    private final static Image          GROUP        = EJUIImages.getImage(EJUIImages.DESC_CANVAS_GROUP);
    private final static Image          FORM         = EJUIImages.getImage(EJUIImages.DESC_CANVAS_FORM);
    private final static Image          STACKED      = EJUIImages.getImage(EJUIImages.DESC_CANVAS_STACKED);
    private final static Image          POPUP        = EJUIImages.getImage(EJUIImages.DESC_CANVAS_POPUP);
    private final static Image          TAB          = EJUIImages.getImage(EJUIImages.DESC_CANVAS_TAB);
    private final static Image          TAB_PAGE     = EJUIImages.getImage(EJUIImages.DESC_CANVAS_TAB_PAGE);
    private final static Image          DRAWER       = EJUIImages.getImage(EJUIImages.DESC_CANVAS_DRAWER);
    private final static Image          DRAWER_PAGE  = EJUIImages.getImage(EJUIImages.DESC_CANVAS_DRAWER_PAGE);
    private final static Image          BLOCK_REF    = EJUIImages.getImage(EJUIImages.DESC_CANVAS_BLOCK_REF);
    private final static Image          GROUP_REF    = EJUIImages.getImage(EJUIImages.DESC_CANVAS_GROUP_REF);
    private final static Image          FORM_REF     = EJUIImages.getImage(EJUIImages.DESC_CANVAS_FORM_REF);
    private final static Image          STACKED_REF  = EJUIImages.getImage(EJUIImages.DESC_CANVAS_STACKED_REF);
    private final static Image          POPUP_REF    = EJUIImages.getImage(EJUIImages.DESC_CANVAS_POPUP_REF);
    private final static Image          TAB_REF      = EJUIImages.getImage(EJUIImages.DESC_CANVAS_TAB_REF);
    private final static Image          DRAWER_REF   = EJUIImages.getImage(EJUIImages.DESC_CANVAS_DRAWER_REF);
    private final static Image          TAB_PAGE_REF = EJUIImages.getImage(EJUIImages.DESC_CANVAS_TAB_PAGE_REF);
    private final static Image          DRAWER_PAGE_REF = EJUIImages.getImage(EJUIImages.DESC_CANVAS_DRAWER_PAGE_REF);

    public CanvasGroupNode(FormDesignTreeSection treeSection)
    {
        super(null, treeSection.getEditor().getFormProperties().getCanvasContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public <S> S getAdapter(Class<S> adapter)
    {
        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
        {
            return adapter.cast(new FormCanvasPreviewImpl());
        }
        return null;
    }

    public String getName()
    {

        return "Canvases";
    }

    @Override
    public String getToolTipText()
    {
        return "Form canvas definitions";
    }

    @Override
    public Image getImage()
    {
        return MAIN;
    }

    @Override
    public boolean isLeaf()
    {
        return source.isEmpty();
    }

    boolean isAncestorCanvas(EJPluginCanvasProperties source, Object parent)
    {
        if (source.equals(parent))
        {
            return true;
        }

        EJPluginCanvasContainer parentCanvasContainer = source.getParentCanvasContainer();
        while (parentCanvasContainer.getParnetCanvas() != null)
        {
            if (parentCanvasContainer.getParnetCanvas().equals(parent))
            {
                return true;
            }
            parentCanvasContainer = parentCanvasContainer.getParnetCanvas().getParentCanvasContainer();

        }

        return false;
    }

    public AbstractSubActions createNewCanvasAction(final AbstractNode<?> parentNode, final EJPluginCanvasContainer container, final boolean isRoot)
    {
        return createNewCanvasAction(treeSection, parentNode, container, isRoot);
    }

    public static AbstractSubActions createNewCanvasAction(final FormDesignTreeSection treeSection, final Object parentNode,
            final EJPluginCanvasContainer container, final boolean isRoot)
    {
        final AbstractEJFormEditor editor = treeSection.getEditor();
        return new AbstractSubActions("New Canvas")
        {
            Action createAction(final EJCanvasType type)
            {

                String name = type.name();
                switch (type)
                {
                    case BLOCK:
                        name = "Block";
                        break;
                    case POPUP:
                        name = "Popup";
                        break;
                    case GROUP:
                        name = "Group";
                        break;
                    case SPLIT:
                        name = "Split";
                        break;
                    case STACKED:
                        name = "Stacked";
                        break;
                    case FORM:
                        name = "Form";
                        break;
                    case SEPARATOR:
                        name = "Separator";
                        break;
                    case TAB:
                        name = "Tab";
                        break;
                    case DRAWER:
                        name = "Drawer";
                        break;
                }

                return new Action(name)
                {
                    @Override
                    public void runWithEvent(Event event)
                    {
                        addCanvas(type, getText());
                    }
                };
            }

            void addCanvas(final EJCanvasType type, String name)
            {
                InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("New Canvas [%s]", name), "Canvas Name", null,
                        new IInputValidator()
                {

                    public String isValid(String newText)
                    {
                        if (newText == null || newText.trim().length() == 0)
                            return "Canvas name can't be empty.";
                        if (container.contains(newText.trim()))
                            return "Canvas with this name already exists.";
                        if (EJPluginCanvasRetriever.canvasExists(editor.getFormProperties(), newText.trim()))
                            return "Block canvas with this name already exists.";
                        return null;
                    }
                });
                if (dlg.open() == Window.OK)
                {
                    final EJPluginCanvasProperties canvasProp = new EJPluginCanvasProperties(editor.getFormProperties(), dlg.getValue().trim());
                    canvasProp.setType(type);
                    ;

                    if (type == EJCanvasType.SEPARATOR)
                    {
                        canvasProp.setExpandVertically(false);
                        canvasProp.setWidth(2);
                        canvasProp.setHeight(2);
                    }

                    CanvasAddOperation addOperation = new CanvasAddOperation(treeSection, container, canvasProp, -1);
                    editor.execute(addOperation);

                }
            }

            @Override
            public Action[] getActions()
            {
                if (isRoot)
                    return new Action[] { createAction(EJCanvasType.BLOCK), createAction(EJCanvasType.GROUP), createAction(EJCanvasType.SPLIT),
                            createAction(EJCanvasType.TAB),createAction(EJCanvasType.DRAWER), createAction(EJCanvasType.STACKED), createAction(EJCanvasType.POPUP),
                            createAction(EJCanvasType.FORM), createAction(EJCanvasType.SEPARATOR) };
                return new Action[] { createAction(EJCanvasType.BLOCK), createAction(EJCanvasType.GROUP), createAction(EJCanvasType.SPLIT),
                        createAction(EJCanvasType.TAB),createAction(EJCanvasType.DRAWER), createAction(EJCanvasType.STACKED), createAction(EJCanvasType.FORM),
                        createAction(EJCanvasType.SEPARATOR) };
            }

        };
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {
        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
        List<EJPluginCanvasProperties> items = source.getCanvasProperties();
        for (EJPluginCanvasProperties canvas : items)
        {
            switch (canvas.getType())
            {
                case GROUP:
                    nodes.add(new GroupCanvasNode(this, canvas));
                    break;
                case SPLIT:
                    nodes.add(new SplitCanvasNode(this, canvas));
                    break;
                case POPUP:
                    nodes.add(new PopupCanvasNode(this, canvas));
                    break;
                case TAB:
                    nodes.add(new TabCanvasNode(this, canvas));
                    break;
                case DRAWER:
                    nodes.add(new DrawerCanvasNode(this, canvas));
                    break;
                case STACKED:
                    nodes.add(new StackedCanvasNode(this, canvas));
                    break;
                case FORM:
                    nodes.add(new FormCanvasNode(this, canvas));
                    break;
                case SEPARATOR:
                    nodes.add(new SeparatorCanvasNode(this, canvas));
                    break;
                default:
                    nodes.add(new BlockCanvasNode(this, canvas));
                    break;
            }
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {
        return new Action[] { createNewCanvasAction(this, source, true) };
    }

    @Override
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        return new AbstractDescriptor<?>[] {};
    }

    public boolean canMove(Neighbor relation, Object source)
    {
        return source instanceof EJPluginCanvasProperties
                && (((EJPluginCanvasProperties) source).isObjectGroupRoot() || !((EJPluginCanvasProperties) source).isImportFromObjectGroup());
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginCanvasProperties> items = source.getCanvasProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addCanvasProperties(index, (EJPluginCanvasProperties) dSource);
            }
        }
        else
            source.addCanvasProperties((EJPluginCanvasProperties) dSource);

    }

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginCanvasProperties> items = source.getCanvasProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                return new CanvasAddOperation(treeSection, source, (EJPluginCanvasProperties) dSource, index);
            }
        }
        return new CanvasAddOperation(treeSection, source, (EJPluginCanvasProperties) dSource, -1);
    }

    private abstract class AbstractCanvas extends AbstractNode<EJPluginCanvasProperties> implements Neighbor, Movable, NodeOverview
    {

        public AbstractCanvas(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        public <S> S getAdapter(Class<S> adapter)
        {
            if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                return parent.getAdapter(adapter);

            return super.getAdapter(adapter);
        }

        @Override
        public String getName()
        {
            return source.getName();
        }

        @Override
        public Image getImage()
        {
            switch (source.getType())
            {
                case GROUP:
                case SPLIT:
                    return source.isImportFromObjectGroup() ? GROUP_REF : GROUP;
                case POPUP:
                    return source.isImportFromObjectGroup() ? POPUP_REF : POPUP;
                case FORM:
                    return source.isImportFromObjectGroup() ? FORM_REF : FORM;
                case TAB:
                    return source.isImportFromObjectGroup() ? TAB_REF : TAB;
                case DRAWER:
                    return source.isImportFromObjectGroup() ? DRAWER_REF : DRAWER;
                case STACKED:
                    return source.isImportFromObjectGroup() ? STACKED_REF : STACKED;
                case SEPARATOR:
                    return EJUIImages.getImage(EJUIImages.DESC_MENU_SEPARATOR);
                default:
                    return source.isImportFromObjectGroup() ? BLOCK_REF : BLOCK;
            }

        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            if (!source.isObjectGroupRoot() && source.isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    if (cleanup)
                    {
                        cleanBlockAssignment(source);
                    }

                    source.getParentCanvasContainer().removeCanvasProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(AbstractCanvas.this.getParent());

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    return new CanvasRemoveOperation(treeSection, source.getParentCanvasContainer(), source);
                }
            };
        }

        protected AbstractGroupDescriptor createMessagePaneSettings()
        {

            final EJCoreMessagePaneProperties messagePaneProperties = source.getMessagePaneProperties();
            
            final AbstractTextDropDownDescriptor va = new AbstractTextDropDownDescriptor("Visual Attribute", "")
            {
                List<String> visualAttributeNames = new ArrayList<String>(
                        editor.getFormProperties().getEntireJProperties().getVisualAttributesContainer().getVisualAttributeNames());

                @Override
                public void setValue(String value)
                {
                    messagePaneProperties.setVa(value);
                    editor.setDirty(true);
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public String getValue()
                {
                    return messagePaneProperties.getVa();
                }

                public String[] getOptions()
                {
                    List<String> list = new ArrayList<String>();

                    list.add("");

                    list.addAll(visualAttributeNames);

                    if (getValue() != null && getValue().length() > 0 && !visualAttributeNames.contains(getValue()))
                    {
                        list.add(getValue());
                    }
                    return list.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {
                    if (t.length() > 0 && !visualAttributeNames.contains(t))
                    {
                        return String.format("Undefined !< %s >", t);
                    }

                    return t;
                }
            };
            va.setText("Visual Attribute");
            final AbstractDescriptor<Boolean> customFormatting = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                
                @Override
                public Boolean getValue()
                {
                    return messagePaneProperties.getCustomFormatting();
                }
                
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                
                @Override
                public void setValue(Boolean value)
                {
                    messagePaneProperties.setCustomFormatting(value.booleanValue());
                    editor.setDirty(true);
                }
                
            };
            customFormatting.setText("Custom Formatting");
            customFormatting.setTooltip("Indicates if the message pane support custom formatting test. eg:html,xhtml");
            final AbstractDescriptor<Boolean> canvasMessagePane = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                
                @Override
                public Boolean getValue()
                {
                    return !messagePaneProperties.getCloseable();
                }
                
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                
                @Override
                public void setValue(Boolean value)
                {
                    messagePaneProperties.setCloseable(!value.booleanValue());
                    editor.setDirty(true);
                }
                
            };
            canvasMessagePane.setText("Keep Message Pane open");
            canvasMessagePane.setTooltip("Indicates if the message pane on the screen should be kept open at all times with no close button");

            final AbstractTextDescriptor sizeHintDescriptor = new AbstractTextDescriptor("Size")
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
                        messagePaneProperties.setSize(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setWidth(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(messagePaneProperties.getSize());
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

            final AbstractDropDownDescriptor<EJCanvasMessagePosition> position = new AbstractDropDownDescriptor<EJCanvasMessagePosition>("Position")
            {

                public EJCanvasMessagePosition[] getOptions()
                {

                    return EJCanvasMessagePosition.values();
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public String getOptionText(EJCanvasMessagePosition t)
                {
                    return t.toString();
                }

                public void setValue(EJCanvasMessagePosition value)
                {
                    messagePaneProperties.setPosition(value);
                    editor.setDirty(true);
                }

                public EJCanvasMessagePosition getValue()
                {
                    return messagePaneProperties.getPosition();
                }
            };

            return new AbstractGroupDescriptor("Message Pane Settings")
            {

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { canvasMessagePane, sizeHintDescriptor, position,va,customFormatting };
                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            if (source.isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Canvas [%s]", source.getName()),
                            "Canvas Name", source.getName(), new IInputValidator()
                    {

                        public String isValid(String newText)
                        {
                            if (newText == null || newText.trim().length() == 0)
                                return "Canvas name can't be empty.";
                            if (source.getName().equals(newText.trim()))
                                return "";
                            if (source.getName().equalsIgnoreCase(newText.trim()))
                                return null;
                            if (CanvasGroupNode.this.source.contains(newText.trim()))
                                return "Canvas with this name already exists.";
                            return null;
                        }
                    });
                    if (dlg.open() == Window.OK)
                    {
                        // String oldName = source.getName();
                        String newName = dlg.getValue().trim();
                        EJPluginBlockProperties blockProperties = source.getPluginBlockProperties();
                        if (blockProperties != null)
                            (blockProperties).setCanvasName(newName);
                        source.internalSetName(newName);
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

        public void addOverview(StyledString styledString)
        {

            if (source.isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }

            styledString.append(" [ ", StyledString.QUALIFIER_STYLER);
            styledString.append(source.getType().name(), StyledString.QUALIFIER_STYLER);
            styledString.append(" ] ", StyledString.QUALIFIER_STYLER);

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

    private class SeparatorCanvasNode extends AbstractCanvas
    {

        public SeparatorCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            if (source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }
            final SeparatorCanvasNode node = SeparatorCanvasNode.this;

            AbstractDropDownDescriptor<EJCanvasSplitOrientation> orientationDescriptor = new AbstractDropDownDescriptor<EJCanvasSplitOrientation>("Orientation")
            {

                public EJCanvasSplitOrientation[] getOptions()
                {

                    return EJCanvasSplitOrientation.values();
                }

                public String getOptionText(EJCanvasSplitOrientation t)
                {
                    return t.toString();
                }

                public void setValue(EJCanvasSplitOrientation value)
                {
                    source.setSplitOrientation(value);

                    source.setExpandVertically(value != EJCanvasSplitOrientation.HORIZONTAL);
                    source.setExpandHorizontally(value == EJCanvasSplitOrientation.HORIZONTAL);

                    editor.setDirty(true);
                    treeSection.getDescriptorViewer().showDetails(node);
                    treeSection.refreshPreview();
                    treeSection.refresh(node);
                }

                public EJCanvasSplitOrientation getValue()
                {
                    return source.getSplitOrientation();
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }
            };
            AbstractDropDownDescriptor<EJLineStyle> styleDecriptor = new AbstractDropDownDescriptor<EJLineStyle>("Line Style")
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
                    source.setLineStyle(value);

                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                public EJLineStyle getValue()
                {
                    return source.getLineStyle();
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }
            };

            AbstractGroupDescriptor layoutGroupDescriptor = createLayoutSettings(editor, treeSection, this);
            if (source.isObjectGroupRoot())
            {

                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
            }

            return new AbstractDescriptor<?>[] { orientationDescriptor, styleDecriptor, layoutGroupDescriptor };

        }
    }

    private class BlockCanvasNode extends AbstractCanvas
    {

        public BlockCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();

            if (source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }

            final EJPluginBlockProperties blockProperties = source.getPluginBlockProperties();

            if (blockProperties != null)
            {
                AbstractTextDescriptor refItemDescriptor = new AbstractTextDescriptor("Assigned Block")
                {

                    @Override
                    public void setValue(String value)
                    {
                    }

                    @Override
                    public String getValue()
                    {

                        return blockProperties != null ? blockProperties.getName() : "";
                    }

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

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
                        return blockProperties != null;
                    }

                    @Override
                    public String lableLinkActivator()
                    {

                        if (blockProperties != null)
                        {
                            Object findNode = (editor.getFormProperties().getBlockContainer());
                            treeSection.selectNodes(false, findNode);
                            treeSection.expand(findNode);
                            treeSection.selectNodes(false, (blockProperties));
                        }
                        return null;
                    }
                };
                descriptors.add(refItemDescriptor);
                descriptors.add(createMessagePaneSettings());
                if (blockProperties.getMainScreenProperties() != null)
                {
                    if (source.getParentCanvasContainer() != null && source.getParentCanvasContainer().getParnetCanvas() != null
                            && source.getParentCanvasContainer().getParnetCanvas().getType() == EJCanvasType.SPLIT)
                    {

                        final EJCanvasSplitOrientation orientation = source.getParentCanvasContainer().getParnetCanvas().getSplitOrientation();

                        final AbstractTextDescriptor widthHintDescriptor = new AbstractTextDescriptor("Weight")
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
                                    if (orientation == EJCanvasSplitOrientation.HORIZONTAL)
                                        blockProperties.getMainScreenProperties().setWidth(Integer.parseInt(value));
                                    else
                                        blockProperties.getMainScreenProperties().setHeight(Integer.parseInt(value));
                                }
                                catch (NumberFormatException e)
                                {
                                    if (orientation == EJCanvasSplitOrientation.HORIZONTAL)
                                        blockProperties.getMainScreenProperties().setWidth(0);
                                    else
                                        blockProperties.getMainScreenProperties().setHeight(0);
                                    if (text != null)
                                    {
                                        text.setText(getValue());
                                        text.selectAll();
                                    }
                                }
                                editor.setDirty(true);
                                treeSection.refresh(BlockCanvasNode.this);
                            }

                            @Override
                            public String getValue()
                            {
                                if (orientation == EJCanvasSplitOrientation.HORIZONTAL)
                                    return String.valueOf(blockProperties.getMainScreenProperties().getWidth());

                                return String.valueOf(blockProperties.getMainScreenProperties().getHeight());
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
                        AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
                        {

                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                                return new AbstractDescriptor<?>[] { widthHintDescriptor };
                            }

                            @Override
                            public void runOperation(AbstractOperation operation)
                            {
                                editor.execute(operation);

                            }
                        };
                        descriptors.add(layoutGroupDescriptor);
                        if (source.isObjectGroupRoot())
                        {

                            return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
                        }
                    }
                    else
                    {
                        AbstractGroupDescriptor groupDescriptor = MainDisplayItemGroup.getLayoutDescriptors(blockProperties.getMainScreenProperties(),
                                treeSection, this, editor);
                        descriptors.add(groupDescriptor);

                        if (source.isObjectGroupRoot())
                        {

                            return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), groupDescriptor };
                        }
                    }
                }
            }
            else
            {
                if (source.isObjectGroupRoot())
                {

                    return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
                }
                AbstractTextDropDownDescriptor canvasDescriptor = new AbstractTextDropDownDescriptor("Assigne Block")
                {

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();

                        Iterator<EJPluginBlockProperties> allBlocks = editor.getFormProperties().getBlockContainer().getAllBlockProperties().iterator();
                        while (allBlocks.hasNext())
                        {

                            EJPluginBlockProperties block = allBlocks.next();
                            if (block.getCanvasName() == null || block.getCanvasName().trim().length() == 0)
                                options.add(block.getName());
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
                        EJPluginBlockProperties newBlockProperties = editor.getFormProperties().getBlockContainer().getBlockProperties(value);
                        if (newBlockProperties != null)
                        {
                            newBlockProperties.setCanvasName(source.getName());
                        }
                        editor.setDirty(true);
                        treeSection.refresh(BlockCanvasNode.this);
                        if (treeSection.getDescriptorViewer() != null)
                            treeSection.getDescriptorViewer().showDetails(BlockCanvasNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return "";// IGNORE
                    }
                };

                descriptors.add(canvasDescriptor);
            }

            return descriptors.toArray(new AbstractDescriptor<?>[0]);
        }

        public void addOverview(StyledString styledString)
        {

            final EJPluginBlockProperties blockProperties = source.getPluginBlockProperties();
            if (blockProperties != null)
            {
                if (source.isImportFromObjectGroup())
                {
                    styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                    styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                    styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
                }
                styledString.append(" : ", StyledString.DECORATIONS_STYLER);
                styledString.append(blockProperties.getName(), StyledString.DECORATIONS_STYLER);
            }
            else
                super.addOverview(styledString);

        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            if (source.isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    if (cleanup)
                    {
                        cleanBlockAssignment(source);
                    }

                    source.getParentCanvasContainer().removeCanvasProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(BlockCanvasNode.this.getParent());

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    if (cleanup)
                    {
                        ReversibleOperation operation = new ReversibleOperation("Remove Canvas");
                        operation.add(new CanvasBlockAssignmentOperation(treeSection, source));
                        operation.add(new CanvasRemoveOperation(treeSection, source.getParentCanvasContainer(), source));
                        return operation;
                    }
                    return new CanvasRemoveOperation(treeSection, source.getParentCanvasContainer(), source);
                }
            };
        }
    }

    private class FormCanvasNode extends AbstractCanvas
    {

        public FormCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            if (source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }
            final FormCanvasNode node = FormCanvasNode.this;

            AbstractDropDownDescriptor<String> formNameDescriptor = new AbstractDropDownDescriptor<String>("Referred Form")
            {

                @Override
                public void setValue(String value)
                {

                    source.setReferredFormId(value);

                    editor.setDirty(true);

                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return source.getReferredFormId();
                }

                public String[] getOptions()
                {
                    IJavaProject javaProject = editor.getJavaProject();
                    if (javaProject != null)
                    {
                        List<String> formNames = FormsUtil.getFormNames(javaProject);
                        formNames.remove(editor.getFormProperties().getName());
                        formNames.add(0, "");
                        return formNames.toArray(new String[0]);
                    }
                    return new String[0];
                }

                public String getOptionText(String t)
                {
                    return t;
                }
            };

            AbstractGroupDescriptor layoutGroupDescriptor = createLayoutSettings(editor, treeSection, node);
            if (source.isObjectGroupRoot())
            {

                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
            }

            return new AbstractDescriptor<?>[] { formNameDescriptor, layoutGroupDescriptor, createMessagePaneSettings() };

        }

        public void addOverview(StyledString styledString)
        {

            if (source.isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }

        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            if (source.isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    source.getParentCanvasContainer().removeCanvasProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(FormCanvasNode.this.getParent());

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    return new CanvasRemoveOperation(treeSection, source.getParentCanvasContainer(), source);
                }
            };
        }
    }

    private static AbstractGroupDescriptor createLayoutSettings(final AbstractEJFormEditor editor, final FormDesignTreeSection treeSection,
            final AbstractCanvas node)
    {
        final AbstractTextDescriptor hSapnDescriptor = new AbstractTextDescriptor("Horizontal Span")
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
                    node.getSource().setHorizontalSpan(Integer.parseInt(value));
                }
                catch (NumberFormatException e)
                {
                    node.getSource().setHorizontalSpan(1);
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
                return String.valueOf(node.getSource().getHorizontalSpan());
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

        final AbstractTextDescriptor vSapnDescriptor = new AbstractTextDescriptor("Vertical Span")
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
                    node.getSource().setVerticalSpan(Integer.parseInt(value));
                }
                catch (NumberFormatException e)
                {
                    node.getSource().setVerticalSpan(1);
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
                return String.valueOf(node.getSource().getVerticalSpan());
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
            public Boolean getValue()
            {
                return node.getSource().canExpandHorizontally();
            }

            @Override
            public void setValue(Boolean value)
            {
                node.getSource().setExpandHorizontally(value.booleanValue());
                editor.setDirty(true);
                treeSection.refresh(node);
            }

            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);

            }
        };
        hExpandDescriptor.setText("Expand Horizontally");
        final AbstractDescriptor<Boolean> vExpandDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
        {

            @Override
            public Boolean getValue()
            {
                return node.getSource().canExpandVertically();
            }

            @Override
            public void setValue(Boolean value)
            {
                node.getSource().setExpandVertically(value.booleanValue());
                editor.setDirty(true);
                treeSection.refresh(node);
            }

            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);

            }
        };
        vExpandDescriptor.setText("Expand Vertically");

        final AbstractTextDescriptor widthHintDescriptor = new AbstractTextDescriptor("Width")
        {

            @Override
            public void setValue(String value)
            {
                try
                {
                    node.getSource().setWidth(Integer.parseInt(value));
                }
                catch (NumberFormatException e)
                {
                    node.getSource().setWidth(0);
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
                return String.valueOf(node.getSource().getWidth());
            }

            Text text;

            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);

            }

            @Override
            public void addEditorAssist(Control control)
            {

                text = (Text) control;
                text.addVerifyListener(new EJPluginEntireJNumberVerifier());

                super.addEditorAssist(control);
            }
        };
        final AbstractTextDescriptor heightHintDescriptor = new AbstractTextDescriptor("Height")
        {

            @Override
            public void setValue(String value)
            {
                try
                {
                    node.getSource().setHeight(Integer.parseInt(value));
                }
                catch (NumberFormatException e)
                {
                    node.getSource().setWidth(0);
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
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);

            }

            @Override
            public String getValue()
            {
                return String.valueOf(node.getSource().getHeight());
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

        if (node.getSource().isObjectGroupRoot())
        {
            hSapnDescriptor.setTooltip(String.format("Object Group Value: %s", "" + node.getSource().getHorizontalSpanOG()));
            vSapnDescriptor.setTooltip(String.format("Object Group Value: %s", "" + node.getSource().getVerticalSpanOG()));
            hExpandDescriptor.setTooltip(String.format("Object Group Value: %s", "" + node.getSource().canExpandHorizontallyOG()));
            vExpandDescriptor.setTooltip(String.format("Object Group Value: %s", "" + node.getSource().canExpandVerticallyOG()));
            hSapnDescriptor.setTooltip(String.format("Object Group Value: %s", "" + node.getSource().getHorizontalSpan()));
            widthHintDescriptor.setTooltip(String.format("Object Group Value: %s", "" + node.getSource().getWidthOG()));
            heightHintDescriptor.setTooltip(String.format("Object Group Value: %s", "" + node.getSource().getHeightOG()));
        }

        // MASTER
        AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
        {

            public Action[] getToolbarActions()
            {
                if (node.getSource().isObjectGroupRoot())
                {
                    return new Action[] { new Action("Reset Object Group Defaults", EJUIImages.DESC_OBJGROUP)
                            {

                                @Override
                                public void run()
                                {
                                    node.getSource().setWidth(node.getSource().getWidthOG());
                                    node.getSource().setHeight(node.getSource().getHeightOG());
                                    node.getSource().setExpandHorizontally(node.getSource().canExpandHorizontallyOG());
                                    node.getSource().setExpandVertically(node.getSource().canExpandVerticallyOG());
                                    node.getSource().setVerticalSpan(node.getSource().getVerticalSpanOG());
                                    node.getSource().setHorizontalSpan(node.getSource().getHorizontalSpanOG());
                                    node.getSource().setNumCols(node.getSource().getNumColsOG());
                                    editor.setDirty(true);
                                    treeSection.refresh(node);
                                    treeSection.refreshPreview();
                                    treeSection.showNodeDetails(node,true);
                                }
                            }

                    };
                }

                return new Action[0];
            }

            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);

            }

            public AbstractDescriptor<?>[] getDescriptors()
            {
                if (node.getSource().getParentCanvasContainer() != null && node.getSource().getParentCanvasContainer().getParnetCanvas() != null
                        && node.getSource().getParentCanvasContainer().getParnetCanvas().getType() == EJCanvasType.SPLIT)
                {
                    AbstractTextDescriptor descriptor = node.getSource().getParentCanvasContainer().getParnetCanvas()
                            .getSplitOrientation() == EJCanvasSplitOrientation.HORIZONTAL ? widthHintDescriptor : heightHintDescriptor;
                    descriptor.setText("Weight");
                    return new AbstractDescriptor<?>[] { descriptor, };
                }

                return new AbstractDescriptor<?>[] { hSapnDescriptor, vSapnDescriptor, hExpandDescriptor, vExpandDescriptor, widthHintDescriptor,
                        heightHintDescriptor };
            }
        };
        return layoutGroupDescriptor;
    }

    private void cleanBlockAssignment(EJPluginCanvasProperties source)
    {
        EJBlockProperties blockProperties = source.getBlockProperties();
        if (blockProperties != null)
        {
            ((EJPluginBlockProperties) blockProperties).setCanvasName("");
        }

        for (EJPluginCanvasProperties sub : source.getGroupCanvasContainer().getCanvasProperties())
        {
            cleanBlockAssignment(sub);
        }
        for (EJPluginCanvasProperties sub : source.getPopupCanvasContainer().getCanvasProperties())
        {
            cleanBlockAssignment(sub);
        }
        for (EJPluginCanvasProperties sub : source.getSplitCanvasContainer().getCanvasProperties())
        {
            cleanBlockAssignment(sub);
        }
        for (EJPluginTabPageProperties tabPageProperties : source.getTabPageContainer().getTabPageProperties())
        {
            for (EJPluginCanvasProperties sub : tabPageProperties.getContainedCanvases().getCanvasProperties())
            {
                cleanBlockAssignment(sub);
            }
        }
        for (EJPluginStackedPageProperties stackedPageProperties : source.getStackedPageContainer().getStackedPageProperties())
        {
            for (EJPluginCanvasProperties sub : stackedPageProperties.getContainedCanvases().getCanvasProperties())
            {
                cleanBlockAssignment(sub);
            }
        }
    }

    private class GroupCanvasNode extends AbstractCanvas implements NodeMoveProvider
    {

        public GroupCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            if (source.isObjectGroupRoot())
                return null;
            return super.getRenameProvider();
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return (!GroupCanvasNode.this.source.isImportFromObjectGroup()) && source instanceof EJPluginCanvasProperties
                    && (((EJPluginCanvasProperties) source).isObjectGroupRoot() || !((EJPluginCanvasProperties) source).isImportFromObjectGroup())
                    && ((EJPluginCanvasProperties) source).getType() != EJCanvasType.POPUP && !isAncestorCanvas(GroupCanvasNode.this.source, source);
        }

        @Override
        public Action[] getActions()
        {

            if (source.isImportFromObjectGroup() || source.isObjectGroupRoot())
            {
                return new Action[0];
            }

            return new Action[] { createNewCanvasAction(this, source.getGroupCanvasContainer(), false) };
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getGroupCanvasContainer().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.getGroupCanvasContainer().addCanvasProperties(index, (EJPluginCanvasProperties) dSource);
                }
            }
            else
                source.getGroupCanvasContainer().addCanvasProperties((EJPluginCanvasProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getGroupCanvasContainer().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new CanvasAddOperation(treeSection, source.getGroupCanvasContainer(), (EJPluginCanvasProperties) dSource, index);

                }
            }

            return new CanvasAddOperation(treeSection, source.getGroupCanvasContainer(), (EJPluginCanvasProperties) dSource, -1);

        }

        public void addOverview(StyledString styledString)
        {

            if (source.getGroupFrameTitle() != null && source.getGroupFrameTitle().trim().length() > 0)
            {
                if (source.isImportFromObjectGroup())
                {
                    styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                    styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                    styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
                }
                styledString.append(" : ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getGroupFrameTitle(), StyledString.COUNTER_STYLER);
            }
            else
                super.addOverview(styledString);

        }

        public boolean isLeaf()
        {
            return source.getGroupCanvasContainer().isEmpty();
        }

        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginCanvasProperties> items = source.getGroupCanvasContainer().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : items)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        nodes.add(new GroupCanvasNode(this, canvas));
                        break;
                    case SPLIT:
                        nodes.add(new SplitCanvasNode(this, canvas));
                        break;
                    case POPUP:
                        nodes.add(new PopupCanvasNode(this, canvas));
                        break;
                    case TAB:
                        nodes.add(new TabCanvasNode(this, canvas));
                        break;
                    case DRAWER:
                        nodes.add(new DrawerCanvasNode(this, canvas));
                        break;
                    case FORM:
                        nodes.add(new FormCanvasNode(this, canvas));
                        break;
                    case STACKED:
                        nodes.add(new StackedCanvasNode(this, canvas));
                        break;
                    case SEPARATOR:
                        nodes.add(new SeparatorCanvasNode(this, canvas));
                        break;
                    default:
                        nodes.add(new BlockCanvasNode(this, canvas));
                        break;
                }
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            if (!source.isObjectGroupRoot() && source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }
            final GroupCanvasNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();

            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Frame Title")
            {

                @Override
                public void setValue(String value)
                {
                    source.setGroupFrameTitle(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return source.getGroupFrameTitle();
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }
            };
            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
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
                        source.setNumCols(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setNumCols(1);
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
                    return String.valueOf(source.getNumCols());
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
                    return source.getDisplayGroupFrame();
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setDisplayGroupFrame(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            borderDescriptor.setText("Display Frame");

            AbstractGroupDescriptor layoutGroupDescriptor = createLayoutSettings(editor, treeSection, this);
            if (source.isObjectGroupRoot())
            {

                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
            }

            return new AbstractDescriptor<?>[] { nameDescriptor, borderDescriptor, colDescriptor, createMessagePaneSettings(), layoutGroupDescriptor };
        }

    }

    private class SplitCanvasNode extends AbstractCanvas implements NodeMoveProvider
    {

        public SplitCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return (!SplitCanvasNode.this.source.isImportFromObjectGroup()) && source instanceof EJPluginCanvasProperties
                    && (((EJPluginCanvasProperties) source).isObjectGroupRoot() || !((EJPluginCanvasProperties) source).isImportFromObjectGroup())
                    && ((EJPluginCanvasProperties) source).getType() != EJCanvasType.POPUP && !isAncestorCanvas(SplitCanvasNode.this.source, source);
        }

        @Override
        public Action[] getActions()
        {
            if (source.isImportFromObjectGroup())
            {
                return new Action[0];
            }
            return new Action[] { createNewCanvasAction(this, source.getSplitCanvasContainer(), false) };
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getSplitCanvasContainer().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.getSplitCanvasContainer().addCanvasProperties(index, (EJPluginCanvasProperties) dSource);
                }
            }
            else
                source.getSplitCanvasContainer().addCanvasProperties((EJPluginCanvasProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getSplitCanvasContainer().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new CanvasAddOperation(treeSection, source.getSplitCanvasContainer(), (EJPluginCanvasProperties) dSource, index);
                }
            }
            return new CanvasAddOperation(treeSection, source.getSplitCanvasContainer(), (EJPluginCanvasProperties) dSource, -1);
        }

        public void addOverview(StyledString styledString)
        {
            if (source.isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }
            if (source.getGroupFrameTitle() != null && source.getGroupFrameTitle().trim().length() > 0)
            {

                styledString.append(" : ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getGroupFrameTitle(), StyledString.COUNTER_STYLER);
            }
            else
                super.addOverview(styledString);

        }

        public boolean isLeaf()
        {
            return source.getSplitCanvasContainer().isEmpty();
        }

        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginCanvasProperties> items = source.getSplitCanvasContainer().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : items)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        nodes.add(new GroupCanvasNode(this, canvas));
                        break;
                    case SPLIT:
                        nodes.add(new SplitCanvasNode(this, canvas));
                        break;
                    case POPUP:
                        nodes.add(new PopupCanvasNode(this, canvas));
                        break;
                    case TAB:
                        nodes.add(new TabCanvasNode(this, canvas));
                        break;
                    case DRAWER:
                        nodes.add(new DrawerCanvasNode(this, canvas));
                        break;
                    case STACKED:
                        nodes.add(new StackedCanvasNode(this, canvas));
                        break;
                    case FORM:
                        nodes.add(new FormCanvasNode(this, canvas));
                        break;
                    case SEPARATOR:
                        nodes.add(new SeparatorCanvasNode(this, canvas));
                        break;
                    default:
                        nodes.add(new BlockCanvasNode(this, canvas));
                        break;
                }
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {

            if (source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }
            final SplitCanvasNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();

            AbstractDropDownDescriptor<EJCanvasSplitOrientation> orientationDescriptor = new AbstractDropDownDescriptor<EJCanvasSplitOrientation>("Orientation")
            {

                public EJCanvasSplitOrientation[] getOptions()
                {

                    return EJCanvasSplitOrientation.values();
                }

                public String getOptionText(EJCanvasSplitOrientation t)
                {
                    return t.toString();
                }

                public void setValue(EJCanvasSplitOrientation value)
                {
                    source.setSplitOrientation(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                public EJCanvasSplitOrientation getValue()
                {
                    return source.getSplitOrientation();
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }
            };

            AbstractGroupDescriptor layoutGroupDescriptor = createLayoutSettings(editor, treeSection, this);
            if (source.isObjectGroupRoot())
            {

                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
            }

            return new AbstractDescriptor<?>[] { orientationDescriptor, createMessagePaneSettings(), layoutGroupDescriptor };
        }

    }

    private class TabCanvasNode extends AbstractCanvas implements NodeMoveProvider
    {

        public TabCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        @Override
        public Action[] getActions()
        {
            if (source.isImportFromObjectGroup())
            {
                return new Action[0];
            }

            return new Action[] { new Action("New Tab Page")
            {
                @Override
                public void runWithEvent(Event event)
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), "New Tab Page", "Page Name", null, new IInputValidator()
                    {

                        public String isValid(String newText)
                        {
                            if (newText == null || newText.trim().length() == 0)
                                return "Page name can't be empty.";
                            if (source.getTabPageContainer().contains(newText.trim()))
                                return "page with this name already exists.";

                            return null;
                        }
                    });
                    if (dlg.open() == Window.OK)
                    {
                        final EJPluginTabPageProperties pageProp = new EJPluginTabPageProperties(source, dlg.getValue().trim());

                        source.getTabPageContainer().addTabPageProperties(pageProp);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh(TabCanvasNode.this);
                                treeSection.selectNodes(false, TabCanvasNode.this);
                                treeSection.expand(TabCanvasNode.this);
                                treeSection.selectNodes(true, (pageProp));

                            }
                        });
                    }
                }
            } };
        }

        public boolean isLeaf()
        {
            return source.getTabPageContainer().isEmpty();
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !TabCanvasNode.this.source.isImportFromObjectGroup() && source instanceof EJPluginTabPageProperties

                    && (TabCanvasNode.this.source.equals(((EJPluginTabPageProperties) source).getTabCanvasProperties()));
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginTabPageProperties> items = source.getTabPageContainer().getTabPageProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.getTabPageContainer().addTabPageProperties(index, (EJPluginTabPageProperties) dSource);
                }
            }
            else
                source.getTabPageContainer().addTabPageProperties((EJPluginTabPageProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginTabPageProperties> tabProperties = source.getTabPageContainer().getTabPageProperties();
            for (EJPluginTabPageProperties pageProperties : tabProperties)
            {
                nodes.add(new TabCanvasPageNode(this, pageProperties));
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if (!source.isObjectGroupRoot() && source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }
            final TabCanvasNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();

            AbstractGroupDescriptor layoutGroupDescriptor = createLayoutSettings(editor, treeSection, this);

            AbstractDropDownDescriptor<EJCanvasTabPosition> orientationDescriptor = new AbstractDropDownDescriptor<EJCanvasTabPosition>("Orientation")
            {

                public EJCanvasTabPosition[] getOptions()
                {

                    return EJCanvasTabPosition.values();
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public String getOptionText(EJCanvasTabPosition t)
                {
                    return t.toString();
                }

                public void setValue(EJCanvasTabPosition value)
                {
                    source.setTabPosition(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                public EJCanvasTabPosition getValue()
                {
                    return source.getTabPosition();
                }
            };

            if (source.isObjectGroupRoot())
            {

                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
            }

            return new AbstractDescriptor<?>[] { orientationDescriptor, createMessagePaneSettings(), layoutGroupDescriptor };
        }

    }
    private class DrawerCanvasNode extends AbstractCanvas implements NodeMoveProvider
    {
        
        public DrawerCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }
        
        @Override
        public Action[] getActions()
        {
            if (source.isImportFromObjectGroup())
            {
                return new Action[0];
            }
            
            return new Action[] { new Action("New Drawer Page")
            {
                @Override
                public void runWithEvent(Event event)
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), "New Drawer Page", "Page Name", null, new IInputValidator()
                    {
                        
                        public String isValid(String newText)
                        {
                            if (newText == null || newText.trim().length() == 0)
                                return "Page name can't be empty.";
                            if (source.getTabPageContainer().contains(newText.trim()))
                                return "page with this name already exists.";
                            
                            return null;
                        }
                    });
                    if (dlg.open() == Window.OK)
                    {
                        final EJPluginDrawerPageProperties pageProp = new EJPluginDrawerPageProperties(source, dlg.getValue().trim());
                        
                        source.getDrawerPageContainer().addDrawerPageProperties(pageProp);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {
                            
                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh(DrawerCanvasNode.this);
                                treeSection.selectNodes(false, DrawerCanvasNode.this);
                                treeSection.expand(DrawerCanvasNode.this);
                                treeSection.selectNodes(true, (pageProp));
                                
                            }
                        });
                    }
                }
            } };
        }
        
        public boolean isLeaf()
        {
            return source.getDrawerPageContainer().isEmpty();
        }
        
        public boolean canMove(Neighbor relation, Object source)
        {
            return !DrawerCanvasNode.this.source.isImportFromObjectGroup() && source instanceof EJPluginDrawerPageProperties
                    
                    && (DrawerCanvasNode.this.source.equals(((EJPluginDrawerPageProperties) source).getDrawerCanvasProperties()));
        }
        
        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginDrawerPageProperties> items = source.getDrawerPageContainer().getDrawerPageProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;
                    
                    source.getDrawerPageContainer().addDrawerPageProperties(index, (EJPluginDrawerPageProperties) dSource);
                }
            }
            else
                source.getDrawerPageContainer().addDrawerPageProperties((EJPluginDrawerPageProperties) dSource);
            
        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }
        
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginDrawerPageProperties> tabProperties = source.getDrawerPageContainer().getDrawerPageProperties();
            for (EJPluginDrawerPageProperties pageProperties : tabProperties)
            {
                nodes.add(new DrawerCanvasPageNode(this, pageProperties));
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }
        
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if (!source.isObjectGroupRoot() && source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }
            final DrawerCanvasNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();
            
            AbstractGroupDescriptor layoutGroupDescriptor = createLayoutSettings(editor, treeSection, this);
            
            AbstractDropDownDescriptor<EJCanvasDrawerPosition> orientationDescriptor = new AbstractDropDownDescriptor<EJCanvasDrawerPosition>("Open Direction")
            {
                
                public EJCanvasDrawerPosition[] getOptions()
                {
                    
                    return EJCanvasDrawerPosition.values();
                }
                
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                
                public String getOptionText(EJCanvasDrawerPosition t)
                {
                    return t.toString();
                }
                
                public void setValue(EJCanvasDrawerPosition value)
                {
                    source.setDrawerPosition(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }
                
                public EJCanvasDrawerPosition getValue()
                {
                    return source.getDrawerPosition();
                }
            };
            orientationDescriptor.setTooltip("The direction the drawer will open");
            
            if (source.isObjectGroupRoot())
            {
                
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
            }
            
            return new AbstractDescriptor<?>[] { orientationDescriptor, createMessagePaneSettings(), layoutGroupDescriptor };
        }
        
    }

    private class StackedCanvasNode extends AbstractCanvas implements NodeMoveProvider
    {

        public StackedCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        public boolean isLeaf()
        {
            return source.getStackedPageContainer().isEmpty();
        }

        @Override
        public Action[] getActions()
        {
            if (source.isImportFromObjectGroup())
            {
                return new Action[] {};
            }

            return new Action[] { new Action("New Stacked Page")
            {
                @Override
                public void runWithEvent(Event event)
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), "New Stacked Page", "Page Name", null, new IInputValidator()
                    {

                        public String isValid(String newText)
                        {
                            if (newText == null || newText.trim().length() == 0)
                                return "Page name can't be empty.";
                            if (source.getStackedPageContainer().contains(newText.trim()))
                                return "page with this name already exists.";

                            return null;
                        }
                    });
                    if (dlg.open() == Window.OK)
                    {
                        final EJPluginStackedPageProperties pageProp = new EJPluginStackedPageProperties(source, dlg.getValue().trim());

                        source.getStackedPageContainer().addStackedPageProperties(pageProp);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh(StackedCanvasNode.this);
                                treeSection.selectNodes(false, StackedCanvasNode.this);
                                treeSection.expand(StackedCanvasNode.this);
                                treeSection.selectNodes(true, (pageProp));

                            }
                        });
                    }
                }
            } };
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !StackedCanvasNode.this.source.isImportFromObjectGroup() && source instanceof EJPluginStackedPageProperties
                    && (StackedCanvasNode.this.source.equals(((EJPluginStackedPageProperties) source).getStackedCanvasProperties()));
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginStackedPageProperties> items = source.getStackedPageContainer().getStackedPageProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.getStackedPageContainer().addStackedPageProperties(index, (EJPluginStackedPageProperties) dSource);
                }
            }
            else
                source.getStackedPageContainer().addStackedPageProperties((EJPluginStackedPageProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object source, boolean before)
        {
            // TODO Auto-generated method stub
            return null;
        }

        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginStackedPageProperties> tabProperties = source.getStackedPageContainer().getStackedPageProperties();
            for (EJPluginStackedPageProperties pageProperties : tabProperties)
            {
                nodes.add(new StackedCanvasPageNode(this, pageProperties));
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if (!source.isObjectGroupRoot() && source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }

            final StackedCanvasNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();

            AbstractGroupDescriptor layoutGroupDescriptor = createLayoutSettings(editor, treeSection, this);

            AbstractDropDownDescriptor<String> orientationDescriptor = new AbstractDropDownDescriptor<String>("Default Page")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    options.add("");
                    List<EJPluginStackedPageProperties> allStackedPageProperties = source.getStackedPageContainer().getStackedPageProperties();
                    for (EJPluginStackedPageProperties properties : allStackedPageProperties)
                    {
                        options.add(properties.getName());
                    }
                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {
                    return t;
                }

                public void setValue(String value)
                {
                    source.setInitalStackedPageName(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                public String getValue()
                {
                    return source.getInitialStackedPageName();
                }
            };

            if (source.isObjectGroupRoot())
            {

                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source), layoutGroupDescriptor };
            }

            return new AbstractDescriptor<?>[] { orientationDescriptor, createMessagePaneSettings(), layoutGroupDescriptor };
        }

    }

    private class PopupCanvasNode extends AbstractCanvas implements NodeMoveProvider
    {

        public PopupCanvasNode(AbstractNode<?> parent, EJPluginCanvasProperties source)
        {
            super(parent, source);
        }

        public <S> S getAdapter(Class<S> adapter)
        {
            if (IFormPreviewProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(new FormCanvasPreviewImpl()
                {

                    public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                    {
                        // layout canvas preview
                        Composite pContent = new Composite(previewComposite, SWT.NONE);

                        EJPluginCanvasContainer container = source.getPopupCanvasContainer();
                        int width = source.getWidth();
                        int height = source.getHeight();
                        previewComposite.setContent(pContent);
                        setPreviewBackground(previewComposite, COLOR_LIGHT_YELLOW);
                        previewComposite.setExpandHorizontal(true);
                        previewComposite.setExpandVertical(true);

                        pContent.setLayout(new GridLayout());
                        setPreviewBackground(pContent, COLOR_LIGHT_YELLOW);

                        Composite layoutBody = new Composite(pContent, SWT.NONE);
                        layoutBody.setLayout(new GridLayout(source.getNumCols(), false));

                        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);

                        sectionData.widthHint = width;
                        sectionData.heightHint = height;
                        layoutBody.setLayoutData(sectionData);
                        setPreviewBackground(layoutBody, COLOR_LIGHT_YELLOW);

                        List<EJPluginCanvasProperties> items = container.getCanvasProperties();
                        for (EJPluginCanvasProperties canvas : items)
                        {
                            switch (canvas.getType())
                            {
                                case GROUP:
                                    createGroupLayout(layoutBody, canvas);
                                    break;
                                case SPLIT:
                                    createSplitLayout(layoutBody, canvas);
                                    break;
                                case POPUP:
                                    // ignore
                                    break;
                                case TAB:
                                    createTabLayout(layoutBody, canvas);
                                    break;
                                case DRAWER:
                                    createDrawerLayout(layoutBody, canvas);
                                    break;
                                case STACKED:
                                    createStackLayout(layoutBody, canvas);
                                    break;
                                default:
                                    createComponent(layoutBody, canvas);
                                    break;
                            }
                        }
                        if (width > 0 && height > 0)
                            previewComposite.setMinSize(width, height);
                        else
                            previewComposite.setMinSize(pContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

                    }
                });
            }
            return parent.getAdapter(adapter);
        }

        @Override
        public Action[] getActions()
        {
            if (source.isImportFromObjectGroup())
            {
                return new Action[0];
            }
            return new Action[] { createNewCanvasAction(this, source.getPopupCanvasContainer(), false) };
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return (!PopupCanvasNode.this.source.isImportFromObjectGroup()) && source instanceof EJPluginCanvasProperties
                    && (((EJPluginCanvasProperties) source).isObjectGroupRoot() || !((EJPluginCanvasProperties) source).isImportFromObjectGroup())
                    && ((EJPluginCanvasProperties) source).getType() != EJCanvasType.POPUP && !isAncestorCanvas(PopupCanvasNode.this.source, source);
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getPopupCanvasContainer().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.getPopupCanvasContainer().addCanvasProperties(index, (EJPluginCanvasProperties) dSource);
                }
            }
            else
                source.getPopupCanvasContainer().addCanvasProperties((EJPluginCanvasProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getPopupCanvasContainer().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new CanvasAddOperation(treeSection, source.getPopupCanvasContainer(), (EJPluginCanvasProperties) dSource, index);
                }
            }
            return new CanvasAddOperation(treeSection, source.getPopupCanvasContainer(), (EJPluginCanvasProperties) dSource, -1);
        }

        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginCanvasProperties> items = source.getPopupCanvasContainer().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : items)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        nodes.add(new GroupCanvasNode(this, canvas));
                        break;
                    case SPLIT:
                        nodes.add(new SplitCanvasNode(this, canvas));
                        break;
                    case POPUP:
                        nodes.add(new PopupCanvasNode(this, canvas));
                        break;
                    case TAB:
                        nodes.add(new TabCanvasNode(this, canvas));
                        break;
                    case DRAWER:
                        nodes.add(new DrawerCanvasNode(this, canvas));
                        break;
                    case STACKED:
                        nodes.add(new StackedCanvasNode(this, canvas));
                        break;
                    case FORM:
                        nodes.add(new FormCanvasNode(this, canvas));
                        break;
                    case SEPARATOR:
                        nodes.add(new SeparatorCanvasNode(this, canvas));
                        break;
                    default:
                        nodes.add(new BlockCanvasNode(this, canvas));
                        break;
                }
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean isLeaf()
        {
            return source.getPopupCanvasContainer().isEmpty();
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if (!source.isObjectGroupRoot() && source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source) };
            }
            final PopupCanvasNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();
            if (source.getParentCanvasContainer() != null && source.getParentCanvasContainer().getParnetCanvas() != null
                    && source.getParentCanvasContainer().getParnetCanvas().getType() == EJCanvasType.SPLIT)
            {
                final EJCanvasSplitOrientation orientation = source.getParentCanvasContainer().getParnetCanvas().getSplitOrientation();
                final AbstractTextDescriptor widthHintDescriptor = new AbstractTextDescriptor("Weight")
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
                            if (orientation == EJCanvasSplitOrientation.HORIZONTAL)
                                source.setWidth(Integer.parseInt(value));
                            else
                                source.setHeight(Integer.parseInt(value));
                        }
                        catch (NumberFormatException e)
                        {
                            if (orientation == EJCanvasSplitOrientation.HORIZONTAL)
                                source.setWidth(0);
                            else
                                source.setHeight(0);
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
                        if (orientation == EJCanvasSplitOrientation.HORIZONTAL)
                            return String.valueOf(source.getWidth());

                        return String.valueOf(source.getHeight());
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
                return new AbstractDescriptor<?>[] { widthHintDescriptor };
            }

            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Frame Title")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    source.setPopupPageTitle(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return source.getPopupPageTitle();
                }
            };
            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
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
                        source.setNumCols(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setNumCols(1);
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
                    return String.valueOf(source.getNumCols());
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

            final AbstractTextDescriptor widthHintDescriptor = new AbstractTextDescriptor("Width")
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
                        source.setWidth(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setWidth(0);
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
                    return String.valueOf(source.getWidth());
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
            final AbstractTextDescriptor heightHintDescriptor = new AbstractTextDescriptor("Height")
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
                        source.setHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setWidth(0);
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
                    return String.valueOf(source.getHeight());
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

            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { widthHintDescriptor, heightHintDescriptor };
                }
            };
            final AbstractTextDescriptor button1Descriptor = new AbstractTextDescriptor("Button - 1")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    source.setButtonOneText(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return source.getButtonOneText();
                }
            };
            final AbstractTextDescriptor button2Descriptor = new AbstractTextDescriptor("Button - 2")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    source.setButtonTwoText(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return source.getButtonTwoText();
                }
            };
            final AbstractTextDescriptor button3Descriptor = new AbstractTextDescriptor("Button - 3")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    source.setButtonThreeText(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return source.getButtonThreeText();
                }
            };

            final AbstractDropDownDescriptor<EJPopupButton> defaultButtonDescriptor = new AbstractDropDownDescriptor<EJPopupButton>("Default Button")
            {

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public EJPopupButton[] getOptions()
                {

                    return EJPopupButton.values();
                }

                @Override
                public void setValue(EJPopupButton value)
                {
                    source.setDefaultPopupButton(value);

                    editor.setDirty(true);
                }

                @Override
                public EJPopupButton getValue()
                {
                    return source.getDefaultPopupButton();
                }

                public String getOptionText(EJPopupButton t)
                {
                    return t.toString();
                }
            };

            AbstractGroupDescriptor actionsGroupDescriptor = new AbstractGroupDescriptor("Actions")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { button1Descriptor, button2Descriptor, button3Descriptor, defaultButtonDescriptor };
                }
            };

            return new AbstractDescriptor<?>[] { nameDescriptor, colDescriptor, createMessagePaneSettings(), layoutGroupDescriptor, actionsGroupDescriptor };
        }

    }

    private class TabCanvasPageNode extends AbstractNode<EJPluginTabPageProperties> implements Neighbor, Movable, NodeOverview, NodeMoveProvider
    {

        public TabCanvasPageNode(AbstractNode<?> parent, EJPluginTabPageProperties source)
        {
            super(parent, source);
        }

        @Override
        public String getName()
        {
            return source.getName();
        }

        @Override
        public Image getImage()
        {
            return source.getTabCanvasProperties().isImportFromObjectGroup() ? TAB_PAGE_REF : TAB_PAGE;

        }

        public <S> S getAdapter(Class<S> adapter)
        {
            return CanvasGroupNode.this.getAdapter(adapter);
        }

        @Override
        public Action[] getActions()
        {
            if (source.getTabCanvasProperties().isImportFromObjectGroup())
            {
                return new Action[0];
            }
            return new Action[] { createNewCanvasAction(this, source.getContainedCanvases(), false) };
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            if (source.getTabCanvasProperties().isImportFromObjectGroup())
            {
                return null;
            }

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    source.getTabCanvasProperties().getTabPageContainer().removeTabPageProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(CanvasGroupNode.this.getParent());

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
            if (source.getTabCanvasProperties().isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Tab Page [%s]", getName()), "Block Name",
                            getName(), new IInputValidator()
                    {

                        public String isValid(String newText)
                        {
                            if (newText == null || newText.trim().length() == 0)
                                return "Tab page name can't be empty.";
                            if (getName().equals(newText.trim()))
                                return "";
                            if (getName().equalsIgnoreCase(newText.trim()))
                                return null;
                            if (source.getTabCanvasProperties().getTabPageContainer().contains(newText.trim()))
                                return "Tab Page with this name already exists.";
                            return null;
                        }
                    });
                    if (dlg.open() == Window.OK)
                    {
                        String newName = dlg.getValue().trim();
                        source.internalSetName(newName);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                treeSection.getEditor().setDirty(true);
                                treeSection.refresh(TabCanvasPageNode.this);

                            }
                        });
                    }

                }
            };
        }

        public void addOverview(StyledString styledString)
        {
            if (source.getTabCanvasProperties().isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getTabCanvasProperties().getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }
            if (source.getPageTitle() != null && source.getPageTitle().trim().length() > 0)
            {
                styledString.append(" : ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getPageTitle(), StyledString.COUNTER_STYLER);
            }

        }

        public boolean canMove()
        {
            return !source.getTabCanvasProperties().isImportFromObjectGroup();
        }

        public Object getNeighborSource()
        {
            return source;
        }

        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : items)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        nodes.add(new GroupCanvasNode(this, canvas));
                        break;
                    case SPLIT:
                        nodes.add(new SplitCanvasNode(this, canvas));
                        break;
                    case POPUP:
                        nodes.add(new PopupCanvasNode(this, canvas));
                        break;
                    case TAB:
                        nodes.add(new TabCanvasNode(this, canvas));
                        break;
                    case DRAWER:
                        nodes.add(new DrawerCanvasNode(this, canvas));
                        break;
                    case STACKED:
                        nodes.add(new StackedCanvasNode(this, canvas));
                        break;
                    case FORM:
                        nodes.add(new FormCanvasNode(this, canvas));
                        break;
                    case SEPARATOR:
                        nodes.add(new SeparatorCanvasNode(this, canvas));
                        break;
                    default:
                        nodes.add(new BlockCanvasNode(this, canvas));
                        break;
                }
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean isLeaf()
        {
            return source.getContainedCanvases().isEmpty();
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !TabCanvasPageNode.this.source.getTabCanvasProperties().isImportFromObjectGroup() && source instanceof EJPluginCanvasProperties
                    && !((EJPluginCanvasProperties) source).isImportFromObjectGroup() && ((EJPluginCanvasProperties) source).getType() != EJCanvasType.POPUP
                    && !isAncestorCanvas(TabCanvasPageNode.this.source.getTabCanvasProperties(), source);
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.getContainedCanvases().addCanvasProperties(index, (EJPluginCanvasProperties) dSource);
                }
            }
            else
                source.getContainedCanvases().addCanvasProperties((EJPluginCanvasProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new CanvasAddOperation(treeSection, source.getContainedCanvases(), (EJPluginCanvasProperties) dSource, index);
                }
            }
            return new CanvasAddOperation(treeSection, source.getContainedCanvases(), (EJPluginCanvasProperties) dSource, -1);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if (source.getTabCanvasProperties().isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source.getTabCanvasProperties()) };
            }
            final TabCanvasPageNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Page Title")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    source.setPageTitle(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

                @Override
                public String getValue()
                {
                    return source.getPageTitle();
                }
            };
            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
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
                        source.setNumCols(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setNumCols(1);
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
                    return String.valueOf(source.getNumCols());
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
            AbstractDescriptor<Boolean> enableDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return source.isEnabled();
                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setEnabled(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            enableDescriptor.setText("Enable");
            AbstractDescriptor<Boolean> visibleDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public Boolean getValue()
                {
                    return source.isVisible();
                }

                @Override
                public void setValue(Boolean value)
                {
                    source.setVisible(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }

            };
            visibleDescriptor.setText("Visible");
            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Navigation Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    AbstractTextDropDownDescriptor naviBlockDescriptor = new AbstractTextDropDownDescriptor("Navigation Block")
                    {
                        @Override
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);

                        }

                        @Override
                        public boolean hasLableLink()
                        {
                            return true;
                        }

                        @Override
                        public String lableLinkActivator()
                        {
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer().getBlockProperties(getValue());
                            if (blockProperties != null)
                            {

                                treeSection.selectNodes(false, editor.getFormProperties().getBlockContainer());
                                treeSection.expand(editor.getFormProperties().getBlockContainer());
                                treeSection.selectNodes(false, (blockProperties));
                            }
                            return null;
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            options.add("");
                            Collection<EJCanvasProperties> canvasesAssignedTabPage = EJPluginCanvasRetriever
                                    .retriveAllBlockCanvasesAssignedTabPage(editor.formProperties, source);
                            for (EJCanvasProperties properties : canvasesAssignedTabPage)
                            {
                                EJBlockProperties block = properties.getBlockProperties();
                                if (block != null)
                                {
                                    options.add(block.getName());
                                }
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
                            source.setFirstNavigationalBlock(value);
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer().getBlockProperties(getValue());
                            if (blockProperties != null)
                            {
                                EJPluginBlockItemProperties itemProperties = blockProperties.getItemContainer()
                                        .getItemProperties(source.getFirstNavigationalItem());
                                if (itemProperties == null)
                                {
                                    source.setFirstNavigationalItem("");
                                }
                            }
                            editor.setDirty(true);
                            treeSection.refresh(node);
                            if (treeSection.getDescriptorViewer() != null)
                                treeSection.getDescriptorViewer().showDetails(node);
                        }

                        @Override
                        public String getValue()
                        {
                            return source.getFirstNavigationalBlock();
                        }
                    };
                    AbstractTextDropDownDescriptor naviItemDescriptor = new AbstractTextDropDownDescriptor("Navigation Item")
                    {
                        @Override
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);

                        }

                        @Override
                        public boolean hasLableLink()
                        {
                            return true;
                        }

                        @Override
                        public String lableLinkActivator()
                        {
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer()
                                    .getBlockProperties(source.getFirstNavigationalBlock());
                            if (blockProperties != null)
                            {
                                EJPluginBlockItemProperties itemProperties = blockProperties.getItemContainer()
                                        .getItemProperties(source.getFirstNavigationalItem());
                                if (itemProperties != null)
                                {
                                    Object findNode = (editor.getFormProperties().getBlockContainer());
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                    findNode = (blockProperties);
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);

                                    findNode = (blockProperties.getItemContainer());
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                    treeSection.selectNodes(false, (itemProperties));
                                }

                            }
                            return null;
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            options.add("");
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer()
                                    .getBlockProperties(source.getFirstNavigationalBlock());
                            if (blockProperties != null)
                            {
                                List<EJPluginBlockItemProperties> allItemProperties = blockProperties.getItemContainer().getAllItemProperties();
                                for (EJPluginBlockItemProperties itemProperties : allItemProperties)
                                {
                                    options.add(itemProperties.getName());
                                }

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
                            source.setFirstNavigationalItem(value);
                            editor.setDirty(true);
                            treeSection.refresh(node);
                        }

                        @Override
                        public String getValue()
                        {
                            return source.getFirstNavigationalItem();
                        }
                    };

                    return new AbstractDescriptor<?>[] { naviBlockDescriptor, naviItemDescriptor };
                }
            };
            return new AbstractDescriptor<?>[] { nameDescriptor, colDescriptor, enableDescriptor, visibleDescriptor, layoutGroupDescriptor };
        }

    }
    private class DrawerCanvasPageNode extends AbstractNode<EJPluginDrawerPageProperties> implements Neighbor, Movable, NodeOverview, NodeMoveProvider
    {
        
        public DrawerCanvasPageNode(AbstractNode<?> parent, EJPluginDrawerPageProperties source)
        {
            super(parent, source);
        }
        
        @Override
        public String getName()
        {
            return source.getName();
        }
        
        @Override
        public Image getImage()
        {
            return source.getDrawerCanvasProperties().isImportFromObjectGroup() ? DRAWER_PAGE_REF : DRAWER_PAGE;
            
        }
        
        public <S> S getAdapter(Class<S> adapter)
        {
            return CanvasGroupNode.this.getAdapter(adapter);
        }
        
        @Override
        public Action[] getActions()
        {
            if (source.getDrawerCanvasProperties().isImportFromObjectGroup())
            {
                return new Action[0];
            }
            return new Action[] { createNewCanvasAction(this, source.getContainedCanvases(), false) };
        }
        
        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            if (source.getDrawerCanvasProperties().isImportFromObjectGroup())
            {
                return null;
            }
            
            return new INodeDeleteProvider()
            {
                
                public void delete(boolean cleanup)
                {
                    
                    source.getDrawerCanvasProperties().getDrawerPageContainer().removeDrawerPageProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(CanvasGroupNode.this.getParent());
                    
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
            if (source.getDrawerCanvasProperties().isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeRenameProvider()
            {
                
                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Drawer Page [%s]", getName()), "Block Name",
                            getName(), new IInputValidator()
                    {
                        
                        public String isValid(String newText)
                        {
                            if (newText == null || newText.trim().length() == 0)
                                return "Drawer page name can't be empty.";
                            if (getName().equals(newText.trim()))
                                return "";
                            if (getName().equalsIgnoreCase(newText.trim()))
                                return null;
                            if (source.getDrawerCanvasProperties().getTabPageContainer().contains(newText.trim()))
                                return "Drawer Page with this name already exists.";
                            return null;
                        }
                    });
                    if (dlg.open() == Window.OK)
                    {
                        String newName = dlg.getValue().trim();
                        source.internalSetName(newName);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {
                            
                            public void run()
                            {
                                treeSection.getEditor().setDirty(true);
                                treeSection.refresh(DrawerCanvasPageNode.this);
                                
                            }
                        });
                    }
                    
                }
            };
        }
        
        public void addOverview(StyledString styledString)
        {
            if (source.getDrawerCanvasProperties().isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getDrawerCanvasProperties().getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }
            if (source.getPageTitle() != null && source.getPageTitle().trim().length() > 0)
            {
                styledString.append(" : ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getPageTitle(), StyledString.COUNTER_STYLER);
            }
            
        }
        
        public boolean canMove()
        {
            return !source.getDrawerCanvasProperties().isImportFromObjectGroup();
        }
        
        public Object getNeighborSource()
        {
            return source;
        }
        
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : items)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        nodes.add(new GroupCanvasNode(this, canvas));
                        break;
                    case SPLIT:
                        nodes.add(new SplitCanvasNode(this, canvas));
                        break;
                    case POPUP:
                        nodes.add(new PopupCanvasNode(this, canvas));
                        break;
                    case TAB:
                        nodes.add(new TabCanvasNode(this, canvas));
                        break;
                    case DRAWER:
                        nodes.add(new DrawerCanvasNode(this, canvas));
                        break;
                    case STACKED:
                        nodes.add(new StackedCanvasNode(this, canvas));
                        break;
                    case FORM:
                        nodes.add(new FormCanvasNode(this, canvas));
                        break;
                    case SEPARATOR:
                        nodes.add(new SeparatorCanvasNode(this, canvas));
                        break;
                    default:
                        nodes.add(new BlockCanvasNode(this, canvas));
                        break;
                }
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }
        
        public boolean isLeaf()
        {
            return source.getContainedCanvases().isEmpty();
        }
        
        public boolean canMove(Neighbor relation, Object source)
        {
            return !DrawerCanvasPageNode.this.source.getDrawerCanvasProperties().isImportFromObjectGroup() && source instanceof EJPluginCanvasProperties
                    && !((EJPluginCanvasProperties) source).isImportFromObjectGroup() && ((EJPluginCanvasProperties) source).getType() != EJCanvasType.POPUP
                    && !isAncestorCanvas(DrawerCanvasPageNode.this.source.getDrawerCanvasProperties(), source);
        }
        
        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;
                    
                    source.getContainedCanvases().addCanvasProperties(index, (EJPluginCanvasProperties) dSource);
                }
            }
            else
                source.getContainedCanvases().addCanvasProperties((EJPluginCanvasProperties) dSource);
            
        }
        
        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;
                    
                    return new CanvasAddOperation(treeSection, source.getContainedCanvases(), (EJPluginCanvasProperties) dSource, index);
                }
            }
            return new CanvasAddOperation(treeSection, source.getContainedCanvases(), (EJPluginCanvasProperties) dSource, -1);
        }
        
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if (source.getDrawerCanvasProperties().isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source.getDrawerCanvasProperties()) };
            }
            final DrawerCanvasPageNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();
            AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Page Title")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                
                @Override
                public void setValue(String value)
                {
                    source.setPageTitle(value);
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }
                
                @Override
                public String getValue()
                {
                    return source.getPageTitle();
                }
            };
            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
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
                        source.setNumCols(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setNumCols(1);
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
                    return String.valueOf(source.getNumCols());
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
            AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Drawer Width")
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
                        source.setDrawerWidth(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setDrawerWidth(1);
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
                    return String.valueOf(source.getDrawerWidth());
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
            AbstractDescriptor<Boolean> enableDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                
                @Override
                public Boolean getValue()
                {
                    return source.isEnabled();
                }
                
                @Override
                public void setValue(Boolean value)
                {
                    source.setEnabled(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }
                
            };
            enableDescriptor.setText("Enable");
            AbstractDescriptor<Boolean> visibleDescriptor = new AbstractDescriptor<Boolean>(AbstractDescriptor.TYPE.BOOLEAN)
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                
                @Override
                public Boolean getValue()
                {
                    return source.isVisible();
                }
                
                @Override
                public void setValue(Boolean value)
                {
                    source.setVisible(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(node);
                }
                
            };
            visibleDescriptor.setText("Visible");
            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Navigation Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);
                    
                }
                
                public AbstractDescriptor<?>[] getDescriptors()
                {
                    AbstractTextDropDownDescriptor naviBlockDescriptor = new AbstractTextDropDownDescriptor("Navigation Block")
                    {
                        @Override
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);
                            
                        }
                        
                        @Override
                        public boolean hasLableLink()
                        {
                            return true;
                        }
                        
                        @Override
                        public String lableLinkActivator()
                        {
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer().getBlockProperties(getValue());
                            if (blockProperties != null)
                            {
                                
                                treeSection.selectNodes(false, editor.getFormProperties().getBlockContainer());
                                treeSection.expand(editor.getFormProperties().getBlockContainer());
                                treeSection.selectNodes(false, (blockProperties));
                            }
                            return null;
                        }
                        
                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            options.add("");
                            Collection<EJCanvasProperties> canvasesAssignedTabPage = EJPluginCanvasRetriever
                                    .retriveAllBlockCanvasesAssignedDrawerPage(editor.formProperties, source);
                            for (EJCanvasProperties properties : canvasesAssignedTabPage)
                            {
                                EJBlockProperties block = properties.getBlockProperties();
                                if (block != null)
                                {
                                    options.add(block.getName());
                                }
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
                            source.setFirstNavigationalBlock(value);
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer().getBlockProperties(getValue());
                            if (blockProperties != null)
                            {
                                EJPluginBlockItemProperties itemProperties = blockProperties.getItemContainer()
                                        .getItemProperties(source.getFirstNavigationalItem());
                                if (itemProperties == null)
                                {
                                    source.setFirstNavigationalItem("");
                                }
                            }
                            editor.setDirty(true);
                            treeSection.refresh(node);
                            if (treeSection.getDescriptorViewer() != null)
                                treeSection.getDescriptorViewer().showDetails(node);
                        }
                        
                        @Override
                        public String getValue()
                        {
                            return source.getFirstNavigationalBlock();
                        }
                    };
                    AbstractTextDropDownDescriptor naviItemDescriptor = new AbstractTextDropDownDescriptor("Navigation Item")
                    {
                        @Override
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);
                            
                        }
                        
                        @Override
                        public boolean hasLableLink()
                        {
                            return true;
                        }
                        
                        @Override
                        public String lableLinkActivator()
                        {
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer()
                                    .getBlockProperties(source.getFirstNavigationalBlock());
                            if (blockProperties != null)
                            {
                                EJPluginBlockItemProperties itemProperties = blockProperties.getItemContainer()
                                        .getItemProperties(source.getFirstNavigationalItem());
                                if (itemProperties != null)
                                {
                                    Object findNode = (editor.getFormProperties().getBlockContainer());
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                    findNode = (blockProperties);
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                    
                                    findNode = (blockProperties.getItemContainer());
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                    treeSection.selectNodes(false, (itemProperties));
                                }
                                
                            }
                            return null;
                        }
                        
                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            options.add("");
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer()
                                    .getBlockProperties(source.getFirstNavigationalBlock());
                            if (blockProperties != null)
                            {
                                List<EJPluginBlockItemProperties> allItemProperties = blockProperties.getItemContainer().getAllItemProperties();
                                for (EJPluginBlockItemProperties itemProperties : allItemProperties)
                                {
                                    options.add(itemProperties.getName());
                                }
                                
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
                            source.setFirstNavigationalItem(value);
                            editor.setDirty(true);
                            treeSection.refresh(node);
                        }
                        
                        @Override
                        public String getValue()
                        {
                            return source.getFirstNavigationalItem();
                        }
                    };
                    
                    return new AbstractDescriptor<?>[] { naviBlockDescriptor, naviItemDescriptor };
                }
            };
            return new AbstractDescriptor<?>[] { nameDescriptor,widthDescriptor, colDescriptor, enableDescriptor, visibleDescriptor, layoutGroupDescriptor };
        }
        
    }

    private class StackedCanvasPageNode extends AbstractNode<EJPluginStackedPageProperties> implements Neighbor, Movable, NodeOverview, NodeMoveProvider
    {

        public StackedCanvasPageNode(AbstractNode<?> parent, EJPluginStackedPageProperties source)
        {
            super(parent, source);
        }

        @Override
        public String getName()
        {
            return source.getName();
        }

        @Override
        public Image getImage()
        {
            return source.getStackedCanvasProperties().isImportFromObjectGroup() ? TAB_PAGE_REF : TAB_PAGE;

        }

        public <S> S getAdapter(Class<S> adapter)
        {
            return CanvasGroupNode.this.getAdapter(adapter);
        }

        @Override
        public Action[] getActions()
        {
            if (source.getStackedCanvasProperties().isImportFromObjectGroup())
            {
                return new Action[0];
            }

            return new Action[] { createNewCanvasAction(this, source.getContainedCanvases(), false) };
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {
            if (source.getStackedCanvasProperties().isImportFromObjectGroup())
            {
                return null;
            }
            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    source.getStackedCanvasProperties().getStackedPageContainer().removeStackedPageProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(CanvasGroupNode.this.getParent());

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
            if (source.getStackedCanvasProperties().isImportFromObjectGroup())
            {
                return null;
            }

            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Stacked Page [%s]", getName()), "Block Name",
                            getName(), new IInputValidator()
                    {

                        public String isValid(String newText)
                        {
                            if (newText == null || newText.trim().length() == 0)
                                return "Stacked page name can't be empty.";
                            if (getName().equals(newText.trim()))
                                return "";
                            if (getName().equalsIgnoreCase(newText.trim()))
                                return null;
                            if (source.getStackedCanvasProperties().getStackedPageContainer().contains(newText.trim()))
                                return "Stacked Page with this name already exists.";
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
                                treeSection.refresh(StackedCanvasPageNode.this);

                            }
                        });
                    }

                }
            };
        }

        public void addOverview(StyledString styledString)
        {
            if (source.getStackedCanvasProperties().isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getStackedCanvasProperties().getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }
        }

        public boolean canMove()
        {
            return !source.getStackedCanvasProperties().isImportFromObjectGroup();
        }

        public Object getNeighborSource()
        {
            return source;
        }

        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
            List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
            for (EJPluginCanvasProperties canvas : items)
            {
                switch (canvas.getType())
                {
                    case GROUP:
                        nodes.add(new GroupCanvasNode(this, canvas));
                        break;
                    case SPLIT:
                        nodes.add(new SplitCanvasNode(this, canvas));
                        break;
                    case POPUP:
                        nodes.add(new PopupCanvasNode(this, canvas));
                        break;
                    case TAB:
                        nodes.add(new TabCanvasNode(this, canvas));
                        break;
                    case DRAWER:
                        nodes.add(new DrawerCanvasNode(this, canvas));
                        break;
                    case STACKED:
                        nodes.add(new StackedCanvasNode(this, canvas));
                        break;
                    case FORM:
                        nodes.add(new FormCanvasNode(this, canvas));
                        break;
                    case SEPARATOR:
                        nodes.add(new SeparatorCanvasNode(this, canvas));
                        break;
                    default:
                        nodes.add(new BlockCanvasNode(this, canvas));
                        break;
                }
            }
            return nodes.toArray(new AbstractNode<?>[0]);
        }

        public boolean isLeaf()
        {
            return source.getContainedCanvases().isEmpty();
        }

        public boolean canMove(Neighbor relation, Object source)
        {
            return !StackedCanvasPageNode.this.source.getStackedCanvasProperties().isImportFromObjectGroup() && source instanceof EJPluginCanvasProperties
                    && !((EJPluginCanvasProperties) source).isImportFromObjectGroup() && ((EJPluginCanvasProperties) source).getType() != EJCanvasType.POPUP
                    && !isAncestorCanvas(StackedCanvasPageNode.this.source.getStackedCanvasProperties(), source);
        }

        public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    source.getContainedCanvases().addCanvasProperties(index, (EJPluginCanvasProperties) dSource);
                }
            }
            else
                source.getContainedCanvases().addCanvasProperties((EJPluginCanvasProperties) dSource);

        }

        public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
        {
            if (neighbor != null)
            {
                Object methodNeighbor = neighbor.getNeighborSource();
                List<EJPluginCanvasProperties> items = source.getContainedCanvases().getCanvasProperties();
                if (items.contains(methodNeighbor))
                {
                    int index = items.indexOf(methodNeighbor);
                    if (!before)
                        index++;

                    return new CanvasAddOperation(treeSection, source.getContainedCanvases(), (EJPluginCanvasProperties) dSource, index);
                }
            }
            return new CanvasAddOperation(treeSection, source.getContainedCanvases(), (EJPluginCanvasProperties) dSource, -1);
        }

        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            if (source.getStackedCanvasProperties().isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { getObjectGroupDescriptor(source.getStackedCanvasProperties()) };
            }
            final StackedCanvasPageNode node = this;
            final AbstractEJFormEditor editor = treeSection.getEditor();

            AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
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
                        source.setNumCols(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setNumCols(1);
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
                    return String.valueOf(source.getNumCols());
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

            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Navigation Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    AbstractTextDropDownDescriptor naviBlockDescriptor = new AbstractTextDropDownDescriptor("Navigation Block")
                    {
                        @Override
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);

                        }

                        @Override
                        public boolean hasLableLink()
                        {
                            return true;
                        }

                        @Override
                        public String lableLinkActivator()
                        {
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer().getBlockProperties(getValue());
                            if (blockProperties != null)
                            {
                                Object findNode = (editor.getFormProperties().getBlockContainer());
                                treeSection.selectNodes(false, findNode);
                                treeSection.expand(findNode);
                                treeSection.selectNodes(false, (blockProperties));
                            }
                            return null;
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            options.add("");
                            Collection<EJCanvasProperties> canvasesAssignedTabPage = EJPluginCanvasRetriever
                                    .retriveAllBlockCanvasesAssignedStackedPage(editor.formProperties, source);
                            for (EJCanvasProperties properties : canvasesAssignedTabPage)
                            {
                                EJBlockProperties block = properties.getBlockProperties();
                                if (block != null)
                                {
                                    options.add(block.getName());
                                }
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
                            source.setFirstNavigationalBlock(value);
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer().getBlockProperties(getValue());
                            if (blockProperties != null)
                            {
                                EJPluginBlockItemProperties itemProperties = blockProperties.getItemContainer()
                                        .getItemProperties(source.getFirstNavigationalItem());
                                if (itemProperties == null)
                                {
                                    source.setFirstNavigationalItem("");
                                }
                            }
                            editor.setDirty(true);
                            treeSection.refresh(node);
                            if (treeSection.getDescriptorViewer() != null)
                                treeSection.getDescriptorViewer().showDetails(node);
                        }

                        @Override
                        public String getValue()
                        {
                            return source.getFirstNavigationalBlock();
                        }
                    };
                    AbstractTextDropDownDescriptor naviItemDescriptor = new AbstractTextDropDownDescriptor("Navigation Item")
                    {
                        @Override
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);

                        }

                        @Override
                        public boolean hasLableLink()
                        {
                            return true;
                        }

                        @Override
                        public String lableLinkActivator()
                        {
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer()
                                    .getBlockProperties(source.getFirstNavigationalBlock());
                            if (blockProperties != null)
                            {
                                EJPluginBlockItemProperties itemProperties = blockProperties.getItemContainer()
                                        .getItemProperties(source.getFirstNavigationalItem());
                                if (itemProperties != null)
                                {
                                    Object findNode = (editor.getFormProperties().getBlockContainer());
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                    findNode = (blockProperties);
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);

                                    findNode = (blockProperties.getItemContainer());
                                    treeSection.selectNodes(false, findNode);
                                    treeSection.expand(findNode);
                                    treeSection.selectNodes(false, (itemProperties));
                                }

                            }
                            return null;
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            options.add("");
                            EJPluginBlockProperties blockProperties = editor.getFormProperties().getBlockContainer()
                                    .getBlockProperties(source.getFirstNavigationalBlock());
                            if (blockProperties != null)
                            {
                                List<EJPluginBlockItemProperties> allItemProperties = blockProperties.getItemContainer().getAllItemProperties();
                                for (EJPluginBlockItemProperties itemProperties : allItemProperties)
                                {
                                    options.add(itemProperties.getName());
                                }

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
                            source.setFirstNavigationalItem(value);
                            editor.setDirty(true);
                            treeSection.refresh(node);
                        }

                        @Override
                        public String getValue()
                        {
                            return source.getFirstNavigationalItem();
                        }
                    };

                    return new AbstractDescriptor<?>[] { naviBlockDescriptor, naviItemDescriptor };
                }
            };
            return new AbstractDescriptor<?>[] { colDescriptor, layoutGroupDescriptor };
        }

    }

    AbstractTextDescriptor getObjectGroupDescriptor(final EJPluginCanvasProperties canvas)
    {
        return new AbstractTextDescriptor("Referenced ObjectGroup")
        {

            public boolean hasLableLink()
            {
                return true;
            }

            @Override
            public void runOperation(AbstractOperation operation)
            {
                editor.execute(operation);

            }

            @Override
            public String lableLinkActivator()
            {

                EJPluginObjectGroupProperties file = editor.getFormProperties().getObjectGroupContainer()
                        .getObjectGroupProperties(canvas.getReferencedObjectGroupName());
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
                return canvas.getReferencedObjectGroupName();
            }

            Text text;

            @Override
            public void addEditorAssist(Control control)
            {

                text = (Text) control;
                text.setEditable(false);
            }
        };
    }

}
