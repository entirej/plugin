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
package org.entirej.framework.plugin.framework.properties.writer;

public abstract class AbstractXmlWriter
{
    private long tagDepth = 0;
    
    // \n\t\t<tagName
    protected void startOpenTAG(StringBuffer buffer, String tagName)
    {
        indent(buffer, tagDepth);
        buffer.append("<");
        buffer.append(tagName);
        tagDepth++;
    }
    
    // \n\t\t
    private void indent(StringBuffer buffer, long depth)
    {
        buffer.append("\n");
        while (depth-- > 0)
        {
            buffer.append("    ");
        }
    }
    
    // >
    protected void closeOpenTAG(StringBuffer buffer)
    {
        buffer.append(">");
    }
    
    // />
    protected void endStartTAG(StringBuffer buffer)
    {
        buffer.append("/");
        closeOpenTAG(buffer);
        tagDepth--;
    }
    
    // \n\t\t<tagName>
    protected void startTAG(StringBuffer buffer, String tagName)
    {
        startOpenTAG(buffer, tagName);
        closeOpenTAG(buffer);
    }
    
    // \n\t\t</tagName>
    protected void endTAG(StringBuffer buffer, String tagName)
    {
        tagDepth--;
        indent(buffer, tagDepth);
        closeTAG(buffer, tagName);
        tagDepth++;
    }
    
    // </tagName>
    protected void closeTAG(StringBuffer buffer, String tagName)
    {
        buffer.append("</");
        buffer.append(tagName);
        closeOpenTAG(buffer);
        tagDepth--;
    }
    
    // <tagName>true|false</tagName>
    protected void writeBooleanTAG(StringBuffer buffer, String tagName, boolean booleanValue)
    {
        writeTAG(buffer, tagName, "" + booleanValue);
    }
    
    // <tagName>12345</tagName>
    protected void writeIntTAG(StringBuffer buffer, String tagName, int intValue)
    {
        writeTAG(buffer, tagName, "" + intValue);
    }
    
    // <tagName><![CDATA[cdataValue]]></tagName>
    protected void writeStringTAG(StringBuffer buffer, String tagName, String cdataValue)
    {
        writeTAG(buffer, tagName, (cdataValue == null || cdataValue.trim().length() == 0) ? "" : "<![CDATA[" + cdataValue + "]]>");
    }
    
    protected void writeTagValue(StringBuffer buffer, String cdataValue)
    {
        buffer.append((cdataValue == null || cdataValue.trim().length() == 0) ? "" : "<![CDATA[" + cdataValue + "]]>");
    }
    
    // name="property"
    protected void writePROPERTY(StringBuffer buffer, String propertyName, String value)
    {
        buffer.append(" ");
        buffer.append(propertyName);
        buffer.append("=\"");
        buffer.append(value == null ? "" : value);
        buffer.append("\"");
    }
    
    // <tagName>value</tagName>
    private void writeTAG(StringBuffer buffer, String tagName, String value)
    {
        startOpenTAG(buffer, tagName);
        closeOpenTAG(buffer);
        buffer.append(value);
        closeTAG(buffer, tagName);
    }
    
}
