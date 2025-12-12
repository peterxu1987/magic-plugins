package org.ssssssss.magicapi.tcp.packet;


import java.nio.charset.StandardCharsets;

import io.netty.buffer.ByteBuf;


public class ErrorLog{
 
    private String log;
    
    public byte[] bufToBytes(ByteBuf buf) {
		byte[] bytes = new byte[buf.readableBytes()];
		buf.readBytes(bytes);
		return bytes;
	}
    
    public ErrorLog(ByteBuf messageBody) {
		this.log = new String(bufToBytes(messageBody),StandardCharsets.UTF_8);
	}

	public String getLog() {
		return log;
	}

 
}

