package org.ssssssss.magicapi.tcp.starter;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.core.web.MagicControllerRegister;
import org.ssssssss.magicapi.tcp.TcpModule;
import org.ssssssss.magicapi.tcp.model.MagicDynamicTcpClient;
import org.ssssssss.magicapi.tcp.service.TcpMagicDynamicRegistry;
import org.ssssssss.magicapi.tcp.service.TcpMagicResourceStorage;
import org.ssssssss.magicapi.tcp.web.MagicTcpController;

@Configuration
public class MagicTcpConfiguration implements MagicPluginConfiguration {

	@Override
	public Plugin plugin() {
		return new Plugin("tcp协议", "tcp", "magic-tcp.1.0.0.iife.js");
	}

	@Override
	public MagicControllerRegister controllerRegister() {
		return (mapping, configuration) -> mapping.registerController(new MagicTcpController(configuration));
	}

	@Bean
	@ConditionalOnMissingBean
	public MagicDynamicTcpClient magicDynamicTcpClient() {
		return new MagicDynamicTcpClient();
	}

	@Bean
	@ConditionalOnMissingBean
	public TcpMagicResourceStorage tcpMagicResourceStorage() {
		return new TcpMagicResourceStorage();
	}

	@Bean
	@ConditionalOnMissingBean
	public TcpMagicDynamicRegistry tcpMagicDynamicRegistry(
			@Qualifier("tcpMagicResourceStorage") TcpMagicResourceStorage tcpMagicResourceStorage,
			@Qualifier("magicDynamicTcpClient") MagicDynamicTcpClient magicDynamicTcpClient) {
		return new TcpMagicDynamicRegistry(tcpMagicResourceStorage, magicDynamicTcpClient);
	}

	@Bean(name = "magicTcpModule")
	@ConditionalOnMissingBean
	public TcpModule magicTcpModule(
			@Qualifier("magicDynamicTcpClient")  MagicDynamicTcpClient magicDynamicTcpClient) {
		return new TcpModule(magicDynamicTcpClient);
	}

}
