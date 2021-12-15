package edu.rpc.task;

import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.Callable;

/**
 * 类功能说明
 *
 * @author ljl
 * @version 1.0.0
 * @date 2021/12/13 20:27
 */
public class GetClassCallable implements Callable {

    private Socket socket;

    public GetClassCallable(Socket socket) {
        this.socket = socket;
    }

    /**
     * Computes a result, or throws an exception if unable to do so.
     *
     * @return computed result
     * @throws Exception if unable to compute a result
     */
    @Override
    public Object call() throws Exception {

        OutputStream os = socket.getOutputStream();

        ObjectOutputStream oos = new ObjectOutputStream(os);


        return null;
    }
}
