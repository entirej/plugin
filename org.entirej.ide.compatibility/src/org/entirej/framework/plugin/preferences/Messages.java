/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.preferences;

import java.util.ResourceBundle;

public class Messages
{
    
    private final static String   RESOURCE_BUNDLE  = "org.entirej.framework.plugin.preferences.Messages"; //$NON-NLS-1$
                                                                                                          
    private static ResourceBundle fgResourceBundle = null;
    
    private static boolean        notRead          = true;
    
    public Messages()
    {
    }
    
    public static ResourceBundle getResourceBundle()
    {
        if (notRead)
        {
            notRead = false;
            try
            {
                fgResourceBundle = ResourceBundle.getBundle(RESOURCE_BUNDLE);
            }
            catch (Exception e)
            {
            }
        }
        
        return fgResourceBundle;
    }
    
    public static String getString(String key)
    {
        try
        {
            return getResourceBundle().getString(key);
        }
        catch (Exception e)
        {
            return "!" + key + "!";//$NON-NLS-2$ //$NON-NLS-1$
        }
    }
}
