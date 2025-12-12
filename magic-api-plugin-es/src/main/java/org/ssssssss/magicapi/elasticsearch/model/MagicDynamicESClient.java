package org.ssssssss.magicapi.elasticsearch.model;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.elasticsearch.ESModule;
import org.ssssssss.magicapi.elasticsearch.util.ESDatasource;
import org.ssssssss.magicapi.utils.Assert;

/**
 * 动态es客戶端对象
 */
public class MagicDynamicESClient {

	private static final Logger logger = LoggerFactory.getLogger(MagicDynamicESClient.class);

	private final Map<String, ESDatasource> dataSourceMap = new HashMap<>();
	private final Map<String, ESModule> esModuleMap = new HashMap<>();

	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param id             数据源ID
	 * @param dataSourceKey  数据源Key
	 * @param datasourceName 数据源名称
	 */
	public void put(String id, String dataSourceKey, String datasourceName, ESDatasource esDatasource) {
		if (esDatasource == null) {
			dataSourceKey = "";
		}
		logger.info("注册elasticsearch数据源：{}", StringUtils.isNotBlank(dataSourceKey) ? dataSourceKey : "沒有注册任何数据源");
		this.dataSourceMap.put(dataSourceKey, esDatasource);
		this.esModuleMap.put(dataSourceKey, new ESModule(esDatasource.getRestHighLevelClient()));
		if (id != null) {
			String finalDataSourceKey = dataSourceKey;
			this.dataSourceMap.entrySet().stream()
					.filter(it -> id.equals(it.getValue().getId()) && !finalDataSourceKey.equals(it.getKey()))
					.findFirst()
					.ifPresent(it -> {
						logger.info("移除旧elasticsearch数据源:{}", it.getKey());
						try {
							this.dataSourceMap.remove(it.getKey()).close();
						} catch (IOException e) {
							e.printStackTrace();
							logger.error("注销 elasticsearch 数据源失败", e);
						}
						this.esModuleMap.remove(it.getKey());
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
	public Collection<ESDatasource> datasourceNodes() {
		return this.dataSourceMap.values();
	}

	/**
	 * 删除数据源
	 *
	 * @param datasourceKey 数据源Key
	 * @throws IOException 
	 */
	public boolean delete(String datasourceKey) throws IOException {
		boolean result = false;
		// 检查参数是否合法
		if (datasourceKey != null && !datasourceKey.isEmpty()) {
			this.dataSourceMap.remove(datasourceKey).close();
			this.esModuleMap.remove(datasourceKey);
			result = true;
		}
		logger.info("删除elasticsearch数据源：{}:{}", datasourceKey, result ? "成功" : "失败");
		return result;
	}

	/**
	 * 获取数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public ESDatasource getDataSource(String datasourceKey) {
		ESDatasource esDatasource = dataSourceMap.get(datasourceKey);
		Assert.isNotNull(esDatasource, String.format("找不到elasticsearch数据源%s", datasourceKey));
		return esDatasource;
	}
	/**
	 * 获取数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public ESModule getESModule(String datasourceKey) {
		ESModule esModule = esModuleMap.get(datasourceKey);
		Assert.isNotNull(esModule, String.format("找不到 ESModule %s", datasourceKey));
		return esModule;
	}
}
