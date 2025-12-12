package org.ssssssss.magicapi.tcp.util;

import java.util.HashMap;
import java.util.Map;

import org.ssssssss.magicapi.tcp.model.TcpInfo;

public class TcpClientDatasource implements Datasource{
	private String id = "";
	private String type = "";
	private Map<String, Object> properties;
	public TcpClientDatasource(TcpInfo info) {
		id = info.getId();
		type = info.getType();
		this.properties = new HashMap<>(info.getProperties());
		this.properties.put("serverType", "client");
	}
	 
	public String getType() {
		return type;
	}

	public Map<String, Object> getProperties() {
		return properties;
	}

	@Override
	public String getId() {
		return id;
	}

}
