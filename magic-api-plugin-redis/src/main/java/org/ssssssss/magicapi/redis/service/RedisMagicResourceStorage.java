package org.ssssssss.magicapi.redis.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.model.JsonCode;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.redis.model.RedisInfo;
import org.ssssssss.magicapi.redis.util.RedisUtil.Pool;
import org.ssssssss.magicapi.utils.JsonUtils;

public class RedisMagicResourceStorage implements MagicResourceStorage<RedisInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "redis";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<RedisInfo> magicClass() {
		return RedisInfo.class;
	}

	@Override
	public boolean requirePath() {
		return false;
	}

	@Override
	public boolean requiredScript() {
		return false;
	}

	@Override
	public boolean allowRoot() {
		return true;
	}

	@Override
	public String buildMappingKey(RedisInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	@SuppressWarnings("unchecked")
	@Override
	public void validate(RedisInfo entity) {
		notBlank(entity.getKey(), DS_KEY_REQUIRED);
		notBlank(entity.getType(), new JsonCode(1020, "redis type 不能为空"));
		notNull(entity.getProperties(), new JsonCode(1020, "redis propertie 不能为空"));
		
		Map<String, Object> properties = new HashMap<>(entity.getProperties());
	 
		try {
			
			Map<String, Object> poolMap = (Map<String, Object>)properties.get("pool");
			new Pool(
				Integer.parseInt(poolMap.get("max-active").toString())
			   ,Integer.parseInt(poolMap.get("max-idle").toString())
			   ,Integer.parseInt(poolMap.get("min-idle").toString())
			   ,Integer.parseInt(poolMap.get("max-wait").toString())
			);
		}catch(Exception e){
			notNull(null, new JsonCode(6020, "redis pool 参数异常："+e.getMessage()));
		}
		
		if (entity.getType().equals("single")) {
			if(properties.get("host") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 host 不能为 空!"));
			}
			if(properties.get("password") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 password 不能为 空!"));
			}
			if(properties.get("port") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 port 不能为 空!"));
			}else if(!properties.get("port").toString().matches("\\d+")) {
				notNull(null, new JsonCode(6021, "redis 配置参数 port 只能是正整数!"));
			}
			if(properties.get("database") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 database 不能为 空，默认配置 0 !"));
			}else if(!properties.get("database").toString().matches("\\d+")) {
				notNull(null, new JsonCode(6021, "redis 配置参数 database 只能是正整数，默认配置 0 !"));
			}
		 
		}else if (entity.getType().equals("sentinel")) {
			if(properties.get("master") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 master 不能为 空!"));
			}
			if(properties.get("password") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 password 不能为 空!"));
			}
			
			if(properties.get("nodes") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 nodes 不能为 空!"));
			}else {
				try {
					List<String> nodes = (List<String>) properties.get("nodes");
					if(nodes.size() <= 1) {
						notNull(null, new JsonCode(6020, "redis 配置参数 nodes 值不能为[]!"));
					}
				}catch(Exception e1){
					notNull(null, new JsonCode(6021, "redis 配置参数 nodes 类型错误，得是[\"ip:port\",\"ip:port\"]!"));
				}
				
			}
		}else if (entity.getType().equals("cluster")) {
			if(properties.get("password") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 password 不能为 空!"));
			}
			
			if(properties.get("nodes") == null) {
				notNull(null, new JsonCode(6020, "redis 配置参数 nodes 不能为 空!"));
			}else {
				try {
					List<String> nodes = (List<String>) properties.get("nodes");
					if(nodes.size() <= 1) {
						notNull(null, new JsonCode(6020, "redis 配置参数 nodes 值不能为[]!"));
					}
				}catch(Exception e1){
					notNull(null, new JsonCode(6021, "redis 配置参数 nodes 类型错误，得是[\"ip:port\",\"ip:port\"]!"));
				}
			}
		}

		boolean noneMatchKey = magicResourceService.listFiles("redis:0").stream()
				.map(it -> (RedisInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, DS_KEY_CONFLICT);
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public RedisInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, RedisInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
