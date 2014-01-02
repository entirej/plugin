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
package org.entirej.ide.ui.editors.descriptors;

import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.swt.widgets.Control;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.utils.JavaAccessUtils;
import org.entirej.ide.ui.utils.PackageAssistProvider;

public abstract class AbstractPackageDescriptor extends AbstractDescriptor<String>
{

    private final IJavaProjectProvider projectProvider;

    public AbstractPackageDescriptor(IJavaProjectProvider projectProvider)
    {
        super(AbstractDescriptor.TYPE.REFERENCE);
        this.projectProvider = projectProvider;
    }

    public AbstractPackageDescriptor(IJavaProjectProvider projectProvider, String lable)
    {
        this(projectProvider);
        setText(lable);

    }

    public AbstractPackageDescriptor(IJavaProjectProvider projectProvider, String lable, String tooltip)
    {
        this(projectProvider, lable);
        setTooltip(tooltip);
    }

    /*
     * @Override public boolean hasLableLink() { return true; }
     * 
     * @Override public String lableLinkActivator() { return
     * JavaAccessUtils.findOrCreateClass(getValue(),
     * projectProvider.getJavaProject().getProject(), baseClass); }
     */

    protected String packageToPath(String pkg)
    {
        if (pkg != null)
        {
            return pkg.replaceAll("\\.", "/");
        }
        return pkg;
    }

    protected String pathTopackage(String path)
    {
        if (path != null)
        {
            return path.replaceAll("/", ".");
        }
        return path;
    }

    @Override
    public void addEditorAssist(Control control)
    {
        PackageAssistProvider.createTypeAssist(control, projectProvider, 0);
    }

    @Override
    public String browseType()
    {

        Object[] result = JavaAccessUtils.choosePackage(EJUIPlugin.getActiveWorkbenchShell(), projectProvider.getJavaProject(), getValue(), false);
        if (result.length > 0 && result[0] instanceof IPackageFragment)
        {
            IPackageFragment fragment = (IPackageFragment) result[0];
            return fragment.getElementName();
        }

        return getValue();
    }

}
