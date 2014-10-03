/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
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
package org.entirej.framework.reports.interfaces;

import java.io.Serializable;
import java.util.Collection;

import org.entirej.framework.reports.properties.EJReportVisualAttributeContainer;

public interface EJEntireJReportProperties extends Serializable
{
   
    public abstract Collection<String> getReportPackageNames();
    
   
    
    /**
     * Returns the <code>VisualAttributeContaier</code> that contains all the
     * visual attributes that can be used within the application
     * 
     * @return The <code>VisualAttributeContainer</code> for the application
     */
    public abstract EJReportVisualAttributeContainer getVisualAttributesContainer();
}
