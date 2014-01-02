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
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.ide.ui.EJUIMessages;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.editors.handlers.NodeElementDeleteHandler;
import org.entirej.ide.ui.editors.handlers.PageActionHandler;
import org.entirej.ide.ui.editors.handlers.PageActionHandlerProvider;
import org.entirej.ide.ui.nodes.NodeDescriptorPart;

public class EJMenuPage extends AbstractEditorPage implements PageActionHandlerProvider
{
    private EJPropertiesEditor editor;
    private MenuTreeSection    treeSection;
    public static final String PAGE_ID = "ej.menu"; //$NON-NLS-1$

    public EJMenuPage(EJPropertiesEditor editor)
    {
        super(editor, PAGE_ID, EJUIMessages.EJMenuPage_title);
        this.editor = editor;
    }

    @Override
    protected void buildBody(IManagedForm managedForm, FormToolkit toolkit)
    {

        Composite body = managedForm.getForm().getBody();
        body.setLayout(EditorLayoutFactory.createFormGridLayout(false, 2));

        treeSection = new MenuTreeSection(editor, this, body);

        NodeDescriptorPart descriptorPart = new NodeDescriptorPart(editor, this, body)
        {

            @Override
            public boolean isStale()
            {
                // force to refresh on show
                return true;
            }
        };
        managedForm.addPart(treeSection);
        managedForm.addPart(descriptorPart);
        treeSection.setNodeDescriptorViewer(descriptorPart);

    }

    @Override
    protected String getPageHeader()
    {
        return EJUIMessages.EJMenuPage_title;
    }

    public PageActionHandler getActionHandler(String commandId)
    {
        // if (ActionFactory.CUT.getCommandId().endsWith(commandId))
        // {
        //
        // }
        // if (ActionFactory.COPY.getCommandId().endsWith(commandId))
        // {
        //
        // }
        // if (ActionFactory.PASTE.getCommandId().endsWith(commandId))
        // {
        //
        // }
        if (ActionFactory.DELETE.getCommandId().endsWith(commandId))
        {
            return new NodeElementDeleteHandler(treeSection.getISelectionProvider());
        }
        // if (ActionFactory.REDO.getCommandId().endsWith(commandId))
        // {
        //
        // }
        // if (ActionFactory.UNDO.getCommandId().endsWith(commandId))
        // {
        //
        // }
        return null;
    }

    public boolean isHandlerActive(String commandId)
    {
        if (ActionFactory.DELETE.getCommandId().endsWith(commandId))
        {
            return true;
        }
        return false;
    }

}
