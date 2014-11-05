package org.entirej.framework.plugin.reports;

import java.util.Collection;
import java.util.List;

import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.plugin.reports.containers.EJReportColumnContainer;
import org.entirej.framework.plugin.reports.containers.EJReportScreenItemContainer;
import org.entirej.framework.report.enumerations.EJReportScreenType;
import org.entirej.framework.report.interfaces.EJReportColumnProperties;
import org.entirej.framework.report.interfaces.EJReportScreenProperties;
import org.entirej.framework.report.properties.EJCoreReportRuntimeProperties;
import org.entirej.framework.report.properties.EJReportVisualAttributeProperties;

public class EJPluginReportScreenProperties implements EJReportScreenProperties
{
    
    private EJPluginReportBlockProperties blockProperties;
    
    private int                           x, y, width, height;
    
    private int                           headerColumnHeight = 20;
    private int                           detailColumnHeight = 20;
    private int                           footerColumnHeight = 20;
    private EJReportScreenType            screenType = EJReportScreenType.NONE;
    
    private final BlockGroup              subBlocks  = new BlockGroup("Sub Blocks");
    
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
    
    @Override
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return blockProperties;
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
    public EJReportScreenType getScreenType()
    {
        return screenType;
    }
    
    public void setScreenType(EJReportScreenType screenType)
    {
        this.screenType = screenType;
    }
    
    @Override
    public List<EJPluginReportBlockProperties> getAllSubBlocks()
    {
        return subBlocks.getAllBlockProperties();
    }
    
    public BlockGroup getSubBlocks()
    {
        return subBlocks;
    }
    
    @Override
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
    
    @Override
    public Collection<? extends EJReportColumnProperties> getAllColumns()
    {
        return _columnContainer.getAllColumnProperties();
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

    @Override
    public EJReportVisualAttributeProperties getOddVAProperties()
    {
        return null;
    }
    
    @Override
    public EJReportVisualAttributeProperties getEvenVAProperties()
    {
        return null;
    }
}
