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
package org.entirej.framework.reports.interfaces;

import java.io.Serializable;

public interface EJReportProperties extends Serializable
{
    
    public enum ORIENTATION
    {
        PORTRAIT, LANDSCAPE
    }
    
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
     * Returns the required Margin of the report from Top
     * <p>
     * The value is the margin in pixels
     * 
     * @return The required margin of the report from Top
     */
    public int getMarginTop();
    
    /**
     * Returns the required Margin of the report from Bottom
     * <p>
     * The value is the margin in pixels
     * 
     * @return The required margin of the report from Bottom
     */
    public int getMarginBottom();
    
    /**
     * Returns the required Margin of the report from Left
     * <p>
     * The value is the margin in pixels
     * 
     * @return The required margin of the report from Left
     */
    public int getMarginLeft();
    
    /**
     * Returns the required Margin of the report from Right
     * <p>
     * The value is the margin in pixels
     * 
     * @return The required margin of the report from Right
     */
    public int getMarginRight();
    
    
    public ORIENTATION getOrientation();
    
}
