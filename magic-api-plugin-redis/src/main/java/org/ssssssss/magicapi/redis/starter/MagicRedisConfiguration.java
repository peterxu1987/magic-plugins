package org.ssssssss.magicapi.redis.starter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.redis.RedisModule;
import org.ssssssss.magicapi.redis.model.MagicDynamicRedisClient;
import org.ssssssss.magicapi.redis.service.RedisMagicDynamicRegistry;
import org.ssssssss.magicapi.redis.service.RedisMagicResourceStorage;
import org.ssssssss.magicapi.redis.web.MagicRedisController;

@Configuration
public class MagicRedisConfiguration implements MagicPluginConfiguration {

	@Override
	public Plugin plugin() {
		return new Plugin("redis缓存", "redis", "magic-redis.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) -> mapping.registerController(new MagicRedisController(configuration));
	}
	
	@Bean
	@ConditionalOnMissingBean
	public RedisMagicResourceStorage redisMagicResourceStorage() {
		return new RedisMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public MagicDynamicRedisClient magicDynamicRedisClient() {
		return new MagicDynamicRedisClient();
	}

	@Bean
	@ConditionalOnMissingBean
	public RedisMagicDynamicRegistry redisMagicDynamicRegistry(
			@Qualifier("redisMagicResourceStorage") RedisMagicResourceStorage redisMagicResourceStorage,
			@Qualifier("magicDynamicRedisClient") MagicDynamicRedisClient magicDynamicRedisClient) {
		return new RedisMagicDynamicRegistry(redisMagicResourceStorage, magicDynamicRedisClient);
	}

	@Bean(name = "magicRedisModule")
	@ConditionalOnMissingBean
	public RedisModule magicRedisModule(
			@Qualifier("magicDynamicRedisClient")  MagicDynamicRedisClient magicDynamicRedisClient) {
		return new RedisModule(magicDynamicRedisClient);
	}

	
}
