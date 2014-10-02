package org.entirej.framework.reports.interfaces;

import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;

public interface EJReportBorderProperties
{
    
    public enum LineStyle
    {
        SOLID, DASHED, DOTTED, DOUBLE;
        
    }
    
    LineStyle getLineStyle();
    
    double getLineWidth();
    
    EJCoreVisualAttributeProperties getVisualAttributeProperties();
    
    
    boolean isShowTopLine();
    boolean isShowBottomLine();
    boolean isShowLeftLine();
    boolean isShowRightLine();
    
}
