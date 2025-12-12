package org.ssssssss.magicapi.tcp.util.client.common;

public class DataUtil {
    public static byte[] hex2Bytes(String hexstr) {
        byte[] b = new byte[hexstr.length() / 2];
        int j = 0;
        for (int i = 0; i < b.length; i++) {
            char c0 = hexstr.charAt(j++);
            char c1 = hexstr.charAt(j++);
            b[i] = ((byte) (parse(c0) << 4 | parse(c1)));
        }
        return b;
    }

    private static int parse(char c) {
        if (c >= 'a')
            return c - 'a' + 10 & 0xF;
        if (c >= 'A')
            return c - 'A' + 10 & 0xF;
        return c - '0' & 0xF;
    }

    public static String bytes2Hex(byte[] b) {
        StringBuilder s = new StringBuilder("");
        int len = b.length;
        for (int i = 0; i < len; i++) {
            s.append(getHex(b[i]));
        }
        return s.toString();
    }

    public static String getHex(short showData) {
        String str = Integer.toHexString(getUnsignedShort(showData));
        int len = str.length();
        return len < 2 ? '0' + str : str.substring(len - 2, len);
    }

    public static int getUnsignedShort(short data) {
        return data & 0xFFFF;
    }

    public static byte[] int2Byte(int res) {
        byte[] targets = new byte[4];
        targets[3] = ((byte) (res & 0xFF));
        targets[2] = ((byte) (res >> 8 & 0xFF));
        targets[1] = ((byte) (res >> 16 & 0xFF));
        targets[0] = ((byte) (res >>> 24 & 0xFF));
        return targets;
    }

    public static int byte2Int(byte[] b) {
        int s = 0;
        int s0 = b[0] & 0xFF;
        int s1 = b[1] & 0xFF;
        int s2 = b[2] & 0xFF;
        int s3 = b[3] & 0xFF;
        s0 <<= 24;
        s1 <<= 16;
        s2 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    public static int byte2Int(byte b0, byte b1, byte b2, byte b3) {
        int s = 0;
        int s0 = b0 & 0xFF;
        int s1 = b1 & 0xFF;
        int s2 = b2 & 0xFF;
        int s3 = b3 & 0xFF;
        s0 <<= 24;
        s1 <<= 16;
        s2 <<= 8;
        s = s0 | s1 | s2 | s3;
        return s;
    }

    public static byte[] short2Byte(short res) {
        byte[] targets = new byte[4];
        targets[1] = ((byte) (res & 0xFF));
        targets[0] = ((byte) (res >> 8 & 0xFF));
        return targets;
    }

    /**
     * 将short转成byte[2]
     *
     * @param a
     * @param b
     * @param offset b中的偏移量
     */
    public static void short2Byte(short a, byte[] b, int offset) {
        b[offset] = (byte) (a >> 8);
        b[offset + 1] = (byte) (a);
    }

    /**
     * 将byte[2]转换成short
     *
     * @param b
     * @return
     */
    public static short byte2Short(byte[] b) {
        return (short) (((b[0] & 0xff) << 8) | (b[1] & 0xff));
    }

    /**
     * 将byte[2]转换成short
     *
     * @param b
     * @return
     */
    public static short byte2Short(byte bh, byte bl) {
        return (short) (((bh & 0xff) << 8) | (bl & 0xff));
    }

    public static byte[] long2Bytes(long num) {
        byte[] byteNum = new byte[8];
        for (int ix = 0; ix < 8; ++ix) {
            int offset = 64 - (ix + 1) * 8;
            byteNum[ix] = (byte) ((num >> offset) & 0xff);
        }
        return byteNum;
    }

    public static long bytes2Long(byte[] byteNum) {
        long num = 0;
        for (int ix = 0; ix < 8; ++ix) {
            num <<= 8;
            num |= (byteNum[ix] & 0xff);
        }
        return num;
    }
}