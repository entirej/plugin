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
package org.entirej.ide.ui.wizards.features;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.entirej.ide.core.EJConstants;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.spi.FeatureConfigProvider;
import org.entirej.ide.ui.EJUIPlugin;

public class NewEntireJFeaturePage extends WizardPage
{
    NewEntireJFeatureWizard featureWizard;
    public NewEntireJFeaturePage(NewEntireJFeatureWizard featureWizard)
    {
        super("ej.feature");//$NON-NLS-1$
        this.featureWizard = featureWizard;
    }

    public void init(IStructuredSelection selection)
    {
        
        System.out.println(selection);
        // IJavaElement jelem = getInitialJavaElement(selection);
        // initContainerPage(jelem);
        // initTypePage(jelem);
        // doStatusUpdate();
        //
        // setSuperClass(EJDefaultReportActionProcessor.class.getName(), true);
    }

    private ComboViewer           comboProjViewer;

    private Label                 featureDescription;
    private Label                 projDescription;

    private ComboViewer           comboFeatureViewer;

    private IJavaProject          selectedProject;
    private FeatureConfigProvider featureConfigProvider;

    public void createControl(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        createProjectGroup(container);
        new Label(container, SWT.NULL);
        createFeatureGroup(container);
        Dialog.applyDialogFont(container);

        // TODO: add help id - >
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(control, fFragment
        // ?help id);
        setControl(container);
    }

    protected boolean validatePage()
    {
        if(selectedProject==null)
        {
            setErrorMessage("Project not selected.");
            setPageComplete(false);
            return false;
        }
        if(featureConfigProvider==null)
        {
            setErrorMessage("Feature not selected.");
            setPageComplete(false);
            return false;
        }

        setErrorMessage(null);
        setMessage("Add a feature to EntireJ project.");
        setPageComplete(true);
        return true;
    }

    protected void createProjectGroup(final Composite container)
    {
        final Group group = new Group(container, SWT.NONE);
        group.setText("Target Project");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        comboProjViewer = new ComboViewer(group);
        comboProjViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        projDescription = createDescComponent(group);

        comboProjViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof IJavaProject)
                {
                    return ((IJavaProject) element).getElementName();
                }
                return super.getText(element);
            }

        });
        comboProjViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                List<IJavaProject> projectList = new LinkedList<IJavaProject>();
                try
                {
                    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
                    IProject[] projects = workspaceRoot.getProjects();
                    for (int i = 0; i < projects.length; i++)
                    {
                        IProject project = projects[i];
                        if (project.isOpen() && project.hasNature(EJConstants.EJ_NATURE))
                        {
                            projectList.add(JavaCore.create(project));
                        }
                    }
                }
                catch (CoreException ce)
                {
                    ce.printStackTrace();
                }

                return projectList.toArray();
            }
        });
        comboProjViewer.setInput(new Object());

        // if (comboProjViewer.getCombo().getItemCount() > 0 &&
        // comboProjViewer.getCombo().getSelectionIndex() == -1)
        // {
        // comboProjViewer.getCombo().select(0);
        // if (comboProjViewer.getSelection() instanceof IStructuredSelection)
        // selectedProject = (IJavaProject) ((IStructuredSelection)
        // comboProjViewer.getSelection()).getFirstElement();
        // }

        comboProjViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (comboProjViewer.getSelection() instanceof IStructuredSelection)
                    selectedProject = (IJavaProject) ((IStructuredSelection) comboProjViewer.getSelection()).getFirstElement();

                featureConfigProvider = null;
                updateCFDesc();
                container.layout();
            }
        });
        updateCFDesc();

    }

    private Label createDescComponent(Composite composite)
    {
        Label label = new Label(composite, SWT.NULL | SWT.WRAP);
        GridData gd = new GridData();
        gd.horizontalIndent = 10;
        gd.verticalIndent = 5;
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = true;
        gd.horizontalSpan = 2;
        gd.minimumHeight = 150;
        label.setLayoutData(gd);
        return label;
    }

    private void createFeatureGroup(final Composite container)
    {
        Group group = new Group(container, SWT.NONE);
        group.setText("Feature");
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboFeatureViewer = new ComboViewer(group);
        comboFeatureViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        featureDescription = createDescComponent(group);

        comboFeatureViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof FeatureConfigProvider)
                {
                    return ((FeatureConfigProvider) element).getProviderName();
                }
                return super.getText(element);
            }

        });
        final List<FeatureConfigProvider> exportProviders = new ArrayList<FeatureConfigProvider>();
        comboFeatureViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {
                exportProviders.clear();
                if (selectedProject == null)
                {
                    return exportProviders.toArray();
                }
                IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(FeatureConfigProvider.EXTENSION_POINT_ID);

                try
                {
                    for (IConfigurationElement element : config)
                    {
                        final Object impl = element.createExecutableExtension("class");
                        if (impl instanceof FeatureConfigProvider)
                        {
                            FeatureConfigProvider configProvider = (FeatureConfigProvider) impl;

                            if (configProvider.isSupport(selectedProject))
                            {
                                exportProviders.add(configProvider);
                            }

                        }
                    }

                }
                catch (CoreException ex)
                {
                    EJCoreLog.log(ex);
                }
                if (exportProviders.isEmpty())
                {
                    featureDescription.setText("No features found for project '"+selectedProject.getElementName()+"'.");
                }

                Collections.sort(exportProviders, new Comparator<FeatureConfigProvider>()
                {

                    public int compare(FeatureConfigProvider o1, FeatureConfigProvider o2)
                    {

                        return o1.getProviderName().compareTo(o2.getProviderName());
                    }
                });
                return exportProviders.toArray();
            }
        });
        comboFeatureViewer.setInput(new Object());
        String lastProvider = EJUIPlugin.getDefault().getPreferenceStore().getString(FeatureConfigProvider.EXTENSION_POINT_ID);
        if (lastProvider != null && lastProvider.length() > 0)
            for (FeatureConfigProvider provider : exportProviders)
            {
                if (provider.getProviderId().equals(lastProvider))
                {
                    comboFeatureViewer.setSelection(new StructuredSelection(provider));
                    featureConfigProvider = provider;
                    break;
                }
            }

        if (comboFeatureViewer.getCombo().getItemCount() > 0 && comboFeatureViewer.getCombo().getSelectionIndex() == -1)
        {
            comboFeatureViewer.getCombo().select(0);
            if (comboFeatureViewer.getSelection() instanceof IStructuredSelection)
                featureConfigProvider = (FeatureConfigProvider) ((IStructuredSelection) comboFeatureViewer.getSelection()).getFirstElement();
        }

        comboFeatureViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (comboFeatureViewer.getSelection() instanceof IStructuredSelection)
                    featureConfigProvider = (FeatureConfigProvider) ((IStructuredSelection) comboFeatureViewer.getSelection()).getFirstElement();

                updateDCDesc();
                container.layout();
            }
        });
        updateDCDesc();
    }

    private void updateDCDesc()
    {
        featureDescription.setText(featureConfigProvider != null ? featureConfigProvider.getDescription() : "Select a feature.");
        validatePage();
    }

    private void updateCFDesc()
    {
        projDescription
                .setText(selectedProject == null ? "Select a project." : "'" + selectedProject.getElementName() + "' project will auto-config with feature.");
        if (comboFeatureViewer != null)
            comboFeatureViewer.setInput(new Object());
        validatePage();
    }

    public void addFeature(IConfigurationElement configElement, IProgressMonitor monitor)
    {
        if(featureConfigProvider!=null && selectedProject!=null)
        {
            featureConfigProvider.config(selectedProject, new NullProgressMonitor());//TODO
        }

    }

}
