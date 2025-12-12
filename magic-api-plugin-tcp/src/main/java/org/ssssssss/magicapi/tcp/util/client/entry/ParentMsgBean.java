package org.ssssssss.magicapi.tcp.util.client.entry;

import com.alibaba.fastjson.JSONObject;
import io.netty.buffer.ByteBuf;

public abstract class ParentMsgBean {

	public String toJsonString(){
		return JSONObject.toJSONString(this);
	}

	public String toString() {
		return JSONObject.toJSONString(this);
	}

	public byte[] toByteArray(){
		return null;
	}

	public ByteBuf toByteBuf(){
		return null;
	}

	public byte[] bufToBytes(ByteBuf buf) {

		byte[] bytes = new byte[buf.readableBytes()];
		buf.getBytes(0, bytes);
		return bytes;
	}
}
