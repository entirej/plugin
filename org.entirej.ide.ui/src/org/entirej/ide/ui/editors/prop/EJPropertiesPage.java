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

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Widget;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIMessages;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.EditorLayoutFactory;

public class EJPropertiesPage extends AbstractEditorPage
{
    private EJPropertiesEditor editor;
    public static final String PAGE_ID = "ej.properties"; //$NON-NLS-1$

    public EJPropertiesPage(EJPropertiesEditor editor)
    {
        super(editor, PAGE_ID, EJUIMessages.EJPropertiesPage_title);
        this.editor = editor;
    }

    @Override
    protected void buildBody(final IManagedForm managedForm, FormToolkit toolkit)
    {
        Composite body = managedForm.getForm().getBody();
        body.setLayout(EditorLayoutFactory.createFormGridLayout(false, 2));
        ApplicationPropertiesPart applicationPropertiesPart = new ApplicationPropertiesPart(editor, this, body);
        managedForm.addPart(applicationPropertiesPart);
        managedForm.addPart(new FormPackagesPart(editor, this, body));
        managedForm.addPart(new ParametersPart(editor, this, body));
        final PropertyDefinitionGroupPart definitionGroupPart = new PropertyDefinitionGroupPart(editor, this, body)
        {

            @Override
            public Action[] getToolbarActions()
            {
                final Action refreshAction = new Action("Refresh", IAction.AS_PUSH_BUTTON)
                {

                    @Override
                    public void run()
                    {
                        buildUI();
                    }

                };

                Action addFeatureAction = new Action("Add Feature", IAction.AS_RADIO_BUTTON)
                {

                    @Override
                    public void runWithEvent(Event e)
                    {
                        FeatureConfigDialog dialog = new FeatureConfigDialog(getSection().getShell(), editor.getJavaProject());
                        dialog.open();

                    }

                };
                addFeatureAction.setImageDescriptor(EJUIImages.DESC_FEATURE);

                refreshAction.setImageDescriptor(EJUIImages.DESC_REFRESH);
                return new Action[] { addFeatureAction, refreshAction };
            }

        };
        managedForm.addPart(definitionGroupPart);
        applicationPropertiesPart.setDescriptorPart(definitionGroupPart);
    }

    @Override
    protected String getPageHeader()
    {
        return EJUIMessages.EJPropertiesPage_title;
    }

}
