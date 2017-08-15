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
package org.entirej.ide.ui.editors.form;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.editor.FormPage;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;

public class EJObjectGroupEditor extends AbstractEJFormEditor
{

    protected EJFormBasePage createFormPage()
    {
        return new EJFormBasePage(this)
        {
            protected FormDesignTreeSection createTreeSection(Composite body)
            {
                return new BlockDesignTreeSection(editor, this, body);
            }

            @Override
            protected String getPageHeader()
            {
                return "ObjectGroup Design";
            }

        };
    }

    @Override
    public String getActivePageID()
    {
        return EJFormBasePage.PAGE_ID;
    }

    @Override
    public void loadFile(IFile file)
    {
        IProject _project = file.getProject();
        synchronized (MODEL_LOCK)
        {
            project = JavaCore.create(_project);

            InputStream inStream = null;
            try
            {

                try
                {
                    inStream = file.getContents();

                }
                catch (CoreException e)
                {
                    file.refreshLocal(IResource.DEPTH_ZERO, new NullProgressMonitor());
                    inStream = file.getContents();
                }
                EntireJFormReader reader = new EntireJFormReader();
                String fileName = file.getName();
                fileName = fileName.substring(0, fileName.lastIndexOf("."));
                formProperties = reader.readForm(new FormHandler(new EJPluginObjectGroupProperties(fileName, project)), project,file, inStream);
                formProperties.initialisationCompleted();
            }
            catch (Exception exception)
            {
                EJCoreLog.logException(exception);
            }
            finally
            {

                try
                {
                    if (inStream != null)
                        inStream.close();
                }
                catch (IOException e)
                {
                    EJCoreLog.logException(e);
                }
            }
        }

    }
    
    @Override
    public void saveFile(IFile file, IProgressMonitor monitor) throws IOException
    {
        
        EJFormReferencePage.updateObjectGroupRef((EJPluginObjectGroupProperties) formProperties, this, monitor);
        super.saveFile(file, monitor);
        
       
    }

    private static class BlockDesignTreeSection extends FormDesignTreeSection
    {

        public BlockDesignTreeSection(AbstractEJFormEditor editor, FormPage page, Composite parent)
        {
            super(editor, page, parent);
        }

        @Override
        public String getSectionTitle()
        {
            return "ObjectGroup Setup";
        }

        @Override
        public String getSectionDescription()
        {

            return "Define design/settings of the ObjectGroup in the following section.";
        }

        @Override
        public AbstractNodeContentProvider getContentProvider()
        {
            return new AbstractNodeContentProvider()
            {

                public Object[] getElements(Object inputElement)
                {
                    // project build errors
                    if (editor.getFormProperties() == null)
                        return new Object[0];
                    return new Object[] { baseNode = new ObjGroupNode(editor.getFormProperties()), new BlockGroupNode(null,BlockDesignTreeSection.this)
                    {
                        @Override
                        public Action[] getActions()
                        {

                            return new Action[] { BlockDesignTreeSection.this.createNewRefBlockAction(false) };
                        }
                    }, new RelationsGroupNode(BlockDesignTreeSection.this), new LovGroupNode(BlockDesignTreeSection.this),
                            new CanvasGroupNode(BlockDesignTreeSection.this) };
                }
            };
        }

        @Override
        public void addToolbarCustomActions(ToolBarManager toolBarManager, ToolBar toolbar)
        {
            // ignore
        }

        @Override
        public Action[] getBaseActions()
        {

            return new Action[] { createNewRefBlockAction(false), null, createNewRefLovAction(), createNewLovAction() };
        }

        protected Action[] getNewBlockActions()
        {
            return new Action[] { /* ignore */};
        }

        private class ObjGroupNode extends AbstractNode<EJPluginFormProperties> implements NodeOverview
        {
            private final Image                 OBJGROUP  = EJUIImages.getImage(EJUIImages.DESC_OBJGROUP);
            private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                          {

                                                              public void refreshNode()
                                                              {
                                                                  refresh(ObjGroupNode.this);
                                                              }

                                                              @Override
                                                              public List<IMarker> getMarkers()
                                                              {
                                                                  List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                  IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
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

            private ObjGroupNode(EJPluginFormProperties source)
            {
                super(null, source);
            }

            public String getName()
            {
                String name = source.getName();
                if (name == null || name.length() == 0)
                    name = "<objectgroup>";

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

                return "Click <a href=\"http://docs.entirej.com/display/EJ1/ObjectGroup+Properties#ObjectGroupProperties\">here</a> for more information on ObjectGroup Properties. All mandatory properties are denoted by \"*\"";
            }

            public <S> S getAdapter(Class<S> adapter)
            {
                if (NodeValidateProvider.class.isAssignableFrom(adapter))
                {
                    return adapter.cast(validator);
                }
                if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                {
                    return adapter.cast(new FormCanvasPreviewImpl()
                    {
                        @Override
                        protected void setPreviewBackground(Control control, Color color)
                        {
                            // IGNORE
                        }

                        @Override
                        public String getDescription()
                        {
                            return "preview the defined layout in ObjectGroup.";
                        }

                        @Override
                        protected void createComponent(Composite parent, EJPluginCanvasProperties component)
                        {
                            if (component.getPluginBlockProperties() != null)
                            {
                                EJPluginMainScreenProperties mainScreenProperties = component.getPluginBlockProperties().getMainScreenProperties();

                                Composite layoutBody = new Composite(parent, SWT.NONE);

                                layoutBody.setLayout(new FillLayout());

                                layoutBody.setLayoutData(createGridData(component));
                                component.getPluginBlockProperties().getBlockRendererDefinition()
                                        .addBlockControlToCanvas(mainScreenProperties, component.getPluginBlockProperties(), layoutBody, editor.getToolkit())
                                        .addItemWidgetChosenListener(chosenListener);
                                
                                MouseAdapter mouseAdapter = new MouseAdapter()
                                {
                                    @Override
                                    public void mouseDoubleClick(MouseEvent e)
                                    {
                                        selectNodes(true, source);
                                    }
                                };
                                
                                
                                Control[] children = layoutBody.getChildren();
                                for (Control control : children)
                                {
                                    control.addMouseListener(mouseAdapter);
                                }

                            }
                            else
                            {
                                super.createComponent(parent, component);
                            }
                        }
                    });
                }
                return null;
            }

            @Override
            public Image getImage()
            {
                return OBJGROUP;
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
                        "If you are using more cryptic names for your ObjectGroup i.e. OG001, OG002 etc, then you may want to have a different name displayed in your project tree so you can find your ObjectGroup easier")
                {
                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

                    @Override
                    public void setValue(String value)
                    {
                        source.setFormDisplayName(value);
                        editor.setDirty(true);
                        refresh(ObjGroupNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return source.getFormDisplayName();
                    }

                };

                AbstractGroupDescriptor metadataGroupDescriptor = new AbstractGroupDescriptor("Metadata")
                {
                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

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
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

                    @Override
                    public String getTooltip()
                    {

                        return "The width (in pixels) of the ObjectGroup within it's container. If the width of the form is wider than the available space then a horizontal scroll bar will be shown ";
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
                            source.setFormWidth(Integer.parseInt(value));
                        }
                        catch (NumberFormatException e)
                        {
                            source.setFormWidth(0);
                            if (text != null)
                            {
                                text.setText(getValue());
                                text.selectAll();
                            }
                        }
                        editor.setDirty(true);
                        refresh(ObjGroupNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return String.valueOf(source.getFormWidth());
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
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

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

                        return "The height (in pixels) of the ObjectGroup within it's container. If the form height is higher than the available space then a vertical scroll bar will be shown";
                    }

                    @Override
                    public void setValue(String value)
                    {
                        try
                        {
                            source.setFormHeight(Integer.parseInt(value));
                        }
                        catch (NumberFormatException e)
                        {
                            source.setFormHeight(0);
                            if (text != null)
                            {
                                text.setText(getValue());
                                text.selectAll();
                            }
                        }
                        editor.setDirty(true);
                        refresh(ObjGroupNode.this);
                    }

                    @Override
                    public String getValue()
                    {
                        return String.valueOf(source.getFormHeight());
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

                final AbstractTextDescriptor colDescriptor = new AbstractTextDescriptor("Columns")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & FormNodeTag.COL) != 0;
                                       }
                                   };

                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

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
                        return "The amount of columns the ObjectGroup will use to layout it's contained canvases";
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
                        refresh(ObjGroupNode.this);
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

                AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
                {
                    @Override
                    public void runOperation(AbstractOperation operation)
                    {
                        editor.execute(operation);

                    }

                    @Override
                    public String getTooltip()
                    {
                        return "Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form\">here</a> For more information on laying out an EntireJ ObjectGroup";

                    }

                    public AbstractDescriptor<?>[] getDescriptors()
                    {
                        return new AbstractDescriptor<?>[] { widthDescriptor, heightDescriptor, colDescriptor };
                    }
                };

                return new AbstractDescriptor<?>[] { layoutGroupDescriptor, metadataGroupDescriptor };
            }

            public void addOverview(StyledString styledString)
            {
                if (source.getFormDisplayName() != null && source.getFormDisplayName().length() != 0)
                {
                    styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                    styledString.append(source.getFormDisplayName(), StyledString.DECORATIONS_STYLER);

                }
                if (source.getTitle() != null && source.getTitle().length() != 0)
                {
                    styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                    styledString.append(source.getTitle(), StyledString.COUNTER_STYLER);

                }

            }
        }

    }

}
