package org.ssssssss.magicapi.hbase1.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.hbase1.HbaseModule;
import org.ssssssss.magicapi.hbase1.util.HbaseDataSource;
import org.ssssssss.magicapi.utils.Assert;
/**
 * 动态hbase客戶端对象
 */
public class MagicDynamicHbaseClient {

	private static final Logger logger = LoggerFactory.getLogger(MagicDynamicHbaseClient.class);

	private final Map<String, HbaseDataSource> dataSourceMap = new HashMap<>();
	private final Map<String, HbaseModule> hbaseModuleMap = new HashMap<>();
	 
	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param id             数据源ID
	 * @param dataSourceKey  数据源Key
	 * @param datasourceName 数据源名称
	 */
	public void put(String id, String dataSourceKey, String datasourceName, HbaseDataSource hbaseDataSource) {
		if (dataSourceKey == null) {
			dataSourceKey = "";
		}
		logger.info("注册hbase1.x数据源：{}", StringUtils.isNotBlank(dataSourceKey) ? dataSourceKey : "沒有注册任何数据源");
		this.dataSourceMap.put(dataSourceKey, hbaseDataSource);
		
		this.hbaseModuleMap.put(dataSourceKey, new HbaseModule(hbaseDataSource.getConnection()));
		
		if (id != null) {
			String finalDataSourceKey = dataSourceKey;
			this.dataSourceMap.entrySet().stream()
					.filter(it -> id.equals(it.getValue().getId()) && !finalDataSourceKey.equals(it.getKey()))
					.findFirst()
					.ifPresent(it -> {
						logger.info("移除hbase1.x旧数据源:{}", it.getKey());
						this.dataSourceMap.remove(it.getKey()).close();
						this.hbaseModuleMap.remove(it.getKey());
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
	public Collection<HbaseDataSource> datasourceNodes() {
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
			this.dataSourceMap.remove(datasourceKey).close();
			this.hbaseModuleMap.remove(datasourceKey);
			result = true;
		}
		logger.info("删除hbase1.x数据源：{}:{}", datasourceKey, result ? "成功" : "失败");
		return result;
	}

	/**
	 * 获取数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public HbaseDataSource getDataSource(String datasourceKey) {
		HbaseDataSource hbaseDataSource = dataSourceMap.get(datasourceKey);
		Assert.isNotNull(hbaseDataSource, String.format("找不到hbase1.x数据源%s", datasourceKey));
		return hbaseDataSource;
	}
	/**
	 * 获取module
	 *
	 * @param datasourceKey 数据源Key
	 */
	public HbaseModule getModule(String datasourceKey) {
		HbaseModule hbaseModule = hbaseModuleMap.get(datasourceKey);
		Assert.isNotNull(hbaseModule, String.format("找不到 hbase1.x 可用 module %s", datasourceKey));
		return hbaseModule;
	}
 
}
