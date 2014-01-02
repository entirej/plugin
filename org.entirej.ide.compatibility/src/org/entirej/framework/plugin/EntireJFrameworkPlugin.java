/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin;

import java.io.File;
import java.io.IOException;
import java.net.URL;

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class EntireJFrameworkPlugin extends AbstractUIPlugin
{
    public static String                  P_DBDRIVER    = "DB_DRIVER";
    public static String                  P_URL         = "DB_URL";
    public static String                  P_SCHEMA      = "DB_SCHEMA";
    public static String                  P_USERNAME    = "DB_USERNAME";
    public static String                  P_PASSWORD    = "DB_PASSWORD";
    
    public static String                  P_FORM_HEIGHT = "FORM_HEIGHT";
    public static String                  P_FORM_WIDTH  = "FORM_WIDTH";
    
    // The plug-in ID
    public static final String            PLUGIN_ID     = "EntireJFrameworkPlugin";
    
    // The shared instance
    private static EntireJFrameworkPlugin plugin;
    
    /**
     * The constructor
     */
    public EntireJFrameworkPlugin()
    {
        
    }
    
    public String getPluginId()
    {
        return PLUGIN_ID;
    }
    
    public File getFileInPlugin(IPath path)
    {
        try
        {
            Bundle bundle = getSharedInstance().getBundle();
            
            URL installURL = new URL(bundle.getEntry("/"), path.toString());
            URL localURL = FileLocator.toFileURL(installURL);
            return new File(localURL.getFile());
        }
        catch (IOException e)
        {
            return null;
        }
    }
    
    @Override
    public void start(BundleContext context) throws Exception
    {
        // TODO Auto-generated method stub
        super.start(context);
        plugin = this;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext
     * )
     */
    public void stop(BundleContext context) throws Exception
    {
        plugin = null;
        
        super.stop(context);
    }
    
    /**
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static EntireJFrameworkPlugin getSharedInstance()
    {
        return plugin;
    }
    
    public IWorkbenchPage getActivePage()
    {
        IWorkbenchWindow window = getWorkbench().getActiveWorkbenchWindow();
        if (window == null) return null;
        return window.getActivePage();
    }
    
    public IWorkbenchWindow getActiveWorkbenchWindow()
    {
        return getSharedInstance().getWorkbench().getActiveWorkbenchWindow();
    }
    
    public static boolean isDebug(String option)
    {
        String value = Platform.getDebugOption(option);
        return (value != null && value.equalsIgnoreCase("true") ? true : false);
    }
    
    public static void log(IStatus status)
    {
        getSharedInstance().getLog().log(status);
    }
    
    /**
     * Writes the message to the plug-in's log
     * 
     * @param message
     *            the text to write to the log
     */
    public static void logError(String message, Throwable exception)
    {
        IStatus status = createErrorStatus(message, exception);
        getSharedInstance().getLog().log(status);
    }
    
    /**
     * Writes the message to the plug-in's log
     * 
     * @param message
     *            the text to write to the log
     */
    public static void logInfo(String message)
    {
        IStatus status = createInfoStatus(message);
        getSharedInstance().getLog().log(status);
    }
    
    public static void log(Throwable exception)
    {
        getSharedInstance().getLog().log(createErrorStatus("Internal Error", exception));
    }
    
    /**
     * Returns a new <code>IStatus</code> for this plug-in
     */
    public static IStatus createInfoStatus(String message)
    {
        if (message == null)
        {
            message = "";
        }
        return new Status(Status.INFO, PLUGIN_ID, 0, message, null);
    }
    
    /**
     * Returns a new <code>IStatus</code> for this plug-in
     */
    public static IStatus createErrorStatus(String message, Throwable exception)
    {
        if (message == null)
        {
            message = "";
        }
        return new Status(Status.ERROR, PLUGIN_ID, 0, message, exception);
    }
    
    /**
     * Returns an image descriptor for the image file at the given plug-in
     * relative path
     * 
     * @param path
     *            the path
     * @return the image descriptor
     */
    public ImageDescriptor getImageDescriptor(String path)
    {
        return imageDescriptorFromPlugin(getPluginId(), path);
    }
    
    public IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }
    
    public Shell getActiveWorkbenchShell()
    {
        return getStandardDisplay().getActiveShell();
    }
    
    public static Display getStandardDisplay()
    {
        Display display;
        display = Display.getCurrent();
        if (display == null) display = Display.getDefault();
        return display;
    }
    
    public IWorkbenchPage getActiveWorkbenchPage()
    {
        return getActiveWorkbenchWindow().getActivePage();
    }
}
