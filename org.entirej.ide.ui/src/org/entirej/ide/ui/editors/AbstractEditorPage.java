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
package org.entirej.ide.ui.editors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.IFormPart;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.editor.FormPage;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.views.contentoutline.IContentOutlinePage;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;

public abstract class AbstractEditorPage extends FormPage
{

    private final AbstractEditor editor;

    public AbstractEditorPage(AbstractEditor editor, String pageId, String title)
    {
        super(editor, pageId, title);
        this.editor = editor;
    }

    protected void reload()
    {
        editor.refresh();
    }

    public AbstractEditor getEditor()
    {
        return editor;
    }

    /*
     * (non-Javadoc)
     * 
     * @see
     * org.eclipse.ui.forms.editor.FormPage#createFormContent(org.eclipse.ui
     * .forms.IManagedForm)
     */
    protected void createFormContent(IManagedForm managedForm)
    {
        super.createFormContent(managedForm);
        ScrolledForm form = managedForm.getForm();
        FormToolkit toolkit = managedForm.getToolkit();
        form.setText(getPageHeader());
        form.setImage(getPageHeaderImage());
        toolkit.decorateFormHeading(form.getForm());
        createPageToolbar(managedForm.getForm(), toolkit);
        buildBody(managedForm, toolkit);

        form.updateToolBar();
    }

    protected Image getPageHeaderImage()
    {
        return null;
    }

    protected abstract void buildBody(IManagedForm managedForm, FormToolkit toolkit);

    protected abstract String getPageHeader();

    @Override
    public void dispose()
    {
        super.dispose();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.forms.editor.FormPage#canLeaveThePage()
     */
    public boolean canLeaveThePage()
    {
        editor.setDirty(isDirty());
        return true;
    }

    protected Action[] getPageToolbarActions(ScrolledForm form, FormToolkit toolkit)
    {

        return new Action[0];
    }

    private void createPageToolbar(final ScrolledForm form, FormToolkit toolkit)
    {
        Action[] actions = getPageToolbarActions(form, toolkit);
        IToolBarManager toolBarManager = form.getToolBarManager();
        for (Action action : actions)
        {
            toolBarManager.add(action);
        }

        final String helpContextId = getHelpContextId();
        Action helpAction;
        if (helpContextId != null)
        {
            helpAction = new Action("help") { //$NON-NLS-1$
                public void run()
                {
                    BusyIndicator.showWhile(form.getForm().getDisplay(), new Runnable()
                    {
                        public void run()
                        {
                            PlatformUI.getWorkbench().getHelpSystem().displayHelp(helpContextId);
                        }
                    });
                }
            };
            helpAction.setImageDescriptor(EJUIImages.DESC_HELP);
            helpAction.setToolTipText("Help");
            toolBarManager.add(helpAction);

        }
        final Action refreshAction = new Action("Refresh", IAction.AS_PUSH_BUTTON)
        {

            @Override
            public void run()
            {
                if (MessageDialog.openQuestion(EJUIPlugin.getActiveWorkbenchShell(), "Refresh",
                        "This will overwrite all unsaved changes, are you sure you want to continue?"))
                {
                    reload();
                }
            }

        };
        refreshAction.setImageDescriptor(EJUIImages.DESC_REFRESH);
        toolBarManager.add(refreshAction);
        form.updateToolBar();
    }

    public String getHelpContextId()
    {
        return null;
    }

    public void refresh()
    {
        IManagedForm managedForm = getManagedForm();
        if (managedForm != null)
        {
            IFormPart[] parts = managedForm.getParts();
            for (IFormPart iFormPart : parts)
            {
                iFormPart.refresh();
            }
        }
    }

    public IContentOutlinePage getContentOutlinePage()
    {
        return null;
    }

}
