/*******************************************************************************
 *    Copyright (c) 2012 Mojave Innovations GmbH
 *    Mojave Innovations GmbH PROPRIETARY/CONFIDENTIAL PROPERTIES. Use is subject to license terms.
 *    You CANNOT use this software unless you receive a written permission from Mojave Innovations GmbH
 *******************************************************************************/
package org.entirej.ide.cf.swing;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public class EJCFSwingPlugin extends AbstractUIPlugin implements IStartup
{
    private BundleContext          bundleContext;

    public static final String     ID = "org.entirej.ide.core";

    private static EJCFSwingPlugin plugin;

    public EJCFSwingPlugin()
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
    public static EJCFSwingPlugin getDefault()
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
