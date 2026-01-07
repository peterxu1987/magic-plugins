package com.opensymphony.workflow.util;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;
import org.ssssssss.magicapi.datasource.model.MagicDynamicDataSource;
import org.ssssssss.magicapi.modules.servlet.RequestModule;

public class WorkFlowSpringManager implements ApplicationContextAware, ApplicationListener<ContextRefreshedEvent> {

	private static TransactionTemplate transactionTemplate;
	private static JdbcTemplate jdbcTemplate;
	private static ContextRefreshedEvent event;
	private static RequestModule requestModule;

	public WorkFlowSpringManager(
			JdbcTemplate jdbcTemplate, 
			MagicDynamicDataSource dynamicDataSource,
			RequestModule requestModule
			) {
		setJdbcTemplate(jdbcTemplate);

		TransactionTemplate transactionTemplate = new TransactionTemplate(
				dynamicDataSource.getDataSource().getDataSourceTransactionManager());
		setTransactionTemplate(transactionTemplate);
		setRequestModule(requestModule);
	}

	private static ApplicationContext appCtx;

	/**
	 * 此方法可以把ApplicationContext对象inject到当前类中作为一个静态成员变量。
	 * 
	 * @param applicationContext ApplicationContext 对象.
	 */
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		appCtx = applicationContext;
	}

	/**
	 * 获取ApplicationContext
	 */
	public static ApplicationContext getApplicationContext() {
		return appCtx;
	}

	/**
	 * 这是一个便利的方法，帮助我们快速得到一个BEAN
	 * 
	 * @param beanName bean的名字
	 * @return 返回一个bean对象
	 */
	public static Object getBean(String beanName) {
		try {
			return appCtx.getBean(beanName);
		} catch (Exception e) {
			return event.getApplicationContext().getBean(beanName);
		}
	}

	public void onApplicationEvent(ContextRefreshedEvent event) {
		setEvent(event);
	}

	public static TransactionTemplate getTransactionTemplate() {
		return transactionTemplate;
	}

	public static void setTransactionTemplate(TransactionTemplate transactionTemplate) {
		WorkFlowSpringManager.transactionTemplate = transactionTemplate;
	}

	public static JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}

	public static void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		WorkFlowSpringManager.jdbcTemplate = jdbcTemplate;
	}

	public static ContextRefreshedEvent getEvent() {
		return event;
	}

	public static void setEvent(ContextRefreshedEvent event) {
		WorkFlowSpringManager.event = event;
	}

	public static RequestModule getRequestModule() {
		return requestModule;
	}

	public static void setRequestModule(RequestModule requestModule) {
		WorkFlowSpringManager.requestModule = requestModule;
	}
	
}
