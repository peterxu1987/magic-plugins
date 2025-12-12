package org.ssssssss.magicapi.tcp.packet;

import io.netty.buffer.ByteBuf;

public abstract class PacketModule {
    public abstract byte[] toBytes();
    public abstract void copy(ByteBuf messageBody);
    
    public byte[] bufToBytes(ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		return bytes;
	}
}
