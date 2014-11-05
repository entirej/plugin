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
/*
 * Created on Nov 5, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.reports;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.plugin.framework.properties.EJPluginApplicationParameter;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer;
import org.entirej.framework.plugin.reports.containers.EJReportBlockContainer.BlockGroup;
import org.entirej.framework.plugin.reports.containers.EJReportBlockItemContainer;
import org.entirej.framework.report.enumerations.EJReportExportType;
import org.entirej.framework.report.interfaces.EJReportProperties;

public class EJPluginReportProperties implements EJReportProperties, Comparable<EJPluginReportProperties>
{
    
    private boolean                            _isReusableBlock          = false;
    private boolean                            _isObjectGroup            = false;
    private IJavaProject                       _reportProject;
    
    private String                             _name                     = "";
    private String                             _reportTitle              = "";
    private String                             _reportDisplayName        = "";
    
    private String                             _actionProcessorClassName = "";
    
    private List<EJPluginApplicationParameter> _reportParameters;
    private HashMap<String, String>            _applicationProperties;
    
    private boolean                            _ignorePagination;
    
    // Display Properties
    private int                                _reportWidth;
    private int                                _reportHeight;
    private int                                _marginTop;
    private int                                _marginBottom;
    private int                                _marginLeft;
    private int                                _marginRight;
    private int                                _headerSectionHeight      = 30;                    // plugin
                                                                                                   // default
    private int                                _footerSectionHeight      = 20;                    // plugin
                                                                                                   // default
    private EJReportProperties.ORIENTATION     _orientation              = ORIENTATION.PORTRAIT;
    
    private EJReportExportType                 exportType                = EJReportExportType.PDF;
    
    private EJReportBlockContainer             blockContainer;
    
    public EJPluginReportProperties(String reportName, IJavaProject javaProject)
    {
        _name = reportName;
        _reportProject = javaProject;
        _applicationProperties = new HashMap<String, String>();
        _reportParameters = new ArrayList<EJPluginApplicationParameter>();
        blockContainer = new EJReportBlockContainer(this);
        
    }
    
    public EJReportBlockContainer getBlockContainer()
    {
        return blockContainer;
    }
    
    public IJavaProject getJavaProject()
    {
        return _reportProject;
    }
    
    /**
     * Returns the <b>EntireJ</b> Properties for this report
     * 
     * @return The name of the <b>EntireJProperties</b> file
     */
    public EJPluginEntireJReportProperties getEntireJProperties()
    {
        return EJPluginEntireJReportPropertiesLoader.getEntireJProperties(getJavaProject());
    }
    
    /**
     * Used to retrieve the name of the report for which these properties are
     * valid
     * 
     * @return The name of the report
     */
    public String getName()
    {
        return _name;
    }
    
    /**
     * This is an EntireJ internal method and should not be used. It will change
     * all references within the Properties from the previous name to the name
     * given. This is needed when nesting reports within each other.
     * 
     * @param newName
     *            The new name or internal name for this report when used as a
     *            nested report
     */
    public void changeName(String newName)
    {
        _name = newName;
    }
    
    @Override
    public boolean isIgnorePagination()
    {
        return _ignorePagination;
    }
    
    public void setIgnorePagination(boolean ignorePagination)
    {
        this._ignorePagination = ignorePagination;
    }
    
    /**
     * the title of the report. This will be the translated title code if a
     * title code has been set otherwise it will return the title code.
     * 
     * @return The title of the report
     */
    public String getTitle()
    {
        return _reportTitle;
    }
    
    public String getReportDisplayName()
    {
        return _reportDisplayName;
    }
    
    public void setReportDisplayName(String _reportDisplayName)
    {
        this._reportDisplayName = _reportDisplayName;
    }
    
    /**
     * Sets the title of this report.
     * 
     * @param title
     *            The report title
     */
    public void setReportTitle(String title)
    {
        _reportTitle = title;
    }
    
    @Override
    public ORIENTATION getOrientation()
    {
        return _orientation;
    }
    
    public void setOrientation(EJReportProperties.ORIENTATION orientation)
    {
        this._orientation = orientation;
    }
    
    /**
     * Returns the required width of the report
     * <p>
     * The value is the width in pixels
     * 
     * @return The required width of the report
     */
    public int getReportWidth()
    {
        return _reportWidth;
    }
    
    /**
     * Sets the required width of the report
     * <p>
     * The value is the width in pixels
     * 
     * @param reportWidth
     *            The required width of the report
     */
    public void setReportWidth(int reportWidth)
    {
        _reportWidth = reportWidth;
    }
    
    /**
     * Returns the required height of the report
     * <p>
     * The value is the height in pixels
     * 
     * @return The required height of the report
     */
    public int getReportHeight()
    {
        return _reportHeight;
    }
    
    /**
     * Sets the required height of the report
     * <p>
     * The value is the height in pixels
     * 
     * @param reportHeight
     *            The required height of the report
     */
    public void setReportHeight(int reportHeight)
    {
        _reportHeight = reportHeight;
    }
    
    public int getMarginTop()
    {
        return _marginTop;
    }
    
    public void setMarginTop(int _marginTop)
    {
        this._marginTop = _marginTop;
    }
    
    public int getMarginBottom()
    {
        return _marginBottom;
    }
    
    public void setMarginBottom(int _marginBottom)
    {
        this._marginBottom = _marginBottom;
    }
    
    public int getMarginLeft()
    {
        return _marginLeft;
    }
    
    public void setMarginLeft(int _marginLeft)
    {
        this._marginLeft = _marginLeft;
    }
    
    public int getMarginRight()
    {
        return _marginRight;
    }
    
    public void setMarginRight(int _marginRight)
    {
        this._marginRight = _marginRight;
    }
    
    /**
     * The Action Processor is responsible for actions within the report.
     * Actions can include buttons being pressed, check boxes being selected or
     * pre-post query methods etc.
     * 
     * @return The name of the Action Processor responsible for this report.
     */
    public String getActionProcessorClassName()
    {
        return _actionProcessorClassName;
    }
    
    /**
     * Sets the action processor name for this report
     * 
     * @param processorClassName
     *            The action processor name for this report
     */
    public void setActionProcessorClassName(String processorClassName)
    {
        _actionProcessorClassName = processorClassName;
    }
    
    public int compareTo(EJPluginReportProperties arg0)
    {
        return this.getName().compareTo(((EJPluginReportProperties) arg0).getName());
    }
    
    public boolean isReusableBlockReport()
    {
        return _isReusableBlock;
    }
    
    public void setIsReusableBlockReport(boolean isReusableBlockreport)
    {
        _isReusableBlock = isReusableBlockreport;
    }
    
    public boolean isObjectGroupReport()
    {
        return _isObjectGroup;
    }
    
    public void setIsObjectGroupReport(boolean isObjectGroupreport)
    {
        _isObjectGroup = isObjectGroupreport;
    }
    
    public Collection<String> getAllApplicationPropertyNames()
    {
        return _applicationProperties.keySet();
    }
    
    public void addApplicationProperty(String name, String value)
    {
        _applicationProperties.put(name, value);
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
    }
    
    public boolean containsApplicationProperty(String name)
    {
        return _applicationProperties.containsKey(name);
    }
    
    public Collection<EJPluginApplicationParameter> getAllReportParameters()
    {
        return _reportParameters;
    }
    
    public void addReportParameter(EJPluginApplicationParameter parameter)
    {
        if (parameter != null)
        {
            _reportParameters.add(parameter);
        }
    }
    
    public EJPluginApplicationParameter getReportParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _reportParameters)
        {
            if (parameter.getName().equalsIgnoreCase(name))
            {
                return parameter;
            }
        }
        return null;
    }
    
    public void removeReportParameter(EJPluginApplicationParameter parameter)
    {
        _reportParameters.remove(parameter);
    }
    
    public boolean containsReportParameter(String name)
    {
        for (EJPluginApplicationParameter parameter : _reportParameters)
        {
            if (parameter.getName().equalsIgnoreCase(name))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean setApplicationProperty(String name, String value)
    {
        if (containsApplicationProperty(name))
        {
            addApplicationProperty(name, value);
            return true;
        }
        else
        {
            return false;
        }
    }
    
    public String getReportName()
    {
        return _name;
    }
    
    public void initialisationCompleted()
    {
        
    }
    
    public List<String> getBlockNames()
    {
        List<String> blockNames = new ArrayList<String>();
        
        for (EJPluginReportBlockProperties properties : blockContainer.getAllBlockProperties())
        {
            
            blockNames.add(properties.getName());
        }
        
        return blockNames;
    }
    
    public List<String> getBlockNamesWithParents(EJPluginReportBlockProperties sub)
    {
        List<String> blockNames = new ArrayList<String>();
        
        for (EJPluginReportBlockProperties properties : blockContainer.getAllBlockProperties())
        {
            findParents(sub.getName(), properties.getLayoutScreenProperties().getSubBlocks(), blockNames);
        }
        for (EJPluginReportBlockProperties properties : blockContainer.getAllBlockProperties())
        {
            
            blockNames.add(properties.getName());
        }
        
        if (!blockNames.contains(sub.getName()))
        {
            blockNames.add(sub.getName());
        }
        
        return blockNames;
    }
    
    private boolean findParents(String child, BlockGroup bGroup, List<String> blockNames)
    {
        List<EJPluginReportBlockProperties> allBlockProperties = bGroup.getAllBlockProperties();
        for (EJPluginReportBlockProperties properties : allBlockProperties)
        {
            if (child.equals(properties.getName()))
            {
                return true;
            }
            if (findParents(child, properties.getLayoutScreenProperties().getSubBlocks(), blockNames))
            {
                blockNames.add(properties.getName());
                return true;
            }
        }
        
        return false;
    }
    
    public EJReportBlockItemContainer getBlockProperties(String blockName)
    {
        EJPluginReportBlockProperties blockProperties = blockContainer.getBlockProperties(blockName);
        return blockProperties != null ? blockProperties.getItemContainer() : null;
    }
    
    @Override
    public EJReportExportType getExportType()
    {
        return exportType;
    }
    
    public void setExportType(EJReportExportType exportType)
    {
        this.exportType = exportType;
    }
    
    @Override
    public int getHeaderSectionHeight()
    {
        return _headerSectionHeight;
    }
    
    public void setHeaderSectionHeight(int headerSectionHeight)
    {
        this._headerSectionHeight = headerSectionHeight;
    }
    
    @Override
    public int getFooterSectionHeight()
    {
        return _footerSectionHeight;
    }
    
    public void setFooterSectionHeight(int footerSectionHeight)
    {
        this._footerSectionHeight = footerSectionHeight;
    }
}
