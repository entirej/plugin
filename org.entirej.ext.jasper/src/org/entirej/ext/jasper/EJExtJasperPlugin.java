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
package org.entirej.ext.jasper;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EJExtJasperPlugin extends AbstractUIPlugin implements IStartup
{
    private BundleContext          bundleContext;

    public static final String     ID = "org.entirej.ext.jasper";

    private static EJExtJasperPlugin plugin;

    public EJExtJasperPlugin()
    {
        plugin = this;
    }

    public void start(BundleContext context) throws Exception
    {
        super.start(context);
        this.bundleContext = context;
    }

    public void stop(BundleContext context) throws Exception
    {
        super.stop(context);
        plugin = null;
    }

    public BundleContext getBundleContext()
    {
        return bundleContext;
    }

    /**
     * Returns the shared instance.
     */
    public static EJExtJasperPlugin getDefault()
    {
        return plugin;
    }

    public void earlyStartup()
    {
        // ignore
    }

    public static String getID()
    {
        return getDefault().getBundle().getSymbolicName();
    }

    public static Display getStandardDisplay()
    {
        Display display;
        display = Display.getCurrent();
        if (display == null)
            display = Display.getDefault();
        return display;
    }

    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public static IWorkbenchWindow getActiveWorkbenchWindow()
    {
        return getDefault().getWorkbench().getActiveWorkbenchWindow();
    }

}
