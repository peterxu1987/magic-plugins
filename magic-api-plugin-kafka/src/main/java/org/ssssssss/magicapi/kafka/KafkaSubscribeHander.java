package org.ssssssss.magicapi.kafka;

@FunctionalInterface
public interface KafkaSubscribeHander {
	void callback(String topicName, String key, String stringMessage, byte[] byteArrayMessage);
}