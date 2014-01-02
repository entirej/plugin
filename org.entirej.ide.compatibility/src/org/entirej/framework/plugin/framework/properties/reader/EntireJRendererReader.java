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
package org.entirej.framework.plugin.framework.properties.reader;

import java.io.IOException;
import java.io.InputStream;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.framework.properties.EJPluginEntireJProperties;
import org.xml.sax.SAXException;

public class EntireJRendererReader
{
    public static void readProperties(EJPluginEntireJProperties newProperties, InputStream inStream) throws EJDevFrameworkException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxPArser = factory.newSAXParser();
            EntireJRendererHandler handler = new EntireJRendererHandler(newProperties);
            saxPArser.parse(inStream, handler);
            inStream.close();
        }
        catch (SAXException e)
        {
            throw new EJDevFrameworkException(e);
        }
        catch (ParserConfigurationException e)
        {
            throw new EJDevFrameworkException(e);
        }
        catch (IOException e)
        {
            throw new EJDevFrameworkException(e);
        }
        finally
        {
            try
            {
                inStream.close();
            }
            catch (IOException e)
            {
                throw new EJDevFrameworkException(e);
            }
        }
    }
    
    public static void refreshProperties(EJPluginEntireJProperties properties, InputStream inStream) throws EJDevFrameworkException
    {
        try
        {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxPArser = factory.newSAXParser();
            EntireJRendererHandler handler = new EntireJRendererHandler(properties);
            saxPArser.parse(inStream, handler);
        }
        catch (SAXException e)
        {
            throw new EJDevFrameworkException(e);
        }
        catch (ParserConfigurationException e)
        {
            throw new EJDevFrameworkException(e);
        }
        catch (IOException e)
        {
            throw new EJDevFrameworkException(e);
        }
    }
}
