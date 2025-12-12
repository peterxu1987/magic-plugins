package org.ssssssss.magicapi.kafka.model;

import java.util.Map;

import org.ssssssss.magicapi.core.model.MagicEntity;

public class KafkaInfo extends MagicEntity {
 
	private String key;
	private String type;
	private Map<String, Object> properties;
	
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

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
		KafkaInfo kafkaInfo = new KafkaInfo();
		kafkaInfo.setKey(this.key);
		kafkaInfo.setType(this.type);
		super.simple(kafkaInfo);
		return kafkaInfo;
	}

	@Override
	public MagicEntity copy() {
		KafkaInfo kafkaInfo = new KafkaInfo();
		super.copyTo(kafkaInfo);
		kafkaInfo.setKey(key);
		kafkaInfo.setType(this.type);
		kafkaInfo.setProperties(properties);
		return kafkaInfo;
	}
}
