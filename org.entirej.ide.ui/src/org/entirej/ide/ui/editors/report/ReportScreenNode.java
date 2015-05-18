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
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.report.enumerations.EJReportChartType;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;

public class ReportScreenNode extends AbstractNode<EJPluginReportScreenProperties> implements NodeOverview
{

    private static final Image            GROUP       = EJUIImages.getImage(EJUIImages.DESC_ITEMS_SCREEN);
    private static final Image            GROUP_ITEM  = EJUIImages.getImage(EJUIImages.DESC_ITEMS_GROUP);
    static final Image                    ITEMS_SPACE = EJUIImages.getImage(EJUIImages.DESC_ITEMS_SPACE);
    private final ReportDesignTreeSection treeSection;
    private final ReportBlockGroupNode    blockGroupNode;
    private boolean                       forColumnSection;
    private AbstractMarkerNodeValidator   validator   = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(ReportScreenNode.this);
                                                          }

                                                          @Override
                                                          public List<IMarker> getMarkers()
                                                          {
                                                              List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                              IMarker[] markers = treeSection.getEditor().getMarkers(EJMarkerFactory.MARKER_ID);
                                                              for (IMarker marker : markers)
                                                              {
                                                                  if ((marker.getAttribute(NodeValidateProvider.NODE_TAG, ReportNodeTag.NONE) & ReportNodeTag.REPORT) != 0)
                                                                  {
                                                                      fmarkers.add(marker);
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

    public ReportScreenNode(ReportDesignTreeSection treeSection, AbstractNode<?> parent, ReportBlockGroupNode node, EJPluginReportScreenProperties group)
    {
        super(parent, group);
        this.treeSection = treeSection;
        this.blockGroupNode = node;
    }

    public ReportScreenNode(ReportDesignTreeSection treeSection, AbstractNode<?> parent, EJPluginReportScreenProperties group)
    {
        super(parent, group);
        this.treeSection = treeSection;
        this.blockGroupNode = null;
        forColumnSection = true;
    }

    public boolean isWidthSuppoted()
    {
        return true;
    }

    @Override
    public String getName()
    {

        return "Report Screen";
    }

    public void addOverview(StyledString styledString)
    {
        // source.addOverview(styledString);

        if (source.getScreenType() != EJReportScreenType.NONE)
        {
            styledString.append(" : ", StyledString.DECORATIONS_STYLER);
            styledString.append(source.getScreenType().toString(), StyledString.QUALIFIER_STYLER);
        }
        if (source.getScreenType() == EJReportScreenType.FORM_LAYOUT)
        {
            styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
            styledString.append("(width,height) = (" + source.getWidth() + " ," + source.getHeight() + ")", StyledString.DECORATIONS_STYLER);

            styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
        }

    }

    @Override
    public Image getImage()
    {
        return GROUP;
    }

    @Override
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();

        final List<IMarker> fmarkers = validator.getMarkers();

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
            public void runOperation(AbstractOperation operation)
            {
                treeSection.getEditor().execute(operation);

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
                treeSection.refresh(ReportScreenNode.this);
                if (blockGroupNode != null)
                    treeSection.refresh(blockGroupNode);
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
            public void runOperation(AbstractOperation operation)
            {
                treeSection.getEditor().execute(operation);

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
                treeSection.refresh(ReportScreenNode.this);
                if (blockGroupNode != null)
                    treeSection.refresh(blockGroupNode);
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
            public void runOperation(AbstractOperation operation)
            {
                treeSection.getEditor().execute(operation);

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
                treeSection.refresh(ReportScreenNode.this);
                if (blockGroupNode != null)
                    treeSection.refresh(blockGroupNode);
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
            public void runOperation(AbstractOperation operation)
            {
                treeSection.getEditor().execute(operation);

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
                treeSection.refresh(ReportScreenNode.this);
                if (blockGroupNode != null)
                    treeSection.refresh(blockGroupNode);
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

        AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Layout", "The renderer you have chosen for your block")
        {
            Filter vfilter = new Filter()
                           {

                               public boolean match(int tag, IMarker marker)
                               {

                                   return (tag & ReportNodeTag.RENDERER) != 0;
                               }
                           };

            @Override
            public String getErrors()
            {

                return validator.getErrorMarkerMsg(fmarkers, vfilter);
            }

            @Override
            public void runOperation(AbstractOperation operation)
            {
                treeSection.getEditor().execute(operation);

            }

            @Override
            public String getWarnings()
            {
                return validator.getWarningMarkerMsg(fmarkers, vfilter);
            }

            public String[] getOptions()
            {
                List<String> options = new ArrayList<String>();

                EJReportBlockContainer blockContainer = source.getBlockProperties().getReportProperties().getBlockContainer();
                boolean blockTableLayout = blockContainer.getHeaderSection().contains(source.getBlockProperties().getName())
                        || blockContainer.getFooterSection().contains(source.getBlockProperties().getName());

                for (EJReportScreenType type : EJReportScreenType.values())
                {
                    if (blockTableLayout && type != EJReportScreenType.FORM_LAYOUT)
                        continue;
                    options.add(type.name());
                }
                return options.toArray(new String[0]);
            }

            public String getOptionText(String t)
            {

                return EJReportScreenType.valueOf(t).toString();
            }

            @Override
            public void setValue(String value)
            {
                source.setScreenType(EJReportScreenType.valueOf(value));

                treeSection.getEditor().setDirty(true);
                treeSection.refresh(ReportScreenNode.this);
                treeSection.expand(ReportScreenNode.this);
                treeSection.getDescriptorViewer().showDetails(ReportScreenNode.this);
                if (blockGroupNode != null)
                    treeSection.refresh(blockGroupNode);
            }

            @Override
            public String getValue()
            {
                return source.getScreenType().name();
            }
        };

        if (!forColumnSection)
        {
            descriptors.add(rendererDescriptor);

            descriptors.add(xDescriptor);
            descriptors.add(yDescriptor);
        }

        if (isWidthSuppoted())
            descriptors.add(widthDescriptor);
        descriptors.add(heightDescriptor);

        tableLayoutSettings(descriptors);

        chartLayoutSettings(fmarkers, descriptors);

        return descriptors.toArray(new AbstractDescriptor<?>[0]);
    }

    private void chartLayoutSettings(final List<IMarker> fmarkers, List<AbstractDescriptor<?>> descriptors)
    {
        if (source.getScreenType() == EJReportScreenType.CHART_LAYOUT)
        {
            AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Type", "The chart type need to renderer")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & ReportNodeTag.CHART_TYPE) != 0;
                                   }
                               };

                @Override
                public String getErrors()
                {

                    return validator.getErrorMarkerMsg(fmarkers, vfilter);
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                @Override
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();

                    for (EJReportChartType type : EJReportChartType.values())
                    {

                        options.add(type.name());
                    }
                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return EJReportChartType.valueOf(t).toString();
                }

                @Override
                public void setValue(String value)
                {
                    source.getChartProperties().setChartType(EJReportChartType.valueOf(value));

                    treeSection.getDescriptorViewer().showDetails(ReportScreenNode.this);

                    treeSection.getEditor().setDirty(true);
                }

                @Override
                public String getValue()
                {
                    return source.getChartProperties().getChartType().name();
                }
            };

            descriptors.add(rendererDescriptor);

            AbstractBooleanDescriptor support3dView = new AbstractBooleanDescriptor("Use 3d View")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                @Override
                public void setValue(Boolean value)
                {
                    source.getChartProperties().setUse3dView(value);
                    treeSection.getEditor().setDirty(true);
                }

                @Override
                public Boolean getValue()
                {
                    return source.getChartProperties().isUse3dView();
                }
            };
            

            // value 1 as basic value provider

            ReportBlockItemsGroupNode.ItemDefaultValue valueProvider = new ReportBlockItemsGroupNode.ItemDefaultValue(treeSection.getEditor(), source
                    .getBlockProperties().getReportProperties(), source.getBlockProperties(), "Value")
            {
                @Override
                public String getValue()
                {
                    return source.getChartProperties().getValue1Item();
                }

                @Override
                public void setValue(Object value)
                {
                    source.getChartProperties().setValue1Item((String) value);
                    editor.setDirty(true);
                }

                @Override
                public String getDefaultBlockValue()
                {
                    return ReportScreenNode.this.source.getBlockProperties().getName();
                }

            };

            ReportBlockItemsGroupNode.ItemDefaultValue labelProvider = new ReportBlockItemsGroupNode.ItemDefaultValue(treeSection.getEditor(), source
                    .getBlockProperties().getReportProperties(), source.getBlockProperties(), "Label")
            {
                @Override
                public String getValue()
                {
                    return source.getChartProperties().getLabelItem();
                }

                @Override
                public void setValue(Object value)
                {
                    source.getChartProperties().setLabelItem((String) value);
                    editor.setDirty(true);
                }

                @Override
                public String getDefaultBlockValue()
                {
                    return ReportScreenNode.this.source.getBlockProperties().getName();
                }

            };

            ReportBlockItemsGroupNode.ItemDefaultValue seriesProvider = new ReportBlockItemsGroupNode.ItemDefaultValue(treeSection.getEditor(), source
                    .getBlockProperties().getReportProperties(), source.getBlockProperties(), "Series")
            {
                @Override
                public String getValue()
                {
                    return source.getChartProperties().getSeriesItem();
                }

                @Override
                public void setValue(Object value)
                {
                    source.getChartProperties().setSeriesItem((String) value);
                    editor.setDirty(true);
                }

                @Override
                public String getDefaultBlockValue()
                {
                    return ReportScreenNode.this.source.getBlockProperties().getName();
                }

            };

            ReportBlockItemsGroupNode.ItemDefaultValue categoryProvider = new ReportBlockItemsGroupNode.ItemDefaultValue(treeSection.getEditor(), source
                    .getBlockProperties().getReportProperties(), source.getBlockProperties(), "Category")
            {
                @Override
                public String getValue()
                {
                    return source.getChartProperties().getCategoryItem();
                }

                @Override
                public void setValue(Object value)
                {
                    source.getChartProperties().setCategoryItem((String) value);
                    editor.setDirty(true);
                }

                @Override
                public String getDefaultBlockValue()
                {
                    return ReportScreenNode.this.source.getBlockProperties().getName();
                }

            };
            
            
            
            
            

            switch (source.getChartProperties().getChartType())
            {
                case BAR_CHART:
                case STACKED_BAR_CHART:
                {

                    seriesProvider.setRequired(true);
                    valueProvider.setRequired(true);
                    descriptors.add(seriesProvider);
                    descriptors.add(valueProvider);
                    descriptors.add(categoryProvider);
                    descriptors.add(labelProvider);
                    descriptors.add(support3dView);
                    break;
                }
                case AREA_CHART:
                case STACKED_AREA_CHART:
                {
                    
                    seriesProvider.setRequired(true);
                    valueProvider.setRequired(true);
                    descriptors.add(seriesProvider);
                    descriptors.add(valueProvider);
                    descriptors.add(categoryProvider);
                    descriptors.add(labelProvider);
                    
                }

                    break;

                case PIE_CHART:
                {

                    seriesProvider.setText("Key");

                    seriesProvider.setRequired(true);
                    valueProvider.setRequired(true);
                    descriptors.add(seriesProvider);
                    descriptors.add(valueProvider);
                    descriptors.add(labelProvider);
                    descriptors.add(support3dView);

                }

                    break;

                default:
                    break;
            }

        }
    }

    private void tableLayoutSettings(List<AbstractDescriptor<?>> descriptors)
    {
        if (source.getScreenType() == EJReportScreenType.TABLE_LAYOUT)
        {

            final AbstractTextDescriptor hHeightDescriptor = new AbstractTextDescriptor("Header")
            {

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setHeaderColumnHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setHeaderColumnHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ReportScreenNode.this);
                    if (blockGroupNode != null)
                        treeSection.refresh(blockGroupNode);
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getHeaderColumnHeight());
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

            final AbstractTextDescriptor dHeightDescriptor = new AbstractTextDescriptor("Detail")
            {

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setDetailColumnHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setDetailColumnHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ReportScreenNode.this);
                    if (blockGroupNode != null)
                        treeSection.refresh(blockGroupNode);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getDetailColumnHeight());
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

            final AbstractTextDescriptor fHeightDescriptor = new AbstractTextDescriptor("Footer")
            {

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setFooterColumnHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setFooterColumnHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    treeSection.getEditor().setDirty(true);
                    treeSection.refresh(ReportScreenNode.this);
                    if (blockGroupNode != null)
                        treeSection.refresh(blockGroupNode);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getFooterColumnHeight());
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

            AbstractGroupDescriptor sectionheights = new AbstractGroupDescriptor("Default Column Heights")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    // TODO Auto-generated method stub
                    return new AbstractDescriptor<?>[] { hHeightDescriptor, dHeightDescriptor, fHeightDescriptor };
                }
            };
            AbstractTextDropDownDescriptor vaOddDescriptor = new AbstractTextDropDownDescriptor("Odd Record VA", "")
            {
                List<String> visualAttributeNames = new ArrayList<String>(treeSection.getEditor().getReportProperties().getEntireJProperties()
                                                          .getVisualAttributesContainer().getVisualAttributeNames());

                @Override
                public void setValue(String value)
                {
                    source.setOddRowVAName(value);
                    treeSection.getEditor().setDirty(true);
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                @Override
                public String getValue()
                {
                    return source.getOddRowVAName();
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
            AbstractTextDropDownDescriptor vaEvenDescriptor = new AbstractTextDropDownDescriptor("Even Record VA", "")
            {
                List<String> visualAttributeNames = new ArrayList<String>(treeSection.getEditor().getReportProperties().getEntireJProperties()
                                                          .getVisualAttributesContainer().getVisualAttributeNames());

                @Override
                public void setValue(String value)
                {
                    source.setEvenRowVAName(value);
                    treeSection.getEditor().setDirty(true);
                }

                @Override
                public void runOperation(AbstractOperation operation)
                {
                    treeSection.getEditor().execute(operation);

                }

                @Override
                public String getValue()
                {
                    return source.getEvenRowVAName();
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

            descriptors.add(sectionheights);
            descriptors.add(vaOddDescriptor);
            descriptors.add(vaEvenDescriptor);
        }
    }

    public <S> S getAdapter(Class<S> adapter)
    {

        if (IReportPreviewProvider.class.isAssignableFrom(adapter))
        {
            if (source.getScreenType() == EJReportScreenType.FORM_LAYOUT)
                return adapter.cast(new ReportScreenPreviewImpl(source));

            if (source.getScreenType() == EJReportScreenType.TABLE_LAYOUT)
                return adapter.cast(new ReportScreenColumnPreviewImpl(source));
        }
        return null;
    }

    @Override
    public INodeDeleteProvider getDeleteProvider()
    {

        return null;
    }

    @Override
    public INodeRenameProvider getRenameProvider()
    {
        return null;
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {

        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();

        if (source.getScreenType() == EJReportScreenType.FORM_LAYOUT)
        {
            nodes.add(new ReportBlockScreenItemsGroupNode(treeSection, this, forColumnSection));
            if (blockGroupNode != null)
                nodes.add(blockGroupNode.createScreenGroupNode(this, source.getSubBlocks()));
        }
        else if (source.getScreenType() == EJReportScreenType.TABLE_LAYOUT)
        {
            nodes.add(new ReportBlockColumnGroupNode(treeSection, this));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public boolean isLeaf()
    {
        return source.getScreenType() != EJReportScreenType.CHART_LAYOUT;
    }

    @Override
    public Action[] getActions()
    {
        return new Action[0];
    }

}
