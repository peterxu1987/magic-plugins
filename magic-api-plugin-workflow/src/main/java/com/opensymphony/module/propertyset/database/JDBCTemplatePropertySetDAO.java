package com.opensymphony.module.propertyset.database;

import com.opensymphony.module.propertyset.PropertyException;

import java.util.Collection;
 
public interface JDBCTemplatePropertySetDAO {


    public Collection<?> getKeys(String prefix, int type) throws PropertyException;

    public int getType(String key) throws PropertyException;

    public boolean exists(String key) throws PropertyException;

    public void remove(String key) throws PropertyException;

    public void setImpl(int type, String key, Object value) throws PropertyException;

    public Object get(int type, String key) throws PropertyException;

    public void setColData(String colData);

    public void setColDate(String colDate);

    public void setColFloat(String colFloat);

    public void setColGlobalKey(String colGlobalKey);

    public void setColItemKey(String colItemKey);

    public void setColItemType(String colItemType);

    public void setColNumber(String colNumber);

    public void setColString(String colString);

    public void setGlobalKey(String globalKey);

    public void setTableName(String tableName);
}
