package com.opensymphony.workflow.util;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
 
/**
 * 反射工具类.
 * 
 * 提供访问私有变量,获取泛型类型Class, 提取集合中元素的属性, 转换字符串到对象等Util函数.
 * 
 * @author 徐海洋
 */
@SuppressWarnings({"unchecked" ,"rawtypes"})
public class ReflectionHelp {

    /**
     * 调用方法.
     */
    public static Object invokeClassMethod(Object obj, String methodName) {
        return invokeMethod(obj, methodName, new Class[] {}, new Object[] {});
    }

    /**
     * 调用Getter方法.
     */
    public static Object invokeGetterMethod(Object obj, String propertyName) {
        String getterMethodName = "get" + capitalize(propertyName);
        return invokeMethod(obj, getterMethodName, new Class[] {}, new Object[] {});
    }

    /**
     * 调用Setter方法.使用value的Class来查找Setter方法.
     */
    public static void invokeSetterMethod(Object obj, String propertyName, Object value) {
        invokeSetterMethod(obj, propertyName, value, null);
    }

    /**
     * 调用Setter方法.
     * 
     * @param propertyType
     *            用于查找Setter方法,为空时使用value的Class替代.
     */
    public static void invokeSetterMethod(Object obj, String propertyName, Object value, Class<?> propertyType) {
        Class<?> type = propertyType != null ? propertyType : value.getClass();
        String setterMethodName = "set" + capitalize(propertyName);
        invokeMethod(obj, setterMethodName, new Class[] { type }, new Object[] { value });
    }

    /**
     * 直接读取对象属性值, 无视private/protected修饰符, 不经过getter函数.
     */
    public static Object getFieldValue(Object obj, String fieldName) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("没有发现 方法 [" + fieldName + "] 在目标类  [" + obj + "] 中");
        }

        Object result = null;
        try {
            result = field.get(obj);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
        return result;
    }

    /**
     * 直接设置对象属性值, 无视private/protected修饰符, 不经过setter函数.
     */
    public static void setFieldValue(Object obj, String fieldName, Object value) {
        Field field = getAccessibleField(obj, fieldName);

        if (field == null) {
            throw new IllegalArgumentException("没有发现 方法 [" + fieldName + "] 在目标类  [" + obj + "] 中");
        }

        try {
            field.set(obj, value);
        } catch (IllegalAccessException e) {
          e.printStackTrace();
        }
    }

    /**
     * 循环向上转型, 获取对象的DeclaredField, 并强制设置为可访问.
     * 
     * 如向上转型到Object仍无法找到, 返回null.
     */
    public static Field getAccessibleField(Object obj, String fieldName) {
        for (Class<?> superClass = obj.getClass(); superClass != Object.class; superClass = superClass.getSuperclass()) {
            try {
                Field field = superClass.getDeclaredField(fieldName);
                field.setAccessible(true);
                return field;
            } catch (NoSuchFieldException e) {// NOSONAR
                // Field不在当前类定义,继续向上转型
            }
        }
        return null;
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
     * 用于方法需要被多次调用的情况. 先使用本函数先取得Method,然后调用Method.invoke(Object obj, Object...  args)
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
     * 通过反射, 获得Class定义中声明的父类的泛型参数的类型.
     * 
     * @param clazz
     *            userDao.class eg. public UserDao extends HibernateDao<User>
     * @return User.class
     */
	public static <T> Class<T> getSuperClassGenricType(Class clazz) {
        return getSuperClassGenricType(clazz, 0);
    }

    /**
     * 通过反射, 获得Class定义中声明的父类的泛型参数的类型. 如无法找到, 返回Object.class.
     * 
     * 如public UserDao extends HibernateDao<User,Long>
     * 
     * @param clazz
     *            userDao.class eg. public UserDao extends
     *            HibernateDao<User,Long>
     * @param index
     *            <User,Long> 这个里面的坐标为 user 在0位 Long在1为
     * @return Long.class
     */
    public static Class getSuperClassGenricType(Class clazz, int index) {

        Type genType = clazz.getGenericSuperclass();

        if (!(genType instanceof ParameterizedType)) {
           System.out.println("父类 " + clazz.getSimpleName() + " 并非泛型");
            return Object.class;
        }

        Type[] params = ((ParameterizedType) genType).getActualTypeArguments();

        if (index >= params.length || index < 0) {
        		System.out.println("父类 " + clazz.getSimpleName() + "不存在 " + index + " 这个下标位的泛型对象 ,最大下标位为 " + params.length);
            return Object.class;
        }
        if (!(params[index] instanceof Class)) {
            System.out.println("父类 " + clazz.getSimpleName() + " 的" + index + " 下标位上的对象的数据类型并非通用类型");
            return Object.class;
        }

        return (Class) params[index];
    }

    /***
     * 将一个map集合信息柱状成Bean信息
     * 
     * @param map
     *            数据集合
     * @param clazz
     *            接受集合信息的对象
     * @return Bean对象
     * @throws Exception
     *             全局异常
     */
    public static Object parseObject(Map<String, Object> map, Class clazz) throws Exception {
        Object obj = clazz.newInstance();
        Field[] fs = clazz.getDeclaredFields();
        for (Field f : fs) {
            String value = String.valueOf(map.get(f.getName().toLowerCase()));
            if (value == null) {
            } else {
                String methodName = "set" + capitalize(f.getName());
                Class[] cl = new Class[1];
                cl[0] = f.getType();
                Method md = clazz.getDeclaredMethod(methodName, cl);
                md.invoke(obj, value);
            }
        }
        return obj;
    }

    /***
     * 将一个list集合信息柱状成Bean信息的集合
     * 
     * @param list
     *            数据集合
     * @param clazz
     *            接受集合信息的对象
     * @return List<Bean>对象
     * @throws Exception
     *             全局异常
     */
    public static List<Object> parseCollectionOfObject(List<Map<String, Object>> list, Class clazz) throws Exception {
        List<Object> result = new LinkedList<Object>();
        for (Map<String, Object> map : list) {
            Object obj = clazz.newInstance();
            Field[] fs = clazz.getDeclaredFields();
            for (Field f : fs) {
                String value = String.valueOf(map.get(f.getName().toLowerCase()));
                if (value == null) {
                } else {
                    String methodName = "set" + capitalize(f.getName());
                    Class[] cl = new Class[1];
                    cl[0] = f.getType();
                    Method md = clazz.getDeclaredMethod(methodName, cl);
                    md.invoke(obj, value);
                }
            }
            result.add(obj);
            obj = null;
        }
        return result;
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
    
    public static String capitalize(String name) {
    	 StringBuffer sb = new StringBuffer();
    	 if(name != null){
    		 sb.append(name.substring(0,1).toUpperCase()).append(name.substring(1));
    	 }
    	 return sb.toString();
    }
}
