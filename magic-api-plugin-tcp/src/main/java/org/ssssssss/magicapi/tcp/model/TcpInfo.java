package org.ssssssss.magicapi.tcp.model;

import java.util.Map;

import org.ssssssss.magicapi.core.model.MagicEntity;

public class TcpInfo extends MagicEntity {
 
	/**
	 * 安装类型
	 */
	private String key;
	private String type;
	private Map<String, Object> properties;
	
	public String getKey() {
		return key;
	}

	public void setKey(String key) {
		this.key = key;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	public void setProperties(Map<String, Object> properties) {
		this.properties = properties;
	}

	@Override
	public MagicEntity simple() {
		TcpInfo tcpInfo = new TcpInfo();
		tcpInfo.setKey(this.key);
		super.simple(tcpInfo);
		return tcpInfo;
	}

	@Override
	public MagicEntity copy() {
		TcpInfo tcpInfo = new TcpInfo();
		super.copyTo(tcpInfo);
		tcpInfo.setType(this.type);
		tcpInfo.setKey(key);
		tcpInfo.setProperties(properties);
		return tcpInfo;
	}
}
