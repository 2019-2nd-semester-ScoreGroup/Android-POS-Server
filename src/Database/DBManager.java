package Database;

import Data.Change;
import Data.Event;
import Data.Stock;

import java.sql.*;
import java.sql.ResultSet;

public class DBManager {
    public boolean isNoisy() {
        return noisy;
    }

    public void setNoisy(boolean noisy) {
        this.noisy = noisy;
    }

    boolean noisy=false;
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

    public ResultSet editStock(Stock tstock) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("INSERT INTO tstock VALUES ('%s', '%s', %d) ON DUPLICATE KEY UPDATE sname='%s',sprice=%d;", tstock.getKey(), tstock.getName(), tstock.getPrice(), tstock.getName(), tstock.getPrice());
        ResultSet ret = executeQuery(msg, stat);
        commit(con);
        close(stat);
        close(con);
        return ret;
    }

    public ResultSet getStock(String key) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        ResultSet ret = executeQuery("SELECT * FROM tstock", stat);
        commit(con);
        close(stat);
        close(con);
        return ret;
    }

    public long addEvent(Event tevent) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("INSERT INTO tevent (etype,etime,estatus,ememo) VALUES (%d, '%s', %d,'%s');", tevent.getType(), tevent.getTime().toString(), tevent.getStatus(), tevent.getMemo());
        executeQuery(msg, stat);
        ResultSet ret = executeQuery("SELECT LAST_INSERT_ID();", stat);
        long retKey = 0;
        try {
            ret.first();
            retKey = ret.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (Change c : tevent.getData()) {
            c.setEventKey(retKey);
            addChange(c, stat);
        }


        commit(con);
        close(stat);
        close(con);
        tevent.setKey(retKey);
        return retKey;
    }

    public ResultSet cancelSetEvent(Event tevent) {
        return cancelSetEvent(tevent.getKey(), tevent.getStatus());
    }

    private ResultSet cancelSetEvent(long key, byte status) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("update tevent set estatus=1 where ekey='%d';", key);
        ResultSet ret = executeQuery(msg, stat);
        commit(con);
        close(stat);
        close(con);
        return ret;

    }

    private long addChange(Change tchange, Statement stat) {
        String msg = String.format("INSERT INTO tchange (cevent,cstock,cnumber) VALUES (%d,'%s',%d);", tchange.getEventKey(), tchange.getStockKey(), tchange.getAmount());
        executeQuery(msg, stat);
        ResultSet ret = executeQuery("SELECT LAST_INSERT_ID();", stat);
        long retKey = 0;
        try {
            ret.first();
            retKey = ret.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tchange.setKey(retKey);
        return retKey;
    }

    private Connection getConnection() {
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
            if(noisy)System.out.println("Connected");
        } catch (SQLException e) {
            System.err.println("con 오류:" + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    private Statement getStatement(Connection con) {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    private ResultSet executeQuery(String query, Statement stat) {

        try {
            if(noisy)System.out.println(String.format("executing : %s", query));
            ResultSet ret = stat.executeQuery(query);
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return null;
    }

    private void commit(Connection con) {
        try {
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void close(Statement con) {
        // 3.해제
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {
        }
    }

    private void close(Connection con) {
        // 3.해제
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {
        }
    }

    private void initialize() {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        executeQuery("CREATE TABLE IF NOT EXISTS `tstock` (`skey` VARCHAR(25) NOT NULL,`sname` VARCHAR(50) NULL DEFAULT NULL, `sprice` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`skey`)) COLLATE='utf8_general_ci' ENGINE=InnoDB;", stat);
        executeQuery("CREATE TABLE IF NOT EXISTS `tevent` ( `ekey` BIGINT(20) NOT NULL AUTO_INCREMENT, `etype` TINYINT(4) NOT NULL DEFAULT 0, `etime` DATETIME NOT NULL, `estatus` TINYINT(4) NOT NULL DEFAULT 0, `ememo` VARCHAR(200) NULL DEFAULT NULL, PRIMARY KEY (`ekey`))COLLATE='utf8_general_ci'ENGINE=InnoDB;", stat);
        executeQuery("CREATE TABLE IF NOT EXISTS `tchange` ( `ckey` BIGINT(20) NOT NULL AUTO_INCREMENT, `cevent` BIGINT(20) NULL DEFAULT NULL, `cstock` VARCHAR(25) NULL DEFAULT NULL, `cnumber` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`ckey`), INDEX `tstock` (`cstock`), INDEX `tevent` (`cevent`), CONSTRAINT `tevent` FOREIGN KEY (`cevent`) REFERENCES `tevent` (`ekey`), CONSTRAINT `tstock` FOREIGN KEY (`cstock`) REFERENCES `tstock` (`skey`) ) COLLATE='utf8_general_ci' ENGINE=InnoDB;", stat);
        commit(con);
        close(stat);
        close(con);

    }

    public ResultSet getStocks(){
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg="SELECT sname,SUM(cnumber) FROM tchange JOIN tstock ON tchange.cstock=tstock.skey GROUP BY skey;";
        if(noisy)System.out.println(msg);
        ResultSet ret=null;
        try {
            ret=stat.executeQuery(msg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        commit(con);
        close(stat);
        close(con);
        return ret;
    }
}
