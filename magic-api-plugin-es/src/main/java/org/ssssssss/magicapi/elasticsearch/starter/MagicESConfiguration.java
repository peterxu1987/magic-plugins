package org.ssssssss.magicapi.elasticsearch.starter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.elasticsearch.ESModule;
import org.ssssssss.magicapi.elasticsearch.model.MagicDynamicESClient;
import org.ssssssss.magicapi.elasticsearch.service.ESMagicDynamicRegistry;
import org.ssssssss.magicapi.elasticsearch.service.ESMagicResourceStorage;
import org.ssssssss.magicapi.elasticsearch.web.MagicESController;

@Configuration
public class MagicESConfiguration implements MagicPluginConfiguration {

	@Override
	public Plugin plugin() {
		return new Plugin("es搜索引擎", "es", "magic-es.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) -> mapping.registerController(new MagicESController(configuration));
	}

	@Bean
	@ConditionalOnMissingBean
	public MagicDynamicESClient magicDynamicESClient() {
		return new MagicDynamicESClient();
	}

	@Bean
	@ConditionalOnMissingBean
	public ESMagicResourceStorage esMagicResourceStorage() {
		return new ESMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public ESMagicDynamicRegistry esMagicDynamicRegistry(
			@Qualifier("esMagicResourceStorage") ESMagicResourceStorage esMagicResourceStorage,
			@Qualifier("magicDynamicESClient") MagicDynamicESClient magicDynamicESClient) {
		return new ESMagicDynamicRegistry(esMagicResourceStorage, magicDynamicESClient);
	}

	@Bean(name = "magicESModule")
	@ConditionalOnMissingBean
	public ESModule magicESModule(
			@Qualifier("magicDynamicESClient") MagicDynamicESClient magicDynamicESClient) {
		return new ESModule(magicDynamicESClient);
	}

}
