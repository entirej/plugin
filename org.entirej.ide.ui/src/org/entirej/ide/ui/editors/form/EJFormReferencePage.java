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
package org.entirej.ide.ui.editors.form;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jdt.ui.ISharedImages;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.editors.AbstractEditorPage;
import org.entirej.ide.ui.editors.EditorLayoutFactory;
import org.entirej.ide.ui.editors.form.UsageTreeSection.Usage;
import org.entirej.ide.ui.editors.form.UsageTreeSection.UsageGroup;
import org.entirej.ide.ui.editors.handlers.PageActionHandler;
import org.entirej.ide.ui.editors.handlers.PageActionHandlerProvider;
import org.entirej.ide.ui.utils.FormsUtil;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class EJFormReferencePage extends AbstractEditorPage implements PageActionHandlerProvider
{
    protected AbstractEJFormEditor editor;
    protected UsageTreeSection     refrenceSection;
    public static final String     PAGE_ID = "ej.form.usage.References"; //$NON-NLS-1$

    public EJFormReferencePage(AbstractEJFormEditor editor)
    {
        super(editor, PAGE_ID, "References");
        this.editor = editor;
    }

    @Override
    protected void buildBody(IManagedForm managedForm, FormToolkit toolkit)
    {

        Composite body = managedForm.getForm().getBody();
        body.setLayout(EditorLayoutFactory.createFormGridLayout(true, 1));

     
        refrenceSection = createRefrenceSection(body);

        managedForm.addPart(refrenceSection);
    }

    protected UsageTreeSection createRefrenceSection(Composite body)
    {

        return new UsageTreeSection(editor, this, body)
        {

            @Override
            protected UsageGroup[] getUsageGroups()
            {
                return new UsageGroup[0];
            }

            @Override
            public String getSectionTitle()
            {
                return "References";
            }

            @Override
            public String getSectionDescription()
            {

                return "Referred from other resources.";
            }
        };
    }

   

    @Override
    protected String getPageHeader()
    {
        return "Form References";
    }

    public PageActionHandler getActionHandler(String commandId)
    {

        return null;
    }

    public boolean isHandlerActive(String commandId)
    {

        return false;
    }

    public void refreshAfterBuid()
    {

    }

    @Override
    public void setActive(boolean active)
    {

        if (active)
        {
            refrenceSection.refresh();

            refrenceSection.expandNodes();
        }
    }

}
