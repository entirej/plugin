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
package org.entirej.ext.oracle;

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
import org.entirej.ext.oracle.lib.OracleRuntimeClasspathContainer;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.spi.ClientFrameworkProvider;
import org.entirej.ide.core.spi.DBConnectivityProvider;

public class OracleDBConnectivityProvider implements DBConnectivityProvider
{
    private static final String ORACLE_CONNECTION_FILE        = "/templates/oracleOptions/Connection.properties";
    private static final String ORACLE_STMT_EXECUTOR          = "/templates/oracleOptions/OracleStatementExecutor.java";
    private static final String ORACLE_TYPE_SERVICE_GENERATOR = "/templates/oracleOptions/OracleCollectionTypeServiceGenerator.java";
    private static final String ORACLE_TYPE_POJO_GENERATOR    = "/templates/oracleOptions/OraclePojoGenerator.java";
    private static final String ORACLE_STMT_PARAMETER_ARRAY   = "/templates/oracleOptions/EJStatementParameterArray.java";
    private static final String ORACLE_SQL_INPUT              = "/templates/oracleOptions/EJSQLInput.java";

    public void addEntireJNature(ClientFrameworkProvider cf, IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");

            CFProjectHelper.addFile(project, EJExtOraclePlugin.getDefault().getBundle(), ORACLE_CONNECTION_FILE, "src/Connection.properties");
            CFProjectHelper.addFile(project, EJExtOraclePlugin.getDefault().getBundle(), ORACLE_STMT_EXECUTOR, "src/org/entirej/OracleStatementExecutor.java");

            CFProjectHelper.addFile(project, EJExtOraclePlugin.getDefault().getBundle(), ORACLE_STMT_PARAMETER_ARRAY,
                    "src/org/entirej/EJStatementParameterArray.java");
            CFProjectHelper.addFile(project, EJExtOraclePlugin.getDefault().getBundle(), ORACLE_SQL_INPUT, "src/org/entirej/EJSQLInput.java");
            CFProjectHelper.addFile(project, EJExtOraclePlugin.getDefault().getBundle(), ORACLE_TYPE_POJO_GENERATOR,
                    "src/org/entirej/generators/OraclePojoGenerator.java");

            CFProjectHelper.addFile(project, EJExtOraclePlugin.getDefault().getBundle(), ORACLE_TYPE_SERVICE_GENERATOR,
                    "src/org/entirej/generators/OracleCollectionTypeServiceGenerator.java");

            CFProjectHelper.addToClasspath(project,
                    JavaCore.newContainerEntry(OracleRuntimeClasspathContainer.ID, new IAccessRule[0], cf.getClasspathAttributes(), true));

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
        return "Oracle Connection Support";
    }

    public String getProviderId()
    {
        return "org.entirej.framework.dbn.oracle";
    }

    public String getDescription()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Adds the Oracle database driver and an Oracle specific statement executor");
        builder.append("\n");
        builder.append("that can execute Oracle stored procedure which have Oracle collection types as arguments");
        return builder.toString();
    }

}
