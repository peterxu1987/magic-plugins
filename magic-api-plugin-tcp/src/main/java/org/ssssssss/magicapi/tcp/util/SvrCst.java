package org.ssssssss.magicapi.tcp.util;

import org.ssssssss.magicapi.tcp.util.server.common.ByteHelper;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

public final class SvrCst {
	// 最小字节长度
	public static final int MIN_MSG_LEN = 16;
	private byte flag;// 标识位置
	private byte[] dataLength = new byte[4];
	private byte type;
	private byte version;
	private long ts;// 时间戳
	private byte ctl;// 控制内容
	private ByteBuf messageBody;
	private Boolean isSuccess = Boolean.FALSE;
	private long cloudTimestamp = System.currentTimeMillis();
	private String devId;
	private long traffic; //数据流量

	public long getCloudTimestamp() {
		return cloudTimestamp;
	}

	public void setCloudTimestamp(long cloudTimestamp) {
		this.cloudTimestamp = cloudTimestamp;
	}

	public static SvrCst getInst() {
		return new SvrCst();
	}

	public SvrCst(){

	}

	public SvrCst(byte flag, byte type, byte version, byte ctl){
		this.flag = flag;
		this.type = type;
		this.version = version;
		this.ctl = ctl;
	}

	public SvrCst(byte flag, byte type, byte version, byte ctl, ByteBuf messageBody){
		this.flag = flag;
		this.type = type;
		this.version = version;
		this.ctl = ctl;
		this.messageBody = messageBody;
	}

//	public void buildAndSendMessage(ChannelHandlerContext ctx) {
////		this.dataLength = ByteHelper.Int2Bytes(body.readableBytes);
//		buildData(ctx, messageBody);
//	}

	public ByteBuf buildAndSendMessage(ByteBuf body) {
//		this.dataLength = ByteHelper.Int2Bytes(body.readableBytes);
		return buildData(body);
	}

	public ByteBuf buildAndSendMessage(byte[] body) {
//		this.dataLength = ByteHelper.Int2Bytes(body.readableBytes);
		return buildAndSendMessage(Unpooled.wrappedBuffer(body));
	}

	private ByteBuf buildData(ByteBuf data) {
//		ByteBuf body = getNewByteBuf(ctx, 15 + data.readableBytes());
		ByteBuf body = Unpooled.buffer();
		body.writeByte(flag);
		body.writeBytes(ByteHelper.Int2Bytes(data.readableBytes()));
		body.writeByte(type);
		body.writeByte(version);
		body.writeLong(System.currentTimeMillis());
		body.writeByte(ctl);
		body.writeBytes(data);
		// 将客户端的信息直接返回写入ctx
		//System.out.println(ByteBufUtil.hexDump(body));

		return body;
	}

	public SvrCst readFromBytebuf(ByteBuf in) {
		if (in.readableBytes() <= SvrCst.MIN_MSG_LEN) {// 不够包头
			this.isSuccess = false;
			return this;
		}
		while (in.readableBytes() > SvrCst.MIN_MSG_LEN) {
			in.markReaderIndex();
			try {
				flag = in.readByte();
				in.readBytes(dataLength);
				int dataLen = ByteHelper.Bytes2Int(dataLength);  //4个字节， 数据段长度
				type = in.readByte();
				version = in.readByte();
				ts = in.readLong();
//				System.err.println(ts );
				ctl = in.readByte();

				if(in.readableBytes()>=dataLen){
					messageBody = in.readSlice(dataLen);
					isSuccess = Boolean.TRUE;
				}else{
					in.resetReaderIndex();
					isSuccess = Boolean.FALSE;
					return this;
				}
			} catch (Exception e) {
				e.printStackTrace();
				in.resetReaderIndex();
//				in.readerIndex(in.writerIndex());
				return this;
			} finally {

			}
		}
		return this;
	}



	public byte getFlag() {
		return flag;
	}

	public void setFlag(byte flag) {
		this.flag = flag;
	}

	public byte[] getDataLength() {
		return dataLength;
	}

	public void setDataLength(byte[] dataLength) {
		this.dataLength = dataLength;
	}

	public byte getType() {
		return type;
	}

	public void setType(byte type) {
		this.type = type;
	}

	public byte getVersion() {
		return version;
	}
	public int getIntVersion() {
		return (version&0x0FF);
	}

	public void setVersion(byte version) {
		this.version = version;
	}

	public long getTs() {
		return ts;
	}

	public void setTs(long ts) {
		this.ts = ts;
	}

	public byte getCtl() {
		return ctl;
	}

	public void setCtl(byte ctl) {
		this.ctl = ctl;
	}

	public ByteBuf getMessageBody() {
		return messageBody;
	}

	public void setMessageBody(ByteBuf messageBody) {
		this.messageBody = messageBody;
	}

	public Boolean getIsSuccess() {
		return isSuccess;
	}

	public void setIsSuccess(Boolean isSuccess) {
		this.isSuccess = isSuccess;
	}

	public long getTraffic() {
		return traffic;
	}

	public void setTraffic(long traffic) {
		this.traffic = traffic;
	}

	public String getDevId() {
		return devId;
	}

	public void setDevId(String devId) {
		this.devId = devId;
	}

	/**
	 * 创建一个新的 ByteBuf （Direct ByteBuf)
	 *
	 * @param ctx
	 * @param initialCapacity
	 * @return
	 */

//	public ByteBuf getNewByteBuf(ChannelHandlerContext ctx, int initialCapacity) {
//		return getNewDirectByteBuf(ctx, initialCapacity);
//	}
//
//	/**
//	 * 创建一个新的 ByteBuf
//	 *
//	 * @param ctx
//	 * @param initialCapacity
//	 * @return
//	 */
//	public ByteBuf getNewDirectByteBuf(ChannelHandlerContext ctx, int initialCapacity) {
//		return ctx.channel().alloc().directBuffer(initialCapacity);
//	}
}
