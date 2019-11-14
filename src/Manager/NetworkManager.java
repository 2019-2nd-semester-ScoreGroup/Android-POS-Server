package Manager;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NetworkManager {
    // 연결할 포트를 지정
    private static final int PORT = 8080;
    // 스레드 풀의 최대 스레드 개수를 지정
    private static final int THREAD_CNT = 5;
    private static ExecutorService threadPool = Executors.newFixedThreadPool(THREAD_CNT);
    public static void main(String[] args) {

        try {
            // 서버소켓 생성
            ServerSocket serverSocket = new ServerSocket(PORT);

            // 소켓서버가 종료될때까지 무한루프
            while(true){
                // 소켓 접속 요청이 올때까지 대기
                Socket socket = serverSocket.accept();
                try{
                    /*
                    요청이 오면 스레드 풀의 스레드로 소켓을 넣음
                    submit : 예외발생시 재사용
                    execut : 예외발생시 종료
                    이후는 스레드 내에서 처리
                    */

                    threadPool.submit(new ConnectionWrap(socket));
                }catch(Exception e){
                    e.printStackTrace();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            threadPool.shutdown();
        }
    }
}

// 소켓 처리용 래퍼 클래스
class ConnectionWrap implements Runnable{

    private Socket socket = null;
    BufferedReader inputBuffer;
    BufferedWriter outputBuffer;

    public ConnectionWrap(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {

        try {
            inputBuffer = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            outputBuffer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                socket.close(); // 반드시 종료합니다.
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        while(true)
        {
            Onreceive();
        }
    }

    public void Onreceive()
    {
        try {
            String msg = inputBuffer.readLine();

            /*
            ParseAndExcuteData();

            output
             */

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
