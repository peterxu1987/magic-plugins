package com.opensymphony.workflow.basic;

import com.opensymphony.workflow.PcAbstractWorkflow;


 
public class PcWorkflow extends PcAbstractWorkflow {

    public PcWorkflow(String caller) {
        super.context = new BasicWorkflowContext(caller);
    }
}
