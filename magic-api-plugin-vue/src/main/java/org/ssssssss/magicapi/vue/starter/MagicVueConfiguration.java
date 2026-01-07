package org.ssssssss.magicapi.vue.starter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.vue.service.VueMagicDynamicRegistry;
import org.ssssssss.magicapi.vue.service.VueMagicResourceStorage;
import org.ssssssss.magicapi.vue.web.MagicVueController;

@Configuration
public class MagicVueConfiguration implements MagicPluginConfiguration {


	@Bean
	@ConditionalOnMissingBean
	public VueMagicResourceStorage vueMagicResourceStorage() {
		return new VueMagicResourceStorage();
	}
	
	private VueMagicDynamicRegistry _vueMagicDynamicRegistry;

	@Bean
	@ConditionalOnMissingBean
	public VueMagicDynamicRegistry vueMagicDynamicRegistry(
			@Qualifier("vueMagicResourceStorage") VueMagicResourceStorage vueMagicResourceStorage) {
		
		_vueMagicDynamicRegistry = new VueMagicDynamicRegistry(vueMagicResourceStorage);
		return _vueMagicDynamicRegistry;
	}

	@Override
	public Plugin plugin() {
		return new Plugin("VUE设计器", "vue", "magic-vue.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister(){
		return (mapping, configuration) -> mapping.registerController(new MagicVueController(configuration, _vueMagicDynamicRegistry));
	}

}
