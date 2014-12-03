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
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.reports.EJPluginReportBlockProperties;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.framework.report.actionprocessor.EJDefaultReportActionProcessor;
import org.entirej.framework.report.actionprocessor.interfaces.EJReportActionProcessor;
import org.entirej.framework.report.enumerations.EJReportExportType;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.report.operations.ReportBlockAddOperation;
import org.entirej.ide.ui.editors.report.wizards.DataBlockServiceWizard;
import org.entirej.ide.ui.editors.report.wizards.DataBlockWizardContext;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.table.TableViewerColumnFactory;

public class ReportDesignTreeSection extends AbstractNodeTreeSection
{
    protected final AbstractEJReportEditor        editor;

    protected ReportPreviewer                     reportPreviewer;

    protected AbstractNode<?>                     baseNode;
    protected final EJDevItemWidgetChosenListener chosenListener = new EJDevItemWidgetChosenListener()
                                                                 {

                                                                     public void fireRendererChosen(EJDevScreenItemDisplayProperties arg0)
                                                                     {
                                                                         if (arg0 != null)
                                                                         {

                                                                             AbstractNode<?> findNode = findNode(arg0, true);
                                                                             if (findNode != null)
                                                                             {
                                                                                 selectNodes(true, findNode);
                                                                             }
                                                                         }

                                                                     }
                                                                 };

    public ReportDesignTreeSection(AbstractEJReportEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent);
        this.editor = editor;
        initTree();
        addDnDSupport(null);// no root move need in layout
    }

    public AbstractEJReportEditor getEditor()
    {
        return editor;
    }

    @Override
    protected void nodesUpdated()
    {
        editor.setDirty(true);
    }

    @Override
    protected void showNodeDetails(AbstractNode<?> node)
    {
        // use report node as default node to editor
        if (node == null)
            node = baseNode;

        super.showNodeDetails(node);
        if (reportPreviewer != null)
            reportPreviewer.showDetails(node);
    }

    public void refreshPreview()
    {
        if (reportPreviewer != null)
            Display.getDefault().asyncExec(new Runnable()
            {

                public void run()
                {
                    reportPreviewer.refresh();

                }
            });
    }

    public void setReportPreviewer(ReportPreviewer reportPreviewer)
    {
        this.reportPreviewer = reportPreviewer;
    }

    @Override
    protected void buildBody(Section section, FormToolkit toolkit)
    {
        super.buildBody(section, toolkit);

        super.buildBody(section, toolkit);
        GridData sectionData = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
        sectionData.widthHint = 400;
        section.setLayoutData(sectionData);

    }

    @Override
    public Object getTreeInput()
    {
        return new Object();
    }

    @Override
    public String getSectionTitle()
    {
        return "Report Setup";
    }

    @Override
    public String getSectionDescription()
    {

        return "Define design/settings of the report in the following section.";
    }

    @Override
    public void addToolbarCustomActions(ToolBarManager toolBarManager, final ToolBar toolbar)
    {
        // create add item Action
        MenuManager popupBlockMenuManager = new MenuManager();
        IMenuListener listenerBlock = new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager mng)
            {
                addMenuActions(mng, getNewBlockActions());
                // if empty, add dummy action item show no actions available
                if (mng.getItems().length == 0)
                {
                    mng.add(new Action("no actions available")
                    {
                    });
                }
            }
        };
        popupBlockMenuManager.addMenuListener(listenerBlock);
        popupBlockMenuManager.setRemoveAllWhenShown(true);
        final Menu menuBlock = popupBlockMenuManager.createContextMenu(toolbar);

        toolBarManager.add(new Separator());
    }

    @Override
    public Action[] getBaseActions()
    {

        return new Action[] { createNewBlockAction(false), createNewBlockAction(true), };
    }

    protected Action[] getNewBlockActions()
    {
        return new Action[] { createNewBlockAction(false), createNewBlockAction(true), };
    }

    @Override
    public AbstractNodeContentProvider getContentProvider()
    {
        return new AbstractNodeContentProvider()
        {

            public Object[] getElements(Object inputElement)
            {
                // project build errors
                if (editor.getReportProperties() == null)
                    return new Object[0];
                return new Object[] { baseNode = new ReportNode(editor.getReportProperties()), new ReportBlockGroupNode(ReportDesignTreeSection.this) };
            }
        };
    }

    private class ReportNode extends AbstractNode<EJPluginReportProperties> implements NodeOverview
    {
        private final Image                 REPORT    = EJUIImages.getImage(EJUIImages.DESC_REPORT);
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              refresh(ReportNode.this);
                                                          }

                                                          @Override
                                                          public List<IMarker> getMarkers()
                                                          {
                                                              List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                              IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
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

        private ReportNode(EJPluginReportProperties source)
        {
            super(null, source);
        }

        public String getName()
        {
            String name = source.getName();
            if (name == null || name.length() == 0)
                name = "<report>";

            return name;
        }

        @Override
        public String getToolTipText()
        {
            return source.getName();
        }

        @Override
        public String getNodeDescriptorDetails()
        {

            return "Click <a href=\"http://docs.entirej.com/display/EJ1/Report+Properties#ReportProperties\">here</a> for more information on Report Properties. All mandatory properties are denoted by \"*\"";
        }

        public <S> S getAdapter(Class<S> adapter)
        {
            if (NodeValidateProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(validator);
            }
            if (IReportPreviewProvider.class.isAssignableFrom(adapter))
            {
                /*
                 * return adapter.cast(new FormCanvasPreviewImpl() {
                 * 
                 * @Override protected void setPreviewBackground(Control
                 * control, Color color) { // IGNORE }
                 * 
                 * @Override public String getDescription() { return
                 * "preview the defined layout in form."; }
                 * 
                 * @Override protected void createComponent(Composite parent,
                 * EJPluginCanvasProperties component) { if
                 * (component.getPluginBlockProperties() != null) {
                 * EJPluginMainScreenProperties mainScreenProperties =
                 * component.
                 * getPluginBlockProperties().getMainScreenProperties();
                 * 
                 * Composite layoutBody = new Composite(parent, SWT.NONE);
                 * 
                 * layoutBody.setLayout(new FillLayout());
                 * 
                 * layoutBody.setLayoutData(createGridData(component));
                 * component
                 * .getPluginBlockProperties().getBlockRendererDefinition()
                 * .addBlockControlToCanvas(mainScreenProperties,
                 * component.getPluginBlockProperties(), layoutBody,
                 * editor.getToolkit())
                 * .addItemWidgetChosenListener(chosenListener); } else {
                 * super.createComponent(parent, component); } } });
                 */
            }
            return null;
        }

        @Override
        public Image getImage()
        {
            return REPORT;
        }

        @Override
        public Action[] getActions()
        {
            return getBaseActions();
        }

        @Override
        public AbstractDescriptor<?>[] getNodeDescriptors()
        {
            final List<IMarker> fmarkers = validator.getMarkers();

            final AbstractTextDescriptor formDisplayNameDescriptor = new AbstractTextDescriptor(
                    "Display Name",
                    "If you are using more cryptic names for your reports i.e. FRM001, FRM002 etc, then you may want to have a different name displayed in your project tree so you can find your report easier")
            {

                @Override
                public void setValue(String value)
                {
                    source.setReportDisplayName(value);
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getReportDisplayName();
                }

            };

            AbstractGroupDescriptor metadataGroupDescriptor = new AbstractGroupDescriptor("Metadata")
            {

                @Override
                public String getTooltip()
                {
                    return "Contains properties that are only used within the plugin and are not used within the running application";
                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { formDisplayNameDescriptor };
                }
            };

            AbstractTextDescriptor titleDescriptor = new AbstractTextDescriptor("Title")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & ReportNodeTag.TITLE) != 0;
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
                public void setValue(String value)
                {
                    source.setReportTitle(value);
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getTitle();
                }

                @Override
                public String getTooltip()
                {
                    return "This is the title displayed to the user when the report is run. The title will be sent to the applications Translator for translation if required";
                }

            };
            titleDescriptor.setRequired(true);

            AbstractTypeDescriptor actionDescriptor = new AbstractTypeDescriptor(editor, "Action Processor")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & ReportNodeTag.ACTION_PROCESSOR) != 0;
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
                public void setValue(String value)
                {
                    source.setActionProcessorClassName(value);
                    editor.setDirty(true);
                    refresh(ReportNode.this);

                }

                @Override
                public String getValue()
                {
                    return source.getActionProcessorClassName();
                }

                @Override
                public String getTooltip()
                {
                    return "The action processor to use for this report. Action Processors are used as event handlers for your report";
                }
            };
            actionDescriptor.setBaseClass(EJReportActionProcessor.class.getName());
            actionDescriptor.setDefaultClass(EJDefaultReportActionProcessor.class.getName());

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
                        source.setReportWidth(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setReportWidth(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getReportWidth());
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
                        source.setReportHeight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setReportHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getReportHeight());
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

            
            
            final AbstractBooleanDescriptor ignorePages = new AbstractBooleanDescriptor("Ignore Page Break")
            {
                
                @Override
                public void setValue(Boolean value)
                {
                    
                    source.setIgnorePagination(value);
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                    
                }
                
                @Override
                public Boolean getValue()
                {
                    // TODO Auto-generated method stub
                    return source.isIgnorePagination();
                }
            }; 
            
            
            final AbstractTextDescriptor headerSectionDescriptor = new AbstractTextDescriptor("Header Height")
            {

                @Override
                public String getTooltip()
                {

                    return "The Header section height <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setHeaderSectionHeight((Integer.parseInt(value)));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setHeaderSectionHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getHeaderSectionHeight());
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
            final AbstractTextDescriptor footerSectionDescriptor = new AbstractTextDescriptor("Footer Height")
            {

                @Override
                public String getTooltip()
                {

                    return "The Footer section height <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setFooterSectionHeight((Integer.parseInt(value)));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setFooterSectionHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getFooterSectionHeight());
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

            final AbstractTextDescriptor topMarginDescriptor = new AbstractTextDescriptor("Top Margin")
            {

                @Override
                public String getTooltip()
                {

                    return "The Top Margin <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setMarginTop(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setMarginTop(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getMarginTop());
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

            final AbstractTextDescriptor bottomMarginDescriptor = new AbstractTextDescriptor("Bottom Margin")
            {

                @Override
                public String getTooltip()
                {

                    return "The Bottom Margin <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setMarginBottom(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setMarginBottom(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getMarginBottom());
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

            final AbstractTextDescriptor leftMarginDescriptor = new AbstractTextDescriptor("Left Margin")
            {

                @Override
                public String getTooltip()
                {

                    return "The Left Margin <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setMarginLeft(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setMarginLeft(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getMarginLeft());
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
            final AbstractTextDescriptor rightMarginDescriptor = new AbstractTextDescriptor("Right Margin")
            {

                @Override
                public String getTooltip()
                {

                    return "The Right Margin <b>(in pixels)</b> of the report within it's Page.";
                }

                @Override
                public void setValue(String value)
                {
                    try
                    {
                        source.setMarginRight(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.setMarginRight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getMarginRight());
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

            AbstractTextDropDownDescriptor exportTypeDescriptor = new AbstractTextDropDownDescriptor("Export Type")
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
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    for (EJReportExportType type : EJReportExportType.values())
                    {
                        options.add(type.name());
                    }
                    return options.toArray(new String[0]);
                }

                public String getOptionText(String t)
                {

                    return EJReportExportType.valueOf(t).toString();
                }

                @Override
                public void setValue(String value)
                {
                    source.setExportType(EJReportExportType.valueOf(value));

                    editor.setDirty(true);
                    refresh(ReportNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getExportType().name();
                }
            };

            
            

            AbstractTextDropDownDescriptor vaDescriptor = new AbstractTextDropDownDescriptor("Default Visual Attributes", "")
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
            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
            {

                @Override
                public String getTooltip()
                {
                    return "Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Report\">here</a> For more information on laying out an EntireJ Report";

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { widthDescriptor, heightDescriptor, headerSectionDescriptor, footerSectionDescriptor,
                            topMarginDescriptor, bottomMarginDescriptor, leftMarginDescriptor, rightMarginDescriptor };
                }
            };

            AbstractGroupDescriptor parametersDes = new AbstractGroupDescriptor("Report Parameters")
            {
                IRefreshHandler              handler;
                TableViewer                  tableViewer;
                EJPluginApplicationParameter entry = null;
                Action                       deleteAction;

                @Override
                public String getTooltip()
                {
                    return "Report parameters are report global variables that can be accessed from the reports action processor or used as default Query  values on the block items. For more information, read the Form Properties section <a href=\"http://docs.entirej.com/display/EJ1/Report+Properties#FormProperties-ReportParameters\">here</a>";
                }

                public Action[] getToolbarActions()
                {

                    Action addAction = new Action("Add", IAction.AS_PUSH_BUTTON)
                    {

                        @Override
                        public void runWithEvent(Event event)
                        {

                            EJPluginApplicationParameter newEntry = new EJPluginApplicationParameter("", "java.lang.String");
                            source.addReportParameter(newEntry);

                            if (tableViewer != null)
                            {
                                tableViewer.add(newEntry);
                                tableViewer.setSelection(new StructuredSelection(newEntry), true);
                            }
                            editor.setDirty(true);
                        }

                    };
                    addAction.setImageDescriptor(EJUIImages.DESC_ADD_ITEM);

                    // create delete Action
                    deleteAction = new Action("Delete", IAction.AS_PUSH_BUTTON)
                    {

                        @Override
                        public void run()
                        {
                            if (entry == null)
                                return;

                            source.removeReportParameter(entry);
                            if (tableViewer != null)
                            {
                                tableViewer.remove(entry);
                                if (tableViewer.getTable().getItemCount() > 0)
                                    tableViewer.getTable().select(tableViewer.getTable().getItemCount() - 1);
                                if (tableViewer.getSelection() instanceof IStructuredSelection)
                                    entry = (EJPluginApplicationParameter) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                                if (handler != null)
                                    handler.refresh();
                                setEnabled(entry != null);
                            }
                            editor.setDirty(true);
                        }

                    };
                    deleteAction.setImageDescriptor(EJUIImages.DESC_DELETE_ITEM);
                    deleteAction.setDisabledImageDescriptor(EJUIImages.DESC_DELETE_ITEM_DISABLED);
                    deleteAction.setEnabled(entry != null);

                    return new Action[] { addAction, deleteAction };
                }

                public Control createHeader(final IRefreshHandler handler, Composite parent, GridData gd)
                {
                    this.handler = handler;
                    tableViewer = new TableViewer(parent, SWT.VIRTUAL | SWT.BORDER | SWT.FULL_SELECTION);

                    Table table = tableViewer.getTable();
                    table.setHeaderVisible(true);
                    TableViewerColumnFactory factory = new TableViewerColumnFactory(tableViewer);
                    ColumnViewerToolTipSupport.enableFor(tableViewer);
                    gd.verticalSpan = 2;
                    gd.heightHint = 150;
                    gd.widthHint = 100;
                    gd.horizontalIndent = 0;
                    table.setLayoutData(gd);

                    factory.createColumn("Name", 120, new ColumnLabelProvider()
                    {

                        @Override
                        public String getText(Object element)
                        {

                            if (element instanceof EJPluginApplicationParameter)
                            {
                                EJPluginApplicationParameter entry = (EJPluginApplicationParameter) element;
                                return entry.getName();
                            }
                            return "";
                        }
                    });
                    factory.createColumn("Data Type", 200, new ColumnLabelProvider()
                    {

                        @Override
                        public String getText(Object element)
                        {

                            if (element instanceof EJPluginApplicationParameter)
                            {
                                EJPluginApplicationParameter entry = (EJPluginApplicationParameter) element;
                                return entry.getDataTypeName();
                            }
                            return "";
                        }
                    });
                    /*
                     * factory.createColumn("Default Value", 120, new
                     * ColumnLabelProvider() {
                     * 
                     * @Override public String getText(Object element) {
                     * 
                     * if (element instanceof EJPluginApplicationParameter) {
                     * EJPluginApplicationParameter entry =
                     * (EJPluginApplicationParameter) element; return
                     * entry.getDefaultValue(); } return ""; } });
                     */
                    tableViewer.setContentProvider(new IStructuredContentProvider()
                    {

                        public void inputChanged(Viewer arg0, Object arg1, Object arg2)
                        {
                        }

                        public void dispose()
                        {
                        }

                        public Object[] getElements(Object arg0)
                        {
                            return (Object[]) getValue();
                        }
                    });
                    tableViewer.addSelectionChangedListener(new ISelectionChangedListener()
                    {

                        public void selectionChanged(SelectionChangedEvent event)
                        {
                            EJPluginApplicationParameter newEntry = null;
                            if (tableViewer.getSelection() instanceof IStructuredSelection)
                                newEntry = (EJPluginApplicationParameter) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                            if ((newEntry == null && entry != null) || (!newEntry.equals(entry)))
                            {
                                entry = newEntry;
                                handler.refresh();
                            }

                            if (deleteAction != null)
                                deleteAction.setEnabled(entry != null);
                        }
                    });

                    tableViewer.setInput(new Object());
                    if (tableViewer.getTable().getItemCount() > 0)
                        tableViewer.getTable().select(0);
                    if (tableViewer.getSelection() instanceof IStructuredSelection)
                        entry = (EJPluginApplicationParameter) ((IStructuredSelection) tableViewer.getSelection()).getFirstElement();
                    return table;
                }

                public Object getValue()
                {
                    return source.getAllReportParameters().toArray();
                };

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    if (entry == null)
                    {
                        return new AbstractDescriptor<?>[0];
                    }
                    AbstractTextDescriptor nameDescriptor = new AbstractTextDescriptor("Name")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            entry.setName(value);
                            editor.setDirty(true);
                            if (tableViewer != null)
                            {
                                tableViewer.refresh(entry);
                            }
                        }

                        @Override
                        public String getValue()
                        {
                            return entry.getName();
                        }
                    };
                    nameDescriptor.setRequired(true);
                    AbstractTypeDescriptor typeDescriptor = new AbstractTypeDescriptor(editor, "Data Type")
                    {

                        @Override
                        public void setValue(String value)
                        {
                            entry.setDataTypeName(value);
                            editor.setDirty(true);
                            if (tableViewer != null)
                            {
                                tableViewer.refresh(entry);
                            }

                        }

                        @Override
                        public String getValue()
                        {
                            return entry.getDataTypeName();
                        }
                    };
                    typeDescriptor.setBaseClass(Object.class.getName());
                    /*
                     * AbstractTextDescriptor defaultValueDescriptor = new
                     * AbstractTextDescriptor("Default Value") {
                     * 
                     * @Override public void setValue(String value) {
                     * entry.setDefaultValue(value); editor.setDirty(true); if
                     * (tableViewer != null) { tableViewer.refresh(entry); } }
                     * 
                     * @Override public String getValue() { return
                     * entry.getDefaultValue(); } }; return new
                     * AbstractDescriptor<?>[] { nameDescriptor,
                     * typeDescriptor,defaultValueDescriptor };
                     */
                    return new AbstractDescriptor<?>[] { nameDescriptor, typeDescriptor };
                }
            };

            return new AbstractDescriptor<?>[] { titleDescriptor, actionDescriptor, exportTypeDescriptor,vaDescriptor,ignorePages, layoutGroupDescriptor, parametersDes,
                    metadataGroupDescriptor };
        }

        public void addOverview(StyledString styledString)
        {
            if (source.getReportDisplayName() != null && source.getReportDisplayName().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getReportDisplayName(), StyledString.DECORATIONS_STYLER);

            }
            if (source.getTitle() != null && source.getTitle().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getTitle(), StyledString.COUNTER_STYLER);

            }

            styledString.append(
                    String.format("[ %d, %d ] [ %d, %d, %d, %d ]", source.getReportWidth(), source.getReportHeight(), source.getMarginLeft(),
                            source.getMarginTop(), source.getMarginRight(), source.getMarginBottom()), StyledString.DECORATIONS_STYLER);

        }
    }

    static interface ReportPreviewer extends INodeDescriptorViewer
    {
        void refresh();
    }

    public Action createNewBlockAction(final boolean controlBlock)
    {

        return new Action(controlBlock ? "New Report Control Block" : "New Report Service Block")
        {

            @Override
            public void runWithEvent(Event event)
            {
                DataBlockServiceWizard wizard = new DataBlockServiceWizard(new DataBlockWizardContext()
                {

                    
                    public boolean isBlockTablelayout()
                    {
                        return false;
                    }
                    public int getDefaultWidth()
                    {
                        final EJPluginReportProperties formProperties = editor.getReportProperties();
                        return formProperties.getReportWidth() - (formProperties.getMarginLeft() + formProperties.getMarginRight());
                    }

                    public int getDefaultHeight()
                    {
                        final EJPluginReportProperties formProperties = editor.getReportProperties();

                        int dtlHeight = formProperties.getReportHeight()
                                - (formProperties.getMarginTop() + formProperties.getMarginBottom() + formProperties.getHeaderSectionHeight() + formProperties
                                        .getFooterSectionHeight());
                        return dtlHeight > 40 ? 40 : dtlHeight;
                    }

                    public void addBlock(String blockName, String serviceClass, EJReportScreenType type, int x, int y, int width, int height)
                    {
                        final EJPluginReportProperties formProperties = editor.getReportProperties();
                        final EJPluginReportBlockProperties blockProperties = new EJPluginReportBlockProperties(formProperties, blockName, controlBlock);

                        EJPluginReportScreenProperties screenProperties = blockProperties.getLayoutScreenProperties();
                        screenProperties.setScreenType(type);
                        screenProperties.setX(x);
                        screenProperties.setY(y);
                        screenProperties.setWidth(width);
                        screenProperties.setHeight(height);
                        
                        // create items if service is also selected
                        if (supportService() && serviceClass != null && serviceClass.trim().length() > 0)
                        {
                            blockProperties.setServiceClassName(serviceClass, true);
                        }
                        
                        
                        ReportBlockAddOperation addOperation = new ReportBlockAddOperation(ReportDesignTreeSection.this, formProperties.getBlockContainer(), blockProperties); 

                        getEditor().execute(addOperation, new NullProgressMonitor());
                    }

                    public boolean hasBlock(String blockName)
                    {
                        return editor.getReportProperties().getBlockContainer().contains(blockName);
                    }

                    public IJavaProject getProject()
                    {
                        return editor.getJavaProject();
                    }

                    public boolean supportService()
                    {
                        return !controlBlock;
                    }

                });
                wizard.open();
            }

        };
    }
}
