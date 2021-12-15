package edu.rpc.handler;

import edu.rpc.task.GetClassCallable;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.FutureTask;
import java.util.concurrent.TimeUnit;

/**
 * 类功能说明
 *
 * @author ljl
 * @version 1.0.0
 * @date 2021/12/11 16:22
 */
public class ProxyInvocationHandler implements InvocationHandler {

    /**
     * 主机名
     */
    private final static String hostName;
    /**
     * 主机IP地址
     */
    private final static String ip;
    /**
     * Socket通信端口
     */
    private final int socketPort = 9527;
    /**
     * 客户端
     */
    private Socket socket = null;


    static {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("——————————获取本机InetAddress实例异常——————————");
            e.printStackTrace();
        }
        hostName = address.getHostName();
        ip = address.getHostAddress();
        System.out.println("——————————本机主机名hostName：" + hostName + "，IP：" + ip);
    }

    public ProxyInvocationHandler(Class<? extends Object> clazz, ClassLoader classLoader) {

        try {
            //建立通信
            //1.创建一个服务器端Socket，即ServerSocket，指定绑定的端口，并监听此端口
            ServerSocket serverSocket = new ServerSocket(socketPort);

            //2.调用accept()等待客户端连接
            socket = serverSocket.accept();

            System.out.println("————————————————————客户端已连接————————————————————");

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

        }

    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        FutureTask getClassFutureTask = new FutureTask(new GetClassCallable(socket));
        Thread getClassThread = new Thread(getClassFutureTask);
        getClassThread.start();

        Class clazz = (Class) getClassFutureTask.get(10, TimeUnit.SECONDS);

        // ClassLoader classLoader = Thread.currentThread().getContextClassLoader();


        Object o = clazz.newInstance();


        // Constructor constructor = clazz.getConstructor();
        // Object o = constructor.newInstance();

        //建立通信
        //1、将服务提供者的Class加载到消费者对于的ClassLoader中
        //2、通过反射生成对象
        //3、针对生成的对象进行代理操作
        //4、服务调用时，将入参以流的形式请求给服务提供者
        //5、获取到服务提供者的执行结果进行反序列化


        // Object o1 = clazz.newInstance();

        System.out.println("————————————————————————————————————————");

        return proxy;
    }
}
