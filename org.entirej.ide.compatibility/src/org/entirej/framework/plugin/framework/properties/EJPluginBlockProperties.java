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

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jface.dialogs.MessageDialog;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.core.extensions.properties.EJCoreFrameworkExtensionProperties;
import org.entirej.framework.core.properties.containers.interfaces.EJItemGroupPropertiesContainer;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJPropertyDefinitionGroup;
import org.entirej.framework.core.properties.interfaces.EJBlockProperties;
import org.entirej.framework.core.properties.interfaces.EJItemGroupProperties;
import org.entirej.framework.core.properties.interfaces.EJItemProperties;
import org.entirej.framework.core.properties.interfaces.EJScreenItemProperties;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockDisplayProperties;
import org.entirej.framework.dev.properties.interfaces.EJDevBlockItemDisplayPropertiesContainer;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevBlockRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevInsertScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevQueryScreenRendererDefinition;
import org.entirej.framework.dev.renderer.definition.interfaces.EJDevUpdateScreenRendererDefinition;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockContainer.BlockContainerItem;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginBlockItemContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginInsertScreenItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginLovMappingContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginMainScreenItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginQueryScreenItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginUpdateScreenItemGroupContainer;
import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;
import org.entirej.ide.core.EJCoreLog;

public class EJPluginBlockProperties implements EJBlockProperties, EJDevBlockDisplayProperties, EJPluginFormPreviewProvider,BlockContainerItem
{
    
    /**
     * 
     */
    private static final long                      serialVersionUID              = -4917065094947867728L;
    private boolean                                _isControlBlock               = false;
    private boolean                                _isMirrorBlock                = false;
    private ArrayList<EJPluginBlockProperties>     _mirrorChildren;
    private String                                 _mirrorParentName;
    private EJPluginBlockProperties                _mirrorParent;
    private String                                 _blockDescription             = "";
    private boolean                                _isReferenced                 = false;
    private String                                 _referencedBlockName          = "";
    private String                                 _referencedObjectGroupName    = "";
    private String                                 _canvasName                   = "";
    
    private EJPluginLovDefinitionProperties        _lovDefinition;
    
    private EJPluginFormProperties                 _formProperties;
    private String                                 _name                         = "";
    private String                                 _blockRendererName            = "";
    
    private EJPluginMainScreenProperties           _mainScreenProperties;
    private EJFrameworkExtensionProperties         _blockRendererProperties;
    private EJFrameworkExtensionProperties         _queryScreenRendererProperties;
    private EJFrameworkExtensionProperties         _insertScreenRendererProperties;
    private EJFrameworkExtensionProperties         _updateScreenRendererProperties;
    
    private String                                 _serviceClassName;
    private String                                 _actionProcessorClassName     = "";
    private boolean                                _insertAllowed                = true;
    
    private boolean                                _addControlBlockDefaultRecord = true;
    private boolean                                _updateAllowed                = true;
    private boolean                                _deleteAllowed                = true;
    private boolean                                _queryAllowed                 = true;
    private boolean                                _queryAllRows                 = false;
    private int                                    _maxResults                   = 0;
    private int                                    _pageSize                     = 0;
    
    private EJPluginBlockItemContainer             _itemContainer;
    private EJPluginMainScreenItemGroupContainer   _mainScreenItemGroupContainer;
    private EJPluginInsertScreenItemGroupContainer _insertScreenItemGroupContainer;
    private EJPluginUpdateScreenItemGroupContainer _updateScreenItemGroupContainer;
    private EJPluginQueryScreenItemGroupContainer  _queryScreenItemGroupContainer;
    
    private HashMap<String, String>                _applicationProperties;
    private EJPluginLovMappingContainer            _lovMappingContainer;
    
    public EJPluginBlockProperties(EJPluginFormProperties formProperties, String blockName, boolean isCcontrolBlock)
    {
        
        _isControlBlock = isCcontrolBlock;
        _itemContainer = new EJPluginBlockItemContainer(this);
        _mainScreenItemGroupContainer = new EJPluginMainScreenItemGroupContainer(this);
        _insertScreenItemGroupContainer = new EJPluginInsertScreenItemGroupContainer(this);
        _updateScreenItemGroupContainer = new EJPluginUpdateScreenItemGroupContainer(this);
        _queryScreenItemGroupContainer = new EJPluginQueryScreenItemGroupContainer(this);
        _lovMappingContainer = new EJPluginLovMappingContainer(this);
        _formProperties = formProperties;
        _name = blockName;
        
        _applicationProperties = new HashMap<String, String>();
        _mirrorChildren = new ArrayList<EJPluginBlockProperties>();
        _mainScreenProperties = new EJPluginMainScreenProperties(this);
    }
    
 
    
    /**
     * Indicates if this block is a mirror block
     * <p>
     * the MirrorParent defines with which block this block should synchronize
     * with
     * 
     * @return <code>true</code> if this is a mirror block otherwise
     *         <code>false</code>
     */
    public boolean isMirrorBlock()
    {
        return _isMirrorBlock;
    }
    
    /**
     * Indicates if this is a mirrored block
     * <p>
     * If the block has been defined as mirrored but there is no mirror parent
     * defined then this block is the mirror parent
     * 
     * @param isMirrored
     *            <code>true</code> if mirrored otherwise <code>false</code>
     */
    public void setIsMirroredBlock(boolean isMirrored)
    {
        _isMirrorBlock = isMirrored;
    }
    
    public ArrayList<EJPluginBlockProperties> getMirrorChildren()
    {
        return _mirrorChildren;
    }
    
    public void addMirrorChild(EJPluginBlockProperties childBlock)
    {
        _mirrorChildren.add(childBlock);
        childBlock.setServiceClassName(getServiceClassName(), false);
        _isMirrorBlock = true;
    }
    
    public void removeMirrorChild(EJPluginBlockProperties childBlock)
    {
        _mirrorChildren.remove(childBlock);
        if (_mirrorChildren.size() == 0)
        {
            _isMirrorBlock = false;
        }
    }
    
    public boolean hasMirroredChildren()
    {
        return !_mirrorChildren.isEmpty();
    }
    
    public boolean isMirrorChild()
    {
        return _isMirrorBlock && _mirrorParentName != null;
    }
    
    /**
     * Returns the name of the block that this block is the mirror of
     * 
     * @return The name of the mirror block
     */
    public String getMirrorBlockName()
    {
        return _mirrorParentName;
    }
    
    /**
     * Sets the name of this blocks mirror
     * 
     * @param name
     *            The name of the block that this block mirrors to
     */
    public void setMirrorBlockName(String name)
    {
        if (name != null && name.trim().length() > 0)
        {
            _mirrorParentName = name;
        }
        else
        {
            _mirrorParentName = null;
        }
    }
    
    /**
     * Indicates if a default record should be created for a control block
     * <p>
     * Most control blocks will be single record blocks with buttons or links
     * that are used to act on data on other blocks. Such blocks will require a
     * record to work correctly and should thus have a default record.
     * <p>
     * If the block is a control block that the user will use to populate
     * records which will later be processed then no default record should be
     * created.
     * <p>
     * This property indicates if such a record should be created
     * 
     * @see #setAddControlBlockDefaultRecord(boolean)
     * @return <code>true</code> if a default record should be created otherwise
     *         <code>false</code>
     */
    public boolean addControlBlockDefaultRecord()
    {
        return _addControlBlockDefaultRecord;
    }
    
    /**
     * Used to indicate if a default record should be added to this block if the
     * block is a control block
     * 
     * @see #addControlBlockDefaultRecord()
     * @param addDefaultRecord
     */
    public void setAddControlBlockDefaultRecord(boolean addDefaultRecord)
    {
        _addControlBlockDefaultRecord = addDefaultRecord;
    }
    
    public void setMirrorParent(EJPluginBlockProperties blockProperties, boolean sync)
    {
        
        _mirrorParent = blockProperties;
        
        if (sync && blockProperties != null)
        {
            _serviceClassName = blockProperties.getServiceClassName();
            
            List<EJPluginBlockItemProperties> remove = new ArrayList<EJPluginBlockItemProperties>();
            for (EJPluginBlockItemProperties itemProps : _itemContainer.getAllItemProperties())
            {
                if (itemProps != null && itemProps.getName() != null)
                {
                    EJPluginBlockItemProperties itemProperties = blockProperties.getItemContainer().getItemProperties(itemProps.getName());
                    if (itemProperties != null)
                        itemProps.setItemRendererProperties(itemProperties.getItemRendererProperties());
                    else
                    {
                        remove.add(itemProps);
                    }
                }
            }
            _itemContainer.getAllItemProperties().removeAll(remove);
        }
        
    }
    
    public EJPluginBlockProperties getMirrorParent()
    {
        return _mirrorParent;
    }
    
    /**
     * Indicates if this block is a control block. If it is, it has no
     * interaction with the data accessor
     * 
     * @return <code>true</code> if this is a control block otherwise
     *         <code>false</code>
     */
    public boolean isControlBlock()
    {
        return _isControlBlock;
    }
    
    public void setIsControlBlock(boolean isControlBlock)
    {
        _isControlBlock = isControlBlock;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public void setFormProperties(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
    }
    
    public void setDescription(String description)
    {
        _blockDescription = description;
    }
    
    public String getDescription()
    {
        return _blockDescription;
    }
    
    public void setIsReferenced(boolean isReferenced)
    {
        _isReferenced = isReferenced;
    }
    
    public String getReferencedBlockName()
    {
        return _referencedBlockName;
    }
    
    public void setReferencedBlockName(String name)
    {
        _referencedBlockName = name;
    }
    
    public String getReferencedObjectGroupName()
    {
        return _referencedObjectGroupName;
    }
    
    public void setReferencedObjectGroupName(String name)
    {
        _referencedObjectGroupName = name;
    }
    
    public boolean isImportFromObjectGroup()
    {
        return _referencedObjectGroupName!=null && _referencedObjectGroupName.trim().length()>0;
    }
    
    public boolean isUsedInLovDefinition()
    {
        return _lovDefinition != null;
    }
    
    public EJPluginLovDefinitionProperties getLovDefinition()
    {
        return _lovDefinition;
    }
    
    public void setLovDefinitionProperties(EJPluginLovDefinitionProperties properties)
    {
        _lovDefinition = properties;
    }
    
    /**
     * Returns the name of the canvas upon which this blocks data should be
     * displayed
     * <p>
     * If the canvas that was chosen is a type of TAB then the blocks data
     * should be displayed on the correct tab page
     * {@link EJPluginBlockProperties#getCanvasTabPageName()}
     * <p>
     * If the canvas name has been set to <code>null</code> then null will be
     * returned. This will mean that the items will not be displayed anywhere
     * <p>
     * 
     * @return the name of the canvas upon which the blocks data will be
     *         displayed
     */
    public String getCanvasName()
    {
        return _canvasName;
    }
    
    /**
     * Sets the canvas name upon which this blocks data will be displayed
     * 
     * @param canvasName
     *            The name of this blocks canvas
     */
    public void setCanvasName(String canvasName)
    {
        _canvasName = canvasName;
    }
    
    public EJPluginEntireJProperties getEntireJProperties()
    {
        return _formProperties.getEntireJProperties();
    }
    
    /**
     * The Action Processor is responsible for actions within the form. Actions
     * can include buttons being pressed, check boxes being selected or pre-post
     * query methods etc.
     * 
     * @return The name of the Action Processor responsible for this form.
     */
    public String getActionProcessorClassName()
    {
        return _actionProcessorClassName;
    }
    
    /**
     * Sets the action processor name for this block
     * <p>
     * If no action processor is defined for this block, then the action methods
     * will propagate to the form level action processor
     * 
     * @param processorClassName
     *            The action processor class name for this block
     */
    public void setActionProcessorClassName(String processorClassName)
    {
        _actionProcessorClassName = processorClassName;
        for (EJPluginBlockProperties childBlock : _mirrorChildren)
        {
            childBlock.setActionProcessorClassName(processorClassName);
        }
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
    
    public void setServiceClassName(String serviceClassName, boolean addItems)
    {
        if (addItems && (_serviceClassName == null || (serviceClassName != null && !_serviceClassName.equals(serviceClassName.trim()))))
        {
            _serviceClassName = serviceClassName.trim();
            
            getItemContainer().sync(getServiceItems());
            
        }
        else
            _serviceClassName = serviceClassName;
    }
    
    public List<EJPluginBlockItemProperties> getServiceItems()
    {
        
        List<EJPluginBlockItemProperties> newItems = new ArrayList<EJPluginBlockItemProperties>();
        try
        {
            
            IType serviceType = getEntireJProperties().getJavaProject().findType(_serviceClassName);
            if (serviceType != null)
            {
                String[] superInterfaces = serviceType.getSuperInterfaceTypeSignatures();
                
                while (superInterfaces.length==0 && !Object.class.getName().equals(Signature.toString(serviceType.getSuperclassTypeSignature())))
                {
                   
                    serviceType = serviceType.newSupertypeHierarchy(new NullProgressMonitor()).getSuperclass(serviceType);
                    superInterfaces = serviceType.getSuperInterfaceTypeSignatures();
                    
                }
                
                
                for (String superInterface : superInterfaces)
                {
                    String typeErasure = Signature.getTypeErasure(Signature.toString(superInterface));
                    
                    if (typeErasure != null && EJBlockService.class.getSimpleName().equals(typeErasure))
                    {
                        String[] typeArguments = Signature.getTypeArguments(superInterface);
                        if (typeArguments.length == 1)
                        {
                            serviceType.getTypeParameter(Signature.toString(typeArguments[0])).getPath();
                            String[][] resolveType = serviceType.resolveType(Signature.toString(typeArguments[0]));
                            if (resolveType != null && resolveType.length == 1)
                            {
                                
                                String[] typeSegments = resolveType[0];
                                String pojoName = Signature.toQualifiedName(typeSegments);
                                IType pojoType = getEntireJProperties().getJavaProject().findType(pojoName);
                                if (pojoType != null)
                                {
                                    IMethod[] methods = pojoType.getMethods();
                                    for (IMethod method : methods)
                                    {
                                        if (!method.isConstructor() && method.getNumberOfParameters() == 0
                                                && (!Signature.SIG_VOID.equals(method.getReturnType())))
                                        {
                                            String name = method.getElementName();
                                            if (name.startsWith("get"))
                                            {
                                                name = name.substring(3);
                                            }
                                            else if (name.startsWith("is"))
                                            {
                                                name = name.substring(2);
                                            }
                                            else
                                            {
                                                continue;
                                            }
                                            // check dose it Has setter name
                                            IMethod setter = pojoType.getMethod(String.format("set%s", name), new String[] { method.getReturnType() });
                                            if (setter != null && setter.exists())
                                            {
                                                String fieldName = String.format("%s%s", name.substring(0, 1).toLowerCase(), name.substring(1));
                                                EJPluginBlockItemProperties item = new EJPluginBlockItemProperties(this, false);
                                                item.setName(fieldName);
                                                String[][] resolveRetrunType = pojoType.resolveType(Signature.toString(method.getReturnType()));
                                                if (resolveRetrunType != null && resolveRetrunType.length == 1)
                                                {
                                                    item.setDataTypeClassName(Signature.toQualifiedName(resolveRetrunType[0]));
                                                }
                                                item.setBlockServiceItem(true);
                                                item.setMandatoryItem(false);
                                                newItems.add(item);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    
                }
            }
            
        }
        catch (JavaModelException e)
        {
            EJCoreLog.logException(e);
        }
        finally
        {
            
        }
        return newItems;
    }
    
    /**
     * Returns the block service used to retrieve and manipulate this blocks
     * data
     * <p>
     * This method will always return <code>null</code> within the plugin
     */
    public EJBlockService<?> getBlockService()
    {
        return null; // _blockService;
    }
    
    /**
     * Returns the internal name of this block
     * 
     * @return The name of this block
     * 
     * @see com.ottomobil.dsys.sprintframework.dataobjects.properties.interfaces.IBlockProperties#getName()
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * <B>INTERNAL USE ONLY</B>
     * <p>
     * This method is used during the creation of a block within the EntireJ
     * Block Wizard
     * 
     * @param blockName
     *            The new block name
     */
    public void internalSetName(String blockName)
    {
        if (blockName != null && blockName.trim().length() > 0)
        {
            _name = blockName;
        }
    }
    
    /**
     * The name of the renderer used for the display of this blocks data
     * <p>
     * All renderers are defined within the <b>EntireJ Properties</b>
     * 
     * @return the blockRendererName
     */
    public String getBlockRendererName()
    {
        return _blockRendererName;
    }
    
    /**
     * Sets the name of the block renderer that is responsible for displaying
     * this blocks data
     * 
     * @param blockRendererName
     *            the name of the Block Renderer
     * @throws NullPointerException
     *             if the renderer name passed is null or of zero length
     */
    public void setBlockRendererName(String blockRendererName, boolean addDefaults)
    {
        if (blockRendererName == null || blockRendererName.trim().length() == 0)
        {
            _blockRendererName = null;
            _blockRendererProperties = null;
        }
        else if (_blockRendererName != null && blockRendererName.equals(_blockRendererName))
        {
            // do nothing otherwise all block renderer property values
            // that have already been set will be lost.
            return;
        }
        else
        {
            
            _blockRendererName = blockRendererName;
            EJDevBlockRendererDefinition _blockRendererDefinition = ExtensionsPropertiesFactory.loadBlockRendererDefinition(
                    _formProperties.getEntireJProperties(), this.getBlockRendererName());
            
            if (_blockRendererDefinition == null)
            {
                return;
            }
            
            EJCoreFrameworkExtensionProperties _newblockRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                    _blockRendererDefinition.getBlockPropertyDefinitionGroup(), null, addDefaults);
            
            if (_newblockRendererProperties != null && _blockRendererProperties != null)
            {
                ExtensionsPropertiesFactory.copyMatchingProperties(_newblockRendererProperties, _blockRendererProperties);
            }
            _blockRendererProperties = _newblockRendererProperties;
            
            setQueryScreenRendererDefinition(_blockRendererDefinition.getQueryScreenRendererDefinition(), addDefaults);
            setInsertScreenRendererDefinition(_blockRendererDefinition.getInsertScreenRendererDefinition(), addDefaults);
            setUpdateScreenRendererDefinition(_blockRendererDefinition.getUpdateScreenRendererDefinition(), addDefaults);
            
            Iterator<EJItemGroupProperties> itemGroups = _mainScreenItemGroupContainer.getAllItemGroupProperties().iterator();
            while (itemGroups.hasNext())
            {
                EJItemGroupProperties itemGroup = itemGroups.next();
                
                EJPropertyDefinitionGroup groupPropertiesDefinitionGroup = _blockRendererDefinition.getItemGroupPropertiesDefinitionGroup();
                if (groupPropertiesDefinitionGroup != null)
                {
                    EJCoreFrameworkExtensionProperties _newGroupRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                            groupPropertiesDefinitionGroup, null, addDefaults);
                    EJFrameworkExtensionProperties rendererProperties = itemGroup.getRendererProperties();
                    if (rendererProperties != null && _newGroupRendererProperties != null)
                    {
                        ExtensionsPropertiesFactory.copyMatchingProperties(_newGroupRendererProperties, rendererProperties);
                        
                    }
                    ((EJPluginItemGroupProperties) itemGroup).setRendererProperties(_newGroupRendererProperties);
                }
                
                Iterator<EJScreenItemProperties> screenItems = itemGroup.getAllItemProperties().iterator();
                while (screenItems.hasNext())
                {
                    EJPluginMainScreenItemProperties props = (EJPluginMainScreenItemProperties) screenItems.next();
                    props.refreshBlockRendererRequiredProperties(addDefaults);
                    
                }
            }
        }
    }
    
    /**
     * Sets the name of the query screen renderer that is responsible for
     * displaying this blocks query screen
     * 
     * @param queryScreenRendererName
     *            the name of the Blocks query screen renderer
     * @throws NullPointerException
     *             if the renderer name passed is null or of zero length
     */
    public void setQueryScreenRendererDefinition(EJDevQueryScreenRendererDefinition queryScreenRendererDef, boolean addDefaultValues)
    {
        if (queryScreenRendererDef == null)
        {
            _queryScreenRendererProperties = null;
        }
        else
        {
            
            EJCoreFrameworkExtensionProperties _newRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                    queryScreenRendererDef.getQueryScreenPropertyDefinitionGroup(), null, addDefaultValues);
            
            if (_newRendererProperties != null && _queryScreenRendererProperties != null)
            {
                ExtensionsPropertiesFactory.copyMatchingProperties(_newRendererProperties, _queryScreenRendererProperties);
            }
            _queryScreenRendererProperties = _newRendererProperties;
        }
        
        Iterator<EJItemGroupProperties> itemGroups = _queryScreenItemGroupContainer.getAllItemGroupProperties().iterator();
        while (itemGroups.hasNext())
        {
            EJItemGroupProperties itemGroup = itemGroups.next();
            
            EJPropertyDefinitionGroup groupPropertiesDefinitionGroup = queryScreenRendererDef != null ? queryScreenRendererDef
                    .getItemGroupPropertiesDefinitionGroup() : null;
            if (groupPropertiesDefinitionGroup != null)
            {
                EJCoreFrameworkExtensionProperties _newGroupRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                        groupPropertiesDefinitionGroup, null, addDefaultValues);
                EJFrameworkExtensionProperties rendererProperties = itemGroup.getRendererProperties();
                if (rendererProperties != null && _newGroupRendererProperties != null)
                {
                    ExtensionsPropertiesFactory.copyMatchingProperties(_newGroupRendererProperties, rendererProperties);
                    
                }
                ((EJPluginItemGroupProperties) itemGroup).setRendererProperties(_newGroupRendererProperties);
            }
            Iterator<EJScreenItemProperties> screenItems = itemGroup.getAllItemProperties().iterator();
            while (screenItems.hasNext())
            {
                EJPluginQueryScreenItemProperties props = (EJPluginQueryScreenItemProperties) screenItems.next();
                props.refreshQueryScreenRendererRequiredProperties(addDefaultValues);
            }
        }
    }
    
    /**
     * Sets the name of the insert screen renderer that is responsible for
     * displaying this blocks insert screen
     * 
     * @param insertScreenRendererName
     *            the name of the Blocks insert screen renderer
     * @throws NullPointerException
     *             if the renderer name passed is null or of zero length
     */
    public void setInsertScreenRendererDefinition(EJDevInsertScreenRendererDefinition insertScreenRendererDef, boolean addDefaultValues)
    {
        if (insertScreenRendererDef == null)
        {
            _insertScreenRendererProperties = null;
        }
        else
        {
            EJCoreFrameworkExtensionProperties _newRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                    insertScreenRendererDef.getInsertScreenPropertyDefinitionGroup(), null, addDefaultValues);
            
            if (_newRendererProperties != null && _insertScreenRendererProperties != null)
            {
                ExtensionsPropertiesFactory.copyMatchingProperties(_newRendererProperties, _insertScreenRendererProperties);
            }
            _insertScreenRendererProperties = _newRendererProperties;
            
        }
        
        Iterator<EJItemGroupProperties> itemGroups = _insertScreenItemGroupContainer.getAllItemGroupProperties().iterator();
        while (itemGroups.hasNext())
        {
            EJItemGroupProperties itemGroup = itemGroups.next();
            EJPropertyDefinitionGroup groupPropertiesDefinitionGroup = insertScreenRendererDef != null ? insertScreenRendererDef
                    .getItemGroupPropertiesDefinitionGroup() : null;
            if (groupPropertiesDefinitionGroup != null)
            {
                EJCoreFrameworkExtensionProperties _newGroupRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                        groupPropertiesDefinitionGroup, null, addDefaultValues);
                EJFrameworkExtensionProperties rendererProperties = itemGroup.getRendererProperties();
                if (rendererProperties != null && _newGroupRendererProperties != null)
                {
                    ExtensionsPropertiesFactory.copyMatchingProperties(_newGroupRendererProperties, rendererProperties);
                    
                }
                ((EJPluginItemGroupProperties) itemGroup).setRendererProperties(_newGroupRendererProperties);
            }
            Iterator<EJScreenItemProperties> screenItems = itemGroup.getAllItemProperties().iterator();
            while (screenItems.hasNext())
            {
                EJPluginInsertScreenItemProperties props = (EJPluginInsertScreenItemProperties) screenItems.next();
                props.refreshInsertScreenRendererRequiredProperties(addDefaultValues);
                
            }
        }
        
    }
    
    /**
     * Sets the name of the update screen renderer that is responsible for
     * displaying this blocks update screen
     * 
     * @param updateScreenRendererName
     *            the name of the Blocks update screen renderer
     * @throws NullPointerException
     *             if the renderer name passed is null or of zero length
     */
    public void setUpdateScreenRendererDefinition(EJDevUpdateScreenRendererDefinition updateScreenRendererDef, boolean addDefaultValues)
    {
        if (updateScreenRendererDef == null)
        {
            _updateScreenRendererProperties = null;
        }
        else
        {
            EJCoreFrameworkExtensionProperties _newRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                    updateScreenRendererDef.getUpdateScreenPropertyDefinitionGroup(), null, addDefaultValues);
            
            if (_newRendererProperties != null && _updateScreenRendererProperties != null)
            {
                ExtensionsPropertiesFactory.copyMatchingProperties(_newRendererProperties, _updateScreenRendererProperties);
            }
            _updateScreenRendererProperties = _newRendererProperties;
        }
        
        Iterator<EJItemGroupProperties> itemGroups = _updateScreenItemGroupContainer.getAllItemGroupProperties().iterator();
        while (itemGroups.hasNext())
        {
            EJItemGroupProperties itemGroup = itemGroups.next();
            EJPropertyDefinitionGroup groupPropertiesDefinitionGroup = updateScreenRendererDef != null ? updateScreenRendererDef
                    .getItemGroupPropertiesDefinitionGroup() : null;
            if (groupPropertiesDefinitionGroup != null)
            {
                EJCoreFrameworkExtensionProperties _newGroupRendererProperties = ExtensionsPropertiesFactory.addExtensionProperties(_formProperties, this,
                        groupPropertiesDefinitionGroup, null, addDefaultValues);
                EJFrameworkExtensionProperties rendererProperties = itemGroup.getRendererProperties();
                if (rendererProperties != null && _newGroupRendererProperties != null)
                {
                    ExtensionsPropertiesFactory.copyMatchingProperties(_newGroupRendererProperties, rendererProperties);
                    
                }
                ((EJPluginItemGroupProperties) itemGroup).setRendererProperties(_newGroupRendererProperties);
            }
            Iterator<EJScreenItemProperties> screenItems = itemGroup.getAllItemProperties().iterator();
            while (screenItems.hasNext())
            {
                EJPluginUpdateScreenItemProperties props = (EJPluginUpdateScreenItemProperties) screenItems.next();
                props.refreshUpdateScreenRendererRequiredProperties(addDefaultValues);
                
            }
        }
        
    }
    
    /**
     * Sets the rendering properties for this block's update screen
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but are used by the update screen renderer assigned to this
     * block
     * 
     * @param renderingProperties
     *            The rendering properties for update screen renderer
     */
    public void setUpdateScreenRendererProperties(EJFrameworkExtensionProperties renderingProperties)
    {
        _updateScreenRendererProperties = renderingProperties;
    }
    
    public EJDevBlockRendererDefinition getBlockRendererDefinition()
    {
        return ExtensionsPropertiesFactory.loadBlockRendererDefinition(_formProperties.getEntireJProperties(), this.getBlockRendererName());
    }
    
    public EJDevQueryScreenRendererDefinition getQueryScreenRendererDefinition()
    {
        if (isUsedInLovDefinition() && getLovDefinition() != null)
        {
            return getLovDefinition().getRendererDefinition().getQueryScreenRendererDefinition();
        }
        EJDevBlockRendererDefinition blockRendererDefinition = getBlockRendererDefinition();
        return blockRendererDefinition != null ? blockRendererDefinition.getQueryScreenRendererDefinition() : null;
    }
    
    public EJDevUpdateScreenRendererDefinition getUpdateScreenRendererDefinition()
    {
        EJDevBlockRendererDefinition blockRendererDefinition = getBlockRendererDefinition();
        return blockRendererDefinition != null ? blockRendererDefinition.getUpdateScreenRendererDefinition() : null;
    }
    
    public EJDevInsertScreenRendererDefinition getInsertScreenRendererDefinition()
    {
        EJDevBlockRendererDefinition blockRendererDefinition = getBlockRendererDefinition();
        return blockRendererDefinition != null ? blockRendererDefinition.getInsertScreenRendererDefinition() : null;
    }
    
    /**
     * Returns the rendering properties for this block
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the block
     * <p>
     * 
     * @return The blocks rendering properties
     */
    public EJFrameworkExtensionProperties getBlockRendererProperties()
    {
        return _blockRendererProperties;
    }
    
    /**
     * Sets the rendering properties for this block
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the block
     * <p>
     * 
     * @param renderingProperties
     *            The rendering properties for this block
     */
    public void setBlockRendererProperties(EJFrameworkExtensionProperties renderingProperties)
    {
        _blockRendererProperties = renderingProperties;
    }
    
    /**
     * Returns the rendering properties for this blocks query screen
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the blocks query screen
     * <p>
     * 
     * @return The blocks query screen rendering properties
     */
    public EJFrameworkExtensionProperties getQueryScreenRendererProperties()
    {
        return _queryScreenRendererProperties;
    }
    
    public void setQueryScreenRendererProperties(EJFrameworkExtensionProperties _queryScreenRendererProperties)
    {
        this._queryScreenRendererProperties = _queryScreenRendererProperties;
    }
    
    /**
     * Returns the rendering properties for this blocks insert screen
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the blocks insert screen
     * <p>
     * 
     * @return The blocks insert screen rendering properties
     */
    public EJFrameworkExtensionProperties getInsertScreenRendererProperties()
    {
        return _insertScreenRendererProperties;
    }
    
    /**
     * Returns the rendering properties for this blocks update screen
     * <p>
     * These properties are not used internally within the EntireJ Core
     * Framework but can be used by the applications rendering engine to display
     * the blocks update screen
     * <p>
     * 
     * @return The blocks update screen rendering properties
     */
    public EJFrameworkExtensionProperties getUpdateScreenRendererProperties()
    {
        return _updateScreenRendererProperties;
    }
    
    /**
     * Returns the main screen properties for this block
     * 
     * @return the main screen properties for this block
     */
    public EJPluginMainScreenProperties getMainScreenProperties()
    {
        return _mainScreenProperties;
    }
    
    public EJScreenItemProperties getScreenItemProperties(EJScreenType screenType, String itemName)
    {
        if (itemName == null)
        {
            return null;
        }
        
        switch (screenType)
        {
            case MAIN:
                return _mainScreenItemGroupContainer.getScreenItemProperties(itemName);
            case INSERT:
                return _insertScreenItemGroupContainer.getScreenItemProperties(itemName);
            case UPDATE:
                return _updateScreenItemGroupContainer.getScreenItemProperties(itemName);
            case QUERY:
                return _queryScreenItemGroupContainer.getScreenItemProperties(itemName);
            default:
                return null;
        }
    }
    
    /**
     * Sets this blocks main screen properties
     * 
     * @param mainScreenProperties
     *            This blocks main screen properties
     */
    public void setMainScreenProperties(EJPluginMainScreenProperties mainScreenProperties)
    {
        if (mainScreenProperties == null)
        {
            return;
        }
        
        _mainScreenProperties = mainScreenProperties;
    }
    
    /**
     * Indicates inserts are allowed to be made on this block.
     * 
     * @return true if this block allows insert operations otherwise false
     */
    public boolean isInsertAllowed()
    {
        return _insertAllowed;
    }
    
    /**
     * Used to set the flag to indicate if insert operations are allowed on this
     * block
     * 
     * @param insertAllowed
     *            True if inserts are allowed, false if they are not.
     */
    public void setInsertAllowed(boolean insertAllowed)
    {
        _insertAllowed = insertAllowed;
    }
    
    /**
     * Indicates if updates are allowed on this blocks data.
     * 
     * @return The flag indicating if update operations are allowed on this
     *         block
     */
    public boolean isUpdateAllowed()
    {
        return _updateAllowed;
    }
    
    /**
     * Used to set the flag to indicate if update operations are allowed on this
     * block
     * 
     * @param updateAllowed
     *            True if updates are allowed, false if they are not.
     */
    public void setUpdateAllowed(boolean updateAllowed)
    {
        _updateAllowed = updateAllowed;
    }
    
    /**
     * Indicates if delete operations are allowed on this blocks data
     * 
     * @return <code>true</code> if delete operations are allowed otherwise
     *         <code>false</code>
     */
    public boolean isDeleteAllowed()
    {
        return _deleteAllowed;
    }
    
    /**
     * Used to set the flag to indicate if delete operations are allowed on this
     * block
     * 
     * @param deleteAllowed
     *            True if deletes are allowed, false if they are not.
     */
    public void setDeleteAllowed(boolean deleteAllowed)
    {
        _deleteAllowed = deleteAllowed;
    }
    
    /**
     * Indicates if queries are alowed on the data blocks defined by these block
     * properties
     * 
     * @return true if query operations are allowed otherwise false
     */
    public boolean isQueryAllowed()
    {
        return _queryAllowed;
    }
    
    /**
     * Used to set the flag to indicate if query operations are allowed on this
     * block
     * 
     * @param pUpdate
     *            True if query are allowed, false if they are not.
     */
    public void setQueryAllowed(boolean queryAllowed)
    {
        _queryAllowed = queryAllowed;
    }
    
    /**
     * The maximum amount of records that should be selected for this block
     * 
     * @return The maximum amount of records to be selected for this block
     */
    public int getMaxResults()
    {
        return _maxResults;
    }
    
    /**
     * sets the maximum number of rows that should be selected for this block
     * 
     * @param maxResults
     *            The maximum number of rows to retrieve if queryAllRows is
     *            selected
     */
    public void setMaxResults(int maxResults)
    {
        _maxResults = maxResults;
        for (EJPluginBlockProperties childBlock : _mirrorChildren)
        {
            childBlock.setMaxResults(maxResults);
        }
    }
    
    /**
     * The page size is the amount of rows returned at one time by the blocks
     * data accessor
     * 
     * @return The page size for the block
     */
    public int getPageSize()
    {
        return _pageSize;
    }
    
    /**
     * Sets the page size for this block
     * <p>
     * Use this option to optimize the retrieval of rows to display to the user.
     * If the block contains thousands of rows, query optimization can be
     * produced by returning a page at a time, thus giving the user quicker
     * results and a shorter waiting time
     * 
     * @param pageSize
     *            the page size for the block
     */
    public void setPageSize(int pageSize)
    {
        _pageSize = pageSize;
        for (EJPluginBlockProperties childBlock : _mirrorChildren)
        {
            childBlock.setPageSize(pageSize);
        }
    }
    
    /**
     * Indicates that the blocks controller should ensure that all rows are
     * retrieved from the data accessor and not in pages
     * 
     * @param allRows
     *            <code>true</code> indicates that all rows will be retrieved.
     *            If <code>false</code> is set, then the data will be retrieved
     *            in pages
     */
    public void setQueryAllRows(boolean allRows)
    {
        _queryAllRows = allRows;
        for (EJPluginBlockProperties childBlock : _mirrorChildren)
        {
            childBlock.setQueryAllRows(allRows);
        }
    }
    
    /**
     * Indicates if all rows should be retrieved for the block instead of in
     * pages
     * 
     * @return the query all rows indicator
     */
    public boolean queryAllRows()
    {
        return _queryAllRows;
    }
    
    public Collection<EJItemProperties> getAllItemProperties()
    {
        List<EJItemProperties> items = new ArrayList<EJItemProperties>(_itemContainer.getAllItemProperties());
        
        return items;
    }
    
    public EJItemProperties getItemProperties(String itemName)
    {
        return _itemContainer.getItemProperties(itemName);
    }
    
    /**
     * @return the itemContainer
     */
    public EJPluginBlockItemContainer getItemContainer()
    {
        return _itemContainer;
    }
    
    public EJDevBlockItemDisplayPropertiesContainer getBlockItemDisplayContainer()
    {
        return _itemContainer;
    }
    
    /**
     * @param itemContainer
     *            the itemContainer to set
     */
    public void setItemContainer(EJPluginBlockItemContainer itemContainer)
    {
        _itemContainer = itemContainer;
    }
    
    public EJPluginItemGroupContainer getMainScreenItemGroupDisplayContainer()
    {
        return _mainScreenItemGroupContainer;
    }
    
    public void setMainScreenItemGroupContainer(EJPluginMainScreenItemGroupContainer itemContainer)
    {
        _mainScreenItemGroupContainer = itemContainer;
    }
    
    public EJPluginItemGroupContainer getUpdateScreenItemGroupDisplayContainer()
    {
        return _updateScreenItemGroupContainer;
    }
    
    public void setUpdateScreenItemGroupContainer(EJPluginUpdateScreenItemGroupContainer itemContainer)
    {
        _updateScreenItemGroupContainer = itemContainer;
    }
    
    public EJPluginItemGroupContainer getQueryScreenItemGroupDisplayContainer()
    {
        return _queryScreenItemGroupContainer;
    }
    
    public void setQueryScreenItemGroupContainer(EJPluginQueryScreenItemGroupContainer itemContainer)
    {
        _queryScreenItemGroupContainer = itemContainer;
    }
    
    /**
     * Returns the <code>ItemGroupContainer</code> that contains all item groups
     * and items for the given screen of this block
     * 
     * @return An <code>ItemGroupContainer</code> containing all items and item
     *         groups of the given screen of this block
     * 
     * @param screenType
     *            The screen type for which the item groups should be returned
     * @return The item group container for the given screen type
     */
    public EJPluginItemGroupContainer getScreenItemGroupContainer(EJScreenType screenType)
    {
        switch (screenType)
        {
            case MAIN:
                return _mainScreenItemGroupContainer;
            case INSERT:
                return _insertScreenItemGroupContainer;
            case QUERY:
                return _queryScreenItemGroupContainer;
            case UPDATE:
                return _updateScreenItemGroupContainer;
        }
        
        return null;
    }
    
    /**
     * Returns the item names displayed on the given screen type
     * 
     * @param screenType
     *            Item displayed on this screen will be returned
     * 
     * @return A <code>List</code> containing all item names displayed on the
     *         given screen
     * 
     */
    public Collection<String> getScreenItemNames(EJScreenType screenType)
    {
        ArrayList<String> names = new ArrayList<String>();
        
        switch (screenType)
        {
            case MAIN:
                addGroupItems(_mainScreenItemGroupContainer, names);
                break;
            case INSERT:
                addGroupItems(_insertScreenItemGroupContainer, names);
                break;
            case QUERY:
                addGroupItems(_queryScreenItemGroupContainer, names);
                break;
            case UPDATE:
                addGroupItems(_updateScreenItemGroupContainer, names);
                break;
        }
        
        return names;
    }
    
    public EJPluginInsertScreenItemGroupContainer getInsertScreenItemGroupDisplayContainer()
    {
        return _insertScreenItemGroupContainer;
    }
    
    public void setInsertScreenItemGroupContainer(EJPluginInsertScreenItemGroupContainer itemContainer)
    {
        _insertScreenItemGroupContainer = itemContainer;
    }
    
    public void setLovMappingContainer(EJPluginLovMappingContainer container)
    {
        _lovMappingContainer = container;
    }
    
    public EJPluginLovMappingContainer getLovMappingContainer()
    {
        if (_isReferenced)
        {
            EJPluginBlockProperties blockProperties = _formProperties.getBlockProperties(_referencedBlockName);
            if (blockProperties != null && !blockProperties.equals(this)) return blockProperties.getLovMappingContainer();
        }
        return _lovMappingContainer;
    }
    
    public Collection<String> getAllApplicationPropertyNames()
    {
        return _applicationProperties.keySet();
    }
    
    public void addApplicationProperty(String name, String value)
    {
        _applicationProperties.put(name, value);
        
        for (EJPluginBlockProperties childBlock : _mirrorChildren)
        {
            childBlock.addApplicationProperty(name, value);
        }
    }
    
    public String getApplicationProperty(String name)
    {
        return _applicationProperties.get(name);
    }
    
    public void removeApplicationProperty(String name)
    {
        if (containsApplicationProperty(name))
        {
            _applicationProperties.remove(name);
        }
        
        for (EJPluginBlockProperties childBlock : _mirrorChildren)
        {
            childBlock.removeApplicationProperty(name);
        }
    }
    
    public boolean containsApplicationProperty(String name)
    {
        return _applicationProperties.containsKey(name);
    }
    
    public boolean setApplicationProperty(String name, String value)
    {
        if (containsApplicationProperty(name))
        {
            addApplicationProperty(name, value);
            for (EJPluginBlockProperties childBlock : _mirrorChildren)
            {
                childBlock.setApplicationProperty(name, value);
            }
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public String toString()
    {
        StringBuffer buffer = new StringBuffer();
        buffer.append("\nBlock: ");
        buffer.append(getName());
        buffer.append("\n  Description:              " + _blockDescription);
        buffer.append("\n  Referenced:               " + _isReferenced);
        buffer.append("\n  ReferencedBlockName:      " + _referencedBlockName);
        buffer.append("\n  CanvasName:               " + _canvasName);
        buffer.append("\n  RendererName:             " + _blockRendererName);
        buffer.append("\n  ServiceClassName:         " + _serviceClassName);
        buffer.append("\n  InsertAllowed:            " + _insertAllowed);
        buffer.append("\n  UpdateAllowed:            " + _updateAllowed);
        buffer.append("\n  DeleteAllowed:            " + _deleteAllowed);
        buffer.append("\n  QueryAllowed:             " + _queryAllowed);
        
        buffer.append("\nITEMS:\n");
        
        Iterator<EJPluginBlockItemProperties> blockItems = _itemContainer.getAllItemProperties().iterator();
        while (blockItems.hasNext())
        {
            buffer.append(blockItems.next());
            buffer.append("\n");
        }
        
        return buffer.toString();
    }
    
    public EJPluginBlockProperties makeCopy(String newName, boolean forMirror)
    {
        return makeCopy(newName, forMirror, null);
    }
    
    public EJPluginBlockProperties makeCopy(String newName, boolean forMirror, EJPluginLovDefinitionProperties newLovDef)
    {
        return makeCopy(newName, forMirror, newLovDef, _formProperties);
    }
    
    public EJPluginBlockProperties createNewBlock(EJPluginFormProperties formProperties, String blockName, boolean isControlBlock)
    {
        return new EJPluginBlockProperties(formProperties, blockName, isControlBlock);
    }
    
    public EJPluginBlockProperties makeCopy(String newName, boolean forMirror, EJPluginLovDefinitionProperties newLovDef, EJPluginFormProperties formProperties)
    {
        if (formProperties == null)
        {
            MessageDialog.openError(EntireJFrameworkPlugin.getSharedInstance().getActiveWorkbenchShell(), "Error", "Cannot copy block without form properties");
            throw new IllegalStateException();
        }
        
        EJPluginBlockProperties targetBlock = createNewBlock(formProperties, newName, _isControlBlock);
        
        if (newLovDef != null)
        {
            targetBlock.setLovDefinitionProperties(newLovDef);
        }
        
        targetBlock.setIsMirroredBlock(forMirror);
        if (forMirror)
        {
            targetBlock.setMirrorBlockName(_name);
            targetBlock.setMirrorParent(this, true);
            addMirrorChild(targetBlock);
        }
        
        targetBlock.setDescription(_blockDescription);
        targetBlock.setMainScreenProperties(_mainScreenProperties.makeCopy(targetBlock));
        targetBlock.setServiceClassName(_serviceClassName);
        
        if (!forMirror)
        {
            // I need to to set the renderer names before I copy the values.
            // Otherwise a null pointer will be thrown
            targetBlock.setBlockRendererName(_blockRendererName, false);
            EJDevBlockRendererDefinition _blockRendererDefinition = getBlockRendererDefinition();
            if (_blockRendererDefinition != null)
            {
                targetBlock.setQueryScreenRendererDefinition(_blockRendererDefinition.getQueryScreenRendererDefinition(), false);
                targetBlock.setInsertScreenRendererDefinition(_blockRendererDefinition.getInsertScreenRendererDefinition(), false);
                targetBlock.setUpdateScreenRendererDefinition(_blockRendererDefinition.getUpdateScreenRendererDefinition(), false);
            }
            
            if (targetBlock.getBlockRendererProperties() != null)
            {
                targetBlock.getBlockRendererProperties().copyValuesFromGroup(_blockRendererProperties);
            }
            _mainScreenItemGroupContainer
                    .copyGroupForScreen(targetBlock.getScreenItemGroupContainer(EJScreenType.MAIN), EJPluginItemGroupContainer.MAIN_SCREEN);
            _insertScreenItemGroupContainer.copyGroupForScreen(targetBlock.getScreenItemGroupContainer(EJScreenType.INSERT),
                    EJPluginItemGroupContainer.INSERT_SCREEN);
            _updateScreenItemGroupContainer.copyGroupForScreen(targetBlock.getScreenItemGroupContainer(EJScreenType.UPDATE),
                    EJPluginItemGroupContainer.UPDATE_SCREEN);
            _queryScreenItemGroupContainer.copyGroupForScreen(targetBlock.getScreenItemGroupContainer(EJScreenType.QUERY),
                    EJPluginItemGroupContainer.QUERY_SCREEN);
            
        }
        
        targetBlock.setActionProcessorClassName(_actionProcessorClassName);
        targetBlock.setInsertAllowed(_insertAllowed);
        targetBlock.setUpdateAllowed(_updateAllowed);
        targetBlock.setDeleteAllowed(_deleteAllowed);
        targetBlock.setQueryAllowed(_queryAllowed);
        targetBlock.setQueryAllRows(_queryAllRows);
        targetBlock.setPageSize(_pageSize);
        
        for (EJPluginBlockItemProperties itemProps : _itemContainer.getAllItemProperties())
        {
            EJPluginBlockItemProperties newItem = itemProps.makeCopy(targetBlock, forMirror);
            targetBlock.getItemContainer().addItemProperties(newItem);
        }
        
        if (!forMirror)
        {
            targetBlock.setLovMappingContainer(_lovMappingContainer.makeCopy(targetBlock));
        }
        
        for (String propertyName : _applicationProperties.keySet())
        {
            targetBlock.addApplicationProperty(propertyName, _applicationProperties.get(propertyName));
        }
        
        return targetBlock;
    }
    
    private void addGroupItems(EJItemGroupPropertiesContainer itemGroupContainer, ArrayList<String> itemNameList)
    {
        Iterator<EJItemGroupProperties> groupProperties = itemGroupContainer.getAllItemGroupProperties().iterator();
        while (groupProperties.hasNext())
        {
            EJItemGroupProperties itemGroupProperties = groupProperties.next();
            Iterator<EJScreenItemProperties> items = itemGroupProperties.getAllItemProperties().iterator();
            while (items.hasNext())
            {
                EJScreenItemProperties itemProps = items.next();
                itemNameList.add(itemProps.getReferencedItemName());
            }
            
            addGroupItems(itemGroupProperties.getChildItemGroupContainer(), itemNameList);
        }
    }
    
    @Override
    public boolean isReferenceBlock()
    {
        return _isReferenced;
    }
    
    public void setInsertScreenRendererProperties(EJCoreFrameworkExtensionProperties _insertScreenRendererProperties)
    {
        this._insertScreenRendererProperties = _insertScreenRendererProperties;
        
    }
    
}
