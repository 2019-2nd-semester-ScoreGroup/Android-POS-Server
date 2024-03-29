package Data;

import java.sql.ResultSet;
import java.sql.SQLException;

public class Stock {
    String key;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    String name;
    int price;

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    int amount;

    public Stock(String key, String name, int price) {
        this.key = key;
        this.name = name;
        this.price = price;
    }

    @Override
    public String toString() {
        return String.format("%s_%s_%d_%d", key, name, price, amount);
    }

    public String toString(int noneAmount) { return String.format("%s_%s_%d", key, name, price); }
}