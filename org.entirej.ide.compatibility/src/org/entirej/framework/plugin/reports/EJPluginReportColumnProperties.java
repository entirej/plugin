package org.entirej.framework.plugin.reports;

import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.framework.report.interfaces.EJReportBlockProperties;
import org.entirej.framework.report.interfaces.EJReportColumnProperties;

public class EJPluginReportColumnProperties implements EJReportColumnProperties
{
    
    private EJPluginReportBlockProperties  blockProperties;
    
    private String                         name;
    private boolean                        showHeader;
    private boolean                        showFooter;
    
    private EJPluginReportScreenProperties header;
    private EJPluginReportScreenProperties detail;
    private EJPluginReportScreenProperties footer;
    
    private EJPluginReportBorderProperties headerBorderProperties = new EJPluginReportBorderProperties();
    private EJPluginReportBorderProperties detailBorderProperties = new EJPluginReportBorderProperties();
    private EJPluginReportBorderProperties footerBorderProperties = new EJPluginReportBorderProperties();
    
    public EJPluginReportColumnProperties(EJPluginReportBlockProperties blockProperties)
    {
        this.blockProperties = blockProperties;
        header = new EJPluginReportScreenProperties(blockProperties);
        detail = new EJPluginReportScreenProperties(blockProperties);
        footer = new EJPluginReportScreenProperties(blockProperties);
        header.setScreenType(EJReportScreenType.FORM_LATOUT);
        detail.setScreenType(EJReportScreenType.FORM_LATOUT);
        footer.setScreenType(EJReportScreenType.FORM_LATOUT);
    }
    
    @Override
    public EJReportBlockProperties getBlockProperties()
    {
        return blockProperties;
    }
    
    public boolean isShowHeader()
    {
        return showHeader;
    }
    
    public void setShowHeader(boolean showHeader)
    {
        this.showHeader = showHeader;
    }
    
    public boolean isShowFooter()
    {
        return showFooter;
    }
    
    public void setShowFooter(boolean showFooter)
    {
        this.showFooter = showFooter;
    }
    
    @Override
    public EJPluginReportScreenProperties getHeaderScreen()
    {
        return header;
    }
    
    @Override
    public EJPluginReportScreenProperties getDetailScreen()
    {
        return detail;
    }
    
    @Override
    public EJPluginReportScreenProperties getFooterScreen()
    {
        return footer;
    }
    
    public String getName()
    {
        return name;
    }
    
    public void setName(String name)
    {
        this.name = name;
    }
    
    @Override
    public EJPluginReportBorderProperties getHeaderBorderProperties()
    {
        return headerBorderProperties;
    }
    
    @Override
    public EJPluginReportBorderProperties getDetailBorderProperties()
    {
        return detailBorderProperties;
    }
    
    
    @Override
    public EJPluginReportBorderProperties getFooterBorderProperties()
    {
        return footerBorderProperties;
    }
   
}
