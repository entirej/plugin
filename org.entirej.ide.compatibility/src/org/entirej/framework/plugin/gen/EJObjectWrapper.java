package org.entirej.framework.plugin.gen;

import org.entirej.framework.core.service.EJTableColumn;

import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;

public class EJObjectWrapper extends DefaultObjectWrapper
{
    
    public EJObjectWrapper(Version incompatibleImprovements)
    {
        super(incompatibleImprovements);
    }
    
    @Override
    protected TemplateModel handleUnknownType(final Object obj) throws TemplateModelException
    {
        if (obj instanceof EJTableColumn)
        {
            return new EJTableColumnAdapter((EJTableColumn) obj, this);
        }
        
        return super.handleUnknownType(obj);
    }
    
}