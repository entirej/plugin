/*
 * Copyright (c) 2003-2007 XClinical GmbH. 
 * Siegfriedstr. 8, 80803 Muenchen, Germany. 
 * All Rights Reserved. 
 * 
 * This software is the confidential and proprietary information of XClinical GmbH.
 * You shall not disclose any information contained in this file and shall use it only in 
 * accordance with the terms of the license agreement you entered into with XClinical.
 */

package org.entirej.ide.ui.editors.preferences;

import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.RadioGroupFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.entirej.ide.ui.EJUIPlugin;

public class EditorPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage
{

    public EditorPreferencePage()
    {
        super("Form Editor", GRID);
    }

    public void init(IWorkbench workbench)
    {
        setPreferenceStore(EJUIPlugin.getDefault().getPreferenceStore());
    }

    @Override
    protected void createFieldEditors()
    {
        {
            RadioGroupFieldEditor autoPerpectiveEd = new RadioGroupFieldEditor("AbstractEJFormEditor.autoPerspectiveSwitch",
                    "Automatically switch to EntireJ Form Perspective", 3, new String[][] { { "Always", MessageDialogWithToggle.ALWAYS },
                            { "Never", MessageDialogWithToggle.NEVER }, { "Prompt", "" }

                    }, getFieldEditorParent(), true);

            addField(autoPerpectiveEd);
        }
        

    }

}
