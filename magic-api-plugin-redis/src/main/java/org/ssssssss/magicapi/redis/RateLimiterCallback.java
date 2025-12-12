package org.ssssssss.magicapi.redis;
@FunctionalInterface
interface RateLimiterCallback {
	Object exec() throws Exception;
}
