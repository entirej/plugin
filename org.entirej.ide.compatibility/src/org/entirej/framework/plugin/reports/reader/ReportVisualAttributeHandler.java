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
package org.entirej.framework.plugin.reports.reader;

import org.entirej.framework.plugin.framework.properties.reader.EntireJTagHandler;
import org.entirej.framework.report.enumerations.EJReportFontStyle;
import org.entirej.framework.report.enumerations.EJReportFontWeight;
import org.entirej.framework.report.enumerations.EJReportMarkupType;
import org.entirej.framework.report.enumerations.EJReportScreenAlignment;
import org.entirej.framework.report.enumerations.EJReportVAPattern;
import org.entirej.framework.report.properties.EJCoreReportVisualAttributeProperties;
import org.entirej.framework.report.properties.EJReportVisualAttributeProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ReportVisualAttributeHandler extends EntireJTagHandler
{
    private EJCoreReportVisualAttributeProperties _vaProperties;
    
    private static final String               VISUAL_ATTRIBUTE  = "visualAttribute";
    private static final String               FONT_NAME         = "fontName";
    private static final String               FONT_SIZE         = "fontSize";
    private static final String               STYLE             = "style";
    
    private static final String               USE_AS_DYNAMIC_VA = "useAsDynamicVA";
    private static final String               WEIGHT            = "weight";
    private static final String               FOREGROUND_COLOR  = "foregroundColor";
    private static final String               BACKGROUND_COLOR  = "backgroundColor";
    
    private static final String               MARKUP            = "markup";
    
    private static final String               HALIGNMENT        = "hAlignment";
    private static final String               VALIGNMENT        = "vAlignment";
    
    private static final String               MANUALFORMAT      = "manualFormat";
    private static final String               LOCALEFORMAT      = "localeFormat";
    

    private static final String                   FORMAT_DECIMAL_DIGITS = "decimalDigits";
    
    @Override
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(VISUAL_ATTRIBUTE))
        {
            String vaName = attributes.getValue("name");
            _vaProperties = new EJCoreReportVisualAttributeProperties(vaName);
        }
    }
    
    public EJCoreReportVisualAttributeProperties getProperties()
    {
        return _vaProperties;
    }
    
    @Override
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(VISUAL_ATTRIBUTE))
        {
            quitAsDelegate();
            return;
        }
        
        if (name.equals(FONT_NAME))
        {
            _vaProperties.setFontName(value);
        }
        else if (name.equals(FONT_SIZE))
        {
            if (value.length() > 0)
            {
                _vaProperties.setFontSize(Integer.parseInt(value));
            }
        }
        else if (name.equals(USE_AS_DYNAMIC_VA))
        {
            if (value.length() > 0)
            {
                _vaProperties.setUsedAsDynamicVA(Boolean.valueOf(value));
            }
        }
        else if (name.equals(WEIGHT))
        {
            if (value.length() > 0)
            {
                _vaProperties.setFontWeight(EJReportFontWeight.valueOf(value));
            }
        }
        else if (name.equals(STYLE))
        {
            if (value.length() > 0)
            {
                _vaProperties.setFontStyle(EJReportFontStyle.valueOf(value));
            }
        }
        else if (name.equals(FOREGROUND_COLOR))
        {
            if (value.length() > 0)
            {
                _vaProperties.setForegroundRGB(value);
            }
        }
        else if (name.equals(BACKGROUND_COLOR))
        {
            if (value.length() > 0)
            {
                _vaProperties.setBackgroundRGB(value);
            }
        }
        else if (name.equals(FORMAT_DECIMAL_DIGITS))
        {
            if (value.length() > 0)
            {
                _vaProperties.setMaximumDecimalDigits(Integer.valueOf(value));
            }
        }
        
        else if (name.equals(MARKUP))
        {
            if (value.length() > 0)
            {
                _vaProperties.setMarkupType(EJReportMarkupType.valueOf(value));
            }
        }
        
        else if (name.equals(HALIGNMENT))
        {
            if (value.length() > 0)
            {
                _vaProperties.setHAlignment(EJReportScreenAlignment.valueOf(value));
            }
        }
        else if (name.equals(VALIGNMENT))
        {
            if (value.length() > 0)
            {
                _vaProperties.setVAlignment(EJReportScreenAlignment.valueOf(value));
            }
        }
        
        else if (name.equals(LOCALEFORMAT))
        {
            if (value.length() > 0)
            {
                _vaProperties.setLocalePattern(EJReportVAPattern.valueOf(value));
            }
        }
        else if (name.equals(MANUALFORMAT))
        {
            if (value.length() > 0)
            {
                _vaProperties.setManualPattern(value);
            }
        }
    }
}
