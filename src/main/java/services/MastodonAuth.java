package services;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import javafx.scene.control.TextInputDialog;
import utils.BrowserLauncher;

import java.util.Optional;

import static utils.http.WebRequest.requestPOST;

public class MastodonAuth {
    final String MASTODON_HOST;
    final String CLIENT_NAME = "test_client";
    final String RegisterURL;
    final String TokenURL;

    public MastodonAuth(String MASTODON_HOST){
        this.MASTODON_HOST = MASTODON_HOST;
        RegisterURL = MASTODON_HOST + "/api/v1/apps";
        TokenURL = MASTODON_HOST + "/oauth/token";
    }

    public String getAuthorizeURL(String host, String clientId){
        try {
            return host
                    + "/oauth/authorize?"
                    + "client_id=" + clientId
                    + "&response_type=code"
                    + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob"
                    + "&scope=read%20write%20follow"
                    ;
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static class AccessTokenResponse {
        public String access_token;
        public String token_type;
        public String scope;
        public Integer created_at;
    }


    public AccessTokenResponse getAccessToken(String clientId, String clientSecret){

        BrowserLauncher.launch(getAuthorizeURL(MASTODON_HOST, clientId));

        TextInputDialog dialog = new TextInputDialog("コードを入れてください");
        Optional<String> msg = dialog.showAndWait();
        if(msg.isPresent()) {//データがあるか？
            System.out.println("入力文字列：" + msg.get());
        }
        else {
            return null;
        }

        // TODO: validation
        String authCode = msg.get();

        String parameterString = new String("client_id=" + clientId
                + "&client_secret=" + clientSecret
                + "&grant_type=authorization_code&code=" + authCode
                + "&redirect_uri=urn:ietf:wg:oauth:2.0:oob"
                + "&scopes=read write follow");
        String response = requestPOST(TokenURL, parameterString);
        System.out.println(response);

        try {
            ObjectMapper mapper = new ObjectMapper();
            AccessTokenResponse responseObj = mapper.readValue(response, new TypeReference<AccessTokenResponse>() {});
            return responseObj;

        }catch (Exception e){
            e.printStackTrace();
        }


        return null;
    }

    public static class ClientRegisterResponse {
        public String id;
        public String name;
        public String website;
        public String redirect_uri;
        public String client_id;
        public String client_secret;;
        public String vapid_key;
    }

    public ClientRegisterResponse registerClient() {
        String parameterString = new String("client_name=" + CLIENT_NAME + "&redirect_uris=urn:ietf:wg:oauth:2.0:oob&scopes=read write follow");
        String response = requestPOST(RegisterURL, parameterString);
        System.out.println(response);

        try {
            ObjectMapper mapper = new ObjectMapper();
            ClientRegisterResponse responseObj = mapper.readValue(response, new TypeReference<ClientRegisterResponse>() {});
            return responseObj;

        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

}
