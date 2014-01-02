/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
package org.entirej.ext.oracle.db;

import java.util.ArrayList;
import java.util.List;

import org.entirej.ext.oracle.db.Argument.Type;

class Procedure
{
    private String             _name;
    private String             _packageName;
    public ArrayList<Argument> _arguments = new ArrayList<Argument>();

    public Procedure(String name)
    {
        _name = name;
    }

    public String getName()
    {
        return _name;
    }

    public void addArgument(Argument arg)
    {
        _arguments.add(arg);
    }

    public List<Argument> getArguments()
    {
        return _arguments;
    }

    public void setPackageName(String _packageName)
    {
        this._packageName = _packageName;
    }

    public String getPackageName()
    {
        return _packageName;
    }

    public String getFullName()
    {
        if (_packageName != null)
            return String.format("%s.%s", _packageName, _name);
        return _name;
    }

    public ObjectArgument getCollectionType()
    {
        for (Argument argument : getArguments())
        {
            if (argument instanceof ObjectArgument)
            {
                ObjectArgument objectArgument = (ObjectArgument) argument;
                if (objectArgument.tableName != null && objectArgument.type != Type.IN)
                {
                    return objectArgument;
                }
            }
        }
        return null;
    }
}
