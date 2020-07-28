package services;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateParseService {
    public static Date parse(String string){
        Date date = null;
        try {
            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            format.setTimeZone(TimeZone.getTimeZone("UTC"));
            date = format.parse(string);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return date;
    }
}
