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

import java.math.BigDecimal;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Struct;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.wizard.IWizardPage;
import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJPojoGeneratorType;
import org.entirej.framework.core.service.EJServiceGeneratorType;
import org.entirej.framework.core.service.EJTableColumn;
import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportPojoGeneratorType;
import org.entirej.framework.report.service.EJReportServiceGeneratorType;
import org.entirej.framework.report.service.EJReportTableColumn;
import org.entirej.ide.core.EJCoreLog;
import org.entirej.ide.core.spi.BlockServiceContentProvider;

public class OraTypeBlockServiceContentProvider implements BlockServiceContentProvider
{

    public OraTypeBlockServiceContentProvider()
    {
    }

    public String getProviderName()
    {
        return "Oracle Collection Type";
    }

    public String getProviderId()
    {
        return "org.entirej.ora_type.block.service.content";
    }

    public String getDescription()
    {
        return "Creates a block pojo/service using the Oracle collection types.";
    }

    public BlockServiceWizardProvider createWizardProvider()
    {
        return new BlockServiceWizardProvider()
        {

            
            public String getPojoSuggest()
            {
                String name = "";
                
                Procedure procedure = columnSelectionPage.getProcedure();
                if (procedure != null)
                {
                    ObjectArgument collectionType = procedure.getCollectionType();

                    if (collectionType != null)
                    {
                     

                        if (collectionType.tableName != null)
                        {

                            if (collectionType.getArguments().size() > 0)
                            {
                                name =  toCamelCase(((ObjectArgument) collectionType.getArguments().get(0)).objName);
                            }
                        }
                        else
                        {
                            name =  toCamelCase(collectionType.objName);
                        }
                    }
                }
                
                
                return name;
            }
            
            public String getServiceSuggest()
            {
                String name = "";
                
                Procedure procedure = columnSelectionPage.getProcedure();
                if (procedure != null)
                {
                       name = toCamelCase(procedure.getFullName().replaceAll("\\.", "_").toUpperCase()).trim()+"Service" ;
                }
                return name;
            }
            
            public String getPogoGenerator()
            {
                return "org.entirej.EJFormOraclePojoGenerator";
            }

            public String getServiceGenerator()
            {
                return "org.entirej.OracleCollectionTypeServiceGenerator";
            }
            public String getReportPogoGenerator()
            {
                return "org.entirej.EJFormOraclePojoGenerator";
            }
            
            public String getReportServiceGenerator()
            {
                return "org.entirej.OracleCollectionTypeServiceGenerator";
            }

            public boolean skipMainPojo()
            {
                return true;
            }

            private DBTypeSelectionPage columnSelectionPage = new DBTypeSelectionPage();
            private DBInnerTypePage innerTypePage;
            private DBProceduresWizardPage proceduresWizardPage = new DBProceduresWizardPage(columnSelectionPage);

            private GeneratorContext context;
            private ReportGeneratorContext rcontext;
            private Map<String, String> innerClass = new LinkedHashMap<String, String>();

            public void init(final GeneratorContext context)
            {
                columnSelectionPage.init(context.getProject());
                this.context = context;
                innerTypePage = new DBInnerTypePage()
                {
                    @Override
                    public IJavaProject getProject()
                    {
                        return context.getProject();
                    }

                };
                innerClass.clear();
            }

            public void init(final ReportGeneratorContext context)
            {
                columnSelectionPage.init(context.getProject());
                this.rcontext = context;
                innerTypePage = new DBInnerTypePage()
                {
                    @Override
                    public IJavaProject getProject()
                    {
                        return context.getProject();
                    }
                };
                innerClass.clear();
            }

            public boolean canFinish(IWizardPage page)
            {
                return page.isPageComplete();
            }

            public boolean skipPage(IWizardPage page)
            {
                if (page == innerTypePage)
                {
                    innerTypePage.init(columnSelectionPage.getProcedure());
                    return innerTypePage.skipPage();
                }
                return false;
            }

            public List<IWizardPage> getPages()
            {

                return Arrays.<IWizardPage> asList(columnSelectionPage, innerTypePage);
            }

            public List<IWizardPage> getOptionalPages()
            {
                if ((context != null && context.skipService()) || (rcontext != null && rcontext.skipService()))
                    return Arrays.<IWizardPage> asList();

                return Arrays.<IWizardPage> asList(proceduresWizardPage);
            }

            public BlockServiceContent getContent()
            {

                return createBlockServiceContent();
            }

            public ReportBlockServiceContent getReportContent()
            {

                return createReportBlockServiceContent();
            }

            private BlockServiceContent createBlockServiceContent()
            {
                if (!columnSelectionPage.isPageComplete())
                {
                    return null;
                }

                EJServiceGeneratorType serviceGeneratorType = new EJServiceGeneratorType();

                EJPojoGeneratorType pojoGeneratorType = new EJPojoGeneratorType();
                Procedure procedure = columnSelectionPage.getProcedure();
                if (procedure != null)
                {
                    serviceGeneratorType.setSelectProcedureName(procedure.getFullName());

                    serviceGeneratorType.setSelectProcedureParameters(getParamters(procedure));

                    // get type details
                    ObjectArgument collectionType = procedure.getCollectionType();

                    if (collectionType != null)
                    {
                        if (collectionType.tableName != null)
                            pojoGeneratorType.setProperty("TABLE_NAME", collectionType.tableName);
                        if (collectionType.objName != null)
                        {
                            pojoGeneratorType.setProperty("DB_OBJECT_NAME", collectionType.objName);
                            pojoGeneratorType.setProperty("JAVA_OBJECT_NAME", toCamelCase(collectionType.objName));
                            serviceGeneratorType.setProperty("JAVA_OBJECT_NAME", toCamelCase(collectionType.objName));

                            if (collectionType.tableName != null)
                            {

                                if (collectionType.getArguments().size() > 0)
                                {
                                    serviceGeneratorType.setProperty("JAVA_REC_NAME",
                                            toCamelCase(((ObjectArgument) collectionType.getArguments().get(0)).objName));
                                }
                            }
                            else
                            {
                                serviceGeneratorType.setProperty("JAVA_REC_NAME", toCamelCase(collectionType.objName));
                            }
                        }
                        pojoGeneratorType.setColumnNames(createPojoCloumns(collectionType));
                        serviceGeneratorType.setTableName(collectionType.getTableName());
                    }

                    if (proceduresWizardPage.getInsertProcedure() != null)
                    {
                        serviceGeneratorType.setInsertProcedureName(proceduresWizardPage.getInsertProcedure().getFullName());
                        serviceGeneratorType.setInsertProcedureParameters(getParamters(proceduresWizardPage.getInsertProcedure()));
                    }
                    if (proceduresWizardPage.getUpdateProcedure() != null)
                    {
                        serviceGeneratorType.setUpdateProcedureName(proceduresWizardPage.getUpdateProcedure().getFullName());
                        serviceGeneratorType.setUpdateProcedureParameters(getParamters(proceduresWizardPage.getUpdateProcedure()));
                    }
                    if (proceduresWizardPage.getDeleteProcedure() != null)
                    {
                        serviceGeneratorType.setDeleteProcedureName(proceduresWizardPage.getDeleteProcedure().getFullName());
                        serviceGeneratorType.setDeleteProcedureParameters(getParamters(proceduresWizardPage.getDeleteProcedure()));
                    }
                }
                return new BlockServiceContent(serviceGeneratorType, pojoGeneratorType);
            }

            
            
            
            
            String toCamelCase(String s)
            {
                String[] parts = s.split("_");
                String camelCaseString = "";
                for (String part : parts)
                {
                    camelCaseString = camelCaseString + toProperCase(part);
                }
                return camelCaseString;
            }

            String toProperCase(String s)
            {
                return s.substring(0, 1).toUpperCase() + s.substring(1).toLowerCase();
            }

            private ReportBlockServiceContent createReportBlockServiceContent()
            {
                if (!columnSelectionPage.isPageComplete())
                {
                    return null;
                }

                EJReportServiceGeneratorType serviceGeneratorType = new EJReportServiceGeneratorType();

                EJReportPojoGeneratorType pojoGeneratorType = new EJReportPojoGeneratorType();
                Procedure procedure = columnSelectionPage.getProcedure();
                if (procedure != null)
                {
                    serviceGeneratorType.setSelectProcedureName(procedure.getFullName());

                    serviceGeneratorType.setSelectProcedureParameters(getReportParamters(procedure));

                    // get type details
                    ObjectArgument collectionType = procedure.getCollectionType();

                    if (collectionType != null)
                    {
                        if (collectionType.tableName != null)
                            pojoGeneratorType.setProperty("TABLE_NAME", collectionType.tableName);
                        if (collectionType.objName != null)
                            pojoGeneratorType.setProperty("OBJECT_NAME", collectionType.objName);
                        pojoGeneratorType.setColumnNames(createReportPojoCloumns(collectionType));
                        serviceGeneratorType.setTableName(collectionType.getTableName());

                        pojoGeneratorType.setProperty("DB_OBJECT_NAME", collectionType.objName);
                        pojoGeneratorType.setProperty("JAVA_OBJECT_NAME", toCamelCase(collectionType.objName));
                        serviceGeneratorType.setProperty("JAVA_OBJECT_NAME", toCamelCase(collectionType.objName));

                        if (collectionType.tableName != null)
                        {

                            if (collectionType.getArguments().size() > 0)
                            {
                                serviceGeneratorType.setProperty("JAVA_REC_NAME", toCamelCase(((ObjectArgument) collectionType.getArguments().get(0)).objName));
                            }
                        }
                        else
                        {
                            serviceGeneratorType.setProperty("JAVA_REC_NAME", toCamelCase(collectionType.objName));
                        }
                    }

                }
                return new ReportBlockServiceContent(serviceGeneratorType, pojoGeneratorType);
            }

            private List<EJTableColumn> createPojoCloumns(ObjectArgument collectionType)
            {
                List<EJTableColumn> columns = new ArrayList<EJTableColumn>();
                for (Argument argument : collectionType.getArguments())
                {
                    EJTableColumn tableColumn = new EJTableColumn();
                    tableColumn.setName(argument._name);

                    tableColumn.setProperty("JAVA_OBJECT_TYPE", "" + argument.getDatatypeInt());

                    if (argument instanceof ObjectArgument)
                    {
                        ObjectArgument objectArgument = (ObjectArgument) argument;
                        if (objectArgument.tableName != null)
                            tableColumn.setProperty("TABLE_NAME", objectArgument.tableName);
                        if (objectArgument.objName != null)
                            tableColumn.setProperty("DB_OBJECT_NAME", objectArgument.objName);

                        if (objectArgument.tableName != null)
                        {
                            tableColumn.setArray(true);
                        }
                        if (objectArgument.objName != null)
                        {
                            String type = innerClass.get(objectArgument.objName);
                            if (type == null)
                            {
                                tableColumn.setDatatypeName(toCamelCase(objectArgument.objName));
                            }
                            else
                            {
                                tableColumn.setDatatypeName(type);
                            }

                        }
                    }

                    if (tableColumn.getDatatypeName() == null)
                    {
                        Class<?> type = getDataTypeForOraType(argument._datatype);
                        if (type != null)
                        {
                            tableColumn.setDatatypeName(type.getName());
                            type = null;
                        }
                        else
                        {
                            tableColumn.setDatatypeName(String.class.getName());
                        }

                    }
                    if (!tableColumn.isArray())
                    {
                        tableColumn.setStruct(isStructForOraType(argument._datatype));
                    }
                    columns.add(tableColumn);
                }
                return columns;
            }

            private List<EJReportTableColumn> createReportPojoCloumns(ObjectArgument collectionType)
            {
                List<EJReportTableColumn> columns = new ArrayList<EJReportTableColumn>();
                for (Argument argument : collectionType.getArguments())
                {
                    EJReportTableColumn tableColumn = new EJReportTableColumn();
                    tableColumn.setName(argument._name);

                    if (argument instanceof ObjectArgument)
                    {
                        ObjectArgument objectArgument = (ObjectArgument) argument;
                        if (objectArgument.tableName != null)
                            tableColumn.setProperty("TABLE_NAME", objectArgument.tableName);
                        if (objectArgument.objName != null)
                            tableColumn.setProperty("OBJECT_NAME", objectArgument.objName);
                        if (objectArgument.tableName != null)
                        {
                            tableColumn.setArray(true);
                        }
                        if (objectArgument.objName != null)
                        {
                            String type = innerClass.get(objectArgument.objName);
                            if (type == null)
                            {
                                tableColumn.setDatatypeName(toCamelCase(objectArgument.objName));
                            }
                            else
                            {
                                tableColumn.setDatatypeName(type);
                            }
                        }
                    }

                    if (tableColumn.getDatatypeName() == null)
                    {
                        Class<?> type = getDataTypeForOraType(argument._datatype);
                        if (type != null)
                        {
                            tableColumn.setDatatypeName(type.getName());
                            type = null;
                        }
                        else
                        {
                            tableColumn.setDatatypeName(String.class.getName());
                        }

                    }
                    if (!tableColumn.isArray())
                    {
                        tableColumn.setStruct(isStructForOraType(argument._datatype));
                    }
                    columns.add(tableColumn);
                }
                return columns;
            }

            private List<EJTableColumn> getParamters(Procedure procedure)
            {
                List<EJTableColumn> args = new ArrayList<EJTableColumn>();

                for (Argument argument : procedure.getArguments())
                {
                    EJTableColumn tableColumn = createColumn(argument);

                    args.add(tableColumn);
                }
                // reverse args order.
                Collections.reverse(args);
                return args;
            }

            private EJTableColumn createColumn(Argument argument)
            {
                EJTableColumn tableColumn = new EJTableColumn();
                tableColumn.setName(argument._name);

                if (argument instanceof ObjectArgument)
                {
                    ObjectArgument objectArgument = (ObjectArgument) argument;
                    if (objectArgument.tableName != null)
                        tableColumn.setProperty("TABLE_NAME", objectArgument.tableName);
                    if (objectArgument.objName != null)
                        tableColumn.setProperty("OBJECT_NAME", objectArgument.objName);
                    if (objectArgument.tableName != null)
                    {
                        tableColumn.setArray(true);
                    }
                    if (objectArgument.objName != null)
                    {
                        String type = innerClass.get(objectArgument.objName);
                        if (type == null)
                        {
                            tableColumn.setDatatypeName(toCamelCase(objectArgument.objName));
                        }
                        else
                        {
                            tableColumn.setDatatypeName(type);
                        }
                    }
                }

                if (tableColumn.getDatatypeName() == null)
                {
                    Class<?> type = getDataTypeForOraType(argument._datatype);
                    if (type != null)
                    {
                        tableColumn.setDatatypeName(type.getName());
                        type = null;
                    }
                    else
                    {
                        tableColumn.setDatatypeName(String.class.getName());
                    }
                }

                if (!tableColumn.isArray())
                {
                    tableColumn.setStruct(isStructForOraType(argument._datatype));
                }

                switch (argument.type)
                {
                    case IN:
                        tableColumn.setParameterType(EJParameterType.IN);
                        break;
                    case IN_OUT:
                        tableColumn.setParameterType(EJParameterType.INOUT);
                        break;
                    case OUT:
                        tableColumn.setParameterType(EJParameterType.OUT);
                        break;
                    case RETURN:
                        tableColumn.setParameterType(EJParameterType.RETURN);
                        break;
                }
                return tableColumn;
            }

            private List<EJReportTableColumn> getReportParamters(Procedure procedure)
            {
                List<EJReportTableColumn> args = new ArrayList<EJReportTableColumn>();

                for (Argument argument : procedure.getArguments())
                {
                    EJReportTableColumn tableColumn = createReportColumn(argument);

                    args.add(tableColumn);
                }
                // reverse args order.
                Collections.reverse(args);
                return args;
            }

            private EJReportTableColumn createReportColumn(Argument argument)
            {
                EJReportTableColumn tableColumn = new EJReportTableColumn();
                tableColumn.setName(argument._name);

                if (argument instanceof ObjectArgument)
                {
                    ObjectArgument objectArgument = (ObjectArgument) argument;
                    if (objectArgument.tableName != null)
                        tableColumn.setProperty("TABLE_NAME", objectArgument.tableName);
                    if (objectArgument.objName != null)
                        tableColumn.setProperty("OBJECT_NAME", objectArgument.objName);
                    if (objectArgument.tableName != null)
                    {
                        tableColumn.setArray(true);
                    }
                    if (objectArgument.objName != null)
                    {
                        String type = innerClass.get(objectArgument.objName);
                        if (type == null)
                        {
                            tableColumn.setDatatypeName(toCamelCase(objectArgument.objName));
                        }
                        else
                        {
                            tableColumn.setDatatypeName(type);
                        }
                    }
                }

                if (tableColumn.getDatatypeName() == null)
                {
                    Class<?> type = getDataTypeForOraType(argument._datatype);
                    if (type != null)
                    {
                        tableColumn.setDatatypeName(type.getName());
                        type = null;
                    }
                    else
                    {
                        tableColumn.setDatatypeName(String.class.getName());
                    }
                }
                if (!tableColumn.isArray())
                {
                    tableColumn.setStruct(isStructForOraType(argument._datatype));
                }

                switch (argument.type)
                {
                    case IN:
                        tableColumn.setParameterType(EJReportParameterType.IN);
                        break;
                    case IN_OUT:
                        tableColumn.setParameterType(EJReportParameterType.INOUT);
                        break;
                    case OUT:
                        tableColumn.setParameterType(EJReportParameterType.OUT);
                        break;
                    case RETURN:
                        tableColumn.setParameterType(EJReportParameterType.RETURN);
                        break;
                }
                return tableColumn;
            }

            public Class<?> getDataTypeForOraType(String jdbcType)
            {
                // BINARY_INTEGER, NATURAL, NATURALN, PLS_INTEGER, POSITIVE,
                // POSITIVEN, SIGNTYPE, INT, INTEGER
                if ("BINARY_INTEGER".equals(jdbcType) || "NATURAL".equals(jdbcType) || "NATURALN".equals(jdbcType) || "PLS_INTEGER".equals(jdbcType)
                        || "POSITIVE".equals(jdbcType) || "SIGNTYPE".equals(jdbcType) || "INTEGER".equals(jdbcType) || "INT".equals(jdbcType)
                        || "POSITIVEN".equals(jdbcType) || "SMALLINT".equals(jdbcType))
                    return Integer.class;

                // DEC, DECIMAL, NUMBER, NUMERIC
                if ("DEC".equals(jdbcType) || "DECIMAL".equals(jdbcType) || "NUMBER".equals(jdbcType) || "NUMERIC".equals(jdbcType))
                    return BigDecimal.class;

                // DOUBLE PRECISION, FLOAT
                if ("DOUBLE".equals(jdbcType) || "PRECISION".equals(jdbcType) || "FLOAT".equals(jdbcType))
                    return Double.class;
                // DOUBLE PRECISION, FLOAT
                if ("DATE".equals(jdbcType) || "TIMESTAMP".equals(jdbcType))
                    return Timestamp.class;
                ;

                if ("REAL".equals(jdbcType))
                    return Float.class;
                if ("BOOLEAN".equals(jdbcType))
                    return Boolean.class;
                if ("OBJECT".equals(jdbcType))
                    return Struct.class;
                if ("TABLE".equals(jdbcType))
                    return Struct.class;
                if ("CLOB".equals(jdbcType))
                    return Clob.class;
                if ("BLOB".equals(jdbcType))
                    return Blob.class;
                if ("STRUCT".equals(jdbcType))
                    return Struct.class;

                return String.class;
            }

            public boolean isStructForOraType(String jdbcType)
            {

                return jdbcType.equals("STRUCT") || jdbcType.equals("OBJECT");
            }

            public void createRequiredResources(IProgressMonitor monitor)
            {
                if (context != null)
                    createFormResources(monitor);
                else if (rcontext != null)
                    createReportResources(monitor);
            }

            private void createFormResources(IProgressMonitor monitor)
            {
                List<String> addedInner = new ArrayList<String>();

                Procedure procedure = columnSelectionPage.getProcedure();

                ObjectArgument collectionType = procedure.getCollectionType();

                if (collectionType != null)
                {
                    for (Argument argument : collectionType.getArguments())
                    {
                        List<EJPojoGeneratorType> innerPojoGeneratorTypes = new ArrayList<EJPojoGeneratorType>();

                        if (argument instanceof ObjectArgument)
                        {
                            ObjectArgument objectArgument = (ObjectArgument) argument;

                            collectTypes(objectArgument, addedInner, innerPojoGeneratorTypes);
                        }

                        Collections.reverse(innerPojoGeneratorTypes);
                        for (EJPojoGeneratorType inner : innerPojoGeneratorTypes)
                        {
                            String objName = inner.getClassName();

                            inner.setClassName(objName);
                            try
                            {
                                reValidateDataTypes(inner);
                                String clazz = context.createPojoClass(inner, monitor);
                                innerClass.put(objName, clazz);
                            }
                            catch (Exception e)
                            {

                                e.printStackTrace();
                            }
                        }
                    }

                }

                for (Argument argument : procedure.getArguments())
                {
                    List<EJPojoGeneratorType> innerPojoGeneratorTypes = new ArrayList<EJPojoGeneratorType>();
                    EJReportTableColumn tableColumn = new EJReportTableColumn();
                    tableColumn.setName(argument._name);

                    if (argument instanceof ObjectArgument)
                    {

                        if (argument instanceof ObjectArgument)
                        {
                            ObjectArgument objectArgument = (ObjectArgument) argument;

                            collectTypes(objectArgument, addedInner, innerPojoGeneratorTypes);
                        }
                    }
                    Collections.reverse(innerPojoGeneratorTypes);
                    for (EJPojoGeneratorType inner : innerPojoGeneratorTypes)
                    {
                        String objName = inner.getClassName();

                        inner.setClassName(objName);
                        try
                        {
                            reValidateDataTypes(inner);
                            String clazz = context.createPojoClass(inner, monitor);
                            innerClass.put(objName, clazz);
                        }
                        catch (Exception e)
                        {

                            e.printStackTrace();
                        }
                    }
                }

            }

            void reValidateDataTypes(EJPojoGeneratorType inner)
            {
                Collection<EJTableColumn> columns = inner.getColumns();
                for (EJTableColumn ejReportTableColumn : columns)
                {
                    if (ejReportTableColumn.getProperty("OBJECT_NAME") != null)
                    {

                        String type = innerClass.get(ejReportTableColumn.getProperty("OBJECT_NAME"));
                        if (type != null)
                        {
                            ejReportTableColumn.setDatatypeName(type);
                        }
                        else
                        {
                            ejReportTableColumn.setDatatypeName(ejReportTableColumn.getProperty("OBJECT_NAME"));
                        }
                    }
                }
            }

            void reValidateReportDataTypes(EJReportPojoGeneratorType inner)
            {
                Collection<EJReportTableColumn> columns = inner.getColumns();
                for (EJReportTableColumn ejReportTableColumn : columns)
                {
                    if (ejReportTableColumn.getProperty("OBJECT_NAME") != null)
                    {

                        String type = innerClass.get(ejReportTableColumn.getProperty("OBJECT_NAME"));
                        if (type != null)
                        {
                            ejReportTableColumn.setDatatypeName(type);
                        }
                        else
                        {
                            ejReportTableColumn.setDatatypeName(ejReportTableColumn.getProperty("OBJECT_NAME"));
                        }
                    }
                }
            }

            private void collectReportTypes(ObjectArgument objectArgument, List<String> addedInner, List<EJReportPojoGeneratorType> innerPojoGeneratorTypes)
            {

                String mappedClass = innerTypePage.getMappedClass(objectArgument);
                if (mappedClass != null)
                {
                    innerClass.put(objectArgument.objName, mappedClass);
                    return;
                }

                if (objectArgument.objName != null && !addedInner.contains(objectArgument.objName))
                {
                    EJReportPojoGeneratorType inner = new EJReportPojoGeneratorType();
                    if (objectArgument.tableName != null)
                        inner.setProperty("TABLE_NAME", objectArgument.tableName);
                    if (objectArgument.objName != null)
                        inner.setProperty("OBJECT_NAME", objectArgument.objName);
                    inner.setColumnNames(createReportPojoCloumns(objectArgument));
                    inner.setClassName(objectArgument.objName);
                    addedInner.add(objectArgument.objName);
                    innerPojoGeneratorTypes.add(inner);
                }

                List<Argument> arguments = objectArgument.getArguments();
                for (Argument inner : arguments)
                {
                    if (inner instanceof ObjectArgument)
                    {
                        collectReportTypes((ObjectArgument) inner, addedInner, innerPojoGeneratorTypes);
                    }
                }
            }

            private void collectTypes(ObjectArgument objectArgument, List<String> addedInner, List<EJPojoGeneratorType> innerPojoGeneratorTypes)
            {

                String mappedClass = innerTypePage.getMappedClass(objectArgument);
                if (mappedClass != null)
                {
                    innerClass.put(objectArgument.objName, mappedClass);
                    return;
                }

                if (objectArgument.objName != null && !addedInner.contains(objectArgument.objName))
                {
                    EJPojoGeneratorType inner = new EJPojoGeneratorType();
                    inner.setProperty("TABLE_NAME", "");
                    if (objectArgument.tableName != null)
                    {
                        inner.setProperty("TABLE_NAME", objectArgument.tableName);
                        if (objectArgument.getArguments().size() > 0)
                        {
                            inner.setProperty("JAVA_REC_NAME", toCamelCase(((ObjectArgument) objectArgument.getArguments().get(0)).objName));
                        }
                    }
                    if (objectArgument.objName != null)
                        inner.setProperty("DB_OBJECT_NAME", objectArgument.objName);
                    if (objectArgument.objName != null)
                        inner.setProperty("JAVA_OBJECT_NAME", toCamelCase(objectArgument.objName));
                    inner.setColumnNames(createPojoCloumns(objectArgument));

                    inner.setClassName(toCamelCase(objectArgument.objName));
                    addedInner.add(objectArgument.objName);
                    innerPojoGeneratorTypes.add(inner);
                }

                List<Argument> arguments = objectArgument.getArguments();
                for (Argument inner : arguments)
                {
                    if (inner instanceof ObjectArgument)
                    {
                        collectTypes((ObjectArgument) inner, addedInner, innerPojoGeneratorTypes);
                    }
                }
            }

            private void createReportResources(IProgressMonitor monitor)
            {
                List<String> addedInner = new ArrayList<String>();

                Procedure procedure = columnSelectionPage.getProcedure();

                ObjectArgument collectionType = procedure.getCollectionType();
                if (collectionType != null)
                {
                    for (Argument argument : collectionType.getArguments())
                    {
                        List<EJReportPojoGeneratorType> innerPojoGeneratorTypes = new ArrayList<EJReportPojoGeneratorType>();

                        if (argument instanceof ObjectArgument)
                        {
                            ObjectArgument objectArgument = (ObjectArgument) argument;

                            collectReportTypes(objectArgument, addedInner, innerPojoGeneratorTypes);
                        }
                        Collections.reverse(innerPojoGeneratorTypes);
                        for (EJReportPojoGeneratorType inner : innerPojoGeneratorTypes)
                        {
                            String objName = inner.getClassName();

                            inner.setClassName(objName);
                            try
                            {
                                reValidateReportDataTypes(inner);
                                String clazz = rcontext.createPojoClass(inner, monitor);
                                innerClass.put(objName, clazz);
                            }
                            catch (Exception e)
                            {

                                e.printStackTrace();
                            }
                        }
                    }

                }

                for (Argument argument : procedure.getArguments())
                {

                    List<EJReportPojoGeneratorType> innerPojoGeneratorTypes = new ArrayList<EJReportPojoGeneratorType>();

                    if (argument instanceof ObjectArgument)
                    {
                        ObjectArgument objectArgument = (ObjectArgument) argument;

                        collectReportTypes(objectArgument, addedInner, innerPojoGeneratorTypes);
                    }
                    Collections.reverse(innerPojoGeneratorTypes);
                    for (EJReportPojoGeneratorType inner : innerPojoGeneratorTypes)
                    {
                        String objName = inner.getClassName();

                        inner.setClassName(objName);
                        try
                        {
                            reValidateReportDataTypes(inner);
                            String clazz = rcontext.createPojoClass(inner, monitor);
                            innerClass.put(objName, clazz);
                        }
                        catch (Exception e)
                        {

                            e.printStackTrace();
                        }
                    }
                }

            }

        };
    }

    public boolean isActive(IJavaProject project)
    {
        try
        {
            return project != null && project.findType("oracle.jdbc.OracleConnection") != null;
        }
        catch (JavaModelException e)
        {
            EJCoreLog.log(e);
        }
        return false;
    }
}
