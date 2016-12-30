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
package org.entirej.ide.ui.wizards.project;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogSettings;
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
import org.entirej.ide.core.spi.ClientFrameworkProvider;
import org.entirej.ide.core.spi.DBConnectivityProvider;
import org.entirej.ide.ui.EJFormUIMessages;
import org.entirej.ide.ui.EJUIPlugin;

public class NewEJProjectConfigPage extends WizardPage
{

    public final static String      CF_PROVIDER_ID = "org.entirej.ide.core.spi.clientframework.providerId";
    public final static String      DB_PROVIDER_ID = "org.entirej.ide.core.spi.databaseconnectivity.providerId";
    private ComboViewer             comboCFViewer;

    private Label                   dbDescription;
    private Label                   cfDescription;

    private ComboViewer             comboDBViewer;

    private ClientFrameworkProvider clientFrameworkProvider;
    private DBConnectivityProvider  dbConnectivityProvider;

    public NewEJProjectConfigPage()
    {
        super("ej.prj.config");//$NON-NLS-1$
        setTitle(EJFormUIMessages.NewProjectWizard_ConfigPag_title);
        setDescription(EJFormUIMessages.NewProjectWizard_ConfigPag_desc);
    }

    public void createControl(Composite parent)
    {
        Composite container = new Composite(parent, SWT.NULL);
        GridLayout layout = new GridLayout();
        container.setLayout(layout);

        createTargetClientPlatfromGroup(container);
        new Label(container, SWT.NULL);
        createDatabaseConnectivityGroup(container);
        Dialog.applyDialogFont(container);

        // TODO: add help id - >
        // PlatformUI.getWorkbench().getHelpSystem().setHelp(control, fFragment
        // ?help id);
        setControl(container);
    }

    protected boolean validatePage()
    {

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    protected void createTargetClientPlatfromGroup(final Composite container)
    {
        final Group group = new Group(container, SWT.NONE);
        group.setText(EJFormUIMessages.NewProjectWizard_ConfigPage_target_platfrom);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        comboCFViewer = new ComboViewer(group);
        comboCFViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        cfDescription = createDescComponent(group);

        comboCFViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof ClientFrameworkProvider)
                {
                    return ((ClientFrameworkProvider) element).getProviderName();
                }
                return super.getText(element);
            }

        });
        final List<ClientFrameworkProvider> exportProviders = new ArrayList<ClientFrameworkProvider>();
        comboCFViewer.setContentProvider(new IStructuredContentProvider()
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
                IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(ClientFrameworkProvider.EXTENSION_POINT_ID);

                try
                {
                    for (IConfigurationElement element : config)
                    {
                        final Object impl = element.createExecutableExtension("class");
                        if (impl instanceof ClientFrameworkProvider)
                        {
                            exportProviders.add((ClientFrameworkProvider) impl);
                        }
                    }
                }
                catch (CoreException ex)
                {
                    EJCoreLog.log(ex);
                }
                Collections.sort(exportProviders,new Comparator<ClientFrameworkProvider>()
                {

                    public int compare(ClientFrameworkProvider o1, ClientFrameworkProvider o2)
                    {
                        return o1.getProviderName().compareTo(o2.getProviderName());
                    }
                });
                return exportProviders.toArray();
            }
        });
        comboCFViewer.setInput(new Object());
        String lastProvider = EJUIPlugin.getDefault().getPreferenceStore().getString(CF_PROVIDER_ID);
        if (lastProvider != null && lastProvider.length() > 0)
            for (ClientFrameworkProvider provider : exportProviders)
            {
                if (provider.getProviderId().equals(lastProvider))
                {
                    comboCFViewer.setSelection(new StructuredSelection(provider));
                    clientFrameworkProvider = provider;
                    break;
                }
            }

        if (comboCFViewer.getCombo().getItemCount() > 0 && comboCFViewer.getCombo().getSelectionIndex() == -1)
        {
            comboCFViewer.getCombo().select(0);
            if (comboCFViewer.getSelection() instanceof IStructuredSelection)
                clientFrameworkProvider = (ClientFrameworkProvider) ((IStructuredSelection) comboCFViewer.getSelection()).getFirstElement();
        }

        comboCFViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (comboCFViewer.getSelection() instanceof IStructuredSelection)
                    clientFrameworkProvider = (ClientFrameworkProvider) ((IStructuredSelection) comboCFViewer.getSelection()).getFirstElement();

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

    private void createDatabaseConnectivityGroup(final Composite container)
    {
        Group group = new Group(container, SWT.NONE);
        group.setText(EJFormUIMessages.NewProjectWizard_ConfigPage_database_connectivity);
        group.setLayout(new GridLayout(2, false));
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        comboDBViewer = new ComboViewer(group);
        comboDBViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        dbDescription = createDescComponent(group);

        comboDBViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof DBConnectivityProvider)
                {
                    return ((DBConnectivityProvider) element).getProviderName();
                }
                return super.getText(element);
            }

        });
        final List<DBConnectivityProvider> exportProviders = new ArrayList<DBConnectivityProvider>();
        comboDBViewer.setContentProvider(new IStructuredContentProvider()
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
                IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(DBConnectivityProvider.EXTENSION_POINT_ID);

                try
                {
                    for (IConfigurationElement element : config)
                    {
                        final Object impl = element.createExecutableExtension("class");
                        if (impl instanceof DBConnectivityProvider)
                        {
                            exportProviders.add((DBConnectivityProvider) impl);
                        }
                    }
                }
                catch (CoreException ex)
                {
                    EJCoreLog.log(ex);
                }
                return exportProviders.toArray();
            }
        });
        comboDBViewer.setInput(new Object());
        String lastProvider = EJUIPlugin.getDefault().getPreferenceStore().getString(DB_PROVIDER_ID);
        if (lastProvider != null && lastProvider.length() > 0)
            for (DBConnectivityProvider provider : exportProviders)
            {
                if (provider.getProviderId().equals(lastProvider))
                {
                    comboDBViewer.setSelection(new StructuredSelection(provider));
                    dbConnectivityProvider = provider;
                    break;
                }
            }

        if (comboDBViewer.getCombo().getItemCount() > 0 && comboDBViewer.getCombo().getSelectionIndex() == -1)
        {
            comboDBViewer.getCombo().select(0);
            if (comboCFViewer.getSelection() instanceof IStructuredSelection)
                dbConnectivityProvider = (DBConnectivityProvider) ((IStructuredSelection) comboDBViewer.getSelection()).getFirstElement();
        }

        comboDBViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (comboDBViewer.getSelection() instanceof IStructuredSelection)
                    dbConnectivityProvider = (DBConnectivityProvider) ((IStructuredSelection) comboDBViewer.getSelection()).getFirstElement();

                updateDCDesc();
                container.layout();
            }
        });

        updateDCDesc();
    }

    private void updateDCDesc()
    {
        dbDescription.setText(dbConnectivityProvider != null ? dbConnectivityProvider.getDescription()
                : EJFormUIMessages.NewProjectWizard_ConfigPage_database_connectivity_desc);
    }

    private void updateCFDesc()
    {
        cfDescription.setText(clientFrameworkProvider != null ? clientFrameworkProvider.getDescription()
                : EJFormUIMessages.NewProjectWizard_ConfigPage_target_platfrom_desc);
    }

    public void saveSettings(IDialogSettings settings)
    {

    }

    public void addEntireNature(IConfigurationElement configElement, IJavaProject javaProject, IProgressMonitor monitor)
    {
        try
        {

            if (clientFrameworkProvider != null)
            {
                clientFrameworkProvider.addEntireJNature(configElement, javaProject, monitor);
                EJUIPlugin.getDefault().getPreferenceStore().putValue(CF_PROVIDER_ID, clientFrameworkProvider.getProviderId());

                if (dbConnectivityProvider != null)
                {
                    dbConnectivityProvider.addEntireJNature(clientFrameworkProvider, configElement, javaProject, monitor);
                    EJUIPlugin.getDefault().getPreferenceStore().putValue(DB_PROVIDER_ID, dbConnectivityProvider.getProviderId());
                }
            }

            // add EJ project nature to add EJ builders
            IProjectDescription description = javaProject.getProject().getDescription();
            String[] natures = description.getNatureIds();
            List<String> newNatures = new ArrayList<String>(Arrays.asList(natures));
            newNatures.add(EJConstants.EJ_NATURE);
            description.setNatureIds(newNatures.toArray(new String[0]));
            javaProject.getProject().setDescription(description, null);
        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }
}
