package org.entirej.framework.plugin.reports;

import java.util.Collection;
import java.util.List;

import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.plugin.reports.containers.EJReportColumnContainer;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.framework.report.properties.EJReportVisualAttributeProperties;

public class EJPluginReportScreenProperties 
{
    
    private EJPluginReportBlockProperties blockProperties;
    
    private int                           x, y, width, height;
    
    private int                           headerColumnHeight = 20;
    private int                           detailColumnHeight = 20;
    private int                           footerColumnHeight = 20;
    private EJReportScreenType            screenType         = EJReportScreenType.NONE;
    
    private final BlockGroup              subBlocks          = new BlockGroup("Sub Blocks");
    
    private EJReportScreenItemContainer   _screenItemContainer;
    private EJReportColumnContainer       _columnContainer;
    
    private String                        oddRowVAName;
    private String                        evenRowVAName;
    
    public EJPluginReportScreenProperties(EJPluginReportBlockProperties blockProperties)
    {
        this.blockProperties = blockProperties;
        _screenItemContainer = new EJReportScreenItemContainer(blockProperties, this);
        _columnContainer = new EJReportColumnContainer(blockProperties);
    }
    
   
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return blockProperties;
    }
    
   
    public int getWidth()
    {
        return width;
    }
    
    public void setWidth(int width)
    {
        this.width = width;
    }
 
    
   
    
   
    public int getHeight()
    {
        return height;
    }
    
    public void setHeight(int height)
    {
        this.height = height;
    }
    
    
    public int getX()
    {
        return x;
    }
    
    public void setX(int x)
    {
        this.x = x;
    }
    
    
    public int getY()
    {
        
        return y;
    }
    
    public void setY(int y)
    {
        this.y = y;
    }
    
    
    public EJReportScreenType getScreenType()
    {
        return screenType;
    }
    
    public void setScreenType(EJReportScreenType screenType)
    {
        this.screenType = screenType;
    }
    
  
    public List<EJPluginReportBlockProperties> getAllSubBlocks()
    {
        return subBlocks.getAllBlockProperties();
    }
    
    public BlockGroup getSubBlocks()
    {
        return subBlocks;
    }
    
    
    public Collection<EJPluginReportScreenItemProperties> getScreenItems()
    {
        return _screenItemContainer.getAllItemProperties();
    }
    
    public EJReportScreenItemContainer getScreenItemContainer()
    {
        return _screenItemContainer;
    }
    
    public EJReportColumnContainer getColumnContainer()
    {
        return _columnContainer;
    }
    
    
    
    public String getOddRowVAName()
    {
        return oddRowVAName;
    }
    
    public void setOddRowVAName(String oddRowVAName)
    {
        this.oddRowVAName = oddRowVAName;
    }
    
    public String getEvenRowVAName()
    {
        return evenRowVAName;
    }
    
    public void setEvenRowVAName(String evenRowVAName)
    {
        this.evenRowVAName = evenRowVAName;
    }
    
    public int getHeaderColumnHeight()
    {
        return headerColumnHeight;
    }
    
    public void setHeaderColumnHeight(int headerColumnHeight)
    {
        this.headerColumnHeight = headerColumnHeight;
    }
    
    public int getDetailColumnHeight()
    {
        return detailColumnHeight;
    }
    
    public void setDetailColumnHeight(int detailColumnHeight)
    {
        this.detailColumnHeight = detailColumnHeight;
    }
    
    public int getFooterColumnHeight()
    {
        return footerColumnHeight;
    }
    
    public void setFooterColumnHeight(int footerColumnHeight)
    {
        this.footerColumnHeight = footerColumnHeight;
    }
    
    
    public EJReportVisualAttributeProperties getOddVAProperties()
    {
        return null;
    }
    
   
    public EJReportVisualAttributeProperties getEvenVAProperties()
    {
        return null;
    }
}
