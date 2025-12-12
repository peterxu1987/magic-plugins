package org.ssssssss.magicapi.redis.web;

import org.redisson.api.RedissonClient;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.redis.model.RedisInfo;
import org.ssssssss.magicapi.redis.util.RedisUtil;

public class MagicRedisController extends MagicController implements MagicExceptionHandler {

	public MagicRedisController(MagicConfiguration configuration) {
		super(configuration);
	}

	@RequestMapping("/redis/jdbc/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody RedisInfo properties) {
		try {
			RedissonClient redissonClient = RedisUtil.extracted(properties);
			redissonClient.shutdown();
		} catch (Exception e) {
			return new JsonBean<>(e.getMessage());
		}
		return new JsonBean<>("ok");
	}
	
}
