/*******************************************************************************
 * Copyright 2013 CRESOFT AG
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 * 
 * Contributors: CRESOFT AG - initial API and implementation
 ******************************************************************************/
/*
 * Created on Nov 3, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties;

import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJItemProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayProperties;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevItemRendererDefinition;
import org.entirej.framework.plugin.EJPluginParameterChecker;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;


public class EJPluginBlockItemProperties implements EJItemProperties, EJDevBlockItemDisplayProperties, EJPluginFormPreviewProvider
{
    /**
     * 
     */
    private static final long serialVersionUID = 3324882767035774955L;
    private boolean                        _belongsToControlBlock;
    private String                         _defaultInsertValue     = "";
    private String                         _defaultQueryValue      = "";
    private EJPluginBlockProperties        _blockProperties        = null;
    private String                         _name;
    private String                         _dataTypeClassName      = "";
    private boolean                        _blockServiceItem       = false;
    private boolean                        _mandatory              = false;
    private EJFrameworkExtensionProperties _itemRendererProperties = null;
    private String                         _itemRendererName       = "";
    
    public EJPluginBlockItemProperties(EJPluginBlockProperties blockProperties, boolean belongsToControlBlock)
    {
        this(blockProperties, "ITEM1", belongsToControlBlock);
    }
    
    public EJPluginBlockItemProperties(EJPluginBlockProperties blockProperties, String itemName, boolean belongsToControlBlock)
    {
        EJPluginParameterChecker.checkNotNull(blockProperties, "ItemProperties", "blockProperties");
        EJPluginParameterChecker.checkNotNull(itemName, "ItemProperties", "itemName");
        
        _name = itemName;
        _blockProperties = blockProperties;
        _belongsToControlBlock = belongsToControlBlock;
    }
    
    public void dispose()
    {
        _itemRendererProperties = null;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _blockProperties.getFormProperties();
    }
    
    /**
     * Returns the properties for the block to which this item belongs
     * 
     * @return The properties of the block to which this item belongs
     */
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public String getBlockName()
    {
        return _blockProperties.getName();
    }
    
    /**
     * Indicates if this item belongs to a control block
     * 
     * @return <code>true</code> if this item belongs to a control block
     *         otherwise <code>false</code>
     */
    public boolean belongsToControlBlock()
    {
        return _belongsToControlBlock;
    }
    
    /**
     * Used to set the default value for this item, when a new record is being
     * created
     * <p>
     * The value will be copied whenever a new record is created. This can be
     * when the user wants to create a new record, but also a new query record.
     * If copied in a query record, the value will be a default value but can be
     * overwritten from the accessor
     * 
     * @param defaultInsertValue
     *            The default insert value
     */
    public void setDefaultInsertValue(String defaultInsertValue)
    {
        _defaultInsertValue = defaultInsertValue;
        for (EJPluginBlockProperties childBlock : _blockProperties.getMirrorChildren())
        {
            if (childBlock.getItemContainer().contains(_name))
            {
                childBlock.getItemContainer().getItemProperties(_name).setDefaultInsertValue(defaultInsertValue);
            }
        }
    }
    
    /**
     * Returns the value to be used as a default when inserting a new record.
     * This could be a block item a form parameter or an application parameter
     * 
     * @return The default insert value
     */
    public String getDefaultInsertValue()
    {
        return _defaultInsertValue;
    }
    
    /**
     * Sets the value that will be used as a default for this item when
     * executing a query
     * <p>
     * This value will be set to the blocks {@link QueryCriteria} before the
     * blocks query is executed. This value can be changed by the developer if
     * the query value is added to a query screen or by implementing one of the
     * action processor methods
     * <p>
     * This could be a block item a form parameter or an application parameter.
     * 
     * @param defaultQueryValue
     *            The default query value
     */
    public void setDefaultQueryValue(String defaultQueryValue)
    {
        _defaultQueryValue = defaultQueryValue;
        for (EJPluginBlockProperties childBlock : _blockProperties.getMirrorChildren())
        {
            if (childBlock.getItemContainer().contains(_name))
            {
                childBlock.getItemContainer().getItemProperties(_name).setDefaultQueryValue(defaultQueryValue);
            }
        }
    }
    
    /**
     * Returns the value to be used as a default when executing a query on the
     * block to which this item belongs
     * <p>
     * This could be a block item a form parameter or an application parameter
     * 
     * @return The default insert value
     */
    public String getDefaultQueryValue()
    {
        return _defaultQueryValue;
    }
    
    /**
     * Returns the name of the item
     * <p>
     * If this is a base table item, meaning that it will receive its data
     * directly from the blocks data source, then the name of the item will also
     * be the name of the property within the blocks base object.
     * 
     * @return The name of this item
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * <b>This method is unused within the EntireJ-Plugin and will always return
     * <code>false</code></b>
     * 
     * @return <code>false</code>
     */
    public boolean hasLov()
    {
        return false;
    }
    
    /**
     * The full name of an item is made up as follows:
     * <code>FormName.&lt;LovDefinitionName&gt;.BlockName.ItemName</code>
     * 
     * @return The full name of this item
     */
    public String getFullName()
    {
        StringBuffer thisName = new StringBuffer();
        thisName.append(_blockProperties.getFormProperties().getName());
        thisName.append(".");
        
        if (_blockProperties.isUsedInLovDefinition())
        {
            thisName.append(_blockProperties.getLovDefinition().getName());
            thisName.append(".");
        }
        
        thisName.append(_blockProperties.getName());
        thisName.append(".");
        thisName.append(this.getName());
        
        return thisName.toString();
    }
    
    /**
     * Sets the name of this item
     * <p>
     * 
     * @return The name of this item
     */
    public void setName(String name)
    {
        _name = name;
        for (EJPluginBlockProperties childBlock : _blockProperties.getMirrorChildren())
        {
            if (childBlock.getItemContainer().contains(_name))
            {
                childBlock.getItemContainer().getItemProperties(_name).setName(name);
            }
        }
    }
    
    /**
     * Used to set the data type of this item
     * 
     * @param dataTypeClassName
     *            The class name of this items data type
     */
    public void setDataTypeClassName(String dataTypeClassName)
    {
        if (dataTypeClassName == null || dataTypeClassName.trim().length() == 0)
        {
            _dataTypeClassName = null;
            // _dataTypeClass = null;
            return;
        }
        
        _dataTypeClassName = dataTypeClassName;
        
        for (EJPluginBlockProperties childBlock : _blockProperties.getMirrorChildren())
        {
            if (childBlock.getItemContainer().contains(_name))
            {
                childBlock.getItemContainer().getItemProperties(_name).setDataTypeClassName(dataTypeClassName);
            }
        }
    }
    
    public Class<?> getDataTypeClass()
    {
        return null;
    }
    
    /**
     * Returns the data type of this item
     * 
     * @return The class name of this items data type
     */
    public String getDataTypeClassName()
    {
        return _dataTypeClassName;
    }
    
    /**
     * Indicates if this item receives its value from the blocks data service or
     * if it is populated programatically by the developer
     * 
     * The default value for this item is true
     * 
     * @param isBlockServiceItem
     *            set to true if this item receives its value from the blocks
     *            data service
     */
    public void setBlockServiceItem(boolean isBlockServiceItem)
    {
        _blockServiceItem = isBlockServiceItem;
        for (EJPluginBlockProperties childBlock : _blockProperties.getMirrorChildren())
        {
            if (childBlock.getItemContainer().contains(_name))
            {
                childBlock.getItemContainer().getItemProperties(_name).setBlockServiceItem(isBlockServiceItem);
            }
        }
    }
    
    /**
     * Returns <code>true</code> or <code>false</code> depending on whether this
     * item is populated from the blocks data service
     * 
     * @return <code>true</code> if this item receives its value from the blocks
     *         data service
     */
    public boolean isBlockServiceItem()
    {
        return _blockServiceItem;
    }
    
    /**
     * Indicates if this item is mandatory
     * <p>
     * This value will be used when adding the item to either the insert or
     * Update screens, otherwise the property has no use
     * 
     * @param mandatory
     *            set to <code>true</code> if this is a mandatory item
     */
    public void setMandatoryItem(boolean mandatory)
    {
        _mandatory = mandatory;
        for (EJPluginBlockProperties childBlock : _blockProperties.getMirrorChildren())
        {
            if (childBlock.getItemContainer().contains(_name))
            {
                childBlock.getItemContainer().getItemProperties(_name).setMandatoryItem(mandatory);
            }
        }
    }
    
    /**
     * Indicates if this item is mandatory
     * <p>
     * This value will be used when adding the item to either the insert or
     * Update screens, otherwise the property has no use
     * 
     * @return <code>true</code> if this is a mandatory item, otherwise
     *         <code>false</code>
     */
    public boolean isMandatoryItem()
    {
        return _mandatory;
    }
    
    /**
     * The name of the renderer used for display this item
     * <p>
     * All renderers are defined within the <b>EntireJ Properties</b>
     * 
     * @return the name of this items renderer
     */
    public String getItemRendererName()
    {
        return _itemRendererName;
    }
    
    public EJDevItemRendererDefinition getItemRendererDefinition()
    {
        return ExtensionsPropertiesFactory.loadItemRendererDefinition(getBlockProperties().getEntireJProperties(), _itemRendererName);
        
    }
    
    public void setItemRendererName(String rendererName, boolean addDefaultValues)
    {
        _itemRendererName = rendererName;
        
        if (rendererName == null || rendererName.length() == 0)
        {
            _itemRendererProperties = null;
        }
        else
        {
            
            _itemRendererProperties = ExtensionsPropertiesFactory.createItemRendererProperties(this, addDefaultValues);
        }
    }
    
    public String getBlockRendererName()
    {
        return _blockProperties.getBlockRendererName();
    }
    
    /**
     * Returns the <code>RenderingProperties</code> that are required by the
     * <code>ItemRenderer</code>
     * 
     * @return The required item renderer properties for this item
     */
    public EJFrameworkExtensionProperties getItemRendererProperties()
    {
        return _itemRendererProperties;
    }
    
    public void setItemRendererProperties(EJFrameworkExtensionProperties properties)
    {
        _itemRendererProperties = properties;
    }
    
    EJPluginBlockItemProperties createNewBlockItem(EJPluginBlockProperties forBlock, String name, boolean isControlBlock)
    {
        return new EJPluginBlockItemProperties(forBlock, name, isControlBlock);
    }
    
    public EJPluginBlockItemProperties makeCopy(EJPluginBlockProperties forBlock, boolean forMirror)
    {
        EJPluginBlockItemProperties itemProps = createNewBlockItem(forBlock, _name, forBlock.isControlBlock());
        itemProps.setMandatoryItem(_mandatory);
        itemProps.setDefaultInsertValue(_defaultInsertValue);
        itemProps.setDefaultQueryValue(_defaultQueryValue);
        itemProps.setDataTypeClassName(_dataTypeClassName);
        itemProps.setBlockServiceItem(_blockServiceItem);
        itemProps.setItemRendererName(_itemRendererName, false);
        itemProps.setItemRendererProperties(_itemRendererProperties);
        
        return itemProps;
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        
        buffer.append("\nItem: ");
        buffer.append(getName());
        buffer.append("\n    IsBlockServiceItem:    ");
        buffer.append(_blockServiceItem);
        buffer.append("\n    MandatoryItem:     ");
        buffer.append(_mandatory);
        buffer.append("\nItemRendererProperties:\n");
        buffer.append(_itemRendererProperties);
        
        return buffer.toString();
    }
    
    @Override
    public String getFieldName()
    {
        return null;
    }
}
