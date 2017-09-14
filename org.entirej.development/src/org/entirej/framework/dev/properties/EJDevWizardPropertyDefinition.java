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

import org.entirej.framework.core.properties.definitions.EJPropertyDefinitionType;

public class EJDevWizardPropertyDefinition extends EJDevPropertyDefinition implements Comparable<Object>
{
    private boolean _createValidValuesDynamically = false;
    private String  _value;

    /**
     * Creates a new instance of a <code>WizardPropertyDefinition</code> with
     * the given name
     * 
     * @param name
     *            The name of this property
     * @param propertyType
     *            The property type of this property
     * @throws NullPointerException
     *             id the name is null
     */
    public EJDevWizardPropertyDefinition(String name, EJPropertyDefinitionType propertyType)
    {
        super(name, propertyType);
    }

    public void setValue(String value)
    {
        _value = value;
    }

    public String getValue()
    {
        return _value;
    }

    public boolean createValidValuesDynamically()
    {
        return _createValidValuesDynamically;
    }

    public boolean hasValidValues()
    {
        if (createValidValuesDynamically())
        {
            return true;
        }
        return super.hasValidValues();
    }

    public void setCreateValidValuesDynamically(boolean setDynamically)
    {
        _createValidValuesDynamically = setDynamically;
    }

    public int compareTo(Object o)
    {
        if (!(o instanceof EJDevWizardPropertyDefinition))
        {
            return -1;
        }
        return ((EJDevWizardPropertyDefinition) o).getName().compareTo(this.getName());
    }

    public boolean equals(EJDevWizardPropertyDefinition def)
    {
        if (def.getName().endsWith(getName()))
        {
            return true;
        }
        else
        {
            return false;
        }
    }

}
