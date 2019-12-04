package Manager;

import Data.Change;
import Data.Event;
import Data.EventList;
import Data.Stock;
import Database.DBManager;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;
import java.util.StringTokenizer;


public class ServerController {

    private NetworkManager networkManager;
    private DBManager dbManager;
    private Scanner scanner = new Scanner(System.in);

    /*
    초기화
    NetworkManager 스레드 생성
     */
    public void initialize()
    {
        new Thread(()->{
            networkManager = new NetworkManager(this);
        }).start();

        dbManager = new DBManager("localhost", "androidpos", "root", "1234");
    }

    public static void main(String[] args) {
        ServerController serverController = new ServerController();

        serverController.initialize();

        serverController.run();
    }

    private void run()
    {
        //종료하기 위한 cui
        System.out.println("Point of Sales System");

        for(int i = 0; i < 30; i++)
            System.out.print("-");

        System.out.println("");

        new Thread(()->
        {
            String s = "";

            while(!s.equals("exit"))
            {
                System.out.println("exit code is 'exit'\n");

                s =scanner.nextLine();

                if(s.equals("exit"))
                {
                    System.out.println("close...");
                    scanner.close();
                    System.exit(0);
                }
            }
        }).start();

        while(true)
        {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 현재 "getStocks" 작동 확인
     * Network Manager에서 받은 소켓에서 보내는 데이터를 해석하고 DBManager를 호출한 후, ack를 반환
     * 인수와 반환값은 String
     * []안의 값은 대치되어야 함
     * @param networkMsg "editStock" + " " + [key] + " " + [name] + " " + [price]
     * @return  true or false : 성공, 실패
     * @param networkMsg "getStock"
     * @return [key] [name] [price] : 띄워쓰기로 연결된 문자열
     * @param networkMsg "getStocks"
     * @return [key] [name] [price],[key] [name] [price] [amount] ,... : stock마다 ,로 구분된 띄워쓰기로 연결된 문자열
     * @param networkMsg "getEvent" + " " + [eventKey]
     * @return [type] [time] [memo] [c.stockKey] [c.amount] [c.eventKey] + [c.key],... : change마다 ,로 구분된 띄워쓰기로 연결된 문자열
     * @param networkMsg "tryChangEvent" + " " + [eventKey] + " " + [status] : status(0 Normal, 1 Cancel, 2 NaN)
     * @return true or false : 성공, 실패
     * @param networkMsg "getEventList" + " " + [type] : type(1 Sell, 2 delivery, 3 NaN)
     * @return [key] [type] [totalPrice],... : event마다 ,로 구분된 띄워쓰기로 연결된 문자열
     * @param networkMsg "getSelling" + " " + [startTime] + " " + [endTime] : startTime, endTime은 타임스탬프 형식(yyyy-MM-dd hh:mm:ss)
     * @return [key] [name] [price] [amount],... : startTime과 endTime 사이에 판매된 stock마다 ,로 구분된 띄워쓰기로 연결된 문자열
     * @param networkMsg "addEvent" + " " + [type] + " " + [time] + " " + [status] + " " + [memo]
     * @return [eventKey]
     * @param networkMsg "addChange" + " " + [eventKey] + " " + [stockKey] + " " + [changedAmount]
     * @return [eventKey]
     */
    public String parseAndExecuteData(String networkMsg)
    {
        StringTokenizer stringTokenizer;
        stringTokenizer = new StringTokenizer(networkMsg, ",");
        String opcode = stringTokenizer.nextToken();

        if(opcode.equals("editStock"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input key";
            String key = stringTokenizer.nextToken();

            if(!stringTokenizer.hasMoreTokens())
                return "input name";
            String name = stringTokenizer.nextToken();

            if(!stringTokenizer.hasMoreTokens())
                return "input price";
            int price = Integer.parseInt(stringTokenizer.nextToken());

            return Boolean.toString(dbManager.editStock(new Stock(key, name, price)));
        }
        else if(opcode.equals("getStock"))
        {
            String key = stringTokenizer.nextToken();

            return dbManager.getStock(key).toString(0);
        }
        else if(opcode.equals("getStocks"))
        {
            String ackMsg = "";
            Stock []stocks = dbManager.getStocks();
            for(Stock s : stocks)
            {
                ackMsg = ackMsg + s.toString() + ",";
            }
            return ackMsg;
        }
        else if(opcode.equals("getEvent"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input key";
            Long key = toLong(stringTokenizer.nextToken());
            return dbManager.getEvent(key).toString(0);
        }
        /*else if(opcode.equals("addChange"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input eventKey";
            String eventKey = stringTokenizer.nextToken();
            if(!stringTokenizer.hasMoreTokens())
                return "input stockKey";
            String stockKey = stringTokenizer.nextToken();
            if(!stringTokenizer.hasMoreTokens())
                return "input changedAmount";
            String changedAmount = stringTokenizer.nextToken();

            Change change = new Change(stockKey, (int)toLong(changedAmount));

            return Long.toString(dbManager.addChange(change));
        }*/
        else if(opcode.equals("tryChangeEvent"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input key";
            Long key = toLong(stringTokenizer.nextToken());
            if(!stringTokenizer.hasMoreTokens())
                return "status";
            Byte status = toByte(stringTokenizer.nextToken());

            return Boolean.toString(dbManager.tryChangeEvent(key, status));
        }
        else if(opcode.equals("getEventList"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "type";
            Byte type = toByte(stringTokenizer.nextToken());
            EventList[] event = dbManager.getEventList(type);
            String ackMsg = null;

            for(EventList e : event)
            {
                ackMsg.concat(e.toString());
                ackMsg.concat(",");
            }

            return ackMsg;
        }
        else if(opcode.equals("getSelling"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input start time";
            String startTime = stringTokenizer.nextToken();
            if(!stringTokenizer.hasMoreTokens())
            startTime += stringTokenizer.nextToken();
            Timestamp startTimeStamp = null;

            if(!stringTokenizer.hasMoreTokens())
                return "input end time";
            String endTime = stringTokenizer.nextToken();
            if(!stringTokenizer.hasMoreTokens())
            endTime += stringTokenizer.nextToken();
            Timestamp endTimeStamp = null;

            try{
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                Date parsedDate = dateFormat.parse(startTime);
                startTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
                parsedDate = dateFormat.parse(endTime);
                endTimeStamp = new java.sql.Timestamp(parsedDate.getTime());
            }catch(Exception e){
                e.printStackTrace();
            }
            Stock[] statistickList = dbManager.getSelling(startTimeStamp, endTimeStamp);

            String ackMsg = null;
            for(int i = 0; i < statistickList.length; i++)
            {
                ackMsg.concat(statistickList[i].toString());
                ackMsg.concat(",");
            }
            return ackMsg;
        }
        else if(opcode.equals("addEvent"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input type";
            Byte type = toByte(stringTokenizer.nextToken());

            if(!stringTokenizer.hasMoreTokens())
                return "input time";
            String time = stringTokenizer.nextToken();
            Timestamp timestamp = null;
            try{
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss.SSS");
                Date parsedDate = dateFormat.parse(time);
                timestamp = new java.sql.Timestamp(parsedDate.getTime());
            }catch(Exception e){
                e.printStackTrace();
            }

            if(!stringTokenizer.hasMoreTokens())
                return "input memo";
            String memo = stringTokenizer.nextToken();

            if(!stringTokenizer.hasMoreTokens())
                return "input status";
            Byte status = toByte(stringTokenizer.nextToken());

            Event event = new Event(type, timestamp, memo);
            event.setStatus(status);
            return Long.toString(dbManager.addEvent(event));
        }
        else
            return "invalid syntax";
    }

    private Byte toByte(String str)
    {
        try
        {
            return Byte.parseByte(str);
        }
        catch(NumberFormatException e)
        {
            //NaN
            return 3;
        }
    }

    private long toLong(String str)
    {
        try
        {
            return Long.parseLong(str);
        }
        catch(NumberFormatException e)
        {
            return 3;
        }
    }
}
