package org.ssssssss.magicapi.workflow.service;

import java.util.Map;

import com.opensymphony.workflow.service.BaseWorkFlowService;

public class WorkFlowService extends BaseWorkFlowService {

	public void doCallback(String wf_id, int step_id, String owner, String nextActionsJson,Map<String, Object> inputs) throws Exception {
			System.out.println(
					"实例编号："+wf_id+
					" 下一步步骤编号："+step_id+
					" 下一步执行动作："+nextActionsJson+
					" 下一步执行人："+owner);
	}
}