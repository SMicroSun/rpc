package edu.rpc;

import static org.junit.Assert.assertTrue;

import edu.rpc.bo.RpcReqBO;
import edu.rpc.bo.RpcRspBO;
import edu.rpc.service.IRpcService;
import net.sf.cglib.proxy.Callback;
import net.sf.cglib.proxy.Enhancer;
import net.sf.cglib.proxy.MethodInterceptor;
import net.sf.cglib.proxy.MethodProxy;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Unit test for simple App.
 * <p>
 * <p>
 * RPC框架的执行原理：
 * 提供者将服务注册到注册中心，消费者从注册中心拉取服务列表到本地，防止注册中心宕掉之后，可以通过本地的服务列表来找到所调用的服务，
 * <p>
 * 整体的服务执行流程是：
 * 消费者将入参转为JSON或者是序列化转为流，然后与服务提供者进行Socket通信，服务提供者执行完毕将执行结果进行序列化，再通过Socket
 * 通过将执行结果返回给消费者
 */
public class ConsumerAppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {
        Socket socket = null;
        try {
            //1.创建一个服务器端Socket，即ServerSocket，指定绑定的端口，并监听此端口
            ServerSocket serverSocket = new ServerSocket(12345);

            InetAddress address = InetAddress.getLocalHost();
            String ip = address.getHostAddress();
            socket = null;

            //2.调用accept()等待客户端连接
            System.out.println("~~~服务端已就绪，等待客户端接入~，服务端ip地址: " + ip);
            socket = serverSocket.accept();

            //3.连接后获取输入流，读取客户端信息
            InputStream is = null;
            InputStreamReader isr = null;
            BufferedReader br = null;
            OutputStream os = null;
            PrintWriter pw = null;
            is = socket.getInputStream();     //获取输入流
            isr = new InputStreamReader(is, "UTF-8");
            br = new BufferedReader(isr);
            String info = null;
            while ((info = br.readLine()) != null) {//循环读取客户端的信息
                System.out.println("客户端发送过来的信息" + info);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.shutdownInput();//关闭输入流
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    @Test
    public void consumerTest() {
        try {
            //1.创建一个服务器端Socket，即ServerSocket，指定绑定的端口，并监听此端口
            ServerSocket serverSocket = new ServerSocket(12345);

            Socket accept = serverSocket.accept();

            InputStream in = accept.getInputStream();

            ObjectInputStream ois = new ObjectInputStream(in);

            RpcReqBO o = (RpcReqBO) ois.readObject();

            System.out.println(o);

            ois.close();

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取消费者客户端连接
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 9:02
     */
    @Test
    public void instanceClientTest() {

        ConsumerApp consumerApp = new ConsumerApp<Object>();

        Socket socket = consumerApp.instanceClient();

        Assert.assertTrue("获取客户端连接成功！", socket != null);

        //接收简单文本
        // consumerApp.receiveContent();

        //接收文件
        File file = consumerApp.receiveFile(IRpcService.class);

        //文件加载到JVM
        Class clazz = consumerApp.loadClassFileToJVM("edu.rpc.service.impl.RpcService");

        try {
            IRpcService o = (IRpcService) clazz.newInstance();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        // Object o = consumerApp.receiveSerializableObj();

        // Assert.assertTrue("IRpcService服务对象创建成功！", o != null);

        // Assert.assertNotNull("对象创建失败！", o);
    }

    @Test
    public void getInterClazz() {
        ConsumerApp consumerApp = new ConsumerApp<IRpcService>();

        String name = consumerApp.getInterClazz(IRpcService.class);

        System.out.println(name);
    }

    @Test
    public void createNewFile() {

        ConsumerApp consumerApp = new ConsumerApp();

        String interClazzPath = consumerApp.getInterClazz(IRpcService.class);

        consumerApp.createNewFile(interClazzPath);
    }

    /**
     * 字节码加载到JVM
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 15:40
     */
    @Test
    public void loadClassFileToJVM() {

        ConsumerApp<Object> consumerApp = new ConsumerApp<>();

        Class<?> clazz = consumerApp.loadClassFileToJVM("edu.rpc.service.impl.RpcService");

        try {
            IRpcService o = (IRpcService) clazz.newInstance();

            RpcRspBO rpcRspBO = o.callRpc(null);
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }

    /**
     * 使用JDK动态代理，仅使用接口来创建代理对象是无法实现
     * 被创建代理对象的接口必须要有一个实现，然后是针对当前接口的实现类的实现进行代理增强
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 15:49
     */
    @Test
    public void proxyJDK() {

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();
        Class<?>[] classes = new Class[]{IRpcService.class};

        IRpcService o = (IRpcService) Proxy.newProxyInstance(classloader, classes, new InvocationHandler() {
            AtomicInteger atomicInteger = new AtomicInteger(0);

            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

                Object result = new Object();
                if (args != null) {
                    for (Object arg : args) {
                        System.out.println(arg);
                        if (arg instanceof RpcReqBO) {
                            RpcReqBO rpcReqBO = (RpcReqBO) arg;
                            RpcRspBO rpcRspBO = new RpcRspBO();
                            rpcRspBO.setMessage(rpcReqBO.getServiceName());
                            rpcRspBO.setCode(200);
                            return rpcRspBO;
                        }
                    }
                }
                return result;
            }
        });

        RpcReqBO reqBO = new RpcReqBO();
        reqBO.setServiceName("丹玉测试");
        reqBO.setId(234);
        RpcRspBO rpcRspBO = o.callRpc(reqBO);
        System.out.println(rpcRspBO);
    }


    /**
     * 使用CGLib动态代理进行动态代理
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 16:41
     */
    @Test
    public void proxyCGlib() {

        Enhancer enhancer = new Enhancer();

        //设置被代理接口
        enhancer.setInterfaces(new Class[]{IRpcService.class});

        enhancer.setCallback(new MethodInterceptor() {

            @Override
            public Object intercept(Object obj, Method method, Object[] args, MethodProxy proxy) throws Throwable {
                Object result = new Object();
                System.out.println("————————————————增强执行————————————————前————————————————");


                if (args != null) {
                    for (Object arg : args) {
                        System.out.println(arg);
                        if (arg instanceof RpcReqBO) {
                            RpcReqBO rpcReqBO = (RpcReqBO) arg;
                            RpcRspBO rpcRspBO = new RpcRspBO();
                            rpcRspBO.setMessage(rpcReqBO.getServiceName());
                            rpcRspBO.setCode(200);
                            return rpcRspBO;
                        }
                    }
                }
                System.out.println("————————————————增强执行————————————————后————————————————");

                return result;
            }
        });

        //创建代理对象
        IRpcService rpcService = (IRpcService) enhancer.create();
        RpcReqBO reqBO = new RpcReqBO();
        reqBO.setId(1);
        reqBO.setServiceName("1231231");
        RpcRspBO rpcRspBO = rpcService.callRpc(reqBO);
        System.out.println(rpcRspBO);
    }


    /**
     * JDK动态代理RPC远程服务调用
     * 消费者
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 19:12
     */
    @Test
    public void consumerRpcInvoke() {

        //映射的服务路由
        final Map<String, Boolean> mapping = new HashMap<>();
        mapping.put("edu.rpc.service.IRpcService", true);

        final ConsumerApp consumerApp = new ConsumerApp<>();

        ClassLoader classloader = Thread.currentThread().getContextClassLoader();

        Class<?>[] classes = new Class[]{IRpcService.class};
        IRpcService rpcService = (IRpcService) Proxy.newProxyInstance(classloader, classes, new InvocationHandler() {
            @Override
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                Object object = new Object();


                Class<?> methodDeclaringClass = method.getDeclaringClass();
                String methodDeclaringClassName = methodDeclaringClass.getName();

                if (mapping.containsKey(methodDeclaringClassName)) {
                    //连接Provider
                    Socket socket = consumerApp.instanceClient();

                    //获取服务入参BO
                    RpcReqBO reqBO = consumerApp.getRpcReqBO(method, args);

                    //调用服务Provider，获取服务执行结果
                    RpcRspBO rpcRspBO = consumerApp.invoke(reqBO);

                    //关闭Socket
                    consumerApp.closeSocket();

                    return rpcRspBO;
                }


                return object;
            }
        });

        RpcReqBO reqBO = new RpcReqBO();
        reqBO.setId(200);
        reqBO.setServiceName("应用App执行");
        RpcRspBO rpcRspBO = rpcService.callRpc(reqBO);
        System.out.println("——————————执行结果——————————");
        System.out.println(rpcRspBO);

    }
}
