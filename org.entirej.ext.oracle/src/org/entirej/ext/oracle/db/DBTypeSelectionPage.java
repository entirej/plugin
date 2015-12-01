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
package org.entirej.ext.oracle.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.entirej.ext.oracle.db.Argument.Type;
import org.entirej.framework.dev.exceptions.EJDevFrameworkException;
import org.entirej.framework.plugin.preferences.EntirejConnectionPreferencePage;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.spi.BlockServiceContentProvider.GeneratorContext;
import org.entirej.ide.ui.EJUIImages;
import org.entirej.ide.ui.EJUIPlugin;
import org.entirej.ide.ui.utils.ProjectConnectionFactory;

public class DBTypeSelectionPage extends WizardPage
{

    private AbstractFilteredTree dbfilteredTree;
    private CheckboxTableViewer  listViewer;

    private Procedure            selectedProcedure;
    private ObjectArgument       selectedObjectArgument;

    private String               dbError;
    private Connection           conn;
    private DBContentProvider    contentProvider;
    private LabelProvider        labelProvider;
    
    private GeneratorContext context;

    public DBTypeSelectionPage()
    {
        super("ej.db.db.fucntion.selection");
        setTitle("Oracle Funtion/Procedure Selection");
        setDescription("Select columns from Type/Funtion/Procedure.");
    }
    
    void setGeneratorContext(GeneratorContext context)
    {
        this.context = context;
    }
    
    
    

    

    public ITreeContentProvider getContentProvider()
    {
        return contentProvider;
    }

    public LabelProvider getLabelProvider()
    {
        return labelProvider;
    }

    
    public static int getDataTypeIntForOraType(String jdbcType)
    {
        
       if("BINARY_INTEGER".equals(jdbcType))
       {
           return Types.BINARY;
       }
       if("NATURAL".equals(jdbcType))
       {
           return Types.INTEGER;
       }
       if("NATURALN".equals(jdbcType))
       {
           return Types.INTEGER;
       }
       if("PLS_INTEGER".equals(jdbcType))
       {
           return Types.INTEGER;
       }
       if("POSITIVE".equals(jdbcType))
       {
           return Types.INTEGER;
       }
       if("SIGNTYPE".equals(jdbcType))
       {
           return Types.INTEGER;
       }
       if("INTEGER".equals(jdbcType))
       {
           return Types.INTEGER;
       }
       if("INT".equals(jdbcType))
       {
           return Types.INTEGER;
       }
       if("SMALLINT".equals(jdbcType))
       {
           return Types.SMALLINT;
       }
       if("BIGINT".equals(jdbcType))
       {
           return Types.BIGINT;
       }
       if("DECIMAL".equals(jdbcType))
       {
           return Types.DECIMAL;
       }
       if("DEC".equals(jdbcType))
       {
           return Types.DECIMAL;
       }
       if("NUMBER".equals(jdbcType))
       {
           return Types.DECIMAL;
       }
       if("NUMERIC".equals(jdbcType))
       {
           return Types.DECIMAL;
       }
       
       
       if("DOUBLE".equals(jdbcType))
       {
           return Types.DOUBLE;
       }
       if("PRECISION".equals(jdbcType))
       {
           return Types.DOUBLE;
       }
       if("FLOAT".equals(jdbcType))
       {
           return Types.FLOAT;
       }
       if("DATE".equals(jdbcType))
       {
           return Types.DATE;
       }
       if("TIMESTAMP".equals(jdbcType))
       {
           return Types.TIMESTAMP;
       }
       if("REAL".equals(jdbcType))
       {
           return Types.REAL;
       }
        
        
        
       
        
       
        if ("BOOLEAN".equals(jdbcType))
            return Types.BOOLEAN;
        if ("OBJECT".equals(jdbcType))
            return Types.ARRAY;
        if ("TABLE".equals(jdbcType))
            return Types.ARRAY;
        if ("CLOB".equals(jdbcType))
            return Types.CLOB;
        if ("BLOB".equals(jdbcType))
            return Types.BLOB;
        if ("STRUCT".equals(jdbcType))
            return Types.STRUCT;
        
        return Types.VARCHAR;
    }
    
    protected void init(IJavaProject javaProject)
    {
        try
        {
            if (conn != null && !conn.isClosed())
            {
                conn.close();
                conn = null;
            }
            try
            {
                conn = ProjectConnectionFactory.createConnection(javaProject);
            }
            catch (EJDevFrameworkException e)
            {
                if (MessageDialog.openQuestion(EJUIPlugin.getActiveWorkbenchShell(), "Connection Error",
                        String.format("%s \n\n Change Project Connection Settings ?",e.getMessage())))
                {
                    EntirejConnectionPreferencePage.openPage(javaProject.getProject());
                    conn = ProjectConnectionFactory.createConnection(javaProject);
                }
            }

        }
        catch (Exception e)
        {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void setVisible(boolean visible)
    {

        super.setVisible(visible);
        if (visible)
        {

            if (dbfilteredTree != null)
                dbfilteredTree.getViewer().setInput(getDBInput());
            doUpdateStatus();
            
            if(context.skipService())
            {
                setTitle("Oracle Types Selection");
                setDescription("Select columns from Type/Funtion/Procedure.");
            }
            
            else
            {
                setTitle("Oracle Funtion/Procedure Selection");
                setDescription("Select the type to use to create your pojo.");
            }
        }

    }

    @Override
    public void dispose()
    {
        try
        {
            if (conn != null && !conn.isClosed())
            {
                conn.close();
                conn = null;
            }
        }
        catch (SQLException e)
        {
            EJCoreLog.logException(e);
        }
        super.dispose();
    }

  

    public void createControl(Composite parent)
    {
        initializeDialogUnits(parent);

        Composite composite = new Composite(parent, SWT.NONE);
        composite.setFont(parent.getFont());
        int nColumns = 1;

        GridLayout layout = new GridLayout();
        layout.numColumns = nColumns;
        layout.makeColumnsEqualWidth = true;
        layout.verticalSpacing = 0;
        composite.setLayout(layout);
        createDBViewComponent(composite);

        setControl(composite);
        Dialog.applyDialogFont(composite);
    }

    public static Control createEmptySpace(Composite parent, int span)
    {
        Label label = new Label(parent, SWT.LEFT);
        GridData gd = new GridData();
        gd.horizontalAlignment = GridData.BEGINNING;
        gd.grabExcessHorizontalSpace = false;
        gd.horizontalSpan = span;
        gd.horizontalIndent = 0;
        gd.widthHint = 0;
        gd.heightHint = 0;
        label.setLayoutData(gd);
        return label;
    }

    public void refresh(Object input)
    {
        TreeViewer treeview = dbfilteredTree != null ? dbfilteredTree.getViewer() : null;
        if (treeview != null)
        {
            Object[] expanded = treeview.getExpandedElements();

            treeview.getControl().setRedraw(false);
            treeview.setInput(input);
            treeview.setExpandedElements(expanded);
            treeview.getControl().setRedraw(true);
            treeview.refresh();
        }

    }

    private void createDBViewComponent(Composite composite)
    {

        dbfilteredTree = new AbstractFilteredTree(composite, SWT.VIRTUAL | SWT.V_SCROLL | SWT.BORDER | SWT.MULTI)
        {

            @Override
            public void filter(String filter)
            {
                if (contentProvider != null && ((filter == null && contentProvider.getFilter() != null) || !filter.equals(contentProvider.getFilter())))
                {
                    contentProvider.setFilter(filter);
                    refresh(filter);
                }

            }

        };

        GridData treeGD = new GridData(GridData.FILL_BOTH | GridData.GRAB_VERTICAL);
        treeGD.widthHint = 250;
        treeGD.heightHint = 300;
        dbfilteredTree.setLayoutData(treeGD);
        final TreeViewer viewer = dbfilteredTree.getViewer();
        viewer.setContentProvider(contentProvider = new DBContentProvider());
        viewer.setLabelProvider(labelProvider = new DBLabelProvider());
        viewer.setComparator(new ViewerComparator()
        {
            @Override
            public int compare(Viewer viewer, Object e1, Object e2)
            {

                String name1 = labelProvider.getText(e1);
                String name2 = labelProvider.getText(e2);
                return name1.compareToIgnoreCase(name2);
            }

        });
        viewer.addSelectionChangedListener(new ISelectionChangedListener()
        {

            public void selectionChanged(SelectionChangedEvent event)
            {
                Object node = new Object();
                ISelection selection = viewer.getSelection();
                IStructuredSelection strutruredSelection = (IStructuredSelection) selection;
                if (strutruredSelection.size() == 1 && strutruredSelection.getFirstElement() != null)
                {
                    node = strutruredSelection.getFirstElement();
                }
                selectedProcedure = null;
                selectedObjectArgument = null;
                if (listViewer != null)
                {
                    listViewer.setInput(node);
                }
                if (node instanceof Procedure)
                {
                    selectedProcedure = (Procedure) node;

                }
                if (node instanceof ObjectArgument)
                {
                    selectedObjectArgument = (ObjectArgument) node;
                    
                }
                doUpdateStatus();
            }
        });
    }

    protected void doUpdateStatus()
    {
        setPageComplete(validatePage());
    }

    
    ObjectArgument getObjectArgument()
    {
        if(context.skipService())
        {
            return selectedObjectArgument;
        }
       
        if (selectedProcedure != null)
        {
            return selectedProcedure.getCollectionType();
        }
        return  null;
    }
    
    protected boolean validatePage()
    {
        if (dbError != null)
        {
            setErrorMessage(dbError);
            return false;
        }
        if(context.skipService())
        {
             if (selectedObjectArgument == null)
            {
                setErrorMessage("Type not selected.");
                return false;
            }
             
        }
        else
        {
            if (selectedProcedure == null)
            {
                setErrorMessage("Function/Procedure not selected.");
                return false;
            }
            
            else
            {
                boolean foundCollectionType = selectedProcedure.getCollectionType() != null;
                if (!foundCollectionType)
                {
                    setErrorMessage("Selected Function/Procedure not provide collection type.");
                    return false;
                }
            }
        }
        
       
       

        setErrorMessage(null);
        setMessage(null);
        return true;
    }

    private Object getDBInput()
    {
        return new Object();
    }

    private final class DBLabelProvider extends LabelProvider
    {
        @Override
        public String getText(Object element)
        {

            if (element instanceof Function)
            {
                Function function = ((Function) element);

                return function.getName();
            }
            if (element instanceof Procedure)
            {
                return ((Procedure) element).getName();
            }
            if (element instanceof ObjectArgument)
            {
                ObjectArgument argument = (ObjectArgument) element;
                String name = argument._name != null ? argument._name : argument.objName;
                if(name.isEmpty())
                {
                    return argument.getObjName();
                }
                String typeDef = argument.tableName != null ? String.format("%s [ %s ]", argument.tableName, name) : name;
                return argument.type != Type.IN_OUT ? String.format("%s --> %s", typeDef, argument.type.name()) : typeDef;
            }
            if (element instanceof Argument)
            {
                Argument argument = (Argument) element;
                String typeDef = argument._name != null ? String.format("%s :( %s )", argument._name, argument._datatype) : argument._datatype;
                return argument.type != Type.IN_OUT ? String.format("%s --> %s", typeDef, argument.type.name()) : typeDef;
            }
            if (element instanceof Group)
            {
                return ((Group) element).name;
            }
            return super.getText(element);
        }

        @Override
        public Image getImage(Object element)
        {

            if (element instanceof Function)
            {
                return EJUIImages.getImage(EJUIImages.DESC_ACTION_LIB);
            }
            if (element instanceof Procedure)
            {
                return EJUIImages.getImage(EJUIImages.DESC_ACTION);
            }
            if (element instanceof ObjectArgument)
            {
                if (((ObjectArgument) element).tableName == null)
                    return EJUIImages.SHARED_INNER_CLASS_PROTECTED;
                return EJUIImages.SHARED_INNER_CLASS_PUBLIC;
            }
            if (element instanceof Argument)
            {
                Argument argument = (Argument) element;
                if (argument.type == Type.IN)
                    return EJUIImages.SHARED_FIELD_PROTECTED;
                if (argument.type == Type.OUT || argument.type == Type.RETURN)
                    return EJUIImages.SHARED_FIELD_PUBLIC;
                return EJUIImages.SHARED_FIELD_DEFAULT;
            }
            if (element instanceof Group)
            {
                return ((Group) element).getImage();
            }
            return super.getImage(element);
        }
    }

    private class DBContentProvider extends AbstractFilteredTree.FilteredContentProvider
    {
        private Object[] objects;
        private boolean checkForTypes = false;

        public void dispose()
        {
            objects = null;

        }

        public void inputChanged(Viewer viewer, Object oldInput, Object newInput)
        {

        }
        
        
        
        public List<ObjectArgument> getTypes(Connection con) throws SQLException
        {
            List<ObjectArgument> packages = new ArrayList<ObjectArgument>();

            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT TYPE_NAME, TYPECODE FROM ALL_TYPES WHERE OWNER = USER");
            try
            {
                while (rset.next())
                {
                    
                    String typeName =  rset.getString("TYPE_NAME");
                   String dataType =  rset.getString("TYPECODE");
                    
                    
                   ObjectArgument argument = null;
                    if ("COLLECTION".equals(dataType))
                    {
                        argument = createObjectArgumentFromTableType(con, typeName, "");

                    }
                    else if ("OBJECT".equals(dataType))
                    {
                        argument = createObjectArgument(con, typeName, "");
                    }
                    packages.add(argument);
                    
                }

                return packages;
            }
            finally
            {
                rset.close();
                statement.close();
            }
        }
        
        
        
        

        public List<Group> getPackages(Connection con) throws SQLException
        {
            List<Group> packages = new ArrayList<Group>();

            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT OBJECT_NAME FROM USER_OBJECTS WHERE OBJECT_TYPE = 'PACKAGE'");
            try
            {
                while (rset.next())
                {
                    packages.add(new Group(rset.getString("OBJECT_NAME"))
                    {
                        private Object[] subObjects;

                        @Override
                        public Object[] getItems()
                        {
                            if (subObjects != null)
                                return subObjects;
                            final List<Procedure> schemas = new ArrayList<Procedure>();

                            IRunnableWithProgress loadSchemas = new IRunnableWithProgress()
                            {
                                public void run(IProgressMonitor monitor)

                                {
                                    try
                                    {

                                        monitor.beginTask("Loading database packages...", 3);

                                        if (conn != null && !conn.isClosed())
                                        {

                                            monitor.worked(1);
                                            schemas.addAll(getPackagedElements(conn, name));
                                            monitor.worked(1);
                                        }
                                    }
                                    catch (Exception e)
                                    {
                                        dbError = e.getMessage();
                                        e.printStackTrace();

                                    }
                                    finally
                                    {
                                        monitor.done();
                                        doUpdateStatus();
                                    }
                                }
                            };

                            setPageComplete(false);
                            try
                            {
                                getContainer().run(false, false, loadSchemas);
                            }
                            catch (Exception e)
                            {
                                dbError = e.getMessage();
                                e.printStackTrace();
                            }
                            finally
                            {
                                doUpdateStatus();
                            }

                            return subObjects = schemas.toArray();
                        }

                        @Override
                        public Image getImage()
                        {
                            return EJUIImages.SHARED_PACKAGE_IMG;
                        }

                    });
                }

                return packages;
            }
            finally
            {
                rset.close();
                statement.close();
            }
        }

        public ArrayList<Procedure> getProcedures(Connection con) throws SQLException
        {
            ArrayList<Procedure> procedures = new ArrayList<Procedure>();

            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT OBJECT_NAME FROM USER_OBJECTS WHERE OBJECT_TYPE = 'PROCEDURE'");
            try
            {
                while (rset.next())
                {
                    Procedure procedure = getProcedure(con, rset.getString("OBJECT_NAME"));
                    if (procedure != null)
                        procedures.add(procedure);
                }

                return procedures;
            }
            finally
            {
                rset.close();
                statement.close();
            }
        }

        public ArrayList<Procedure> getFunctions(Connection con) throws SQLException
        {
            ArrayList<Procedure> functions = new ArrayList<Procedure>();

            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT OBJECT_NAME FROM USER_OBJECTS WHERE OBJECT_TYPE = 'FUNCTIONS'");
            try
            {
                while (rset.next())
                {
                    Procedure procedure = getProcedure(con, rset.getString("OBJECT_NAME"));
                    if (procedure != null)
                        functions.add(getProcedure(con, rset.getString("OBJECT_NAME")));
                }

                return functions;
            }
            finally
            {
                rset.close();
                statement.close();
            }
        }

        
        
        
        public List<Procedure> getPackagedElements(Connection con, String pkgName) throws SQLException
        {
            List<Procedure> procedures = new ArrayList<Procedure>();
            List<Function> functions = new ArrayList<Function>();
            Statement statement = con.createStatement();

            ResultSet rset = statement
                    .executeQuery("SELECT * FROM user_arguments WHERE (argument_name IS NOT NULL OR (argument_name IS NULL AND position = 0)) AND package_name = '"
                            + pkgName + "' ORDER BY OBJECT_NAME, NVL(OVERLOAD,0), position");
            try
            {
                Procedure proc = null;
                while (rset.next())
                {
                    // int objectId = rset.getInt("OBJECT_ID");
                    int position = rset.getInt("POSITION");
                    String overload = rset.getString("OVERLOAD");
                    String objectName = rset.getString("OBJECT_NAME");
                    String dataType = rset.getString("DATA_TYPE");
                    String inOut = rset.getString("IN_OUT");
                    String typeName = rset.getString("TYPE_NAME");
                    String argName = rset.getString("ARGUMENT_NAME");

                    if (createNew(objectName, overload))
                    {
                        if (position == 0)
                        {
                            Argument returnArg = null;
                            if ("TABLE".equals(dataType))
                            {
                                returnArg = createObjectArgumentFromTableType(con, typeName, argName);
                            }
                            else if ("OBJECT".equals(dataType))
                            {
                                returnArg = createObjectArgument(con, typeName, argName);
                            }
                            else
                            {
                                returnArg = new Argument(argName, dataType,getDataTypeIntForOraType(dataType));
                            }
                            if(argName==null)
                            {
                               
                                returnArg.type = Type.RETURN;
                            }else {
                                returnArg.type = Type.OUT;
                            }
                           
                            
                            proc = new Function(objectName, returnArg);
                            proc.setPackageName(pkgName);
                            functions.add((Function) proc);
                        }
                        else
                        {
                            proc = new Procedure(objectName);
                            proc.setPackageName(pkgName);
                            procedures.add(proc);
                        }

                    }

                    Argument argument;
                    if ("TABLE".equals(dataType))
                    {
                        argument = createObjectArgumentFromTableType(con, typeName, argName);

                    }
                    else if ("OBJECT".equals(dataType))
                    {
                        argument = createObjectArgument(con, typeName, argName);
                    }
                    else
                    {
                        argument = new Argument(argName, dataType,getDataTypeIntForOraType(dataType));
                    }
                    if ("IN/OUT".equals(inOut))
                        argument.type = Type.IN_OUT;
                    else if ("OUT".equals(inOut))
                        argument.type = argName==null?Type.RETURN :Type.OUT;
                    else
                        argument.type = Type.IN;
                    
                    if(argName==null)
                    {
                        
                    }

                    if(proc!=null)
                        proc.addArgument(argument);
                }

                procedures.addAll(functions);
                return procedures;
            }
            finally
            {
                rset.close();
                statement.close();
            }
        }

        public Procedure getProcedure(Connection con, String procedureName) throws SQLException
        {
            Statement statement = con.createStatement();
            ResultSet rset = statement
                    .executeQuery("SELECT * FROM user_arguments WHERE (argument_name IS NOT NULL OR (argument_name IS NULL AND position = 0)) AND PACKAGE_NAME IS NULL AND object_name = '"
                            + procedureName + "' ORDER BY OBJECT_ID, NVL(OVERLOAD,0)");
            try
            {
                Procedure proc = null;
                while (rset.next())
                {
                    // int objectId = rset.getInt("OBJECT_ID");
                    int position = rset.getInt("POSITION");
                    String overload = rset.getString("OVERLOAD");
                    String objectName = rset.getString("OBJECT_NAME");
                    String dataType = rset.getString("DATA_TYPE");
                    String inOut = rset.getString("IN_OUT");
                    String typeName = rset.getString("TYPE_NAME");
                    String argName = rset.getString("ARGUMENT_NAME");

                    if (createNew(objectName, overload))
                    {
                        if (position == 0)
                        {
                            Argument returnArg = null;
                            if ("TABLE".equals(dataType))
                            {
                                returnArg = createObjectArgumentFromTableType(con, typeName, argName);
                            }
                            else if ("OBJECT".equals(dataType))
                            {
                                returnArg = createObjectArgument(con, typeName, argName);
                            }
                            else
                            {
                                returnArg = new Argument(argName, dataType,getDataTypeIntForOraType(dataType));
                                returnArg.type = Type.OUT;
                            }
                            proc = new Function(objectName, returnArg);
                        }
                        else
                        {
                            proc = new Procedure(objectName);
                        }
                    }

                    Argument argument;
                    if ("TABLE".equals(dataType))
                    {
                        argument = createObjectArgumentFromTableType(con, typeName, argName);

                    }
                    else if ("OBJECT".equals(dataType))
                    {
                        argument = createObjectArgument(con, typeName, argName);
                    }
                    else
                    {
                        argument = new Argument(argName, dataType,getDataTypeIntForOraType(dataType));
                    }
                    if ("IN/OUT".equals(inOut))
                        argument.type = Type.IN_OUT;
                    else if ("OUT".equals(inOut))
                        argument.type = Type.OUT;
                    else if ("RETURN".equals(inOut))
                        argument.type = Type.RETURN;
                    else
                        argument.type = Type.IN;

                    proc.addArgument(argument);
                }

                return proc;
            }
            finally
            {
                rset.close();
                statement.close();
            }
        }

        public ObjectArgument createObjectArgumentFromTableType(Connection con, String TableType, String argName) throws SQLException
        {
            
            ObjectArgument tab = new ObjectArgument(TableType, TableType, argName, "TABLE",Types.ARRAY);
            
            
            
            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT ELEM_TYPE_NAME FROM USER_COLL_TYPES WHERE TYPE_NAME = '" + TableType + "'");
            try
            {
                String objectType = null;
             
                while (rset.next())
                {
                    objectType = rset.getString("ELEM_TYPE_NAME");
                    break;
                }

                if (objectType != null)
                {
                    tab.addArgument(createObjectArgument(con, null, objectType, objectType));
                }
            }
            finally
            {
                rset.close();
                statement.close();
            }

            return tab;
        }

        public ObjectArgument createObjectArgument(Connection con, String objectName, String argName) throws SQLException
        {
            return createObjectArgument(con, null, objectName, argName);
        }

        public ObjectArgument createObjectArgument(Connection con, String tableName, String objectName, String argName) throws SQLException
        {
            ObjectArgument argument = new ObjectArgument(tableName, objectName, argName, "OBJECT",Types.STRUCT);
            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT * FROM USER_TYPE_ATTRS WHERE TYPE_NAME = '" + objectName + "' ORDER BY ATTR_NO");
            try
            {
                while (rset.next())
                {
                    String attrName = rset.getString("ATTR_NAME");
                    String attrTypeName = rset.getString("ATTR_TYPE_NAME");

                    argument.addArgument(createAttributeArgument(con, attrName, attrTypeName));
                }

                return argument;
            }
            finally
            {
                rset.close();
                statement.close();
            }
        }

        public Argument createAttributeArgument(Connection con, String attrName, String attrTypeName) throws SQLException
        {

            Statement statement = con.createStatement();
            ResultSet rset = statement.executeQuery("SELECT TYPECODE FROM ALL_TYPES WHERE TYPE_NAME = '" + attrTypeName + "' ");
            String type = null;
            try
            {
                while (rset.next())
                {
                    type = rset.getString("TYPECODE");
                    break;
                }
            }
            finally
            {
                rset.close();
                statement.close();
            }
            if ("OBJECT".equals(type))
            {
                return createObjectArgument(con, attrTypeName, attrName);
            }
            if ("COLLECTION".equals(type))
            {
                return createObjectArgumentFromTableType(con, attrTypeName, attrName);
            }
            return new Argument(attrName, attrTypeName,getDataTypeIntForOraType(attrTypeName));
        }

        private String prevObjectName = null;
        private String prevOverride   = null;

        // If you override a procedure in a package then it will have the same
        // name. The only difference is the Override flag. Each override has its
        // own
        // number. So if the names are the same and the override flags are
        // different, then we need to create a new type because we are working
        // with
        // a different type
        public boolean createNew(String objectName, String override)
        {
            try
            {
                if (prevObjectName == null)
                {
                    return true;
                }

                if (!prevObjectName.equals(objectName))
                {
                    return true;
                }
                else
                {
                    if (prevOverride == null && override == null)
                    {
                        return false;
                    }

                    if (prevOverride == null && override != null)
                    {
                        return true;
                    }

                    if (prevOverride != null && override == null)
                    {
                        return true;
                    }

                    if (!prevOverride.equals(override))
                    {
                        return true;
                    }

                    return false;
                }
            }
            finally
            {
                prevObjectName = objectName;
                prevOverride = override;
            }
        }

        public Object[] getElements(Object inputElement)
        {
            if (objects != null && checkForTypes == (context!=null && context.skipService()))
                return objects;
            checkForTypes = context!=null && context.skipService();
            if(checkForTypes)
            {
                Group packages = new Group("Types")
                {
                    private Object[] subObjects;

                    @Override
                    public Object[] getItems()
                    {
                        if (subObjects != null)
                            return filterItem(subObjects);
                        final List<ObjectArgument> schemas = new ArrayList<ObjectArgument>();

                        IRunnableWithProgress loadSchemas = new IRunnableWithProgress()
                        {
                            public void run(IProgressMonitor monitor)

                            {
                                try
                                {

                                    monitor.beginTask("Loading database types...", 3);

                                    if (conn != null && !conn.isClosed())
                                    {

                                        monitor.worked(1);
                                        schemas.addAll(getTypes(conn));
                                        monitor.worked(1);
                                    }
                                }
                                catch (Exception e)
                                {
                                    dbError = e.getMessage();

                                }
                                finally
                                {
                                    monitor.done();
                                    doUpdateStatus();
                                }
                            }
                        };

                        setPageComplete(false);
                        try
                        {
                            getContainer().run(false, false, loadSchemas);
                        }
                        catch (Exception e)
                        {
                            dbError = e.getMessage();

                        }
                        finally
                        {
                            doUpdateStatus();
                        }

                        return filterItem(subObjects = schemas.toArray());
                    }

                    @Override
                    public Image getImage()
                    {
                        return EJUIImages.SHARED_CLASS;
                    }
                };
                
                return objects = new Object[] { packages };
            }
            
            Group packages = new Group("Packages")
            {
                private Object[] subObjects;

                @Override
                public Object[] getItems()
                {
                    if (subObjects != null)
                        return filterItem(subObjects);
                    final List<Group> schemas = new ArrayList<Group>();

                    IRunnableWithProgress loadSchemas = new IRunnableWithProgress()
                    {
                        public void run(IProgressMonitor monitor)

                        {
                            try
                            {

                                monitor.beginTask("Loading database packages...", 3);

                                if (conn != null && !conn.isClosed())
                                {

                                    monitor.worked(1);
                                    schemas.addAll(getPackages(conn));
                                    monitor.worked(1);
                                }
                            }
                            catch (Exception e)
                            {
                                dbError = e.getMessage();

                            }
                            finally
                            {
                                monitor.done();
                                doUpdateStatus();
                            }
                        }
                    };

                    setPageComplete(false);
                    try
                    {
                        getContainer().run(false, false, loadSchemas);
                    }
                    catch (Exception e)
                    {
                        dbError = e.getMessage();

                    }
                    finally
                    {
                        doUpdateStatus();
                    }

                    return filterItem(subObjects = schemas.toArray());
                }

                @Override
                public Image getImage()
                {
                    return EJUIImages.SHARED_PACKFRAG_ROOT_IMG;
                }
            };

            Group procedures = new Group("Procedures")
            {
                private Object[] subObjects;

                @Override
                public Object[] getItems()
                {
                    if (subObjects != null)
                        return filterItem(subObjects);
                    final List<Procedure> schemas = new ArrayList<Procedure>();

                    IRunnableWithProgress loadSchemas = new IRunnableWithProgress()
                    {
                        public void run(IProgressMonitor monitor)

                        {
                            try
                            {

                                monitor.beginTask("Loading database procedures...", 3);

                                if (conn != null && !conn.isClosed())
                                {

                                    monitor.worked(1);
                                    schemas.addAll(getProcedures(conn));
                                    monitor.worked(1);
                                }
                            }
                            catch (Exception e)
                            {
                                dbError = e.getMessage();

                            }
                            finally
                            {
                                monitor.done();
                                doUpdateStatus();
                            }
                        }
                    };

                    setPageComplete(false);
                    try
                    {
                        getContainer().run(false, false, loadSchemas);
                    }
                    catch (Exception e)
                    {
                        dbError = e.getMessage();

                    }
                    finally
                    {
                        doUpdateStatus();
                    }

                    return filterItem(subObjects = schemas.toArray());
                }

                @Override
                public Image getImage()
                {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                }
            };
            Group functions = new Group("Functions")
            {
                private Object[] subObjects;

                @Override
                public Object[] getItems()
                {
                    if (subObjects != null)
                        return filterItem(subObjects);
                    final List<Procedure> schemas = new ArrayList<Procedure>();

                    IRunnableWithProgress loadSchemas = new IRunnableWithProgress()
                    {
                        public void run(IProgressMonitor monitor)

                        {
                            try
                            {

                                monitor.beginTask("Loading database functions...", 3);

                                if (conn != null && !conn.isClosed())
                                {

                                    monitor.worked(1);
                                    schemas.addAll(getFunctions(conn));
                                    monitor.worked(1);
                                }
                            }
                            catch (Exception e)
                            {
                                dbError = e.getMessage();

                            }
                            finally
                            {
                                monitor.done();
                                doUpdateStatus();
                            }
                        }
                    };

                    setPageComplete(false);
                    try
                    {
                        getContainer().run(false, false, loadSchemas);
                    }
                    catch (Exception e)
                    {
                        dbError = e.getMessage();

                    }
                    finally
                    {
                        doUpdateStatus();
                    }

                    return filterItem(subObjects = schemas.toArray());
                }

                @Override
                public Image getImage()
                {
                    return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FOLDER);
                }
            };

            return objects = new Object[] { packages, procedures, functions };

        }

        Object[] filterItem(Object[] items)
        {
            if (filter != null && filter.trim().length() > 0)
            {

                List<Object> fitems = new ArrayList<Object>();

                for (Object object : items)
                {
                    if (object instanceof Group)
                    {
                        if (((Group) object).name.toLowerCase().contains(filter.toLowerCase()))
                        {
                            fitems.add(object);
                        }
                        continue;
                    }
                    if (object instanceof Procedure)
                    {
                        if (((Procedure) object).getName().toLowerCase().contains(filter.toLowerCase()))
                        {
                            fitems.add(object);
                        }
                        continue;
                    }
                    if (object instanceof Procedure)
                    {
                        if (((Procedure) object).getName().toLowerCase().contains(filter.toLowerCase()))
                        {
                            fitems.add(object);
                        }
                        continue;
                    }
                    if (context.skipService() && object instanceof ObjectArgument)
                    {
                        if (((ObjectArgument) object).getObjName().toLowerCase().contains(filter.toLowerCase()))
                        {
                            fitems.add(object);
                        }
                        continue;
                    }
                }

                return fitems.toArray();

            }

            return items;
        }

        public Object[] getChildren(Object parentElement)
        {

            if (parentElement instanceof Group)
            {
                return ((Group) parentElement).getItems();
            }
            if (parentElement instanceof Procedure)
            {
                return ((Procedure) parentElement).getArguments().toArray();
            }
            if (parentElement instanceof ObjectArgument)
            {
                return ((ObjectArgument) parentElement).getArguments().toArray();
            }
            return new Object[] {};
        }

        public Object getParent(Object element)
        {

            return null;
        }

        public boolean hasChildren(Object element)
        {

            if (element instanceof Group)
            {
                return true;
            }
            if (element instanceof Procedure)
            {
                return true;
            }
            if (element instanceof ObjectArgument)
            {
                return true;
            }
            return false;
        }

    }

    public Procedure getProcedure()
    {
        return selectedProcedure;
    }
}
