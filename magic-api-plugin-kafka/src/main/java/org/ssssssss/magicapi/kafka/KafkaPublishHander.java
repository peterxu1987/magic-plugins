package org.ssssssss.magicapi.kafka;

@FunctionalInterface
public interface KafkaPublishHander {
	void callback(String topicName, String errorMessage);
}