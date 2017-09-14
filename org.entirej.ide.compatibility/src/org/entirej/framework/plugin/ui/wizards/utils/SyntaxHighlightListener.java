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
package org.entirej.framework.plugin.ui.wizards.utils;

import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;

public class SyntaxHighlightListener implements ModifyListener
{
    // Displays the file
    private StyledText            st;
    
    // Syntax data for the current extension
    private SyntaxDescription     sd;
    
    // Line style listener
    private CodeLineStyleListener lineStyleListener;
    
    public SyntaxHighlightListener(StyledText styledText, String language)
    {
        super();
        st = styledText;
        updateSyntaxData(language);
    }
    
    @Override
    public void modifyText(ModifyEvent arg0)
    {
        // Update the comments
        if (lineStyleListener != null)
        {
            lineStyleListener.refreshMultilineComments(st.getText());
            st.redraw();
        }
    }
    
    /**
     * Updates the syntax data based on the filename's extension
     */
    public void updateSyntaxData(String filetype)
    {
        // Get the syntax data for the extension
        sd = SyntaxDescriptionLoader.getSyntaxData(filetype);
        
        // Reset the line style listener
        if (lineStyleListener != null)
        {
            st.removeLineStyleListener(lineStyleListener);
        }
        lineStyleListener = new CodeLineStyleListener(sd);
        st.addLineStyleListener(lineStyleListener);
        
        // Redraw the contents to reflect the new syntax data
        st.redraw();
    }
    
}
