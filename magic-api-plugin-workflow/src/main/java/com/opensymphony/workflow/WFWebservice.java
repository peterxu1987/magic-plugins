package com.opensymphony.workflow;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.spi.WorkflowEntry;
import com.opensymphony.workflow.util.WorkFlowSpringManager;


@SuppressWarnings("rawtypes")
public class WFWebservice {
	  public WorkflowContext context;
	  public WorkflowEntry entry;
	public Map transientVars;
	  public Map args;
	  public PropertySet ps;
  
	public  WFWebservice(WorkflowContext context ,WorkflowEntry entry ,Map transientVars, Map args, PropertySet ps) {
		 this.context = context;
		 this.entry = entry;
		 this.transientVars = transientVars;
		 this.args = args;
		 this.ps = ps;
	}

	//~ Methods ////////////////////////////////////////////////////////////////
    public void classForName(String className,String method) throws Exception{
    		invokeMethod(Class.forName(className).newInstance(), method, new Class<?>[]{WorkflowContext.class ,WorkflowEntry.class ,Map.class, Map.class, PropertySet.class}, new Object[]{context , entry , transientVars,  args,  ps});
    }
    
    public void spring(String beanName, String method) throws Exception{
    		invokeMethod(WorkFlowSpringManager.getBean(beanName), method, new Class<?>[]{WorkflowContext.class ,WorkflowEntry.class ,Map.class, Map.class, PropertySet.class}, new Object[]{context , entry , transientVars,  args,  ps});
    }

    /**
     * 直接调用对象方法, 无视private/protected修饰符. 用于一次性调用的情况.
     */
    public static Object invokeMethod(Object obj, String methodName, Class<?>[] parameterTypes, final Object[] args) {
        Method method = getAccessibleMethod(obj, methodName, parameterTypes);
        if (method == null) {
            throw new IllegalArgumentException("没有发现 方法 [" + methodName + "] 在目标类  [" + obj + "] 中");
        }

        try {
            return method.invoke(obj, args);
        } catch (Exception e) {
            throw convertReflectionExceptionToUnchecked(e);
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredMethod,并强制设置为可访问. 如向上转型到Object仍无法找到, 返回null.
     * 
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object...
     * args)
     */
    public static Method getAccessibleMethod(Object obj, String methodName, final Class<?>... parameterTypes) {

        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Method method = superClass.getDeclaredMethod(methodName, parameterTypes);

                method.setAccessible(true);

                return method;

            } catch (NoSuchMethodException e) {
                // Nothing to do here , go to superclass
            }
        }
        return null;
    }

    /**
     * 将反射时的checked exception转换为unchecked exception.
     */
    public static RuntimeException convertReflectionExceptionToUnchecked(Exception e) {
        if (e instanceof IllegalAccessException || e instanceof IllegalArgumentException
                || e instanceof NoSuchMethodException) {
            return new IllegalArgumentException("反射异常.", e);
        } else if (e instanceof InvocationTargetException) {
            return new RuntimeException("反射异常.", ((InvocationTargetException) e).getTargetException());
        } else if (e instanceof RuntimeException) {
            return (RuntimeException) e;
        }
        return new RuntimeException("意外的异常截取.", e);
    }
	
	
}
