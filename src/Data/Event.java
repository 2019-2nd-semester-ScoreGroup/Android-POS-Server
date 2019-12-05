package Data;

import java.sql.Timestamp;
import java.util.ArrayList;

public class Event {

    private long key;
    private byte type, status;
    private Timestamp time;
    private ArrayList<Change> data;
    private String memo;

    public Event(byte type, Timestamp time, String memo) {
        this.type = type;
        this.time = time;
        this.memo = memo;
        data = new ArrayList<>();
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
                return "Normal";
            case 1:
                return "Canceled";
            default:
                return "NaN";
        }
    }

    @Override
    public String toString() {
        return String.format("%d %s %s %s %s", key, byteToType(type), time.toString(), memo, byteToStatus(status));

    }

    public String toString(int recipe)
    {
        String ackMsg;
        ackMsg = String.format("%s %s %s ", byteToType(type), time.toString(), memo);

        for(Change c : data)
        {
            ackMsg = ackMsg + String.format("%s %d %d %d,", c.stockKey, c.amount, c.eventKey, c.key);
        }

        return ackMsg;
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

    public Timestamp getTime() {
        return time;
    }

    public void setTime(Timestamp time) {
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
