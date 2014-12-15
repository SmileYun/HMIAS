package com.cqupt.hmi.core.ioc;

public class CCAppException extends Exception {

    /**
     * @author CC
     * @date 2014年12月4日 下午2:39:10 
     * @Fields serialVersionUID : The Constant serialVersionUID.
     */
    private static final long serialVersionUID = 458961853501479810L;
    private String msg;

    public CCAppException() {

    }

    public CCAppException(String msg) {
        super(msg);
        this.msg = msg;
    }

    /**
     * 描述：获取异常信息.
     *
     * @return the message
     */
    @Override
    public String getMessage() {
        return msg;
    }
}
