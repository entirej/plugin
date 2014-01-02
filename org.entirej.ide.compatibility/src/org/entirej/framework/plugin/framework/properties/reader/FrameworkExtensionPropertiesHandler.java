/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 * Contributors: Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.framework.properties.reader;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperties;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperty;
import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinition;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJFormProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FrameworkExtensionPropertiesHandler extends EntireJTagHandler
{
    private String                             _exitTag              = "";
    private EJFormProperties                   _formProperties;
    private EJBlockProperties                  _blockProperties;
    private EJCoreFrameworkExtensionProperties _mainPropertyGroup;
    private EJCoreFrameworkExtensionProperties _currentPropertyGroup;
    
    private static final String                ELEMENT_GROUP         = "propertyGroup";
    private static final String                ELEMENT_PROPERTY      = "property";
    private static final String                ELEMENT_PROPERTY_LIST = "propertyList";
    
    private EJCoreFrameworkExtensionProperty   _currentProperty;
    
    public FrameworkExtensionPropertiesHandler(EJFormProperties formProperties, EJBlockProperties blockProperties, String exitTag)
    {
        _formProperties = formProperties;
        _blockProperties = blockProperties;
        _mainPropertyGroup = new EJCoreFrameworkExtensionProperties(formProperties, _blockProperties, "MAIN", null);
        _currentPropertyGroup = _mainPropertyGroup;
        _exitTag = exitTag;
    }
    
    public EJCoreFrameworkExtensionProperties getMainPropertiesGroup()
    {
        return _mainPropertyGroup;
    }
    
    public EJCoreFrameworkExtensionProperties getMainPropertiesGroup(EJPropertyDefinitionGroup base)
    {
        
        if (base != null)
        {
            validateProperties(base, _mainPropertyGroup);
        }
        
        return _mainPropertyGroup;
    }
    
    protected void validateProperties(EJPropertyDefinitionGroup group, EJCoreFrameworkExtensionProperties groupProp)
    {
        if (group != null && groupProp != null)
        {
            Collection<EJPropertyDefinition> propertyDefinitions = group.getPropertyDefinitions();
            List<String> basePropName = new ArrayList<String>();
            for (EJPropertyDefinition def : propertyDefinitions)
            {
                basePropName.add(def.getName());
                if (!groupProp.propertyExists(def.getName()))
                {
                    EJCoreFrameworkExtensionProperty property = new EJCoreFrameworkExtensionProperty(def.getPropertyType(), def.getName(), def.isMultilingual());
                    property.setValue(def.getDefaultValue());
                    groupProp.addProperty(property);
                }
            }
            
            Collection<EJPropertyDefinitionGroup> subGroups = group.getSubGroups();
            for (EJPropertyDefinitionGroup subGroup : subGroups)
            {
                EJCoreFrameworkExtensionProperties propertyGroup = groupProp.getPropertyGroup(subGroup.getName());
                if (propertyGroup == null)
                {
                    propertyGroup = new EJCoreFrameworkExtensionProperties(_formProperties, _blockProperties, subGroup.getName(), groupProp);
                    groupProp.addPropertyGroup(propertyGroup);
                }
                validateProperties(subGroup, propertyGroup);
            }
            
        }
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_GROUP))
        {
            // If the element being read is a renderer group then retrieve the
            // group from the current group. If the group does not exist, then
            // there is an error in the configuration
            EJCoreFrameworkExtensionProperties props = new EJCoreFrameworkExtensionProperties(_formProperties, _blockProperties, attributes.getValue("name"),
                    _currentPropertyGroup);
            _currentPropertyGroup.addPropertyGroup(props);
            _currentPropertyGroup = props;
        }
        else if (name.equals(ELEMENT_PROPERTY))
        {
            _currentProperty = new EJCoreFrameworkExtensionProperty(EJPropertyDefinitionType.valueOf(attributes.getValue("propertyType")),
                    attributes.getValue("name"));
            
            String multilingual = attributes.getValue("multilingual");
            if (multilingual != null)
            {
                _currentProperty.setMultilingual(Boolean.parseBoolean(multilingual));
            }
        }
        else if (name.equals(ELEMENT_PROPERTY_LIST))
        {
            setDelegate(new FrameworkExtensionPropertiesListHandler());
        }
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (_exitTag.equals(name))
        {
            quitAsDelegate();
            return;
        }
        
        if (name.equals(ELEMENT_GROUP))
        {
            // If I have come to the end of a group, then set the current
            // group to its parent
            _currentPropertyGroup = (EJCoreFrameworkExtensionProperties) _currentPropertyGroup.getParentGroup();
        }
        else if (name.equals(ELEMENT_PROPERTY))
        {
            _currentProperty.setValue(value);
            _currentPropertyGroup.addProperty(_currentProperty);
        }
    }
    
    @Override
    protected void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_PROPERTY_LIST))
        {
            _currentPropertyGroup.addPropertyList(((FrameworkExtensionPropertiesListHandler) currentDelegate).getPropertyList());
        }
    }
}
