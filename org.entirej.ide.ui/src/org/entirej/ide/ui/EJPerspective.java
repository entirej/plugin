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

import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

;

public class EJPerspective implements IPerspectiveFactory
{

    public void createInitialLayout(IPageLayout layout)
    {
        IFolderLayout topLeft = layout.createFolder("topLeft", //$NON-NLS-1$
                IPageLayout.LEFT, 0.20f, layout.getEditorArea());

        topLeft.addView(JavaUI.ID_PACKAGES);
        topLeft.addView(IPageLayout.ID_PROJECT_EXPLORER);
        IFolderLayout bottom = layout.createFolder("bottomRight", //$NON-NLS-1$
                IPageLayout.BOTTOM, 0.75f, layout.getEditorArea());
        IFolderLayout topRight = layout.createFolder("topRight", //$NON-NLS-1$
                IPageLayout.RIGHT, 0.8f, layout.getEditorArea());
        bottom.addView(IPageLayout.ID_TASK_LIST);

        bottom.addView(IPageLayout.ID_PROBLEM_VIEW);
        bottom.addView("org.eclipse.ui.console.ConsoleView");//$NON-NLS-1$

        topRight.addView(IPageLayout.ID_OUTLINE);
        topRight.addView("org.eclipse.jdt.junit.ResultView"); // NON-NLS-1

        // Add "show views".
        layout.addShowViewShortcut(IPageLayout.ID_PROJECT_EXPLORER);
        layout.addShowViewShortcut(JavaUI.ID_PACKAGES);
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
        layout.addShowViewShortcut(IPageLayout.ID_PROBLEM_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);

        // Add "new wizards".
        layout.addNewWizardShortcut(EJUIConstants.EJ_PROJECT_WIZARD_ID);
        layout.addNewWizardShortcut(EJUIConstants.EJ_FORM_WIZARD_ID);
        layout.addNewWizardShortcut(EJUIConstants.EJ_SERVICE_WIZARD_ID);
        layout.addNewWizardShortcut(EJUIConstants.EJ_REUSABLEBLOCK_WIZARD_ID);
        layout.addNewWizardShortcut(EJUIConstants.EJ_REUSABLELOVDEF_WIZARD_ID);

        // Add JDT "new wizards".
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewPackageCreationWizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewClassCreationWizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewInterfaceCreationWizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewEnumCreationWizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewAnnotationCreationWizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewSourceFolderCreationWizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.jdt.ui.wizards.NewJavaWorkingSetWizard");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.folder");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.wizards.new.file");//$NON-NLS-1$
        layout.addNewWizardShortcut("org.eclipse.ui.editors.wizards.UntitledTextFileWizard");//$NON-NLS-1$


        layout.addActionSet("org.eclipse.debug.ui.launchActionSet"); // NON-NLS-1
        layout.addActionSet("org.eclipse.debug.ui.debugActionSet"); // NON-NLS-1
        
        // //NON-NLS-1
        layout.addActionSet("org.eclipse.jdt.debug.ui.JDTDebugActionSet"); // NON-NLS-1
        layout.addActionSet("org.eclipse.jdt.junit.JUnitActionSet"); // NON-NLS-1\
        layout.addActionSet(JavaUI.ID_ACTION_SET);
        layout.addActionSet(JavaUI.ID_ELEMENT_CREATION_ACTION_SET);
        layout.addActionSet(EJUIConstants.EJ_ACTION_SET_ID);
       
        layout.addActionSet(IPageLayout.ID_NAVIGATE_ACTION_SET); // NON-NLS-1

    }

}
