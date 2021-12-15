package edu.rpc;

import com.alibaba.fastjson.JSONObject;
import edu.rpc.bo.RpcReqBO;
import edu.rpc.bo.RpcRspBO;
import edu.rpc.service.impl.RpcService;

import java.io.*;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

/**
 * Hello world!
 */
public class ProviderApp {

    /**
     * 主机名
     */
    private final static String hostName;
    /**
     * 主机IP地址
     */
    private final static String IP;
    /**
     * Socket通信端口
     */
    private static final int PORT = 9527;


    static {
        InetAddress address = null;
        try {
            address = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            System.err.println("——————————获取本机InetAddress实例异常——————————");
            e.printStackTrace();
        }
        hostName = address.getHostName();
        IP = address.getHostAddress();
        System.out.println("——————————本机主机名hostName：" + hostName + "，IP：" + IP);
    }


    /**
     * @param args
     * @return void
     * @author ljl
     * @date 2021/12/12 10:19
     * @see [java.lang.String[]]
     */
    public static void main(String[] args) throws Exception {

        //1.创建客户端Socket，指定服务器地址和端口
        Socket socket = new Socket(IP, PORT);
        //2.获取输出流，向服务器端发送信息
        OutputStream os = socket.getOutputStream();//字节输出流

        ObjectOutputStream oos = new ObjectOutputStream(os);
    }


    private static InputStream seriesObj() {
        ObjectInputStream in = null;

        try {
            //D:\DevelopmentTools\IDEA.Workspace\rpc\center\biz-center
            java.lang.String property = System.getProperty("user.dir");

            java.lang.String file = property.concat("\\").concat("obj.txt");

            ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(file));

            RpcReqBO reqBOOut = new RpcReqBO();
            reqBOOut.setId(200);
            reqBOOut.setServiceName("服务名");

            out.writeObject(reqBOOut);

            out.close();
            in = new ObjectInputStream(new FileInputStream(file));


            RpcReqBO reqBOIn = (RpcReqBO) in.readObject();

            System.out.println(JSONObject.toJSONString(reqBOIn));


        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
        }

        return in;
    }

    private Socket socket = null;

    /**
     * 创建生产者Socket
     *
     * @param
     * @return java.net.Socket
     * @author ljl
     * @date 2021/12/14 9:09
     */
    public Socket instanceSocket() {

        try {
            socket = new Socket(IP, PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("——————————提供者创建Socket成功——————————");
        return socket;
    }


    /**
     * 发送简单内容
     *
     * @param content
     * @return void
     * @author ljl
     * @date 2021/12/14 9:17
     * @see java.lang.String
     */
    public void sendSimpleContent(String content) {
        OutputStream os = null;
        try {
            os = socket.getOutputStream();
            os.write(content.getBytes());
            os.flush();
            System.out.println("——————————提供者发送内容完毕——————————");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 发送文件
     *
     * @param filePath 文件路径
     * @return void
     * @author ljl
     * @date 2021/12/14 11:38
     * @see java.lang.String
     */
    public void sendFile(String filePath) {
        OutputStream os = null;
        FileInputStream fis = null;
        try {
            os = socket.getOutputStream();

            fis = new FileInputStream(filePath);

            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = fis.read(buffer)) != -1) {
                os.write(buffer, 0, len);
            }
            os.flush();
            System.out.println("——————————提供者发送文件完毕——————————");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeOutputStream(os);
            closeInputStream(fis);
        }


    }


    /**
     * 发送序列化对象
     *
     * @param clazz
     * @return void
     * @author ljl
     * @date 2021/12/14 11:53
     * @see java.lang.Class<edu.rpc.service.impl.RpcService>
     */
    public void sendSerializableObj(Class<RpcService> clazz) {

        OutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            os = socket.getOutputStream();

            RpcService rpcService = clazz.newInstance();

            oos = new ObjectOutputStream(os);

            oos.writeObject(clazz);

            os.flush();

            System.out.println("——————————发送序列化对象完毕——————————");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } finally {
            try {
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                os.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


    }

    /**
     * 关闭输入流
     *
     * @param is
     * @return void
     * @author ljl
     * @date 2021/12/14 14:07
     * @see java.io.InputStream
     */
    private void closeInputStream(InputStream is) {
        if (is != null) {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 关闭输出流
     *
     * @param out
     * @return void
     * @author ljl
     * @date 2021/12/14 14:07
     * @see java.io.InputStream
     */
    private void closeOutputStream(OutputStream out) {
        if (out != null) {
            try {
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void maintainConnecting() {
        System.out.println("——————————保持提供者不关闭——————————");
        while (true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 服务提供者执行
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 19:43
     */
    public void rpcInvoke() {

        Object object = null;
        InputStream is = null;
        ObjectInputStream osi = null;
        try {
            is = socket.getInputStream();

            osi = new ObjectInputStream(is);

            object = osi.readObject();

            System.out.println("——————————提供者获取消费者的入参——————————" + object);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            // closeInputStream(osi);
            // closeInputStream(is);
        }


        if (object instanceof RpcReqBO) {


            RpcReqBO reqBO = (RpcReqBO) object;

            RpcService rpcService = new RpcService();
            RpcRspBO rpcRspBO = rpcService.callRpc(reqBO);


            System.out.println("——————————服务提供者执行结果——————————" + rpcRspBO);
            //将执行结果返回
            OutputStream os = null;
            ObjectOutputStream oos = null;
            try {
                os = socket.getOutputStream();
                oos = new ObjectOutputStream(os);

                oos.reset();
                oos.writeObject(rpcRspBO);

                oos.flush();

                System.out.println("——————————服务提供者完成服务执行结果发送给消费者——————————");
            } catch (IOException e) {
                e.printStackTrace();
            } finally {

                // closeOutputStream(oos);

                // closeOutputStream(os);
            }


        }
    }

    /**
     * 关闭Socket
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/15 8:54
     */
    public void closeSocket() {
        System.out.println("——————————关闭提供者的Socket——————————");
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
