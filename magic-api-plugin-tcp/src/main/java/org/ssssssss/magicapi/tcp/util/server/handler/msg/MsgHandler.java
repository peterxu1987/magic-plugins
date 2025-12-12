package org.ssssssss.magicapi.tcp.util.server.handler.msg;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
//import java.security.cert.Certificate;
import java.security.cert.X509CRL;
import java.security.cert.X509Certificate;
import java.util.Map;

//import javax.net.ssl.SSLEngine;
//import javax.net.ssl.SSLSession;

import org.ssssssss.magicapi.tcp.util.ChannelCache;
import org.ssssssss.magicapi.tcp.util.SvrCst;
import org.ssssssss.magicapi.tcp.util.TcpHander;
//import org.ssssssss.magicapi.tcp.util.server.common.CertificateRevoke;
import org.ssssssss.magicapi.tcp.util.server.common.SSLUtil;
import org.ssssssss.magicapi.tcp.util.server.entry.ErrorLog;

import com.alibaba.fastjson.JSONObject;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;
import io.netty.channel.ChannelInboundHandlerAdapter;
//import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.AttributeKey;
import io.netty.util.ReferenceCountUtil;

@Sharable
public class MsgHandler extends ChannelInboundHandlerAdapter {
	
	// 定义 AttributeKey
	private static final AttributeKey<X509Certificate> SSL_CERT_KEY = AttributeKey.valueOf("SSL_CERT_KEY");
	
	String id = "";
	public MsgHandler(String id) {
		this.id = id;
	}
	TcpHander tcpserverHander;
	Map<String,Object> attr;
	public void setHander(TcpHander tcpserverHander, Map<String,Object> attr) {
		this.tcpserverHander = tcpserverHander;
		this.attr = attr;
	}
	
	public byte[] bufToBytes(ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.getBytes(0, bytes);
		return bytes;
	}
	
	private boolean validate(ChannelHandlerContext ctx) throws Exception {
		// 获取客户端证书
	    X509Certificate clientCert = (X509Certificate) ctx.channel().attr(SSL_CERT_KEY).get();
	    // 检查证书是否被撤销
        if (clientCert != null) {
            // 加载 CRL
            X509CRL crl = SSLUtil.loadPem();
            if (SSLUtil.isCertificateRevoked(clientCert, crl)) {
            	ErrorLog errorLog = new ErrorLog();
				String message = "签发证书过期";
				errorLog.setType(0);
				errorLog.setTime(System.currentTimeMillis());
				errorLog.setVersion(0); // version:表示协议解析或者处理失败！
				errorLog.setCommand(0);
				errorLog.setData(message.getBytes(StandardCharsets.UTF_8));
				ctx.writeAndFlush(Unpooled.wrappedBuffer(errorLog.toBytes()));
                ctx.close(); // 拒绝连接
                return false;
            }
            return true;
        }
        return false;
	}

	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{
		
		Object sslPassword = attr.get("sslPassword");
		
		if(sslPassword != null) {
			if(!validate(ctx)) {
				return ;
			}
		}

		ByteBuf buf = (ByteBuf) msg;

		SvrCst svrCst = SvrCst.getInst();
		svrCst.setTraffic(buf.readableBytes());
		svrCst.setFlag(buf.readByte());
		//int dataLen = Unpooled.buffer().writeByte(0x0).writeBytes(buf.readSlice(3)).readInt();
		Unpooled.buffer().writeBytes(buf.readSlice(4)).readInt();
		svrCst.setType(buf.readByte());
		svrCst.setVersion(buf.readByte());
		svrCst.setTs(buf.readLong());
		svrCst.setCtl(buf.readByte());
		
		svrCst.setMessageBody(buf);
		try {
			ByteBuf bb = svrCst.getMessageBody();
			String bodyStr = new String(ByteBufUtil.getBytes(bb, bb.readerIndex(), buf.writerIndex() - bb.readerIndex(), true),"UTF-8");
			tcpserverHander.hander(svrCst,ctx,bodyStr);
		//	ctx.fireChannelRead(svrCst); // 它的作用是将消息传递给下一个 ChannelHandler 进行处理
		} catch (Exception e) {
			try{
				ErrorLog errorLog = new ErrorLog();
				String message = "协议包数据处理失败："+new String(bufToBytes(buf),"UTF-8");
				errorLog.setType(0);
				errorLog.setTime(System.currentTimeMillis());
				errorLog.setVersion(0); // version:表示协议解析或者处理失败！
				errorLog.setCommand(0);
				errorLog.setData(message.getBytes(StandardCharsets.UTF_8));
				ctx.writeAndFlush(Unpooled.wrappedBuffer(errorLog.toBytes()));
				System.err.println("服务端的数据解析失败,失败数据--->: "+JSONObject.toJSONString(errorLog));
			}catch (Exception le){
				System.err.println(le.getMessage());
			}
			e.printStackTrace();
		} finally {
			release(msg);
		}
	}

	private void release(Object msg) {
		try {
			ReferenceCountUtil.release(msg);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void channelActive(ChannelHandlerContext ctx) {
		
//		Object sslPassword = attr.get("sslPassword");
		
		InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientAddr = insocket.getAddress().getHostAddress()+":"+insocket.getPort();
		
		// 获取连接通道唯一标识
		ChannelId channelId = ctx.channel().id();
		try {
//			if(sslPassword != null) {
	            
//				ctx.pipeline().get(SslHandler.class).handshakeFuture().addListener((f) -> {
//					
//						SSLEngine engine = ctx.pipeline().get(SslHandler.class).engine();
//						SSLSession sslSession = engine.getSession();
//						// 获取客户端证书
//			            X509Certificate[] peerCertificates = (X509Certificate[]) sslSession.getPeerCertificates();
//			            if (peerCertificates != null && peerCertificates.length > 0) {
//			                // 将客户端证书存储到 Channel 属性中
//			                ctx.channel().attr(SSL_CERT_KEY).set(peerCertificates[0]);
//			            }
//					
//						if(!validate(ctx)) {
//							return ;
//						}
//						
//						Certificate[] certs = sslSession.getPeerCertificates();
//						String vehId = CertificateRevoke.getCommonName(certs[0]);
//						if(ChannelCache.getChannel(vehId) != null) {
//							ChannelCache.getChannel(vehId).channel().close();
//						}
//						ChannelCache.addVehChannel(vehId, ctx, 0);
//						// 如果map中不包含此连接，就保存连接
//						System.out.println("客户端【" + channelId + "】【" + vehId + "】【"+clientAddr+"】连接netty服务器!");
//						String serverAddr = InetAddress.getLocalHost().getHostAddress();
//						System.out.println("服务端【" + serverAddr + "】连接通道数量: " + ChannelCache.getVehChannelMap().mappingCount());
//				});
//			}else {
				ChannelCache.addVehChannel(id+"__"+channelId.toString(), ctx, 0);
				String serverAddr = InetAddress.getLocalHost().getHostAddress();
				System.out.println("客户端【" + channelId + "】【"+clientAddr+"】连接"+serverAddr+":"+attr.get("port").toString()+"服务器!");
				
				System.out.println("服务端【" + serverAddr + "】连接通道数量: " + ChannelCache.getVehChannelMapCount(id+"__"));
//			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
		
		InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientAddr = insocket.getAddress().getHostAddress()+":"+insocket.getPort();
		ChannelId channelId = ctx.channel().id();
		String vehId = ChannelCache.getChannelVehMap().get(ctx.channel().id().asLongText());
		String serverAddr = InetAddress.getLocalHost().getHostAddress();
		ChannelCache.removeChannel(ctx);
		try{
			if(vehId != null){
				System.err.println("客户端【"+channelId+"】【"+clientAddr+"】从服务端【" + serverAddr+":"+attr.get("port").toString() +"】断开连接！");
			}else {
				System.err.println("客户端【"+channelId+"】【"+clientAddr+"】从服务端【" + serverAddr +":"+attr.get("port").toString()+"】断开连接！");
			}
			
			System.err.println("服务端【" + serverAddr + "】连接通道数量: " + ChannelCache.getVehChannelMapCount(id+"__"));
		}catch (Exception e){
			System.err.println("记录车辆离线日志失败");
		}
        
    }
	
	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {

		String socketString = ctx.channel().remoteAddress().toString();

		if (evt instanceof IdleStateEvent) {
			IdleStateEvent event = (IdleStateEvent) evt;
			if (event.state() == IdleState.READER_IDLE) {
				System.err.println("Client: " + socketString + " READER_IDLE 读超时");
				ctx.disconnect();
			} else if (event.state() == IdleState.WRITER_IDLE) {
				System.err.println("Client: " + socketString + " WRITER_IDLE 写超时");
				ctx.disconnect();
			} else if (event.state() == IdleState.ALL_IDLE) {
				System.err.println("Client: " + socketString + " ALL_IDLE 总超时");
				ctx.disconnect();
			}
		}
	}
	
	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		String vehId = ChannelCache.getChannelVehMap().get(ctx.channel().id().asLongText());
		InetSocketAddress insocket = (InetSocketAddress) ctx.channel().remoteAddress();
		String clientAddr = insocket.getAddress().getHostAddress()+":"+insocket.getPort();
		ChannelId channelId = ctx.channel().id();
		if(vehId != null){
			System.err.println("客户端【"+channelId+"】【"+vehId+"】【"+clientAddr+"】发送数据在业务处理时发生了错误！");
		}else {
			System.err.println("客户端【"+channelId+"】【"+clientAddr+"】发送数据在业务处理时发生了错误！");
		}
		ctx.close();
		System.err.println("错误: "+cause.getMessage());
		cause.printStackTrace();
	}


}