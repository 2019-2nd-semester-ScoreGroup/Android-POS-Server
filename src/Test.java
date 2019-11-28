import java.sql.Timestamp;
import java.time.LocalDateTime;

public class Test {


    public static void main(String[] args) {
        int amount;
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        System.out.println(ts);
        System.out.println(ts.toString());

    }
}

