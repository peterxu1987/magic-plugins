package org.ssssssss.magicapi.elasticsearch;


/**
 * es 执行异常
 */
public class ElasticSearchRunException extends BaseException {
	private static final long serialVersionUID = 1L;

	public ElasticSearchRunException(String message) {
        super(message);
    }

    public ElasticSearchRunException(String mess, Object... args) {
        super(mess, args);
    }

}
