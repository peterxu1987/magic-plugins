package org.ssssssss.magicapi.tcp.util.server.common;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

public class ByteHelper {
  private static final char[] HEXES = new char[] { 
      '0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 
      'a', 'b', 'c', 'd', 'e', 
      'f' };
  
  public static String[] bytes2Hex(byte[] bytes) {
    if (bytes == null || bytes.length == 0)
      return null; 
    String[] tmp = new String[bytes.length];
    int i = 0;
    byte b;
    int j;
    byte[] arrayOfByte;
    for (j = (arrayOfByte = bytes).length, b = 0; b < j; ) {
      byte b1 = arrayOfByte[b];
      tmp[i++] = String.format("0x%s%s", new Object[] { Character.valueOf(HEXES[b1 >> 4 & 0xF]), Character.valueOf(HEXES[b1 & 0xF]) });
      b++;
    } 
    return tmp;
  }
  
  public static byte[] hexStringToByte(String hex) {
    byte[] b = new byte[hex.length() / 2];
    int j = 0;
    for (int i = 0; i < b.length; i++) {
      char c0 = hex.charAt(j++);
      char c1 = hex.charAt(j++);
      b[i] = (byte)(parse(c0) << 4 | parse(c1));
    } 
    return b;
  }
  
  public static int hexStrToByte(String hex) {
    char c0 = hex.charAt(hex.length() - 2);
    char c1 = hex.charAt(hex.length() - 1);
    return (byte)(parse(c0) << 4 | parse(c1)) & 0xFF;
  }
  
  private static int parse(char c) {
    if (c >= 'a')
      return c - 97 + 10 & 0xF; 
    if (c >= 'A')
      return c - 65 + 10 & 0xF; 
    return c - 48 & 0xF;
  }
  
  public static String byteToHex(byte b) {
    String format = String.format("0x%s%s", new Object[] { Character.valueOf(HEXES[b >> 4 & 0xF]), Character.valueOf(HEXES[b & 0xF]) });
    return format;
  }
  
  public static List<String> getListStringFromBytes(byte[] bytes) {
    return Arrays.asList(bytes2Hex(bytes));
  }
  
  public static List<String> getListStringBytebuf(ByteBuf buf) {
    ByteBuf duplicate = buf.duplicate();
    List<String> byteList = new LinkedList<>();
    while (duplicate.isReadable()) {
      byte readByte = duplicate.readByte();
      byteList.add(byteToHex(readByte));
    } 
    return byteList;
  }
  
  public static String strTo16(String s) {
    String str = "";
    for (int i = 0; i < s.length(); i++) {
      int ch = s.charAt(i);
      String s4 = Integer.toHexString(ch);
      str = String.valueOf(str) + s4;
    } 
    return str;
  }
  
  public static String hexStringToString(String s) {
    if (s == null || s.equals(""))
      return null; 
    s = s.replace(" ", "");
    byte[] baKeyword = new byte[s.length() / 2];
    for (int i = 0; i < baKeyword.length; i++) {
      try {
        baKeyword[i] = (byte)(0xFF & Integer.parseInt(s.substring(i * 2, i * 2 + 2), 16));
      } catch (Exception e) {
        e.printStackTrace();
      } 
    } 
    try {
      s = new String(baKeyword, "UTF-8");
    } catch (Exception e1) {
      e1.printStackTrace();
    } 
    return s;
  }
  
  public static String unicode2String(String unicode) {
    StringBuffer string = new StringBuffer();
    String[] hex = unicode.split("\\\\u");
    for (int i = 1; i < hex.length; i++) {
      int data = Integer.parseInt(hex[i], 16);
      string.append((char)data);
    } 
    return string.toString();
  }
  
  public static String str2HexStr(String str) {
    char[] chars = "0123456789ABCDEF".toCharArray();
    StringBuilder sb = new StringBuilder("");
    byte[] bs = str.getBytes();
    for (int i = 0; i < bs.length; i++) {
      int bit = (bs[i] & 0xF0) >> 4;
      sb.append(chars[bit]);
      bit = bs[i] & 0xF;
      sb.append(chars[bit]);
    } 
    return sb.toString().trim();
  }
  
  public static String string2Unicode(String string) {
    StringBuffer unicode = new StringBuffer();
    for (int i = 0; i < string.length(); i++) {
      char c = string.charAt(i);
      unicode.append("\\u" + Integer.toHexString(c));
    } 
    return unicode.toString();
  }
  
  public static String hexStr2Str(String hexStr) {
    String str = "0123456789ABCDEF";
    char[] hexs = hexStr.toCharArray();
    byte[] bytes = new byte[hexStr.length() / 2];
    for (int i = 0; i < bytes.length; i++) {
      int n = str.indexOf(hexs[2 * i]) * 16;
      n += str.indexOf(hexs[2 * i + 1]);
      bytes[i] = (byte)(n & 0xFF);
    } 
    return new String(bytes);
  }
  
  protected int getLength(ChannelHandlerContext ctx, ByteBuf in) {
    int multiplier = 1;
    int length = 0;
    int digit = 0;
    if (in.readableBytes() < 1)
      System.out.println("" + ctx.channel().remoteAddress() + ", readableBytes().length: " + 
          in.readableBytes()); 
    byte remaining = in.readByte();
    digit = Integer.valueOf(remaining).intValue();
    length += (digit & 0x7F) * multiplier;
    multiplier *= 128;
    return length;
  }
  
  public static int Bytes2Int(byte[] dataSegment) {
    int intValue = (dataSegment[0] & 0xFF) << 16 | (dataSegment[1] & 0xFF) << 8 | dataSegment[2] & 0xFF;
    return intValue;
  }
  
  public static byte[] Int2Bytes(int intValue) {
    ByteBuffer buffer = ByteBuffer.allocate(4).putInt(intValue);
    byte[] dataSegment = new byte[3];
    buffer.position(1);
    buffer.get(dataSegment, 0, 3);
    return dataSegment;
  }
  
  public static byte[] timeStamp2bytesBig(long timestamp) {
    byte[] bytes = new byte[8];
    for (int i = 0; i < 8; i++) {
      int offset = 64 - (i + 1) * 8;
      bytes[i] = (byte)(int)(timestamp >> offset & 0xFFL);
    } 
    return bytes;
  }
  
  public static byte[] timeStamp2bytesSmall(long timestamp) {
    byte[] bytes = new byte[8];
    for (int i = 0; i < 8; i++) {
      int offset = i * 8;
      bytes[i] = (byte)(int)(timestamp >> offset & 0xFFL);
    } 
    return bytes;
  }
  
  public static long bytes2timeStampBig(byte[] bytes) {
    long timestamp = 0L;
    for (int i = 0; i < 8; i++) {
      int offset = 64 - (i + 1) * 8;
      timestamp |= (bytes[i] & 0xFF) << offset;
    } 
    return timestamp;
  }
  
  public static long bytes2timeStampSmall(byte[] bytes) {
    long result = 0L;
    for (int i = 0; i < 8; i++)
      result |= (bytes[i] & 0xFF) << i * 8; 
    return result;
  }
  
  public static long smallTime2BigTime(long timestampLittleEndian) {
    byte[] bytesLittleEndian = new byte[8];
    byte[] bytesBigEndian = new byte[8];
    int i;
    for (i = 0; i < 8; i++) {
      int offset = i * 8;
      bytesLittleEndian[i] = (byte)(int)(timestampLittleEndian >> offset & 0xFFL);
    } 
    for (i = 0; i < 8; i++)
      bytesBigEndian[i] = bytesLittleEndian[7 - i]; 
    long timestampBigEndian = 0L;
    for (int j = 0; j < 8; j++) {
      int offset = 56 - j * 8;
      timestampBigEndian |= (bytesBigEndian[j] & 0xFF) << offset;
    } 
    return timestampBigEndian;
  }
  
  public static long bigTime2SamllTime(long timestampBigEndian) {
    byte[] bytesBigEndian = new byte[8];
    byte[] bytesLittleEndian = new byte[8];
    int i;
    for (i = 0; i < 8; i++) {
      int offset = 56 - i * 8;
      bytesBigEndian[i] = (byte)(int)(timestampBigEndian >> offset & 0xFFL);
    } 
    for (i = 0; i < 8; i++)
      bytesLittleEndian[i] = bytesBigEndian[7 - i]; 
    long timestampLittleEndian = 0L;
    for (int j = 0; j < 8; j++) {
      int offset = j * 8;
      timestampLittleEndian |= (bytesLittleEndian[j] & 0xFF) << offset;
    } 
    return timestampLittleEndian;
  }
  
  public static void main(String[] args) {
    long currentTimeMillis = System.currentTimeMillis();
    byte[] timeStamp2bytes = timeStamp2bytesBig(currentTimeMillis);
    System.err.println(Arrays.asList(bytes2Hex(timeStamp2bytes)));
    System.err.println(bytes2timeStampBig(timeStamp2bytes));
  }
}
