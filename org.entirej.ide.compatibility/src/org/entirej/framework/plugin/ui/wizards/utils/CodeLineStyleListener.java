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
import java.util.LinkedList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.LineStyleEvent;
import org.eclipse.swt.custom.LineStyleListener;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;

/**
 * This class performs the syntax highlighting and styling
 */
class CodeLineStyleListener implements LineStyleListener
{
    // Colors
    private static final Color COMMENT_COLOR      = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_GREEN);
    
    private static final Color COMMENT_BACKGROUND = Display.getCurrent().getSystemColor(SWT.COLOR_WHITE);
    
    private static final Color PUNCTUATION_COLOR  = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE);
    
    private static final Color KEYWORD_COLOR      = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_MAGENTA);
    
    private static final Color PARAMETER_COLOR    = Display.getCurrent().getSystemColor(SWT.COLOR_DARK_RED);
    
    private static final Color QUOTED_COLOR       = Display.getCurrent().getSystemColor(SWT.COLOR_BLUE);
    
    // Holds the syntax data
    private SyntaxDescription  syntaxDescription;
    
    // Holds the offsets for all multiline comments
    List<int[]>                commentOffsets;
    
    /**
     * CodeLineStyleListener constructor
     * 
     * @param syntaxDescription
     *            the syntax data to use
     */
    public CodeLineStyleListener(SyntaxDescription syntaxDescription)
    {
        this.syntaxDescription = syntaxDescription;
        commentOffsets = new LinkedList<int[]>();
    }
    
    /**
     * Refreshes the offsets for all multiline comments in the parent
     * StyledText. The parent StyledText should call this whenever its text is
     * modified. Note that this code doesn't ignore comment markers inside
     * strings.
     * 
     * @param text
     *            the text from the StyledText
     */
    public void refreshMultilineComments(String text)
    {
        // Clear any stored offsets
        commentOffsets.clear();
        
        if (syntaxDescription != null)
        {
            // Go through all the instances of COMMENT_START
            for (int pos = text.indexOf(syntaxDescription.getMultiLineCommentStart()); pos > -1; pos = text.indexOf(
                    syntaxDescription.getMultiLineCommentStart(), pos))
            {
                // offsets[0] holds the COMMENT_START offset
                // and COMMENT_END holds the ending offset
                int[] offsets = new int[2];
                offsets[0] = pos;
                
                // Find the corresponding end comment.
                pos = text.indexOf(syntaxDescription.getMultiLineCommentEnd(), pos);
                
                // If no corresponding end comment, use the end of the text
                offsets[1] = pos == -1 ? text.length() - 1 : pos + syntaxDescription.getMultiLineCommentEnd().length() - 1;
                pos = offsets[1];
                // Add the offsets to the collection
                commentOffsets.add(offsets);
            }
        }
    }
    
    /**
     * Checks to see if the specified section of text begins inside a multiline
     * comment. Returns the index of the closing comment, or the end of the line
     * if the whole line is inside the comment. Returns -1 if the line doesn't
     * begin inside a comment.
     * 
     * @param start
     *            the starting offset of the text
     * @param length
     *            the length of the text
     * @return int
     */
    private int getBeginsInsideComment(int start, int length)
    {
        // Assume section doesn't being inside a comment
        int index = -1;
        
        // Go through the multiline comment ranges
        for (int i = 0, n = commentOffsets.size(); i < n; i++)
        {
            int[] offsets = commentOffsets.get(i);
            
            // If starting offset is past range, quit
            if (offsets[0] > start + length)
            {
                break;
            }
            // Check to see if section begins inside a comment
            if (offsets[0] <= start && offsets[1] >= start)
            {
                // It does; determine if the closing comment marker is inside
                // this section
                index = offsets[1] > start + length ? start + length : offsets[1] + syntaxDescription.getMultiLineCommentEnd().length() - 1;
            }
        }
        return index;
    }
    
    /**
     * Called by StyledText to get styles for a line
     */
    @Override
    public void lineGetStyle(LineStyleEvent event)
    {
        // Only do styles if syntax data has been loaded
        if (syntaxDescription != null)
        {
            // Create collection to hold the StyleRanges
            List<StyleRange> styles = new ArrayList<StyleRange>();
            
            int start = 0;
            int length = event.lineText.length();
            
            // Check if line begins inside a multiline comment
            int mlIndex = getBeginsInsideComment(event.lineOffset, event.lineText.length());
            if (mlIndex > -1)
            {
                // Line begins inside multiline comment; create the range
                styles.add(new StyleRange(event.lineOffset, mlIndex - event.lineOffset, COMMENT_COLOR, COMMENT_BACKGROUND));
                start = mlIndex;
            }
            // Do punctuation, single-line comments, and keywords
            while (start < length)
            {
                // Check for multiline comments that begin inside this line
                if (event.lineText.indexOf(syntaxDescription.getMultiLineCommentStart(), start) == start)
                {
                    // Determine where comment ends
                    int endComment = event.lineText.indexOf(syntaxDescription.getMultiLineCommentEnd(), start);
                    
                    // If comment doesn't end on this line, extend range to end
                    // of line
                    if (endComment == -1)
                    {
                        endComment = length;
                    }
                    else
                    {
                        endComment += syntaxDescription.getMultiLineCommentEnd().length();
                    }
                    styles.add(new StyleRange(event.lineOffset + start, endComment - start, COMMENT_COLOR, COMMENT_BACKGROUND));
                    
                    // Move marker
                    start = endComment;
                }
                // Check for single line comments
                else if (event.lineText.indexOf(syntaxDescription.getComment(), start) == start)
                {
                    // Comment rest of line
                    styles.add(new StyleRange(event.lineOffset + start, length - start, COMMENT_COLOR, COMMENT_BACKGROUND));
                    
                    // Move marker
                    start = length;
                }
                // Check for punctuation
                else if (syntaxDescription.getPunctuation().indexOf(event.lineText.charAt(start)) > -1)
                {
                    // Add range for punctuation
                    styles.add(new StyleRange(event.lineOffset + start, 1, PUNCTUATION_COLOR, null));
                    ++start;
                }
                else if (event.lineText.charAt(start) == '"' || event.lineText.charAt(start) == '\'')
                {// quoted text
                 // Get the text
                    char mark = event.lineText.charAt(start);
                    StringBuilder buf = new StringBuilder();
                    int i = start + 1;// pass the first "/'
                    // search for the second "/'
                    boolean ok = false;
                    for (; i < length; i++)
                    {
                        boolean inside = true;
                        if (event.lineText.charAt(i) != mark)
                        {
                            inside = true;
                        }
                        else
                        {
                            if (event.lineText.charAt(i - 1) == '\\')
                            {
                                inside = true;
                            }
                            else
                            {
                                inside = true;
                                ok = true;
                            }
                        }
                        if (inside)
                        {
                            buf.append(event.lineText.charAt(i));
                        }
                        if (ok)
                        {
                            i++;
                            break;
                        }
                    }
                    if (ok)
                    {
                        // It's a quoted text; create the StyleRange
                        styles.add(new StyleRange(event.lineOffset + start, i - start, QUOTED_COLOR, null, SWT.NONE));
                        start = i;
                    }
                    else
                    {
                        styles.add(new StyleRange(event.lineOffset + start, 1, null, Display.getCurrent().getSystemColor(SWT.COLOR_RED)));
                        ++start;
                    }
                }
                else if (event.lineText.charAt(start) == ':')
                {// It may be a parameter :PARAM or :BLOCK.ITEM_NAME
                
                    // Get the next word
                    StringBuilder buf = new StringBuilder();
                    int i = start + 1;// pass the :
                    // Call any consecutive letters a word
                    for (; i < length && (syntaxDescription.isLetterOrDigit(event.lineText.charAt(i))); i++)
                    {
                        buf.append(event.lineText.charAt(i));
                    }
                    if (i < length && event.lineText.charAt(i) == '.')
                    {// if it is the second part
                        buf.append('.');
                        i++;
                        for (; i < length && (syntaxDescription.isLetterOrDigit(event.lineText.charAt(i))); i++)
                        {
                            buf.append(event.lineText.charAt(i));
                        }
                    }
                    if (buf.length() > 0)
                    {
                        Event ev = null;
                        if (event.widget.isListening(HandleParameterListener.ParameterEvent))
                        {
                            ev = new Event();
                            ev.text = buf.toString();
                            event.widget.notifyListeners(HandleParameterListener.ParameterEvent, ev);
                        }
                        // It's a parameter; create the StyleRange
                        styles.add(new StyleRange(event.lineOffset + start, i - start, (ev == null || ev.doit) ? PARAMETER_COLOR : Display.getCurrent()
                                .getSystemColor(SWT.COLOR_RED), null, SWT.ITALIC | ((ev == null || ev.doit) ? SWT.NORMAL : SWT.BOLD)));
                    }
                    start = i;
                }
                else if (syntaxDescription.isLetter(event.lineText.charAt(start)))
                {
                    // Get the next word
                    StringBuilder buf = new StringBuilder();
                    int i = start;
                    // Call any consecutive letters a word
                    for (; i < length && (syntaxDescription.isLetter(event.lineText.charAt(i))); i++)
                    {
                        buf.append(event.lineText.charAt(i));
                    }
                    // See if the word is a keyword
                    if (syntaxDescription.getKeywords().contains(buf.toString().toUpperCase()))
                    {
                        // It's a keyword; create the StyleRange
                        styles.add(new StyleRange(event.lineOffset + start, i - start, KEYWORD_COLOR, null, SWT.BOLD));
                    }
                    // Move the marker to the last char (the one that wasn't a
                    // letter)
                    // so it can be retested in the next iteration through the
                    // loop
                    start = i;
                }
                else
                {
                    // It's nothing we're interested in; advance the marker
                    ++start;
                }
            }
            
            // Copy the StyleRanges back into the event
            event.styles = styles.toArray(new StyleRange[0]);
        }
    }
}
