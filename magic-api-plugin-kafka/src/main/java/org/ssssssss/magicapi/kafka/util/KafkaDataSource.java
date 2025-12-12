package org.ssssssss.magicapi.kafka.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Consumer;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.DeleteConsumerGroupsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.errors.GroupIdNotFoundException;
import org.apache.kafka.common.serialization.ByteArrayDeserializer;
import org.apache.kafka.common.serialization.StringDeserializer;
import org.apache.kafka.common.serialization.StringSerializer;
import org.springframework.kafka.core.ConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaConsumerFactory;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.AcknowledgingMessageListener;
import org.springframework.kafka.listener.ConcurrentMessageListenerContainer;
import org.springframework.kafka.listener.ContainerProperties;
import org.springframework.kafka.support.SendResult;
import org.springframework.lang.Nullable;
import org.ssssssss.magicapi.kafka.model.KafkaInfo;

public class KafkaDataSource {

	private final KafkaTemplate<String, Object> kafkaTemplate;
	private final Map<String, ConcurrentMessageListenerContainer<String, Object>> activeContainers;
	private final ConsumerFactory<String, Object> consumerFactory;
	private final Object lock = new Object();

	private final String id;
	private final String serverConfig;
	private final String valueDeserializer;
	private final String autoOffsetReset;
	private final int batchSize;
	private final int bufferMemory;
	private final int linger;

	public String getValueDeserializer() {
		return valueDeserializer;
	}

	public KafkaDataSource(KafkaInfo info) {
		this.id = info.getId();
		Map<String, Object> properties = new HashMap<>(info.getProperties());

		this.serverConfig = properties.getOrDefault("serverConfig", "localhost:9092").toString();
		this.batchSize = Integer.parseInt(properties.getOrDefault("batchSize", "524288").toString());
		this.bufferMemory = Integer.parseInt(properties.getOrDefault("bufferMemory", "67108864").toString());
		this.linger = Integer.parseInt(properties.getOrDefault("linger", "20").toString());// 等待20ms以填充批次
		this.autoOffsetReset = properties.getOrDefault("autoOffsetReset", "latest").toString();
		this.valueDeserializer = properties.getOrDefault("valueDeserializer", "string").toString();
		this.activeContainers = new ConcurrentHashMap<>();
		this.consumerFactory = createConsumerFactory();
		this.kafkaTemplate = createKafkaTemplate();

	}

	public boolean validate() {
		if (this.consumerFactory == null || this.kafkaTemplate == null) {
			return false;
		}

		// 重新构建配置
		Map<String, Object> configs = new HashMap<>();
		configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.serverConfig);

		// 如果有安全配置，需要从原始配置中获取并添加
		// 这里需要您根据实际情况添加安全配置

		try (AdminClient adminClient = AdminClient.create(configs)) {
			adminClient.describeCluster().nodes().get(5, TimeUnit.SECONDS);
			return true;
		} catch (Exception e) {
			System.err.println("Kafka连接验证失败: " + e.getMessage());
			return false;
		}
	}

	/**
	 * 创建消费者工厂
	 */
	private ConsumerFactory<String, Object> createConsumerFactory() {
		Map<String, Object> props = new HashMap<>();
		props.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.serverConfig);
		props.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		if (valueDeserializer.equals("string")) {
			props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
		} else if (valueDeserializer.equals("byteArray")) {
			props.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		}

		props.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoOffsetReset);
		props.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);

		// 高性能配置
		props.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, 102400); // 至少等待100KB数据
		props.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, 500); // 最长等待500ms
		props.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, 1048576); // 每个分区最多返回的最大字节数，1MB
		props.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, 1000); // 每次poll最多1000条记录

		// 重要配置 - 会话和心跳
		props.put(ConsumerConfig.SESSION_TIMEOUT_MS_CONFIG, 10000); // 10秒会话超时
		props.put(ConsumerConfig.HEARTBEAT_INTERVAL_MS_CONFIG, 3000); // 3秒心跳间隔

		props.put(ConsumerConfig.MAX_POLL_INTERVAL_MS_CONFIG, 300000); // 它定义了消费者在处理一批消息时两次 poll 调用之间的最大允许间隔时间
																		// 5分钟，避免消费者被误认为死亡
		props.put(ConsumerConfig.REQUEST_TIMEOUT_MS_CONFIG, 305000); // 请求超时(略大于MAX_POLL_INTERVAL_MS)
		props.put(ConsumerConfig.FETCH_MAX_BYTES_CONFIG, 52428800); // 50MB最大fetch字节数（一次所有分区的数据获取总大小，一般不需要配置，除非有大文件，如果没有可以设置成
																	// 10485760 10MB）
		props.put(ConsumerConfig.RECONNECT_BACKOFF_MS_CONFIG, 1000); // 重连基础间隔
		props.put(ConsumerConfig.RECONNECT_BACKOFF_MAX_MS_CONFIG, 10000); // 最大重连间隔

		return new DefaultKafkaConsumerFactory<>(props);
	}

	/**
	 * 创建Kafka模板
	 */
	private KafkaTemplate<String, Object> createKafkaTemplate() {
		Map<String, Object> props = new HashMap<>();
		props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.serverConfig);
		props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		if (valueDeserializer.equals("string")) {
			props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
		} else if (valueDeserializer.equals("byteArray")) {
			props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
		}

		// 高性能配置
		props.put("batch.size", batchSize);// 16KB批次大小，可根据消息大小调整到32KB或64KB
		props.put("linger.ms", linger); // 毫秒，等待20ms以填充批次
		props.put("compression.type", "lz4");
		props.put("buffer.memory", bufferMemory); // 32MB 发送消息的缓冲区大小
		props.put("max.in.flight.requests.per.connection", 5);
		props.put("enable.idempotence", true);

		props.put("connections.max.idle.ms", 540000); // 连接池中连接的最大空闲时间
		props.put("max.block.ms", 60000); // 控制生产者在发送请求时阻塞的最大时间

		DefaultKafkaProducerFactory<String, Object> producerFactory = new DefaultKafkaProducerFactory<>(props);
		return new KafkaTemplate<>(producerFactory);
	}

	/**
	 * 发布消息到指定主题
	 * 
	 * @param topic   主题名称
	 * @param message 消息内容
	 * @return 发送结果
	 */
	public CompletableFuture<SendResult<String, Object>> publish(String topic, Object message) {
		return kafkaTemplate.send(topic, message);
	}
	/**
	 * 发布消息到指定主题
	 * 
	 * @param topic   主题名称
	 * @param key     消息键
	 * @param message 消息内容
	 * @return 发送结果
	 */
	public CompletableFuture<SendResult<String, Object>> publish(String topic, String key, Object message) {
		return kafkaTemplate.send(topic, key, message);
	}

	/**
	 * 发布消息到指定主题、分区
	 * 
	 * @param topic     主题名称
	 * @param partition 分区
	 * @param key       消息键
	 * @param message   消息内容
	 * @return 发送结果
	 */
	public CompletableFuture<SendResult<String, Object>> publish(String topic, Integer partition, String key,
			Object message) {
		return kafkaTemplate.send(new ProducerRecord<>(topic, partition, key, message));
	}
	
	/**
	 * 发布消息到指定主题、分区和时间戳
	 * 
	 * @param topic     主题名称
	 * @param partition 分区
	 * @param timestamp 时间戳
	 * @param key       消息键
	 * @param message   消息内容
	 * @return 发送结果
	 */
	public CompletableFuture<SendResult<String, Object>> publish(String topic, Integer partition, Long timestamp,
			String key, Object message) {
		return kafkaTemplate.send(new ProducerRecord<>(topic, partition, timestamp, key, message));
	}

	/**
	 * 同步发布消息（等待确认）
	 * 
	 * @param topic   主题名称
	 * @param message 消息内容
	 * @param timeout 超时时间
	 * @param unit    时间单位
	 * @return 发送结果
	 */
	public SendResult<String, Object> publishSync(String topic, Object message, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return publish(topic, message).get(timeout, unit);
	}
	/**
	 * 同步发布消息（等待确认）
	 * 
	 * @param topic   主题名称
	 * @param key     消息键
	 * @param message 消息内容
	 * @param timeout 超时时间
	 * @param unit    时间单位
	 * @return 发送结果
	 */
	public SendResult<String, Object> publishSync(String topic, String key ,Object message, long timeout, TimeUnit unit)
			throws InterruptedException, ExecutionException, TimeoutException {
		return publish(topic, key, message).get(timeout, unit);
	}

	/**
	 * 订阅主题并指定消息处理器
	 * 
	 * @param topic          主题名称
	 * @param groupId        消费者组ID
	 * @param messageHandler 消息处理函数
	 * @return 订阅ID（用于取消订阅）
	 */
	public String subscribe(String topic, String groupId, Consumer<ConsumerRecord<String, Object>> messageHandler) {
		return subscribe(Collections.singletonList(topic), groupId, messageHandler, null);
	}

	/**
	 * 订阅主题并指定消息处理器和错误处理器
	 * 
	 * @param topic          主题名称
	 * @param groupId        消费者组ID
	 * @param messageHandler 消息处理函数
	 * @param errorHandler   错误处理函数
	 * @return 订阅ID（用于取消订阅）
	 */
	public String subscribe(String topic, String groupId, Consumer<ConsumerRecord<String, Object>> messageHandler,
			@Nullable Consumer<Exception> errorHandler) {
		return subscribe(Collections.singletonList(topic), groupId, messageHandler, errorHandler);
	}

	/**
	 * 订阅多个主题并指定消息处理器
	 * 
	 * @param topics         主题列表
	 * @param groupId        消费者组ID
	 * @param messageHandler 消息处理函数
	 * @return 订阅ID（用于取消订阅）
	 */
	public String subscribe(List<String> topics, String groupId,
			Consumer<ConsumerRecord<String, Object>> messageHandler) {
		return subscribe(topics, groupId, messageHandler, null);
	}

	/**
	 * 订阅多个主题并指定消息处理器和错误处理器
	 * 
	 * @param topics         主题列表
	 * @param groupId        消费者组ID
	 * @param messageHandler 消息处理函数
	 * @param errorHandler   错误处理函数
	 * @return 订阅ID（用于取消订阅）
	 */
	public String subscribe(List<String> topics, String groupId,
			Consumer<ConsumerRecord<String, Object>> messageHandler, @Nullable Consumer<Exception> errorHandler) {
		String subscriptionId = "sub-kafka-" + UUID.randomUUID().toString();

		if(this.autoOffsetReset.equals("latest")) {
			// 在创建消费者前重置消费组偏移量
		    resetConsumerGroupOffsets(groupId, topics);
		}
		
		synchronized (lock) {
			// 创建容器属性
			ContainerProperties containerProps = new ContainerProperties(topics.toArray(new String[0]));
			containerProps.setGroupId(groupId);
			containerProps.setAckMode(ContainerProperties.AckMode.MANUAL_IMMEDIATE);

			// 创建消息监听器容器
			ConcurrentMessageListenerContainer<String, Object> container = new ConcurrentMessageListenerContainer<>(
					consumerFactory, containerProps);

			// 设置消息监听器 - 修改为使用AcknowledgingMessageListener
			container.setupMessageListener((AcknowledgingMessageListener<String, Object>) (record, acknowledgment) -> {
				try {
					messageHandler.accept(record);
					acknowledgment.acknowledge(); // 手动提交偏移量
//					System.out.println("已提交偏移量: topic=" + record.topic() + ", partition=" + record.partition()
//							+ ", offset=" + record.offset());
				} catch (Exception e) {
					if (errorHandler != null) {
						errorHandler.accept(e);
					} else {
						System.err.println("kakfa处理消息时发生错误: " + e.getMessage());
						e.printStackTrace();
					}
					// 注意：这里没有调用acknowledgment.acknowledge()，所以偏移量不会提交
					// 这样消息会被重新投递（至少一次交付语义）
				}
			});

			// 设置并发度（根据主题分区数动态调整）
			int concurrency = calculateConcurrency(topics);
			container.setConcurrency(concurrency);

			// 启动容器
			container.start();

			// 保存容器引用
			activeContainers.put(subscriptionId, container);

			System.out.println("kafka已订阅主题: " + topics + ", 订阅ID: " + subscriptionId + ", 并发度: " + concurrency);
			return subscriptionId;
		}
	}

	/**
	 * 计算合适的并发度（基于主题分区数）
	 */
	private synchronized int calculateConcurrency(List<String> topics) {
		try {
			Map<String, Object> configs = new HashMap<>();
			configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.serverConfig);

			int maxPartitions = 0;
			try (AdminClient adminClient = AdminClient.create(configs)) {
				DescribeTopicsResult describeResult = adminClient.describeTopics(topics);
				Map<String, TopicDescription> topicDescriptions = describeResult.allTopicNames().get(10,
						TimeUnit.SECONDS);

				for (TopicDescription description : topicDescriptions.values()) {
					maxPartitions = Math.max(maxPartitions, description.partitions().size());
				}
			}

			int availableProcessors = Runtime.getRuntime().availableProcessors();
			int calculatedConcurrency = Math.min(maxPartitions, availableProcessors * 2);
			calculatedConcurrency = Math.max(1, calculatedConcurrency);

			System.out.println("kafka计算并发度 - 主题: " + topics + ", 最大分区数: " + maxPartitions + ", CPU核心数: "
					+ availableProcessors + ", 最终并发度: " + calculatedConcurrency);

			return calculatedConcurrency;
		} catch (Exception e) {
			System.err.println("kafka获取主题分区信息失败，使用默认并发度: " + e.getMessage());
			return Math.min(3, Runtime.getRuntime().availableProcessors());
		}
	}

	/**
	 * 取消订阅
	 * 
	 * @param subscriptionId 订阅ID
	 * @return 是否成功取消
	 */
	public boolean unsubscribe(String subscriptionId) {
		synchronized (lock) {
			ConcurrentMessageListenerContainer<String, Object> container = activeContainers.get(subscriptionId);
			if (container != null) {
				try {
					container.stop();
					activeContainers.remove(subscriptionId);
					System.out.println("kafka已取消订阅: " + subscriptionId);
					return true;
				} catch (Exception e) {
					System.err.println("kafka取消订阅时发生错误: " + e.getMessage());
					return false;
				}
			}
			return false;
		}
	}

	/**
	 * 获取所有活跃订阅
	 * 
	 * @return 订阅ID列表
	 */
	public List<String> getActiveSubscriptions() {
		return new ArrayList<>(activeContainers.keySet());
	}

	/**
	 * 获取订阅详情
	 * 
	 * @param subscriptionId 订阅ID
	 * @return 订阅详情
	 */
	public Map<String, Object> getSubscriptionDetails(String subscriptionId) {
		ConcurrentMessageListenerContainer<String, Object> container = activeContainers.get(subscriptionId);
		if (container != null) {
			Map<String, Object> details = new HashMap<>();
			details.put("subscriptionId", subscriptionId);
			details.put("topics", Arrays.asList(container.getContainerProperties().getTopics()));
			details.put("groupId", container.getContainerProperties().getGroupId());
			details.put("isRunning", container.isRunning());
			details.put("concurrency", container.getConcurrency());

			if (container.getAssignedPartitions() != null) {
				List<String> partitions = new ArrayList<>();
				for (TopicPartition partition : container.getAssignedPartitions()) {
					partitions.add(partition.topic() + "-" + partition.partition());
				}
				details.put("assignedPartitions", partitions);
			}

			return details;
		}
		return null;
	}

	/**
	 * 暂停订阅（停止消费但不释放资源）
	 * 
	 * @param subscriptionId 订阅ID
	 * @return 是否成功暂停
	 */
	public boolean pauseSubscription(String subscriptionId) {
		ConcurrentMessageListenerContainer<String, Object> container = activeContainers.get(subscriptionId);
		if (container != null && container.isRunning()) {
			container.pause();
			System.out.println("kafka已暂停订阅: " + subscriptionId);
			return true;
		}
		return false;
	}

	/**
	 * 恢复暂停的订阅
	 * 
	 * @param subscriptionId 订阅ID
	 * @return 是否成功恢复
	 */
	public boolean resumeSubscription(String subscriptionId) {
		ConcurrentMessageListenerContainer<String, Object> container = activeContainers.get(subscriptionId);
		if (container != null && !container.isRunning()) {
			container.resume();
			System.out.println("kafka已恢复订阅: " + subscriptionId);
			return true;
		}
		return false;
	}

	/**
	 * 应用关闭时清理所有订阅
	 */
	public void cleanup() {
		synchronized (lock) {
			System.out.println("正在清理Kafka订阅...");
			List<String> subscriptionIds = new ArrayList<>(activeContainers.keySet());
			for (String subscriptionId : subscriptionIds) {
				unsubscribe(subscriptionId);
			}
		}
	}

	/**
	 * 获取Kafka模板（用于高级操作）
	 * 
	 * @return KafkaTemplate实例
	 */
	public KafkaTemplate<String, Object> getKafkaTemplate() {
		return kafkaTemplate;
	}

	public String getId() {
		return id;
	}

	public void close() {
		synchronized (lock) {
			// 1. 停止所有活跃的消费者容器
			List<String> subscriptionIds = new ArrayList<>(activeContainers.keySet());
			for (String subscriptionId : subscriptionIds) {
				unsubscribe(subscriptionId);
			}

			// 2. 关闭Kafka生产者工厂
			if (kafkaTemplate != null) {
				try {
					DefaultKafkaProducerFactory<String, Object> producerFactory = (DefaultKafkaProducerFactory<String, Object>) kafkaTemplate
							.getProducerFactory();
					if (producerFactory != null) {
						producerFactory.destroy();
					}
				} catch (Exception e) {
					System.err.println("关闭Kafka生产者工厂时发生错误: " + e.getMessage());
				}
			}

			// 3. 清理资源引用
			activeContainers.clear();
			System.out.println("Kafka数据源已关闭: " + this.id);
		}
	}

	// 特定类型辅助方法

	/**
	 * 发布字符串消息到指定主题
	 * 
	 * @param topic   主题名称
	 * @param key 	  消息键
	 * @param message 字符串消息内容
	 * @return 发送结果
	 */
	public CompletableFuture<SendResult<String, Object>> publishString(String topic, String key, String message) {
		return publish(topic, key,message);
	}

	/**
	 * 发布字节数组消息到指定主题
	 * 
	 * @param topic   主题名称
	 * @param key 	  消息键
	 * @param message 字节数组消息内容
	 * @return 发送结果
	 */
	public CompletableFuture<SendResult<String, Object>> publishBytes(String topic, String key, byte[] message) {
		return publish(topic,key ,message);
	}

	/**
	 * 订阅主题并指定字符串消息处理器
	 * 
	 * @param topic          主题名称
	 * @param groupId        消费者组ID
	 * @param messageHandler 字符串消息处理函数
	 * @return 订阅ID（用于取消订阅）
	 */
	public String subscribeString(String topic, String groupId,
			Consumer<ConsumerRecord<String, String>> messageHandler) {
		// 创建错误处理器
		java.util.function.Consumer<Exception> errorHandler = ex -> {
			System.err.println("kafka处理消息时发生错误: " + ex.getMessage());
			// 这里可以添加重试逻辑或发送到死信队列
		};

		return subscribe(topic, groupId, record -> {
			if (record.value() instanceof String) {
				// 使用非弃用的构造函数创建新的ConsumerRecord
				ConsumerRecord<String, String> typedRecord = new ConsumerRecord<String,String>(
						record.topic(), record.partition(),
						record.offset(), record.timestamp(), 
						record.timestampType(), 
						record.serializedKeySize(),
						record.serializedValueSize(), 
						record.key(), 
						(String) record.value(), 
						record.headers(),
						record.leaderEpoch());
				messageHandler.accept(typedRecord);
			} else {
				System.err.println("kafka收到非字符串消息，主题：" + record.topic());
			}
		}, errorHandler);
	}

	/**
	 * 订阅主题并指定字节数组消息处理器
	 * 
	 * @param topic          主题名称
	 * @param groupId        消费者组ID
	 * @param messageHandler 字节数组消息处理函数
	 * @return 订阅ID（用于取消订阅）
	 */
	public String subscribeBytes(String topic, String groupId,
			Consumer<ConsumerRecord<String, byte[]>> messageHandler) {
		// 创建错误处理器
		java.util.function.Consumer<Exception> errorHandler = ex -> {
			System.err.println("kafka处理消息时发生错误: " + ex.getMessage());
			// 这里可以添加重试逻辑或发送到死信队列
		};

		return subscribe(topic, groupId, record -> {
			if (record.value() instanceof byte[]) {
				// 使用非弃用的构造函数创建新的ConsumerRecord
				ConsumerRecord<String, byte[]> typedRecord = new ConsumerRecord<>(record.topic(), record.partition(),
						record.offset(), record.timestamp(), record.timestampType(), record.serializedKeySize(),
						record.serializedValueSize(), record.key(), (byte[]) record.value(), record.headers(),
						record.leaderEpoch());
				messageHandler.accept(typedRecord);
			} else {
				System.err.println("kafka收到非字节数组消息，主题: " + record.topic());
			}
		}, errorHandler);
	}
	
	private void resetConsumerGroupOffsets(String groupId, List<String> topics) {
	    try {
	        Map<String, Object> configs = new HashMap<>();
	        configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.serverConfig);
	        
	        try (AdminClient adminClient = AdminClient.create(configs)) {
	            // 获取主题的分区信息
	            DescribeTopicsResult describeResult = adminClient.describeTopics(topics);
	            Map<String, TopicDescription> topicDescriptions = describeResult.allTopicNames().get(10, TimeUnit.SECONDS);
	            
	            // 构建分区列表
	            List<TopicPartition> partitions = new ArrayList<>();
	            for (TopicDescription description : topicDescriptions.values()) {
	                for (TopicPartitionInfo partitionInfo : description.partitions()) {
	                    partitions.add(new TopicPartition(description.name(), partitionInfo.partition()));
	                }
	            }
	            
	            // 创建临时消费者来获取分区的末尾偏移量
	            Map<String, Object> consumerConfigs = new HashMap<>();
	            consumerConfigs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.serverConfig);
	            consumerConfigs.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
	            consumerConfigs.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, false);
	            consumerConfigs.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	            
	            if (valueDeserializer.equals("string")) {
	                consumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, StringDeserializer.class);
	            } else if (valueDeserializer.equals("byteArray")) {
	                consumerConfigs.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, ByteArrayDeserializer.class);
	            }
	            
	            try (KafkaConsumer<String, Object> consumer = new KafkaConsumer<>(consumerConfigs)) {
	                // 获取每个分区的末尾偏移量
	                Map<TopicPartition, Long> endOffsets = consumer.endOffsets(partitions);
	                
	                // 提交新的偏移量（设置为每个分区的末尾）
	                Map<TopicPartition, OffsetAndMetadata> offsets = new HashMap<>();
	                for (TopicPartition partition : partitions) {
	                    offsets.put(partition, new OffsetAndMetadata(endOffsets.get(partition)));
	                }
	                
	                adminClient.alterConsumerGroupOffsets(groupId, offsets).all().get(10, TimeUnit.SECONDS);
	                System.out.println("kafka已重置消费组 " + groupId + " 的偏移量到最新位置");
	            }
	        }
	    } catch (Exception e) {
	        System.err.println("kafka重置消费组偏移量时发生错误: " + e.getMessage());
	        // 注意：这里不抛出异常，因为即使重置失败，我们也希望继续创建消费者
	    }
	}
	
	
	 /**
     * 动态删除Kafka消费者组(Group ID)
     * 
     * @param groupId 要删除的消费者组ID
     * @return 是否成功删除
     */
    public boolean deleteConsumerGroup(String groupId) {
        return deleteConsumerGroup(groupId, 3, 1000); // 默认重试3次，每次间隔1秒
    }
    
    /**
     * 动态删除Kafka消费者组(Group ID)，带重试机制
     * 
     * @param groupId 要删除的消费者组ID
     * @param maxRetries 最大重试次数
     * @param retryIntervalMs 重试间隔(毫秒)
     * @return 是否成功删除
     */
    private boolean deleteConsumerGroup(String groupId, int maxRetries, long retryIntervalMs) {
        int attempt = 0;
        
        while (attempt < maxRetries) {
            try {
                attempt++;
                
                Map<String, Object> configs = new HashMap<>();
                configs.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, this.serverConfig);
                
                try (AdminClient adminClient = AdminClient.create(configs)) {
                    // 尝试删除消费者组
                    DeleteConsumerGroupsResult deleteResult = adminClient.deleteConsumerGroups(Arrays.asList(groupId));
                    deleteResult.all().get(10, TimeUnit.SECONDS);
                    
                    System.out.println("成功删除Kafka消费者组: " + groupId);
                    return true;
                }
            } catch (ExecutionException e) {
                // 处理特定的Kafka异常
                if (e.getCause() instanceof GroupIdNotFoundException) {
                    System.out.println("消费者组 " + groupId + " 不存在，无需删除");
                    return true; // 组不存在视为成功
                }
                
                // 其他异常处理
                if (attempt >= maxRetries) {
                    System.err.println("删除消费者组 " + groupId + " 失败，已达最大重试次数: " + e.getMessage());
                    return false;
                } else {
                    System.err.println("删除消费者组 " + groupId + " 失败，第" + attempt + "次重试: " + e.getMessage());
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                    // 指数退避策略，增加重试间隔
                    retryIntervalMs *= 2;
                }
            } catch (Exception e) {
                if (attempt >= maxRetries) {
                    System.err.println("删除消费者组 " + groupId + " 失败，已达最大重试次数: " + e.getMessage());
                    return false;
                } else {
                    System.err.println("删除消费者组 " + groupId + " 失败，第" + attempt + "次重试: " + e.getMessage());
                    try {
                        Thread.sleep(retryIntervalMs);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        return false;
                    }
                    // 指数退避策略，增加重试间隔
                    retryIntervalMs *= 2;
                }
            }
        }
        
        return false;
    }
	
    /**
     * 增强的取消订阅方法，可选择性删除消费者组
     * 
     * @param subscriptionId 订阅ID
     * @param deleteGroup 是否删除对应的消费者组
     * @return 是否成功取消订阅
     */
    public boolean unsubscribe(String subscriptionId, boolean deleteGroup) {
        StringBuffer groupId = new StringBuffer();
        synchronized (lock) {
            ConcurrentMessageListenerContainer<String, Object> container = activeContainers.get(subscriptionId);
            if (container != null) {
                try {
                    // 获取消费者组ID
                    groupId.append(container.getContainerProperties().getGroupId());
                    container.stop();
                    activeContainers.remove(subscriptionId);
                    System.out.println("Kafka已取消订阅: " + subscriptionId);
                } catch (Exception e) {
                    System.err.println("Kafka取消订阅时发生错误: " + e.getMessage());
                    return false;
                }
            } else {
                return false;
            }
        }
        
        // 如果需要删除消费者组，则异步执行删除操作
        if (deleteGroup && groupId != null) {
            CompletableFuture.runAsync(() -> {
                boolean deleted = deleteConsumerGroup(groupId.toString(), 5, 2000); // 重试5次，初始间隔2秒
                if (deleted) {
                    System.out.println("成功删除kafka消费者组: " + groupId + " (订阅ID: " + subscriptionId + ")");
                } else {
                    System.err.println("删除kafka消费者组失败: " + groupId + " (订阅ID: " + subscriptionId + ")");
                }
            });
        }
        
        return true;
    }
	
    /**
     * 直接删除指定消费者组（不依赖订阅）
     * 
     * @param groupId 消费者组ID
     * @return 是否成功删除
     */
    public boolean deleteConsumerGroupOnly(String groupId) {
        return deleteConsumerGroup(groupId);
    }

	
}