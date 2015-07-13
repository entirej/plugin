package org.entirej.framework.plugin.reports;

import org.entirej.framework.report.enumerations.EJReportChartType;

public class EJPluginReportChartProperties
{
    
    private EJReportChartType                    chartType = EJReportChartType.BAR_CHART;
    
    private String                               value1Item;
    private String                               value2Item;
    private String                               value3Item;
    private String                               labelItem;
    private String                               categoryItem;
    private String                               seriesItem;
    
    private String                               title;
    private String                               subtitle;
    private String                               titleVA;
    private String                               subtitleVA;
    
    private boolean                              use3dView;
    
    private final EJPluginReportScreenProperties screenProperties;
    
    public EJPluginReportChartProperties(EJPluginReportScreenProperties screenProperties)
    {
        this.screenProperties = screenProperties;
    }
    
    public EJReportChartType getChartType()
    {
        return chartType;
    }
    
    public void setChartType(EJReportChartType chartType)
    {
        this.chartType = chartType;
    }
    
    public EJPluginReportScreenProperties getScreenProperties()
    {
        return screenProperties;
    }
    
    public String getValue1Item()
    {
        return value1Item;
    }
    
    public void setValue1Item(String valueItem)
    {
        this.value1Item = valueItem;
    }
    
    public String getValue2Item()
    {
        return value2Item;
    }
    
    public void setValue2Item(String valueItem)
    {
        this.value2Item = valueItem;
    }
    
    public String getValue3Item()
    {
        return value3Item;
    }
    
    public void setValue3Item(String valueItem)
    {
        this.value3Item = valueItem;
    }
    
    public String getLabelItem()
    {
        return labelItem;
    }
    
    public void setLabelItem(String labelItem)
    {
        this.labelItem = labelItem;
    }
    
    public String getCategoryItem()
    {
        return categoryItem;
    }
    
    public void setCategoryItem(String categoryItem)
    {
        this.categoryItem = categoryItem;
    }
    
    public String getSeriesItem()
    {
        return seriesItem;
    }
    
    public void setSeriesItem(String seriesItem)
    {
        this.seriesItem = seriesItem;
    }
    
    public String getTitle()
    {
        return title;
    }
    
    public void setTitle(String title)
    {
        this.title = title;
    }
    
    public String getSubtitle()
    {
        return subtitle;
    }
    
    public void setSubtitle(String subtitle)
    {
        this.subtitle = subtitle;
    }
    
    public String getTitleVA()
    {
        return titleVA;
    }
    
    public void setTitleVA(String titleVA)
    {
        this.titleVA = titleVA;
    }
    
    public String getSubtitleVA()
    {
        return subtitleVA;
    }
    
    public void setSubtitleVA(String subtitleVA)
    {
        this.subtitleVA = subtitleVA;
    }
    
    public boolean isUse3dView()
    {
        return use3dView;
    }
    
    public void setUse3dView(boolean use3dView)
    {
        this.use3dView = use3dView;
    }
    
}
