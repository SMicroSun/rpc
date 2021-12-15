package edu.rpc;

import edu.rpc.bo.RpcReqBO;
import edu.rpc.bo.RpcRspBO;
import edu.rpc.handler.ProxyInvocationHandler;
import edu.rpc.service.IRpcService;

import java.io.*;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.net.*;
import java.util.concurrent.*;

/**
 * Hello world!
 */
public class ConsumerApp<T> {


    private static IRpcService rpcService;

    public static void main(String[] args) {


        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?>[] interfaces = new Class[]{IRpcService.class};
        InvocationHandler handler = new ProxyInvocationHandler(IRpcService.class, classLoader);

        rpcService = (IRpcService) Proxy.newProxyInstance(classLoader, interfaces, handler);

        RpcReqBO rpcReqBO = new RpcReqBO();

        RpcRspBO rpcRspBO = rpcService.callRpc(rpcReqBO);

        System.out.println(rpcRspBO.toString());
    }


    /**
     * 端口
     */
    private static final int PORT = 9527;

    private static final String IP;

    static {
        InetAddress inetAddress = null;
        try {
            inetAddress = InetAddress.getLocalHost();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        IP = inetAddress.getHostAddress();
    }


    private ServerSocket serverSocket = null;

    private Socket accept = null;

    public Socket instanceClient() {

        try {
            serverSocket = new ServerSocket(PORT);
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("——————————等待客户端连接——————————");

        try {
            accept = serverSocket.accept();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("——————————客户端连接成功——————————");
        return accept;
    }

    /**
     * 接收内容
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/14 9:55
     */
    public void receiveContent() {
        InputStream is = null;
        try {
            is = accept.getInputStream();

            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = is.read(buffer)) != -1) {
                System.out.println(new String(buffer, 0, len, "UTF-8"));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * 方法说明
     *
     * @param interClazz
     * @return java.io.File
     * @author ljl
     * @date 2021/12/14 15:12
     * @see java.lang.Class
     * @see java.io.File
     */
    public File receiveFile(Class interClazz) {

        InputStream is = null;

        File file = null;

        try {

            is = accept.getInputStream();

            String filePath = getInterClazz(interClazz);

            file = createNewFile(filePath);

            writeByteToFile(is, file);

        } catch (IOException e) {
            e.printStackTrace();
        } finally {

            // closeInputStream(is);

        }

        return file;
    }


    /**
     * 将流中字节写入文件中
     *
     * @param is   输入流
     * @param file 流字节保存的文件
     * @return void
     * @author ljl
     * @date 2021/12/14 14:46
     * @see java.io.InputStream
     * @see java.io.File
     */
    public void writeByteToFile(InputStream is, File file) {

        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream(file);

            int len = 0;
            byte[] buffer = new byte[1024];

            /**
             * 如果Provider的输出流未关闭，则必须使用此种方式is.available()来读取流内容
             *
             */
            // do {
            //     len = is.read(buffer);
            //     fos.write(buffer, 0, len);
            // } while (is.available() != 0);

            /**
             * 使用is.read(buffer)方式来读取流内容的话，必须将Provider的输出流关闭
             * 否则会报错
             * java.net.SocketException: Connection reset
             */
            while ((len = is.read(buffer)) != -1) {
                fos.write(buffer, 0, len);
            }


            fos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeOutputStream(fos);
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

    /**
     * 接收序列化对象
     *
     * @param
     * @return edu.rpc.service.IRpcService
     * @author ljl
     * @date 2021/12/14 11:56
     */
    public Object receiveSerializableObj() {

        InputStream is = null;
        ObjectInputStream ois = null;
        Object obj = null;

        try {
            is = accept.getInputStream();

            ois = new ObjectInputStream(is);

            //接收Class对象
            Object o = ois.readObject();

            if (o instanceof Class) {
                Class clazz = (Class) o;
                try {
                    obj = clazz.newInstance();
                } catch (InstantiationException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                }
            }

            System.out.println("——————————消费者接收序列化对象完毕——————————");
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            try {
                ois.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        return obj;
    }

    /**
     * 获取class文件保存的位置
     *
     * @param clazz
     * @return java.lang.String
     * @author ljl
     * @date 2021/12/14 14:05
     * @see java.lang.Class<T>
     * @see java.lang.String
     */
    public String getInterClazz(Class<T> clazz) {
        String property = System.getProperty("user.dir");
        String name = clazz.getName();


        int i = name.lastIndexOf(".");
        name = name.substring(0, i).concat(".impl").concat(".").concat(name.substring(i + 2));

        name = name.replaceAll("\\.", "\\\\");
        name = property.concat("\\").concat("src\\main\\java").concat("\\").concat(name).concat(".java");
        return name;
    }

    /**
     * 创建Class文件
     *
     * @param clazzPath
     * @return void
     * @author ljl
     * @date 2021/12/14 14:33
     * @see java.lang.String
     */
    public File createNewFile(String clazzPath) {
        File file = new File(clazzPath);
        if (!file.exists()) {

            int i = clazzPath.lastIndexOf("\\");

            String directoryPath = clazzPath.substring(0, i);
            File directory = new File(directoryPath);
            boolean mkdirs = directory.mkdirs();
            System.out.println("——————————创建文件夹成功——————————" + mkdirs);

            boolean newFile = false;
            try {
                newFile = file.createNewFile();
                System.out.println("——————————创建文件成功——————————" + newFile);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return file;
    }

    /**
     * 文件加载到JVM
     *
     * @param name
     * @return void
     * @author ljl
     * @date 2021/12/14 15:12
     * @see java.io.File
     */
    public Class<?> loadClassFileToJVM(String name) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        Class<?> aClass = null;
        try {
            aClass = classLoader.loadClass(name);
        } catch (ClassNotFoundException e) {
            ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
            try {
                aClass = systemClassLoader.loadClass(name);
            } catch (ClassNotFoundException classNotFoundException) {
                classNotFoundException.printStackTrace();
            }
        }

        return aClass;
    }

    /**
     * 获取服务入参BO
     *
     * @param method
     * @param args
     * @return edu.rpc.bo.RpcReqBO
     * @author ljl
     * @date 2021/12/14 19:28
     * @see java.lang.reflect.Method, java.lang.Object[]
     * @see edu.rpc.bo.RpcReqBO
     */
    public RpcReqBO getRpcReqBO(Method method, Object[] args) {
        RpcReqBO reqBO = new RpcReqBO();
        if (args != null) {
            for (Object arg : args) {
                if (arg instanceof RpcReqBO) {
                    reqBO = (RpcReqBO) arg;
                    return reqBO;
                }
            }
        }
        return reqBO;
    }

    /**
     * 请求服务提供者Provider，
     * 获取服务提供者Provider执行结果
     *
     * @param reqBO
     * @return edu.rpc.bo.RpcRspBO
     * @author ljl
     * @date 2021/12/14 19:32
     * @see edu.rpc.bo.RpcReqBO
     * @see edu.rpc.bo.RpcRspBO
     */
    public RpcRspBO invoke(final RpcReqBO reqBO) {

        RpcRspBO rpcRspBO = null;

        /**
         * 发起请求
         */
        OutputStream os = null;
        ObjectOutputStream oos = null;
        try {
            os = accept.getOutputStream();

            oos = new ObjectOutputStream(os);

            oos.writeObject(reqBO);

            oos.flush();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // closeOutputStream(oos);
            // closeOutputStream(os);
        }


        // Thread reqBOThread = new Thread(new Runnable() {
        //     @Override
        //     public void run() {
        //
        //     }
        // });
        // System.out.println("——————————消费者已经发起服务调用——————————");
        // reqBOThread.start();


        try {
            InputStream is = accept.getInputStream();
            ObjectInputStream ois = new ObjectInputStream(is);
            Object object = ois.readObject();
            if (object instanceof RpcRspBO) {
                rpcRspBO = (RpcRspBO) object;
                return rpcRspBO;
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        // FutureTask<RpcRspBO> rspBOFutureTask = new FutureTask<>(new Callable<RpcRspBO>() {
        //
        //     @Override
        //     public RpcRspBO call() throws Exception {
        //
        //         return null;
        //     }
        // });
        // Thread rspBOThread = new Thread(rspBOFutureTask);
        // rspBOThread.start();

        // try {
        //     System.out.println("——————————消费者等待获取执行结果——————————");
        //     rpcRspBO = rspBOFutureTask.get(20, TimeUnit.SECONDS);
        // } catch (InterruptedException e) {
        //     e.printStackTrace();
        // } catch (ExecutionException e) {
        //     e.printStackTrace();
        // } catch (TimeoutException e) {
        //     e.printStackTrace();
        // }


        return rpcRspBO;
    }

    /**
     * 关闭Socket
     *
     * @param
     * @return void
     * @author ljl
     * @date 2021/12/15 8:56
     */
    public void closeSocket() {
        System.out.println("——————————关闭消费者的Socket——————————");
        if (serverSocket != null) {
            try {
                serverSocket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
