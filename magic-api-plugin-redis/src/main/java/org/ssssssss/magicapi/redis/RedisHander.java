package org.ssssssss.magicapi.redis;

import java.util.List;
import java.util.Map;

public interface RedisHander {
	@FunctionalInterface
	interface Exec {
		void exec() throws Exception;
	}
	@FunctionalInterface
	interface Keys {
		List<String> getKeys();
	}
	@FunctionalInterface
	interface RowDataByKey {
		Map<String, String> getRowDataByKey(String key);
	}
	@FunctionalInterface
	interface Insert {
		void insert(String key, Object value);
	}
	@FunctionalInterface
	interface Remove {
		void remove(String key);
	}
}