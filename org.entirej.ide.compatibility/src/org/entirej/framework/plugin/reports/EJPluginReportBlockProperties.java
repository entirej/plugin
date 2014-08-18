package org.entirej.framework.plugin.reports;

import java.util.Collection;

import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockContainerItem;
import org.entirej.framework.reports.interfaces.EJReportBlockProperties;
import org.entirej.framework.reports.interfaces.EJReportItemProperties;
import org.entirej.framework.reports.interfaces.EJReportProperties;
import org.entirej.framework.reports.interfaces.EJReportScreenItemProperties;
import org.entirej.framework.reports.interfaces.EJReportScreenProperties;

public class EJPluginReportBlockProperties implements EJReportBlockProperties, BlockContainerItem
{

    @Override
    public EJReportScreenProperties getMainScreenProperties()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EJReportScreenItemProperties getScreenItemProperties(String itemName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<EJReportItemProperties> getAllItemProperties()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EJReportItemProperties getItemProperties(String itemName)
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getCanvasName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean isControlBlock()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isReferenceBlock()
    {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public String getDescription()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EJReportProperties getReportProperties()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getBlockRendererName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getServiceClassName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public EJBlockService<?> getBlockService()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public String getActionProcessorClassName()
    {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getScreenItemNames()
    {
        // TODO Auto-generated method stub
        return null;
    }
    
}
