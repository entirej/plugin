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
package org.entirej.ide.ui.editors.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockItemContainer;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractCustomDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.report.ReportBlockGroupNode.BlockNode;
import org.entirej.ide.ui.editors.report.operations.ReportBlockItemAddOperation;
import org.entirej.ide.ui.editors.report.operations.ReportBlockItemRemoveOperation;
import org.entirej.ide.ui.editors.report.wizards.BlockItemWizard;
import org.entirej.ide.ui.editors.report.wizards.BlockItemWizardContext;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;

public class ReportBlockItemsGroupNode extends AbstractNode<EJReportBlockItemContainer> implements NodeMoveProvider
{

    public static final Image             GROUP    = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    public static final Image             BLOCK_ND = EJUIImages.getImage(EJUIImages.DESC_BLOCK_ITEM_ND);
    public static final Image             BLOCK    = EJUIImages.getImage(EJUIImages.DESC_BLOCK_ITEM);

    private final ReportDesignTreeSection treeSection;
    private final AbstractEJReportEditor  editor;

    public ReportBlockItemsGroupNode(ReportDesignTreeSection treeSection, BlockNode node)
    {
        super(node, node.getSource().getItemContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public ReportBlockItemsGroupNode(ReportDesignTreeSection treeSection, AbstractNode<?> node, EJReportBlockItemContainer container)
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
                    treeSection.refresh(ReportBlockItemsGroupNode.this);
                }

                @Override
                public List<IMarker> getMarkers()
                {
                    List<IMarker> fmarkers = new ArrayList<IMarker>();

                    IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                    for (IMarker marker : markers)
                    {
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, ReportNodeTag.NONE);
                        if ((tag & ReportNodeTag.GROUP) != 0 && ((tag & ReportNodeTag.BLOCK) != 0 || (tag & ReportNodeTag.LOV) != 0)
                                && (tag & ReportNodeTag.ITEM) != 0)
                        {
                            fmarkers.add(marker);
                        }
                    }

                    return fmarkers;
                }
            });
        }

        if (IReportPreviewProvider.class.isAssignableFrom(adapter))
        {
            return getParent().getAdapter(adapter);
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
        List<EJPluginReportItemProperties> items = source.getAllItemProperties();
        for (EJPluginReportItemProperties itemProperties : items)
        {
            nodes.add(new ItemNode(this, itemProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        return new Action[] { createNewBlockItemAction(-1) };
    }

    class ItemNode extends AbstractNode<EJPluginReportItemProperties> implements Neighbor, Movable, NodeOverview
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
                    int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, ReportNodeTag.NONE);
                    if ((tag & ReportNodeTag.BLOCK) != 0 && source.getBlockProperties().getName() != null
                            && source.getBlockProperties().getName().equals(marker.getAttribute(ReportNodeTag.BLOCK_ID, null)) && source.getName() != null
                            && source.getName().equals(marker.getAttribute(ReportNodeTag.ITEM_ID, null)))
                    {

                        fmarkers.add(marker);
                    }

                }

                return fmarkers;
            }
        };

        public ItemNode(AbstractNode<?> parent, EJPluginReportItemProperties source)
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

            int indexOf = ReportBlockItemsGroupNode.this.source.getAllItemProperties().indexOf(source);
            return new Action[] { createNewBlockItemAction(++indexOf), null, createCopyBINameAction() };
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
            return true;
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

            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    if (cleanup)
                    {
                        ReportBlockItemsGroupNode.this.source.removeItem(source);
                    }
                    else
                    {
                        ReportBlockItemsGroupNode.this.source.getAllItemProperties().remove(source);
                    }
                    editor.setDirty(true);
                    treeSection.refresh(ReportBlockItemsGroupNode.this.getParent());

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {

                    return new ReportBlockItemRemoveOperation(treeSection, ReportBlockItemsGroupNode.this.source, source);
                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            // if it is a mirror child or Referenced should not be able to
            // rename from mirror
            // level

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
                            if (ReportBlockItemsGroupNode.this.source.contains(newText.trim()))
                                return "Item with this name already exists.";
                            return null;
                        }
                    });
                    if (dlg.open() == Window.OK)
                    {
                        String oldName = source.getName();
                        String newName = dlg.getValue().trim();
                        source.setName(newName);
                        // FIXME
                        // EJPluginItemChanger.renameItemOnForm(source.getBlockProperties(),
                        // oldName, newName);
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
            ItemDefaultValue queryItemDefaultValue = new ItemDefaultValue(editor, source.getBlockProperties().getReportProperties(),
                    source.getBlockProperties(), "Default Query Value")
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

            if (this.source.getBlockProperties().isReferenceBlock())
            {

                return new AbstractDescriptor<?>[] { queryItemDefaultValue };
            }
            else
            {
                AbstractTypeDescriptor dataTypeDescriptor = new AbstractTypeDescriptor(editor, "Data Type")
                {
                    Filter vfilter = new Filter()
                    {

                        public boolean match(int tag, IMarker marker)
                        {

                            return (tag & ReportNodeTag.TYPE) != 0;
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

                if (!source.getBlockProperties().isControlBlock())
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
                        public void runOperation(AbstractOperation operation)
                        {
                            editor.execute(operation);

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

            descriptors.add(queryItemDefaultValue);

            return descriptors.toArray(new AbstractDescriptor<?>[0]);
        }

        public void addOverview(StyledString styledString)
        {
            if (source.getDataTypeClassName() != null && source.getDataTypeClassName().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getDataTypeClassName(), StyledString.DECORATIONS_STYLER);

            }

        }

    }

    public boolean canMove(Neighbor relation, Object source)
    {
        // only allow to DnD with in the same block
        return (source instanceof EJPluginReportItemProperties
                && ((EJPluginReportItemProperties) source).getBlockProperties().equals(this.source.getBlockProperties()));
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {

        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginReportItemProperties> items = source.getAllItemProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addItemProperties(index, (EJPluginReportItemProperties) dSource);
            }
        }
        else
            source.addItemProperties((EJPluginReportItemProperties) dSource);

    }

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginReportItemProperties> items = source.getAllItemProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                return new ReportBlockItemAddOperation(treeSection, source, (EJPluginReportItemProperties) dSource, index);
            }
        }

        return new ReportBlockItemAddOperation(treeSection, source, (EJPluginReportItemProperties) dSource);
    }

    public Action createNewBlockItemAction(final int index)
    {

        return new Action("New Report Block Item")
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

                    public void addBlock(String blockItemName, String dataType, boolean serviceItem)
                    {
                        final EJPluginReportItemProperties itemProperties = new EJPluginReportItemProperties(source.getBlockProperties(), blockItemName);
                        itemProperties.setDataTypeClassName(dataType);
                        itemProperties.setBlockServiceItem(serviceItem);

                        ReportBlockItemAddOperation addOperation = new ReportBlockItemAddOperation(treeSection, source, itemProperties, index);

                        editor.execute(addOperation, new NullProgressMonitor());

                    }
                };
                BlockItemWizard wizard = new BlockItemWizard(context);
                wizard.open();
            }

        };
    }

    public static class ItemDefaultValue extends AbstractGroupDescriptor
    {

        final EJPluginReportProperties      formProp;
        final EJPluginReportBlockProperties blockProperties;

        @Override
        public boolean isExpand()
        {
            return true;
        }

        @Override
        public void runOperation(AbstractOperation operation)
        {
            if (editor != null)
                editor.execute(operation);
            else
            {
                try
                {
                    operation.redo(null, null);
                }
                catch (ExecutionException e)
                {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }
            }

        }

        enum TYPE
        {
            EMPTY, BLOCK_ITEM, REPORT_PARAMETER, APP_PARAMETER, CLASS_FIELD, VARIABLE;

            public String toString()
            {
                switch (this)
                {
                    case EMPTY:
                        return "";
                    case BLOCK_ITEM:
                        return "Block Item";
                    case APP_PARAMETER:
                        return "Runtime Level Parameter";
                    case REPORT_PARAMETER:
                        return "Report Parameter";
                    case CLASS_FIELD:
                        return "Class Field";
                    case VARIABLE:
                        return "Report Variable";
                    default:
                        return super.toString();
                }
            }
        }

        TYPE                         entry;
        final AbstractEJReportEditor editor;

        public ItemDefaultValue(AbstractEJReportEditor editor, EJPluginReportProperties formProp, EJPluginReportBlockProperties blockProperties, String lable)
        {
            super(lable);
            this.formProp = formProp;
            this.editor = editor;
            this.blockProperties = blockProperties;
        }

        public ItemDefaultValue(EJPluginReportProperties formProp, EJPluginReportBlockProperties blockProperties, String lable)
        {
            super(lable);
            this.formProp = formProp;
            this.editor = null;
            this.blockProperties = blockProperties;
        }

        public String getDefaultBlockValue()
        {
            return "";
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
                    case REPORT_PARAMETER:
                    case APP_PARAMETER:
                        return new AbstractDescriptor<?>[] { new AbstractTextDropDownDescriptor("Parameter")
                        {
                            @Override
                            public void runOperation(AbstractOperation operation)
                            {
                                editor.execute(operation);

                            }

                            public String[] getOptions()
                            {
                                List<String> list = new ArrayList<String>();
                                list.add("");
                                if (entry == ItemDefaultValue.TYPE.REPORT_PARAMETER)
                                {
                                    Collection<EJPluginApplicationParameter> allFormParameters = formProp.getAllReportParameters();
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

                    case VARIABLE:
                        return new AbstractDescriptor<?>[] { new AbstractTextDropDownDescriptor("Variable")
                        {
                            @Override
                            public void runOperation(AbstractOperation operation)
                            {
                                editor.execute(operation);

                            }

                            public String[] getOptions()
                            {
                                List<String> list = new ArrayList<String>();
                                list.add("");
                                list.add("PAGE_NUMBER");
                                list.add("PAGE_COUNT");
                                list.add("CURRENT_DATE");
                                list.add("PAGE_NUMBER_OF_TOTAL_PAGES");
                                list.add("");

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
                            ComboViewer         blockViewer;
                            ComboViewer         itemViewer;

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
                            public void runOperation(AbstractOperation operation)
                            {
                                editor.execute(operation);

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

                                        List<String> blockNames = new ArrayList<String>(formProp.getBlockNamesWithParents(blockProperties));

                                        return blockNames.toArray();
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
                                        EJReportBlockItemContainer blockItemContainer = formProp.getBlockProperties((String) inputElement);
                                        if (blockItemContainer == null)
                                            return new Object[0];
                                        Collection<EJPluginReportItemProperties> allItemProperties = blockItemContainer.getAllItemProperties();
                                        List<EJPluginReportItemProperties> blockItemNames = new ArrayList<EJPluginReportItemProperties>();
                                        for (EJPluginReportItemProperties ejItemProperties : allItemProperties)
                                        {

                                            blockItemNames.add(ejItemProperties);
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
                                blockViewer.setSelection(new StructuredSelection(getDefaultBlockValue()));
                                itemViewer.getCombo().select(-1);
                                if (defaultValue != null)
                                {
                                    String[] split = defaultValue.split("\\.");
                                    if (split.length == 2)
                                    {
                                        blockViewer.setSelection(new StructuredSelection(split[0]));

                                        EJReportBlockItemContainer blockProperties2 = formProp.getBlockProperties(split[0]);
                                        if (blockProperties2 != null)
                                        {
                                            Collection<EJPluginReportItemProperties> allItemProperties = blockProperties2.getAllItemProperties();
                                            for (EJPluginReportItemProperties ejItemProperties : allItemProperties)
                                            {
                                                if (ejItemProperties.getName().equals(split[1]))
                                                {
                                                    itemViewer.setSelection(new StructuredSelection(ejItemProperties));
                                                    break;
                                                }
                                            }
                                        }

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
                                                EJPluginReportItemProperties item = (EJPluginReportItemProperties) ((IStructuredSelection) itemViewer
                                                        .getSelection()).getFirstElement();
                                                if (item != null)
                                                    setValue(String.format("%s.%s", lov, item.getName()));
                                            }

                                        }

                                    }
                                });

                                itemViewer.setLabelProvider(new ColumnLabelProvider()
                                {

                                    public String getText(Object element)
                                    {
                                        if (element instanceof EJPluginReportItemProperties)
                                        {
                                            EJPluginReportItemProperties itemProperties = ((EJPluginReportItemProperties) element);
                                            return String.format("%s - %s", itemProperties.getName(), itemProperties.getDataTypeClassName());
                                        }

                                        return super.getText(element);
                                    };

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
