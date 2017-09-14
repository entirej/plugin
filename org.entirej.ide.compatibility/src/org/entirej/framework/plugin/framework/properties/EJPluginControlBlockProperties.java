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
package org.entirej.framework.plugin.framework.properties;

/**
 * This is just an empty wrapper around the <code>BlockProperties</code> which
 * allows proper functionality of the form list tree.
 * <p>
 * The form list tree maps a specified class to a specified block control.
 * Because I need a slight variation of controls for the control block and the
 * normal block, I need the forms list to believe that they are actually
 * different types, instead of just a flag within the
 * <code>BlockProperties</code>
 * 
 * 
 * 
 */
public class EJPluginControlBlockProperties extends EJPluginBlockProperties
{
    /**
     * 
     */
    private static final long serialVersionUID = -6872610369696203589L;

    public EJPluginControlBlockProperties(EJPluginFormProperties formProperties, String blockName)
    {
        super(formProperties, blockName, true);
    }
    
}
