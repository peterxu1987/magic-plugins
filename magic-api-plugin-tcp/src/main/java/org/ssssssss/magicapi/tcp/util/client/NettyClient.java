package org.ssssssss.magicapi.tcp.util.client;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLEngine;

import org.ssssssss.magicapi.tcp.util.ChannelCache;
import org.ssssssss.magicapi.tcp.util.SvrCst;
import org.ssssssss.magicapi.tcp.util.TcpHander;
import org.ssssssss.magicapi.tcp.util.TcpServer;
import org.ssssssss.magicapi.tcp.util.client.common.ByteHelper;
import org.ssssssss.magicapi.tcp.util.client.common.ErrorLog;
import org.ssssssss.magicapi.tcp.util.client.handler.MsgDecoder;

import com.alibaba.fastjson.JSONObject;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler.Sharable;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ReferenceCountUtil;

public class NettyClient implements TcpServer{
    private final String host;
    private final String id;
    private final int port;
    private Bootstrap bootstrap;
    private EventLoopGroup group;
    private TcpHander tcpcHander;
    Map<String,Object> attr;
    
    public void setTcpHander(TcpHander tcpcHander) {
    	this.tcpcHander = tcpcHander;
    }
    public NettyClient(String id,String host, int port,  Map<String,Object> attr) {
    	this.id = id;
        this.host = host;
        this.port = port;
        this.attr = attr;
    }

    public void start() {
        try {
            this.group = new NioEventLoopGroup();
            this.bootstrap = new Bootstrap();
        	bootstrap.group(group);
            /**
             * ServerSocketChannel是以NIO的selector为基础进行实现的，用来接收新的连接，这里告诉Channel通过NioServerSocketChannel获取新的连接
             */
        	bootstrap = bootstrap.channel(NioSocketChannel.class);
 
            /**
             * 立即发送数据，默认值为Ture（Netty默认为True而操作系统默认为False）。
             * 该值设置Nagle算法的启用，改算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量，如果需要发送一些较小的报文，则需要禁用该算法。
             * Netty默认禁用该算法，从而最小化报文传输延时。
             */
        	bootstrap = bootstrap.option(ChannelOption.TCP_NODELAY, true);
            /**
             * 设置 I/O处理类,主要用于网络I/O事件，记录日志，编码、解码消息
             */
        	bootstrap = bootstrap.handler(new ChildLengthFieldChannelHandler(id,host, port, tcpcHander, attr,this));
            // 开始连接
            connect();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void connect() {
        ChannelFuture future = bootstrap.connect(host, port);

        future.addListener((ChannelFutureListener) channelFuture -> {
            if (channelFuture.isSuccess()) {
                System.out.println("客户端成功连接: " + host + ":" + port);
            } else {
                if(attr.get("reconnect") != null && attr.get("reconnect").toString().equals("true")) {
                    System.err.println("连接失败，5 秒后尝试重连...");
                    channelFuture.channel().eventLoop().schedule(() -> {
                        System.err.println("尝试重新连接...");
                        connect(); // 直接重连，不创建新实例
                    }, 5, TimeUnit.SECONDS);
                }
            }
        });
    }

    // 优雅关闭客户端
    public void shutdown() {
    	if(group != null) {
    		 group.shutdownGracefully();
    		 group = null;
    	}
    	
    	bootstrap = null;
    }
	public String getId() {
		return id;
	}
}

@Sharable
class NettyClientHandler extends ChannelInboundHandlerAdapter {
	TcpHander tcpcHander;
	String host;
	String id;
	int port;
	NettyClient clientObj;
	Map<String, Object> attr;
	public NettyClientHandler(String id,String host, int port, TcpHander tcpcHander, Map<String, Object> attr, NettyClient clientObj) {
		this.id = id;
		this.tcpcHander = tcpcHander;
		this.port = port;
		this.host = host;
		this.attr = attr;
		this.clientObj = clientObj;
	}

	@Override
	public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception{

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
			this.tcpcHander.hander(svrCst, ctx, bodyStr);
		} catch (Exception e) {
			try{
				ErrorLog errorLog = new ErrorLog();
				errorLog.setType(ByteHelper.byteToHex(svrCst.getType()).replaceFirst("0x",""));
				errorLog.setTimestamp(System.currentTimeMillis());
				errorLog.setError_type(0);
				errorLog.setVehId("unknownvehicle");
				errorLog.setHex(ByteBufUtil.hexDump(buf));
				System.err.println("客户端的数据解析失败,失败数据--->: "+JSONObject.toJSONString(errorLog));
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
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
    	this.tcpcHander.hander(null, ctx,null);
    }
    
    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        ChannelCache.removeChannel(ctx);
        
        if(attr.get("reconnect") != null && attr.get("reconnect").toString().equals("true")) {
            // 不再创建新客户端，而是使用现有的bootstrap重连
            ctx.channel().eventLoop().schedule(() -> {
                System.err.println("连接断开后尝试重新连接...");
                clientObj.connect(); // 使用现有客户端对象重连
            }, 5, TimeUnit.SECONDS);
        }
        super.channelInactive(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close(); // 关闭连接
    }
}

class ChildLengthFieldChannelHandler extends ChannelInitializer<SocketChannel>{
	    private NettyClientHandler nettyClientHandler;
	    private Map<String, Object> attr;
	  	public ChildLengthFieldChannelHandler(String id ,String host, int port, TcpHander tcpcHander, Map<String, Object> attr, NettyClient clientObj) {
	  		nettyClientHandler = new NettyClientHandler(id, host, port, tcpcHander,attr, clientObj);
	  		this.attr = attr;
	  	}

		@Override
		protected void initChannel(SocketChannel ch) throws Exception {
			
			Object sslPassword = attr.get("sslPassword");
			if (sslPassword != null) {
				String basePath =   attr.get("certBasePath").toString();
				String caCrtFile = basePath+"/ca.crt";
				String crtFile = basePath+"/"+attr.get("vehId").toString()+".crt";
				String keyFile = basePath+"/"+attr.get("vehId").toString()+"_pkcs8.key";
				SslContext sslContext = SslContextBuilder.forClient().keyManager(new File(crtFile), new File(keyFile), sslPassword.toString())
						.trustManager(new File(caCrtFile)).build();
				SSLEngine sslEngine = sslContext.newEngine(ch.alloc());
				sslEngine.setUseClientMode(true);
				ch.pipeline().addLast("sslHandler",new SslHandler(sslEngine));
			}
			ch.pipeline().addLast("decoder", new MsgDecoder());
			ch.pipeline().addLast("nettyClientHandler", nettyClientHandler);

		}
}
