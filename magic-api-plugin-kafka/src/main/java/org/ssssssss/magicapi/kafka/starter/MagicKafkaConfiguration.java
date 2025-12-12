package org.ssssssss.magicapi.kafka.starter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.kafka.KafkaModule;
import org.ssssssss.magicapi.kafka.model.MagicDynamicKafkaClient;
import org.ssssssss.magicapi.kafka.service.KafkaMagicDynamicRegistry;
import org.ssssssss.magicapi.kafka.service.KafkaMagicResourceStorage;
import org.ssssssss.magicapi.kafka.web.MagicKafkaController;

@Configuration
public class MagicKafkaConfiguration implements MagicPluginConfiguration {

	@Override
	public Plugin plugin() {
		return new Plugin("kafka消息中间件", "kafka", "magic-kafka.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) -> mapping.registerController(new MagicKafkaController(configuration));
	}

	@Bean
	@ConditionalOnMissingBean
	public MagicDynamicKafkaClient magicDynamicKafkaClient() {
		return new MagicDynamicKafkaClient();
	}

	@Bean
	@ConditionalOnMissingBean
	public KafkaMagicResourceStorage kafkaMagicResourceStorage() {
		return new KafkaMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public KafkaMagicDynamicRegistry kafkaMagicDynamicRegistry(
			@Qualifier("kafkaMagicResourceStorage") KafkaMagicResourceStorage kafkaMagicResourceStorage,
			@Qualifier("magicDynamicKafkaClient") MagicDynamicKafkaClient magicDynamicKafkaClient) {
		return new KafkaMagicDynamicRegistry(kafkaMagicResourceStorage, magicDynamicKafkaClient);
	}
 
	@Bean(name = "magicKafkaModule")
	@ConditionalOnMissingBean
	public KafkaModule magicKafkaModule(
			@Qualifier("magicDynamicKafkaClient") MagicDynamicKafkaClient magicDynamicKafkaClient) {
		return new KafkaModule(magicDynamicKafkaClient);
	}

}
