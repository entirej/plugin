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
package org.entirej.ext.mysql;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.entirej.ext.mysql.lib.MySQLRuntimeClasspathContainer;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.spi.ClientFrameworkProvider;
import org.entirej.ide.core.spi.DBConnectivityProvider;

public class MySQLDBConnectivityProvider implements DBConnectivityProvider
{
    private static final String MYSQL_CONNECTION_FILE = "/templates/mysqlOptions/Connection.properties";

    public void addEntireJNature(ClientFrameworkProvider cf, IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");

            CFProjectHelper.addFile(project, EJExtMySQLPlugin.getDefault().getBundle(), MYSQL_CONNECTION_FILE, "src/Connection.properties");

            CFProjectHelper.addToClasspath(project,
                    JavaCore.newContainerEntry(MySQLRuntimeClasspathContainer.ID, new IAccessRule[0], cf.getClasspathAttributes(), true));

            CFProjectHelper.refreshProject(project, monitor);
            final IFile file = project.getProject().getFile("src/Connection.properties");
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
    public void addEntireJReportNature( IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");
            
            CFProjectHelper.addFile(project, EJExtMySQLPlugin.getDefault().getBundle(), MYSQL_CONNECTION_FILE, "src/Connection.properties");
            
            CFProjectHelper.addToClasspath(project,
                    JavaCore.newContainerEntry(MySQLRuntimeClasspathContainer.ID, true));
            
            CFProjectHelper.refreshProject(project, monitor);
            final IFile file = project.getProject().getFile("src/Connection.properties");
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
        return "MySQL Connection Support";
    }

    public String getProviderId()
    {
        return "org.entirej.framework.dbn.mysql";
    }

    public String getDescription()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Adds the MySQL database driver and an MySQL specific connection settings");
        builder.append("\n");
        return builder.toString();
    }

}
