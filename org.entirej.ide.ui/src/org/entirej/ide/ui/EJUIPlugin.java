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
package org.entirej.ide.ui;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.ISelectionService;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.core.project.EJReportProject;
import org.entirej.ide.ui.editors.AbstractEditor;
import org.osgi.framework.BundleContext;

public class EJUIPlugin extends AbstractUIPlugin implements IStartup
{
    private BundleContext      bundleContext;

    private static EJUIPlugin  plugin;

    public static final String VERSION = "1.1.0";

    public EJUIPlugin()
    {
        plugin = this;
    }

    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        this.bundleContext = context;
        
    }

    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    public BundleContext getBundleContext()
    {
        return bundleContext;
    }

    /**
     * Returns the shared instance.
     */
    public static EJUIPlugin getDefault()
    {
        return plugin;
    }

    public void earlyStartup()
    {
        // ignore
        
        final IWorkbench workbench = PlatformUI.getWorkbench();
         workbench.getDisplay().asyncExec(new Runnable() {
           public void run() {
             IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
             if (window != null) {
                 System.out.println("earlyStartup");
                 ISelectionService iSelectionService = window.getSelectionService();
                 iSelectionService.addPostSelectionListener(new ISelectionListener()
                 {
                     
                     public void selectionChanged(IWorkbenchPart part, ISelection selection)
                     {
                         if(selection instanceof IStructuredSelection) {
                             IStructuredSelection ss = (IStructuredSelection) selection;
                             Object firstElement = ss.getFirstElement();
                             
                             
                             
                             if(firstElement instanceof org.eclipse.jdt.core.IJavaElement ) {
                                 IProject pro = ((org.eclipse.jdt.core.IJavaElement) firstElement).getJavaProject().getProject();
                                 if(EJProject.hasPluginNature(pro))
                                 {
                                     AbstractEditor.autoPerspectiveSwitch(
                                             "org.entirej.ide.ui.perspective",
                                             EJUIPlugin.getDefault().getPreferenceStore(),
                                             "AbstractEJFormEditor.autoPerspectiveSwitch",
                                             "This Editor is associated with the EntireJ Form Perspective.\n\nIt is highly recommended to switch to that perspective when editing EntireJ Forms.\n\nDo you want to switch to the EntireJ Forms Perspective now?");
                                 
                                 }
                                 else  if(EJReportProject.hasPluginNature(pro)) {
                                         AbstractEditor.autoPerspectiveSwitch("org.entirej.ide.ui.report.perspective", EJUIPlugin.getDefault().getPreferenceStore(),
                                                 "AbstractEJReportEditor.autoPerspectiveSwitch",
                                                 "This Editor is associated with the EntireJ Report Perspective.\n\nIt is highly recommended to switch to that perspective when editing EntireJ Reports.\n\nDo you want to switch to the EntireJ Report Perspective now?");
                                     
                                     
                                 }
                             }
                             
                         }

                     }
                 });                

             }
           }
         });
    }

    public static String getID()
    {
        return getDefault().getBundle().getSymbolicName();
    }

    public static Display getStandardDisplay()
    {
        Display display;
        display = Display.getCurrent();
        if (display == null)
            display = Display.getDefault();
        return display;
    }

    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

    public static Shell getActiveWorkbenchShell()
    {
        IWorkbenchWindow window = getActiveWorkbenchWindow();
        if (window != null)
        {
            return window.getShell();
        }
        return null;
    }
}
