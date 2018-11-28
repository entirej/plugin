package org.entirej;

import oracle.jdbc.OracleData;
import oracle.jdbc.OracleDataFactory;

public interface EJOraCollectionType extends OracleData, OracleDataFactory
{

    public String getSqlName();
}
