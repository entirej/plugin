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
package org.entirej.framework.plugin.ui.wizards;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.widgets.Display;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;

public class Utils
{
    public static Class<?> createBlockServiceClass(IJavaProject javaProject, String blockName, String serviceClassName)
    {
        if (serviceClassName == null || serviceClassName.trim().length() == 0)
        {
            return null;
        }
        
        try
        {
            Class<?> serviceClass = EJPluginEntireJClassLoader.loadClass(javaProject, serviceClassName);
            return serviceClass;
        }
        catch (ClassNotFoundException e)
        {
            MessageDialog.openError(Display.getCurrent().getActiveShell(), "Error", "The service " + serviceClassName
                    + " cannot be found on the classpath. Please change the Service for block: " + blockName);
            return null;
        }
    }
}
