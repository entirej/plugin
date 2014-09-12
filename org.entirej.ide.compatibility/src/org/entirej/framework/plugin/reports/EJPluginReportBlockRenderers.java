package org.entirej.framework.plugin.reports;

import java.util.Arrays;
import java.util.List;

public class EJPluginReportBlockRenderers
{
    private static final String FIELDS = "Fields";
    private static final String TABLE  = "Table";
    
    public static List<String> getBlockRenderers()
    {
        
        return Arrays.asList(FIELDS, TABLE);
    }
    
}
