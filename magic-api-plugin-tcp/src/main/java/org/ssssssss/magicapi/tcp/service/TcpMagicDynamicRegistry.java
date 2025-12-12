package org.ssssssss.magicapi.tcp.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.tcp.model.MagicDynamicTcpClient;
import org.ssssssss.magicapi.tcp.model.TcpInfo;
import org.ssssssss.magicapi.tcp.util.Datasource;
import org.ssssssss.magicapi.tcp.util.TcpClientDatasource;
import org.ssssssss.magicapi.tcp.util.TcpServerDatasource;

public class TcpMagicDynamicRegistry extends AbstractMagicDynamicRegistry<TcpInfo> {

	private final MagicDynamicTcpClient magicDynamicTcpClient;

	private static final Logger logger = LoggerFactory.getLogger(TcpMagicDynamicRegistry.class);

	public TcpMagicDynamicRegistry(MagicResourceStorage<TcpInfo> magicResourceStorage,
			MagicDynamicTcpClient magicDynamicTcpClient) {
		super(magicResourceStorage);
		this.magicDynamicTcpClient = magicDynamicTcpClient;
	}

	@EventListener(condition = "#event.type == 'tcp'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("注册tcp数据源失败", e);
		}
	}

	@Override
	protected boolean register(MappingNode<TcpInfo> mappingNode) {
		TcpInfo info = mappingNode.getEntity();
		Datasource tcpDatasource = null;
		if(info.getType().equals("client")) {
			tcpDatasource = new TcpClientDatasource(info);
		}else {
			tcpDatasource = new TcpServerDatasource(info);
		}
		try {
			magicDynamicTcpClient.put(info.getId(), info.getKey(), info.getName(), tcpDatasource);
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		
	}

	@Override
	protected void unregister(MappingNode<TcpInfo> mappingNode) {
		magicDynamicTcpClient.delete(mappingNode.getEntity().getKey());
	}

	
}
