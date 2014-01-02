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
package org.entirej.framework.plugin.preferences;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer
{
    
    @Override
    public void initializeDefaultPreferences()
    {
        IPreferenceStore store = EntireJFrameworkPlugin.getSharedInstance().getPreferenceStore();
        store.setDefault(EntireJFrameworkPlugin.P_DBDRIVER, "<DriverClassName>");
        store.setDefault(EntireJFrameworkPlugin.P_URL, "<connection URL>");
        store.setDefault(EntireJFrameworkPlugin.P_USERNAME, "<username>");
        store.setDefault(EntireJFrameworkPlugin.P_PASSWORD, "<password>");
        
        store.setDefault(EntireJFrameworkPlugin.P_FORM_HEIGHT, "520");
        store.setDefault(EntireJFrameworkPlugin.P_FORM_WIDTH, "820");
        
    }
}
