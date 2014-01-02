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
package org.entirej.framework.plugin.utils;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;

public class EJPluginLogger
{
    public static void logInfo(final EntireJFrameworkPlugin plugin, final String message)
    {
        plugin.getLog().log(new Status(Status.INFO, plugin.getPluginId(), Status.OK, message, null));
        
        Display.getDefault().asyncExec(new Runnable()
        {
            
            @Override
            public void run()
            {
                MessageDialog.openInformation(plugin.getActiveWorkbenchShell(), "Information", message);
                
            }
        });
    }
    
    public static void logError(final EntireJFrameworkPlugin plugin, final String message, Exception e)
    {
        plugin.getLog().log(new Status(Status.ERROR, plugin.getPluginId(), Status.OK, message, e));
        
        Display.getDefault().asyncExec(new Runnable()
        {
            
            @Override
            public void run()
            {
                MessageDialog.openError(plugin.getActiveWorkbenchShell(), "Error", message);
                
            }
        });
    }
    
    public static void logError(final EntireJFrameworkPlugin plugin, final String message)
    {
        plugin.getLog().log(new Status(Status.ERROR, plugin.getPluginId(), Status.OK, message, null));
        Display.getDefault().asyncExec(new Runnable()
        {
            
            @Override
            public void run()
            {
                MessageDialog.openError(plugin.getActiveWorkbenchShell(), "Error", message);
                
            }
        });
        
    }
}
