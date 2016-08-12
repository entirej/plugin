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
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.AlignmentBaseItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Date.DateFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line.LineDirection;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line.LineStyle;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Number.NumberFormats;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.RotatableItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.ValueBaseItem;
import org.entirej.framework.plugin.reports.EJPluginReportScreenProperties;
import org.entirej.framework.report.enumerations.EJReportMarkupType;
import org.entirej.framework.report.enumerations.EJReportScreenAlignment;
import org.entirej.framework.report.enumerations.EJReportScreenItemType;
import org.entirej.framework.report.enumerations.EJReportScreenRotation;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class ScreenItemHandler extends EntireJTagHandler
{
    private EJPluginReportScreenItemProperties _itemProperties;
    private EJPluginReportScreenProperties     _blockProperties;
    
    private static final String                ELEMENT_ITEM                        = "screenitem";
    private static final String                ELEMENT_SCREEN_X                    = "x";
    private static final String                ELEMENT_SCREEN_Y                    = "y";
    private static final String                ELEMENT_SCREEN_WIDTH                = "width";
    private static final String                ELEMENT_SCREEN_HEIGHT               = "height";
    
    private static final String                ELEMENT_SCREEN_WIDTH_AS_PERCENTAGE  = "widthAsPercentage";
    private static final String                ELEMENT_SCREEN_HEIGHT_AS_PERCENTAGE = "heightAsPercentage";
    private static final String                ELEMENT_SCREEN_VISIBLE              = "visible";
    private static final String                ELEMENT_SCREEN_VA                   = "va";
    private static final String                ELEMENT_SCREEN_VALUE_PROVIDER       = "valueProvider";
    
    private static final String                ELEMENT_SCREEN_EXPAND_TO_FIT        = "expandToFit";
    private static final String                ELEMENT_SCREEN_MARKUP               = "markup";
    private static final String                ELEMENT_SCREEN_HALIGNMENT           = "hAlignment";
    private static final String                ELEMENT_SCREEN_VALIGNMENT           = "vAlignment";
    private static final String                ELEMENT_SCREEN_ROTATION             = "rotation";
    private static final String                ELEMENT_SCREEN_TEXT                 = "text";
    private static final String                ELEMENT_SCREEN_MANUAL_FORMAT        = "manualFormat";
    private static final String                ELEMENT_SCREEN_LOCALE_FORMAT        = "localeFormat";
    private static final String                ELEMENT_SCREEN_LINE_STYLE           = "lineStyle";
    private static final String                ELEMENT_SCREEN_LINE_WIDTH           = "lineWidth";
    private static final String                ELEMENT_SCREEN_LINE_DIRECTION       = "lineDirection";
    private static final String                ELEMENT_SCREEN_RECT_RADIUS          = "rectRadius";
    
    private static final String                ELEMENT_SCREEN_DEFAULT_IMAGE        = "defaultImage";
    
    private static final String              ELEMENT_SCREEN_PADDING_LEFT         = "leftPadding";
    private static final String              ELEMENT_SCREEN_PADDING_RIGHT        = "rightPadding";
   
    
    public ScreenItemHandler(EJPluginReportScreenProperties blockProperties)
    {
        _blockProperties = blockProperties;
        
    }
    
    public EJPluginReportScreenItemProperties getItemProperties()
    {
        return _itemProperties;
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_ITEM))
        {
            String type = attributes.getValue("type");
            EJReportScreenItemType screenItemType = EJReportScreenItemType.valueOf(type);
            
            String itemname = attributes.getValue("name");
            
            _itemProperties = _blockProperties.getScreenItemContainer().createItem(screenItemType, itemname, -1);
            
        }
        
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_ITEM))
        {
            quitAsDelegate();
            return;
        }
        
        else if (name.equals(ELEMENT_SCREEN_X))
        {
            _itemProperties.setX(Integer.parseInt(value));
        }
        else if (name.equals(ELEMENT_SCREEN_Y))
        {
            _itemProperties.setY(Integer.parseInt(value));
        }
        else if (name.equals(ELEMENT_SCREEN_WIDTH))
        {
            _itemProperties.setWidth(Integer.parseInt(value));
        }
        else if (name.equals(ELEMENT_SCREEN_HEIGHT))
        {
            _itemProperties.setHeight(Integer.parseInt(value));
        }
        else if (name.equals(ELEMENT_SCREEN_PADDING_LEFT))
        {
            _itemProperties.setLeftPadding(Integer.parseInt(value));
        }
        else if (name.equals(ELEMENT_SCREEN_PADDING_RIGHT))
        {
            _itemProperties.setRightPadding(Integer.parseInt(value));
        }
        else if (name.equals(ELEMENT_SCREEN_WIDTH_AS_PERCENTAGE))
        {
            _itemProperties.setWidthAsPercentage(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_SCREEN_HEIGHT_AS_PERCENTAGE))
        {
            _itemProperties.setHeightAsPercentage(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_SCREEN_VISIBLE))
        {
            _itemProperties.setVisible(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_SCREEN_VA))
        {
            _itemProperties.setVisualAttributeName(value);
        }
        else if (name.equals(ELEMENT_SCREEN_VALUE_PROVIDER))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.ValueBaseItem)
            {
                final EJPluginReportScreenItemProperties.ValueBaseItem item = (ValueBaseItem) _itemProperties;
                item.setValue(value);
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_EXPAND_TO_FIT))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.ValueBaseItem)
            {
                final EJPluginReportScreenItemProperties.ValueBaseItem item = (ValueBaseItem) _itemProperties;
                item.setExpandToFit(Boolean.parseBoolean(value));
                
            }
        }
        
        else if (name.equals(ELEMENT_SCREEN_MARKUP))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.ValueBaseItem)
            {
                final EJPluginReportScreenItemProperties.ValueBaseItem item = (ValueBaseItem) _itemProperties;
                item.setMarkup(EJReportMarkupType.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_HALIGNMENT))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.AlignmentBaseItem)
            {
                final EJPluginReportScreenItemProperties.AlignmentBaseItem item = (AlignmentBaseItem) _itemProperties;
                item.setHAlignment(EJReportScreenAlignment.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_VALIGNMENT))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.AlignmentBaseItem)
            {
                final EJPluginReportScreenItemProperties.AlignmentBaseItem item = (AlignmentBaseItem) _itemProperties;
                item.setVAlignment(EJReportScreenAlignment.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_ROTATION))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.RotatableItem)
            {
                final EJPluginReportScreenItemProperties.RotatableItem item = (RotatableItem) _itemProperties;
                item.setRotation(EJReportScreenRotation.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_TEXT))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Label)
            {
                final EJPluginReportScreenItemProperties.Label item = (EJPluginReportScreenItemProperties.Label) _itemProperties;
                item.setText(value);
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_LINE_WIDTH))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Line)
            {
                final EJPluginReportScreenItemProperties.Line item = (EJPluginReportScreenItemProperties.Line) _itemProperties;
                item.setLineWidth(Double.parseDouble(value));
                
            }
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Rectangle)
            {
                final EJPluginReportScreenItemProperties.Rectangle item = (EJPluginReportScreenItemProperties.Rectangle) _itemProperties;
                item.setLineWidth(Double.parseDouble(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_RECT_RADIUS))
        {
            
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Rectangle)
            {
                final EJPluginReportScreenItemProperties.Rectangle item = (EJPluginReportScreenItemProperties.Rectangle) _itemProperties;
                item.setRadius(Integer.parseInt(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_LINE_STYLE))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Line)
            {
                final EJPluginReportScreenItemProperties.Line item = (EJPluginReportScreenItemProperties.Line) _itemProperties;
                item.setLineStyle(LineStyle.valueOf(value));
                
            }
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Rectangle)
            {
                final EJPluginReportScreenItemProperties.Rectangle item = (EJPluginReportScreenItemProperties.Rectangle) _itemProperties;
                item.setLineStyle(LineStyle.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_LINE_DIRECTION))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Line)
            {
                final EJPluginReportScreenItemProperties.Line item = (EJPluginReportScreenItemProperties.Line) _itemProperties;
                item.setLineDirection(LineDirection.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_MANUAL_FORMAT))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Number)
            {
                final EJPluginReportScreenItemProperties.Number item = (EJPluginReportScreenItemProperties.Number) _itemProperties;
                item.setManualFormat(value);
                
            }
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Date)
            {
                final EJPluginReportScreenItemProperties.Date item = (EJPluginReportScreenItemProperties.Date) _itemProperties;
                item.setManualFormat(value);
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_DEFAULT_IMAGE))
        {
            
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Image)
            {
                final EJPluginReportScreenItemProperties.Image item = (EJPluginReportScreenItemProperties.Image) _itemProperties;
                item.setDefaultImage(value);
                
            }
        }
        
        else if (name.equals(ELEMENT_SCREEN_LINE_STYLE))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Line)
            {
                final EJPluginReportScreenItemProperties.Line item = (EJPluginReportScreenItemProperties.Line) _itemProperties;
                item.setLineStyle(LineStyle.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_LINE_DIRECTION))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Line)
            {
                final EJPluginReportScreenItemProperties.Line item = (EJPluginReportScreenItemProperties.Line) _itemProperties;
                item.setLineDirection(LineDirection.valueOf(value));
                
            }
        }
        else if (name.equals(ELEMENT_SCREEN_MANUAL_FORMAT))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Number)
            {
                final EJPluginReportScreenItemProperties.Number item = (EJPluginReportScreenItemProperties.Number) _itemProperties;
                item.setManualFormat(value);
                
            }
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Date)
            {
                final EJPluginReportScreenItemProperties.Date item = (EJPluginReportScreenItemProperties.Date) _itemProperties;
                item.setManualFormat(value);
                
            }
        }
        
        else if (name.equals(ELEMENT_SCREEN_LOCALE_FORMAT))
        {
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Number)
            {
                final EJPluginReportScreenItemProperties.Number item = (EJPluginReportScreenItemProperties.Number) _itemProperties;
                item.setLocaleFormat(NumberFormats.valueOf(value));
                
            }
            if (_itemProperties instanceof EJPluginReportScreenItemProperties.Date)
            {
                final EJPluginReportScreenItemProperties.Date item = (EJPluginReportScreenItemProperties.Date) _itemProperties;
                item.setLocaleFormat(DateFormats.valueOf(value));
                
            }
        }
    }
    
    @Override
    protected void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        
    }
    
}
