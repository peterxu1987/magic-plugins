package org.ssssssss.magicapi.redis;

import org.redisson.api.RMap;

@FunctionalInterface
interface MapHander {
	void hander(RMap<String, Object> map, RedisModule redisModule);
}
