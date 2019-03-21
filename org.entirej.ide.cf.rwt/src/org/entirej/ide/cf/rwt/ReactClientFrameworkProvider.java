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

import org.eclipse.core.resources.IFile;
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
import org.entirej.ide.cf.rwt.lib.RWTCoreRuntimeClasspathContainer;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.cf.CFProjectHelper;
import org.entirej.ide.core.cf.EmptyClientFrameworkProvider;
import org.entirej.ide.core.spi.ClientFrameworkProvider;

public class ReactClientFrameworkProvider implements ClientFrameworkProvider {

	private static final String RWT_PROJECT_PROPERTIES_FILE = "/templates/react/application.ejprop";
	private static final String RWT_PROJECT_RENDERER_FILE = "/templates/react/renderers.ejprop";
	private static final String RWT_APP_LAUNCHER = "/templates/react/ApplicationLauncher.java";

	public void addEntireJNature(IConfigurationElement configElement, IJavaProject project, IProgressMonitor monitor) {
		try {
			CFProjectHelper.verifySourceContainer(project, "src");
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_PROJECT_PROPERTIES_FILE,
					"src/application.ejprop");
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_PROJECT_RENDERER_FILE,
					"src/renderers.ejprop");
			CFProjectHelper.addFile(project, EJCFRwtPlugin.getDefault().getBundle(), RWT_APP_LAUNCHER,
					"src/org/entirej/ApplicationLauncher.java");

			IClasspathAttribute[] attributes = getClasspathAttributes();
			CFProjectHelper.addEntireJBaseLibraries(project, attributes);
			CFProjectHelper.addToClasspath(project, JavaCore.newContainerEntry(RWTCoreRuntimeClasspathContainer.ID,
					new IAccessRule[0], attributes, true));
			CFProjectHelper.addToClasspath(project, JavaCore.newProjectEntry(new Path("/entirej-react-sb-def"), false));
			CFProjectHelper.addToClasspath(project, JavaCore.newProjectEntry(new Path("/entirej-react-sb"), false));

			EmptyClientFrameworkProvider.addGeneratorFiles(project, monitor);
			CFProjectHelper.refreshProject(project, monitor);
			final IFile file = project.getProject().getFile("src/application.ejprop");
			Display.getDefault().asyncExec(new Runnable() {
				public void run() {
					IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						IDE.openEditor(page, file, true);
					} catch (PartInitException e) {
						EJCoreLog.logException(e);
					}
				}
			});
		} catch (Exception e) {
			EJCoreLog.logException(e);
		}
	}

	public String getProviderName() {
		return "React.js Application Framework";
	}

	public String getProviderId() {
		return "org.entirej.framework.cf.react";
	}

	public String getDescription() {
		return "Creates a project adding the React.js Application Framework and renderers.\nThe application.ejprop file will be pre-configured with references to the React.js Renderers";
	}

	@Override
	public IClasspathAttribute[] getClasspathAttributes() {
		return new IClasspathAttribute[0];
	}

}
