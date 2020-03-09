/*******************************************************************************
 * Copyright 2013 CRESOFT AG
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
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.framework.properties;

public class EJPluginResourceChangedProperty
{
    private String _deletedName;
    private String _addedName;
    private String _changedName;
    
    public String getDeletedName()
    {
        return _deletedName;
    }
    
    public void setDeletedName(String name)
    {
        _deletedName = name;
    }
    
    public String getAddedName()
    {
        return _addedName;
    }
    
    public void setAddedName(String name)
    {
        _addedName = name;
    }
    
    public String getChangedName()
    {
        return _changedName;
    }
    
    public void setChangedName(String name)
    {
        _changedName = name;
    }
    
}
