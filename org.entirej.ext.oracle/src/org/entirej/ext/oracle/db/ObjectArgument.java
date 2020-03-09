/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 *     CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.ext.oracle.db;

import java.util.ArrayList;
import java.util.List;

class ObjectArgument extends Argument
{
    final String                tableName;
    final String                objName;
    private ArrayList<Argument> _arguments = new ArrayList<Argument>();

    public ObjectArgument(String tableName, String objName, String name, String dataType,int typeInt)
    {
        super(name, dataType,typeInt);
        this.tableName = tableName;
        this.objName = objName;
    }

    public ObjectArgument(String name, String type,int typeInt)
    {
        this(null, null, name, type,typeInt);
    }

    public void addArgument(Argument arg)
    {
        _arguments.add(arg);
    }

    public List<Argument> getArguments()
    {
        return _arguments;
    }

    public String getTableName()
    {
        return tableName;
    }

    public String getObjName()
    {
        return objName;
    }

}
