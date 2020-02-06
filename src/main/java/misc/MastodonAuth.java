package misc;

import javafx.scene.control.TextInputDialog;

import java.util.Optional;

import static connection.WebRequest.requestPOST;

public class MastodonAuth {
    final String CLIENT_NAME = "test_client";
    final String RegisterURL = Akan.MASTODON_HOST + "/api/v1/apps";
    final String TokenURL = Akan.MASTODON_HOST + "/oauth/token";

    public String getAuthorizeURL(String host){
        try {
            return host
                    + "/oauth/authorize?"
                    + "client_id=" + Akan.CLIENT_ID
                    + "&response_type=code"
                    + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob"
                    + "&scope=read"
                    ;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public String getAccessToken(){

        BrowserLauncher.launch(getAuthorizeURL(Akan.MASTODON_HOST));

        TextInputDialog dialog = new TextInputDialog("コードを入れてください");
        Optional<String> msg = dialog.showAndWait();
        if(msg.isPresent()) {//データがあるか？
            System.out.println("入力文字列：" + msg.get());
        }
        else {
            return "";
        }

        // TODO: validation
        String authCode = msg.get();

        String parameterString = new String("client_id=" + Akan.CLIENT_ID
                + "&client_secret=" + Akan.CLIENT_SECRET
                + "&grant_type=authorization_code&code=" + authCode
                + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob");
        String response = requestPOST(TokenURL, parameterString);
        System.out.println(response); // TODO: ここでJSON解釈してtoken返す
        return response;
    }

    public void registerClient() {
        String parameterString = new String("client_name=" + CLIENT_NAME + "&redirect_uris=urn:ietf:wg:oauth:2.0:oob&scope=read write follow"); // TODO: "scopes=read write follow"
        String response = requestPOST(RegisterURL, parameterString);
        System.out.println(response);
    }

}
