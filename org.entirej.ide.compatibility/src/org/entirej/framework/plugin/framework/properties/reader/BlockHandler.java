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
package org.entirej.framework.plugin.framework.properties.reader;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.core.enumerations.EJScreenType;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockItemProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginControlBlockProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginLovDefinitionProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginReusableBlockProperties;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;

public class BlockHandler extends EntireJTagHandler
{
    private EJPluginBlockProperties         _blockProperties;
    private EJPluginFormProperties          _formProperties;
    private EJPluginLovDefinitionProperties _lovDefinitionProperties;
    
    private static final String             ELEMENT_BLOCK                             = "block";
    private static final String             ELEMENT_OBJECTGROUP                       = "objectgroup";
    private static final String             ELEMENT_MIRRORED_BLOCK                    = "isMirrored";
    private static final String             ELEMENT_MIRROR_PARENT                     = "mirrorParent";
    private static final String             ELEMENT_DESCRIPTION                       = "description";
    private static final String             ELEMENT_QUERY_ALLOWED                     = "queryAllowed";
    private static final String             ELEMENT_INSERT_ALLOWED                    = "insertAllowed";
    private static final String             ELEMENT_UPDATE_ALLOWED                    = "updateAllowed";
    protected static final String           ELEMENT_ADD_DEFAULT_CONTROL_RECORD        = "addControlBlockDefaultRecord";
    private static final String             ELEMENT_DELETE_ALLOWED                    = "deleteAllowed";
    private static final String             ELEMENT_QUERY_ALL_ROWS                    = "queryAllRows";
    private static final String             ELEMENT_MAX_RESULTS                       = "maxResults";
    private static final String             ELEMENT_PAGE_SIZE                         = "pageSize";
    private static final String             ELEMENT_CANVAS                            = "canvasName";
    private static final String             ELEMENT_RENDERER                          = "blockRendererName";
    private static final String             ELEMENT_SERVICE_CLASS_NAME                = "serviceClassName";
    private static final String             ELEMENT_ACTION_PROCESSOR                  = "actionProcessorClassName";
    private static final String             ELEMENT_PROPERTY                          = "property";
    
    private static final String             ELEMENT_ITEM                              = "item";
    private static final String             ELEMENT_LOV_MAPPING                       = "lovMapping";
    
    private static final String             ELEMENT_MAIN_SCREEN_PROPERTIES            = "mainScreenProperties";
    private static final String             ELEMENT_MAIN_SCREEN                       = "mainScreen";
    private static final String             ELEMENT_QUERY_SCREEN                      = "queryScreen";
    private static final String             ELEMENT_INSERT_SCREEN                     = "insertScreen";
    private static final String             ELEMENT_UPDATE_SCREEN                     = "updateScreen";
    
    private static final String             ELEMENT_APPLICATION_PROPERTIES            = "applicationProperties";
    private static final String             ELEMENT_RENDERER_PROPERTIES               = "blockRendererProperties";
    private static final String             ELEMENT_INSERT_SCREEN_RENDERER_PROPERTIES = "insertScreenRendererProperties";
    private static final String             ELEMENT_QUERY_SCREEN_RENDERER_PROPERTIES  = "queryScreenRendererProperties";
    private static final String             ELEMENT_UPDATE_SCREEN_RENDERER_PROPERTIES = "updateScreenRendererProperties";
    
    private boolean                         _gettingApplicationProperties             = false;
    private String                          _lastApplicationPropertyName              = "";
    
    public BlockHandler(EJPluginFormProperties formProperties, EJPluginLovDefinitionProperties lovDefinitionProperties)
    {
        _formProperties = formProperties;
        _lovDefinitionProperties = lovDefinitionProperties;
    }
    
    public void dispose()
    {
        _formProperties = null;
        _lovDefinitionProperties = null;
        _blockProperties = null;
    }
    
    public EJPluginBlockProperties getBlockProperties()
    {
        return _blockProperties;
    }
    
    public EJPluginBlockProperties createNewBlockProperties(IJavaProject javaProject, EJPluginFormProperties formProperties, String blockName,
            boolean isControlBlock)
    {
        return new EJPluginBlockProperties(formProperties, blockName, isControlBlock);
    }
    
    public void startLocalElement(String name, Attributes attributes) throws SAXException
    {
        if (name.equals(ELEMENT_APPLICATION_PROPERTIES))
        {
            _gettingApplicationProperties = true;
        }
        
        if (_gettingApplicationProperties)
        {
            if (name.equals(ELEMENT_PROPERTY))
            {
                _lastApplicationPropertyName = attributes.getValue("name");
            }
            return;
        }
        
        if (name.equals(ELEMENT_ITEM))
        {
            setDelegate(new ItemHandler(_blockProperties));
            return;
        }
        else if (name.equals(ELEMENT_LOV_MAPPING))
        {
            setDelegate(new LovMappingHandler(_formProperties, _blockProperties));
            return;
        }
        if (name.equals(ELEMENT_BLOCK))
        {
            String blockName = attributes.getValue("name");
            String referenced = attributes.getValue("referenced");
            String referencedBlockName = attributes.getValue("referencedBlockName");
            String isControlBlock = attributes.getValue("controlBlock");
            
            if (Boolean.parseBoolean(referenced))
            {
                String objectGroup = attributes.getValue(ELEMENT_OBJECTGROUP);
                if(objectGroup!=null && objectGroup.length()>0)
                {
                    // dummy
                    _blockProperties = new EJPluginControlBlockProperties(_formProperties, objectGroup);
                    _blockProperties.setFormProperties(_formProperties);
                    _blockProperties.internalSetName(blockName);
                    _blockProperties.setReferencedObjectGroupName(objectGroup);
                }
                else
                {
                    try
                    {
                        EJPluginReusableBlockProperties reusableBlockProperties = _formProperties.getEntireJProperties().getReusableBlockProperties(
                                referencedBlockName);
                        if (reusableBlockProperties != null && reusableBlockProperties.getBlockProperties() != null)
                        {
                            _blockProperties = reusableBlockProperties.getBlockProperties().makeCopy(blockName, false);
                            _blockProperties.setFormProperties(_formProperties);
                            _blockProperties.internalSetName(blockName);
                            _blockProperties.setReferencedBlockName(referencedBlockName);
                            if (_lovDefinitionProperties != null) _lovDefinitionProperties.setBlockProperties(_blockProperties);
                        }
                        else
                        {
                            // dummy
                            _blockProperties = new EJPluginControlBlockProperties(_formProperties, referencedBlockName);
                            _blockProperties.setFormProperties(_formProperties);
                            _blockProperties.internalSetName(blockName);
                            _blockProperties.setReferencedBlockName(referencedBlockName);
                            if (_lovDefinitionProperties != null) _lovDefinitionProperties.setBlockProperties(_blockProperties);
                        }
                        
                    }
                    catch (EJDevFrameworkException e)
                    {
                        // dummy
                        _blockProperties = new EJPluginControlBlockProperties(_formProperties, referencedBlockName);
                        _blockProperties.setFormProperties(_formProperties);
                        _blockProperties.internalSetName(blockName);
                        _blockProperties.setReferencedBlockName(referencedBlockName);
                        if (_lovDefinitionProperties != null) _lovDefinitionProperties.setBlockProperties(_blockProperties);
                    }
                }
                
            }
            else
            {
                if (Boolean.parseBoolean(isControlBlock == null ? "false" : isControlBlock))
                {
                    _blockProperties = new EJPluginControlBlockProperties(_formProperties, blockName);
                }
                else
                {
                    _blockProperties = createNewBlockProperties(_formProperties.getJavaProject(), _formProperties, blockName, false);
                }
                _blockProperties.setLovDefinitionProperties(_lovDefinitionProperties);
            }
            if (_blockProperties != null)
            {
                _blockProperties.setIsReferenced(Boolean.parseBoolean(referenced));
            }
        }
        else if (name.equals(ELEMENT_MAIN_SCREEN_PROPERTIES))
        {
            setDelegate(new MainScreenHandler(_blockProperties));
        }
        if (_blockProperties == null || !_blockProperties.isReferenceBlock())
        {
            if (name.equals(ELEMENT_RENDERER_PROPERTIES))
            {
                // Now I am starting the selection of the renderer properties
                setDelegate(new FrameworkExtensionPropertiesHandler(_formProperties, _blockProperties, ELEMENT_RENDERER_PROPERTIES));
            }
            else if (name.equals(ELEMENT_INSERT_SCREEN_RENDERER_PROPERTIES))
            {
                // Now I am starting the selection of the screen renderer
                // properties
                setDelegate(new FrameworkExtensionPropertiesHandler(_formProperties, _blockProperties, ELEMENT_INSERT_SCREEN_RENDERER_PROPERTIES));
            }
            else if (name.equals(ELEMENT_QUERY_SCREEN_RENDERER_PROPERTIES))
            {
                // Now I am starting the selection of the screen renderer
                // properties
                setDelegate(new FrameworkExtensionPropertiesHandler(_formProperties, _blockProperties, ELEMENT_QUERY_SCREEN_RENDERER_PROPERTIES));
            }
            else if (name.equals(ELEMENT_UPDATE_SCREEN_RENDERER_PROPERTIES))
            {
                // Now I am starting the selection of the screen renderer
                // properties
                setDelegate(new FrameworkExtensionPropertiesHandler(_formProperties, _blockProperties, ELEMENT_UPDATE_SCREEN_RENDERER_PROPERTIES));
            }
            
            else if (name.equals(ELEMENT_MAIN_SCREEN))
            {
                setDelegate(new MainScreenItemGroupHandler(_blockProperties.getScreenItemGroupContainer(EJScreenType.MAIN), ELEMENT_MAIN_SCREEN));
            }
            else if (name.equals(ELEMENT_INSERT_SCREEN))
            {
                setDelegate(new InsertScreenItemGroupHandler(_blockProperties.getScreenItemGroupContainer(EJScreenType.INSERT), ELEMENT_INSERT_SCREEN));
            }
            else if (name.equals(ELEMENT_UPDATE_SCREEN))
            {
                setDelegate(new UpdateScreenItemGroupHandler(_blockProperties.getScreenItemGroupContainer(EJScreenType.UPDATE), ELEMENT_UPDATE_SCREEN));
            }
            else if (name.equals(ELEMENT_QUERY_SCREEN))
            {
                setDelegate(new QueryScreenItemGroupHandler(_blockProperties.getScreenItemGroupContainer(EJScreenType.QUERY), ELEMENT_QUERY_SCREEN));
            }
        }
    }
    
    public void endLocalElement(String name, String value, String untrimmedValue)
    {
        if (name.equals(ELEMENT_BLOCK))
        {
            quitAsDelegate();
            return;
        }
        if (_gettingApplicationProperties)
        {
            if (name.equals(ELEMENT_APPLICATION_PROPERTIES))
            {
                _gettingApplicationProperties = false;
            }
            else if (name.equals(ELEMENT_PROPERTY))
            {
                _blockProperties.addApplicationProperty(_lastApplicationPropertyName, value);
            }
            return;
        }
        
        if (name.equals(ELEMENT_MIRRORED_BLOCK))
        {
            if (value.length() > 0)
            {
                _blockProperties.setIsMirroredBlock(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_MIRROR_PARENT))
        {
            _blockProperties.setMirrorBlockName(value);
        }
        else if (name.equals(ELEMENT_DESCRIPTION))
        {
            _blockProperties.setDescription(value);
        }
        else if (name.equals(ELEMENT_INSERT_ALLOWED))
        {
            _blockProperties.setInsertAllowed(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_QUERY_ALLOWED))
        {
            _blockProperties.setQueryAllowed(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_UPDATE_ALLOWED))
        {
            _blockProperties.setUpdateAllowed(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_DELETE_ALLOWED))
        {
            _blockProperties.setDeleteAllowed(Boolean.parseBoolean(value));
        }
        else if (name.equals(ELEMENT_QUERY_ALL_ROWS))
        {
            if (value.length() > 0)
            {
                _blockProperties.setQueryAllRows(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_PAGE_SIZE))
        {
            if (value.length() > 0)
            {
                _blockProperties.setPageSize(Integer.parseInt(value));
            }
        }
        else if (name.equals(ELEMENT_ADD_DEFAULT_CONTROL_RECORD))
        {
            if (value.length() > 0)
            {
                getBlockProperties().setAddControlBlockDefaultRecord(Boolean.parseBoolean(value));
            }
        }
        else if (name.equals(ELEMENT_MAX_RESULTS))
        {
            if (value.length() > 0)
            {
                _blockProperties.setMaxResults(Integer.parseInt(value));
            }
        }
        else if (name.equals(ELEMENT_CANVAS))
        {
            _blockProperties.setCanvasName(value);
        }
        else if (name.equals(ELEMENT_RENDERER))
        {
            _blockProperties.setBlockRendererName(value, false);
        }
        // else if (name.equals(ELEMENT_QUERY_SCREEN_RENDERER))
        // {
        // _blockProperties.setQueryScreenRendererName(value, false);
        // }
        // else if (name.equals(ELEMENT_INSERT_SCREEN_RENDERER))
        // {
        // _blockProperties.setInsertScreenRendererName(value, false);
        // }
        // else if (name.equals(ELEMENT_UPDATE_SCREEN_RENDERER))
        // {
        // _blockProperties.setUpdateScreenRendererName(value, false);
        // }
        else if (name.equals(ELEMENT_SERVICE_CLASS_NAME))
        {
            _blockProperties.setServiceClassName(value, false);
        }
        else if (name.equals(ELEMENT_ACTION_PROCESSOR))
        {
            _blockProperties.setActionProcessorClassName(value);
        }
    }
    
    public void cleanUpAfterDelegate(String name, EntireJTagHandler currentDelegate)
    {
        if (name.equals(ELEMENT_ITEM))
        {
            EJPluginBlockItemProperties itemProperties = ((ItemHandler) currentDelegate).getItemProperties();
            if (itemProperties == null)
            {
                return;
            }
            
            // If the item name is null, then this item is for a screen item and
            // should be ignored
            if (itemProperties.getName() == null)
            {
                return;
            }
            if (_blockProperties.isReferenceBlock())
            {
                EJPluginBlockItemProperties refItemProps = _blockProperties.getItemContainer().getItemProperties(itemProperties.getName());
                
                if (refItemProps != null)
                {
                    if (itemProperties.getDefaultQueryValue() != null && itemProperties.getDefaultQueryValue().trim().length() > 0)
                    {
                        refItemProps.setDefaultQueryValue(itemProperties.getDefaultQueryValue());
                    }
                    if (itemProperties.getDefaultInsertValue() != null && itemProperties.getDefaultInsertValue().trim().length() > 0)
                    {
                        refItemProps.setDefaultInsertValue(itemProperties.getDefaultInsertValue());
                    }
                }
            }
            else
            {
                _blockProperties.getItemContainer().addItemProperties(itemProperties);
            }
            return;
        }
        else if (name.equals(ELEMENT_LOV_MAPPING))
        {
            _blockProperties.getLovMappingContainer().addLovMappingProperties(((LovMappingHandler) currentDelegate).getLovMappingProperties());
            return;
        }
        if (_blockProperties == null || !_blockProperties.isReferenceBlock())
        {
            if (name.equals(ELEMENT_RENDERER_PROPERTIES))
            {
                if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
                {
                    if (_blockProperties.getBlockRendererDefinition() != null)
                    {
                        _blockProperties.setBlockRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate)
                                .getMainPropertiesGroup(_blockProperties.getBlockRendererDefinition().getBlockPropertyDefinitionGroup()));
                    }
                    else
                    {
                        _blockProperties.setBlockRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
                    }
                }
            }
            else if (name.equals(ELEMENT_QUERY_SCREEN_RENDERER_PROPERTIES))
            {
                if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
                {
                    if (_blockProperties.getQueryScreenRendererDefinition() != null)
                    {
                        _blockProperties.setQueryScreenRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate)
                                .getMainPropertiesGroup(_blockProperties.getQueryScreenRendererDefinition().getQueryScreenPropertyDefinitionGroup()));
                    }
                    else
                    {
                        _blockProperties.setQueryScreenRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
                    }
                }
            }
            else if (name.equals(ELEMENT_INSERT_SCREEN_RENDERER_PROPERTIES))
            {
                if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
                {
                    if (_blockProperties.getInsertScreenRendererDefinition() != null)
                    {
                        _blockProperties.setInsertScreenRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate)
                                .getMainPropertiesGroup(_blockProperties.getInsertScreenRendererDefinition().getInsertScreenPropertyDefinitionGroup()));
                    }
                    else
                    {
                        _blockProperties.setInsertScreenRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
                    }
                }
            }
            else if (name.equals(ELEMENT_UPDATE_SCREEN_RENDERER_PROPERTIES))
            {
                if (((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup() != null)
                {
                    if (_blockProperties.getUpdateScreenRendererDefinition() != null)
                    {
                        _blockProperties.setUpdateScreenRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate)
                                .getMainPropertiesGroup(_blockProperties.getUpdateScreenRendererDefinition().getUpdateScreenPropertyDefinitionGroup()));
                    }
                    else
                    {
                        _blockProperties.setUpdateScreenRendererProperties(((FrameworkExtensionPropertiesHandler) currentDelegate).getMainPropertiesGroup());
                    }
                }
            }
        }
    }
}
