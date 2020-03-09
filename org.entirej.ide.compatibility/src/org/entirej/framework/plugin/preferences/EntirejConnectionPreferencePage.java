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
package org.entirej.framework.plugin.preferences;

import java.lang.reflect.InvocationTargetException;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.SQLException;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.jface.preference.IPreferenceNode;
import org.eclipse.jface.preference.PreferenceDialog;
import org.eclipse.jface.preference.PreferenceManager;
import org.eclipse.jface.preference.PreferenceNode;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbenchPropertyPage;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.EntireJFrameworkPlugin;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.EJCorePlugin;
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

public class EntirejConnectionPreferencePage extends FieldEditorOverlayPage implements IWorkbenchPropertyPage
{
    
    public EntirejConnectionPreferencePage()
    {
        super(GRID,false);
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
        if (getElement() instanceof IProject)
        {
            addField(new LabelFieldEditor(getFieldEditorParent(), "Database Settings:"));
            addField(new LabelFieldEditor(getFieldEditorParent(), ""));
            
            final StringFieldEditor driver = new StringFieldEditor(EntireJFrameworkPlugin.P_DBDRIVER, "  &Driver:", getFieldEditorParent());
            addField(driver);
            final StringFieldEditor url = new StringFieldEditor(EntireJFrameworkPlugin.P_URL, "  &Connection URL:", getFieldEditorParent());
            addField(url);
            final StringFieldEditor schema = new StringFieldEditor(EntireJFrameworkPlugin.P_SCHEMA, "  &Schema:", getFieldEditorParent());
            addField(schema);
            final StringFieldEditor user = new StringFieldEditor(EntireJFrameworkPlugin.P_USERNAME, "  User&name:", getFieldEditorParent());
            addField(user);
            final StringFieldEditor password = new StringFieldEditor(EntireJFrameworkPlugin.P_PASSWORD, "  P&assword:", getFieldEditorParent());
            password.getTextControl(getFieldEditorParent()).setEchoChar('*');
            addField(password);
            
            final IProject project = (IProject) getElement();
            
            new Label(getFieldEditorParent(), SWT.NONE);
            final Button test = new Button(getFieldEditorParent(), SWT.PUSH);
            test.setText("Validate");
            addField(new LabelFieldEditor(getFieldEditorParent(), "")
            {
                
                @Override
                public void setEnabled(boolean enabled, Composite parent)
                {
                    super.setEnabled(enabled, parent);
                    test.setEnabled(enabled);
                }
                
                @Override
                public int getNumberOfControls()
                {
                    return 1;
                }
            });
            
            new Label(getFieldEditorParent(), SWT.NONE);
            test.addSelectionListener(new SelectionAdapter()
            {
                @Override
                public void widgetSelected(SelectionEvent evt)
                {
                    final String driverV = driver.getStringValue();
                    final String urlV = url.getStringValue();
                    final String schemaV = schema.getStringValue();
                    final String userV = user.getStringValue();
                    final String passV = password.getStringValue();
                    final IRunnableWithProgress activation = new IRunnableWithProgress()
                    {
                        
                        public void run(IProgressMonitor monitor)
                        {
                            try
                            {
                                monitor.beginTask("Validating connection", 2);
                                monitor.worked(1);
                                try
                                {
                                    
                                    validConnection(driverV, urlV, schemaV, userV, passV, project);
                                    final Display display = EJCorePlugin.getStandardDisplay();
                                    display.asyncExec(new Runnable()
                                    {
                                        
                                        public void run()
                                        {
                                            // Validation error
                                            MessageDialog dialog = new MessageDialog(getShell(), //
                                                    "Validation", //
                                                    null, "Successfully validated!", MessageDialog.INFORMATION, //
                                                    new String[] { IDialogConstants.OK_LABEL }, //
                                                    0);
                                            dialog.open();
                                        }
                                    });
                                }
                                catch (EJDevFrameworkException e)
                                {
                                    final String error = e.getMessage();
                                    final Display display = EJCorePlugin.getStandardDisplay();
                                    display.asyncExec(new Runnable()
                                    {
                                        
                                        public void run()
                                        {
                                            // Validation error
                                            MessageDialog dialog = new MessageDialog(getShell(), //
                                                    "Validation error", //
                                                    null, error, MessageDialog.ERROR, //
                                                    new String[] { IDialogConstants.OK_LABEL }, //
                                                    0);
                                            dialog.open();
                                        }
                                    });
                                }
                                monitor.worked(2);
                            }
                            finally
                            {
                                monitor.done();
                                
                            }
                            
                        }
                    };
                    ProgressMonitorDialog dialog = new ProgressMonitorDialog(getShell());
                    try
                    {
                        dialog.run(true, true, activation);
                    }
                    catch (InvocationTargetException e)
                    {
                        EJCoreLog.log(e);
                    }
                    catch (InterruptedException e)
                    {
                        EJCoreLog.log(e);
                    }
                }
            });
        }
        
        
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

   public static void openPage(IProject project)
   {
       EntirejConnectionPreferencePage page = new EntirejConnectionPreferencePage();
       
       final IPreferenceNode targetNode = new PreferenceNode(page.getPageId(), page);
       PreferenceManager manager = new PreferenceManager();
       manager.addToRoot(targetNode);
       page.setElement(project);
       page.setTitle("Connection Settings");
       final PreferenceDialog dialog = new PreferenceDialog(Display.getCurrent().getActiveShell(), manager);
       BusyIndicator.showWhile(Display.getCurrent(), new Runnable()
       {
           public void run()
           {
               dialog.create();
               dialog.setMessage(targetNode.getLabelText());
               dialog.open();
           }
       });
   }
}
