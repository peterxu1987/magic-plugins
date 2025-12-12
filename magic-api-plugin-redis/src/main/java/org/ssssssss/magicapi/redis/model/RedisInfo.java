package org.ssssssss.magicapi.redis.model;

import java.util.Map;

import org.ssssssss.magicapi.core.model.MagicEntity;

public class RedisInfo extends MagicEntity {
 
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
		RedisInfo redisInfo = new RedisInfo();
		redisInfo.setKey(this.key);
		super.simple(redisInfo);
		return redisInfo;
	}

	@Override
	public MagicEntity copy() {
		RedisInfo redisInfo = new RedisInfo();
		super.copyTo(redisInfo);
		redisInfo.setType(this.type);
		redisInfo.setKey(key);
		redisInfo.setProperties(properties);
		return redisInfo;
	}
}
