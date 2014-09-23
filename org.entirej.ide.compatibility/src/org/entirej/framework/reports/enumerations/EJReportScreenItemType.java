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
package org.entirej.framework.reports.enumerations;

public enum EJReportScreenItemType
{
    LABEL,TEXT,NUMBER,DATE,IMAGE,LINE;
    
    public String toString()
    {
        switch (this)
        {
            
            case LABEL:
                return "Label";
            case LINE:
                return "Line";
            case TEXT:
                return "Text";
            case NUMBER:
                return "Number";
            case DATE:
                return "Date";
            case IMAGE:
                return "Image";
            default:
                return super.toString();
        }
    }
}
