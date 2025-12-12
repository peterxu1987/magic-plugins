package org.ssssssss.magicapi.hbase1.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.hbase1.model.HbaseInfo;
import org.ssssssss.magicapi.hbase1.util.HbaseDataSource;

public class MagicHbaseController extends MagicController implements MagicExceptionHandler {

	public MagicHbaseController(MagicConfiguration configuration) {
		super(configuration);
	}

	@RequestMapping("/hbase1/jdbc/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody HbaseInfo properties) {
		try {
			HbaseDataSource hbaseDataSource = new HbaseDataSource(properties);
			hbaseDataSource.close();
		} catch (Exception e) {
			return new JsonBean<>(e.getMessage());
		}
		return new JsonBean<>("ok");
	}
	
}
