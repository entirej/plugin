/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
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
 * Contributors: Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.plugin.framework.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.JavaModelException;
import org.entirej.framework.core.application.definition.interfaces.EJApplicationDefinition;
import org.entirej.framework.core.enumerations.EJRendererType;
import org.entirej.framework.core.properties.EJCoreLayoutContainer;
import org.entirej.framework.core.properties.EJCoreMenuContainer;
import org.entirej.framework.core.properties.EJCoreMenuProperties;
import org.entirej.framework.core.properties.EJCoreVisualAttributeContainer;
import org.entirej.framework.core.properties.EJCoreVisualAttributeProperties;
import org.entirej.framework.core.properties.definitions.interfaces.EJFrameworkExtensionProperties;
import org.entirej.framework.core.properties.interfaces.EJEntireJProperties;
import org.entirej.framework.core.properties.interfaces.EJRendererAssignment;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EJPluginConstants;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginAssignedRendererContainer;
import org.entirej.framework.plugin.framework.properties.containers.EJPluginMenuContainer;
import org.entirej.ide.core.EJCoreLog;

public abstract class EJPluginEntireJProperties implements EJEntireJProperties
{
    /**
     * 
     */
    private static final long                  serialVersionUID = 381774160434998099L;
    
    private String                             _version         = "1.0";               // default
    private String                             _applicationManagerDefClassName;
    
    private EJFrameworkExtensionProperties     _applicationManagerProperties;
    private IJavaProject                       _javaProject;
    private ArrayList<String>                  _formPackageNames;
    private String                             _connectionFactoryClassName;
    private String                             _translatorClassName;
    private EJCoreVisualAttributeContainer     _visualAttributeContainer;
    private EJPluginMenuContainer              _menuContainer;
    private EJCoreLayoutContainer              _layoutContainer;
    private EJPluginAssignedRendererContainer  _itemRendererContainer;
    private EJPluginAssignedRendererContainer  _blockRendererContainer;
    private EJPluginAssignedRendererContainer  _formRendererContainer;
    private EJPluginAssignedRendererContainer  _lovRendererContainer;
    private EJPluginAssignedRendererContainer  _menuRenderers;
    private EJPluginAssignedRendererContainer  _appComponentRendererContainer;
    
    private List<EJPluginApplicationParameter> _applicationLevelParameters;
    
    private String                             _reusableBlocksLocation;
    private String                             _reusableLovDefsLocation;
    private String                             _objectGroupDefsLocation;
    
    public EJPluginEntireJProperties(IJavaProject javaProject)
    {
        _javaProject = javaProject;
        _formPackageNames = new ArrayList<String>();
        _visualAttributeContainer = new EJCoreVisualAttributeContainer(new ArrayList<EJCoreVisualAttributeProperties>());
        _menuContainer = new EJPluginMenuContainer();
        _layoutContainer = new EJCoreLayoutContainer();
        _itemRendererContainer = new EJPluginAssignedRendererContainer(EJRendererType.ITEM);
        _blockRendererContainer = new EJPluginAssignedRendererContainer(EJRendererType.BLOCK);
        _formRendererContainer = new EJPluginAssignedRendererContainer(EJRendererType.FORM);
        _lovRendererContainer = new EJPluginAssignedRendererContainer(EJRendererType.LOV);
        _menuRenderers = new EJPluginAssignedRendererContainer(EJRendererType.MENU);
        _appComponentRendererContainer = new EJPluginAssignedRendererContainer(EJRendererType.APP_COMPONENT);
        _applicationLevelParameters = new ArrayList<EJPluginApplicationParameter>();
    }
    
    public abstract EJPluginReusableBlockProperties loadBlock(EJPluginEntireJProperties entirejProperties, IProject project, IFile file, String blockName)
            throws EJDevFrameworkException;
    
    public abstract EJPluginLovDefinitionProperties loadLovDefinition(EJPluginEntireJProperties entirejProperties, IProject project, IFile file,
            String definitionName) throws EJDevFrameworkException;
    
    public abstract EJPluginObjectGroupProperties loadObjectGroupDefinition(EJPluginEntireJProperties entirejProperties, IProject project, IFile file,
            String definitionName) throws EJDevFrameworkException;
    
    public void clear()
    {
        _itemRendererContainer.clear();
        _blockRendererContainer.clear();
        _formRendererContainer.clear();
        _lovRendererContainer.clear();
        _appComponentRendererContainer.clear();
        _menuRenderers.clear();
        _javaProject = null;
    }
    
    public List<EJPluginRenderer> getAllPluginRenderers()
    {
        List<EJPluginRenderer> renderers = new ArrayList<EJPluginRenderer>();
        
        renderers.addAll(_itemRendererContainer.getAllRenderers());
        renderers.addAll(_blockRendererContainer.getAllRenderers());
        renderers.addAll(_formRendererContainer.getAllRenderers());
        renderers.addAll(_lovRendererContainer.getAllRenderers());
        renderers.addAll(_menuRenderers.getAllRenderers());
        renderers.addAll(_appComponentRendererContainer.getAllRenderers());
        
        return renderers;
    }
    
    public String getVersion()
    {
        return _version;
    }
    
    public void setVersion(String version)
    {
        _version = version;
    }
    
    public void setApplicationManagerDefinitionClassName(String className)
    {
        setApplicationManagerDefinitionClassName(className, false);
    }
    
    public void setApplicationManagerDefinitionClassName(String className, boolean addDefult)
    {
        if (className == null || className.trim().length() == 0)
        {
            _applicationManagerDefClassName = "";
            _applicationManagerProperties = null;
        }
        else
        {
            boolean reload = !className.equals(_applicationManagerDefClassName);
            _applicationManagerDefClassName = className;
            
            if (reload)
            {
                EJApplicationDefinition _applicationManagerDef = ExtensionsPropertiesFactory.loadApplicationManager(this, className);
                
                if (_applicationManagerDef != null)
                {
                    _applicationManagerProperties = ExtensionsPropertiesFactory.createApplicationManagerProperties(this, addDefult);
                }
                else
                {
                    _applicationManagerProperties = null;
                }
            }
        }
    }
    
    public String getApplicationManagerDefinitionClassName()
    {
        return _applicationManagerDefClassName;
    }
    
    public String getConnectionFactoryClassName()
    {
        return _connectionFactoryClassName;
    }
    
    public void setConnectionFactoryClassName(String className)
    {
        _connectionFactoryClassName = className;
        
        if (className == null || className.trim().length() == 0)
        {
            _connectionFactoryClassName = null;
        }
    }
    
    public String getTranslatorClassName()
    {
        return _translatorClassName;
    }
    
    public void setTranslatorClassName(String className)
    {
        _translatorClassName = className;
        
        if (className == null || className.trim().length() == 0)
        {
            _translatorClassName = null;
        }
    }
    
    public EJApplicationDefinition getApplicationManager()
    {
        return ExtensionsPropertiesFactory.loadApplicationManager(this, _applicationManagerDefClassName);
    }
    
    public EJFrameworkExtensionProperties getApplicationDefinedProperties()
    {
        return _applicationManagerProperties;
    }
    
    public void setApplicationDefinedProperties(EJFrameworkExtensionProperties properties)
    {
        _applicationManagerProperties = properties;
    }
    
    /**
     * Returns the name of the package in which all reusable blocks are stored
     * 
     * @return The name of the package in which all reusable blocks are stored
     */
    public String getReusableBlocksLocation()
    {
        return _reusableBlocksLocation;
    }
    
    /**
     * Sets the package name in which all reusable blocks are stored
     * 
     * @param location
     *            The name of the package
     */
    public void setReusableBlocksLocation(String location)
    {
        if (location == null || location.trim().length() == 0)
        {
            _reusableBlocksLocation = null;
        }
        else
        {
            _reusableBlocksLocation = location;
        }
    }
    
    /**
     * Returns the name of the package in which all reusable lov definitions are
     * stored
     * 
     * @return The name of the package in which all reusable lov definitions are
     *         stored
     */
    public String getReusableLovDefinitionLocation()
    {
        return _reusableLovDefsLocation;
    }
    
    /**
     * Sets the package name in which all reusable lov definitions are stored
     * 
     * @param location
     *            The name of the package
     */
    public void setReusableLovDefinitionLocation(String location)
    {
        if (location == null || location.trim().length() == 0)
        {
            _reusableLovDefsLocation = null;
        }
        else
        {
            _reusableLovDefsLocation = location;
        }
    }
    
    /**
     * Returns the name of the package in which all object group definitions are
     * stored
     * 
     * @return The name of the package in which all object group definitions are
     *         stored
     */
    public String getObjectGroupDefinitionLocation()
    {
        return _objectGroupDefsLocation;
    }
    
    /**
     * Sets the package name in which all object group definitions are stored
     * 
     * @param location
     *            The name of the package
     */
    public void setObjectGroupDefinitionLocation(String location)
    {
        if (location == null || location.trim().length() == 0)
        {
            _objectGroupDefsLocation = null;
        }
        else
        {
            _objectGroupDefsLocation = location;
        }
    }
    
    /**
     * Used to log an information message to the workbench log file
     * 
     * @param message
     *            The message to log
     */
    public void logInfoMessage(String message)
    {
        System.out.println(message);
    }
    
    /**
     * Used to log an error message to the workbench log file
     * 
     * @param message
     *            The message to log
     * @param ex
     *            The exception that caused the error or <code>null</code> if
     *            none is available
     */
    public void logErrorMessage(String message, Exception ex)
    {
        System.err.println(message);
        ex.printStackTrace();
    }
    
    public EJCoreVisualAttributeContainer getVisualAttributesContainer()
    {
        return _visualAttributeContainer;
    }
    
    public EJPluginMenuContainer getPluginMenuContainer()
    {
        return _menuContainer;
    }
    
    @Override
    public EJCoreLayoutContainer getLayoutContainer()
    {
        return _layoutContainer;
    }
    
    public void setLayoutContainer(EJCoreLayoutContainer _layoutContainer)
    {
        this._layoutContainer = _layoutContainer;
    }
    
    /**
     * Retrieve a <code>Collection</code> containing all package names where
     * form definition files can be found
     * 
     * @return A <code>Collection</code> containing forms package names
     */
    public Collection<String> getFormPackageNames()
    {
        return _formPackageNames;
    }
    
    /**
     * Adds a given package name to the list of form package names
     * 
     * @param packageName
     *            The package name to add
     * @throws NullPointerException
     *             if the package name passed is either null or of zero length
     */
    public void addFormPackageName(String packageName)
    {
        if (packageName == null || packageName.trim().length() == 0)
        {
            throw new NullPointerException("The package name passed to addFormPackageName is either null or of zero length");
        }
        _formPackageNames.add(packageName);
    }
    
    /**
     * Returns the container that holds all item renderers that are to be used
     * by the application
     * 
     * @return The renderer container containing the applications item renderer
     */
    public EJPluginAssignedRendererContainer getItemRendererContainer()
    {
        return _itemRendererContainer;
    }
    
    /**
     * Returns the item renderers that have been defined for this application
     * 
     * @return A list of renderers
     */
    public Collection<EJRendererAssignment> getApplicationAssignedItemRenderers()
    {
        ArrayList<EJRendererAssignment> list = new ArrayList<EJRendererAssignment>();
        
        Iterator<EJPluginRenderer> iti = _itemRendererContainer.getAllRenderers().iterator();
        while (iti.hasNext())
        {
            list.add(iti.next());
        }
        
        return list;
    }
    
    public EJRendererAssignment getApplicationAssignedItemRenderer(String name)
    {
        return _itemRendererContainer.getRenderer(name);
    }
    
    /**
     * Returns the container that holds all block renderer that are to be used
     * by the application
     * 
     * @return The renderer container containing the applications block renderer
     *         definitions
     */
    public EJPluginAssignedRendererContainer getBlockRendererContainer()
    {
        return _blockRendererContainer;
    }
    
    /**
     * Returns the block renderers that have been defined for this application
     * 
     * @return A list of renderers
     */
    public Collection<EJRendererAssignment> getApplicationAssignedBlockRenderers()
    {
        ArrayList<EJRendererAssignment> list = new ArrayList<EJRendererAssignment>();
        
        Iterator<EJPluginRenderer> iti = _blockRendererContainer.getAllRenderers().iterator();
        while (iti.hasNext())
        {
            list.add(iti.next());
        }
        
        return list;
    }
    
    public EJRendererAssignment getApplicationAssignedBlockRenderer(String name)
    {
        return _blockRendererContainer.getRenderer(name);
    }
    
    /**
     * Returns the container that holds all form renderer that are to be used by
     * the application
     * 
     * @return The renderer container containing the applications form renderer
     *         definitions
     */
    public EJPluginAssignedRendererContainer getFormRendererContainer()
    {
        return _formRendererContainer;
    }
    
    /**
     * Returns the form renderers that have been defined for this application
     * 
     * @return A list of renderers
     */
    public Collection<EJRendererAssignment> getApplicationAssignedFormRenderers()
    {
        ArrayList<EJRendererAssignment> list = new ArrayList<EJRendererAssignment>();
        
        Iterator<EJPluginRenderer> iti = _formRendererContainer.getAllRenderers().iterator();
        while (iti.hasNext())
        {
            list.add(iti.next());
        }
        
        return list;
    }
    
    public EJRendererAssignment getApplicationAssignedFormRenderer(String name)
    {
        return _formRendererContainer.getRenderer(name);
    }
    
    /**
     * Returns the container that holds all lov renderers that are to be used by
     * the application
     * 
     * @return The renderer container containing the applications lov renderer
     *         definitions
     */
    public EJPluginAssignedRendererContainer getLovRendererContainer()
    {
        return _lovRendererContainer;
    }
    
    /**
     * Returns the lov renderers that have been defined for this application
     * 
     * @return A list of renderers
     */
    public Collection<EJRendererAssignment> getApplicationAssignedLovRenderers()
    {
        ArrayList<EJRendererAssignment> list = new ArrayList<EJRendererAssignment>();
        
        Iterator<EJPluginRenderer> iti = _lovRendererContainer.getAllRenderers().iterator();
        while (iti.hasNext())
        {
            list.add(iti.next());
        }
        
        return list;
    }
    
    public EJRendererAssignment getApplicationAssignedLovRenderer(String name)
    {
        return _lovRendererContainer.getRenderer(name);
    }
    
    /**
     * Returns the container that holds all menu renderers that are to be used
     * by the application
     * 
     * @return The renderer container containing the applications menu renderer
     *         definitions
     */
    public EJPluginAssignedRendererContainer getMenuRendererContainer()
    {
        return _menuRenderers;
    }
    
    /**
     * Returns the container that holds all application component renderers that
     * are to be used by the application
     * 
     * @return The renderer container containing the applications menu renderer
     *         definitions
     */
    public EJPluginAssignedRendererContainer getAppComponentRendererContainer()
    {
        return _appComponentRendererContainer;
    }
    
    /**
     * Returns the menu renderers that have been defined for this application
     * 
     * @return A list of renderers
     */
    public Collection<EJRendererAssignment> getApplicationAssignedMenuRenderers()
    {
        ArrayList<EJRendererAssignment> list = new ArrayList<EJRendererAssignment>();
        
        Iterator<EJPluginRenderer> iti = _menuRenderers.getAllRenderers().iterator();
        while (iti.hasNext())
        {
            list.add(iti.next());
        }
        
        return list;
    }
    
    public EJRendererAssignment getApplicationAssignedMenuRenderer(String name)
    {
        return _menuRenderers.getRenderer(name);
    }
    
    public Collection<EJRendererAssignment> getApplicationAssignedComponentRenderers()
    {
        List<EJRendererAssignment> list = new ArrayList<EJRendererAssignment>();
        for (EJPluginRenderer ejRendererAssignment : _appComponentRendererContainer.getAllRenderers())
        {
            list.add(ejRendererAssignment);
        }
        return list;
    }
    
    public EJRendererAssignment getApplicationAssignedComponentRenderer(String name)
    {
        return _appComponentRendererContainer.getRenderer(name);
    }
    
    /**
     * Checks this properties object and indicates if it is valid
     * <p>
     * An Invalid property file could be for example that it has renderers
     * defined that do not exist. If this is the case, then any form based upon
     * these properties will not run correctly and should therefore not be
     * loaded
     * 
     * @return
     */
    public boolean isValid()
    {
        if (!checkFormRenderers())
        {
            return false;
        }
        else if (!checkBlockRenderers())
        {
            return false;
        }
        else if (!checkItemRenderers())
        {
            return false;
        }
        else if (!checkLovRenderers())
        {
            return false;
        }
        else if (!checkMenuRenderers())
        {
            return false;
        }
        
        return true;
    }
    
    private boolean checkFormRenderers()
    {
        Iterator<EJPluginRenderer> factories = _formRendererContainer.getAllRenderers().iterator();
        while (factories.hasNext())
        {
            EJPluginRenderer def = factories.next();
            if (def.getRendererClassName() == null)
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkBlockRenderers()
    {
        Iterator<EJPluginRenderer> factories = _blockRendererContainer.getAllRenderers().iterator();
        while (factories.hasNext())
        {
            EJPluginRenderer def = factories.next();
            if (def.getRendererClassName() == null)
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkItemRenderers()
    {
        Iterator<EJPluginRenderer> factories = _itemRendererContainer.getAllRenderers().iterator();
        while (factories.hasNext())
        {
            EJPluginRenderer def = factories.next();
            if (def.getRendererClassName() == null)
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkLovRenderers()
    {
        Iterator<EJPluginRenderer> factories = _lovRendererContainer.getAllRenderers().iterator();
        while (factories.hasNext())
        {
            EJPluginRenderer def = factories.next();
            if (def.getRendererClassName() == null)
            {
                return false;
            }
        }
        return true;
    }
    
    private boolean checkMenuRenderers()
    {
        Iterator<EJPluginRenderer> factories = _menuRenderers.getAllRenderers().iterator();
        while (factories.hasNext())
        {
            EJPluginRenderer def = factories.next();
            if (def.getRendererClassName() == null)
            {
                return false;
            }
        }
        return true;
    }
    
    public List<String> getReusableBlockNames()
    {
        if (_reusableBlocksLocation != null && _reusableBlocksLocation.trim().length() > 0)
        {
            try
            {
                return loadFileNames(_reusableBlocksLocation, EJPluginConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
            }
            catch (EJDevFrameworkException e)
            {
                EJCoreLog.logWarnning(e);
            }
        }
        
        return Collections.emptyList();
        
    }
    
    public List<String> getReusableLovDefinitionNames()
    {
        if (_reusableLovDefsLocation != null && _reusableLovDefsLocation.trim().length() > 0)
        {
            try
            {
                return loadFileNames(_reusableLovDefsLocation, EJPluginConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX);
            }
            catch (EJDevFrameworkException e)
            {
                EJCoreLog.logWarnning(e);
            }
        }
        
        return Collections.emptyList();
    }
    
    public List<String> getObjectGroupDefinitionNames()
    {
        if (_objectGroupDefsLocation != null && _objectGroupDefsLocation.trim().length() > 0)
        {
            try
            {
                return loadFileNames(_objectGroupDefsLocation, EJPluginConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX);
            }
            catch (EJDevFrameworkException e)
            {
                EJCoreLog.logWarnning(e);
            }
        }
        
        return Collections.emptyList();
    }
    
    @Override
    public EJCoreMenuProperties getMenuProperties(String name)
    {
        return null;
    }
    
    public EJCoreMenuContainer getMenuContainer()
    {
        return null;
    }
    
    public EJPluginReusableBlockProperties getReusableBlockProperties(String blockName) throws EJDevFrameworkException
    {
        if (blockName == null || blockName.trim().length() == 0)
        {
            return null;
        }
        
        IFile file = loadFile(blockName, _reusableBlocksLocation, EJPluginConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
        if (file == null)
        {
            return null;
        }
        return loadBlock(this, _javaProject.getProject(), file, blockName);
    }
    
    public IFile getReusableBlockFile(String blockName) throws EJDevFrameworkException
    {
        if (blockName == null || blockName.trim().length() == 0)
        {
            return null;
        }
        
        IFile file = loadFile(blockName, _reusableBlocksLocation, EJPluginConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
        
        return file;
    }
    
    public IFile getReusableLovFile(String definitionName) throws EJDevFrameworkException
    {
        if (definitionName == null || definitionName.trim().length() == 0)
        {
            return null;
        }
        
        IFile file = loadFile(definitionName, _reusableLovDefsLocation, EJPluginConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX);
        
        return file;
    }
    
    public IFile getObjectGroupFile(String definitionName) throws EJDevFrameworkException
    {
        if (definitionName == null || definitionName.trim().length() == 0)
        {
            return null;
        }
        
        IFile file = loadFile(definitionName, _objectGroupDefsLocation, EJPluginConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX);
        
        return file;
    }
    
    public boolean containsReusableBlockProperties(String blockName)
    {
        if (_reusableBlocksLocation != null && _reusableBlocksLocation.trim().length() > 0)
        {
            try
            {
                return hasFileName(_reusableBlocksLocation, blockName, EJPluginConstants.REFERENCED_BLOCK_PROPERTIES_FILE_SUFFIX);
            }
            catch (EJDevFrameworkException e)
            {
                EJCoreLog.logWarnning(e);
            }
        }
        
        return false;
    }
    
    public EJPluginLovDefinitionProperties getReusableLovDefinitionProperties(String definitionName) throws EJDevFrameworkException
    {
        if (definitionName == null || definitionName.trim().length() == 0)
        {
            return null;
        }
        
        IFile file = loadFile(definitionName, _reusableLovDefsLocation, EJPluginConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX);
        if (file == null)
        {
            return null;
        }
        return loadLovDefinition(this, _javaProject.getProject(), file, definitionName);
    }
    
    public EJPluginObjectGroupProperties getObjectGroupDefinitionProperties(String definitionName) throws EJDevFrameworkException
    {
        if (definitionName == null || definitionName.trim().length() == 0)
        {
            return null;
        }
        
        IFile file = loadFile(definitionName, _objectGroupDefsLocation, EJPluginConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX);
        if (file == null)
        {
            return null;
        }
        return loadObjectGroupDefinition(this, _javaProject.getProject(), file, definitionName);
    }
    
    public boolean containsReusableLovDefinitionProperties(String definitionName)
    {
        if (_reusableLovDefsLocation != null && _reusableLovDefsLocation.trim().length() > 0)
        {
            try
            {
                return hasFileName(_reusableLovDefsLocation, definitionName, EJPluginConstants.REFERENCED_LOVDEF_PROPERTIES_FILE_SUFFIX);
            }
            catch (EJDevFrameworkException e)
            {
                EJCoreLog.logWarnning(e);
            }
        }
        
        return false;
    }
    
    public boolean containsObjectGroupDefinitionProperties(String definitionName)
    {
        if (_objectGroupDefsLocation != null && _objectGroupDefsLocation.trim().length() > 0)
        {
            try
            {
                return hasFileName(_objectGroupDefsLocation, definitionName, EJPluginConstants.OBJECT_GROUP_PROPERTIES_FILE_SUFFIX);
            }
            catch (EJDevFrameworkException e)
            {
                EJCoreLog.logWarnning(e);
            }
        }
        
        return false;
    }
    
    private List<String> loadFileNames(String path, String fileSuffix) throws EJDevFrameworkException
    {
        ArrayList<String> fileNames = new ArrayList<String>();
        try
        {
            IJavaElement element = _javaProject.findElement(new Path(path));
            
            if (element == null)
            {
                return null;
            }
            
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            {
                IPackageFragment packageFragment = (IPackageFragment) element;
                
                Object[] obs = packageFragment.getNonJavaResources();
                for (Object object : obs)
                {
                    if (object instanceof IFile)
                    {
                        IFile file = (IFile) object;
                        if (file.getFileExtension().equals(fileSuffix))
                        {
                            String fileName = file.getName();
                            fileName = fileName.substring(0, fileName.indexOf("." + fileSuffix));
                            fileNames.add(fileName);
                        }
                    }
                }
                
            }
            return fileNames;
        }
        catch (JavaModelException e)
        {
            throw new EJDevFrameworkException(e.getMessage());
        }
    }
    
    private boolean hasFileName(String path, String name, String fileSuffix) throws EJDevFrameworkException
    {
        
        try
        {
            IJavaElement element = _javaProject.findElement(new Path(path));
            
            if (element == null)
            {
                return false;
            }
            
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            {
                IPackageFragment packageFragment = (IPackageFragment) element;
                
                Object[] obs = packageFragment.getNonJavaResources();
                for (Object object : obs)
                {
                    if (object instanceof IFile)
                    {
                        IFile file = (IFile) object;
                        if (file.getFileExtension().equals(fileSuffix))
                        {
                            String fileName = file.getName();
                            fileName = fileName.substring(0, fileName.indexOf("." + fileSuffix));
                            if (fileName.equals(name)) return true;
                        }
                    }
                }
                
            }
            
        }
        catch (JavaModelException e)
        {
            throw new EJDevFrameworkException(e.getMessage());
        }
        return false;
    }
    
    private IFile loadFile(String name, String location, String fileSuffix) throws EJDevFrameworkException
    {
        if (location == null || location.trim().length() == 0)
        {
            return null;
        }
        
        try
        {
            IJavaElement element = getJavaProject().findElement(new Path(location));
            
            if (element == null)
            {
                return null;
            }
            
            if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            {
                IPackageFragment packageFragment = (IPackageFragment) element;
                
                Object[] obs = packageFragment.getNonJavaResources();
                for (Object object : obs)
                {
                    if (object instanceof IFile)
                    {
                        IFile file = (IFile) object;
                        if (file.getFileExtension().equals(fileSuffix))
                        {
                            String fileName = file.getName();
                            fileName = fileName.substring(0, fileName.indexOf("." + fileSuffix));
                            if (fileName.equals(name))
                            {
                                return file;
                            }
                        }
                    }
                }
                
            }
            
            return null;
        }
        catch (JavaModelException e)
        {
            throw new EJDevFrameworkException(e.getMessage());
        }
    }
    
    /**
     * Indicates if there is a reusable block available with the given name
     * 
     * @param name
     *            The name to check for
     * @return <code>true</code> if a block exists with the given name,
     *         otherwise <code>false</code>
     */
    public boolean reusableBlockExists(String name)
    {
        return containsReusableBlockProperties(name);
    }
    
    /**
     * Indicates if there is a reusable lov definition available with the given
     * name
     * 
     * @param name
     *            The name to check for
     * @return <code>true</code> if an lov definition exists with the given
     *         name, otherwise <code>false</code>
     */
    public boolean reusableLovDefinitionExists(String name)
    {
        return containsReusableLovDefinitionProperties(name);
    }
    
    public IJavaProject getJavaProject()
    {
        return _javaProject;
    }
    
    public Collection<EJPluginApplicationParameter> getAllApplicationLevelParameters()
    {
        return _applicationLevelParameters;
    }
    
    public void addApplicationLevelParameter(EJPluginApplicationParameter parameter)
    {
        if (parameter != null)
        {
            _applicationLevelParameters.add(parameter);
        }
    }
    
    public EJPluginApplicationParameter getApplicationLevelParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _applicationLevelParameters)
        {
            if (parameter.getName().equals(name))
            {
                return parameter;
            }
        }
        return null;
    }
    
    public void removeApplicationLevelParameter(EJPluginApplicationParameter parameter)
    {
        _applicationLevelParameters.remove(parameter);
    }
    
    public boolean containsApplicationLevelParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _applicationLevelParameters)
        {
            if (parameter.getName().equals(name))
            {
                return true;
            }
        }
        return false;
    }
    
}
