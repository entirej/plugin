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
package org.entirej.ide.core.project;

import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.spi.EJPropertiesValidateProvider;

public class EJPropertiesConsistencyChecker extends IncrementalProjectBuilder
{
    private static IProject[] EMPTY_LIST   = new IProject[0];

    private int               PROPERTIES   = 0x1;
    private static boolean    DEBUG        = false;

    private SelfVisitor       fSelfVisitor = new SelfVisitor();

    @Override
    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException
    {
        if (monitor.isCanceled())
            return EMPTY_LIST;
        IProject project = getProject();

        int type = getDeltaType(project);
        if ((type & PROPERTIES) != 0)
        {

            validateProject(type, monitor);
        }

        return EMPTY_LIST;
    }

    private void validateProject(int type, IProgressMonitor monitor)
    {
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EJPropertiesValidateProvider.EXTENSION_POINT_ID);
        monitor.beginTask("Validating application.ejprop ", config.length + 1);
        IProject project = getProject();
        IFile file = EJProject.getPropertiesFile(project);

        try
        {
            if (!validateProjectStructure(project, file, new SubProgressMonitor(monitor, 1)))
                return;

            SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, config.length);
            for (IConfigurationElement element : config)
            {
                if (monitor.isCanceled())
                    return;

                final Object impl = element.createExecutableExtension("class");
                if (impl instanceof EJPropertiesValidateProvider)
                {
                    if (DEBUG)
                        EJCoreLog.logWarnningMessage("DEBUG: Build called # " + impl.getClass().getName());
                    ((EJPropertiesValidateProvider) impl).validate(file, new SubProgressMonitor(subProgressMonitor, 1));
                }
            }
            subProgressMonitor.done();
        }
        catch (CoreException ex)
        {
            EJCoreLog.log(ex);
        }
        finally
        {
            monitor.done();
        }

    }

    private boolean validateProjectStructure(IProject project, IFile file, IProgressMonitor monitor)
    {
        // clear markers from project
        try
        {
            cleanProblems(project, IResource.DEPTH_ZERO);
        }
        catch (CoreException e)
        {
        }
        try
        {
            if (file == null || !file.exists())
            {
                // Will place a marker on the project if the
                // application.ejprop does not exist

                if (DEBUG)
                    EJCoreLog.logWarnningMessage("DEBUG: adding maker of  application.ejprop does not exist");
                IMarker marker = EJMarkerFactory.createMarker(project);
                EJMarkerFactory.addErrorMessage(marker, "application.ejprop does not exist");

                monitor.done();
                return false;
            }
            else
            {
                cleanProblems(file, IResource.DEPTH_ZERO);
            }
        }
        catch (CoreException e)
        {
            EJCoreLog.logException(e);
        }
        monitor.done();
        return true;
    }

    private int getDeltaType(IProject project) throws CoreException
    {
        IResourceDelta delta = getDelta(project);

        // always do a build of the project if a full build or an unspecified
        // change has occurred
        if (delta == null)
        {
            if (DEBUG)
            {
                System.out.println("Project [" + getProject().getName() + "] - full build"); //$NON-NLS-1$ //$NON-NLS-2$
            }
            int type = 0;
            return type | PROPERTIES;
        }

        // check if any "significant" files have been changed/added/removed
        // and build a subset or all manifest files accordingly
        fSelfVisitor.reset();
        delta.accept(fSelfVisitor);
        int type = fSelfVisitor.getType();

        return type;
    }

    protected void clean(IProgressMonitor monitor) throws CoreException
    {
        SubMonitor localmonitor = SubMonitor.convert(monitor, NLS.bind("Cleaning {0}", getProject().getName()), 1);
        try
        {
            // clean problem markers on the project
            cleanProblems(getProject(), IResource.DEPTH_ZERO);

            IFile file = EJProject.getPropertiesFile(getProject());
            cleanProblems(file, IResource.DEPTH_ZERO);

            localmonitor.worked(1);
        }
        finally
        {
            localmonitor.done();
        }
    }

    private void cleanProblems(IResource resource, int depth) throws CoreException
    {
        if (resource.exists())
        {
            resource.deleteMarkers(EJMarkerFactory.MARKER_ID, true, depth);
        }
    }

    class SelfVisitor implements IResourceDeltaVisitor
    {
        int type = 0;

        public boolean visit(IResourceDelta delta) throws CoreException
        {
            if (delta != null && type != (PROPERTIES))
            {
                int kind = delta.getKind();
                if (kind == IResourceDelta.ADDED || kind == IResourceDelta.REMOVED)
                {
                    type = type | PROPERTIES;
                    if (DEBUG)
                    {
                        System.out.print("Needs to rebuild project [" + getProject().getName() + "]: "); //$NON-NLS-1$ //$NON-NLS-2$
                        System.out.print(delta.getResource().getProjectRelativePath().toString());
                        System.out.print(" - "); //$NON-NLS-1$
                        System.out.println(kind == IResourceDelta.ADDED ? "added" : "removed"); //$NON-NLS-1$ //$NON-NLS-2$
                    }
                    return false;
                }
                IResource resource = delta.getResource();
                // by ignoring derived resources we should scale a bit better.
                if (resource.isDerived())
                    return false;
                if (resource.getType() == IResource.FILE)
                {
                    IFile file = (IFile) resource;
                    IProject project = file.getProject();

                    if (file.equals(EJProject.getPropertiesFile(project)))
                    {
                        type = type | PROPERTIES;
                        if (DEBUG)
                        {
                            System.out.print("Needs to rebuild project [" + getProject().getName() + "]: "); //$NON-NLS-1$ //$NON-NLS-2$
                            System.out.print(delta.getResource().getProjectRelativePath().toString());
                            System.out.println(" - changed"); //$NON-NLS-1$
                        }

                    }
                }

            }
            return type != (type | PROPERTIES);
        }

        public int getType()
        {
            return type;
        }

        public void reset()
        {
            type = 0;
        }
    }
}
