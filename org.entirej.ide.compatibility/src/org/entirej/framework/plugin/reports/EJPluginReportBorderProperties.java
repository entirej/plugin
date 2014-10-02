package org.entirej.framework.plugin.reports;

import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.reports.interfaces.EJReportBorderProperties;

public class EJPluginReportBorderProperties implements EJReportBorderProperties
{
    
    private LineStyle lineStyle = LineStyle.SOLID;
    private double       lineWidth = 1;
    
    private boolean   showTopLine;
    private boolean   showBottomLine;
    private boolean   showLeftLine;
    private boolean   showRightLine;
    
    private String    visualAttributeName;
    
    public LineStyle getLineStyle()
    {
        return lineStyle;
    }
    
    public void setLineStyle(LineStyle lineStyle)
    {
        this.lineStyle = lineStyle;
    }
    
    public double getLineWidth()
    {
        return lineWidth;
    }
    
    public void setLineWidth(double lineWidth)
    {
        this.lineWidth = lineWidth;
    }
    
    public boolean isShowTopLine()
    {
        return showTopLine;
    }
    
    public void setShowTopLine(boolean showTopLine)
    {
        this.showTopLine = showTopLine;
    }
    
    public boolean isShowBottomLine()
    {
        return showBottomLine;
    }
    
    public void setShowBottomLine(boolean showBottomLine)
    {
        this.showBottomLine = showBottomLine;
    }
    
    public boolean isShowLeftLine()
    {
        return showLeftLine;
    }
    
    public void setShowLeftLine(boolean showLeftLine)
    {
        this.showLeftLine = showLeftLine;
    }
    
    public boolean isShowRightLine()
    {
        return showRightLine;
    }
    
    public void setShowRightLine(boolean showRightLine)
    {
        this.showRightLine = showRightLine;
    }
    
    public String getVisualAttributeName()
    {
        return visualAttributeName;
    }
    
    public void setVisualAttributeName(String visualAttributeName)
    {
        this.visualAttributeName = visualAttributeName;
    }
    
    @Override
    public EJCoreVisualAttributeProperties getVisualAttributeProperties()
    {
        // igmore this in PLUGIN
        return null;
    }
    
}
