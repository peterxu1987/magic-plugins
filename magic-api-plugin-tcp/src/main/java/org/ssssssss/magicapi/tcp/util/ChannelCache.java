package org.ssssssss.magicapi.tcp.util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelId;

/**
 * 管理所有的车端-channel链路
 */
public class ChannelCache {


    private static ConcurrentHashMap<String, AtomicInteger>  vehMsgSeq = new ConcurrentHashMap<>();

    // key:vehId, value:session
    private static ConcurrentHashMap<String, SubChannel> vehChannelMap = new ConcurrentHashMap<String, SubChannel>();

    public static ConcurrentHashMap<String, SubChannel> getVehChannelMap() {
        return vehChannelMap;
    }
    public static int getVehChannelMapCount(String preStr) {
    	int ct = 0;
    	for (Map.Entry<String, SubChannel> entry: vehChannelMap.entrySet()) {
    		String key = entry.getKey();
			if(key.startsWith(preStr)) {
				ct++;
			}
		}
    	return ct;
    }

    // keyh:channel.id.aslongtext, value:vehId
    private static ConcurrentHashMap<String, String> channelVehMap = new ConcurrentHashMap<String, String>();

    public static int getMsgSeq(String vehId){

        if(vehMsgSeq.get(vehId)==null){
            vehMsgSeq.put(vehId, new AtomicInteger(1));
            return 1;
        }else{
            return vehMsgSeq.get(vehId).incrementAndGet();
        }

    }

    public static void addVehChannel(String vehId, ChannelHandlerContext channel, int version) {

        if (!vehChannelMap.containsKey(vehId) 
                || (vehChannelMap.get(vehId) != null && !vehChannelMap.get(vehId).getChannel().channel().id().asLongText().equals(channel.channel().id().asLongText()))
         ) {
            try {
                if (vehChannelMap.containsKey(vehId) && vehChannelMap.get(vehId) != null
                        && !vehChannelMap.get(vehId).getChannel().channel().id().asLongText().equals(channel.channel().id().asLongText()) ) {
                    //同一个设备创建新channel时 需要判断旧的channel是否还存在，存在则删除
                    SubChannel oldChannel = vehChannelMap.remove(vehId);
                    if (oldChannel != null) {
                        channelVehMap.remove(oldChannel.getChannel().channel().id().asLongText());
                    }
                }
                vehChannelMap.put(vehId, new SubChannel(version,channel));
                channelVehMap.put(channel.channel().id().asLongText(), vehId);
            } catch (Exception e) {
            	System.err.println(e.getMessage());
            }
        }

    }

    public static void removeVehChannel(String vehId) {
        try {
            SubChannel channel = vehChannelMap.remove(vehId);
            if (channel != null) {
                channelVehMap.remove(channel.getChannel().channel().id().asLongText());
            }
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        }
    }

    public static ChannelHandlerContext getChannel(String vehId) {
        try {
            SubChannel channel = vehChannelMap.get(vehId);
            if (channel != null) {
                return channel.channel;
            }
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        }
        return null;
    }

    public static void removeChannel(ChannelHandlerContext channel) {
        try {
            ChannelId id = channel.channel().id();
            String vehId = channelVehMap.remove(channel.channel().id().asLongText());
            if(vehId != null && vehId.trim().length()>0 && vehChannelMap.containsKey(vehId)){
                SubChannel currChannel = vehChannelMap.get(vehId);
                //断开channel时，只有hashmap中的channel和当前待断开的channelid一致，才从hashmap中移除，
                //防止设备断开重连后才端来老链路
                if(currChannel!=null && currChannel.getChannel().channel().id().asLongText().equals(id.asLongText())){
                    vehChannelMap.remove(vehId);
                }
            }
        } catch (Exception e) {
        	System.err.println(e.getMessage());
        }
    }

    public static ConcurrentHashMap<String, String> getChannelVehMap() {
        return channelVehMap;
    }

    public static class SubChannel{
        private int version;
        private ChannelHandlerContext channel;
		public SubChannel(int version, ChannelHandlerContext channel) {
			this.version = version;
			this.channel = channel;
		}
		public int getVersion() {
			return version;
		}
		public void setVersion(int version) {
			this.version = version;
		}
		public ChannelHandlerContext getChannel() {
			return channel;
		}
		public void setChannel(ChannelHandlerContext channel) {
			this.channel = channel;
		}
        
    }

}
