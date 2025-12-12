package org.ssssssss.magicapi.kafka.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.kafka.model.KafkaInfo;
import org.ssssssss.magicapi.kafka.util.KafkaDataSource;

public class MagicKafkaController extends MagicController implements MagicExceptionHandler {

	public MagicKafkaController(MagicConfiguration configuration) {
		super(configuration);
	}

	@RequestMapping("/kafka/jdbc/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody KafkaInfo properties) {
		try {
			KafkaDataSource kafkaDataSource = new KafkaDataSource(properties);
			if(kafkaDataSource.validate()){
				kafkaDataSource.close();
				return new JsonBean<>("ok");
			}else {
				kafkaDataSource.close();
				return new JsonBean<>("连接失败！");
			}
		} catch (Exception e) {
			return new JsonBean<>(e.getMessage());
		}
	}
	
}
