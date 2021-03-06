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
package org.entirej.ext.mysql.lib;

import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPage;
import org.eclipse.jdt.ui.wizards.IClasspathContainerPageExtension;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;

public class MySQLRuntimeContainerPage extends WizardPage implements IClasspathContainerPage, IClasspathContainerPageExtension
{

    IJavaProject javaProject;

    public MySQLRuntimeContainerPage()
    {
        super("EntireJ MySQL Runtime");
    }

    public void initialize(IJavaProject javaProject, IClasspathEntry[] currentEntries)
    {
        this.javaProject = javaProject;
    }

    public IClasspathEntry getSelection()
    {
        return JavaCore.newContainerEntry(MySQLRuntimeClasspathContainer.ID);
    }

    public void setSelection(IClasspathEntry containerEntry)
    {
    }

    public void createControl(Composite parent)
    {
        setTitle("EntireJ MySQL Runtime");
        setDescription("Add entirej MySQL libraries to project path.");

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setLayout(new GridLayout());
        setControl(composite);

        Link link = new Link(composite, SWT.NONE);
        link.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
        link.setText("EntireJ MySQL runtime classpath container.");
        // TODO add link ???
    }

    public boolean finish()
    {
        return true;
    }
}
