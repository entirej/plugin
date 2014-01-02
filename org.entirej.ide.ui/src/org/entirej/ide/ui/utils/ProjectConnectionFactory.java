/*******************************************************************************
 * Copyright 2013 Mojave Innovations GmbH
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
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
package org.entirej.ide.ui.utils;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.jdt.core.IJavaProject;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;

public class ProjectConnectionFactory
{
    private ProjectConnectionFactory()
    {
        throw new AssertionError();
    }

    public static void validateConnectionSettings(IJavaProject javaProject) throws EJDevFrameworkException
    {
        String driver = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_DBDRIVER);
        String url = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_URL);
        String username = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_USERNAME);
        String password = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_PASSWORD);

        if (driver == null || driver.trim().length() == 0 || EJPropertyRetriever.isDefault(javaProject.getProject(), EntireJFrameworkPlugin.P_DBDRIVER)
                || url == null || url.trim().length() == 0 || EJPropertyRetriever.isDefault(javaProject.getProject(), EntireJFrameworkPlugin.P_URL)
                || username == null || EJPropertyRetriever.isDefault(javaProject.getProject(), EntireJFrameworkPlugin.P_USERNAME) || password == null
                || EJPropertyRetriever.isDefault(javaProject.getProject(), EntireJFrameworkPlugin.P_PASSWORD))
        {
            throw new EJDevFrameworkException(
                    "One or more preferences that are required for Service/Pojo generation are missing.\n\nRequired preferences:\n- Database Driver\n- URL\n- Username\n- Password");
        }
    }

    public static void validateConnectionSettings(String driver, String url, String username, String password) throws EJDevFrameworkException
    {

        if (driver == null || driver.trim().length() == 0 || EJPropertyRetriever.isDefault(driver, EntireJFrameworkPlugin.P_DBDRIVER) || url == null
                || url.trim().length() == 0 || EJPropertyRetriever.isDefault(url, EntireJFrameworkPlugin.P_URL) || username == null
                || EJPropertyRetriever.isDefault(username, EntireJFrameworkPlugin.P_USERNAME) || password == null
                || EJPropertyRetriever.isDefault(password, EntireJFrameworkPlugin.P_PASSWORD))
        {
            throw new EJDevFrameworkException(
                    "One or more preferences that are required for Service/Pojo generation are missing.\n\nRequired preferences:\n- Database Driver\n- URL\n- Username\n- Password");
        }
    }

    public static String getDBSchema(IJavaProject javaProject)
    {
        return EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_SCHEMA);
    }

    public static Connection createConnection(IJavaProject javaProject) throws EJDevFrameworkException
    {
        validateConnectionSettings(javaProject);
        try
        {

            String driver = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_DBDRIVER);
            String url = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_URL);
            String username = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_USERNAME);
            String password = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_PASSWORD);

            Class<?> driverClass = null;
            try
            {
                driverClass = EJPluginEntireJClassLoader.loadClass(javaProject, driver);
            }
            catch (ClassNotFoundException e)
            {

                throw new EJDevFrameworkException("Unable to load database driver: " + driver, e);
            }

            Object driverObject = null;

            try
            {
                driverObject = driverClass.newInstance();
            }
            catch (InstantiationException e)
            {
                throw new EJDevFrameworkException("Unable to initialise database driver: " + driver, e);
            }
            catch (IllegalAccessException e)
            {

                throw new EJDevFrameworkException("Unable to access database driver: " + driver, e);
            }

            Properties userProps = new Properties();
            userProps.put("user", username);
            userProps.put("password", password);
            String schema = EJPropertyRetriever.getValue(javaProject.getProject(), EntireJFrameworkPlugin.P_SCHEMA);
            if (schema != null && schema.trim().length() > 0)
                userProps.put("currentSchema", schema);

            Connection con = ((Driver) driverObject).connect(url, userProps);
            return con;
        }
        catch (SQLException e)
        {
            throw new EJDevFrameworkException("Unable to access database : " + e.getMessage(), e);
        }
    }

}
