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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.jface.preference.IPreferenceStore;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;

public class EJPropertyRetriever
{
    private static final String PROPERTY_QUALIFIER = "org.entirej.framework.plugin.preferences.EntirejPreferencePage";
    
    public static String getValue(IProject project, String name)
    {
        IPreferenceStore store = EntireJFrameworkPlugin.getSharedInstance().getPreferenceStore();
        
        String value = null;
        if (useProjectSettings(project))
        {
            value = getProperty(project, name);
        }
        if (value != null) return value;
        return store.getString(name);
    }
    
    public static boolean isDefault(IProject project, String name)
    {
        String val = getValue(project, name);
        String defaultVal = EntireJFrameworkPlugin.getSharedInstance().getPreferenceStore().getDefaultString(name);
        
        if (defaultVal != null && val == null)
        {
            return false;
        }
        
        if (val != null && defaultVal == null)
        {
            return false;
        }
        
        if (defaultVal.equals(val))
        {
            return true;
        }
        
        return false;
    }
    
    public static boolean isDefault(String val, String name)
    {
        
        String defaultVal = EntireJFrameworkPlugin.getSharedInstance().getPreferenceStore().getDefaultString(name);
        
        if (defaultVal != null && val == null)
        {
            return false;
        }
        
        if (val != null && defaultVal == null)
        {
            return false;
        }
        
        if (defaultVal.equals(val))
        {
            return true;
        }
        
        return false;
    }
    
    public static int getIntValue(IProject project, String name)
    {
        String val = getValue(project, name);
        if (val == null)
        {
            return 0;
        }
        
        try
        {
            return Integer.parseInt(val);
        }
        catch (NumberFormatException e)
        {
            return 0;
        }
    }
    
    public static boolean getBooleanValue(IProject project, String name)
    {
        String val = getValue(project, name);
        if (val == null)
        {
            return false;
        }
        
        return Boolean.parseBoolean(val);
    }
    
    private static boolean useProjectSettings(IProject project)
    {
        String use = getProperty(project, FieldEditorOverlayPage.USEPROJECTSETTINGS);
        return "true".equals(use);
    }
    
    private static String getProperty(IProject project, String key)
    {
        try
        {
            return project.getPersistentProperty(createQualifiedName(key));
        }
        catch (CoreException e)
        {
        }
        return null;
    }
    
    public static QualifiedName createQualifiedName(String key)
    {
        return new QualifiedName(PROPERTY_QUALIFIER, key);
    }
}
