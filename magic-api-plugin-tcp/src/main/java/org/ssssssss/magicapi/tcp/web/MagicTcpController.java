package org.ssssssss.magicapi.tcp.web;

import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.model.JsonBean;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.tcp.model.TcpInfo;
import org.ssssssss.magicapi.tcp.util.TcpServerDatasource;

public class MagicTcpController extends MagicController implements MagicExceptionHandler {

	public MagicTcpController(MagicConfiguration configuration) {
		super(configuration);
	}

	@RequestMapping("/tcp/jdbc/test")
	@ResponseBody
	public JsonBean<String> test(@RequestBody TcpInfo properties) {
		try {
			new TcpServerDatasource(properties);
		} catch (Exception e) {
			return new JsonBean<>(e.getMessage());
		}
		return new JsonBean<>("ok");
	}
	
}
