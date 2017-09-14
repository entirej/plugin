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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.entirej.framework.dev.properties.interfaces.EJDevScreenItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemWidgetChosenListener;

public class EJDevItemRendererDefinitionControl
{
    private ArrayList<EJDevItemWidgetChosenListener> _widgetChosenListeners;
    private EJDevScreenItemDisplayProperties         _itemProperties;
    private Control                                  _itemControl;
    private Color                                    _unselectedColor;
    private boolean                                  useFontDimensions = true;

    public EJDevItemRendererDefinitionControl(EJDevScreenItemDisplayProperties itemProperties, Control itemControl)
    {
        _widgetChosenListeners = new ArrayList<EJDevItemWidgetChosenListener>();

        _itemControl = itemControl;
        _itemProperties = itemProperties;
        _itemControl.addMouseListener(new MouseAdapter()
        {

            @Override
            public void mouseDown(MouseEvent e)
            {
                fireFocusGained();
            }

        });
    }

    public EJDevItemRendererDefinitionControl(EJDevScreenItemDisplayProperties itemProperties, Control itemControl, boolean useFontDimensions)
    {
        this(itemProperties, itemControl);
        this.useFontDimensions = useFontDimensions;
    }

    public boolean useFontDimensions()
    {
        return useFontDimensions;
    }

    public void setUseFontDimensions(boolean useFontDimensions)
    {
        this.useFontDimensions = useFontDimensions;
    }

    public void dispose()
    {
        _widgetChosenListeners.clear();
        _itemProperties = null;
        _itemControl.dispose();
    }

    /**
     * Returns the name of the item that this control displays
     * 
     * @return The name of the item
     */
    public String getItemName()
    {
        return _itemProperties.getReferencedItemName();
    }

    /**
     * The item chosen listener should be informed if the user clicks on the
     * widget.
     * <p>
     * This will inform the EntireJ Form Plugin that the properties of the given
     * widget should be selected
     * <p>
     * If the user chooses a different widget within the properties tree of the
     * plugin, then the plugin will call the
     * <code>{@link #itemRendererSelected(boolean)}</code>
     * 
     * @param listener
     *            The listener to add
     */
    public void addItemChosenListener(EJDevItemWidgetChosenListener listener)
    {
        if (listener != null)
        {
            _widgetChosenListeners.add(listener);
        }
    }

    /**
     * Used to return the control for this item
     * <p>
     * The item should be added to the given <code>Control</code> and returned
     * from this method
     * 
     * @param parent
     *            The <code>Composite</code> that will contain this item
     * @param screenDisplayProperties
     *            The screen properties for the screen upon which this item will
     *            be displayed
     * @param formToolkit
     *            The toolkit to use for the creation of the item widget
     * @return This items, plugin GUI widget
     */
    public Control getItemControl()
    {
        return _itemControl;
    }

    /**
     * The EntireJ Form Plugin will inform the renderer has been chosen. If the
     * renderer is the currently selected item within the plugin and another
     * widget is selected this method will also be called.
     * 
     * @param selected
     *            <code>true</code> if the item has been selected,
     *            <code>false</code> if this renderer is de-selected
     */
    public void itemWidgetSelected(boolean selected)
    {
        if (selected)
        {
            _unselectedColor = _itemControl.getBackground();
            _itemControl.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_GREEN));
        }
        else
        {
            if (_unselectedColor != null && (!_unselectedColor.isDisposed()))
            {
                _itemControl.setBackground(_unselectedColor);
            }
            else if (_itemControl.getParent() != null)
            {
                _itemControl.setBackground(_itemControl.getParent().getBackground());
            }
            _itemControl.redraw();
        }
    }

    /**
     * Removes a listener from this objects list of item renderer chosen
     * listeners
     * 
     * @param listener
     *            The <code>EJDevItemWidgetChosenListener</code> to remove
     */
    public void removeItemWidgetChosenListener(EJDevItemWidgetChosenListener listener)
    {
        if (listener != null)
        {
            _widgetChosenListeners.remove(listener);
        }
    }

    /**
     * Called by item renderer definitions when the item control does not accept
     * focus and therefore cannot show the properties in the properties editor
     * <p>
     * If for example, the item control is a button and the button does not
     * accept item focus, then when the buttons selection listener fires, then
     * this method can be called thereby indicating to the EJ editor that the
     * items properties need to be displayed
     */
    public void fireFocusGained()
    {
        for (EJDevItemWidgetChosenListener listener : _widgetChosenListeners)
        {
            listener.fireRendererChosen(_itemProperties);
        }
    }
}
