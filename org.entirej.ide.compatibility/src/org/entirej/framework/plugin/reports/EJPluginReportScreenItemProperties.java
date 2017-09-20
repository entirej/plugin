package org.entirej.framework.plugin.reports;

import org.entirej.framework.plugin.reports.EJPluginReportScreenItemProperties.Line.LineStyle;
import org.entirej.framework.report.enumerations.EJReportMarkupType;
import org.entirej.framework.report.enumerations.EJReportScreenAlignment;
import org.entirej.framework.report.enumerations.EJReportScreenItemType;
import org.entirej.framework.report.enumerations.EJReportScreenRotation;
import org.entirej.framework.report.interfaces.EJReportScreenItemProperties;
import org.entirej.framework.report.properties.EJReportVisualAttributeProperties;

public abstract class EJPluginReportScreenItemProperties implements EJReportScreenItemProperties
{
    
    private boolean                       _visible = true;
    
    private String                        name;
    
    private String                        _visualAttributeName;
    
    private int                           x, y, width, height;

    private int                                 rightPadding      = -1;
    private int                                 leftPadding       = -1;
    

    private boolean                     widthAsPercentage, heightAsPercentage;
    
    private EJPluginReportBlockProperties blockProperties;
    
    public EJPluginReportScreenItemProperties(EJPluginReportBlockProperties blockProperties)
    {
        this.blockProperties = blockProperties;
    }
    
    @Override
    public EJReportVisualAttributeProperties getVisualAttributeProperties()
    {
        
        return null;
    }
    
    @Override
    public void setVisualAttribute(String name)
    {
        _visualAttributeName = name;
        
    }
    
    
    
    public int getRightPadding()
    {
        return rightPadding;
    }

    public void setRightPadding(int rightPadding)
    {
        this.rightPadding = rightPadding;
    }

    public int getLeftPadding()
    {
        return leftPadding;
    }

    public void setLeftPadding(int leftPadding)
    {
        this.leftPadding = leftPadding;
    }

    @Override
    public int getWidth()
    {
        return width;
    }
    
    public void setWidth(int width)
    {
        this.width = width;
    }
    
    @Override
    public int getHeight()
    {
        return height;
    }
    
    public void setHeight(int height)
    {
        this.height = height;
    }
    
    public boolean isWidthAsPercentage()
    {
        return widthAsPercentage;
    }

    public void setWidthAsPercentage(boolean widthAsPercentage)
    {
        this.widthAsPercentage = widthAsPercentage;
    }

    public boolean isHeightAsPercentage()
    {
        return heightAsPercentage;
    }

    public void setHeightAsPercentage(boolean heightAsPercentage)
    {
        this.heightAsPercentage = heightAsPercentage;
    }

    
    @Override
    public int getX()
    {
        return x;
    }
    
    public void setX(int x)
    {
        this.x = x;
    }
    
    @Override
    public int getY()
    {
        
        return y;
    }
    
    public void setY(int y)
    {
        this.y = y;
    }
    
    @Override
    public boolean isVisible()
    {
        return _visible;
    }
    
    public void setVisible(boolean visible)
    {
        _visible = visible;
    }
    
    public String getVisualAttributeName()
    {
        return _visualAttributeName;
    }
    
    public void setVisualAttributeName(String _visualAttributeName)
    {
        this._visualAttributeName = _visualAttributeName;
    }
    
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return blockProperties;
    }
    
    public void setBlockProperties(EJPluginReportBlockProperties blockProperties)
    {
        this.blockProperties = blockProperties;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    public static class Label extends AlignmentBaseItem implements RotatableItem
    {
        
        private String                 text;
        
        private EJReportScreenRotation rotation = EJReportScreenRotation.NONE;
        
        public Label(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.LABEL;
        }
        
        public String getText()
        {
            return text;
        }
        
        public void setText(String text)
        {
            this.text = text;
        }
        
        public EJReportScreenRotation getRotation()
        {
            return rotation;
        }
        
        public void setRotation(EJReportScreenRotation rotation)
        {
            this.rotation = rotation;
        }
    }
    
    public static class Line extends EJPluginReportScreenItemProperties
    {
        
        private double        lineWidth     = 1.0;
        private LineStyle     lineStyle     = LineStyle.SOLID;
        private LineDirection lineDirection = LineDirection.TO_DOWN;
        
        public enum LineStyle
        {
            SOLID, DASHED, DOTTED, DOUBLE;
            
        }
        public enum LineDirection
        {
            TO_DOWN, BOTTOM_UP;
            
        }
        
        public Line(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.LINE;
        }
        
        public double getLineWidth()
        {
            return lineWidth;
        }
        
        public void setLineWidth(double lineWidth)
        {
            this.lineWidth = lineWidth;
        }
        
        public LineStyle getLineStyle()
        {
            return lineStyle;
        }
        
        public void setLineStyle(LineStyle lineStyle)
        {
            this.lineStyle = lineStyle;
        }
        
        public LineDirection getLineDirection()
        {
            return lineDirection;
        }
        
        public void setLineDirection(LineDirection lineDirection)
        {
            this.lineDirection = lineDirection;
        }
        
    }
    public static class Rectangle extends EJPluginReportScreenItemProperties
    {
        
        private double    lineWidth = 1.0;
        private int       radius;
        private LineStyle lineStyle = LineStyle.SOLID;
        
        public Rectangle(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.RECTANGLE;
        }
        
        public double getLineWidth()
        {
            return lineWidth;
        }
        
        public void setLineWidth(double lineWidth)
        {
            this.lineWidth = lineWidth;
        }
        
        public LineStyle getLineStyle()
        {
            return lineStyle;
        }
        
        public void setLineStyle(LineStyle lineStyle)
        {
            this.lineStyle = lineStyle;
        }
        
        public void setRadius(int radius)
        {
            this.radius = radius;
        }
        
        public int getRadius()
        {
            return radius;
        }
        
    }
    public static class Text extends ValueBaseItem implements RotatableItem
    {
        private EJReportScreenRotation rotation = EJReportScreenRotation.NONE;
        
        public Text(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.TEXT;
        }
        
        public EJReportScreenRotation getRotation()
        {
            return rotation;
        }
        
        public void setRotation(EJReportScreenRotation rotation)
        {
            this.rotation = rotation;
        }
        
    }
    
    public static interface RotatableItem
    {
        public EJReportScreenRotation getRotation();
        
        public void setRotation(EJReportScreenRotation rotation);
    }
    
    public static abstract class AlignmentBaseItem extends EJPluginReportScreenItemProperties
    {
        
        private EJReportScreenAlignment hAlignment = EJReportScreenAlignment.LEFT;
        private EJReportScreenAlignment vAlignment = EJReportScreenAlignment.CENTER;
        
        private EJPluginReportBorderProperties borderProperties = new EJPluginReportBorderProperties();
        
        public AlignmentBaseItem(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        public EJReportScreenAlignment getHAlignment()
        {
            return hAlignment;
        }
        
        public void setHAlignment(EJReportScreenAlignment hAlignment)
        {
            this.hAlignment = hAlignment;
        }
        
        public EJReportScreenAlignment getVAlignment()
        {
            return vAlignment;
        }
        
        public void setVAlignment(EJReportScreenAlignment vAlignment)
        {
            this.vAlignment = vAlignment;
        }
        
        public EJPluginReportBorderProperties getBorderProperties()
        {
            return borderProperties;
        }
        
        
        
        
    }
    public static abstract class ValueBaseItem extends AlignmentBaseItem
    {
        
        private boolean            expandToFit;
        
        private String             value;
        
        private EJReportMarkupType markup = EJReportMarkupType.NONE;
        
        public ValueBaseItem(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        public String getValue()
        {
            return value;
        }
        
        public void setValue(String value)
        {
            this.value = value;
        }
        
        public boolean isExpandToFit()
        {
            return expandToFit;
        }
        
        public void setExpandToFit(boolean expandToFit)
        {
            this.expandToFit = expandToFit;
        }
        
        public void setMarkup(EJReportMarkupType markup)
        {
            this.markup = markup;
        }
        
        public EJReportMarkupType getMarkup()
        {
            return markup;
        }
        
    }
    public static class Number extends ValueBaseItem implements RotatableItem
    {
        private EJReportScreenRotation rotation     = EJReportScreenRotation.NONE;
        private String                 manualFormat;
        private NumberFormats          localeFormat = NumberFormats.NUMBER;
        public enum NumberFormats
        {
            NUMBER, INTEGER, CURRENCY, PERCENT;
            
        }
        
        public Number(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.NUMBER;
        }
        
        public EJReportScreenRotation getRotation()
        {
            return rotation;
        }
        
        public void setRotation(EJReportScreenRotation rotation)
        {
            this.rotation = rotation;
        }
        
        public String getManualFormat()
        {
            return manualFormat;
        }
        
        public void setManualFormat(String manualFormat)
        {
            this.manualFormat = manualFormat;
        }
        
        public NumberFormats getLocaleFormat()
        {
            return localeFormat;
        }
        
        public void setLocaleFormat(NumberFormats localeFormat)
        {
            this.localeFormat = localeFormat;
        }
        
    }
    public static class Date extends ValueBaseItem implements RotatableItem
    {
        private EJReportScreenRotation rotation     = EJReportScreenRotation.NONE;
        
        private DateFormats            localeFormat = DateFormats.DATE_SHORT;
        
        private String                 manualFormat;
        
        public enum DateFormats
        {
            DATE_LONG, DATE_MEDIUM, DATE_SHORT, DATE_FULL,
            
            DATE_TIME_LONG, DATE_TIME_MEDIUM, DATE_TIME_SHORT, DATE_TIME_FULL,
            
            TIME_LONG, TIME_MEDIUM, TIME_SHORT, TIME_FULL,
            
        }
        
        public Date(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.DATE;
        }
        
        public EJReportScreenRotation getRotation()
        {
            return rotation;
        }
        
        public void setRotation(EJReportScreenRotation rotation)
        {
            this.rotation = rotation;
        }
        
        public void setLocaleFormat(DateFormats localeFormat)
        {
            this.localeFormat = localeFormat;
        }
        
        public DateFormats getLocaleFormat()
        {
            return localeFormat;
        }
        
        public String getManualFormat()
        {
            return manualFormat;
        }
        
        public void setManualFormat(String manualFormat)
        {
            this.manualFormat = manualFormat;
        }
    }
    
    public static class Image extends ValueBaseItem
    {
        
        private String defaultImage;
        
        public String getDefaultImage()
        {
            return defaultImage;
        }
        
        public void setDefaultImage(String defaultImage)
        {
            this.defaultImage = defaultImage;
        }
        
        public Image(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.IMAGE;
        }
        
    }
}
