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

class Argument
{
    public enum Type
    {
        IN, OUT, IN_OUT, RETURN
    }

    protected String        _name;
    protected String        _datatype;
    protected Argument.Type type = Type.IN_OUT;

    public Argument(String name, String datatype)
    {
        _name = name;
        this._datatype = datatype;
    }

    public String getName()
    {
        return _name;
    }

    public String getDataType()
    {
        return _datatype;
    }

}
