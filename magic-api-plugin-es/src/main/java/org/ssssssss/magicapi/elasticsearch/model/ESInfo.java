package org.ssssssss.magicapi.elasticsearch.model;

import java.util.Map;

import org.ssssssss.magicapi.core.model.MagicEntity;

public class ESInfo extends MagicEntity {
 
	/**
	 * 安装类型
	 */
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
		ESInfo esInfo = new ESInfo();
		esInfo.setKey(this.key);
		super.simple(esInfo);
		return esInfo;
	}

	@Override
	public MagicEntity copy() {
		ESInfo esInfo = new ESInfo();
		super.copyTo(esInfo);
		esInfo.setKey(key);
		esInfo.setProperties(properties);
		return esInfo;
	}
}
