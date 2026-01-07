package com.opensymphony.workflow.util;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

public class PropertiesHelp {

	private static PropertiesHelp instance = null;

	/**
	 * 初始化工具类
	 * 
	 * @return 工具对象
	 */
	public static PropertiesHelp getInstance() {
		if (instance == null) {
			instance = new PropertiesHelp();
		}
		return instance;
	}

	/**
	 * 获取读取文件对象.
	 * 
	 * @param propertiesName
	 *            文件名称
	 * @param path
	 *            文件路径
	 * @return 文件对象.
	 * @throws Exception
	 */
	private Properties getResource(final InputStream inputStream) throws Exception {
		synchronized (PropertiesHelp.class) {
			Properties properties = new Properties();
			properties.load(inputStream);
			inputStream.close(); // 关闭流
			return properties;
		}
	}

	/**
	 * 读取资源文件,并处理中文乱码
	 * 
	 * @param propertiesName
	 *            文件名称
	 * @param path
	 *            文件路径
	 * @param key
	 *            key
	 * @return val.
	 * @throws Exception
	 */
	public String readPropertiesFile(final String propertiesName, final String key,final String... path) throws Exception {
		InputStream inputStream = null;
		if(path.length == 0){
			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(propertiesName+".properties");
		}else{
			inputStream =  new FileInputStream(path[0] + File.separator + propertiesName + ".properties");
		}
		 // 处理中文乱码
		return new String(getResource(inputStream).getProperty(key).getBytes("ISO-8859-1"), "UTF-8");
	}
	
	
	/***
	 * 获取目录下指定名称指定后缀的文件名称数组
	 * @param file
	 * 			文件夹目录流对象
	 * @param modelName
	 * 			文件名称
	 * @param type
	 * 			文件的后缀如 .json .txt
	 * @return [{}]
	 */
	public  List<Map<String,String>> getFileNameInDirectory(File file, String fileName, String type) {
		List<Map<String,String>> fileNameList = new LinkedList<Map<String,String>>();
		File[] files = file.listFiles();
		if (files == null)
			return fileNameList;
		List<File> fileList = new ArrayList<File>();
		for (File f : files) {
			fileList.add(f);
		}
		Collections.sort(fileList, new Comparator<File>() {
			public int compare(File o1, File o2) {
				if (o1.isDirectory() && o2.isFile())
					return -1;
				if (o1.isFile() && o2.isDirectory())
					return 1;
				return o2.getName().compareTo(o1.getName());
			}
		});
	
		for (File f : fileList) {
			if (!f.isDirectory() && f.getName().endsWith(type)) {
				Map<String,String> map = new LinkedHashMap<String,String>();
				if (fileName != null && f.getName().indexOf(fileName) != -1) {
					map.put("name", f.getName());
				}
				if (fileName == null) {
					map.put("name", f.getName());
				}
				fileNameList.add(map);
			}
		}
		return fileNameList;
	}

	// 根据key读取value
	public  String readValue(String filePath, String key) {
		Properties props = new Properties();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			props.load(in);
			String value = props.getProperty(key);
			return value;
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	// 读取properties的全部信息
	public  List<Map<String,String>> readProperties(String filePath) {
		Properties props = new Properties();
		List<Map<String,String>> result = new ArrayList<Map<String,String>>();
		try {
			InputStream in = new BufferedInputStream(new FileInputStream(
					filePath));
			props.load(in);
			Enumeration<?> en = props.propertyNames();
			
			while (en.hasMoreElements()) {
				String key = (String) en.nextElement();
				Map<String,String> map = new HashMap<String,String>();
				map.put("key", key);
				String Property = props.getProperty(key);
				map.put("value", Property);
				result.add(map);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return result;
	}
	
	 /** 
     * 修改或添加键值对 如果key存在，修改, 反之，添加。 
     * @param filePath 文件路径，即文件所在包的路径，例如：java/util/config.properties 
     * @param key 键 
     * @param value 键对应的值 
	 * @throws IOException 
     */  
	public  void writeProperties(String filePath, String parameterName,
			String parameterValue) throws IOException{
		Properties prop = new Properties();
			InputStream fis = new FileInputStream(filePath);
			// 从输入流中读取属性列表（键和元素对）
			prop.load(fis);
			// 调用 Hashtable 的方法 put。使用 getProperty 方法提供并行性。
			// 强制要求为属性的键和值使用字符串。返回值是 Hashtable 调用 put 的结果。
			OutputStream fos = new FileOutputStream(filePath);
			prop.setProperty(parameterName, parameterValue);
			// 以适合使用 load 方法加载到 Properties 表中的格式，
			// 将此 Properties 表中的属性列表（键和元素对）写入输出流
			prop.store(fos, "Update '" + parameterName + "' value");
			   fos.close();  
	}
	/** 
	 * 删除 根据 parameterName。 
	 * @param filePath 文件路径，即文件所在包的路径，例如：java/util/config.properties 
	 * @param parameterName 键 
	 * @throws IOException 
	 */  
	public  void deleteProperties(String filePath, String parameterName) throws IOException{
		Properties prop = new Properties();
		InputStream fis = new FileInputStream(filePath);
		// 从输入流中读取属性列表（键和元素对）
		prop.load(fis);
		prop.remove(parameterName);
		OutputStream fos = new FileOutputStream(filePath);
		 
		prop.store(fos, "Delete "+parameterName);
		fos.close();  
	}
}
