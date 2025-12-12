package org.ssssssss.magicapi.tcp.util.client.common;

public class ErrorLog {

    private String vehId;
    private long timestamp;
    private String type;  //文档中消息头里边的消息类型hex
    private int error_type; //0收到车端的数据解析失败 1车端数据推送到kafka失败(没有做)  2从云端收到的数据组装成bytebuf失败 3数据下发给车端重试3次依然失败
    private String hex;  //最终组装完成的hex或者收到的hex
    private String body; //消息体json格式内容
    
	public ErrorLog() {
		super();
	}
	public ErrorLog(String vehId, long timestamp, String type, int error_type, String hex, String body) {
		super();
		this.vehId = vehId;
		this.timestamp = timestamp;
		this.type = type;
		this.error_type = error_type;
		this.hex = hex;
		this.body = body;
	}
	public String getVehId() {
		return vehId;
	}
	public void setVehId(String vehId) {
		this.vehId = vehId;
	}
	public long getTimestamp() {
		return timestamp;
	}
	public void setTimestamp(long timestamp) {
		this.timestamp = timestamp;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public int getError_type() {
		return error_type;
	}
	public void setError_type(int error_type) {
		this.error_type = error_type;
	}
	public String getHex() {
		return hex;
	}
	public void setHex(String hex) {
		this.hex = hex;
	}
	public String getBody() {
		return body;
	}
	public void setBody(String body) {
		this.body = body;
	}
    

}
