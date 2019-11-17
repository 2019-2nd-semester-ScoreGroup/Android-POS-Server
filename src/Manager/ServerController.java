package Manager;

import Data.Change;
import Data.Event;
import Data.Stock;
import Database.DBManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Random;

public class ServerController {
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
         ResultSet set=dbManager.getStocks();
         try{
             set.first();
             do{
                 System.out.println(String.format("%s : %d",set.getString(1),set.getInt(2)));
             }
             while(set.next());
         }catch(SQLException e){
             e.printStackTrace();
         }



    }

}
