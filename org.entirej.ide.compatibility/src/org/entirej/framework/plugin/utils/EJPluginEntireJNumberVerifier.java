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

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.VerifyEvent;
import org.eclipse.swt.events.VerifyListener;

public class EJPluginEntireJNumberVerifier implements VerifyListener
{
    
    boolean supportNegative;
    public EJPluginEntireJNumberVerifier()
    {
       
    }
    public EJPluginEntireJNumberVerifier(boolean supportNegative)
    {
       this.supportNegative = supportNegative;
    }
    protected boolean validate(String value)
    {
        
        try
        {
            int intValue = Integer.parseInt(value);
            
            if (intValue >= 0)
            {
                return true;
            }
            else
            {
               return false;
            }
        }
        catch (NumberFormatException exception)
        {
           //ignore
        }
        
        return false;
    }
    
    public void verifyText(VerifyEvent e)
    {
        String value = e.text;
        
        
        if(value.equals(".")){
            e.doit = true;
            return ;
        }
        if(supportNegative && value.startsWith("-")){
            e.doit = true;
            return ;
        }
        // Backspace and Delete
        if (e.keyCode == SWT.DEL || e.keyCode == SWT.BS)
        {
            e.doit = true;
            
            if(!validate(value))
            {
                e.text = "";
                
            }
        }
        else
        {
            if (value == null || value.length() == 0)
            {
                e.doit = true;
            }
            else
            {
                e.doit = validate(value);
            }
        }
    }
}
