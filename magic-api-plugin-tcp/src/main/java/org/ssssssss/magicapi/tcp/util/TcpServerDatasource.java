package org.ssssssss.magicapi.tcp.util;

import java.util.HashMap;
import java.util.Map;

import org.ssssssss.magicapi.tcp.model.TcpInfo;

public class TcpServerDatasource implements Datasource{
	private String id = "";
	private String type = "";
	private Map<String, Object> properties;
	public TcpServerDatasource(TcpInfo info) {
		id = info.getId();
		type = info.getType();
		this.properties = new HashMap<>(info.getProperties());
		this.properties.put("serverType", "server");
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
