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

import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.form.FormNodeTag;
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
    private final ReportBlockGroupNode blockGroupNode;
    private boolean forColumnSection;
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
                                                                  if ((marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE) & FormNodeTag.FORM) != 0)
                                                                  {
                                                                      fmarkers.add(marker);
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

    public ReportScreenNode(ReportDesignTreeSection treeSection,AbstractNode<?> parent, ReportBlockGroupNode node, EJPluginReportScreenProperties group)
    {
        super(parent, group);
        this.treeSection = treeSection;
        this.blockGroupNode = node;
    }
    public ReportScreenNode(ReportDesignTreeSection treeSection,AbstractNode<?> parent, EJPluginReportScreenProperties group)
    {
        super(parent, group);
        this.treeSection = treeSection;
        this.blockGroupNode = null;
        forColumnSection = true;
    }

    @Override
    public String getName()
    {

        return "Report Screen";
    }

    public void addOverview(StyledString styledString)
    {
        // source.addOverview(styledString);
        
        if(source.getScreenType()!=EJReportScreenType.NONE)
        {
            styledString.append(" : ", StyledString.DECORATIONS_STYLER);
            styledString.append(source.getScreenType().toString(), StyledString.QUALIFIER_STYLER);
        }
        if(source.getScreenType()==EJReportScreenType.FORM_LATOUT)
        {
            styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
            styledString.append("(width,height) = ("+source.getWidth()+" ,"+source.getHeight()+")",StyledString.DECORATIONS_STYLER);
     
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
                treeSection.refresh(ReportScreenNode.this);
                if(blockGroupNode!=null)
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
                treeSection.refresh(ReportScreenNode.this);
                if(blockGroupNode!=null)
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
                if(blockGroupNode!=null)
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
                if(blockGroupNode!=null)
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

        AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Layout", "The renderer you have chosen for your block")
        {
            Filter vfilter = new Filter()
                           {

                               public boolean match(int tag, IMarker marker)
                               {

                                   return (tag & FormNodeTag.RENDERER) != 0;
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

            public String[] getOptions()
            {
                List<String> options = new ArrayList<String>();
                for (EJReportScreenType type : EJReportScreenType.values())
                {
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
                if(blockGroupNode!=null)
                    treeSection.refresh(blockGroupNode);
            }

            @Override
            public String getValue()
            {
                return source.getScreenType().name();
            }
        };

        if(!forColumnSection)
        {
            descriptors.add(rendererDescriptor);
            
            descriptors.add(xDescriptor);
            descriptors.add(yDescriptor);
        }
        
        descriptors.add(widthDescriptor);
        descriptors.add(heightDescriptor);
        return descriptors.toArray(new AbstractDescriptor<?>[0]);
    }

    public <S> S getAdapter(Class<S> adapter)
    {
       
        
        if (IReportPreviewProvider.class.isAssignableFrom(adapter))
        {
            if(source.getScreenType()==EJReportScreenType.FORM_LATOUT)
                return adapter.cast(new  ReportScreenPreviewImpl(source));
            
            if(source.getScreenType()==EJReportScreenType.TABLE_LAYOUT)
                return adapter.cast(new  ReportScreenColumnPreviewImpl(source));
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

        if(source.getScreenType()==EJReportScreenType.FORM_LATOUT)
        {
            nodes.add(new ReportBlockScreenItemsGroupNode(treeSection, this,forColumnSection));
            if(blockGroupNode!=null)
                nodes.add(blockGroupNode.createScreenGroupNode(this,source.getSubBlocks()));
        }
        else if (source.getScreenType()==EJReportScreenType.TABLE_LAYOUT)
        {
            nodes.add(new ReportBlockColumnGroupNode(treeSection, this));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public boolean isLeaf()
    {
        return false;
    }

    
   
    
    @Override
    public Action[] getActions()
    {
        return new Action[0];
    }

}
