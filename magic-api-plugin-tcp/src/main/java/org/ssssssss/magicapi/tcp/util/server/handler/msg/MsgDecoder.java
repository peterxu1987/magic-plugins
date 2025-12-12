package org.ssssssss.magicapi.tcp.util.server.handler.msg;

import java.util.List;

import org.ssssssss.magicapi.tcp.util.SvrCst;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

public class MsgDecoder extends ByteToMessageDecoder {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf in, List<Object> list) throws Exception {
//        log.info( TsariByteBufUtil.buf2HexStr((ByteBuf) in));

        if (in == null || in.readableBytes() <= SvrCst.MIN_MSG_LEN)  //最坏打算，至少16个字节时才能读到数据体长度  1 3 1 1 8 1
            return;
        in.markReaderIndex();
        byte flag = in.readByte();
        if(flag == (byte)0xf2){

//            int dataLen = Unpooled.buffer().writeByte(0x0).writeBytes(in.readSlice(3)).readInt();
            int dataLen = Unpooled.buffer().writeBytes(in.readSlice(4)).readInt();

            if(in.readableBytes()>=(dataLen+11)){
                in.resetReaderIndex();
                list.add(in.readSlice(16+dataLen).retain());
            }else{
                in.resetReaderIndex();
                return;
            }
        }else{
            //数据错误，主动断开
        	System.err.println("出现不正常数据, 平台主动断开---->: "+ ByteBufUtil.hexDump(in));
            while (in.readableBytes()>0){
                in.readSlice(in.readableBytes());
            }
            channelHandlerContext.channel().close();
        }

    }

}
