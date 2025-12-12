package org.ssssssss.magicapi.elasticsearch.service;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.elasticsearch.model.ESInfo;
import org.ssssssss.magicapi.elasticsearch.model.MagicDynamicESClient;
import org.ssssssss.magicapi.elasticsearch.util.ESDatasource;

public class ESMagicDynamicRegistry extends AbstractMagicDynamicRegistry<ESInfo> {

	private final MagicDynamicESClient magicDynamicESClient;

	private static final Logger logger = LoggerFactory.getLogger(ESMagicDynamicRegistry.class);

	public ESMagicDynamicRegistry(MagicResourceStorage<ESInfo> magicResourceStorage,
			MagicDynamicESClient magicDynamicESClient) {
		super(magicResourceStorage);
		this.magicDynamicESClient = magicDynamicESClient;
	}

	@EventListener(condition = "#event.type == 'es'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("注册elasticsearch数据源失败", e);
		}
	}

	@Override
	protected boolean register(MappingNode<ESInfo> mappingNode) {
		ESInfo info = mappingNode.getEntity();
		ESDatasource esDatasource = new ESDatasource(info);
		if(esDatasource.getRestHighLevelClient() == null) {
			return false;
		}
		magicDynamicESClient.put(info.getId(), info.getKey(), info.getName(), esDatasource);
		return true;
	}

	@Override
	protected void unregister(MappingNode<ESInfo> mappingNode) {
		try {
			magicDynamicESClient.delete(mappingNode.getEntity().getKey());
		} catch (IOException e) {
			e.printStackTrace();
			logger.error("注销 elasticsearch 数据源失败", e);
		}
	}

	
}
