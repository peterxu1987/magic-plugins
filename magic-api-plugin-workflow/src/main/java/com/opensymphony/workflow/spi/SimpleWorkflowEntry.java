 
package com.opensymphony.workflow.spi;

import java.io.Serializable;

import com.opensymphony.workflow.spi.WorkflowEntry;


public class SimpleWorkflowEntry implements WorkflowEntry, Serializable {
	private static final long serialVersionUID = 1L;
	protected String workflowName;
    protected boolean initialized;
    protected int state;
    protected String id;

    //~ Constructors ///////////////////////////////////////////////////////////

    public SimpleWorkflowEntry(String id, String workflowName, int state) {
        this.id = id;
        this.workflowName = workflowName;
        this.state = state;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setState(int state) {
        this.state = state;
    }

    public int getState() {
        return state;
    }

    public void setWorkflowName(String workflowName) {
        this.workflowName = workflowName;
    }

    public String getWorkflowName() {
        return workflowName;
    }
}
