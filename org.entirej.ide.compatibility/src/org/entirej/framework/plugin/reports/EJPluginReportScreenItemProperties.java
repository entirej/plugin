package org.entirej.framework.plugin.reports;

import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.reports.enumerations.EJReportScreenItemType;
import org.entirej.framework.reports.interfaces.EJReportScreenItemProperties;

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
    
    public static class Label extends EJPluginReportScreenItemProperties
    {
        
        private String text;
        
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
    }
    public static class Text extends ValueBaseItem
    {
        
        public Text(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.TEXT;
        }
        
    }
    public static abstract class ValueBaseItem extends EJPluginReportScreenItemProperties
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
    public static class Number extends ValueBaseItem
    {
        
        public Number(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.NUMBER;
        }
        
    }
    public static class Date extends ValueBaseItem
    {
        
        public Date(EJPluginReportBlockProperties blockProperties)
        {
            super(blockProperties);
        }
        
        @Override
        public EJReportScreenItemType getType()
        {
            return EJReportScreenItemType.DATE;
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
