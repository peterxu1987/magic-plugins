package org.ssssssss.magicapi.redis.util;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.ssssssss.magicapi.redis.model.RedisInfo;

public class RedisUtil {
	
	public static RedissonClient extracted(RedisInfo info) {
		RedissonClient redissonClient = null;
		Map<String, Object> properties = new HashMap<>(info.getProperties());
		@SuppressWarnings("unchecked")
		Map<String, Object> poolMap = (Map<String, Object>)properties.get("pool");
		Pool pool = new Pool(
			Integer.parseInt(poolMap.get("max-active").toString())
		   ,Integer.parseInt(poolMap.get("max-idle").toString())
		   ,Integer.parseInt(poolMap.get("min-idle").toString())
		   ,Integer.parseInt(poolMap.get("max-wait").toString())
		);
		if (info.getType().equals("single")) {
			SingleServerConfig config = new SingleServerConfig(
					info.getKey()
				   ,properties.get("host").toString()
				   ,Integer.parseInt(properties.get("port").toString())
				   ,properties.get("password").toString()
				   ,Integer.parseInt(properties.get("database").toString())
				   ,pool
			);
			redissonClient = createRedissonClient(config, null, null);
		}else if (info.getType().equals("sentinel")) {
			@SuppressWarnings("unchecked")
			List<String> nodes = (List<String>) properties.get("nodes");
			SentinelServerConfig config = new SentinelServerConfig(
					info.getKey()
				   ,properties.get("master").toString()
				   ,nodes
				   ,properties.get("password").toString()
				   ,pool
			);
			redissonClient = createRedissonClient(null, config, null);
		}else if (info.getType().equals("cluster")) {
			@SuppressWarnings("unchecked")
			List<String> nodes = (List<String>) properties.get("nodes");
			ClusterServerConfig config = new ClusterServerConfig(
					info.getKey()
				   ,nodes
				   ,properties.get("password").toString()
				   ,pool
			);
			redissonClient = createRedissonClient(null, null, config);
		}
		return redissonClient;
	}
	
	private static RedissonClient createRedissonClient(SingleServerConfig singleConfig, SentinelServerConfig sentinelConfig, ClusterServerConfig clusterConfig) {
		Config config = new Config();

		if (singleConfig != null) {
			config.useSingleServer().setAddress("redis://" + singleConfig.getHost() + ":" + singleConfig.getPort())
					.setPassword(singleConfig.getPassword()).setDatabase(singleConfig.getDatabase())
					.setConnectionPoolSize(singleConfig.getPool().getMaxActive())
					.setConnectionMinimumIdleSize(singleConfig.getPool().getMinIdle());
		} else if (sentinelConfig != null) {
			config.useSentinelServers().setMasterName(sentinelConfig.getMaster())
					.addSentinelAddress(
							sentinelConfig.getNodes().stream().map(node -> "redis://" + node).toArray(String[]::new))
					.setPassword(sentinelConfig.getPassword())
					.setMasterConnectionPoolSize(sentinelConfig.getPool().getMaxActive())
					.setSlaveConnectionPoolSize(sentinelConfig.getPool().getMaxIdle())
					.setMasterConnectionMinimumIdleSize(sentinelConfig.getPool().getMinIdle())
					.setSlaveConnectionMinimumIdleSize(sentinelConfig.getPool().getMinIdle());
		} else if (clusterConfig != null) {
			config.useClusterServers()
					.addNodeAddress(
							clusterConfig.getNodes().stream().map(node -> "redis://" + node).toArray(String[]::new))
					.setPassword(clusterConfig.getPassword())
					.setMasterConnectionPoolSize(clusterConfig.getPool().getMaxActive())
					.setSlaveConnectionPoolSize(clusterConfig.getPool().getMaxIdle())
					.setMasterConnectionMinimumIdleSize(clusterConfig.getPool().getMinIdle())
					.setSlaveConnectionMinimumIdleSize(clusterConfig.getPool().getMinIdle());
		}

		return Redisson.create(config);
	}

	public static class SingleServerConfig {
		private String name;
		private String host;
		private int port;
		private String password;
		private int database;
		private Pool pool;
		
		public SingleServerConfig(String name, String host, int port, String password, int database, Pool pool) {
			this.name = name;
			this.host = host;
			this.port = port;
			this.password = password;
			this.database = database;
			this.pool = pool;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getHost() {
			return host;
		}

		public void setHost(String host) {
			this.host = host;
		}

		public int getPort() {
			return port;
		}

		public void setPort(int port) {
			this.port = port;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public int getDatabase() {
			return database;
		}

		public void setDatabase(int database) {
			this.database = database;
		}

		public Pool getPool() {
			return pool;
		}

		public void setPool(Pool pool) {
			this.pool = pool;
		}
	}

	public static class SentinelServerConfig {
		private String name;
		private String master;
		private List<String> nodes;
		private String password;
		private Pool pool;
		
		public SentinelServerConfig(String name, String master, List<String> nodes, String password, Pool pool) {
			super();
			this.name = name;
			this.master = master;
			this.nodes = nodes;
			this.password = password;
			this.pool = pool;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getMaster() {
			return master;
		}

		public void setMaster(String master) {
			this.master = master;
		}

		public List<String> getNodes() {
			return nodes;
		}

		public void setNodes(List<String> nodes) {
			this.nodes = nodes;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Pool getPool() {
			return pool;
		}

		public void setPool(Pool pool) {
			this.pool = pool;
		}

	}

	public static class ClusterServerConfig {
		private String name;
		private List<String> nodes;
		private String password;
		private Pool pool;
		
		public ClusterServerConfig(String name, List<String> nodes, String password, Pool pool) {
			super();
			this.name = name;
			this.nodes = nodes;
			this.password = password;
			this.pool = pool;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getNodes() {
			return nodes;
		}

		public void setNodes(List<String> nodes) {
			this.nodes = nodes;
		}

		public String getPassword() {
			return password;
		}

		public void setPassword(String password) {
			this.password = password;
		}

		public Pool getPool() {
			return pool;
		}

		public void setPool(Pool pool) {
			this.pool = pool;
		}

	}

	public static class Pool {
		private int maxActive;
		private int maxIdle;
		private int minIdle;
		private int maxWait;
		
		public Pool(int maxActive, int maxIdle, int minIdle, int maxWait) {
			super();
			this.maxActive = maxActive;
			this.maxIdle = maxIdle;
			this.minIdle = minIdle;
			this.maxWait = maxWait;
		}

		public int getMaxActive() {
			return maxActive;
		}

		public void setMaxActive(int maxActive) {
			this.maxActive = maxActive;
		}

		public int getMaxIdle() {
			return maxIdle;
		}

		public void setMaxIdle(int maxIdle) {
			this.maxIdle = maxIdle;
		}

		public int getMinIdle() {
			return minIdle;
		}

		public void setMinIdle(int minIdle) {
			this.minIdle = minIdle;
		}

		public int getMaxWait() {
			return maxWait;
		}

		public void setMaxWait(int maxWait) {
			this.maxWait = maxWait;
		}
	}

}
