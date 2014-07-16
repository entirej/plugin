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

package org.entirej.framework.plugin.reports;

import java.util.ArrayList;
import java.util.List;

public class EJPluginReportFormats
{
    
    
    public static final ReportFormat A3 = new ReportFormat("A3", 842, 1190, 1, 20 , 20, 20, 20);
    public static final ReportFormat A4 = new ReportFormat("A4", 595, 842, 1, 20 , 20, 20, 20);

    public static List<ReportFormat> getFormats()
    {
        List<ReportFormat> formats = new ArrayList<ReportFormat>();
        
        formats.add(A3);
        formats.add(A4);
        
        
        return formats;
    }
    
    public static class ReportFormat
    {
        
        public final String name;
        
        public final int         reportWidth;
        public final int         reportHeight;
        public final int         numCols;
        public final int         marginTop;
        public final int         marginBottom;
        public final int         marginLeft;
        public final int         marginRight;
        
        
        
        
        private ReportFormat(String name,
                int reportWidth,
                int reportHeight, 
                int numCols,
                int marginTop,
                int marginBottom,
                int marginLeft,
                int marginRight)
        {
            super();
            this.name = name;
            this.reportWidth = reportWidth;
            this.reportHeight = reportHeight;
            this.numCols = numCols;
            this.marginTop = marginTop;
            this.marginBottom = marginBottom;
            this.marginLeft = marginLeft;
            this.marginRight = marginRight;
        }




        @Override
        public String toString()
        {
            return name;
        }
        
    }
}
