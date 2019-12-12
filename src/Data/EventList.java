package Data;

import java.sql.Timestamp;

public class EventList {
    long key;
    Timestamp time;

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
        this.time = time;
    }

    public int getTotalPrice() {
        return totalPrice;
    }

    public void setTotalPrice(int totalPrice) {
        this.totalPrice = totalPrice;
    }

    int totalPrice;
    @Override
    public String toString(){
        return String.format("%d_%s_%d", key,time.toString(),totalPrice);
    }
}
