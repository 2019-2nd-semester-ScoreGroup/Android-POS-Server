package Manager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class NetworkManager {
    private static final int PORT = 8080;
    private ServerController serverController;

    NetworkManager(ServerController serverController)
    {
        this.serverController = serverController;
        Run();
    }

    public void Run() {
        try
        {
            ServerSocket serverSocket = new ServerSocket(PORT);

            while(true)
            {
                Socket socket = serverSocket.accept();
                new Thread(()->{
                    ConnectionWrap connectionSocket = new ConnectionWrap(socket, serverController);
                }).start();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
    }
}

class ConnectionWrap implements Runnable{

    private Socket socket = null;
    private ServerController serverController;
    private String msg = null;
    private BufferedReader inputBuffer;
    private PrintWriter printWriter;

    public ConnectionWrap(Socket socket, ServerController serverController)
    {
        this.serverController = serverController;
        this.socket = socket;
        run();
    }

    @Override
    public void run() {

        System.out.println(Thread.currentThread().getName() + " thread accpet");

        try
        {
            getIOstream();

            onReceive();
        }
        catch (IOException e)
        {
            e.printStackTrace();
            try {
                socket.close();
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }

        /* 시험용
        if(msg.equals("hello"))
            reply("world!");
         */

        System.out.println(Thread.currentThread().getName() + " thread end");
    }

    private void getIOstream() throws IOException
    {
        inputBuffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
    }

    public void onReceive() throws IOException
    {
        getMsg();

        reqeustParse();

        reply();
    }

    private void getMsg() throws IOException
    {
        msg = inputBuffer.readLine();

        System.out.println(msg);

    }

    private void reqeustParse()
    {
        msg = serverController.parseAndExecuteData(msg);
    }

    private void reply()
    {
        printWriter.println(msg);
        printWriter.flush();
    }
}
