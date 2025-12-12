package org.ssssssss.magicapi.redis;

@FunctionalInterface
interface RedisConsumerCallback {
	void processMessage(String topicName, String message) throws Exception;
}
