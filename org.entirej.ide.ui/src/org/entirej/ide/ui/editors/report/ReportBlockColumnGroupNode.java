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
import org.entirej.framework.plugin.reports.EJPluginReportBorderProperties;
import org.entirej.framework.plugin.reports.EJPluginReportColumnProperties;
import org.entirej.framework.plugin.reports.containers.EJReportColumnContainer;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.reports.interfaces.EJReportBorderProperties;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.FormNodeTag;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;

public class ReportBlockColumnGroupNode extends AbstractNode<EJReportColumnContainer> implements NodeMoveProvider
{

    public static final Image             GROUP = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    public static final Image             BLOCK = EJUIImages.getImage(EJUIImages.DESC_CANVAS_TAB_PAGE);

    private final ReportDesignTreeSection treeSection;
    private final AbstractEJReportEditor  editor;

    public ReportBlockColumnGroupNode(ReportDesignTreeSection treeSection, ReportScreenNode node)
    {
        super(node, node.getSource().getColumnContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public ReportBlockColumnGroupNode(ReportDesignTreeSection treeSection, AbstractNode<?> node, EJReportColumnContainer container)
    {
        super(node, container);
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    @Override
    public String getName()
    {
        return "Screen Columns";
    }

    @Override
    public String getToolTipText()
    {
        return "block Screen Column definitions";
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
                    treeSection.refresh(ReportBlockColumnGroupNode.this);
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
        return source.getColumnCount() == 0;
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {
        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
        List<EJPluginReportColumnProperties> items = source.getAllColumnProperties();
        for (EJPluginReportColumnProperties itemProperties : items)
        {
            nodes.add(new ScreenColumnNode(this, itemProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        return new Action[] { createNewColumnAction(source, -1) };
    }

    class ScreenColumnNode extends AbstractNode<EJPluginReportColumnProperties> implements Neighbor, Movable, NodeOverview
    {
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(ScreenColumnNode.this);
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

        public ScreenColumnNode(AbstractNode<?> parent, EJPluginReportColumnProperties source)
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

            int indexOf = ReportBlockColumnGroupNode.this.source.getAllColumnProperties().indexOf(source);
            return new Action[] { createNewColumnAction(ReportBlockColumnGroupNode.this.source, ++indexOf), };
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
                        ReportBlockColumnGroupNode.this.source.removeColumn(source);
                    }
                    else
                    {
                        ReportBlockColumnGroupNode.this.source.getAllColumnProperties().remove(source);
                    }
                    editor.setDirty(true);
                    treeSection.refresh(ReportBlockColumnGroupNode.this.getParent());

                }
            };
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

            if (source.isShowHeader())
                nodes.add(new ReportScreenNode(treeSection, this, source.getHeaderScreen())
                {

                    @Override
                    public String getName()
                    {
                        return "Column Header";
                    }

                    @Override
                    public AbstractDescriptor<?>[] getNodeDescriptors()
                    {

                        return addBorderDescriptors(super.getNodeDescriptors(), ScreenColumnNode.this.source.getHeaderBorderProperties());
                    }

                });
            nodes.add(new ReportScreenNode(treeSection, this, source.getDetailScreen())
            {

                @Override
                public String getName()
                {
                    return "Detail";
                }

                @Override
                public AbstractDescriptor<?>[] getNodeDescriptors()
                {

                    return addBorderDescriptors(super.getNodeDescriptors(), ScreenColumnNode.this.source.getDetailBorderProperties());
                }

            });
            if (source.isShowFooter())
                nodes.add(new ReportScreenNode(treeSection, this, source.getFooterScreen())
                {

                    @Override
                    public String getName()
                    {
                        return "Column Footer";
                    }

                    @Override
                    public AbstractDescriptor<?>[] getNodeDescriptors()
                    {

                        return addBorderDescriptors(super.getNodeDescriptors(), ScreenColumnNode.this.source.getFooterBorderProperties());
                    }

                });

            return nodes.toArray(new AbstractNode<?>[0]);
        }

        AbstractDescriptor<?>[] addBorderDescriptors(AbstractDescriptor<?>[] current, final EJPluginReportBorderProperties borderProperties)
        {

            List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>(Arrays.asList(current));

            AbstractTextDropDownDescriptor lineStyle = new AbstractTextDropDownDescriptor("Border Line Style")
            {
                @Override
                public String getValue()
                {
                    return borderProperties.getLineStyle().name();
                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    for (EJReportBorderProperties.LineStyle formats : EJReportBorderProperties.LineStyle.values())
                    {
                        options.add(formats.name());
                    }
                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return EJReportBorderProperties.LineStyle.valueOf(t).toString();
                }

                @Override
                public void setValue(String value)
                {
                    borderProperties.setLineStyle(EJReportBorderProperties.LineStyle.valueOf(value));

                }

            };

            final AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Border Line Width")
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
                        borderProperties.setLineWidth(Double.parseDouble(value));
                    }
                    catch (NumberFormatException e)
                    {
                        borderProperties.setLineWidth(1);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ScreenColumnNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(borderProperties.getLineWidth());
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

            AbstractTextDropDownDescriptor vaDescriptor = new AbstractTextDropDownDescriptor("Border Visual Attributes", "")
            {
                List<String> visualAttributeNames = new ArrayList<String>(editor.getReportProperties().getEntireJProperties().getVisualAttributesContainer()
                                                          .getVisualAttributeNames());

                @Override
                public void setValue(String value)
                {
                    borderProperties.setVisualAttributeName(value);
                    editor.setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return borderProperties.getVisualAttributeName();
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

            descriptors.add(lineStyle);
            descriptors.add(widthDescriptor);
            descriptors.add(vaDescriptor);
            descriptors.add(new AbstractBooleanDescriptor("Border Top Line")
            {

                @Override
                public void setValue(Boolean value)
                {
                    borderProperties.setShowTopLine(value);
                    editor.setDirty(true);
                }

                @Override
                public Boolean getValue()
                {
                    return borderProperties.isShowTopLine();
                }
            });
            descriptors.add(new AbstractBooleanDescriptor("Border Bottom Line")
            {

                @Override
                public void setValue(Boolean value)
                {
                    borderProperties.setShowBottomLine(value);
                    editor.setDirty(true);
                }

                @Override
                public Boolean getValue()
                {
                    return borderProperties.isShowBottomLine();
                }
            });
            descriptors.add(new AbstractBooleanDescriptor("Border Left Line")
            {

                @Override
                public void setValue(Boolean value)
                {
                    borderProperties.setShowLeftLine(value);
                    editor.setDirty(true);
                }

                @Override
                public Boolean getValue()
                {
                    return borderProperties.isShowLeftLine();
                }
            });
            descriptors.add(new AbstractBooleanDescriptor("Border Right Line")
            {

                @Override
                public void setValue(Boolean value)
                {
                    borderProperties.setShowRightLine(value);
                    editor.setDirty(true);
                }

                @Override
                public Boolean getValue()
                {
                    return borderProperties.isShowRightLine();
                }
            });

            return descriptors.toArray(new AbstractDescriptor<?>[0]);

        }

        @Override
        public boolean isLeaf()
        {
            return false;
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            return new INodeRenameProvider()
            {

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename Column [%s]", source.getName()),
                            "Column Name", source.getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "Column name can't be empty.";
                                    if (source.getName().equals(newText.trim()))
                                        return "";
                                    if (source.getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (ReportBlockColumnGroupNode.this.source.contains(newText.trim()))
                                        return "Column with this name already exists.";
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

            AbstractBooleanDescriptor showHeader = new AbstractBooleanDescriptor("Show Header")
            {

                @Override
                public void setValue(Boolean value)
                {
                    source.setShowHeader(value);
                    editor.setDirty(true);
                    treeSection.refresh(getParent());
                }

                @Override
                public Boolean getValue()
                {
                    return source.isShowHeader();
                }
            };
            AbstractBooleanDescriptor showFooter = new AbstractBooleanDescriptor("Show Footer")
            {

                @Override
                public void setValue(Boolean value)
                {
                    source.setShowFooter(value);
                    editor.setDirty(true);
                    treeSection.refresh(getParent());
                }

                @Override
                public Boolean getValue()
                {
                    return source.isShowFooter();
                }
            };
            descriptors.add(showHeader);
            descriptors.add(showFooter);
            return descriptors.toArray(new AbstractDescriptor<?>[0]);
        }

        public void addOverview(StyledString styledString)
        {

        }

    }

    public boolean canMove(Neighbor relation, Object source)
    {
        // only allow to DnD with in the same block
        return (source instanceof EJPluginReportColumnProperties && ((EJPluginReportColumnProperties) source).getBlockProperties().equals(
                this.source.getBlockProperties()));
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {

        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginReportColumnProperties> items = source.getAllColumnProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addColumnProperties(index, (EJPluginReportColumnProperties) dSource);
            }
        }
        else
            source.addColumnProperties((EJPluginReportColumnProperties) dSource);

    }

    public Action createNewColumnAction(final EJReportColumnContainer container, final int index)
    {

        return new Action("New Screen Column")
        {
            @Override
            public void runWithEvent(Event event)
            {
                addScreenColumn(getText());
            }

            void addScreenColumn(String name)
            {
                InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("New Screen Column : [%s]", name), "Column Name", null,
                        new IInputValidator()
                        {

                            public String isValid(String newText)
                            {
                                if (newText == null || newText.trim().length() == 0)
                                    return "Column name can't be empty.";
                                if (container.contains(newText.trim()))
                                    return "Column with this name already exists.";

                                return null;
                            }
                        });
                if (dlg.open() == Window.OK)
                {
                    final EJPluginReportColumnProperties itemProperties = new EJPluginReportColumnProperties(container.getBlockProperties());
                    container.addColumnProperties(itemProperties);
                    if (itemProperties != null)
                    {
                        // set default width/height
                        itemProperties.setName(dlg.getValue());

                        itemProperties.getHeaderScreen().setWidth(120);
                        itemProperties.getHeaderScreen().setHeight(30);
                        itemProperties.getDetailScreen().setWidth(120);
                        itemProperties.getDetailScreen().setHeight(30);
                        itemProperties.getFooterScreen().setWidth(120);
                        itemProperties.getFooterScreen().setHeight(30);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                treeSection.refresh(ReportBlockColumnGroupNode.this);
                                treeSection.selectNodes(true, treeSection.findNode(itemProperties, true));

                            }
                        });
                    }
                }
            }

        };
    }

}
