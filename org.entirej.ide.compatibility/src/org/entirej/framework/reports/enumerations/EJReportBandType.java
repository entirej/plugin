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

public enum EJReportBandType
{
    TITILE, PAGE_HEADER, CLOUMN_HEADER, DETAIL, CLOUMN_FOOTER, PAGE_FOOTER, LAST_PAGE_FOOTER, SUMMARY;
    
    public String toString()
    {
        switch (this)
        {
            case TITILE:
                return "Title";
            case PAGE_HEADER:
                return "Page Header";
            case CLOUMN_HEADER:
                return "Column Header";
            case DETAIL:
                return "Detail";
            case CLOUMN_FOOTER:
                return "Column Footer";
            case PAGE_FOOTER:
                return "Page Footer";
            case LAST_PAGE_FOOTER:
                return "Last Page Footer";
            case SUMMARY:
                return "Summary";
            default:
                return super.toString();
        }
    }
}
