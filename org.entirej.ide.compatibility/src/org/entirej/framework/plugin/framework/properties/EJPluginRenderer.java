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

import java.util.ArrayList;
import java.util.List;

import org.entirej.framework.core.enumerations.EJRendererType;
import org.entirej.framework.core.properties.interfaces.EJRendererAssignment;
import org.entirej.framework.core.renderers.definitions.interfaces.EJAppComponentRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJBlockRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJFormRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJInsertScreenRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJItemRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJLovRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJMenuRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJQueryScreenRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJRendererDefinition;
import org.entirej.framework.core.renderers.definitions.interfaces.EJUpdateScreenRendererDefinition;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;

/**
 * Indicates the name and class of a renderer definition that has been assigned
 * to this application
 * <p>
 * Renderer Definitions are classes that provide configuration information of a
 * specific renderer. Each definition holds specific properties that the user
 * can or must set.
 * 
 * 
 */
public class EJPluginRenderer implements EJRendererAssignment, Comparable<EJPluginRenderer>
{
    /**
     * 
     */
    private static final long serialVersionUID = 4632592074671794254L;
    private EJPluginEntireJProperties _entireJProperties;
    private String                    _name;
    private String                    _rendererDefinitionClassName;
    private String                    _rendererClassName;
    private EJRendererType            _rendererType;
    
    private ArrayList<String>         _dataTypeNames;
    
    public EJPluginRenderer(EJPluginEntireJProperties entireJProperties, String name, EJRendererType rendererType)
    {
        _entireJProperties = entireJProperties;
        _name = name;
        _dataTypeNames = new ArrayList<String>();
        _rendererType = rendererType;
    }
    
    public EJPluginRenderer(EJPluginEntireJProperties entireJProperties, String name, EJRendererType rendererType, String rendererDefClassName,
            String rendererClassName)
    {
        this(entireJProperties, name, rendererType);
        this._rendererDefinitionClassName = rendererDefClassName;
        this._rendererClassName = rendererClassName;
    }
    
    public void clear()
    {
        _dataTypeNames.clear();
        _entireJProperties = null;
    }
    
    /**
     * <B>INTERNAL USE ONLY</B>
     * <p>
     * This method is used internally to set the name of the renderer
     * 
     * @param name
     *            The renderer name
     */
    public void internalSetName(String name)
    {
        _name = name;
    }
    
    public void internalSetRendererClassName(String name)
    {
        _rendererClassName = name;
    }
    
    public void internalSetRendererDefinitionClassName(String name)
    {
        _rendererDefinitionClassName = name;
    }
    
    /**
     * An item renderer can become the default renderer for a number of
     * specified data types
     * <p>
     * Use this method to add a data type for which this renderer is to be used
     * 
     * @param dataTypeName
     *            The name of the data type for which this renderer is the
     *            default
     */
    public void addDataTypeName(String dataTypeName)
    {
        if (dataTypeName != null && dataTypeName.trim().length() > 0)
        {
            _dataTypeNames.add(dataTypeName);
        }
    }
    
    public List<String> getDataTypeNames()
    {
        return _dataTypeNames;
    }
    
    public boolean isRendererForDataType(String dataTypeName)
    {
        if (dataTypeName == null || dataTypeName.trim().length() == 0)
        {
            return false;
        }
        else
        {
            return _dataTypeNames.contains(dataTypeName);
        }
    }
    
    public void setRendererDefinitionClassName(String rendererDefinitionClassName, boolean validate)
    {
        if (rendererDefinitionClassName == null || rendererDefinitionClassName.trim().length() == 0)
        {
            _rendererDefinitionClassName = "";
            _rendererClassName = "";
        }
        else
        {
            _rendererDefinitionClassName = rendererDefinitionClassName;
            
            if (!validate) return;
            
            Class<?> rendererDefinitionClass = getRendererDefClass(rendererDefinitionClassName);
            if (rendererDefinitionClass == null) return;
            EJRendererDefinition _rendererDefinition = createRendererDefinition(rendererDefinitionClass);
            if (_rendererDefinition == null) return;
            // Now get the class name of the renderer for which the
            // definition is for, and check if it is a valid renderer
            switch (_rendererType)
            {
                case FORM:
                    if (EJFormRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJFormRenderer");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("This renderer definition is not an EJFormRendererDefinition");
                        _rendererDefinition = null;
                    }
                    break;
                
                case BLOCK:
                    if (EJBlockRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJBlockRenderer");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("This renderer definition is not an EJBlockRendererDefinition");
                        _rendererDefinition = null;
                    }
                    break;
                
                case ITEM:
                    if (EJItemRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJItemRenderer");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("This renderer definition is not an EJItemRendererDefinition");
                        _rendererDefinition = null;
                    }
                    break;
                
                case LOV:
                    if (EJLovRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJLovRenderer");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("This renderer definition is not an EJLovRendererDefinition");
                        _rendererDefinition = null;
                        return;
                    }
                    break;
                case MENU:
                    if (EJMenuRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJMenuRenderer");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("This renderer definition is not an EJMenuRendererDefinition");
                        _rendererDefinition = null;
                        return;
                    }
                    break;
                case APP_COMPONENT:
                    if (EJAppComponentRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                            
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJApplicationComponentRenderer");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("This renderer definition is not an EJAppComponentRendererDefinition");
                        _rendererDefinition = null;
                        return;
                    }
                    break;
                case QUERY_SCREEN:
                    if (EJQueryScreenRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJQueryScreenRenderer");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("The renderer: " + _name + " is not a valid IQueryScreenRender");
                        _rendererDefinition = null;
                    }
                    break;
                case UPDATE_SCREEN:
                    if (EJUpdateScreenRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJUpdateScreenRendererDefinition");
                            _rendererDefinition = null;
                            return;
                        }
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("The renderer: " + _name + " is not a valid EJUpdateScreenRenderer");
                        _rendererDefinition = null;
                    }
                    break;
                case INSERT_SCREEN:
                    if (EJInsertScreenRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
                    {
                        if (_rendererDefinition.getRendererClassName() != null)
                        {
                            _rendererClassName = _rendererDefinition.getRendererClassName();
                        }
                        else
                        {
                            EJCoreLog.logWarnningMessage("The renderer definition does not specify an EJInsertScreenRendererDefinition");
                            _rendererDefinition = null;
                            return;
                        }
                        
                    }
                    else
                    {
                        EJCoreLog.logWarnningMessage("The renderer: " + _name + " is not a valid EJInsertScreenRenderer");
                        _rendererDefinition = null;
                    }
                    break;
            }
            
        }
    }
    
    private EJRendererDefinition createRendererDefinition(Class<?> rendererDefinitionClass)
    {
        try
        {
            return (EJRendererDefinition) rendererDefinitionClass.newInstance();
        }
        catch (InstantiationException e)
        {
            EJCoreLog.logWarnningMessage("Unable to instantiate the renderer definition: " + _name);
            
        }
        catch (IllegalAccessException e)
        {
            EJCoreLog.logWarnningMessage("Illegal access violation when creating an instance of the renderer definition: " + _name);
            
        }
        return null;
    }
    
    private Class<?> getRendererDefClass(String rendererDefinitionClassName)
    {
        Class<?> rendererDefinitionClass = null;
        try
        {
            rendererDefinitionClass = EJPluginEntireJClassLoader.loadClass(_entireJProperties.getJavaProject(), rendererDefinitionClassName);
            if (!EJRendererDefinition.class.isAssignableFrom(rendererDefinitionClass))
            {
                EJCoreLog.logWarnningMessage("The class name passed to Renderer.setRendererClassName is not an EJRendererDefinition. Name: " + _name);
                
            }
            
        }
        catch (ClassNotFoundException e)
        {
            EJCoreLog.logWarnningMessage("Unable to load class: " + e.getMessage()
                    + ".\nPlease ensure the class path has been set correctly and the given class exists");
            
        }
        return rendererDefinitionClass;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see
     * org.entirej.framework.plugin.framework.properties.EJRenderer#getName()
     */
    public String getAssignedName()
    {
        return _name;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @seeorg.entirej.framework.plugin.framework.properties.EJRenderer#
     * getRendererClassName()
     */
    public String getRendererClassName()
    {
        return _rendererClassName;
    }
    
    public String getRendererDefinitionClassName()
    {
        return _rendererDefinitionClassName;
    }
    
    /**
     * Returns this renderers type
     * 
     * @return The renderer type
     */
    public EJRendererType getRendererType()
    {
        return _rendererType;
    }
    
    public EJRendererDefinition getRendererDefinition()
    {
        if (_rendererDefinitionClassName == null || _rendererDefinitionClassName.length() == 0) return null;
        
        Class<?> rendererDefinitionClass = getRendererDefClass(_rendererDefinitionClassName);
        if (rendererDefinitionClass == null) return null;
        
        return createRendererDefinition(rendererDefinitionClass);
    }
    
    /**
     * Compares this renderer property to another and returns if they are the
     * same
     * <p>
     * The comparison is made on the renderers name
     * 
     * @param property
     *            The property to compare with
     * 
     * @return 0 if the items are the same otherwise -1
     */
    public int compareTo(EJPluginRenderer property)
    {
        if (getAssignedName().equalsIgnoreCase(property.getAssignedName()))
        {
            return 0;
        }
        else
        {
            return 1;
        }
    }
    
}
