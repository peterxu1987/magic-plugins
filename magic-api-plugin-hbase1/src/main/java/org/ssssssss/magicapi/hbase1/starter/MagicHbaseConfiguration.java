package org.ssssssss.magicapi.hbase1.starter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.hbase1.HbaseModule;
import org.ssssssss.magicapi.hbase1.model.MagicDynamicHbaseClient;
import org.ssssssss.magicapi.hbase1.service.HbaseMagicDynamicRegistry;
import org.ssssssss.magicapi.hbase1.service.HbaseMagicResourceStorage;
import org.ssssssss.magicapi.hbase1.web.MagicHbaseController;

@Configuration
public class MagicHbaseConfiguration implements MagicPluginConfiguration {

	@Override
	public Plugin plugin() {
		return new Plugin("hbase", "hbase1", "magic-hbase1.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) -> mapping.registerController(new MagicHbaseController(configuration));
	}

	@Bean
	@ConditionalOnMissingBean
	public MagicDynamicHbaseClient magicDynamicHbaseClient() {
		return new MagicDynamicHbaseClient();
	}

	@Bean
	@ConditionalOnMissingBean
	public HbaseMagicResourceStorage hbaseMagicResourceStorage() {
		return new HbaseMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public HbaseMagicDynamicRegistry hbaseMagicDynamicRegistry(
			@Qualifier("hbaseMagicResourceStorage") HbaseMagicResourceStorage hbaseMagicResourceStorage,
			@Qualifier("magicDynamicHbaseClient") MagicDynamicHbaseClient magicDynamicHbaseClient) {
		return new HbaseMagicDynamicRegistry(hbaseMagicResourceStorage, magicDynamicHbaseClient);
	}

	@Bean(name = "magicHbase1Module")
	@ConditionalOnMissingBean
	public HbaseModule magicHbase1Module(
			@Qualifier("magicDynamicHbaseClient") MagicDynamicHbaseClient magicDynamicHbaseClient) {
		return new HbaseModule(magicDynamicHbaseClient);
	}
	
}
