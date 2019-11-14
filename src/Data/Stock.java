package Data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Stock {
    String key, name;
    int price;

    public Stock(String key, String name, int price) {
        this.key = key;
        this.name = name;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("(%s) %s : %d won", key, name, price);
    }
}