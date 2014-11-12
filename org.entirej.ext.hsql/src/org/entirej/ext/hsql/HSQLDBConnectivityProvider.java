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
package org.entirej.ext.hsql;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.entirej.ext.hsql.lib.HSQLRuntimeClasspathContainer;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.EntirejPluginPropertiesEnterpriseEdition;
import org.entirej.framework.plugin.framework.properties.reader.EntireJPropertiesReader;
import org.entirej.framework.plugin.framework.properties.reader.EntireJRendererReader;
import org.entirej.framework.plugin.framework.properties.writer.EntireJPropertiesWriter;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.framework.plugin.preferences.FieldEditorOverlayPage;
import org.entirej.framework.plugin.reports.EJPluginEntireJReportProperties;
import org.entirej.framework.plugin.reports.EntirejReportPropertiesUtils;
import org.entirej.framework.plugin.reports.writer.EntireJReportPropertiesWriter;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.project.EJProject;
import org.entirej.ide.core.project.EJReportProject;
import org.entirej.ide.core.spi.ClientFrameworkProvider;
import org.entirej.ide.core.spi.DBConnectivityProvider;

public class HSQLDBConnectivityProvider implements DBConnectivityProvider
{
    private static final String HSQL_CONNECTION_FILE = "/templates/hsqlOptions/EmbeddedReportConnectionFactory.java";
    private static final String HSQL_CONNECTION_DB   = "/templates/hsqlOptions/demo.h2.db";

    public void addEntireJReportNature(IConfigurationElement configElement, final IJavaProject project, final IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");

            CFProjectHelper.addFile(project, EJExtHSQLPlugin.getDefault().getBundle(), HSQL_CONNECTION_FILE,
                    "src/org/entirej/db/connection/EmbeddedReportConnectionFactory.java");
            CFProjectHelper.addFile(project, EJExtHSQLPlugin.getDefault().getBundle(), HSQL_CONNECTION_DB, "src/db/demo.h2.db");

            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(HSQLRuntimeClasspathContainer.ID, true));

            CFProjectHelper.refreshProject(project, monitor);

            // change connection file
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    IProject iProject = project.getProject();
                    try
                    {

                        // auto config project to db
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(FieldEditorOverlayPage.USEPROJECTSETTINGS), "true");
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_DBDRIVER), "org.h2.Driver");
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_URL),
                                String.format("jdbc:h2:%s/demo", iProject.getFile("src/db").getRawLocation().toFile().getAbsolutePath()));
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_USERNAME), "SA");
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_PASSWORD), "");
                    }
                    catch (CoreException e1)
                    {
                        // ignore
                    }
                    IFile pfile = EJReportProject.getPropertiesFile(project.getProject());
                    if (pfile != null)
                    {
                        EJPluginEntireJReportProperties entirejProperties;
                        try
                        {
                            entirejProperties = EntirejReportPropertiesUtils.retrieveEntirejProperties(project);
                            entirejProperties.setConnectionFactoryClassName("org.entirej.db.connection.EmbeddedConnectionFactory");
                            EntireJReportPropertiesWriter saver = new EntireJReportPropertiesWriter();
                            saver.saveEntireJProperitesFile(entirejProperties, pfile, monitor);
                        }
                        catch (CoreException e)
                        {
                            e.printStackTrace();
                        }

                    }

                }
            });
        }
        catch (Exception e)
        {
            EJCoreLog.logException(e);
        }
    }

    public void addEntireJNature(ClientFrameworkProvider cf, IConfigurationElement configElement, final IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");

            CFProjectHelper.addFile(project, EJExtHSQLPlugin.getDefault().getBundle(), HSQL_CONNECTION_FILE,
                    "src/org/entirej/db/connection/EmbeddedConnectionFactory.java");
            CFProjectHelper.addFile(project, EJExtHSQLPlugin.getDefault().getBundle(), HSQL_CONNECTION_DB, "src/db/demo.h2.db");

            CFProjectHelper.addToClasspath(project,
                    JavaCore.newContainerEntry(HSQLRuntimeClasspathContainer.ID, new IAccessRule[0], cf.getClasspathAttributes(), true));

            CFProjectHelper.refreshProject(project, monitor);

            // change connection file
            Display.getDefault().asyncExec(new Runnable()
            {
                public void run()
                {
                    IProject iProject = project.getProject();
                    try
                    {

                        // auto config project to db
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(FieldEditorOverlayPage.USEPROJECTSETTINGS), "true");
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_DBDRIVER), "org.h2.Driver");
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_URL),
                                String.format("jdbc:h2:%s/demo", iProject.getFile("src/db").getRawLocation().toFile().getAbsolutePath()));
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_USERNAME), "SA");
                        iProject.setPersistentProperty(EJPropertyRetriever.createQualifiedName(EntireJFrameworkPlugin.P_PASSWORD), "");
                    }
                    catch (CoreException e1)
                    {
                        // ignore
                    }
                    IFile pfile = EJProject.getPropertiesFile(iProject);
                    if (pfile != null)
                    {
                        EntirejPluginPropertiesEnterpriseEdition entireJProperties = new EntirejPluginPropertiesEnterpriseEdition(project);

                        try
                        {

                            EntireJPropertiesReader.readProperties(entireJProperties, project, pfile.getContents(), pfile, EJProject.getRendererFile(iProject));
                        }
                        catch (Exception exception)
                        {
                            EJCoreLog.logException(exception);
                        }
                        entireJProperties.setConnectionFactoryClassName("org.entirej.db.connection.EmbeddedConnectionFactory");
                        EntireJPropertiesWriter saver = new EntireJPropertiesWriter();
                        saver.saveEntireJProperitesFile(entireJProperties, pfile, new NullProgressMonitor());
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
        return "H2  Connection Support";
    }

    public String getProviderId()
    {
        return "org.entirej.framework.dbn.hsql";
    }

    public String getDescription()
    {
        StringBuilder builder = new StringBuilder();
        builder.append("Adds the H2 database driver and an H2 specific connection settings");
        builder.append("\n");
        return builder.toString();
    }

}
