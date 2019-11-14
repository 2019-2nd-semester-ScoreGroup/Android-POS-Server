package Manager;

import Data.*;
import Data.Stock;
import Database.DBManager;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

public class ServerController {
    public static void main(String args[]){

        DBManager dbManager=new DBManager();
        dbManager.editStock(new Stock("12345","개밥",1500));
        ResultSet set=dbManager.getStock("12345");
        try {
            set.first();
            Stock ret = new Stock(set.getString(1),set.getString(2),set.getInt(3));
            System.out.println(ret);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

}
