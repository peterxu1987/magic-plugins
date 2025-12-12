package org.ssssssss.magicapi.elasticsearch.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.model.JsonCode;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.elasticsearch.model.ESInfo;
import org.ssssssss.magicapi.utils.JsonUtils;

public class ESMagicResourceStorage implements MagicResourceStorage<ESInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "es";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<ESInfo> magicClass() {
		return ESInfo.class;
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
	public String buildMappingKey(ESInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	@Override
	public void validate(ESInfo entity) {
		notBlank(entity.getKey(), DS_KEY_REQUIRED);
		notNull(entity.getProperties(), new JsonCode(1020, "elasticsearch propertie 不能为空"));
		
		Map<String, Object> properties = new HashMap<>(entity.getProperties());
	 
		try {
			properties.get("address").toString();
			properties.get("username").toString();
			properties.get("password").toString();
			Integer.parseInt(properties.get("connectTimeout").toString());
			Integer.parseInt(properties.get("socketTimeout").toString());
			Integer.parseInt(properties.get("connectionRequestTimeout").toString());
			Integer.parseInt(properties.get("maxConnTotal").toString());
			Integer.parseInt(properties.get("maxConnPerRoute").toString());
			
		}catch(Exception e){
			e.printStackTrace();
			notNull(null, new JsonCode(6020, "elasticsearch 配置参数 异常："+e.getMessage()));
		}
		boolean noneMatchKey = magicResourceService.listFiles("es:0").stream()
				.map(it -> (ESInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, DS_KEY_CONFLICT);
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public ESInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, ESInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
