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
package org.entirej.framework.plugin.utils;

import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class EJPluginEntireJFloatVerifier implements VerifyListener
{
    public void verifyText(VerifyEvent e)
    {
        String value = e.text;
        
        // Backspace and Delete
        if (e.keyCode == 8 || e.keyCode == 127 )
        {
            e.doit = true;
        }
        else
        {
            if (value == null || value.length() == 0)
            {
                e.doit = true;
            }
            else
            {
                try
                {
                    if(value.startsWith(".") )
                        value="0"+value;
                    if(value.endsWith(".") )
                        value = value.substring(0, value.length()-1);
                    
                    
                    float floatValue = Float.parseFloat(value);
                    
                    if (floatValue >= 0)
                    {
                        e.doit = true;
                    }
                    else
                    {
                        e.doit = false;
                    }
                }
                catch (NumberFormatException exception)
                {
                    e.doit = false;
                }
            }
        }
    }
}
