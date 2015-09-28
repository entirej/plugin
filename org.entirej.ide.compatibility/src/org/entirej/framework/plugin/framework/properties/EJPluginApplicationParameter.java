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
package org.entirej.framework.plugin.framework.properties;

import java.math.BigDecimal;

public class EJPluginApplicationParameter
{
    private String _name = null;
    private String _dataTypeName;
    private String _defaultValue;
    
    
    public EJPluginApplicationParameter(String name, String dataTypeName)
    {
        _name = name;
        _dataTypeName = dataTypeName;
    }
    
    
    
    public EJPluginApplicationParameter(String name, String dataTypeName, String defaultValue)
    {
        this._name = name;
        this._dataTypeName = dataTypeName;
        this._defaultValue = defaultValue;
    }



    public void setName(String name)
    {
        if (name != null && name.trim().length() > 0)
        {
            _name = name;
        }
    }
    
    public String getName()
    {
        return _name;
    }
    
    public void setDataTypeName(String dataTypeName)
    {
        if (dataTypeName != null && dataTypeName.trim().length() > 0)
        {
            _dataTypeName = dataTypeName;
        }
    }
    
    public String getDataTypeName()
    {
        return _dataTypeName;
    }
    
    
    public String getDefaultValue()
    {
        return _defaultValue;
    }
    
    public void setDefaultValue(String defaultValue)
    {
        this._defaultValue = defaultValue;
    }
    
    
    public static boolean isValidDefaultValueType(String dataTypeName)
    {
        
        // support only primitives and String
        // String
        if (String.class.getName().equals(dataTypeName))
        {
            return true;
        }
        // Boolean
        if (Boolean.class.getName().equals(dataTypeName))
        {
            return true;
        }
        // int
        if (Integer.class.getName().equals(dataTypeName))
        {
            return true;
        }
        // long
        if (Long.class.getName().equals(dataTypeName))
        {
            return true;
        }
        
        // Float
        if (Float.class.getName().equals(dataTypeName))
        {
            return true;
        }
        
        // Double
        if (Double.class.getName().equals(dataTypeName))
        {
            return true;
        }
        // BigDecimal
        if (BigDecimal.class.getName().equals(dataTypeName))
        {
            return true;
        }
        
        
        
        
        return false;
    }
    
    
    public static String validateDefaultValue(String dataTypeName,String value)
    {
        
        if(value==null || value.trim().length()==0)
            return null;
        // support only primitives and String
        // String
        if (String.class.getName().equals(dataTypeName))
        {
            //String is valid always
            return null;
        }
        // Boolean
        if (Boolean.class.getName().equals(dataTypeName))
        {
            
            if("TRUE".equalsIgnoreCase(value) || "FALSE".equalsIgnoreCase(value))
            {
                return null;
            }
            
            return "Incorrect default value for 'Boolean' Type: Valid values are 'true' & 'false' (ignoring case).";
        }
        // int
        if (Integer.class.getName().equals(dataTypeName))
        {
            try
            {
                Integer.parseInt(value);
            }
            catch (NumberFormatException  e)
            {
                return "Incorrect default value for 'Integer' Type: Valid values are numbers.";
            }
            
            return null;
        }
        // long
        if (Long.class.getName().equals(dataTypeName))
        {
            try
            {
                Long.parseLong(value);
            }
            catch (NumberFormatException  e)
            {
                return "Incorrect default value for 'Long' Type: Valid values are numbers.";
            }
            
            return null;
        }
        
        // Float
        if (Float.class.getName().equals(dataTypeName))
        {
            try
            {
                Float.parseFloat(value);
            }
            catch (NumberFormatException  e)
            {
                return "Incorrect default value for 'Float' Type: Valid values are numbers.";
            }
            return null;
        }
        
        // Double
        if (Double.class.getName().equals(dataTypeName))
        {
            try
            {
                Double.parseDouble(value);
            }
            catch (NumberFormatException  e)
            {
                return "Incorrect default value for 'Double' Type: Valid values are numbers.";
            }
            return null;
        }
        // BigDecimal
        if (BigDecimal.class.getName().equals(dataTypeName))
        {
            try
            {
                new BigDecimal(value);
            }
            catch (NumberFormatException  e)
            {
                return "Incorrect default value for 'BigDecimal' Type: Valid values are numbers.";
            }
            return null;
        }
        
        
        
        
        return null;
    }
}
