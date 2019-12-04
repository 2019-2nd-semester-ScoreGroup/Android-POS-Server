package Manager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

/*
클라이언트와 연결될 때마다 새로운 쓰레드로 소켓을 생성함
소켓에서 메시지를 보내오면 SeverController.parseAndExecuteData() 호출 후,
반환 값을 해당 소켓에 리턴
 */
public class NetworkManager {
    //포트 번호, 자신, 종료값 입력을 위한 스캐너
    private static final int PORT = 12142;
    private ServerController serverController;

    NetworkManager(ServerController serverController)
    {
        this.serverController = serverController;
        Run();
    }

    public void Run() {
        try
        {
            //서버 생성
            ServerSocket serverSocket = new ServerSocket(PORT);

            //서버가 종료되기 전
            while(true)
            {
                //클라이언트 연결 소켓 생성
                Socket socket = serverSocket.accept();
                //스레드를 새로 생성하여 래핑한 소켓 생성
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

        //IO 객체를 생성하지 못하면 소켓을 종료, 전송 실패해도 에러는 나지 않음
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

    //IO 객체 생성
    private void getIOstream() throws IOException
    {
        inputBuffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        printWriter = new PrintWriter(new BufferedWriter(new OutputStreamWriter(socket.getOutputStream())));
    }

    //main 함수 간소화
    public void onReceive() throws IOException
    {
        getMsg();

        reqeustParse();

        reply();
    }

    //소켓으로 들어온 메시지를 라인 단위로 읽음
    private void getMsg() throws IOException
    {
        msg = inputBuffer.readLine();

        System.out.println("query " + msg);
    }

    //메시지 해석을 요청
    private void reqeustParse()
    {
        msg = serverController.parseAndExecuteData(msg);
    }

    //requestParse()에서 반환된 메시지를 소켓을 통해 반환
    private void reply()
    {
        printWriter.println(msg);
        printWriter.flush();

        System.out.println("ack " + msg);
    }
}
