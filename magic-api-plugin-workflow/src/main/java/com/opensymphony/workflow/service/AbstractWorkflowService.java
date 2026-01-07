package com.opensymphony.workflow.service;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import com.opensymphony.workflow.Workflow;
import com.opensymphony.workflow.basic.PcWorkflow;
import com.opensymphony.workflow.config.Configuration;
import com.opensymphony.workflow.spi.Step;
import com.opensymphony.workflow.util.WorkFlowSpringManager;

public class AbstractWorkflowService {
	public Configuration osworkflowConfiguration;
	public BaseWorkFlowService callbackService = null;
	
	public BaseWorkFlowService getCallbackClass() throws Exception {
    	try {
    		if(callbackService == null){
    			callbackService = (BaseWorkFlowService)WorkFlowSpringManager.getBean("workFlowService");
    		}
		} catch (Exception e) {
			throw new Exception("获取工作流业务回调服务类出错，"+e.getMessage());
		}
    	return callbackService;
	}

	public String doInitialize(String workflowName, int initializeActionId,
			String username, Map<String, Object> inputs) throws Exception {
		Workflow wf = new PcWorkflow(username);
		wf.setConfiguration(osworkflowConfiguration);
		String wf_id = wf.initialize(workflowName, initializeActionId, inputs);
	    Collection<?> currentSteps = wf.getCurrentSteps(wf_id);
		getCurrentStepInfo(wf, currentSteps,wf_id, inputs);
		return wf_id;
	}
	
 
	public void doAction(String wf_id, int action_id, int step_id,String username,Map<String, Object> inputs) throws Exception {
		Workflow wf = new PcWorkflow(username);
		wf.setConfiguration(osworkflowConfiguration);
		wf.doAction(wf_id, action_id, inputs);
		
		boolean joinReject;
		try {
			joinReject = wf.getPropertySet(wf_id).getBoolean("joinReject");
		} catch (Exception e) {
			joinReject = false;
		}
		
		if(joinReject){
			String sql = "DELETE FROM OS_CURRENTSTEP_PREV WHERE ID IN (SELECT ID FROM OS_CURRENTSTEP WHERE ENTRY_ID = ? AND STATUS = ? )";
			WorkFlowSpringManager.getJdbcTemplate().update(sql, new Object[]{wf_id,"JOIN"});
			sql = "DELETE FROM OS_CURRENTSTEP WHERE ENTRY_ID = ? AND STATUS = ?";
			WorkFlowSpringManager.getJdbcTemplate().update(sql, new Object[]{wf_id,"JOIN"});
			wf.getPropertySet(wf_id).setBoolean("joinReject",false);
		}
		Collection<?> currentSteps = wf.getCurrentSteps(wf_id);
		getCurrentStepInfo(wf, currentSteps ,wf_id, inputs);
		if(currentSteps.size() > 1){
			  Iterator<?> iterator=currentSteps.iterator();
		      while(iterator.hasNext()){
		    	 	Step currentStep = (Step) iterator.next();
		    	 	if(currentStep.getStatus().equals("Underway")){
		    	 		String sql = "UPDATE OS_CURRENTSTEP SET STATUS = ? WHERE ENTRY_ID = ?";
						WorkFlowSpringManager.getJdbcTemplate().update(sql, new Object[]{"JOIN",wf_id});
		    	 	}
		      }
       	}
	}
	
	
	public void getCurrentStepInfo(Workflow wf, Collection<?> currentSteps, String entryId, Map<String, Object> inputs) throws Exception{

		 //获取下一步    步骤id
   
       Iterator<?> iterator=currentSteps.iterator();
       while(iterator.hasNext()){
	       	Step currentStep = (Step) iterator.next();
	       	String owner=currentStep.getOwner();//获取下一步步骤的所有人职称  并且通知当事人
	       	int stepId=currentStep.getStepId();
	       	String status = currentStep.getStatus();
	       
	       	//获取步骤中所有action  每个action组成按钮  
	       	final String nextActionsJson = wf.getAvailableAutoActionsJSONForStep(entryId,currentStep.getStepId());
	       	if(status.equals("Underway")){
	       		getCallbackClass().doCallback(entryId, stepId, owner, nextActionsJson, inputs);
	       	}
       }

}
	

	public Configuration getOsworkflowConfiguration() {
		return osworkflowConfiguration;
	}

	public void setOsworkflowConfiguration(Configuration osworkflowConfiguration) {
		this.osworkflowConfiguration = osworkflowConfiguration;
	}
}