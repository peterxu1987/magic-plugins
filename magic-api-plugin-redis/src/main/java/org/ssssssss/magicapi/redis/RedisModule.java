package org.ssssssss.magicapi.redis;

import java.beans.Transient;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.redisson.api.LocalCachedMapOptions;
import org.redisson.api.MapOptions.WriteMode;
import org.redisson.api.RAtomicLong;
import org.redisson.api.RBucket;
import org.redisson.api.RList;
import org.redisson.api.RLocalCachedMap;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RMapCache;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RScoredSortedSet;
import org.redisson.api.RSet;
import org.redisson.api.RTopic;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.redisson.api.listener.MessageListener;
import org.redisson.api.map.MapLoader;
import org.redisson.api.map.MapWriter;
import org.redisson.client.codec.StringCodec;
import org.redisson.client.protocol.ScoredEntry;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.redis.model.MagicDynamicRedisClient;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * redis模块
 *
 * @author xuhaiyang
 */
@MagicModule("redis")
public class RedisModule implements DynamicAttribute<RedisModule, RedisModule>{
	
	private MagicDynamicRedisClient magicDynamicRedisClient;
	private RedissonClient redisson;

	public RedisModule(MagicDynamicRedisClient magicDynamicRedisClient) {
		this.magicDynamicRedisClient = magicDynamicRedisClient;
	}
	
	public RedisModule(RedissonClient redissonClient) {
		this.redisson = redissonClient;
	}
	
	/**
	 * 数据源切换
	 */
	@Override
	@Transient
	public RedisModule getDynamicAttribute(String key) {
		return magicDynamicRedisClient.getRedisModule(key);
	}

    @Comment("获取字符串类型缓存值")
    public String getString(
    		@Comment(name = "redisKey", value = "redisKey") String redisKey) {
        RBucket<String> bucket = redisson.getBucket(redisKey, StringCodec.INSTANCE);
        return bucket.get();
    }
 
    @Comment("设置字符串类型缓存值")
    public void putString(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "value", value = "存储的字符串信息") String value) {
        RBucket<String> bucket = redisson.getBucket(redisKey, StringCodec.INSTANCE);
        bucket.set(value);
    }

    @Comment("设置字符串类型缓存值并给值添加过期时间")
    public void putString(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "value", value = "存储的字符串信息") String value,
    		@Comment(name = "expired", value = "存储的字符串过期时间 单位秒（-1：为永久有效）") long expired) {
        RBucket<String> bucket = redisson.getBucket(redisKey, StringCodec.INSTANCE);
        if(expired != -1) {
        	 bucket.set(value, expired <= 0? 300: expired, TimeUnit.SECONDS);
        }else {
        	bucket.set(value);
        }
    }

    /**
     * 如果不存在则写入缓存
     *
     * @param key     缓存key
     * @param value   缓存值
     * @param expired 缓存过期时间
     * 		0：会默认保存300秒
     */
    @Comment("当redisKey不存在时, 设置字符串类型缓存值并给值添加过期时间")
    public boolean putStringIfAbsent(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "value", value = "存储的字符串信息") String value,
    		@Comment(name = "expired", value = "存储的字符串过期时间 单位秒（-1：为永久有效）") long expired) {
        RBucket<String> bucket = redisson.getBucket(redisKey, StringCodec.INSTANCE);
        if(expired != -1) {
        	 return bucket.trySet(value,  expired <= 0? 300: expired, TimeUnit.SECONDS);
        }
        return bucket.trySet(value);
    }

    /**
     * 如果不存在则写入缓存（永久保存）
     *
     * @param key   缓存key
     * @param value 缓存值
     */
    @Comment("当redisKey不存在时, 设置字符串类型缓存值")
    public boolean putStringIfAbsent(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "value", value = "存储的字符串信息") String value) {
        RBucket<String> bucket = redisson.getBucket(redisKey, StringCodec.INSTANCE);
        return bucket.trySet(value);
    }
    
    /**
     * 计数器自增（+1），并返回计算前的原值
     * 如果key不存在则按当前值为0计算
     *
     * @param key     缓存key
     * @param expired 过期时间
     */
    @Comment("计数器自增（+1），并返回计算前的原值 \n\n"
    		+ "如果key不存在则按当前值为0计算")
    public long getAndIncrement(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "expired", value = "存储的字符串过期时间 单位秒（-1：为永久有效）") long expired) {
        RAtomicLong atomicLong = redisson.getAtomicLong(redisKey);
        long num = atomicLong.getAndIncrement();
        if(expired != -1) {
        	atomicLong.expire( expired <= 0? 300: expired, TimeUnit.SECONDS);
        }
        return num;
    }

    /**
     * 计数器累加指定的值，并返回计算前的原值
     * 如果key不存在则按当前值为0计算
     *
     * @param key     缓存key
     * @param expired 过期时间
     * @param delta   本次累加的值
     */
    @Comment("计数器累加指定的值，并返回计算前的原值 \n\n"
    		+ "如果key不存在则按当前值为0计算")
    public long getAndIncrement(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "delta", value = "每次累加的数") long delta,
    		@Comment(name = "expired", value = "存储的字符串过期时间 单位秒（-1：为永久有效）") long expired) {
        RAtomicLong atomicLong = redisson.getAtomicLong(redisKey);
        long num = atomicLong.getAndAdd(delta);
        if(expired != -1) {
        	atomicLong.expire( expired <= 0? 300: expired, TimeUnit.SECONDS);
        }
        return num;
    }
    
    @Comment("获取map存储对象")
    public RMap<String, Object> getMap(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "expired", value = "存储值的过期时间 单位秒（ <= 0：为永久有效 ）") long expired
    		) {
    	
    	RMap<String, Object> map = redisson.getMap(redisKey);//, JsonJacksonCodec.INSTANCE
    	if(!exists(redisKey) && expired > 0) {
    	    map.expire( expired, TimeUnit.SECONDS);
    	}
        return map;
    }
    
    @Comment("保存或者更新Map数据")
    public void saveOrUpdateMap(
    		@Comment(name = "rmap", value = "redis的rmap对象") RMap<String, Object> rmap, 
    		@Comment(name = "mapKey", value = "mapKey") String mapKey, 
    		@Comment(name = "mapValue", value = "mapValue 可以是字符串、json对象等") Object mapValue) {
    	rmap.put(mapKey, mapValue);
    }
    
    @Comment("删除map数据")
    public void delMapByMapkey(
    		@Comment(name = "rmap", value = "redis的rmap对象") RMap<String, Object> rmap,
    		@Comment(name = "mapKeyPattern", value = "匹配的正则表达式")String mapKeyPattern) {
 
    	Set<Entry<String, Object>> entrySet = rmap.entrySet(mapKeyPattern);
        for (Entry<String, Object> entry : entrySet) {
        	rmap.remove(entry.getKey());
        }
    }

    @Comment("模糊查询,获取Map数据")
    public Map<String, Object> getMapByMapkey(
    		@Comment(name = "rmap", value = "redis的rmap对象") RMap<String, Object> rmap,
    		@Comment(name = "mapKeyPattern", value = "匹配的正则表达式")String mapKeyPattern) {
    	Set<Entry<String, Object>> entrySet = rmap.entrySet(mapKeyPattern);
        Map<String, Object> map = new HashMap<>();
        for (Entry<String, Object> entry : entrySet) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
    
    @Comment("获取带有单行数据有效器的Map对象")
    public RMapCache<String, Object> getMapCache(
    		@Comment(name = "redisKey", value = "redisKey") String redisKey) {
    	return redisson.getMapCache(redisKey);
    }
    
    @Comment("保存或者更新带有单行数据有效器的Map中的数据")
    public void saveOrUpdateMapCache(
    		@Comment(name = "rmapCache", value = "redis的rmapCache对象") RMapCache<String, Object> rmapCache, 
    		@Comment(name = "mapKey", value = "mapKey") String mapKey, 
    		@Comment(name = "mapValue", value = "mapValue 可以是字符串、json对象等") Object mapValue,
    		@Comment(name = "expired", value = "存储值的过期时间 单位秒（ <= 0：为永久有效 ）") long expired
    		) {
    	if(expired > 0 ) {
    		rmapCache.put(mapKey, mapValue, expired, TimeUnit.SECONDS);
    	}else {
    		rmapCache.put(mapKey, mapValue);
    	}
    }
    
    @Comment("删除带有单行数据有效器的Map中的数据")
    public void delMapCacheByMapkey(
    		@Comment(name = "rmapCache", value = "redis的rmapCache对象") RMapCache<String, Object> rmapCache,
    		@Comment(name = "mapKeyPattern", value = "匹配的正则表达式")String mapKeyPattern) {
 
    	Set<Entry<String, Object>> entrySet = rmapCache.entrySet(mapKeyPattern);
        for (Entry<String, Object> entry : entrySet) {
        	rmapCache.remove(entry.getKey());
        }
    }

    @Comment("模糊查询,获取带有单行数据有效器的Map中的数据")
    public Map<String, Object> getMapCacheByMapkey(
    		@Comment(name = "rmapCache", value = "redis的rmapCache对象") RMap<String, Object> rmapCache,
    		@Comment(name = "mapKeyPattern", value = "匹配的正则表达式")String mapKeyPattern) {
    	Set<Entry<String, Object>> entrySet = rmapCache.entrySet(mapKeyPattern);
        Map<String, Object> map = new HashMap<>();
        for (Entry<String, Object> entry : entrySet) {
            map.put(entry.getKey(), entry.getValue());
        }
        return map;
    }
    
    @Comment("获取List<String>类型的全量数据")
    public RList<Object> getList(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "expired", value = "存储值的过期时间 单位秒（ <= 0：为永久有效 ）") long expired) {
    	RList<Object> list =  redisson.getList(redisKey);//, JsonJacksonCodec.INSTANCE
    	if(!exists(redisKey) && expired > 0) {
    		list.expire( expired, TimeUnit.SECONDS);
    	}
        return list;
    }
    
    
    @Comment("写入List<Object>类型数据")
    public void addList(@Comment(name = "rList", value = "rList 对象") RList<Object> rList,
	@Comment(name = "value", value = "写入数据") Object value) {
    	rList.add(value);
    }
    
    /**
     * 移除缓存
     * @throws JsonProcessingException 
     */
    @Comment("删除List中的数据")
    public void delList(@Comment(name = "rList", value = "rList 对象") RList<Object> rList,
    		@Comment(name = "valuePattern", value = "匹配字符串状态下的value，使用正则表达式\n\n 如：.*yourPattern.*")String valuePattern) throws JsonProcessingException {
    	// 模糊查询并删除指定数据
        Pattern regex = Pattern.compile(valuePattern);// 替换为你的匹配模式

        Iterator<Object> iterator = rList.iterator();
        while (iterator.hasNext()) {
        	// 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            String element = objectMapper.writeValueAsString(iterator.next());
            if (regex.matcher(element).matches()) {
                iterator.remove(); // 删除符合条件的元素
            }
        }
    }
    
    @Comment("模糊查询,List中的数据")
    public  List<Object> getListByValStr(@Comment(name = "rList", value = "rList 对象") RList<Object> rList,
    		@Comment(name = "valuePattern", value = "匹配字符串状态下的value，使用正则表达式\n\n 如：.*yourPattern.*")String valuePattern) throws JsonProcessingException{
    	// 模糊查询并删除指定数据
        Pattern regex = Pattern.compile(valuePattern);// 替换为你的匹配模式
        List<Object> result = new ArrayList<>();
        Iterator<Object> iterator = rList.iterator();
        while (iterator.hasNext()) {
        	// 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            Object elem = iterator.next();
            String element = objectMapper.writeValueAsString(elem);
            if (regex.matcher(element).matches()) {
                result.add(elem);
            }
        }
        return result;
    }
    

    /**
     * 判断缓存是否存在
     */
    @Comment("判断缓存是否存在")
    public boolean exists(@Comment(name = "redisKey", value = "redisKey") String redisKey) {
        return redisson.getBucket(redisKey).isExists();
    }
    
    
    @Comment("获取不含score的Set")
    public  RSet<Object> getSet(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "expired", value = "value值的过期时间 （<= 0：为永久有效）") long expired) {
    	RSet<Object> setObj = redisson.getSet(redisKey);
    	if(expired > 0) {
    		setObj.expire(expired, TimeUnit.SECONDS);
        }
    	return setObj;
    }
    
    @Comment("写入不含score的Set<Object>数据")
    public void addSet(@Comment(name = "rSet", value = "不含score的RSet对象") RSet<Object> rSet,
    		@Comment(name = "value", value = "写入的字符串信息") Object value) {
    	rSet.add(value);
    }
    
    @Comment("删除不含score的Set<Object>数据")
    public void delSet(@Comment(name = "rSet", value = "不含score的RSet对象") RSet<Object> rSet,
    		@Comment(name = "valuePattern", value = "匹配字符串状态下的value，使用正则表达式\n\n 如：.*yourPattern.*")String valuePattern) throws JsonProcessingException {
    	// 模糊查询并删除指定数据
        Pattern regex = Pattern.compile(valuePattern);// 替换为你的匹配模式

        Iterator<Object> iterator = rSet.iterator();
        while (iterator.hasNext()) {
        	// 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            String element = objectMapper.writeValueAsString(iterator.next());
            if (regex.matcher(element).matches()) {
                iterator.remove(); // 删除符合条件的元素
            }
        }
    }
    
    @Comment("模糊查询,不含score的Set中的数据")
    public  Set<Object> getSetByValStr(@Comment(name = "rSet", value = "不含score的RSet对象") RSet<Object> rSet,
    		@Comment(name = "valuePattern", value = "匹配字符串状态下的value，使用正则表达式\n\n 如：.*yourPattern.*")String valuePattern) throws JsonProcessingException{
    	// 模糊查询并删除指定数据
        Pattern regex = Pattern.compile(valuePattern);// 替换为你的匹配模式
        Set<Object> result = new HashSet<>();
        Iterator<Object> iterator = rSet.iterator();
        while (iterator.hasNext()) {
        	// 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            Object elem = iterator.next();
            String element = objectMapper.writeValueAsString(elem);
            if (regex.matcher(element).matches()) {
                result.add(elem);
            }
        }
        return result;
    }
    
    @Comment("获取含有score的Set（默认升序）")
    public  RScoredSortedSet<Object> getScoreSet(@Comment(name = "redisKey", value = "redisKey") String redisKey,
    		@Comment(name = "expired", value = "value值的过期时间 （<= 0：为永久有效）") long expired) {
    	RScoredSortedSet<Object> setObj = redisson.getScoredSortedSet(redisKey);
    	if(expired > 0) {
    		setObj.expire(expired, TimeUnit.SECONDS);
        }
    	return setObj;
    }
    
    @Comment("写入含有score的Set<Object>数据")
    public void addScoreSet(@Comment(name = "rSet", value = "含有score的RSet对象") RScoredSortedSet<Object> rSet,
    		@Comment(name = "score", value = "用于排序的double类型数据") double score,
    		@Comment(name = "value", value = "写入的字符串信息") Object value) {
    	rSet.add(score, value);
    }
    
    @Comment("删除含有score的Set<Object>数据")
    public void delScoreSet(@Comment(name = "rSet", value = "含有score的RSet对象") RScoredSortedSet<Object> rSet,
    		@Comment(name = "valuePattern", value = "匹配字符串状态下的value，使用正则表达式\n\n 如：.*yourPattern.*")String valuePattern) throws JsonProcessingException {
    	// 模糊查询并删除指定数据
        Pattern regex = Pattern.compile(valuePattern);// 替换为你的匹配模式

        Iterator<Object> iterator = rSet.iterator();
        while (iterator.hasNext()) {
        	// 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            String element = objectMapper.writeValueAsString(iterator.next());
            if (regex.matcher(element).matches()) {
                iterator.remove(); // 删除符合条件的元素
            }
        }
    }
    
    @Comment("模糊查询,含有score的Set中的数据")
    public  Set<Object> getSetByValStr(@Comment(name = "rSet", value = "含有score的RSet对象") RScoredSortedSet<Object> rSet,
    		@Comment(name = "valuePattern", value = "匹配字符串状态下的value，使用正则表达式\n\n 如：.*yourPattern.*")String valuePattern) throws JsonProcessingException{
    	// 模糊查询并删除指定数据
        Pattern regex = Pattern.compile(valuePattern);// 替换为你的匹配模式
        Set<Object> result = new HashSet<>();
        Iterator<Object> iterator = rSet.iterator();
        while (iterator.hasNext()) {
        	// 创建 ObjectMapper 实例
            ObjectMapper objectMapper = new ObjectMapper();
            Object elem = iterator.next();
            String element = objectMapper.writeValueAsString(elem);
            if (regex.matcher(element).matches()) {
                result.add(elem);
            }
        }
        return result;
    }
    
    @Comment("获取score升序后的Set<Object>")
    public  List<Object> getScoreSetByAscScore(@Comment(name = "rSet", value = "含有score的RSet对象") RScoredSortedSet<Object> rSet) {
    	// 获取倒排元素集合
        return rSet.stream()
                .sorted((e1, e2) -> Double.compare(rSet.getScore(e1), rSet.getScore(e2)))
                .collect(Collectors.toList());
    }
    
    @Comment("获取score降序后的Set<Object>")
    public  List<Object> getScoreSetByDescScore(@Comment(name = "rSet", value = "含有score的RSet对象") RScoredSortedSet<Object> rSet) {
    	// 获取倒排元素集合
    	return rSet.stream()
    			.sorted((e1, e2) -> Double.compare(rSet.getScore(e2), rSet.getScore(e1)))
    			.collect(Collectors.toList());
    }
    
    /**
     * 获取区间分数（score）内的全部数据，并按照score的数值升序排序
     * @param redisKey
     * @param startIndex
     * 			value 值的起始索引，即：下标
     * @param endIndex
     * 			value 值的结束索引，即：下标；-1：标识到最后
     * @return
     */
    @Comment("按照score的数值升序排序,后获取区间索引内的全部Set<Object>数据")
    public Collection<ScoredEntry<Object>> getAscScoreSetByBetweenScore(
    		@Comment(name = "rSet", value = "含有score的RSet对象") RScoredSortedSet<Object> rSortedSet,
    		@Comment(name = "startIndex", value = "value 值的起始索引，即：下标") int startIndex, 
    		@Comment(name = "endIndex", value = "value 值的结束索引，即：下标；-1：标识到最后") int endIndex) {
        return rSortedSet.entryRange(startIndex, endIndex);
    }
 
 
    /**
     * 获取区间分数（score）内的全部数据，并按照score的数值降序排序
     * @param redisKey
     * @param startIndex
     * 			value 值的起始索引，即：下标
     * @param endIndex
     * 			value 值的结束索引，即：下标；-1：标识到最后
     * @return
     */
    @Comment("按照score的数值降序排序,后获取区间索引内的全部Set<Object>数据")
    public Collection<ScoredEntry<Object>> getDescScoreSetByBetweenScore(
    		@Comment(name = "rSet", value = "含有score的RSet对象") RScoredSortedSet<Object> rSortedSet,
    		@Comment(name = "startIndex", value = "value 值的起始索引，即：下标") int startIndex, 
    		@Comment(name = "endIndex", value = "value 值的结束索引，即：下标；-1：标识到最后") int endIndex) {
        return rSortedSet.entryRangeReversed(startIndex, endIndex);
    }
 
	private final ThreadPoolExecutor threadPoolExecutor;
    {
        final AtomicInteger num = new AtomicInteger(0);
        threadPoolExecutor = new ThreadPoolExecutor(8, 8, 60, TimeUnit.SECONDS,
          new LinkedBlockingDeque<>(1000), new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread thread = new Thread(r);
                thread.setDaemon(true);//设置为后台线程
                thread.setName("Redis-Topic-" + num.getAndIncrement());
                return thread;
            }
        });
    }
    
	@Comment("向topic中写入数据")
	public void publish(@Comment(name = "topicName", value = "topicName") String topicName,
    		@Comment(name = "data", value = "写入topic的数据") String data) throws Throwable{
		RMap<String,String> topicNamesObj = redisson.getMap("topicNames", StringCodec.INSTANCE);
		Set<Entry<String, String>> entrySet = topicNamesObj.entrySet("*___"+topicName+"*");
		int i = 0;
		for (Entry<String, String> entry : entrySet) {
	        i++;
	    }
		if(i > 0) {
			RTopic topic = redisson.getTopic(topicName);
	        topic.publish(data);
		}else {
			throw new Throwable("CUSTOM:当前topicName（"+topicName+"）未被监听，请您先启动监听服务！");
		}
	}
	
	@Comment("取消topicName的指定索引订阅")
	public void delTopicListener(@Comment(name = "index", value = "订阅信息的唯一身份索引") String index,
			@Comment(name = "topicName", value = "topicName") String topicName){
		RMap<String,String> topicNamesObj = redisson.getMap("topicNames", StringCodec.INSTANCE);
		Object lid = topicNamesObj.get(index+"___"+topicName);
		if( lid != null) {
			RTopic topic = redisson.getTopic(topicName);
			topic.removeListener(Integer.parseInt(lid.toString()));
			topicNamesObj.remove(index+"___"+topicName);
		}
	}
	
	@Comment("取消topicNamed的全部订阅")
	public void delTopicAllListener(@Comment(name = "topicName", value = "topicName") String topicName){
		RTopic topic = redisson.getTopic(topicName);
        topic.removeAllListeners();
    	RMap<String,String> topicNamesObj = redisson.getMap("topicNames", StringCodec.INSTANCE);
		Set<Entry<String, String>> entrySet = topicNamesObj.entrySet("*___"+topicName+"*");
		for (Entry<String, String> entry : entrySet) {
			topicNamesObj.remove(entry.getKey());
	    }
	}
	
	/**
	 * 订阅topic数据
	 * @param topicName
	 * @param url
	 */
	@Comment("订阅topic")
	public void listenerTopicName(
			@Comment(name = "index", value = "订阅信息的唯一身份索引") String index,
			@Comment(name = "topicName", value = "topicName") String topicName,
			@Comment(name = "redisConsumerCallback", value = "回调函数 ;\n\n 如：(topicName,content)->{print(content)}") RedisConsumerCallback redisConsumerCallback){
		RMap<String,String> topicNamesObj = redisson.getMap("topicNames", StringCodec.INSTANCE);
		Object lid = topicNamesObj.get(index+"___"+topicName);
		if( lid != null) {
			delTopicListener(index, topicName);
		}
		threadPoolExecutor.execute(() ->{ 
			 RTopic topic = redisson.getTopic(topicName);
			 int idx = topic.addListener(String.class, new MessageListener<String>() {
	            @Override
	            public void onMessage(CharSequence charSequence, String msg) {
	            	try {
	            		//TODO: 回调发送数据
	    		        redisConsumerCallback.processMessage(topicName,msg);
					} catch (Exception e) {
						e.printStackTrace();
					}
	            }
	        });
			topicNamesObj.put(index+"___"+topicName, idx+"");
		});
	}
	
	@Comment("创建限流器，注意如果名称相同则返回第一次创建的对象")
    public void createRateLimiter(
    		@Comment(name = "name", value = "限流器名称") String name, 
    		@Comment(name = "rateInterval", value = "限速的间隔大小") int rateInterval,
    		@Comment(name = "rateUnit", value = "限速的单位(s:秒，m:分，h:小时，d:天)") String rateUnit ) {
    	// 获取限流器对象
        RRateLimiter rateLimiter = redisson.getRateLimiter(name);
        if(rateUnit.equals("s")) {
        	 // 初始化限流器：每秒最多 rateInterval 个请求
            rateLimiter.trySetRate(RateType.OVERALL, rateInterval, 1, RateIntervalUnit.SECONDS);
        }else if(rateUnit.equals("m")) {
        	 // 初始化限流器：每分钟最多 rateInterval 个请求
            rateLimiter.trySetRate(RateType.OVERALL, rateInterval, 1, RateIntervalUnit.MINUTES);
        }else if(rateUnit.equals("h")) {
        	 // 初始化限流器：每小时最多 rateInterval 个请求
            rateLimiter.trySetRate(RateType.OVERALL, rateInterval, 1, RateIntervalUnit.HOURS);
        }else if(rateUnit.equals("d")) {
        	 // 初始化限流器：每天最多 rateInterval 个请求
            rateLimiter.trySetRate(RateType.OVERALL, rateInterval, 1, RateIntervalUnit.DAYS);
        }
    }
	
	@Comment("删除限流器")
	public boolean deleteRateLimiter(
	        @Comment(name = "name", value = "限流器名称") String name) {
	    // 获取限流器对象
	    RRateLimiter rateLimiter = redisson.getRateLimiter(name);
	    // 删除限流器
	    return rateLimiter.delete();
	}
    
	@Comment("根据限流器，调用指定回调逻辑")
    public Object accessLimitedMethod(
    		@Comment(name = "name", value = "限流器名称") String name, 
    		@Comment(name = "rateLimiterCallback", value = "回调函数 ;\n\n 如：()->{...}") RateLimiterCallback rateLimiterCallback) throws Exception {
		 RRateLimiter rateLimiter = redisson.getRateLimiter(name);
         // 尝试获取限流许可
         if (rateLimiter.tryAcquire()) {
             // 访问受限方法
        	return rateLimiterCallback.exec();
         } else {
        	return new JsonBean<>(403, "服务过于繁忙，请稍后重试！");
         }
    }
	
	@Comment("同步数据到缓存中使用")
	public RLocalCachedMap<String,  Object> syncData2Cache(
			@Comment(name = "redisKey", value = "redisKey") String redisKey, 
			@Comment(name = "keys", value = ""
					+ "()->{获取被加载表全主键集合并返回} \n\n ") RedisHander.Keys keys,
			@Comment(name = "rowDataByKey", value = ""
					+ "(key)->{根据主键值获取一条数据信息并返回}") RedisHander.RowDataByKey rowDataByKey) throws Exception {
		MapLoader<String,Object> mapLoader = new MapLoader<String, Object>() {

			//用于预加载数据
			@Override
			public Iterable<String> loadAllKeys() {
				 // 加载所有表数据，并返回id集合
		        return keys.getKeys();
			}

			@Override
			public Object load(String key) {
				return rowDataByKey.getRowDataByKey(key);
			}
		};
		LocalCachedMapOptions<String,Object> options = LocalCachedMapOptions.<String, Object>defaults().loader(mapLoader);
		return redisson.getLocalCachedMap(redisKey, options);
	}
	
	@Comment("同步数据给外部数据库使用")
	public RMap<String, Object> syncCacheData2External(
			@Comment(name = "redisKey", value = "redisKey") String redisKey, 
			@Comment(name = "insert", value = ""
					+ "(key,val)->{有新的数据key、value插入缓存} \n\n ") RedisHander.Insert insert,
			@Comment(name = "remove", value = ""
					+ "(key)->{有数据被删除，主键是key}") RedisHander.Remove remove) throws Exception {
		MapWriter<String, Object> mapWriter = new MapWriter<String, Object>() {
		    @Override
		    public void write(Map<String, Object> map) {
	            for (Entry<String, Object> entry : map.entrySet()) {
	            	insert.insert(entry.getKey(),entry.getValue());
	            	//System.out.println("插入数据key："+entry.getKey()+";value:"+entry.getValue());
	            }
		    }
		    @Override
		    public void delete(Collection<String> keys) {
	            for (String key : keys) {
	            	remove.remove(key);
	            	//System.out.println("删除数据key："+key);
	            }
		    }
		};
		LocalCachedMapOptions<String, Object> options = LocalCachedMapOptions.<String, Object>defaults()
				.writer(mapWriter)
				.writeMode(WriteMode.WRITE_THROUGH);
		return redisson.getLocalCachedMap(redisKey, options);
	}
	
	
	@Comment("获取所有符合正则表达式的 keys 的缓存数据")
    public Set<String> getKeys(@Comment(name = "prefix", value = "匹配reidskey字符串的正则表达式") String prefix) {
        Iterable<String> keysByPattern = redisson.getKeys().getKeysByPattern(prefix);
        Set<String> keys = new HashSet<>();
        for (String s : keysByPattern) {
            keys.add(s);
        }
        return keys;
    }

    @Comment("获取一把锁")
    public RLock getRedisLock(String key) {
        return redisson.getLock(key);
    }
    
    @Comment("移除缓存")
    public void remove(String redisKey) {
        redisson.getBucket(redisKey).delete();
    }
}
