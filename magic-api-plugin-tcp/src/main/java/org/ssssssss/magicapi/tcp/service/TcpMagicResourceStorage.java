package org.ssssssss.magicapi.tcp.service;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.ssssssss.magicapi.core.config.JsonCodeConstants;
import org.ssssssss.magicapi.core.model.JsonCode;
import org.ssssssss.magicapi.core.model.MagicEntity;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.core.service.MagicResourceStorage;
import org.ssssssss.magicapi.tcp.model.TcpInfo;
import org.ssssssss.magicapi.tcp.util.PortChecker;
import org.ssssssss.magicapi.utils.JsonUtils;

public class TcpMagicResourceStorage implements MagicResourceStorage<TcpInfo>, JsonCodeConstants {

	private MagicResourceService magicResourceService;

	@Override
	public String folder() {
		return "tcp";
	}

	@Override
	public String suffix() {
		return ".json";
	}

	@Override
	public Class<TcpInfo> magicClass() {
		return TcpInfo.class;
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
	public String buildMappingKey(TcpInfo info) {
		return String.format("%s-%s", info.getKey(), info.getUpdateTime());
	}

	/**
	 *
	 */
	@Override
	public void validate(TcpInfo entity) {
		notBlank(entity.getKey(), DS_KEY_REQUIRED);
		notBlank(entity.getType(), new JsonCode(1020, "tcp type 不能为空"));
		notNull(entity.getProperties(), new JsonCode(1020, "tcp propertie 不能为空"));
		
		Map<String, Object> properties = new HashMap<>(entity.getProperties());
	 
		try {
			properties.get("sslPassword").toString();
			properties.get("certBasePath").toString();
		}catch(Exception e){
			notNull(null, new JsonCode(6020, "tcp propertie 参数异常："+e.getMessage()));
		}
		
		if (entity.getType().equals("client")) {
			if(properties.get("ip") == null || properties.get("ip").toString().equals("")) {
				notNull(null, new JsonCode(6020, "tcp propertie 配置参数 ip 不能为 空!"));
			}
			if(properties.get("port") == null || properties.get("port").toString().equals("")) {
				notNull(null, new JsonCode(6020, "tcp propertie 配置参数 port 不能为 空!"));
			}
			if(properties.get("vehId") == null) {
				notNull(null, new JsonCode(6020, "tcp propertie 配置参数 vehId 不能为 空!"));
			}
			if(properties.get("reconnect") == null || 
					(
							!properties.get("reconnect").toString().equals("true") &&
							!properties.get("reconnect").toString().equals("false") 
					)) {
				notNull(null, new JsonCode(6020, "tcp propertie 配置参数 reconnect 不能为 空 且 必须 为 true/false!"));
			}
		}else if (entity.getType().equals("server")) {
			if(properties.get("port") == null || properties.get("port").toString().equals("")) {
				notNull(null, new JsonCode(6020, "tcp propertie 配置参数 port 不能为 空!"));
			}
			if(PortChecker.isPortOccupied(Integer.parseInt(properties.get("port").toString()))) {
				notNull(null, new JsonCode(1010, "port 端口被占用!"));
			}
		}

		boolean noneMatchKey = magicResourceService.listFiles("tcp:0").stream()
				.map(it -> (TcpInfo)it)
				.filter(it -> !it.getId().equals(entity.getId()))
				.noneMatch(it -> Objects.equals(it.getKey(), entity.getKey()));
		isTrue(noneMatchKey, DS_KEY_CONFLICT);
	}

	@Override
	public void setMagicResourceService(MagicResourceService magicResourceService) {
		this.magicResourceService = magicResourceService;
	}

	@Override
	public TcpInfo read(byte[] bytes) {
		return JsonUtils.readValue(bytes, TcpInfo.class);
	}

	@Override
	public byte[] write(MagicEntity entity) {
		return JsonUtils.toJsonBytes(entity);
	}
}
