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
package org.entirej.ide.ui.editors.report.wizards;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.report.enumerations.EJReportScreenItemType;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptor;
import org.entirej.ide.ui.editors.descriptors.AbstractDescriptorPart;

public class ScreenItemSelectionPage extends WizardPage
{

    private final ScreenItemWizardContext     wizardContext;
    private String                             screenItemName;
    private EJPluginReportScreenItemProperties itemProperties;
    Composite                                  body;

    protected ScreenItemSelectionPage(ScreenItemWizardContext wizardContext)
    {
        super("ej.data.screenitem.selection");
        this.wizardContext = wizardContext;
        setTitle("New Screen Item");
        setDescription("Properties for the new screen item.");
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
        createBlockScreenTypeControls(composite, nColumns);
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
                screenItemName = blockNameText.getText();
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
    }

    private void createBlockScreenTypeControls(final Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Screen Item Type:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        final ComboViewer blockRenderersViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        blockRenderersViewer.getCombo().setLayoutData(gd);
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

        blockRenderersViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<EJReportScreenItemType> renderers = wizardContext.getBlockItemTypes();

                return renderers.toArray();
            }
        });

        blockRenderersViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (blockRenderersViewer.getSelection() instanceof IStructuredSelection)
                {
                    EJReportScreenItemType type = (EJReportScreenItemType) ((IStructuredSelection) blockRenderersViewer.getSelection()).getFirstElement();

                    if (body != null && !body.isDisposed())
                    {
                        body.dispose();
                    }
                    if (type != null)
                    {
                        itemProperties = wizardContext.newScreenItem(type);
                         body = new Group(composite, SWT.NONE);
                        body.setLayout(new GridLayout());

                        GridData layoutData = new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL | GridData.GRAB_VERTICAL);
                        layoutData.heightHint = 500;
                        layoutData.horizontalSpan = 2;
                        body.setLayoutData(layoutData);

                        AbstractDescriptorPart part = new AbstractDescriptorPart(wizardContext.getToolkit(), body, true)
                        {

                            @Override
                            public String getSectionTitle()
                            {
                                return "Screen Item Settings";
                            }

                            @Override
                            public String getSectionDescription()
                            {
                                return "";
                            }

                            @Override
                            public AbstractDescriptor<?>[] getDescriptors()
                            {
                               

                                return wizardContext.getDescriptors(itemProperties);
                            }
                        };
                        body.setBackground(body.getBackground());
                        part.buildUI();
                        composite.layout(true);
                    }
                }
                doUpdateStatus();
            }
        });
        createEmptySpace(composite, 1);
        blockRenderersViewer.setInput(new Object());
    }

    public String getScreenItemName()
    {
        return screenItemName;
    }

   

    public EJPluginReportScreenItemProperties getItemProperties()
    {
        return itemProperties;
    }

    
    
    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

   

    protected boolean validatePage()
    {

        if (screenItemName == null || screenItemName.trim().length() == 0)
        {
            setErrorMessage("Screen item name can't be empty.");
            return false;
        }
        if (itemProperties == null)
        {
            setErrorMessage("Screen item type need to select.");
            return false;
        }
        else if (wizardContext.hasScreenItem(screenItemName))
        {
            setErrorMessage("A screen item with this name already exists.");
            return false;
        }

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
