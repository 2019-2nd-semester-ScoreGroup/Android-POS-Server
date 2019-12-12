package Manager;

/*
    change : 한 stock에 대한 변화 기록
    Event : change들로 이루어진 하나의 사건
    EventList : event들로 이루어진 리스트
    stock : 재고의 키, 품명, 가격, 수량을 가지고 있는 클래스
 */
import Data.Change;
import Data.Event;
import Data.EventList;
import Data.Stock;

/*
    데이터베이스 매니저
    DB에 query 후 result 반환
 */
import Database.DBManager;

// yyyy-MM-dd hh:mm:ss 형식 (year, month, day, hour, minute, second)
import java.sql.Timestamp;

// String 변수를 Timestamp로 변환하기 위해 사용
import java.text.SimpleDateFormat;

// 날짜 계산
import java.util.Date;

// cui 입력
import java.util.Scanner;

// String을 데이터 단위로 끊기 위해 사용 split()과 차이점 주의
import java.util.StringTokenizer;

/*
    네트워크로 전달된 메시지를 파싱하여 디비매니저를 호출 후, 반환
 */
public class ServerController {

    private NetworkManager networkManager;
    private DBManager dbManager;
    private Scanner scanner;
    private static ServerController serverController;

    private ServerController() { }

    public ServerController getInstance()
    {
        if(serverController == null)
            serverController = new ServerController();

        return serverController;
    }

    /*
    초기화
    NetworkManager 스레드 생성
     */
    private void initialize() {
        new Thread(() -> {
            networkManager = new NetworkManager(this);
        }).start();

        dbManager = new DBManager("localhost", "androidpos", "root", "1234");
        scanner = new Scanner(System.in);
    }

    public static void main(String[] args) {
        serverController = new ServerController();

        serverController.initialize();

        serverController.run();
    }

    /*
        종료하기 위한 cui
        종료는 exit
        새로운 스레드를 생성하여 입력받기를 대기하고
        메인 스레드는 Thread.sleep문 반복하며 대기
     */
    private void run() {
        //TODO 상훈이 코드 연결
        System.out.println("Point of Sales System");

        for (int i = 0; i < 30; i++)
            System.out.print("-");

        System.out.println("");

        new Thread(() ->
        {
            String s = "";

            while (!s.equals("exit")) {
                System.out.println("exit code is 'exit'\n");

                s = scanner.nextLine();

                if (s.equals("exit")) {
                    System.out.println("close...");
                    scanner.close();
                    System.exit(0);
                }
            }
        }).start();

        while (true) {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**DB와 통신을 위한 메소드
     * Network Manager에서 받은 소켓에서 보내는 데이터를 해석하고 DBManager를 호출한 후, ack를 반환
     * 인수와 반환값은 String
     * 인수마다 _로 구분
     * []안의 값은 대치되어야 함
     * ( A, B ) 는 A or B
     * 인수가 포맷을 따르지 않을 경우 invalid syntax
     * 인수안에 필요한 데이터가 없으면 해당 데이터를 요구하는 스트링 반환                                                                 ex) "input key"
     * DBManager의 리턴이 null이면 null반환
     * @param "editStock" + " " + [key] + " " + [name] + " " + [price]                                                              ex) "editStock_1_alpha_100"
     * @return  (true, false) : 성공, 실패                                                                                           ex) "true"
     * @param "getStock" + [key]                                                                                                    ex) "getStock_1"
     * @return [key] [name] [price]                                                                                                 ex) "1_alpha_100"
     * @param "getStocks"                                                                                                           ex) "getStocks"
     * @return [key] [name] [price],[key] [name] [price] [amount] ,... : stock마다 ,로 구분된 띄워쓰기로 연결된 문자열                  ex) "1_alpha_100, 2_bravo_200,"
     * @param "getEvent" + " " + [eventKey]                                                                                         ex) "getEvent 2"
     * @return [type] [time] [memo] [c.stockKey] [c.amount] [c.eventKey] + [c.key],... : change마다 ,로 구분된 띄워쓰기로 연결된 문자열 ex) "DELIVERY_2019-12-05_14:40:31.0_delivering_1_1_2_6,2_1_2_7,3_1_2_8,4_1_2_9,"
     * @param "tryChangEvent" + " " + [eventKey] + " " + [status] : status(0 Normal, 1 Cancel, 2 NaN)                               ex) "tryChangeEvent_1_0"
     * @return (true, false) : 성공, 실패                                                                                            ex) "false"
     * @param "getEventList" + " " + [type] : type(1 Sell, 2 delivery, 3 NaN)                                                       ex) "getEventList_1"
     * @return [key] [time] [totalPrice],... : event마다 ,로 구분된 띄워쓰기로 연결된 문자열                                            ex) "2_2019-12-05_14:40:31.0_1000,"
     * @param "getSelling" + " " + [startTime] + " " + [endTime] : startTime, endTime은 타임스탬프 형식(yyyy-MM-dd hh:mm:ss)          ex) "getSelling_2019-11-1_2019-12-17"
     * @return [key] [name] [price] [amount],... : startTime과 endTime 사이에 판매된 stock마다 ,로 구분된 띄워쓰기로 연결된 문자열        ex) "1_alpha_100_-2,2_bravo_200_-7,3_charlie_300_-1,5_echo_500_-1,6_foxtrot_600_-1,"
     * @param "addEvent" + " " + [type] + " " + [time] + " " + [status] + " " + [memo]                                              ex) "addEvent_2_2019-12-07_14:00:26.443_0_delivering"
     * @return [eventKey]                                                                                                           ex) "4"
     * @param "addChange" + " " + [eventKey] + " " + [stockKey] + " " + [changedAmount]                                             ex) "addChange_4_1_1"
     * @return ([changeKey], false)                                                                                                 ex) "12"
     */
    public String parseAndExecuteData(String networkMsg) {

        String delim = "_";

        //null 입력 받을 시 null 반환
        if(networkMsg==null)
            return "input error";

        StringTokenizer stringTokenizer;
        stringTokenizer = new StringTokenizer(networkMsg, delim);
        String opcode = stringTokenizer.nextToken();

        try {
            if (opcode.equals("editStock")) {
                if (!stringTokenizer.hasMoreTokens())
                    return "input key";
                String key = stringTokenizer.nextToken();

                if (!stringTokenizer.hasMoreTokens())
                    return "input name";
                String name = stringTokenizer.nextToken();

                if (!stringTokenizer.hasMoreTokens())
                    return "input price";
                int price = Integer.parseInt(stringTokenizer.nextToken());

                return Boolean.toString(dbManager.editStock(new Stock(key, name, price)));
            } else if (opcode.equals("getStock")) {
                String key = stringTokenizer.nextToken();
                Stock stock = dbManager.getStock(key);
                return stock != null ? dbManager.getStock(key).toString(0) : null;
            } else if (opcode.equals("getStocks")) {
                String ackMsg = "";
                Stock[] stocks = dbManager.getStocks();

                if(stocks == null)
                    return null;

                for (Stock s : stocks) {
                    ackMsg = ackMsg + s.toString() + ",";
                }

                return ackMsg;
            } else if (opcode.equals("getEvent")) {
                if (!stringTokenizer.hasMoreTokens())
                    return "input key";

                Long key = toLong(stringTokenizer.nextToken());
                Event event = dbManager.getEvent(key);

                return event != null ? event.toString(0) : "null";
            } else if (opcode.equals("addChange")) {
                if (!stringTokenizer.hasMoreTokens())
                    return "input eventKey";
                String eventKey = stringTokenizer.nextToken();

                if (!stringTokenizer.hasMoreTokens())
                    return "input stockKey";
                String stockKey = stringTokenizer.nextToken();

                if (!stringTokenizer.hasMoreTokens())
                    return "input changedAmount";
                String changedAmount = stringTokenizer.nextToken();

                Change change = new Change(stockKey, toInt(changedAmount));
                change.setEventKey(toLong(eventKey));

                Long ack = dbManager.addChange(change);

                if(ack == -1)
                    return "false";

                return Long.toString(dbManager.addChange(change));
            } else if (opcode.equals("tryChangeEvent")) {
                if (!stringTokenizer.hasMoreTokens())
                    return "input key";
                Long key = toLong(stringTokenizer.nextToken());

                if (!stringTokenizer.hasMoreTokens())
                    return "status";
                Byte status = toByte(stringTokenizer.nextToken());

                return Boolean.toString(dbManager.tryChangeEvent(key, status));
            } else if (opcode.equals("getEventList")) {
                if (!stringTokenizer.hasMoreTokens())
                    return "type";
                Byte type = toByte(stringTokenizer.nextToken());

                EventList[] event = dbManager.getEventList(type);
                String ackMsg = "";

                for (EventList e : event) {
                    ackMsg = ackMsg.concat(e.toString() + ",");
                }

                return ackMsg;
            } else if (opcode.equals("getSelling")) {

                if (!stringTokenizer.hasMoreTokens())
                    return "input start time";
                String startTime = stringTokenizer.nextToken() + " 00:00:00";
                Timestamp startTimeStamp = null;

                if (!stringTokenizer.hasMoreTokens())
                    return "input end time";
                String endTime = stringTokenizer.nextToken() + " 00:00:00";
                Timestamp endTimeStamp = null;

                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    Date parsedDate = dateFormat.parse(startTime);
                    startTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
                    parsedDate = dateFormat.parse(endTime);
                    endTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Stock[] statistickList;

                if(stringTokenizer.hasMoreTokens())
                {
                    byte type = toByte(stringTokenizer.nextToken());
                    statistickList = dbManager.getSelling(startTimeStamp, endTimeStamp, type);
                }
                else
                    statistickList = dbManager.getSelling(startTimeStamp, endTimeStamp);

                String ackMsg = "";
                for (int i = 0; i < statistickList.length; i++) {
                    ackMsg = ackMsg.concat(statistickList[i].toString());
                    ackMsg = ackMsg.concat(",");
                }
                return ackMsg;
            } else if (opcode.equals("addEvent")) {
                if (!stringTokenizer.hasMoreTokens())
                    return "input type";
                Byte type = toByte(stringTokenizer.nextToken());

                if (!stringTokenizer.hasMoreTokens())
                    return "input time";
                String time = stringTokenizer.nextToken();

                if (!stringTokenizer.hasMoreTokens())
                    return "invalid time";
                time = time.concat("" + stringTokenizer.nextToken());

                Timestamp timestamp = null;
                try {
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                    Date parsedDate = dateFormat.parse(time);
                    timestamp = new java.sql.Timestamp(parsedDate.getTime());
                } catch (Exception e) {
                    e.printStackTrace();
                }

                if (!stringTokenizer.hasMoreTokens())
                    return "input status";
                Byte status = toByte(stringTokenizer.nextToken());

                if (!stringTokenizer.hasMoreTokens())
                    return "input memo";
                String memo = stringTokenizer.nextToken();

                Event event = new Event(type, timestamp, memo);
                event.setStatus(status);
                return Long.toString(dbManager.addEvent(event));
            } else
                return "invalid syntax";
        } catch ( Exception e)
        {
            e.printStackTrace();
            return "unexpected ack";
        }
    }

    private Byte toByte(String str) {
        try {
            return Byte.parseByte(str);
        } catch (NumberFormatException e) {
            //NaN
            return 3;
        }
    }

    private long toLong(String str) {
        try {
            return Long.parseLong(str);
        } catch (NumberFormatException e) {
            return 999;
        }
    }

    private int toInt(String str)
    {
        try{
            return Integer.parseInt(str);
        } catch (NumberFormatException e)
        {
            return 999;
        }
    }
}
