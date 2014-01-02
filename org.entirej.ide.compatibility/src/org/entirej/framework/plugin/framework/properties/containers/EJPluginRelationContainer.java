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
package org.entirej.framework.plugin.framework.properties.containers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.entirej.framework.plugin.framework.properties.EJPluginFormProperties;
import org.entirej.framework.plugin.framework.properties.EJPluginRelationProperties;

public class EJPluginRelationContainer
{
    private List<EJPluginRelationProperties> _relationProperties;
    private EJPluginFormProperties           _formProperties;
    
    public EJPluginRelationContainer(EJPluginFormProperties formProperties)
    {
        _formProperties = formProperties;
        _relationProperties = new ArrayList<EJPluginRelationProperties>();
    }
    
    public void dispose()
    {
        _relationProperties.clear();
        _formProperties = null;
    }
    
    public EJPluginFormProperties getFormProperties()
    {
        return _formProperties;
    }
    
    public EJPluginRelationProperties getLastAddedRelation()
    {
        return _relationProperties.size() > 0 ? _relationProperties.get(_relationProperties.size() - 1) : null;
    }
    
    public void addRelationProperties(EJPluginRelationProperties relationProperties)
    {
        if (relationProperties != null)
        {
            _relationProperties.add(relationProperties);
        }
    }
    
    public void addRelationProperties(int index, EJPluginRelationProperties relationProperties)
    {
        if (relationProperties != null)
        {
            _relationProperties.add(index, relationProperties);
        }
    }
    
    /**
     * Returns the relation where the block name given is the detail block. If
     * there exists no relation having the given block as the detail block, then
     * null will be returned.
     * 
     * @param detailBlockName
     *            The name of the detail block
     * @return The relation where the given block is the detail block or null if
     *         there is no relation
     */
    public EJPluginRelationProperties getRelationForDetailBlock(String detailBlockName)
    {
        Iterator<EJPluginRelationProperties> relations = _relationProperties.iterator();
        
        while (relations.hasNext())
        {
            EJPluginRelationProperties relation = relations.next();
            
            if (relation.getDetailBlockName().equalsIgnoreCase(detailBlockName))
            {
                return relation;
            }
        }
        return null;
    }
    
    /**
     * Returns all detail relations where the block name given is the master
     * block. If no relation exists having the given block name as the master
     * block, then null will be returned.
     * 
     * @param masterBlockName
     *            The name of the master block
     * @return A collection of relations where the given block is the master
     *         block or an empty list if there are no relations having the given
     *         block as master.
     */
    public Collection<EJPluginRelationProperties> getRelationsForMasterBlock(String masterBlockName)
    {
        ArrayList<EJPluginRelationProperties> relationList = new ArrayList<EJPluginRelationProperties>();
        
        Iterator<EJPluginRelationProperties> relations = _relationProperties.iterator();
        
        while (relations.hasNext())
        {
            EJPluginRelationProperties relation = relations.next();
            
            if (relation.getMasterBlockName().equalsIgnoreCase(masterBlockName))
            {
                relationList.add(relation);
            }
        }
        return relationList;
    }
    
    public void removeRelationProperties(String relationName)
    {
        Iterator<EJPluginRelationProperties> iti = _relationProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginRelationProperties relation = iti.next();
            
            if (relation.getName().equalsIgnoreCase(relationName))
            {
                _relationProperties.remove(relation);
                
                break;
            }
        }
    }
    
    public void removeRelationProperties(EJPluginRelationProperties relation)
    {
        _relationProperties.remove(relation);
    }
    
    public EJPluginRelationProperties getRelationProperties(String relationName)
    {
        
        Iterator<EJPluginRelationProperties> iti = _relationProperties.iterator();
        while (iti.hasNext())
        {
            EJPluginRelationProperties relation = iti.next();
            
            if (relation.getName().equalsIgnoreCase(relationName))
            {
                return relation;
            }
        }
        return null;
    }
    
    public List<EJPluginRelationProperties> getAllRelationProperties()
    {
        return _relationProperties;
    }
    
    public boolean contains(String relationName)
    {
        Iterator<EJPluginRelationProperties> relations = _relationProperties.iterator();
        while (relations.hasNext())
        {
            EJPluginRelationProperties relation = relations.next();
            
            if (relation.getName().equalsIgnoreCase(relationName))
            {
                return true;
            }
        }
        return false;
    }
    
    public boolean isEmpty()
    {
        return _relationProperties.isEmpty();
    }
    
}
