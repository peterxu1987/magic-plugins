package org.ssssssss.magicapi.tcp.util;

public interface TcpServer {
	
	public String getId();
	
	public void shutdown();
	
	 public void setTcpHander(TcpHander tcpcHander);
	 public void start();
 
}
