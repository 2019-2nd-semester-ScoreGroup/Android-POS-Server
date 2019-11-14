package Data;

import java.sql.Time;
import java.util.ArrayList;

public class Event {
    private long key;
    private byte type, status;
    private Time time;
    private ArrayList<Change> data;
    private String memo;

    public Event(long key, byte type, byte status, Time time, ArrayList<Change> data, String memo) {
        this.key = key;
        this.type = type;
        this.status = status;
        this.time = time;
        this.data = data;
        this.memo = memo;
    }

    private String byteToType(byte type) {
        switch (type) {
            case 1:
                return "SELL";
            case 2:
                return "DELIVERY";
            default:
                return "NaN";
        }
    }

    private String byteToStatus(byte stat) {
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

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public byte getType() {
        return type;
    }

    public void setType(byte type) {
        this.type = type;
    }

    public byte getStatus() {
        return status;
    }

    public void setStatus(byte status) {
        this.status = status;
    }

    public Time getTime() {
        return time;
    }

    public void setTime(Time time) {
        this.time = time;
    }

    public ArrayList<Change> getData() {
        return data;
    }

    public void setData(ArrayList<Change> data) {
        this.data = data;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
