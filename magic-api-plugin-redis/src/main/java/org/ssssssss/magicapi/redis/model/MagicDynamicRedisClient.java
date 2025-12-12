package org.ssssssss.magicapi.redis.model;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.redisson.api.RedissonClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.ssssssss.magicapi.redis.RedisModule;
import org.ssssssss.magicapi.utils.Assert;

/**
 * 动态redis客戶端对象
 */
public class MagicDynamicRedisClient {

	private static final Logger logger = LoggerFactory.getLogger(MagicDynamicRedisClient.class);

	private final Map<String, RedissonClient> dataSourceMap = new HashMap<>();
	private final Map<String, RedisModule> redisModuleMap = new HashMap<>();

	/**
	 * 注册数据源（可以运行时注册）
	 *
	 * @param id             数据源ID
	 * @param dataSourceKey  数据源Key
	 * @param datasourceName 数据源名称
	 */
	public void put(String id, String dataSourceKey, String datasourceName, RedissonClient redissonClient) {
		if (dataSourceKey == null) {
			dataSourceKey = "";
		}
		logger.info("注册緩存数据源：{}", StringUtils.isNotBlank(dataSourceKey) ? dataSourceKey : "default");
		this.dataSourceMap.put(dataSourceKey, redissonClient);
		this.redisModuleMap.put(dataSourceKey, new RedisModule(redissonClient));
		if (id != null) {
			String finalDataSourceKey = dataSourceKey;
			this.dataSourceMap.entrySet().stream()
					.filter(it -> id.equals(it.getValue().getId()) && !finalDataSourceKey.equals(it.getKey()))
					.findFirst()
					.ifPresent(it -> {
						logger.info("移除redis旧数据源:{}", it.getKey());
						this.dataSourceMap.remove(it.getKey()).shutdown();
						this.redisModuleMap.remove(it.getKey());
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
	public Collection<RedissonClient> datasourceNodes() {
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
			this.redisModuleMap.remove(datasourceKey);
			result = true;
		}
		logger.info("删除redis数据源：{}:{}", datasourceKey, result ? "成功" : "失败");
		return result;
	}

	/**
	 * 获取数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public RedissonClient getDataSource(String datasourceKey) {
		RedissonClient redissonClient = dataSourceMap.get(datasourceKey);
		Assert.isNotNull(redissonClient, String.format("找不到redis数据源%s", datasourceKey));
		return redissonClient;
	}
	/**
	 * 获取数据源
	 *
	 * @param datasourceKey 数据源Key
	 */
	public RedisModule getRedisModule(String datasourceKey) {
		RedisModule redisModule = redisModuleMap.get(datasourceKey);
		Assert.isNotNull(redisModule, String.format("找不到 RedisModule %s", datasourceKey));
		return redisModule;
	}
}
