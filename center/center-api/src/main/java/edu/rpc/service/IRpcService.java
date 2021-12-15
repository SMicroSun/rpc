package edu.rpc.service;

import edu.rpc.bo.RpcReqBO;
import edu.rpc.bo.RpcRspBO;

import java.io.Serializable;

/**
 * 类功能说明
 *
 * @author ljl
 * @version 1.0.0
 * @date 2021/12/11 16:06
 */
public interface IRpcService  {


    RpcRspBO callRpc(RpcReqBO reqBO);
}
