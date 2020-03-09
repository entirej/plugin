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
/*
 * Created on Nov 30, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties;

public class EJPluginRelationJoinProperties
{
    private String _masterItemName;
    private String _detailItemName;
    
    public EJPluginRelationJoinProperties(String masterItemName, String detailItemName)
    {
        _masterItemName = masterItemName;
        _detailItemName = detailItemName;
    }
    
    public String getMasterItemName()
    {
        return _masterItemName;
    }
    
    public void setMasterItemName(String name)
    {
        _masterItemName = name;
    }
    
    public String getDetailItemName()
    {
        return _detailItemName;
    }
    
    public void setDetailItemName(String name)
    {
        _detailItemName = name;
    }
}
