package Manager;

import Data.Stock;
import Database.DBManager;

import java.sql.ResultSet;

public class ServerController {
    public static void main(String args[]){
        System.out.println("Hello");
        DBManager dbManager=new DBManager();
        dbManager.editStock("12345","개밥",1500);
        ResultSet set=dbManager.getStock("12345");
        System.out.println(Stock.BuildStock(set));


    }

}
