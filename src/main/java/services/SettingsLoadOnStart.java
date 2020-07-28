package services;

import javafx.scene.control.TextInputDialog;
import services.MastodonAuth;
import services.Settings;

import java.util.List;
import java.util.Optional;

public class SettingsLoadOnStart {
    private Settings settings;

    public SettingsLoadOnStart(Settings settings){
        this.settings = settings;
    }

    public void startSequence(){ // TODO: 多インスタンス対応
        settings.load();
        List<Settings.InstanceSetting> instanceSetting = settings.getInstanceSettings();
        if( instanceSetting != null ){

        }
        else {
            boolean settingsEnd = false;
            while(!settingsEnd) {
                TextInputDialog dialog = new TextInputDialog("MastodonサーバーのURLを入れてください(https://は不要 末尾の/も不要)");
                Optional<String> msg = dialog.showAndWait();
                if (msg.isPresent()) {// データがあるか？
                    // TODO: validation
                    System.out.println("https：//" + msg.get());
                    String MASTDON_HOST = "https://" + msg.get();
                    MastodonAuth mastodonAuth = new MastodonAuth(MASTDON_HOST);
                    MastodonAuth.ClientRegisterResponse clientResponse = mastodonAuth.registerClient();
                    MastodonAuth.AccessTokenResponse accessTokenResponse = mastodonAuth.getAccessToken(clientResponse.client_id, clientResponse.client_secret);
                    System.out.println(accessTokenResponse.access_token);
                    if (clientResponse == null && accessTokenResponse == null) {

                    } else {
                        settings.addInstance(new Settings.InstanceSetting(MASTDON_HOST, clientResponse.client_id, clientResponse.client_secret, accessTokenResponse.access_token));
                        settings.save();
                        settingsEnd = true;
                    }
                } else {
                }
            }
        }

        if (false){ // For Developper:　token定数強制書き込み
            // settings.addInstance(new Settings.InstanceSetting(Akan.MASTODON_HOST, Akan.CLIENT_ID, Akan.CLIENT_SECRET, Akan.TOKEN));
            // settings.save();
        }
    }
}
