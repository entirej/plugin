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
package org.entirej.ide.ui.editors.prop;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.ide.ui.EJUIMessages;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.EditorLayoutFactory;

public class EJReportPropertiesPage extends AbstractEditorPage
{
    private EJReportPropertiesEditor editor;
    public static final String PAGE_ID = "ej.properties"; //$NON-NLS-1$

    public EJReportPropertiesPage(EJReportPropertiesEditor editor)
    {
        super(editor, PAGE_ID, EJUIMessages.EJReportPropertiesPage_title);
        this.editor = editor;
    }

    @Override
    protected void buildBody(IManagedForm managedForm, FormToolkit toolkit)
    {
        Composite body = managedForm.getForm().getBody();
        body.setLayout(EditorLayoutFactory.createFormGridLayout(false, 2));
        ReportApplicationPropertiesPart applicationPropertiesPart = new ReportApplicationPropertiesPart(editor, this, body);
        managedForm.addPart(applicationPropertiesPart);
        managedForm.addPart(new ReportPackagesPart(editor, this, body));
        managedForm.addPart(new ReportParametersPart(editor, this, body));
    }

    @Override
    protected String getPageHeader()
    {
        return EJUIMessages.EJReportPropertiesPage_title;
    }

}
