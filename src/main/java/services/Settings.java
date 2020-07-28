package services;

import java.io.*;
import java.util.*;

public class Settings {

    final String settingsFileName = "settings.txt";
    final int MAX_INSTANCES = 2;
    List<InstanceSetting> instanceSettings;

    public Settings() {
        instanceSettings = new ArrayList<InstanceSetting>();
    }

    public static class InstanceSetting {
        public String hostName;
        public String clientId;
        public String clientSecret;
        public String accessToken;

        public InstanceSetting(String hostName, String clientId, String clientSecret, String accessToken) {
            this.hostName = hostName;
            this.clientId = clientId;
            this.clientSecret = clientSecret;
            this.accessToken = accessToken;
        }

    }

    public void addInstance(InstanceSetting instanceSetting) {
        if (instanceSettings.size() < MAX_INSTANCES) {
            instanceSettings.add(instanceSetting);
        }
    }

    public List<InstanceSetting> getInstanceSettings() {
        if (instanceSettings.size() >= 1) {
            return instanceSettings;
        } else {
            return null;
        }
    }

    public void save() {
        try (FileOutputStream f = new FileOutputStream(settingsFileName);
             BufferedOutputStream b = new BufferedOutputStream(f)) {

            Properties prop = new Properties();
            prop.setProperty("debug", "on");

            for (int i = 0; i < MAX_INSTANCES; ++i) {
                if (i < instanceSettings.size()) {
                    prop.setProperty(String.format("instance[%d].hostName", i), instanceSettings.get(i).hostName);
                    prop.setProperty(String.format("instance[%d].clientId", i), instanceSettings.get(i).clientId);
                    prop.setProperty(String.format("instance[%d].clientSecret", i), instanceSettings.get(i).clientSecret);
                    prop.setProperty(String.format("instance[%d].accessToken", i), instanceSettings.get(i).accessToken);
                }
            }

            prop.store(b, "test setting");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    ;

    public void load() {
        try (FileInputStream fis = new FileInputStream(settingsFileName);
             BufferedInputStream bis = new BufferedInputStream(fis)) {

            Properties prop = new Properties();
            prop.load(bis);

            if ("on".equals(prop.getProperty("debug"))) {
                System.out.println("デバッグモード有効");
            }

            for (int i = 0; i < MAX_INSTANCES; ++i) {
                //if(i < instanceSettings.size()){
                String hostName = prop.getProperty(String.format("instance[%d].hostName", i));
                String clientId = prop.getProperty(String.format("instance[%d].clientId", i));
                String clientSecret = prop.getProperty(String.format("instance[%d].clientSecret", i));
                String accessToken = prop.getProperty(String.format("instance[%d].accessToken", i));

                if (hostName != null && clientId != null && clientSecret != null && accessToken != null) {
                    instanceSettings.add(i, new InstanceSetting(hostName, clientId, clientSecret, accessToken));
                } else {
                    break;
                }
                //}
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
