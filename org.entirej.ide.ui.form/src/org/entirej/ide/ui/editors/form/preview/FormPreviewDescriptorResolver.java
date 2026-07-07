/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 *
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ide.ui.editors.form.preview;

import org.entirej.framework.core.enumerations.EJCanvasType;
import org.entirej.framework.core.properties.EJCoreLayoutItem;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewDescriptor;
import org.entirej.framework.dev.renderer.definition.EJDevPreviewKind;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevAppComponentPreviewProvider;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockPreviewProvider;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenPreviewProvider;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemPreviewProvider;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenPreviewProvider;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenPreviewProvider;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginCanvasProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginItemGroupProperties;
import org.entirej.framework.plugin.framework.properties.ExtensionsPropertiesFactory;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginScreenItemProperties;

public class FormPreviewDescriptorResolver
{
    public EJDevPreviewDescriptor forCanvas(EJPluginCanvasProperties canvas)
    {
        if (canvas == null)
        {
            return EJDevPreviewDescriptor.box(null, "Canvas", null);
        }
        EJDevPreviewKind kind = canvasKind(canvas.getType());
        String label = value(canvas.getGroupFrameTitle(), canvas.getPopupPageTitle(), canvas.getName());
        return EJDevPreviewDescriptor.create(kind, canvas.getName(), label, canvas.getType() == null ? null : canvas.getType().name(), canvas.getWidth(),
                canvas.getHeight());
    }

    public EJDevPreviewDescriptor forBlock(EJPluginBlockProperties block)
    {
        if (block == null)
        {
            return EJDevPreviewDescriptor.box(null, "Block", null);
        }

        EJDevBlockRendererDefinition definition = block.getBlockRendererDefinition();
        if (definition instanceof EJDevBlockPreviewProvider)
        {
            EJDevPreviewDescriptor descriptor = ((EJDevBlockPreviewProvider) definition).getBlockPreviewDescriptor(block.getMainScreenProperties(), block);
            if (descriptor != null)
            {
                return descriptor.withName(block.getName()).withRendererName(block.getBlockRendererName());
            }
        }

        return EJDevPreviewDescriptor.create(inferBlockKind(block.getBlockRendererName(), definition), block.getName(), block.getName(),
                block.getBlockRendererName(), block.getMainScreenProperties().getWidth(), block.getMainScreenProperties().getHeight());
    }

    public EJDevPreviewDescriptor forItemGroup(EJPluginItemGroupProperties group)
    {
        if (group == null)
        {
            return EJDevPreviewDescriptor.box(null, "Item Group", null);
        }
        if (group.isSeparator())
        {
            return EJDevPreviewDescriptor.create(EJDevPreviewKind.SEPARATOR, group.getName(), group.getName(), "separator", group.getWidth(), group
                    .getHeight());
        }
        return EJDevPreviewDescriptor.create(EJDevPreviewKind.GROUP, group.getName(), value(group.getFrameTitle(), group.getName()), null, group.getWidth(),
                group.getHeight());
    }

    public EJDevPreviewDescriptor forInsertScreen(EJPluginBlockProperties block)
    {
        EJDevInsertScreenRendererDefinition definition = block == null ? null : block.getInsertScreenRendererDefinition();
        if (definition instanceof EJDevInsertScreenPreviewProvider)
        {
            EJDevPreviewDescriptor descriptor = ((EJDevInsertScreenPreviewProvider) definition).getInsertScreenPreviewDescriptor(block);
            if (descriptor != null)
            {
                return applyScreenIdentity(descriptor, block, "insert", "Insert Screen", definition);
            }
        }
        return applyScreenIdentity(EJDevPreviewDescriptor.box(null, "Insert Screen", rendererName(definition)), block, "insert", "Insert Screen",
                definition);
    }

    public EJDevPreviewDescriptor forUpdateScreen(EJPluginBlockProperties block)
    {
        EJDevUpdateScreenRendererDefinition definition = block == null ? null : block.getUpdateScreenRendererDefinition();
        if (definition instanceof EJDevUpdateScreenPreviewProvider)
        {
            EJDevPreviewDescriptor descriptor = ((EJDevUpdateScreenPreviewProvider) definition).getUpdateScreenPreviewDescriptor(block);
            if (descriptor != null)
            {
                return applyScreenIdentity(descriptor, block, "update", "Update Screen", definition);
            }
        }
        return applyScreenIdentity(EJDevPreviewDescriptor.box(null, "Update Screen", rendererName(definition)), block, "update", "Update Screen",
                definition);
    }

    public EJDevPreviewDescriptor forQueryScreen(EJPluginBlockProperties block)
    {
        EJDevQueryScreenRendererDefinition definition = block == null ? null : block.getQueryScreenRendererDefinition();
        if (definition instanceof EJDevQueryScreenPreviewProvider)
        {
            EJDevPreviewDescriptor descriptor = ((EJDevQueryScreenPreviewProvider) definition).getQueryScreenPreviewDescriptor(block);
            if (descriptor != null)
            {
                return applyScreenIdentity(descriptor, block, "query", "Query Screen", definition);
            }
        }
        return applyScreenIdentity(EJDevPreviewDescriptor.box(null, "Query Screen", rendererName(definition)), block, "query", "Query Screen",
                definition);
    }

    public EJDevPreviewDescriptor forScreenItem(EJPluginScreenItemProperties screenItem)
    {
        if (screenItem == null)
        {
            return EJDevPreviewDescriptor.box(null, "Item", null);
        }
        if (screenItem.isSeparator())
        {
            return EJDevPreviewDescriptor.create(EJDevPreviewKind.SEPARATOR, screenItem.getName(), screenItem.getName(), null, 0, 0);
        }
        if (screenItem.isSpacerItem())
        {
            return EJDevPreviewDescriptor.create(EJDevPreviewKind.SPACER, screenItem.getName(), screenItem.getName(), null, 0, 0);
        }

        EJDevBlockItemDisplayProperties blockItem = screenItem.getBlockItemDisplayProperties();
        EJDevItemRendererDefinition definition = blockItem == null ? null : blockItem.getItemRendererDefinition();
        if (definition instanceof EJDevItemPreviewProvider)
        {
            EJDevPreviewDescriptor descriptor = ((EJDevItemPreviewProvider) definition).getItemPreviewDescriptor(screenItem);
            if (descriptor != null)
            {
                return descriptor.withName(screenItem.getReferencedItemName()).withLabel(value(screenItem.getLabel(), screenItem.getReferencedItemName()))
                        .withRendererName(blockItem.getItemRendererName());
            }
        }

        String rendererName = blockItem == null ? null : blockItem.getItemRendererName();
        return EJDevPreviewDescriptor.create(inferItemKind(rendererName, definition), screenItem.getReferencedItemName(),
                value(screenItem.getLabel(), screenItem.getReferencedItemName()), rendererName, 0, 0);
    }

    public EJDevPreviewDescriptor forApplicationComponent(EJPluginEntireJProperties properties, EJCoreLayoutItem.LayoutComponent component)
    {
        if (component == null)
        {
            return EJDevPreviewDescriptor.box(null, "Component", null);
        }
        Object definition = ExtensionsPropertiesFactory.loadAppComponentDefinition(properties, component.getRenderer());
        if (definition instanceof EJDevAppComponentPreviewProvider)
        {
            EJDevPreviewDescriptor descriptor = ((EJDevAppComponentPreviewProvider) definition).getAppComponentPreviewDescriptor(component.getRenderer(),
                    component.getRendereProperties());
            if (descriptor != null)
            {
                return descriptor.withName(component.getName()).withRendererName(component.getRenderer());
            }
        }
        return EJDevPreviewDescriptor.create(inferAppComponentKind(component.getRenderer(), definition), component.getName(),
                value(component.getTitle(), component.getName(), component.getRenderer()), component.getRenderer(), component.getHintWidth(),
                component.getHintHeight());
    }

    public EJDevPreviewDescriptor forLayoutItem(EJCoreLayoutItem item)
    {
        if (item == null)
        {
            return EJDevPreviewDescriptor.box(null, "Layout", null);
        }
        switch (item.getType())
        {
            case GROUP:
                return EJDevPreviewDescriptor.create(EJDevPreviewKind.GROUP, item.getName(), value(item.getTitle(), item.getName()), item.getType().name(),
                        item.getHintWidth(), item.getHintHeight());
            case SPACE:
                return EJDevPreviewDescriptor.create(EJDevPreviewKind.SPACE, item.getName(), value(item.getTitle(), item.getName()), item.getType().name(),
                        item.getHintWidth(), item.getHintHeight());
            case SPLIT:
                return EJDevPreviewDescriptor.create(EJDevPreviewKind.SPLIT, item.getName(), value(item.getTitle(), item.getName()), item.getType().name(),
                        item.getHintWidth(), item.getHintHeight());
            case TAB:
                return EJDevPreviewDescriptor.create(EJDevPreviewKind.TAB_FOLDER, item.getName(), value(item.getTitle(), item.getName()),
                        item.getType().name(), item.getHintWidth(), item.getHintHeight());
            case COMPONENT:
            default:
                return EJDevPreviewDescriptor.create(EJDevPreviewKind.APP_COMPONENT, item.getName(), value(item.getTitle(), item.getName()),
                        item.getType().name(), item.getHintWidth(), item.getHintHeight());
        }
    }

    private EJDevPreviewKind canvasKind(EJCanvasType type)
    {
        if (type == null)
        {
            return EJDevPreviewKind.CANVAS;
        }
        switch (type)
        {
            case BLOCK:
                return EJDevPreviewKind.BLOCK;
            case GROUP:
            case FORM:
                return EJDevPreviewKind.GROUP;
            case SPLIT:
                return EJDevPreviewKind.SPLIT;
            case TAB:
                return EJDevPreviewKind.TAB_FOLDER;
            case DRAWER:
                return EJDevPreviewKind.DRAWER;
            case POPUP:
                return EJDevPreviewKind.POPUP;
            case STACKED:
                return EJDevPreviewKind.STACKED;
            case SEPARATOR:
                return EJDevPreviewKind.SEPARATOR;
            default:
                return EJDevPreviewKind.CANVAS;
        }
    }

    private EJDevPreviewKind inferBlockKind(String rendererName, Object definition)
    {
        String name = normalized(rendererName, definition);
        if (contains(name, "tree"))
        {
            return EJDevPreviewKind.TREE;
        }
        if (contains(name, "table", "multi"))
        {
            return EJDevPreviewKind.TABLE;
        }
        if (contains(name, "chart", "pie", "bar", "line", "radar", "diagram"))
        {
            return EJDevPreviewKind.CHART;
        }
        return EJDevPreviewKind.BLOCK;
    }

    private EJDevPreviewKind inferItemKind(String rendererName, Object definition)
    {
        String name = normalized(rendererName, definition);
        if (contains(name, "textarea", "text_area", "text area"))
        {
            return EJDevPreviewKind.TEXT_AREA;
        }
        if (contains(name, "number"))
        {
            return EJDevPreviewKind.NUMBER_FIELD;
        }
        if (contains(name, "datetime"))
        {
            return EJDevPreviewKind.DATE_TIME_FIELD;
        }
        if (contains(name, "date"))
        {
            return EJDevPreviewKind.DATE_FIELD;
        }
        if (contains(name, "check"))
        {
            return EJDevPreviewKind.CHECKBOX;
        }
        if (contains(name, "radio"))
        {
            return EJDevPreviewKind.RADIO_GROUP;
        }
        if (contains(name, "combo", "dropdown"))
        {
            return EJDevPreviewKind.COMBO;
        }
        if (contains(name, "listbox", "list"))
        {
            return EJDevPreviewKind.LIST;
        }
        if (contains(name, "button", "help"))
        {
            return EJDevPreviewKind.BUTTON;
        }
        if (contains(name, "label"))
        {
            return EJDevPreviewKind.LABEL;
        }
        if (contains(name, "image"))
        {
            return EJDevPreviewKind.IMAGE;
        }
        if (contains(name, "html", "editor"))
        {
            return EJDevPreviewKind.HTML;
        }
        if (contains(name, "stacked", "stackitem"))
        {
            return EJDevPreviewKind.STACKED;
        }
        if (contains(name, "text"))
        {
            return EJDevPreviewKind.TEXT_FIELD;
        }
        return EJDevPreviewKind.UNKNOWN;
    }

    private EJDevPreviewKind inferAppComponentKind(String rendererName, Object definition)
    {
        String name = normalized(rendererName, definition);
        if (contains(name, "menu", "tree"))
        {
            return EJDevPreviewKind.MENU_TREE;
        }
        if (contains(name, "toolbar"))
        {
            return EJDevPreviewKind.TOOLBAR;
        }
        if (contains(name, "status"))
        {
            return EJDevPreviewKind.STATUS_BAR;
        }
        if (contains(name, "banner"))
        {
            return EJDevPreviewKind.BANNER;
        }
        if (contains(name, "tab"))
        {
            return EJDevPreviewKind.TAB_FOLDER;
        }
        if (contains(name, "stack"))
        {
            return EJDevPreviewKind.STACKED;
        }
        if (contains(name, "form"))
        {
            return EJDevPreviewKind.FORM_CONTAINER;
        }
        return EJDevPreviewKind.APP_COMPONENT;
    }

    private String normalized(String rendererName, Object definition)
    {
        StringBuilder builder = new StringBuilder();
        if (rendererName != null)
        {
            builder.append(rendererName);
        }
        if (definition != null)
        {
            builder.append(' ');
            builder.append(definition.getClass().getSimpleName());
        }
        return builder.toString().toLowerCase();
    }

    private EJDevPreviewDescriptor applyScreenIdentity(EJDevPreviewDescriptor descriptor, EJPluginBlockProperties block, String suffix,
            String fallbackLabel, Object definition)
    {
        String blockName = block == null ? null : block.getName();
        String name = blockName == null ? suffix : blockName + "." + suffix;
        return descriptor.withName(name).withLabel(value(descriptor.getLabel(), fallbackLabel)).withRendererName(value(descriptor.getRendererName(),
                rendererName(definition)));
    }

    private String rendererName(Object definition)
    {
        return definition == null ? null : definition.getClass().getSimpleName();
    }

    private boolean contains(String value, String... fragments)
    {
        for (String fragment : fragments)
        {
            if (value.indexOf(fragment) > -1)
            {
                return true;
            }
        }
        return false;
    }

    private String value(String... values)
    {
        for (String value : values)
        {
            if (value != null && value.trim().length() > 0)
            {
                return value;
            }
        }
        return null;
    }
}
