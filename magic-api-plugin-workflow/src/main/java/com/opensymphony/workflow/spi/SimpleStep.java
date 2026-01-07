package com.opensymphony.workflow.spi;

import java.io.Serializable;
import java.util.Date;


public class SimpleStep implements Step, Serializable {
    //~ Static fields/initializers /////////////////////////////////////////////

    private static final long serialVersionUID = 1093783480189853982L;

    //~ Instance fields ////////////////////////////////////////////////////////

    private Date dueDate;
    private Date finishDate;
    private Date startDate;
    private String caller;
    private String owner;
    private String status;
    private String[] previousStepIds;
    private int actionId;
    private int stepId;
    private String entryId;
    private String id;

    //~ Constructors ///////////////////////////////////////////////////////////

    public SimpleStep() {
    }

    public SimpleStep(String id, String entryId, int stepId, int actionId, String owner, Date startDate, Date dueDate, Date finishDate, String status, String[] prevIds, String caller) {
        this.id = id;
        this.entryId = entryId;
        this.stepId = stepId;
        this.actionId = actionId;
        this.owner = owner;
        this.startDate = startDate;
        this.finishDate = finishDate;
        this.dueDate = dueDate;
        this.status = status;
        this.previousStepIds = prevIds;
        this.caller = caller;
    }

    //~ Methods ////////////////////////////////////////////////////////////////

    public void setActionId(int actionId) {
        this.actionId = actionId;
    }

    public int getActionId() {
        return actionId;
    }

    public void setCaller(String caller) {
        this.caller = caller;
    }

    public String getCaller() {
        return caller;
    }

    public Date getDueDate() {
        return dueDate;
    }

    public void setEntryId(String entryId) {
        this.entryId = entryId;
    }

    public String getEntryId() {
        return entryId;
    }

    public void setFinishDate(Date finishDate) {
        this.finishDate = finishDate;
    }

    public Date getFinishDate() {
        return finishDate;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public String getOwner() {
        return owner;
    }

    public void setPreviousStepIds(String[] previousStepIds) {
        this.previousStepIds = previousStepIds;
    }

    public String[] getPreviousStepIds() {
        return previousStepIds;
    }

    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    public Date getStartDate() {
        return startDate;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getStatus() {
        return status;
    }

    public void setStepId(int stepId) {
        this.stepId = stepId;
    }

    public int getStepId() {
        return stepId;
    }

    public String toString() {
        return "SimpleStep@" + stepId + "[owner=" + owner + ", actionId=" + actionId + ", status=" + status + "]";
    }
}
