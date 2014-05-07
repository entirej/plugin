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
package org.entirej.framework.plugin.framework.properties.reader;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginObjectGroupProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FormHandler extends EntireJTagHandler
{
    private EJPluginFormProperties _formProperties;
    
    private static final String    ELEMENT_FORM_TITLE             = "formTitle";
    private static final String    ELEMENT_FORM_DISPLAY_NAME      = "formDisplayName";
    private static final String    ELEMENT_FORM_WIDTH             = "formWidth";
    private static final String    ELEMENT_FORM_HEIGHT            = "formHeight";
    private static final String    ELEMENT_NUM_COLS               = "numCols";
    private static final String    ELEMENT_ACTION_PROCESSOR       = "actionProcessorClassName";
    private static final String    ELEMENT_RENDERER_NAME          = "formRendererName";
    private static final String    ELEMENT_RENDERER_PROPERTIES    = "formRendererProperties";
    
    private static final String    ELEMENT_FORM_PARAMETER         = "formParameter";
    private static final String    ELEMENT_APPLICATION_PROPERTIES = "applicationProperties";
    
    private static final String    ELEMENT_CANVAS                 = "canvas";
    private static final String    ELEMENT_BLOCK                  = "block";
    private static final String    ELEMENT_RELATION               = "relation";
    private static final String    ELEMENT_LOV_DEFINITION         = "lovDefinition";
    private static final String    ELEMENT_OBJGROUP_DEFINITION    = "objGroupDefinition";
    private static final String    ELEMENT_PROPERTY               = "property";
    
    private boolean                _gettingApplicationProperties  = false;
    private String                 _lastApplicationPropertyName   = "";
    
    public FormHandler(IJavaProject javaProject, String formName)
    {
        _formProperties = new EJPluginFormProperties(formName, javaProject);
    }
    
    public FormHandler(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public EntireJTagHandler getBlockHandler(EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties lovDefinitionProperties)
    {
        return new BlockHandler(formProperties, lovDefinitionProperties);
    }
    
    public EntireJTagHandler getLovDefinitionHandler(EJPluginFormProperties formProperties)
    {
        return new LovDefinitionHandler(formProperties);
    }
    
    public EntireJTagHandler getObjectgroupDefinitionHandler(EJPluginFormProperties formProperties)
    {
        return new ObjGroupDefinitionHandler(formProperties);
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        
        if (name.equals(ELEMENT_APPLICATION_PROPERTIES))
        {
            _gettingApplicationProperties = true;
        }
        
        if (_gettingApplicationProperties)
        {
            if (name.equals(ELEMENT_PROPERTY))
            {
                _lastApplicationPropertyName = attributes.getValue("name");
            }
            return;
        }
        
        // Now process the FORM PROPERTIES elements
        if (name.equals(ELEMENT_RENDERER_PROPERTIES))
        {
            setDelegate(new FrameworkExtensionPropertiesHandler(_formProperties, null, ELEMENT_RENDERER_PROPERTIES));
        }
        else if (name.equals(ELEMENT_CANVAS))
        {
            setDelegate(new CanvasHandler(_formProperties));
        }
        else if (name.equals(ELEMENT_BLOCK))
        {
            setDelegate(getBlockHandler(_formProperties, null));
        }
        else if (name.equals(ELEMENT_RELATION))
        {
            setDelegate(new RelationHandler(_formProperties));
        }
        else if (name.equals(ELEMENT_LOV_DEFINITION))
        {
            setDelegate(getLovDefinitionHandler(_formProperties));
        }
        else if (name.equals(ELEMENT_OBJGROUP_DEFINITION))
        {
            setDelegate(getObjectgroupDefinitionHandler(_formProperties));
        }
        else if (name.equals(ELEMENT_FORM_PARAMETER))
        {
            String paramName = attributes.getValue("name");
            String dataTypeName = attributes.getValue("dataType");
            String defaultValue = attributes.getValue("defaultValue");
            
            _formProperties.addFormParameter(new EJPluginApplicationParameter(paramName, dataTypeName,defaultValue));
        }
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (_gettingApplicationProperties)
        {
            if (name.equals(ELEMENT_APPLICATION_PROPERTIES))
            {
                _gettingApplicationProperties = false;
            }
            else if (name.equals(ELEMENT_PROPERTY))
            {
                _formProperties.addApplicationProperty(_lastApplicationPropertyName, value);
            }
            return;
        }
        
        if (name.equals(ELEMENT_FORM_TITLE))
        {
            _formProperties.setFormTitle(value);
        }
        else if (name.equals(ELEMENT_FORM_DISPLAY_NAME))
        {
            _formProperties.setFormDisplayName(value);
        }
        else if (name.equals(ELEMENT_FORM_HEIGHT))
        {
            if (value.length() > 0)
            {
                _formProperties.setFormHeight(Integer.parseInt(value));
            }
            else
            {
                _formProperties.setFormHeight(0);
            }
        }
        else if (name.equals(ELEMENT_FORM_WIDTH))
        {
            if (value.length() > 0)
            {
                _formProperties.setFormWidth(Integer.parseInt(value));
            }
            else
            {
                _formProperties.setFormWidth(0);
            }
        }
        else if (name.equals(ELEMENT_NUM_COLS))
        {
            if (value.length() > 0)
            {
                _formProperties.setNumCols(Integer.parseInt(value));
            }
            else
            {
                _formProperties.setNumCols(1);
            }
        }
        else if (name.equals(ELEMENT_ACTION_PROCESSOR))
        {
            _formProperties.setActionProcessorClassName(value);
        }
        else if (name.equals(ELEMENT_RENDERER_NAME))
        {
            _formProperties.setFormRendererName(value);
        }
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_CANVAS))
        {
            _formProperties.getCanvasContainer().addCanvasProperties(((CanvasHandler) currentDelegate).getCanvasProperties());
        }
        else if (name.equals(ELEMENT_BLOCK))
        {
            _formProperties.getBlockContainer().addBlockProperties(((BlockHandler) currentDelegate).getBlockProperties());
            return;
        }
        else if (name.equals(ELEMENT_RELATION))
        {
            _formProperties.getRelationContainer().addRelationProperties(((RelationHandler) currentDelegate).getRelationProperties());
            return;
        }
        else if (name.equals(ELEMENT_LOV_DEFINITION))
        {
            _formProperties.getLovDefinitionContainer().addLovDefinitionProperties(((LovDefinitionHandler) currentDelegate).getLovDefinitionProperties());
            ((LovDefinitionHandler) currentDelegate).getLovDefinitionProperties().getBlockProperties().setFormProperties(_formProperties);
            return;
        }
        else if (name.equals(ELEMENT_OBJGROUP_DEFINITION))
        {
            EJPluginObjectGroupProperties objectGroupDefinitionProperties = ((ObjGroupDefinitionHandler) currentDelegate).getObjectGroupDefinitionProperties();
            _formProperties.getObjectGroupContainer().addObjectGroupProperties(objectGroupDefinitionProperties);
            objectGroupDefinitionProperties.importObjectsToForm(_formProperties);

            return;
        }
        else if (name.equals(ELEMENT_RENDERER_PROPERTIES))
        {
            if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
            {
                
                if (_formProperties.getFormRendererDefinition() != null)
                {
                    _formProperties.setFormRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup(_formProperties
                            .getFormRendererDefinition().getFormPropertyDefinitionGroup()));
                }
                else
                {
                    _formProperties.setFormRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
                }
            }
            return;
        }
    }
}
