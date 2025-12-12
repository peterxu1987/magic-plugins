package org.ssssssss.magicapi.tcp.util.server.common;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import java.util.Arrays;

public class BytesWrapper {
    final BytesWrapper self = this;
    
    
    /* Constructors */
    public BytesWrapper(byte[] bytes) {
        if (bytes == null)  {
            throw new NullPointerException();
        }
        this.bytes = bytes;
    }
    
    /* Public Methods */
    public boolean equalsBytes(byte[] bytes) {
        return Arrays.equals(getBytes(), bytes);
    }
    
    /* Properties */
    private final byte[] bytes;
    public byte[] getBytes() {
        return this.bytes;
    }
    
    /* Overrides */
    @Override
    public boolean equals(Object other)
    {
        if (!(other instanceof BytesWrapper)) {
            return false;
        }
        return equalsBytes(((BytesWrapper)other).getBytes());
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(getBytes());
    }
    
    /* Delegates */
    
    
    /* Private Methods */
    public static byte[] bufToBytes(ByteBuf buf){

        byte[] bytes = new byte[buf.readableBytes()];
        buf.getBytes(0, bytes);
        return bytes;
    }

    /**
     * DirectByteBuf，可以减少Socket读写的内存拷贝，即著名的 ”零拷贝”。
     　　 * 由于是直接内存，因此无法直接转换成堆内存，因此它并不支持array()方法
     */
    public static ByteBuf bytesToBuf(byte[] bytes){
        return Unpooled.wrappedBuffer(bytes);
    }

    public static byte[] bytesMatchLenWith0(byte[] bytes, int len){
        if(bytes == null){
            return new byte[len];
        }

        int thisSize = bytes.length;

        if(thisSize==len){
            return bytes;
        }else{
            byte[] re = new byte[len];
            System.arraycopy(bytes, 0 ,re, 0, Math.min(thisSize,len));
            return re;
        }

    }
}