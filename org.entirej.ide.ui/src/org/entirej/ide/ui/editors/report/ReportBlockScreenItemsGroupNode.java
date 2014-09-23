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
package org.entirej.ide.ui.editors.report;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IMarker;
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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.reports.EJPluginReportItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.AlignmentBaseItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Date.DateFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Number.NumberFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.RotatableItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.ValueBaseItem;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.reports.enumerations.EJReportScreenAlignment;
import org.entirej.framework.reports.enumerations.EJReportScreenItemType;
import org.entirej.framework.reports.enumerations.EJReportScreenRotation;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractCustomDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.form.FormNodeTag;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractSubActions;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;

public class ReportBlockScreenItemsGroupNode extends AbstractNode<EJReportScreenItemContainer> implements NodeMoveProvider
{

    public static final Image             GROUP = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    public static final Image             BLOCK = EJUIImages.getImage(EJUIImages.DESC_BLOCK_ITEM);

    private final ReportDesignTreeSection treeSection;
    private final AbstractEJReportEditor  editor;

    public ReportBlockScreenItemsGroupNode(ReportDesignTreeSection treeSection, ReportScreenNode node)
    {
        super(node, node.getSource().getScreenItemContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public ReportBlockScreenItemsGroupNode(ReportDesignTreeSection treeSection, AbstractNode<?> node, EJReportScreenItemContainer container)
    {
        super(node, container);
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    @Override
    public String getName()
    {
        return "Screen Items";
    }

    @Override
    public String getToolTipText()
    {
        return "block Screen item definitions";
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
                    treeSection.refresh(ReportBlockScreenItemsGroupNode.this);
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
        List<EJPluginReportScreenItemProperties> items = source.getAllItemProperties();
        for (EJPluginReportScreenItemProperties itemProperties : items)
        {
            nodes.add(new ScreenItemNode(this, itemProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        return new Action[] { createNewScreenItemAction(source, -1) };
    }

    class ScreenItemNode extends AbstractNode<EJPluginReportScreenItemProperties> implements Neighbor, Movable, NodeOverview
    {
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(ScreenItemNode.this);
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

                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

        public ScreenItemNode(AbstractNode<?> parent, EJPluginReportScreenItemProperties source)
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
            
            if (IReportPreviewProvider.class.isAssignableFrom(adapter))
            {
                return getParent().getAdapter(adapter);
            }
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
            return BLOCK;
        }

        @Override
        public Action[] getActions()
        {

            int indexOf = ReportBlockScreenItemsGroupNode.this.source.getAllItemProperties().indexOf(source);
            return new Action[] { createNewScreenItemAction(ReportBlockScreenItemsGroupNode.this.source, ++indexOf), };
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
                        ReportBlockScreenItemsGroupNode.this.source.removeItem(source);
                    }
                    else
                    {
                        ReportBlockScreenItemsGroupNode.this.source.getAllItemProperties().remove(source);
                    }
                    editor.setDirty(true);
                    treeSection.refresh(ReportBlockScreenItemsGroupNode.this.getParent());

                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
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
                                    if (ReportBlockScreenItemsGroupNode.this.source.contains(newText.trim()))
                                        return "Item with this name already exists.";
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        String oldName = source.getName();
                        String newName = dlg.getValue().trim();
                        source.setName(newName);
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

            final AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Width")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.WIDTH) != 0;
                                   }
                               };

                @Override
                public String getErrors()
                {

                    return validator.getErrorMarkerMsg(fmarkers, vfilter);
                }

                @Override
                public String getTooltip()
                {

                    return "The width <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
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
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ScreenItemNode.this);
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

            final AbstractTextDescriptor heightDescriptor = new AbstractTextDescriptor("Height")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.HEIGHT) != 0;
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
                public String getTooltip()
                {

                    return "The height <b>(in pixels)</b> of the report within it's Page.";
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
                        source.setHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ScreenItemNode.this);
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

            final AbstractTextDescriptor xDescriptor = new AbstractTextDescriptor("X")
            {

                @Override
                public String getTooltip()
                {

                    return "The X <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setX(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setX(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ScreenItemNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getX());
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

            final AbstractTextDescriptor yDescriptor = new AbstractTextDescriptor("Y")
            {

                @Override
                public String getTooltip()
                {

                    return "The Y <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setY(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setY(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ScreenItemNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getX());
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

            descriptors.add(xDescriptor);
            descriptors.add(yDescriptor);
            descriptors.add(widthDescriptor);
            descriptors.add(heightDescriptor);

            AbstractTextDropDownDescriptor vaDescriptor = new AbstractTextDropDownDescriptor("Visual Attributes", "")
            {
                List<String> visualAttributeNames = new ArrayList<String>(editor.getReportProperties().getEntireJProperties().getVisualAttributesContainer()
                                                          .getVisualAttributeNames());

                @Override
                public void setValue(String value)
                {
                    source.setVisualAttributeName(value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return source.getVisualAttributeName();
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

            descriptors.add(vaDescriptor);

            final AbstractBooleanDescriptor visiableDescriptor = new AbstractBooleanDescriptor("Visible",
                    "Indicates if the item is visible to the Report at runtime. ")
            {

                @Override
                public void setValue(Boolean value)
                {
                    source.setVisible(value.booleanValue());
                    editor.setDirty(true);
                    treeSection.refresh(ScreenItemNode.this);

                }

                @Override
                public Boolean getValue()
                {
                    return source.isVisible();
                }
            };
            descriptors.add(visiableDescriptor);

            addTypeBaseDescriptors(descriptors, source);

            return descriptors.toArray(new AbstractDescriptor<?>[0]);
        }

        private void addTypeBaseDescriptors(List<AbstractDescriptor<?>> descriptors, EJPluginReportScreenItemProperties source)
        {

            if (source instanceof EJPluginReportScreenItemProperties.ValueBaseItem)
            {
                final EJPluginReportScreenItemProperties.ValueBaseItem item = (ValueBaseItem) source;
                ItemValueProvider valueProvider = new ItemValueProvider(source.getBlockProperties().getReportProperties(), "Value Provider")
                {
                    @Override
                    public String getValue()
                    {
                        return item.getValue();
                    }

                    @Override
                    public void setValue(Object value)
                    {
                        item.setValue((String) value);
                        editor.setDirty(true);
                        treeSection.refresh(ScreenItemNode.this);
                    }

                    @Override
                    public String getDefaultBlockValue()
                    {
                        return ReportBlockScreenItemsGroupNode.this.source.getBlockProperties().getName();
                    }

                };
                descriptors.add(valueProvider);
            }

            if (source instanceof EJPluginReportScreenItemProperties.AlignmentBaseItem)
            {
                final EJPluginReportScreenItemProperties.AlignmentBaseItem item = (AlignmentBaseItem) source;
                AbstractTextDropDownDescriptor hAlignment = new AbstractTextDropDownDescriptor("Horizontal Alignment")
                {
                    @Override
                    public String getValue()
                    {
                        return item.getHAlignment().name();
                    }

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();
                        options.add(EJReportScreenAlignment.LEFT.name());
                        options.add(EJReportScreenAlignment.CENTER.name());
                        options.add(EJReportScreenAlignment.RIGHT.name());
                        options.add(EJReportScreenAlignment.JUSTIFIED.name());
                        return options.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return EJReportScreenAlignment.valueOf(t).toString();
                    }

                    @Override
                    public void setValue(String value)
                    {
                        item.setHAlignment(EJReportScreenAlignment.valueOf(value));

                    }

                };
                AbstractTextDropDownDescriptor vAlignment = new AbstractTextDropDownDescriptor("Vertical Alignment")
                {
                    @Override
                    public String getValue()
                    {
                        return item.getVAlignment().name();
                    }

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();
                        options.add(EJReportScreenAlignment.TOP.name());
                        options.add(EJReportScreenAlignment.CENTER.name());
                        options.add(EJReportScreenAlignment.BOTTOM.name());
                        options.add(EJReportScreenAlignment.JUSTIFIED.name());
                        return options.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return EJReportScreenAlignment.valueOf(t).toString();
                    }

                    @Override
                    public void setValue(String value)
                    {
                        item.setVAlignment(EJReportScreenAlignment.valueOf(value));

                    }

                };

                descriptors.add(hAlignment);
                descriptors.add(vAlignment);
            }

            if (source instanceof EJPluginReportScreenItemProperties.RotatableItem)
            {
                final EJPluginReportScreenItemProperties.RotatableItem item = (RotatableItem) source;
                AbstractTextDropDownDescriptor rotation = new AbstractTextDropDownDescriptor("Rotation")
                {
                    @Override
                    public String getValue()
                    {
                        return item.getRotation().name();
                    }

                    public String[] getOptions()
                    {
                        List<String> options = new ArrayList<String>();
                        options.add(EJReportScreenRotation.NONE.name());
                        options.add(EJReportScreenRotation.LEFT.name());
                        options.add(EJReportScreenRotation.RIGHT.name());
                        options.add(EJReportScreenRotation.UPSIDEDOWN.name());
                        return options.toArray(new String[0]);
                    }

                    public String getOptionText(String t)
                    {

                        return EJReportScreenRotation.valueOf(t).toString();
                    }

                    @Override
                    public void setValue(String value)
                    {
                        item.setRotation(EJReportScreenRotation.valueOf(value));

                    }

                };

                descriptors.add(rotation);
            }

            switch (source.getType())
            {
                case LABEL:
                {
                    final EJPluginReportScreenItemProperties.Label label = (EJPluginReportScreenItemProperties.Label) source;
                    final AbstractTextDescDescriptor textDescriptor = new AbstractTextDescDescriptor("Text")
                    {

                        @Override
                        public String getTooltip()
                        {

                            return "Label Text";
                        }

                        @Override
                        public void setValue(String value)
                        {
                            label.setText(value);
                            treeSection.getEditor().setDirty(true);
                            treeSection.refresh(ScreenItemNode.this);
                        }

                        @Override
                        public String getValue()
                        {
                            return label.getText();
                        }

                    };
                    descriptors.add(textDescriptor);
                }
                    break;
                case NUMBER:
                {
                    final EJPluginReportScreenItemProperties.Number number = (EJPluginReportScreenItemProperties.Number) source;
                    final AbstractTextDescriptor mformatDescriptor = new AbstractTextDescriptor("Manual Format")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            number.setManualFormat(value);
                            treeSection.getEditor().setDirty(true);
                            treeSection.refresh(ScreenItemNode.this);
                        }

                        @Override
                        public String getValue()
                        {
                            return number.getManualFormat();
                        }

                    };

                    AbstractTextDropDownDescriptor lformat = new AbstractTextDropDownDescriptor("Locale Format")
                    {
                        @Override
                        public String getValue()
                        {
                            return number.getLocaleFormat().name();
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            for (NumberFormats formats : NumberFormats.values())
                            {
                                options.add(formats.name());
                            }
                            return options.toArray(new String[0]);
                        }

                        public String getOptionText(String t)
                        {

                            return NumberFormats.valueOf(t).toString();
                        }

                        @Override
                        public void setValue(String value)
                        {
                            number.setLocaleFormat(NumberFormats.valueOf(value));

                        }

                    };
                    descriptors.add(lformat);
                    descriptors.add(mformatDescriptor);
                }
                    break;

                case DATE:
                {
                    final EJPluginReportScreenItemProperties.Date number = (EJPluginReportScreenItemProperties.Date) source;
                    final AbstractTextDescriptor mformatDescriptor = new AbstractTextDescriptor("Manual Format")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            number.setManualFormat(value);
                            treeSection.getEditor().setDirty(true);
                            treeSection.refresh(ScreenItemNode.this);
                        }

                        @Override
                        public String getValue()
                        {
                            return number.getManualFormat();
                        }

                    };

                    AbstractTextDropDownDescriptor lformat = new AbstractTextDropDownDescriptor("Locale Format")
                    {
                        @Override
                        public String getValue()
                        {
                            return number.getLocaleFormat().name();
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            for (DateFormats formats : DateFormats.values())
                            {
                                options.add(formats.name());
                            }
                            return options.toArray(new String[0]);
                        }

                        public String getOptionText(String t)
                        {

                            return DateFormats.valueOf(t).toString();
                        }

                        @Override
                        public void setValue(String value)
                        {
                            number.setLocaleFormat(DateFormats.valueOf(value));

                        }

                    };
                    descriptors.add(lformat);
                    descriptors.add(mformatDescriptor);
                }
                    break;

                default:
                    break;
            }

        }

        public void addOverview(StyledString styledString)
        {

            styledString.append(" : ", StyledString.QUALIFIER_STYLER);
            styledString.append(source.getType().toString(), StyledString.COUNTER_STYLER);

            styledString.append(" (" + source.getX() + " ," + source.getY() + ")", StyledString.DECORATIONS_STYLER);

            addTypeBaseOverview(source, styledString);
        }

        private void addTypeBaseOverview(EJPluginReportScreenItemProperties source, StyledString styledString)
        {

            if (source instanceof EJPluginReportScreenItemProperties.ValueBaseItem)
            {
                final EJPluginReportScreenItemProperties.ValueBaseItem item = (ValueBaseItem) source;
                if (item.getValue() != null && !item.getValue().isEmpty())
                {
                    styledString.append(" ");
                    String value = item.getValue();
                    if (value != null && value.trim().length() > 0 && value.indexOf(":") > 0)
                    {
                        styledString.append(value.substring(value.indexOf(":") + 1), StyledString.QUALIFIER_STYLER);
                    }

                }

            }
            switch (source.getType())
            {
                case LABEL:
                    EJPluginReportScreenItemProperties.Label label = (EJPluginReportScreenItemProperties.Label) source;
                    if (label.getText() != null && !label.getText().isEmpty())
                    {
                        styledString.append(" ");
                        styledString.append(label.getText(), StyledString.QUALIFIER_STYLER);
                    }
                    break;

                default:
                    break;
            }

        }

    }

    public boolean canMove(Neighbor relation, Object source)
    {
        // only allow to DnD with in the same block
        return (source instanceof EJPluginReportScreenItemProperties && ((EJPluginReportScreenItemProperties) source).getBlockProperties().equals(
                this.source.getBlockProperties()));
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {

        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginReportScreenItemProperties> items = source.getAllItemProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addItemProperties(index, (EJPluginReportScreenItemProperties) dSource);
            }
        }
        else
            source.addItemProperties((EJPluginReportScreenItemProperties) dSource);

    }

    public Action createNewScreenItemAction(final EJReportScreenItemContainer container, final int index)
    {

        return new AbstractSubActions("New Screen Item")
        {
            Action createAction(final EJReportScreenItemType type)
            {

                String name = type.toString();

                return new Action(name)
                {
                    @Override
                    public void runWithEvent(Event event)
                    {
                        addScreenItem(type, getText());
                    }
                };
            }

            void addScreenItem(final EJReportScreenItemType type, String name)
            {
                InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("New Screen Item : [%s]", name), "Item Name", null,
                        new IInputValidator()
                        {

                            public String isValid(String newText)
                            {
                                if (newText == null || newText.trim().length() == 0)
                                    return "Item name can't be empty.";
                                if (container.contains(newText.trim()))
                                    return "Item with this name already exists.";

                                return null;
                            }
                        });
                if (dlg.open() == Window.OK)
                {
                    final EJPluginReportScreenItemProperties itemProperties = container.createItem(type, dlg.getValue(), index);
                    if (itemProperties != null)
                    {
                        // set default width/height

                        itemProperties.setWidth(80);
                        itemProperties.setHeight(itemProperties.getType()==EJReportScreenItemType.LINE?1:22);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh(ReportBlockScreenItemsGroupNode.this);
                                treeSection.selectNodes(true, treeSection.findNode(itemProperties, true));

                            }
                        });
                    }
                }
            }

            @Override
            public Action[] getActions()
            {

                List<Action> actions = new ArrayList<Action>();

                for (EJReportScreenItemType type : EJReportScreenItemType.values())
                {
                    actions.add(createAction(type));
                }
                return actions.toArray(new Action[0]);
            }

        };
    }

    private static class ItemValueProvider extends AbstractGroupDescriptor
    {

        final EJPluginReportProperties formProp;

        @Override
        public boolean isExpand()
        {
            return true;
        }

        enum TYPE
        {
            EMPTY, BLOCK_ITEM, FORM_PARAMETER, APP_PARAMETER;

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
                    default:
                        return super.toString();
                }
            }
        }

        TYPE entry;

        public ItemValueProvider(EJPluginReportProperties formProp, String lable)
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
                                if (entry == ItemValueProvider.TYPE.FORM_PARAMETER)
                                {
                                    Collection<EJPluginApplicationParameter> allFormParameters = formProp.getAllReportParameters();
                                    for (EJPluginApplicationParameter parameter : allFormParameters)
                                    {
                                        list.add(parameter.getName());
                                    }
                                }
                                if (entry == ItemValueProvider.TYPE.APP_PARAMETER)
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
                                    ItemValueProvider.this.setValue(String.format("%s:%s", entry.name(), value));
                                }
                                else
                                    ItemValueProvider.this.setValue("");

                            }

                            @Override
                            public String getValue()
                            {
                                String value = ItemValueProvider.this.getValue();
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
                                    ItemValueProvider.this.setValue(String.format("%s:%s", entry.name(), value));
                                }
                                else
                                    ItemValueProvider.this.setValue("");
                            }

                            @Override
                            public String getValue()
                            {
                                String value = ItemValueProvider.this.getValue();
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
                                        Collection<EJPluginReportItemProperties> allItemProperties = formProp.getBlockProperties((String) inputElement)
                                                .getAllItemProperties();
                                        List<String> blockItemNames = new ArrayList<String>();
                                        for (EJPluginReportItemProperties ejItemProperties : allItemProperties)
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
                                IStructuredSelection selection = (IStructuredSelection) blockViewer.getSelection();
                                if (selection.isEmpty())
                                {
                                    blockViewer.setSelection(new StructuredSelection(getDefaultBlockValue()));
                                    itemViewer.setSelection(new StructuredSelection());
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

                }
            return new AbstractDescriptor<?>[0];
        }

        @Override
        public String getValue()
        {
            return null;
        }

        public String getDefaultBlockValue()
        {
            return null;
        }

    }

}
