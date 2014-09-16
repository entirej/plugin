/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 * Contributors:
 *     Mojave Innovations GmbH - initial API and implementation
 ******************************************************************************/
package org.entirej.framework.reports.interfaces;

import java.io.Serializable;
import java.util.Collection;

import org.entirej.framework.core.service.EJBlockService;

public interface EJReportBlockProperties extends Serializable
{
    /**
     * Returns the main screen properties for this block
     * 
     * @return the main screen properties for this block
     */
    public EJReportScreenProperties getLayoutScreenProperties();
    

    
    /**
     * Returns the properties of each item defined within this block
     * 
     * @return The properties of each item defined within this block
     */
    public Collection<EJReportItemProperties> getAllItemProperties();
    
    /**
     * Returns the item properties for the given item
     * 
     * @param itemName
     *            The item name
     * @return The properties of the given item
     */
    public EJReportItemProperties getItemProperties(String itemName);
    
    
    /**
     * Returns the name of the canvas upon which this blocks data should be
     * displayed or <code>null</code> if no canvas has been defined for the
     * block
     * <p>
     * If the canvas that was chosen is a type of TAB then the blocks data
     * should be displayed on the correct tab page
     * <p>
     * If no canvas has been defined for this block, then the block will not be
     * displayed anywhere
     * <p>
     * 
     * @return the name of the canvas upon which the blocks data will be
     *         displayed or <code>null</code> if no canvas has been defined
     */
    public String getCanvasName();
    
    /**
     * Indicates if this is a control block
     * <p>
     * Control blocks can be used as normal blocks but they have no interaction
     * with the data source. Therefore no queries, inserts updates or deletes
     * are made.
     * 
     * @return <code>true</code> if this is a control block otherwise
     *         <code>false</code>
     */
    public boolean isControlBlock();
    
    /**
     * Indicates if this block is a referenced block
     * 
     * @return <code>true</code> if this is a referenced block otherwise
     *         <code>false</code>
     */
    public boolean isReferenceBlock();
    
    /**
     * This method is used within the EntireJ Plugin
     * 
     * @return <code>null</code>
     */
    public String getDescription();
    

    
    /**
     * Returns the properties of the Form to which this block belongs
     * 
     * @return The form properties
     */
    public EJReportProperties getReportProperties();
    

    

    
    /**
     * Returns the internal name of this block
     * 
     * @return The name of this block
     */
    public String getName();
    

    
    /**
     * Gets the fully qualified class name of the service that is responsible
     * for the retrieval and modification of this blocks data
     * 
     * @return The fully qualified class name of the service that is responsible
     *         for the retrieval and modification of this blocks data
     */
    public String getServiceClassName();
    
    /**
     * Returns the service used to retrieve and manipulate the data of this
     * block
     * 
     * @return This blocks service
     */
    public EJBlockService<?> getBlockService();
    
    /**
     * The Action Processor is responsible for actions within this block
     * <p>
     * Actions can include buttons being pressed, check boxes being selected or
     * pre-post query methods etc.
     * 
     * @return The name of the Action Processor responsible for this form.
     */
    public String getActionProcessorClassName();

    

    
 


    
}
