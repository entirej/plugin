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
package org.entirej.framework.dev.properties;

import java.util.ArrayList;
import java.util.Collection;

import org.entirej.framework.core.common.utils.EJParameterChecker;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionList;

public class EJDevPropertyDefinitionList implements EJPropertyDefinitionList
{
    private String                          _name;
    private String                          _label;
    private String                          _description;
    private ArrayList<EJPropertyDefinition> _properties;
    private boolean                         _notifyWhenChanged = false;

    public boolean notifyWhenChanged()
    {
        return _notifyWhenChanged;
    }

    public void setNotifyWhenChanged(boolean notify)
    {
        _notifyWhenChanged = notify;
    }

    /**
     * Creates an instance of this <code>PropertyDefinitionList</code> with the
     * given name
     * <p>
     * A <code>PropertyDefinitionList</code> contains a list of
     * <code>PropertyDefinition</code>s.
     * <p>
     * Property Lists will be displayed as a table within the extension control.
     * The user will be able to add, edit and remove values
     * 
     * @param name
     *            The name of this group
     */
    public EJDevPropertyDefinitionList(String name)
    {
        this(name, null);
    }

    /**
     * Creates an instance of this <code>PropertyDefinitionList</code> with the
     * given name
     * <p>
     * A <code>PropertyDefinitionList</code> contains a list of
     * <code>PropertyDefinition</code>s.
     * <p>
     * Property Lists will be displayed as a table within the extension control.
     * The user will be able to add, edit and remove values
     * 
     * @param name
     *            The name of this group
     * @param label
     *            The label to display for this list
     */
    public EJDevPropertyDefinitionList(String name, String label)
    {
        EJParameterChecker.checkNotZeroLength(name, "PropertyDefinitionList", "name");

        _name = name;
        _label = label;
        _properties = new ArrayList<EJPropertyDefinition>();
    }

    public String getName()
    {
        return _name;
    }

    public void setLabel(String label)
    {
        _label = label;
    }

    public String getLabel()
    {
        if (_label == null || _label.trim().length() == 0)
        {
            return _name;
        }
        else
        {
            return _label;
        }
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        this._description = description;
    }

    public void addPropertyDefinition(EJPropertyDefinition definition)
    {
        EJParameterChecker.checkNotNull(definition, "addPropertyDefinition", "definition");

        _properties.add(definition);
    }

    public Collection<EJPropertyDefinition> getPropertyDefinitions()
    {
        return _properties;
    }

}
