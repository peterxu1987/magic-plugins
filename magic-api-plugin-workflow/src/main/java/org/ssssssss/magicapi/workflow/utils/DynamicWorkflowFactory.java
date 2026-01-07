package org.ssssssss.magicapi.workflow.utils;

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import com.opensymphony.workflow.FactoryException;
import com.opensymphony.workflow.loader.WorkflowDescriptor;
import com.opensymphony.workflow.loader.WorkflowLoader;
import com.opensymphony.workflow.spi.hibernate.SpringWorkflowFactory;

public class DynamicWorkflowFactory extends SpringWorkflowFactory {
    
	private static final long serialVersionUID = 1L;
	private Map<String, WorkflowDescriptor> dynamicWorkflows = new HashMap<>();
    
    @Override
    public WorkflowDescriptor getWorkflow(String name) throws FactoryException {
        // 先检查动态工作流
        if (dynamicWorkflows.containsKey(name)) {
            return dynamicWorkflows.get(name);
        }
        // 否则调用父类方法
        return super.getWorkflow(name);
    }
    
    /**
     * 从XML字符串动态添加工作流
     */
    public void addWorkflowFromXml(String name, String xmlContent) throws Exception {
    	 // 清理XML内容，移除多余的XML声明和DOCTYPE
        String cleanedXml = cleanXmlContent(xmlContent);
        
        WorkflowDescriptor descriptor = WorkflowLoader.load(
            new ByteArrayInputStream(cleanedXml.getBytes("UTF-8")), 
            false
        );
        descriptor.setName(name);
        dynamicWorkflows.put(name, descriptor);
    }
    /**
     * 清理XML内容，确保只有一个XML声明
     */
    private String cleanXmlContent(String xmlContent) {
        if (xmlContent == null || xmlContent.isEmpty()) {
            return xmlContent;
        }
        
        // 1. 移除所有重复的XML声明
        String cleaned = xmlContent;
        
        // 找到第一个XML声明的位置
        int firstDeclIndex = cleaned.indexOf("<?xml");
        if (firstDeclIndex != -1) {
            int endDeclIndex = cleaned.indexOf("?>", firstDeclIndex);
            if (endDeclIndex != -1) {
                // 提取第一个声明
                String firstDeclaration = cleaned.substring(firstDeclIndex, endDeclIndex + 2);
                
                // 移除所有其他声明
                String withoutDeclarations = cleaned.replaceAll("<\\?xml[^>]*\\?>", "");
                
                // 重新组合：第一个声明 + 剩余内容
                cleaned = firstDeclaration + withoutDeclarations;
            }
        }
        
        // 2. 确保DOCTYPE在正确的位置
        if (cleaned.contains("<!DOCTYPE")) {
            // 如果DOCTYPE在XML声明之前，需要调整
            int doctypeIndex = cleaned.indexOf("<!DOCTYPE");
            int xmlDeclIndex = cleaned.indexOf("<?xml");
            
            if (xmlDeclIndex != -1 && doctypeIndex < xmlDeclIndex) {
                // 提取DOCTYPE
                int doctypeEnd = cleaned.indexOf(">", doctypeIndex);
                String doctype = cleaned.substring(doctypeIndex, doctypeEnd + 1);
                
                // 移除原来的DOCTYPE
                String withoutDoctype = cleaned.replace(doctype, "");
                
                // 重新组合：XML声明 + DOCTYPE + 剩余内容
                int declEnd = withoutDoctype.indexOf("?>") + 2;
                String before = withoutDoctype.substring(0, declEnd);
                String after = withoutDoctype.substring(declEnd);
                cleaned = before + "\n" + doctype + "\n" + after;
            }
        }
        
        return cleaned.trim();
    }
    
    @Override
    public String[] getWorkflowNames() {
        // 合并动态工作流和配置的工作流
        String[] parentNames = super.getWorkflowNames();
        String[] dynamicNames = dynamicWorkflows.keySet().toArray(new String[0]);
        
        String[] allNames = new String[parentNames.length + dynamicNames.length];
        System.arraycopy(parentNames, 0, allNames, 0, parentNames.length);
        System.arraycopy(dynamicNames, 0, allNames, parentNames.length, dynamicNames.length);
        
        return allNames;
    }
    
    /**
     * 动态添加工作流
     */
    public void addWorkflow(String name, WorkflowDescriptor descriptor) {
        dynamicWorkflows.put(name, descriptor);
    }
    
    /**
     * 动态移除工作流
     */
    public boolean removeWorkflow(String name) {
    	if(dynamicWorkflows.get(name) != null) {
    		dynamicWorkflows.remove(name);
    		return true;
    	}
        return false;
    }
    
    /**
     * 清空动态工作流
     */
    public void clearDynamicWorkflows() {
        dynamicWorkflows.clear();
    }
}