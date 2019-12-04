import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.Date;

public class Test {
    public static void main(String[] args) throws ParseException {
        int amount;
        Timestamp ts = Timestamp.valueOf(LocalDateTime.now());
        System.out.println(ts);
        String time = "2019-10-12 00:00:00";

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        Date parsedDate = dateFormat.parse(time);
        System.out.println(parsedDate);

        Timestamp timestamp = new java.sql.Timestamp(parsedDate.getTime());
        System.out.println(timestamp);


    }
}

