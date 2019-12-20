package com.zlp.common;

/**
 * @ClassName: Result
 * @Description: 返回结果集
 * @Autor:13528
 * @Date: 2019/12/19 8:42
 * @Version 1.0
 **/
public class Result {
    private boolean success;
    private String message;

    public Result(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public boolean getSuccess() {
        return success;
    }

    public void setSuccess(boolean success) {
        this.success = success;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
