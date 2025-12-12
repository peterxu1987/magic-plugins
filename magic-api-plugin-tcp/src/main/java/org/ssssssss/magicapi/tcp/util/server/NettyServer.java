package org.ssssssss.magicapi.tcp.util.server;

import java.io.File;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import javax.net.ssl.SSLEngine;

import org.ssssssss.magicapi.tcp.util.TcpHander;
import org.ssssssss.magicapi.tcp.util.TcpServer;
import org.ssssssss.magicapi.tcp.util.client.handler.MsgDecoder;
import org.ssssssss.magicapi.tcp.util.server.handler.log.DecoderLogHandler;
import org.ssssssss.magicapi.tcp.util.server.handler.log.InitLogHandler;
import org.ssssssss.magicapi.tcp.util.server.handler.msg.MsgHandler;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.ssl.ClientAuth;
import io.netty.handler.ssl.SslContext;
import io.netty.handler.ssl.SslContextBuilder;
import io.netty.handler.ssl.SslHandler;
import io.netty.util.ResourceLeakDetector;

public class NettyServer implements TcpServer{
    private final int port;
    private final String id;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private TcpHander tcpcHander;
    private ChildLengthFieldChannelHandler childLengthFieldChannelHandler;
    Map<String,Object> attr;

	    public void setTcpHander(TcpHander tcpcHander) {
	    	this.tcpcHander = tcpcHander;
	    }
    	public NettyServer(String id,int port,  Map<String,Object> attr) {
        this.id = id;
        this.port = port;
        MsgHandler msgHandler = new MsgHandler(id);
        this.childLengthFieldChannelHandler = new ChildLengthFieldChannelHandler(msgHandler);
        
        this.attr = attr;
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.PARANOID);
		
        /**
         * 	配置服务端的NIO线程组
         * 	NioEventLoopGroup 是用来处理I/O操作的Reactor线程组
         * 	bossGroup：用来接收进来的连接，workerGroup：用来处理已经被接收的连接,进行socketChannel的网络读写，
         * 	bossGroup接收到连接后就会把连接信息注册到workerGroup
         * 	workerGroup的EventLoopGroup默认的线程数是CPU核数的二倍，int nThreads = Runtime.getRuntime().availableProcessors() * 2; // 或者根据具体情况调整
         */
        this.bossGroup = new NioEventLoopGroup(1);
        this.workerGroup = new NioEventLoopGroup();
        
    }

    public void start() {
    	 CompletableFuture<String> startFuture = new CompletableFuture<>();
    	// 如果EventLoopGroup已关闭，则重新创建
	    if (bossGroup == null || bossGroup.isShutdown()) {
	        bossGroup = new NioEventLoopGroup(1);
	    }
	    if (workerGroup == null || workerGroup.isShutdown()) {
	        workerGroup = new NioEventLoopGroup();
	    }
    	childLengthFieldChannelHandler.setHandler(tcpcHander,attr);
	    try {
	    	/**
        	 * ServerBootstrap 是一个启动NIO服务的辅助启动类
        	 */
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            /**
             * 	设置group，将bossGroup， workerGroup线程组传递到ServerBootstrap
             */
            serverBootstrap = serverBootstrap.group(bossGroup, workerGroup);
            /**
             * ServerSocketChannel是以NIO的selector为基础进行实现的，用来接收新的连接，这里告诉Channel通过NioServerSocketChannel获取新的连接
             */
            serverBootstrap = serverBootstrap.channel(NioServerSocketChannel.class);
 
            /**
             * 服务端接受连接的队列长度，如果队列已满，客户端连接将被拒绝(队列被接收后，拒绝的客户端下次连接上来只要队列有空余就能连上)
             * 如果未设置或所设置的值小于1，Java将使用默认值50。
             */
            serverBootstrap = serverBootstrap.option(ChannelOption.SO_BACKLOG, 10024);
            /**
             * 立即发送数据，默认值为Ture（Netty默认为True而操作系统默认为False）。
             * 该值设置Nagle算法的启用，改算法将小的碎片数据连接成更大的报文来最小化所发送的报文的数量，如果需要发送一些较小的报文，则需要禁用该算法。
             * Netty默认禁用该算法，从而最小化报文传输延时。
             */
            serverBootstrap = serverBootstrap.childOption(ChannelOption.TCP_NODELAY, true);
            /**
             * 连接保活，默认值为False。启用该功能时，TCP会主动探测空闲连接的有效性。
             * 可以将此功能视为TCP的心跳机制，默认的心跳间隔是7200s即2小时, Netty默认关闭该功能。
             */
            serverBootstrap = serverBootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);
            /**
             * 设置 I/O处理类,主要用于网络I/O事件，记录日志，编码、解码消息
             */
            serverBootstrap = serverBootstrap.childHandler(childLengthFieldChannelHandler);

            // 异步绑定端口
            ChannelFuture bindFuture = serverBootstrap.bind(port);
            
            // 添加监听器处理绑定结果
            bindFuture.addListener(future -> {
                if (!future.isSuccess()) {
                    startFuture.completeExceptionally(future.cause());
                    shutdown();
                    return;
                }
                
                // 成功启动
                startFuture.complete("Server started successfully on port " + port);
                
                // 添加关闭钩子
                bindFuture.channel().closeFuture().addListener(closeFuture -> {
                    shutdown();
                });
            });
           
	    }catch (Exception e) {
            startFuture.completeExceptionally(e);
            shutdown(); // 发生异常，释放资源
        }
	     
    }

    // 优雅关闭客户端
 // 优雅关闭客户端
    public void shutdown() {
            if (bossGroup != null) {
                bossGroup.shutdownGracefully();
                bossGroup = null;  // 设置为null，便于后续重新创建
            }
            if (workerGroup != null) {
                workerGroup.shutdownGracefully();
                workerGroup = null;  // 设置为null，便于后续重新创建
            }
        
    }
    
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

	@Override
	public String getId() {
		return id;
	}
}