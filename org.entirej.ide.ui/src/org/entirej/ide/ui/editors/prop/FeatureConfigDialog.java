package org.entirej.ide.ui.editors.prop;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.entirej.framework.report.enumerations.EJReportScreenItemType;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.spi.FeatureConfigProvider;
import org.entirej.ide.ui.EJUIMessages;
import org.entirej.ide.ui.EJUIPlugin;

public class FeatureConfigDialog extends TitleAreaDialog
{

    private String                message = "";

    private IJavaProject          project;

    private ComboViewer           comboCFViewer;

    private Label                 cfDescription;

    private FeatureConfigProvider featureConfigProvider;
    
    FeatureConfigProvider EMPTY = new FeatureConfigProvider()
    {
        
        public boolean isSupport(IJavaProject project)
        {
            
            return true;
        }
        
        public String getProviderName()
        {
            return "Empty";
        }
        
        public String getProviderId()
        {
           
            return "empty";
        }
        
        public String getDescription()
        {
            return "No Features available";
        }
        
        public void config(IJavaProject project, IProgressMonitor monitor)
        {
            // TODO Auto-generated method stub
            
        }
    };

    public FeatureConfigDialog(Shell parentShell, IJavaProject project)
    {
        super(parentShell);
        setShellStyle(SWT.RESIZE | SWT.MAX | SWT.APPLICATION_MODAL);
        setHelpAvailable(false);
        this.project = project;
    }
    
    @Override
    public void create()
    {
        
        super.create();
        getShell().setSize(400,500);
    }

    @Override
    protected Control createContents(Composite parent)
    {
        Control contents = super.createContents(parent);

        setTitle("Add Feature");

        message = "Config feature to project.";
        getShell().setText("Feature");
        validate();

        return contents;
    }

    @Override
    protected Control createDialogArea(Composite parent)
    {

        Composite body = new Composite(parent, SWT.BORDER);
        body.setLayout(new GridLayout());
        GridData sectionData = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        body.setLayoutData(sectionData);
        buildControls(body);

        return parent;
    }

    private void buildControls(Composite parent)
    {
        // TODO: rework layout code
        final Composite container = new Composite(parent, SWT.NULL);

        container.setLayout(new GridLayout(2, false));
        container.setLayoutData(new GridData(GridData.FILL_BOTH | GridData.GRAB_HORIZONTAL));

        comboCFViewer = new ComboViewer(container);
        comboCFViewer.getCombo().setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        cfDescription = createDescComponent(container);

        comboCFViewer.setLabelProvider(new ColumnLabelProvider()
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
                IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(FeatureConfigProvider.EXTENSION_POINT_ID);

                try
                {
                    for (IConfigurationElement element : config)
                    {
                        final Object impl = element.createExecutableExtension("class");
                        if (impl instanceof FeatureConfigProvider)
                        {
                            FeatureConfigProvider configProvider = (FeatureConfigProvider) impl;
                            
                            if(configProvider.isSupport(project))
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
                if(exportProviders.isEmpty())
                {
                    exportProviders.add(EMPTY);
                }
                
                Collections.sort(exportProviders,new Comparator<FeatureConfigProvider>()
                {

                    public int compare(FeatureConfigProvider o1, FeatureConfigProvider o2)
                    {
                     
                        return o1.getProviderName().compareTo(o2.getProviderName());
                    }
                });
                return exportProviders.toArray();
            }
        });
        comboCFViewer.setInput(new Object());
        String lastProvider = EJUIPlugin.getDefault().getPreferenceStore().getString(FeatureConfigProvider.EXTENSION_POINT_ID);
        if (lastProvider != null && lastProvider.length() > 0)
            for (FeatureConfigProvider provider : exportProviders)
            {
                if (provider.getProviderId().equals(lastProvider))
                {
                    comboCFViewer.setSelection(new StructuredSelection(provider));
                    featureConfigProvider = provider;
                    break;
                }
            }

        if (comboCFViewer.getCombo().getItemCount() > 0 && comboCFViewer.getCombo().getSelectionIndex() == -1)
        {
            comboCFViewer.getCombo().select(0);
            if (comboCFViewer.getSelection() instanceof IStructuredSelection)
                featureConfigProvider = (FeatureConfigProvider) ((IStructuredSelection) comboCFViewer.getSelection()).getFirstElement();
        }

        comboCFViewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {

                if (comboCFViewer.getSelection() instanceof IStructuredSelection)
                    featureConfigProvider = (FeatureConfigProvider) ((IStructuredSelection) comboCFViewer.getSelection()).getFirstElement();

                updateCFDesc();
                container.layout();
            }
        });
        updateCFDesc();

    }

    private void updateCFDesc()
    {
        cfDescription.setText(
                featureConfigProvider != null ? featureConfigProvider.getDescription() : EJUIMessages.NewProjectWizard_ConfigPage_target_platfrom_desc);
    }

    private void validate()
    {
        IStatus iStatus = org.eclipse.core.runtime.Status.OK_STATUS;

        if (iStatus.isOK())
        {
            setMessage(message);
        }
        else
        {
            setMessage(iStatus.getMessage(), IMessageProvider.ERROR);

        }
        
        
        
        if (getButton(IDialogConstants.OK_ID) != null)
            getButton(IDialogConstants.OK_ID).setEnabled(iStatus.isOK()&& featureConfigProvider!=null  && EMPTY!=featureConfigProvider);

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

    @Override
    protected void buttonPressed(int buttonId)
    {
        if (buttonId == IDialogConstants.CANCEL_ID)
        {
            super.buttonPressed(buttonId);
        }
        else
        {

            
            
            
            if(featureConfigProvider!=null)
            {
                featureConfigProvider.config(project, new NullProgressMonitor());//TODO
            }
            super.buttonPressed(buttonId);
        }

    }

}