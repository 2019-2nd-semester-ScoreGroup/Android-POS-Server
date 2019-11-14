package Data;

public class Change {
     String stockKey;
     int amount;
     long key,eventKey;

    public Change(long key, String stockKey, int amount, long eventKey) {
        this.key = key;
        this.stockKey = stockKey;
        this.amount = amount;
        this.eventKey = eventKey;
    }

    @Override
    public String toString(){
        return String.format("(%d) %s X %d in %d",key,stockKey,amount,eventKey);
    }
}
