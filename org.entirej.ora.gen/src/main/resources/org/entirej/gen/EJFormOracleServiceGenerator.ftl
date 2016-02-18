package ${package_name};

import java.util.List;

import java.util.List;

import org.entirej.EJStatementParameterOraStruct;
import org.entirej.EJStatementParameterOraArray;
import org.entirej.framework.core.service.EJStoredProcedureStatementParameter;
import org.entirej.OracleStatementExecutor;
import org.entirej.framework.core.EJForm;
import org.entirej.framework.core.service.EJBlockService;
import org.entirej.framework.core.service.EJParameterType;
import org.entirej.framework.core.service.EJQueryCriteria;



<#list imports as import>
import ${import};
</#list>

public class ${service_name} implements EJBlockService<${JAVA_REC_NAME}>
{
    private final OracleStatementExecutor _statementExecutor;

    public ${service_name}()
    {
        _statementExecutor = new OracleStatementExecutor();
    }

    @Override
    public List<${JAVA_REC_NAME}> executeQuery(EJForm form, EJQueryCriteria queryCriteria)
    {
    
 <#if query_procedure != "">  
  <#if query_returntype?? >            
        StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("   ? := ${query_procedure} (");
        stmt.append("  <#list query_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


        EJStatementParameterOraStruct<${query_returntype.data_type}> ${query_returntype.var_name} = new EJStatementParameterOraStruct<${query_returntype.data_type}>(${query_returntype.data_type}.class, EJParameterType.${query_returntype.param_type});

        <#list query_parameters as column>
            <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#if column.is_struct =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStoredProcedureStatementParameter ${column.var_name} = new EJStoredProcedureStatementParameter(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>   
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(),${query_returntype.var_name}, <#list query_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

		<#list query_parameters as column>
		    <#if column.param_type=="INOUT" && column.data_type == JAVA_OBJECT_NAME>
       		return ${column.var_name}.getValue().getValues();
       		 </#if>
        </#list>

 
   <#else>
      StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("  ${query_procedure} (");
        stmt.append("  <#list query_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


      
        <#list query_parameters as column>
       		 <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#if column.is_struct =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStoredProcedureStatementParameter ${column.var_name} = new EJStoredProcedureStatementParameter(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(), <#list query_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

		<#list query_parameters as column>
		    <#if column.param_type=="INOUT" && column.data_type == JAVA_OBJECT_NAME>
       		return ${column.var_name}.getValue().getValues();
       		 </#if>
        </#list>
   </#if>               
 <#else>
         return new java.util.ArrayList<${JAVA_REC_NAME}>(0);
 </#if>       
    }
    


    @Override
    public void executeInsert(EJForm form, List<${JAVA_REC_NAME}> newRecords)
    {
    
 <#if insert_procedure != "">  
  <#if insert_returntype?? >            
        StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("   ? := ${insert_procedure} (");
        stmt.append("  <#list insert_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


        EJStatementParameterOraStruct<${insert_returntype.data_type}> ${insert_returntype.var_name} = new EJStatementParameterOraStruct<${insert_returntype.data_type}>(${insert_returntype.data_type}.class, EJParameterType.${insert_returntype.param_type});

        <#list insert_parameters as column>
       		 <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStatementParameterOraStruct<${column.data_type}> ${column.var_name} = new EJStatementParameterOraStruct<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(),${insert_returntype.var_name}, <#list insert_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

		

 
   <#else>
      StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("  ${insert_procedure} (");
        stmt.append("  <#list insert_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


      
        <#list insert_parameters as column>
       		 <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStatementParameterOraStruct<${column.data_type}> ${column.var_name} = new EJStatementParameterOraStruct<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(), <#list insert_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

   </#if>
    </#if>        
    }

    @Override
    public void executeUpdate(EJForm form, List<${JAVA_REC_NAME}> updateRecords)
    {
    
 <#if update_procedure != "">  
  <#if update_returntype?? >            
        StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("   ? := ${update_procedure} (");
        stmt.append("  <#list update_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


        EJStatementParameterOraStruct<${update_returntype.data_type}> ${update_returntype.var_name} = new EJStatementParameterOraStruct<${update_returntype.data_type}>(${update_returntype.data_type}.class, EJParameterType.${update_returntype.param_type});

        <#list update_parameters as column>
       		 <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStatementParameterOraStruct<${column.data_type}> ${column.var_name} = new EJStatementParameterOraStruct<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(),${update_returntype.var_name}, <#list update_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

		

 
   <#else>
      StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("  ${update_procedure} (");
        stmt.append("  <#list update_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


      
        <#list update_parameters as column>
       		 <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStatementParameterOraStruct<${column.data_type}> ${column.var_name} = new EJStatementParameterOraStruct<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(), <#list update_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

   </#if>
    </#if>  
    }

    @Override
    public void executeDelete(EJForm form, List<${JAVA_REC_NAME}> deleteRecords)
    {
    
 <#if delete_procedure != "">  
  <#if delete_returntype?? >            
        StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("   ? := ${delete_procedure} (");
        stmt.append("  <#list delete_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


        EJStatementParameterOraStruct<${delete_returntype.data_type}> ${delete_returntype.var_name} = new EJStatementParameterOraStruct<${delete_returntype.data_type}>(${delete_returntype.data_type}.class, EJParameterType.${delete_returntype.param_type});

        <#list delete_parameters as column>
       		 <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStatementParameterOraStruct<${column.data_type}> ${column.var_name} = new EJStatementParameterOraStruct<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(),${delete_returntype.var_name}, <#list delete_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

		

 
   <#else>
      StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("  ${delete_procedure} (");
        stmt.append("  <#list delete_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


      
        <#list delete_parameters as column>
       		 <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJStatementParameterOraStruct<${column.data_type}> ${column.var_name} = new EJStatementParameterOraStruct<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(form, stmt.toString(), <#list delete_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

   </#if> 
    </#if>  
    }
    
    
    @Override
    public boolean canQueryInPages()
    {
        return ${queryInPages};
    }  
}
