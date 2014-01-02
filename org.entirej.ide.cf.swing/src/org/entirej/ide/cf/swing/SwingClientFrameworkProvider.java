package org.entirej.ide.cf.swing;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.entirej.ide.cf.swing.lib.SwingRuntimeClasspathContainer;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.cf.EmptyClientFrameworkProvider;
import org.entirej.ide.core.spi.ClientFrameworkProvider;

public class SwingClientFrameworkProvider implements ClientFrameworkProvider
{

    private static final String SWING_PROJECT_PROPERTIES_FILE = "/templates/swing/EntireJApplication.properties";
    private static final String SWING_APP_LAUNCHER            = "/templates/swing/ApplicationLauncher.java";

    public void addEntireJNature(IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");
            CFProjectHelper.addFile(project, EJCFSwingPlugin.getDefault().getBundle(), SWING_PROJECT_PROPERTIES_FILE, "src/EntireJApplication.properties");
            CFProjectHelper.addFile(project, EJCFSwingPlugin.getDefault().getBundle(), SWING_APP_LAUNCHER, "src/org/entirej/ApplicationLauncher.java");
            CFProjectHelper.addEntireJBaseLibraries(project);

            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(SwingRuntimeClasspathContainer.ID));
            EmptyClientFrameworkProvider.addGeneratorFiles(project, monitor);
            CFProjectHelper.refreshProject(project, monitor);
            final IFile file = project.getProject().getFile("src/EntireJApplication.properties");
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
                    try
                    {
                        IDE.openEditor(page, file, true);
                    }
                    catch (PartInitException e)
                    {
                        EJCoreLog.logException(e);
                    }
                }
            });
        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }

    public String getProviderName()
    {
        return "Swing Application Framework";
    }

    public String getProviderId()
    {
        return "org.entirej.framework.cf.swing";
    }

    public String getDescription()
    {
        return "Creates a project adding the Swing Application Framework and renderers.\nThe EntireJApplication.properties file will be pre-configured with references to the Swing Renderers";
    }
    
    public IClasspathAttribute[] getClasspathAttributes()
    {
        return new IClasspathAttribute[0];
    }

}
