 
package com.opensymphony.module.propertyset.database;

 
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.Map;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.sql.DataSource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.opensymphony.module.propertyset.AbstractPropertySet;
import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.Data;

 
public class JDBCPropertySet extends AbstractPropertySet {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final Log log = LogFactory.getLog(JDBCPropertySet.class);

    //~ Instance fields ////////////////////////////////////////////////////////

    // config
    DataSource ds ;
    String colData;
    String colDate;
    String colFloat;
    String colGlobalKey;
    String colItemKey;
    String colItemType;
    String colNumber;
    String colString;

    // args
    String globalKey;
    String tableName;

    //~ Methods ////////////////////////////////////////////////////////////////

    public Collection<?> getKeys(String prefix, int type) throws PropertyException {
        if (prefix == null) {
            prefix = "";
        }

        Connection conn = null;

        try {
            conn = ds.getConnection();

            PreparedStatement ps = null;
            String sql = "SELECT " + colItemKey + " FROM " + tableName + " WHERE " + colItemKey + " LIKE ? AND " + colGlobalKey + " = ?";

            if (type == 0) {
                ps = conn.prepareStatement(sql);
                ps.setString(1, prefix + "%");
                ps.setString(2, globalKey);
            } else {
                sql = sql + " AND " + colItemType + " = ?";
                ps = conn.prepareStatement(sql);
                ps.setString(1, prefix + "%");
                ps.setString(2, globalKey);
                ps.setInt(3, type);
            }

            ArrayList<Object> list = new ArrayList<Object>();
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(rs.getString(colItemKey));
            }

            rs.close();
            ps.close();

            return list;
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    public int getType(String key) throws PropertyException {
        Connection conn = null;

        try {
            conn = ds.getConnection();

            String sql = "SELECT " + colItemType + " FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, globalKey);
            ps.setString(2, key);

            ResultSet rs = ps.executeQuery();
            int type = 0;

            if (rs.next()) {
                type = rs.getInt(colItemType);
            }

            rs.close();
            ps.close();

            return type;
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    public boolean exists(String key) throws PropertyException {
        return getType(key) != 0;
    }

    @SuppressWarnings("rawtypes")
	public void init(Map config, Map args) {
        // args
        globalKey = (String) args.get("globalKey");

        // config  --> modified by chirs chen  2007-07-03
        try {
            InitialContext initCtx = new InitialContext();
            Context context = (Context) initCtx.lookup("java:comp/env");
            ds = (DataSource) context.lookup((String) config.get("datasource"));
        } catch (Exception e) {
            log.fatal("Could not get DataSource", e);
        }

        tableName = (String) config.get("table.name");
        colGlobalKey = (String) config.get("col.globalKey");
        colItemKey = (String) config.get("col.itemKey");
        colItemType = (String) config.get("col.itemType");
        colString = (String) config.get("col.string");
        colDate = (String) config.get("col.date");
        colData = (String) config.get("col.data");
        colFloat = (String) config.get("col.float");
        colNumber = (String) config.get("col.number");
    }

    public void remove(String key) throws PropertyException {
        Connection conn = null;

        try {
            conn = ds.getConnection();

            String sql = "DELETE FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, globalKey);
            ps.setString(2, key);
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    protected void setImpl(int type, String key, Object value) throws PropertyException {
        if (value == null) {
            throw new PropertyException("JDBCPropertySet does not allow for null values to be stored");
        }

        Connection conn = null;

        try {
            conn = ds.getConnection();

            String sql = "UPDATE " + tableName + " SET " + colString + " = ?, " + colDate + " = ?, " + colData + " = ?, " + colFloat + " = ?, " + colNumber + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            PreparedStatement ps = conn.prepareStatement(sql);
            setValues(ps, type, key, value);

            int rows = ps.executeUpdate();
            ps.close();

            if (rows != 1) {
                // ok, this is a new value, insert it
                sql = "INSERT INTO " + tableName + " (" + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
                ps = conn.prepareStatement(sql);
                setValues(ps, type, key, value);
                ps.executeUpdate();
                ps.close();
            }
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }
    }

    protected Object get(int type, String key) throws PropertyException {
        String sql = "SELECT " + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " = ? AND " + colGlobalKey + " = ?";

        Object o = null;
        Connection conn = null;

        try {
            conn = ds.getConnection();

            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, key);
            ps.setString(2, globalKey);

            int propertyType;
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                propertyType = rs.getInt(colItemType);

                if (propertyType != type) {
                    throw new InvalidPropertyTypeException();
                }

                switch (type) {
                    case PropertySet.BOOLEAN:

                        int boolVal = rs.getInt(colNumber);
                        o = new Boolean(boolVal == 1);

                        break;

                    case PropertySet.DATA:
                        o = rs.getBytes(colData);

                        break;

                    case PropertySet.DATE:
                        o = rs.getTimestamp(colDate);

                        break;

                    case PropertySet.DOUBLE:
                        o = new Double(rs.getDouble(colFloat));

                        break;

                    case PropertySet.INT:
                        o = new Integer(rs.getInt(colNumber));

                        break;

                    case PropertySet.LONG:
                        o = new Long(rs.getLong(colNumber));

                        break;

                    case PropertySet.STRING:
                        o = rs.getString(colString);

                        break;

                    default:
                        throw new InvalidPropertyTypeException("JDBCPropertySet doesn't support this type yet.");
                }
            }

            rs.close();
            ps.close();
        } catch (SQLException e) {
            throw new PropertyException(e.getMessage());
        } catch (NumberFormatException e) {
            throw new PropertyException(e.getMessage());
        } finally {
            closeConnection(conn);
        }

        return o;
    }

    private void setValues(PreparedStatement ps, int type, String key, Object value) throws SQLException, PropertyException {
        // Patched by Edson Richter for MS SQL Server JDBC Support!
        String driverName;

        try {
            driverName = ps.getConnection().getMetaData().getDriverName().toUpperCase();
        } catch (Exception e) {
            driverName = "";
        }

        ps.setNull(1, Types.VARCHAR);
        ps.setNull(2, Types.TIMESTAMP);

        // Patched by Edson Richter for MS SQL Server JDBC Support!
        // Oracle support suggestion also Michael G. Slack
        if ((driverName.indexOf("SQLSERVER") >= 0) || (driverName.indexOf("ORACLE") >= 0)) {
            ps.setNull(3, Types.BINARY);
        } else {
            ps.setNull(3, Types.BLOB);
        }

        ps.setNull(4, Types.FLOAT);
        ps.setNull(5, Types.NUMERIC);
        ps.setInt(6, type);
        ps.setString(7, globalKey);
        ps.setString(8, key);

        switch (type) {
            case PropertySet.BOOLEAN:

                Boolean boolVal = (Boolean) value;
                ps.setInt(5, boolVal.booleanValue() ? 1 : 0);

                break;

            case PropertySet.DATA:

                Data data = (Data) value;
                ps.setBytes(3, data.getBytes());

                break;

            case PropertySet.DATE:

                Date date = (Date) value;
                ps.setTimestamp(2, new Timestamp(date.getTime()));

                break;

            case PropertySet.DOUBLE:

                Double d = (Double) value;
                ps.setDouble(4, d.doubleValue());

                break;

            case PropertySet.INT:

                Integer i = (Integer) value;
                ps.setInt(5, i.intValue());

                break;

            case PropertySet.LONG:

                Long l = (Long) value;
                ps.setLong(5, l.longValue());

                break;

            case PropertySet.STRING:
                ps.setString(1, (String) value);

                break;

            default:
                throw new PropertyException("This type isn't supported!");
        }
    }

    private void closeConnection(Connection conn) {
        try {
            if ((conn != null) && !conn.isClosed()) {
                conn.close();
            }
        } catch (SQLException e) {
            log.error("Could not close connection");
        }
    }

    public void remove() throws PropertyException {

    }
}
