/*
 * Copyright (c) 2002-2003 by OpenSymphony
 * All rights reserved.
 */
package com.opensymphony.workflow.util.beanshell;

import java.util.HashMap;
import java.util.Map;

import org.ssssssss.magicapi.utils.ScriptManager;
import org.ssssssss.script.MagicScriptContext;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.PcAbstractWorkflow;
import com.opensymphony.workflow.WorkflowContext;
import com.opensymphony.workflow.WorkflowException;
import com.opensymphony.workflow.spi.WorkflowEntry;

@SuppressWarnings({"rawtypes"})
public class BeanShellFunctionProvider implements FunctionProvider {

    public void execute(Map transientVars, Map args, PropertySet ps) throws WorkflowException {
        String script = (String) args.get(PcAbstractWorkflow.BSH_SCRIPT);
        WorkflowContext context = (WorkflowContext) transientVars.get("context");
        WorkflowEntry entry = (WorkflowEntry) transientVars.get("entry");
    	
    	Map<String,Object> body = new HashMap<String,Object>();
    	body.put("base_data", entry);
        body.put("rollback", context);
        body.put("form_data", transientVars);
        body.put("global_data", ps);
        body.put("join", transientVars.get("jn"));
        Map<String,Object> parms = new HashMap<String,Object>();
        parms.put("wf", body);
        
        MagicScriptContext magicScriptContext = new MagicScriptContext();
        magicScriptContext.putMapIntoContext(parms);
		ScriptManager.executeScript(script, magicScriptContext);
    	 
    }
}
