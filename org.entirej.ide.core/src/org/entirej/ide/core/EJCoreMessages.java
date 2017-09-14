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
package org.entirej.ide.core;

import org.eclipse.osgi.util.NLS;

public class EJCoreMessages extends NLS
{
    private static final String BUNDLE_NAME = "org.entirej.ide.core.resources"; //$NON-NLS-1$

    // public static String PreferencesPage_summary;
    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, EJCoreMessages.class);
    }

    private EJCoreMessages()
    {
    }

    public static String ActivationPage_summary;
}
