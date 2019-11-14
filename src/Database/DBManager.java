package Database;

import Data.Change;
import Data.Event;
import Data.Stock;

import java.sql.*;
import java.sql.ResultSet;

public class DBManager {

    String server = "localhost"; // MySQL 서버 주소
    String database = "AndroidPOS"; // MySQL DATABASE 이름
    String user_name = "root"; //  MySQL 서버 아이디
    String password = "root"; // MySQL 서버 비밀번호
    ConnectionBuilder builder;
    public DBManager() {
        initialize();
    }

    public DBManager(String IP, String DB, String ID, String PW) {
        server = IP; // MySQL 서버 주소
        database = DB; // MySQL DATABASE 이름
        user_name = ID; //  MySQL 서버 아이디
        password = PW; // MySQL 서버 비밀번호
        initialize();
    }
    public ResultSet editStock(Stock stock){
        String msg = String.format("INSERT INTO stock VALUES ('%s', '%s', %d) ON DUPLICATE KEY UPDATE sname='%s',sprice=%d;", stock.getKey(), stock.getName(), stock.getPrice(),stock.getName(),stock.getPrice());
        return executeQuery(msg);
    }

    public ResultSet getStock(String key){
        return executeQuery("SELECT * FROM stock");
    }

    public ResultSet addEvent(Event event){
        String msg = String.format("INSERT INTO event (etype,etime,estatus,ememo) VALUES (%d, '%s', %d,%s);", event.getKey(), event.getTime().toString(), event.getStatus(),event.getMemo());
        for(Change c : event.getData()){

        }

        return executeQuery(msg);
    }
    private ResultSet addChange(Change change){
        String msg = String.format("INSERT INTO event (cevent,cstock,cnumber) VALUES (%d,'%s',%d",change.getEventKey(),change.getStockKey(),change.getAmount());
        return executeQuery(msg);
    }

    private ResultSet executeQuery(String query){
        Connection con = null;
        // 1.드라이버 로딩
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println(" !! <JDBC 오류> Driver load 오류: " + e.getMessage());
            e.printStackTrace();
        }

        // 2.연결
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database + "?useSSL=false", user_name, password);
            System.out.println("Connected");
        } catch (SQLException e) {
            System.err.println("con 오류:" + e.getMessage());
            e.printStackTrace();
        }
        try {
            System.out.println(String.format("executing : %s",query));
            ResultSet ret=con.createStatement().executeQuery(query);
            con.commit();
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        // 3.해제
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {
        }
        return null;
    }

    private void initialize() {
        executeQuery("CREATE TABLE IF NOT EXISTS `stock` (`skey` VARCHAR(25) NOT NULL,`sname` VARCHAR(50) NULL DEFAULT NULL, `sprice` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`skey`)) COLLATE='utf8_general_ci' ENGINE=InnoDB;");
        executeQuery("CREATE TABLE IF NOT EXISTS `event` ( `ekey` BIGINT(20) NOT NULL AUTO_INCREMENT, `etype` TINYINT(4) NOT NULL DEFAULT 0, `etime` DATETIME NOT NULL, `estatus` TINYINT(4) NOT NULL DEFAULT 0, `ememo` VARCHAR(200) NULL DEFAULT NULL, PRIMARY KEY (`ekey`))COLLATE='utf8_general_ci'ENGINE=InnoDB;");
        executeQuery("CREATE TABLE IF NOT EXISTS `change` ( `ckey` BIGINT(20) NOT NULL AUTO_INCREMENT, `cevent` BIGINT(20) NULL DEFAULT NULL, `cstock` VARCHAR(25) NULL DEFAULT NULL, `cnumber` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`ckey`), INDEX `stock` (`cstock`), INDEX `event` (`cevent`), CONSTRAINT `event` FOREIGN KEY (`cevent`) REFERENCES `event` (`ekey`), CONSTRAINT `stock` FOREIGN KEY (`cstock`) REFERENCES `stock` (`skey`) ) COLLATE='utf8_general_ci' ENGINE=InnoDB;");
    }
}
