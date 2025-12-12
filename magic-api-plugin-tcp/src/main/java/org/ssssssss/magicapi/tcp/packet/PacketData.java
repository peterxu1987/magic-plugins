package org.ssssssss.magicapi.tcp.packet;

import java.io.Serializable;
import java.util.Arrays;

import org.ssssssss.magicapi.tcp.util.client.common.DataUtil;

public class PacketData implements Serializable {
 
	private static final long serialVersionUID = 1L;

    private byte[] data;

    private int type;
    private int version;
    private long time;
    private int command;

    public PacketData() {
    }

    public int getType() {
        return type;
    }

    public void setType(int type) {
        this.type = type;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public int getCommand() {
        return command;
    }

    public void setCommand(int command) {
        this.command = command;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public byte[] toBytes() {
    	
    	int length = data.length;
    	// 缓冲区大小调整为 16 + length（原15改为16）
        byte[] buff = new byte[16+length];
        int cnt = 0;
        // 从0xF3 改成 0xF2
        buff[cnt++] = (byte) 0xF2;
        // 写入4字节长度（原3字节改为4字节）
        buff[cnt++] = (byte) ((length >> 24) & 0xFF); // 新增最高位字节
        buff[cnt++] = (byte) ((length >> 16) & 0xFF);
        buff[cnt++] = (byte) ((length >> 8) & 0xFF);
        buff[cnt++] = (byte) (length & 0xFF);
        buff[cnt++] = (byte) type;
        buff[cnt++] = (byte) version;

        System.arraycopy(DataUtil.long2Bytes(time), 0, buff, cnt, 8);
        cnt += 8;

        buff[cnt++] = (byte) command;
        // data起始位置后移1字节（原索引15->16）
        System.arraycopy(data, 0, buff, 16, length);
        
        return buff;
    }

    @Override
    public String toString() {
        return "PacketHeader{" +
                "data=" + Arrays.toString(data) +
                ", type=" + type +
                ", version=" + version +
                ", time=" + time +
                ", command=" + command +
                '}';
    }
}
