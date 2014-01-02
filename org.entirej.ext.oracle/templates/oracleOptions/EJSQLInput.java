package org.entirej;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Date;
import java.sql.NClob;
import java.sql.Ref;
import java.sql.RowId;
import java.sql.SQLException;
import java.sql.SQLInput;
import java.sql.SQLXML;
import java.sql.Struct;
import java.sql.Time;
import java.sql.Timestamp;

public class EJSQLInput implements SQLInput
{
    private final Object[] _attributes;
    private int            _currentIndex;
    private boolean        _lastNull;

    public EJSQLInput(Struct struct) throws SQLException
    {
        _attributes = struct.getAttributes();
        _currentIndex = -1;
    }

    @Override
    public String readString() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public boolean readBoolean() throws SQLException
    {
        return Boolean.TRUE.equals(readAttribute());
    }

    @Override
    public byte readByte() throws SQLException
    {
        Object obj = readAttribute();
        if (obj instanceof Number)
        {
            return ((Number) obj).byteValue();
        }
        return (Byte) obj;
    }

    @Override
    public short readShort() throws SQLException
    {
        Object obj = readAttribute();
        if (obj instanceof Number)
        {
            return ((Number) obj).shortValue();
        }
        return (Short) obj;
    }

    @Override
    public int readInt() throws SQLException
    {
        Object obj = readAttribute();
        if (obj instanceof Number)
        {
            return ((Number) obj).intValue();
        }
        return (Integer) obj;
    }

    @Override
    public long readLong() throws SQLException
    {
        Object obj = readAttribute();
        if (obj instanceof Number)
        {
            return ((Number) obj).longValue();
        }
        return (Long) obj;
    }

    @Override
    public float readFloat() throws SQLException
    {
        Object obj = readAttribute();
        if (obj instanceof Number)
        {
            return ((Number) obj).floatValue();
        }
        return (Float) obj;
    }

    @Override
    public double readDouble() throws SQLException
    {
        Object obj = readAttribute();
        if (obj instanceof Number)
        {
            return ((Number) obj).doubleValue();
        }
        return (Double) obj;
    }

    @Override
    public BigDecimal readBigDecimal() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public byte[] readBytes() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Date readDate() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Time readTime() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Timestamp readTimestamp() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Reader readCharacterStream() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public InputStream readAsciiStream() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public InputStream readBinaryStream() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Object readObject() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Ref readRef() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Blob readBlob() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Clob readClob() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public Array readArray() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public boolean wasNull() throws SQLException
    {
        return _lastNull;
    }

    @Override
    public URL readURL() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public NClob readNClob() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public String readNString() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public SQLXML readSQLXML() throws SQLException
    {
        return readAttribute();
    }

    @Override
    public RowId readRowId() throws SQLException
    {
        return readAttribute();
    }

    private <T extends Object> T readAttribute()
    {
        _lastNull = _attributes[++_currentIndex] == null;

        @SuppressWarnings("unchecked")
        T retValue = (T) _attributes[_currentIndex];
        return retValue;
    }
}