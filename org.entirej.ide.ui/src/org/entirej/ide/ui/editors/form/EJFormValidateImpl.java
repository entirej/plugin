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

import static org.entirej.ide.ui.editors.form.FormNodeTag.ACTION_PROCESSOR;
import static org.entirej.ide.ui.editors.form.FormNodeTag.BLOCK;
import static org.entirej.ide.ui.editors.form.FormNodeTag.BLOCK_ID;
import static org.entirej.ide.ui.editors.form.FormNodeTag.CANVAS;
import static org.entirej.ide.ui.editors.form.FormNodeTag.COL;
import static org.entirej.ide.ui.editors.form.FormNodeTag.DETAIL;
import static org.entirej.ide.ui.editors.form.FormNodeTag.FORM;
import static org.entirej.ide.ui.editors.form.FormNodeTag.GROUP;
import static org.entirej.ide.ui.editors.form.FormNodeTag.HEIGHT;
import static org.entirej.ide.ui.editors.form.FormNodeTag.INSET;
import static org.entirej.ide.ui.editors.form.FormNodeTag.ITEM;
import static org.entirej.ide.ui.editors.form.FormNodeTag.ITEM_ID;
import static org.entirej.ide.ui.editors.form.FormNodeTag.LOV;
import static org.entirej.ide.ui.editors.form.FormNodeTag.LOV_ID;
import static org.entirej.ide.ui.editors.form.FormNodeTag.MAIN;
import static org.entirej.ide.ui.editors.form.FormNodeTag.MAPPING;
import static org.entirej.ide.ui.editors.form.FormNodeTag.MAPPING_ID;
import static org.entirej.ide.ui.editors.form.FormNodeTag.MASTER;
import static org.entirej.ide.ui.editors.form.FormNodeTag.QUERY;
import static org.entirej.ide.ui.editors.form.FormNodeTag.REALTION;
import static org.entirej.ide.ui.editors.form.FormNodeTag.REALTION_ID;
import static org.entirej.ide.ui.editors.form.FormNodeTag.REF;
import static org.entirej.ide.ui.editors.form.FormNodeTag.RENDERER;
import static org.entirej.ide.ui.editors.form.FormNodeTag.SERVICE;
import static org.entirej.ide.ui.editors.form.FormNodeTag.TITLE;
import static org.entirej.ide.ui.editors.form.FormNodeTag.TYPE;
import static org.entirej.ide.ui.editors.form.FormNodeTag.UPDATE;
import static org.entirej.ide.ui.editors.form.FormNodeTag.WIDTH;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.entirej.framework.core.actionprocessor.interfaces.EJBlockActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJFormActionProcessor;
import org.entirej.framework.core.actionprocessor.interfaces.EJLovActionProcessor;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJLovDefinitionProperties;
import org.entirej.framework.core.properties.interfaces.EJRendererAssignment;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.EJDevConstants;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevFormRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevLovRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovItemMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovMappingProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginMainScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationJoinProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;
import org.entirej.framework.plugin.framework.properties.reader.EntireJFormReader;
import org.entirej.framework.plugin.framework.properties.reader.FormHandler;
import org.entirej.framework.plugin.utils.EJPluginCanvasRetriever;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJMarkerFactory;
import org.entirej.ide.core.spi.EJFormValidateProvider;
import org.entirej.ide.ui.nodes.NodeValidateProvider;
import org.entirej.ide.ui.utils.FormsUtil;
import org.entirej.ide.ui.utils.JavaAccessUtils;

public class EJFormValidateImpl implements EJFormValidateProvider
{

    public void validate(IFile file, IProgressMonitor monitor)
    {
        IProject _project = file.getProject();

        IJavaProject project = JavaCore.create(_project);
        EJPluginFormProperties formProperties = getFormProperties(file, project);
        if (formProperties != null)
        {

            if (isForm(file))
            {

                // validate base form title
                addMarker(file, validateFormTitle(file, formProperties, project), FORM | TITLE);
                // validate base form Renderer
                addMarker(file, validateFormRenderer(file, formProperties, project), FORM | RENDERER);

                // validate base form Action Processor
                addMarker(file, validateFormActionProcessor(file, formProperties, project), FORM | ACTION_PROCESSOR);

                // validate base form Layout Settings
                validateFormLayoutSettings(file, formProperties, project);

            }

            // validate blocks
            validateBlocks(file, formProperties, project);

            // validate lovs
            validateLovs(file, formProperties, project);

            // validate relations
            validateRelations(file, formProperties, project);

            // validate canvas
            // validateCanvases(file, formProperties, project);

        }
        monitor.done();
    }

    /*
     * private void validateCanvases(IFile file, EJPluginFormProperties
     * formProperties, IJavaProject project) { Collection<EJCanvasProperties>
     * canvasProperties =
     * formProperties.getCanvasContainer().getAllCanvasProperties(); for
     * (EJCanvasProperties canvasProp : canvasProperties) {
     * 
     * } }
     */

    private void validateRelations(IFile file, EJPluginFormProperties formProperties, IJavaProject project)
    {
        List<EJPluginRelationProperties> allRelationProperties = formProperties.getRelationContainer().getAllRelationProperties();
        for (EJPluginRelationProperties relationProp : allRelationProperties)
        {

            if (!validateRelation(file, formProperties, relationProp, project))
            {
                Collection<EJPluginRelationJoinProperties> relationJoins = relationProp.getRelationJoins();
                for (EJPluginRelationJoinProperties joinProp : relationJoins)
                {
                    IMarker marker = addMarker(file, validateRelationJoinItem(file, formProperties, relationProp, joinProp, project), GROUP | REALTION);
                    if (marker != null)
                    {
                        addMarkerAttribute(marker, REALTION_ID, relationProp.getName());
                    }
                }
            }
        }

    }

    private boolean validateRelation(IFile file, EJPluginFormProperties formProperties, EJPluginRelationProperties relationProp, IJavaProject project)
    {
        String name = relationProp.getName();
        String masterBlockName = relationProp.getMasterBlockName();
        if (masterBlockName == null || masterBlockName.trim().length() == 0)
        {
            Problem problem = new Problem(Problem.TYPE.ERROR, String.format("Relation '%s': master block name is not specified.", name));

            IMarker marker = addMarker(file, problem, GROUP | REALTION | MASTER);
            if (marker != null)
            {
                addMarkerAttribute(marker, REALTION_ID, relationProp.getName());
            }
            return true;
        }
        String detailBlockName = relationProp.getDetailBlockName();
        if (detailBlockName == null || detailBlockName.trim().length() == 0)
        {
            Problem problem = new Problem(Problem.TYPE.ERROR, String.format("Relation '%s': detail block name is not specified.", name));
            IMarker marker = addMarker(file, problem, GROUP | REALTION | DETAIL);
            if (marker != null)
            {
                addMarkerAttribute(marker, REALTION_ID, relationProp.getName());
            }
            return true;
        }

        EJBlockProperties masterProperties = formProperties.getBlockProperties(masterBlockName);
        if (masterProperties == null)
        {
            Problem problem = new Problem(Problem.TYPE.ERROR, String.format("Relation '%s': master block '%s'  is not defined in from.", name, masterBlockName));
            IMarker marker = addMarker(file, problem, GROUP | REALTION | MASTER);
            if (marker != null)
            {
                addMarkerAttribute(marker, REALTION_ID, relationProp.getName());
            }
            return true;
        }
        EJBlockProperties detailProperties = formProperties.getBlockProperties(detailBlockName);
        if (detailProperties == null)
        {
            Problem problem = new Problem(Problem.TYPE.ERROR, String.format("Relation '%s' detail block '%s'  is not defined in from.", name, detailBlockName));
            IMarker marker = addMarker(file, problem, GROUP | REALTION | DETAIL);
            if (marker != null)
            {
                addMarkerAttribute(marker, REALTION_ID, relationProp.getName());
            }
            return true;
        }
        return false;
    }

    private Problem validateRelationJoinItem(IFile file, EJPluginFormProperties formProperties, EJPluginRelationProperties relationProp,
            EJPluginRelationJoinProperties joinProp, IJavaProject project)
    {

        String masterItemName = joinProp.getMasterItemName();
        String detailItemName = joinProp.getDetailItemName();
        if (detailItemName == null || detailItemName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, String.format("['%s' relation]  master item is not specified.", relationProp.getName()));
        }
        if (masterItemName == null || masterItemName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, String.format("['%s' relation]  detail item is not specified.", relationProp.getName()));
        }

        EJPluginBlockProperties masterblockProp = formProperties.getBlockProperties(relationProp.getMasterBlockName());

        EJPluginBlockProperties detailblockProp = formProperties.getBlockProperties(relationProp.getDetailBlockName());

        if (!masterblockProp.getItemContainer().contains(masterItemName))
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' relation]  master block item '%s' is not found in in block items '%s'.",
                    relationProp.getName(), masterItemName, detailblockProp.getName()));
        }

        if (!detailblockProp.getItemContainer().contains(detailItemName))
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' relation]   detail block item '%s' is not found in block items '%s'.",
                    relationProp.getName(), detailItemName, masterblockProp.getName()));
        }

        return null;
    }

    private void validateLovs(IFile file, EJPluginFormProperties formProperties, IJavaProject project)
    {
        List<EJPluginLovDefinitionProperties> definitionProperties = formProperties.getLovDefinitionContainer().getAllLovDefinitionProperties();
        for (EJPluginLovDefinitionProperties blockProp : definitionProperties)
        {
            if (blockProp.isReferenceBlock())
            {
                if (!formProperties.getEntireJProperties().containsReusableLovDefinitionProperties(blockProp.getReferencedLovDefinitionName()))
                {
                    IMarker marker = addMarker(
                            file,
                            new Problem(Problem.TYPE.ERROR, String.format("'%s' referenced lov definition is misssing.",
                                    blockProp.getReferencedLovDefinitionName())), GROUP | LOV | REF);

                    if (marker != null)
                    {
                        addMarkerAttribute(marker, LOV_ID, blockProp.getName());
                    }
                }
                continue;
            }
            // validate renderer
            IMarker marker = addMarker(file, validateLovRenderer(file, formProperties, blockProp, project), GROUP | LOV | RENDERER);
            if (marker != null)
            {
                addMarkerAttribute(marker, LOV_ID, blockProp.getName());
            }
            // validate base lov Action Processor
            marker = addMarker(file, validateLovActionProcessor(file, formProperties, blockProp, project), GROUP | LOV | ACTION_PROCESSOR);
            if (marker != null)
            {
                addMarkerAttribute(marker, LOV_ID, blockProp.getName());
            }
            // validate base lov service
            marker = addMarker(file, validateLovBlockService(file, formProperties, blockProp, project), GROUP | LOV | SERVICE);
            if (marker != null)
            {
                addMarkerAttribute(marker, LOV_ID, blockProp.getName());
            }
            // validate base Lov block items
            validateLovBlockItems(file, formProperties, blockProp, project);
        }

    }

    private Problem validateLovRenderer(IFile file, EJPluginFormProperties formProperties, final EJPluginLovDefinitionProperties lovProp, IJavaProject project)
    {

        String renderer = lovProp.getLovRendererName();
        if (renderer == null || renderer.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("'%s' lov renderer is not specified.", lovProp.getName()));
        }

        EJRendererAssignment assignment = formProperties.getEntireJProperties().getApplicationAssignedLovRenderer(renderer);
        if (assignment == null)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("'%s' lov renderer '%s' is not defined in application.ejprop file.", lovProp.getName(),
                    renderer));
        }
        EJDevLovRendererDefinition rendererDefinition = lovProp.getRendererDefinition();
        EJFrameworkExtensionProperties rendererProperties = lovProp.getLovRendererProperties();
        if (rendererDefinition != null)
        {
            final RendererPropMask mask = new RendererPropMask()
            {

                public void mask(IMarker marker)
                {
                    tagMarker(marker, GROUP | LOV | MAIN);
                    addMarkerAttribute(marker, LOV_ID, lovProp.getName());
                }
            };
            validateRendererProperties(mask, String.format("'%s' lov renderer properties: ", lovProp.getName()), file, formProperties,
                    lovProp.getBlockProperties(), rendererDefinition.getLovPropertyDefinitionGroup(), rendererProperties, project);
            List<EJPluginItemGroupProperties> itemGroups = lovProp.getBlockProperties().getMainScreenItemGroupDisplayContainer().getItemGroups();

            EJPropertyDefinitionGroup propertyDefinitionGroup = rendererDefinition.getItemPropertiesDefinitionGroup();

            for (EJPluginItemGroupProperties groupProperties : itemGroups)
            {
                RendererPropMask smask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {

                        mask.mask(marker);
                        // TODO subtag
                    }
                };

                validateItemGroupProperties(smask, String.format("'%s' lov main screen: ", lovProp.getName()), file, formProperties, propertyDefinitionGroup,
                        groupProperties, project);
            }
        }
        rendererDefinition = null;

        if (lovProp.isUserQueryAllowed() && !lovProp.getBlockProperties().getQueryScreenItemGroupDisplayContainer().isEmpty())
        {
            EJDevQueryScreenRendererDefinition queryRendererDefinition = lovProp.getBlockProperties().getQueryScreenRendererDefinition();
            rendererProperties = lovProp.getBlockProperties().getQueryScreenRendererProperties();
            if (queryRendererDefinition != null)
            {
                final RendererPropMask mask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        tagMarker(marker, GROUP | LOV | QUERY);
                        addMarkerAttribute(marker, LOV_ID, lovProp.getName());
                    }
                };
                validateRendererProperties(mask, String.format("'%s' lov query screen renderer properties: ", lovProp.getName()), file, formProperties,
                        lovProp.getBlockProperties(), queryRendererDefinition.getQueryScreenPropertyDefinitionGroup(), rendererProperties, project);
                List<EJPluginItemGroupProperties> itemGroups = lovProp.getBlockProperties().getQueryScreenItemGroupDisplayContainer().getItemGroups();

                EJPropertyDefinitionGroup propertyDefinitionGroup = queryRendererDefinition.getItemPropertyDefinitionGroup();

                for (EJPluginItemGroupProperties groupProperties : itemGroups)
                {
                    RendererPropMask smask = new RendererPropMask()
                    {

                        public void mask(IMarker marker)
                        {

                            mask.mask(marker);
                            // TODO subtag
                        }
                    };
                    validateItemGroupProperties(smask, String.format("'%s' lov query screen: ", lovProp.getName()), file, formProperties,
                            propertyDefinitionGroup, groupProperties, project);
                }
            }
            queryRendererDefinition = null;
        }
        return null;
    }

    private Problem validateLovActionProcessor(IFile file, EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties blockProp,
            IJavaProject project)
    {

        String defClassName = blockProp.getActionProcessorClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return null;
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' lov action processor: '%s' can't find in project build path.", blockProp.getName(),
                        defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJLovActionProcessor.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' lov action processor: '%s' is not a sub type of '%s'.", blockProp.getName(),
                        defClassName, EJLovActionProcessor.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private Problem validateLovBlockService(IFile file, EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties lovDef, IJavaProject project)
    {

        String defClassName = lovDef.getBlockProperties().getServiceClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("'%s' lov block service is not specified.", lovDef.getName()));
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' lov block service: '%s' can't find in project build path.", lovDef.getName(),
                        defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJBlockService.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' lov block service: '%s' is not a sub type of '%s'.", lovDef.getName(), defClassName,
                        EJBlockService.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private void validateLovBlockItems(IFile file, EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties blockProp, IJavaProject project)
    {
        List<EJPluginBlockItemProperties> itemProperties = blockProp.getBlockProperties().getItemContainer().getAllItemProperties();
        for (EJPluginBlockItemProperties itemProp : itemProperties)
        {
            IMarker marker = addMarker(file, validateLovBlockItemDataType(file, formProperties, blockProp, itemProp, project), GROUP | LOV | ITEM | TYPE);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                addMarkerAttribute(marker, ITEM_ID, itemProp.getName());
            }
            marker = addMarker(file, validateLovBlockItemRenderer(file, formProperties, blockProp, itemProp, project), GROUP | LOV | ITEM | RENDERER);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                addMarkerAttribute(marker, ITEM_ID, itemProp.getName());
            }
        }

    }

    private Problem validateLovBlockItemDataType(IFile file, EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties blockProp,
            EJPluginBlockItemProperties itemProp, IJavaProject project)
    {

        String defClassName = itemProp.getDataTypeClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' lov] item: '%s' data type is not specified.", blockProp.getName(), itemProp.getName()));
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("['%s' lov] item: '%s' data type '%s' can't find in project build path.",
                        blockProp.getName(), itemProp.getName(), defClassName));
            }

        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private Problem validateLovBlockItemRenderer(IFile file, EJPluginFormProperties formProperties, final EJPluginLovDefinitionProperties blockProp,
            final EJPluginBlockItemProperties itemProp, IJavaProject project)
    {

        String renderer = itemProp.getItemRendererName();
        if (renderer == null || renderer.trim().length() == 0)
        {
            return null;// new Problem(Problem.TYPE.ERROR,
                        // String.format("'%s' block renderer is not specified.",
                        // blockProp.getName()));
        }

        EJRendererAssignment assignment = formProperties.getEntireJProperties().getApplicationAssignedItemRenderer(renderer);
        if (assignment == null)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' lov] item: '%s' renderer '%s' is not defined in application.ejprop file.",
                    blockProp.getName(), itemProp.getName(), renderer));
        }

        EJDevItemRendererDefinition rendererDefinition = itemProp.getItemRendererDefinition();
        EJFrameworkExtensionProperties rendererProperties = itemProp.getItemRendererProperties();
        if (rendererDefinition != null)
        {
            RendererPropMask mask = new RendererPropMask()
            {

                public void mask(IMarker marker)
                {
                    tagMarker(marker, GROUP | LOV | ITEM | RENDERER);
                    addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                    addMarkerAttribute(marker, ITEM_ID, itemProp.getName());

                }
            };
            validateRendererProperties(mask, String.format("['%s' lov] item '%s' renderer properties: ", blockProp.getName(), itemProp.getName()), file,
                    formProperties, blockProp.getBlockProperties(), rendererDefinition.getItemPropertyDefinitionGroup(), rendererProperties, project);

        }
        return null;
    }

    void validateRendererProperties(RendererPropMask mask, String tag, IFile file, EJPluginFormProperties formProperties,
            EJPluginBlockProperties blockProperties, EJPropertyDefinitionGroup definitionGroup, EJFrameworkExtensionProperties rendererProperties,
            IJavaProject project)
    {
        if (definitionGroup != null && rendererProperties != null)
        {
            validatePropertyDefinitionGroup(mask, tag, file, formProperties, blockProperties, rendererProperties, definitionGroup, project);
        }
    }

    void validateItemGroupProperties(final RendererPropMask pmask, String tag, IFile file, EJPluginFormProperties formProperties,
            EJPropertyDefinitionGroup definitionGroup, EJPluginItemGroupProperties groupProperties, IJavaProject project)
    {
        if (definitionGroup != null && groupProperties != null)
        {
            Collection<EJPluginScreenItemProperties> allItemDisplayProperties = groupProperties.getAllResequencableItemProperties();
            for (EJPluginScreenItemProperties itemDisplayProperties : allItemDisplayProperties)
            {
                if (itemDisplayProperties.isSpacerItem())
                    continue;

                RendererPropMask mask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        pmask.mask(marker);
                        // TODO subtag

                    }
                };
                EJFrameworkExtensionProperties rendererRequiredProperties = null;
                if ((!(itemDisplayProperties instanceof EJPluginMainScreenItemProperties))
                        || !itemDisplayProperties.getBlockProperties().isUsedInLovDefinition())
                {
                    rendererRequiredProperties = itemDisplayProperties.getBlockRendererRequiredProperties();
                }
                else
                {
                    rendererRequiredProperties = ((EJPluginMainScreenItemProperties) itemDisplayProperties).getLovRendererRequiredProperties();
                }

                validateRendererProperties(mask, String.format("%s item '%s, '", tag, itemDisplayProperties.getReferencedItemName()), file, formProperties,
                        groupProperties.getBlockProperties(), definitionGroup, rendererRequiredProperties, project);
            }

            Collection<EJPluginItemGroupProperties> allItemGroupDisplayProperties = groupProperties.getChildItemGroupContainer().getItemGroups();
            for (EJPluginItemGroupProperties subGroupProperties : allItemGroupDisplayProperties)
            {
                validateItemGroupProperties(pmask, tag, file, formProperties, definitionGroup, subGroupProperties, project);
            }
        }
    }

    void validatePropertyDefinitionGroup(RendererPropMask mask, String tag, IFile file, EJPluginFormProperties formProperties,
            EJPluginBlockProperties blockProperties, EJFrameworkExtensionProperties rendererProperties, EJPropertyDefinitionGroup definitionGroup,
            IJavaProject project)
    {
        if (definitionGroup == null)
            return;

        Collection<EJPropertyDefinition> propertyDefinitions = definitionGroup.getPropertyDefinitions();
        for (EJPropertyDefinition definition : propertyDefinitions)
        {
            validatePropertyDefinition(mask, tag, file, formProperties, blockProperties, rendererProperties, definitionGroup, definition, project);
        }

        // handle sub groups
        Collection<EJPropertyDefinitionGroup> subGroups = definitionGroup.getSubGroups();
        for (final EJPropertyDefinitionGroup subGroup : subGroups)
        {
            validatePropertyDefinitionGroup(mask, tag, file, formProperties, blockProperties, rendererProperties, subGroup, project);
        }

    }

    void validatePropertyDefinition(RendererPropMask mask, String tag, IFile file, EJPluginFormProperties formProperties,
            EJPluginBlockProperties blockProperties, EJFrameworkExtensionProperties rendererProperties, EJPropertyDefinitionGroup definitionGroup,
            EJPropertyDefinition definition, IJavaProject project)
    {
        final String label = definition.getLabel();

        final String groupName;
        if (definitionGroup.getFullGroupName() == null || definitionGroup.getFullGroupName().trim().length() == 0)
        {
            groupName = definition.getName();
        }
        else
        {
            groupName = String.format("%s.%s", definitionGroup.getFullGroupName(), definition.getName());
        }

        String strValue = rendererProperties.getStringProperty(groupName);
        boolean vlaueNull = (strValue == null || strValue.trim().length() == 0);
        if (vlaueNull && definition.isMandatory())
        {

            IMarker marker = addMarker(file, new Problem(Problem.TYPE.ERROR, String.format("%s mandatory property '%s' not defined.", tag, label)));
            if (marker != null)
                mask.mask(marker);
            return;
        }

        if (vlaueNull)
            return;

        final EJPropertyDefinitionType dataType = definition.getPropertyType();
        switch (dataType)
        {
            case BLOCK_ITEM:
            {
                if (blockProperties != null && !blockProperties.getItemContainer().contains(strValue))
                {
                    IMarker marker = addMarker(
                            file,
                            new Problem(Problem.TYPE.ERROR, String.format("%s property '%s' value not defined in '%s' block items.", tag, label,
                                    blockProperties.getName())));
                    if (marker != null)
                        mask.mask(marker);
                }
            }
                break;

            case VISUAL_ATTRIBUTE:
            {
                if (!formProperties.getEntireJProperties().getVisualAttributesContainer().contains(strValue))
                {
                    IMarker marker = addMarker(
                            file,
                            new Problem(Problem.TYPE.ERROR, String.format("%s property '%s' visual attribute not defined in application.ejprop file.", tag,
                                    label)));
                    if (marker != null)
                        mask.mask(marker);
                }
            }
                break;
            case FORM_ID:
            {
                IJavaProject javaProject = formProperties.getJavaProject();
                if (javaProject != null && FormsUtil.isFormExist(javaProject, strValue))
                {
                    IMarker marker = addMarker(file,
                            new Problem(Problem.TYPE.ERROR, String.format("%s property '%s' form id not defined in project form packages.", tag, label)));
                    if (marker != null)
                        mask.mask(marker);
                }
            }
                break;

            case LOV_DEFINITION:
            {
                if (!formProperties.getLovDefinitionContainer().contains(strValue))
                {
                    IMarker marker = addMarker(file,
                            new Problem(Problem.TYPE.ERROR, String.format("%s property '%s' lov definition not defined in form.", tag, label)));
                    if (marker != null)
                        mask.mask(marker);
                }
            }
                break;
            case LOV_DEFINITION_WITH_ITEMS:
            {
                String[] split = strValue.split("\\.");
                if (split.length == 2)
                {
                    EJPluginLovDefinitionProperties definitionProperties = formProperties.getLovDefinitionContainer().getLovDefinitionProperties(split[0]);
                    if (definitionProperties == null)
                    {
                        IMarker marker = addMarker(file,
                                new Problem(Problem.TYPE.ERROR, String.format("%s property '%s' lov definition not defined in form.", tag, label)));
                        if (marker != null)
                            mask.mask(marker);
                    }
                    else if (!definitionProperties.getBlockProperties().getItemContainer().contains(split[1]))
                    {
                        IMarker marker = addMarker(file,
                                new Problem(Problem.TYPE.ERROR, String.format("%s property '%s' value not defined in '%s' lov items.", tag, label, split[0])));
                        if (marker != null)
                            mask.mask(marker);

                    }
                }
            }
                break;
            case PROJECT_CLASS_FILE:
            {
                try
                {
                    IType findType = project.findType(strValue);
                    if (findType == null)
                    {
                        IMarker marker = addMarker(
                                file,
                                new Problem(Problem.TYPE.ERROR, String.format("%s property '%s' value class '%s' can't find in project build path.", tag,
                                        label, strValue)));
                        if (marker != null)
                            mask.mask(marker);

                    }

                }
                catch (CoreException e)
                {
                    IMarker marker = addMarker(file, new Problem(Problem.TYPE.ERROR, e.getMessage()));
                    if (marker != null)
                        mask.mask(marker);
                }
            }
                break;

        }

    }

    private void validateBlocks(IFile file, EJPluginFormProperties formProperties, IJavaProject project)
    {
        List<EJPluginBlockProperties> allBlockProperties = formProperties.getBlockContainer().getAllBlockProperties();
        for (EJPluginBlockProperties blockProp : allBlockProperties)
        {

            if (blockProp.isReferenceBlock())
            {
                if (!formProperties.getEntireJProperties().containsReusableBlockProperties(blockProp.getReferencedBlockName()))
                {
                    IMarker marker = addMarker(
                            file,
                            new Problem(Problem.TYPE.ERROR, String.format("'%s' referenced block definition is misssing.", blockProp.getReferencedBlockName())),
                            GROUP | BLOCK | REF);
                    if (marker != null)
                    {
                        addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                    }
                }
                continue;
            }

            boolean hasCanvas = blockProp.getCanvasName() != null && blockProp.getCanvasName().trim().length() > 0;
            if (hasCanvas)
            {
                // validate base block Renderer
                IMarker marker = addMarker(file, validateBlockRenderer(file, formProperties, blockProp, project), GROUP | BLOCK | RENDERER);
                if (marker != null)
                {
                    addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                }
                // validate base block canvas
                marker = addMarker(file, validateBlockCanvas(file, formProperties, blockProp, project), GROUP | BLOCK | CANVAS);
                if (marker != null)
                {
                    addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                }
            }
            // validate base block service
            IMarker marker = addMarker(file, validateBlockService(file, formProperties, blockProp, project), GROUP | BLOCK | SERVICE);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
            }

            // validate base block items
            validateBlockItems(file, formProperties, blockProp, project);
            if (!blockProp.isMirrorChild())
            {
                // validate base block Action Processor
                marker = addMarker(file, validateBlockActionProcessor(file, formProperties, blockProp, project), GROUP | BLOCK | ACTION_PROCESSOR);
                if (marker != null)
                {
                    addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                }
                validateBlockLovMappings(file, formProperties, blockProp, project);
            }
            validateBlockScreens(file, formProperties, blockProp, project);
        }

    }

    private void validateBlockScreens(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp, IJavaProject project)
    {

    }

    private void validateBlockItems(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp, IJavaProject project)
    {
        List<EJPluginBlockItemProperties> itemProperties = blockProp.getItemContainer().getAllItemProperties();

        if (!blockProp.isControlBlock() && !blockProp.isMirrorChild() && !blockProp.isReferenceBlock())
        {
            List<EJPluginBlockItemProperties> serviceItems = blockProp.getServiceItems();

            for (EJPluginBlockItemProperties itemProp : itemProperties)
            {
                if (itemProp.isBlockServiceItem())
                {
                    boolean found = false;
                    for (EJPluginBlockItemProperties serviceItem : serviceItems)
                    {
                        if (serviceItem.getName().equals(itemProp.getName()))
                        {
                            found = true;
                            break;
                        }
                    }
                    if (!found)
                    {
                        IMarker marker = addMarker(
                                file,
                                new Problem(Problem.TYPE.ERROR, String.format("['%s' block] item: '%s' is not specified in Service '%s'.", blockProp.getName(),
                                        itemProp.getName(), blockProp.getServiceClassName())), GROUP | BLOCK | ITEM | TYPE);
                        if (marker != null)
                        {
                            addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                            addMarkerAttribute(marker, ITEM_ID, itemProp.getName());
                        }
                    }
                }
            }

        }

        for (EJPluginBlockItemProperties itemProp : itemProperties)
        {
            IMarker marker = addMarker(file, validateBlockItemDataType(file, formProperties, blockProp, itemProp, project), GROUP | BLOCK | ITEM | TYPE);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                addMarkerAttribute(marker, ITEM_ID, itemProp.getName());
            }
            marker = addMarker(file, validateBlockItemRenderer(file, formProperties, blockProp, itemProp, project), GROUP | BLOCK | ITEM | RENDERER);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                addMarkerAttribute(marker, ITEM_ID, itemProp.getName());
            }
        }

    }

    private void validateBlockLovMappings(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp, IJavaProject project)
    {

        List<EJPluginLovMappingProperties> mappingProperties = blockProp.getLovMappingContainer().getAllLovMappingProperties();
        for (EJPluginLovMappingProperties mappingProp : mappingProperties)
        {
            Problem problem = validateBlockLovMapping(file, formProperties, blockProp, mappingProp, project);
            IMarker marker = addMarker(file, problem, GROUP | BLOCK | LOV | MAPPING);
            if (marker != null)
            {
                addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                addMarkerAttribute(marker, MAPPING_ID, mappingProp.getName());
            }
            if (problem == null)
            {
                List<EJPluginLovItemMappingProperties> itemMappingProperties = mappingProp.getAllItemMappingProperties();
                for (EJPluginLovItemMappingProperties itemMapping : itemMappingProperties)
                {
                    marker = addMarker(file, validateBlockLovMappingItem(file, formProperties, blockProp, mappingProp, itemMapping, project), GROUP | BLOCK
                            | LOV | MAPPING);
                    if (marker != null)
                    {
                        addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                        addMarkerAttribute(marker, MAPPING_ID, mappingProp.getName());

                        if (itemMapping.getBlockItemName() != null && itemMapping.getBlockItemName().trim().length() > 0)
                        {
                            addMarkerAttribute(marker, ITEM_ID, itemMapping.getBlockItemName());
                        }
                        if (itemMapping.getLovDefinitionItemName() != null && itemMapping.getLovDefinitionItemName().trim().length() > 0)
                        {
                            addMarkerAttribute(marker, LOV_ID, itemMapping.getLovDefinitionItemName());
                        }
                    }
                }
            }
        }

    }

    private Problem validateBlockLovMapping(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp,
            EJPluginLovMappingProperties mappingProp, IJavaProject project)
    {

        String lovDefinitionName = mappingProp.getLovDefinitionName();
        if (lovDefinitionName == null || lovDefinitionName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' block] lov mappling : '%s' lov definition name is not specified.", blockProp.getName(),
                    mappingProp.getName()));
        }

        EJLovDefinitionProperties definitionProperties = formProperties.getLovDefinitionProperties(lovDefinitionName);
        if (definitionProperties == null)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' block] lov mappling : '%s' lov definition '%s' is not defined in form.",
                    blockProp.getName(), mappingProp.getName(), lovDefinitionName));
        }

        return null;
    }

    private Problem validateBlockLovMappingItem(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp,
            EJPluginLovMappingProperties mappingProp, EJPluginLovItemMappingProperties itemMapping, IJavaProject project)
    {

        String blockItemName = itemMapping.getBlockItemName();
        String lovDefinitionItemName = itemMapping.getLovDefinitionItemName();
        if (lovDefinitionItemName == null || lovDefinitionItemName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, String.format("['%s' block] lov mappling : '%s', lov item is not specified.", blockProp.getName(),
                    mappingProp.getName()));
        }
        if (blockItemName == null || blockItemName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, String.format("['%s' block] lov mappling : '%s', block item is not specified.", blockProp.getName(),
                    mappingProp.getName()));
        }

        // validate lov Item in LOV def
        EJPluginLovDefinitionProperties definitionProperties = formProperties.getLovDefinitionProperties(mappingProp.getLovDefinitionName());
        if (definitionProperties != null && !definitionProperties.getBlockProperties().getItemContainer().contains(lovDefinitionItemName))
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' block] lov mappling : '%s', lov item '%s' is not found in lov definition '%s'.",
                    blockProp.getName(), mappingProp.getName(), lovDefinitionItemName, mappingProp.getLovDefinitionName()));
        }

        if (!blockProp.getItemContainer().contains(blockItemName))
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' block] lov mappling : '%s', block item '%s' is not found in block items '%s'.",
                    blockProp.getName(), mappingProp.getName(), blockItemName, blockProp.getName()));
        }

        return null;
    }

    private Problem validateBlockItemRenderer(IFile file, EJPluginFormProperties formProperties, final EJPluginBlockProperties blockProp,
            final EJPluginBlockItemProperties itemProp, IJavaProject project)
    {

        String renderer = itemProp.getItemRendererName();
        if (renderer == null || renderer.trim().length() == 0)
        {
            return null;// new Problem(Problem.TYPE.ERROR,
                        // String.format("'%s' block renderer is not specified.",
                        // blockProp.getName()));
        }

        EJRendererAssignment assignment = formProperties.getEntireJProperties().getApplicationAssignedItemRenderer(renderer);
        if (assignment == null)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("['%s' block] item: '%s' renderer '%s' is not defined in application.ejprop file.",
                    blockProp.getName(), itemProp.getName(), renderer));
        }

        EJDevItemRendererDefinition rendererDefinition = itemProp.getItemRendererDefinition();
        EJFrameworkExtensionProperties rendererProperties = itemProp.getItemRendererProperties();
        if (rendererDefinition != null)
        {
            final RendererPropMask mask = new RendererPropMask()
            {

                public void mask(IMarker marker)
                {
                    tagMarker(marker, GROUP | BLOCK | ITEM | RENDERER);
                    addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                    addMarkerAttribute(marker, ITEM_ID, itemProp.getName());

                }
            };
            validateRendererProperties(mask, String.format("['%s' block] item '%s' renderer properties: ", blockProp.getName(), itemProp.getName()), file,
                    formProperties, blockProp, rendererDefinition.getItemPropertyDefinitionGroup(), rendererProperties, project);
        }
        return null;
    }

    private Problem validateBlockItemDataType(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp,
            EJPluginBlockItemProperties itemProp, IJavaProject project)
    {

        String defClassName = itemProp.getDataTypeClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR,
                    String.format("['%s' block] item: '%s' data type is not specified.", blockProp.getName(), itemProp.getName()));
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("['%s' block] item: '%s' data type '%s' can't find in project build path.",
                        blockProp.getName(), itemProp.getName(), defClassName));
            }

        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private Problem validateBlockRenderer(IFile file, EJPluginFormProperties formProperties, final EJPluginBlockProperties blockProp, IJavaProject project)
    {

        String renderer = blockProp.getBlockRendererName();
        if (renderer == null || renderer.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("'%s' block renderer is not specified.", blockProp.getName()));
        }

        EJRendererAssignment assignment = formProperties.getEntireJProperties().getApplicationAssignedBlockRenderer(renderer);
        if (assignment == null)
        {
            return new Problem(Problem.TYPE.ERROR, String.format("'%s' block renderer '%s' is not defined in application.ejprop file.", blockProp.getName(),
                    renderer));
        }

        EJDevBlockRendererDefinition rendererDefinition = blockProp.getBlockRendererDefinition();
        EJFrameworkExtensionProperties rendererProperties = blockProp.getBlockRendererProperties();
        if (rendererDefinition != null)
        {
            final RendererPropMask mask = new RendererPropMask()
            {

                public void mask(IMarker marker)
                {
                    tagMarker(marker, GROUP | BLOCK | RENDERER);
                    addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());

                }
            };
            validateRendererProperties(mask, String.format("'%s' block renderer properties: ", blockProp.getName()), file, formProperties, blockProp,
                    rendererDefinition.getBlockPropertyDefinitionGroup(), rendererProperties, project);
            List<EJPluginItemGroupProperties> itemGroups = blockProp.getMainScreenItemGroupDisplayContainer().getItemGroups();

            EJPropertyDefinitionGroup propertyDefinitionGroup = rendererDefinition.getItemPropertiesDefinitionGroup();
            final RendererPropMask imask = new RendererPropMask()
            {

                public void mask(IMarker marker)
                {
                    tagMarker(marker, GROUP | BLOCK | MAIN);
                    addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                }
            };
            for (EJPluginItemGroupProperties groupProperties : itemGroups)
            {

                validateItemGroupProperties(imask, String.format("'%s' block main screen: ", blockProp.getName()), file, formProperties,
                        propertyDefinitionGroup, groupProperties, project);
            }

        }
        rendererDefinition = null;

        if (blockProp.isInsertAllowed() && !blockProp.getInsertScreenItemGroupDisplayContainer().isEmpty())
        {
            EJDevInsertScreenRendererDefinition insertRendererDefinition = blockProp.getInsertScreenRendererDefinition();
            rendererProperties = blockProp.getInsertScreenRendererProperties();
            if (insertRendererDefinition != null)
            {
                final RendererPropMask mask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        tagMarker(marker, GROUP | BLOCK | INSET);
                        addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());
                    }
                };
                validateRendererProperties(mask, String.format("'%s' block insert screen renderer properties: ", blockProp.getName()), file, formProperties,
                        blockProp, insertRendererDefinition.getInsertScreenPropertyDefinitionGroup(), rendererProperties, project);
                List<EJPluginItemGroupProperties> itemGroups = blockProp.getInsertScreenItemGroupDisplayContainer().getItemGroups();

                EJPropertyDefinitionGroup propertyDefinitionGroup = insertRendererDefinition.getItemPropertyDefinitionGroup();

                final RendererPropMask imask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        mask.mask(marker);
                        // TODO subtag

                    }
                };
                for (EJPluginItemGroupProperties groupProperties : itemGroups)
                {
                    validateItemGroupProperties(imask, String.format("'%s' block insert screen: ", blockProp.getName()), file, formProperties,
                            propertyDefinitionGroup, groupProperties, project);
                }

            }
            insertRendererDefinition = null;

        }

        if (blockProp.isUpdateAllowed() && !blockProp.getUpdateScreenItemGroupDisplayContainer().isEmpty())
        {
            EJDevUpdateScreenRendererDefinition updateRendererDefinition = blockProp.getUpdateScreenRendererDefinition();
            rendererProperties = blockProp.getUpdateScreenRendererProperties();
            if (updateRendererDefinition != null)
            {
                final RendererPropMask mask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        tagMarker(marker, GROUP | BLOCK | UPDATE);
                        addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());

                    }
                };
                validateRendererProperties(mask, String.format("'%s' block update screen renderer properties: ", blockProp.getName()), file, formProperties,
                        blockProp, updateRendererDefinition.getUpdateScreenPropertyDefinitionGroup(), rendererProperties, project);
                List<EJPluginItemGroupProperties> itemGroups = blockProp.getUpdateScreenItemGroupDisplayContainer().getItemGroups();

                EJPropertyDefinitionGroup propertyDefinitionGroup = updateRendererDefinition.getItemPropertyDefinitionGroup();

                final RendererPropMask imask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        mask.mask(marker);
                        // TODO subtag

                    }
                };
                for (EJPluginItemGroupProperties groupProperties : itemGroups)
                {
                    validateItemGroupProperties(imask, String.format("'%s' block update screen: ", blockProp.getName()), file, formProperties,
                            propertyDefinitionGroup, groupProperties, project);
                }
            }
            updateRendererDefinition = null;
        }

        if (blockProp.isQueryAllowed() && !blockProp.getQueryScreenItemGroupDisplayContainer().isEmpty())
        {
            EJDevQueryScreenRendererDefinition queryRendererDefinition = blockProp.getQueryScreenRendererDefinition();
            rendererProperties = blockProp.getQueryScreenRendererProperties();
            if (queryRendererDefinition != null)
            {
                final RendererPropMask mask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        tagMarker(marker, GROUP | BLOCK | QUERY);
                        addMarkerAttribute(marker, BLOCK_ID, blockProp.getName());

                    }
                };
                validateRendererProperties(mask, String.format("'%s' block query screen renderer properties: ", blockProp.getName()), file, formProperties,
                        blockProp, queryRendererDefinition.getQueryScreenPropertyDefinitionGroup(), rendererProperties, project);
                List<EJPluginItemGroupProperties> itemGroups = blockProp.getQueryScreenItemGroupDisplayContainer().getItemGroups();

                EJPropertyDefinitionGroup propertyDefinitionGroup = queryRendererDefinition.getItemPropertyDefinitionGroup();
                final RendererPropMask imask = new RendererPropMask()
                {

                    public void mask(IMarker marker)
                    {
                        mask.mask(marker);
                        // TODO subtag

                    }
                };
                for (EJPluginItemGroupProperties groupProperties : itemGroups)
                {
                    validateItemGroupProperties(imask, String.format("'%s' block query screen: ", blockProp.getName()), file, formProperties,
                            propertyDefinitionGroup, groupProperties, project);
                }
            }
            queryRendererDefinition = null;
        }

        return null;
    }

    private Problem validateBlockCanvas(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp, IJavaProject project)
    {

        String canvas = blockProp.getCanvasName();

        if (canvas != null && !EJPluginCanvasRetriever.canvasExists(formProperties, canvas))
        {
            return new Problem(Problem.TYPE.ERROR, String.format("'%s' block assigned canvas '%s' is not specified in Form.", blockProp.getName(), canvas));
        }

        return null;
    }

    private Problem validateBlockService(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp, IJavaProject project)
    {

        if (blockProp.isControlBlock() || blockProp.isReferenceBlock() || blockProp.isMirrorChild())
            return null;
        String defClassName = blockProp.getServiceClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.WARNING, String.format("'%s' block service is not specified.", blockProp.getName()));
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block service: '%s' can't find in project build path.", blockProp.getName(),
                        defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJBlockService.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block service: '%s' is not a sub type of '%s'.", blockProp.getName(), defClassName,
                        EJBlockService.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private Problem validateBlockActionProcessor(IFile file, EJPluginFormProperties formProperties, EJPluginBlockProperties blockProp, IJavaProject project)
    {

        String defClassName = blockProp.getActionProcessorClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return null;
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block action processor: '%s' can't find in project build path.",
                        blockProp.getName(), defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJBlockActionProcessor.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("'%s' block action processor: '%s' is not a sub type of '%s'.", blockProp.getName(),
                        defClassName, EJBlockActionProcessor.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    public Problem validateFormTitle(IFile file, EJPluginFormProperties formProperties, IJavaProject project)
    {
        String title = formProperties.getTitle();
        if (title == null || title.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Form Title is not specified.");
        }
        return null;
    }

    public Problem validateFormRenderer(IFile file, EJPluginFormProperties formProperties, IJavaProject project)
    {

        String renderer = formProperties.getFormRendererName();
        if (renderer == null || renderer.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Form Renderer is not specified.");
        }

        EJRendererAssignment assignment = formProperties.getEntireJProperties().getApplicationAssignedFormRenderer(renderer);
        if (assignment == null)
        {
            return new Problem(Problem.TYPE.ERROR, "Form Renderer is not defined in application.ejprop file.");
        }

        EJDevFormRendererDefinition rendererDefinition = ExtensionsPropertiesFactory.loadFormRendererDefinition(formProperties.getEntireJProperties(),
                formProperties.getFormRendererName());
        EJFrameworkExtensionProperties rendererProperties = formProperties.getFormRendererProperties();
        if (rendererDefinition != null)
        {
            final RendererPropMask mask = new RendererPropMask()
            {

                public void mask(IMarker marker)
                {
                    tagMarker(marker, FORM | RENDERER);

                }
            };
            validateRendererProperties(mask, "Form renderer properties: ", file, formProperties, null, rendererDefinition.getFormPropertyDefinitionGroup(),
                    rendererProperties, project);

        }
        return null;
    }

    public Problem validateFormActionProcessor(IFile file, EJPluginFormProperties formProperties, IJavaProject project)
    {

        String defClassName = formProperties.getActionProcessorClassName();
        if (defClassName == null || defClassName.trim().length() == 0)
        {
            return new Problem(Problem.TYPE.ERROR, "Action Processor class must be specified.");
        }

        try
        {
            IType findType = project.findType(defClassName);
            if (findType == null)
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s can't find in project build path.", defClassName));
            }

            if (!JavaAccessUtils.isSubTypeOfInterface(findType, EJFormActionProcessor.class))
            {
                return new Problem(Problem.TYPE.ERROR, String.format("%s is not a sub type of %s.", defClassName, EJFormActionProcessor.class.getName()));
            }
        }
        catch (CoreException e)
        {
            return new Problem(Problem.TYPE.ERROR, e.getMessage());
        }
        return null;
    }

    private void validateFormLayoutSettings(IFile file, EJPluginFormProperties formProperties, IJavaProject project)
    {
        int numOfCol = formProperties.getNumCols();
        if (numOfCol < 1)
        {
            addMarker(file, new Problem(Problem.TYPE.ERROR, "Form should have a minimum of 1 column in order for the form to display anything."), FORM | COL);

        }

        boolean hasWidth = formProperties.getFormWidth() > 0;
        boolean hasHeight = formProperties.getFormHeight() > 0;
        if (!hasWidth && !hasHeight)
        {
            addMarker(file, new Problem(Problem.TYPE.WARNING, "Form height and width is not specified."), FORM | HEIGHT | WIDTH);
        }
        else
        {
            if (!hasWidth)
            {
                addMarker(file, new Problem(Problem.TYPE.WARNING, "Form width is not specified."), FORM | WIDTH);
            }
            if (!hasHeight)
            {
                addMarker(file, new Problem(Problem.TYPE.WARNING, "Form height is not specified."), FORM | HEIGHT);
            }
        }

    }

    boolean isForm(IFile file)
    {
        return file.getName().endsWith(EJDevConstants.FORM_PROPERTIES_FILE_SUFFIX);
    }

    boolean isRefBlock(IFile file)
    {
        return file.getName().endsWith(EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
    }

    boolean isRefLov(IFile file)
    {
        return file.getName().endsWith(EJDevConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
    }

    EJPluginFormProperties getFormProperties(IFile file, IJavaProject project)
    {

        EJPluginFormProperties formProperties = null;
        /*
         * IWorkbenchWindow[] windows =
         * EJUIPlugin.getDefault().getWorkbench().getWorkbenchWindows(); for
         * (IWorkbenchWindow window : windows) { if (window != null) {
         * IWorkbenchPage[] activePages = window.getPages(); for (IWorkbenchPage
         * page : activePages) { try { IEditorPart editor = page.findEditor(new
         * FileEditorInput(file)); if (editor instanceof AbstractEJFormEditor) {
         * formProperties = ((AbstractEJFormEditor) editor).getFormProperties();
         * if (formProperties != null) return formProperties; } } catch
         * (Throwable e) { //ignore any error } } } }
         */

        // read from file
        InputStream inStream = null;
        try
        {

            inStream = file.getContents();

            EntireJFormReader reader = new EntireJFormReader();
            String fileName = file.getName();
            fileName = fileName.substring(0, fileName.lastIndexOf("."));
            formProperties = reader.readForm(new FormHandler(project, fileName), project, inStream);
            formProperties.initialisationCompleted();
        }
        catch (Exception exception)
        {
            addMarker(file, new Problem(Problem.TYPE.ERROR, exception.getMessage()));
            EJCoreLog.logWarnningMessage(exception.getMessage());
        }
        finally
        {

            try
            {
                if (inStream != null)
                    inStream.close();
            }
            catch (IOException e)
            {
                EJCoreLog.logException(e);
            }
        }

        return formProperties;
    }

    public IMarker addMarker(IFile file, Problem p, int tag)
    {
        return tagMarker(addMarker(file, p), tag);
    }

    public IMarker addMarker(IFile file, Problem p)
    {
        if (p != null)
        {
            try
            {
                IMarker marker = EJMarkerFactory.createMarker(file);
                switch (p.type)
                {
                    case ERROR:
                        EJMarkerFactory.addErrorMessage(marker, p.message);
                        break;
                    case INFO:
                        EJMarkerFactory.addInfoMessage(marker, p.message);
                        break;
                    case WARNING:
                        EJMarkerFactory.addWarningMessage(marker, p.message);
                        break;
                }
                return marker;
            }
            catch (CoreException e)
            {
                EJCoreLog.logException(e);
            }
        }
        return null;
    }

    public IMarker addMarkerAttribute(IMarker marker, String key, Object val)
    {
        if (marker != null)
        {
            try
            {
                marker.setAttribute(key, val);
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }
        }

        return marker;
    }

    public IMarker tagMarker(IMarker marker, int tag)
    {
        if (marker != null)
        {
            try
            {
                marker.setAttribute(NodeValidateProvider.NODE_TAG, tag);
            }
            catch (CoreException e)
            {
                EJCoreLog.log(e);
            }
        }

        return marker;
    }

    public static class Problem
    {
        enum TYPE
        {
            INFO, ERROR, WARNING
        }

        final TYPE   type;
        final String message;

        Problem(TYPE type, String message)
        {
            super();
            this.type = type;
            this.message = message;
        }

    }

    protected static interface RendererPropMask
    {
        void mask(IMarker marker);
    }

}
