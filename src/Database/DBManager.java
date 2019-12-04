package Database;

import Data.Change;
import Data.Event;
import Data.EventList;
import Data.Stock;

import java.sql.*;
import java.util.ArrayList;

public class DBManager {
    public static byte STATUS_NORMAL = 0, STATUS_CANCELED = 1, TYPE_SELL = 1, TYPE_DELIVERY = 2;


    /**
     * 로그 출력 여부
     */
    public boolean isNoisy() {
        return noisy;
    }

    /**
     * 로그 출력 여부 설정
     *
     * @param noisy 여부
     */
    public void setNoisy(boolean noisy) {
        this.noisy = noisy;
    }

    private boolean noisy = false;
    private String server = "localhost"; // MySQL 서버 주소
    private String database = "AndroidPOS"; // MySQL DATABASE 이름
    private String user_name = "root"; //  MySQL 서버 아이디
    private String password = "root"; // MySQL 서버 비밀번호
    ConnectionBuilder builder;

    public DBManager() {
        initialize();
    }

    /**
     * 일반적인 생성자
     *
     * @param IP IP번호
     * @param DB DB이름
     * @param ID 유저ID
     * @param PW 비밀번호
     */
    public DBManager(String IP, String DB, String ID, String PW) {
        server = IP; // MySQL 서버 주소
        database = DB; // MySQL DATABASE 이름
        user_name = ID; //  MySQL 서버 아이디
        password = PW; // MySQL 서버 비밀번호
        initialize();
    }

    /**
     * 품목 변경
     *
     * @param stock 해당 품목 데이터
     * @return 성공 여부
     */
    public boolean editStock(Stock stock) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("INSERT INTO tstock VALUES ('%s', '%s', %d) ON DUPLICATE KEY UPDATE sname='%s',sprice=%d;", stock.getKey(), stock.getName(), stock.getPrice(), stock.getName(), stock.getPrice());
        ResultSet ret = null;
        boolean result = false;
        try {
            ret = stat.executeQuery(msg);
            result = true;
        } catch (SQLException e) {
            result = false;
            e.printStackTrace();
        } finally {
            commit(con);
            close(stat);
            close(con);
            return result;
        }
    }

    /**
     * 품목 리스트 요청
     *
     * @param key 키값
     * @return 품목 데이터
     */
    public Stock getStock(String key) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        ResultSet ret = null;
        Stock result = null;
        String msg = "SELECT * FROM tstock where skey=%s";
        try {
            ret = stat.executeQuery(String.format(msg, key));
            ret.first();
            int index = 0;
            result = new Stock(ret.getString("skey"), ret.getString("sname"), ret.getInt("sprice"));
        } catch (SQLException e) {
            e.printStackTrace();
            return null;
        }
        commit(con);
        close(stat);
        close(con);
        return result;
    }

    /**
     * 거래기록 추가
     *
     * @param event 추가할려는 기록(id 없이 요청)
     * @return 추가하여 얻은 키
     */
    public long addEvent(Event event) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("INSERT INTO tevent (etype,etime,estatus,ememo) VALUES (%d, '%s', %d,'%s');", event.getType(), event.getTime().toString(), event.getStatus(), event.getMemo());
        ResultSet ret = null;
        try {
            stat.executeQuery(msg);
            ret = stat.executeQuery("SELECT LAST_INSERT_ID();");
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        long retKey = 0;
        try {
            ret.first();
            retKey = ret.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }

        commit(con);
        close(stat);
        close(con);
        event.setKey(retKey);
        return retKey;
    }

    /**
     * 변동 개별기록 추가
     *
     * @param change 개별기록
     * @return 추가된 키
     */
    public long addChange(Change change) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        String msg = String.format("INSERT INTO tchange (cevent,cstock,cnumber) VALUES (%d,'%s',%d);", change.getEventKey(), change.getStockKey(), change.getAmount());
        long retKey = 0;
        try {
            stat.executeQuery(msg);
            ResultSet ret = stat.executeQuery("SELECT LAST_INSERT_ID();");
            ret.first();
            retKey = ret.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
            return -1;
        }
        change.setKey(retKey);
        close(stat);
        close(con);
        return retKey;
    }

    /**
     * 품목 리스트 요청
     *
     * @return 품목 리스트
     */
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

    /**
     * 거래기록 요청
     *
     * @param key 키값
     * @return 거래기록
     */
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
            return null;
        }
        commit(con);
        close(stat);
        close(con);
        return result;
    }

    /**
     * 거래기록 상태 변경 시도
     *
     * @param key    기록 키
     * @param status 변경할려는 상태
     * @return 성공 여부
     */
    public boolean tryChangeEvent(long key, byte status) {
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

    /**
     * 거래 기록 리스트 요청
     *
     * @param type 거래 기록 타입
     * @return 해당 타입으로 된 간략한 기록
     */
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

    /**
     * 매출 요청
     *
     * @param start 시작날짜
     * @param end   끝날짜
     * @return 물건당 판매 갯수가 들어있는 리스트
     */
    public Stock[] getSelling(Timestamp start, Timestamp end) {
        return getSelling(start, end, TYPE_SELL);
    }

    public Stock[] getSelling(Timestamp start, Timestamp end, byte type) {
        Connection con = getConnection();
        Statement stat = getStatement(con);
        Stock[] result = null;
        String msg = String.format("SELECT skey,sname, SUM(cnumber) as totNum,sprice \n" +
            "FROM ((tevent JOIN tchange ON tevent.ekey=tchange.cevent)JOIN tstock ON tchange.cstock=tstock.skey) \n" +
            "WHERE tevent.etime BETWEEN TIMESTAMP('%s') AND  TIMESTAMP('%s')\n" +
            "GROUP BY skey;", start.toString(), end.toString());
        try {
            ResultSet ret = stat.executeQuery(msg);
            log(msg);
            ret.last();
            result = new Stock[ret.getRow()];
            ret.first();
            int index = 0;
            do {
                Stock t = new Stock(ret.getString("skey"), ret.getString("sname"), ret.getInt("sprice"));
                t.setAmount(ret.getInt("totNum"));
                result[index] = t;
                index++;
            } while (ret.next());
        } catch (SQLException e) {
            e.printStackTrace();
        }
        close(stat);
        close(con);
        return result;
    }

    //private methods

    /**
     * Noisy여부에 따른 로그 출력
     *
     * @param msg 메세지
     */
    private void log(String msg) {
        if (noisy) System.out.println(msg);
    }

    private Connection getConnection() {
        return getConnection("jdbc:mysql://" + server + "/" + database + "?useSSL=false");
    }

    /**
     * 연결 생성
     *
     * @return 연결
     */
    private Connection getConnection(String url) {
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
            con = DriverManager.getConnection(url, user_name, password);
            log("Connected");
        } catch (SQLException e) {
            log("con 오류:" + e.getMessage());
            e.printStackTrace();
        }
        return con;
    }

    /**
     * 구문 생성
     *
     * @param con 연결
     * @return 구문
     */
    private Statement getStatement(Connection con) {
        try {
            return con.createStatement();
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 커밋 수행
     *
     * @param con 연결
     */
    private void commit(Connection con) {
        try {
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * 구문 닫기
     *
     * @param stat 구문
     */
    private void close(Statement stat) {
        // 3.해제
        try {
            if (stat != null)
                stat.close();
        } catch (SQLException e) {
        }
    }

    /**
     * 연결 닫기
     *
     * @param con 연결
     */
    private void close(Connection con) {
        // 3.해제
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {
        }
    }

    /**
     * 초기화 : 테이블이 없으면 새로 정의
     */
    private void initialize() {

        Connection con = getConnection("jdbc:mysql://" + server + "/?useSSL=false");
        Statement stat = getStatement(con);
        try {
            stat.executeQuery("CREATE DATABASE IF NOT EXISTS androidPos;");
            stat.close();
            con.commit();
            con.close();
            con = getConnection();
            stat = getStatement(con);

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
