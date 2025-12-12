package org.ssssssss.magicapi.tcp.util;

import io.netty.channel.ChannelHandlerContext;

@FunctionalInterface
public interface TcpHander {
	void hander(SvrCst message,ChannelHandlerContext channel,String messageBody);
}