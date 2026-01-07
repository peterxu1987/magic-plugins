package org.ssssssss.magicapi.workflow;

import java.util.Map;

import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.workflow.service.WorkflowDynamicService;
import org.ssssssss.script.annotation.Comment;

import com.opensymphony.workflow.service.AbstractWorkflowService;
import com.opensymphony.workflow.spi.hibernate.SpringWorkflowFactory;

/**
 * 工作流模块
 *
 * @author xuhaiyang
 */
@MagicModule("workflow")
public class WorkFlowModule {

	private AbstractWorkflowService abstractWorkflowService;
	private SpringWorkflowFactory workflowFactory;
	private WorkflowDynamicService workflowService;

	public WorkFlowModule(
			AbstractWorkflowService abstractWorkflowService,
			SpringWorkflowFactory workflowFactory,
			WorkflowDynamicService workflowService) {
		this.abstractWorkflowService = abstractWorkflowService;
		this.workflowFactory = workflowFactory;
		this.workflowService = workflowService;
	}
	
	@Comment("重新加载workflows.xml配置文件")
	public void reloadXml() {
		try {
			workflowFactory.init();
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("重新加载workflows.xml配置文件错误！！！");
		}
	}
	
	@Comment("初始化工作流实例，并返回流程实例 wf_id ")
	public String init(
			@Comment(name = "workflowName", value = "流程文件名称")  String workflowName, 
			@Comment(name = "initializeActionId", value = "流程初始化动作编号") int initializeActionId,
			@Comment(name = "username", value = "执行人名称或编号") String username, 
			@Comment(name = "inputs", value = "初始化表单数据")Map<String, Object> inputs
			) {
		try {
			return abstractWorkflowService.doInitialize("example", 100, "templeUser", inputs);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("workflow(\"workflowName="+workflowName+";initializeActionId="+initializeActionId+"\") init error!!!");
			return null;
		}
	}
	
	@Comment("执行任务")
	public void doAction(
			@Comment(name = "wf_id", value = "流程实例 编号")  String wf_id, 
			@Comment(name = "step_id", value = "流程步骤编号") int step_id,
			@Comment(name = "action_id", value = "流程步骤中动作编号") int action_id,
			@Comment(name = "username", value = "执行人名称或编号") String username, 
			@Comment(name = "inputs", value = "初始化表单数据")Map<String, Object> inputs
			) {
		try {
			abstractWorkflowService.doAction(wf_id, action_id, step_id, username, inputs);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("workflow(\"wf_id="+wf_id+"\") action exec error!!!");
		}
	}
	
	@Comment("动态添加工作流")
	public void addWorkflow(
			@Comment(name = "workFlowName", value = "流程名称")  String workFlowName, 
			@Comment(name = "workFlowXmlContent", value = "流程图XML文件字符串") String workFlowXmlContent
			) {
		try { 
			workflowService.addWorkflow(workFlowName, workFlowXmlContent);
		} catch (Exception e) {
			e.printStackTrace();
			System.err.println("动态添加工作流【"+workFlowName+"】异常！！！");
		}
	}
	
	
	
}