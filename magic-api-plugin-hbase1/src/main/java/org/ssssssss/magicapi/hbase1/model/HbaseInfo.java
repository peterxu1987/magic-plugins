package org.ssssssss.magicapi.hbase1.model;

import java.util.Map;

import org.ssssssss.magicapi.core.model.MagicEntity;

public class HbaseInfo extends MagicEntity {
 
	private String key;
	private Map<String, Object> properties;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public MagicEntity simple() {
		HbaseInfo hbaseInfo = new HbaseInfo();
		hbaseInfo.setKey(this.key);
		super.simple(hbaseInfo);
		return hbaseInfo;
	}

	@Override
	public MagicEntity copy() {
		HbaseInfo hbaseInfo = new HbaseInfo();
		super.copyTo(hbaseInfo);
		hbaseInfo.setKey(key);
		hbaseInfo.setProperties(properties);
		return hbaseInfo;
	}
}
