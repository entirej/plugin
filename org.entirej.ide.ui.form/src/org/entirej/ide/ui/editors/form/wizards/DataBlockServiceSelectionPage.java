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
package org.entirej.ide.ui.editors.form.wizards;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.common.viewers.CTreeComboViewer;
import org.entirej.ide.ui.editors.descriptors.IJavaProjectProvider;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.TypeAssistProvider;

public class DataBlockServiceSelectionPage extends WizardPage
{
    public static final String           NEW_CANVAS  = "[New Canvas]";
    public static final String           NONE_CANVAS = "[NONE]";
    private final DataBlockWizardContext wizardContext;
    private String                       blockName;

    private String                       newCanvasName;
    private Text                         newCanvasText;

    private EJPluginRenderer             blockRenderer;
    private CTreeComboViewer                  blockRenderersViewer;

    private String                       blockCanves;
    private ComboViewer                  blockCanvesViewer;
    private String                       blockServiceClass;

    protected DataBlockServiceSelectionPage(DataBlockWizardContext wizardContext)
    {
        super("ej.data.service.selection");
        this.wizardContext = wizardContext;
        setTitle("Data Block");
        setDescription("Properties for the new data block.");
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);
        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        Dialog.applyDialogFont(composite);
        int nColumns = 4;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);
        createBlockName(composite, nColumns);
        createBlockRendererControls(composite, nColumns);
        if (wizardContext.supportService())
        {
            createBlockServiceControls(composite, nColumns);
        }
        createSeparator(composite, nColumns);
        createBlockCanvasControls(composite, nColumns);
        createNewCanvasName(composite, nColumns);

        setControl(composite);

        setPageComplete(false);
    }

    protected void createSeparator(Composite composite, int nColumns)
    {
        GridData gridData = new GridData(GridData.FILL_HORIZONTAL);
        gridData.horizontalSpan = nColumns;
        gridData.heightHint = convertHeightInCharsToPixels(1);
        (new Label(composite, SWT.SEPARATOR | SWT.HORIZONTAL)).setLayoutData(gridData);
    }

    public static Control createEmptySpace(Composite parent, int span)
    {
        Label label = new Label(parent, SWT.LEFT);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.horizontalIndent = 0;
        gd.widthHint = 0;
        gd.heightHint = 0;
        label.setLayoutData(gd);
        return label;
    }

    private void createBlockName(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Name:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final Text blockNameText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        blockNameText.setLayoutData(gd);
        blockNameText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                blockName = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createBlockRendererControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Block Renderer:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        blockRenderersViewer = new CTreeComboViewer(composite,SWT.READ_ONLY|SWT.SINGLE|SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockRenderersViewer.getTree().setLayoutData(gd);
        blockRenderersViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof EJPluginRenderer)
                {
                    EJPluginRenderer renderer = ((EJPluginRenderer) element);
                    return String.format("%s", renderer.getAssignedName(), renderer.getRendererClassName());
                }
                return super.getText(element);
            }

        });
        
        
        blockRenderersViewer.setAutoExpandLevel(3);
       
        blockRenderersViewer.setContentProvider(new ITreeContentProvider()
        {
            List<EJPluginRenderer> renderers;
            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
                 
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                renderers = wizardContext.getBlockRenderer();
                List<String>  groups = new ArrayList<String>();
                
                List<EJPluginRenderer> other = new ArrayList<EJPluginRenderer>();
                
                for (EJPluginRenderer renderer : renderers)
                {
                    if(renderer.getGroup()!=null && !renderer.getGroup().isEmpty())
                    {
                        if(!groups.contains(renderer.getGroup()))
                        groups.add(renderer.getGroup());
                    }
                    else
                        other.add(renderer); 
                        
                    
                }
                
                Collections.sort(other,new Comparator<EJPluginRenderer>()
                {

                    public int compare(EJPluginRenderer o1, EJPluginRenderer o2)
                    {
                     
                        return o1.getAssignedName().compareTo(o2.getAssignedName());
                    }
                });
               
                List<Object> all = new ArrayList<Object>();
                all.addAll(groups);
                all.addAll(other);
                
                return all.toArray();
            }

            public Object[] getChildren(Object parentElement)
            {
                if(parentElement instanceof String)
                {
                    List<EJPluginRenderer> group = new ArrayList<EJPluginRenderer>();
                    
                    for (EJPluginRenderer renderer : renderers)
                    {
                        if(parentElement.equals(renderer.getGroup()))
                        {
                            group.add(renderer);
                        }
                    }
                    
                    Collections.sort(group,new Comparator<EJPluginRenderer>()
                    {

                        public int compare(EJPluginRenderer o1, EJPluginRenderer o2)
                        {
                         
                            return o1.getAssignedName().compareTo(o2.getAssignedName());
                        }
                    });
                    
                    return group.toArray();
                }
                return new  Object[0];
            }

            public Object getParent(Object element)
            {
                if(element instanceof EJPluginRenderer)
                    return ((EJPluginRenderer)element).getGroup();
                return null;
            }

            public boolean hasChildren(Object element)
            {
                return element instanceof String;
            }
        });
        blockRenderersViewer.getTree().setItemCount(12);
        
        blockRenderersViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockRenderersViewer.getSelection() instanceof IStructuredSelection  )
                {
                    Object firstElement = ((IStructuredSelection) blockRenderersViewer.getSelection()).getFirstElement();
                    if(firstElement instanceof EJPluginRenderer)
                    {
                        blockRenderer = (EJPluginRenderer) firstElement; 
                        blockRenderersViewer.getTree().hideDropDown();
                    }
                    else
                        blockRenderer = null;
                    
                }
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        refreshFormRenderers();
    }

    public void refreshFormRenderers()
    {
        if (blockRenderersViewer != null)
        {
            blockRenderersViewer.setInput(new Object());
            //TODO: select first 
//            blockRenderersViewer.getTree().select(-1);
//            if (blockRenderersViewer.getCombo().getItemCount() > 0 && blockRenderersViewer.getCombo().getSelectionIndex() == -1)
//            {
//                blockRenderersViewer.getCombo().select(0);
//                if (blockRenderersViewer.getSelection() instanceof IStructuredSelection)
//                    blockRenderer = (EJPluginRenderer) ((IStructuredSelection) blockRenderersViewer.getSelection()).getFirstElement();
//            }
            doUpdateStatus();
        }
    }

    private void createBlockCanvasControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Block Canvas:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        blockCanvesViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockCanvesViewer.getCombo().setLayoutData(gd);
        blockCanvesViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {

                return super.getText(element);
            }

        });

        blockCanvesViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<EJCanvasProperties> cances = wizardContext.getCanvas();

                Collections.sort(cances,new Comparator<EJCanvasProperties>()
                {

                    public int compare(EJCanvasProperties o1, EJCanvasProperties o2)
                    {
                     
                        return o1.getName().compareTo(o2.getName());
                    }
                });
                
                List<String> list = new ArrayList<String>(cances.size());
                list.add(NONE_CANVAS);
                list.add(NEW_CANVAS);
                for (EJCanvasProperties canvasProp : cances)
                {
                    list.add(canvasProp.getName());
                }
                return list.toArray();
            }
        });

        blockCanvesViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockCanvesViewer.getSelection() instanceof IStructuredSelection)
                    blockCanves = (String) ((IStructuredSelection) blockCanvesViewer.getSelection()).getFirstElement();
                if (newCanvasText != null)
                    if (NEW_CANVAS.equals(blockCanves))
                    {
                        newCanvasText.setEnabled(true);
                    }
                    else
                    {
                        newCanvasText.setText("");
                        newCanvasText.setEnabled(false);
                    }
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        refreshCanvases();
    }

    public void refreshCanvases()
    {
        if (blockCanvesViewer != null)
        {
            blockCanvesViewer.setInput(new Object());
            if (newCanvasText != null)
                newCanvasText.setText("");
            blockCanves = NEW_CANVAS;
            blockCanvesViewer.getCombo().select(1);
            doUpdateStatus();
        }
    }

    private void createNewCanvasName(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("New Canvas Name:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        newCanvasText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        newCanvasText.setLayoutData(gd);
        newCanvasText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                newCanvasName = newCanvasText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createBlockServiceControls(Composite composite, int nColumns)
    {
        Label serviceGenLabel = new Label(composite, SWT.NULL);
        serviceGenLabel.setText("Block Service:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        serviceGenLabel.setLayoutData(gd);
        final Text serviceGenText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        serviceGenText.setLayoutData(gd);
        if (blockServiceClass != null)
            serviceGenText.setText(blockServiceClass);
        serviceGenText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                blockServiceClass = serviceGenText.getText().trim();
                doUpdateStatus();
            }
        });
        TypeAssistProvider.createTypeAssist(serviceGenText, new IJavaProjectProvider()
        {

            public IJavaProject getJavaProject()
            {
                return wizardContext.getProject();
            }
        }, IJavaElementSearchConstants.CONSIDER_CLASSES, EJBlockService.class.getName());
        final Button browse = new Button(composite, SWT.PUSH);
        browse.setText("Browse...");
        browse.addSelectionListener(new SelectionAdapter()
        {
            public void widgetSelected(SelectionEvent e)
            {
                String value = serviceGenText.getText();
                IType type = JavaAccessUtils.selectType(EJUIPlugin.getActiveWorkbenchShell(), wizardContext.getProject().getResource(),
                        IJavaElementSearchConstants.CONSIDER_CLASSES, value == null ? "" : value, EJBlockService.class.getName());
                if (type != null)
                {
                    serviceGenText.setText(type.getFullyQualifiedName('$'));
                }
            }

        });
        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 1;
        browse.setLayoutData(gd);
    }

    public String getBlockName()
    {
        return blockName;
    }

    public EJPluginRenderer getBlockRenderer()
    {
        return blockRenderer;
    }

    public String getBlockCanves()
    {
        if (NONE_CANVAS.equals(blockCanves))
        {
            return null;
        }
        return blockCanves;
    }

    public String getNewCanvasName()
    {
        return newCanvasName;
    }

    public String getBlockServiceClass()
    {
        return blockServiceClass;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (blockName == null || blockName.trim().length() == 0)
        {
            setErrorMessage("Block name can't be empty.");
            return false;
        }
        else if (wizardContext.hasBlock(blockName))
        {
            setErrorMessage("A block with this name already exists.");
            return false;
        }

        if (blockRenderer == null)
        {
            setErrorMessage("A block renderer must be specified.");
            return false;
        }

        if (NEW_CANVAS.equals(blockCanves))
        {
            if (newCanvasName == null || newCanvasName.trim().length() == 0)
            {
                setErrorMessage("new canvas name can't be empty.");
                return false;
            }
            else if (wizardContext.hasCanvas(newCanvasName))
            {
                setErrorMessage("A canvas with this name already exists.");
                return false;
            }
        }

        if (wizardContext.supportService() && blockServiceClass != null && blockServiceClass.trim().length() > 0)
        {
            IJavaProject javaProject = wizardContext.getProject();
            if (javaProject != null)
            {

                try
                {
                    IType findType = javaProject.findType(blockServiceClass);
                    if (findType == null)
                    {
                        setErrorMessage(String.format("%s can't find in project build path.", blockServiceClass));
                        return false;
                    }
                }
                catch (CoreException e)
                {
                    setErrorMessage(e.getMessage());
                    return false;
                }
            }
        }
        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
