package org.entirej.framework.plugin.reports;

import java.util.ArrayList;
import java.util.Collection;

import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.reports.containers.EJReportBlockItemContainer;
import org.entirej.framework.reports.interfaces.EJReportBlockProperties;
import org.entirej.framework.reports.interfaces.EJReportItemProperties;
import org.entirej.framework.reports.interfaces.EJReportProperties;
import org.entirej.framework.reports.interfaces.EJReportScreenItemProperties;

public class EJPluginReportBlockProperties implements EJReportBlockProperties, BlockContainerItem
{
    
    private boolean                        _isControlBlock           = false;
    
    private String                         _blockDescription         = "";
    private boolean                        _isReferenced             = false;
    private String                         _canvasName               = "";
    
    private EJPluginReportProperties       _reportProperties;
    private String                         _name                     = "";
    private String                         _blockRendererName        = "";
    
    private EJPluginReportScreenProperties _mainScreenProperties;
    
    private String                         _serviceClassName;
    private String                         _actionProcessorClassName = "";
    
    private EJReportBlockItemContainer     _itemContainer;
    
    public EJPluginReportBlockProperties(EJPluginReportProperties formProperties, String blockName, boolean isCcontrolBlock)
    {
        _name = blockName;
        _isControlBlock = isCcontrolBlock;
        _mainScreenProperties = new EJPluginReportScreenProperties(this);
        _itemContainer = new EJReportBlockItemContainer(this);
    }
    
    @Override
    public EJPluginReportScreenProperties getMainScreenProperties()
    {
        return _mainScreenProperties;
    }
    
    @Override
    public EJReportScreenItemProperties getScreenItemProperties(String itemName)
    {
        return _mainScreenProperties.getScreenItemProperties(itemName);
    }
    
    @Override
    public Collection<EJReportItemProperties> getAllItemProperties()
    {
        return new ArrayList<EJReportItemProperties>(_itemContainer.getAllItemProperties());
    }
    
    @Override
    public EJPluginReportItemProperties getItemProperties(String itemName)
    {
        // TODO Auto-generated method stub
        return _itemContainer.getItemProperties(itemName);
    }
    
    @Override
    public String getCanvasName()
    {
        return _canvasName;
    }
    
    public void setCanvasName(String canvasName)
    {
        this._canvasName = canvasName;
    }
    
    @Override
    public boolean isControlBlock()
    {
        return _isControlBlock;
    }
    
    public void setControlBlock(boolean isControlBlock)
    {
        _isControlBlock = isControlBlock;
    }
    
    @Override
    public boolean isReferenceBlock()
    {
        return _isReferenced;
    }
    
    @Override
    public String getDescription()
    {
        return _blockDescription;
    }
    
    public void setDescription(String description)
    {
        _blockDescription = description;
    }
    
    @Override
    public EJReportProperties getReportProperties()
    {
        return _reportProperties;
    }
    

    public String getName()
    {
        return _name;
    }
    

    public void internalSetName(String blockName)
    {
        if (blockName != null && blockName.trim().length() > 0)
        {
            _name = blockName;
        }
    }
    
    @Override
    public String getBlockRendererName()
    {
        return _blockRendererName;
    }
    
  
    
   
    
 
    public String getActionProcessorClassName()
    {
        return _actionProcessorClassName;
    }
    
 
    public void setActionProcessorClassName(String processorClassName)
    {
        _actionProcessorClassName = processorClassName;
        
    }
    
    /**
     * Returns the class name of the service that is responsible for the data
     * manipulation for this block
     * 
     * @return the service class name
     */
    public String getServiceClassName()
    {
        return _serviceClassName;
    }
    
    /**
     * Sets the class name of the service that is responsible for the retrieval
     * and manipulation of this blocks data
     * 
     * @return the service class name
     */
    public void setServiceClassName(String serviceClassName)
    {
        _serviceClassName = serviceClassName;
    }
    
    @Override
    public Collection<String> getScreenItemNames()
    {
        return _mainScreenProperties.getScreenItemNames();
    }

    @Override
    public EJBlockService<?> getBlockService()
    {
        return null;
    }
    
}
