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
package org.entirej.ide.cf.rwt.lib;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.entirej.ide.cf.rwt.lib.RWTRuntimeVersions.Version;

public class RWTCFRuntimeContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension
{

    IJavaProject            javaProject;
    private IClasspathEntry containerEntryResult;

    public RWTCFRuntimeContainerPage()
    {
        super("EntireJ RWT/CF Runtime");
        containerEntryResult = JavaCore.newContainerEntry(RWTCFRuntimeClasspathContainer.ID);
    }

    public void initialize(IJavaProject javaProject, IClasspathEntry[] currentEntries)
    {
        this.javaProject = javaProject;
    }

    public IClasspathEntry getSelection()
    {
        return containerEntryResult;
    }

    public void setSelection(IClasspathEntry containerEntry)
    {
        if (containerEntry == null)
        {
            containerEntry = JavaCore.newContainerEntry(RWTCFRuntimeClasspathContainer.ID);
        }
        containerEntryResult = containerEntry;
    }

    public void createControl(Composite parent)
    {
        setTitle("EntireJ RWT/CF Runtime");
        setDescription("Add entirej rwt/CF libraries to project path.");

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        setControl(composite);

        Link link = new Link(composite, SWT.NONE);
        link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        link.setText("EntireJ RWT/CF runtime classpath container.");
        Composite versionsPanel = new Composite(composite, SWT.NONE);
        versionsPanel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
        versionsPanel.setLayout(new GridLayout(2, false));
        Label versionLbl = new Label(versionsPanel, SWT.NONE);
        versionLbl.setText("Version:");
        final ComboViewer versionsList = new ComboViewer(versionsPanel);

        versionsList.setLabelProvider(new ColumnLabelProvider()
        {

            @Override
            public String getText(Object element)
            {
                if (element instanceof RWTRuntimeVersions.Version)
                {
                    RWTRuntimeVersions.Version version = (Version) element;

                    return version.getName();
                }
                return super.getText(element);
            }
        });
        versionsList.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {

            }

            public void dispose()
            {

            }

            public Object[] getElements(Object inputElement)
            {
                return new Object[] { RWTRuntimeVersions.CF_V_2_1 };
            }
        });
        versionsList.setInput(new Object());

        versionsList.setSelection(new StructuredSelection(RWTRuntimeVersions.CF_V_2_1));

        versionsList.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                IStructuredSelection selection = (IStructuredSelection) versionsList.getSelection();
                if (!selection.isEmpty())
                {
                    RWTRuntimeVersions.Version version = (Version) selection.getFirstElement();
                    containerEntryResult = JavaCore.newContainerEntry(version.getPath());
                }
            }
        });
    }

    public boolean finish()
    {
        return true;
    }
}
