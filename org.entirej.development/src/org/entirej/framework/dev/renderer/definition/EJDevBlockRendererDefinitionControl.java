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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.entirej.framework.dev.properties.interfaces.EJDevBlockDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockWidgetChosenListener;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;

public class EJDevBlockRendererDefinitionControl
{
    private EJDevBlockDisplayProperties                     _blockDisplayProperties;
    private Map<String, EJDevItemRendererDefinitionControl> _itemRendererControls;
    private ArrayList<EJDevBlockWidgetChosenListener>       _listeners;

    private EJDevItemRendererDefinitionControl              _selectedItem;

    public EJDevBlockRendererDefinitionControl(EJDevBlockDisplayProperties blockDisplayProperties, List<EJDevItemRendererDefinitionControl> containedItems)
    {
        _itemRendererControls = new HashMap<String, EJDevItemRendererDefinitionControl>();
        _listeners = new ArrayList<EJDevBlockWidgetChosenListener>();

        _blockDisplayProperties = blockDisplayProperties;

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
        _blockDisplayProperties = null;
        for (EJDevItemRendererDefinitionControl control : _itemRendererControls.values())
        {
            control.dispose();
        }
        _listeners.clear();
        _selectedItem = null;
    }

    /**
     * Returns the name of the block that this control is for
     * 
     * @return The name of the block
     */
    public String getBlockName()
    {
        return _blockDisplayProperties.getName();
    }

    /**
     * Returns the name of the item that is currently selected within this block
     * or <code>null</code> if no item is selected
     * 
     * @return The name of the currently selected item within this block
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
     * Returns the block display properties of this control
     * 
     * @return The block display properties
     */
    public EJDevBlockDisplayProperties getBlockDisplayProperties()
    {
        return _blockDisplayProperties;
    }

    /**
     * Returns a collection of all ItemRendererDefinitionControl contained
     * within this block renderer definition control
     * 
     * @return A collection of all ItemRendererDefinitionControl contained
     *         within this block renderer definition control
     */
    public Collection<EJDevItemRendererDefinitionControl> getAllItemRendererDefinitionControls()
    {
        return _itemRendererControls.values();
    }

    /**
     * The block chosen listener should be informed if the user clicks on the
     * widget.
     * <p>
     * This will inform the EntireJ Form Plugin that the properties of the given
     * widget should be selected
     * <p>
     * If the user chooses a different widget within the properties tree of the
     * plugin, then the plugin will call the
     * <code>{@link #blockWidgetSelected(boolean)}</code>
     * 
     * @param listener
     *            The listener to add
     */
    public void addBlockWidgetChosenListener(EJDevBlockWidgetChosenListener listener)
    {
        if (listener != null)
        {
            _listeners.add(listener);
        }
    }

    /**
     * The item chosen listener should be informed if the user clicks on an item
     * widget displayed on this block control
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
     * Removes a listener from this objects list of block renderer chosen
     * listeners
     * 
     * @param listener
     *            The <code>EJDevBlockWidgetChosenListener</code> to remove
     */
    public void removeBlockWidgetChosenListener(EJDevBlockWidgetChosenListener listener)
    {
        if (listener != null)
        {
            _listeners.remove(listener);
        }
    }

    /**
     * Removes a listener from this blocks list of item renderer chosen
     * listeners
     * 
     * @param listener
     *            The <code>EJDevItemWidgetChosenListener</code> to remove
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
     * @param itemName
     *            The name of the item that was selected
     * @param <code>true</code> indicates the property was selected,
     *        <code>false</code> indicated that it was de-selected
     */
    public void blockItemWidgetSelected(String itemName, boolean selected)
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
