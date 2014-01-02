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
package org.entirej.framework.plugin.preferences;

import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IntegerFieldEditor;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.ide.core.project.EJPluginEntireJClassLoader;

/**
 * This class represents a preference page that is contributed to the
 * Preferences dialog. By subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows us to create a page
 * that is small and knows how to save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They are stored in the
 * preference store that belongs to the main plug-in class. That way,
 * preferences can be accessed directly via the preference store.
 */

public class EntirejPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPreferencePage ,IWorkbenchPropertyPage
{
    public EntirejPreferencePage()
    {
        super(GRID);
        setPreferenceStore(EntireJFrameworkPlugin.getSharedInstance().getPreferenceStore());
    }
    
    protected String getPageId()
    {
        return "org.entirej.framework.plugin.preferences.EntirejPreferencePage";
    }
    
    /**
     * Creates the field editors. Field editors are abstractions of the common
     * GUI blocks needed to manipulate various types of preferences. Each field
     * editor knows how to save and restore itself.
     */
    
    @Override
    public void createFieldEditors()
    {
        
        addField(new LabelFieldEditor(getFieldEditorParent(), "Form Defaults:"));
        addField(new LabelFieldEditor(getFieldEditorParent(), ""));
        
        addField(new IntegerFieldEditor(EntireJFrameworkPlugin.P_FORM_HEIGHT, "  Fo&rm Height:", getFieldEditorParent()));
        addField(new IntegerFieldEditor(EntireJFrameworkPlugin.P_FORM_WIDTH, "  Form &Width:", getFieldEditorParent()));
        
    }
    
    @Override
    public void init(IWorkbench workbench)
    {
    }
    
    private class LabelFieldEditor extends FieldEditor
    {
        public LabelFieldEditor(Composite parent, String labelText)
        {
            super("", labelText, parent);
        }
        
        @Override
        protected void adjustForNumColumns(int numColumns)
        {
            // do nothing
        }
        
        @Override
        protected void doFillIntoGrid(Composite parent, int numColumns)
        {
            getLabelControl(parent);
        }
        
        @Override
        protected void doLoad()
        {
            // do nothing
        }
        
        @Override
        protected void doLoadDefault()
        {
            // do nothing
        }
        
        @Override
        protected void doStore()
        {
            // do nothing
        }
        
        @Override
        public int getNumberOfControls()
        {
            return 0;
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
    
    public static void validConnection(String driver, String url, String schema, String username, String password, IProject project)
            throws EJDevFrameworkException
    {
        validateConnectionSettings(driver, url, username, password);
        try
        {
            
            Class<?> driverClass = null;
            try
            {
                driverClass = EJPluginEntireJClassLoader.loadClass(JavaCore.create(project), driver);
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
            
            if (schema != null && schema.trim().length() > 0) userProps.put("currentSchema", schema);
            
            Connection con = ((Driver) driverObject).connect(url, userProps);
            con.close();
        }
        catch (SQLException e)
        {
            throw new EJDevFrameworkException("Unable to access database : " + e.getMessage(), e);
        }
    }
}
