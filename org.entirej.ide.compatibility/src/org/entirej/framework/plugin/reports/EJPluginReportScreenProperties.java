package org.entirej.framework.plugin.reports;

import java.util.List;

import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.reports.enumerations.EJReportScreenType;
import org.entirej.framework.reports.interfaces.EJReportBlockProperties;
import org.entirej.framework.reports.interfaces.EJReportScreenProperties;

public class EJPluginReportScreenProperties implements EJReportScreenProperties
{
    
    private EJReportBlockProperties blockProperties;
    
    private int                     x, y, width, height;
    private EJReportScreenType      screenType = EJReportScreenType.NONE;
    
    private final BlockGroup              subBlocks  = new BlockGroup("Sub Blocks");
    
    public EJPluginReportScreenProperties(EJReportBlockProperties blockProperties)
    {
        this.blockProperties = blockProperties;
    }
    
    @Override
    public EJReportBlockProperties getBlockProperties()
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
}
