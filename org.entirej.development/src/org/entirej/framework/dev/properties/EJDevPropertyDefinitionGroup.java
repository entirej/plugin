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
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionList;

public class EJDevPropertyDefinitionGroup implements EJPropertyDefinitionGroup
{
    private EJPropertyDefinitionGroup            _parentGroup;
    private String                               _name;
    private String                               _label;
    private String                               _description;

    private ArrayList<EJPropertyDefinition>      _properties;
    private ArrayList<EJPropertyDefinitionGroup> _subGroups;
    private ArrayList<EJPropertyDefinitionList>  _propertyLists;

    /**
     * Creates an instance of this <code>PropertyDefinitionGroup</code> with the
     * given name
     * <p>
     * A <code>PropertyDefinitionGroup</code> contains a list of
     * <code>PropertyDefinition</code>s as well as any sub groups. Groups will
     * be displayed within the properties sheet within the <b>EntireJ Framework
     * Plugin</b> when the corresponding item is selected. Properties can be
     * sorted into groups, this class defines this grouping
     * <p>
     * The main group, ie. the group that contains all other groups will not be
     * displayed and therefore the name and label will be ignored.
     * 
     * @param name
     *            The name of this group
     */
    public EJDevPropertyDefinitionGroup(String name)
    {
        EJParameterChecker.checkNotZeroLength(name, "PropertyDefinitionGroup", "name");
        _name = name;

        _properties = new ArrayList<EJPropertyDefinition>();
        _subGroups = new ArrayList<EJPropertyDefinitionGroup>();
        _propertyLists = new ArrayList<EJPropertyDefinitionList>();
    }

    /**
     * Creates an instance of this <code>PropertyDefinitionGroup</code> with the
     * given name
     * <p>
     * A <code>PropertyDefinitionGroup</code> contains a list of
     * <code>PropertyDefinition</code>s as well as any sub groups. Groups will
     * be displayed within the properties sheet within the <b>EntireJ Framework
     * Plugin</b> when the corresponding item is selected. Properties can be
     * sorted into groups, this class defines this grouping
     * <p>
     * The main group, ie. the group that contains all other groups will not be
     * displayed and therefor the name and label will be ignored.
     * 
     * @param name
     *            The name of this group
     */
    public EJDevPropertyDefinitionGroup(String name, String label)
    {
        EJParameterChecker.checkNotZeroLength(name, "PropertyDefinitionGroup", "name");
        EJParameterChecker.checkNotZeroLength(label, "PropertyDefinitionGroup", "label");

        _name = name;
        _label = label;

        _properties = new ArrayList<EJPropertyDefinition>();
        _subGroups = new ArrayList<EJPropertyDefinitionGroup>();
        _propertyLists = new ArrayList<EJPropertyDefinitionList>();
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
        return _label;
    }

    public String getDescription()
    {
        return _description;
    }

    public void setDescription(String description)
    {
        this._description = description;
    }

    public String getFullGroupName()
    {
        StringBuffer name = new StringBuffer();

        if (_parentGroup == null)
        {
            return "";
        }
        else
        {
            name.append(_parentGroup.getFullGroupName());
            if (name.length() > 0)
            {
                name.append(".");
            }
            name.append(getName());
            return name.toString();
        }

    }

    public void setParentGroup(EJPropertyDefinitionGroup group)
    {
        _parentGroup = group;
    }

    public void addPropertyDefinition(EJPropertyDefinition definition)
    {
        EJParameterChecker.checkNotNull(definition, "addPropertyDefinition", "definition");

        definition.setParentPropertyDefinitionGroup(this);
        _properties.add(definition);
    }

    public void addSubGroup(EJPropertyDefinitionGroup group)
    {
        EJParameterChecker.checkNotNull(group, "addSubGroup", "group");

        group.setParentGroup(this);
        _subGroups.add(group);
    }

    public void addPropertyDefinitionList(EJPropertyDefinitionList definitionList)
    {
        EJParameterChecker.checkNotNull(definitionList, "addPropertyDefinitionList", "definitionList");

        _propertyLists.add(definitionList);
    }

    public Collection<EJPropertyDefinitionList> getPropertyDefinitionLists()
    {
        return _propertyLists;
    }

    public Collection<EJPropertyDefinitionGroup> getSubGroups()
    {
        return _subGroups;
    }

    public Collection<EJPropertyDefinition> getPropertyDefinitions()
    {
        return _properties;
    }

}
