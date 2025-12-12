package org.ssssssss.magicapi.task;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.core.service.MagicResourceService;
import org.ssssssss.magicapi.task.model.TaskInfo;
import org.ssssssss.magicapi.task.service.TaskMagicDynamicRegistry;
import org.ssssssss.script.annotation.Comment;

@MagicModule("task")    // 模块名称
public class TaskModule {
	
	// 创建一个线程池调度器
    private ScheduledExecutorService scheduler;
	
	@Autowired
	MagicResourceService service;
	TaskMagicDynamicRegistry taskMagicDynamicRegistry;
	
	public TaskModule(TaskMagicDynamicRegistry taskMagicDynamicRegistry) {
		this.taskMagicDynamicRegistry = taskMagicDynamicRegistry;
		initScheduler(100);
	}

	private Map<String, TaskInfo> getTasks(String excludeUserName){
		List<TaskInfo> files = service.files("task");
		Map<String, TaskInfo> tasks = new HashMap<>();
		if(excludeUserName == null) {
			for (TaskInfo taskInfo : files) {
				String name = service.getScriptName(taskInfo);
				String path = name.substring(name.indexOf("(")+1,name.length() - 1);
				tasks.put(path, taskInfo);
			}
			return tasks;
		}
		for (TaskInfo taskInfo : files) {
			String name = service.getScriptName(taskInfo);
			String path = name.substring(name.indexOf("(")+1,name.length() - 1);
			if(taskInfo.getCreateBy() != null && !taskInfo.getCreateBy().equals(excludeUserName)) {
				tasks.put(path, taskInfo);
			}else if(taskInfo.getUpdateBy() != null && !taskInfo.getUpdateBy().equals(excludeUserName)) {
				tasks.put(path, taskInfo);
			}
		}
		return tasks;
	}
	 
    @Comment("启动设计器中的任务")
	public void startDesignerTask(@Comment(name = "taskPath", value = "任务路径")  String taskPath) {
    	Map<String, TaskInfo> tasks = getTasks(null);
    	TaskInfo ti = tasks.get(taskPath);
    	ti.setEnabled(true);
    	taskMagicDynamicRegistry.register(ti);
	}
    
    @Comment("暂停设计器中的任务")
    public void stopDesignerTask(@Comment(name = "taskPath", value = "任务路径")  String taskPath) {
    	Map<String, TaskInfo> tasks = getTasks(null);
    	TaskInfo ti = tasks.get(taskPath);
    	ti.setEnabled(false);
    	taskMagicDynamicRegistry.register(ti);
    }
    
    // 用于存储任务ID和其对应的ScheduledFuture对象
    private final Map<String, ScheduledFuture<?>> tasks = new ConcurrentHashMap<>();
    
    // 添加一个新的定时任务
    private void addTask(String taskId, Runnable task, long delay, long period, TimeUnit unit) {
    	tasks.remove(taskId);
        ScheduledFuture<?> scheduledTask = scheduler.scheduleAtFixedRate(task, delay, period, unit);
        tasks.put(taskId, scheduledTask);
        System.out.println("Task added: " + taskId);
    }
    
    @Comment("初始化调度器大小")
    public void initScheduler(@Comment(name = "size", value = "调度器的大小")  int size) {
    	this.scheduler = Executors.newScheduledThreadPool(size);
    }
    
    @Comment("关闭调度器并终止所有任务")
    public void shutdown() {
    	this.scheduler.shutdown();
    	tasks.clear();
        System.out.println("Scheduler shutdown.");
    }
    
    @Comment("添加一个新的定时任务")
    public void addTask(
    		@Comment(name = "taskId", value = "任务编号")  String taskId,
    		@Comment(name = "taskHandle", value = "定时任务的逻辑 ;\n\n 如：()->{...}") TaskHandle taskHandle, 
    		@Comment(name = "period", value = "任务间隔（毫秒）")  long period) {
    	
    	addTask(taskId, () ->{
        	taskHandle.hander();
        }, 0, period, TimeUnit.MILLISECONDS);
        
    }
    
    @Comment("添加一个新的定时任务")
    public void addTask(
    		@Comment(name = "taskId", value = "任务编号")  String taskId,
    		@Comment(name = "taskHandle", value = "定时任务的逻辑 ;\n\n 如：()->{...}") TaskHandle taskHandle, 
    		@Comment(name = "period", value = "任务间隔")  long period,
    		@Comment(name = "period", value = "任务间隔单位") TimeUnit unit) {
    	
    	addTask(taskId, () ->{
        	taskHandle.hander();
        }, 0, period, unit);
        
    }

    @Comment("取消指定的任务")
    public void cancelTask(@Comment(name = "taskId", value = "任务编号")  String taskId) {
        ScheduledFuture<?> scheduledTask = tasks.get(taskId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false); // 取消任务，不等待任务完成
            tasks.remove(taskId); // 从任务列表中移除
            System.out.println("Task cancelled: " + taskId);
        } else {
            System.out.println("Task not found: " + taskId);
        }
    }
}
