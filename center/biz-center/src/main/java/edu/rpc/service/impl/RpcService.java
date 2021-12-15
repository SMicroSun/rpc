package edu.rpc.service.impl;

import com.alibaba.fastjson.JSONObject;
import edu.rpc.bo.RpcReqBO;
import edu.rpc.bo.RpcRspBO;
import edu.rpc.service.IRpcService;

/**
 * 类功能说明
 *
 * @author ljl
 * @version 1.0.0
 * @date 2021/12/11 16:45
 */
public class RpcService implements IRpcService {

    @Override
    public RpcRspBO callRpc(RpcReqBO reqBO) {
        RpcRspBO rpcRspBO = new RpcRspBO();
        rpcRspBO.setCode(200);
        rpcRspBO.setMessage(JSONObject.toJSONString(reqBO));
        return rpcRspBO;
    }
}
