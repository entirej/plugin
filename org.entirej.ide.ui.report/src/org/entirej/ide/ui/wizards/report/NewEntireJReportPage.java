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
package org.entirej.ide.ui.wizards.report;

import java.net.URI;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.ui.IJavaElementSearchConstants;
import org.eclipse.jdt.ui.wizards.NewTypeWizardPage;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.entirej.framework.plugin.reports.EJPluginReportFormats;
import org.entirej.framework.plugin.reports.EJPluginReportProperties;
import org.entirej.framework.plugin.reports.writer.ReportPropertiesWriter;
import org.entirej.framework.report.actionprocessor.EJDefaultReportActionProcessor;
import org.entirej.framework.report.actionprocessor.interfaces.EJReportActionProcessor;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.core.project.EJReportProject;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.wizards.NewWizard;

public class NewEntireJReportPage extends NewTypeWizardPage
{

    private String                             reportTitle;

    protected IStatus                          fReportRendererStatus = new Status(IStatus.OK, EJUIPlugin.getID(), null);
    protected IStatus                          fReportFormatStatus   = new Status(IStatus.OK, EJUIPlugin.getID(), null);
    protected IStatus                          fReportTitleStatus    = new Status(IStatus.OK, EJUIPlugin.getID(), null);

    private EJPluginReportFormats.ReportFormat reportFormat;

    private ComboViewer                        reportFormatViewer;

    public NewEntireJReportPage()
    {
        super(false, "ej.report");
    }

    /**
     * The wizard owning this page is responsible for calling this method with
     * the current selection. The selection is used to initialize the fields of
     * the wizard page.
     * 
     * @param selection
     *            used to initialize the fields
     */
    public void init(IStructuredSelection selection)
    {
        IJavaElement jelem = getInitialJavaElement(selection);
        initContainerPage(jelem);
        initTypePage(jelem);
        doStatusUpdate();

        setSuperClass(EJDefaultReportActionProcessor.class.getName(), true);
    }

    private void doStatusUpdate()
    {
        
        IStatus projectTypeStatus = projectTypeStatus();
        // status of all used components
        IStatus[] status = new IStatus[] {projectTypeStatus, fContainerStatus, fPackageStatus, fTypeNameStatus, fReportRendererStatus, fReportFormatStatus, fReportTitleStatus };

        // the mode severe status will be displayed and the OK button
        // enabled/disabled.
        updateStatus(status);
    }

    private IStatus projectTypeStatus()
    {
        IStatus projectType = Status.OK_STATUS;
        
        if(getJavaProject()==null || !EJReportProject.hasPluginNature(getJavaProject().getProject()))
        {
            projectType = new Status(IStatus.ERROR, EJUIPlugin.getID(), "To create Report Project should be an Entirej Report Type");
        }
        return projectType;
    }
    
    
    protected void handleFieldChanged(String fieldName)
    {
        super.handleFieldChanged(fieldName);

        doStatusUpdate();
    }

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());

        int nColumns = 4;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        composite.setLayout(layout);

        // pick & choose the wanted UI components

        createContainerControls(composite, nColumns);
        createPackageControls(composite, nColumns);
        createSeparator(composite, nColumns);
        createTypeNameControls(composite, nColumns);
        createReportTitleControls(composite, nColumns);
        createReportFormatControls(composite, nColumns);
        createSuperClassControls(composite, nColumns);
        setControl(composite);
        Dialog.applyDialogFont(composite);
        
      
    }

    public String getTitleText()
    {
        return reportTitle;
    }

    @Override
    protected String getTypeNameLabel()
    {
        return "Report Name:";
    }

    @Override
    protected String getSuperClassLabel()
    {
        return "Action Processor:";
    }

    protected IType chooseSuperClass()
    {
        IJavaProject project = getJavaProject();
        if (project == null)
        {
            return null;
        }

        return JavaAccessUtils.selectType(getShell(), project.getResource(), IJavaElementSearchConstants.CONSIDER_CLASSES, getSuperClass(),
                EJReportActionProcessor.class.getName());
    }

    private void createReportTitleControls(Composite composite, int nColumns)
    {
        Label reportTitleLabel = new Label(composite, SWT.NULL);
        reportTitleLabel.setText("Report Title:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        reportTitleLabel.setLayoutData(gd);
        final Text formTitleText = new Text(composite, SWT.BORDER | SWT.SINGLE);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        formTitleText.setLayoutData(gd);
        formTitleText.addModifyListener(new ModifyListener()
        {

            public void modifyText(ModifyEvent e)
            {
                reportTitle = formTitleText.getText();
                fReportTitleStatus = reportTitleChanged();
                doStatusUpdate();
            }
        });
        fReportTitleStatus = reportTitleChanged();
        createEmptySpace(composite, 1);
    }

    private void createReportFormatControls(Composite composite, int nColumns)
    {
        Label formTitleLabel = new Label(composite, SWT.NULL);
        formTitleLabel.setText("Report Formats:");
        GridData gd = new GridData(GridData.HORIZONTAL_ALIGN_FILL);
        gd.horizontalSpan = 1;
        formTitleLabel.setLayoutData(gd);
        reportFormatViewer = new ComboViewer(composite);

        gd = new GridData();
        gd.horizontalAlignment = GridData.FILL;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = 2;
        reportFormatViewer.getCombo().setLayoutData(gd);
        reportFormatViewer.setLabelProvider(new ColumnLabelProvider()
        {
            @Override
            public String getText(Object element)
            {
                if (element instanceof EJPluginReportFormats.ReportFormat)
                {
                    EJPluginReportFormats.ReportFormat renderer = ((EJPluginReportFormats.ReportFormat) element);
                    return String.format("%s - [%d,%d]", renderer.name, renderer.reportWidth, renderer.reportHeight);
                }
                return super.getText(element);
            }

        });

        reportFormatViewer.setContentProvider(new IStructuredContentProvider()
        {

            public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
            {
            }

            public void dispose()
            {
            }

            public Object[] getElements(Object inputElement)
            {

                return EJPluginReportFormats.getFormats().toArray();
            }
        });

        reportFormatViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (reportFormatViewer.getSelection() instanceof IStructuredSelection)
                    reportFormat = (EJPluginReportFormats.ReportFormat) ((IStructuredSelection) reportFormatViewer.getSelection()).getFirstElement();

                fReportFormatStatus = reportFormatChanged();
                doStatusUpdate();
            }
        });
        createEmptySpace(composite, 1);
        refreshReportFormats();
    }

    public void refreshReportFormats()
    {
        if (reportFormatViewer != null)
        {
            reportFormatViewer.setInput(new Object());
            reportFormatViewer.setSelection(new StructuredSelection(EJPluginReportFormats.A4));
            reportFormat = EJPluginReportFormats.A4;
            fReportFormatStatus = reportFormatChanged();
            doStatusUpdate();
        }
    }

    protected IStatus typeNameChanged()
    {

        String typeName = getTypeName();
        // must not be empty
        if (typeName.length() == 0)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Report name is empty.");
        }

        IStatus result = ResourcesPlugin.getWorkspace().validateName(typeName, IResource.FILE);
        if (!result.isOK())
        {
            return result;
        }

        IPackageFragment packageFragment = getPackageFragment();

        IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

        IFolder folder = root.getFolder(packageFragment.getPath());
        final IFile reportFile = folder.getFile(new Path(typeName + ".ejreport"));
        if (folder.exists() && reportFile.exists())
        {

            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Report already exists.");

        }

        URI location = reportFile.getLocationURI();
        if (location != null)
        {
            try
            {
                IFileStore store = EFS.getStore(location);
                if (store.fetchInfo().exists())
                {
                    return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Report with same name but different case exists.");
                }
            }
            catch (CoreException e)
            {
                new Status(IStatus.ERROR, EJUIPlugin.getID(), e.getMessage());
            }
        }

        return new Status(IStatus.OK, EJUIPlugin.getID(), "");
    }

    protected IStatus reportTitleChanged()
    {

        String typeName = getTitleText();
        // must not be empty
        if (typeName == null || typeName.length() == 0)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Report title is empty.");
        }

        return new Status(IStatus.OK, EJUIPlugin.getID(), "");
    }

    protected IStatus reportFormatChanged()
    {

        if (reportFormat == null)
        {
            return new Status(IStatus.ERROR, EJUIPlugin.getID(), "Please choose a report format.");
        }
        return new Status(IStatus.OK, EJUIPlugin.getID(), "");
    }

    public void setFormName(String fname)
    {
        setTypeName(fname, true);

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

    public void createReport(IConfigurationElement configElement, IProgressMonitor monitor)
    {
        try
        {
            IJavaProject project = getJavaProject();
            if (project != null)
            {
                IPackageFragment packageFragment = getPackageFragment();
                String reportName = getTypeName();

                // create a sample file
                monitor.beginTask("Creating Report " + reportName, 2);
                IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();

                IFolder folder = root.getFolder(packageFragment.getPath());
                if (!folder.exists())
                {
                    folder.create(false, true, monitor);
                }

                final IFile formFile = folder.getFile(new Path(reportName + ".ejreport"));

                String reportTitle = getTitleText();

                String actionProcessor = getSuperClass();

                EJPluginReportProperties formProperties = new EJPluginReportProperties(reportName, project);
                formProperties.setReportTitle(reportTitle);
                if (actionProcessor != null && actionProcessor.length() > 0)
                {
                    formProperties.setActionProcessorClassName(actionProcessor);
                }

                formProperties.setReportHeight(reportFormat.reportHeight);
                formProperties.setReportWidth(reportFormat.reportWidth);
                formProperties.setMarginTop(reportFormat.marginTop);
                formProperties.setMarginBottom(reportFormat.marginBottom);
                formProperties.setMarginLeft(reportFormat.marginLeft);
                formProperties.setMarginRight(reportFormat.marginRight);
                formProperties.setHeaderSectionHeight(30);
                formProperties.setFooterSectionHeight(20);

                ReportPropertiesWriter writer = new ReportPropertiesWriter();
                writer.saveReport(formProperties, formFile, monitor);
                folder.refreshLocal(IResource.DEPTH_ONE, monitor);
                getShell().getDisplay().asyncExec(new Runnable()
                {
                    public void run()
                    {
                        IWizard iWizard = getWizard();
                        if (iWizard instanceof NewWizard)
                        {
                            NewWizard wizard = (NewWizard) iWizard;
                            wizard.selectAndReveal(formFile);
                            wizard.openResource(formFile);
                        }
                    }
                });
            }

        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }
}
