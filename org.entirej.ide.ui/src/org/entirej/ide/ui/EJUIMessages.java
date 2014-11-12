/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui;

import org.eclipse.osgi.util.NLS;

public class EJUIMessages extends NLS
{
    private static final String BUNDLE_NAME = "org.entirej.ide.ui.resources"; //$NON-NLS-1$

    static
    {
        // initialize resource bundle
        NLS.initializeMessages(BUNDLE_NAME, EJUIMessages.class);
    }

    private EJUIMessages()
    {
    }

    public static String NewWizard_wtitle;
    public static String NewProjectCreationPage_invalidProjectName;
    public static String NewProjectCreationPage_invalidLocationPath;

    public static String NewProjectWizard_title;
    public static String NewReportProjectWizard_title;
    public static String NewProjectWizard_MainPage_title;
    public static String NewReportProjectWizard_MainPage_title;
    public static String NewProjectWizard_MainPage_desc;
    public static String NewReportProjectWizard_MainPage_desc;
    public static String NewProjectWizard_ConfigPag_title;
    public static String NewReportProjectWizard_ConfigPag_title;
    public static String NewProjectWizard_ConfigPag_desc;
    public static String NewReportProjectWizard_ConfigPag_desc;
    public static String NewProjectWizard_ConfigPage_target_platfrom;
    public static String NewProjectWizard_ConfigPage_target_platfrom_desc;
    public static String NewProjectWizard_ConfigPage_database_connectivity;
    public static String NewProjectWizard_ConfigPage_database_connectivity_desc;

    public static String NodeAction_help;

    public static String EJPropertiesPage_title;
    public static String EJReportPropertiesPage_title;
    public static String EJDefinedPropertiesPage_title;
    public static String EJRenderersPage_title;
    public static String EJMenuPage_title;
    public static String EJLayoutPage_title;
    public static String EJVisualAttributePage;

    public static String NewPojoServiceWizard_title;
    public static String NewPojoServiceWizard_MainPage_title;
    public static String NewPojoServiceWizard_MainPage_desc;

    public static String NewFormWizard_title;
    public static String NewFormWizard_MainPage_title;
    public static String NewFormWizard_MainPage_desc;
    
    public static String NewReportWizard_title;
    public static String NewReportWizard_MainPage_title;
    public static String NewReportWizard_MainPage_desc;
}
