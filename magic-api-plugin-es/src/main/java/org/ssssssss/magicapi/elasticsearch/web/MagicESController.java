package org.ssssssss.magicapi.elasticsearch.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.elasticsearch.model.ESInfo;
import org.ssssssss.magicapi.elasticsearch.util.ESDatasource;

public class MagicESController extends MagicController implements MagicExceptionHandler {

	public MagicESController(MagicConfiguration configuration) {
		super(configuration);
	}

	@RequestMapping("/es/jdbc/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody ESInfo properties) {
		try {
			ESDatasource esDatasource = new ESDatasource(properties);
			boolean isConnected = esDatasource.ping();
			if (isConnected) {
				esDatasource.close();
			    return new JsonBean<>("ok");
			} else {
			    return new JsonBean<>("ES 连接失败");
			}
		} catch (Exception e) {
			return new JsonBean<>(e.getMessage());
		}
	}
	
}
