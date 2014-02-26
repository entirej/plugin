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
/*
 * Created on Nov 5, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevFormRendererDefinition;

public class EJPluginObjectGroupProperties extends EJPluginFormProperties
{

    private static final long serialVersionUID = 4791971005262621103L;

    public EJPluginObjectGroupProperties(String formName, IJavaProject javaProject)
    {
        super(formName, javaProject);
    }
    
    public EJPluginObjectGroupProperties getObjectgroupProperties()
    {
        return this;
    }
    
    
    public String getActionProcessorClassName()
    {
        return null;
    }
    
    
    public void setActionProcessorClassName(String processorClassName)
    {
        //ignore
    }
    
    @Override
    public String getFormRendererName()
    {
        return null;
    }
    
    @Override
    public void setFormRendererName(String formRendererName)
    {
       //ignore
    }
    
    @Override
    public void setFormRendererProperties(EJFrameworkExtensionProperties properties)
    {
       //ignore
    }
    
    @Override
    public EJDevFormRendererDefinition getFormRendererDefinition()
    {
        return null;
    }
    
    
    
}
