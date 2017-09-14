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
 * Created on Nov 30, 2005
 * 
 * TODO To change the template for this generated file go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
package org.entirej.framework.plugin.framework.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.entirej.framework.plugin.framework.properties.interfaces.EJPluginFormPreviewProvider;


public class EJPluginRelationProperties implements Comparable<EJPluginRelationProperties>, EJPluginFormPreviewProvider
{
    private EJPluginFormProperties               _formProperties;
    private String                               _name;
    private String                               _masterBlock;
    private String                               _detailBlock;
    private boolean                              _deferredQuery               = false;
    private boolean                              _autoQuery                   = true;
    private boolean                              _preventMasterlessOperations = true;

    private String                                 _referencedObjectGroupName    = "";
    
    
    
    private List<EJPluginRelationJoinProperties> _joinList;
    
    public EJPluginRelationProperties(EJPluginFormProperties formProperties, String name)
    {
        _formProperties = formProperties;
        _name = name;
        _joinList = new ArrayList<EJPluginRelationJoinProperties>();
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    /**
     * @return the name of this relation
     */
    public String getName()
    {
        return _name;
    }
    
    public void setName(String name)
    {
        _name = name;
    }
    
    public void deleteAllJoins()
    {
        _joinList.clear();
    }
    
    public void setDeferredQuery(boolean deferred)
    {
        _deferredQuery = deferred;
    }
    
    public boolean isDeferredQuery()
    {
        return _deferredQuery;
    }
    
    public boolean preventMasterlessOperations()
    {
        return _preventMasterlessOperations;
    }
    
    public void setPreventMasterlessOperations(boolean prevent)
    {
        _preventMasterlessOperations = prevent;
    }
    
    public boolean isAutoQuery()
    {
        return _autoQuery;
    }
    
    public void setAutoQuery(boolean query)
    {
        _autoQuery = query;
    }
    
    /**
     * Returns the master block name for this relation
     * 
     * @return The master block name
     */
    public String getMasterBlockName()
    {
        return _masterBlock;
    }
    
    /**
     * Sets the master block name for this relation
     * 
     * @param blockName
     *            The name of the master block
     */
    public void setMasterBlockName(String blockName)
    {
        _masterBlock = blockName;
    }
    
    /**
     * Sets the detail block name for this relation
     * 
     * @param blockName
     *            The name of the detail block
     */
    public void setDetailBlockName(String blockName)
    {
        _detailBlock = blockName;
    }
    
    /**
     * Returns the name of the detail block for this relation
     * 
     * @return The name of the detail block
     */
    public String getDetailBlockName()
    {
        return _detailBlock;
    }
    
    /**
     * Adds join items to this block relation
     * <p>
     * A join is made by linking an item in the detail block to an item within
     * the master block. This method is used to create this join by passing in
     * an item name from the master block and and item name from the detail
     * block. If the item names are valid then a join will be created. If the
     * link from master to detail blocks is made over more than one item, then a
     * join will be needed for each item.
     * 
     * @param masterItemName
     *            The name of the master item in the join
     * @param detailItemName
     *            The name of the detail item in the join
     */
    public void addJoin(String masterItemName, String detailItemName)
    {
        if (masterItemName == null || masterItemName.trim().length() == 0)
        {
            throw new NullPointerException("The masterItemName passed to addJoin is either null or of zero length.");
        }
        if (detailItemName == null || detailItemName.trim().length() == 0)
        {
            throw new NullPointerException("The detailItemName passed to addJoin is either null or of zero length.");
        }
        
        EJPluginRelationJoinProperties join = new EJPluginRelationJoinProperties(masterItemName, detailItemName);
        _joinList.add(join);
    }
    
    /**
     * Returns a collection of <code>RelationJoinProperties</code> used within
     * this relation
     * 
     * @return The collection containing the <code>RelationJoinProperties</code>
     */
    public Collection<EJPluginRelationJoinProperties> getRelationJoins()
    {
        return _joinList;
    }
    
    /*
     * (non-Javadoc)
     * 
     * @see java.lang.Comparable#compareTo(java.lang.Object)
     */
    public int compareTo(EJPluginRelationProperties arg0)
    {
        return this.getName().compareTo(arg0.getName());
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
    
}
