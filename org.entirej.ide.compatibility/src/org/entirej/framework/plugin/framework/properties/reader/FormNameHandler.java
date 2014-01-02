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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.entirej.framework.plugin.framework.properties.EntirejPropertiesUtils;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class FormNameHandler extends EntireJTagHandler
{
    
    private static final String ELEMENT_FORM_TITLE        = "formTitle";
    private static final String ELEMENT_FORM_DISPLAY_NAME = "formDisplayName";
    private String              formName;
    private String              formDisplayName;
    
    public EJPluginEntireJProperties createNewEntireJPluginProperties(IJavaProject javaProject) throws CoreException
    {
        return EntirejPropertiesUtils.retrieveEntirejProperties(javaProject);
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        
        if (name.equals(ELEMENT_FORM_TITLE))
        {
            formName = (value);
            
        }
        if (name.equals(ELEMENT_FORM_DISPLAY_NAME))
        {
            formDisplayName = (value);
            
        }
        
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        
    }
    
    public String getDefaultFormName()
    {
        return formDisplayName != null && formDisplayName.length() > 0 ? formDisplayName : formName;
    }
    
    public String getFormName()
    {
        return formName;
    }
}
