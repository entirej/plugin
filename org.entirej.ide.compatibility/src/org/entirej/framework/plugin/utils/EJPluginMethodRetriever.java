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
package org.entirej.framework.plugin.utils;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.entirej.framework.core.EJApplicationException;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;

@Deprecated
public class EJPluginMethodRetriever
{
    public static List<String> getServiceNamesFromFactory(Class<?> serviceFactoryClass)
    {
        ArrayList<String> nameList = new ArrayList<String>();
        
        if (serviceFactoryClass == null)
        {
            return nameList;
        }
        
        for (Method method : serviceFactoryClass.getMethods())
        {
            if (!method.getName().startsWith("get"))
            {
                continue;
            }
            
            Class<?> returnType = method.getReturnType();
            
            if (EJBlockService.class.isAssignableFrom(returnType))
            {
                nameList.add(method.getName());
            }
        }
        return nameList;
    }
    
    
    
    public static Class<?> getPojoFromService(Class<?> service)
    {

        Type[] types = service.getGenericInterfaces();
        
        while (types.length==0 && !Object.class.equals(service.getSuperclass()))
        {
            service = service.getSuperclass();
            types = service.getGenericInterfaces();
            
        }
        if(types.length>0)
        {
            for (Type type : types)
            {
                if(type instanceof ParameterizedType && ((ParameterizedType)type).getRawType().equals(EJBlockService.class))
                {
                 
                    
                    Type[] sub =  ((ParameterizedType)type).getActualTypeArguments();

                    if(sub.length>0)
                    {
                       return  (Class<?>) sub[0];
                    }
                   
                }
            }
            
        }
            
       
        throw new EJApplicationException("Pojo Is not correclty defind on impl of  Interface EJBlockService<>");

    }
    public static Map<String, String> getPropertyNamesAndDatatypesFromService(EJPluginEntireJProperties entireJProperties, Class<?> serviceClass)
    {
        
        
        return getPojoProperties(getPojoFromService(serviceClass));
    }
    
    public static Map<String, String> getPojoProperties(Class<?> pojoClass)
    {
        TreeMap<String, String> propertyNames = new TreeMap<String, String>();
        
        if (pojoClass == null)
        {
            return propertyNames;
        }
        
        for (Method method : pojoClass.getMethods())
        {
            String methodName = "";
            if (method.getName().startsWith("get") || method.getName().startsWith("is"))
            {
                if (method.getName().startsWith("get"))
                {
                    methodName = method.getName().substring(3);
                }
                else
                {
                    methodName = method.getName().substring(2);
                }
            }
            
            // Only include the method if the getter method
            // has no input parameters
            if (method.getParameterTypes().length == 0)
            {
                // Only getters that return a value are needed
                if (method.getReturnType() != null)
                {
                    Class<?> returnType = method.getReturnType();
                    
                    try
                    {
                        pojoClass.getMethod("set" + methodName, returnType);
                    }
                    catch (NoSuchMethodException noSuchMethodException)
                    {
                        // Move to the next method as there is no set method for
                        // the corresponding get method
                        continue;
                    }
                    
                    // If I get this far then the method can be used
                    // Convert the method names first character to lower
                    // case and then add the name to the list of methods
                    methodName = methodName.substring(0, 1).toLowerCase() + methodName.substring(1);
                    propertyNames.put(methodName, returnType.getName());
                }
                
            }
        }
        return propertyNames;
    }
    
}
