package org.entirej.framework.plugin.gen;

import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.entirej.framework.core.service.EJTableColumn;

import freemarker.template.AdapterTemplateModel;
import freemarker.template.ObjectWrapper;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.WrappingTemplateModel;

public class EJTableColumnAdapter extends WrappingTemplateModel implements AdapterTemplateModel, TemplateHashModel
{
    
    private final EJTableColumn column;
    
    private Map<String, String> values = new HashMap<String, String>();
    
    public EJTableColumnAdapter(EJTableColumn column, ObjectWrapper ow)
    {
        super(ow); // coming from WrappingTemplateModel
        this.column = column;
        values.put("name", column.getName());
        values.put("var_name", toPropertyName(column.getName()));
        values.put("method_name", toMethodName(column.getName()));
        values.put("data_type_with_pkg", column.getDatatypeName());
        
        if (column.getDatatypeName().contains("."))
        {
            values.put("data_type", column.getDatatypeName().substring(column.getDatatypeName().lastIndexOf(".") + 1));
        }
        else
        {
            values.put("data_type", column.getDatatypeName());
        }
        
        if (column.getParameterType() != null) values.put("param_type", column.getParameterType().name());
        
        for (String key : column.getPropertyKeys())
        {
            values.put(key, column.getProperty(key));
        }
    }
    
    @Override // coming from AdapterTemplateModel
    public Object getAdaptedObject(Class hint)
    {
        return column;
    }
    
    @Override
    public TemplateModel get(String arg0) throws TemplateModelException
    {
        String str = values.get(arg0);
        
        return wrap(str == null ? "" : str);
    }
    
    @Override
    public boolean isEmpty() throws TemplateModelException
    {
        return values.isEmpty();
    }
    
    protected String toPropertyName(String columnName)
    {
        if (columnName != null && !columnName.contains("_"))
        {
            if (columnName.toUpperCase().equals(columnName))
            {
                return columnName.toLowerCase();
            }
            return columnName;
        }
        int n = columnName.length();
        StringBuilder fieldName = new StringBuilder(n);
        for (int i = 0, flag = 0; i < n; i++)
        {
            char c = columnName.charAt(i);
            if (c == '_')
            {
                flag = 1;
                continue;
            }
            fieldName.append(flag == 0 ? Character.toLowerCase(c) : Character.toUpperCase(c));
            flag = 0;
        }
        return fieldName.toString();
    }
    
    protected String toMethodName(String columnName)
    {
        
        String propertyName = toPropertyName(columnName);
        return propertyName.substring(0, 1).toUpperCase() + propertyName.substring(1);
    }
    
}
