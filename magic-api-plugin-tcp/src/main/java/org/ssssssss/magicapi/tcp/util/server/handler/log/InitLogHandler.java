package org.ssssssss.magicapi.tcp.util.server.handler.log;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

public class InitLogHandler extends ChannelInboundHandlerAdapter {

    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println( "收到原始报文---->: "+ ByteBufUtil.hexDump((ByteBuf) msg));
        ctx.fireChannelRead(msg);
    }
}