package org.entirej.framework.plugin.reports;

import org.entirej.framework.reports.interfaces.EJReportItemProperties;

public class EJPluginReportItemProperties implements EJReportItemProperties
{

    private EJPluginReportBlockProperties blockProperties;
    
    
    private String                         _name;
    private String                         _dataTypeClassName      = "";
    private boolean                        _blockServiceItem       = false;

    private String                         _defaultQueryValue      = "";
    
    public EJPluginReportItemProperties(EJPluginReportBlockProperties blockProperties)
    {
       this.blockProperties = blockProperties;
    }
    public EJPluginReportItemProperties(EJPluginReportBlockProperties blockProperties,String itemName)
    {
        this.blockProperties = blockProperties;
        this._name = itemName;
    }
    
    
    
    public EJPluginReportBlockProperties getBlockProperties()
    {
        return blockProperties;
    }
    
    public String getBlockName()
    {
        return blockProperties.getName();
    }
    
    
    public boolean belongsToControlBlock()
    {
        return blockProperties.isControlBlock();
    }
    
    public void setDefaultQueryValue(String defaultQueryValue)
    {
        _defaultQueryValue = defaultQueryValue;
        
    }
    

    public String getDefaultQueryValue()
    {
        return _defaultQueryValue;
    }

    @Override
    public String getName()
    {
        return _name;
    }

    public void setName(String name)
    {
        _name = name;
    }
    
    @Override
    public String getFullName()
    {
        StringBuffer thisName = new StringBuffer();
        thisName.append(blockProperties.getReportProperties().getName());
        thisName.append(".");
        
        
        
        thisName.append(blockProperties.getName());
        thisName.append(".");
        thisName.append(this.getName());
        
        return thisName.toString();
    }

    public void setDataTypeClassName(String dataTypeClassName)
    {
        if (dataTypeClassName == null || dataTypeClassName.trim().length() == 0)
        {
            _dataTypeClassName = null;
            // _dataTypeClass = null;
            return;
        }
        
        _dataTypeClassName = dataTypeClassName;
     
    }
    


    public void setBlockServiceItem(boolean isBlockServiceItem)
    {
        _blockServiceItem = isBlockServiceItem;
    }

    @Override
    public boolean isBlockServiceItem()
    {
        return _blockServiceItem;
    }



    @Override
    public String getDataTypeClassName()
    {
        return _dataTypeClassName;
    }

    @Override
    public Class<?> getDataTypeClass()
    {
        return null;
    }
    

    
    public String getFieldName()
    {
        return null;
        
    }
    
  
}
