package org.ssssssss.magicapi.tcp.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.tcp.TcpModule;
import org.ssssssss.magicapi.tcp.util.Datasource;
import org.ssssssss.magicapi.tcp.util.PortChecker;
import org.ssssssss.magicapi.tcp.util.TcpServer;
import org.ssssssss.magicapi.tcp.util.client.NettyClient;
import org.ssssssss.magicapi.tcp.util.server.NettyServer;
import org.ssssssss.magicapi.utils.Assert;

public class MagicDynamicTcpClient {

	private static final Logger logger = LoggerFactory.getLogger(MagicDynamicTcpClient.class);

	private final Map<String, TcpServer> dataSourceMap = new HashMap<>();
	private final Map<String, TcpModule> tcpModuleMap = new HashMap<>();

	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param id             数据源ID
	 * @param dataSourceKey  数据源Key
	 * @param datasourceName 数据源名称
	 * @throws Exception 
	 */
	public void put(String id, String dataSourceKey, String datasourceName, Datasource tcpDatasource) throws Exception {
		if (dataSourceKey == null) {
			dataSourceKey = "";
		}
		logger.info("注册tcp数据源：{}", StringUtils.isNotBlank(dataSourceKey) ? dataSourceKey : "没有匹配的数据源");
		
		Map<String, Object> properties = tcpDatasource.getProperties();
		String serverType = properties.get("serverType").toString();
		if(serverType.equals("server")) {
			String sslPassword = properties.get("sslPassword").toString();
			String certBasePath = properties.get("certBasePath").toString();
			int port = Integer.parseInt(properties.get("port").toString());
			
			if(PortChecker.isPortOccupied(port)) {
				throw new Exception(port+" 端口被占用!");
			}
			 
			Map<String,Object> attr = new HashMap<>();
			if(!sslPassword.equals("")){
				attr.put("sslPassword", sslPassword);
			}
			if(!certBasePath.equals("")){
				attr.put("certBasePath", certBasePath);
			}
			attr.put("port", port);
			NettyServer server = new NettyServer(tcpDatasource.getId(),port, attr);
			this.dataSourceMap.put(dataSourceKey, server);
			this.tcpModuleMap.put(dataSourceKey, new TcpModule(server, id));
		}else {
			String sslPassword = properties.get("sslPassword").toString();
			String certBasePath = properties.get("certBasePath").toString();
			String ip = properties.get("ip").toString();
			int port = Integer.parseInt(properties.get("port").toString());
			String vehId = properties.get("vehId").toString();
			String reconnect = properties.get("reconnect").toString();
			Map<String,Object> attr = new HashMap<>();
			if(!sslPassword.equals("")){
				attr.put("sslPassword", sslPassword);
			}
			if(!certBasePath.equals("")){
				attr.put("certBasePath", certBasePath);
			}
			if(!vehId.equals("")){
				attr.put("vehId", vehId);
			}
			if(!reconnect.equals("")){
				attr.put("reconnect", reconnect);
			}
			NettyClient client = new NettyClient(tcpDatasource.getId(),ip, port, attr);
			this.dataSourceMap.put(dataSourceKey, client);
			this.tcpModuleMap.put(dataSourceKey, new TcpModule(client, id));
		}
		
		
		if (id != null) {
			String finalDataSourceKey = dataSourceKey;
			this.dataSourceMap.entrySet().stream()
					.filter(it -> id.equals(it.getValue().getId()) && !finalDataSourceKey.equals(it.getKey()))
					.findFirst()
					.ifPresent(it -> {
						logger.info("移除tcp旧数据源:{}", it.getKey());
						this.dataSourceMap.remove(it.getKey()).shutdown();
						this.tcpModuleMap.remove(it.getKey());
					});
		}
	}

	/**
	 * 获取全部数据源
	 */
	public List<String> datasources() {
		return new ArrayList<>(this.dataSourceMap.keySet());
	}

	public boolean isEmpty() {
		return this.dataSourceMap.isEmpty();
	}

	/**
	 * 获取全部数据源
	 */
	public Collection<TcpServer> datasourceNodes() {
		return this.dataSourceMap.values();
	}

	/**
	 * 删除数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public boolean delete(String datasourceKey) {
		boolean result = false;
		// 检查参数是否合法
		if (datasourceKey != null && !datasourceKey.isEmpty()) {
			this.dataSourceMap.remove(datasourceKey).shutdown();
			this.tcpModuleMap.remove(datasourceKey);
			result = true;
		}
		logger.info("删除tcp数据源：{}:{}", datasourceKey, result ? "成功" : "失败");
		return result;
	}

	/**
	 * 获取tcp服务
	 *
	 * @param datasourceKey 数据源Key
	 */
	public TcpServer getTcpServer(String datasourceKey) {
		TcpServer tcpServer = dataSourceMap.get(datasourceKey);
		Assert.isNotNull(tcpServer, String.format("找不到tcp服务 %s", datasourceKey));
		return tcpServer;
	}
	public TcpModule getTcpModule(String datasourceKey) {
		TcpModule tcpModule = tcpModuleMap.get(datasourceKey);
		Assert.isNotNull(tcpModule, String.format("找不到tcp module %s", datasourceKey));
		return tcpModule;
	}
}
