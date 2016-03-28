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

import java.util.Collections;
import java.util.Comparator;
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
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.entirej.framework.plugin.framework.properties.EJPluginRenderer;

public class RefObjectGroupSelectionPage extends WizardPage
{
    private final RefObjectGroupWizardContext wizardContext;

    private String                            lovRef;
    private ComboViewer                       lovRefViewer;

    protected RefObjectGroupSelectionPage(RefObjectGroupWizardContext wizardContext)
    {
        super("ej.ref.objectgroup.selection");
        this.wizardContext = wizardContext;
        setTitle("Referenced ObjectGroup Definition");
        setDescription("Properties for the new referenced ObjectGroup definition.");
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
        createLovRefControls(composite, nColumns);
        createSeparator(composite, nColumns);

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

    private void createLovRefControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("ObjectGroup:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        lovRefViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        lovRefViewer.getCombo().setLayoutData(gd);
        lovRefViewer.setLabelProvider(new ColumnLabelProvider()
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

        lovRefViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<String> renderers = wizardContext.getReferencedObjectGroupNames();
                Collections.sort(renderers);
                return renderers.toArray();
            }
        });

        lovRefViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (lovRefViewer.getSelection() instanceof IStructuredSelection)
                    lovRef = (String) ((IStructuredSelection) lovRefViewer.getSelection()).getFirstElement();
                doUpdateStatus();
            }
        });
        
        createEmptySpace(composite, 1);
        refreshRefLovs();
    }

    public void refreshRefLovs()
    {
        if (lovRefViewer != null)
        {
            lovRefViewer.setInput(new Object());
            lovRefViewer.getCombo().select(-1);
            
            doUpdateStatus();
        }
    }

    public String getObjectGroupRef()
    {
        return lovRef;
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    protected boolean validatePage()
    {

        if (lovRef == null)
        {
            setErrorMessage("A refernced ObjectGroup  must be specified.");
            return false;
        }
        else if (wizardContext.hasObjectGroup(lovRef))
        {
            setErrorMessage("ObjectGroup already Added.");
            return false;
        }
        
        //validate elements 
        String elementValidation = wizardContext.elementValidation(lovRef);
        if(elementValidation!=null)
        {
            setErrorMessage(elementValidation);
            return false;
        }
        

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

}
