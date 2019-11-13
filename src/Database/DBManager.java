package Database;

import java.sql.*;
import java.sql.ResultSet;

public class DBManager {
    Connection con = null;
    PreparedStatement pstmt = null;
    ResultSet rs = null;
    boolean established = false;
    String server = "localhost"; // MySQL 서버 주소
    String database = "AndroidPOS"; // MySQL DATABASE 이름
    String user_name = "root"; //  MySQL 서버 아이디
    String password = "root"; // MySQL 서버 비밀번호

    public DBManager() {
        connect();
    }

    public DBManager(String IP, String DB, String ID, String PW) {
        server = IP; // MySQL 서버 주소
        database = DB; // MySQL DATABASE 이름
        user_name = ID; //  MySQL 서버 아이디
        password = PW; // MySQL 서버 비밀번호
        connect();
        initialize();
    }

    public ResultSet insertStock(String key, String name, int price) {
        String msg = String.format("INSERT INTO stock VALUES ('%s', '%s', %d);", key, name, price);
        try {
            return executeQuery(msg);
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            return null;
        }
    }

    private void connect() {
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
            System.out.println("정상적으로 연결되었습니다.");
            established = true;
        } catch (SQLException e) {
            System.err.println("con 오류:" + e.getMessage());
            e.printStackTrace();
        }
    }


    private ResultSet executeQuery(String query) throws SQLException {
        if (!established) {
            System.err.println("Not connected");
            return null;
        }
        return con.createStatement().executeQuery(query);
    }

    public void Close() {
        // 3.해제
        try {
            if (con != null)
                con.close();
        } catch (SQLException e) {
        }
    }

    public void initialize() {
        if (!established) return;
        try {
            executeQuery("CREATE TABLE IF NOT EXISTS `stock` (`skey` VARCHAR(25) NOT NULL,`sname` VARCHAR(50) NULL DEFAULT NULL, `sprice` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`skey`)) COLLATE='utf8_general_ci' ENGINE=InnoDB;");
            executeQuery("CREATE TABLE IF NOT EXISTS `event` ( `ekey` BIGINT(20) NOT NULL AUTO_INCREMENT, `etype` TINYINT(4) NOT NULL DEFAULT 0, `etime` DATETIME NOT NULL, `estatus` TINYINT(4) NOT NULL DEFAULT 0, `ememo` VARCHAR(200) NULL DEFAULT NULL, PRIMARY KEY (`ekey`))COLLATE='utf8_general_ci'ENGINE=InnoDB;");
            executeQuery("CREATE TABLE IF NOT EXISTS `change` ( `ckey` BIGINT(20) NOT NULL AUTO_INCREMENT, `cevent` BIGINT(20) NULL DEFAULT NULL, `cstock` VARCHAR(25) NULL DEFAULT NULL, `cnumber` INT(11) NULL DEFAULT NULL, PRIMARY KEY (`ckey`), INDEX `stock` (`cstock`), INDEX `event` (`cevent`), CONSTRAINT `event` FOREIGN KEY (`cevent`) REFERENCES `event` (`ekey`), CONSTRAINT `stock` FOREIGN KEY (`cstock`) REFERENCES `stock` (`skey`) ) COLLATE='utf8_general_ci' ENGINE=InnoDB;");
            con.commit();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
