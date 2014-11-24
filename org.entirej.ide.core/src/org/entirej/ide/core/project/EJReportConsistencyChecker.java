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
package org.entirej.ide.core.project;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.osgi.util.NLS;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.spi.EJReportValidateProvider;

public class EJReportConsistencyChecker extends IncrementalProjectBuilder
{
    class DeltaVisitor implements IResourceDeltaVisitor
    {
        private IProgressMonitor monitor;

        public DeltaVisitor(IProgressMonitor monitor)
        {
            this.monitor = monitor;
        }

        public boolean visit(IResourceDelta delta)
        {
            IResource resource = delta.getResource();

            if (resource instanceof IProject)
                return isInterestingProject((IProject) resource);

            if (resource instanceof IFolder)
                return true;

            if (resource instanceof IFile)
            {

                // see if this is it
                IFile candidate = (IFile) resource;
                if (isReportFile(candidate))
                {
                    // That's it, but only check it if it has been added or
                    // changed
                    if (delta.getKind() != IResourceDelta.REMOVED)
                    {
                        try
                        {
                            candidate.deleteMarkers(EJMarkerFactory.MARKER_ID, true, IResource.DEPTH_ZERO);
                        }
                        catch (CoreException e)
                        {
                            EJCoreLog.log(e);
                        }
                        validateFile(candidate, getValidateProviders(), monitor);
                    }
                    
                }
            }
            return false;
        }
    }

    List<EJReportValidateProvider> getValidateProviders()
    {
        IConfigurationElement[] config = Platform.getExtensionRegistry().getConfigurationElementsFor(EJReportValidateProvider.EXTENSION_POINT_ID);
        List<EJReportValidateProvider> providers = new ArrayList<EJReportValidateProvider>(config.length);
        for (IConfigurationElement element : config)
        {

            Object impl;
            try
            {
                impl = element.createExecutableExtension("class");
                if (impl instanceof EJReportValidateProvider)
                {

                    providers.add(((EJReportValidateProvider) impl));
                }
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }

        }
        return providers;
    }

    protected IProject[] build(int kind, Map<String, String> args, IProgressMonitor monitor) throws CoreException
    {
        IResourceDelta delta = null;
        if (kind != FULL_BUILD)
            delta = getDelta(getProject());

        if (delta == null || kind == FULL_BUILD)
        {
            if (isInterestingProject(getProject()))
            {
                clean(monitor);
                IJavaProject project = JavaCore.create(getProject());
                // make sure it is refresh before build again
                EJPluginEntireJClassLoader.reload(project);
                IPackageFragmentRoot[] packageFragmentRoots = project.getPackageFragmentRoots();

                for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots)
                {
                    if (iPackageFragmentRoot.getResource() instanceof IContainer)
                        validateReportsIn((IContainer) iPackageFragmentRoot.getResource(), getValidateProviders(), monitor);
                }
            }
        }
        else
        {
            delta.accept(new DeltaVisitor(monitor));
        }
        return new IProject[0];
    }

    protected void clean(IProgressMonitor monitor) throws CoreException
    {
        SubMonitor localmonitor = SubMonitor.convert(monitor, NLS.bind("Cleaning EJ Reports in {0}", getProject().getName()), 1);
        try
        {
            // clean existing markers on schema files
            cleanReportsIn(getProject(), localmonitor);
            localmonitor.worked(1);
        }
        finally
        {
            localmonitor.done();
        }
    }

    private void cleanReportsIn(IContainer container, IProgressMonitor monitor) throws CoreException
    {
        if (monitor.isCanceled())
        {
            throw new OperationCanceledException();
        }
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            IResource member = members[i];
            if (member instanceof IContainer)
                cleanReportsIn((IContainer) member, monitor);
            else if (member instanceof IFile && isReportFile((IFile) member))
            {
                member.deleteMarkers(EJMarkerFactory.MARKER_ID, true, IResource.DEPTH_ZERO);
            }
        }
    }

    private boolean isInterestingProject(IProject project)
    {
        return EJReportProject.hasPluginNature(project);
    }

    private void validateFile(IFile file, List<EJReportValidateProvider> providers, IProgressMonitor monitor)
    {
        if (providers.isEmpty())
            return;

        try
        {
            // try to ignore outpu path
            IJavaProject project = JavaCore.create(file.getProject());
            IPath outputLocation = project.getOutputLocation();
            if (outputLocation.isPrefixOf(file.getFullPath()))
                return;
        }
        catch (JavaModelException e)
        {
            // ignore
        }
        IFile propFile = EJReportProject.getPropertiesFile(file.getProject());
        if (propFile == null || !propFile.exists())
        {
            return;
        }

        String message = NLS.bind("Validating {0} ...", file.getFullPath().toString());
        monitor.subTask(message);

        SubProgressMonitor subProgressMonitor = new SubProgressMonitor(monitor, providers.size());
        for (EJReportValidateProvider element : providers)
        {
            if (monitor.isCanceled())
                return;

            element.validate(file, new SubProgressMonitor(subProgressMonitor, 1));
        }
        subProgressMonitor.done();
        monitor.subTask(" Updating ...");
        monitor.done();
    }

    private void validateReportsIn(IContainer container, List<EJReportValidateProvider> providers, IProgressMonitor monitor) throws CoreException
    {
        if (providers.isEmpty())
            return;
        monitor.subTask("Compiling EJ Reports...");
        IResource[] members = container.members();
        for (int i = 0; i < members.length; i++)
        {
            IResource member = members[i];
            if (member instanceof IContainer)
                validateReportsIn((IContainer) member, providers, monitor);
            else if (member instanceof IFile && isReportFile((IFile) member))
            {
                validateFile((IFile) member, providers, monitor);
            }
        }
        monitor.done();
    }

    private boolean isReportFile(IFile file)
    {
        return "ejreport".equalsIgnoreCase(file.getFileExtension()) ;
    }

   
}
