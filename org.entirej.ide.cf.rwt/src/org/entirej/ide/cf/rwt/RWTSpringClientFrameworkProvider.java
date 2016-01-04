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
package org.entirej.ide.cf.rwt;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IAccessRule;
import org.eclipse.jdt.core.IClasspathAttribute;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.entirej.ide.cf.rwt.lib.RWTCFRuntimeClasspathContainer;
import org.entirej.ide.cf.rwt.lib.RWTCoreRuntimeClasspathContainer;
import org.entirej.ide.cf.rwt.lib.rap.RWTRapRuntimeClasspathContainer;
import org.entirej.ide.cf.rwt.lib.spring.RWTSpringRuntimeClasspathContainer;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.cf.EmptyClientFrameworkProvider;
import org.entirej.ide.core.spi.ClientFrameworkProvider;

public class RWTSpringClientFrameworkProvider implements ClientFrameworkProvider
{

    private static final String RWT_PROJECT_PROPERTIES_FILE = "/templates/rwt/application.ejprop";
    private static final String RWT_PROJECT_RENDERER_FILE   = "/templates/rwt/renderers.ejprop";
    private static final String RWT_APP_LAUNCHER            = "/templates/rwt/ApplicationLauncher.java";
    private static final String RWT_WEB_DD                  = "/templates/rwt/web.xml";
    private static final String RWT_WEB_INDEX               = "/templates/rwt/index.html";

    public void addEntireJNature(IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor)
    {
        try
        {
            CFProjectHelper.verifySourceContainer(project, "src");
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
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(RWTCFRuntimeClasspathContainer.ID, new IAccessRule[0], attributes, true));
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(RWTCoreRuntimeClasspathContainer.ID, new IAccessRule[0], attributes, true));
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(RWTRapRuntimeClasspathContainer.ID, new IAccessRule[0], attributes, true));
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(RWTSpringRuntimeClasspathContainer.ID, new IAccessRule[0], attributes, true));
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(new Path("org.eclipse.jst.j2ee.internal.web.container")));
            CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(new Path("org.eclipse.jst.j2ee.internal.module.container")));

            addWebNeatures(project);

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
        String name = project.getProject().getName();
        builder.append(String.format("<wb-module deploy-name=\"%s\">", name));
        builder.append("<wb-resource deploy-path=\"/\" source-path=\"/WebContent\" tag=\"defaultRootSource\"/>");
        builder.append("<wb-resource deploy-path=\"/WEB-INF/classes\" source-path=\"/src\"/>");
        builder.append(String.format(" <property name=\"context-root\" value=\"%s\"/>", name));
        builder.append(String.format("<property name=\"java-output-path\" value=\"/%s/bin\"/>", name));
        builder.append("</wb-module>");
        builder.append("</project-modules>");

        return builder.toString().getBytes();
    }

    private byte[] getFactesSource(IJavaProject project)
    {
        String option = project.getOption("org.eclipse.jdt.core.compiler.source", true);
        if (option == null)
        {
            option = "1.6";// default
        }
        StringBuilder builder = new StringBuilder();
        builder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>");
        builder.append("<faceted-project>");
        builder.append("<fixed facet=\"wst.jsdt.web\"/>");
        builder.append(String.format("<installed facet=\"java\" version=\"%s\"/>", option));
        builder.append("<installed facet=\"jst.web\" version=\"3.0\"/>");
        builder.append("<installed facet=\"wst.jsdt.web\" version=\"1.0\"/>");
        builder.append("</faceted-project>");
        return builder.toString().getBytes();
    }

    private void addWebNeatures(IJavaProject project)
    {
        try
        {
            /*
             * <nature>org.eclipse.jem.workbench.JavaEMFNature</nature>
             * <nature>org
             * .eclipse.wst.common.modulecore.ModuleCoreNature</nature>
             * <nature>org.eclipse.wst.common.project.facet.core.nature</nature>
             * <nature>org.eclipse.wst.jsdt.core.jsNature</nature>
             */
            IProjectDescription description = project.getProject().getDescription();
            String[] natures = description.getNatureIds();
            List<String> newNatures = new ArrayList<String>(Arrays.asList(natures));
            newNatures.add("org.eclipse.jem.workbench.JavaEMFNature");
            newNatures.add("org.eclipse.wst.common.modulecore.ModuleCoreNature");
            newNatures.add("org.eclipse.wst.common.project.facet.core.nature");
            newNatures.add("org.eclipse.wst.jsdt.core.jsNature");
            description.setNatureIds(newNatures.toArray(new String[0]));
            project.getProject().setDescription(description, null);
        }
        catch (CoreException e)
        {
            e.printStackTrace();
        }

    }

    public String getProviderName()
    {
        return "Eclipse RAP Application Framework with Spring Security";
    }

    public String getProviderId()
    {
        return "org.entirej.framework.cf.rwt_rap_spring";
    }

    public String getDescription()
    {
        return "Creates a project adding the Eclipse RAP Application Framework and renderers with Spring Security extention.\nThe application.ejprop file will be pre-configured with references to the RAP Renderers and Spring Security will be pre-configured";
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
