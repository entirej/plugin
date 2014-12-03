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
import java.util.Arrays;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.AlignmentBaseItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Date.DateFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line.LineDirection;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line.LineStyle;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Number.NumberFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.RotatableItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.ValueBaseItem;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.report.enumerations.EJReportMarkupType;
import org.entirej.framework.report.enumerations.EJReportScreenAlignment;
import org.entirej.framework.report.enumerations.EJReportScreenItemType;
import org.entirej.framework.report.enumerations.EJReportScreenRotation;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.report.wizards.ScreenItemWizard;
import org.entirej.ide.ui.editors.report.wizards.ScreenItemWizardContext;
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
    boolean                               forColumn;

    public ReportBlockScreenItemsGroupNode(ReportDesignTreeSection treeSection, ReportScreenNode node, boolean forColumn)
    {
        super(node, node.getSource().getScreenItemContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
        this.forColumn = forColumn;
    }

    public ReportBlockScreenItemsGroupNode(ReportDesignTreeSection treeSection, AbstractNode<?> node, EJReportScreenItemContainer container, boolean forColumn)
    {
        super(node, container);
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
        this.forColumn = forColumn;
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
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, ReportNodeTag.NONE);
                        if ((tag & ReportNodeTag.GROUP) != 0 && ((tag & ReportNodeTag.BLOCK) != 0 || (tag & ReportNodeTag.LOV) != 0) && (tag & ReportNodeTag.ITEM) != 0)
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

    public class ScreenItemNode extends AbstractNode<EJPluginReportScreenItemProperties> implements Neighbor, Movable, NodeOverview
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
                                                                  int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, ReportNodeTag.NONE);
                                                                  if ((tag & ReportNodeTag.BLOCK) != 0
                                                                          && source.getBlockProperties().getName() != null
                                                                          && source.getBlockProperties().getName()
                                                                                  .equals(marker.getAttribute(ReportNodeTag.BLOCK_ID, null))
                                                                          && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(ReportNodeTag.ITEM_ID, null)))
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
            return new Action[] { createNewScreenItemAction(ReportBlockScreenItemsGroupNode.this.source, ++indexOf), null,
                    createConvertScreenItemAction(ReportBlockScreenItemsGroupNode.this.source, source) };
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

                                       return (tag & ReportNodeTag.WIDTH) != 0;
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

                                       return (tag & ReportNodeTag.HEIGHT) != 0;
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
                    return String.valueOf(source.getY());
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
                ReportBlockItemsGroupNode.ItemDefaultValue valueProvider = new ReportBlockItemsGroupNode.ItemDefaultValue(source.getBlockProperties()
                        .getReportProperties(), source.getBlockProperties(), "Value Provider")
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
                
                if(!(source instanceof EJPluginReportScreenItemProperties.Image))
                {
                    
                    
                    AbstractBooleanDescriptor expandToFit = new AbstractBooleanDescriptor("Expand To Fit")
                    {
                        
                        @Override
                        public void setValue(Boolean value)
                        {
                            item.setExpandToFit(value);
                            editor.setDirty(true);
                            treeSection.refresh(ScreenItemNode.this);
                            
                        }
                        
                        @Override
                        public Boolean getValue()
                        {
                            return item.isExpandToFit();
                        }
                    };
                    
                    descriptors.add(expandToFit);
                    
                }
                if((source instanceof EJPluginReportScreenItemProperties.Text))
                {
                    
                    
                    AbstractTextDropDownDescriptor markup = new AbstractTextDropDownDescriptor("Markup")
                    {
                        
                        @Override
                        public void setValue(String value)
                        {
                            item.setMarkup(EJReportMarkupType.valueOf(value));
                            editor.setDirty(true);
                            treeSection.refresh(ScreenItemNode.this);
                            
                        }
                        
                        public String[] getOptions()
                        {
                            String  [] options = new String[EJReportMarkupType.values().length];
                            int index =0 ;
                            for (EJReportMarkupType markupType : EJReportMarkupType.values())
                            {
                                options[index] = markupType.name();
                                index++;
                            }
                            return options;
                        }
                        
                        public String getOptionText(String t)
                        {
                            return EJReportMarkupType.valueOf(t).toString();
                        }
                        
                        @Override
                        public String getValue()
                        {
                            return item.getMarkup().name();
                        }
                    };
                    
                    descriptors.add(markup);
                    
                }
                
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
                        editor.setDirty(true);
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
                        editor.setDirty(true);
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
                        editor.setDirty(true);
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
                            editor.setDirty(true);
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
                            editor.setDirty(true);
                        }

                    };
                    descriptors.add(lformat);
                    descriptors.add(mformatDescriptor);
                }
                    break;

                case LINE:
                {
                    final EJPluginReportScreenItemProperties.Line number = (EJPluginReportScreenItemProperties.Line) source;

                    AbstractTextDropDownDescriptor lineStyle = new AbstractTextDropDownDescriptor("Line Style")
                    {
                        @Override
                        public String getValue()
                        {
                            return number.getLineStyle().name();
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            for (LineStyle formats : LineStyle.values())
                            {
                                options.add(formats.name());
                            }
                            return options.toArray(new String[0]);
                        }

                        public String getOptionText(String t)
                        {

                            return LineStyle.valueOf(t).toString();
                        }

                        @Override
                        public void setValue(String value)
                        {
                            number.setLineStyle(LineStyle.valueOf(value));
                            editor.setDirty(true);
                        }

                    };
                    AbstractTextDropDownDescriptor lineDirection = new AbstractTextDropDownDescriptor("Line Direction")
                    {
                        @Override
                        public String getValue()
                        {
                            return number.getLineDirection().name();
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            for (LineDirection formats : LineDirection.values())
                            {
                                options.add(formats.name());
                            }
                            return options.toArray(new String[0]);
                        }

                        public String getOptionText(String t)
                        {

                            return LineDirection.valueOf(t).toString();
                        }

                        @Override
                        public void setValue(String value)
                        {
                            number.setLineDirection(LineDirection.valueOf(value));
                            editor.setDirty(true);
                        }

                    };

                    final AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Line Width")
                    {

                        @Override
                        public String getTooltip()
                        {

                            return "The width <b>(in pixels)</b> of the Line.";
                        }

                        @Override
                        public void setValue(String value)
                        {
                            try
                            {
                                number.setLineWidth(Double.parseDouble(value));
                            }
                            catch (NumberFormatException e)
                            {
                                number.setLineWidth(1);
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
                            return String.valueOf(number.getLineWidth());
                        }

                        Text text;

                        @Override
                        public void addEditorAssist(Control control)
                        {

                            text = (Text) control;
                            text.addVerifyListener(new EJPluginEntireJNumberVerifier()
                            {

                                @Override
                                protected boolean validate(String value)
                                {
                                    try
                                    {
                                        Double intValue = Double.parseDouble(value);

                                        if (intValue > -1)
                                        {
                                            return true;
                                        }
                                        else
                                        {
                                            return false;
                                        }
                                    }
                                    catch (NumberFormatException exception)
                                    {
                                        // ignore
                                    }

                                    return false;
                                }

                            });

                            super.addEditorAssist(control);
                        }
                    };

                    descriptors.add(lineStyle);
                    descriptors.add(lineDirection);
                    descriptors.add(widthDescriptor);
                }
                    break;
                case RECTANGLE:
                {
                    final EJPluginReportScreenItemProperties.Rectangle number = (EJPluginReportScreenItemProperties.Rectangle) source;

                    AbstractTextDropDownDescriptor lineStyle = new AbstractTextDropDownDescriptor("Line Style")
                    {
                        @Override
                        public String getValue()
                        {
                            return number.getLineStyle().name();
                        }

                        public String[] getOptions()
                        {
                            List<String> options = new ArrayList<String>();
                            for (LineStyle formats : LineStyle.values())
                            {
                                options.add(formats.name());
                            }
                            return options.toArray(new String[0]);
                        }

                        public String getOptionText(String t)
                        {

                            return LineStyle.valueOf(t).toString();
                        }

                        @Override
                        public void setValue(String value)
                        {
                            number.setLineStyle(LineStyle.valueOf(value));
                            editor.setDirty(true);
                        }

                    };

                    final AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Line Width")
                    {

                        @Override
                        public String getTooltip()
                        {

                            return "The width <b>(in pixels)</b> of the Line.";
                        }

                        @Override
                        public void setValue(String value)
                        {
                            try
                            {
                                number.setLineWidth(Double.parseDouble(value));
                            }
                            catch (NumberFormatException e)
                            {
                                number.setLineWidth(1);
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
                            return String.valueOf(number.getLineWidth());
                        }

                        Text text;

                        @Override
                        public void addEditorAssist(Control control)
                        {

                            text = (Text) control;
                            text.addVerifyListener(new EJPluginEntireJNumberVerifier()
                            {

                                @Override
                                protected boolean validate(String value)
                                {
                                    try
                                    {
                                        Double intValue = Double.parseDouble(value);

                                        if (intValue > -1)
                                        {
                                            return true;
                                        }
                                        else
                                        {
                                            return false;
                                        }
                                    }
                                    catch (NumberFormatException exception)
                                    {
                                        // ignore
                                    }

                                    return false;
                                }

                            });

                            super.addEditorAssist(control);
                        }
                    };
                    final AbstractTextDescriptor radiusDescriptor = new AbstractTextDescriptor("Radius")
                    {

                        @Override
                        public String getTooltip()
                        {

                            return "The radius <b>(in pixels)</b> of the rectangle.";
                        }

                        @Override
                        public void setValue(String value)
                        {
                            try
                            {
                                number.setRadius(Integer.parseInt(value));
                            }
                            catch (NumberFormatException e)
                            {
                                number.setRadius(0);
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
                            return String.valueOf(number.getRadius());
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

                    descriptors.add(lineStyle);

                    descriptors.add(widthDescriptor);
                    descriptors.add(radiusDescriptor);
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

        return new Action("New Screen Item")
        {

            @Override
            public void runWithEvent(Event event)
            {
                newScreenItem(0, 0, container, index);
            }

        };
    }

    public Action createConvertScreenItemAction(final EJReportScreenItemContainer container, final EJPluginReportScreenItemProperties source)
    {

        return new AbstractSubActions("Change Screen Item Type")
        {

            Action createAction(final EJReportScreenItemType type)
            {

                String name = type.toString();

                return new Action(name)
                {
                    @Override
                    public void runWithEvent(Event event)
                    {
                        EJPluginReportScreenItemProperties target = container.convertItemType(type, source);
                        editor.refresh(container);
                        editor.select(target);
                    }
                };
            }

           

            @Override
            public Action[] getActions()
            {

                List<Action> actions = new ArrayList<Action>();

                for (EJReportScreenItemType type : EJReportScreenItemType.values())
                {
                    if (type == source.getType())
                        continue;
                    actions.add(createAction(type));
                }
                return actions.toArray(new Action[0]);
            }

        };
    }

    void newScreenItem(final int x, final int y, final EJReportScreenItemContainer container, final int index)
    {
        ScreenItemWizardContext context = new ScreenItemWizardContext()
        {

            public EJPluginReportScreenItemProperties newScreenItem(EJReportScreenItemType type)
            {
                EJPluginReportScreenItemProperties itemProperties = container.newItem(type);

                if (forColumn && container.getItemCount() == 0)
                {
                    itemProperties.setX(0);
                    itemProperties.setWidth(source.getScreenProperties().getWidth());
                    
                    itemProperties.setHeight(10);
                }
                else
                {
                    itemProperties.setWidth(80);
                    itemProperties.setHeight(itemProperties.getType() == EJReportScreenItemType.LINE ? 1 : 20);
                }

                if (x > 0)
                    itemProperties.setX(x);
                if (y > 0)
                    itemProperties.setY(y);
                return itemProperties;
            }

            public boolean hasScreenItem(String name)
            {
                return container.contains(name);
            }

            public FormToolkit getToolkit()
            {
                return editor.getToolkit();
            }

            public IJavaProject getProject()
            {
                return editor.getJavaProject();
            }

            public AbstractDescriptor<?>[] getDescriptors(EJPluginReportScreenItemProperties itemProperties)
            {
                if (itemProperties != null)
                {
                    final ScreenItemNode node = new ScreenItemNode(null, itemProperties);
                    return node.getNodeDescriptors();
                }
                return new AbstractDescriptor<?>[0];
            }

            public String getDefaultBlockValue()
            {

                return container.getBlockProperties().getName();
            }

            public List<EJReportScreenItemType> getBlockItemTypes()
            {
                return Arrays.asList(EJReportScreenItemType.values());
            }

            public void addScreenItem(String name, final EJPluginReportScreenItemProperties itemProperties)
            {

                itemProperties.setName(name);
                container.addItemProperties(index, itemProperties);

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
        };

        ScreenItemWizard wizard = new ScreenItemWizard(context);
        wizard.open();
    }

}
