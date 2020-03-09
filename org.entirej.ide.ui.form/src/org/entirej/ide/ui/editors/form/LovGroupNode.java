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
import java.util.List;

import org.eclipse.core.commands.operations.AbstractOperation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.StyledString;
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.wizards.newresource.BasicNewResourceWizard;
import org.entirej.framework.core.actionprocessor.EJDefaultLovActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJLovActionProcessor;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.renderers.definitions.interfaces.EJLovRendererDefinition;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator;
import org.entirej.ide.ui.editors.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.descriptors.AbstractBooleanDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.DisplayItemGroupNode.MainDisplayItemGroup;
import org.entirej.ide.ui.editors.form.operations.LovAddOperation;
import org.entirej.ide.ui.editors.form.operations.LovRemoveOperation;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart.IExtensionValues;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.INodeDeleteProvider;
import org.entirej.ide.ui.nodes.INodeRenameProvider;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.nodes.dnd.NodeContext;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider;
import org.entirej.ide.ui.nodes.dnd.NodeMoveProvider.Neighbor;
import org.entirej.ide.ui.wizards.service.NewEJPojoServiceWizard;

public class LovGroupNode extends AbstractNode<EJPluginLovDefinitionContainer> implements NodeMoveProvider
{
    private final FormDesignTreeSection         treeSection;
    private final AbstractEJFormEditor          editor;
    private final static Image                  GROUP          = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
    private final static Image                  LOV            = EJUIImages.getImage(EJUIImages.DESC_LOV_DEF);
    private final static Image                  LOV_REF        = EJUIImages.getImage(EJUIImages.DESC_LOV_REF);
    private final EJDevItemWidgetChosenListener chosenListener = new EJDevItemWidgetChosenListener()
                                                               {

                                                                   public void fireRendererChosen(EJDevScreenItemDisplayProperties arg0)
                                                                   {
                                                                       if (arg0 != null && treeSection != null)
                                                                       {

                                                                           Object findNode = (arg0);
                                                                           if (findNode != null)
                                                                           {
                                                                               treeSection.selectNodes(true, findNode);
                                                                           }
                                                                       }

                                                                   }
                                                               };

    public LovGroupNode(FormDesignTreeSection treeSection)
    {
        super(null, treeSection.getEditor().getFormProperties().getLovDefinitionContainer());
        this.editor = treeSection.getEditor();
        this.treeSection = treeSection;
    }

    public String getName()
    {

        return "LOV Definitions";
    }

    @Override
    public String getToolTipText()
    {
        return "LOV definitions for blocks";
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
                    treeSection.refresh(LovGroupNode.this);
                }

                @Override
                public List<IMarker> getMarkers()
                {
                    List<IMarker> fmarkers = new ArrayList<IMarker>();

                    IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                    for (IMarker marker : markers)
                    {
                        int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                        if ((tag & FormNodeTag.GROUP) != 0 && (tag & FormNodeTag.LOV) != 0)
                        {
                            fmarkers.add(marker);
                        }
                    }

                    return fmarkers;
                }
            });
        }
        return super.getAdapter(adapter);
    }

    @Override
    public boolean isLeaf()
    {
        return source.isEmpty();
    }

    @Override
    public AbstractNode<?>[] getChildren()
    {
        List<AbstractNode<?>> nodes = new ArrayList<AbstractNode<?>>();
        List<EJPluginLovDefinitionProperties> allBlockProperties = source.getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties blockProperties : allBlockProperties)
        {
            nodes.add(new LovNode(this, blockProperties));
        }
        return nodes.toArray(new AbstractNode<?>[0]);
    }

    @Override
    public Action[] getActions()
    {

        return new Action[] { treeSection.createNewRefLovAction(), treeSection.createNewLovAction() };
    }

    @Override
    public AbstractDescriptor<?>[] getNodeDescriptors()
    {
        return new AbstractDescriptor<?>[] {};
    }

    class LovNode extends AbstractNode<EJPluginLovDefinitionProperties> implements Neighbor, Movable, NodeOverview
    {
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              treeSection.refresh(LovNode.this);
                                                          }

                                                          @Override
                                                          public List<IMarker> getMarkers()
                                                          {
                                                              List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                              IMarker[] markers = editor.getMarkers(EJMarkerFactory.MARKER_ID);
                                                              for (IMarker marker : markers)
                                                              {
                                                                  int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                  if ((tag & FormNodeTag.LOV) != 0 && source.getName() != null
                                                                          && source.getName().equals(marker.getAttribute(FormNodeTag.LOV_ID, null)))
                                                                  {

                                                                      fmarkers.add(marker);
                                                                  }
                                                              }

                                                              return fmarkers;
                                                          }
                                                      };

        public LovNode(AbstractNode<?> parent, EJPluginLovDefinitionProperties source)
        {
            super(parent, source);

        }

        @Override
        public String getName()
        {
            return source.getName();
        }

        @Override
        public String getNodeDescriptorDetails()
        {
            if (source.isReferenceBlock())
            {
                return "LOV Definitions are edited in their own editor. Click the Referenced LOV label to open the LOV in the LOV Definition editor. Click <a href=\"http://docs.entirej.com/display/EJ1/Working+with+LOV%27s\">here</a> for more information on LOV's";
            }
            return super.getNodeDescriptorDetails();
        }

        @Override
        public Action[] getActions()
        {

            return LovGroupNode.this.getActions();
        }

        public void addOverview(StyledString styledString)
        {

            if (source.isImportFromObjectGroup())
            {
                styledString.append(" [ ", StyledString.DECORATIONS_STYLER);
                styledString.append(source.getReferencedObjectGroupName(), StyledString.DECORATIONS_STYLER);
                styledString.append(" ] ", StyledString.DECORATIONS_STYLER);
            }
            if (source.isReferenceBlock() && source.getReferencedLovDefinitionName() != null && source.getReferencedLovDefinitionName().length() != 0)
            {
                styledString.append(" [ ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getReferencedLovDefinitionName(), StyledString.QUALIFIER_STYLER);
                // if
                // (!source.getFormProperties().getEntireJProperties().containsReusableLovDefinitionProperties(source.getReferencedLovDefinitionName()))
                // {
                // styledString.append(" < missing! >",
                // StyledString.QUALIFIER_STYLER);
                // }
                styledString.append(" ] ", StyledString.QUALIFIER_STYLER);
            }

            if (source.getLovRendererName() != null && source.getLovRendererName().length() != 0)
            {
                styledString.append(" : ", StyledString.QUALIFIER_STYLER);
                styledString.append(source.getLovRendererName(), StyledString.DECORATIONS_STYLER);

            }

        }

        @Override
        public Image getImage()
        {
            if (source.isReferenceBlock())
                return LOV_REF;
            return LOV;
        }

        public boolean canMove()
        {
            return true;
        }

        public Object getNeighborSource()
        {
            return source;
        }

        @Override
        public boolean isLeaf()
        {
            return false;
        }

        public <S> S getAdapter(Class<S> adapter)
        {
            if (NodeValidateProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(validator);
            }

            if (IFormPreviewProvider.class.isAssignableFrom(adapter))
            {
                return adapter.cast(new IFormPreviewProvider()

                {

                    public String getDescription()
                    {
                        return "preview the defined layout in LOV screen.";
                    }

                    public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                    {
                        Composite pContent = new Composite(previewComposite, SWT.NONE);

                        EJPluginMainScreenProperties mainScreenProperties = source.getBlockProperties().getMainScreenProperties();
                        int width = mainScreenProperties.getWidth();
                        int height = mainScreenProperties.getWidth();
                        previewComposite.setContent(pContent);
                        previewComposite.setExpandHorizontal(true);
                        previewComposite.setExpandVertical(true);

                        pContent.setLayout(new GridLayout());

                        Composite layoutBody = new Composite(pContent, SWT.NONE);
                        layoutBody.setLayout(new GridLayout(mainScreenProperties.getNumCols(), false));

                        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);

                        sectionData.widthHint = width;
                        sectionData.heightHint = height;
                        layoutBody.setLayoutData(sectionData);
                        EJDevLovRendererDefinition rendererDefinition = source.getRendererDefinition();
                        if (rendererDefinition != null)
                            rendererDefinition.addLovControlToCanvas(source, layoutBody, editor.getToolkit()).addItemWidgetChosenListener(chosenListener);

                        if (width > 0 && height > 0)
                            previewComposite.setMinSize(width, height);
                        else
                            previewComposite.setMinSize(pContent.computeSize(SWT.DEFAULT, SWT.DEFAULT));

                    }

                    public void dispose()
                    {

                    }
                });
            }
            return null;
        }

        @Override
        public AbstractNode<?>[] getChildren()
        {
            List<AbstractNode<?>> list = new ArrayList<AbstractNode<?>>();
            if (source.isReferenceBlock())
                source.getBlockProperties().setIsReferenced(true);// mark block
                                                                  // referenced
                                                                  // if lov is
                                                                  // referenced
            list.add(new BlockItemsGroupNode(treeSection, this, source.getBlockProperties().getItemContainer())
            {

                @Override
                public String getNodeDescriptorDetails()
                {
                    if (LovNode.this.source.isReferenceBlock())
                    {
                        return "Item properties are editable within the LOV Definition Editor however it is possible to add default query and insert values to the lov definition items. Any default query value entered here will be used in the LOV Definition query, making it a good place to restrict LOV values for your form";
                    }
                    return super.getNodeDescriptorDetails();
                }

            });
            //
            if (!source.isReferenceBlock())
            {
                MainDisplayItemGroup mainItemGroup = new DisplayItemGroupNode.MainDisplayItemGroup("Main Screen", source.getBlockProperties()
                        .getMainScreenProperties(), source.getBlockProperties().getMainScreenItemGroupDisplayContainer())
                {
                    private AbstractMarkerNodeValidator svalidator = new AbstractMarkerNodeValidator()
                                                                   {

                                                                       public void refreshNode()
                                                                       {
                                                                           treeSection.refresh(LovNode.this);
                                                                       }

                                                                       @Override
                                                                       public List<IMarker> getMarkers()
                                                                       {
                                                                           List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                           List<IMarker> markers = validator.getMarkers();
                                                                           for (IMarker marker : markers)
                                                                           {
                                                                               int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG, FormNodeTag.NONE);
                                                                               if ((tag & FormNodeTag.MAIN) != 0)
                                                                               {

                                                                                   fmarkers.add(marker);
                                                                               }
                                                                           }

                                                                           return fmarkers;
                                                                       }
                                                                   };

                    public <S> S getAdapter(Class<S> adapter)
                    {
                        if (NodeValidateProvider.class.isAssignableFrom(adapter))
                        {
                            return adapter.cast(svalidator);
                        }
                        if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                            return LovNode.this.getAdapter(adapter);

                        return super.getAdapter(adapter);

                    }
                };
                list.add(new DisplayItemGroupNode(treeSection, this, mainItemGroup)
                {

                });

                //
                if (source.isUserQueryAllowed())
                {

                    DisplayItemGroupNode.ExtensionDisplayItemGroup itemGroup = new DisplayItemGroupNode.ExtensionDisplayItemGroup("Query Screen", source
                            .getBlockProperties().getQueryScreenRendererProperties(), source.getBlockProperties().getQueryScreenItemGroupDisplayContainer())
                    {

                        @Override
                        public EJPropertyDefinitionGroup getDefinitionGroup()
                        {
                            EJDevQueryScreenRendererDefinition definition = source.getRendererDefinition().getQueryScreenRendererDefinition();
                            return definition != null ? definition.getQueryScreenPropertyDefinitionGroup() : null;
                        }

                        @Override
                        public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                EJPropertyDefinition propertyDefinition)
                        {
                            source.getRendererDefinition().getQueryScreenRendererDefinition()
                                    .loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                        }

                        private AbstractMarkerNodeValidator svalidator = new AbstractMarkerNodeValidator()
                                                                       {

                                                                           public void refreshNode()
                                                                           {
                                                                               treeSection.refresh(LovNode.this);
                                                                           }

                                                                           @Override
                                                                           public List<IMarker> getMarkers()
                                                                           {
                                                                               List<IMarker> fmarkers = new ArrayList<IMarker>();

                                                                               List<IMarker> markers = validator.getMarkers();
                                                                               for (IMarker marker : markers)
                                                                               {
                                                                                   int tag = marker.getAttribute(NodeValidateProvider.NODE_TAG,
                                                                                           FormNodeTag.NONE);
                                                                                   if ((tag & FormNodeTag.QUERY) != 0)
                                                                                   {

                                                                                       fmarkers.add(marker);
                                                                                   }
                                                                               }

                                                                               return fmarkers;
                                                                           }
                                                                       };

                        public <S> S getAdapter(Class<S> adapter)
                        {
                            if (NodeValidateProvider.class.isAssignableFrom(adapter))
                            {
                                return adapter.cast(svalidator);
                            }
                            if (IFormPreviewProvider.class.isAssignableFrom(adapter))
                            {
                                return adapter.cast(new IFormPreviewProvider()

                                {

                                    public String getDescription()
                                    {
                                        return "preview the defined layout in block query screen.";
                                    }

                                    public void buildPreview(AbstractEJFormEditor editor, ScrolledComposite previewComposite)
                                    {
                                        source.getRendererDefinition().getQueryScreenRendererDefinition()
                                                .addQueryScreenControl(source.getBlockProperties(), previewComposite, editor.getToolkit())
                                                .addItemWidgetChosenListener(chosenListener);
                                        Control[] children = previewComposite.getChildren();
                                        if (children.length > 0)
                                        {
                                            previewComposite.setContent(previewComposite.getChildren()[0]);
                                            previewComposite.setExpandHorizontal(true);
                                            previewComposite.setExpandVertical(true);
                                        }

                                    }

                                    public void dispose()
                                    {

                                    }
                                });
                            }
                            return null;
                        }
                    };
                    list.add(new DisplayItemGroupNode(treeSection, this, itemGroup));
                }
            }

            return list.toArray(new AbstractNode[0]);
        }

        @Override
        public INodeDeleteProvider getDeleteProvider()
        {

            if (!supportLovDelete() || source.isImportFromObjectGroup())
                return null;
            return new INodeDeleteProvider()
            {

                public void delete(boolean cleanup)
                {

                    LovGroupNode.this.source.removeLovDefinitionProperties(source);
                    editor.setDirty(true);
                    treeSection.refresh(LovGroupNode.this);

                }

                public AbstractOperation deleteOperation(boolean cleanup)
                {
                    return new LovRemoveOperation(treeSection, LovGroupNode.this.source, source);
                }
            };
        }

        @Override
        public INodeRenameProvider getRenameProvider()
        {
            if (!supportLovRename() || source.isImportFromObjectGroup())
                return null;

            return new INodeRenameProvider()
            {

                private void renameDefinitionOnLovMapping(String oldName, String newName)
                {
                    List<EJPluginBlockProperties> allBlockProperties = editor.getFormProperties().getBlockContainer().getAllBlockProperties();
                    for (EJPluginBlockProperties blockProperties : allBlockProperties)
                    {
                        List<EJPluginLovMappingProperties> lovMappingProperties = blockProperties.getLovMappingContainer().getAllLovMappingProperties();
                        for (EJPluginLovMappingProperties mapping : lovMappingProperties)
                        {
                            if (mapping.getLovDefinitionName() != null && mapping.getLovDefinitionName().equals(oldName))
                            {
                                mapping.setLovDefinitionName(newName);
                            }
                        }

                    }

                }

                public void rename()
                {
                    InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Rename LOV [%s]", source.getName()), "LOV Name",
                            source.getName(), new IInputValidator()
                            {

                                public String isValid(String newText)
                                {
                                    if (newText == null || newText.trim().length() == 0)
                                        return "LOV name can't be empty.";
                                    if (source.getName().equals(newText.trim()))
                                        return "";
                                    if (source.getName().equalsIgnoreCase(newText.trim()))
                                        return null;
                                    if (LovGroupNode.this.source.contains(newText.trim()))
                                        return "LOV with this name already exists.";
                                    return null;
                                }
                            });
                    if (dlg.open() == Window.OK)
                    {
                        String oldName = source.getName();
                        String newName = dlg.getValue().trim();
                        source.internalSetName(newName);

                        renameDefinitionOnLovMapping(oldName, newName);
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

            if (source.isImportFromObjectGroup())
            {
                return new AbstractDescriptor<?>[] { new AbstractTextDescriptor("Referenced ObjectGroup")
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
                                .getObjectGroupProperties(source.getReferencedObjectGroupName());
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
                        return source.getReferencedObjectGroupName();
                    }

                    Text text;

                    @Override
                    public void addEditorAssist(Control control)
                    {

                        text = (Text) control;
                        text.setEditable(false);
                    }
                } };
            }
            final List<IMarker> fmarkers = validator.getMarkers();
            if (source.isReferenceBlock())
            {
                final AbstractTextDescriptor referencedDescriptor = new AbstractTextDescriptor("Referenced LOV")
                {
                    Filter vfilter = new Filter()
                                   {

                                       public boolean match(int tag, IMarker marker)
                                       {

                                           return (tag & FormNodeTag.REF) != 0;
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
                    public String getWarnings()
                    {
                        return validator.getWarningMarkerMsg(fmarkers, vfilter);
                    }

                    public boolean hasLableLink()
                    {
                        return true;
                    }

                    @Override
                    public String lableLinkActivator()
                    {

                        try
                        {
                            IFile file = editor.getFormProperties().getEntireJProperties().getReusableLovFile(getValue());
                            if (file != null)
                            {
                                BasicNewResourceWizard.selectAndReveal(file, EJUIPlugin.getActiveWorkbenchWindow());
                                final IWorkbenchPage activePage = EJUIPlugin.getActiveWorkbenchWindow().getActivePage();
                                if (activePage != null)
                                {
                                    IDE.openEditor(activePage, file, true);
                                }
                            }
                        }
                        catch (EJDevFrameworkException e)
                        {
                            EJCoreLog.logException(e);
                        }
                        catch (PartInitException e)
                        {
                            EJCoreLog.logException(e);
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
                        return source.getReferencedLovDefinitionName();
                    }

                    Text text;

                    @Override
                    public void addEditorAssist(Control control)
                    {

                        text = (Text) control;
                        text.setEditable(false);
                    }
                };

                return new AbstractDescriptor<?>[] { referencedDescriptor };
            }

            List<AbstractDescriptor<?>> descriptors = new ArrayList<AbstractDescriptor<?>>();
            final List<AbstractDescriptor<?>> dataDescriptors = new ArrayList<AbstractDescriptor<?>>();

            AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Renderer",
                    "block renderer defined in application renderers.")
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
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                @Override
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }

                public String[] getOptions()
                {
                    List<String> options = new ArrayList<String>();
                    EJPluginAssignedRendererContainer rendererContainer = source.getEntireJProperties().getLovRendererContainer();
                    Collection<EJPluginRenderer> allRenderers = rendererContainer.getAllRenderers();
                    for (EJPluginRenderer renderer : allRenderers)
                    {
                        options.add(renderer.getAssignedName());
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
                    source.setLovRendererName(value, true);

                    editor.setDirty(true);
                    treeSection.refresh(LovNode.this);
                    if (treeSection.getDescriptorViewer() != null)
                        treeSection.getDescriptorViewer().showDetails(LovNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getLovRendererName();
                }
            };

            descriptors.add(rendererDescriptor);

            AbstractTypeDescriptor serivceDescriptor = new AbstractTypeDescriptor(editor, "Block Service")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.SERVICE) != 0;
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
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }

                @Override
                public void setValue(String value)
                {
                    if (source.getBlockProperties() == null)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();
                        final EJPluginBlockProperties blockProperties = new EJPluginBlockProperties(formProperties, null, false);
                        source.setBlockProperties(blockProperties);
                    }
                    source.getBlockProperties().setServiceClassName(value, true);
                    editor.setDirty(true);
                    treeSection.refresh(LovNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getBlockProperties() != null ? source.getBlockProperties().getServiceClassName() : null;
                }

                @Override
                public String lableLinkActivator()
                {
                    if (getValue() == null || getValue().trim().length() == 0)
                    {
                        NewEJPojoServiceWizard wizard = new NewEJPojoServiceWizard();
                        wizard.setServiceOptional(false);
                        wizard.init(EJUIPlugin.getDefault().getWorkbench(), new StructuredSelection(editor.getFile()));
                        EJUIPlugin.getDefault();
                        WizardDialog dialog = new WizardDialog(EJUIPlugin.getActiveWorkbenchShell(), wizard);
                        dialog.open();
                        String typeName = wizard.getServiceTypeName();
                        return typeName != null ? typeName : "";
                    }
                    return super.lableLinkActivator();
                }

            };
            serivceDescriptor.setBaseClass(EJBlockService.class.getName());
            descriptors.add(serivceDescriptor);
            AbstractTypeDescriptor actionDescriptor = new AbstractTypeDescriptor(editor, "Action Processor")
            {
                Filter vfilter = new Filter()
                               {

                                   public boolean match(int tag, IMarker marker)
                                   {

                                       return (tag & FormNodeTag.ACTION_PROCESSOR) != 0;
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
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }

                @Override
                public void setValue(String value)
                {
                    source.setActionProcessorClassName(value);
                    editor.setDirty(true);
                    treeSection.refresh(LovNode.this);

                }

                @Override
                public String getValue()
                {
                    return source.getActionProcessorClassName();
                }
            };
            actionDescriptor.setBaseClass(EJLovActionProcessor.class.getName());
            actionDescriptor.setDefaultClass(EJDefaultLovActionProcessor.class.getName());
            descriptors.add(actionDescriptor);

            final AbstractTextDescriptor maxResultsDescriptor = new AbstractTextDescriptor("Max Results")
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
                        source.getBlockProperties().setMaxResults(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.getBlockProperties().setMaxResults(0);

                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    treeSection.refresh(LovNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getBlockProperties().getMaxResults());
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
            final AbstractTextDescriptor pageSizeDescriptor = new AbstractTextDescriptor("Page Size")
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
                        source.getBlockProperties().setPageSize(Integer.parseInt(value));
                    }
                    catch (NumberFormatException e)
                    {
                        source.getBlockProperties().setPageSize(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    treeSection.refresh(LovNode.this);
                }

                @Override
                public String getValue()
                {
                    return String.valueOf(source.getBlockProperties().getPageSize());
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

            dataDescriptors.add(maxResultsDescriptor);
            dataDescriptors.add(pageSizeDescriptor);

            AbstractGroupDescriptor dataGroupDescriptor = new AbstractGroupDescriptor("Data Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return dataDescriptors.toArray(new AbstractDescriptor<?>[0]);
                }
            };
            descriptors.add(dataGroupDescriptor);

            final AbstractTextDescriptor widthDescriptor = new AbstractTextDescriptor("Width")
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
                    treeSection.refresh(LovNode.this);
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

            
            
            final AbstractBooleanDescriptor automaticRefresh = new AbstractBooleanDescriptor("Automatic Refresh")
            {
                
                @Override
                public void setValue(Boolean value)
                {
                    source.setAutomaticRefresh(value);
                    editor.setDirty(true);
                }
                
                @Override
                public Boolean getValue()
                {
                    return source.refreshAutomatically();
                }
                
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }
            };
            
            
            final AbstractTextDescriptor heightDescriptor = new AbstractTextDescriptor("Height")
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
                        source.setHeight(0);
                        if (text != null)
                        {
                            text.setText(getValue());
                            text.selectAll();
                        }
                    }
                    editor.setDirty(true);
                    treeSection.refresh(LovNode.this);
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

            descriptors.add(automaticRefresh);
            AbstractGroupDescriptor layoutGroupDescriptor = new AbstractGroupDescriptor("Layout Settings")
            {
                @Override
                public void runOperation(AbstractOperation operation)
                {
                    editor.execute(operation);

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { widthDescriptor, heightDescriptor };
                }
            };
            descriptors.add(layoutGroupDescriptor);

            // try to load renderer group
            final EJFrameworkExtensionProperties rendereProperties = source.getLovRendererProperties();

            if (rendereProperties != null)
            {

                final EJDevLovRendererDefinition formRendererDefinition = ExtensionsPropertiesFactory.loadLovRendererDefinition(source.getEntireJProperties(),
                        source.getLovRendererName());
                if (formRendererDefinition != null)
                {
                    final EJPropertyDefinitionGroup definitionGroup = formRendererDefinition.getLovPropertyDefinitionGroup();
                    if (definitionGroup != null)
                    {

                        AbstractGroupDescriptor rendererGroupDescriptor = new AbstractGroupDescriptor("Renderer Settings")
                        {
                            @Override
                            public void runOperation(AbstractOperation operation)
                            {
                                editor.execute(operation);

                            }

                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                                return PropertyDefinitionGroupPart.createGroupDescriptors(editor, source.getEntireJProperties(), definitionGroup,
                                        rendereProperties, new IExtensionValues()
                                        {

                                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                                    EJPropertyDefinition propertyDefinition)
                                            {
                                                propertyDefinition.clearValidValues();
                                                EJLovRendererDefinition rendererDef = ExtensionsPropertiesFactory.loadLovRendererDefinition(
                                                        source.getEntireJProperties(), source.getLovRendererName());
                                                rendererDef.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);

                                            }

                                            public EJPluginBlockProperties getBlockProperties()
                                            {
                                                return source.getBlockProperties();
                                            }
                                        });
                            }
                        };
                        descriptors.add(rendererGroupDescriptor);

                    }
                }
            }
            return descriptors.toArray(new AbstractDescriptor<?>[0]);

        }
    }

    public boolean canMove(Neighbor relation, Object source)
    {
        return source instanceof EJPluginLovDefinitionProperties && !((EJPluginLovDefinitionProperties) source).isImportFromObjectGroup();
    }

    public void move(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginLovDefinitionProperties> items = source.getAllLovDefinitionProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                source.addLovDefinitionProperties(index, (EJPluginLovDefinitionProperties) dSource);
            }
        }
        else
            source.addLovDefinitionProperties((EJPluginLovDefinitionProperties) dSource);

    }

    public AbstractOperation moveOperation(NodeContext context, Neighbor neighbor, Object dSource, boolean before)
    {
        if (neighbor != null)
        {
            Object methodNeighbor = neighbor.getNeighborSource();
            List<EJPluginLovDefinitionProperties> items = source.getAllLovDefinitionProperties();
            if (items.contains(methodNeighbor))
            {
                int index = items.indexOf(methodNeighbor);
                if (!before)
                    index++;

                return new LovAddOperation(treeSection, source, (EJPluginLovDefinitionProperties) dSource, index);
            }
        }
        return new LovAddOperation(treeSection, source, (EJPluginLovDefinitionProperties) dSource, -1);
    }

    protected boolean supportLovDelete()
    {
        return true;
    }

    protected boolean supportLovRename()
    {
        return true;
    }
}
