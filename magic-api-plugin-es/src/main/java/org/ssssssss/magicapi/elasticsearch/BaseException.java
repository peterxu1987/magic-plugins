package org.ssssssss.magicapi.elasticsearch;

import java.text.MessageFormat;

/**
 * 基础异常
 */
public class BaseException extends RuntimeException {
 
	private static final long serialVersionUID = 1L;
	public String msg;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String msgFormat, Object... args) {
        super(MessageFormat.format(msgFormat, args));
        this.msg = MessageFormat.format(msgFormat, args);
    }

    public String getMsg() {
        return this.msg;
    }

}
