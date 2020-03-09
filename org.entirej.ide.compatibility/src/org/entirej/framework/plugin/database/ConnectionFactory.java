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
package org.entirej.framework.plugin.database;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.framework.plugin.preferences.EJPropertyRetriever;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;

public class ConnectionFactory
{
    public Connection createConnection(IJavaProject javaProject) throws EJDevFrameworkException
    {
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
                MessageDialog.openError(EntireJFrameworkPlugin.getSharedInstance().getActiveWorkbenchShell(), "ClassNotFoundException",
                        "Unable to load database driver: " + driver);
                throw new EJDevFrameworkException(e.getMessage(), e);
            }
            
            Object driverObject = null;
            
            try
            {
                driverObject = driverClass.newInstance();
            }
            catch (InstantiationException e)
            {
                MessageDialog.openError(EntireJFrameworkPlugin.getSharedInstance().getActiveWorkbenchShell(), "InstantiationException",
                        "Unable to initialise database driver: " + driver);
                throw new EJDevFrameworkException(e.getMessage(), e);
            }
            catch (IllegalAccessException e)
            {
                MessageDialog.openError(EntireJFrameworkPlugin.getSharedInstance().getActiveWorkbenchShell(), "IllegalAccessException",
                        "Unable to access database driver: " + driver);
                throw new EJDevFrameworkException(e.getMessage(), e);
            }
            
            Properties userProps = new Properties();
            userProps.put("user", username);
            userProps.put("password", password);
            Connection con = ((Driver) driverObject).connect(url, userProps);
            return con;
        }
        catch (SQLException e)
        {
            e.printStackTrace();
            MessageDialog.openError(EntireJFrameworkPlugin.getSharedInstance().getActiveWorkbenchShell(), "SQLException", e.getMessage());
            throw new EJDevFrameworkException(e.getMessage(), e);
        }
    }
}
