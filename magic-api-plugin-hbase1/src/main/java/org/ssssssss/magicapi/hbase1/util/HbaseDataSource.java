package org.ssssssss.magicapi.hbase1.util;

import java.util.HashMap;
import java.util.Map;

import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.client.Connection;
import org.apache.hadoop.hbase.client.ConnectionFactory;
import org.ssssssss.magicapi.hbase1.model.HbaseInfo;
public class HbaseDataSource{
	
	private String id = "";
	private Connection connection;
	
	public HbaseDataSource(HbaseInfo info) throws Exception {
		this.id = info.getId();
		Map<String, Object> properties = new HashMap<>(info.getProperties());
		
			String zkUrls = properties.get("zkUrls").toString();
			String hbaseRootdir = properties.get("hbaseRootdir").toString();
			//获取配置
	        Configuration conf = HBaseConfiguration.create();
	        conf.set("hbase.zookeeper.quorum", zkUrls);
	        //指定HBase在hdfs上的根目录
	        conf.set("hbase.rootdir", hbaseRootdir);
	        // 使用本地文件系统
	        if(hbaseRootdir.indexOf("hdfs://") == -1) {
	        	// 使用本地文件系统
	        	conf.set("hbase.cluster.distributed", "false"); 
	        }
	        //创建HBase连接，负责对HBase中的数据的一些增删改查（DML操作）
	        connection = ConnectionFactory.createConnection(conf);
	}

	public Connection getConnection() {
		return connection;
	}
	
	public String getId() {
		return id;
	}

	public void close() {
		try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (Exception e) {
            System.err.println("关闭连接时出错: " + e.getMessage());
        }
	}
}
