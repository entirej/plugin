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

package org.entirej.framework.plugin.framework.properties.reader;

import java.util.HashMap;
import java.util.Map;


public class EJRWTEJApplicationPropertiesFixV1 
{

    private Map<String, String> mapping = new HashMap<String, String>();

    public EJRWTEJApplicationPropertiesFixV1()
    {
        // add renderer maping
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.EJRwtApplicationDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTApplicationDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.form.EJRwtFormRenderer",
                "org.entirej.applicationframework.rwt.renderers.form.EJRWTFormRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.blocks.EJRwtSingleRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.blocks.EJRWTSingleRecordBlockRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.blocks.EJRwtMultiRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.blocks.EJRWTMultiRecordBlockRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.blocks.EJRwtTreeRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.blocks.EJRWTTreeRecordBlockRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.blocks.EJRwtTreeTableRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.blocks.EJRWTTreeTableRecordBlockRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtTextItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTTextItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtTextAreaRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTTextAreaRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtNumberItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTNumberItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtDateItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTDateItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtDateTimeItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTDateTimeItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtLabelItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTLabelItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtButtonItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTButtonItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtComboItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTComboItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtRadioGroupItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTRadioGroupItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtCheckBoxItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTCheckBoxItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.EJRwtImageItemRenderer",
                "org.entirej.applicationframework.rwt.renderers.item.EJRWTImageItemRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.lov.EJRwtStandardLovRenderer",
                "org.entirej.applicationframework.rwt.renderers.lov.EJRWTStandardLovRenderer");
        mapping.put("org.entirej.applicationframework.rwt.renderers.lov.EJRwtLookupFormLovRenderer",
                "org.entirej.applicationframework.rwt.renderers.lov.EJRWTLookupFormLovRenderer");
        mapping.put("org.entirej.applicationframework.rwt.application.components.menu.EJRwtMenuTreeComponent",
                "org.entirej.applicationframework.rwt.application.components.menu.EJRWTMenuTreeComponent");
        mapping.put("org.entirej.applicationframework.rwt.application.form.containers.EJDefaultRwtFormContainerToolbar",
                "org.entirej.applicationframework.rwt.application.form.containers.EJRWTDefaultFormContainerToolbar");

        // add renderer def mapings
        mapping.put("org.entirej.applicationframework.rwt.renderers.form.definition.EJRwtFormRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.form.definition.EJRWTFormRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.block.definition.EJRwtSingleRecordBlockDefinition",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTSingleRecordBlockDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.block.definition.EJRwtMultiRecordBlockDefinition",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTMultiRecordBlockDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.block.definition.EJRwtTreeRecordBlockDefinition",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTTreeRecordBlockDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.block.definition.EJRwtTreeTableRecordBlockDefinition",
                "org.entirej.applicationframework.rwt.renderers.block.definition.EJRWTTreeTableRecordBlockDefinition");

        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtTextItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTTextItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtTextAreaRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTTextAreaRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtNumberItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTNumberItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtDateItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTDateItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtDateTimeItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTDateTimeItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtLabelItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTLabelItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtButtonItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTButtonItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtComboBoxRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTComboBoxRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtRadioGroupItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTRadioGroupItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtCheckBoxRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTCheckBoxRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.item.definition.EJRwtImageItemRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.item.definition.EJRWTImageItemRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.lov.definition.EJRwtStandardLovRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.lov.definition.EJRWTStandardLovRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.lov.definition.EJRwtLookupFormLovRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.lov.definition.EJRWTLookupFormLovRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.TabFormContainerRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTTabFormContainerRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.MenuTreeRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTMenuTreeRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.FormToolBarRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTFormToolBarRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.BannerRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTBannerRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.SingleFormRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTSingleFormRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.StackedFormContainerRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTStackedFormContainerRendererDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.application.StatusbarRendererDefinition",
                "org.entirej.applicationframework.rwt.renderers.application.EJRWTStatusbarRendererDefinition");
        
        mapping.put("org.entirej.applicationframework.rwt.renderers.mobile.block.definition.EJRwtListRecordBlockDefinition",
                "org.entirej.applicationframework.rwt.renderers.mobile.block.definition.EJRWTListRecordBlockDefinition");
        mapping.put("org.entirej.applicationframework.rwt.renderers.mobile.blocks.EJRwtListRecordBlockRenderer",
                "org.entirej.applicationframework.rwt.renderers.mobile.blocks.EJRWTListRecordBlockRenderer");
    }

   

    public String fixRendererName(String name)
    {
        if (mapping.containsKey(name))
        {
            return mapping.get(name);
        }
        return name;
    }

    public String fixRendererDefName(String name)
    {

        if (mapping.containsKey(name))
        {
            return mapping.get(name);
        }
        return name;
    }

    public String getUpdateVesion()
    {
        return "2.0";
    }

}
