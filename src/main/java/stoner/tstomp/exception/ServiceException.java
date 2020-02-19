/******************************************************************************
 * Copyright (C) 2017 ShenZhen ComTop Information Technology Co.,Ltd
 * All Rights Reserved.
 * 本软件为深圳康拓普开发研制。未经本公司正式书面同意，其他任何个人、团体不得使用、
 * 复制、修改或发布本软件.
 ******************************************************************************/
package stoner.tstomp.exception;

/**
 * Service层公用的Exception.
 *
 * 继承自RuntimeException, 从由Spring管理事务的函数中抛出时会触发事务回滚.
 *
 * @author calvin
 */
public class ServiceException extends RuntimeException {

    /**  */
    private static final long serialVersionUID = 3583566093089790852L;

    /**
     * 构造函数
     */
    public ServiceException() {
        super();
    }

    /**
     * 构造函数
     *
     * @param message xx
     */
    public ServiceException(String message) {
        super(message);
    }

    /**
     * 构造函数
     *
     * @param cause xx
     */
    public ServiceException(Throwable cause) {
        super(cause);
    }

    /**
     * 构造函数
     *
     * @param message xx
     * @param cause xx
     */
    public ServiceException(String message, Throwable cause) {
        super(message, cause);
    }
}
