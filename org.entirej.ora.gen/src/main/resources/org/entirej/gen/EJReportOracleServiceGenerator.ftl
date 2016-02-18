package ${package_name};

import java.util.List;

import java.util.List;

import org.entirej.report.EJStatementParameterOraStruct;
import org.entirej.report.EJStatementParameterOraArray;
import org.entirej.report.OracleStatementExecutor;
import org.entirej.framework.report.EJReport;
import org.entirej.framework.report.service.EJReportBlockService;
import org.entirej.framework.report.service.EJReportParameterType;
import org.entirej.framework.report.service.EJReportQueryCriteria;
import org.entirej.framework.report.service.EJReportStoredProcedureStatementParameter;



<#list imports as import>
import ${import};
</#list>

public class ${service_name} implements EJReportBlockService<${JAVA_REC_NAME}>
{
    private final OracleStatementExecutor _statementExecutor;

    public ${service_name}()
    {
        _statementExecutor = new OracleStatementExecutor();
    }

    @Override
    public List<${JAVA_REC_NAME}> executeQuery(EJReport report, EJReportQueryCriteria queryCriteria)
    {
    
 <#if query_procedure != "">  
  <#if query_returntype?? >            
        StringBuilder stmt = new StringBuilder();
        stmt.append(" BEGIN");
        stmt.append("   ? := ${query_procedure} (");
        stmt.append("  <#list query_parameters as column>${column.name} => ? <#if column?has_next >, </#if></#list>" );
        stmt.append("         );");
        stmt.append(" END;");


        EJStatementParameterOraStruct<${query_returntype.data_type}> ${query_returntype.var_name} = new EJStatementParameterOraStruct<${query_returntype.data_type}>(${query_returntype.data_type}.class, EJReportParameterType.${query_returntype.param_type});

        <#list query_parameters as column>
            <#if column.is_array =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJReportParameterType.${column.param_type});
       		<#elseif column.is_struct =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJReportStoredProcedureStatementParameter ${column.var_name} = new EJReportStoredProcedureStatementParameter(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(report, stmt.toString(),${query_returntype.var_name}, <#list query_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

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
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJReportParameterType.${column.param_type});
       		<#elseif column.is_struct =="true" >
       		EJStatementParameterOraArray<${column.data_type}> ${column.var_name} = new EJStatementParameterOraArray<${column.data_type}>(${column.data_type}.class, EJParameterType.${column.param_type});
       		<#else>
       		EJReportStoredProcedureStatementParameter ${column.var_name} = new EJReportStoredProcedureStatementParameter(${column.data_type}.class, EJParameterType.${column.param_type});
       		</#if>  
        </#list>
        
      
        _statementExecutor.executePLSQLStoredProcedure(report, stmt.toString(), <#list query_parameters as column>${column.var_name} <#if column?has_next >, </#if></#list>);

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
    


    
    
    
}
