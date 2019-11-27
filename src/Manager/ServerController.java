package Manager;

import Data.Event;
import Data.EventList;
import Data.Stock;
import Database.DBManager;

import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.StringTokenizer;

//판매리스트, 기록 리스트, 상세 기록은 객체 출력 고려
public class ServerController {

    private NetworkManager networkManager;
    private DBManager dbManager;

    public void initialize()
    {
        new Thread(()->{
            networkManager = new NetworkManager(this);
        }).start();

        dbManager = new DBManager("localhost", "androidpos", "root", "15937456");
    }

    public static void main(String[] args) {
        ServerController serverController = new ServerController();

        serverController.initialize();

        serverController.run();
    }

    private void run()
    {
        while(true)
        {
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

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

            return dbManager.getStock(key).toString();
        }
        else if(opcode.equals("getStocks"))
        {
            String ackMsg = "";
            Stock []stocks = dbManager.getStocks();
            for(int i = 0; i < stocks.length; i++)
            {
                ackMsg = ackMsg + stocks[i].toString() + ",";
            }
            return ackMsg;
        }
        else if(opcode.equals("getEvent"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input key";
            Long key = toLong(stringTokenizer.nextToken());
            return dbManager.getEvent(key).toString();
        }
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
            for(int i =0; i<event.length;i++)
            {
                ackMsg.concat(event[i].toString());
                ackMsg.concat(",");
            }
            return ackMsg;
        }
        else if(opcode.equals("getSelling"))
        {
            if(!stringTokenizer.hasMoreTokens())
                return "input start time";
            String startTime = stringTokenizer.nextToken();
            Timestamp startTimeStamp = null;

            if(!stringTokenizer.hasMoreTokens())
                return "input end time";
            String endTime = stringTokenizer.nextToken();
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
                ackMsg.concat(", ");
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
