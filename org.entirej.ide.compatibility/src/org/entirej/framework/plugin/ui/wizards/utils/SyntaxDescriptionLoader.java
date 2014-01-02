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
package org.entirej.framework.plugin.ui.wizards.utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.StringTokenizer;

/**
 * This class manages the syntax coloring and styling data
 */
public class SyntaxDescriptionLoader
{
    // Lazy cache of SyntaxDescription objects
    private static Map<String, SyntaxDescription> data = new HashMap<String, SyntaxDescription>();
    
    /**
     * Gets the syntax data for an extension
     */
    public static synchronized SyntaxDescription getSyntaxData(String extension)
    {
        // Check in cache
        SyntaxDescription sd = data.get(extension);
        if (sd == null)
        {
            // Not in cache; load it and put in cache
            sd = loadSyntaxData(extension);
            if (sd != null)
            {
                data.put(sd.getExtension(), sd);
            }
        }
        return sd;
    }
    
    /**
     * Loads the syntax data for an extension
     * 
     * @param extension
     *            the extension to load
     * @return SyntaxDescription
     */
    private static SyntaxDescription loadSyntaxData(String extension)
    {
        SyntaxDescription sd = null;
        try
        {
            ResourceBundle rb = ResourceBundle.getBundle(SyntaxDescriptionLoader.class.getPackage().getName() + "." + extension);
            sd = new SyntaxDescription(extension);
            sd.setComment(rb.getString("comment"));
            sd.setMultiLineCommentStart(rb.getString("multilinecommentstart"));
            sd.setMultiLineCommentEnd(rb.getString("multilinecommentend"));
            
            // Load the keywords
            Collection<String> keywords = new ArrayList<String>();
            for (StringTokenizer st = new StringTokenizer(rb.getString("keywords"), " "); st.hasMoreTokens();)
            {
                keywords.add(st.nextToken());
            }
            sd.setKeywords(keywords);
            
            // Load the punctuation
            sd.setPunctuation(rb.getString("punctuation"));
        }
        catch (MissingResourceException e)
        {
            System.err.println(e.getMessage());
        }
        return sd;
    }
}
