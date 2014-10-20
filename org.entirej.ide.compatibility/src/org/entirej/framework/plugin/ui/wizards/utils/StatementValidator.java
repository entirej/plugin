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
package org.entirej.framework.plugin.ui.wizards.utils;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.entirej.framework.core.service.EJTableColumn;


public class StatementValidator
{
    
    public static List<EJTableColumn> validateStatement(Connection connection, String statement) throws InvalidStatementException
    {
        try
        {
            // statement = replaceParameters(statement);
            
            PreparedStatement stmt = null;
            
            try
            {
                stmt = connection.prepareStatement(statement);
                
                stmt.setFetchSize(1);
                stmt.execute();
                
                List<EJTableColumn> columns = new ArrayList<EJTableColumn>();
                
                ResultSetMetaData metaData = stmt.getMetaData();
                for (int i = 1; i <= metaData.getColumnCount(); i++)
                {
                    EJTableColumn param = new EJTableColumn();
                    param.setName(metaData.getColumnLabel(i));
                    String columnClassName = metaData.getColumnClassName(i);
                    
                            
                    param.setDatatypeName(columnClassName.equals("[B")?"Object":columnClassName);
                    columns.add(param);
                }
                
                return columns;
            }
            finally
            {
                if (stmt != null)
                {
                    stmt.close();
                }
            }
        }
        catch (SQLException e)
        {
            throw new InvalidStatementException(e.getMessage());
        }
    }
   
    
}
