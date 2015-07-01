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

import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.forms.IManagedForm;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.interfaces.EJCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
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

public class EJFormUsagePage extends AbstractEditorPage implements PageActionHandlerProvider
{
    protected AbstractEJFormEditor editor;
    protected UsageTreeSection     dependencySection;
    protected UsageTreeSection     refrenceSection;
    public static final String     PAGE_ID = "ej.form.usage"; //$NON-NLS-1$

    public EJFormUsagePage(AbstractEJFormEditor editor)
    {
        super(editor, PAGE_ID, "Usage");
        this.editor = editor;
    }

    @Override
    protected void buildBody(IManagedForm managedForm, FormToolkit toolkit)
    {

        Composite body = managedForm.getForm().getBody();
        body.setLayout(EditorLayoutFactory.createFormGridLayout(true, 2));

        dependencySection = createDependencySection(body);
        refrenceSection = createRefrenceSection(body);

        managedForm.addPart(dependencySection);
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

    protected UsageTreeSection createDependencySection(Composite body)
    {
        return new UsageTreeSection(editor, this, body)
        {

            @Override
            protected UsageGroup[] getUsageGroups()
            {

                List<UsageGroup> groups = new ArrayList<UsageTreeSection.UsageGroup>();

                // detect usage.
                detectDependency(groups);

                return groups.toArray(new UsageGroup[0]);
            }

            @Override
            public String getSectionTitle()
            {
                return "Dependencies";
            }

            @Override
            public String getSectionDescription()
            {

                return "Dependencies on other resources";
            }
        };
    }

    protected void detectDependency(List<UsageGroup> groups)
    {

        EJPluginFormProperties form = editor.getFormProperties();

        {// detect inner forms
            List<Usage> innerFormUsage = new ArrayList<UsageTreeSection.Usage>();
            Collection<EJCanvasProperties> canvases = EJPluginCanvasRetriever.retriveAllCanvases(form);

            for (final EJCanvasProperties canvas : canvases)
            {

                if (canvas.getType() == EJCanvasType.FORM)
                {
                    final String formId = canvas.getReferredFormId();
                    if (formId != null)
                    {
                        Usage usage = new Usage(formId)
                        {

                            @Override
                            public void open()
                            {
                                FormsUtil.openForm(editor.getJavaProject(), formId);

                            }

                            @Override
                            public String getUsageInfo()
                            {
                                return String.format("Used in Canvas : '%s'", canvas.getName());
                            }

                            @Override
                            public Image getImage()
                            {
                                return EJUIImages.getImage(EJUIImages.DESC_CANVAS_FORM);
                            }
                        };
                        innerFormUsage.add(usage);

                    }
                }
            }

            if (innerFormUsage.size() > 0)
            {
                groups.add(new UsageGroup("Referred Forms", "Referred forms inside this form.", innerFormUsage));

            }
        }

        {// ref Object groups
            List<Usage> innerObjGroupUsage = new ArrayList<UsageTreeSection.Usage>();
            List<EJPluginObjectGroupProperties> groupProperties = form.getObjectGroupContainer().getAllObjectGroupProperties();
            for (final EJPluginObjectGroupProperties objgroup : groupProperties)
            {
                Usage usage = new Usage((objgroup.getTitle() == null || objgroup.getTitle().isEmpty()) ? objgroup.getName() : objgroup.getTitle())
                {

                    @Override
                    public void open()
                    {
                        FormsUtil.openObjectRefrence(editor.getJavaProject(), objgroup.getName());

                    }

                    @Override
                    public String getUsageInfo()
                    {
                        return String.format("Imported ObjectGroup: '%s'", objgroup.getName());
                    }

                    @Override
                    public Image getImage()
                    {
                        return EJUIImages.getImage(EJUIImages.DESC_OBJGROUP);
                    }
                };
                innerObjGroupUsage.add(usage);
            }
            if (innerObjGroupUsage.size() > 0)
            {
                groups.add(new UsageGroup("Referred ObjectGroups", "Referred objectgroups inside this form.", innerObjGroupUsage));

            }

        }

        {// ref Blocks groups
            List<Usage> innerObjGroupUsage = new ArrayList<UsageTreeSection.Usage>();
            List<EJPluginBlockProperties> groupProperties = form.getBlockContainer().getAllBlockProperties();
            for (final EJPluginBlockProperties objgroup : groupProperties)
            {
                if (objgroup.isImportFromObjectGroup() || !objgroup.isReferenceBlock())
                {
                    continue;
                }
                Usage usage = new Usage(objgroup.getReferencedBlockName())
                {

                    @Override
                    public void open()
                    {
                        FormsUtil.openObjectRefrence(editor.getJavaProject(), objgroup.getReferencedBlockName());

                    }

                    @Override
                    public String getUsageInfo()
                    {
                        return String.format("Referenced by  Block : '%s'", objgroup.getName());
                    }

                    @Override
                    public Image getImage()
                    {
                        return EJUIImages.getImage(EJUIImages.DESC_BLOCK_REF);
                    }
                };
                innerObjGroupUsage.add(usage);
            }
            if (innerObjGroupUsage.size() > 0)
            {
                groups.add(new UsageGroup("Referred Blocks", "Referred blocks inside this form.", innerObjGroupUsage));

            }

        }

        {// ref LOV groups
            List<Usage> innerObjGroupUsage = new ArrayList<UsageTreeSection.Usage>();
            List<EJPluginLovDefinitionProperties> groupProperties = form.getLovDefinitionContainer().getAllLovDefinitionProperties();
            for (final EJPluginLovDefinitionProperties objgroup : groupProperties)
            {
                if (objgroup.isImportFromObjectGroup())
                {
                    continue;
                }
                Usage usage = new Usage(objgroup.getReferencedLovDefinitionName())
                {

                    @Override
                    public void open()
                    {
                        FormsUtil.openObjectRefrence(editor.getJavaProject(), objgroup.getReferencedLovDefinitionName());

                    }

                    @Override
                    public String getUsageInfo()
                    {
                        return String.format("Referenced by LOV : '%s'", objgroup.getName());
                    }

                    @Override
                    public Image getImage()
                    {
                        return EJUIImages.getImage(EJUIImages.DESC_LOV_REF);
                    }
                };
                innerObjGroupUsage.add(usage);
            }
            if (innerObjGroupUsage.size() > 0)
            {
                groups.add(new UsageGroup("Referred LOVs", "Referred Lovs inside this form.", innerObjGroupUsage));

            }

        }
    }

    @Override
    protected String getPageHeader()
    {
        return "Form Usage";
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
            dependencySection.refresh();
            refrenceSection.refresh();
            dependencySection.expandNodes();
            refrenceSection.expandNodes();
        }
    }

}
