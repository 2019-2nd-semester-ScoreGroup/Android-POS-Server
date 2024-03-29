package Data;

public class Change {
     String stockKey;
     int amount;
     long key,eventKey;

    public Change(String stockKey, int amount) {
        this.stockKey = stockKey;
        this.amount = amount;
    }

    @Override
    public String toString(){
        return String.format("%d_%s_%d_%d",key,stockKey,amount,eventKey);
    }

    public String getStockKey() {
        return stockKey;
    }

    public void setStockKey(String stockKey) {
        this.stockKey = stockKey;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }

    public long getKey() {
        return key;
    }

    public void setKey(long key) {
        this.key = key;
    }

    public long getEventKey() {
        return eventKey;
    }

    public void setEventKey(long eventKey) {
        this.eventKey = eventKey;
    }
}
