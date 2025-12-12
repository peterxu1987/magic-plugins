package org.ssssssss.magicapi.tcp;

import java.beans.Transient;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.ssssssss.magicapi.core.annotation.MagicModule;
import org.ssssssss.magicapi.tcp.model.MagicDynamicTcpClient;
import org.ssssssss.magicapi.tcp.packet.ErrorLog;
import org.ssssssss.magicapi.tcp.packet.PacketData;
import org.ssssssss.magicapi.tcp.util.ChannelCache;
import org.ssssssss.magicapi.tcp.util.ChannelCache.SubChannel;
import org.ssssssss.magicapi.tcp.util.SvrCst;
import org.ssssssss.magicapi.tcp.util.TcpHander;
import org.ssssssss.magicapi.tcp.util.TcpServer;
import org.ssssssss.magicapi.tcp.util.client.NettyClient;
import org.ssssssss.magicapi.tcp.util.server.NettyServer;
import org.ssssssss.script.annotation.Comment;
import org.ssssssss.script.functions.DynamicAttribute;

import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;

/**
 * tcp模块
 *
 * @author xuhaiyang
 */
@MagicModule("tcp")
public class TcpModule implements DynamicAttribute<TcpModule, TcpModule>{
	
	private MagicDynamicTcpClient magicDynamicTcpClient;
	private TcpServer tcpServer;
	private String serverVehId = null;
	private String id = null;

	public TcpModule(MagicDynamicTcpClient magicDynamicTcpClient) {
		this.magicDynamicTcpClient = magicDynamicTcpClient;
	}
	
	public TcpModule(TcpServer tcpServer,String id) {
		this.tcpServer = tcpServer;
		this.id = id;
	}
	
	/**
	 * 数据源切换
	 */
	@Override
	@Transient
	public TcpModule getDynamicAttribute(String key) {
		return magicDynamicTcpClient.getTcpModule(key);
	}
	@Comment("初始化连接")
	public void init(
			@Comment(name = "tcpHander", value = "回调函数 ;\n\n 如：(message, channel, body)->{...}") TcpHander tcpHander)
			throws Exception {
			if(this.serverVehId != null) {
				throw new Exception("当前客户端不能重复初始化，请先shutdown，再重新初始化！");
			}
			this.serverVehId = id;
			
			tcpServer.setTcpHander(new TcpHander() {
				@Override
				public void hander(SvrCst message, ChannelHandlerContext channel, String messageBody) {
					if(tcpServer instanceof NettyClient) {
						if(message == null){ // 连接成功后回调
					        addChannelCache(id ,channel); 
					    }
					    else{ //服务端响应后的处理
					        if(message.getType() == 0){// 异常信息打印
					            ErrorLog log = new ErrorLog(message.getMessageBody());
					            throw new RuntimeException("连接服务端错误: "+log.getLog());
					        }else{
					        	tcpHander.hander(message, channel, messageBody);
					        }
					    }
					}else {
						tcpHander.hander(message, channel, messageBody);
					}
				}
			});
			tcpServer.start();
	}
	
	@Comment("发送数据")
	public void sent(
			@Comment(name = "type", value = "发送的数据类型（用于判断业务类型）") int type,
			@Comment(name = "version", value = "发送的数据版本") int version,
			@Comment(name = "message", value = "发送的数据，字符串") String message,
			@Comment(name = "channel", value = "ChannelHandlerContext channel") ChannelHandlerContext channel) {

		PacketData pd = new PacketData();
		pd.setType(type);
		pd.setVersion(version);
		pd.setTime(System.currentTimeMillis());
		pd.setCommand(0);
		pd.setData(message.getBytes());
		channel.writeAndFlush(Unpooled.wrappedBuffer(pd.toBytes()));
	}
	@Comment("发送数据")
	public void sent(
			@Comment(name = "type", value = "发送的数据类型（用于判断业务类型）") int type,
			@Comment(name = "version", value = "发送的数据版本") int version,
			@Comment(name = "message", value = "发送的数据,字节数组") byte[] message,
			@Comment(name = "channel", value = "ChannelHandlerContext channel") ChannelHandlerContext channel) {
		
		PacketData pd = new PacketData();
		pd.setType(type);
		pd.setVersion(version);
		pd.setTime(System.currentTimeMillis());
		pd.setCommand(0);
		pd.setData(message);
		channel.writeAndFlush(Unpooled.wrappedBuffer(pd.toBytes()));
	}
	
	
	@Comment("获取TCP通道缓存，根据唯一编号")
	public ChannelHandlerContext getChannelCacheByVehId(@Comment(name = "vehId", value = "TCP唯一编号") String vehId) {
		return ChannelCache.getChannel(vehId);
	}
	
	@Comment("添加TCP通道缓存")
	public void addChannelCache(
			@Comment(name = "vehId", value = "TCP唯一编号") String vehId,
			@Comment(name = "channel", value = "ChannelHandlerContext channel") ChannelHandlerContext channel) {
		if(getChannelCacheByVehId(vehId) != null) {
			throw new RuntimeException("当前标识唯一编号已经存在！！！");
		}else
			ChannelCache.addVehChannel(vehId, channel, 0);
	}

	@Comment("获取全部TCP通道缓存")
    public ConcurrentHashMap<String, ChannelHandlerContext> getAllChannelCache() {
		ConcurrentHashMap<String, ChannelHandlerContext> map = new ConcurrentHashMap<String, ChannelHandlerContext>();
		ConcurrentHashMap<String, SubChannel> vehChannelMap = ChannelCache.getVehChannelMap();
		if(tcpServer instanceof NettyServer) {
			for (Map.Entry<String, SubChannel> entry : vehChannelMap.entrySet()) {
				String key = entry.getKey();
				if(key.startsWith(this.id+"__")) {
					SubChannel val = entry.getValue();
					map.put(key, val.getChannel());
				}
			}
		}else {
			ChannelHandlerContext channel = getChannelCacheByVehId(this.id);
			if(channel != null) {
				map.put(this.id, channel);
			}
		}
		 
        return map;
    }
	
	@Comment("优雅关闭TCP指定服务")
	public void shutdown() {
		this.serverVehId = null;
		tcpServer.shutdown();
	}
	@Comment("获取唯一编号")
	public String getId() {
		return this.id;
	}
	
	
	
}
