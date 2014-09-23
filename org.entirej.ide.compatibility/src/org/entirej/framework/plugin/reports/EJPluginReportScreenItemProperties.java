package org.entirej.framework.plugin.reports;

import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.reports.enumerations.EJReportScreenAlignment;
import org.entirej.framework.reports.enumerations.EJReportScreenItemType;
import org.entirej.framework.reports.enumerations.EJReportScreenRotation;
import org.entirej.framework.reports.interfaces.EJReportScreenItemProperties;
import org.omg.CORBA.PUBLIC_MEMBER;

public abstract class EJPluginReportScreenItemProperties implements EJReportScreenItemProperties
{
    
    private boolean                       _visible = true;
    
    private String                        name;
    
    private String                        _visualAttributeName;
    
    private int                           x, y, width, height;
    
    private EJPluginReportBlockProperties blockProperties;
    
    public EJPluginReportScreenItemProperties(EJPluginReportBlockProperties blockProperties)
    {
        this.blockProperties = blockProperties;
    }
    
    @Override
    public EJCoreVisualAttributeProperties getVisualAttributeProperties()
    {
        
        return null;
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
        
      
        
        
        public Line(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.LINE;
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
        private EJReportScreenAlignment vAlignment = EJReportScreenAlignment.TOP;
        
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
        
    }
    public static abstract class ValueBaseItem extends AlignmentBaseItem
    {
        
        private String value;
        
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
        
        public Image(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.DATE;
        }
        
    }
}
