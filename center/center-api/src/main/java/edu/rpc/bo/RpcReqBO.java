package edu.rpc.bo;

import java.io.Serializable;

/**
 * 类功能说明
 *
 * @author ljl
 * @version 1.0.0
 * @date 2021/12/11 16:08
 */
public class RpcReqBO implements Serializable {

    private Integer id;

    private String serviceName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return "RpcReqBO{" +
                "id=" + id +
                ", serviceName='" + serviceName + '\'' +
                '}';
    }
}
