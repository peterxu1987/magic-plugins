package org.ssssssss.magicapi.tcp.util.server.entry;


import java.io.Serializable;
import java.util.Arrays;

import org.ssssssss.magicapi.tcp.util.server.common.DataUtil;


public class ErrorLog implements Serializable {
 
	private static final long serialVersionUID = 1L;

    private byte[] data;

    private int type;
    private int version;
    private long time;
    private int command;

    public ErrorLog() {
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
        byte[] buff = new byte[16+length];
        int cnt = 0;
        buff[cnt++] = (byte) 0xF2;
        buff[cnt++] = (byte) ((length >> 24) & 0xFF);
        buff[cnt++] = (byte) ((length >> 16) & 0xFF);
        buff[cnt++] = (byte) ((length >> 8) & 0xFF);
        buff[cnt++] = (byte) (length & 0xFF);
        buff[cnt++] = (byte) type;
        buff[cnt++] = (byte) version;

        System.arraycopy(DataUtil.long2Bytes(time), 0, buff, cnt, 8);
        cnt += 8;

        buff[cnt++] = (byte) command;
        
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

