package org.ssssssss.magicapi.redis.service;

import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.redis.model.MagicDynamicRedisClient;
import org.ssssssss.magicapi.redis.model.RedisInfo;
import org.ssssssss.magicapi.redis.util.RedisUtil;

public class RedisMagicDynamicRegistry extends AbstractMagicDynamicRegistry<RedisInfo> {

	private final MagicDynamicRedisClient magicDynamicRedisClient;

	private static final Logger logger = LoggerFactory.getLogger(RedisMagicDynamicRegistry.class);

	public RedisMagicDynamicRegistry(MagicResourceStorage<RedisInfo> magicResourceStorage,
			MagicDynamicRedisClient magicDynamicRedisClient) {
		super(magicResourceStorage);
		this.magicDynamicRedisClient = magicDynamicRedisClient;
	}

	@EventListener(condition = "#event.type == 'redis'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("注册redis数据源失败", e);
		}
	}

	@Override
	protected boolean register(MappingNode<RedisInfo> mappingNode) {
		RedisInfo info = mappingNode.getEntity();
		RedissonClient redissonClient = RedisUtil.extracted(info);
		if(redissonClient == null) {
			return false;
		}
		magicDynamicRedisClient.put(info.getId(), info.getKey(), info.getName(), redissonClient);
		return true;
	}

	@Override
	protected void unregister(MappingNode<RedisInfo> mappingNode) {
		magicDynamicRedisClient.delete(mappingNode.getEntity().getKey());
	}

	
}
