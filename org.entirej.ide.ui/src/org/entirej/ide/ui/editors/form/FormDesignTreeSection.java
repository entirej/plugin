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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.IInputValidator;
import org.eclipse.jface.dialogs.InputDialog;
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
import org.eclipse.jface.window.Window;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.FillLayout;
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
import org.entirej.framework.core.actionprocessor.EJDefaultFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJFormActionProcessor;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.renderers.definitions.interfaces.EJFormRendererDefinition;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevFormRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJPropertiesLoader;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.framework.properties.EJPluginReusableBlockProperties;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginCanvasContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovDefinitionContainer;
import org.entirej.framework.plugin.framework.properties.writer.FormPropertiesWriter;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.framework.plugin.utils.EJPluginEntireJNumberVerifier;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractGroupDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTextDropDownDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractTypeDescriptor;
import org.entirej.ide.ui.editors.form.AbstractMarkerNodeValidator.Filter;
import org.entirej.ide.ui.editors.form.wizards.DataBlockServiceWizard;
import org.entirej.ide.ui.editors.form.wizards.DataBlockWizardContext;
import org.entirej.ide.ui.editors.form.wizards.MirrorBlockWizard;
import org.entirej.ide.ui.editors.form.wizards.MirrorBlockWizardContext;
import org.entirej.ide.ui.editors.form.wizards.RefBlockWizard;
import org.entirej.ide.ui.editors.form.wizards.RefBlockWizardContext;
import org.entirej.ide.ui.editors.form.wizards.RefLovWizard;
import org.entirej.ide.ui.editors.form.wizards.RefLovWizardContext;
import org.entirej.ide.ui.editors.form.wizards.RelationWizard;
import org.entirej.ide.ui.editors.form.wizards.RelationWizardContext;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart;
import org.entirej.ide.ui.editors.prop.PropertyDefinitionGroupPart.IExtensionValues;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.AbstractNodeContentProvider;
import org.entirej.ide.ui.nodes.AbstractNodeTreeSection;
import org.entirej.ide.ui.nodes.AbstractSubActions;
import org.entirej.ide.ui.nodes.INodeDescriptorViewer;
import org.entirej.ide.ui.nodes.NodeOverview;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.table.TableViewerColumnFactory;
import org.entirej.ide.ui.wizards.form.NewEntireJRefLovWizard;

public class FormDesignTreeSection extends AbstractNodeTreeSection
{
    protected final AbstractEJFormEditor        editor;

    protected FormPreviewer                     formPreviewer;

    protected AbstractNode<?>                   baseNode;
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

    public FormDesignTreeSection(AbstractEJFormEditor editor, FormPage page, Composite parent)
    {
        super(editor, page, parent);
        this.editor = editor;
        initTree();
        addDnDSupport(null);// no root move need in layout
    }

    public AbstractEJFormEditor getEditor()
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
        // use form node as default node to editor
        if (node == null)
            node = baseNode;

        super.showNodeDetails(node);
        if (formPreviewer != null)
            formPreviewer.showDetails(node);
    }

    public void refreshPreview()
    {
        if (formPreviewer != null)
            Display.getDefault().asyncExec(new Runnable()
            {

                public void run()
                {
                    formPreviewer.refresh();

                }
            });
    }

    public void setFormPreviewer(FormPreviewer formPreviewer)
    {
        this.formPreviewer = formPreviewer;
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
        return "Form Setup";
    }

    @Override
    public String getSectionDescription()
    {

        return "Define design/settings of the form in the following section.";
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
        Action addAction = new Action("Add Block...", IAction.AS_DROP_DOWN_MENU)
        {

            @Override
            public void runWithEvent(Event event)
            {

                Rectangle rect = event.getBounds();
                Point pt;
                if (event.detail != SWT.ARROW)
                    pt = new Point(rect.x, rect.y + toolbar.getBounds().height);
                else
                    pt = new Point(rect.x, rect.y + rect.height);
                pt = toolbar.toDisplay(pt);
                menuBlock.setLocation(pt.x, pt.y);
                menuBlock.setVisible(true);
            }

        };
        addAction.setImageDescriptor(EJUIImages.DESC_BLOCK);
        toolBarManager.add(addAction);

        MenuManager popupCanvasMenuManager = new MenuManager();
        IMenuListener listenerCanvas = new IMenuListener()
        {
            public void menuAboutToShow(IMenuManager mng)
            {
                EJPluginCanvasContainer canvasContainer = FormDesignTreeSection.this.getEditor().getFormProperties().getCanvasContainer();
                AbstractNode<?> findNode = FormDesignTreeSection.this.findNode(canvasContainer);
                AbstractSubActions canvasAction = CanvasGroupNode.createNewCanvasAction(FormDesignTreeSection.this, findNode, canvasContainer, true);
                addMenuActions(mng, canvasAction.getActions());
                // if empty, add dummy action item show no actions available
                if (mng.getItems().length == 0)
                {
                    mng.add(new Action("no actions available")
                    {
                    });
                }
            }
        };
        popupCanvasMenuManager.addMenuListener(listenerCanvas);
        popupCanvasMenuManager.setRemoveAllWhenShown(true);
        final Menu menuCanvas = popupCanvasMenuManager.createContextMenu(toolbar);
        Action addCanvasAction = new Action("Add Canvas...", IAction.AS_DROP_DOWN_MENU)
        {

            @Override
            public void runWithEvent(Event event)
            {

                Rectangle rect = event.getBounds();
                Point pt;
                if (event.detail != SWT.ARROW)
                    pt = new Point(rect.x, rect.y + toolbar.getBounds().height);
                else
                    pt = new Point(rect.x, rect.y + rect.height);
                pt = toolbar.toDisplay(pt);
                menuCanvas.setLocation(pt.x, pt.y);
                menuCanvas.setVisible(true);
            }

        };
        addCanvasAction.setImageDescriptor(EJUIImages.DESC_LAYOUT_COMP);
        toolBarManager.add(addCanvasAction);
        toolBarManager.add(new Separator());
    }

    @Override
    public Action[] getBaseActions()
    {

        return new Action[] { createNewBlockAction(false), createNewBlockAction(true), createNewMirrorBlockAction(null), createNewRefBlockAction(true), null,
                createNewRelationAction(), null, createNewRefLovAction(), createNewLovAction() };
    }

    protected Action[] getNewBlockActions()
    {
        return new Action[] { createNewBlockAction(false), createNewBlockAction(true), createNewMirrorBlockAction(null), createNewRefBlockAction(true) };
    }

    public Action createNewBlockAction(final boolean controlBlock)
    {

        return new Action(controlBlock ? "New Control Block" : "New Service Block")
        {

            @Override
            public void runWithEvent(Event event)
            {
                DataBlockServiceWizard wizard = new DataBlockServiceWizard(new DataBlockWizardContext()
                {

                    public void addBlock(String blockName, EJPluginRenderer block, String canvas, boolean createCanvas, String serviceClass)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();
                        final EJPluginBlockProperties blockProperties = new EJPluginBlockProperties(formProperties, blockName, controlBlock);

                        if (createCanvas)
                        {
                            EJPluginCanvasContainer container = formProperties.getCanvasContainer();
                            EJCanvasProperties canvasProp = new EJPluginCanvasProperties(formProperties, canvas);
                            container.addCanvasProperties(canvasProp);
                        }

                        formProperties.getBlockContainer().addBlockProperties(blockProperties);
                        blockProperties.setCanvasName(canvas);
                        blockProperties.setBlockRendererName(block.getAssignedName(), true);
                        // create items if service is also selected
                        if (supportService() && serviceClass != null && serviceClass.trim().length() > 0)
                        {
                            blockProperties.setServiceClassName(serviceClass, true);
                        }
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                refresh(findNode(formProperties.getBlockContainer()), true);
                                refresh(findNode(formProperties.getCanvasContainer()));
                                selectNodes(true, findNode(blockProperties));

                            }
                        });

                    }

                    public List<EJPluginRenderer> getBlockRenderer()
                    {
                        Collection<EJPluginRenderer> allRenderers = editor.getFormProperties().getEntireJProperties().getBlockRendererContainer()
                                .getAllRenderers();
                        return new ArrayList<EJPluginRenderer>(allRenderers);
                    }

                    public List<EJCanvasProperties> getCanvas()
                    {
                        Collection<EJCanvasProperties> canvasCollection = EJPluginCanvasRetriever.retriveAllNonAssignedBlockCanvases(editor.getFormProperties());
                        return new ArrayList<EJCanvasProperties>(canvasCollection);
                    }

                    public boolean hasBlock(String blockName)
                    {
                        return editor.getFormProperties().getBlockContainer().contains(blockName);
                    }

                    public boolean hasCanvas(String canvasName)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();
                        return EJPluginCanvasRetriever.canvasExists(formProperties, canvasName);
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

    public Action createNewMirrorBlockAction(final String defaultBlock)
    {

        return new Action("New Mirror Block")
        {

            @Override
            public void runWithEvent(Event event)
            {
                MirrorBlockWizard wizard = new MirrorBlockWizard(new MirrorBlockWizardContext()
                {

                    public String getDefault()
                    {
                        return defaultBlock;
                    }

                    public void addBlock(String blockName, EJPluginRenderer block, String canvas, boolean createCanvas, String parentBlock)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();
                        EJPluginBlockProperties parent = editor.getFormProperties().getBlockContainer().getBlockProperties(parentBlock);
                        parent.setIsMirroredBlock(true);
                        final EJPluginBlockProperties blockProperties = parent.makeCopy(blockName, true);

                        if (createCanvas)
                        {
                            EJPluginCanvasContainer container = formProperties.getCanvasContainer();
                            EJCanvasProperties canvasProp = new EJPluginCanvasProperties(formProperties, canvas);
                            container.addCanvasProperties(canvasProp);
                        }

                        formProperties.getBlockContainer().addBlockProperties(blockProperties);
                        blockProperties.setCanvasName(canvas);
                        blockProperties.setBlockRendererName(block.getAssignedName(), true);

                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                refresh(findNode(formProperties.getBlockContainer()), true);
                                refresh(findNode(formProperties.getCanvasContainer()));
                                selectNodes(true, findNode(blockProperties));

                            }
                        });

                    }

                    public List<EJPluginRenderer> getBlockRenderer()
                    {
                        Collection<EJPluginRenderer> allRenderers = editor.getFormProperties().getEntireJProperties().getBlockRendererContainer()
                                .getAllRenderers();
                        return new ArrayList<EJPluginRenderer>(allRenderers);
                    }

                    public List<EJCanvasProperties> getCanvas()
                    {
                        Collection<EJCanvasProperties> canvasCollection = EJPluginCanvasRetriever.retriveAllNonAssignedBlockCanvases(editor.getFormProperties());
                        return new ArrayList<EJCanvasProperties>(canvasCollection);
                    }

                    public boolean hasBlock(String blockName)
                    {
                        return editor.getFormProperties().getBlockContainer().contains(blockName);
                    }

                    public boolean hasCanvas(String canvasName)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();
                        return EJPluginCanvasRetriever.canvasExists(formProperties, canvasName);
                    }

                    public IJavaProject getProject()
                    {
                        return editor.getJavaProject();
                    }

                    public List<String> getBlockNames()
                    {
                        List<String> names = new ArrayList<String>();
                        for (EJPluginBlockProperties blockProperties : editor.getFormProperties().getBlockContainer().getAllBlockProperties())
                        {
                            if (!blockProperties.isMirrorChild())
                            {
                                names.add(blockProperties.getName());
                            }
                        }
                        return names;
                    }

                });
                wizard.open();
            }

        };
    }

    public Action createGenerateRefBlockAction(final EJPluginBlockProperties properties)
    {
        return new Action("Generate Referenced Block")
        {
            String P_FORM_HEIGHT = "FORM_HEIGHT";
            String P_FORM_WIDTH  = "FORM_WIDTH";

            @Override
            public void runWithEvent(Event event)
            {

                final EJPluginFormProperties lformProperties = editor.getFormProperties();
                final List<String> reusableBlockNames;
                if (lformProperties.getEntireJProperties().getReusableBlocksLocation() != null
                        && lformProperties.getEntireJProperties().getReusableBlocksLocation().trim().length() > 0)
                {

                    reusableBlockNames = lformProperties.getEntireJProperties().getReusableBlockNames();
                }
                else
                    reusableBlockNames = null;

                InputDialog dlg = new InputDialog(EJUIPlugin.getActiveWorkbenchShell(), String.format("Generate Referenced Block using [%s]",
                        properties.getName()), "Block Name", properties.getName(), new IInputValidator()
                {

                    public String isValid(String newText)
                    {
                        if (reusableBlockNames == null)
                        {
                            return "No Referenced Block Location is specified then it will not be possible to generate Reusable Blocks.";
                        }
                        if (newText == null || newText.trim().length() == 0)
                            return "Referenced Block name can't be empty.";
                        if (reusableBlockNames.contains(newText.trim()))
                            return "Referenced Block with this name already exists.";
                        return null;
                    }
                });
                if (dlg.open() == Window.OK)
                {
                    IJavaElement element;
                    try
                    {
                        IJavaProject project = editor.getJavaProject();
                        element = editor.getJavaProject().findElement(new Path(lformProperties.getEntireJProperties().getReusableBlocksLocation()));

                        if (element == null)
                        {
                            return;
                        }
                        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                        IFolder folder = root.getFolder(element.getPath());
                        if (!folder.exists())
                        {
                            folder.create(false, true, null);
                        }

                        final IFile formFile = folder.getFile(new Path(dlg.getValue() + "." + EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX));

                        EJPluginFormProperties formProperties = new EJPluginFormProperties(dlg.getValue(), project);

                        int height = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_HEIGHT);
                        int width = EJPropertyRetriever.getIntValue(project.getProject(), P_FORM_WIDTH);

                        formProperties.setFormHeight(height);
                        formProperties.setFormWidth(width);
                        formProperties.setNumCols(1);

                        String oblockName = properties.getName();
                        properties.internalSetName(dlg.getValue());
                        formProperties.getBlockContainer().addBlockProperties(properties);

                        // read lov usage in mapping
                        EJPluginLovDefinitionContainer lovDefinitionContainer = formProperties.getLovDefinitionContainer();
                        for (EJPluginLovMappingProperties map : properties.getLovMappingContainer().getAllLovMappingProperties())
                        {

                            String definitionName = map.getLovDefinitionName();
                            if (lovDefinitionContainer.contains(definitionName))
                                continue;
                            EJPluginLovDefinitionProperties def = properties.getFormProperties().getLovDefinitionProperties(definitionName);
                            if (def != null)
                            {
                                lovDefinitionContainer.addLovDefinitionProperties(def);
                            }
                        }

                        String canvasName = properties.getCanvasName();
                        try
                        {
                            properties.setCanvasName(null);
                            FormPropertiesWriter writer = new FormPropertiesWriter();
                            writer.saveForm(formProperties, formFile, new NullProgressMonitor());
                            EJPluginEntireJPropertiesLoader.reload(formProperties.getJavaProject());
                            folder.refreshLocal(IResource.DEPTH_ONE, new NullProgressMonitor());
                            properties.internalSetName(oblockName);
                            properties.setReferencedBlockName(dlg.getValue());
                            properties.setIsReferenced(true);
                        }
                        finally
                        {
                            properties.setCanvasName(canvasName);
                        }
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {

                                editor.setDirty(true);
                                refresh(findNode(lformProperties.getBlockContainer()), true);

                            }
                        });
                    }
                    catch (JavaModelException e)
                    {
                        EJCoreLog.logException(e);
                    }
                    catch (CoreException e)
                    {
                        EJCoreLog.logException(e);
                    }
                }

            }
        };
    }

    public Action createNewRefBlockAction(final boolean copyOption)
    {

        return new Action("Add Referenced Block")
        {

            @Override
            public void runWithEvent(Event event)
            {
                RefBlockWizard wizard = new RefBlockWizard(new RefBlockWizardContext()
                {

                    public boolean copyOption()
                    {
                        return copyOption;
                    }
                    
                    public void addBlock(String blockName, String refblock, boolean copyRefBlock, String canvas, boolean createCanvas)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();

                        EJPluginReusableBlockProperties reusableEJPluginBlockProperties = null;
                        try
                        {
                            reusableEJPluginBlockProperties = formProperties.getEntireJProperties().getReusableBlockProperties(refblock);
                        }
                        catch (EJDevFrameworkException e)
                        {
                            EJCoreLog.logException(e);
                            return;
                        }
                        final EJPluginBlockProperties blockProperties = reusableEJPluginBlockProperties.getBlockProperties().makeCopy(blockName, false);

                        if (createCanvas)
                        {
                            EJPluginCanvasContainer container = formProperties.getCanvasContainer();
                            EJCanvasProperties canvasProp = new EJPluginCanvasProperties(formProperties, canvas);
                            container.addCanvasProperties(canvasProp);
                        }

                        formProperties.getBlockContainer().addBlockProperties(blockProperties);
                        blockProperties.setCanvasName(canvas);
                        if (!copyRefBlock)
                        {
                            blockProperties.setReferencedBlockName(refblock);

                        }
                        blockProperties.setIsReferenced(!copyRefBlock);

                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                refresh(findNode(formProperties.getBlockContainer()), true);
                                refresh(findNode(formProperties.getCanvasContainer()));
                                selectNodes(true, findNode(blockProperties));

                            }
                        });

                    }

                    public List<EJCanvasProperties> getCanvas()
                    {
                        Collection<EJCanvasProperties> canvasCollection = EJPluginCanvasRetriever.retriveAllNonAssignedBlockCanvases(editor.getFormProperties());
                        return new ArrayList<EJCanvasProperties>(canvasCollection);
                    }

                    public boolean hasBlock(String blockName)
                    {
                        return editor.getFormProperties().getBlockContainer().contains(blockName);
                    }

                    public boolean hasCanvas(String canvasName)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();
                        return EJPluginCanvasRetriever.canvasExists(formProperties, canvasName);
                    }

                    public IJavaProject getProject()
                    {
                        return editor.getJavaProject();
                    }

                    public List<String> getReferencedBlockNames()
                    {

                        return editor.getFormProperties().getEntireJProperties().getReusableBlockNames();
                    }

                });
                wizard.open();
            }

        };
    }

    public Action createNewLovAction()
    {

        return new Action("New LOV Definition")
        {

            @Override
            public void runWithEvent(Event event)
            {
                NewEntireJRefLovWizard wizard = new NewEntireJRefLovWizard(new NewEntireJRefLovWizard.LovContext()
                {

                    public void addRefrence(String lovName, EJPluginLovDefinitionProperties reusableLovDef)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();

                        if (reusableLovDef != null)
                        {
                            final EJPluginLovDefinitionProperties lovProperties = reusableLovDef.makeCopy(lovName, formProperties);

                            lovProperties.setReferencedLovDefinitionName(lovName);
                            lovProperties.setIsReferenced(true);

                            formProperties.getLovDefinitionContainer().addLovDefinitionProperties(lovProperties);

                            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                            {

                                public void run()
                                {
                                    editor.setDirty(true);
                                    refresh(findNode(formProperties.getLovDefinitionContainer()), true);
                                    selectNodes(true, findNode(lovProperties));

                                }
                            });
                        }

                    }
                });
                wizard.init(getEditor().getEditorSite().getWorkbenchWindow().getWorkbench(), new StructuredSelection(getEditor().getFile()));

                WizardDialog dialog = new WizardDialog(getSection().getShell(), wizard);
                dialog.open();
            }

        };
    }

    public Action createNewRefLovAction()
    {

        return new Action("Add Referenced LOV Definition")
        {

            @Override
            public void runWithEvent(Event event)
            {
                RefLovWizard wizard = new RefLovWizard(new RefLovWizardContext()
                {

                    public void addLov(final String lovName, final String refLov)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();

                        EJPluginLovDefinitionProperties reusableLovDef;
                        try
                        {
                            reusableLovDef = formProperties.getEntireJProperties().getReusableLovDefinitionProperties(refLov);
                        }
                        catch (EJDevFrameworkException e)
                        {
                            EJCoreLog.logException(e);
                            return;
                        }
                        final EJPluginLovDefinitionProperties lovProperties = reusableLovDef.makeCopy(lovName, formProperties);

                        lovProperties.setReferencedLovDefinitionName(refLov);
                        lovProperties.setIsReferenced(true);

                        formProperties.getLovDefinitionContainer().addLovDefinitionProperties(lovProperties);

                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                refresh(findNode(formProperties.getLovDefinitionContainer()), true);
                                selectNodes(true, findNode(lovProperties));

                            }
                        });

                    }

                    public List<String> getReferencedLovNames()
                    {

                        return editor.getFormProperties().getEntireJProperties().getReusableLovDefinitionNames();
                    }

                    public boolean hasLov(String lovName)
                    {
                        return editor.getFormProperties().getLovDefinitionContainer().contains(lovName);
                    }

                    public IJavaProject getProject()
                    {
                        return editor.getJavaProject();
                    }

                });
                wizard.open();
            }

        };
    }

    public Action createNewRelationAction()
    {

        return new Action("New Block Relation")
        {

            @Override
            public void runWithEvent(Event event)
            {
                RelationWizard wizard = new RelationWizard(new RelationWizardContext()
                {

                    public List<String> getBlockNames()
                    {
                        return new ArrayList<String>(editor.getFormProperties().getBlockNames());
                    }

                    public boolean hasRelation(String lovName)
                    {
                        return editor.getFormProperties().getRelationContainer().contains(lovName);
                    }

                    public IJavaProject getProject()
                    {
                        return editor.getJavaProject();
                    }

                    public void addRelation(String relationName, String master, String detail)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();

                        final EJPluginRelationProperties relationProperties = new EJPluginRelationProperties(formProperties, relationName);
                        relationProperties.setMasterBlockName(master);
                        relationProperties.setDetailBlockName(detail);
                        formProperties.getRelationContainer().addRelationProperties(relationProperties);
                        EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
                        {

                            public void run()
                            {
                                editor.setDirty(true);
                                refresh(findNode(formProperties.getRelationContainer()), true);
                                selectNodes(true, findNode(relationProperties));

                            }
                        });

                    }

                    public String validDtlRelation(String dtlBlock)
                    {
                        final EJPluginFormProperties formProperties = editor.getFormProperties();

                        List<EJPluginRelationProperties> allRelationProperties = formProperties.getRelationContainer().getAllRelationProperties();
                        for (EJPluginRelationProperties relationProperties : allRelationProperties)
                        {
                            if (relationProperties.getDetailBlockName() != null && relationProperties.getDetailBlockName().equals(dtlBlock))
                            {
                                return String.format("relation '%s' already defined with detail block '%s'.", relationProperties.getName(), dtlBlock);
                            }
                        }
                        return null;
                    }

                });
                wizard.open();
            }

        };
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
                return new Object[] { baseNode = new FormNode(editor.getFormProperties()), new ObjectGroupNode(FormDesignTreeSection.this),new BlockGroupNode(FormDesignTreeSection.this),
                        new RelationsGroupNode(FormDesignTreeSection.this), new LovGroupNode(FormDesignTreeSection.this),
                        new CanvasGroupNode(FormDesignTreeSection.this) };
            }
        };
    }

    private class FormNode extends AbstractNode<EJPluginFormProperties> implements NodeOverview
    {
        private final Image                 FORM      = EJUIImages.getImage(EJUIImages.DESC_FORM);
        private AbstractMarkerNodeValidator validator = new AbstractMarkerNodeValidator()
                                                      {

                                                          public void refreshNode()
                                                          {
                                                              refresh(FormNode.this);
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

        private FormNode(EJPluginFormProperties source)
        {
            super(null, source);
        }

        public String getName()
        {
            String name = source.getName();
            if (name == null || name.length() == 0)
                name = "<form>";

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
            
            return "Click <a href=\"http://docs.entirej.com/display/EJ1/Form+Properties#FormProperties\">here</a> for more information on Form Properties. All mandatory properties are denoted by \"*\"";
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
                        return "preview the defined layout in form.";
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
            return FORM;
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
                    "If you are using more cryptic names for your forms i.e. FRM001, FRM002 etc, then you may want to have a different name displayed in your project tree so you can find your form easier")
            {

                @Override
                public void setValue(String value)
                {
                    source.setFormDisplayName(value);
                    editor.setDirty(true);
                    refresh(FormNode.this);
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

                                       return (tag & FormNodeTag.TITLE) != 0;
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
                    source.setFormTitle(value);
                    editor.setDirty(true);
                    refresh(FormNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getTitle();
                }

                @Override
                public String getTooltip()
                {
                    return "This is the title displayed to the user when the form is run. The title will be sent to the applications Translator for translation if required";
                }

            };
            titleDescriptor.setRequired(true);

            AbstractTextDropDownDescriptor rendererDescriptor = new AbstractTextDropDownDescriptor("Renderer",
                    "The form renderer defined for the client framework you are using")
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
                    EJPluginAssignedRendererContainer rendererContainer = source.getEntireJProperties().getFormRendererContainer();
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
                    source.setFormRendererName(value);
                    EJFrameworkExtensionProperties extensionProperties = ExtensionsPropertiesFactory.createFormRendererProperties(source, true);
                    source.setFormRendererProperties(extensionProperties);
                    editor.setDirty(true);
                    refresh(FormNode.this);
                    if (descriptorViewer != null)
                        descriptorViewer.showDetails(FormNode.this);
                }

                @Override
                public String getValue()
                {
                    return source.getFormRendererName();
                }
            };
            rendererDescriptor.setRequired(true);

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
                public String getWarnings()
                {
                    return validator.getWarningMarkerMsg(fmarkers, vfilter);
                }

                @Override
                public void setValue(String value)
                {
                    source.setActionProcessorClassName(value);
                    editor.setDirty(true);
                    refresh(FormNode.this);

                }

                @Override
                public String getValue()
                {
                    return source.getActionProcessorClassName();
                }

                @Override
                public String getTooltip()
                {
                    return "The action processor to use for this form. Action Processors are used as event handlers for your form";
                }
            };
            actionDescriptor.setBaseClass(EJFormActionProcessor.class.getName());
            actionDescriptor.setDefaultClass(EJDefaultFormActionProcessor.class.getName());

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

                    return "The width (in pixels) of the form within it's container. If the width of the form is wider than the available space then a horizontal scroll bar will be shown ";
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
                    refresh(FormNode.this);
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

                    return "The height (in pixels) of the form within it's container. If the form height is higher than the available space then a vertical scroll bar will be shown";
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
                    refresh(FormNode.this);
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
                    return "The amount of columns the form will use to layout it's contained canvases";
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
                    refresh(FormNode.this);
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
                public String getTooltip()
                {
                    return "Click <a href=\"http://docs.entirej.com/display/EJ1/Laying+out+an+EntireJ+Form\">here</a> For more information on laying out an EntireJ Form";

                }

                public AbstractDescriptor<?>[] getDescriptors()
                {
                    return new AbstractDescriptor<?>[] { widthDescriptor, heightDescriptor, colDescriptor };
                }
            };

            // try to load renderer group
            EJFrameworkExtensionProperties rendereProperties = source.getFormRendererProperties();
            if (rendereProperties == null && source.getFormRendererName() != null)
            {
                rendereProperties = ExtensionsPropertiesFactory.createFormRendererProperties(source, true);
                source.setFormRendererProperties(rendereProperties);
            }
            if (rendereProperties != null)
            {

                final EJDevFormRendererDefinition formRendererDefinition = ExtensionsPropertiesFactory.loadFormRendererDefinition(
                        source.getEntireJProperties(), source.getFormRendererName());
                if (formRendererDefinition != null)
                {
                    final EJPropertyDefinitionGroup definitionGroup = formRendererDefinition.getFormPropertyDefinitionGroup();
                    if (definitionGroup != null)
                    {

                        AbstractGroupDescriptor rendererGroupDescriptor = new AbstractGroupDescriptor("Renderer Settings")
                        {

                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                                return PropertyDefinitionGroupPart.createGroupDescriptors(editor, source.getEntireJProperties(), definitionGroup,
                                        source.getFormRendererProperties(), new IExtensionValues()
                                        {

                                            public void loadValidValuesFromExtension(EJFrameworkExtensionProperties frameworkExtensionProperties,
                                                    EJPropertyDefinition propertyDefinition)
                                            {
                                                propertyDefinition.clearValidValues();
                                                EJFormRendererDefinition rendererDef = ExtensionsPropertiesFactory.loadFormRendererDefinition(
                                                        source.getEntireJProperties(), source.getFormRendererName());
                                                rendererDef.loadValidValuesForProperty(frameworkExtensionProperties, propertyDefinition);
                                            }

                                            public EJPluginBlockProperties getBlockProperties()
                                            {
                                                return null;
                                            }
                                        });
                            }
                        };

                        return new AbstractDescriptor<?>[] { titleDescriptor, rendererDescriptor, actionDescriptor, layoutGroupDescriptor,
                                rendererGroupDescriptor, metadataGroupDescriptor };
                    }
                }
            }

            AbstractGroupDescriptor parametersDes = new AbstractGroupDescriptor("Form Parameters")
            {
                IRefreshHandler              handler;
                TableViewer                  tableViewer;
                EJPluginApplicationParameter entry = null;
                Action                       deleteAction;

                @Override
                public String getTooltip()
                {
                    return "Form parameters are form global variables that can be accessed from the forms action processor or used as default Query / Insert values on the block items. For more information, read the Form Properties section <a href=\"http://docs.entirej.com/display/EJ1/Form+Properties#FormProperties-FormParameters\">here</a>";
                }

                public Action[] getToolbarActions()
                {

                    Action addAction = new Action("Add", IAction.AS_PUSH_BUTTON)
                    {

                        @Override
                        public void runWithEvent(Event event)
                        {

                            EJPluginApplicationParameter newEntry = new EJPluginApplicationParameter("", "java.lang.String");
                            source.getFormProperties().addFormParameter(newEntry);

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

                            source.getFormProperties().removeFormParameter(entry);
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
                    return source.getFormProperties().getAllFormParameters().toArray();
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
                    return new AbstractDescriptor<?>[] { nameDescriptor, typeDescriptor };
                }
            };

            return new AbstractDescriptor<?>[] { titleDescriptor, rendererDescriptor, actionDescriptor, layoutGroupDescriptor, parametersDes,
                    metadataGroupDescriptor };
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

    static interface FormPreviewer extends INodeDescriptorViewer
    {
        void refresh();
    }
}
