package com.opensymphony.module.propertyset.database;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import javax.sql.DataSource;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import com.opensymphony.module.propertyset.InvalidPropertyTypeException;
import com.opensymphony.module.propertyset.PropertyException;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.util.Data;
import com.opensymphony.workflow.util.WorkFlowSpringManager;

 
public class JDBCTemplatePropertySetDAOImpl implements JDBCTemplatePropertySetDAO {
    private String colData;
    private String colDate;
    private String colFloat;
    private String colGlobalKey;
    private String colItemKey;
    private String colItemType;
    private String colNumber;
    private String colString;

    // args
    private String globalKey;
    private String tableName;
    
	
	public TransactionTemplate getTransactionTemplate() {
		return WorkFlowSpringManager.getTransactionTemplate();
	}

	public final JdbcTemplate getJdbcTemplate() {
		return WorkFlowSpringManager.getJdbcTemplate();
	}

	public void setColData(String colData) {
        this.colData = colData;
    }

    public void setColDate(String colDate) {
        this.colDate = colDate;
    }

    public void setColFloat(String colFloat) {
        this.colFloat = colFloat;
    }

    public void setColGlobalKey(String colGlobalKey) {
        this.colGlobalKey = colGlobalKey;
    }

    public void setColItemKey(String colItemKey) {
        this.colItemKey = colItemKey;
    }

    public void setColItemType(String colItemType) {
        this.colItemType = colItemType;
    }

    public void setColNumber(String colNumber) {
        this.colNumber = colNumber;
    }

    public void setColString(String colString) {
        this.colString = colString;
    }

    public void setGlobalKey(String globalKey) {
        this.globalKey = globalKey;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Collection<?> getKeys(String prefix, int type) throws PropertyException {
        if (prefix == null) {
            prefix = "";
        }

        try {
            String sql = "SELECT " + colItemKey + " FROM " + tableName + " WHERE " + colItemKey + " LIKE ? AND " + colGlobalKey + " = ?";
            final ArrayList<Object> list = new ArrayList<Object>();
            if (type == 0) {
                this.getJdbcTemplate().query(sql, new Object[]{prefix + "%", globalKey}, new RowCallbackHandler() {
                    public void processRow(ResultSet rs) throws SQLException {
                        list.add(rs.getString(colItemKey));
                    }
                });
            } else {
                sql = sql + " AND " + colItemType + " = ?";
                this.getJdbcTemplate().query(sql, new Object[]{prefix + "%", globalKey, new Integer(type)}, new RowCallbackHandler() {
                    public void processRow(ResultSet rs) throws SQLException {
                        list.add(rs.getString(colItemKey));
                    }
                });
            }

            return list;
        } catch (DataAccessException
                e) {
            throw new PropertyException(e.getMessage());
        }
    }

	public int getType(String key) throws PropertyException {
        try {

            String sql = "SELECT " + colItemType + " FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            return this.getJdbcTemplate().queryForObject(sql,Integer.class ,new Object[]{globalKey, key});

        } catch (DataAccessException e) {
            throw new PropertyException(e.getMessage());
        }
    }

    public boolean exists(String key) throws PropertyException {
        return getType(key) != 0;
    }


    public void remove(final String key) throws PropertyException {
    	getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
        try {

            String sql = "DELETE FROM " + tableName + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            getJdbcTemplate().update(sql, new Object[]{globalKey, key});
        } catch (DataAccessException e) {
        	status.setRollbackOnly();
			throw new PropertyException(e.getMessage());
        }
			}
		});
    }

    public void setImpl(int type, String key, Object value) throws PropertyException {
        if (value == null) {
            throw new PropertyException("JDBCTemplatePropertySet does not allow for null values to be stored");
        }
        try {
            int rows = updateValues(type, key, value);

            if (rows != 1) {
                insertValues(type, key, value);
            }
        } catch (Exception e) {
            throw new PropertyException(e.getMessage());
        }
    }

    @SuppressWarnings("rawtypes")
	public Object get(final int type, String key) throws PropertyException {
        String sql = "SELECT " + colItemType + ", " + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + " FROM " + tableName + " WHERE " + colItemKey + " = ? AND " + colGlobalKey + " = ?";
        final List list = new ArrayList();
        try {

            this.getJdbcTemplate().query(sql, new Object[]{key, globalKey}, new RowCallbackHandler() {

                @SuppressWarnings("unchecked")
				public void processRow(ResultSet rs) throws SQLException {
                    int propertyType = rs.getInt(colItemType);

                    if (propertyType != type) {
                        throw new InvalidPropertyTypeException();
                    }
                    Object o;
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
                    list.add(o);

                }

            });

        } catch (NumberFormatException e) {
            throw new PropertyException(e.getMessage());
        }
        return list.get(0);
    }

    private int updateValues(final int type, final String key, final Object value) throws DataAccessException, PropertyException {
    	 return getTransactionTemplate().execute(new TransactionCallback<Integer>() {  
             public Integer doInTransaction(TransactionStatus status) {  
        //String sql = "UPDATE " + tableName + " SET " + colString + " = ?, " + colDate + " = ?, " + colData + " = ?, " + colFloat + " = ?, " + colNumber + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
            	 try {  
            	 String sql = "";
        switch (type) {
            case PropertySet.BOOLEAN:
                Boolean boolVal = (Boolean) value;
                sql = "UPDATE " + tableName + " SET " + colNumber + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
                return getJdbcTemplate().update(sql, new Object[]{(boolVal.booleanValue() ? 1 : 0), type, globalKey, key});

            case PropertySet.DATA:

                Data data = (Data) value;

                sql = "UPDATE " + tableName + " SET " + colData + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
                return getJdbcTemplate().update(sql, new Object[]{data.getBytes(), type, globalKey, key});


            case PropertySet.DATE:

                java.util.Date date = (java.util.Date) value;
                sql = "UPDATE " + tableName + " SET " + colDate + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
                return getJdbcTemplate().update(sql, new Object[]{new Timestamp(date.getTime()), type, globalKey, key});


            case PropertySet.DOUBLE:

                Double d = (Double) value;

                sql = "UPDATE " + tableName + " SET " + colFloat + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
                return getJdbcTemplate().update(sql, new Object[]{d, type, globalKey, key});


            case PropertySet.INT:

                Integer i = (Integer) value;

                sql = "UPDATE " + tableName + " SET " + colNumber + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
                return getJdbcTemplate().update(sql, new Object[]{i, type, globalKey, key});


            case PropertySet.LONG:

                Long l = (Long) value;
                sql = "UPDATE " + tableName + " SET " + colNumber + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
                return getJdbcTemplate().update(sql, new Object[]{l, type, globalKey, key});


            case PropertySet.STRING:
                sql = "UPDATE " + tableName + " SET " + colString + " = ?, " + colItemType + " = ? " + " WHERE " + colGlobalKey + " = ? AND " + colItemKey + " = ?";
                
                return getJdbcTemplate().update(sql, new Object[]{(String) value, type, globalKey, key});

            default:
                throw new PropertyException("This type isn't supported!");
                
        }
             } catch (Exception ex) {
					// 通过调用 TransactionStatus 对象的 setRollbackOnly() 方法来回滚事务。
					status.setRollbackOnly();
					throw new RuntimeException(ex.getMessage());
				}
             }  
         });  
    }

    private void insertValues(final int type, final String key, final Object value) throws DataAccessException, PropertyException {
    	getTransactionTemplate().execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus status) {
				try {
        //String sql = "INSERT INTO " + tableName + " (" + colString + ", " + colDate + ", " + colData + ", " + colFloat + ", " + colNumber + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        String sql = "";
        switch (type) {
            case PropertySet.BOOLEAN:
                Boolean boolVal = (Boolean) value;
                sql = "INSERT INTO " + tableName + " (" + colNumber + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?)";
                getJdbcTemplate().update(sql, new Object[]{(boolVal.booleanValue() ? 1 : 0), type, globalKey, key});
                break;
            case PropertySet.DATA:

                Data data = (Data) value;

                sql = "INSERT INTO " + tableName + " (" + colData + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?)";
                getJdbcTemplate().update(sql, new Object[]{data.getBytes(), type, globalKey, key});
                break;

            case PropertySet.DATE:

                java.util.Date date = (java.util.Date) value;
                sql = "INSERT INTO " + tableName + " (" + colDate + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?)";
                getJdbcTemplate().update(sql, new Object[]{new Timestamp(date.getTime()), type, globalKey, key});
                break;

            case PropertySet.DOUBLE:

                Double d = (Double) value;

                sql = "INSERT INTO " + tableName + " (" + colFloat + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?)";
                getJdbcTemplate().update(sql, new Object[]{d, type, globalKey, key});
                break;

            case PropertySet.INT:

                Integer i = (Integer) value;

                sql = "INSERT INTO " + tableName + " (" + colNumber + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?)";
                getJdbcTemplate().update(sql, new Object[]{i, type, globalKey, key});
                break;

            case PropertySet.LONG:

                Long l = (Long) value;
                sql = "INSERT INTO " + tableName + " (" + colNumber + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?)";
                getJdbcTemplate().update(sql, new Object[]{l, type, globalKey, key});
                break;

            case PropertySet.STRING:
                sql = "INSERT INTO " + tableName + " (" + colString + ", " + colItemType + ", " + colGlobalKey + ", " + colItemKey + ") VALUES (?, ?, ?, ?)";
                getJdbcTemplate().update(sql, new Object[]{(String) value, type, globalKey, key});
                break;

            default:
                throw new PropertyException("This type isn't supported!");
        }
			} catch (Exception ex) {
				// 通过调用 TransactionStatus 对象的 setRollbackOnly() 方法来回滚事务。
				status.setRollbackOnly();
				throw new RuntimeException(ex.getMessage());
			}
			}
		});
    }

}
