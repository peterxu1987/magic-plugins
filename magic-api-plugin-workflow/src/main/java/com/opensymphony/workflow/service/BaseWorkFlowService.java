package com.opensymphony.workflow.service;

import java.util.Map;

public abstract class BaseWorkFlowService {
	
	/***
	 * 流程提交动作执行后的回调函数
	 * @param wf_id
	 * 			流程实例编号
	 * @param step_id
	 * 			当前执行流程的的步骤编号
	 * @param owner
	 * 			流程的下一步审核人的表达式字符串
	 * @param nextActionsJson
	 * 			当前步骤下可操作的动作集
	 * @param inputs
	 * @throws Exception
	 */
	public abstract void doCallback(String wf_id, int step_id, String owner, String nextActionsJson,Map<String, Object> inputs) throws Exception;

}
