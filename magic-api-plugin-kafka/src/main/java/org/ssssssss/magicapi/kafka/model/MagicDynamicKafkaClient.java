package org.ssssssss.magicapi.kafka.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.kafka.KafkaModule;
import org.ssssssss.magicapi.kafka.util.KafkaDataSource;
import org.ssssssss.magicapi.utils.Assert;
/**
 * 动态kafka客戶端对象
 */
public class MagicDynamicKafkaClient {

	private static final Logger logger = LoggerFactory.getLogger(MagicDynamicKafkaClient.class);

	private final Map<String, KafkaDataSource> dataSourceMap = new HashMap<>();
	private final Map<String, KafkaModule> kafkaModuleMap = new HashMap<>();
	 
	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param id             数据源ID
	 * @param dataSourceKey  数据源Key
	 * @param datasourceName 数据源名称
	 */
	public void put(String id, String dataSourceKey, String datasourceName, KafkaDataSource kafkaDataSource) {
		this.dataSourceMap.put(dataSourceKey, kafkaDataSource);
		
		this.kafkaModuleMap.put(dataSourceKey, new KafkaModule(kafkaDataSource));
		
		if (id != null) {
			String finalDataSourceKey = dataSourceKey;
			this.dataSourceMap.entrySet().stream()
					.filter(it -> id.equals(it.getValue().getId()) && !finalDataSourceKey.equals(it.getKey()))
					.findFirst()
					.ifPresent(it -> {
						logger.info("移除kafka旧数据源:{}", it.getKey());
						this.dataSourceMap.remove(it.getKey()).close();
						this.kafkaModuleMap.remove(it.getKey());
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
	public Collection<KafkaDataSource> datasourceNodes() {
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
			this.kafkaModuleMap.remove(datasourceKey);
			result = true;
		}
		logger.info("删除kafka数据源：{}:{}", datasourceKey, result ? "成功" : "失败");
		return result;
	}

	/**
	 * 获取数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public KafkaDataSource getDataSource(String datasourceKey) {
		KafkaDataSource kafkaDataSource = dataSourceMap.get(datasourceKey);
		Assert.isNotNull(kafkaDataSource, String.format("找不到kafka数据源%s", datasourceKey));
		return kafkaDataSource;
	}
	/**
	 * 获取module
	 *
	 * @param datasourceKey 数据源Key
	 */
	public KafkaModule getModule(String datasourceKey) {
		KafkaModule kafkaModule = kafkaModuleMap.get(datasourceKey);
		Assert.isNotNull(kafkaModule, String.format("找不到kafka 可用 module %s", datasourceKey));
		return kafkaModule;
	}
 
}
