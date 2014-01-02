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

import java.util.Collection;

/**
 * This class contains information for syntax coloring and styling for an
 * extension
 */
class SyntaxDescription
{
    private String             extension;
    
    private Collection<String> keywords;
    
    private String             punctuation;
    
    private String             comment;
    
    private String             multiLineCommentStart;
    
    private String             multiLineCommentEnd;
    
    /**
     * Constructs a SyntaxDescription
     * 
     * @param extension
     *            the extension
     */
    public SyntaxDescription(String extension)
    {
        this.extension = extension;
    }
    
    /**
     * Gets the extension
     * 
     * @return String
     */
    public String getExtension()
    {
        return extension;
    }
    
    /**
     * Gets the comment
     * 
     * @return String
     */
    public String getComment()
    {
        return comment;
    }
    
    /**
     * Sets the comment
     * 
     * @param comment
     *            The comment to set.
     */
    public void setComment(String comment)
    {
        this.comment = comment;
    }
    
    /**
     * Gets the keywords
     * 
     * @return Collection
     */
    public Collection<String> getKeywords()
    {
        return keywords;
    }
    
    /**
     * Sets the keywords
     * 
     * @param keywords
     *            The keywords to set.
     */
    public void setKeywords(Collection<String> keywords)
    {
        this.keywords = keywords;
    }
    
    public boolean isLetter(char ch)
    {
        return Character.isLetter(ch) || ch == '_';
    }
    
    public boolean isLetterOrDigit(char ch)
    {
        return Character.isLetterOrDigit(ch) || ch == '_';
    }
    
    /**
     * Gets the multiline comment end
     * 
     * @return String
     */
    public String getMultiLineCommentEnd()
    {
        return multiLineCommentEnd;
    }
    
    /**
     * Sets the multiline comment end
     * 
     * @param multiLineCommentEnd
     *            The multiLineCommentEnd to set.
     */
    public void setMultiLineCommentEnd(String multiLineCommentEnd)
    {
        this.multiLineCommentEnd = multiLineCommentEnd;
    }
    
    /**
     * Gets the multiline comment start
     * 
     * @return String
     */
    public String getMultiLineCommentStart()
    {
        return multiLineCommentStart;
    }
    
    /**
     * Sets the multiline comment start
     * 
     * @param multiLineCommentStart
     *            The multiLineCommentStart to set.
     */
    public void setMultiLineCommentStart(String multiLineCommentStart)
    {
        this.multiLineCommentStart = multiLineCommentStart;
    }
    
    /**
     * Gets the punctuation
     * 
     * @return String
     */
    public String getPunctuation()
    {
        return punctuation;
    }
    
    /**
     * Sets the punctuation
     * 
     * @param punctuation
     *            The punctuation to set.
     */
    public void setPunctuation(String punctuation)
    {
        this.punctuation = punctuation;
    }
}
