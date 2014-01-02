/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.dev.properties;

public class EJDevTransactionFactoryDefinition implements Comparable<EJDevTransactionFactoryDefinition>
{
    private String _factoryName;
    private String _factoryClassName;

    public EJDevTransactionFactoryDefinition(String factoryName, String className)
    {
        _factoryName = factoryName;
        _factoryClassName = className;
    }

    public String getFactoryName()
    {
        return _factoryName;
    }

    public void setFactoryName(String name)
    {
        _factoryName = name;
    }

    public String getFactoryClassName()
    {
        return _factoryClassName;
    }

    public void setFactoryClassName(String className)
    {
        _factoryClassName = className;
    }

    public int compareTo(EJDevTransactionFactoryDefinition props)
    {
        return this.getFactoryName().compareTo(props.getFactoryName());
    }

}
