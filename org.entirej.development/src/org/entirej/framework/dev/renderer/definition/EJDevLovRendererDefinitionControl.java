/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
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
package org.entirej.framework.dev.renderer.definition;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.entirej.framework.dev.properties.interfaces.EJDevLovDefinitionDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;

public class EJDevLovRendererDefinitionControl
{
    private EJDevLovDefinitionDisplayProperties             _lovDisplayProperties;
    private Map<String, EJDevItemRendererDefinitionControl> _itemRendererControls;

    private EJDevItemRendererDefinitionControl              _selectedItem;

    public EJDevLovRendererDefinitionControl(EJDevLovDefinitionDisplayProperties lovDisplayProperties, List<EJDevItemRendererDefinitionControl> containedItems)
    {
        _itemRendererControls = new HashMap<String, EJDevItemRendererDefinitionControl>();
        _lovDisplayProperties = lovDisplayProperties;

        if (containedItems != null)
        {
            for (EJDevItemRendererDefinitionControl control : containedItems)
            {
                if (control != null)
                {
                    _itemRendererControls.put(control.getItemName(), control);
                }
            }
        }
    }

    public void dispose()
    {
        _lovDisplayProperties = null;
        for (EJDevItemRendererDefinitionControl control : _itemRendererControls.values())
        {
            control.dispose();
        }
        _itemRendererControls.clear();
        _selectedItem = null;
    }

    /**
     * Returns the name of the lov definition that this control is for
     * 
     * @return The name of the block
     */
    public String getBlockName()
    {
        return _lovDisplayProperties.getName();
    }

    /**
     * Returns the name of the item that is currently selected within this lov
     * definition or <code>null</code> if no item is selected
     * 
     * @return The name of the currently selected item within this lov
     *         definition
     */
    public String getCurrentItemName()
    {
        if (_selectedItem == null)
        {
            return null;
        }
        else
        {
            return _selectedItem.getItemName();
        }

    }

    /**
     * Returns the lov definition display properties of this control
     * 
     * @return The lov definition display properties
     */
    public EJDevLovDefinitionDisplayProperties getLovDefinitionDisplayProperties()
    {
        return _lovDisplayProperties;
    }

    /**
     * The item chosen listener should be informed if the user clicks on an item
     * widget displayed on this lov definition control
     * <p>
     * This will inform the EntireJ Form Plugin that the properties of the given
     * widget should be selected
     * 
     * @param listener
     *            The listener to add
     */
    public void addItemWidgetChosenListener(EJDevItemWidgetChosenListener listener)
    {
        for (EJDevItemRendererDefinitionControl control : _itemRendererControls.values())
        {
            control.addItemChosenListener(listener);
        }
    }

    /**
     * Removes a listener from this lov definition list of item renderer chosen
     * listeners
     * 
     * @param listener
     *            The <code>IItemWidgetChosenListener</code> to remove
     */
    public void removeItemWidgetChosenListener(EJDevItemWidgetChosenListener listener)
    {
        for (EJDevItemRendererDefinitionControl control : _itemRendererControls.values())
        {
            control.removeItemWidgetChosenListener(listener);
        }
    }

    /**
     * The EntireJ Plugin will inform this control when one of the block items
     * has been selected from the form definition list
     * 
     * @param itemProperties
     *            The properties of the item that was selected
     */
    public void lovItemWidgetSelected(String itemName, boolean selected)
    {
        if (itemName != null)
        {
            EJDevItemRendererDefinitionControl control = _itemRendererControls.get(itemName);
            focusLost();

            if (control != null)
            {
                control.itemWidgetSelected(selected);
                _selectedItem = control;
            }
        }
    }

    /**
     * This should be called when the block looses focus. If not called the
     * currently selected item will be selected even if another item is selected
     * within another block
     */
    public void focusLost()
    {
        if (_selectedItem != null)
        {
            _selectedItem.itemWidgetSelected(false);
            _selectedItem = null;
        }
    }

    public void focusGained()
    {
        if (_selectedItem != null)
        {
            _selectedItem.itemWidgetSelected(true);
        }
    }

}
