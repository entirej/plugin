package org.entirej;

import oracle.sql.Datum;
import oracle.sql.ORAData;
import oracle.sql.ORADataFactory;

public interface EJOraCollectionType extends ORAData, ORADataFactory
{

    public String getSqlName();

    public ORAData create(Datum d, int sqlType);
}
