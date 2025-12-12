package org.ssssssss.magicapi.kafka;

import java.beans.Transient;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.support.SendResult;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.kafka.model.MagicDynamicKafkaClient;
import org.ssssssss.magicapi.kafka.util.KafkaDataSource;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

/**
 * kafka模块
 *
 * @author xuhaiyang
 */
@MagicModule("kafka")
public class KafkaModule implements DynamicAttribute<KafkaModule, KafkaModule> {

	private MagicDynamicKafkaClient magicDynamicKafkaClient;
	private KafkaDataSource kafkaDataSource;
	private String valueDeserializer;
	// 存储订阅ID和对应的主题，便于管理
	private Map<String, String> subscriptionTopics = new HashMap<>();

	public KafkaModule(MagicDynamicKafkaClient magicDynamicKafkaClient) {
		this.magicDynamicKafkaClient = magicDynamicKafkaClient;
	}

	public KafkaModule(KafkaDataSource kafkaDataSource) {
		this.kafkaDataSource = kafkaDataSource;
		this.valueDeserializer = kafkaDataSource.getValueDeserializer();
	}

	public KafkaDataSource getKafkaDataSource() {
		return kafkaDataSource;
	}

	private void valid() {
		if (kafkaDataSource == null) {
			kafkaDataSource = magicDynamicKafkaClient.getModule("def").getKafkaDataSource();
		}
	}

	/**
	 * 数据源切换
	 */
	@Override
	@Transient
	public KafkaModule getDynamicAttribute(String key) {
		return magicDynamicKafkaClient.getModule(key);
	}

	@Comment("同步发布消息（等待确认）")
	public boolean publishSync(@Comment(name = "topicName", value = "主题名称") String topicName,
			@Comment(name = "key", value = "消息键") String key,
			@Comment(name = "topicContent", value = "写入内容") Object topicContent) {
		try {
			valid();
			if (valueDeserializer.equals("string")) {
				if (topicContent instanceof byte[]) {
					kafkaDataSource.publishSync(topicName, key, new String((byte[]) topicContent), 5, TimeUnit.SECONDS);
				} else if (topicContent instanceof String) {
					kafkaDataSource.publishSync(topicName, key, topicContent.toString(), 5, TimeUnit.SECONDS);
				}
			} else if (valueDeserializer.equals("byteArray")) {
				if (topicContent instanceof byte[]) {
					kafkaDataSource.publishSync(topicName,key, (byte[]) topicContent, 5, TimeUnit.SECONDS);
				} else if (topicContent instanceof String) {
					kafkaDataSource.publishSync(topicName, key,topicContent.toString().getBytes(), 5, TimeUnit.SECONDS);
				}
			}
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	@Comment("发布消息")
	public void publish(@Comment(name = "topicName", value = "主题名称") String topicName,
			@Comment(name = "key", value = "消息键") String key,
			@Comment(name = "topicContent", value = "写入内容") Object topicContent) {
		valid();
		if (valueDeserializer.equals("string")) {
			if (topicContent instanceof byte[]) {
				kafkaDataSource.publishString(topicName, key,new String((byte[]) topicContent));
			} else if (topicContent instanceof String) {
				kafkaDataSource.publishString(topicName, key,topicContent.toString());
			}
		} else if (valueDeserializer.equals("byteArray")) {
			if (topicContent instanceof byte[]) {
				kafkaDataSource.publishBytes(topicName, key,(byte[]) topicContent);
			} else if (topicContent instanceof String) {
				kafkaDataSource.publishBytes(topicName, key, topicContent.toString().getBytes());
			}
		}
	}
	@Comment("发布消息到指定分区")
	public void publish2Partition(
			@Comment(name = "topicName", value = "主题名称") String topicName,
			@Comment(name = "partition", value = "指定分区") int partition,
			@Comment(name = "key", value = "消息键") String key,
			@Comment(name = "topicContent", value = "写入内容") Object topicContent) {
		valid();
		if (valueDeserializer.equals("string")) {
			if (topicContent instanceof byte[]) {
				kafkaDataSource.publish(topicName, partition,key,new String((byte[]) topicContent));
			} else if (topicContent instanceof String) {
				kafkaDataSource.publish(topicName, partition,key, topicContent.toString());
			}
		} else if (valueDeserializer.equals("byteArray")) {
			if (topicContent instanceof byte[]) {
				kafkaDataSource.publish(topicName, partition,key, (byte[]) topicContent);
			} else if (topicContent instanceof String) {
				kafkaDataSource.publish(topicName, partition,key, topicContent.toString().getBytes());
			}
		}
	}

	@Comment("发布消息")
	public void publish(@Comment(name = "topicName", value = "主题名称") String topicName,
			@Comment(name = "key", value = "消息键") String key,
			@Comment(name = "topicContent", value = "写入内容") Object topicContent,
			@Comment(name = "kafkaPublishHander", value = "回调函数 ;\n\n 如：(topicName,errorMessage)->{...}") KafkaPublishHander kafkaPublishHander) {
		valid();
		CompletableFuture<SendResult<String, Object>> future = null;
		if (valueDeserializer.equals("string")) {
			if (topicContent instanceof byte[]) {
				future = kafkaDataSource.publish(topicName, key,new String((byte[]) topicContent));
			} else if (topicContent instanceof String) {
				future = kafkaDataSource.publish(topicName, key, topicContent.toString());
			}
		} else if (valueDeserializer.equals("byteArray")) {
			if (topicContent instanceof byte[]) {
				future = kafkaDataSource.publish(topicName, key, (byte[]) topicContent);
			} else if (topicContent instanceof String) {
				future = kafkaDataSource.publish(topicName, key, topicContent.toString().getBytes());
			}
		}

		if (future != null) {
	        future.whenComplete((result, ex) -> {
	            if (ex != null) {
	                // 处理异常情况
	                kafkaPublishHander.callback(topicName, ex.getMessage());
	            } else {
	                // 处理成功情况
	                kafkaPublishHander.callback(topicName, null);
	            }
	        });
	    }
	}

	@Comment("订阅主题")
	public String subscribe(@Comment(name = "topicName", value = "主题名称") String topicName,
			@Comment(name = "kafkaSubscribeHander", value = "回调函数 ;\n\n 如：(topicName,key,stringContent,byteArrayContent)->{...}") KafkaSubscribeHander kafkaSubscribeHander) {
		return subscribe(topicName, null, kafkaSubscribeHander);
	}

	@Comment("订阅主题")
	public String subscribe(@Comment(name = "topicName", value = "主题名称") String topicName,
			@Comment(name = "groupId", value = "消费组") String groupId,
			@Comment(name = "kafkaSubscribeHander", value = "回调函数 ;\n\n 如：(topicName,key,stringContent,byteArrayContent)->{...}") KafkaSubscribeHander kafkaSubscribeHander) {
		valid();
		unsubscribe(topicName, true);
		if (valueDeserializer.equals("string")) {
			// 创建消息处理器
			java.util.function.Consumer<ConsumerRecord<String, String>> messageHandler = record -> {
//				System.out.printf("收到消息 - 主题: %s, 分区: %d, 偏移量: %d, 键: %s, 值: %s%n", record.topic(), record.partition(),
//						record.offset(), record.key(), record.value());

				// 在这里处理业务逻辑
				kafkaSubscribeHander.callback(topicName, record.key(),record.value(), record.value().getBytes());
			};
			// 订阅主题（使用随机生成的消费者组ID）
			if (groupId == null) {
				groupId = "group-" + topicName + "_" + System.currentTimeMillis();
			}

			String subscriptionId = kafkaDataSource.subscribeString(topicName, groupId, messageHandler);

			// 保存订阅信息
			subscriptionTopics.put(subscriptionId, topicName);
			return subscriptionId;
		} else {
			// 创建消息处理器
			java.util.function.Consumer<ConsumerRecord<String, byte[]>> messageHandler = record -> {
//				System.out.printf("收到消息 - 主题: %s, 分区: %d, 偏移量: %d, 键: %s, 值: %s%n", record.topic(), record.partition(),
//						record.offset(), record.key(), record.value());

				// 在这里处理业务逻辑
				kafkaSubscribeHander.callback(topicName, record.key(), new String(record.value()), record.value());
			};

			// 订阅主题（使用随机生成的消费者组ID）
			if (groupId == null) {
				groupId = "group-" + topicName + "_" + System.currentTimeMillis();
			}
			String subscriptionId = kafkaDataSource.subscribeBytes(topicName, groupId, messageHandler);
			// 保存订阅信息
			subscriptionTopics.put(subscriptionId, topicName);
			return subscriptionId;
		}

	}

	@Comment("取消订阅")
	public boolean unsubscribe(@Comment(name = "topicName", value = "主题名称") String topicName) {
		valid();
		StringBuffer status = new StringBuffer("");
		getSubscriptionIds(topicName).forEach(subscriptionId -> {
			if (!kafkaDataSource.unsubscribe(subscriptionId)) {
				status.append("," + subscriptionId);
			}
		});
		if (status.toString().equals("")) {
			return true;
		}
		return false;
	}
	
	@Comment("取消订阅")
	public boolean unsubscribe(
				@Comment(name = "topicName", value = "主题名称") String topicName,
				@Comment(name = "deleteGroup", value = "是否删除") boolean deleteGroup
			) {
		valid();
		StringBuffer status = new StringBuffer("");
		getSubscriptionIds(topicName).forEach(subscriptionId -> {
			if (!kafkaDataSource.unsubscribe(subscriptionId,deleteGroup)) {
				status.append("," + subscriptionId);
			}
		});
		if (status.toString().equals("")) {
			return true;
		}
		return false;
	}
	
	@Comment("删除消费组")
	public boolean deleteConsumerGroup(@Comment(name = "groupId", value = "消费组编号") String groupId) {
		valid();
		if (kafkaDataSource.deleteConsumerGroupOnly(groupId)) {
			 return true;
		}
		return false;
	}

	@Comment("获取所有活跃订阅")
	public Map<String, Object> getSubscriptions() {
		valid();
		Map<String, Object> result = new HashMap<>();
		result.put("activeSubscriptions", kafkaDataSource.getActiveSubscriptions());

		// 添加每个订阅的详情
		Map<String, Object> details = new HashMap<>();
		for (String subscriptionId : kafkaDataSource.getActiveSubscriptions()) {
			details.put(subscriptionId, kafkaDataSource.getSubscriptionDetails(subscriptionId));
		}
		result.put("subscriptionDetails", details);

		return result;
	}

	@Comment("清理所有订阅")
	public void cleanup() {
		valid();
		kafkaDataSource.cleanup();
	}

	@Comment("暂停订阅（停止消费但不释放资源）")
	public boolean pauseSubscription(@Comment(name = "topicName", value = "主题名称") String topicName) {
		valid();
		StringBuffer status = new StringBuffer("");
		getSubscriptionIds(topicName).forEach(subscriptionId -> {
			if (!kafkaDataSource.pauseSubscription(subscriptionId)) {
				status.append("," + subscriptionId);
			}
		});
		if (status.toString().equals("")) {
			return true;
		}
		return false;
	}

	@Comment("恢复暂停的订阅")
	public boolean resumeSubscription(@Comment(name = "topicName", value = "主题名称") String topicName) {
		valid();
		StringBuffer status = new StringBuffer("");
		getSubscriptionIds(topicName).forEach(subscriptionId -> {
			if (!kafkaDataSource.resumeSubscription(subscriptionId)) {
				status.append("," + subscriptionId);
			}
		});
		if (status.toString().equals("")) {
			return true;
		}
		return false;
	}

	private Set<String> getSubscriptionIds(String topicName) {
		return subscriptionTopics.entrySet().stream().filter(entry -> topicName.equals(entry.getValue()))
				.map(Map.Entry::getKey).collect(Collectors.toSet());
	}
}