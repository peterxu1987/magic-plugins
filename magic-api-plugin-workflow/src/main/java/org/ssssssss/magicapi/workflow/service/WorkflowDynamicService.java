package org.ssssssss.magicapi.workflow.service;

import org.ssssssss.magicapi.workflow.utils.DynamicWorkflowFactory;

public class WorkflowDynamicService {

	private DynamicWorkflowFactory workflowFactory;

	public WorkflowDynamicService(DynamicWorkflowFactory workflowFactory) {
		this.workflowFactory = workflowFactory;
	}

	// 动态添加一个流程
	public void addWorkflow(String workFlowName, String workFlowXmlContent) throws Exception {
		// 从XML字符串加载
		workflowFactory.addWorkflowFromXml(workFlowName, workFlowXmlContent);
	}

}