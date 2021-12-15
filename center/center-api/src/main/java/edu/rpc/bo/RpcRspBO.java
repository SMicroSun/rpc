package edu.rpc.bo;

import java.io.Serializable;

/**
 * 类功能说明
 *
 * @author ljl
 * @version 1.0.0
 * @date 2021/12/11 16:09
 */
public class RpcRspBO implements Serializable {

    private Integer code;

    private String message;

    public Integer getCode() {
        return code;
    }

    public void setCode(Integer code) {
        this.code = code;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return "RpcRspBO{" +
                "code=" + code +
                ", message='" + message + '\'' +
                '}';
    }
}
