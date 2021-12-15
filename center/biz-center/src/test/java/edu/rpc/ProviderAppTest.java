package edu.rpc;

import static org.junit.Assert.assertTrue;

import edu.rpc.bo.RpcReqBO;
import edu.rpc.bo.RpcRspBO;
import edu.rpc.service.IRpcService;
import edu.rpc.service.impl.RpcService;
import org.junit.Assert;
import org.junit.Test;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Properties;

/**
 * Unit test for simple App.
 */
public class ProviderAppTest {
    /**
     * Rigorous Test :-)
     */
    @Test
    public void shouldAnswerWithTrue() {

        IRpcService rpcService = new RpcService();
        RpcReqBO reqBO = new RpcReqBO();
        reqBO.setId(1);
        reqBO.setServiceName("test content");
        RpcRspBO rpcRspBO = rpcService.callRpc(reqBO);

        System.out.println(rpcRspBO);

        assertTrue(true);
    }

    @Test
    public void getEnve() {

        Properties properties = System.getProperties();
        for (Object o : properties.keySet()) {
            Object value = properties.get(o);
            System.out.println(o + "===" + value);
        }

        String property = System.getProperty("user.dir");
        System.out.println(property);

    }

    @Test
    public void provider() {
        Socket socket = null;
        try {
            //获取客户端的IP地址
            InetAddress address = InetAddress.getLocalHost();
            String ip = address.getHostAddress();
            //1.创建客户端Socket，指定服务器地址和端口
            socket = new Socket(ip, 12345);
            //2.获取输出流，向服务器端发送信息
            OutputStream os = socket.getOutputStream();//字节输出流
            PrintWriter pw = new PrintWriter(os);//将输出流包装为打印流

            pw.write("客户端：~" + ip + "~ 接入服务器！！");
            pw.flush();

            pw.write("123");
            pw.flush();

            RpcReqBO reqBO = new RpcReqBO();
            reqBO.setId(222);
            reqBO.setServiceName("测试数据信息啊大大");

            String property = System.getProperty("user.dir");
            String filePath = property.concat("\\").concat("a.txt");

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(new File(filePath)));


            out.writeObject(reqBO);
            out.close();


            InputStream in = new ObjectInputStream(new FileInputStream(filePath));

            byte[] buffer = new byte[1024];
            int len = 0;
            while ((len = in.read(buffer, 0, len)) != -1) {
                pw.write(new String(buffer, 0, len, "UTF-8"));
            }

            pw.flush();

            in.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.shutdownOutput();//关闭输出流
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
    public void series() {

        try {
            RpcReqBO reqBO = new RpcReqBO();
            reqBO.setId(222);
            reqBO.setServiceName("单元测试啥大大");

            String property = System.getProperty("user.dir");
            String filePath = property.concat("\\").concat("obj.bat");

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath));
            out.writeObject(reqBO);

            out.flush();

            ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath));
            RpcReqBO o = (RpcReqBO) in.readObject();

            System.out.println(o);

            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test
    public void providerTest() {

        try {
            //获取客户端的IP地址
            InetAddress address = InetAddress.getLocalHost();
            String ip = address.getHostAddress();
            //1.创建客户端Socket，指定服务器地址和端口
            Socket socket = new Socket(ip, 12345);

            OutputStream out = socket.getOutputStream();


            RpcReqBO reqBO = new RpcReqBO();
            reqBO.setId(222);
            reqBO.setServiceName("单元测试啥大大");

            ObjectOutputStream oos = new ObjectOutputStream(out);

            oos.writeObject(reqBO);

            oos.flush();
            oos.close();

            out.close();

        } catch (IOException e) {
            e.printStackTrace();
        } finally {
        }
    }

    /**
     * 单元测试
     * 创建服务提供者Socket
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 9:11
     */
    @Test
    public void instanceSocketTest() {

        ProviderApp providerApp = new ProviderApp();

        //创建Socket
        Socket socket = providerApp.instanceSocket();

        Assert.assertTrue("创建服务提供者Socket成功！", socket != null);

        //简单发送内容
        // providerApp.sendSimpleContent("你好！");

        String filePath = "D:\\DevelopmentTools\\IDEA.Workspace\\rpc\\center\\biz-center\\src\\main\\java\\edu\\rpc\\service\\impl\\RpcService.java";
        //发送文件
        providerApp.sendFile(filePath);

        // providerApp.maintainConnecting();

        //发送序列化对象
        // providerApp.sendSerializableObj(RpcService.class);

    }


    /**
     * JDK动态代理RPC远程服务调用
     * 提供者
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 19:36
     */
    @Test
    public void providerRpcInvoke() {

        ProviderApp providerApp = new ProviderApp();

        Socket socket = providerApp.instanceSocket();

        providerApp.rpcInvoke();

        providerApp.closeSocket();

    }
}
