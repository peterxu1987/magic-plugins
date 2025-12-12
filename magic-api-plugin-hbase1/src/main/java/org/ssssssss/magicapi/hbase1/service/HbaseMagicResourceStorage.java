package org.ssssssss.magicapi.hbase1.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.model.JsonCode;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.hbase1.model.HbaseInfo;
import org.ssssssss.magicapi.utils.JsonUtils;

public class HbaseMagicResourceStorage implements MagicResourceStorage<HbaseInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "hbase1";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<HbaseInfo> magicClass() {
		return HbaseInfo.class;
	}

	@Override
	public boolean requirePath() {
		return false;
	}

	@Override
	public boolean requiredScript() {
		return false;
	}

	@Override
	public boolean allowRoot() {
		return true;
	}

	@Override
	public String buildMappingKey(HbaseInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	@Override
	public void validate(HbaseInfo entity) {
		notBlank(entity.getKey(), DS_KEY_REQUIRED);
		notNull(entity.getProperties(), new JsonCode(1020, "Hbase1 propertie 不能为空"));
		
		Map<String, Object> properties = new HashMap<>(entity.getProperties());
		try {
			properties.get("zkUrls").toString();
			properties.get("hbaseRootdir").toString();
		 
		}catch(Exception e){
			e.printStackTrace();
			notNull(null, new JsonCode(6020, "Hbase1.x 配置参数 异常："+e.getMessage()));
		}

		boolean noneMatchKey = magicResourceService.listFiles("hbase1:0").stream()
				.map(it -> (HbaseInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, DS_KEY_CONFLICT);
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public HbaseInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, HbaseInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
