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
package org.entirej.ide.cf.rwt;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.cf.EmptyClientFrameworkProvider;
import org.entirej.ide.core.spi.ClientFrameworkProvider;

public class RWTClientFrameworkProvider implements ClientFrameworkProvider
{

    private static final String RWT_PROJECT_PROPERTIES_FILE = "/templates/rwt/application.ejprop";
    private static final String RWT_PROJECT_RENDERER_FILE   = "/templates/rwt/renderers.ejprop";
    private static final String RWT_APP_LAUNCHER            = "/templates/rwt/ApplicationLauncher.java";
    private static final String RWT_POM                     = "/templates/rwt/pom.xml";
    private static final String RWT_WEB_DD                  = "/templates/rwt/web.xml";
    private static final String RWT_WEB_INDEX               = "/templates/rwt/index.html";

    public void addEntireJNature(IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");
            CFProjectHelper.configureMavenProject(project, monitor);
            Map<String, String> pomParameters = Map.of("%PROJECT_NAME%",
                    CFProjectHelper.escapeXml(project.getElementName()));
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_POM, "pom.xml",
                    pomParameters);
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_PROJECT_PROPERTIES_FILE, "src/application.ejprop");
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_PROJECT_RENDERER_FILE, "src/renderers.ejprop");
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_APP_LAUNCHER, "src/org/entirej/ApplicationLauncher.java");
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_WEB_DD, "WebContent/WEB-INF/web.xml");
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_WEB_INDEX, "WebContent/index.html");

            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), getComponentSource(project), ".settings/org.eclipse.wst.common.component");
            CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), getFactesSource(project),
                    ".settings/org.eclipse.wst.common.project.facet.core.xml");

            IClasspathAttribute[] attributes = getClasspathAttributes();
            CFProjectHelper.addEntireJBaseLibraries(project, attributes);
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(new Path("org.eclipse.jst.j2ee.internal.web.container")));
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(new Path("org.eclipse.jst.j2ee.internal.module.container")));

            addWebNatures(project);

            EmptyClientFrameworkProvider.addGeneratorFiles(project, monitor);
            CFProjectHelper.refreshProject(project, monitor);
            final IFile file = project.getProject().getFile("src/application.ejprop");
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

    private byte[] getComponentSource(IJavaProject project)
    {
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?><project-modules id=\"moduleCoreId\" project-version=\"1.5.0\">");
        String name = CFProjectHelper.escapeXml(project.getProject().getName());
        builder.append(String.format("<wb-module deploy-name=\"%s\">", name));
        builder.append("<wb-resource deploy-path=\"/\" source-path=\"/WebContent\" tag=\"defaultRootSource\"/>");
        builder.append("<wb-resource deploy-path=\"/WEB-INF/classes\" source-path=\"/src\"/>");
        builder.append(String.format(" <property name=\"context-root\" value=\"%s\"/>", name));
        builder.append(String.format("<property name=\"java-output-path\" value=\"/%s/target/classes\"/>", name));
        builder.append("</wb-module>");
        builder.append("</project-modules>");

        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private byte[] getFactesSource(IJavaProject project)
    {
        String option = project.getOption("org.eclipse.jdt.core.compiler.source", true);
        if (option == null)
        {
            option = "21";
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        builder.append("<faceted-project>");
        builder.append("<fixed facet=\"wst.jsdt.web\"/>");
        builder.append(String.format("<installed facet=\"java\" version=\"%s\"/>", option));
        builder.append("<installed facet=\"jst.web\" version=\"6.0\"/>");
        builder.append("<installed facet=\"wst.jsdt.web\" version=\"1.0\"/>");
        builder.append("</faceted-project>");
        return builder.toString().getBytes(StandardCharsets.UTF_8);
    }

    private void addWebNatures(IJavaProject project)
    {
        try
        {
            IProjectDescription description = project.getProject().getDescription();
            Set<String> newNatures = new LinkedHashSet<>(Arrays.asList(description.getNatureIds()));
            newNatures.add("org.eclipse.m2e.core.maven2Nature");
            newNatures.add("org.eclipse.jdt.core.javanature");
            newNatures.add("org.eclipse.jem.workbench.JavaEMFNature");
            newNatures.add("org.eclipse.wst.common.modulecore.ModuleCoreNature");
            newNatures.add("org.eclipse.wst.common.project.facet.core.nature");
            newNatures.add("org.eclipse.wst.jsdt.core.jsNature");
            description.setNatureIds(newNatures.toArray(new String[0]));
            project.getProject().setDescription(description, null);
        }
        catch (CoreException e)
        {
            EJCoreLog.logException(e);
        }

    }

    public String getProviderName()
    {
        return "Eclipse RAP Application Framework";
    }

    public String getProviderId()
    {
        return "org.entirej.framework.cf.rwt_rap";
    }

    public String getDescription()
    {
        return "Creates a project adding the Eclipse RAP Application Framework and renderers.\nThe application.ejprop file will be pre-configured with references to the RAP Renderers";
    }

    public IClasspathAttribute[] getClasspathAttributes()
    {

        return new IClasspathAttribute[] { new IClasspathAttribute()
        {

            public String getValue()
            {
                return "/WEB-INF/lib";
            }

            public String getName()
            {
                return "org.eclipse.jst.component.dependency";
            }
        } };
    }

}
