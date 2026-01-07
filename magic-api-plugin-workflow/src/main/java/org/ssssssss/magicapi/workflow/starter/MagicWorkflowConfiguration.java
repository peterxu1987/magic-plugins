package org.ssssssss.magicapi.workflow.starter;

import java.util.Properties;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.JdbcTemplate;
import org.ssssssss.magicapi.core.config.MagicPluginConfiguration;
import org.ssssssss.magicapi.core.model.Plugin;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;
import org.ssssssss.magicapi.modules.servlet.RequestModule;
import org.ssssssss.magicapi.workflow.WorkFlowModule;
import org.ssssssss.magicapi.workflow.service.WorkFlowService;
import org.ssssssss.magicapi.workflow.service.WorkflowDynamicService;
import org.ssssssss.magicapi.workflow.utils.DynamicWorkflowFactory;

import com.opensymphony.workflow.config.SpringConfiguration;
import com.opensymphony.workflow.service.AbstractWorkflowService;
import com.opensymphony.workflow.spi.hibernate.SpringWorkflowFactory;
import com.opensymphony.workflow.spi.jdbc.DefaultJDBCTemplatePropertySetDelegate;
import com.opensymphony.workflow.spi.jdbc.JDBCTemplateWorkflowStore;
import com.opensymphony.workflow.util.SpringTypeResolver;
import com.opensymphony.workflow.util.WorkFlowSpringManager;

@Configuration
public class MagicWorkflowConfiguration implements MagicPluginConfiguration {

	@Autowired
	private JdbcTemplate template;
	@Autowired
	private MagicDynamicDataSource dynamicDataSource;
	
	@Autowired
	private RequestModule requestModule;
	
	@Override
	public Plugin plugin() {
//		return new Plugin("workflow工作流插件", "workflow", "magic-workflow.1.0.0.iife.js");
		return new Plugin("workflow");
	}

//	@Override
//	public MagicControllerRegister controllerRegister() {
//		return (mapping, configuration) -> mapping.registerController(new MagicMqttController(configuration));
//	}

	@Bean(name = "propertySetDelegate")
	@ConditionalOnMissingBean
	public DefaultJDBCTemplatePropertySetDelegate propertySetDelegate() {
		return new DefaultJDBCTemplatePropertySetDelegate();
	}

	@Bean(name = "WorkFlowSpringManager")
	@ConditionalOnMissingBean
	public WorkFlowSpringManager workFlowSpringManager() {
		return new WorkFlowSpringManager(template,dynamicDataSource,requestModule);
	}

	@Bean(name = "templateWorkflowStore")
	@ConditionalOnMissingBean
	public JDBCTemplateWorkflowStore templateWorkflowStore(
			@Qualifier("propertySetDelegate") DefaultJDBCTemplatePropertySetDelegate propertySetDelegate) {
		JDBCTemplateWorkflowStore jtws = new JDBCTemplateWorkflowStore();
		jtws.setPropertySetDelegate(propertySetDelegate);
		Properties p = new Properties();
		p.setProperty("history.table", "OS_HISTORYSTEP");
		p.setProperty("historyPrev.table", "OS_HISTORYSTEP_PREV");
		p.setProperty("current.table", "OS_CURRENTSTEP");
		p.setProperty("currentPrev.table", "OS_CURRENTSTEP_PREV");
		p.setProperty("step.id", "ID");
		p.setProperty("step.entryId", "ENTRY_ID");
		p.setProperty("step.stepId", "STEP_ID");
		p.setProperty("step.actionId", "ACTION_ID");
		p.setProperty("step.owner", "OWNER");
		p.setProperty("step.caller", "CALLER");
		p.setProperty("step.startDate", "START_DATE");
		p.setProperty("step.finishDate", "FINISH_DATE");
		p.setProperty("step.dueDate", "DUE_DATE");
		p.setProperty("step.status", "STATUS");
		p.setProperty("step.previousId", "PREVIOUS_ID");
		p.setProperty("step.sequence", "SELECT max(ID)+1 FROM OS_STEPIDS");
		p.setProperty("entry.sequence", "SELECT max(ID)+1 FROM OS_WFENTRY");
		p.setProperty("entry.table", "OS_WFENTRY");
		p.setProperty("entry.id", "ID");
		p.setProperty("entry.name", "NAME");
		p.setProperty("entry.state", "STATE");
		jtws.setJdbcTemplateProperties(p);
		return jtws;
	}

	@Bean(name = "workflowTypeResolver")
	@ConditionalOnMissingBean
	public SpringTypeResolver workflowTypeResolver() {
		return new SpringTypeResolver();
	}

	@Bean(name = "workflowFactory")
	@ConditionalOnMissingBean
	public DynamicWorkflowFactory workflowFactory() {
//		SpringWorkflowFactory springWorkflowFactory = new SpringWorkflowFactory();
//		springWorkflowFactory.setResource("workflows.xml");
//		springWorkflowFactory.setReload("true");
//		return springWorkflowFactory;
		DynamicWorkflowFactory dynamicWorkflowFactory = new DynamicWorkflowFactory();
		dynamicWorkflowFactory.setResource("workflows.xml");
		dynamicWorkflowFactory.setReload("true");
		return dynamicWorkflowFactory;
	}
	
	@Bean(name = "workflowService")
	@ConditionalOnMissingBean
	public WorkflowDynamicService workflowService(
		@Qualifier("workflowFactory") DynamicWorkflowFactory workflowFactory
	) {
		return new WorkflowDynamicService(workflowFactory);
	}

	@Bean(name = "osworkflowConfiguration")
	@ConditionalOnMissingBean
	public SpringConfiguration osworkflowConfiguration(
			@Qualifier("workflowFactory") SpringWorkflowFactory workflowFactory,
			@Qualifier("templateWorkflowStore") JDBCTemplateWorkflowStore templateWorkflowStore) {
		SpringConfiguration springConfiguration = new SpringConfiguration();
		springConfiguration.setStore(templateWorkflowStore);
		springConfiguration.setFactory(workflowFactory);
		return springConfiguration;
	}

	@Bean(name = "abstractWorkflowService")
	@ConditionalOnMissingBean
	public AbstractWorkflowService abstractWorkflowService(
			@Qualifier("osworkflowConfiguration") SpringConfiguration osworkflowConfiguration) {
		AbstractWorkflowService abstractWorkflowService = new AbstractWorkflowService();
		abstractWorkflowService.setOsworkflowConfiguration(osworkflowConfiguration);
		return abstractWorkflowService;
	}

	/**
	 * 注入workflow模块
	 */
	@Bean(name = "magicWorkFlowModule")
	@ConditionalOnMissingBean
	public WorkFlowModule magicWorkFlowModule(
			@Qualifier("abstractWorkflowService") AbstractWorkflowService abstractWorkflowService,
			@Qualifier("workflowFactory") SpringWorkflowFactory workflowFactory,
			@Qualifier("workflowService") WorkflowDynamicService workflowService
			) {
		return new WorkFlowModule(abstractWorkflowService, workflowFactory, workflowService);
	}

	@Bean(name = "workFlowService")
	@ConditionalOnMissingBean
	public WorkFlowService workFlowService() {
		return new WorkFlowService();
	}
}
