package Manager;

import Data.EventList;
import Data.Stock;
import Database.DBManager;

import java.sql.Timestamp;

import java.util.Random;

public class MainForDB {
    public static void main(String args[]){
        Random rand=new Random();
        DBManager dbManager=new DBManager();

        dbManager.editStock(new Stock("1","alpha",100));
        dbManager.editStock(new Stock("2","bravo",200));
        dbManager.editStock(new Stock("3","charlie",300));
        dbManager.editStock(new Stock("4","delta",400));
        dbManager.editStock(new Stock("5","echo",500));

        //TODO 품목수정
        dbManager.editStock(new Stock("6","foxtrot",600));

        //TODO 내역 추가
        /*
        Event event=new Event(Event.TYPE_SELL, Timestamp.valueOf(LocalDateTime.now()),"Testing");
        for(int i=0;i<6;i++){
            Change temp=new Change(""+(i+1),rand.nextInt(100));
            event.getData().add(temp);
        }
        dbManager.addEvent(event);
        */
        //dbManager.setNoisy(true);
        System.out.println(dbManager.getEvent(2));
        for(EventList t :dbManager.getEventList(DBManager.TYPE_SELL)){
            System.out.println(t.toString());
        }
        System.out.println();
        System.out.println("2019-11-15 11:27:30 ~ 2019-12-15 11:27:30 거래기록");
        for(Stock t : dbManager.getSelling(Timestamp.valueOf("2019-11-15 11:27:30"),Timestamp.valueOf("2019-12-15 11:27:30"))){
            System.out.println(t.toString());
        }
    }

}
