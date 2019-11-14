package Data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Stock {
    String key, name;
    int price;

    public static Stock BuildStock(ResultSet set) {

        try {
            Stock ret = new Stock();
            set.first();
            ret.key = set.getString(1);
            ret.name = set.getString(2);
            ret.price = set.getInt(3);
            return ret;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("%s_%s : %d won", key, name, price);
    }
}