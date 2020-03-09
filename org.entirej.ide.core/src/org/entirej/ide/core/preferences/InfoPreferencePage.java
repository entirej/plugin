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
package org.entirej.ide.core.preferences;

import java.net.URL;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.entirej.ide.core.EJCoreImages;
import org.entirej.ide.core.EJCoreLog;

public class InfoPreferencePage extends PreferencePage implements IWorkbenchPreferencePage
{

    public void init(IWorkbench workbench)
    {
        noDefaultAndApplyButton();
    }

    @Override
    protected Control createContents(Composite parent)
    {

        Composite base = new Composite(parent, SWT.NULL);
        base.setLayout(new GridLayout());
        base.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        Composite summary = new Composite(base, SWT.NULL);
        summary.setLayout(new GridLayout(2, false));
        summary.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        Label ejIcon = new Label(summary, SWT.NONE);
        ejIcon.setAlignment(SWT.RIGHT);
        Label lblInfo = new Label(summary, SWT.NONE);
        ejIcon.setImage(EJCoreImages.getImage(EJCoreImages.DESC_EJ_ICON));

        new Label(summary, SWT.NONE);
        Link link = new Link(summary, SWT.NONE);
        link.setText("<A>http://www.entirej.com</A>");
        link.addSelectionListener(new SelectionAdapter()
        {
            @Override
            public void widgetSelected(SelectionEvent evet)
            {
                try
                {
                    PlatformUI.getWorkbench().getBrowserSupport().getExternalBrowser().openURL(new URL("http://www.entirej.com"));
                }
                catch (Exception e)
                {
                    EJCoreLog.log(e);
                }
            }
        });

        lblInfo.setText("EntireJ \n" + "Copyright (c) 2012-2013  CRESOFT AG \n");
        return base;
    }

}
