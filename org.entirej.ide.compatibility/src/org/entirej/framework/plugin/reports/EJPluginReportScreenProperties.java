package org.entirej.framework.plugin.reports;

import java.util.Collection;

import org.entirej.framework.reports.interfaces.EJReportBlockProperties;
import org.entirej.framework.reports.interfaces.EJReportScreenItemProperties;
import org.entirej.framework.reports.interfaces.EJReportScreenProperties;

public class EJPluginReportScreenProperties implements EJReportScreenProperties
{
    
    private EJReportBlockProperties blockProperties;
    
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
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public int getHeight()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public int getX()
    {
        // TODO Auto-generated method stub
        return 0;
    }
    
    @Override
    public int getY()
    {
        // TODO Auto-generated method stub
        return 0;
    }

    public EJReportScreenItemProperties getScreenItemProperties(String itemName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    public Collection<String> getScreenItemNames()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
