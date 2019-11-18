package Database;

import Data.Change;
import Data.Event;
import Data.EventList;
import Data.Stock;

import java.sql.*;
import java.util.ArrayList;

public class DBManager {
    public static byte STATUS_NORMAL = 0, STATUS_CANCELED = 1, TYPE_SELL = 1, TYPE_DELIVERY = 2;

    public boolean isNoisy() {
        return noisy;
    }

    public void setNoisy(boolean noisy) {
        this.noisy = noisy;
    }

    boolean noisy = false;
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
        ResultSet ret = null;
        try {
            ret = stat.executeQuery(msg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        commit(con);
        close(stat);
        close(con);
        return ret;
    }

    public ResultSet getStock(String key) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        ResultSet ret = null;
        try {
            ret = stat.executeQuery("SELECT * FROM tstock");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        commit(con);
        close(stat);
        close(con);
        return ret;
    }

    public long addEvent(Event tevent) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("INSERT INTO tevent (etype,etime,estatus,ememo) VALUES (%d, '%s', %d,'%s');", tevent.getType(), tevent.getTime().toString(), tevent.getStatus(), tevent.getMemo());
        ResultSet ret = null;
        try {
            stat.executeQuery(msg);
            ret = stat.executeQuery("SELECT LAST_INSERT_ID();");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        long retKey = 0;
        try {
            ret.first();
            retKey = ret.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }


        for (Change c : tevent.getData()) {
            c.setEventKey(retKey);
            addChange(c);
        }
        commit(con);
        close(stat);
        close(con);
        tevent.setKey(retKey);
        return retKey;
    }

    private long addChange(Change tchange) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("INSERT INTO tchange (cevent,cstock,cnumber) VALUES (%d,'%s',%d);", tchange.getEventKey(), tchange.getStockKey(), tchange.getAmount());
        long retKey = 0;
        try {
            stat.executeQuery(msg);
            ResultSet ret = stat.executeQuery("SELECT LAST_INSERT_ID();");
            ret.first();
            retKey = ret.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        tchange.setKey(retKey);
        close(stat);
        close(con);
        return retKey;
    }

    public Stock[] getStocks() {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = "SELECT skey,sname,sprice,SUM(cnumber) AS samount FROM tchange JOIN tstock ON tchange.cstock=tstock.skey GROUP BY skey;";
        Stock[] result = null;
        log(msg);
        ResultSet ret = null;
        try {
            ret = stat.executeQuery(msg);

            commit(con);
            close(stat);
            close(con);
            ret.last();
            result = new Stock[ret.getRow()];
            ret.first();
            int index = 0;
            do {
                result[index] = new Stock(ret.getString("skey"), ret.getString("sname"), ret.getInt("sprice"));
                result[index].setAmount(ret.getInt("samount"));
                index++;
            } while (ret.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return result;
    }

    public Event getEvent(long key) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        ResultSet ret = null;
        Event result = null;
        String msg = "SELECT * FROM (tevent JOIN tchange ON tchange.cevent=tevent.ekey) WHERE tevent.ekey=%d;";
        try {
            ret = stat.executeQuery(String.format(msg, key));
            ret.first();
            result = new Event(ret.getByte("etype"), ret.getTimestamp("etime"), ret.getString("ememo"));
            result.setData(new ArrayList<Change>());
            ArrayList<Change> data = result.getData();
            do {
                Change t = new Change(ret.getString("cstock"), ret.getInt("cnumber"));
                t.setEventKey(ret.getLong("cevent"));
                t.setKey(ret.getLong("ckey"));
                data.add(t);
            }
            while (ret.next());
            log(msg);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        commit(con);
        close(stat);
        close(con);
        return result;
    }

    public boolean tryCancelEvent(long key, byte status) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        ResultSet ret = null;
        boolean result = false;
        String msg = "update tevent set estatus=%d where ekey=%d";
        try {
            ret = stat.executeQuery(String.format(msg, status, key));
            result = true;
        } catch (SQLException e) {
            e.printStackTrace();
            result = false;
        } finally {
            commit(con);
            close(stat);
            close(con);
            return result;
        }
    }

    public EventList[] getEventList(byte type) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        ResultSet ret = null;
        EventList[] result = null;
        String msg = "SELECT ekey,etime, SUM(sprice*cnumber) as total FROM (tevent JOIN (tchange JOIN tstock ON tchange.cstock=tstock.skey) ON tchange.cevent=tevent.ekey)WHERE etype=%d GROUP BY eKey;";
        try {
            ret = stat.executeQuery(String.format(msg, type));
            ret.last();
            result = new EventList[ret.getRow()];
            ret.first();
            int index = 0;
            do {
                EventList t = new EventList();
                t.setKey(ret.getLong("ekey"));
                t.setTime(ret.getTimestamp("etime"));
                t.setTotalPrice(ret.getInt("total"));
                result[index] = t;
                index++;
            } while (ret.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        commit(con);
        close(stat);
        close(con);
        return result;
    }

    private void log(String msg) {
        if (noisy) System.out.println(msg);
    }

    private Connection getConnection() {
        Connection con = null;
        // 1.드라이버 로딩
        try {
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            log(" !! <JDBC 오류> Driver load 오류: " + e.getMessage());
            e.printStackTrace();
        }

        // 2.연결
        try {
            con = DriverManager.getConnection("jdbc:mysql://" + server + "/" + database + "?useSSL=false", user_name, password);
            log("Connected");
        } catch (SQLException e) {
            log("con 오류:" + e.getMessage());
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
        try {
            stat.executeQuery("CREATE TABLE IF NOT EXISTS `tstock` (`skey` VARCHAR(25) NOT NULL,`sname` VARCHAR(50) NULL DEFAULT NULL, `sprice` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`skey`)) COLLATE='utf8_general_ci' ENGINE=InnoDB;");
            stat.executeQuery("CREATE TABLE IF NOT EXISTS `tevent` ( `ekey` BIGINT(20) NOT NULL AUTO_INCREMENT, `etype` TINYINT(4) NOT NULL DEFAULT 0, `etime` DATETIME NOT NULL, `estatus` TINYINT(4) NOT NULL DEFAULT 0, `ememo` VARCHAR(200) NULL DEFAULT NULL, PRIMARY KEY (`ekey`))COLLATE='utf8_general_ci'ENGINE=InnoDB;");
            stat.executeQuery("CREATE TABLE IF NOT EXISTS `tchange` ( `ckey` BIGINT(20) NOT NULL AUTO_INCREMENT, `cevent` BIGINT(20) NULL DEFAULT NULL, `cstock` VARCHAR(25) NULL DEFAULT NULL, `cnumber` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`ckey`), INDEX `tstock` (`cstock`), INDEX `tevent` (`cevent`), CONSTRAINT `tevent` FOREIGN KEY (`cevent`) REFERENCES `tevent` (`ekey`), CONSTRAINT `tstock` FOREIGN KEY (`cstock`) REFERENCES `tstock` (`skey`) ) COLLATE='utf8_general_ci' ENGINE=InnoDB;");
        } catch (SQLException e) {
            e.printStackTrace();
        }
        commit(con);
        close(stat);
        close(con);

    }
}
