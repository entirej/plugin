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

import org.entirej.framework.reports.renderers.definitions.interfaces.EJReportFrameworkExtensionProperties;

public interface EJReportProperties extends Serializable
{
    /**
     * Returns the name of the report
     * 
     * @return The report name
     */
    public String getName();
    
    /**
     * Returns the title of this report
     * 
     * @return The report title
     */
    public String getTitle();
    
    /**
     * Returns the number of display columns that this for uses
     * <p>
     * The report will lay out the main content canvases within a grid. This
     * property defines how many columns the grid should have. A value of
     * <code>1</code> (the default), indicates that all content canvases will be
     * stacked one above each other
     * 
     * @return The number of columns that the report will use to display the
     *         content canvases
     */
    public int getNumCols();
    
    /**
     * Returns the required height of the report
     * <p>
     * The value is the height in pixels
     * 
     * @return The required height of the report
     */
    public int getReportHeight();
    
    /**
     * Returns the required width of the report
     * <p>
     * The value is the width in pixels
     * 
     * @return The required width of the report
     */
    public int getReportWidth();
    
    /**
     * Retrieves the name of the renderer that is responsible for displaying
     * this report
     * 
     * @return the report renderers name
     */
    public String getReportRendererName();
    
    
    public EJReportFrameworkExtensionProperties getReportRendererProperties();
    
}
