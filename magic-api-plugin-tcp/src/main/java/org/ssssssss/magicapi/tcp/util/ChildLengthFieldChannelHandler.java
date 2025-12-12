package org.ssssssss.magicapi.tcp.util;

import java.io.File;
import java.util.Map;

import javax.net.ssl.SSLEngine;

import org.ssssssss.magicapi.tcp.util.client.handler.MsgDecoder;
import org.ssssssss.magicapi.tcp.util.server.handler.log.DecoderLogHandler;
import org.ssssssss.magicapi.tcp.util.server.handler.log.InitLogHandler;
import org.ssssssss.magicapi.tcp.util.server.handler.msg.MsgHandler;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;

class ChildLengthFieldChannelHandler  extends ChannelInitializer<SocketChannel> {
	private MsgHandler msgHandler;
	private Map<String, Object> attr;

	public ChildLengthFieldChannelHandler(MsgHandler msgHandler) {
		this.msgHandler = msgHandler;
	}

	public void setHandler(TcpHander tcpserverHander, Map<String, Object> attr) {
		msgHandler.setHander(tcpserverHander, attr);
		this.attr = attr;
	}

	@Override
	protected void initChannel(SocketChannel ch) throws Exception {
		
		Object sslPassword = attr.get("sslPassword");
		Object logs = attr.get("logs");
		if (sslPassword != null) {
			String basePath = attr.get("certBasePath").toString();
			String caCrtFile = basePath+"/ca.crt";
			String crtFile = basePath+"/server.crt";
			String keyFile = basePath+"/server_pkcs8.key";
			SslContext sslContext = SslContextBuilder
					.forServer(new File(crtFile), new File(keyFile), sslPassword.toString()).clientAuth(ClientAuth.REQUIRE)
					.trustManager(new File(caCrtFile)).build();

			SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
			ch.pipeline().addLast("sslHandler", new SslHandler(sslEngine));
		 
		}

		//ch.pipeline().addLast(new IdleStateHandler(60, 60, 0, TimeUnit.SECONDS));
		// 带编码
		if (logs != null) {
			ch.pipeline().addLast("initialLog", new InitLogHandler());
		}

		ch.pipeline().addLast("decoder", new MsgDecoder());

		if (logs != null) {
			ch.pipeline().addLast("decodedLog", new DecoderLogHandler());
		}
		ch.pipeline().addLast("msgHandler", msgHandler);

	}
}
