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

import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
/**
 * The activator class controls the plug-in life cycle
 */
public class EntireJFrameworkPlugin implements BundleActivator
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
    private IPreferenceStore preferenceStore;
    private Bundle bundle;
    
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
    
   
    
    @Override
    public void start(BundleContext context) throws Exception
    {
        bundle = context.getBundle();
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
    
   
    
  
    public IPreferenceStore getPreferenceStore() {
        // Create the preference store lazily.
        if (preferenceStore == null) {
            preferenceStore = new ScopedPreferenceStore(InstanceScope.INSTANCE, bundle.getSymbolicName());

        }
        return preferenceStore;
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
    
    
    
    public IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }

    public Shell getActiveWorkbenchShell()
    {
        return Display.getCurrent().getActiveShell();
    }
    
   
  
    
}
