package services.date;

import java.text.SimpleDateFormat; // FIXME: DateFormatter
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

    public static String dateToJapaneseString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());

        String dateString = simpleDateFormat.format(date);

        return dateString;
    }

    public static String dateToGraphString(Date date){
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd HH:00");
        simpleDateFormat.setTimeZone(TimeZone.getDefault());

        String dateString = simpleDateFormat.format(date);

        return dateString;
    }

}
