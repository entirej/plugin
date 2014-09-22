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
package org.entirej.ide.ui.editors.report;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.actions.ActionFactory;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.Section;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.editors.handlers.NodeElementDeleteHandler;
import org.entirej.ide.ui.editors.handlers.PageActionHandler;
import org.entirej.ide.ui.editors.handlers.PageActionHandlerProvider;
import org.entirej.ide.ui.nodes.AbstractNode;
import org.entirej.ide.ui.nodes.NodeDescriptorPart;

public class EJReportBasePage extends AbstractEditorPage implements PageActionHandlerProvider
{
    protected AbstractEJReportEditor  editor;
    protected ReportDesignTreeSection treeSection;
    protected NodeDescriptorPart    descriptorPart;
    public static final String      PAGE_ID = "ej.report.base"; //$NON-NLS-1$

    public EJReportBasePage(AbstractEJReportEditor editor)
    {
        super(editor, PAGE_ID, "Design");
        this.editor = editor;
    }

    @Override
    protected void buildBody(IManagedForm managedForm, FormToolkit toolkit)
    {

        Composite body = managedForm.getForm().getBody();
        body.setLayout(EditorLayoutFactory.createFormGridLayout(false, 3));

        treeSection = createTreeSection(body);
        descriptorPart = new NodeDescriptorPart(editor, this, body)
        {

            @Override
            public boolean isStale()
            {
                // force to refresh on show
                return true;
            }

            @Override
            protected void buildBody(Section section, FormToolkit toolkit)
            {
                super.buildBody(section, toolkit);

                GridData sectionData = new GridData(GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL);
                sectionData.widthHint = 400;
                section.setLayoutData(sectionData);

            }

        };

        treeSection.setNodeDescriptorViewer(descriptorPart);
        ReportPreviewPart layoutPreviewPart = new ReportPreviewPart(editor, this, body);
        managedForm.addPart(treeSection);
        managedForm.addPart(descriptorPart);
        managedForm.addPart(layoutPreviewPart);
        treeSection.setReportPreviewer(layoutPreviewPart);
    }

    protected ReportDesignTreeSection createTreeSection(Composite body)
    {
        return new ReportDesignTreeSection(editor, this, body);
    }

    @Override
    protected String getPageHeader()
    {
        return "Report Design";
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

    @Override
    protected Action[] getPageToolbarActions(ScrolledForm form, FormToolkit toolkit)
    {
        final GridData hideData = new GridData(GridData.FILL_VERTICAL);
        hideData.heightHint = 0;
        hideData.widthHint = 0;
        final Action treeAction = new Action("Show/Hide: Elements Tree", IAction.AS_CHECK_BOX)
        {
            Object layoutData;

            @Override
            public void run()
            {

                if (treeSection != null)
                {

                    treeSection.getSection().getParent().setRedraw(false);
                    if (layoutData != null)
                    {
                        treeSection.getSection().setLayoutData(layoutData);
                        layoutData = null;
                    }
                    else
                    {
                        layoutData = treeSection.getSection().getLayoutData();
                        treeSection.getSection().setLayoutData(hideData);
                    }

                    treeSection.getSection().getParent().layout(true);
                    treeSection.getSection().getParent().setRedraw(true);
                }
            }

        };
        treeAction.setImageDescriptor(EJUIImages.DESC_FORM_EDIT_TREE);

        final Action propAction = new Action("Show/Hide: Element Details", IAction.AS_CHECK_BOX)
        {
            Object layoutData;

            @Override
            public void run()
            {

                if (descriptorPart != null)
                {
                    descriptorPart.getSection().getParent().setRedraw(false);
                    if (layoutData != null)
                    {
                        descriptorPart.getSection().setLayoutData(layoutData);
                        layoutData = null;
                    }
                    else
                    {
                        layoutData = descriptorPart.getSection().getLayoutData();
                        descriptorPart.getSection().setLayoutData(hideData);
                    }

                    descriptorPart.getSection().getParent().layout(true);
                    descriptorPart.getSection().getParent().setRedraw(true);
                }
            }

        };
        propAction.setImageDescriptor(EJUIImages.DESC_FORM_EDIT_PROP);
        return new Action[] { treeAction, propAction };
    }

    public void refreshAfterBuid()
    {
        if (treeSection != null)
        {
            EJUIPlugin.getStandardDisplay().asyncExec(new Runnable()
            {

                public void run()
                {
                    treeSection.refreshNodes();

                }
            });
        }

    }

    public void select(Object object)
    {
        if(treeSection!=null)
        {
            treeSection.selectNodes(false, treeSection.findNode(object, true));
        }
        
    }

    public void expand(Object objects)
    {
        if(treeSection!=null)
        {
            treeSection.expand(treeSection.findNode(objects, true) );
        }
        
    }

    public void refreshProperties()
    {
        if(descriptorPart!=null)
        {
            descriptorPart.buildUI();
        }
        
    }

    public void refresh(Object objects)
    {
        if(treeSection!=null)
        {
            AbstractNode<?> findNode = treeSection.findNode(objects, false);
            if(findNode!=null)
                treeSection.refresh(findNode);
        }
        
    }

}
