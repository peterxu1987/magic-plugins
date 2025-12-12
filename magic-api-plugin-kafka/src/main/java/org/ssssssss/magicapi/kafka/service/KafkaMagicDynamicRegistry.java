package org.ssssssss.magicapi.kafka.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.kafka.model.KafkaInfo;
import org.ssssssss.magicapi.kafka.model.MagicDynamicKafkaClient;
import org.ssssssss.magicapi.kafka.util.KafkaDataSource;

public class KafkaMagicDynamicRegistry extends AbstractMagicDynamicRegistry<KafkaInfo> {

	private final MagicDynamicKafkaClient magicDynamicKafkaClient;

	private static final Logger logger = LoggerFactory.getLogger(KafkaMagicDynamicRegistry.class);

	public KafkaMagicDynamicRegistry(MagicResourceStorage<KafkaInfo> magicResourceStorage,
			MagicDynamicKafkaClient magicDynamicKafkaClient) {
		super(magicResourceStorage);
		this.magicDynamicKafkaClient = magicDynamicKafkaClient;
	}

	@EventListener(condition = "#event.type == 'kafka'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("注册kafka数据源失败", e);
		}
	}

	@Override
	protected boolean register(MappingNode<KafkaInfo> mappingNode) {
		KafkaInfo info = mappingNode.getEntity();
		try {
			KafkaDataSource kafkaDataSource = new KafkaDataSource(info);
			if(!kafkaDataSource.validate()) {
				return false;
			}
			magicDynamicKafkaClient.put(info.getId(), info.getKey(), info.getName(), kafkaDataSource);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	protected void unregister(MappingNode<KafkaInfo> mappingNode) {
		magicDynamicKafkaClient.delete(mappingNode.getEntity().getKey());
	}

	
}
