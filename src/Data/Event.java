package Data;

import java.sql.Time;
import java.util.ArrayList;

public class Event {
    long key;
    byte type, status;
    Time time;
    ArrayList<Change> data;
    String memo;

    public Event(long key, byte type, byte status, Time time, ArrayList<Change> data, String memo) {
        this.key = key;
        this.type = type;
        this.status = status;
        this.time = time;
        this.data = data;
        this.memo = memo;
    }

    public String byteToType(byte type) {
        switch (type) {
            case 1:
                return "SELL";
            case 2:
                return "DELIVERY";
            default:
                return "NaN";
        }
    }

    public String byteToStatus(byte stat) {
        switch (stat) {
            case 0:
                return "";
            case 1:
                return "Canceled";
            default:
                return "NaN";
        }
    }

    @Override
    public String toString() {
        return String.format("(%d) %s - %s %s (%s)", key,byteToType(type),time.toString(),memo,byteToStatus(status) );
    }

}
