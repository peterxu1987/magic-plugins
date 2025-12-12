package org.ssssssss.magicapi.hbase1.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.ssssssss.magicapi.core.event.FileEvent;
import org.ssssssss.magicapi.core.service.AbstractMagicDynamicRegistry;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.hbase1.model.HbaseInfo;
import org.ssssssss.magicapi.hbase1.model.MagicDynamicHbaseClient;
import org.ssssssss.magicapi.hbase1.util.HbaseDataSource;

public class HbaseMagicDynamicRegistry extends AbstractMagicDynamicRegistry<HbaseInfo> {

	private final MagicDynamicHbaseClient magicDynamicHbaseClient;

	private static final Logger logger = LoggerFactory.getLogger(HbaseMagicDynamicRegistry.class);

	public HbaseMagicDynamicRegistry(MagicResourceStorage<HbaseInfo> magicResourceStorage,
			MagicDynamicHbaseClient magicDynamicHbaseClient) {
		super(magicResourceStorage);
		this.magicDynamicHbaseClient = magicDynamicHbaseClient;
	}

	@EventListener(condition = "#event.type == 'hbase1'")
	public void onFileEvent(FileEvent event) {
		try {
			processEvent(event);
		} catch (Exception e) {
			logger.error("注册hbase1.x数据源失败", e);
		}
	}

	@Override
	protected boolean register(MappingNode<HbaseInfo> mappingNode) {
		HbaseInfo info = mappingNode.getEntity();
		try {
			HbaseDataSource hbaseDataSource = new HbaseDataSource(info);
			if(hbaseDataSource.getConnection() == null) {
				return false;
			}
			magicDynamicHbaseClient.put(info.getId(), info.getKey(), info.getName(), hbaseDataSource);
		}catch(Exception e) {
			e.printStackTrace();
			return false;
		}
		
		return true;
	}

	@Override
	protected void unregister(MappingNode<HbaseInfo> mappingNode) {
		magicDynamicHbaseClient.delete(mappingNode.getEntity().getKey());
	}

	
}
