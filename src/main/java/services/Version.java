package services;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Version {
    public static String versionString() {
        try {
            SimpleDateFormat df = (SimpleDateFormat) SimpleDateFormat.getInstance();
            df.applyPattern("yyyyMMdd");

            String revision =
                    df.format(new Date(new File(Version.class
                            .getClassLoader()
                            .getResource(Version.class.getCanonicalName().replace('.', '/') + ".class")
                            .toURI()).lastModified()));
            return Constant.versionString + "." + revision;

        } catch (Exception e) {
        }
        return Constant.versionString;
    }
}
