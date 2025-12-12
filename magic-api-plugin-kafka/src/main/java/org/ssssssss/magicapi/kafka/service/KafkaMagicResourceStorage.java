package org.ssssssss.magicapi.kafka.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.model.JsonCode;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.kafka.model.KafkaInfo;
import org.ssssssss.magicapi.utils.JsonUtils;

public class KafkaMagicResourceStorage implements MagicResourceStorage<KafkaInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "kafka";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<KafkaInfo> magicClass() {
		return KafkaInfo.class;
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
	public String buildMappingKey(KafkaInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	@Override
	public void validate(KafkaInfo entity) {
		notBlank(entity.getKey(), DS_KEY_REQUIRED);
		notNull(entity.getProperties(), new JsonCode(1020, "kafka propertie 不能为空"));
		
		Map<String, Object> properties = new HashMap<>(entity.getProperties());
	 
		try {
			properties.get("serverConfig").toString();
			Integer.parseInt(properties.get("batchSize").toString());
			Integer.parseInt(properties.get("bufferMemory").toString());
			Integer.parseInt(properties.get("linger").toString());
			properties.get("autoOffsetReset").toString();
			properties.get("valueDeserializer").toString();
		}catch(Exception e){
			e.printStackTrace();
			notNull(null, new JsonCode(6020, "Kafka 配置参数 异常："+e.getMessage()));
		}

		boolean noneMatchKey = magicResourceService.listFiles("kafka:0").stream()
				.map(it -> (KafkaInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, DS_KEY_CONFLICT);
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public KafkaInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, KafkaInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
