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

import java.util.Collection;
import java.util.LinkedHashMap;

import org.entirej.framework.core.common.utils.EJParameterChecker;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;

public class EJDevPropertyDefinition implements EJPropertyDefinition
{
    /**
     * If the data type is DATATYPE_STRING, and the value that needs to be
     * entered can contain allot of data then the Plugins field can span more
     * than one row. The rows attribute defines how many rows the plugin item
     * will span
     */
    private int                           _rows                    = 1;
    private boolean                       _grabExcessVerticalSpace = false;
    private boolean                       _monospacedFont          = false;
    private EJPropertyDefinitionType      _propertyType;
    private String                        _name;
    private String                        _label;
    private String                        _description;
    private boolean                       _mandatory               = false;
    private boolean                       _multilingual            = false;
    private boolean                       _canBeSetProgramatically = false;
    private String                        _defaultValue;
    private String                        _classParent;
    private boolean                       _loadValidValuesDynamically;
    private boolean                       _notifyWhenChanged       = false;
    private LinkedHashMap<String, String> _validValues;
    private EJPropertyDefinitionGroup     _parentDefinitionGroup;
    private LinkedHashMap<String, String> _validLabels;

    /**
     * Creates a new instance of a <code>RendererProperty</code> with the given
     * name
     * 
     * @param name
     *            The name of this property
     * @param datatype
     *            The datatype of this property
     * @throws NullPointerException
     *             id the name is null
     */
    public EJDevPropertyDefinition(String name, EJPropertyDefinitionType type)
    {
        EJParameterChecker.checkNotZeroLength(name, "RendererProperty", "name");

        _name = name;
        _propertyType = type;

        _validValues = new LinkedHashMap<String, String>();
        _validLabels = new LinkedHashMap<String, String>();
    }

    public void setParentPropertyDefinitionGroup(EJPropertyDefinitionGroup group)
    {
        _parentDefinitionGroup = group;
    }

    /**
     * Returns the <code>RendererPropertyDefinitionGroup</code> that contains
     * this property definition
     * 
     * @return This properties definition group
     */
    public EJPropertyDefinitionGroup getParentPropertyDefinitionGroup()
    {
        return _parentDefinitionGroup;
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

    public String getDefaultValue()
    {
        return _defaultValue;
    }

    public void setDefaultValue(String defaultValue)
    {
        _defaultValue = defaultValue;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        _description = description;
    }

    public boolean isMandatory()
    {
        return _mandatory;
    }

    public void setMandatory(boolean mandatory)
    {
        _mandatory = mandatory;
    }

    public boolean isMultilingual()
    {
        return _multilingual;
    }

    public void setMultilingual(boolean multilingual)
    {
        _multilingual = multilingual;
    }

    public boolean canBeSetProgramatically()
    {
        return _canBeSetProgramatically;
    }

    public void setCanBeSetProgramatically(boolean canBeSetProgramatically)
    {
        _canBeSetProgramatically = canBeSetProgramatically;
    }

    public String getName()
    {
        return _name;
    }

    public EJPropertyDefinitionType getPropertyType()
    {
        return _propertyType;
    }

    public void setLoadValidValuesDynamically(boolean loadDynamically)
    {
        _loadValidValuesDynamically = loadDynamically;
    }

    public boolean loadValidValuesDynamically()
    {
        return _loadValidValuesDynamically;
    }

    public boolean notifyWhenChanged()
    {
        return _notifyWhenChanged;
    }

    public void setNotifyWhenChanged(boolean notify)
    {
        _notifyWhenChanged = notify;
    }

    public boolean hasValidValues()
    {
        if (_validValues.size() > 0)
        {
            return true;
        }
        else
        {
            return false;
        }
    }

    public boolean isValidValue(String value)
    {
        if (hasValidValues())
        {
            if ((value == null || value.trim().length() == 0) && isMandatory())
            {
                return false;
            }
            else if ((value == null || value.trim().length() == 0) && (!isMandatory()))
            {
                return true;
            }
            return _validValues.containsKey(value);
        }
        else
        {
            return true;
        }
    }

    public Collection<String> getValidValueLabels()
    {
        return _validValues.keySet();
    }

    public String getValidValueNameForLabel(String label)
    {
        return _validValues.get(label);
    }

    public String getLabelForValidValue(String value)
    {
        return _validLabels.get(value);
    }

    public void addValidValue(String name, String label)
    {
        EJParameterChecker.checkNotZeroLength(name, "addValidValue", "name");
        EJParameterChecker.checkNotZeroLength(label, "addValidValue", "label");

        _validValues.put(label, name);
        _validLabels.put(name, label);
    }

    /**
     * Clears any valid values added to this property definition
     */
    public void clearValidValues()
    {
        _validValues.clear();
    }

    public void setUseMonospacedFont(boolean use)
    {
        _monospacedFont = use;
    }

    public boolean useMonospacedFont()
    {
        return _monospacedFont;
    }

    public void setRowSpan(int span)
    {
        if (span > 0)
        {
            _rows = span;
        }
    }

    public int getRowSpan()
    {
        return _rows;
    }

    public boolean getGrabExcessVerticalSpace()
    {
        return _grabExcessVerticalSpace;
    }

    public void setGrabExcessVerticalSpace()
    {
        _grabExcessVerticalSpace = true;
    }

    public void setClassParent(String classParent)
    {
        this._classParent = classParent;
    }

    public String getClassParent()
    {
        return _classParent;
    }

}
