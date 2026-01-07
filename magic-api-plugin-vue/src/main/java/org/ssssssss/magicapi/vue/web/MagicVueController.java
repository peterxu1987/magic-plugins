package org.ssssssss.magicapi.vue.web;

import java.util.HashMap;
import java.util.Map;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.ssssssss.magicapi.core.config.MagicConfiguration;
import org.ssssssss.magicapi.core.exception.MagicAPIException;
import org.ssssssss.magicapi.core.web.MagicController;
import org.ssssssss.magicapi.core.web.MagicExceptionHandler;
import org.ssssssss.magicapi.utils.JsonUtils;
import org.ssssssss.magicapi.vue.model.VueInfo;
import org.ssssssss.magicapi.vue.service.VueMagicDynamicRegistry;
import org.ssssssss.script.annotation.Comment;

public class MagicVueController extends MagicController implements MagicExceptionHandler {
	
	private VueMagicDynamicRegistry vueMagicDynamicRegistry;
	
	public MagicVueController(MagicConfiguration configuration, VueMagicDynamicRegistry _vueMagicDynamicRegistry) {
		super(configuration);
		this.vueMagicDynamicRegistry = _vueMagicDynamicRegistry;
	}

	@RequestMapping("/vue/components")
	@ResponseBody
	public Object test(@RequestParam String componentName) {
		Map<String, Object> response = new HashMap<>();
		response.put("code", 200);
		response.put("data", invokeVue(componentName));
		return response;
	}
	
	@SuppressWarnings("unchecked")
	@Comment("执行VUE组件接口")
	public Object invokeVue(String vueComponentName) {
		
		VueInfo vueInfo = vueMagicDynamicRegistry.getMapping(vueComponentName);
		if (vueInfo == null) {
			throw new MagicAPIException(String.format("找不到对应VUE组件 [%s]", vueComponentName));
		}
		Map<String,String> result = JsonUtils.readValue(vueInfo.getScript(), Map.class);
		result.remove("script");
		return result;
	}
}
