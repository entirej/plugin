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
package org.entirej.ide.ui.editors.descriptors;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceProxy;
import org.eclipse.core.resources.IResourceProxyVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.window.Window;
import org.eclipse.ui.dialogs.ResourceListSelectionDialog;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.ui.EJUIPlugin;

public abstract class AbstractProjectSrcFileDescriptor extends AbstractDescriptor<String>
{

    private final IJavaProjectProvider projectProvider;

    public AbstractProjectSrcFileDescriptor(IJavaProjectProvider projectProvider)
    {
        super(AbstractDescriptor.TYPE.REFERENCE);
        this.projectProvider = projectProvider;
    }

    public AbstractProjectSrcFileDescriptor(IJavaProjectProvider projectProvider, String lable)
    {
        this(projectProvider);
        setText(lable);

    }

    public AbstractProjectSrcFileDescriptor(IJavaProjectProvider projectProvider, String lable, String tooltip)
    {
        this(projectProvider, lable);
        setTooltip(tooltip);
    }

    @Override
    public String browseType()
    {
        List<IPackageFragmentRoot> elements = new ArrayList<IPackageFragmentRoot>();
        IJavaProject javaProject = projectProvider.getJavaProject();
        try
        {
            IPackageFragmentRoot[] roots = javaProject.getPackageFragmentRoots();
            for (int i = 0; i < roots.length; i++)
            {
                if (roots[i].getKind() == IPackageFragmentRoot.K_SOURCE)
                {
                    elements.add(roots[i]);

                }
            }
        }
        catch (JavaModelException e)
        {
            EJCoreLog.logException(e);
        }

        final List<IResource> resources = new ArrayList<IResource>();
        for (IPackageFragmentRoot root : elements)
        {
            try
            {
                IResource resource = root.getResource();
                if (resource instanceof IContainer)
                    ((IContainer) resource).accept(new IResourceProxyVisitor()
                    {
                        public boolean visit(IResourceProxy proxy)
                        {

                            if (proxy.isDerived())
                            {
                                return false;
                            }
                            int type = proxy.getType();
                            if ((IResource.FILE & type) != 0)
                            {

                                IResource res = proxy.requestResource();

                                resources.add(res);
                                return true;

                            }
                            if (type == IResource.FILE)
                            {
                                return false;
                            }
                            return true;
                        }
                    }, IResource.NONE);
            }
            catch (CoreException e)
            {
                // ignore
            }
        }

        ResourceListSelectionDialog dialog = new ResourceListSelectionDialog(EJUIPlugin.getActiveWorkbenchShell(), resources.toArray(new IResource[0]));

        dialog.setTitle("Select File");
        dialog.setMessage("Select a project file:");

        if (dialog.open() == Window.OK)
        {
            Object[] result = dialog.getResult();
            if (result != null && result.length > 0)
            {
                IResource resource = (IResource) result[0];

                String chosenPath = resource.getFullPath().toPortableString();

                // Return the chosen package without the source directory path

                for (IPackageFragmentRoot root : elements)
                {
                    String sourcePath = root.getPath().toPortableString();
                    if (chosenPath.startsWith(sourcePath))
                    {
                        chosenPath = chosenPath.replace(sourcePath, "");
                        break;
                    }
                }
                return chosenPath;
            }
        }

        return getValue();
    }

}
